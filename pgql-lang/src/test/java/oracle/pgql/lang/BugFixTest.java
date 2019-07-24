/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.SelectQuery;

public class BugFixTest extends AbstractPgqlTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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
  public void testSparqlLikeAscDesc() throws Exception {
    thrown.expectMessage("10234234234234 is too large to be stored as int");
    pgql.parse("SELECT COUNT(*) MATCH TOP 10234234234234 SHORTEST ( (a) ->* (b) )");
  }

  @Test
  public void noErrorOnSelectStarSingleVertex() throws Exception {
    String query = "SELECT * MATCH (n)";
    assertTrue(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testUpdate() throws Exception {
    String query = "MODIFY/*beta*/ ( UPDATE n SET PROPERTIES ( [[n.prop]] = 123 ) ) " + //
        "FROM g MATCH (n) " + //
        "GROUP BY n.prop AS nProp";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void orderByEdgeIsPermittedOnlyInPgql10() throws Exception {
    String queryPgql10 = "SELECT 1 WHERE () -[e]-> () ORDER BY e";
    assertTrue(pgql.parse(queryPgql10).isQueryValid());

    String queryPgql11 = "SELECT 1 MATCH () -[e]-> () ORDER BY e";
    assertFalse(pgql.parse(queryPgql11).isQueryValid());

    String queryPgql13 = "SELECT 1 FROM EXPERIMENTAL_MATCH ( () -[e]-> () ) ORDER BY e";
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
    String query = "SELECT u, v MATCH SHORTEST( (u) -> (x0) -> (x1) -> (x2) -> (v) ) WHERE u != v ORDER BY id(u)";
    PgqlResult result = pgql.parse(query); // this used to fail
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("Not yet supported: multiple edge patterns in SHORTEST"));
  }

  @Test
  public void multipleEdgesInShortest2() throws Exception {
    String query = "SELECT 1 MATCH SHORTEST ( (a) ( (n) -[e1]-> (m) -[e2]-> (o) )* (b) )";
    PgqlResult result = pgql.parse(query); // this should not fail
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("Not yet supported: multiple edge patterns in SHORTEST"));
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
    String pgql13Query = "PATH p AS (x) -> (y) " //
        + "SELECT n.prop, '_\"_\"_''_''_\n_\t_\\n_' AS v1, '_\"_\"_''_''_\n_\t_\\n_' AS v2" //
        + "  FROM EXPERIMENTAL_MATCH ( (n:\"lbl1\"|\"lbl2\") -[e:\"lbl3\"|\"lbl4\"|\"_\"\"_\"\"_'_'_\n_\t_\"]-> (m) -/:p*/-> (o) )" //
        + " WHERE m.prop = o.prop"; // TODO why is it not equal?

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
}
