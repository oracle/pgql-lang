/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

import java.util.List;

public abstract class Label {

  private final String label;
  private final List<Property> properties;

  public Label(String label, List<Property> properties) {
    this.label = label;
    this.properties = properties;
  }

  public String getLabel() {
    return label;
  }

  public List<Property> getProperties() {
    return properties;
  }
}
