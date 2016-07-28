/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class VertexPairConnection extends QueryVariable {

  protected final QueryVertex src;

  protected final QueryVertex dst;

  public VertexPairConnection(QueryVertex src, QueryVertex dst, String name, boolean anonymous) {
    super(name, anonymous);
    this.src = src;
    this.dst = dst;
  }

  public QueryVertex getSrc() {
    return src;
  }

  public QueryVertex getDst() {
    return dst;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    VertexPairConnection that = (VertexPairConnection) o;

    if (!src.equals(that.src)) {
      return false;
    }
    return dst.equals(that.dst);

  }

  @Override
  public int hashCode() {
    int result = src.hashCode();
    result = 31 * result + dst.hashCode();
    return result;
  }
}
