/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class Label {

  /**
   * The label name.
   */
  private String name;

  /**
   * The label properties. Empty list in case of NO PROPERTIES. NULL in case the properties have not yet been inferred
   * from either 'propertiesAreAllColumnsExcept' or 'propertiesAreAllColumns'.
   */
  private List<Property> properties;

  /**
   * NULL unless PROPERTIES ARE ALL COLUMNS EXCEPT ( .. ) is used.
   */
  private List<String> propertiesAreAllColumnsExcept;

  /**
   * Whether properties should be all columns. True in case of PROPERTIES ARE ALL COLUMNS [EXCEPT ( .. )], false
   * otherwise.
   */
  private boolean propertiesAreAllColumns;

  /**
   * Constructor for PROPERTIES ( .. ) and NO PROPERTIES
   */
  public Label(String name, List<Property> properties) {
    this.name = name;
    this.properties = properties;
  }

  /**
   * Constructor for PROPERTIES ARE ALL COLUMNS EXCEPT ( .. )
   */
  public Label(String name, boolean propertiesAreAllColumns, List<String> except) {
    this(name, propertiesAreAllColumns);
    this.propertiesAreAllColumnsExcept = except;
  }

  /**
   * Constructor for PROPERTIES ARE ALL COLUMNS
   */
  public Label(String name, boolean propertiesAreAllColumns) {
    this.name = name;
    this.propertiesAreAllColumns = propertiesAreAllColumns;
    assert propertiesAreAllColumns == true;
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

  public List<String> getPropertiesAreAllColumnsExcept() {
    return propertiesAreAllColumnsExcept;
  }

  public void setPropertiesAreAllColumnsExcept(List<String> propertiesAreAllColumnsExcept) {
    this.propertiesAreAllColumnsExcept = propertiesAreAllColumnsExcept;
  }

  public boolean isPropertiesAreAllColumns() {
    return propertiesAreAllColumns;
  }

  public void setPropertiesAreAllColumns(boolean propertiesAreAllColumns) {
    this.propertiesAreAllColumns = propertiesAreAllColumns;
  }

  @Override
  public String toString() {
    return "LABEL " + printIdentifier(name, false) + " " + printProperties();
  }

  String printProperties() {
    if (propertiesAreAllColumns) {
      assert properties == null;
      if (propertiesAreAllColumnsExcept == null) {
        return "PROPERTIES ARE ALL COLUMNS";
      } else {
        return "PROPERTIES ARE ALL COLUMNS EXCEPT ( " + propertiesAreAllColumnsExcept.stream() //
            .map(x -> printIdentifier(x, false)) //
            .collect(Collectors.joining(", ")) + " )";
      }
    } else {
      assert properties != null;
      if (properties.isEmpty()) {
        return "NO PROPERTIES";
      } else {
        return "PROPERTIES ( " + properties.stream() //
            .map(x -> x.toString()) //
            .collect(Collectors.joining(", ")) + " )";
      }
    }
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
    if (propertiesAreAllColumns != other.propertiesAreAllColumns)
      return false;
    if (propertiesAreAllColumnsExcept == null) {
      if (other.propertiesAreAllColumnsExcept != null)
        return false;
    } else if (!propertiesAreAllColumnsExcept.equals(other.propertiesAreAllColumnsExcept))
      return false;
    return true;
  }

}
