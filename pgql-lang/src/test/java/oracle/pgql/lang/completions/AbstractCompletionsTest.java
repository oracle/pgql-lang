/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.AbstractPgqlTest;
import oracle.pgql.lang.editor.completion.PgqlCompletion;
import oracle.pgql.lang.editor.completion.PgqlCompletionContext;

public abstract class AbstractCompletionsTest extends AbstractPgqlTest {

  private static final String[] VERTEX_PROPS_DEFAULT_GRAPH = { "line_no" };

  private static final String[] EDGE_PROPS_DEFAULT_GRAPH = {};

  private static final String[] VERTEX_LABELS_DEFAULT_GRAPH = { "Function", "Variable" };

  private static final String[] EDGE_LABELS_DEFAULT_GRAPH = { "calls" };

  private static final String GRAPH_NAME = "g";

  private static final String[] VERTEX_PROPS_G = { "name", "age" };

  private static final String[] EDGE_PROPS_G = { "weight" };

  private static final String[] VERTEX_LABELS_G = { "Person", "Student", "Professor" };

  private static final String[] EDGE_LABELS_G = { "likes", "knows" };

  protected PgqlCompletionContext getCompletionContext() {
    return new PgqlCompletionContext() {

      @Override
      public List<String> getGraphNames() {
        return Collections.singletonList(GRAPH_NAME);
      }

      @Override
      public List<String> getVertexProperties(String graphName) {
        return getData(graphName, VERTEX_PROPS_DEFAULT_GRAPH, VERTEX_PROPS_G);
      }

      @Override
      public List<String> getEdgeProperties(String graphName) {
        return getData(graphName, EDGE_PROPS_DEFAULT_GRAPH, EDGE_PROPS_G);
      }

      @Override
      public List<String> getVertexLabels(String graphName) {
        return getData(graphName, VERTEX_LABELS_DEFAULT_GRAPH, VERTEX_LABELS_G);
      }

      @Override
      public List<String> getEdgeLabels(String graphName) {
        return getData(graphName, EDGE_LABELS_DEFAULT_GRAPH, EDGE_LABELS_G);
      }
    };
  }

  private List<String> getData(String graphName, String[] dataDefaultGraph, String[] dataGraphG) {
    if (graphName == null) {
      return new ArrayList<>(Arrays.asList(dataDefaultGraph));
    } else if (graphName.equals(GRAPH_NAME)) {
      return new ArrayList<>(Arrays.asList(dataGraphG));
    } else
      return Collections.emptyList();
  }

  protected void check(String query, List<PgqlCompletion> expected) throws Exception {
    check(query, expected, false);
  }

  protected void checkSubset(String query, List<PgqlCompletion> expected) throws Exception {
    check(query, expected, true);
  }

  private void check(String query, List<PgqlCompletion> expected, boolean subset) throws Exception {
    int cursor = query.indexOf("???");
    query = query.replaceAll("\\?\\?\\?", "");

    List<PgqlCompletion> actual = pgql.complete(query, cursor, getCompletionContext());
    String actualAsString = actual.stream().map(c -> c.toString()).collect(Collectors.joining("\n"));

    if (subset) {
      for (PgqlCompletion completion : expected) {
        String errorMessage = "\nexpected completion\n\n" + completion + "\n\not in\n\n" + actualAsString + "\n";
        assertTrue(errorMessage, actual.contains(completion));
      }
    } else {
      String expectedAsString = expected.stream().map(c -> c.toString()).collect(Collectors.joining("\n"));
      String errorMessage = "\nexpected\n\n" + expectedAsString + "\n\nactual\n\n" + actualAsString + "\n";
      assertEquals(errorMessage, expected, actual);
    }
  }
}
