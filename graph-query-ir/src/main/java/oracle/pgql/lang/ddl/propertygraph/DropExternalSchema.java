/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.StatementType;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class DropExternalSchema implements PgqlStatement {

  private String schemaName;

  public DropExternalSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  @Override
  public String toString() {
    return "DROP EXTERNAL SCHEMA " + printIdentifier(schemaName, false);
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.DROP_EXTERNAL_SCHEMA;
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
    DropExternalSchema other = (DropExternalSchema) obj;
    if (schemaName == null) {
      if (other.schemaName != null)
        return false;
    } else if (!schemaName.equals(other.schemaName))
      return false;
    return true;
  }
}
