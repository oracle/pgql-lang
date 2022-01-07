/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.unnest;

import oracle.pgql.lang.ir.QueryVertex;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class OneRowPerVertex implements RowsPerMatch {

  QueryVertex vertex;

  public OneRowPerVertex(QueryVertex vertex) {
    this.vertex = vertex;
  }

  public QueryVertex getVertex() {
    return vertex;
  }

  public void setVertex(QueryVertex vertex) {
    this.vertex = vertex;
  }

  @Override
  public RowsPerMatchType getRowsPerMatchType() {
    return RowsPerMatchType.ONE_ROW_PER_VERTEX;
  }

  @Override
  public String toString() {
    return "ONE ROW PER VERTEX (" + printIdentifier(vertex.getName()) + ")";
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
    OneRowPerVertex other = (OneRowPerVertex) obj;
    if (vertex == null) {
      if (other.vertex != null)
        return false;
    } else if (!vertex.equals(other.vertex))
      return false;
    return true;
  }
}
