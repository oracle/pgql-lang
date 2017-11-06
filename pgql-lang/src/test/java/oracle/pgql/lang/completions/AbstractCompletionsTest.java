/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;

import oracle.pgql.lang.Pgql;

public abstract class AbstractCompletionsTest {

  protected static Pgql pgql;

  @BeforeClass
  public static void setUp() throws Exception {
    pgql = new Pgql();
  }

  protected abstract PgqlCompletionContext getCompletionContext();

  protected void check(String query, List<PgqlCompletion> expected) throws Exception {
    check(query, expected, false);
  }

  protected void checkSubset(String query, List<PgqlCompletion> expected) throws Exception {
    check(query, expected, true);
  }

  private void check(String query, List<PgqlCompletion> expected, boolean subset) throws Exception {
    int cursor = query.indexOf("???");
    query = query.replaceAll("\\?\\?\\?", "");

    List<PgqlCompletion> actual = pgql.generateCompletions(query, cursor, getCompletionContext());
    String actualAsString = actual.stream().map(c -> c.toString()).collect(Collectors.joining("\n"));

    if (subset) {
      for (PgqlCompletion completion : expected) {
        String errorMessage = "\nexpected completion\n\n" + completion + "\n\not in\n\n" + actualAsString + "\n";
        assertTrue(errorMessage, actual.contains(completion));
      }
    } else {
      String expectedAsString = expected.stream().map(c -> c.toString()).collect(Collectors.joining("\n"));
      String errorMessage = "\nexpected\n\n" + expectedAsString + "\n\nactual\n\n" + actualAsString + "\n";
      assertEquals(errorMessage, expected, actual);
    }
  }
}
