/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class Projection {

  private final List<ExpAsVar> elements;

  public Projection(List<ExpAsVar> elements) {
    this.elements = elements;
  }

  public List<ExpAsVar> getElements() {
    return elements;
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

    Projection that = (Projection) o;

    return elements.equals(that.elements);
  }

  @Override
  public int hashCode() {
    return elements.hashCode();
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
