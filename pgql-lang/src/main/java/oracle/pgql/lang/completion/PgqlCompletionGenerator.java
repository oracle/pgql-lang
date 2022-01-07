/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.core.completion.ICompletion;

import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.editor.completion.PgqlCompletion;
import oracle.pgql.lang.editor.completion.PgqlCompletionContext;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;

public class PgqlCompletionGenerator {

  private static final String FROM = "FROM";

  private static final String MATCH = "MATCH";

  public static final PgqlCompletion MATCH_CLAUSE_COMPLETION = completion("MATCH (n)", "match clause");

  private static final String VERTEX = "(n)";

  private static final String EDGE_VERTEX = "-[e]-> (m)";

  private static final String REV_EDGE_VERTEX = "<-[e]- (m)";

  private static final String PATH_VERTEX = "-/:lbl*/-> (m)";

  private static final String REV_PATH_VERTEX = "<-/:lbl*/- (m)";

  public static final PgqlCompletion EMPTY_STRING_COMPLETION = completion("SELECT n.prop\n  FROM g\n MATCH (n)",
      "query");

  public static final PgqlCompletion VERTEX_COMPLETION = completion(VERTEX, "vertex");

  public static final PgqlCompletion SPACE_VERTEX_COMPLETION = completion(" " + VERTEX, "vertex");

  public static final PgqlCompletion COMMA_VERTEX_COMPLETION = completion(", " + VERTEX, "vertex");

  public static final PgqlCompletion[] RELATION_VERTEX_COMPLETIONS = { //
      completion(EDGE_VERTEX, "edge and vertex"), //
      completion(PATH_VERTEX, "path and vertex"), //
      completion(REV_EDGE_VERTEX, "edge and vertex"), //
      completion(REV_PATH_VERTEX, "path and vertex")//
  };

  public static final PgqlCompletion[] SPACE_RELATION_VERTEX_COMPLETIONS = { //
      completion(" " + EDGE_VERTEX, "edge and vertex"), //
      completion(" " + PATH_VERTEX, "path and vertex"), //
      completion(" " + REV_EDGE_VERTEX, "edge and vertex"), //
      completion(" " + REV_PATH_VERTEX, "path and vertex")//
  };

