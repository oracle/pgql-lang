/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completion;

public enum Function implements Keyword {
  ID("ID", "id", "(elem)", "get identifier"), //
  LABEL("LABEL", "label", "(edge)", "get label of edge"), //
  LABELS("LABELS", "labels", "(vertex)", "get labels of vertex"), //
  HAS_LABEL("HAS_LABEL", "has_label", "(elem, lbl)", "check if elem has label"), //
  ALL_DIFFERENT("ALL_DIFFERENT", "all_different", "(exp1, exp2, ...)", "check if values are all different"), //
  IN_DEGREE("IN_DEGREE", "in_degree", "(vertex)", "number of incoming neighbors"), //
  OUT_DEGREE("OUT_DEGREE", "out_degree", "(vertex)", "number of outgoin neighbors"), //
  ABS("ABS", "abs", "(exp)", "absolute value of the expression"), //
  CEIL("CEIL", "ceil", "(exp)", "smallest integer value that is greater than or equal to the expression"), //
  CEILING("CEILING", "ceiling", "(exp)", "smallest integer value that is greater than or equal to the expression"), //
  FLOOR("FLOOR", "floor", "(exp)", "biggest integer value that is smaller than or equal to the expression"), //
  ROUND("ROUND", "round", "(exp)", "closest integer to the given expression"), //
  EXTRACT("EXTRACT", "EXTRACT", " field FROM exp", "extract datetime field out of a datetime expression"), //
  IN("IN", "exp IN", "(val1, val2, ...)", "check if expression is in array of values"), //
  IS_NULL("IS NULL", "exp IS NULL", "", "check if expression is null"), //
  IS_NOT_NULL("IS NOT NULL", "exp IS NOT NULL", "", "check if expression is not null");
  
  private final Fields fields;
  
  Function(String... parameters) {
    this.fields = new Fields(parameters);
  }
  
  @Override
  public Fields getFields() {
    return fields;
  }
}
