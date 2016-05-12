/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class QueryEdge extends QueryVariable implements VertexPairConnection {

  private final QueryVertex src;
  
  private final QueryVertex dst;

  public QueryEdge(String name, QueryVertex src, QueryVertex dst) {
    super(name);
    this.src = src;
    this.dst = dst;
  }

  @Override
  public QueryVertex getSrc() {
    return src;
  }

  @Override
  public QueryVertex getDst() {
    return dst;
  }

  @Override
  public ConnectionType getConnectionType() {
     return ConnectionType.EDGE;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.EDGE;
  }
  
  @Override
  public String toString() {
    return src + "-[" + getName() + "]->" + dst;
  }
}
