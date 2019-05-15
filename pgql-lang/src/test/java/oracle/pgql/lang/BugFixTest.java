/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}
