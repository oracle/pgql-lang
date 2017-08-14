/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

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

  public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("(\\w)+$");

  public static List<PgqlCompletion> generate(Pgql pgql, Iterable<ICompletion> spoofaxCompletions, String queryString,
      int cursor, PgqlCompletionContext ctx) throws PgqlException {

    // labels
    if (queryString.charAt(cursor - 1) == ':') {
      String queryUpToCursor = queryString.substring(0, cursor - 2);
      if (queryUpToCursor.lastIndexOf('(') > queryUpToCursor.lastIndexOf('[')) {
        return ctx.getVertexLabels().stream().map(lbl -> new PgqlCompletion(lbl, lbl, "vertex label"))
            .collect(Collectors.toList());
      } else {
        return ctx.getEdgeLabels().stream().map(lbl -> new PgqlCompletion(lbl, lbl, "edge label"))
            .collect(Collectors.toList());
      }
      // properties
    } else if (queryString.charAt(cursor - 1) == '.') {
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
        return ctx.getVertexProperties().stream().map(prop -> new PgqlCompletion(prop, prop, "vertex property"))
            .collect(Collectors.toList());
      }

      Set<VertexPairConnection> edges = graphPattern.getConnections();
      boolean isEdgeVariable = edges.stream() //
          .filter((n) -> n.getVariableType() == VariableType.EDGE) //
          .map(VertexPairConnection::getName) //
          .anyMatch(variableName::equals);
      if (isEdgeVariable) {
        return ctx.getEdgeProperties().stream().map(prop -> new PgqlCompletion(prop, prop, "edge property"))
            .collect(Collectors.toList());
      }
    } else if (spoofaxCompletions != null) {
      for (ICompletion c : spoofaxCompletions) {
        System.out.println(c + ", " + c.prefix());
      }
    }
    return Collections.emptyList();
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
