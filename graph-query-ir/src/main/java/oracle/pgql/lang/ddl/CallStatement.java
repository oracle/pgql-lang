/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;

public class CallStatement implements PgqlStatement {

  private SchemaQualifiedName routineName;

  private List<QueryExpression> argumentList;

  public CallStatement(SchemaQualifiedName routineName, List<QueryExpression> argumentList) {
    this.routineName = routineName;
    this.argumentList = argumentList;
  }

  public SchemaQualifiedName getRoutineName() {
    return routineName;
  }

  public void setRoutineName(SchemaQualifiedName routineName) {
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
    String arguments = argumentList.stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(", "));
    return "CALL " + routineName + "(" + arguments + ")";
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
    if (routineName == null) {
      if (other.routineName != null)
        return false;
    } else if (!routineName.equals(other.routineName))
      return false;
    return true;
  }
}