  private static final String IDENTIFIER = "[A-Za-z][A-Za-z0-9_]*";

  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER);

  private static final Pattern IDENTIFIER_AT_END_PATTERN = Pattern.compile(IDENTIFIER + "$");

  public static List<PgqlCompletion> generate(PgqlResult pgqlResult, Iterable<ICompletion> spoofaxCompletions,
      String queryString, int cursor, PgqlCompletionContext ctx)
      throws PgqlException {

    List<PgqlCompletion> result = new ArrayList<>();

    if (queryString.trim().isEmpty()) {
      result.add(EMPTY_STRING_COMPLETION);
      return result;
    }

    String stringBeforeCursor = queryString.substring(0, cursor);
    String trimmedStringBeforeCursor = stringBeforeCursor.trim().toUpperCase();
    String stringAfterCursor = queryString.substring(cursor);
    String trimmedStringAfterCursor = stringAfterCursor.trim().toUpperCase();

    ClauseOrAggregate currentClause = getCurrentClauseOrAggregate(trimmedStringBeforeCursor);

    List<PgqlCompletion> incompleteKeywords = getIncompleteKeywords(stringBeforeCursor.trim(), trimmedStringAfterCursor,
        currentClause);
    if (!incompleteKeywords.isEmpty()) {
      result.addAll(incompleteKeywords);
      return result;
    }

    if (pgqlResult == null) {
      if (queryString.toUpperCase().trim().endsWith(MATCH)) {
        if (queryString.endsWith(" ")) {
          return Collections.singletonList(VERTEX_COMPLETION);
        } else {
          return Collections.singletonList(SPACE_VERTEX_COMPLETION);
        }
      }

      return Collections.emptyList();
    }

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

    if (queryString.charAt(cursor - 1) == ':') {
      // labels
      return generateLabelSuggestions(graphName, queryString, cursor, ctx);
    } else if (queryString.charAt(cursor - 1) == '.') {
      // properties
      return generatePropertySuggestions(graphName, pgqlResult, queryString, cursor, ctx);
    } else {

      List<PgqlCompletion> variableProposals = getVariableProposals(pgqlResult);

      boolean proposeExpressions = false;
      boolean proposeAggregations = false;
      if (currentClause == null) {
        return result;
      }
      switch (currentClause) {
        case SELECT:
        case ORDER_BY:
          proposeExpressions = true;
          proposeAggregations = true;
          break;
        case FROM:
          if (trimmedStringBeforeCursor.endsWith(FROM)) {
            return generateInputGraphCompletions(ctx);
          } else {
            return Collections.singletonList(MATCH_CLAUSE_COMPLETION);
          }
        case MATCH:
          if (trimmedStringBeforeCursor.endsWith(",")) {
            if (stringBeforeCursor.endsWith(" ")) {
              return Collections.singletonList(VERTEX_COMPLETION);
            } else {
              return Collections.singletonList(SPACE_VERTEX_COMPLETION);
            }
          } else if (trimmedStringBeforeCursor.endsWith(")")) {
            List<PgqlCompletion> completions = new ArrayList<>();
            if (stringBeforeCursor.endsWith(" ")) {
              completions.addAll(Arrays.asList(RELATION_VERTEX_COMPLETIONS));
            } else {
              completions.addAll(Arrays.asList(SPACE_RELATION_VERTEX_COMPLETIONS));
            }
            completions.add(COMMA_VERTEX_COMPLETION);
            return completions;
          }
          break;
        case WHERE:
        case GROUP_BY:
        case COUNT:
        case LISTAGG:
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

  private static List<PgqlCompletion> generateInputGraphCompletions(PgqlCompletionContext ctx) {
    List<PgqlCompletion> completions = new ArrayList<>();
    for (String graphName : ctx.getGraphNames()) {
      completions.add(completion(graphName, "graph name"));
    }
    return completions;
  }

  private static List<PgqlCompletion> getVariableProposals(PgqlResult pgqlResult) throws PgqlException {
    List<PgqlCompletion> variables = new ArrayList<>();
    GraphQuery graphQuery = pgqlResult.getGraphQuery();
    if (graphQuery == null) {
      return variables;
    }

    GraphPattern graphPattern = graphQuery.getGraphPattern();
    if (graphPattern == null) {
      return variables;
    }

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
    if (graphPattern == null) {
      return Collections.emptyList();
    }

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

  private static final Map<ClauseOrAggregate, List<Keyword>> allowedClauseKeywords = initAllowedClauseKeywords();

  private static final Map<ClauseOrAggregate, List<Keyword>> initAllowedClauseKeywords() {

    List<ClauseOrAggregate> aggregates = new ArrayList<>();
    aggregates.add(ClauseOrAggregate.COUNT);
    aggregates.add(ClauseOrAggregate.LISTAGG);
    aggregates.add(ClauseOrAggregate.MIN);
    aggregates.add(ClauseOrAggregate.MAX);
    aggregates.add(ClauseOrAggregate.AVG);
    aggregates.add(ClauseOrAggregate.SUM);

    List<Function> functions = new ArrayList<>();
    for (Function f : Function.values()) {
      functions.add(f);
    }

    Map<ClauseOrAggregate, List<Keyword>> allowedKeywords = new HashMap<>();

    // Aggregates
    for (ClauseOrAggregate c : aggregates) {
      allowedKeywords.put(c, Collections.emptyList());
    }

    // null means that we haven't even completed SELECT clause
    List<Keyword> nullList = new ArrayList<>();
    nullList.add(ClauseOrAggregate.SELECT);
    allowedKeywords.put(null, nullList);

    // SELECT
    List<Keyword> selectList = new ArrayList<>();
    selectList.addAll(aggregates);
    selectList.addAll(functions);
    selectList.add(ClauseOrAggregate.FROM);
    selectList.add(ClauseOrAggregate.MATCH);
    allowedKeywords.put(ClauseOrAggregate.SELECT, selectList);

    // FROM
    List<Keyword> fromList = new ArrayList<>();
    fromList.add(ClauseOrAggregate.MATCH);
    allowedKeywords.put(ClauseOrAggregate.FROM, fromList);

    // MATCH
    List<Keyword> matchList = new ArrayList<>();
    matchList.add(ClauseOrAggregate.WHERE);
    matchList.add(ClauseOrAggregate.GROUP_BY);
    matchList.add(ClauseOrAggregate.HAVING);
    matchList.add(ClauseOrAggregate.ORDER_BY);
    matchList.add(ClauseOrAggregate.LIMIT);
    matchList.add(ClauseOrAggregate.OFFSET);
    allowedKeywords.put(ClauseOrAggregate.MATCH, matchList);

    // WHERE
    List<Keyword> whereList = new ArrayList<>();
    whereList.addAll(aggregates);
    whereList.addAll(functions);
    whereList.add(ClauseOrAggregate.GROUP_BY);
    whereList.add(ClauseOrAggregate.HAVING);
    whereList.add(ClauseOrAggregate.ORDER_BY);
    whereList.add(ClauseOrAggregate.LIMIT);
    whereList.add(ClauseOrAggregate.OFFSET);
    allowedKeywords.put(ClauseOrAggregate.WHERE, whereList);

    // GROUP BY
    List<Keyword> groupByList = new ArrayList<>();
    groupByList.add(ClauseOrAggregate.HAVING);
    groupByList.add(ClauseOrAggregate.ORDER_BY);
    groupByList.add(ClauseOrAggregate.LIMIT);
    groupByList.add(ClauseOrAggregate.OFFSET);
    allowedKeywords.put(ClauseOrAggregate.GROUP_BY, groupByList);

    // HAVING
    List<Keyword> havingList = new ArrayList<>();
    havingList.addAll(aggregates);
    havingList.addAll(functions);
    havingList.add(ClauseOrAggregate.ORDER_BY);
    havingList.add(ClauseOrAggregate.LIMIT);
    havingList.add(ClauseOrAggregate.OFFSET);
    allowedKeywords.put(ClauseOrAggregate.HAVING, havingList);

    // ORDER BY
    List<Keyword> orderByList = new ArrayList<>();
    orderByList.addAll(aggregates);
    orderByList.addAll(functions);
    orderByList.add(ClauseOrAggregate.LIMIT);
    orderByList.add(ClauseOrAggregate.OFFSET);
    allowedKeywords.put(ClauseOrAggregate.ORDER_BY, orderByList);

    // LIMIT
    List<Keyword> limitList = new ArrayList<>();
    limitList.add(ClauseOrAggregate.OFFSET);
    allowedKeywords.put(ClauseOrAggregate.LIMIT, limitList);

    // OFFSET
    List<Keyword> offsetList = new ArrayList<>();
    offsetList.add(ClauseOrAggregate.LIMIT);
    allowedKeywords.put(ClauseOrAggregate.OFFSET, offsetList);

    return allowedKeywords;
  }

  private static PgqlCompletion getKeywordCompletion(String beforeQuery, String afterQuery, Keyword keyword) {
    String keywordExpression = keyword.getStringExpression();
    if (afterQuery.contains(keywordExpression)) {
      return null;
    }
    String[] keywordParts = StringUtils.split(keywordExpression);
    String[] words = StringUtils.split(beforeQuery.toUpperCase());
    int idxLastWord = words.length - 1;
    boolean isKeyword = false;
    int partsIdx;
    // Check if last word before cursor is a prefix of some of the keyword parts
    for (partsIdx = keywordParts.length - 1; partsIdx >= 0 && !isKeyword; partsIdx--) {
      String currentPart = keywordParts[partsIdx];
      // If last word is prefix of some of the keyword parts, then all the previous
      // words should be equal to the corresponding previous keyword parts, for example:
      // IS NOT NULL <-- keyword parts
      // |equal | prefix
      // IS N <-- words
      isKeyword = currentPart.startsWith(words[idxLastWord]);
      for (int idx = 1; isKeyword && idx <= partsIdx; idx++) {
        isKeyword = keywordParts[partsIdx - idx].equals(words[idxLastWord - idx]);
      }
    }
    if (!isKeyword) {
      return null;
    }
    // Get the missing part to complete the keyword
    String completion = keywordParts[partsIdx + 1].substring(words[idxLastWord].length());
    for (int idx = partsIdx + 2; idx < keywordParts.length; idx++) {
      completion += " " + keywordParts[idx];
    }
    // Return upper case or lower case completion depending on the last character of the word
    if (!Character.isUpperCase(beforeQuery.charAt(beforeQuery.length() - 1))) {
      completion = completion.toLowerCase();
    }
    return keyword.getCompletion(completion);
  }

  private static List<PgqlCompletion> getIncompleteKeywords(String beforeQuery, String afterQuery,
      ClauseOrAggregate currentClause) {
    List<PgqlCompletion> keywords = new ArrayList<>();
    for (Keyword allowedKeyword : allowedClauseKeywords.get(currentClause)) {
      PgqlCompletion completion = getKeywordCompletion(beforeQuery, afterQuery, allowedKeyword);
      if (completion != null) {
        keywords.add(completion);
      }
    }
    return keywords;
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
    List<PgqlCompletion> functions = new ArrayList<>();
    for (Function f : Function.values()) {
      functions.add(f.getCompletion());
    }
    return functions;
  }

  public static List<PgqlCompletion> aggregations() {
    return completions(//
        completion("COUNT(*)", "count the number of matches"), //
        ClauseOrAggregate.COUNT.getCompletion(), //
        ClauseOrAggregate.LISTAGG.getCompletion(), //
        ClauseOrAggregate.MIN.getCompletion(), //
        ClauseOrAggregate.MAX.getCompletion(), //
        ClauseOrAggregate.AVG.getCompletion(), //
        ClauseOrAggregate.SUM.getCompletion());
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
