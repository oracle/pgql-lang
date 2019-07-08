/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.createpropertygraph;

import java.util.List;
import java.util.stream.Collectors;

public class Label {

  /**
   * The label name.
   */
  private String name;

  /**
   * The label properties.
   */
  private List<Property> properties;

  /**
   * The constructor.
   */
  public Label(String name, List<Property> properties) {
    this.name = name;
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return "LABEL " + name + printProperties();
  }

  private String printProperties() {
    return " PROPERTIES (" + properties.stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(", ")) + ")";
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Label other = (Label) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    return true;
  }
}
