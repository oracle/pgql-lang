/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import oracle.pgql.lang.ddl.propertygraph.CreateSuperPropertyGraph;
import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.PathFindingGoal;
import oracle.pgql.lang.ir.QueryExpression.AllProperties;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrJsonArrayagg;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.VertexPairConnection;

public class BugFixTest extends AbstractPgqlTest {

  /* OL-Jira GM-13537 */
  @Test
  public void testDuplicateOrderByElems() throws Exception {
    String query = "SELECT NOTfunc() MATCH (n)";

    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    FunctionCall funcCall = (FunctionCall) selectQuery.getProjection().getElements().get(0).getExp();

    assertEquals("NOTfunc", funcCall.getFunctionName());
  }

  @Test
  public void testArrayAgg() throws Exception {
    String query = "SELECT ARRAY_AGG(e.weight) MATCH TOP 10 SHORTEST ( (n) -[e]->* (m) )";

    assertTrue(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testTopKTooLarge() throws Exception {
    PgqlResult result = pgql.parse("SELECT COUNT(*) MATCH TOP 10234234234234 SHORTEST ( (a) ->* (b) )");
    assertEquals("10234234234234 is too large to be stored as int", result.getErrorMessages());
  }

  @Test
  public void noErrorOnSelectStarSingleVertex() throws Exception {
    String query = "SELECT * MATCH (n)";
    assertTrue(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testUpdate() throws Exception {
    String query = "UPDATE n SET ( [[n.prop]] = 123 ) " + //
        "FROM MATCH (n) " + //
        "GROUP BY n.prop AS nProp";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void orderByEdgeIsPermittedOnlyInPgql10() throws Exception {
    String queryPgql10 = "SELECT 1 WHERE () -[e]-> () ORDER BY e";
    assertTrue(pgql.parse(queryPgql10).isQueryValid());

    String queryPgql11 = "SELECT 1 MATCH () -[e]-> () ORDER BY e";
    assertFalse(pgql.parse(queryPgql11).isQueryValid());

    String queryPgql13 = "SELECT 1 FROM MATCH () -[e]-> () ORDER BY e";
    assertFalse(pgql.parse(queryPgql13).isQueryValid());
  }

  @Test /* GM-16567 */
  public void selectStarOrderBy() throws Exception {
    String query = "SELECT * MATCH (v)-[e]->(u) ORDER BY v.name";
    String prettyPrintedQuery = pgql.parse(query).getGraphQuery().toString();
    GraphQuery graphQuery = pgql.parse(prettyPrintedQuery).getGraphQuery();
    PropertyAccess propertyAccess = (PropertyAccess) graphQuery.getOrderBy().getElements().get(0).getExp();
    assertEquals("v", propertyAccess.getVariable().getName());
  }

  @Test
  public void multiplePgqlInstances() throws Exception {
    String dummyQuery = "SELECT * FROM g MATCH (n)";
    Pgql pgql1 = new Pgql();
    Pgql pgql2 = new Pgql();

    assertTrue(pgql1.parse(dummyQuery).isQueryValid());

    pgql1.close();
    thrown.expectMessage("Pgql instance was closed");
    pgql1.parse(dummyQuery);

    // even though pgql1 was close, we can still use pgql2
    assertTrue(pgql2.parse(dummyQuery).isQueryValid());

    pgql2.close();
    thrown.expectMessage("Pgql instance was closed");
    pgql2.parse(dummyQuery);

    pgql1.close(); // closing things twice is fine
    pgql2.close(); // closing things twice if fine
  }

  @Test
  public void escapingOfIdentifiers() throws Exception {
    String query = "SELECT n.\"123_property\" FROM \"456_graph\" MATCH (n:\"789_label1\"|\"_label2\")";
    String prettyPrintedQuery = pgql.parse(query).getGraphQuery().toString();
    assertTrue(pgql.parse(prettyPrintedQuery).isQueryValid());
  }

  @Test /* GM-16464 */
  public void duplicateVertexInPathMacro() throws Exception {
    String query = "PATH self AS (a)-(a) SELECT start, end MATCH (start)-/:self*/->(end)";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());

    String prettyPrintedQuery = result.getGraphQuery().toString();
    assertTrue(pgql.parse(prettyPrintedQuery).isQueryValid());
  }

  @Test /* GM-18250 */
  public void multipleEdgesInShortest() throws Exception {
    String query = "SELECT u, v FROM MATCH ANY SHORTEST( (u) -> (x0) -> (x1) -> (x2) -> (v) ) WHERE u != v ORDER BY id(u)";
    PgqlResult result = pgql.parse(query); // this used to fail
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains(
        "Not supported: path pattern containing multiple edge patterns in combination with ANY, SHORTEST or CHEAPEST; try splitting up the pattern into multiple path patterns"));
  }

  @Test
  public void multipleEdgesInShortest2() throws Exception {
    String query = "SELECT 1 FROM MATCH ANY SHORTEST ( (a) ( (n) -[e1]-> (m) -[e2]-> (o) )* (b) )";
    PgqlResult result = pgql.parse(query); // this should not fail
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains(
        "Not supported: path pattern containing multiple edge patterns in combination with ANY, SHORTEST or CHEAPEST; try splitting up the pattern into multiple path patterns"));
  }

  @Test
  public void escapingBetweenDifferentPgqlVersions() throws Exception {
    String pgql10Query = "PATH p := (x) -> (y) " //
        + "SELECT n.prop AS prop, '_\\\"_\"_\\'_\\'_\\n_\\t_\\\\n_' AS v1, \"_\\\"_\\\"_'_\\'_\\n_\\t_\\\\n_\" AS v2" //
        + " WHERE (n:lbl1|lbl2) -[e:'lbl3'|'lbl4'|'_\"_\"_\\'_\\'_\\n_\\t_']-> (m) -/:p*/-> (o)" //
        + "     , m.prop = o.prop";
    String pgql11Query = "PATH p AS (x) -> (y) " //
        + "SELECT n.prop AS prop, '_\\\"_\"_''_\\'_\\n_\\t_\\\\n_' AS v1, '_\"_\\\"_''_\\'_\\n_\\t_\\\\n_' AS v2" //
        + " MATCH (n:lbl1|lbl2) -[e:\"lbl3\"|\"lbl4\"|\"_\"\"_\\\"_'_\\'_\\n_\\t_\"]-> (m) -/:p*/-> (o)" //
        + " WHERE m.prop = o.prop";
    String pgql13Query = "PATH \"p\" AS (\"x\") -> (\"y\") " //
        + "SELECT \"n\".\"prop\", '_\"_\"_''_''_\n_\t_\\n_' AS \"v1\", '_\"_\"_''_''_\n_\t_\\n_' AS \"v2\"" //
        + "  FROM MATCH (\"n\":\"lbl1\"|\"lbl2\") -[\"e\":\"lbl3\"|\"lbl4\"|\"_\"\"_\"\"_'_'_\n_\t_\"]-> (\"m\") -/:\"p\"*/-> (\"o\")" //
        + " WHERE \"m\".\"prop\" = \"o\".\"prop\"";

    PgqlResult result10 = pgql.parse(pgql10Query);
    assertTrue(result10.isQueryValid());
    PgqlResult result11 = pgql.parse(pgql11Query);
    assertTrue(result11.isQueryValid());
    PgqlResult result13 = pgql.parse(pgql13Query);
    assertTrue(result13.isQueryValid());

    assertEquals(result10.getGraphQuery(), result11.getGraphQuery());
    assertEquals(result11.getGraphQuery(), result13.getGraphQuery());

    pgql10Query = "SELECT 1 WHERE (n WITH prop = \"xyz\")";
    pgql11Query = "SELECT 1 MATCH (n) WHERE n.prop = 'xyz'";

    result10 = pgql.parse(pgql10Query);
    assertTrue(result10.isQueryValid());
    result11 = pgql.parse(pgql11Query);
    assertTrue(result11.isQueryValid());

    assertEquals(result10.getGraphQuery(), result11.getGraphQuery());
  }

  @Test
  public void quotedVariableReferenceInLagacyPgqlVersions() throws Exception {
    String errorMessage = "Double quoted variable references are only available in PGQL 1.3 and up";

    String pgql10Query = "SELECT \"n\" WHERE (n)";
    PgqlResult result10 = pgql.parse(pgql10Query);
    assertTrue(result10.isQueryValid());

    String pgql11Query = "SELECT \"n\" MATCH (n)";
    PgqlResult result11 = pgql.parse(pgql11Query);
    assertFalse(result11.isQueryValid());
    assertTrue(result11.getErrorMessages().contains(errorMessage));

    pgql10Query = "SELECT \"n\".prop WHERE (n)";
    result10 = pgql.parse(pgql10Query);
    assertTrue(result10.isQueryValid());

    pgql11Query = "SELECT \"n\".prop MATCH (n)";
    result11 = pgql.parse(pgql11Query);
    assertFalse(result11.isQueryValid());
    assertTrue(result11.getErrorMessages().contains(errorMessage));
  }

  @Test
  public void missingEqualsForCast() throws Exception {
    String pgqlQuery = "SELECT CAST(n.prop AS STRING) MATCH (n)";
    assertEquals(pgql.parse(pgqlQuery).getGraphQuery(), pgql.parse(pgqlQuery).getGraphQuery());
  }

  @Test
  public void errorOnMissingQuantifier() throws Exception {
    String shortestQuery = "SELECT * MATCH SHORTEST ((n) -[e]-> (m))";
    String cheapestQuery = "SELECT * MATCH SHORTEST ((n) -[e]-> (m))";
    String errorMessage = "Quantifier of the form * or + or {1,4} expected";
    assertTrue(pgql.parse(shortestQuery).getErrorMessages().contains(errorMessage));
    assertTrue(pgql.parse(cheapestQuery).getErrorMessages().contains(errorMessage));
  }

  @Test
  public void errorsOnUndefinedVertexTable() throws Exception {
    String statement = "create property graph g\n" + //
        "  vertex tables (\n" + //
        "    Person\n" + //
        "  )\n" + //
        "  edge tables (\n" + //
        "    knows Source person destination xyz\n" + //
        "  )\n";
    String errorMessage = "Undefined vertex table";
    assertTrue(pgql.parse(statement).getErrorMessages().contains(errorMessage));
  }

  @Test
  public void errorsOnDuplicateVertexTable() throws Exception {
    String statement = "create property graph g\n" + //
        "  vertex tables (\n" + //
        "    Person, Person\n" + //
        "  )";
    String errorMessage = "Duplicate vertex table name; use an alias to make the vertex table name unique (table AS alias)";
    assertTrue(pgql.parse(statement).getErrorMessages().contains(errorMessage));
  }

  @Test
  public void errorsOnDuplicateEdgeTable() throws Exception {
    String statement = "create property graph g\n" + //
        "  vertex tables (\n" + //
        "    Person\n" + //
        "  )\n" + //
        "  edge tables (\n" + //
        "    knows SOURCE Person DESTINATION Person,\n" + //
        "    knows SOURCE Person DESTINATION Person\n" + //
        "  )";
    String errorMessage = "Duplicate edge table name; use an alias to make the edge table name unique (table AS alias)";
    assertTrue(pgql.parse(statement).getErrorMessages().contains(errorMessage));
  }

  @Test
  public void defaultToPropertiesAreAllColumns() throws Exception {
    String statement1 = "CREATE PROPERTY GRAPH g\n" + //
        "  VERTEX TABLES (\n" + //
        "    X,\n" + //
        "    Y LABEL Y,\n" + //
        "    Z LABEL Z1 LABEL Z2\n" + //
        "  )\n" + //
        "  EDGE TABLES (\n" + //
        "    E1 SOURCE X DESTINATION Y,\n" + //
        "    E2 SOURCE X DESTINATION Y LABEL E2,\n" + //
        "    E3 SOURCE X DESTINATION Y LABEL E3_1 LABEL E3_2\n" + //
        "  )";
    String statement2 = "CREATE PROPERTY GRAPH g\n" + //
        "  VERTEX TABLES (\n" + //
        "    X PROPERTIES ARE ALL COLUMNS,\n" + //
        "    Y LABEL Y PROPERTIES ARE ALL COLUMNS,\n" + //
        "    Z LABEL Z1 PROPERTIES ARE ALL COLUMNS LABEL Z2 PROPERTIES ARE ALL COLUMNS\n" + //
        "  )\n" + //
        "  EDGE TABLES (\n" + //
        "    E1 SOURCE X DESTINATION Y PROPERTIES ARE ALL COLUMNS,\n" + //
        "    E2 SOURCE X DESTINATION Y LABEL E2 PROPERTIES ARE ALL COLUMNS,\n" + //
        "    E3 SOURCE X DESTINATION Y LABEL E3_1 PROPERTIES ARE ALL COLUMNS LABEL E3_2 PROPERTIES ARE ALL COLUMNS\n" + //
        "  )";

    assertEquals(pgql.parse(statement1).getPgqlStatement(), pgql.parse(statement2).getPgqlStatement());
  }

  @Test
  public void twoForwardSlashesInCommentIdentifierOrLiteral() throws Exception {
    assertTrue(pgql.parse("SELECT * /* https://pgql-lang.org/ */" + //
        "                  FROM MATCH (n:\"//\"\"//\") " + //
        "                  WHERE n.name = '//''//'").isQueryValid());
  }

  @Test
  public void testPropertyExpressionWithoutAlias() throws Exception {
    String statement = "  CREATE PROPERTY GRAPH myGraph " + //
        "VERTEX TABLES ( Person PROPERTIES ( CAST(age AS INTEGER) ) )";

    String errorMessage = "Alias required (.. AS name)";
    assertTrue(pgql.parse(statement).getErrorMessages().contains(errorMessage));
  }

  @Test
  public void testInPredicateColumnName() throws Exception {
    String query = "SELECT a.number in (a.number), a.number AS \"a.number\" FROM MATCH (a)";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    List<ExpAsVar> projectionElements = selectQuery.getProjection().getElements();
    assertEquals("a.number in (a.number)", projectionElements.get(0).getName());
    assertEquals("a.number", projectionElements.get(1).getName());
  }

  @Test
  public void testNoAmbiguityIdFunction() throws Exception {
    pgql.parse("SELECT n.id() WHERE (n)");
  }

  @Test
  public void testColumnNamesOfLegacyPgql10Functions() throws Exception {
    String query = "SELECT x.has('name'), x.has('name', 'age'), x.id(), x.label(), x.labels(), x.inDegree(), x.outDegree() WHERE (x)";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getGraphQuery();
    List<ExpAsVar> projectionElements = selectQuery.getProjection().getElements();

    assertEquals("x.has('name')", projectionElements.get(0).getName());
    assertEquals("x.has('name', 'age')", projectionElements.get(1).getName());
    assertEquals("x.id()", projectionElements.get(2).getName());
    assertEquals("x.label()", projectionElements.get(3).getName());
    assertEquals("x.labels()", projectionElements.get(4).getName());
    assertEquals("x.inDegree()", projectionElements.get(5).getName());
    assertEquals("x.outDegree()", projectionElements.get(6).getName());
  }

  @Test
  public void testUniqueNonAnonymousNameAfterPrettyPrintingDuplicateGroupByExpression() throws Exception {
    String prettyPrintedQuery = pgql.parse("SELECT e AS e1, e AS e2 FROM MATCH (v)-[e]->(v1) GROUP BY e1, e2")
        .getGraphQuery().toString();
    assertTrue(pgql.parse(prettyPrintedQuery).isQueryValid());

    prettyPrintedQuery = pgql.parse("SELECT e.prop AS e1, e.prop AS e2 FROM MATCH (v)-[e]->(v1) GROUP BY e1, e2")
        .getGraphQuery().toString();
    assertTrue(pgql.parse(prettyPrintedQuery).isQueryValid());
  }

  @Test
  public void testPreservePrefixWhenNoMetadata() throws Exception {
    SelectQuery query = (SelectQuery) pgql.parse("SELECT n.* PREFIX 'N_' FROM MATCH (n)").getGraphQuery();
    AllProperties allProperties = (AllProperties) query.getProjection().getElements().get(0).getExp();
    assertEquals("N_", allProperties.getPrefix());
  }

  @Test
  public void testGetGraphNameForLateralQuery() throws Exception {
    GraphQuery graphQuery = pgql.parse("SELECT number " + //
        "FROM LATERAL ( " + //
        "       SELECT a.number " + //
        "       FROM MATCH (a) ON financial_transactions " + //
        "     )").getGraphQuery();
    assertEquals("FINANCIAL_TRANSACTIONS", graphQuery.getGraphName().getName());
  }

  /*
   * test if SELECT * gets correctly translated into an equivalent query without SELECT * even when no metadata is
   * available
   */
  @Test
  public void testSelectStartWithSelectAllPropertiesInLateralAndNoMetadata() throws Exception {
    GraphQuery graphQuery = pgql.parse("SELECT * " + //
        "FROM LATERAL ( " + //
        "       SELECT * " + //
        "       FROM LATERAL ( SELECT n.*, ( SELECT 123 FROM MATCH () LIMIT 1 ) AS sub FROM MATCH (n) -> ()) ) " + //
        "   , LATERAL ( SELECT n.* FROM MATCH (n) -> () )").getGraphQuery();

    GraphQuery equivalentQuery = pgql.parse("SELECT \"anonymous_7\".*, sub, \"anonymous_8\".*" + //
        "FROM LATERAL ( " + //
        "     SELECT \"anonymous_6\" AS \"anonymous_7\", sub " + //
        "     FROM LATERAL ( " + //
        "            SELECT n AS \"anonymous_6\", ( SELECT 123 FROM MATCH (\"anonymous_1\") LIMIT 1 ) AS sub " + //
        "            FROM MATCH (n) -[\"anonymous_2\"]-> (\"anonymous_3\") )) " + //
        "   , LATERAL ( SELECT n AS \"anonymous_8\" FROM MATCH (n) -[\"anonymous_4\"]-> (\"anonymous_5\") )")
        .getGraphQuery();

    assertEquals(equivalentQuery.toString(), graphQuery.toString());
  }

  @Test
  public void testNoErrorWhenVertexTablesOfBaseGraphAreNotDefined() throws Exception {
    PgqlResult result = pgql.parse(//
        "CREATE PROPERTY GRAPH g1 " + //
            "  BASE GRAPHS ( g2 ) " + //
            "  EDGE TABLES ( " + //
            "    e1 SOURCE v1 DESTINATION v2 " + // there should be no issue with references v1 and v2 here
            "  )");
    assertTrue(result.isQueryValid());

    result = pgql.parse(//
        "CREATE PROPERTY GRAPH g1 " + //
            "  BASE GRAPHS ( g2 ALL ELEMENT TABLES ) " + //
            "  EDGE TABLES ( " + //
            "    e1 SOURCE v1 DESTINATION v2 " + // there should be no issue with references v1 and v2 here
            "  )");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testNonStringLiteralInHasLabel() throws Exception {
    // make sure that normalization only happens in case of string literal, but not in case of e.g. a bind variable (PG
    // View limitation)
    testNonStringLiteralInHasLabelHelper("SELECT n.name FROM MATCH (n) WHERE has_label(n, 'Person')", "has_label");
    testNonStringLiteralInHasLabelHelper("SELECT n.name FROM MATCH (n) WHERE \"Has_Label\"(n, 'Person')", "has_label");
    testNonStringLiteralInHasLabelHelper("SELECT n.name FROM MATCH (n) WHERE has_label(n, ?)", "HAS_LABEL");
    testNonStringLiteralInHasLabelHelper("SELECT n.name FROM MATCH (n) WHERE has_label(n, 'Per' || 'son')",
        "HAS_LABEL");
  }

  private void testNonStringLiteralInHasLabelHelper(String query, String expectedFunctionName) throws Exception {
    PgqlResult result = pgql.parse(query);
    FunctionCall hasLabel = (FunctionCall) result.getGraphQuery().getGraphPattern().getConstraints().iterator().next();
    assertEquals(expectedFunctionName, hasLabel.getFunctionName());
  }

  @Test
  @Ignore // fix me once PG View can handle it (see above)
  public void testHasLabelNormalizedInNestedFashion() throws Exception {
    PgqlResult result1 = pgql.parse(
        "SELECT has_label(n, CASE WHEN has_label(m, 'PERSON') THEN 'CAR' ELSE 'HOUSE' END) FROM MATCH (n) -> (m)");
    PgqlResult result2 = pgql.parse(
        "SELECT \"has_label\"(n, CASE WHEN \"has_label\"(m, 'PERSON') THEN 'CAR' ELSE 'HOUSE' END) FROM MATCH (n) -> (m)");
    assertEquals(result2.getGraphQuery().toString(), result1.getGraphQuery().toString());
  }

  @Test
  public void testAllElementTablesExcept() throws Exception {
    PgqlResult result = pgql.parse("CREATE PROPERTY GRAPH g1 BASE GRAPHS ( g2 ALL ELEMENT TABLES EXCEPT ( t1, t2 ) )");
    CreateSuperPropertyGraph createPropertyGraph = (CreateSuperPropertyGraph) result.getPgqlStatement();
    List<String> allEmentTablesExcept = createPropertyGraph.getBaseGraphs().get(0).getAllElementTablesExcept();
    assertEquals("T1", allEmentTablesExcept.get(0));
    assertEquals("T2", allEmentTablesExcept.get(1));
  }

  @Test
  public void testReferenceVertexTableFromBaseGraph1() throws Exception {
    PgqlResult result = pgql.parse(
        "CREATE PROPERTY GRAPH g1 BASE GRAPHS ( g2 ) EDGE TABLES ( e1 SOURCE v1 DESTINATION KEY ( c1 ) REFERENCES v2 ( c2 ) )");
    assertTrue(result.isQueryValid());

    CreateSuperPropertyGraph cspg = (CreateSuperPropertyGraph) result.getPgqlStatement();
    assertNotNull(cspg.getEdgeTables().get(0).getSourceVertexTable());
    assertNotNull(cspg.getEdgeTables().get(0).getDestinationVertexTable());

    // make sure pretty printing does not fail and pretty printed statement can be parsed
    assertTrue(pgql.parse(result.getPgqlStatement().toString()).isQueryValid());
  }

  @Test
  public void testReferenceVertexTableFromBaseGraph2() throws Exception {
    PgqlResult result = pgql.parse("CREATE PROPERTY GRAPH g1 " + //
        "  BASE GRAPHS( " + //
        "   g2 ELEMENT TABLES( v1, v2 ) " + //
        "  ) " + //
        "  EDGE TABLES( " + //
        "    e KEY(id) " + //
        "    SOURCE KEY (sid) REFERENCES v1 (id) " + //
        "    DESTINATION KEY (did) REFERENCES v2 (id) " + //
        "  )");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void illegalQueryShouldNotCrashParser() throws Exception {
    PgqlResult result = pgql.parse("SELECT * FROM MATCH (v:STUDENT), LATERAL ( DELETE (w) FROM MATCH (w) )");
    assertNotNull(result.getErrorMessages());
  }

  @Test
  public void formatJson() throws Exception {
    PgqlResult result = pgql.parse("SELECT JSON_ARRAYAGG(v.prop FORMAT JSON) FROM MATCH (v)");
    AggrJsonArrayagg agg = (AggrJsonArrayagg) ((SelectQuery) result.getGraphQuery()).getProjection().getElements()
        .get(0).getExp();
    assertTrue(agg.isFormatJson());

    result = pgql.parse("SELECT JSON_ARRAYAGG(v.prop) FROM MATCH (v)");
    agg = (AggrJsonArrayagg) ((SelectQuery) result.getGraphQuery()).getProjection().getElements().get(0).getExp();
    assertFalse(agg.isFormatJson());
  }

  @Test
  public void ambiguityForOptionalMatch() throws Exception {
    /*
     * Previously, the below query was a valid PGQL 1.1/1.2 query in which "OPTIONAL" is the name of the graph. This
     * caused an ambiguity because the query is now also seen as a PGQL 2.0 query with OPTIONAL MATCH pattern. The fix
     * was to disallow "OPTIONAL" as graph name in PGQL 1.1/1.2.
     */
    PgqlResult result = pgql
        .parse("SELECT n FROM OPTIONAL MATCH (n IS Person) -[e IS likes]-> (m IS Person) WHERE n.name = 'Dave'");
    assertTrue(result.isQueryValid());

    /*
     * You can still use OPTIONAL as graph name in PGQL 1.2, but it requires double quoting. Also, you can still use
     * OPTIONAL as property name or variable name.
     */
    result = pgql.parse(
        "SELECT n FROM \"OPTIONAL\" MATCH (n IS Person) -[e IS likes]-> (optional IS Person) WHERE optional.optional = true");
    assertTrue(result.isQueryValid());

    /*
     * And you can also use OPTIONAL as graph name in newer PGQL versions, without requiring double quoting.
     */
    result = pgql
        .parse("SELECT n FROM MATCH (n IS Person) -[e IS likes]-> (m IS Person) ON optional WHERE n.name = 'Dave'");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void explicitOneRowPerMatch() throws Exception {
    PgqlResult result = pgql.parse(
        "SELECT sum FROM LATERAL (SELECT sum(v2.integerprop) as sum FROM MATCH ANY SHORTEST (v)  ->* (v2) ONE ROW PER MATCH)");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void parsePathSelectorInParenthesizedMatch() throws Exception {
    String query = "SELECT SUM(e.amount) as sum " //
        + "FROM MATCH ( ANY CHEAPEST (y)(-[e:transaction]-> COST e.amount)* (x)) ";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
    Iterator<VertexPairConnection> it = result.getGraphQuery().getGraphPattern().getConnections().iterator();
    QueryPath path = (QueryPath) it.next();
    assertEquals(PathFindingGoal.CHEAPEST, path.getPathFindingGoal());

    query = "SELECT SUM(e.amount) as sum " //
        + "FROM MATCH ( ANY CHEAPEST (y)(-[e:transaction]-> COST e.amount)* (x), " //
        + "ANY CHEAPEST (y)(-[e2:transaction]-> COST e.amount)* (x) WHERE sum < 2000) ";
    result = pgql.parse(query);
    assertTrue(result.isQueryValid());
    it = result.getGraphQuery().getGraphPattern().getConnections().iterator();
    path = (QueryPath) it.next();
    assertEquals(PathFindingGoal.CHEAPEST, path.getPathFindingGoal());
    path = (QueryPath) it.next();
    assertEquals(PathFindingGoal.CHEAPEST, path.getPathFindingGoal());
  }
}
