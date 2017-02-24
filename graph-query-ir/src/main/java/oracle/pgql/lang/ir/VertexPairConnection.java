/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class VertexPairConnection extends QueryVariable {

  protected final QueryVertex src;

  protected final QueryVertex dst;

  protected final boolean undirected;

  public VertexPairConnection(QueryVertex src, QueryVertex dst, String name, boolean anonymous, boolean undirected) {
    super(name, anonymous);
    this.src = src;
    this.dst = dst;
    this.undirected = undirected;
  }

  public QueryVertex getSrc() {
    return src;
  }

  public QueryVertex getDst() {
    return dst;
  }

  public boolean isUndirected() {
    return undirected;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((dst == null) ? 0 : dst.hashCode());
    result = prime * result + ((src == null) ? 0 : src.hashCode());
    result = prime * result + (undirected ? 1231 : 1237);
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
    VertexPairConnection other = (VertexPairConnection) obj;
    if (dst == null) {
      if (other.dst != null)
        return false;
    } else if (!dst.equals(other.dst))
      return false;
    if (src == null) {
      if (other.src != null)
        return false;
    } else if (!src.equals(other.src))
      return false;
    if (undirected != other.undirected)
      return false;
    return true;
  }
}
