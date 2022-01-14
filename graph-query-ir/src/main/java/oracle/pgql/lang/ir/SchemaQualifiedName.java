/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class SchemaQualifiedName {

  /**
   * The schema name.
   */
  private String schemaName;

  /**
   * The local name;
   */
  private String name;

  public SchemaQualifiedName(String schemaName, String name) {
    this.schemaName = schemaName;
    this.name = name;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String toString() {
    if (schemaName == null) {
      return printIdentifier(name, false);
    } else {
      return printIdentifier(schemaName, false) + "." + printIdentifier(name, false);
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
    SchemaQualifiedName other = (SchemaQualifiedName) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (schemaName == null) {
      if (other.schemaName != null)
        return false;
    } else if (!schemaName.equals(other.schemaName))
      return false;
    return true;
  }
}
