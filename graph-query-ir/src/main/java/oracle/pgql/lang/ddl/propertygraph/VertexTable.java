/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;

public class VertexTable extends ElementTable {

  /**
   * The vertex table key.
   */
  Key key;

  /**
   * The constructor.
   */
  public VertexTable(String tableName, Key key, List<Label> labels) {
    super(tableName, labels);
    this.key = key;
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  @Override
  public String toString() {
    String keyText = key == null ? "" : "\n      KEY " + key;
    return getTableName() + keyText + printLabels("\n      ");
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
    VertexTable other = (VertexTable) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    return true;
  }
}
