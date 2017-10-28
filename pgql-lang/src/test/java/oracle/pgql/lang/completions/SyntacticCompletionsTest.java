/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import oracle.pgql.lang.completions.PgqlCompletion;
import oracle.pgql.lang.completions.PgqlCompletionContext;

public class SyntacticCompletionsTest extends AbstractCompletionsTest {

  @Override
  protected PgqlCompletionContext getCompletionContext() {
    return new PgqlCompletionContext() {

      @Override
      public List<String> getVertexProperties() {
        return Collections.emptyList();
      }

      @Override
      public List<String> getEdgeProperties() {
        return Collections.emptyList();
      }

      @Override
      public List<String> getVertexLabels() {
        return Collections.emptyList();
      }

      @Override
      public List<String> getEdgeLabels() {
        return Collections.emptyList();
      }
    };
  }

  @Test
  public void testVertexProps() throws Exception {

    List<PgqlCompletion> expected = expected(//
        completion("m", "vertex"), //
        completion("n", "vertex"), //
        completion("e", "edge"), //
        completion("true", "True"), //
        completion("false", "False"), //
        completion("DATE '2017-01-01'", "Date"), //
        completion("TIME '20:15:00'", "Time"), //
        completion("TIMESTAMP '2017-01-01 20:15:00'", "Timestamp"), //
        completion("CAST(exp AS type)", "Cast"), //
        completion("exp IS NULL", "IsNull"), //
        completion("exp IS NOT NULL", "IsNotNull"), //
        completion("COUNT(*)", "COUNT"), //
        completion("MIN(exp)", "MIN"), //
        completion("MAX(exp)", "MAX"), //
        completion("SUM(exp)", "SUM"), //
        completion("AVG(exp)", "AVG"), //
        completion("NOT exp", "Not"), //
        completion("exp AND exp", "And"), //
        completion("exp OR exp", "Or"), //
        completion("exp * exp", "Mul"), //
        completion("exp + exp", "Add"), //
        completion("exp / exp", "Div"), //
        completion("exp % exp", "Mod"), //
        completion("exp - exp", "Sub"), //
        completion("exp = exp", "Eq"), //
        completion("exp > exp", "Gt"), //
        completion("exp < exp", "Lt"), //
        completion("exp >= exp", "Gte"), //
        completion("exp <= exp", "Lte"), //
        completion("exp <> exp", "Neq2"));

    String query = "SELECT 123 MATCH (n) -[e]-> (m) WHERE ???";
    check(query, expected);
  }
}