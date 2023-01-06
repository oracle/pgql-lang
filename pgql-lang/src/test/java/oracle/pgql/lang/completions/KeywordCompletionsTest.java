/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completions;

import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.completion.ClauseOrAggregate;
import oracle.pgql.lang.completion.Function;
import oracle.pgql.lang.editor.completion.PgqlCompletion;

public class KeywordCompletionsTest extends AbstractCompletionsTest {

  @Test
  public void incompleteSelect1() throws Exception {
    String query = "S???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.SELECT.getCompletion("ELECT"));
    check(query, expected);
  }

  @Test
  public void incompleteSelect2() throws Exception {
    String query = "SELE???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.SELECT.getCompletion("CT"));
    check(query, expected);
  }

  @Test
  public void incompleteILower() throws Exception {
    String query = "SELECT i???";
    List<PgqlCompletion> expected = completions(//
        Function.ID.getCompletion("d"),
        Function.IN_DEGREE.getCompletion("n_degree"),
        Function.IN.getCompletion("n"),
        Function.IS_NULL.getCompletion("s null"),
        Function.IS_NOT_NULL.getCompletion("s not null"));
    check(query, expected);
  }

  @Test
  public void incompleteIUpper() throws Exception {
    String query = "SELECT i???";
    List<PgqlCompletion> expected = completions(//
        Function.ID.getCompletion("d"),
        Function.IN_DEGREE.getCompletion("n_degree"),
        Function.IN.getCompletion("n"),
        Function.IS_NULL.getCompletion("s null"),
        Function.IS_NOT_NULL.getCompletion("s not null"));
    check(query, expected);
  }

  @Test
  public void incompleteIsNull() throws Exception {
    String query = "SELECT n IS N???";
    List<PgqlCompletion> expected = completions(//
        Function.IS_NULL.getCompletion("ULL"),
        Function.IS_NOT_NULL.getCompletion("OT NULL"));
    check(query, expected);
  }

  @Test
  public void incompleteLabel() throws Exception {
    String query = "SELECT lab???";
    List<PgqlCompletion> expected = completions(//
        Function.LABEL.getCompletion("el"),
        Function.LABELS.getCompletion("els"));
    check(query, expected);
  }

  @Test
  public void incompleteHasLabel() throws Exception {
    String query = "SELECT has_lab???";
    List<PgqlCompletion> expected = completions(//
        Function.HAS_LABEL.getCompletion("el"));
    check(query, expected);
  }

  @Test
  public void incompleteAllDifferent() throws Exception {
    String query = "SELECT all_dif???";
    List<PgqlCompletion> expected = completions(//
        Function.ALL_DIFFERENT.getCompletion("ferent"));
    check(query, expected);
  }
  
  @Test
  public void incompleteOutDegree() throws Exception {
    String query = "SELECT OUT_D???";
    List<PgqlCompletion> expected = completions(//
        Function.OUT_DEGREE.getCompletion("EGREE"));
    check(query, expected);
  }

  @Test
  public void incompleteAbs() throws Exception {
    String query = "SELECT ab???";
    List<PgqlCompletion> expected = completions(//
        Function.ABS.getCompletion("s"));
    check(query, expected);
  }

  @Test
  public void incompleteCeil() throws Exception {
    String query = "SELECT ce???";
    List<PgqlCompletion> expected = completions(//
        Function.CEIL.getCompletion("il"),
        Function.CEILING.getCompletion("iling"));
    check(query, expected);
  }

  @Test
  public void incompleteFloor() throws Exception {
    String query = "SELECT fl???";
    List<PgqlCompletion> expected = completions(//
        Function.FLOOR.getCompletion("oor"));
    check(query, expected);
  }

  @Test
  public void incompleteRound() throws Exception {
    String query = "SELECT rou???";
    List<PgqlCompletion> expected = completions(//
        Function.ROUND.getCompletion("nd"));
    check(query, expected);
  }

  @Test
  public void incompleteExtract() throws Exception {
    String query = "SELECT EXTR???";
    List<PgqlCompletion> expected = completions(//
        Function.EXTRACT.getCompletion("ACT"));
    check(query, expected);
  }

  @Test
  public void incompleteLower() throws Exception {
    String query = "SELECT LO???";
    List<PgqlCompletion> expected = completions(//
        Function.LOWER.getCompletion("WER"));
    check(query, expected);
  }

  @Test
  public void incompleteUpper() throws Exception {
    String query = "SELECT UP???";
    List<PgqlCompletion> expected = completions(//
        Function.UPPER.getCompletion("PER"));
    check(query, expected);
  }

