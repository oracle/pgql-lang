/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.update;

import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;

public class PropertyUpdate {

  public PropertyAccess propertyAccess;

  public QueryExpression valueExpression;

  public PropertyUpdate(PropertyAccess propertyAccess, QueryExpression valueExpression) {
    this.propertyAccess = propertyAccess;
    this.valueExpression = valueExpression;
  }

  public PropertyAccess getPropertyAccess() {
    return propertyAccess;
  }

  public void setPropertyAccess(PropertyAccess propertyAccess) {
    this.propertyAccess = propertyAccess;
  }

  public QueryExpression getValueExpression() {
    return valueExpression;
  }

  public void setValueExpression(QueryExpression valueExpression) {
    this.valueExpression = valueExpression;
  }

  @Override
  public String toString() {
    return propertyAccess + " = " + valueExpression;
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
    PropertyUpdate other = (PropertyUpdate) obj;
    if (propertyAccess == null) {
      if (other.propertyAccess != null)
        return false;
    } else if (!propertyAccess.equals(other.propertyAccess))
      return false;
    if (valueExpression == null) {
      if (other.valueExpression != null)
        return false;
    } else if (!valueExpression.equals(other.valueExpression))
      return false;
    return true;
  }
}
