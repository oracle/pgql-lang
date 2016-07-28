/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GroupBy {

  private final List<ExpAsVar> elements;
  
  public GroupBy(List<ExpAsVar> elements) {
    this.elements = elements;
  }
  
  public List<ExpAsVar> getElements() {
    return elements;
  }
  
  @Override
  public String toString() {
    return printPgqlString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GroupBy groupBy = (GroupBy) o;

    return elements.equals(groupBy.elements);

  }

  @Override
  public int hashCode() {
    return elements.hashCode();
  }
}
