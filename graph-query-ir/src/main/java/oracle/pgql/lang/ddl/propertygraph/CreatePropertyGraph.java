/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.StatementType;

public class CreatePropertyGraph implements PgqlStatement {

  public enum Organization {
    PG_SCHEMA,
    PG_VIEW
  }

  /**
   * The name of the property graph.
   */
  private SchemaQualifiedName graphName;

  /**
   * The organization of the property graph.
   */
  private Organization organization;

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
  public CreatePropertyGraph(SchemaQualifiedName graphName, List<VertexTable> vertexTables,
      List<EdgeTable> edgeTables) {
    this.graphName = graphName;
    this.organization = Organization.PG_SCHEMA;
    this.vertexTables = vertexTables;
    this.edgeTables = edgeTables;
  }

  public SchemaQualifiedName getGraphName() {
    return graphName;
  }

  public void setGraphName(SchemaQualifiedName graphName) {
    this.graphName = graphName;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = Organization.valueOf(organization);
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
    return "CREATE PROPERTY GRAPH " + graphName + printOrganization() + printVertexTables() + printEdgeTables();
  }

  private String printOrganization() {
    if (Organization.PG_SCHEMA.equals(organization)) {
      return "";
    }
    return "\n  ORGANIZATION " + organization;
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
