/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.StatementType;

public class DropPropertyGraph implements PgqlStatement {

  private SchemaQualifiedName schemaName;

  public DropPropertyGraph(SchemaQualifiedName graphName) {
    this.schemaName = graphName;
  }

  public SchemaQualifiedName getGraphName() {
    return schemaName;
  }

  public void setGraphName(SchemaQualifiedName graphName) {
    this.schemaName = graphName;
  }

  @Override
  public String toString() {
    return "DROP PROPERTY GRAPH " + schemaName;
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
    if (schemaName == null) {
      if (other.schemaName != null)
        return false;
    } else if (!schemaName.equals(other.schemaName))
      return false;
    return true;
  }
}
