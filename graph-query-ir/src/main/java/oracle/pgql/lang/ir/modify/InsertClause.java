/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

public class InsertClause implements Modification {

  private List<Insertion> insertions;

  public InsertClause(List<Insertion> insertions) {
    this.insertions = insertions;
  }

  public List<Insertion> getInsertions() {
    return insertions;
  }

  public void setInsertions(List<Insertion> insertions) {
    this.insertions = insertions;
  }

  @Override
  public String toString() {
    return "INSERT " + insertions.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
  }

  @Override
  public ModificationType getModificationType() {
    return ModificationType.INSERT;
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
    InsertClause other = (InsertClause) obj;
    if (insertions == null) {
      if (other.insertions != null)
        return false;
    } else if (!insertions.equals(other.insertions))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
