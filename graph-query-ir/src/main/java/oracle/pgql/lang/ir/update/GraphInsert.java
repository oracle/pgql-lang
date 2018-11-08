/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.update;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.QueryExpressionVisitor;

public class GraphInsert {

  private GraphPattern graphPattern;

  private String intoGraphName;

  private List<SetPropertyExpression> setPropertyExpressions;

  public GraphInsert(GraphPattern graphPattern, String intoGraphName,
      List<SetPropertyExpression> setPropertyExpressions) {
    this.graphPattern = graphPattern;
    this.intoGraphName = intoGraphName;
    this.setPropertyExpressions = setPropertyExpressions;
  }

  public GraphPattern getGraphPattern() {
    return graphPattern;
  }

  public void setGraphPattern(GraphPattern graphPattern) {
    this.graphPattern = graphPattern;
  }

  public String getIntoGraphName() {
    return intoGraphName;
  }

  public void setIntoGraphName(String graphName) {
    this.intoGraphName = graphName;
  }

  public List<SetPropertyExpression> getSetPropertyExpressions() {
    return setPropertyExpressions;
  }

  public void setSetPropertyExpressions(List<SetPropertyExpression> setPropertyExpressions) {
    this.setPropertyExpressions = setPropertyExpressions;
  }

  @Override
  public String toString() {
    String intoGraphString;
    if (intoGraphName == null) {
      intoGraphString = "";
    } else {
      intoGraphString = " INTO " + intoGraphName;
    }

    String setPropertyExpressionsString;
    if (setPropertyExpressions.isEmpty()) {
      setPropertyExpressionsString = "";
    } else {
      setPropertyExpressionsString = "SET " + setPropertyExpressions.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(", "));
    }

    return "INSERT " + graphPattern + intoGraphString + setPropertyExpressionsString;
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
    GraphInsert other = (GraphInsert) obj;
    if (graphPattern == null) {
      if (other.graphPattern != null)
        return false;
    } else if (!graphPattern.equals(other.graphPattern))
      return false;
    if (intoGraphName == null) {
      if (other.intoGraphName != null)
        return false;
    } else if (!intoGraphName.equals(other.intoGraphName))
      return false;
    if (setPropertyExpressions == null) {
      if (other.setPropertyExpressions != null)
        return false;
    } else if (!setPropertyExpressions.equals(other.setPropertyExpressions))
      return false;
    return true;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
