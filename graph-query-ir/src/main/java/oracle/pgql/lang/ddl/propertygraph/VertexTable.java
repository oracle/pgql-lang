/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;

import oracle.pgql.lang.ir.SchemaQualifiedName;

public class VertexTable extends ElementTable {

  /**
   * Constructor without alias.
   */
  public VertexTable(SchemaQualifiedName tableName, Key key, List<Label> labels) {
    super(key, tableName, tableName.getName(), labels);
  }

  /**
   * Constructor with alias.
   */
  public VertexTable(SchemaQualifiedName tableName, String tableAlias, Key key, List<Label> labels) {
    super(key, tableName, tableAlias, labels);
  }

  @Override
  public String toString() {
    return getTableName() + printAlias(" ") + printKey("\n      ") + printLabels("\n      ");
  }
}
