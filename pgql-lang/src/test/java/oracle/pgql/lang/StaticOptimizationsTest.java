/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.DerivedTable;
import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.PathFindingGoal;
import oracle.pgql.lang.ir.PathMode;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.And;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.TableExpression;
import oracle.pgql.lang.ir.TableExpressionType;
import oracle.pgql.lang.ir.VertexPairConnection;

public class StaticOptimizationsTest extends AbstractPgqlTest {

  /*
   * Test that although the ORDER BY has 4 expressions, they are reduced to only 1 since all 4 expressions are the same
   */
  @Test
  public void testDuplicateOrderByElems() throws Exception {
    String query = "SELECT n.age AS nAge MATCH (n) ORDER BY n.age, n.age, nAge, nAge";
    int numOrderByExpressions = pgql.parse(query).getGraphQuery().getOrderBy().getElements().size();
    assertEquals(1, numOrderByExpressions);
  }

  /*
   * Test that an IN expression with numeric literals is not translated into OR expressions
   */
  @Test
  public void testInPredicate() throws Exception {
    String query = "SELECT n.age IN ( 1, -1, 2.0, -10.0 ) " //
        + "              , n.time IN ( TIME '10:15:00', TIME '15:30:00+01:00' ) " //
        + "              , n.timestamp IN ( TIMESTAMP '2000-01-01 10:15:00', TIMESTAMP '2000-01-01 15:30:00+01:00' ) " //
        + "         FROM MATCH (n)";
    List<ExpAsVar> projectionElements = ((SelectQuery) pgql.parse(query).getGraphQuery()).getProjection().getElements();

    QueryExpression numericExpression = projectionElements.get(0).getExp();
    assertEquals(ExpressionType.IN_EXPRESSION, numericExpression.getExpType());
    QueryExpression timeExpression = projectionElements.get(1).getExp();
    assertEquals(ExpressionType.IN_EXPRESSION, timeExpression.getExpType());
    QueryExpression timestampExpression = projectionElements.get(2).getExp();
    assertEquals(ExpressionType.IN_EXPRESSION, timestampExpression.getExpType());
  }

  /*
   * Test that unary minus gets normalized away when possible
   */
  @Test
  public void testNormalizeUnaryMinus() throws Exception {
    String query = "SELECT -1, -0.1 FROM MATCH (n)";
    List<ExpAsVar> projectionElements = ((SelectQuery) pgql.parse(query).getGraphQuery()).getProjection().getElements();

    QueryExpression integerExpression = projectionElements.get(0).getExp();
    assertEquals(ExpressionType.INTEGER, integerExpression.getExpType());
    QueryExpression decimalExpression = projectionElements.get(1).getExp();
    assertEquals(ExpressionType.DECIMAL, decimalExpression.getExpType());
  }

