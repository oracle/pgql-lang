/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class Projection {

  private final boolean distinct;

  private final List<ExpAsVar> elements;

  public Projection(boolean distinct, List<ExpAsVar> elements) {
    this.distinct = distinct;
    this.elements = elements;
  }

  public boolean hasDistinct() {
    return distinct;
  }

  public List<ExpAsVar> getElements() {
    return elements;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (distinct ? 1231 : 1237);
    result = prime * result + ((elements == null) ? 0 : elements.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Projection other = (Projection) obj;
    if (distinct != other.distinct)
      return false;
    if (elements == null) {
      if (other.elements != null)
        return false;
    } else if (!elements.equals(other.elements))
      return false;
    return true;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
