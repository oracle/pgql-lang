/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class QueryEdge extends VertexPairConnection {

  public QueryEdge(QueryVertex src, QueryVertex dst, String name, boolean anonymous) {
    super(src, dst, name, anonymous);
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EDGE;
  }
  
  @Override
  public String toString() {
    return getSrc() + "-[" + getName() + "]->" + getDst();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QueryEdge queryEdge = (QueryEdge) o;

    if (anonymous != queryEdge.anonymous) {
      return false;
    }
    if (!src.equals(queryEdge.src)) {
      return false;
    }
    if (!dst.equals(queryEdge.dst)) {
      return false;
    }
    return name.equals(queryEdge.name);

  }

  @Override
  public int hashCode() {
    int result = (anonymous ? 1 : 0);
    result = 31 * result + name.hashCode();
    result = 31 * result + src.hashCode();
    result = 31 * result + dst.hashCode();
    return result;
  }
}
