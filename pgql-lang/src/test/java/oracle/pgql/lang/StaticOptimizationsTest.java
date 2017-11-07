package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class StaticOptimizationsTest {

  private static Pgql pgql;

  @BeforeClass
  public static void setUp() throws Exception {
    pgql = new Pgql();
  }
  
  @Test
  public void testDuplicateOrderByElems() throws Exception {
    String query = "SELECT n.age AS nAge MATCH (n) ORDER BY n.age, n.age, nAge, nAge";
    assertEquals(pgql.parse(query).getGraphQuery().getOrderBy().getElements().size(), 1);
  }
}
