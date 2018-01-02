/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphQuery {

  private final List<CommonPathExpression> commonPathExpressions;

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
  public GraphQuery(List<CommonPathExpression> commonPathExpressions, Projection projection, String inputGraphName,
      GraphPattern graphPattern, GroupBy groupBy, OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    this.commonPathExpressions = commonPathExpressions;
    this.projection = projection;
    this.inputGraphName = inputGraphName;
    this.graphPattern = graphPattern;
    this.groupBy = groupBy;
    this.orderBy = orderBy;
    this.limit = limit;
    this.offset = offset;
  }

  public List<CommonPathExpression> getCommonPathExpressions() {
    return commonPathExpressions;
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

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((commonPathExpressions == null) ? 0 : commonPathExpressions.hashCode());
    result = prime * result + ((graphPattern == null) ? 0 : graphPattern.hashCode());
    result = prime * result + ((groupBy == null) ? 0 : groupBy.hashCode());
    result = prime * result + ((inputGraphName == null) ? 0 : inputGraphName.hashCode());
    result = prime * result + ((limit == null) ? 0 : limit.hashCode());
    result = prime * result + ((offset == null) ? 0 : offset.hashCode());
    result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
    result = prime * result + ((projection == null) ? 0 : projection.hashCode());
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
    GraphQuery other = (GraphQuery) obj;
    if (commonPathExpressions == null) {
      if (other.commonPathExpressions != null)
        return false;
    } else if (!commonPathExpressions.equals(other.commonPathExpressions))
      return false;
    if (graphPattern == null) {
      if (other.graphPattern != null)
        return false;
    } else if (!graphPattern.equals(other.graphPattern))
      return false;
    if (groupBy == null) {
      if (other.groupBy != null)
        return false;
    } else if (!groupBy.equals(other.groupBy))
      return false;
    if (inputGraphName == null) {
      if (other.inputGraphName != null)
        return false;
    } else if (!inputGraphName.equals(other.inputGraphName))
      return false;
    if (limit == null) {
      if (other.limit != null)
        return false;
    } else if (!limit.equals(other.limit))
      return false;
    if (offset == null) {
      if (other.offset != null)
        return false;
    } else if (!offset.equals(other.offset))
      return false;
    if (orderBy == null) {
      if (other.orderBy != null)
        return false;
    } else if (!orderBy.equals(other.orderBy))
      return false;
    if (projection == null) {
      if (other.projection != null)
        return false;
    } else if (!projection.equals(other.projection))
      return false;
    return true;
  }
}
