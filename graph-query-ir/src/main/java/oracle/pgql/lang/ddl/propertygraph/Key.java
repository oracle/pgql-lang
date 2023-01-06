/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class Key {

  /**
   * The column that make up the key.
   */
  List<String> columnNames;

  /**
   * The constructor.
   */
  public Key(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  @Override
  public String toString() {
    return "( " + columnNames.stream() //
        .map(x -> printIdentifier(x, false)) //
        .collect(Collectors.joining(", ")) + " )";
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
    Key other = (Key) obj;
    if (columnNames == null) {
      if (other.columnNames != null)
        return false;
    } else if (!columnNames.equals(other.columnNames))
      return false;
    return true;
  }
}
