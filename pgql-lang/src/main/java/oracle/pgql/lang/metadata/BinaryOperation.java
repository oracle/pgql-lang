/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

public enum BinaryOperation {

  // arithmetic expressions
  SUB,
  ADD,
  MUL,
  DIV,
  MOD,

  // logical expressions
  AND,
  OR,

  // relational expressions
  EQUAL,
  NOT_EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL
}
