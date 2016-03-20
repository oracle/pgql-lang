/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

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
import oracle.pgql.lang.ir.QueryExpression.PropAccess;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Equal;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Greater;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.GreaterEqual;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Less;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.LessEqual;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.NotEqual;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

public abstract class AbstractTopDownExpressionVisitor implements ExpressionVisitor {

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
    sub.exp1.accept(this);
    sub.exp2.accept(this);
  }

  @Override
  public void visit(Add add) {
    add.exp1.accept(this);
    add.exp2.accept(this);
  }

  @Override
  public void visit(Mul mul) {
    mul.exp1.accept(this);
    mul.exp2.accept(this);
  }

  @Override
  public void visit(Div div) {
    div.exp1.accept(this);
    div.exp2.accept(this);
  }

  @Override
  public void visit(Mod mod) {
    mod.exp1.accept(this);
    mod.exp2.accept(this);
  }

  @Override
  public void visit(UMin uMin) {
    uMin.exp.accept(this);
  }

  @Override
  public void visit(And and) {
    and.exp1.accept(this);
    and.exp2.accept(this);
  }

  @Override
  public void visit(Or or) {
    or.exp1.accept(this);
    or.exp2.accept(this);
  }

  @Override
  public void visit(Not not) {
    not.exp.accept(this);
  }

  @Override
  public void visit(Equal equal) {
    equal.exp1.accept(this);
    equal.exp2.accept(this);
  }

  @Override
  public void visit(NotEqual notEqual) {
    notEqual.exp1.accept(this);
    notEqual.exp2.accept(this);
  }

  @Override
  public void visit(Greater greater) {
    greater.exp1.accept(this);
    greater.exp2.accept(this);
  }

  @Override
  public void visit(GreaterEqual greaterEqual) {
    greaterEqual.exp1.accept(this);
    greaterEqual.exp2.accept(this);
  }

  @Override
  public void visit(Less less) {
    less.exp1.accept(this);
    less.exp2.accept(this);
  }

  @Override
  public void visit(LessEqual lessEqual) {
    lessEqual.exp1.accept(this);
    lessEqual.exp2.accept(this);
  }

  @Override
  public void visit(AggrCount aggrCount) {
    aggrCount.exp.accept(this);
  }

  @Override
  public void visit(AggrMin aggrMin) {
    aggrMin.exp.accept(this);
  }

  @Override
  public void visit(AggrMax aggrMax) {
    aggrMax.exp.accept(this);
  }

  @Override
  public void visit(AggrSum aggrSum) {
    aggrSum.exp.accept(this);
  }

  @Override
  public void visit(AggrAvg aggrAvg) {
    aggrAvg.exp.accept(this);
  }

  @Override
  public void visit(VarRef varRef) {
  }

  @Override
  public void visit(Star star) {
  }

  @Override
  public void visit(Regex regex) {
    regex.exp1.accept(this);
    regex.exp2.accept(this);
  }

  @Override
  public void visit(Id id) {
    id.exp.accept(this);
  }

  @Override
  public void visit(PropAccess propAccess) {
  }

  @Override
  public void visit(HasProp hasProp) {
    hasProp.exp1.accept(this);
    hasProp.exp2.accept(this);
  }

  @Override
  public void visit(HasLabel hasLabel) {
    hasLabel.exp1.accept(this);
    hasLabel.exp2.accept(this);
  }

  @Override
  public void visit(VertexLabels vertexLabels) {
    vertexLabels.exp.accept(this);
  }

  @Override
  public void visit(InDegree inDegree) {
    inDegree.exp.accept(this);
  }

  @Override
  public void visit(OutDegree outDegree) {
    outDegree.exp.accept(this);
  }

  @Override
  public void visit(EdgeLabel edgeLabel) {
    edgeLabel.exp.accept(this);
  }
}