/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import oracle.pgql.lang.ir.Statement;
import oracle.pgql.lang.ir.StatementType;

import static oracle.pgql.lang.ir.PgqlUtils.printLocalOrSchemaQualifiedName;

public class DropPropertyGraph implements Statement {

  private String schemaName;

  private String graphName;

  public DropPropertyGraph(String schemaName, String graphName) {
    this.schemaName = schemaName;
    this.graphName = graphName;
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

  @Override
  public String toString() {
    return "DROP PROPERTY GRAPH " + printLocalOrSchemaQualifiedName(schemaName, graphName);
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.DROP_PROPERTY_GRAPH;
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
    DropPropertyGraph other = (DropPropertyGraph) obj;
    if (graphName == null) {
      if (other.graphName != null)
        return false;
    } else if (!graphName.equals(other.graphName))
      return false;
    if (schemaName == null) {
      if (other.schemaName != null)
        return false;
    } else if (!schemaName.equals(other.schemaName))
      return false;
    return true;
  }
}
