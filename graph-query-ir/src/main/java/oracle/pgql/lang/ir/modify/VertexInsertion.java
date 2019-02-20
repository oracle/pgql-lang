/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.Set;

import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryVertex;

public class VertexInsertion extends AbstractInsertion {

  private QueryVertex vertex;

  public VertexInsertion(QueryVertex vertex, Set<String> labels, List<SetPropertyExpression> properties) {
    super(labels, properties);
    this.vertex = vertex;
  }

  public QueryVertex getVertex() {
    return vertex;
  }

  @Override
  public ModificationType getModificationType() {
    return ModificationType.INSERT_VERTEX;
  }

  @Override
  public String toString() {
    return "INSERT VERTEX " + vertex.getName() + printLabels() + printProperties();
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj))
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
