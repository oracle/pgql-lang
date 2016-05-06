/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

public class GraphQuery {

  private final List<ExpAsVar> select;

  private final GraphPattern graphPattern;

  private final List<ExpAsVar> groupBy;
  
  private final List<OrderByElem> orderBy;
  
  private final long limit;
  
  private final long offset;

  /**
   * Constructor
   */
  public GraphQuery(List<ExpAsVar> selectElems, GraphPattern graphPattern, List<ExpAsVar> groupByElems,
      List<OrderByElem> orderByElems, long limit, long offset) {
    this.select = selectElems;
    this.graphPattern = graphPattern;
    this.groupBy = groupByElems;
    this.orderBy = orderByElems;
    this.limit = limit;
    this.offset = offset;
  }

  public List<ExpAsVar> getSelect() {
    return select;
  }

  public GraphPattern getGraphPattern() {
    return graphPattern;
  }

  public List<ExpAsVar> getGroupBy() {
    return groupBy;
  }

  public List<OrderByElem> getOrderBy() {
    return orderBy;
  }

  public long getLimit() {
    return limit;
  }

  public long getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    String result = "Query:";
    result += "\n  Projection:";
    for (ExpAsVar e : select) {
      result += "\n    " + e;
    }
    System.out.println(graphPattern);
    result += "\n  GroupBy:";
    for (ExpAsVar e : groupBy) {
      result += "\n    " + e;
    }
    result += "\n  OrderBy:";
    for (OrderByElem e : orderBy) {
      result += "\n    " + e;
    }
    result += "\n  Limit:\n    " + limit;
    result += "\n  Offset:\n    " + offset;
    return result;
  }
}
