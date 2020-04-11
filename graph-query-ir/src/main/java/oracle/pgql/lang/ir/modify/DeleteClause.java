/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

public class DeleteClause implements Modification {

  List<VarRef> deletions;

  public DeleteClause(List<VarRef> deletions) {
    this.deletions = deletions;
  }

  public List<VarRef> getDeletions() {
    return deletions;
  }

  public void setDeletions(List<VarRef> deletions) {
    this.deletions = deletions;
  }

  @Override
  public ModificationType getModificationType() {
    return ModificationType.DELETE;
  }

  @Override
  public String toString() {
    return "DELETE " + deletions.stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(", "));
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
    DeleteClause other = (DeleteClause) obj;
    if (deletions == null) {
      if (other.deletions != null)
        return false;
    } else if (!deletions.equals(other.deletions))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
