/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class QueryEdge extends VertexPairConnection {

  private boolean directed;

  public QueryEdge(QueryVertex src, QueryVertex dst, String name, boolean anonymous, boolean directed) {
    super(src, dst, name, anonymous);
    this.directed = directed;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EDGE;
  }

  public boolean isDirected() {
    return directed;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (directed ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    QueryEdge other = (QueryEdge) obj;
    if (directed != other.directed)
      return false;
    return true;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
