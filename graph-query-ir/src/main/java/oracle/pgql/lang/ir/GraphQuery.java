/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphQuery {

  private final Projection projection;

  private final GraphPattern graphPattern;

  private final GroupBy groupBy;
  
  private final OrderBy orderBy;
  
  private final long limit;
  
  private final long offset;

  /**
   * Constructor
   */
  public GraphQuery(Projection projection, GraphPattern graphPattern, GroupBy groupBy,
      OrderBy orderBy, long limit, long offset) {
    this.projection = projection;
    this.graphPattern = graphPattern;
    this.groupBy = groupBy;
    this.orderBy = orderBy;
    this.limit = limit;
    this.offset = offset;
  }

  public Projection getProjection() {
    return projection;
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

  public long getLimit() {
    return limit;
  }

  public long getOffset() {
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
    result = 31 * result + (int) (limit ^ (limit >>> 32));
    result = 31 * result + (int) (offset ^ (offset >>> 32));
    return result;
  }
}
