/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class VertexPairConnection extends QueryVariable {

  protected QueryVertex src;

  protected QueryVertex dst;

  protected Direction direction;

  public VertexPairConnection(QueryVertex src, QueryVertex dst, String name, boolean anonymous, Direction direction) {
    super(name, anonymous);
    this.src = src;
    this.dst = dst;
    this.direction = direction;
  }

  public QueryVertex getSrc() {
    return src;
  }

  public void setSrc(QueryVertex src) {
    this.src = src;
  }

  public QueryVertex getDst() {
    return dst;
  }

  public void setDst(QueryVertex dst) {
    this.dst = dst;
  }

  /**
   * @return direction of edge in query
   */
  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  @Override
  public int hashCode() {
    return 31;
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
    if (direction != other.direction)
      return false;
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
    return true;
  }

}
