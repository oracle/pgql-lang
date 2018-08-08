/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class QueryEdge extends VertexPairConnection {

  public QueryEdge(QueryVertex src, QueryVertex dst, String name, boolean anonymous, Direction direction) {
    super(src, dst, name, anonymous, direction);
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EDGE;
  }

  public boolean isDirected() {
    return direction != Direction.ANY;
  }

  @Override
  public String toString() {
    String edge;
    if (isAnonymous()) {
      edge = "-";
    } else {
      edge = "-[" + name + "]-";
    }
    if (isDirected()) {
      edge += ">";
    }
    return edge;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
