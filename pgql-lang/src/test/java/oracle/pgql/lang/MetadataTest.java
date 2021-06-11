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
    assertTrue(result.getErrorMessages(), result.getErrorMessages().contains("Edge label does not exist"));

    result = pgql.parse("SELECT * FROM MATCH () -[e:\"knows\"]-> ()", new ExampleMetadataProvider());
    assertTrue(result.isQueryValid());

    // test case insensitive matching
    result = pgql.parse("SELECT * FROM MATCH () -[e:knows]-> ()", new ExampleMetadataProvider());
  }
}
