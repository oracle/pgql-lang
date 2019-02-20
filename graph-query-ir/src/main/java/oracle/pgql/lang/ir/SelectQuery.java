/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

public class SelectQuery extends GraphQuery {

  private Projection projection;

  public SelectQuery(List<CommonPathExpression> commonPathExpressions, Projection projection, String inputGraphName,
      GraphPattern graphPattern, GroupBy groupBy, QueryExpression having, OrderBy orderBy, QueryExpression limit,
      QueryExpression offset) {
    super(commonPathExpressions, inputGraphName, graphPattern, groupBy, having, orderBy, limit, offset);
    this.projection = projection;
  }

  @Override
  public QueryType getQueryType() {
    return QueryType.SELECT;
  }

  public Projection getProjection() {
    return projection;
  }

  public void setProjection(Projection projection) {
    this.projection = projection;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SelectQuery other = (SelectQuery) obj;
    if (projection == null) {
      if (other.projection != null)
        return false;
    } else if (!projection.equals(other.projection))
      return false;
    return true;
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
