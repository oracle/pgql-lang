/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

public class Property {

  /**
   * The value expression.
   */
  QueryExpression valueExpression;

  /**
   * The property name.
   */
  String propertyName;

  /**
   * Constructor with column name and property name.
   */
  public Property(QueryExpression valueExpression, String propertyName) {
    this.valueExpression = valueExpression;
    this.propertyName = propertyName;
  }

  public QueryExpression getValueExpression() {
    return valueExpression;
  }

  public void setValueExpression(QueryExpression valueExpression) {
    this.valueExpression = valueExpression;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  /**
   * @deprecated use getValueExpression() instead
   */
  @Deprecated
  public String getColumnName() {
    if (valueExpression.getExpType() == ExpressionType.VARREF) {
      VarRef varRef = (VarRef) valueExpression;
      return varRef.getVariable().getName();
    } else {
      return null;
    }
  }

  @Override
  public String toString() {
    if (getColumnName() != null && getColumnName().equals(propertyName)) {
      return printIdentifier(propertyName, false);
    } else {
      return valueExpression + " AS " + printIdentifier(propertyName, false);
    }
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
    Property other = (Property) obj;
    if (propertyName == null) {
      if (other.propertyName != null)
        return false;
    } else if (!propertyName.equals(other.propertyName))
      return false;
    if (valueExpression == null) {
      if (other.valueExpression != null)
        return false;
    } else if (!valueExpression.equals(other.valueExpression))
      return false;
    return true;
  }
}
