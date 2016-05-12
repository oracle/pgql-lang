/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.Random;

public abstract class QueryVariable {

  private static final Random random = new Random();
  
  public enum VariableType {
    VERTEX,
    EDGE,
    PATH,
    EXP_AS_VAR
  }
  
  protected final boolean anonymous;
  
  protected final String name;

  public QueryVariable(String name, boolean anonymous) {
    this.name = name;
    this.anonymous = anonymous;
  }
  
  public QueryVariable(String name) {
    this(name, false);
  }
  
  public QueryVariable() {
    this("anonymous" + random.nextLong(), true);
  }
  
  public String getName() {
    return name;
  }
  
  public boolean isAnonymous() {
    return anonymous;
  }
  
  public abstract VariableType getVariableType();
}
