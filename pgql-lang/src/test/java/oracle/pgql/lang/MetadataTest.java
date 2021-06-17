/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MetadataTest extends AbstractPgqlTest {

  @Test
  public void testVertexLabel() throws Exception {
    PgqlResult result = pgql.parse("SELECT * FROM MATCH (n:NotExists)", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages(), result.getErrorMessages().contains("Vertex label does not exist"));

    result = pgql.parse("SELECT * FROM MATCH (n:\"Person\")", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    // test case insensitive matching
    result = pgql.parse("SELECT * FROM MATCH (n:Person)", new ExampleMetadataProvider());
  }

  @Test
  public void testEdgeLabel() throws Exception {
    PgqlResult result = pgql.parse("SELECT * FROM MATCH () -[e:NotExists]-> ()", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = pgql.parse("SELECT * FROM MATCH () -[e:\"knows\"]-> ()", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    // test case insensitive matching
    result = pgql.parse("SELECT * FROM MATCH () -[e:knows]-> ()", new ExampleMetadataProvider());
  }

  @Test
  public void testVertexProperty() throws Exception {
    PgqlResult result = pgql.parse("SELECT n.\"firstNme\" FROM MATCH (n:\"Person\")", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = pgql.parse("SELECT n.firstName FROM MATCH (n:Person)", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    result = pgql.parse("SELECT n.firstName FROM MATCH (n)", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    result = pgql.parse("SELECT n.firstNme FROM MATCH (n:Person)", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = pgql.parse("SELECT n.firstNme FROM MATCH (n)", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testEdgeProperty() throws Exception {
    PgqlResult result = pgql.parse("SELECT e.\"xyz\" FROM MATCH () -[e:\"knows\"]-> ()", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = pgql.parse("SELECT e.since FROM MATCH () -[e:knows]-> ()", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    result = pgql.parse("SELECT e.since FROM MATCH () -[e]-> ()", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    result = pgql.parse("SELECT e.xyz FROM MATCH () -[e:knows]-> ()", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = pgql.parse("SELECT e.xyz FROM MATCH () -[e]-> ()", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }

  @Test
  public void testLabelDisjunction() throws Exception {
    PgqlResult result = pgql.parse("SELECT n.firstName FROM MATCH (n:Person|NotExists)", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = pgql.parse("SELECT n.firstName FROM MATCH (n:Person|University)", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    result = pgql.parse("SELECT e.since FROM MATCH () -[e:knows|notExists]-> ()", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = pgql.parse("SELECT e.since FROM MATCH () -[e:knows|studyAt]-> ()", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());
  }

  @Test
  public void testUserDefinedGraphName() throws Exception {
    PgqlResult result = pgql.parse(//
        "SELECT p.name, a.number, c.name AS company, t.amount " //
            + "FROM MATCH (p:Person) -[:owner]-> (a:Account) -[t:transaction]-> (b:Account) ON financialNetwork, " //
            + "MATCH (p:Person) -[:worksFor]-> (c:Company) ON financialNetwork",
        new ExampleMetadataProvider());

    result = pgql.parse("SELECT * FROM MATCH (:notExists) ON financialNetwork", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = pgql.parse("SELECT * FROM MATCH () -[:notExists]-> () ON financialNetwork", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Edge label does not exist"));

    result = pgql.parse("SELECT * FROM MATCH (:notExists) ON \"financialNetwork\"", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Vertex label does not exist"));

    result = pgql.parse("SELECT n.notExists FROM MATCH (n) ON financialNetwork", new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));

    result = pgql.parse("SELECT e.notExists FROM MATCH () -[e]-> () ON \"financialNetwork\"",
        new ExampleMetadataProvider());
    assertTrue(result.getErrorMessages().contains("Property does not exist for any of the labels"));
  }
}
