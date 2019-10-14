/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.SchemaQualifiedName;

public abstract class ElementTable {

  /**
   * The vertex or edge table name.
   */
  private SchemaQualifiedName tableName;

  /**
   * The key.
   */
  Key key;

  /**
   * The vertex or edge labels.
   */
  private List<Label> labels;

  /**
   * The constructor.
   */
  protected ElementTable(Key key, SchemaQualifiedName tableName, List<Label> labels) {
    this.key = key;
    this.tableName = tableName;
    this.labels = labels;
  }

  public SchemaQualifiedName getTableName() {
    return tableName;
  }

  public void setTableName(SchemaQualifiedName tableName) {
    this.tableName = tableName;
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }

  protected String printKey(String indentation) {
    return key == null ? "" : indentation + "KEY " + key;
  }

  protected String printLabels(String indentation) {
    return indentation + labels.stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(indentation));
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
    ElementTable other = (ElementTable) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (labels == null) {
      if (other.labels != null)
        return false;
    } else if (!labels.equals(other.labels))
      return false;
    if (tableName == null) {
      if (other.tableName != null)
        return false;
    } else if (!tableName.equals(other.tableName))
      return false;
    return true;
  }
}
