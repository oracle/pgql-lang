/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.PgqlStatement;

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
        + "LISTAGG(DISTINCT n.age, ';')," //
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
    String identifier = "\"\""; // ""
    String escapedIdentifier = "\"\\\"\"\"\""; // "\""""
    String query = "SELECT n." + escapedIdentifier + " FROM " + escapedIdentifier + " MATCH (n:" + escapedIdentifier
        + ")";
    SelectQuery selectQuery = (SelectQuery) pgql.parse(query).getPgqlStatement();

    PropertyAccess propertyAccess = (PropertyAccess) selectQuery.getProjection().getElements().get(0).getExp();
    assertEquals(identifier, propertyAccess.getPropertyName());

    assertEquals(identifier, selectQuery.getGraphName().getName());

    FunctionCall funcCall = (FunctionCall) selectQuery.getGraphPattern().getConstraints().iterator().next();
    assertEquals(funcCall.getFunctionName(), "has_label");
    String label = ((ConstString) funcCall.getArgs().get(1)).getValue();
    assertEquals(identifier, label);
  }

  @Test
  public void testIdentifierEscapingPgql12() throws Exception {
    String query = "PATH \"my path\" AS (\"vertex 1\") -[\"edge 1\"]-> (\"vertex 2\") " //
        + "SELECT n1.\"my prop\" AS \"some column name\"" //
        + "  FROM \"my graph\"" //
        + " MATCH (\"n1\":\"my label\") -/:\"my path\"*/-> (\"n 2\")";

    PgqlResult result1 = pgql.parse(query);
    assertTrue(result1.isQueryValid());

    String prettyPrintedQuery = result1.getGraphQuery().toString();

    PgqlResult result2 = pgql.parse(prettyPrintedQuery);
    assertTrue(result2.isQueryValid());
  }

  @Test
  public void testIdentifierEscapingPgql13() throws Exception {
    String query = "PATH \"my path\" AS (\"vertex 1\") -[\"edge 1\"]-> (\"vertex 2\") " //
        + "SELECT \"n1\".\"my prop\" AS \"some column name\"" //
        + "  FROM MATCH (\"n1\":\"my label\") -/:\"my path\"*/-> (\"n 2\") ON \"my graph\"";

    PgqlResult result1 = pgql.parse(query);
    assertTrue(result1.isQueryValid());

    String prettyPrintedQuery = result1.getGraphQuery().toString();

    PgqlResult result2 = pgql.parse(prettyPrintedQuery);
    assertTrue(result2.isQueryValid());
  }

  @Test
  public void testEscapingInCreatePropertyGraph() throws Exception {
    String statement = "CREATE PROPERTY GRAPH \"\n  \"\"  \t\" VERTEX TABLES ( \"\n  \"\"  \t\" AS \"\n \"\" \t\")";
    PgqlResult result1 = pgql.parse(statement);
    assertTrue(result1.isQueryValid());
    String prettyPrintedStatement = result1.getPgqlStatement().toString();
    PgqlResult result2 = pgql.parse(prettyPrintedStatement);
    assertEquals(result1.getPgqlStatement(), result2.getPgqlStatement());
  }

  @Test
  public void testEscapingInInsert() throws Exception {
    String statement = "INSERT INTO \"graph\n\\n  \"\"  \t\" VERTEX \"vertex\n\\n  \"\"  \t\" " //
        + "LABELS ( \"label\n\\n  \"\"  \t\" ) " //
        + "PROPERTIES ( \"vertex\n\\n  \"\"  \t\".\"property\n  \"\"  \t\" =  'value''\n\\n\t\"\"'), " //
        + "EDGE \"edge\n\\n  \"\"  \t\" BETWEEN \"vertex\n\\n  \"\"  \t\" AND \"vertex\n\\n  \"\"  \t\"";
    PgqlResult result1 = pgql.parse(statement);
    assertTrue(result1.isQueryValid());
    String prettyPrintedStatement = result1.getPgqlStatement().toString();
    PgqlResult result2 = pgql.parse(prettyPrintedStatement);
    assertEquals(result1.getPgqlStatement(), result2.getPgqlStatement());
  }

  @Test
  public void testEscapingInUpdate() throws Exception {
    String statement = "UPDATE \"vertex\n\\n  \"\"  \t\" " //
        + "SET ( \"vertex\n\\n  \"\"  \t\".\"property\n\\n  \"\"  \t\" =  'value''\n\\n\t\"\"' ) " //
        + "FROM MATCH (\"vertex\n\\n  \"\"  \t\")";
    PgqlResult result1 = pgql.parse(statement);
    assertTrue(result1.isQueryValid());
    String prettyPrintedStatement = result1.getPgqlStatement().toString();
    PgqlResult result2 = pgql.parse(prettyPrintedStatement);
    assertEquals(result1.getPgqlStatement(), result2.getPgqlStatement());
  }

  @Test
  public void testEscapingInDelete() throws Exception {
    String statement = "DELETE \"vertex\n\\n  \"\"  \t\"" //
        + "FROM MATCH (\"vertex\n\\n  \"\"  \t\")";
    PgqlResult result1 = pgql.parse(statement);
    assertTrue(result1.isQueryValid());
    String prettyPrintedStatement = result1.getPgqlStatement().toString();
    PgqlResult result2 = pgql.parse(prettyPrintedStatement);
    assertEquals(result1.getPgqlStatement(), result2.getPgqlStatement());
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
  public void testAnyShortest() throws Exception {
    String query = "SELECT 1 FROM MATCH ANY SHORTEST ( (n) -[e]->* (m) )";
    checkRoundTrip(query);
  }

  @Test
  public void testAnyCheapest() throws Exception {
    String query = "SELECT 1 FROM MATCH ANY CHEAPEST ( (n) (-[e]-> COST e.cost)* (m) )";
    checkRoundTrip(query);
  }

  @Test
  public void testAllShortest() throws Exception {
    String query = "SELECT 1 FROM MATCH ALL SHORTEST ( (n) -[e]->* (m) )";
    checkRoundTrip(query);
  }

  @Test
  public void testAnyPath() throws Exception {
    String query = "SELECT LISTAGG(e2.prop, ',') FROM MATCH ANY (n) ->* (m), MATCH ANY ( (n) <-[e2]-* (m) )";
    checkRoundTrip(query);
  }

  @Test
  public void testAllPath() throws Exception {
    String query = "SELECT LISTAGG(e2.prop, ',') FROM MATCH ALL (n) ->{3} (m), MATCH ALL ( (n) <-[e2]-{,2} (m) )";
    checkRoundTrip(query);
  }

  @Test
  public void testDeprecatedDefinitionInGroupBy() throws Exception {
    String query = "SELECT age FROM g MATCH (n) GROUP BY n.age AS age";
    checkRoundTrip(query);
  }

  @Test
  public void testModify() throws Exception {
    String query = //
        "INSERT VERTEX v LABELS ( Person ) PROPERTIES ( v.first_name = 'Scott', v.last_name = 'Tiger' ), " //
            + "       EDGE e BETWEEN v AND u LABELS ( Likes ) PROPERTIES ( e.weight = 10 ) " //
            + "UPDATE u SET ( u.first_name = 'Jane' ), " //
            + "       x SET ( x.first_name = 'Bob' ) " //
            + "DELETE w, e3 " //
            + "FROM MATCH (u) -> (w) -[e3]-> (x)";
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
        "      KEY (EDGE_ID)\n" + //
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
        "      SOURCE KEY (PERSON1_ID) REFERENCES PERSON\n" + //
        "      DESTINATION KEY (PERSON2_ID) REFERENCES PERSON\n" + //
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

  @Test
  public void testCreatePropertyGraphEdgeKeys() throws Exception {
    String statement = "CREATE PROPERTY GRAPH g\n" + //
        "  VERTEX TABLES (\n" + //
        "    Person\n" + //
        "  )\n" + //
        "  EDGE TABLES (\n" + //
        "    knows KEY ( person1, person2 ) SOURCE KEY ( person ) REFERENCES Person DESTINATION KEY ( person ) REFERENCES Person\n"
        + "  )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphDoubleQuotedIdentifiers1() throws Exception {
    String statement = "CREATE PROPERTY GRAPH \"my graph\"\n" + //
        "  VERTEX TABLES ( \"my vt\" )\n" + //
        "  EDGE TABLES ( \"my et\" SOURCE \"my vt\" DESTINATION \"my vt\" )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphDoubleQuotedIdentifiers2() throws Exception {
    String statement = "CREATE PROPERTY GRAPH \"my graph\"\n" + //
        "  VERTEX TABLES (\n" + //
        "    \"my vt\"\n" + //
        "      LABEL \"my vl\"\n" + //
        "      PROPERTIES ( \"my column 1\", \"my column 2\" AS \"my property 2\" )\n" + //
        "  )\n" + //
        "  EDGE TABLES (\n" + //
        "    \"my et\"\n" + //
        "      SOURCE \"my vt\"\n" + //
        "      DESTINATION \"my vt\"\n" + //
        "      LABEL \"my el\"\n" + //
        "  )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphWithAliases() throws Exception {
    String statement = "CREATE PROPERTY GRAPH hr\n" + //
        "  VERTEX TABLES (\n" + //
        "    employees as \"Employee\",\n" + //
        "    departments Department\n" + //
        "  )\n" + //
        "  EDGE TABLES (\n" + //
        "    employees AS manages SOURCE \"Employee\" DESTINATION \"Employee\",\n" + //
        "    employees AS worksFor SOURCE \"Employee\" DESTINATION Department\n" + //
        "  )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCastInCreatePropertyGraph() throws Exception {
    String statement = "CREATE PROPERTY GRAPH g VERTEX TABLES ( Person PROPERTIES ( CAST ( n AS STRING ) AS n ) )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphPgView() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  ORGANIZATION PG_VIEW\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE PERSON\n" + //
        "      DESTINATION PERSON )";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreatePropertyGraphPgSchema() throws Exception {
    String statement = "CREATE PROPERTY GRAPH STUDENT_NETWORK\n" + //
        "  ORGANIZATION PG_SCHEMA\n" + //
        "  VERTEX TABLES (\n" + //
        "    PERSON )\n" + //
        "  EDGE TABLES (\n" + //
        "    KNOWS\n" + //
        "      SOURCE PERSON\n" + //
        "      DESTINATION PERSON )";
    checkRoundTrip(statement);
  }

  @Test
  public void testDropPropertyGraph() throws Exception {
    String statement = "DROP PROPERTY GRAPH myGraph";
    checkRoundTrip(statement);
  }

  @Test
  public void testDropPropertyGraphWithQuotedIdentifiers() throws Exception {
    String statement = "DROP PROPERTY GRAPH \"my schema\".\"my graph\"";
    checkRoundTrip(statement);
  }

  @Test
  public void testSchemaQualifiedNames() throws Exception {
    String statement = "INSERT INTO scott.socialNetwork VERTEX v\n" //
        + "FROM MATCH (n:Person) ON Scott.SocialNetwork";
    checkRoundTrip(statement);
  }

  @Test
  public void testCaseStatement() throws Exception {
    String statement = "SELECT CASE n.prop WHEN 1 THEN 'a' WHEN 2 THEN 'b' ELSE 'c' END " //
        + "                  , CASE n.prop WHEN 1 THEN 'a' WHEN 2 THEN 'b' END " //
        + "                  , CASE WHEN n.prop < 1 THEN 'a' ELSE 'b' END " //
        + "                  , CASE WHEN n.prop < 1 THEN 'a' END " //
        + "         MATCH (n)";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreateExternalSchema1() throws Exception {
    String statement = "CREATE EXTERNAL SCHEMA HR\n"
        + "FROM DATABASE URL 'jdbc:oracle:thin@myhost:1521/oracl' USER 'hr' KEYSTORE_ALIAS 'hr_alias'";
    checkRoundTrip(statement);
  }

  @Test
  public void testCreateExternalSchema2() throws Exception {
    String statement = "CREATE EXTERNAL SCHEMA HR\n" //
        + "FROM DATABASE DATA_SOURCE 'my data source' SCHEMA 'HR'";
    checkRoundTrip(statement);
  }

  @Test
  public void testDropExternalSchema() throws Exception {
    String statement = "DROP EXTERNAL SCHEMA \" my schema \"";
    checkRoundTrip(statement);
  }

  private void checkRoundTrip(String query1) throws PgqlException {

    /*
     * First, assert that when parsing a query into a GraphQuery object and then pretty printing that GraphQuery object,
     * we obtain a string that is a valid PGQL query.
     */
    PgqlResult result1 = pgql.parse(query1);
    PgqlStatement iR1 = result1.getPgqlStatement();
    System.out.println(iR1);
    assertTrue(result1.getErrorMessages(), result1.isQueryValid() && iR1 != null);
    String query2 = iR1.toString();
    PgqlResult result2 = pgql.parse(query2);
    PgqlStatement iR2 = result2.getPgqlStatement();
    assertTrue(result2.getErrorMessages(), result2.isQueryValid() && iR2 != null);

    /*
     * Since pretty-printed queries are in normal form, we can now round trip endlessly. Here, we assert that when
     * pretty-printing a GraphQuery object that was parsed from a pretty-printed query, we obtain another GraphQuery
     * object that is equal to the first.
     */
    String query3 = iR2.toString();
    PgqlStatement iR3 = pgql.parse(query3).getPgqlStatement();
    assertEquals(iR2, iR3);
  }
}
