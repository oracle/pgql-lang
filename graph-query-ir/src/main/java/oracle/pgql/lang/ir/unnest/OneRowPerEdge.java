/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.unnest;

import oracle.pgql.lang.ir.QueryEdge;

public class OneRowPerEdge implements RowsPerMatch {

  private QueryEdge edge;

  public OneRowPerEdge(QueryEdge edge) {
    this.edge = edge;
  }

  public QueryEdge getEdge() {
    return edge;
  }

  public void setEdge(QueryEdge edge) {
    this.edge = edge;
  }

  @Override
  public RowsPerMatchType getRowsPerMatchType() {
    return RowsPerMatchType.ONE_ROW_PER_EDGE;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OneRowPerEdge other = (OneRowPerEdge) obj;
    if (edge == null) {
      if (other.edge != null)
        return false;
    } else if (!edge.equals(other.edge))
      return false;
    return true;
  }
}
