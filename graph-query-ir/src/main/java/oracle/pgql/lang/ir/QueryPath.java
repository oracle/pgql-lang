/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class QueryPath extends VertexPairConnection {

  public enum Direction {
    OUTGOING,
    INCOMING
  }

  public enum Repetition {
    KLEENE_STAR,
    KLEENE_PLUS, // not yet used
    NONE
  }

  private final List<QueryVertex> vertices;

  private final List<VertexPairConnection> connections;

  private final List<Direction> directions;

  private final Set<QueryExpression> constraints;

  private final Repetition repetition;

  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections,
      List<Direction> directions, Set<QueryExpression> constraints, Repetition repetition, String name,
      boolean anonymous) {
    super(src, dst, name, anonymous);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
    this.repetition = repetition;
  }

  public List<QueryVertex> getVertices() {
    return vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }

  public List<Direction> getDirections() {
    return directions;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  public Repetition getRepetition() {
    return repetition;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QueryPath queryPath = (QueryPath) o;

    if (anonymous != queryPath.anonymous) {
      return false;
    }
    if (!name.equals(queryPath.name)) {
      return false;
    }
    if (!src.equals(queryPath.src)) {
      return false;
    }
    if (!dst.equals(queryPath.dst)) {
      return false;
    }
    if (repetition != queryPath.repetition) {
      return false;
    }
    if (!vertices.equals(queryPath.vertices)) {
      return false;
    }
    if (!connections.equals(queryPath.connections)) {
      return false;
    }
    if (!directions.equals(queryPath.directions)) {
      return false;
    }
    return constraints.equals(queryPath.constraints);

  }

  @Override
  public int hashCode() {
    int result = (anonymous ? 1 : 0);
    result = 31 * result + name.hashCode();
    result = 31 * result + src.hashCode();
    result = 31 * result + dst.hashCode();
    result = 31 * result + vertices.hashCode();
    result = 31 * result + connections.hashCode();
    result = 31 * result + directions.hashCode();
    result = 31 * result + constraints.hashCode();
    result = 31 * result + repetition.hashCode();
    return result;
  }
}
