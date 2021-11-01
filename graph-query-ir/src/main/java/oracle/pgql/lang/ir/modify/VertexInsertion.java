/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;

import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryVertex;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class VertexInsertion extends AbstractInsertion {

  private QueryVertex vertex;

  public VertexInsertion(QueryVertex vertex, List<QueryExpression> labels, List<SetPropertyExpression> properties) {
    super(labels, properties);
    this.vertex = vertex;
  }

  public QueryVertex getVertex() {
    return vertex;
  }

  @Override
  public InsertionType getInsertionType() {
    return InsertionType.VERTEX_INSERTION;
  }

  @Override
  public String toString() {
    return "VERTEX " + printIdentifier(vertex.getName(), false) + printLabels() + printProperties();
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
