/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

import static oracle.pgql.lang.ir.PgqlUtils.printHops;

public class QueryPath extends VertexPairConnection {

  private CommonPathExpression commonPathExpression; // FIXME: setter missing

  private long minHops;

  private long maxHops;

  private PathFindingGoal goal; // FIXME: setter missing

  private long kValue; // FIXME: setter missing

  public QueryPath(QueryVertex src, QueryVertex dst, String name, CommonPathExpression commonPathExpression,
      boolean anonymous, long minHops, long maxHops, PathFindingGoal goal, long kValue) {
    super(src, dst, name, anonymous);
    this.commonPathExpression = commonPathExpression;
    this.minHops = minHops;
    this.maxHops = maxHops;
    this.goal = goal;
    this.kValue = kValue;
  }

  public String getPathExpressionName() {
    return commonPathExpression.getName();
  }

  public List<QueryVertex> getVertices() {
    return commonPathExpression.getVertices();
  }

  public List<VertexPairConnection> getConnections() {
    return commonPathExpression.getConnections();
  }

  public Set<QueryExpression> getConstraints() {
    return commonPathExpression.getConstraints();
  }

  /**
   * @return minimal number of hops
   */
  public long getMinHops() {
    return minHops;
  }

  public void setMinHops(long minHops) {
    this.minHops = minHops;
  }

  /**
   * @return maximal number of hops, -1 if none is specified
   */
  public long getMaxHops() {
    return maxHops;
  }

  public void setMaxHops(long maxHops) {
    this.maxHops = maxHops;
  }

  public PathFindingGoal getPathFindingGoal() {
    return goal;
  }

  public long getKValue() {
    return kValue;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }

  @Override
  public String toString() {
    String path = "-/";
    if (!isAnonymous()) {
      path += name;
    }
    return path + ":" + commonPathExpression.getName() + printHops(this) + "/->";
  }

  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
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
    QueryPath other = (QueryPath) obj;
    if (commonPathExpression == null) {
      if (other.commonPathExpression != null)
        return false;
    } else if (!commonPathExpression.equals(other.commonPathExpression))
      return false;
    if (maxHops != other.maxHops)
      return false;
    if (minHops != other.minHops)
      return false;
    if (goal != other.goal) {
      return false;
    }
    return true;
  }
}
