/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class Projection {

  private boolean distinct;

  private List<ExpAsVar> elements;

  public Projection(boolean distinct, List<ExpAsVar> elements) {
    this.distinct = distinct;
    this.elements = elements;
  }

  /**
   * @deprecated Replaced by {@link #isDistinct()}
   */
  @Deprecated
  public boolean hasDistinct() {
    return distinct;
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  public List<ExpAsVar> getElements() {
    return elements;
  }

  public void setElements(List<ExpAsVar> elements) {
    this.elements = elements;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
  }

  @Override
  public int hashCode() {
    return 31;
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
