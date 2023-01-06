/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public abstract class GraphQuery implements PgqlStatement {

  private List<CommonPathExpression> commonPathExpressions;

  private SchemaQualifiedName graphName;

  private List<TableExpression> tableExpressions;

  private GroupBy groupBy;

  private QueryExpression having;

  private OrderBy orderBy;

  private QueryExpression limit;

  private QueryExpression offset;

  /**
   * Constructor
   */
  protected GraphQuery(List<CommonPathExpression> commonPathExpressions, SchemaQualifiedName graphName,
      List<TableExpression> tableExpressions, GroupBy groupBy, QueryExpression having, OrderBy orderBy,
      QueryExpression limit, QueryExpression offset) {
    this.commonPathExpressions = commonPathExpressions;
    this.graphName = graphName;
    this.tableExpressions = tableExpressions;
    this.groupBy = groupBy;
    this.having = having;
    this.orderBy = orderBy;
    this.limit = limit;
    this.offset = offset;
  }

  public abstract QueryType getQueryType();

  public List<CommonPathExpression> getCommonPathExpressions() {
    return commonPathExpressions;
  }

  public void setCommonPathExpressions(List<CommonPathExpression> commonPathExpressions) {
    this.commonPathExpressions = commonPathExpressions;
  }

  /**
   * @deprecated cast {@link GraphQuery} into {@link SelectQuery} before calling {@link SelectQuery#getProjection}
   */
  @Deprecated
  public abstract Projection getProjection();

  /**
   * @deprecated cast {@link GraphQuery} into {@link SelectQuery} before calling {@link SelectQuery#setProjection}
   */
  @Deprecated
  public abstract void setProjection(Projection projection);

  /**
   * @deprecated use {@link #getGraphName()} instead
   */
  @Deprecated
  public String getInputGraphName() {
    return graphName == null ? null : graphName.getName();
  }

  /**
   * @deprecated use {@link #setGraphName()} instead
   */
  @Deprecated
  public void setInputGraphName(String inputGraphName) {
    if (graphName == null) {
      this.graphName = new SchemaQualifiedName(null, inputGraphName);
    } else {
      this.graphName.setName(inputGraphName);
    }
  }

  public SchemaQualifiedName getGraphName() {
    return graphName;
  }

  public void setGraphName(SchemaQualifiedName graphName) {
    this.graphName = graphName;
  }

  public GraphPattern getGraphPattern() {
    guaranteeQueryHasAtMostSingleGraphPattern();
    if (tableExpressions.isEmpty()) {
      return null;
    } else {
      return (GraphPattern) tableExpressions.get(0);
    }
  }

  public void setGraphPattern(GraphPattern graphPattern) {
    guaranteeQueryHasAtMostSingleGraphPattern();
    this.tableExpressions.set(0, graphPattern);
  }

  public List<TableExpression> getTableExpressions() {
    return tableExpressions;
  }

  public void setTableExpressions(List<TableExpression> tableExpressions) {
    this.tableExpressions = tableExpressions;
  }

  public GroupBy getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(GroupBy groupBy) {
    this.groupBy = groupBy;
  }

  public QueryExpression getHaving() {
    return having;
  }

  public void setHaving(QueryExpression having) {
    this.having = having;
  }

  public OrderBy getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
  }

  public QueryExpression getLimit() {
    return limit;
  }

  public void setLimit(QueryExpression limit) {
    this.limit = limit;
  }

  public QueryExpression getOffset() {
    return offset;
  }

  public void setOffset(QueryExpression offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
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
    GraphQuery other = (GraphQuery) obj;
    if (commonPathExpressions == null) {
      if (other.commonPathExpressions != null)
        return false;
    } else if (!commonPathExpressions.equals(other.commonPathExpressions))
      return false;
    if (graphName == null) {
      if (other.graphName != null)
        return false;
    } else if (!graphName.equals(other.graphName))
      return false;
    if (groupBy == null) {
      if (other.groupBy != null)
        return false;
    } else if (!groupBy.equals(other.groupBy))
      return false;
    if (having == null) {
      if (other.having != null)
        return false;
    } else if (!having.equals(other.having))
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
    if (tableExpressions == null) {
      if (other.tableExpressions != null)
        return false;
    } else if (!tableExpressions.equals(other.tableExpressions))
      return false;
    return true;
  }

  public abstract void accept(QueryExpressionVisitor v);

  private void guaranteeQueryHasAtMostSingleGraphPattern() {
    if (tableExpressions.size() == 0) {
      return; // INSERT without FROM clause
    }

    if (tableExpressions.get(0).getTableExpressionType() != TableExpressionType.GRAPH_PATTERN) {
      throw new UnsupportedOperationException("Subqueries in FROM clause not supported");
    }
  }
}
