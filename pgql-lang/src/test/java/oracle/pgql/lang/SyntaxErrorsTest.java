/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import oracle.pgql.lang.ir.GraphQuery;

public class SyntaxErrorsTest extends AbstractPgqlTest {

  @Test
  public void testBasicGraphPattern1() throws Exception {
    PgqlResult result = pgql.parse(
        "SELECT label(e1), label(e2) MATCH (n) -[e1]-> (m) -[e2:likes|dislikes|blabla]-> (o), label(e1) != label(e2)");
    assertFalse(result.isQueryValid());
    assertFalse(result.getErrorMessages() == null);
  }

  @Test
  public void testEmptyString() throws Exception {
    PgqlResult result = pgql.parse("");
    assertFalse(result.isQueryValid());
    assertTrue(result.getErrorMessages().contains("Empty query string"));
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
    String query = "INSERT VERTEX v1 INSERT VERTEX v2" //
        + " DELETE e1 DELETE e2" //
        + " UPDATE n SET ( n.prop = 1 ) UPDATE m SET ( n.prop = 2 )" //
        + " FROM g MATCH (n) -[e1]-> (m) -[e2]-> (o)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testUnresolvedReferencesInModify1() throws Exception {
    String query = "INSERT EDGE e BETWEEN x AND y FROM MATCH (v)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testUnresolvedReferencesInModify2() throws Exception {
    String query = "UPDATE x SET ( x.prop = y.prop + z.prop ) DELETE y FROM MATCH (v)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testWrongVariableInInsertAndUpdate() throws Exception {
    String query = "INSERT VERTEX w LABELS ( x, y.prop ) PROPERTIES ( v.prop = 1, u.prop = 2 ) " //
        + "UPDATE v SET ( w.prop = 3, u.prop = 4) FROM MATCH (v)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testModifySubquery() throws Exception {
    String query = "SELECT ( INSERT VERTEX v ) FROM MATCH (n)";
    assertFalse(pgql.parse(query).isQueryValid());
  }

