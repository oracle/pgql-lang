/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
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
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;

public class ModifyQuery extends GraphQuery {

  private static final String PROJECTION_ERROR = "A modify query does not have a SELECT";

  private List<Modification> modifications;

  public ModifyQuery(List<CommonPathExpression> commonPathExpressions, List<Modification> modifications,
      SchemaQualifiedName graphName, GraphPattern graphPattern, GroupBy groupBy, QueryExpression having,
      OrderBy orderBy, QueryExpression limit, QueryExpression offset) {
    super(commonPathExpressions, graphName, graphPattern, groupBy, having, orderBy, limit, offset);
    this.modifications = modifications;
  }

  @Override
  public QueryType getQueryType() {
    return QueryType.MODIFY;
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.GRAPH_MODIFY;
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
    return 31;
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
