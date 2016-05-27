/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class QueryEdge extends VertexPairConnection {
  
  public QueryEdge(QueryVertex src, QueryVertex dst) {
    super(src, dst);
  }

  public QueryEdge(QueryVertex src, QueryVertex dst, String name) {
    super(src, dst, name);
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EDGE;
  }
  
  @Override
  public String toString() {
    return getSrc() + "-[" + getName() + "]->" + getDst();
  }
}
