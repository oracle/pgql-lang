/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printLocalOrSchemaQualifiedName;

public class VertexTable extends ElementTable {

  /**
   * Constructor without alias.
   */
  public VertexTable(String schemaName, String tableName, Key key, List<Label> labels) {
    super(key, schemaName, tableName, tableName, labels);
  }

  /**
   * Constructor with alias.
   */
  public VertexTable(String schemaName, String tableName, String tableAlias, Key key, List<Label> labels) {
    super(key, schemaName, tableName, tableAlias, labels);
  }

  @Override
  public String toString() {
    return printLocalOrSchemaQualifiedName(getSchemaName(), getTableName()) + printAlias(" ") + printKey("\n      ")
        + printLabels("\n      ");
  }
}
