/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import oracle.pgql.lang.ir.QueryExpression.FunctionCall;;

public class BugFixTest extends AbstractPgqlTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /* OL-Jira GM-13537 */
  @Test
  public void testDuplicateOrderByElems() throws Exception {
    String query = "SELECT NOTfunc() MATCH (n)";

    FunctionCall funcCall = (FunctionCall) pgql.parse(query).getGraphQuery().getProjection().getElements().get(0)
        .getExp();

    assertEquals("NOTfunc", funcCall.getFunctionName());
  }

  @Test
  public void testArrayAgg() throws Exception {
    String query = "SELECT ARRAY_AGG(e.weight) MATCH TOP 10 SHORTEST (n) -[e]->* (m)";

    assertTrue(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testSparqlLikeAscDesc() throws Exception {
    thrown.expectMessage("10234234234234 is too large to be stored as int");
    pgql.parse("SELECT COUNT(*) MATCH TOP 10234234234234 SHORTEST (a) ->* (b)");
  }

  @Test
  public void noErrorOnSelectStarSingleVertex() throws Exception {
    String query = "SELECT * MATCH (n)";
    assertTrue(pgql.parse(query).isQueryValid());
  }
}