  @Test
  public void testReferencesToSelectExpression1() throws Exception {
    String query = "SELECT n.age * 2 AS doubleAge "//
        + "    FROM MATCH (n) ON g "//
        + "   WHERE doubleAge = n.age + n.age "//
        + "GROUP BY doubleAge "//
        + "  HAVING doubleAge = n.age * 2 "//
        + "ORDER BY 2 * doubleAge ASC, 2 * (n.age * 2) DESC";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testReferencesToSelectExpression2() throws Exception {
    String query = "SELECT n.age * 2 AS doubleAge "//
        + "    FROM MATCH (n) ON g "//
        + "   WHERE doubleAge = n.age + n.age "//
        + "GROUP BY n.age * 2 "//
        + "  HAVING doubleAge = n.age * 2 "//
        + "ORDER BY 2* doubleAge ASC, 2 * (n.age * 2) DESC";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testImplicitPropertyReferences1() throws Exception {
    String query = "SELECT n.prop"//
        + "           FROM MATCH (n) ON g"//
        + "       ORDER BY prop";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testImplicitPropertyReferences2() throws Exception {
    String query = "SELECT n.prop"//
        + "           FROM MATCH (n) ON g"//
        + "          WHERE prop = 3"//
        + "       GROUP BY prop"//
        + "         HAVING prop > 10";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testPredicatePushdownForExistsQuery1() throws Exception {
    String query = "SELECT id(n) FROM MATCH (n) WHERE EXISTS (SELECT * FROM MATCH (n) -> (m) WHERE m.age > n.age)";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    assertEquals(1L, selectQuery.getGraphPattern().getConstraints().size());
  }

  @Test
  public void testPredicatePushdownForExistsQuery2() throws Exception {
    String query = "SELECT COUNT(*) "//
        + "FROM MATCH (n) -> (m) " //
        + "WHERE EXISTS ( SELECT * FROM MATCH (n) -> (o) WHERE id(m) <> id(o) )";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    Set<QueryExpression> constraints = selectQuery.getGraphPattern().getConstraints();
    assertEquals(1L, constraints.size());
    Exists exists = (Exists) constraints.iterator().next();
    assertEquals(1L, exists.getQuery().getGraphPattern().getConstraints().size());
  }

  @Test
  public void testPredicatePushdownForExistsQuery3() throws Exception {
    String query = "SELECT n.age FROM MATCH (n) " //
        + "WHERE EXISTS ( SELECT m.age FROM MATCH (n)->(m) GROUP BY m.age, n.name ) ORDER BY n.age";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    assertEquals(1L, selectQuery.getGraphPattern().getConstraints().size());
  }

  @Test
  public void testPredicatePushdownForExistsQuery4() throws Exception {
    String query = "PATH p AS (a) -> (b) WHERE EXISTS ( SELECT x FROM MATCH (x) WHERE x.age > b.age ) " //
        + "SELECT id(n), id(m) " //
        + "FROM MATCH (n) -/:p/-> (m)";
    CommonPathExpression commonPathExpression = pgql.parse(query).getGraphQuery().getCommonPathExpressions().get(0);
    assertEquals(1L, commonPathExpression.getConstraints().size());
  }

  @Test
  public void testPredicatePushdownAfterGroupBy() throws Exception {
    String query = "SELECT m.age, ( " + //
        "  SELECT COUNT(*) " + //
        "  FROM MATCH (n) " + //
        "  WHERE m.age = 24 OR m.age = 28 ) " + //
        "FROM MATCH (m) " + //
        "GROUP BY m.age";
    ExpAsVar expAsVar = ((SelectQuery) pgql.parse(query).getGraphQuery()).getProjection().getElements().get(1);
    ScalarSubquery scalarSubquery = (ScalarSubquery) expAsVar.getExp();
    Set<QueryExpression> constraints = scalarSubquery.getQuery().getGraphPattern().getConstraints();
    assertEquals(1L, constraints.size());
  }

  @Test
  public void testPredicatePushDownLateral() throws Exception {
    String query = "SELECT m.prop AS m_prop, n_prop " + //
        "FROM MATCH (m) " + //
        "   , LATERAL ( SELECT n.prop AS n_prop FROM MATCH (n) -> (m)  ) " + //
        "WHERE n_prop > 4 AND m_prop > 4 AND m.prop2 > 4";

    SelectQuery outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    GraphPattern graphPattern = (GraphPattern) outerQuery.getTableExpressions().get(0);
    assertEquals(2L, graphPattern.getConstraints().size());

    SelectQuery innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(1)).getQuery();
    Set<QueryExpression> constraintsInnerQuery = innerQuery.getGraphPattern().getConstraints();
    assertEquals(1L, constraintsInnerQuery.size());
  }

  @Test
  public void testPredicatePushDownToHavingInLateral() throws Exception {
    String query = "SELECT sum FROM LATERAL (SELECT v, sum(v2.integerprop) as sum " //
        + "FROM MATCH ANY SHORTEST (v) ->* (v2) GROUP BY (v)) WHERE sum > 100";
    SelectQuery outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    SelectQuery innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(0)).getQuery();
    assertEquals(ExpressionType.GREATER, innerQuery.getHaving().getExpType());

    // check if the pretty printed version is valid
    PgqlResult result = pgql.parse(outerQuery.toString());
    assertTrue(result.getErrorMessages(), result.isQueryValid());

    // do the same for a variant of the query
    query.replace("sum > 100", "id(v) > 5");
    outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(0)).getQuery();
    assertEquals(ExpressionType.GREATER, innerQuery.getHaving().getExpType());

    // check if the pretty printed version is valid
    result = pgql.parse(outerQuery.toString());
    assertTrue(result.getErrorMessages(), result.isQueryValid());

    // also test the case where there already exists a HAVING clause
    query = "SELECT sum FROM LATERAL (SELECT v, sum(v2.integerprop) as sum " //
        + "FROM MATCH ANY SHORTEST (v) ->* (v2) GROUP BY (v) HAVING COUNT(*) >= 0) WHERE sum > 100";
    outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(0)).getQuery();
    And and = (And) innerQuery.getHaving();
    assertEquals(ExpressionType.GREATER_EQUAL, and.getExp1().getExpType());
    assertEquals(ExpressionType.GREATER, and.getExp2().getExpType());
  }

