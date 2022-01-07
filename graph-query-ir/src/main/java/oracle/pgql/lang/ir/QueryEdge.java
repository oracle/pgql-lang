/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

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
