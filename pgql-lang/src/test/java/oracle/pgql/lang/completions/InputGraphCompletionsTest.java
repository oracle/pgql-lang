/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static oracle.pgql.lang.completions.PgqlCompletionGenerator.MATCH_CLAUSE_COMPLETION;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.completion;
import static oracle.pgql.lang.completions.PgqlCompletionGenerator.completions;

import java.util.List;

import org.junit.Test;

public class InputGraphCompletionsTest extends AbstractCompletionsTest {

  @Test
  public void testGraphName() throws Exception {
    List<PgqlCompletion> expected = completions(completion("g", "graph name"));

    String query = "SELECT * FROM ???";
    check(query, expected);
  }

  @Test
  public void testMatchClause() throws Exception {
    List<PgqlCompletion> expected = completions(MATCH_CLAUSE_COMPLETION);

    String query = "SELECT * FROM g ???";
    check(query, expected);
  }
}
