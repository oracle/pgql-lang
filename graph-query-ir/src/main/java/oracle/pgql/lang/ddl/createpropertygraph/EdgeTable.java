/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.createpropertygraph;

import java.util.List;

public class EdgeTable extends ElementTable {

  /**
   * The source vertex table.
   */
  VertexTable source;

  /**
   * The columns in this table that make up the foreign key into the source vertex table.
   */
  Key sourceKey;

  /**
   * The destination vertex table.
   */
  VertexTable destination;

  /**
   * The column in this table that make up the foreign key into the destination vertex table.
   */
  Key destinationKey;

  /**
   * The constructor.
   */
  public EdgeTable(String tableName, VertexTable source, Key sourceKey, VertexTable destination, Key destinationKey,
      List<Label> labels) {
    super(tableName, labels);
    this.source = source;
    this.sourceKey = sourceKey;
    this.destination = destination;
    this.destinationKey = destinationKey;
  }

  public VertexTable getSource() {
    return source;
  }

  public void setSource(VertexTable source) {
    this.source = source;
  }

  public Key getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(Key sourceKey) {
    this.sourceKey = sourceKey;
  }

  public VertexTable getDestination() {
    return destination;
  }

  public void setDestination(VertexTable destination) {
    this.destination = destination;
  }

  public Key getDestinationKey() {
    return destinationKey;
  }

  public void setDestinationKey(Key destinationKey) {
    this.destinationKey = destinationKey;
  }

  @Override
  public String toString() {
    return getTableName() + " " + printSource() + " " + printDestination() + " " + printLabels();
  }

  private String printSource() {
    return "SOURCE " + sourceKey + " REFERENCES " + source.getTableName();
  }

  private String printDestination() {
    return "DESTINATION " + destinationKey + " REFERENCES " + destination.getTableName();
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
    EdgeTable other = (EdgeTable) obj;
    if (destination == null) {
      if (other.destination != null)
        return false;
    } else if (!destination.equals(other.destination))
      return false;
    if (destinationKey == null) {
      if (other.destinationKey != null)
        return false;
    } else if (!destinationKey.equals(other.destinationKey))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    if (sourceKey == null) {
      if (other.sourceKey != null)
        return false;
    } else if (!sourceKey.equals(other.sourceKey))
      return false;
    return true;
  }
}
