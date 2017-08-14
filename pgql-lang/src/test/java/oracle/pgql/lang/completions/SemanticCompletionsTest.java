/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.completions.PgqlCompletion;
import oracle.pgql.lang.completions.PgqlCompletionContext;

public class SemanticCompletionsTest extends AbstractCompletionsTest {

  private static final String[] VERTEX_PROPS = { "name", "age" };

  private static final String[] EDGE_PROPS = { "weight" };

  private static final String[] VERTEX_LABELS = { "Person", "Student", "Professor" };

  private static final String[] EDGE_LABELS = { "likes", "knows" };

  @Override
  protected PgqlCompletionContext getCompletionContext() {
    return new PgqlCompletionContext() {

      @Override
      public List<String> getVertexProperties() {
        return new ArrayList<>(Arrays.asList(VERTEX_PROPS));
      }

      @Override
      public List<String> getEdgeProperties() {
        return new ArrayList<>(Arrays.asList(EDGE_PROPS));
      }

      @Override
      public List<String> getVertexLabels() {
        return new ArrayList<>(Arrays.asList(VERTEX_LABELS));
      }

      @Override
      public List<String> getEdgeLabels() {
        return new ArrayList<>(Arrays.asList(EDGE_LABELS));
      }
    };
  }

  @Test
  public void testVertexProps() throws Exception {
    String query = "SELECT n.??? FROM g WHERE (n)";

    List<PgqlCompletion> expected = new ArrayList<>();
    expected.add(new PgqlCompletion("name", "name", "vertex property"));
    expected.add(new PgqlCompletion("age", "age", "vertex property"));

    checkResult(query, expected);
  }

  @Test
  public void testEdgeProps() throws Exception {
    String query = "SELECT edge.??? FROM g WHERE (n) -[edge]-> (m)";

    List<PgqlCompletion> expected = new ArrayList<>();
    expected.add(new PgqlCompletion("weight", "weight", "edge property"));

    checkResult(query, expected);
  }

  @Test
  public void testVertexLabels() throws Exception {
    String query = "SELECT n.name FROM g WHERE (n:???)";

    List<PgqlCompletion> expected = new ArrayList<>();
    expected.add(new PgqlCompletion("Person", "Person", "vertex label"));
    expected.add(new PgqlCompletion("Student", "Student", "vertex label"));
    expected.add(new PgqlCompletion("Professor", "Professor", "vertex label"));

    checkResult(query, expected);
  }

  @Test
  public void testEdgeLabels() throws Exception {
    String query = "SELECT e.weight FROM g WHERE () -[e:???]-> ()";

    List<PgqlCompletion> expected = new ArrayList<>();
    expected.add(new PgqlCompletion("likes", "likes", "edge label"));
    expected.add(new PgqlCompletion("knows", "knows", "edge label"));

    checkResult(query, expected);
  }
}