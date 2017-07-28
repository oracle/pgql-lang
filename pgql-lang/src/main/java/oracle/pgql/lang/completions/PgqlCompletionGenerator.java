/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PgqlCompletionGenerator {

  public static List<PgqlCompletion> generate(String query, int cursor, PgqlCompletionContext ctx) {
    List<PgqlCompletion> results = new ArrayList<>();

    // labels
    if (query.charAt(cursor - 1) == ':') {
      String queryUpToCursor = query.substring(0, cursor - 2);
      if (queryUpToCursor.lastIndexOf('(') > queryUpToCursor.lastIndexOf('[')) {
        results.addAll(ctx.getVertexLabels().stream().map(lbl -> new PgqlCompletion(lbl, lbl, "vertex label")).collect(Collectors.toList()));
      } else {
        results.addAll(ctx.getEdgeLabels().stream().map(lbl -> new PgqlCompletion(lbl, lbl, "edge label")).collect(Collectors.toList()));
      }
    }
    return results;
  }
}
