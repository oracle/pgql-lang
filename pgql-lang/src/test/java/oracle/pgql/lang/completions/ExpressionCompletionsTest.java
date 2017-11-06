/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.completions.PgqlCompletion;

import static oracle.pgql.lang.completions.PgqlCompletionGenerator.EMPTY_STRING_COMPLETION;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.completion;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.completions;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.functions;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.aggregations;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.otherExpressions;

public class ExpressionCompletionsTest extends AbstractCompletionsTest {

  private List<PgqlCompletion> expressions() {
    List<PgqlCompletion> expected = completions(//
        completion("m", "vertex"), //
        completion("n", "vertex"), //
        completion("e", "edge"));
    expected.addAll(functions());
    expected.addAll(aggregations());
    expected.addAll(otherExpressions());
    return expected;
  }

  private List<PgqlCompletion> expressionsExceptAggregations() {
    List<PgqlCompletion> expected = completions(//
        completion("m", "vertex"), //
        completion("n", "vertex"), //
        completion("e", "edge"));
    expected.addAll(functions());
    expected.addAll(otherExpressions());
    return expected;
  }

  private List<PgqlCompletion> expressionsExceptVariables() {
    List<PgqlCompletion> expected = functions();
    expected.addAll(aggregations());
    expected.addAll(otherExpressions());
    return expected;
  }

  @Test
  public void emptySelect() throws Exception {
    String query = "SELECT ??? MATCH (n) -[e]-> (m) where n";
    List<PgqlCompletion> expected = expressionsExceptVariables();
    // ultimately, it would suggest vertices and edges too, but the parser's error recovery isn't working for this case
    check(query, expected);
  }

  @Test
  public void nonEmptySelect() throws Exception {
    String query = "SELECT n.name, ??? MATCH (n) -[e]-> (m) where n";
    List<PgqlCompletion> expected = expressions();
    check(query, expected);
  }

  @Test
  public void emptyWhere() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) where ???";
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
  public void emptyAggregation() throws Exception {
    String query = "SELECT MIN(???) MATCH (n) -[e]-> (m)";
    List<PgqlCompletion> expected = expressionsExceptAggregations();
    check(query, expected);
  }

  @Test
  public void emptyString() throws Exception {
    String query = "";
    List<PgqlCompletion> expected = new ArrayList<>();
    expected.add(EMPTY_STRING_COMPLETION);
    check(query, expected);
  }
}