  @Test
  public void testPredicatePushDownLateralWithLimitOffset1() throws Exception {
    String query = "SELECT * FROM LATERAL ( SELECT n.prop FROM MATCH (n) ORDER BY n.prop LIMIT 5 ) "
        + "WHERE prop < 10";

    SelectQuery outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    assertEquals(1L, outerQuery.getConstraints().size());

    SelectQuery innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(0)).getQuery();
    assertEquals(0L, innerQuery.getGraphPattern().getConstraints().size());

    // now try the same query but without LIMIT (this time the predicate will be pushed down)
    query = "SELECT * FROM LATERAL ( SELECT n.prop FROM MATCH (n) ORDER BY n.prop ) WHERE prop < 10";

    outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    assertEquals(0L, outerQuery.getConstraints().size());

    innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(0)).getQuery();
    assertEquals(1L, innerQuery.getGraphPattern().getConstraints().size());
  }

  @Test
  public void testPredicatePushDownLateralWithLimitOffset2() throws Exception {
    String query = "SELECT * FROM MATCH (m), LATERAL ( SELECT n.prop FROM MATCH (n) ORDER BY n.prop LIMIT 5 ), MATCH (o) "
        + "WHERE prop < 10 AND m.prop < 20 AND o.prop < 30";

    SelectQuery outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    assertEquals(0L, outerQuery.getConstraints().size());

    GraphPattern graphPattern1 = (GraphPattern) outerQuery.getTableExpressions().get(0);
    assertEquals(1L, graphPattern1.getConstraints().size());

    SelectQuery innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(1)).getQuery();
    assertEquals(0L, innerQuery.getGraphPattern().getConstraints().size());

    GraphPattern graphPattern2 = (GraphPattern) outerQuery.getTableExpressions().get(2);
    assertEquals(2L, graphPattern2.getConstraints().size());

    // now try the same query but without LIMIT
    query = "SELECT * FROM MATCH (m), LATERAL ( SELECT n.prop FROM MATCH (n) ORDER BY n.prop ), MATCH (o) "
        + "WHERE prop < 10 AND m.prop < 20 AND o.prop < 30";

    outerQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    assertEquals(0L, outerQuery.getConstraints().size());

    graphPattern1 = (GraphPattern) outerQuery.getTableExpressions().get(0);
    assertEquals(1L, graphPattern1.getConstraints().size());

    innerQuery = ((DerivedTable) outerQuery.getTableExpressions().get(1)).getQuery();
    assertEquals(1L, innerQuery.getGraphPattern().getConstraints().size());

    graphPattern2 = (GraphPattern) outerQuery.getTableExpressions().get(2);
    assertEquals(1L, graphPattern2.getConstraints().size());
  }

  @Test
  public void testSelectStarWithLateral() throws Exception {
    String query = "SELECT * " + //
        "FROM MATCH (n) " + //
        "   , LATERAL ( SELECT m, m.prop FROM MATCH (m) ) " + //
        "   , MATCH (o)";

    Projection projection = ((SelectQuery) pgql.parse(query).getGraphQuery()).getProjection();
    assertEquals(4L, projection.getElements().size());
  }

  @Test
  public void testPredicatePushDownEvenForCorrelatedVertices1() throws Exception {
    String query = "SELECT x2 AS x3 " + //
        "FROM LATERAL ( " + //
        "       SELECT x AS x2, y, z AS p " + //
        "       FROM MATCH (x) -> (y) -> (z) ) " + //
        "   , MATCH (y) -> (p) " + //
        "WHERE id(x3) > id(y) AND p.prop > 3";
    GraphQuery graphQuery = pgql.parse(query).getGraphQuery();

    DerivedTable lateralQuery = (DerivedTable) graphQuery.getTableExpressions().get(0);
    Iterator<QueryExpression> it = lateralQuery.getQuery().getGraphPattern().getConstraints().iterator();
    assertEquals("(id(x) > id(y))", it.next().toString());
    assertEquals("(z.prop > 3)", it.next().toString());

    GraphPattern graphPattern = (GraphPattern) graphQuery.getTableExpressions().get(1);
    assertTrue(graphPattern.getConstraints().isEmpty());
  }

  @Test
  public void testPredicatePushDownEvenForCorrelatedVertices2() throws Exception {
    String query = "SELECT x2 AS x3 " + //
        "FROM LATERAL ( " + //
        "       SELECT x AS x2, y2, z AS p " + //
        "       FROM MATCH (x) -> (y) -> (z) " + //
        "       GROUP BY x, y AS y2, z ) " + //
        "   , MATCH (y2) -> (p) " + //
        "WHERE id(x3) > id(y2) AND p.prop > 3";
    GraphQuery graphQuery = pgql.parse(query).getGraphQuery();

    DerivedTable lateralQuery = (DerivedTable) graphQuery.getTableExpressions().get(0);
    QueryExpression havingClause = lateralQuery.getQuery().getHaving();
    assertEquals("((id(x) > id(y2)) AND (z.prop > 3))", havingClause.toString());

    GraphPattern graphPattern = (GraphPattern) graphQuery.getTableExpressions().get(1);
    assertTrue(graphPattern.getConstraints().isEmpty());
  }

  @Test
  public void testReachesOptimization() throws Exception {
    // test that ANY translates to REACHES as long as vertex and edge variables along paths, other than the first and
    // last vertex variable, are not referenced and there is also no ONE ROW PER VERTEX/STEP specified

    GraphQuery graphQuery = pgql.parse("SELECT 1 FROM MATCH ANY () ->* ()").getGraphQuery();
    QueryPath path = (QueryPath) graphQuery.getGraphPattern().getConnections().iterator().next();
    assertEquals(PathFindingGoal.REACHES, path.getPathFindingGoal());

    graphQuery = pgql.parse("SELECT 1 FROM MATCH ANY () (-[e]-> (x) WHERE e.prop = 1 AND x.prop = 1)* ()")
        .getGraphQuery();
    path = (QueryPath) graphQuery.getGraphPattern().getConnections().iterator().next();
    assertEquals(PathFindingGoal.REACHES, path.getPathFindingGoal());

    graphQuery = pgql.parse("SELECT SUM(e.prop) FROM MATCH ANY () (-[e]-> (x))* ()").getGraphQuery();
    path = (QueryPath) graphQuery.getGraphPattern().getConnections().iterator().next();
    assertEquals(PathFindingGoal.SHORTEST, path.getPathFindingGoal());

    graphQuery = pgql.parse("SELECT 1 FROM MATCH ANY () ->* () ONE ROW PER STEP ( src, e, dst )").getGraphQuery();
    path = (QueryPath) graphQuery.getGraphPattern().getConnections().iterator().next();
    assertEquals(PathFindingGoal.SHORTEST, path.getPathFindingGoal());

    graphQuery = pgql.parse("SELECT 1 FROM MATCH ANY () ->* () ONE ROW PER VERTEX ( v )").getGraphQuery();
    path = (QueryPath) graphQuery.getGraphPattern().getConnections().iterator().next();
    assertEquals(PathFindingGoal.SHORTEST, path.getPathFindingGoal());
  }

  @Test
  public void testNormalizeHasLabel() throws Exception {
    // We want to make sure that has_label / "has_label" / "Has_Label" all end up being the same

    GraphQuery graphQuery = pgql.parse("SELECT 1 FROM MATCH (n:LBL1) " //
        + "WHERE has_label(n, 'LBL2') AND \"has_label\"(n, 'LBL3') AND \"Has_Label\"(n, 'LBL4')").getGraphQuery();
    Set<QueryExpression> constraints = graphQuery.getGraphPattern().getConstraints();
    assertEquals(4, constraints.size());

    for (QueryExpression constraint : constraints) {
      FunctionCall functionCall = (FunctionCall) constraint;
      assertEquals("has_label", functionCall.getFunctionName());
    }
  }

  @Test
  public void testNoMergingOfGraphTableOrLateral() throws Exception {
    checkThatNoMergingHappened("SELECT prop FROM LATERAL ( SELECT 1/0, n.prop FROM MATCH (n) )");
    checkThatNoMergingHappened("SELECT prop1 FROM LATERAL ( SELECT n.prop1, n.prop2 FROM MATCH (n) )");
    checkThatNoMergingHappened("SELECT * FROM LATERAL ( SELECT n.prop FROM MATCH (n) ORDER BY n.prop )");
    checkThatNoMergingHappened("SELECT * FROM LATERAL ( SELECT n.prop FROM MATCH (n) FETCH FIRST 10 ROWS ONLY )");
    checkThatNoMergingHappened("SELECT * FROM LATERAL ( SELECT n.prop FROM MATCH (n) OFFSET 10 )");
    checkThatNoMergingHappened("SELECT * FROM LATERAL ( SELECT AVG(n.prop) AS avg FROM MATCH (n) )");
    checkThatNoMergingHappened("SELECT * FROM LATERAL ( SELECT n.prop AS avg FROM MATCH (n) GROUP BY n.prop )");
    checkThatNoMergingHappened("SELECT * FROM LATERAL ( SELECT 1 FROM MATCH (n) HAVING COUNT(*) > 10 )");
  }

  private void checkThatNoMergingHappened(String query) throws Exception {
    GraphQuery graphQuery = pgql.parse(query).getGraphQuery();
    TableExpression tableExpression = graphQuery.getTableExpressions().get(0);
    assertEquals(TableExpressionType.DERIVED_TABLE, tableExpression.getTableExpressionType());
  }

  @Test
  public void testMergingOfGraphTableOrLateral() throws Exception {
    checkThatMergingHappened("SELECT * FROM LATERAL ( SELECT n.* FROM MATCH (n) )");
    checkThatMergingHappened("SELECT * FROM LATERAL ( SELECT 1/0, n.prop FROM MATCH (n) )");

    // SELECT references prop1 and prop2 while ORDER BY references prop3 and prop4
    checkThatMergingHappened("SELECT prop1, ( SELECT COUNT(*) FROM MATCH (x) WHERE x.prop = prop2 ) " //
        + "FROM LATERAL ( SELECT n.prop1, n.prop2, n.prop3, n.prop4 FROM MATCH (n) ) " //
        + "ORDER BY prop3 + CASE WHEN EXISTS ( SELECT * FROM MATCH (y) WHERE y.prop = prop4 ) THEN 5 ELSE 10 END");

    // GROUP BY references prop1 and prop2 while HAVING references prop3 and prop4
    checkThatMergingHappened("SELECT 123 " //
        + "FROM LATERAL ( SELECT n.prop1, n.prop2, n.prop3, n.prop4 FROM MATCH (n) ) " //
        + "GROUP BY prop1, ( SELECT COUNT(*) FROM MATCH (x) WHERE x.prop = prop2 )"
        + "HAVING 10 < AVG(prop3) + AVG(CASE WHEN EXISTS ( SELECT * FROM MATCH (y) WHERE y.prop = prop4 ) THEN 5 ELSE 10 END)");
  }

  private void checkThatMergingHappened(String query) throws Exception {
    GraphQuery graphQuery = pgql.parse(query).getGraphQuery();
    TableExpression tableExpression = graphQuery.getTableExpressions().get(0);
    assertEquals(TableExpressionType.GRAPH_PATTERN, tableExpression.getTableExpressionType());
  }

  @Test
  public void testMergeNestedGraphTableAndLateralIntoOuterQuery() throws Exception {
    GraphQuery graphQuery = pgql.parse("SELECT * " + //
        "FROM LATERAL ( SELECT x AS y " + //
        "  FROM LATERAL ( SELECT prop AS x FROM GRAPH_TABLE ( g MATCH (n) COLUMNS ( n.prop ) ) ) " + //
        ") " + //
        "WHERE y > 10").getGraphQuery();

    assertEquals(1, graphQuery.getTableExpressions().size());
    assertTrue(graphQuery.getConstraints().isEmpty()); // predicate is pushed down into the graph pattern

    TableExpression tableExpression = graphQuery.getTableExpressions().get(0);

    // outer query has only one table expression which is a graph pattern
    // all LATERAL subqueries (incl. GRAPH_TABLE operators) disappeared from the query
    assertEquals(TableExpressionType.GRAPH_PATTERN, tableExpression.getTableExpressionType());

    GraphPattern graphPattern = (GraphPattern) tableExpression;

    // "y > 10" got translated into "n.prop > 10"
    assertEquals("(n.prop > 10)", graphPattern.getConstraints().iterator().next().toString());

    ExpAsVar column = ((SelectQuery) graphQuery).getProjection().getElements().get(0);
    assertEquals("Y", column.getName()); // column name is still "Y"
    assertEquals("y", column.getNameOriginText());
  }

  @Test
  public void keepClause() throws Exception {
    GraphQuery graphQuery = pgql.parse("SELECT * " + //
        "FROM GRAPH_TABLE ( g " + //
        "       MATCH (n) ->* (m), (m) ->* (n) " + //
        "       KEEP ANY SHORTEST ACYCLIC " + //
        "       WHERE n.prop > m.prop " + //
        "       COLUMNS ( n.prop ) " + //
        "     )").getGraphQuery();
    Set<VertexPairConnection> connections = graphQuery.getGraphPattern().getConnections();
    assertEquals(2, connections.size());
    for (VertexPairConnection connection : connections) {
      QueryPath path = (QueryPath) connection;
      assertEquals(PathFindingGoal.SHORTEST, path.getPathFindingGoal());
      assertEquals(PathMode.ACYCLIC, path.getPathMode());
      assertEquals(1, path.getKValue());
    }

    graphQuery = pgql.parse("SELECT * " + //
        "FROM GRAPH_TABLE ( g " + //
        "       MATCH (n) ->* (m), (m) ->* (n) " + //
        "       KEEP CHEAPEST 10 TRAIL " + //
        "       WHERE n.prop > m.prop " + //
        "       COLUMNS ( n.prop ) " + //
        "     )").getGraphQuery();
    connections = graphQuery.getGraphPattern().getConnections();
    assertEquals(2, connections.size());
    for (VertexPairConnection connection : connections) {
      QueryPath path = (QueryPath) connection;
      assertEquals(PathFindingGoal.CHEAPEST, path.getPathFindingGoal());
      assertEquals(PathMode.TRAIL, path.getPathMode());
      assertEquals(10, path.getKValue());
    }
  }
}
