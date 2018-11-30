/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpressionVisitor;

public class EdgeInsertion implements Insertion {

  private QueryEdge edge;

  public EdgeInsertion(QueryEdge edge) {
    this.edge = edge;
  }

  public QueryEdge getEdge() {
    return edge;
  }

  @Override
  public String toString() {
    return "INSERT EDGE " + edge.getName() + " FROM " + edge.getSrc().getName() + " TO " + edge.getDst().getName();
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
    EdgeInsertion other = (EdgeInsertion) obj;
    if (edge == null) {
      if (other.edge != null)
        return false;
    } else if (!edge.equals(other.edge))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
