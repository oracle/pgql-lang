/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.Set;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphPattern {

  private final Set<QueryVertex> vertices;

  private final Set<VertexPairConnection> connections;

  private final Set<QueryExpression> constraints;

  public GraphPattern(Set<QueryVertex> vertices, Set<VertexPairConnection> connections,
      Set<QueryExpression> constraints) {
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
    return printPgqlString(this);
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
}
