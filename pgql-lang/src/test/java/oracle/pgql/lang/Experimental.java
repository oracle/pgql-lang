package oracle.pgql.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Experimental extends AbstractPgqlTest {

  @Test
  public void testReferencesToSelectExpression1() throws Exception {
    String query = "SELECT n.age * 2 AS doubleAge "//
        + "    FROM MATCH (n) ON g "//
        + "   WHERE doubleAge = n.age + n.age "//
        + "GROUP BY doubleAge "//
        + "  HAVING doubleAge = n.age * 2 "//
        + "ORDER BY 2 * doubleAge ASC, 2 * (n.age * 2) DESC";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testReferencesToSelectExpression2() throws Exception {
    String query = "SELECT n.age * 2 AS doubleAge "//
        + "    FROM MATCH (n) ON g "//
        + "   WHERE doubleAge = n.age + n.age "//
        + "GROUP BY n.age * 2 "//
        + "  HAVING doubleAge = n.age * 2 "//
        + "ORDER BY 2* doubleAge ASC, 2 * (n.age * 2) DESC";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testImplicitPropertyReferences1() throws Exception {
    String query = "SELECT n.prop"//
        + "           FROM MATCH (n) ON g"//
        + "       ORDER BY prop";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testImplicitPropertyReferences2() throws Exception {
    String query = "SELECT n.prop"//
        + "           FROM MATCH (n) ON g"//
        + "          WHERE prop = 3"//
        + "       GROUP BY prop"//
        + "         HAVING prop > 10";
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
  }
}
