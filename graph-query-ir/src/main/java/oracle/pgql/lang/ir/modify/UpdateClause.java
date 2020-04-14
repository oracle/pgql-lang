/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

public class UpdateClause implements Modification {

  private List<Update> updates;

  public UpdateClause(List<Update> updates) {
    this.updates = updates;
  }

  public List<Update> getUpdates() {
    return updates;
  }

  public void setUpdates(List<Update> updates) {
    this.updates = updates;
  }

  @Override
  public String toString() {
    return "UPDATE " + updates.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
  }

  @Override
  public ModificationType getModificationType() {
    return ModificationType.UPDATE;
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
    UpdateClause other = (UpdateClause) obj;
    if (updates == null) {
      if (other.updates != null)
        return false;
    } else if (!updates.equals(other.updates))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
