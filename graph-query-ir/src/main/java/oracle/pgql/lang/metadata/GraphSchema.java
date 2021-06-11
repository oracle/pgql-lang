/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

import java.util.List;

public class GraphSchema {

  private final List<VertexLabel> vertexLabels;
  
  private final List<EdgeLabel> edgeLabels;
  
  public GraphSchema(List<VertexLabel> vertexLabels, List<EdgeLabel> edgeLabels) {
    this.vertexLabels = vertexLabels;
    this.edgeLabels = edgeLabels;
  }

  public List<VertexLabel> getVertexLabels() {
    return vertexLabels;
  }

  public List<EdgeLabel> getEdgeLabels() {
    return edgeLabels;
  }
}
