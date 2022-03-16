/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.unnest;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryVertex;

public class OneRowPerStep implements RowsPerMatch {

  private QueryVertex vertex1;

  private QueryEdge edge;

  private QueryVertex vertex2;

  public OneRowPerStep(QueryVertex vertex1, QueryEdge edge, QueryVertex vertex2) {
    this.vertex1 = vertex1;
    this.edge = edge;
    this.vertex2 = vertex2;
  }

  public QueryVertex getVertex1() {
    return vertex1;
  }

  public QueryEdge getEdge() {
    return edge;
  }

  public QueryVertex getVertex2() {
    return vertex2;
  }

  public void setVertex1(QueryVertex vertex) {
    this.vertex1 = vertex;
  }

  public void setEdge(QueryEdge edge) {
    this.edge = edge;
  }

  public void setVertex2(QueryVertex vertex) {
    this.vertex2 = vertex;
  }

  @Override
  public RowsPerMatchType getRowsPerMatchType() {
    return RowsPerMatchType.ONE_ROW_PER_STEP;
  }

  @Override
  public String toString() {
    return "ONE ROW PER STEP (" + printIdentifier(vertex1.getName()) + ", " + printIdentifier(edge.getName()) + ", "
        + printIdentifier(vertex2.getName()) + ")";
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
    OneRowPerStep other = (OneRowPerStep) obj;
    if (edge == null) {
      if (other.edge != null)
        return false;
    } else if (!edge.equals(other.edge))
      return false;
    if (vertex1 == null) {
      if (other.vertex1 != null)
        return false;
    } else if (!vertex1.equals(other.vertex1))
      return false;
    if (vertex2 == null) {
      if (other.vertex2 != null)
        return false;
    } else if (!vertex2.equals(other.vertex2))
      return false;
    return true;
  }
}