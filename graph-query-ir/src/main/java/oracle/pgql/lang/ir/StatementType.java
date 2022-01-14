/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public enum StatementType {

  SELECT,
  GRAPH_MODIFY,
  CREATE_PROPERTY_GRAPH,
  DROP_PROPERTY_GRAPH,
  CREATE_EXTERNAL_SCHEMA,
  DROP_EXTERNAL_SCHEMA
}
