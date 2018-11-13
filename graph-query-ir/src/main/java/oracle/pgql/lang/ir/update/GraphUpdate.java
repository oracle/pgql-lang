/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.update;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

import static oracle.pgql.lang.ir.PgqlUtils.BETA_FEATURES_FLAG;

public class GraphUpdate {

  private List<SetPropertyExpression> setPropertyExpressions;

  public GraphUpdate(List<SetPropertyExpression> propertyUpdates) {
    this.setPropertyExpressions = propertyUpdates;
  }

  public List<SetPropertyExpression> getSetPropertyExpressions() {
    return setPropertyExpressions;
  }

  public void setPropertyUpdates(List<SetPropertyExpression> setPropertyExpressions) {
    this.setPropertyExpressions = setPropertyExpressions;
  }

  @Override
  public String toString() {
    return "UPDATE" + BETA_FEATURES_FLAG + " " + setPropertyExpressions.stream() //
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
    GraphUpdate other = (GraphUpdate) obj;
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
