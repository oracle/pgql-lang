/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import oracle.pgql.lang.metadata.EdgeLabel;
import oracle.pgql.lang.metadata.GraphSchema;
import oracle.pgql.lang.metadata.Property;
import oracle.pgql.lang.metadata.VertexLabel;

public class ExampleMetadataProvider extends AbstractMetadataProvider {

  public Optional<GraphSchema> getGraphSchema() {
    List<VertexLabel> vertexLabels = new ArrayList<>();

    List<Property> personProperties = new ArrayList<>();
    personProperties.add(new Property("firstName", "STRING"));
    personProperties.add(new Property("dob", "DATE"));
    vertexLabels.add(new VertexLabel("Person", personProperties));

    List<Property> universityProperties = new ArrayList<>();
    universityProperties.add(new Property("name", "STRING"));
    vertexLabels.add(new VertexLabel("University", universityProperties));

    List<EdgeLabel> edgeLabels = new ArrayList<>();

    List<Property> knowsProperties = new ArrayList<>();
    knowsProperties.add(new Property("since", "DATE"));
    edgeLabels.add(new EdgeLabel("knows", knowsProperties));

    List<Property> studyAtProperties = new ArrayList<>();
    studyAtProperties.add(new Property("since", "DATE"));
    edgeLabels.add(new EdgeLabel("studyAt", studyAtProperties));

    GraphSchema graphSchema = new GraphSchema(vertexLabels, edgeLabels);
    return Optional.of(graphSchema);
  }
}
