/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

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

public class ModifyQuery extends GraphQuery {

  private static final String PROJECTION_ERROR = "A modify query does not have a SELECT";

  private String graphName;

  private List<Modification> modifications;

  public ModifyQuery(List<CommonPathExpression> commonPathExpressions, String graphName,
      List<Modification> modifications, String inputGraphName, GraphPattern graphPattern, GroupBy groupBy,
      QueryExpression having, OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    super(commonPathExpressions, inputGraphName, graphPattern, groupBy, having, orderBy, limit, offset);
    this.graphName = graphName;
    this.modifications = modifications;
  }

  @Override
  public QueryType getQueryType() {
    return QueryType.MODIFY;
  }

  public String getGraphName() {
    return graphName;
  }

  public void setGraphName(String graphName) {
    this.graphName = graphName;
  }

  public List<Modification> getModifications() {
    return modifications;
  }

  public void setModifications(List<Modification> modifications) {
    this.modifications = modifications;
  }

  @Override
  public Projection getProjection() {
    throw new UnsupportedOperationException(PROJECTION_ERROR);
  }

  @Override
  public void setProjection(Projection projection) {
    throw new UnsupportedOperationException(PROJECTION_ERROR);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((graphName == null) ? 0 : graphName.hashCode());
    result = prime * result + ((modifications == null) ? 0 : modifications.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ModifyQuery other = (ModifyQuery) obj;
    if (graphName == null) {
      if (other.graphName != null)
        return false;
    } else if (!graphName.equals(other.graphName))
      return false;
    if (modifications == null) {
      if (other.modifications != null)
        return false;
    } else if (!modifications.equals(other.modifications))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }

}
