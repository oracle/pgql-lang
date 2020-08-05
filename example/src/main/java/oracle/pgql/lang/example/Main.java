/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.example;

import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlResult;

public class Main {

  public static void main(String[] args) throws PgqlException {

    try (Pgql pgql = new Pgql()) {

      // parse query and print graph query
      PgqlResult result1 = pgql.parse("SELECT n FROM MATCH (n:Person) -[e:likes]-> (m:Person) WHERE n.name = 'Dave'");
      System.out.println(result1.getPgqlStatement());

      // parse query with errors and print error messages
      PgqlResult result2 = pgql.parse("SELECT x, y FROM MATCH (n) -[e]-> (m)");
      System.out.println(result2.getErrorMessages());
    }
  }
}
