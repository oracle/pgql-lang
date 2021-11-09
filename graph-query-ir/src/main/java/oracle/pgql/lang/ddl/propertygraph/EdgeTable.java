/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class EdgeTable extends ElementTable {

  /**
   * The source vertex table.
   */
  VertexTable sourceVertexTable;

  /**
   * One or more columns (of the edge table) uniquely referencing a row in the source vertex table
   */
  Key edgeSourceKey;

  /**
   * One or more columns (of the source vertex table) that correspond to the edge source key. If NULL, the source vertex
   * key defaults to the key defined as part of the source vertex table.
   */
  Key sourceVertexKey;

  /**
   * The destination vertex table.
   */
  VertexTable destinationVertexTable;

  /**
   * One or more columns (of the edge table) uniquely referencing a row in the destination vertex table.
   */
  Key edgeDestinationKey;

  /**
   * One or more columns (of the destination vertex table) that correspond to the edge destination key. If NULL, the
   * destination vertex key defaults to the key defined as part of the destination vertex table.
   */
  Key destinationVertexKey;

  /**
   * Constructor without table alias, source vertex key and destination vertex key (relies on the presence of keys of
   * vertex tables).
   */
  public EdgeTable(SchemaQualifiedName tableName, VertexTable sourceVertexTable, Key edgeSourceKey,
      VertexTable destinationVertexTable, Key edgeDestinationKey, List<Label> labels) {
    this(tableName, tableName.getName(), null, sourceVertexTable, edgeSourceKey, null, destinationVertexTable,
        edgeDestinationKey, null, labels);
  }

  /**
   * Constructor with table alias, but without source vertex key and destination vertex key (relies on the presence of
   * keys of vertex tables).
   */
  public EdgeTable(SchemaQualifiedName tableName, String tableAlias, VertexTable sourceVertexTable, Key edgeSourceKey,
      VertexTable destinationVertexTable, Key edgeDestinationKey, List<Label> labels) {
    this(tableName, tableAlias, null, sourceVertexTable, edgeSourceKey, null, destinationVertexTable,
        edgeDestinationKey, null, labels);
  }

  /**
   * Constructor with table alias, edge key, source vertex key and destination vertex key.
   */
  public EdgeTable(SchemaQualifiedName tableName, String tableAlias, Key key, VertexTable sourceVertexTable,
      Key edgeSourceKey, Key sourceVertexKey, VertexTable destinationVertexTable, Key edgeDestinationKey,
      Key destinationVertexKey, List<Label> labels) {
    super(key, tableName, tableAlias, labels);
    this.sourceVertexTable = sourceVertexTable;
    this.edgeSourceKey = edgeSourceKey;
    this.sourceVertexKey = sourceVertexKey;
    this.destinationVertexTable = destinationVertexTable;
    this.edgeDestinationKey = edgeDestinationKey;
    this.destinationVertexKey = destinationVertexKey;
  }

  public VertexTable getSourceVertexTable() {
    return sourceVertexTable;
  }

  public void setSourceVertexTable(VertexTable sourceVertexTable) {
    this.sourceVertexTable = sourceVertexTable;
  }

  public Key getEdgeSourceKey() {
    return edgeSourceKey;
  }

  public void setEdgeSourceKey(Key edgeSourceKey) {
    this.edgeSourceKey = edgeSourceKey;
  }

  public Key getSourceVertexKey() {
    return sourceVertexKey;
  }

  public void setSourceVertexKey(Key sourceVertexKey) {
    this.sourceVertexKey = sourceVertexKey;
  }

  public VertexTable getDestinationVertexTable() {
    return destinationVertexTable;
  }

  public void setDestinationVertexTable(VertexTable destinationVertexTable) {
    this.destinationVertexTable = destinationVertexTable;
  }

  public Key getEdgeDestinationKey() {
    return edgeDestinationKey;
  }

  public void setEdgeDestinationKey(Key edgeDestinationKey) {
    this.edgeDestinationKey = edgeDestinationKey;
  }

  public Key getDestinationVertexKey() {
    return destinationVertexKey;
  }

  public void setDestinationVertexKey(Key destinationVertexKey) {
    this.destinationVertexKey = destinationVertexKey;
  }

  @Override
  public String toString() {
    return getTableName() + printAlias(" ") + printKey(" ") + printSource() + printDestination()
        + printLabels("\n      ");
  }

  private String printSource() {
    return "\n      SOURCE " + printVertexTableReference(edgeSourceKey, sourceVertexTable)
        + printKeyForReferencedVertexTable(sourceVertexKey);
  }

  private String printDestination() {
    return "\n      DESTINATION " + printVertexTableReference(edgeDestinationKey, destinationVertexTable)
        + printKeyForReferencedVertexTable(destinationVertexKey);
  }

  private String printVertexTableReference(Key key, VertexTable referencedVertexTable) {
    String vertexTableName = referencedVertexTable.getTableAlias() == null
        ? referencedVertexTable.getTableName().getName()
        : referencedVertexTable.getTableAlias();
    if (key == null) {
      return printIdentifier(vertexTableName, false);
    } else {
      return "KEY " + key + " REFERENCES " + printIdentifier(vertexTableName, false);
    }
  }

  private String printKeyForReferencedVertexTable(Key vertexKey) {
    if (vertexKey == null) {
      return "";
    } else {
      return vertexKey.toString();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    EdgeTable other = (EdgeTable) obj;
    if (destinationVertexKey == null) {
      if (other.destinationVertexKey != null)
        return false;
    } else if (!destinationVertexKey.equals(other.destinationVertexKey))
      return false;
    if (destinationVertexTable == null) {
      if (other.destinationVertexTable != null)
        return false;
    } else if (!destinationVertexTable.equals(other.destinationVertexTable))
      return false;
    if (edgeDestinationKey == null) {
      if (other.edgeDestinationKey != null)
        return false;
    } else if (!edgeDestinationKey.equals(other.edgeDestinationKey))
      return false;
    if (edgeSourceKey == null) {
      if (other.edgeSourceKey != null)
        return false;
    } else if (!edgeSourceKey.equals(other.edgeSourceKey))
      return false;
    if (sourceVertexKey == null) {
      if (other.sourceVertexKey != null)
        return false;
    } else if (!sourceVertexKey.equals(other.sourceVertexKey))
      return false;
    if (sourceVertexTable == null) {
      if (other.sourceVertexTable != null)
        return false;
    } else if (!sourceVertexTable.equals(other.sourceVertexTable))
      return false;
    return true;
  }
}
