/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

public class Deletion implements Modification {

  List<VarRef> elements;

  public Deletion(List<VarRef> elements) {
    this.elements = elements;
  }

  public List<VarRef> getElements() {
    return elements;
  }

  public void setElements(List<VarRef> elements) {
    this.elements = elements;
  }

  @Override
  public ModificationType getModificationType() {
    return ModificationType.DELETE;
  }

  @Override
  public String toString() {
    return "DELETE " + elements.stream() //
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
    Deletion other = (Deletion) obj;
    if (elements == null) {
      if (other.elements != null)
        return false;
    } else if (!elements.equals(other.elements))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
