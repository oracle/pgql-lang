/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.completions.PgqlCompletion;
import oracle.pgql.lang.completions.PgqlCompletionContext;

public class SyntacticCompletionsTest extends AbstractCompletionsTest {

  @Override
  protected PgqlCompletionContext getCompletionContext() {
    return new PgqlCompletionContext() {

      @Override
      public List<String> getVertexProperties() {
        return Collections.emptyList();
      }

      @Override
      public List<String> getEdgeProperties() {
        return Collections.emptyList();
      }

      @Override
      public List<String> getVertexLabels() {
        return Collections.emptyList();
      }

      @Override
      public List<String> getEdgeLabels() {
        return Collections.emptyList();
      }
    };
  }

  @Test
  public void testVertexProps() throws Exception {
    String query = "SELECT 123 MATCH (n) -[e]-> (m) WHERE ???";

    List<PgqlCompletion> expected = new ArrayList<>();
    // TODO

    checkResult(query, expected);
  }
}