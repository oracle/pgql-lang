/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.util;

import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrAvg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrCount;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMax;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMin;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrSum;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.Star;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Add;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Div;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mod;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mul;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Sub;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.UMin;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryExpression.ConstNull;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstBoolean;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstDecimal;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstInteger;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.Function.EdgeLabel;
import oracle.pgql.lang.ir.QueryExpression.Function.HasLabel;
import oracle.pgql.lang.ir.QueryExpression.Function.HasProp;
import oracle.pgql.lang.ir.QueryExpression.Function.Id;
import oracle.pgql.lang.ir.QueryExpression.Function.InDegree;
import oracle.pgql.lang.ir.QueryExpression.Function.OutDegree;
import oracle.pgql.lang.ir.QueryExpression.Function.Regex;
import oracle.pgql.lang.ir.QueryExpression.Function.VertexLabels;
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
import oracle.pgql.lang.ir.QueryExpression.VarRef;

public abstract class AbstractExpressionVisitor implements QueryExpressionVisitor {

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
  public void visit(ConstNull constantNull) {
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
  public void visit(VarRef varRef) {
  }

  @Override
  public void visit(Star star) {
  }

  @Override
  public void visit(Regex regex) {
    regex.getExp1().accept(this);
    regex.getExp2().accept(this);
  }

  @Override
  public void visit(Id id) {
    id.getExp().accept(this);
  }

  @Override
  public void visit(PropertyAccess propAccess) {
  }

  @Override
  public void visit(HasProp hasProp) {
    hasProp.getExp1().accept(this);
    hasProp.getExp2().accept(this);
  }

  @Override
  public void visit(HasLabel hasLabel) {
    hasLabel.getExp1().accept(this);
    hasLabel.getExp2().accept(this);
  }

  @Override
  public void visit(VertexLabels vertexLabels) {
    vertexLabels.getExp().accept(this);
  }

  @Override
  public void visit(InDegree inDegree) {
    inDegree.getExp().accept(this);
  }

  @Override
  public void visit(OutDegree outDegree) {
    outDegree.getExp().accept(this);
  }

  @Override
  public void visit(EdgeLabel edgeLabel) {
    edgeLabel.getExp().accept(this);
  }
}