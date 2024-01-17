/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class QueryEdge extends VertexPairConnection {

  private QueryVariable correlationEdgeInOuterQuery;

  public QueryEdge(QueryVertex src, QueryVertex dst, String name, boolean anonymous, Direction direction) {
    this(src, dst, name, null, anonymous, direction, null);
  }

  public QueryEdge(QueryVertex src, QueryVertex dst, String name, String uniqueName, boolean anonymous,
      Direction direction, QueryVariable correlationEdgeInOuterQuery) {
    super(src, dst, name, uniqueName, anonymous, direction);
    this.correlationEdgeInOuterQuery = correlationEdgeInOuterQuery;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EDGE;
  }

  public boolean isDirected() {
    return direction != Direction.ANY;
  }

  public QueryVariable getCorrelationEdgeInOuterQuery() {
    return correlationEdgeInOuterQuery;
  }

  public void setCorrelationEdgeInOuterQuery(QueryVariable correlationEdgeInOuterQuery) {
    this.correlationEdgeInOuterQuery = correlationEdgeInOuterQuery;
  }

  @Override
  public String toString() {
    String edge;
    edge = "-[" + printIdentifier(name, false) + "]-";
    if (isDirected()) {
      edge += ">";
    }
    return edge;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
