/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;

public class PgqlCompletionGenerator {

  public static List<PgqlCompletion> generate(Pgql pgql, String queryString, int cursor, PgqlCompletionContext ctx)
      throws PgqlException {
    List<PgqlCompletion> results = new ArrayList<>();

    // labels
    if (queryString.charAt(cursor - 1) == ':') {
      String queryUpToCursor = queryString.substring(0, cursor - 2);
      if (queryUpToCursor.lastIndexOf('(') > queryUpToCursor.lastIndexOf('[')) {
        results.addAll(ctx.getVertexLabels().stream().map(lbl -> new PgqlCompletion(lbl, lbl, "vertex label"))
            .collect(Collectors.toList()));
      } else {
        results.addAll(ctx.getEdgeLabels().stream().map(lbl -> new PgqlCompletion(lbl, lbl, "edge label"))
            .collect(Collectors.toList()));
      }
    } else if (queryString.charAt(cursor - 1) == '.') {
      String variableName = parseIdentifier(queryString, cursor - 1);
      if (variableName == null) {
        return results;
      }
      PgqlResult result = pgql.parse(queryString);
      GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();

      Set<QueryVertex> vertices = graphPattern.getVertices();
      boolean isVertexVariable = vertices.stream() //
          .map(QueryVertex::getName) //
          .anyMatch(variableName::equals);

      Set<VertexPairConnection> edges = graphPattern.getConnections();
      boolean isEdgeVariable = edges.stream() //
          .filter((n) -> n.getVariableType() == VariableType.EDGE) //
          .map(VertexPairConnection::getName) //
          .anyMatch(variableName::equals);

      if (isVertexVariable) {
        results.addAll(ctx.getVertexProperties().stream().map(prop -> new PgqlCompletion(prop, prop, "vertex property"))
            .collect(Collectors.toList()));
      } else if (isEdgeVariable) {
        results.addAll(ctx.getEdgeProperties().stream().map(prop -> new PgqlCompletion(prop, prop, "edge property"))
            .collect(Collectors.toList()));
      }
    }
    return results;
  }

  private static String parseIdentifier(String queryString, int positionLastCharacter) {
    queryString = queryString.substring(0, positionLastCharacter);
    Pattern pattern = Pattern.compile("(\\w)+$");
    Matcher matcher = pattern.matcher(queryString);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }
}
