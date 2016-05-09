/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class OrderByElem {

  private final QueryExpression exp;
  
  private final boolean ascending;

  public QueryExpression getExp() {
    return exp;
  }

  public boolean isAscending() {
    return ascending;
  }

  public OrderByElem(QueryExpression exp, boolean ascending) {
    this.exp = exp;
    this.ascending = ascending;
  }

  @Override
  public String toString() {
    return (ascending ? "ASC" : "DESC") + "(" + exp + ")";
  }
}
