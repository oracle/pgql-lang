/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import oracle.pgql.lang.ir.QueryExpression.Subquery;

public class DerivedTable extends Subquery implements TableExpression {

  private boolean lateral;

  public DerivedTable(SelectQuery query, boolean lateral) {
    super(query);
    this.lateral = lateral;
  }

  public boolean isLateral() {
    return lateral;
  }

  public void setLateral(boolean lateral) {
    this.lateral = lateral;
  }

  @Override
  public TableExpressionType getTableExpressionType() {
    return TableExpressionType.DERIVED_TABLE;
  }

  @Override
  public ExpressionType getExpType() {
    return ExpressionType.DERIVED_TABLE;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }

  @Override
  public String toString() {
    return (lateral ? "LATERAL " : "") + "(" + getQuery() + ")";
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    DerivedTable other = (DerivedTable) obj;
    if (lateral != other.lateral)
      return false;
    return true;
  }
}