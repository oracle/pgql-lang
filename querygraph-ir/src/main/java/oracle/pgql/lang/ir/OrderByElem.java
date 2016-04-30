/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class OrderByElem {

  public final QueryExpression exp;
  public final boolean ascending;

  public OrderByElem(QueryExpression exp, boolean ascending) {
    this.exp = exp;
    this.ascending = ascending;
  }

  @Override
  public String toString() {
    return (ascending ? "ASC" : "DESC") + "(" + exp + ")";
  }
}
