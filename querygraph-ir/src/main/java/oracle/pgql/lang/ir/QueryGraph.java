/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

public class QueryGraph {

  // SELECT
  public final List<ExpAsVar> selectElems;

  // FROM
  // nothing here yet

  // WHERE
  public final List<QueryVertex> vertices;
  public final List<QueryConnection> connections; // edges or paths
  public final List<QueryExpression> constraints;

  // Solution modifiers
  public final List<ExpAsVar> groupByElems;
  public final List<OrderByElem> orderByElems;
  public final long limit;
  public final long offset;

  /**
   * Constructor
   */
  public QueryGraph(List<ExpAsVar> selectElems, List<QueryVertex> vertices, List<QueryConnection> connections,
      List<QueryExpression> constraints, List<ExpAsVar> groupByElems, List<OrderByElem> orderByElems, long limit,
      long offset) {
    this.selectElems = selectElems;
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
    this.groupByElems = groupByElems;
    this.orderByElems = orderByElems;
    this.limit = limit;
    this.offset = offset;
  }

  @Override
  public String toString() {
    String result = "Query:";
    result += "\n  Projection:";
    for (ExpAsVar e : selectElems) {
      result += "\n    " + e;
    }
    result += "\n  Vertices:";
    for (QueryVertex n : vertices) {
      result += "\n    " + n;
    }
    result += "\n  Connections:";
    for (QueryConnection c : connections) {
      result += "\n    " + c;
    }
    result += "\n  Constraints:";
    for (QueryExpression e : constraints) {
      result += "\n    " + e;
    }
    result += "\n  GroupBy:";
    for (ExpAsVar e : groupByElems) {
      result += "\n    " + e;
    }
    result += "\n  OrderBy:";
    for (OrderByElem e : orderByElems) {
      result += "\n    " + e;
    }
    result += "\n  Limit:\n    " + limit;
    result += "\n  Offset:\n    " + offset;
    return result;
  }
}
