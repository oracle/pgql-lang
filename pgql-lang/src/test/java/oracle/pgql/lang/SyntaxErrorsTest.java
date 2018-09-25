/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.*;

import org.junit.Test;

public class SyntaxErrorsTest extends AbstractPgqlTest {

  @Test
  public void testBasicGraphPattern1() throws Exception {
    PgqlResult result = pgql.parse(
        "SELECT label(e1), label(e2) MATCH (n) -[e1]-> (m) -[e2:likes|dislikes|blabla]-> (o), label(e1) != label(e2)");
    assertFalse(result.isQueryValid());
    assertFalse(result.getErrorMessages() == null);
    assertFalse(result.getGraphQuery() == null);
    result.getGraphQuery().toString(); // may pretty-print syntactically/semantically incorrect query but should not
                                       // produce errors
  }

  @Test
  public void testEmptyString() throws Exception {
    PgqlResult result = pgql.parse("");
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("Syntax error"));
    assertTrue(result.getGraphQuery() == null);
  }

  @Test
  public void testSelectStarGroupBy() throws Exception {
    PgqlResult result = pgql.parse("SELECT * MATCH (n) GROUP BY n.prop");
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("SELECT * not allowed in combination with GROUP BY"));
    assertFalse(result.getGraphQuery() == null);
  }

  @Test
  public void testSparqlLikeAscDesc() throws Exception {
    PgqlResult result = pgql.parse("SELECT n.prop MATCH (n) ORDER BY ASC(n.prop1), DESC(n.prop2)");
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("Use [n.prop1 ASC] instead of [ASC(n.prop1)]"));
    assertTrue(result.getErrorMessages().contains("Use [n.prop2 DESC] instead of [DESC(n.prop2)]"));
    assertFalse(result.getGraphQuery() == null);
  }

  @Test
  public void testSelectStarGroupVariable() throws Exception {
    PgqlResult result = pgql.parse("SELECT * MATCH SHORTEST (n) -[e:lbl]->+ (m)");
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages()
        .contains("Cannot select all variables since e is a group variable but group variables cannot be selected"));
  }

  public void testDatetimes() throws Exception {
    PgqlResult result = pgql.parse("SELECT DATE 'x', TIME 'y', TIMESTAMP 'z' MATCH (n)");
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("Not a valid date"));
    assertTrue(result.getErrorMessages().contains("Not a valid time"));
    assertTrue(result.getErrorMessages().contains("Not a valid timestamp"));
    assertFalse(result.getGraphQuery() == null);
  }
}
