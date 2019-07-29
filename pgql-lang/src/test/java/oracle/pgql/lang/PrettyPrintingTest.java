/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.Statement;;

public class PrettyPrintingTest extends AbstractPgqlTest {

  @Test
  public void testBasicGraphPattern1() throws Exception {
    String query = "SELECT n.name FROM g MATCH (n) -> (m) WHERE m.prop1 = 'abc' AND n.prop2 = m.prop2";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern1Reverse() throws Exception {
    String query = "SELECT n.name FROM g MATCH (n) <- (m) WHERE m.prop1 = 'abc' AND n.prop2 = m.prop2";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern2() throws Exception {
    String query = "SELECT n.name FROM g MATCH (n) -[e]-> () WHERE e.weight = 10 OR e.weight < n.weight";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern2Reverse() throws Exception {
    String query = "SELECT n.name FROM g MATCH (n) <-[e]- () WHERE e.weight = 10 OR e.weight < n.weight";
    checkRoundTrip(query);
  }

  @Test
  public void testBasicGraphPattern3() throws Exception {
    String query = "SELECT n.name FROM g MATCH (n) -> () WHERE n.prop1 = 10";
    checkRoundTrip(query);
  }

  @Test
  public void testPathQuery1() throws Exception {
    String query = "SELECT n.name, m.name FROM g MATCH (n) -/:likes*/-> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testPathQuery1Reverse() throws Exception {
    String query = "SELECT n.name, m.name FROM g MATCH (n) <-/:likes*/- (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testPathQuery2() throws Exception {
    String query = "PATH knows AS (n:Person) -[e:likes|dislikes]-> (m:Person) SELECT n.name, m.name FROM g MATCH (n) -/:knows*/-> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testPredicatesOnAnonymousVariables() throws Exception {
    String query = "SELECT m.name FROM g MATCH (:a|b) -> (m)";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithOrderBy() throws Exception {
    String query = "SELECT m.name, m.age FROM g MATCH (m)->(n) ORDER BY m.age";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithOrderByLimit() throws Exception {
    String query = "SELECT m.name, m.age FROM g MATCH (m)->(n) ORDER BY m.age LIMIT 10";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithOrderByOffsetLimit() throws Exception {
    String query = "SELECT m.name, m.age FROM g MATCH (m)->(n) ORDER BY m.age OFFSET 2 LIMIT 1";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithFromClause() throws Exception {
    String query = "SELECT m.name, n.age FROM persons MATCH (m)->(n)";
    checkRoundTrip(query);
  }

  @Test
  public void testQueryWithoutFromClause() throws Exception {
    String query = "SELECT m.name, n.age MATCH (m)->(n)";
    checkRoundTrip(query);
  }

  @Test
  public void testUndirectedEdge() throws Exception {
    String query = "SELECT m.name, m.age FROM g MATCH (m)-(n)";
    checkRoundTrip(query);
  }

  @Test
  public void testAggregation() throws Exception {
    String query = "SELECT COUNT(*) AS count, AVG(n.age) AS AVG FROM g MATCH (n)";
    checkRoundTrip(query);
  }

  @Test
  public void testDistinct() throws Exception {
    String query = "SELECT DISTINCT " //
        + "COUNT(DISTINCT n.age) AS count," //
        + "MIN(DISTINCT n.age)," //
        + "MAX(DISTINCT n.age)," //
        + "AVG(DISTINCT n.age)," //
        + "SUM(DISTINCT n.age)" //
        + "FROM g MATCH (n)";
    checkRoundTrip(query);
  }

  @Test
  public void testDateTime() throws Exception {
    String query = "SELECT " //
        + "DATE '2017-01-01', "//
        + "TIME '20:00:00', " //
        + "TIMESTAMP '2017-01-01 20:00:00', "//
        + "TIME '20:00:00.1234+01:00', "//
        + "TIMESTAMP '2017-01-01 20:00:00.1234-01:00'" //
        + "FROM g MATCH ()";
    checkRoundTrip(query);
  }

  @Test
  public void testExistsQuery() throws Exception {
    String query = "SELECT id(n) FROM g MATCH (n) WHERE EXISTS ( SELECT 1 FROM g MATCH (n) -[:likes]-> (m) )";
    checkRoundTrip(query);
  }

  @Test
  public void testExistsInAggregation() throws Exception {
    String query = "SELECT MAX(EXISTS (SELECT * FROM g MATCH (m)->(o) WHERE o.age > n.age)) FROM g MATCH (n)->(m)";
    checkRoundTrip(query);
  }

  @Test
  public void testExistsInOrderBy() throws Exception {
    String query = "SELECT id(n), 3 AS three FROM g MATCH (n) ORDER BY EXISTS ( SELECT * FROM g MATCH (m) WHERE m.age + three = n.age), id(n)";
    checkRoundTrip(query);
  }

  @Test
  public void testIdentifierEscapingPgql11() throws Exception {
    String identifier = "\"\"";
    String escapedIdentifier = "\"\\\"\"\"\"";
    String query = "SELECT n." + escapedIdentifier + " FROM " + escapedIdentifier + " MATCH (n:" + escapedIdentifier
        + ")";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getStatement();

    PropertyAccess propertyAccess = (PropertyAccess) selectQuery.getProjection().getElements().get(0).getExp();
    assertEquals(propertyAccess.getPropertyName(), identifier);

    assertEquals(identifier, selectQuery.getInputGraphName());

    FunctionCall funcCall = (FunctionCall) selectQuery.getGraphPattern().getConstraints().iterator().next();
    assertEquals(funcCall.getFunctionName(), "has_label");
    String label = ((ConstString) funcCall.getArgs().get(1)).getValue();
    assertEquals(identifier, label);
  }

  @Test
  public void testHaving() throws Exception {
    String query = "SELECT n.age, COUNT(*) FROM g MATCH (n) GROUP BY n.age HAVING COUNT(*) > 100";
    checkRoundTrip(query);
  }

  @Test
  public void testShortest1() throws Exception {
    String query = "SELECT n.prop MATCH SHORTEST ( (n) (-[e:lbl]-> WHERE e.prop = 123)* (o) )";
    checkRoundTrip(query);
  }

  @Test
  public void testShortest2() throws Exception {
    String query = "SELECT ARRAY_AGG(e.weight) AS weights FROM g MATCH () -> (n), SHORTEST ( (n) -[e]->* (m) ), (m) -> (o)";
    checkRoundTrip(query);
  }

  @Test
  public void testShortest3() throws Exception {
    String query = "SELECT SUM(e.weight), COUNT(COUNT(e.weight)) FROM g MATCH SHORTEST ( (a) -[e]->* (b) ) GROUP BY SUM(e.weight) ORDER BY SUM(e.weight)";
    checkRoundTrip(query);
  }

  @Test
  public void testDeprecatedDefinitionInGroupBy() throws Exception {
    String query = "SELECT age FROM g MATCH (n) GROUP BY n.age AS age";
    checkRoundTrip(query);
  }

  @Test
  public void testModify() throws Exception {
    String query = "MODIFY/*beta*/ (" //
        + "  INSERT VERTEX v LABELS ( 'Person' ) PROPERTIES ( v.first_name = 'Scott', v.last_name = 'Tiger' ), " //
        + "         EDGE e BETWEEN v AND u LABELS ( 'Likes' ) PROPERTIES ( e.weight = 10 )" //
        + "  UPDATE u SET PROPERTIES ( u.first_name = 'Jane' ), " //
        + "         x SET PROPERTIES ( x.first_name = 'Bob' ) " //
        + "  DELETE w, e3" //
        + ")" //
        + "FROM g MATCH (u) -> (w) -[e3]-> (x)";
    checkRoundTrip(query);
  }

  @Test
  public void testCreatePropertyGraph() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON\n" + //
        "      KEY (ID)\n" + //
        "      LABEL PERSON PROPERTIES (NAME AS NAME, DOB AS DOB),\n" + //
        "    UNIVERSITY\n" + //
        "      KEY (ID)\n" + //
        "      LABEL UNIVERSITY PROPERTIES (NAME AS NAME) )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE KEY (PERSON1_ID) REFERENCES PERSON\n" + //
        "      DESTINATION KEY (PERSON2_ID) REFERENCES PERSON\n" + //
        "      LABEL KNOWS,\n" + //
        "    STUDENTOF\n" + //
        "      SOURCE KEY (PERSON_ID) REFERENCES PERSON\n" + //
        "      DESTINATION KEY (UNIVERSITY_ID) REFERENCES UNIVERSITY\n" + //
        "      LABEL STUDENTOF )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphSchemaQualifiedNames() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENTS.STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    STUDENTS.PERSON\n" + //
        "      KEY (ID)\n" + //
        "      LABEL PERSON PROPERTIES (NAME AS NAME, DOB AS DOB) )" + //
        "  EDGE TABLES (\n" + //
        "    STUDENTS.KNOWS\n" + //
        "      SOURCE KEY (PERSON1_ID) REFERENCES STUDENTS.PERSON\n" + //
        "      DESTINATION KEY (PERSON2_ID) REFERENCES STUDENTS.PERSON\n" + //
        "      LABEL KNOWS )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphDefaultLabel() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON\n" + //
        "      KEY (ID)\n" + //
        "      PROPERTIES (NAME AS NAME, DOB AS DOB) )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE KEY (PERSON1_ID) REFERENCES PERSON\n" + //
        "      DESTINATION KEY (PERSON2_ID) REFERENCES PERSON )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphNoProperties() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON\n" + //
        "      KEY (ID)\n" + //
        "      NO PROPERTIES )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE KEY (PERSON1_ID) REFERENCES PERSON\n" + //
        "      DESTINATION KEY (PERSON2_ID) REFERENCES PERSON\n" + //
        "    NO PROPERTIES )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphNoEdgeTables() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphNoVertexOrEdgeTables() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphNoKeys() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE PERSON\n" + //
        "      DESTINATION PERSON )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphPropertiesAreAllColumnsExcept() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON\n" + //
        "      KEY (ID)\n" + //
        "      LABEL PERSON PROPERTIES ALL COLUMNS,\n" + //
        "    UNIVERSITY\n" + //
        "      KEY (ID)\n" + //
        "      LABEL UNIVERSITY PROPERTIES ARE ALL COLUMNS EXCEPT ( ID, NAME ) )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE PERSON\n" + //
        "      DESTINATION PERSON\n" + //
        "      LABEL KNOWS PROPERTIES ARE ALL COLUMNS,\n" + //
        "    STUDENTOF\n" + //
        "      SOURCE PERSON\n" + //
        "      DESTINATION UNIVERSITY\n" + //
        "      LABEL STUDENTOF PROPERTIES ARE ALL COLUMNS EXCEPT ( PERSONID, UNIVERSITYID ) )";
    checkRoundTrip(statement);
  }

  private void checkRoundTrip(String query1) throws PgqlException {

    /*
     * First, assert that when parsing a query into a GraphQuery object and then pretty printing that GraphQuery object,
     * we obtain a string that is a valid PGQL query.
     */
    PgqlResult result1 = pgql.parse(query1);
    Statement iR1 = result1.getStatement();
    assertTrue(result1.getErrorMessages(), result1.isQueryValid() && iR1 != null);
    String query2 = iR1.toString();
    PgqlResult result2 = pgql.parse(query2);
    Statement iR2 = result2.getStatement();
    assertTrue(result2.getErrorMessages(), result2.isQueryValid() && iR2 != null);

    /*
     * Since pretty-printed queries are in normal form, we can now round trip endlessly. Here, we assert that when
     * pretty-printing a GraphQuery object that was parsed from a pretty-printed query, we obtain another GraphQuery
     * object that is equal to the first.
     */
    String query3 = iR2.toString();
    Statement iR3 = pgql.parse(query3).getStatement();
    assertEquals(iR2, iR3);
  }
}
