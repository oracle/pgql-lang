package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StaticOptimizationsTest extends AbstractPgqlTest {

  @Test
  public void testDuplicateOrderByElems() throws Exception {
    String query = "SELECT n.age AS nAge MATCH (n) ORDER BY n.age, n.age, nAge, nAge";
    assertEquals(pgql.parse(query).getGraphQuery().getOrderBy().getElements().size(), 1);
  }
}
