/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class QueryPath extends VertexPairConnection {

  private final List<QueryVertex> vertices;

  private final List<VertexPairConnection> connections;

  private final Set<QueryExpression> constraints;

  private final long minHops;

  private final long maxHops;

  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections,
      Set<QueryExpression> constraints, String name,
      boolean anonymous, long minHops, long maxHops) {
    super(src, dst, name, anonymous);
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
    this.minHops = minHops;
    this.maxHops = maxHops;
  }

  public List<QueryVertex> getVertices() {
    return vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  /**
   * @return minimal number of hops
   */
  public long getMinHops() {
    return minHops;
  }

  /**
   * @return maximal number of hops, -1 if none is specified
   */
  public long getMaxHops() {
    return maxHops;
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
    result = prime * result + (int) (maxHops ^ (maxHops >>> 32));
    result = prime * result + (int) (minHops ^ (minHops >>> 32));
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
    if (maxHops != other.maxHops)
      return false;
    if (minHops != other.minHops)
      return false;
    if (vertices == null) {
      if (other.vertices != null)
        return false;
    } else if (!vertices.equals(other.vertices))
      return false;
    return true;
  }
}
