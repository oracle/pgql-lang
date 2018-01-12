/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class CommonPathExpression {

  private String name;

  private List<QueryVertex> vertices;

  private List<VertexPairConnection> connections;

  private Set<QueryExpression> constraints;

  public CommonPathExpression(String name, List<QueryVertex> vertices, List<VertexPairConnection> connections,
      Set<QueryExpression> constraints) {
    this.name = name;
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<QueryVertex> getVertices() {
    return vertices;
  }

  public void setVertices(List<QueryVertex> vertices) {
    this.vertices = vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }

  public void setConnections(List<VertexPairConnection> connections) {
    this.connections = connections;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  public void setConstraints(Set<QueryExpression> constraints) {
    this.constraints = constraints;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((connections == null) ? 0 : connections.hashCode());
    result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((vertices == null) ? 0 : vertices.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CommonPathExpression other = (CommonPathExpression) obj;
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
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (vertices == null) {
      if (other.vertices != null)
        return false;
    } else if (!vertices.equals(other.vertices))
      return false;
    return true;
  }
}
