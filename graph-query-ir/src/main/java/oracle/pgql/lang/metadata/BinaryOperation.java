/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

public enum BinaryOperation {

  // arithmetic operations
  ADD,
  SUB,
  MUL,
  DIV,
  MOD,

  // relational operations
  EQUAL,
  NOT_EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL,

  // logical operations
  AND,
  OR,

  // other
  STRING_CONCAT
}
