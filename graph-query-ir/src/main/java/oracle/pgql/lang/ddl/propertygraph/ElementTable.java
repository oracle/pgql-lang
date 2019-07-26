/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ElementTable {

  /**
   * The name of the schema.
   */
  private String schemaName;

  /**
   * The vertex or edge table name.
   */
  private String tableName;

  /**
   * The vertex or edge labels.
   */
  private List<Label> labels;

  /**
   * The constructor.
   */
  protected ElementTable(String schemaName, String tableName, List<Label> labels) {
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.labels = labels;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }

  protected String printLabels(String indentation) {
    return indentation + labels.stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(indentation));
  }
}
