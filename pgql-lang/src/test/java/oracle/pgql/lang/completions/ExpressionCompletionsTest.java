/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static oracle.pgql.lang.completion.PgqlCompletionGenerator.EMPTY_STRING_COMPLETION;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.aggregations;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completion;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completions;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.functions;
import static oracle.pgql.lang.completion.PgqlCompletionGenerator.otherExpressions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.editor.completion.PgqlCompletion;

public class ExpressionCompletionsTest extends AbstractCompletionsTest {

  private List<PgqlCompletion> expressions() {
    List<PgqlCompletion> expected = completions(//
        completion("n", "vertex"), //
        completion("m", "vertex"), //
        completion("e", "edge"));
    expected.addAll(functions());
    expected.addAll(aggregations());
    expected.addAll(otherExpressions());
    return expected;
  }

  private List<PgqlCompletion> expressionsExceptAggregations() {
    List<PgqlCompletion> expected = completions(//
        completion("n", "vertex"), //
        completion("m", "vertex"), //
        completion("e", "edge"));
    expected.addAll(functions());
    expected.addAll(otherExpressions());
    return expected;
  }

  @Test
  public void emptySelect1() throws Exception {
    String query = "SELECT ??? FROM g MATCH (n) -[e]-> (m) WHERE n";
    List<PgqlCompletion> expected = expressions();
    check(query, expected);
  }

  @Test
  public void emptySelect2() throws Exception {
    String query = "SELECT ??? MATCH (n) -[e]-> (m) WHERE n";
    List<PgqlCompletion> expected = expressions();
    check(query, expected);
  }

  @Test
  public void nonEmptySelect() throws Exception {
    String query = "SELECT n.name, ??? MATCH (n) -[e]-> (m) WHERE n";
    List<PgqlCompletion> expected = expressions();
    check(query, expected);
  }

  @Test
  public void emptyWhere() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) WHERE ???";
    List<PgqlCompletion> expected = expressionsExceptAggregations();
    check(query, expected);
  }

  @Test
  public void emptyGroupBy() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) GROUP BY ???";
    List<PgqlCompletion> expected = expressionsExceptAggregations();
    check(query, expected);
  }

  @Test
  public void nonEmptyGroupBy() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) GROUP BY n, ???";
    List<PgqlCompletion> expected = expressionsExceptAggregations();
    check(query, expected);
  }

  @Test
  public void emptyOrderBy() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) ORDER BY ???";
    List<PgqlCompletion> expected = expressions();
    check(query, expected);
  }

  @Test
  public void nonEmptyOrderBy() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) ORDER BY n.name, ???";
    List<PgqlCompletion> expected = expressions();
    check(query, expected);
  }

  @Test
  public void emptyAggregation1() throws Exception {
    String query = "SELECT MIN(???) MATCH (n) -[e]-> (m)";
    List<PgqlCompletion> expected = expressionsExceptAggregations();
    check(query, expected);
  }

  @Test
  public void emptyAggregation2() throws Exception {
    String query = "SELECT AVG(n.age) MATCH (n) -[e]-> (m) GROUP BY n.name ORDER BY COUNT(???)";
    List<PgqlCompletion> expected = expressionsExceptAggregations();
    check(query, expected);
  }

  @Test
  public void emptyString() throws Exception {
    List<PgqlCompletion> expected = new ArrayList<>();
    expected.add(EMPTY_STRING_COMPLETION);

    String query = "";
    check(query, expected);

    query = " ";
    check(query, expected);

    query = "\t";
    check(query, expected);

    query = "\n";
    check(query, expected);
  }
}
