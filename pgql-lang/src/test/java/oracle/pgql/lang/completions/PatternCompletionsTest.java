/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static oracle.pgql.lang.completion.PgqlCompletionGenerator.COMMA_VERTEX_COMPLETION;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.RELATION_VERTEX_COMPLETIONS;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.SPACE_RELATION_VERTEX_COMPLETIONS;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.SPACE_VERTEX_COMPLETION;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.VERTEX_COMPLETION;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.editor.completion.PgqlCompletion;

public class PatternCompletionsTest extends AbstractCompletionsTest {

  @Test
  public void testVertex1() throws Exception {
    List<PgqlCompletion> expected = completions(VERTEX_COMPLETION);

    String query = "SELECT * FROM g MATCH ???";
    check(query, expected);
  }

  @Test
  public void testVertex2() throws Exception {
    List<PgqlCompletion> expected = completions(SPACE_VERTEX_COMPLETION);

    String query = "SELECT * FROM g MATCH???";
    check(query, expected);
  }

  @Test
  public void testVertex3() throws Exception {
    List<PgqlCompletion> expected = completions(VERTEX_COMPLETION);

    String query = "SELECT * FROM g MATCH (n), ???";
    check(query, expected);
  }

  @Test
  public void testVertex4() throws Exception {
    List<PgqlCompletion> expected = completions(SPACE_VERTEX_COMPLETION);

    String query = "SELECT * FROM g MATCH (n),???";
    check(query, expected);
  }

  @Test
  public void testEdgeVertex1() throws Exception {
    List<PgqlCompletion> expected = new ArrayList<>();
    expected.addAll(Arrays.asList(RELATION_VERTEX_COMPLETIONS));
    expected.add(COMMA_VERTEX_COMPLETION);

    String query = "SELECT * FROM g MATCH (n) ???";
    check(query, expected);
  }

  @Test
  public void testEdgeVertex2() throws Exception {
    List<PgqlCompletion> expected = new ArrayList<>();
    expected.addAll(Arrays.asList(SPACE_RELATION_VERTEX_COMPLETIONS));
    expected.add(COMMA_VERTEX_COMPLETION);

    String query = "SELECT * FROM g MATCH (n)???";
    check(query, expected);
  }
}
