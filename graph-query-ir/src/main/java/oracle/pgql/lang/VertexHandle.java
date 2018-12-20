/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

/**
 * A {@code VertexHandle} represents a handle to a vertex in a graph.
 * 
 * Vertex and edge handles (also see {@link EdgeHandle}) provide the ability to get hold of vertices and edges that are
 * retrieved from graphs through an initial query. The handles can then be passed into follow-up queries such that
 * additional data related to the vertices and edges can be retrieved. Typical use cases are graph visualization
 * toolkits and other tools that interact with vertices and edges that are stored in a graph system.
 * 
 * Here is an example:
 * 
 * <blockquote><pre>
 *     VertexHandle vertexHandle = resultSet.getVertexHandle(1);
 *     preparedStatement.setVertexHandle(1, vertexHandle);
 * </pre></blockquote>
 * 
 * NOTE TO IMPLEMENTERS OF THIS INTERFACE: please also implement equals(Object o) by using equals(VertexHandle other)
 * after determining that the object is a vertex handle.
 */
public interface VertexHandle {

  /**
   * Indicates whether some other {@code VertexHandle} is "equal to" this one.
   * 
   * @return {@code true} if this {@code VertexHandle} is the same as the other argument; {@code false} otherwise.
   */
  boolean equals(VertexHandle other);

  /**
   * @return a hash code value for the object.
   */
  int hashCode();
}