  @Test
  public void incompleteSubstring() throws Exception {
    String query = "SELECT SUB???";
    List<PgqlCompletion> expected = completions(//
        Function.SUBSTRING.getCompletion("STRING"));
    check(query, expected);
  }

  @Test
  public void incompleteFrom() throws Exception {
    String query = "SELECT n.name FR???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.FROM.getCompletion("OM"));
    check(query, expected);
  }

  @Test
  public void incompleteAggregate() throws Exception {
    String query = "SELECT n.name M??? MATCH(n)";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.MIN.getCompletion("IN"),
        ClauseOrAggregate.MAX.getCompletion("AX"));
    check(query, expected);
  }

  @Test
  public void incompleteMatch1() throws Exception {
    String query = "SELECT n.name M???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.MIN.getCompletion("IN"),
        ClauseOrAggregate.MAX.getCompletion("AX"),
        ClauseOrAggregate.MATCH.getCompletion("ATCH"));
    check(query, expected);
  }

  @Test
  public void incompleteMatch2() throws Exception {
    String query = "SELECT n.name FROM g MA???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.MATCH.getCompletion("TCH"));
    check(query, expected);
  }

  @Test
  public void incompleteWhere() throws Exception {
    String query = "SELECT n.name MATCH (n) WHER???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.WHERE.getCompletion("E"));
    check(query, expected);
  }
  
  @Test
  public void incompleteFunctionInWhere() throws Exception {
    String query = "SELECT n MATCH(n) WHERE has???";
    List<PgqlCompletion> expected = completions(//
        Function.HAS_LABEL.getCompletion("_label"));
    check(query, expected);
  }

  @Test
  public void incompleteAggregateInWhere() throws Exception {
    String query = "SELECT n MATCH(n) WHERE CO???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.COUNT.getCompletion("UNT"));
    check(query, expected);
  }

  @Test
  public void incompleteGroupBy1() throws Exception {
    String query = "SELECT n.name MATCH (n) GROU???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.GROUP_BY.getCompletion("P BY"));
    check(query, expected);
  }

  @Test
  public void incompleteGroupBy2() throws Exception {
    String query = "SELECT n.name MATCH (n) GROUP ???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.GROUP_BY.getCompletion(" BY"));
    check(query, expected);
  }

  @Test
  public void incompleteGroupBy3() throws Exception {
    String query = "SELECT n.name MATCH (n) GROUP B???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.GROUP_BY.getCompletion("Y"));
    check(query, expected);
  }

  @Test
  public void incompleteHaving() throws Exception {
    String query = "SELECT n.name MATCH (n) HAVIN???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.HAVING.getCompletion("G"));
    check(query, expected);
  }

  @Test
  public void incompleteFunctionInHaving() throws Exception {
    String query = "SELECT n.salary MATCH (n) -[e]-> (m) GROUP BY n HAVING ceili???";
    List<PgqlCompletion> expected = completions(//
        Function.CEILING.getCompletion("ng"));
    check(query, expected);
  }

  @Test
  public void incompleteAggregateInHaving() throws Exception {
    String query = "SELECT n.name MATCH (n) -[e]-> (m) GROUP BY n HAVING MA???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.MAX.getCompletion("X"));
    check(query, expected);
  }

  @Test
  public void incompleteOrderBy1() throws Exception {
    String query = "SELECT n.name MATCH (n) ORDER???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.ORDER_BY.getCompletion(" BY"));
    check(query, expected);
  }

  @Test
  public void incompleteOrderBy2() throws Exception {
    String query = "SELECT n.name MATCH (n) ORDER B???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.ORDER_BY.getCompletion("Y"));
    check(query, expected);
  }

  @Test
  public void incompleteFunctionInOrderBy() throws Exception {
    String query = "SELECT n.name MATCH (n) ORDER BY OU???";
    List<PgqlCompletion> expected = completions(//
        Function.OUT_DEGREE.getCompletion("T_DEGREE"));
    check(query, expected);
  }

  @Test
  public void incompleteAggregateInOrderBy() throws Exception {
    String query = "SELECT COUNT(*) MATCH (n) GROUP BY n.age ORDER BY CO???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.COUNT.getCompletion("UNT"));
    check(query, expected);
  }

  @Test
  public void incompleteLimit() throws Exception {
    String query = "SELECT n.name MATCH (n) L???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.LIMIT.getCompletion("IMIT"));
    check(query, expected);
  }

  @Test
  public void incompleteOffset() throws Exception {
    String query = "SELECT n.name MATCH (n) LIMIT 3 OFF???";
    List<PgqlCompletion> expected = completions(//
        ClauseOrAggregate.OFFSET.getCompletion("SET"));
    check(query, expected);
  }
}
