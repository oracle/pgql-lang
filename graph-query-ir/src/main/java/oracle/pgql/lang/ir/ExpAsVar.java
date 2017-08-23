/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

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
    return printPgqlString(this);
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EXP_AS_VAR;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ExpAsVar expAsVar = (ExpAsVar) o;

    if (anonymous != expAsVar.anonymous) {
      return false;
    }
    if (!name.equals(expAsVar.name)) {
      return false;
    }
    return exp.equals(expAsVar.exp);
  }

  @Override
  public int hashCode() {
    int result = (anonymous ? 1 : 0);
    result = 31 * result + name.hashCode();
    result = 31 * result + exp.hashCode();
    return result;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
