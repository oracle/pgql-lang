/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.update;

import java.util.List;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryType;

public class GraphInsertQuery extends GraphQuery {

  private static final String PROJECTION_ERROR = "An insert query does not have a SELECT";

  private GraphInsert graphInsert;

  public GraphInsertQuery(List<CommonPathExpression> commonPathExpressions, GraphInsert graphInsert,
      String inputGraphName, GraphPattern graphPattern, GroupBy groupBy, QueryExpression having, OrderBy orderBy,
      QueryExpression limit, QueryExpression offset) {
    super(commonPathExpressions, inputGraphName, graphPattern, groupBy, having, orderBy, limit, offset);
    this.graphInsert = graphInsert;
  }

  @Override
  public QueryType getQueryType() {
    return QueryType.GRAPH_INSERT;
  }

  public GraphInsert getGraphInsert() {
    return graphInsert;
  }

  public void setGraphInsert(GraphInsert graphInsert) {
    this.graphInsert = graphInsert;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    GraphInsertQuery other = (GraphInsertQuery) obj;
    if (graphInsert == null) {
      if (other.graphInsert != null)
        return false;
    } else if (!graphInsert.equals(other.graphInsert))
      return false;
    return true;
  }

  @Override
  public Projection getProjection() {
    throw new UnsupportedOperationException(PROJECTION_ERROR);
  }

  @Override
  public void setProjection(Projection projection) {
    throw new UnsupportedOperationException(PROJECTION_ERROR);
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
