/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

public enum UnaryOperation {

  // logical expressions
  NOT,

  // arithmetic expressions
  UMIN,

  // aggregates
  SUM,
  MIN,
  MAX,
  AVG,
  LISTAGG,
  ARRAY_AGG
}
