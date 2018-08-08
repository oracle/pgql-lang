/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.util;

import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrAvg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrCount;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMax;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMin;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrSum;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrArrayAgg;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Add;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Div;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mod;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mul;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Sub;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.UMin;
import oracle.pgql.lang.ir.QueryExpression.BindVariable;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstBoolean;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstDate;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstDecimal;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstInteger;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTime;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTimeWithTimezone;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTimestamp;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTimestampWithTimezone;
import oracle.pgql.lang.ir.QueryExpression.ExtractExpression;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.InPredicate;
import oracle.pgql.lang.ir.QueryExpression.InPredicate.InValueList;
import oracle.pgql.lang.ir.QueryExpression.Function.Cast;
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.And;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Not;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Or;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Equal;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Greater;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.GreaterEqual;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Less;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.LessEqual;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.NotEqual;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.QueryExpression.Star;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVertex;

public abstract class AbstractQueryExpressionVisitor implements QueryExpressionVisitor {

  @Override
  public void visit(VarRef varRef) {
  }

  @Override
  public void visit(BindVariable bindVariable) {
  }

  @Override
  public void visit(PropertyAccess propAccess) {
  }

  @Override
  public void visit(ConstInteger constInteger) {
  }

  @Override
  public void visit(ConstDecimal constDecimal) {
  }

  @Override
  public void visit(ConstString constString) {
  }

  @Override
  public void visit(ConstBoolean constBoolean) {
  }

  @Override
  public void visit(ConstDate constDate) {
  }

  @Override
  public void visit(ConstTime constTime) {
  }

  @Override
  public void visit(ConstTimestamp constTimestamp) {
  }

  @Override
  public void visit(ConstTimeWithTimezone constTimeWithTimezone) {
  }

  @Override
  public void visit(ConstTimestampWithTimezone constTimestampWithTimezone) {
  }

  @Override
  public void visit(Sub sub) {
    sub.getExp1().accept(this);
    sub.getExp2().accept(this);
  }

  @Override
  public void visit(Add add) {
    add.getExp1().accept(this);
    add.getExp2().accept(this);
  }

  @Override
  public void visit(Mul mul) {
    mul.getExp1().accept(this);
    mul.getExp2().accept(this);
  }

  @Override
  public void visit(Div div) {
    div.getExp1().accept(this);
    div.getExp2().accept(this);
  }

  @Override
  public void visit(Mod mod) {
    mod.getExp1().accept(this);
    mod.getExp2().accept(this);
  }

  @Override
  public void visit(UMin uMin) {
    uMin.getExp().accept(this);
  }

  @Override
  public void visit(And and) {
    and.getExp1().accept(this);
    and.getExp2().accept(this);
  }

  @Override
  public void visit(Or or) {
    or.getExp1().accept(this);
    or.getExp2().accept(this);
  }

  @Override
  public void visit(Not not) {
    not.getExp().accept(this);
  }

  @Override
  public void visit(Equal equal) {
    equal.getExp1().accept(this);
    equal.getExp2().accept(this);
  }

  @Override
  public void visit(NotEqual notEqual) {
    notEqual.getExp1().accept(this);
    notEqual.getExp2().accept(this);
  }

  @Override
  public void visit(Greater greater) {
    greater.getExp1().accept(this);
    greater.getExp2().accept(this);
  }

  @Override
  public void visit(GreaterEqual greaterEqual) {
    greaterEqual.getExp1().accept(this);
    greaterEqual.getExp2().accept(this);
  }

  @Override
  public void visit(Less less) {
    less.getExp1().accept(this);
    less.getExp2().accept(this);
  }

  @Override
  public void visit(LessEqual lessEqual) {
    lessEqual.getExp1().accept(this);
    lessEqual.getExp2().accept(this);
  }

  @Override
  public void visit(AggrCount aggrCount) {
    aggrCount.getExp().accept(this);
  }

  @Override
  public void visit(AggrMin aggrMin) {
    aggrMin.getExp().accept(this);
  }

  @Override
  public void visit(AggrMax aggrMax) {
    aggrMax.getExp().accept(this);
  }

  @Override
  public void visit(AggrSum aggrSum) {
    aggrSum.getExp().accept(this);
  }

  @Override
  public void visit(AggrAvg aggrAvg) {
    aggrAvg.getExp().accept(this);
  }

  @Override
  public void visit(AggrArrayAgg aggrArrayAgg) {
    aggrArrayAgg.getExp().accept(this);
  }

  @Override
  public void visit(Star star) {
  }

  @Override
  public void visit(Cast cast) {
    cast.getExp().accept(this);
  }

  @Override
  public void visit(FunctionCall functionCall) {
    functionCall.getArgs().stream().forEach(e -> e.accept(this));
  }

  @Override
  public void visit(ExtractExpression extractExpression) {
    extractExpression.getExp().accept(this);
  }

  @Override
  public void visit(InPredicate inPredicate) {
    inPredicate.getExp().accept(this);
    inPredicate.getInValueList().accept(this);
  }

  @Override
  public void visit(InValueList inValueList) {
  }

  @Override
  public void visit(Exists exists) {
    exists.getQuery().accept(this);
  }

  @Override
  public void visit(ScalarSubquery scalarSubquery) {
    scalarSubquery.getQuery().accept(this);
  }

  @Override
  public void visit(GraphQuery query) {
    query.getProjection().accept(this);
    query.getGraphPattern().accept(this);
    if (query.getGroupBy() != null) {
      query.getGroupBy().accept(this);
    }
    if (query.getHaving() != null) {
      query.getHaving().accept(this);
    }
    query.getOrderBy().accept(this);
    if (query.getLimit() != null) {
      query.getLimit().accept(this);
    }
    if (query.getOffset() != null) {
      query.getOffset().accept(this);
    }
  }

  @Override
  public void visit(GraphPattern graphPattern) {
    graphPattern.getVertices().stream().forEach(e -> e.accept(this));
    graphPattern.getConnections().stream().forEach(e -> e.accept(this));
    graphPattern.getConstraints().stream().forEach(e -> e.accept(this));
  }

  @Override
  public void visit(Projection projection) {
    projection.getElements().stream().forEach(e -> e.accept(this));
  }

  @Override
  public void visit(ExpAsVar expAsVar) {
    expAsVar.getExp().accept(this);
  }

  @Override
  public void visit(QueryVertex queryVertex) {
  }

  @Override
  public void visit(QueryEdge queryEdge) {
  }

  @Override
  public void visit(QueryPath queryPath) {
    queryPath.getConnections().stream().forEach(e -> e.accept(this));
    queryPath.getConstraints().stream().forEach(e -> e.accept(this));
  }

  @Override
  public void visit(GroupBy groupBy) {
    groupBy.getElements().stream().forEach(e -> e.accept(this));
  }

  @Override
  public void visit(OrderBy orderBy) {
    orderBy.getElements().stream().forEach(e -> e.accept(this));
  }

  @Override
  public void visit(OrderByElem orderByElem) {
    orderByElem.getExp().accept(this);
  }
}