  @Test
  public void testSetPropertyThatIsGroupedBy() throws Exception {
    String query = "UPDATE n SET ( n.prop = 123 ) " //
        + "FROM MATCH (n) " //
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

  /*
   * Tests the same as tests "Update other element than the one that was meant to be updated" and
   * "Set properties of other element than the one that is being inserted" in pgql-tests/error-messages/modify.spt, but
   * then for double quoted variables since Spoofax doesn't allow testing for double quotes.
   */
  @Test
  public void testUpdateOtherElementThanWasMeantAndWithDoubleQuotes() throws Exception {
    String query = "INSERT VERTEX \"o\" PROPERTIES ( \"n\".prop = 3 )\n" + //
        "                , EDGE \"e\" BETWEEN \"n\" AND \"o\" PROPERTIES ( \"n\".prop = 4 )\n" + //
        "           UPDATE \"n\" SET ( \"m\".prop = 3 )\n" + //
        "                , \"m\" SET ( \"n\".prop = 4 )\n" + //
        "             FROM MATCH (\"n\") -> (\"m\")";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Did you mean \"o\"?"));
    assertTrue(result.getErrorMessages().contains("Did you mean \"e\"?"));
    assertTrue(result.getErrorMessages().contains("Did you mean \"m\"?"));
    assertTrue(result.getErrorMessages().contains("Did you mean \"o\"?"));
  }

  @Test
  public void testJavaLikeCommentInSelect() throws Exception {
    String query = "SELECT * FROM MATCH (n) // comment";
    thrown.expectMessage("Use /* .. */ instead of // .. to introduce a comment");
    pgql.parse(query);
  }

  @Test
  public void testJavaLikeCommentInInsert() throws Exception {
    String query = "INSERT VERTEX v // comment\n FROM MATCH (n)";
    thrown.expectMessage("Use /* .. */ instead of // .. to introduce a comment");
    pgql.parse(query);
  }

  @Test
  public void testJavaLikeCommentInCreatePropertyGraph() throws Exception {
    String query = "CREATE PROPERTY GRAPH g VERTEX TABLES ( Person ) // comment";
    thrown.expectMessage("Use /* .. */ instead of // .. to introduce a comment");
    pgql.parse(query);
  }

  @Test
  public void testJavaLikeCommentInDropPropertyGraph() throws Exception {
    String query = "DROP // comment\n PROPERTY GRAPH g";
    thrown.expectMessage("Use /* .. */ instead of // .. to introduce a comment");
    pgql.parse(query);
  }

  @Test
  public void testMissingOnClause1() throws Exception {
    String query = "SELECT * FROM MATCH (n), MATCH (m) ON g2";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Missing graph name"));
  }

  @Test
  public void testMissingOnClause2() throws Exception {
    String query = "SELECT EXISTS ( SELECT * FROM MATCH (n) -[e]-> (m), MATCH (m) -[e2]-> (o) ON g2 ) FROM MATCH (n)";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Missing graph name"));
  }

  @Test
  public void testMissingOnClause3() throws Exception {
    String query = "SELECT EXISTS ( SELECT * FROM MATCH (n) -[e]-> (m) ) FROM MATCH (n) ON g";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Missing graph name"));
  }

  @Test
  public void testMissingOnClause4() throws Exception {
    // inner queries in PGQL 1.2 are checked separately; no error here
    String query = "SELECT EXISTS ( SELECT * MATCH (n) -[e]-> (m) ) FROM g MATCH (n)";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testMultipleGraphNames1() throws Exception {
    String query = "SELECT * FROM MATCH (n) ON g1, MATCH (m) ON g2";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Querying multiple graphs is not supported"));
  }

  @Test
  public void testMultipleGraphNames2() throws Exception {
    String query = "SELECT EXISTS ( SELECT * FROM MATCH (m) ON g2 ) FROM MATCH (n) ON g1";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Querying multiple graphs is not supported"));
  }

  @Test
  public void testNegativeLowerBound() throws Exception {
    String query = "SELECT 1 FROM MATCH ANY SHORTEST () ->{-1, 2} ()";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains("Syntax error, not expected here: '-'"));
  }

  @Test
  public void testUnsupportedSubqueryInFromClause() throws Exception {
    String query = "SELECT 1 FROM MATCH LATERAL ( SELECT * FROM MATCH (n) )";
    GraphQuery result = pgql.parse(query).getGraphQuery();
    result.getTableExpressions(); // make sure this doesn't fail

    thrown.expectMessage("Subqueries in FROM clause not supported");
    result.getGraphPattern();
  }

  @Test
  public void testOriginOffsetLineBoundaries() throws Exception {
    String query = "SELECT *\n" + //
        "FROM GRAPH_TABLE ( financial_transactions\n" + //
        "       MATCH (a IS Account) -[IS transaction]->+ (a)\n" + //
        "       KEEP ALL SIMPLE PATHS\n" + //
        "       ONE ROW PER STEP ( v1, e, v2 )\n" + //
        "       COLUMNS ( MATCHNUM() AS match_num, ELEMENT_NUMBER(e) AS elem_num,\n" + //
        "\n" + //
        "v23\n" + //
        ".number AS account1, e2\n" + //
        ".amount, v21.number AS account2 )\n" + //
        "     )\n" + //
        "ORDER BY match_num, elem_num, x";
    PgqlResult result = pgql.parse(query);
    assertEquals("Error(s) in line 8:\n" + //
        "\n" + //
        "  v23\n" + //
        "  ^^^\n" + //
        "Unresolved variable\n" + //
        "\n" + //
        "Error(s) in line 9:\n" + //
        "\n" + //
        "  .number AS account1, e2\n" + //
        "                       ^^\n" + //
        "Unresolved variable\n" + //
        "\n" + //
        "Error(s) in line 10:\n" + //
        "\n" + //
        "  .amount, v21.number AS account2 )\n" + //
        "           ^^^\n" + //
        "Unresolved variable\n" + //
        "\n" + //
        "Error(s) in line 12:\n" + //
        "\n" + //
        "  ORDER BY match_num, elem_num, x\n" + //
        "                                ^\n" + //
        "Unresolved variable", result.getErrorMessages());

    query = "SELECT 1\n" + //
        "FROM GRAPH_TABLE ( financial_transactions\n" + //
        "       MATCH (a IS Account) -[IS transaction]->+ (a)\n" + //
        "       KEEP ALL SIMPLE PATHS\n" + //
        "       WHERE a.number = 10039\n" + //
        "       ONE ROW PER STEP ( v1, e, v2 )\n" + //
        "       COLUMNS ( MATCHNUM() AS match_num, ELEMENT_NUMBER(e) AS elem_num,\n" + //
        "                 v1.number AS account1, e.amount, v2.number AS account2 )\n" + //
        "     )\n" + //
        "ORDER BY AVG(\n" + //
        "1)";
    result = pgql.parse(query);
    assertEquals("Error(s) in line 10:\n" + //
        "\n" + //
        "  ORDER BY AVG(\n" + //
        "           ^^^^\n" + //
        "Aggregation in ORDER BY only allowed if SELECT has aggregations", result.getErrorMessages());
  }

  @Test
  public void testNonBreakingWhiteSpace() throws Exception {
    String query = "SELECT x, y FROM MATCH \u00a0(n) -[e]-> (m)";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.getErrorMessages().contains(Pgql.NON_BREAKING_WHITE_SPACE_ERROR));
  }
}
