/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class QueryVar {

  /**
   * Constant String used for variable names to indicated that they were generated.
   * 
   * For example, for query "SELECT COUNT(*) WHERE () -> ()" there will be two generated
   * vertex names and one generate edge name, since the names are not provided by the user.
   */
  public static final String GENERATED_NAME_SUBSTR = "<<generated>>";

  public final String name;

  public QueryVar(String name) {
    this.name = name;
  }
}
