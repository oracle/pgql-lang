/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphPattern {

  private final Set<QueryVertex> vertices;

  private final Set<VertexPairConnection> connections;

  private final Set<QueryExpression> constraints;

  /**
   * @param vertices
   * @param connections
   *          the edges and paths
   * @param constraints
   *          the filters
   *
   *          The connections and constraints are instances of LinkedHashSet such that ordering is maintained for
   *          pretty-printing purposes (i.e. toString()). It is expected that the ordering corresponds to the ordering
   *          in which the elements appear in the query string.
   */
  public GraphPattern(Set<QueryVertex> vertices, LinkedHashSet<VertexPairConnection> connections,
      LinkedHashSet<QueryExpression> constraints) {
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public Set<VertexPairConnection> getConnections() {
    return connections;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    return printPgqlString(this, new ArrayList<QueryPath>());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphPattern that = (GraphPattern) o;

    if (!vertices.equals(that.vertices)) {
      return false;
    }
    if (!connections.equals(that.connections)) {
      return false;
    }
    return constraints.equals(that.constraints);
  }

  @Override
  public int hashCode() {
    int result = vertices.hashCode();
    result = 31 * result + connections.hashCode();
    result = 31 * result + constraints.hashCode();
    return result;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
