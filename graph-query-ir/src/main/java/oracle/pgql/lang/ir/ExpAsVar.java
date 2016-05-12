/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class ExpAsVar extends QueryVariable {

  private final QueryExpression exp;

  public ExpAsVar(QueryExpression exp, String name) {
    super(name);
    this.exp = exp;
  }

  public QueryExpression getExp() {
    return exp;
  }
  
  @Override
  public String toString() {
    return exp + " AS " + name;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EXP_AS_VAR;
  }
}
