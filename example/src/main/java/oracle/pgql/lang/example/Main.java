/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.example;

import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlResult;

public class Main {

  public static void main(String[] args) throws PgqlException {

    Pgql pgql = new Pgql();

    // parse query and print graph query
    PgqlResult result1 = pgql.parse("SELECT * WHERE () -> (n) <- ()");
    PgqlResult result2 = pgql.parse("SELECT * WHERE () -> (n), (n) <- ()");

    System.out.println(result1.getGraphQuery().equals(result2.getGraphQuery()));
  }
}
