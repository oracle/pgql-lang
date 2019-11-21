/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

public class Update {

  private VarRef element;

  private List<SetPropertyExpression> setPropertyExpressions;

  public Update(VarRef element, List<SetPropertyExpression> setPropertyExpressions) {
    this.element = element;
    this.setPropertyExpressions = setPropertyExpressions;
  }

  public List<SetPropertyExpression> getSetPropertyExpressions() {
    return setPropertyExpressions;
  }

  public void setSetPropertyExpressions(List<SetPropertyExpression> setProperties) {
    this.setPropertyExpressions = setProperties;
  }

  public VarRef getElement() {
    return element;
  }

  public void setElement(VarRef element) {
    this.element = element;
  }

  @Override
  public String toString() {
    String result = element.toString();
    if (!setPropertyExpressions.isEmpty()) {
      result += " SET ( "
          + setPropertyExpressions.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + " )";
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
    if (element == null) {
      if (other.element != null)
        return false;
    } else if (!element.equals(other.element))
      return false;
    if (setPropertyExpressions == null) {
      if (other.setPropertyExpressions != null)
        return false;
    } else if (!setPropertyExpressions.equals(other.setPropertyExpressions))
      return false;
    return true;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
