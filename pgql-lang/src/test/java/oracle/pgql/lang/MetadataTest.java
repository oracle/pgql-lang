/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MetadataTest extends AbstractPgqlTest {

  private PgqlResult parse(String query) throws Exception {
    return pgql.parse(query, new ExampleMetadataProvider());
  }

  @Test
  public void testVertexLabel() throws Exception {
    PgqlResult result = parse("SELECT * FROM MATCH (n:NotExists)");
    assertTrue(result.getErrorMessages(), result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("SELECT * FROM MATCH (n:\"Person\")");
    assertTrue(result.isQueryValid());

    // test case insensitive matching
    result = parse("SELECT * FROM MATCH (n:Person)");
  }

  @Test
  public void testEdgeLabel() throws Exception {
    PgqlResult result = parse("SELECT * FROM MATCH () -[e:NotExists]-> ()");
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = parse("SELECT * FROM MATCH () -[e:\"knows\"]-> ()");
    assertTrue(result.isQueryValid());

    // test case insensitive matching
    result = parse("SELECT * FROM MATCH () -[e:knows]-> ()");
  }

  @Test
  public void testVertexProperty() throws Exception {
    PgqlResult result = parse("SELECT n.\"firstNme\" FROM MATCH (n:\"Person\")");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT n.firstName FROM MATCH (n:Person)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT n.firstName FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT n.firstNme FROM MATCH (n:Person)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT n.firstNme FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testEdgeProperty() throws Exception {
    PgqlResult result = parse("SELECT e.\"xyz\" FROM MATCH () -[e:\"knows\"]-> ()");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT e.since FROM MATCH () -[e:knows]-> ()");
    assertTrue(result.isQueryValid());

    result = parse("SELECT e.since FROM MATCH () -[e]-> ()");
    assertTrue(result.isQueryValid());

    result = parse("SELECT e.xyz FROM MATCH () -[e:knows]-> ()");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT e.xyz FROM MATCH () -[e]-> ()");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testLabelDisjunction() throws Exception {
    PgqlResult result = parse("SELECT n.firstName FROM MATCH (n:Person|NotExists)");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("SELECT n.firstName FROM MATCH (n:Person|University)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT e.since FROM MATCH () -[e:knows|notExists]-> ()");
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = parse("SELECT e.since FROM MATCH () -[e:knows|studyAt]-> ()");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testInsertUpdate() throws Exception {
    PgqlResult result = parse("INSERT VERTEX v LABELS ( Person ) PROPERTIES ( v.firstName = 'Pete' )");
    assertTrue(result.isQueryValid());

    result = parse("INSERT VERTEX v LABELS ( Person2 ) PROPERTIES ( v.firstName = 'Pete' )");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("INSERT VERTEX v LABELS ( Person ), EDGE e BETWEEN v AND v LABELS ( knows )");
    assertTrue(result.isQueryValid());

    result = parse("INSERT VERTEX v LABELS ( Person ), EDGE e BETWEEN v AND v LABELS ( knows2 )");
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = parse("UPDATE n SET ( n.firstName = 'Larry' ) FROM MATCH (n:Person)");
    assertTrue(result.isQueryValid());

    result = parse("UPDATE n SET ( n.notExists = 'Larry' ) FROM MATCH (n:Person)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("UPDATE e SET ( e.since = DATE '2000-01-01' ) FROM MATCH () -[e:knows]-> ()");
    assertTrue(result.isQueryValid());

    result = parse("UPDATE e SET ( e.notExists = DATE '2000-01-01' ) FROM MATCH () -[e:knows]-> ()");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testUserDefinedGraphName() throws Exception {
    PgqlResult result = parse(//
        "SELECT p.name, a.number, c.name AS company, t.amount " //
            + "FROM MATCH (p:Person) -[:owner]-> (a:Account) -[t:transaction]-> (b:Account) ON financialNetwork, " //
            + "MATCH (p:Person) -[:worksFor]-> (c:Company) ON financialNetwork");
    assertTrue(result.isQueryValid());

    result = parse("SELECT * FROM MATCH (:notExists) ON financialNetwork");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("SELECT * FROM MATCH () -[:notExists]-> () ON financialNetwork");
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = parse("SELECT * FROM MATCH (:notExists) ON \"financialNetwork\"");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("SELECT n.notExists FROM MATCH (n) ON financialNetwork");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT e.notExists FROM MATCH () -[e]-> () ON \"financialNetwork\"");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("INSERT INTO financialNetwork VERTEX v LABELS ( Account )");
    assertTrue(result.isQueryValid());

    result = parse("INSERT INTO financialNetwork VERTEX v LABELS ( Account2 )");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));
  }

  @Test
  public void testTypeCheckingInPathMacro() throws Exception {
    PgqlResult result = parse("PATH p AS (p:Person) -[study:studyAt]-> (:University) " //
        + "WHERE p.firstName = 'Alice' AND study.since > DATE '2000-01-01'" //
        + "SELECT COUNT(*) FROM MATCH () -/:p*/-> ()");
    assertTrue(result.isQueryValid());

    result = parse("PATH p AS (:University2) -> () SELECT COUNT(*) FROM MATCH () -/:p*/-> ()");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("PATH p AS () -[:studyAt2]-> () SELECT COUNT(*) FROM MATCH () -/:p*/-> ()");
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = parse("PATH p AS (n) -[]-> () WHERE n.notExists = 3 SELECT COUNT(*) FROM MATCH () -/:p*/-> ()");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("PATH p AS () -[e]-> () WHERE e.notExists = 3 SELECT COUNT(*) FROM MATCH () -/:p*/-> ()");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testTypeCheckingForGroupVariables() throws Exception {
    PgqlResult result = parse("SELECT MIN(x.firstName), MAX(e.since), MIN(x.dob) " //
        + "FROM MATCH ANY SHORTEST (n) (" //
        + "  (x) -[e]-> (y) WHERE x.dob > DATE '2000-01-01' AND e.since > DATE '2000-01-01' AND y.dob > DATE '2000-01-01'" //
        + ")* (m)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT MIN(x.firstName), MAX(e.since), MIN(x.dob) " //
        + "FROM MATCH ALL (n) (" //
        + "  (x:Person) -[e:knows]-> (y:Person) WHERE x.dob > DATE '2000-01-01' AND e.since > DATE '2000-01-01' AND y.dob > DATE '2000-01-01'" //
        + "){1,4} (m)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT MIN(x.firstName) " //
        + "FROM MATCH ANY (n) (" //
        + "  (x:Person2) -[e]-> (y)" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = parse("SELECT MIN(x.firstName) " //
        + "FROM MATCH ANY SHORTEST (n) (" //
        + "  (x) -[e:knows2]-> (y)" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = parse("SELECT MIN(x.firstName2) " //
        + "FROM MATCH ALL SHORTEST (n) (" //
        + "  (x) -[e]-> (y)" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT MAX(e.since2) " //
        + "FROM MATCH ANY SHORTEST (n) (" //
        + "  (x) -[e]-> (y)" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT MIN(y.dob2) " //
        + "FROM MATCH ANY SHORTEST (n) (" //
        + "  (x) -[e]-> (y)" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 123 " //
        + "FROM MATCH ANY SHORTEST (n) (" //
        + "  (x:Person) -[e]-> (y) WHERE x.name > 'a'" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 123 " //
        + "FROM MATCH ANY SHORTEST (n) (" //
        + "  (x) -[e:knows]-> (y) WHERE x.studyAt > DATE '2000-01-01'" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 123 " //
        + "FROM MATCH ANY CHEAPEST (n) (" //
        + "  (x) -[e:knows]-> (y) COST x.studyAt > DATE '2000-01-01'" //
        + ")* (m)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testPropertyAccessInWhereGroupByHavingOrderByAggregation() throws Exception {
    PgqlResult result = parse("SELECT 1 FROM MATCH (u:University) WHERE u.firstName = 'Jack'");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 1 FROM MATCH (u:University) GROUP BY u.firstName");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT u.firstName FROM MATCH (u:University) GROUP BY u");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT x.firstName FROM MATCH (u:University) GROUP BY u AS x");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT MIN(u.firstName) FROM MATCH (u:University)");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT MIN(u.firstName) FROM MATCH (u:University) GROUP BY u.name");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 1 FROM MATCH (u:University) ORDER BY u.firstName");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 1 FROM MATCH (u:University) GROUP BY u.name HAVING MIN(u.firstName) > ''");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT 1 FROM MATCH (u:University) GROUP BY u AS x HAVING x.dob > DATE '2000-01-01'");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = parse("SELECT x AS y FROM MATCH (u:University) GROUP BY u AS x ORDER BY y.firstName");
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testNonBooleanInWhereClause() throws Exception {
    PgqlResult result = parse("SELECT * FROM MATCH (n) WHERE true");
    assertTrue(result.isQueryValid());

    result = parse("SELECT * FROM MATCH (n) WHERE 1");
    assertTrue(result.getErrorMessages().contains("WHERE clause expects a BOOLEAN expression"));

    result = parse("SELECT * FROM MATCH (n) WHERE 1.2");
    assertTrue(result.getErrorMessages().contains("WHERE clause expects a BOOLEAN expression"));

    result = parse("SELECT * FROM MATCH (n) WHERE 'abc'");
    assertTrue(result.getErrorMessages().contains("WHERE clause expects a BOOLEAN expression"));

    result = parse("PATH p AS () -> () WHERE 1 SELECT * FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("WHERE clause expects a BOOLEAN expression"));

    result = parse("SELECT 1 FROM MATCH ANY (n) (-[e]-> WHERE 1)* (m)");
    assertTrue(result.getErrorMessages().contains("WHERE clause expects a BOOLEAN expression"));
  }

  @Test
  public void testNonBooleanInHavingClause() throws Exception {
    PgqlResult result = parse("SELECT n.name FROM MATCH (n) GROUP BY n HAVING true");
    assertTrue(result.isQueryValid());

    result = parse("SELECT n.name  FROM MATCH (n) GROUP BY n HAVING 123");
    assertTrue(result.getErrorMessages().contains("HAVING clause expects a BOOLEAN expression"));
  }

  @Test
  public void testNonNumericInCostClause() throws Exception {
    PgqlResult result = parse("SELECT 1 FROM MATCH ANY CHEAPEST (n) (-[e]-> COST 1.23)* (m)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT 1 FROM MATCH ANY CHEAPEST (n) (-[e]-> COST 1)* (m)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT 1 FROM MATCH ANY CHEAPEST (n) (-[e]-> COST true)* (m)");
    assertTrue(result.getErrorMessages().contains("COST clause expects a numeric expression"));

    result = parse("SELECT 1 FROM MATCH ANY CHEAPEST (n) (-[e]-> COST 'abc')* (m)");
    assertTrue(result.getErrorMessages().contains("COST clause expects a numeric expression"));

    result = parse("SELECT 1 FROM MATCH ANY CHEAPEST (n) (-[e]-> COST DATE '2000-01-01')* (m)");
    assertTrue(result.getErrorMessages().contains("COST clause expects a numeric expression"));
  }

  @Test
  public void testInPredicateAndUnionTyping() throws Exception {
    // mix of LONG and DOUBLE
    PgqlResult result = parse("SELECT 1 IN ( 1.0, 3, 1.2) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    // mix of INTEGER, LONG and DOUBLE
    result = parse("SELECT 1 IN ( n.numericProp, 1.0, 3) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    // mix of INTEGER, LONG and DOUBLE
    result = parse("SELECT n.numericProp IN ( 1, 1.0, 3) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    // mix of TIME WITH TTIME ZONE and TIME
    result = parse("SELECT TIME '12:30:00+08:00' IN ( TIME '12:30:00' ) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    // mix of TIMESTAMP WITH TTIME ZONE and TIMESTAMP
    result = parse(
        "SELECT TIMESTAMP '2000-01-01 12:30:00+08:00' IN ( TIMESTAMP '2000-01-01 12:30:00' ) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT 1 IN ( true ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages()
        .contains("The IN predicate is undefined for left-hand operand type LONG and list value type BOOLEAN"));

    result = parse("SELECT TIMESTAMP '2000-01-01 12:30:00+08:00' IN ( true ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains(
        "The IN predicate is undefined for left-hand operand type TIMESTAMP WITH TIME ZONE and list value type BOOLEAN"));

    result = parse("SELECT TIMESTAMP '2000-01-01 12:30:00' IN ( DATE '2000-01-01' ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages()
        .contains("The IN predicate is undefined for left-hand operand type TIMESTAMP and list value type DATE"));

    result = parse("SELECT true IN ( true, DATE '2000-01-01' ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with BOOLEAN expected"));

    result = parse("SELECT 123 IN ( 456, true ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with LONG expected"));

    result = parse("SELECT 123 IN ( n.numericProp, true ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DOUBLE expected"));

    result = parse("SELECT 123 IN ( n.numericProp, true ) FROM MATCH (n:University)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DOUBLE expected"));

    result = parse("SELECT 123 IN ( n.numericProp, true ) FROM MATCH (n:Person)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with INTEGER expected"));

    result = parse("SELECT n.dob IN ( n.dob, true ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DATE expected"));

    result = parse("SELECT n.dob IN ( DATE '2000-01-01', true ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DATE expected"));

    result = parse("SELECT 'abc' IN ( n.numericProp ) FROM MATCH (n)");
    assertTrue(result.getErrorMessages()
        .contains("The IN predicate is undefined for left-hand operand type STRING and list value type DOUBLE"));
  }

  @Test
  public void testAmbiguousPropertyType() throws Exception {
    PgqlResult result = parse("SELECT n.typeConflictProp FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Property has incompatible types for different labels"));

    result = parse("SELECT n.typeConflictProp FROM MATCH (n:Person|University)");
    assertTrue(result.getErrorMessages().contains("Property has incompatible types for different labels"));

    result = parse("SELECT n.typeConflictProp FROM MATCH (n:Person)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT n.typeConflictProp FROM MATCH (n:University)");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testSamePropertyWhenUpperCased() throws Exception {
    PgqlResult result = parse("SELECT e.prop FROM MATCH () -[e]-> ()");
    assertTrue(result.isQueryValid());

    result = parse("SELECT e.prop IN (1) FROM MATCH () -[e]-> ()");
    assertTrue(result.getErrorMessages().contains(
        "The IN predicate is undefined for left-hand operand type TIME WITH TIME ZONE and list value type LONG"));

    result = parse("SELECT e.prop FROM MATCH () -[e:knows|studyAt]-> ()");
    assertTrue(result.isQueryValid());

    result = parse("SELECT e.prop IN (1) FROM MATCH () -[e]-> ()");
    assertTrue(result.getErrorMessages().contains(
        "The IN predicate is undefined for left-hand operand type TIME WITH TIME ZONE and list value type LONG"));
  }

  @Test
  public void testCaseStatement() throws Exception {
    PgqlResult result = parse("SELECT CASE WHEN 'a' = 'a' THEN n.firstName ELSE 'abc' END FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT CASE WHEN 'a' THEN n.firstName ELSE 'abc' END FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("BOOLEAN expression expected"));

    result = parse("SELECT CASE WHEN true THEN n.numericProp ELSE 'abc' END FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DOUBLE expected"));

    result = parse("SELECT CASE WHEN true THEN n.firstName ELSE n.dob END FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with STRING expected"));

    result = parse("SELECT CASE WHEN true THEN n.dob ELSE n.firstName END FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DATE expected"));

    result = parse(
        "SELECT CASE WHEN true THEN n.dob WHEN n.firstName IS NULL THEN DATE '1970-01-01' ELSE n.firstName END FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Expression of a type compatible with DATE expected"));

    result = parse("SELECT CASE n.firstName WHEN 123 THEN true ELSE false END FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator = is undefined for the argument types STRING, LONG"));
  }

  @Test
  public void testDataTypeSynonyms() throws Exception {
    PgqlResult result = parse("SELECT CAST('1' AS INT) = CAST('1' AS INTEGER) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT CAST('1' AS INT) = CAST('1' AS STRING) FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator = is undefined for the argument types INTEGER, STRING"));
  }

  @Test
  public void testCastToTypeNotInGraph() throws Exception {
    // none of the properties in the graph have type FLOAT but we still figure out that FLOAT is compatible with INTEGER
    PgqlResult result = parse("SELECT CAST('1' AS FLOAT) = CAST('1' AS INTEGER) FROM MATCH (n)");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testBinaryOperations() throws Exception {
    PgqlResult result = parse("SELECT 1 = 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator = is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 < 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator < is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 <= 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator <= is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 <> 'abc' FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator <> (or !=) is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 != 'abc' FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator <> (or !=) is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 >= 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator >= is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 > 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator > is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 || 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator || is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 + 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator + is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 - 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator - is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 / 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator / is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 * 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator * is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 % 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator % is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 AND 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator AND is undefined for the argument types LONG, STRING"));

    result = parse("SELECT 1 OR 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator OR is undefined for the argument types LONG, STRING"));
  }

  @Test
  public void testUnaryOperations() throws Exception {
    PgqlResult result = parse("SELECT NOT 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator NOT is undefined for the argument type STRING"));

    result = parse("SELECT -'abc' FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator - (unary minus) is undefined for the argument type STRING"));

    result = parse("SELECT -n.numericProp = 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator = is undefined for the argument types DOUBLE, STRING"));

    result = parse("SELECT SUM('abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The aggregate SUM is undefined for the argument type STRING"));

    result = parse("SELECT SUM(CAST(n.numericProp AS FLOAT)) AND SUM(n.numericProp) FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator AND is undefined for the argument types DOUBLE, DOUBLE"));

    result = parse("SELECT SUM(CAST(n.numericProp AS INTEGER)) AND SUM(n.numericProp) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator AND is undefined for the argument types LONG, DOUBLE"));

    result = parse("SELECT MIN(n) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The aggregate MIN is undefined for the argument type VERTEX"));

    result = parse("SELECT MIN(1) AND MIN('abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator AND is undefined for the argument types LONG, STRING"));

    result = parse("SELECT MIN(true) AND MIN(false) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT MAX(n) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The aggregate MAX is undefined for the argument type VERTEX"));

    result = parse("SELECT MAX(1) AND MAX('abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator AND is undefined for the argument types LONG, STRING"));

    result = parse("SELECT MAX(true) AND MAX(false) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT AVG('abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The aggregate AVG is undefined for the argument type STRING"));

    result = parse("SELECT AVG(123) + 'abc' FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator + is undefined for the argument types DOUBLE, STRING"));

    result = parse("SELECT ARRAY_AGG(n) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The aggregate ARRAY_AGG is undefined for the argument type VERTEX"));

    result = parse("SELECT ARRAY_AGG(n.firstName) = ARRAY_AGG(n.dob) FROM MATCH (n)");
    assertTrue(result.getErrorMessages()
        .contains("The operator = is undefined for the argument types ARRAY<STRING>, ARRAY<DATE>"));

    result = parse("SELECT ARRAY_AGG(n.firstName) = ARRAY_AGG('abc') FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT LISTAGG(n) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The aggregate LISTAGG is undefined for the argument type VERTEX"));

    result = parse("SELECT LISTAGG(n.firstName) <> LISTAGG(n.dob) AND LISTAGG(n.dob) <> 'abc' FROM MATCH (n)");
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testIsNull() throws Exception {
    PgqlResult result = parse("SELECT (n.firstName IS NULL) + (n.firstName IS NOT NULL) FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator + is undefined for the argument types BOOLEAN, BOOLEAN"));
  }

  @Test
  public void testLiterals() throws Exception {
    PgqlResult result = parse("SELECT DATE '2000-01-01' + true FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator + is undefined for the argument types DATE, BOOLEAN"));

    result = parse("SELECT TIME '19:30:00' + 123 FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator + is undefined for the argument types TIME, LONG"));

    result = parse("SELECT TIMESTAMP '2000-01-01 19:30:00' + 'abc' FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator + is undefined for the argument types TIMESTAMP, STRING"));

    result = parse("SELECT TIME '19:30:00+08:00' + TIMESTAMP '2000-01-01 19:30:00+08:00' FROM MATCH (n)");
    assertTrue(result.getErrorMessages()
        .contains("The operator + is undefined for the argument types TIME WITH TIME ZONE, TIMESTAMP WITH TIME ZONE"));
  }

  @Test
  public void testSubqueries() throws Exception {
    PgqlResult result = parse("SELECT ( SELECT x FROM MATCH (n) GROUP BY n.firstName AS x ) + 123 FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("The operator + is undefined for the argument types STRING, LONG"));

    result = parse(
        "SELECT EXISTS ( SELECT * FROM MATCH (n) -> (m) ) + NOT EXISTS ( SELECT * FROM MATCH (n) -> (m) ) FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator + is undefined for the argument types BOOLEAN, BOOLEAN"));
  }

  @Test
  public void testExtract() throws Exception {
    PgqlResult result = parse(
        "SELECT EXTRACT(YEAR FROM DATE '2000-01-01') || EXTRACT(MONTH FROM DATE '2000-01-01') FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types INTEGER, INTEGER"));

    result = parse(
        "SELECT EXTRACT(DAY FROM DATE '2000-01-01') || EXTRACT(HOUR FROM TIME '20:30:00+08:00') FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types INTEGER, INTEGER"));

    result = parse(
        "SELECT EXTRACT(MINUTE FROM TIME '20:30:00+08:00') || EXTRACT(SECOND FROM TIME '20:30:00+08:00') FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types INTEGER, DOUBLE"));

    result = parse(
        "SELECT EXTRACT(TIMEZONE_HOUR FROM TIME '20:30:00+08:00') || EXTRACT(TIMEZONE_MINUTE FROM TIME '20:30:00+08:00') FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types INTEGER, INTEGER"));
  }

  @Test
  public void testSubstring() throws Exception {
    PgqlResult result = parse("SELECT SUBSTRING('A string' FROM 3 FOR 2) || true FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types STRING, BOOLEAN"));

    result = parse("SELECT SUBSTRING(DATE '2000-01-01' FROM 3 FOR 2) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Character string expected"));

    result = parse("SELECT SUBSTRING('A string' FROM 'abc' FOR 2) FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Numeric expected"));

    result = parse("SELECT SUBSTRING('A string' FROM 3 FOR 'abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Numeric expected"));
  }

  @Test
  public void testAllDifferent() throws Exception {
    PgqlResult result = parse("SELECT ALL_DIFFERENT(n, m) FROM MATCH (n) -[e]-> (m)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT ALL_DIFFERENT(n, e) FROM MATCH (n) -[e]-> (m)");
    assertTrue(result.getErrorMessages().contains("Function does not exist or argument types do not match"));

    result = parse("SELECT ALL_DIFFERENT() FROM MATCH (n) -[e]-> (m)");
    assertTrue(result.getErrorMessages().contains("Function does not exist or argument types do not match"));
  }

  @Test
  public void testUdfs() throws Exception {
    PgqlResult result = parse("SELECT myUdfs.pi() || true FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types DOUBLE, BOOLEAN"));

    result = parse("SELECT pi('abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Function does not exist or argument types do not match"));

    result = parse("SELECT myUdfs.pi() + 3 + myUdfs.numericFunction(4) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT myUdfs.numericFunction('abc') FROM MATCH (n)");
    assertTrue(result.getErrorMessages().contains("Function does not exist or argument types do not match"));

    // test union typing
    result = parse(
        "SELECT myUdfs.numericFunction(CAST(4 AS INTEGER)), myUdfs.numericFunction(CAST(4 AS FLOAT)), myUdfs.numericFunction(4.0) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    // test exact function matching
    result = parse(
        "SELECT myUdfs.\"numericFunction\"(CAST(4 AS INTEGER)), \"myUdfs\".numericFunction(CAST(4 AS FLOAT)), \"myUdfs\".\"numericFunction\"(4.0) FROM MATCH (n)");
    assertTrue(result.isQueryValid());

    result = parse("SELECT \"MyUdFS\".\"AmBiguousFuNCtion\"(123) || true FROM MATCH (n)");
    assertTrue(
        result.getErrorMessages().contains("The operator || is undefined for the argument types DOUBLE, BOOLEAN"));

    result = parse("SELECT myUdfs.ambiguousFunction(123)FROM MATCH (n)");
    assertTrue(result.getErrorMessages()
        .contains("Multiple functions exist that match the specified function name and argument types"));
  }
}
