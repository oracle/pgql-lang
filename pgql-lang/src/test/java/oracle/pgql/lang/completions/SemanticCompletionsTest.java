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

    List<PgqlCompletion> expected = expected(//
        completion("name", "vertex property"), //
        completion("age", "vertex property"));

    String query = "SELECT n.??? FROM g MATCH (n)";
    check(query, expected);

    query = "SELECT n.name FROM g MATCH (n) WHERE n.???";
    check(query, expected);

    query = "SELECT n.name FROM g MATCH (n) WHERE n.name = 'Ana' GROUP BY n.???";
    check(query, expected);

    query = "SELECT n.name FROM g MATCH (n) WHERE n.name = 'Ana' GROUP BY n ORDER BY n.???";
    check(query, expected);
  }

  @Test
  public void testEdgeProps() throws Exception {

    List<PgqlCompletion> expected = expected(completion("weight", "edge property"));

    String query = "SELECT edge.??? FROM g WHERE (n) -[edge]-> (m)";
    check(query, expected);

    query = "SELECT edge.weight FROM g WHERE (n) -[edge]-> (m) WHERE edge.???";
    check(query, expected);

    query = "SELECT edge.weight FROM g WHERE (n) -[edge]-> (m) WHERE n.name = 'Ana' GROUP BY edge.???";
    check(query, expected);

    query = "SELECT edge.weight FROM g WHERE (n) -[edge]-> (m) WHERE n.name = 'Ana' GROUP BY edge ORDER BY edge.???";
    check(query, expected);
  }

  @Test
  public void testVertexLabels() throws Exception {

    List<PgqlCompletion> expected = expected(//
        completion("Person", "vertex label"), //
        completion("Student", "vertex label"), //
        completion("Professor", "vertex label"));

    String query = "SELECT n.name FROM g MATCH (n:???)";
    check(query, expected);

    query = "SELECT n.name FROM g MATCH (n) -> (m:???";
    check(query, expected);

    query = "SELECT n.name FROM g MATCH (n) -> (m:??? GROUP BY n";
    check(query, expected);
  }

  @Test
  public void testEdgeLabels() throws Exception {

    List<PgqlCompletion> expected = expected(//
        completion("likes", "edge label"), //
        completion("knows", "edge label"));

    String query = "SELECT e.weight FROM g MATCH () -[e:???]-> ()";
    check(query, expected);

    query = "SELECT e.weight FROM g MATCH () -[e:???";
    check(query, expected);

    query = "SELECT e.weight FROM g MATCH () -[e:??? GROUP BY e";
    check(query, expected);
  }
}