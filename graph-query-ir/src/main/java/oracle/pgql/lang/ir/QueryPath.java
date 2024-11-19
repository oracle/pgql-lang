/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

import oracle.pgql.lang.ir.unnest.OneRowPerMatch;
import oracle.pgql.lang.ir.unnest.RowsPerMatch;
import oracle.pgql.lang.ir.unnest.RowsPerMatchType;

import static oracle.pgql.lang.ir.PgqlUtils.GENERATED_VAR_PREFIX;
import static oracle.pgql.lang.ir.PgqlUtils.printHops;
import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;
import static oracle.pgql.lang.ir.PgqlUtils.printPathExpression;

public class QueryPath extends VertexPairConnection {

  private CommonPathExpression commonPathExpression;

  private long minHops;

  private long maxHops;

  private PathFindingGoal goal;

  private int kValue;

  private boolean withTies;

  private PathMode pathMode;

  private RowsPerMatch rowsPerMatch;

  public QueryPath(QueryVertex src, QueryVertex dst, String name, CommonPathExpression commonPathExpression,
      boolean anonymous, long minHops, long maxHops, PathFindingGoal goal, int kValue, boolean withTies,
      PathMode pathMode, Direction direction, RowsPerMatch rowsPerMatch) {
    super(src, dst, name, anonymous, direction);
    this.commonPathExpression = commonPathExpression;
    this.minHops = minHops;
    this.maxHops = maxHops;
    this.goal = goal;
    this.kValue = kValue;
    this.withTies = withTies;
    this.pathMode = pathMode;
    this.rowsPerMatch = rowsPerMatch;
  }

  public QueryPath(QueryVertex src, QueryVertex dst, String name, CommonPathExpression commonPathExpression,
      boolean anonymous, long minHops, long maxHops, PathFindingGoal goal, int kValue, boolean withTies,
      Direction direction, RowsPerMatch rowsPerMatch) {
    this(src, dst, name, commonPathExpression, anonymous, minHops, maxHops, goal, kValue, withTies, PathMode.WALK,
        direction, rowsPerMatch);
  }

  public QueryPath(QueryVertex src, QueryVertex dst, String name, CommonPathExpression commonPathExpression,
      boolean anonymous, long minHops, long maxHops, PathFindingGoal goal, int kValue, boolean withTies,
      Direction direction) {
    this(src, dst, name, commonPathExpression, anonymous, minHops, maxHops, goal, kValue, withTies, PathMode.WALK,
        direction, new OneRowPerMatch());
  }

  public String getPathExpressionName() {
    return commonPathExpression.getName();
  }

  public void setPathExpressionName() {
    commonPathExpression.setName(name);
  }

  public List<QueryVertex> getVertices() {
    return commonPathExpression.getVertices();
  }

  public void setVertices(List<QueryVertex> vertices) {
    commonPathExpression.setVertices(vertices);
  }

  public List<VertexPairConnection> getConnections() {
    return commonPathExpression.getConnections();
  }

  public void setConnections(List<VertexPairConnection> connections) {
    commonPathExpression.setConnections(connections);
  }

  public Set<QueryExpression> getConstraints() {
    return commonPathExpression.getConstraints();
  }

  public void setConstraints(Set<QueryExpression> contstraints) {
    commonPathExpression.setConstraints(contstraints);
  }

  public QueryExpression getCost() {
    return commonPathExpression.getCost();
  }

  public void setCost(QueryExpression cost) {
    commonPathExpression.setCost(cost);
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

  public void setPathFindingGoal(PathFindingGoal goal) {
    this.goal = goal;
  }

  public int getKValue() {
    return kValue;
  }

  public void setKValue(int kValue) {
    this.kValue = kValue;
  }

  public boolean getWithTies() {
    return withTies;
  }

  public void setWithTies(boolean withTies) {
    this.withTies = withTies;
  }

  public PathMode getPathMode() {
    return pathMode;
  }

  public void setPathMode(PathMode pathMode) {
    this.pathMode = pathMode;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }

  public RowsPerMatch getRowsPerMatch() {
    return rowsPerMatch;
  }

  public void setRowsPerMatch(RowsPerMatch rowsPerMatch) {
    this.rowsPerMatch = rowsPerMatch;
  }

  @Override
  public String toString() {
    switch (goal) {
      case REACHES:
        if (commonPathExpression.getName().startsWith(GENERATED_VAR_PREFIX)) {
          // ANY
          return printVariableLengthPathPattern(goal);
        } else {
          // -/../->
          String path = "-/";
          if (!isAnonymous()) {
            path += printIdentifier(name, false);
          }
          return path + ":" + printIdentifier(commonPathExpression.getName(), false) + printHops(this) + "/->";
        }
      case SHORTEST:
      case CHEAPEST:
      case ALL:
        return printVariableLengthPathPattern(goal);
      default:
        throw new UnsupportedOperationException(goal.name());
    }
  }

  private String printVariableLengthPathPattern(PathFindingGoal goal) {
    String pathPatternPrefix = null;
    switch (goal) {
      case REACHES:
        pathPatternPrefix = "ANY ";
        break;
      case ALL:
        pathPatternPrefix = "ALL ";
        break;
      case SHORTEST:
      case CHEAPEST:
        if (kValue == 1) {
          pathPatternPrefix = (withTies ? "ALL " : "ANY ") + goal.toString() + " ";
        } else {
          pathPatternPrefix = goal.toString() + " " + kValue + " ";
        }
        break;
      default:
        new UnsupportedOperationException(goal.name());
    }

    pathPatternPrefix += pathMode == PathMode.WALK ? "" : pathMode.toString() + " ";
    String result = pathPatternPrefix + getSrc() + " ";
    String pathExpression = printPathExpression(commonPathExpression, true);
    if (pathExpression.contains("WHERE") || pathExpression.contains("COST") || pathExpression.startsWith("(")
        || pathExpression.endsWith(")")) {
      result += "(" + pathExpression + ")";
    } else {
      result += pathExpression;
    }

    result += printHops(this) + " " + getDst();

    return result;
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
    if (goal != other.goal)
      return false;
    if (kValue != other.kValue)
      return false;
    if (withTies != other.withTies)
      return false;
    if (!pathMode.equals(other.pathMode))
      return false;
    if (!rowsPerMatch.equals(other.rowsPerMatch)) {
      return false;
    }
    return true;
  }
}
