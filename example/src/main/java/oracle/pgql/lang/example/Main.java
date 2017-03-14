/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.example;

import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlResult;

public class Main {

  public static void main(String[] args) throws PgqlException {

    Pgql pgql = new Pgql();

    // parse query and print graph query
    PgqlResult result1 = pgql.parse("SELECT DATE '1999-10-14', TIME '13:35:16', TIMESTAMP '1997-07-15 13:35:16' WHERE (n) -[e]-> (m)");
    System.out.println(result1.getGraphQuery());

  }
}
