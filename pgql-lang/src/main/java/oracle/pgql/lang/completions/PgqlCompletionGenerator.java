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

import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;

public class PgqlCompletionGenerator {

  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("(\\w)+$");

  private static final String[] BLACK_LIST = { "Legacy", "Neq1", "UMin", "Null", "BindVariable", "PropRef", //
      "Integer", "Decimal", "String", "ExpAsVar" };

  private static final String[] BASIC_EXPRESSIONS_ARRAY = { "Not", "And", "Or", "Mul", "Add", "Div", "Mod", "Sub", "Eq",
      "Gt", "Lt", "Gte", "Lte", "Neq2" };

  private static final String[] AGGREGATIONS_ARRAY = { "COUNT", "MIN", "MAX", "AVG", "SUM" };

  private static final ArrayList<String> BASIC_EXPRESSIONS = new ArrayList<>(Arrays.asList(BASIC_EXPRESSIONS_ARRAY));

  private static final ArrayList<String> AGGREGATIONS = new ArrayList<>(Arrays.asList(AGGREGATIONS_ARRAY));

  public static List<PgqlCompletion> generate(Pgql pgql, Iterable<ICompletion> spoofaxCompletions, String queryString,
      int cursor, PgqlCompletionContext ctx)
      throws PgqlException {

    // labels
    if (queryString.charAt(cursor - 1) == ':') {
      return generateLabelSuggestions(queryString, cursor, ctx);
      // properties
    } else if (queryString.charAt(cursor - 1) == '.') {
      return generatePropertySuggestions(pgql, queryString, cursor, ctx);
    } else if (spoofaxCompletions != null) {
      List<PgqlCompletion> variables = new ArrayList<>();
      List<PgqlCompletion> functions = new ArrayList<>();
      List<PgqlCompletion> basicExpressions = new ArrayList<>();
      List<PgqlCompletion> aggregations = new ArrayList<>();
      List<PgqlCompletion> otherCompletions = new ArrayList<>();

      for (ICompletion c : spoofaxCompletions) {

        String text = c.text().substring(c.prefix().length() + 1, c.text().length() - c.suffix().length());

        if (isBlackListed(c)) {
          continue;
        } else if (BASIC_EXPRESSIONS.contains(c.name())) {
          basicExpressions.add(new PgqlCompletion(fixText(text), c.name()));
          continue;
        } else if (AGGREGATIONS.contains(c.name())) {
          aggregations.add(new PgqlCompletion(fixText(text), c.name()));
          continue;
        }

        switch (c.name()) {
          case "VarRef":
            PgqlResult result = pgql.parse(queryString);
            GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();
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
                variables.add(
                    new PgqlCompletion(connection.getName(), connection.getVariableType().toString().toLowerCase()));
              }
            }
            break;
          case "True":
          case "False":
            otherCompletions.add(new PgqlCompletion(text, c.name()));
            break;
          case "Date":
            otherCompletions.add(new PgqlCompletion("DATE '2017-01-01'", c.name()));
            break;
          case "Time":
            otherCompletions.add(new PgqlCompletion("TIME '20:15:00'", c.name()));
            break;
          case "Timestamp":
            otherCompletions.add(new PgqlCompletion("TIMESTAMP '2017-01-01 20:15:00'", c.name()));
            break;
          case "Cast":
            otherCompletions.add(new PgqlCompletion("CAST(exp AS type)", c.name()));
            break;
          case "IsNull":
          case "IsNotNull":
            otherCompletions.add(new PgqlCompletion(fixText(text), c.name()));
            break;
          case "Exists":
            functions.add(new PgqlCompletion("EXISTS( SELECT * MATCH (n) )", c.name()));
            break;
          case "FunctionCall":
            addFunctions(functions);
            break;
          case "VarAssign":
            functions.add(new PgqlCompletion(" AS var", "variable asignment"));
            break;
          case "FromClause":
            functions.add(new PgqlCompletion("FROM g", "FROM clause"));
            break;
          default:
            throw new UnsupportedOperationException(
                "Completion " + c.name() + " with text '" + text + "' not supported");
        }
      }

      List<PgqlCompletion> result = variables;
      result.addAll(otherCompletions);
      result.addAll(aggregations);
      result.addAll(basicExpressions);

      return result;
    }
    return Collections.emptyList();
  }

  private static void addFunctions(List<PgqlCompletion> functions) {
    functions.add(new PgqlCompletion("id(elem)", "get identifier"));
    functions.add(new PgqlCompletion("label(edge)", "get label of edge"));
    functions.add(new PgqlCompletion("labels(vertex)", "get labels of vertex"));
    functions.add(new PgqlCompletion("has_label(elem, lbl)", "check if elem has label"));
    functions.add(new PgqlCompletion("all_different(exp1, exp2, ...)", "check if values are all different"));
    functions.add(new PgqlCompletion("in_degree(vertex)", "number of incoming neighbors"));
    functions.add(new PgqlCompletion("out_degree(vertex)", "number of outgoin neighbors"));
  }

  private static String fixText(String text) {
    return text.replace("##CURSOR##", "").toUpperCase().replace("[[EXP]]", "exp").replace("[[STAROREXP]]", "*")
        .replace("$EXP", "exp").replace("$STAROREXP", "*");
  }

  private static boolean isBlackListed(ICompletion c) {
    for (String s : BLACK_LIST) {
      if (c.name().startsWith(s)) {
        return true;
      }
    }
    return false;
  }

  private static List<PgqlCompletion> generatePropertySuggestions(Pgql pgql, String queryString, int cursor,
      PgqlCompletionContext ctx)
      throws PgqlException {
    String variableName = parseIdentifier(queryString, cursor - 1);
    if (variableName == null) {
      return Collections.emptyList();
    }
    PgqlResult result = pgql.parse(queryString);
    GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();

    Set<QueryVertex> vertices = graphPattern.getVertices();
    boolean isVertexVariable = vertices.stream() //
        .map(QueryVertex::getName) //
        .anyMatch(variableName::equals);
    if (isVertexVariable) {
      return ctx.getVertexProperties().stream().map(prop -> new PgqlCompletion(prop, "vertex property"))
          .collect(Collectors.toList());
    }

    Set<VertexPairConnection> edges = graphPattern.getConnections();
    boolean isEdgeVariable = edges.stream() //
        .filter((n) -> n.getVariableType() == VariableType.EDGE) //
        .map(VertexPairConnection::getName) //
        .anyMatch(variableName::equals);
    if (isEdgeVariable) {
      return ctx.getEdgeProperties().stream().map(prop -> new PgqlCompletion(prop, "edge property"))
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  private static List<PgqlCompletion> generateLabelSuggestions(String queryString, int cursor,
      PgqlCompletionContext ctx) {
    String queryUpToCursor = queryString.substring(0, cursor - 2);
    if (queryUpToCursor.lastIndexOf('(') > queryUpToCursor.lastIndexOf('[')) {
      return ctx.getVertexLabels().stream().map(lbl -> new PgqlCompletion(lbl, "vertex label"))
          .collect(Collectors.toList());
    } else {
      return ctx.getEdgeLabels().stream().map(lbl -> new PgqlCompletion(lbl, "edge label"))
          .collect(Collectors.toList());
    }
  }

  private static String parseIdentifier(String queryString, int positionLastCharacter) {
    queryString = queryString.substring(0, positionLastCharacter);
    Matcher matcher = IDENTIFIER_PATTERN.matcher(queryString);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }
}
