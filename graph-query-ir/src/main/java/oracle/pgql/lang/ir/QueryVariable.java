/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class QueryVariable {

  public enum VariableType {
    VERTEX,
    EDGE,
    PATH,
    EXP_AS_VAR
  }

  protected String name;

  protected boolean anonymous;

  public QueryVariable(String name, boolean anonymous) {
    this.name = name;
    this.anonymous = anonymous;
  }

  public QueryVariable(String name) {
    this(name, false);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public abstract VariableType getVariableType();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (anonymous ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    QueryVariable other = (QueryVariable) obj;
    if (anonymous != other.anonymous)
      return false;
    if (!anonymous) {
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
    }
    return true;
  }

  public abstract void accept(QueryExpressionVisitor v);
}
