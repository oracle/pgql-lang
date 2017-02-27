/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.springbootapp;

import oracle.pgql.lang.PgqlException;

import oracle.pgql.lang.Pgql;

public class Main {

  public static void main(String[] args) throws PgqlException {

    new Pgql().parse("SELECT COUNT(*) WHERE (n)");
    System.out.println("success");
  }
}
