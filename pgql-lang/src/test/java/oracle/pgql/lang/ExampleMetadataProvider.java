/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import oracle.pgql.lang.metadata.EdgeLabel;
import oracle.pgql.lang.metadata.GraphSchema;
import oracle.pgql.lang.metadata.VertexLabel;

public class ExampleMetadataProvider extends AbstractMetadataProvider {

  public Optional<GraphSchema> getGraphSchema() {
    List<VertexLabel> vertexLabels = new ArrayList<>();
    List<EdgeLabel> edgeLabels = new ArrayList<>();

    vertexLabels.add(new VertexLabel("Person", Collections.emptyList()));
    edgeLabels.add(new EdgeLabel("knows", Collections.emptyList()));

    GraphSchema graphSchema = new GraphSchema(vertexLabels, edgeLabels);
    return Optional.of(graphSchema);
  }
}
