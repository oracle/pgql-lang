/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryVertex;

public class VertexInsertion implements Insertion {

  private QueryVertex vertex;

  public VertexInsertion(QueryVertex vertex) {
    this.vertex = vertex;
  }

  public QueryVertex getVertex() {
    return vertex;
  }

  @Override
  public String toString() {
    return "INSERT VERTEX " + vertex.getName();

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
    VertexInsertion other = (VertexInsertion) obj;
    if (vertex == null) {
      if (other.vertex != null)
        return false;
    } else if (!vertex.equals(other.vertex))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
