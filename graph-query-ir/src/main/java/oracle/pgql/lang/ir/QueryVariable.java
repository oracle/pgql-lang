/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class QueryVariable {

  public enum VariableType {
    VERTEX,
    EDGE,
    PATH,
    EXP_AS_VAR
  }
  
  /**
   * Constant String used for variable names to indicated that they were generated.
   * 
   * For example, for query "SELECT COUNT(*) WHERE () -> ()" there will be two generated
   * vertex names and one generate edge name, since the names are not provided by the user.
   */
  public static final String GENERATED_NAME_SUBSTR = "<<generated>>";

  protected final String name;

  public QueryVariable(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public abstract VariableType getVariableType();
}
