/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class QueryVertex extends QueryVariable {

  private QueryVariable correlationVertexInOuterQuery;

  public QueryVertex(String name, boolean anonymous) {
    this(name, null, anonymous, null);
  }

  public QueryVertex(String name, String uniqueName, boolean anonymous, QueryVariable correlationVertexInOuterQuery) {
    super(name, uniqueName, anonymous);
    this.correlationVertexInOuterQuery = correlationVertexInOuterQuery;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.VERTEX;
  }

  public QueryVariable getCorrelationVertexInOuterQuery() {
    return correlationVertexInOuterQuery;
  }

  public void setCorrelationVertexInOuterQuery(QueryVariable correlationVertexInOuterQuery) {
    this.correlationVertexInOuterQuery = correlationVertexInOuterQuery;
  }

  @Override
  public String toString() {
    return "(" + printIdentifier(name, false) + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QueryVariable that = (QueryVariable) o;

    if (anonymous != that.anonymous) {
      return false;
    }
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return 31;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
