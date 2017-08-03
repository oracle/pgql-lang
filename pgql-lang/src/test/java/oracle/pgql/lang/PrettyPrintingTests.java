/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import oracle.pgql.lang.ir.GraphQuery;

public class PrettyPrintingTests {

  private static Pgql pgql;

  @BeforeClass
  public static void setUp() throws Exception {
    pgql = new Pgql();
  }

  @Test
  public void testBasicGraphPattern1() throws Exception {
    String query = "SELECT n.name WHERE (n) -> (m), m.prop1 = 'abc' AND n.prop2 = m.prop2";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern1Reverse() throws Exception {
    String query = "SELECT n.name WHERE (n) <- (m), m.prop1 = 'abc' AND n.prop2 = m.prop2";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern2() throws Exception {
    String query = "SELECT n.name WHERE (n) -[e]-> (), e.weight = 10 OR e.weight < n.weight";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern2Reverse() throws Exception {
    String query = "SELECT n.name WHERE (n) <-[e]- (), e.weight = 10 OR e.weight < n.weight";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern3() throws Exception {
    String query = "SELECT n.name WHERE (n WITH prop1 = 10) -> ()";
    checkRoundTrip(query);
  }

  @Test
  public void testPathQuery1() throws Exception {
    String query = "SELECT n.name, m.name WHERE (n) -/:likes*/-> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testPathQuery1Reverse() throws Exception {
    String query = "SELECT n.name, m.name WHERE (n) <-/:likes*/- (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testPathQuery2() throws Exception {
    String query = "PATH knows := (n:Person) -[e:likes|dislikes]-> (m:Person) SELECT n.name, m.name WHERE (n) -/:knows*/-> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testPredicatesOnAnonymousVariables() throws Exception {
    String query = "SELECT m.name WHERE (WITH prop1 = 10) -> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testNestedPath() throws Exception {
    String query = "PATH abc := () -[:a|b|c]-> (b) PATH abc_star := () -/:abc*/-> () "
        + "PATH abc_star_star := () -/:abc_star*/-> () SELECT m.name WHERE (n) -/:abc_star_star+/-> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithOrderBy() throws Exception {
    String query = "SELECT m.name, m.age WHERE (m)->(n) ORDER BY m.age";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithOrderByLimit() throws Exception {
    String query = "SELECT m.name, m.age WHERE (m)->(n) ORDER BY m.age LIMIT 10";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithOrderByOffsetLimit() throws Exception {
    String query = "SELECT m.name, m.age WHERE (m)->(n) ORDER BY m.age OFFSET 2 LIMIT 1";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithFromClause() throws Exception {
    String query = "SELECT m.name, n.age FROM persons WHERE (m)->(n)";
    checkRoundTrip(query);
  }

  @Test
  public void testUndirectedEdge() throws Exception {
    String query = "SELECT m.name, m.age WHERE (m)-(n)";
    checkRoundTrip(query);
  }

  private void checkRoundTrip(String query1) throws PgqlException {

    /*
     * First, assert that when parsing a query into a GraphQuery object and then pretty printing that GraphQuery object,
     * we obtain a string that is a valid PGQL query.
     */
    GraphQuery iR1 = pgql.parse(query1).getGraphQuery();
    String query2 = iR1.toString();
    PgqlResult result2 = pgql.parse(query2);
    GraphQuery iR2 = result2.getGraphQuery();
    assertTrue(result2.getErrorMessages(), iR2 != null);

    /*
     * Since pretty-printed queries are in normal form, we can now round trip endlessly.
     * Here, we assert that when pretty-printing a GraphQuery object that was parsed from a pretty-printed query,
     * we obtain another GraphQuery object that is equal to the first.
     */
    String query3 = iR2.toString();
    GraphQuery iR3 = pgql.parse(query3).getGraphQuery();
    assertEquals(iR2, iR3);
  }
}