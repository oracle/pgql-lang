/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completion;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completions;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.editor.completion.PgqlCompletion;

public class PropertyCompletionsTest extends AbstractCompletionsTest {

  @Test
  public void testVertexPropsGraphG() throws Exception {

    List<PgqlCompletion> expected = completions(//
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
  public void testEdgePropsGraphG() throws Exception {

    List<PgqlCompletion> expected = completions(completion("weight", "edge property"));

    String query = "SELECT edge.??? FROM g MATCH (n) -[edge]-> (m)";
    check(query, expected);

    query = "SELECT edge.weight FROM g MATCH (n) -[edge]-> (m) WHERE edge.???";
    check(query, expected);

    query = "SELECT edge.weight FROM g MATCH (n) -[edge]-> (m) WHERE n.name = 'Ana' GROUP BY edge.???";
    check(query, expected);

    query = "SELECT edge.weight FROM g MATCH (n) -[edge]-> (m) WHERE n.name = 'Ana' GROUP BY edge ORDER BY edge.???";
    check(query, expected);
  }

  /* FIXME
  @Test
  public void testVertexPropsDefaultGraph() throws Exception {

    List<PgqlCompletion> expected = completions(//
        completion("line_no", "vertex property"));

    String query = "SELECT n.??? MATCH (n)";
    check(query, expected);

    query = "SELECT n.name MATCH (n) WHERE n.???";
    check(query, expected);

    query = "SELECT n.name MATCH (n) WHERE n.name = 'Ana' GROUP BY n.???";
    check(query, expected);

    query = "SELECT n.name MATCH (n) WHERE n.name = 'Ana' GROUP BY n ORDER BY n.???";
    check(query, expected);
  }
  */

  @Test
  public void testEdgePropsDefaultGraph() throws Exception {

    List<PgqlCompletion> expected = Collections.emptyList();

    String query = "SELECT edge.??? MATCH (n) -[edge]-> (m)";
    check(query, expected);

    query = "SELECT edge.weight MATCH (n) -[edge]-> (m) WHERE edge.???";
    check(query, expected);

    query = "SELECT edge.weight MATCH (n) -[edge]-> (m) WHERE n.name = 'Ana' GROUP BY edge.???";
    check(query, expected);

    query = "SELECT edge.weight MATCH (n) -[edge]-> (m) WHERE n.name = 'Ana' GROUP BY edge ORDER BY edge.???";
    check(query, expected);
  }
}
