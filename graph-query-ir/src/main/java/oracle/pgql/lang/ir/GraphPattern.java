/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.LinkedHashSet;
import java.util.Set;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphPattern {

  private Set<QueryVertex> vertices;

  private Set<VertexPairConnection> connections;

  private Set<QueryExpression> constraints;

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

  public void setVertices(Set<QueryVertex> vertices) {
    this.vertices = vertices;
  }

  public Set<VertexPairConnection> getConnections() {
    return connections;
  }

  public void setConnections(Set<VertexPairConnection> connections) {
    this.connections = connections;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  public void setConstraints(Set<QueryExpression> constraints) {
    this.constraints = constraints;
  }

  @Override
  public String toString() {
    return printPgqlString(this, false);
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
    return 31;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
