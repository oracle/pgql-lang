/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphQuery {

  private final Projection projection;

  private final String inputGraphName;

  private final GraphPattern graphPattern;

  private final GroupBy groupBy;
  
  private final OrderBy orderBy;
  
  private final QueryExpression limit;
  
  private final QueryExpression offset;

  /**
   * Constructor
   */
  public GraphQuery(Projection projection, String inputGraphName, GraphPattern graphPattern, GroupBy groupBy,
      OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    this.projection = projection;
    this.inputGraphName = inputGraphName;
    this.graphPattern = graphPattern;
    this.groupBy = groupBy;
    this.orderBy = orderBy;
    this.limit = limit;
    this.offset = offset;
  }

  public Projection getProjection() {
    return projection;
  }

  public String getInputGraphName() {
    return inputGraphName;
  }

  public GraphPattern getGraphPattern() {
    return graphPattern;
  }

  public GroupBy getGroupBy() {
    return groupBy;
  }

  public OrderBy getOrderBy() {
    return orderBy;
  }

  public QueryExpression getLimit() {
    return limit;
  }

  public QueryExpression getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphQuery that = (GraphQuery) o;

    if (limit != that.limit) {
      return false;
    }
    if (offset != that.offset) {
      return false;
    }
    if (!projection.equals(that.projection)) {
      return false;
    }
    if (!graphPattern.equals(that.graphPattern)) {
      return false;
    }
    if (!groupBy.equals(that.groupBy)) {
      return false;
    }
    return orderBy.equals(that.orderBy);
  }

  @Override
  public int hashCode() {
    int result = projection.hashCode();
    result = 31 * result + graphPattern.hashCode();
    result = 31 * result + groupBy.hashCode();
    result = 31 * result + orderBy.hashCode();
    result = 31 * result + limit.hashCode();
    result = 31 * result + offset.hashCode();
    return result;
  }
}
