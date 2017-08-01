/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
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
    checkRoundtripAndEquals(query);
  }

  @Test
  public void testBasicGraphPattern2() throws Exception {
    String query = "SELECT n.name WHERE (n) -[e]-> (), e.weight = 10 OR e.weight < n.weight";
    checkRoundtripAndEquals(query);
  }

  @Test
  public void testBasicGraphPattern3() throws Exception {
    String query = "SELECT n.name WHERE (n WITH prop1 = 10) -> ()";
    checkRoundtripAndEquals(query);
  }

  @Test
  public void testPathQuery1() throws Exception {
    String query = "SELECT n.name, m.name WHERE (n) -/:likes*/-> (m)";
    checkRoundtrip(query);
  }

  @Test
  public void testPathQuery2() throws Exception {
    String query = "PATH knows := (n:Person) -[e:likes|dislikes]-> (m:Person) SELECT n.name, m.name WHERE (n) -/:knows*/-> (m)";
    checkRoundtrip(query);
  }

  @Test
  public void testPredicatesOnAnonymousVariables() throws Exception {
    String query = "SELECT m.name WHERE (WITH prop1 = 10) -> (m)";
    checkRoundtrip(query);
  }

  /**
   * Asserts that when parsing a query into a GraphQuery object and then pretty printing that GraphQuery object,
   * we obtain a string that is a valid PGQL query.
   */
  private void checkRoundtrip(String query) throws PgqlException {
    GraphQuery queryIR = pgql.parse(query).getGraphQuery();
    String prettyPrintedQuery = queryIR.toString();
    PgqlResult result = pgql.parse(prettyPrintedQuery); 
    assertTrue(result.getErrorMessages(), result.getGraphQuery() != null);
  }

  /**
   * Asserts that when parsing a query into a GraphQuery object A and then pretty printing A and parsing the result again
   * into a GraphQuery object B, A and B are equal.
   */
  private void checkRoundtripAndEquals(String query) throws PgqlException {
    GraphQuery queryIR = pgql.parse(query).getGraphQuery();
    String prettyPrintedQuery = queryIR.toString();
    GraphQuery prettyPrintedQueryIR = pgql.parse(prettyPrintedQuery).getGraphQuery();
    assertEquals(queryIR, prettyPrintedQueryIR);
  }
}