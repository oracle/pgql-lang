package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import oracle.pgql.lang.ir.QueryExpression.FunctionCall;;

public class BugFixTest extends AbstractPgqlTest {

  /* OL-Jira GM-13537 */
  @Test
  public void testDuplicateOrderByElems() throws Exception {
    String query = "SELECT NOTfunc() MATCH (n)";

    FunctionCall funcCall = (FunctionCall) pgql.parse(query).getGraphQuery().getProjection().getElements().get(0)
        .getExp();

    assertEquals("NOTfunc", funcCall.getFunctionName());
  }

}
