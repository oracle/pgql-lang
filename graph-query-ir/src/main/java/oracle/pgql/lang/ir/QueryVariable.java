/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
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

  /**
   * Generated name that is unique even across subqueries
   */
  protected String uniqueName;

  protected boolean anonymous;

  public QueryVariable(String name, boolean anonymous) {
    this(name, null, anonymous);
  }

  public QueryVariable(String name, String uniqueName, boolean anonymous) {
    this.name = name;
    this.uniqueName = uniqueName;
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

  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
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
    return 31;
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
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  public abstract void accept(QueryExpressionVisitor v);
}
