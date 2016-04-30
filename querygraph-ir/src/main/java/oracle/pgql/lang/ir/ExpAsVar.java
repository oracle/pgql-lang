/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class ExpAsVar extends QueryVar {

  public final QueryExpression exp;

  public ExpAsVar(QueryExpression exp, String name) {
    super(name);
    this.exp = exp;
  }

  @Override
  public String toString() {
    return exp + " AS " + name;
  }
}
