/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class BaseElementTable {

  private String referencedTableName;

  /**
   * Never null since the PGQL parser defaults the alias to the referenced element table name in case no alias is specified.
   */
  private String tableAlias;

  public BaseElementTable(String referencedTableName, String tableAlias) {
    this.referencedTableName = referencedTableName;
    this.tableAlias = tableAlias;
  }

  public String getReferencedTableName() {
    return referencedTableName;
  }

  public void setReferencedTableName(String referencedTableName) {
    this.referencedTableName = referencedTableName;
  }

  public String getTableAlias() {
    return tableAlias;
  }

  public void setTableAlias(String tableAlias) {
    this.tableAlias = tableAlias;
  }

  @Override
  public String toString() {
    return printIdentifier(referencedTableName)
        + (referencedTableName.equals(tableAlias) ? "" : " AS " + printIdentifier(tableAlias));
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
    BaseElementTable other = (BaseElementTable) obj;
    if (referencedTableName == null) {
      if (other.referencedTableName != null)
        return false;
    } else if (!referencedTableName.equals(other.referencedTableName))
      return false;
    if (tableAlias == null) {
      if (other.tableAlias != null)
        return false;
    } else if (!tableAlias.equals(other.tableAlias))
      return false;
    return true;
  }
}
