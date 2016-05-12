/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class ExpAsVar extends QueryVariable {

  private final QueryExpression exp;

  /**
   * @param exp       an expression
   * @param name      the name with which the the element can be referred to in the result set
   * @param anonymous false if the name was provided via the query (i.e. exp AS name), true if the name
   *                  was not provided via the query (i.e. exp) but via some other mechanism
   */
  public ExpAsVar(QueryExpression exp, String name, boolean anonymous) {
    super(name, anonymous);
    this.exp = exp;
  }
  
  public QueryExpression getExp() {
    return exp;
  }
  
  @Override
  public String toString() {
    return anonymous ? exp.toString() : exp + " AS " + name;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EXP_AS_VAR;
  }
}
