/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.createpropertygraph;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ElementTable {

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
  protected ElementTable(String tableName, List<Label> labels) {
    this.tableName = tableName;
    this.labels = labels;
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
    return labels.stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(indentation));
  }
}
