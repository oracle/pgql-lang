/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.update;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

import static oracle.pgql.lang.ir.PgqlUtils.BETA_FEATURES_FLAG;

public class GraphUpdate {

  private List<PropertyUpdate> propertyUpdates;

  public GraphUpdate(List<PropertyUpdate> propertyUpdates) {
    this.propertyUpdates = propertyUpdates;
  }

  public List<PropertyUpdate> getPropertyUpdates() {
    return propertyUpdates;
  }

  public void setPropertyUpdates(List<PropertyUpdate> propertyUpdates) {
    this.propertyUpdates = propertyUpdates;
  }

  @Override
  public String toString() {
    return "UPDATE" + BETA_FEATURES_FLAG + " " + propertyUpdates.stream() //
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
    if (propertyUpdates == null) {
      if (other.propertyUpdates != null)
        return false;
    } else if (!propertyUpdates.equals(other.propertyUpdates))
      return false;
    return true;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
