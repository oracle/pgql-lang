/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.Statement;
import oracle.pgql.lang.ir.StatementType;

public class DropPropertyGraph implements Statement {

  private SchemaQualifiedName graphName;

  public DropPropertyGraph(SchemaQualifiedName graphName) {
    this.graphName = graphName;
  }

  public SchemaQualifiedName getGraphName() {
    return graphName;
  }

  public void setGraphName(SchemaQualifiedName graphName) {
    this.graphName = graphName;
  }

  @Override
  public String toString() {
    return "DROP PROPERTY GRAPH " + graphName;
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
    return true;
  }
}
