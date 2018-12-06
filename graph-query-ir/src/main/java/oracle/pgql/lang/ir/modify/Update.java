/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

public class Update implements Modification {

  private List<VarRef> elements;

  private List<SetPropertyExpression> setPropertyExpressions;

  public Update(List<VarRef> elements, List<SetPropertyExpression> setPropertyExpressions) {
    this.elements = elements;
    this.setPropertyExpressions = setPropertyExpressions;
  }

  public List<SetPropertyExpression> getSetPropertyExpressions() {
    return setPropertyExpressions;
  }

  public void setSetPropertyExpressions(List<SetPropertyExpression> setProperties) {
    this.setPropertyExpressions = setProperties;
  }

  public List<VarRef> getElements() {
    return elements;
  }

  public void setElements(List<VarRef> elements) {
    this.elements = elements;
  }

  @Override
  public String toString() {
    String result = "UPDATE " + elements.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
    if (!elements.isEmpty()) {
      result += " SET PROPERTIES "
          + setPropertyExpressions.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
    }
    return result;
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
    Update other = (Update) obj;
    if (elements == null) {
      if (other.elements != null)
        return false;
    } else if (!elements.equals(other.elements))
      return false;
    if (setPropertyExpressions == null) {
      if (other.setPropertyExpressions != null)
        return false;
    } else if (!setPropertyExpressions.equals(other.setPropertyExpressions))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
