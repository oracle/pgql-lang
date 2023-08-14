/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl;

import static java.util.stream.Collectors.joining;
import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

import java.util.List;

import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.StatementType;

public class CallStatement implements PgqlStatement {

  private String schemaName;

  private String packageName;

  private String routineName;

  private List<QueryExpression> argumentList;

  public CallStatement(String routineName, List<QueryExpression> argumentList) {
    this(null, routineName, argumentList);
  }

  public CallStatement(String packageName, String routineName, List<QueryExpression> argumentList) {
    this(null, packageName, routineName, argumentList);
  }

  public CallStatement(String schemaName, String packageName, String routineName, List<QueryExpression> argumentList) {
    this.schemaName = schemaName;
    this.packageName = packageName;
    this.routineName = routineName;
    this.argumentList = argumentList;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getRoutineName() {
    return routineName;
  }

  public void setRoutineName(String routineName) {
    this.routineName = routineName;
  }

  public List<QueryExpression> getArgumentList() {
    return argumentList;
  }

  public void setArgumentList(List<QueryExpression> argumentList) {
    this.argumentList = argumentList;
  }

  @Override
  public String toString() {
    String schemaNamePart = schemaName == null ? "" : printIdentifier(schemaName, false) + ".";
    String packageNamePart = packageName == null ? "" : printIdentifier(packageName, false) + ".";
    String arguments = argumentList.stream().map(QueryExpression::toString).collect(joining(", "));
    return "CALL " + schemaNamePart + packageNamePart + printIdentifier(routineName, false) + "(" + arguments + ")";
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.CALL;
  }

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
    CallStatement other = (CallStatement) obj;
    if (argumentList == null) {
      if (other.argumentList != null)
        return false;
    } else if (!argumentList.equals(other.argumentList))
      return false;
    if (packageName == null) {
      if (other.packageName != null)
        return false;
    } else if (!packageName.equals(other.packageName))
      return false;
    if (routineName == null) {
      if (other.routineName != null)
        return false;
    } else if (!routineName.equals(other.routineName))
      return false;
    if (schemaName == null) {
      if (other.schemaName != null)
        return false;
    } else if (!schemaName.equals(other.schemaName))
      return false;
    return true;
  }
}
