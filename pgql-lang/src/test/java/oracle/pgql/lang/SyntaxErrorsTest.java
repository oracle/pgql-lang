/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
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
    PgqlResult result = pgql.parse("SELECT * MATCH SHORTEST ( (n) -[e:lbl]->+ (m) )");
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

  @Test
  public void testDuplicateInsertUpdateDeleteClausesShouldNotThrowUnexpectedExceptions() throws Exception {
    String query = "MODIFY/*beta*/ ( INSERT VERTEX v1 INSERT VERTEX v2" //
        + " DELETE e1 DELETE e2" //
        + " UPDATE n SET PROPERTIES ( n.prop = 1 ) UPDATE m SET PROPERTIES ( n.prop = 2 ) )" //
        + " FROM g MATCH (n) -[e1]-> (m) -[e2]-> (o)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testUnresolvedReferencesInModify1() throws Exception {
    String query = "MODIFY/*beta*/ g ( INSERT EDGE e BETWEEN x AND y ) MATCH (v)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testUnresolvedReferencesInModify2() throws Exception {
    String query = "MODIFY/*beta*/ g ( UPDATE x SET PROPERTIES ( x.prop = y.prop + z.prop ) DELETE y ) MATCH (v)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testWrongVariableInInsertAndUpdate() throws Exception {
    String query = "MODIFY/*beta*/ ( INSERT VERTEX w LABELS ( x, y.prop ) PROPERTIES ( v.prop = 1, u.prop = 2 ) " //
        + "UPDATE v SET PROPERTIES ( w.prop = 3, u.prop = 4) ) FROM g MATCH (v)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testModifySubquery() throws Exception {
    String query = "SELECT ( MODIFY/*beta*/ ( INSERT VERTEX v ) ) FROM g MATCH (n)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testSetPropertyThatIsGroupedBy() throws Exception {
    String query = "MODIFY/*beta*/ g ( UPDATE n SET PROPERTIES ( [[n.prop]] = 123 ) ) " //
        + "FROM g MATCH (n) " //
        + "GROUP BY n.prop AS nProp";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testImplicitPropertyReferenceInPgql12() throws Exception {
    String query = "SELECT n.prop"//
        + "             FROM g MATCH (n)"//
        + "         ORDER BY prop"; // this is allowed only in PGQL >= 1.3
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Unresolved variable"));
  }
}
