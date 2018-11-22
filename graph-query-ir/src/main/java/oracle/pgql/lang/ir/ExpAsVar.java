/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class ExpAsVar extends QueryVariable {

  private QueryExpression exp;

  private boolean isContainedInSelectClause;

  /**
   * @param exp
   *          an expression
   * @param name
   *          the name with which the the element can be referred to in the result set
   * @param anonymous
   *          false if the name was provided via the query (i.e. exp AS name), true if the name was not provided via the
   *          query (i.e. exp) but via some other mechanism
   */
  public ExpAsVar(QueryExpression exp, String name, boolean anonymous) {
    this(exp, name, anonymous, true);
  }

  /**
   * @param exp
   *          an expression
   * @param name
   *          the name with which the the element can be referred to in the result set
   * @param anonymous
   *          false if the name was provided via the query (i.e. exp AS name), true if the name was not provided via the
   *          query (i.e. exp) but via some other mechanism
   * @param isContainedInSelectClause
   *          true if in SELECT, false if in GROUP BY
   */
  public ExpAsVar(QueryExpression exp, String name, boolean anonymous, boolean isContainedInSelectClause) {
    super(name, anonymous);
    this.exp = exp;
    this.isContainedInSelectClause = isContainedInSelectClause;
  }

  public QueryExpression getExp() {
    return exp;
  }

  public void setExp(QueryExpression exp) {
    this.exp = exp;
  }

  public boolean isContainedInSelectClause() {
    return isContainedInSelectClause;
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
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ExpAsVar other = (ExpAsVar) obj;
    if (exp == null) {
      if (other.exp != null)
        return false;
    } else if (!exp.equals(other.exp))
      return false;
    if (isContainedInSelectClause != other.isContainedInSelectClause)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
