/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public class QueryVertex extends QueryVariable {

  public QueryVertex(String name) {
    super(name);
  }
  
  @Override
  public VariableType getVariableType() {
    return VariableType.VERTEX;
  }

  @Override
  public String toString() {
    return name;
  }
}
