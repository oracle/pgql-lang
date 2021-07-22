/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.unnest;

public class OneRowPerMatch implements RowsPerMatch {

  @Override
  public RowsPerMatchType getRowsPerMatchType() {
    return RowsPerMatchType.ONE_ROW_PER_MATCH;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (getClass() != obj.getClass())
      return false;
    return true;
  }
}
