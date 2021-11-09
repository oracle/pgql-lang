/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.springbootapp;

import oracle.pgql.lang.PgqlException;

import oracle.pgql.lang.Pgql;

public class Main {

  public static void main(String[] args) throws PgqlException {

    try (Pgql pgql = new Pgql()) {
      pgql.parse("SELECT COUNT(*) FROM g MATCH (n)");
      System.out.println("success");
    }
  }
}
