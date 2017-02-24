/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class QueryPath extends VertexPairConnection {

  @Deprecated
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
      boolean anonymous, boolean undirected) {
    super(src, dst, name, anonymous, undirected);
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

  /**
   * Use {@link #getSrc()} or {@link #getDst()} of connections instead.
   * For example: PATH pattern := (n) -[e1]-> (m) <-[e2]- (o)
   * e1.getSrc() -> (n) ==> direction is outgoing
   * e2.getSrc() -> (o) ==> direction is incoming
   */
  @Deprecated
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
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((connections == null) ? 0 : connections.hashCode());
    result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
    result = prime * result + ((directions == null) ? 0 : directions.hashCode());
    result = prime * result + ((repetition == null) ? 0 : repetition.hashCode());
    result = prime * result + ((vertices == null) ? 0 : vertices.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    QueryPath other = (QueryPath) obj;
    if (connections == null) {
      if (other.connections != null)
        return false;
    } else if (!connections.equals(other.connections))
      return false;
    if (constraints == null) {
      if (other.constraints != null)
        return false;
    } else if (!constraints.equals(other.constraints))
      return false;
    if (directions == null) {
      if (other.directions != null)
        return false;
    } else if (!directions.equals(other.directions))
      return false;
    if (repetition != other.repetition)
      return false;
    if (vertices == null) {
      if (other.vertices != null)
        return false;
    } else if (!vertices.equals(other.vertices))
      return false;
    return true;
  }
}
