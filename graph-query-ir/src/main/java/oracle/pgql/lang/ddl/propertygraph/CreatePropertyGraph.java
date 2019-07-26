/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import static oracle.pgql.lang.ir.PgqlUtils.printLocalOrSchemaQualifiedName;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.Statement;
import oracle.pgql.lang.ir.StatementType;

public class CreatePropertyGraph implements Statement {

  /**
   * The name of the schema.
   */
  private String schemaName;

  /**
   * The name of the property graph.
   */
  private String graphName;

  /**
   * Vertex tables. List may be empty but cannot be null.
   */
  private List<VertexTable> vertexTables;

  /**
   * Edge tables. List may be empty but cannot be null.
   */
  private List<EdgeTable> edgeTables;

  /**
   * The constructor.
   */
  public CreatePropertyGraph(String schemaName, String graphName, List<VertexTable> vertexTables,
      List<EdgeTable> edgeTables) {
    this.schemaName = schemaName;
    this.graphName = graphName;
    this.vertexTables = vertexTables;
    this.edgeTables = edgeTables;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getGraphName() {
    return graphName;
  }

  public void setGraphName(String graphName) {
    this.graphName = graphName;
  }

  public List<VertexTable> getVertexTables() {
    return vertexTables;
  }

  public void setVertexTables(List<VertexTable> vertexTables) {
    this.vertexTables = vertexTables;
  }

  public List<EdgeTable> getEdgeTables() {
    return edgeTables;
  }

  public void setEdgeTables(List<EdgeTable> edgeTables) {
    this.edgeTables = edgeTables;
  }

  @Override
  public String toString() {
    return "CREATE PROPERTY GRAPH " + printLocalOrSchemaQualifiedName(schemaName, graphName) + printVertexTables()
        + printEdgeTables();
  }

  private String printVertexTables() {
    if (vertexTables.isEmpty()) {
      return "";
    } else {
      return "\n  VERTEX TABLES (\n    " + vertexTables.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(",\n    ")) + "\n  )";
    }
  }

  private String printEdgeTables() {
    if (edgeTables.isEmpty()) {
      return "";
    } else {
      return "\n  EDGE TABLES (\n    " + edgeTables.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(",\n    ")) + "\n  )";
    }
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.CREATE_PROPERTY_GRAPH;
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
    CreatePropertyGraph other = (CreatePropertyGraph) obj;
    if (edgeTables == null) {
      if (other.edgeTables != null)
        return false;
    } else if (!edgeTables.equals(other.edgeTables))
      return false;
    if (schemaName == null) {
      if (other.schemaName != null)
        return false;
    } else if (!schemaName.equals(other.schemaName))
      return false;
    if (graphName == null) {
      if (other.graphName != null)
        return false;
    } else if (!graphName.equals(other.graphName))
      return false;
    if (vertexTables == null) {
      if (other.vertexTables != null)
        return false;
    } else if (!vertexTables.equals(other.vertexTables))
      return false;
    return true;
  }
}
