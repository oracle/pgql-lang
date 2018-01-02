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
}
