/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;

import oracle.pgql.lang.ir.SchemaQualifiedName;

public class VertexTable extends ElementTable {

  /**
   * The constructor.
   */
  public VertexTable(SchemaQualifiedName tableName, Key key, List<Label> labels) {
    super(key, tableName, labels);
  }

  @Override
  public String toString() {
    return getTableName() + printKey("\n      ") + printLabels("\n      ");
  }
}
