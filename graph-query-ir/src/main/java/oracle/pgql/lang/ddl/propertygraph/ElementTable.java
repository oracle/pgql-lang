/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public abstract class ElementTable {

  /**
   * The name of the table. This name is used as vertex/edge table name unless an alias is defined, in which case the
   * alias is used as vertex/edge table name.
   */
  private SchemaQualifiedName tableName;

  /**
   * The alias. If not null, the alias is used as vertex/edge table name. The PGQL parser always defaults the alias to
   * the table name in case no alias is defined.
   */
  private String tableAlias;

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
  protected ElementTable(Key key, SchemaQualifiedName tableName, String tableAlias, List<Label> labels) {
    this.key = key;
    this.tableName = tableName;
    this.tableAlias = tableAlias;
    this.labels = labels;
  }

  public SchemaQualifiedName getTableName() {
    return tableName;
  }

  public void setTableName(SchemaQualifiedName tableName) {
    this.tableName = tableName;
  }

  public String getTableAlias() {
    return tableAlias == null ? tableName.getName() : tableAlias;
  }

  public void setTableAlias(String tableAlias) {
    this.tableAlias = tableAlias;
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

  protected String printAlias(String indentation) {
    if (tableAlias == null || tableAlias.equals(tableName.getName())) {
      return "";
    } else {
      return indentation + "AS " + printIdentifier(tableAlias, false);
    }
  }

  protected String printKey(String indentation) {
    return key == null ? "" : indentation + "KEY " + key;
  }

  protected String printLabels(String indentation) {
    if (labels.size() == 1) {
      Label label = labels.get(0);
      if (label.getName().equals(getTableAlias())) {
        // skip printing the label since it can default to the table alias
        return indentation + label.printProperties();
      } else {
        // prints the label and its properties
        return indentation + label.toString();
      }
    }

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
    if (tableAlias == null) {
      if (other.tableAlias != null)
        return false;
    } else if (!tableAlias.equals(other.tableAlias))
      return false;
    return true;
  }
}
