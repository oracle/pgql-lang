/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.SelectQuery;

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
}
