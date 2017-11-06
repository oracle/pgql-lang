/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.metaborg.core.completion.ICompletion;

import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;

public class PgqlCompletionGenerator {

  private static String FROM = "FROM";

  public static final PgqlCompletion EMPTY_STRING_COMPLETION = completion("SELECT n.prop FROM g MATCH (n:Lbl)",
      "Query");

  private static final String IDENTIFIER = "[A-Za-z][A-Za-z0-9_]*";
  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER);
  private static final Pattern IDENTIFIER_AT_END_PATTERN = Pattern.compile(IDENTIFIER + "$");

  public static List<PgqlCompletion> generate(PgqlResult pgqlResult, Iterable<ICompletion> spoofaxCompletions,
      String queryString, int cursor, PgqlCompletionContext ctx)
      throws PgqlException {

    String graphName = null;
    if (pgqlResult.getGraphQuery() != null) {
      graphName = pgqlResult.getGraphQuery().getInputGraphName();
    }
    if (graphName == null) {
      // fallback; for example, above fails for SELECT n.??? FROM g MATCH (n)
      int lastIndexOfFrom = queryString.toUpperCase().lastIndexOf(FROM);
      if (lastIndexOfFrom != -1) {
        String stringAfterFrom = queryString.substring(lastIndexOfFrom + FROM.length());
        graphName = parseIdentifierAtBeginning(stringAfterFrom);
      }
    }

    if (queryString.trim().isEmpty()) {
      List<PgqlCompletion> result = new ArrayList<>();
      result.add(EMPTY_STRING_COMPLETION);
      return result;
    } else if (queryString.charAt(cursor - 1) == ':') {
      // labels
      return generateLabelSuggestions(graphName, queryString, cursor, ctx);
    } else if (queryString.charAt(cursor - 1) == '.') {
      // properties
      return generatePropertySuggestions(graphName, pgqlResult, queryString, cursor, ctx);
    } else {
      List<PgqlCompletion> result = new ArrayList<>();

      List<PgqlCompletion> variableProposals = getVariableProposals(pgqlResult);

      String stringBeforeCursor = queryString.substring(0, cursor).trim().toUpperCase();

      ClauseOrAggregate currentClause = getCurrentClauseOrAggregate(stringBeforeCursor);
      boolean proposeExpressions = false;
      boolean proposeAggregations = false;
      switch (currentClause) {
        case SELECT:
        case ORDER_BY:
          proposeExpressions = true;
          proposeAggregations = true;
          break;
        case WHERE:
        case GROUP_BY:
        case COUNT:
        case MIN:
        case MAX:
        case AVG:
        case SUM:
          proposeExpressions = true;
          break;
        default:
          break;
      }

      if (proposeExpressions) {
        result.addAll(variableProposals);
        result.addAll(functions());
      }
      if (proposeAggregations) {
        result.addAll(aggregations());
      }
      if (proposeExpressions) {
        result.addAll(otherExpressions());
      }

      return result;
    }
  }

  private static List<PgqlCompletion> getVariableProposals(PgqlResult pgqlResult) throws PgqlException {
    List<PgqlCompletion> variables = new ArrayList<>();
    GraphPattern graphPattern = pgqlResult.getGraphQuery().getGraphPattern();
    for (QueryVertex vertex : graphPattern.getVertices()) {
      if (vertex.getName().contains("<<vertex-without-brackets>>")) {
        continue;
      }

      if (!vertex.isAnonymous()) {
        variables.add(new PgqlCompletion(vertex.getName(), "vertex"));
      }
    }
    for (VertexPairConnection connection : graphPattern.getConnections()) {
      if (!connection.isAnonymous()) {
        variables.add(new PgqlCompletion(connection.getName(), connection.getVariableType().toString().toLowerCase()));
      }
    }
    return variables;
  }

  private static List<PgqlCompletion> generatePropertySuggestions(String graphName, PgqlResult pgqlResult,
      String queryString, int cursor, PgqlCompletionContext ctx)
      throws PgqlException {
    String variableName = parseIdentifierAtEnd(queryString, cursor - 1);
    if (variableName == null) {
      return Collections.emptyList();
    }
    GraphPattern graphPattern = pgqlResult.getGraphQuery().getGraphPattern();

    Set<QueryVertex> vertices = graphPattern.getVertices();
    boolean isVertexVariable = vertices.stream() //
        .map(QueryVertex::getName) //
        .anyMatch(variableName::equals);
    if (isVertexVariable) {
      return ctx.getVertexProperties(graphName).stream().map(prop -> new PgqlCompletion(prop, "vertex property"))
          .collect(Collectors.toList());
    }

    Set<VertexPairConnection> edges = graphPattern.getConnections();
    boolean isEdgeVariable = edges.stream() //
        .filter((n) -> n.getVariableType() == VariableType.EDGE) //
        .map(VertexPairConnection::getName) //
        .anyMatch(variableName::equals);
    if (isEdgeVariable) {
      return ctx.getEdgeProperties(graphName).stream().map(prop -> new PgqlCompletion(prop, "edge property"))
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  private static List<PgqlCompletion> generateLabelSuggestions(String graphName, String queryString, int cursor,
      PgqlCompletionContext ctx) {
    String queryUpToCursor = queryString.substring(0, cursor - 2);
    if (queryUpToCursor.lastIndexOf('(') > queryUpToCursor.lastIndexOf('[')) {
      return ctx.getVertexLabels(graphName).stream().map(lbl -> new PgqlCompletion(lbl, "vertex label"))
          .collect(Collectors.toList());
    } else {
      return ctx.getEdgeLabels(graphName).stream().map(lbl -> new PgqlCompletion(lbl, "edge label"))
          .collect(Collectors.toList());
    }
  }

  private static ClauseOrAggregate getCurrentClauseOrAggregate(String trimmedQuery) {
    ClauseOrAggregate result = null;
    int index = -1;

    for (ClauseOrAggregate c : ClauseOrAggregate.values()) {
      int newIndex = trimmedQuery.lastIndexOf(c.toString().replace("_", " "));
      if (newIndex > index) {
        result = c;
        index = newIndex;
      }
    }

    return result;
  }

  private static String parseIdentifierAtBeginning(String s) {
    return matchPattern(s, IDENTIFIER_PATTERN);
  }

  private static String parseIdentifierAtEnd(String queryString, int positionLastCharacter) {
    String s = queryString.substring(0, positionLastCharacter);
    return matchPattern(s, IDENTIFIER_AT_END_PATTERN);
  }

  private static String matchPattern(String s, Pattern pattern) {
    Matcher matcher = pattern.matcher(s);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }

  public static PgqlCompletion completion(String value, String meta) {
    return new PgqlCompletion(value, meta);
  }

  public static List<PgqlCompletion> completions(PgqlCompletion... completions) {
    return new ArrayList<>(Arrays.asList(completions));
  }

  public static List<PgqlCompletion> functions() {
    return completions(//
        completion("id(elem)", "get identifier"), //
        completion("label(edge)", "get label of edge"), //
        completion("labels(vertex)", "get labels of vertex"), //
        completion("has_label(elem, lbl)", "check if elem has label"), //
        completion("all_different(exp1, exp2, ...)", "check if values are all different"), //
        completion("in_degree(vertex)", "number of incoming neighbors"), //
        completion("out_degree(vertex)", "number of outgoin neighbors"));
  }

  public static List<PgqlCompletion> aggregations() {
    return completions(//
        completion("COUNT(*)", "count the number of matches"), //
        completion("COUNT(exp)", "count the number of times the expression evaluates to a non-null value"), //
        completion("MIN(exp)", "minimum"), //
        completion("MAX(exp)", "maximum"), //
        completion("AVG(exp)", "average"), //
        completion("SUM(exp)", "sum"));
  }

  public static List<PgqlCompletion> otherExpressions() {
    return completions(//
        completion("true", "boolean literal"), //
        completion("false", "boolean literal"), //
        completion("DATE '2017-01-01'", "date literal"), //
        completion("TIME '20:15:00'", "time literal"), //
        completion("TIMESTAMP '2017-01-01 20:15:00'", "timestamp literal"), //
        completion("CAST(exp AS type)", "cast"), //
        completion("exp IS NULL", "is null"), //
        completion("exp IS NOT NULL", "is not null"), //
        completion("exp AND exp", "conjunction"), //
        completion("exp OR exp", "disjuncion"), //
        completion("NOT exp", "negation"), //
        completion("exp * exp", "multiplication"), //
        completion("exp + exp", "addition"), //
        completion("exp / exp", "division"), //
        completion("exp % exp", "modulo"), //
        completion("exp - exp", "subtraction"), //
        completion("exp = exp", "equals"), //
        completion("exp > exp", "greater than"), //
        completion("exp < exp", "less than"), //
        completion("exp >= exp", "greater than equals"), //
        completion("exp <= exp", "less than equals"), //
        completion("exp <> exp", "not equals"));
  }
}
