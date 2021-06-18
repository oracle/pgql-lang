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

  // test PATH, GROUP BY, ORDER BY, HAVING, aggregations, subqueries, INSERT/UPDATE/DELETE
}
