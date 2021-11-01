/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class OrderBy {

  private List<OrderByElem> elements;

  public OrderBy(List<OrderByElem> elements) {
    this.elements = elements;
  }

  public List<OrderByElem> getElements() {
    return elements;
  }

  public void setElements(List<OrderByElem> elements) {
    this.elements = elements;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    OrderBy orderBy = (OrderBy) o;

    return elements.equals(orderBy.elements);
  }

  @Override
  public int hashCode() {
    return 31;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
