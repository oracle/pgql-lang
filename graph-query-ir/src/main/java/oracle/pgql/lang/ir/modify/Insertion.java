/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import oracle.pgql.lang.ir.QueryExpressionVisitor;

public interface Insertion {

  void accept(QueryExpressionVisitor v);

  InsertionType getInsertionType();
}
