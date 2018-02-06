/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.example;

import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlResult;

public class Main {

  public static void main(String[] args) throws PgqlException {

    try (Pgql pgql = new Pgql()) {

      // parse query and print graph query
      PgqlResult result1 = pgql.parse("SELECT n, n.\"\\\"\", n.\"'!@#$%^&*()招弟\\t\\n\\r\\b\\f\", n.\"my graph\" FROM g MATCH (n)");
      System.out.println(result1.getGraphQuery());

      // parse query with errors and print error messages
      PgqlResult result2 = pgql.parse("SELECT x, y, WHERE (n) -[e]-> (m)");
      System.out.println(result2.getErrorMessages());
    }
  }
}
