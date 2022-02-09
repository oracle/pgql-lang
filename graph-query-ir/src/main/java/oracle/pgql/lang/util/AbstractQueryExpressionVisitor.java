/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
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
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrListagg;
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
import oracle.pgql.lang.ir.QueryExpression.BetweenPredicate;
import oracle.pgql.lang.ir.QueryExpression.BindVariable;
import oracle.pgql.lang.ir.QueryExpression.ConcatExpression;
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
import oracle.pgql.lang.ir.QueryExpression.IfElse;
import oracle.pgql.lang.ir.QueryExpression.InPredicate;
import oracle.pgql.lang.ir.QueryExpression.InPredicate.InValueList;
import oracle.pgql.lang.ir.QueryExpression.IsNull;
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
import oracle.pgql.lang.ir.QueryExpression.SimpleCase;
import oracle.pgql.lang.ir.QueryExpression.SubstringExpression;
import oracle.pgql.lang.ir.modify.DeleteClause;
import oracle.pgql.lang.ir.modify.EdgeInsertion;
import oracle.pgql.lang.ir.modify.InsertClause;
import oracle.pgql.lang.ir.modify.ModifyQuery;
import oracle.pgql.lang.ir.modify.SetPropertyExpression;
import oracle.pgql.lang.ir.modify.Update;
import oracle.pgql.lang.ir.modify.UpdateClause;
import oracle.pgql.lang.ir.modify.VertexInsertion;
import oracle.pgql.lang.ir.unnest.OneRowPerEdge;
import oracle.pgql.lang.ir.unnest.OneRowPerStep;
import oracle.pgql.lang.ir.unnest.OneRowPerVertex;
import oracle.pgql.lang.ir.unnest.RowsPerMatch;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.QueryExpression.Star;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.SelectQuery;

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
  public void visit(ConcatExpression concat) {
    concat.getExp1().accept(this);
    concat.getExp2().accept(this);
  }

  @Override
  public void visit(AggrCount aggrCount) {
    aggrCount.getExp().accept(this);
  }

  @Override
  public void visit(AggrListagg aggrListagg) {
    aggrListagg.getExp().accept(this);
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
  public void visit(IsNull isNull) {
    isNull.getExp().accept(this);
  }

  @Override
  public void visit(IfElse ifElse) {
    ifElse.getExp1().accept(this);
    ifElse.getExp2().accept(this);
    if (ifElse.getExp3() != null) {
      ifElse.getExp3().accept(this);
    }
  }

  @Override
  public void visit(SimpleCase simpleCase) {
    simpleCase.getCaseOperand().accept(this);
    simpleCase.getWhenThenExps().forEach(whenThen -> {
      whenThen.getWhen().accept(this);
      whenThen.getThen().accept(this);
    });
    if (simpleCase.getElseExp() != null) {
      simpleCase.getElseExp().accept(this);
    }
  }

  @Override
  public void visit(SubstringExpression substringExpression) {
    substringExpression.getExp().accept(this);
    substringExpression.getStartPosition().accept(this);
    if (substringExpression.getStringLength() != null) {
      substringExpression.getStringLength().accept(this);
    }
  }

  @Override
  public void visit(BetweenPredicate betweenPredicate) {
    betweenPredicate.getExp1().accept(this);
    betweenPredicate.getExp2().accept(this);
    betweenPredicate.getExp3().accept(this);
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
  public void visit(SelectQuery selectQuery) {
    selectQuery.getProjection().accept(this);
    visitQuery(selectQuery);
  }

  private void visitQuery(GraphQuery query) {
    if (query.getGraphPattern() != null) {
      query.getGraphPattern().accept(this);
    }
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
    RowsPerMatch rowsPerMatch = queryPath.getRowsPerMatch();
    switch (queryPath.getRowsPerMatch().getRowsPerMatchType()) {
      case ONE_ROW_PER_VERTEX:
        ((OneRowPerVertex) rowsPerMatch).getVertex().accept(this);
        break;
      case ONE_ROW_PER_EDGE:
        ((OneRowPerEdge) rowsPerMatch).getEdge().accept(this);
        break;
      case ONE_ROW_PER_MATCH:
        break;
      case ONE_ROW_PER_STEP:
        OneRowPerStep oneRowPerStep = (OneRowPerStep) rowsPerMatch;
        oneRowPerStep.getVertex1().accept(this);
        oneRowPerStep.getEdge().accept(this);
        oneRowPerStep.getVertex2().accept(this);
        break;
      default:
        throw new UnsupportedOperationException(rowsPerMatch.getRowsPerMatchType() + " not supported");
    }
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

  @Override
  public void visit(ModifyQuery modifyQuery) {
    modifyQuery.getModifications().stream().forEach(modification -> modification.accept(this));
    visitQuery(modifyQuery);
  }

  @Override
  public void visit(InsertClause insertClause) {
    insertClause.getInsertions().stream().forEach(element -> element.accept(this));
  }

  @Override
  public void visit(UpdateClause updateClause) {
    updateClause.getUpdates().stream().forEach(element -> element.accept(this));
  }

  @Override
  public void visit(DeleteClause deleteClause) {
    deleteClause.getDeletions().stream().forEach(element -> element.accept(this));
  }

  @Override
  public void visit(VertexInsertion vertexInsertion) {
    vertexInsertion.getVertex().accept(this);
    vertexInsertion.getLabels().stream().forEach(label -> label.accept(this));
    vertexInsertion.getProperties().stream().forEach(setProperty -> setProperty.accept(this));
  }

  @Override
  public void visit(EdgeInsertion edgeInsertion) {
    edgeInsertion.getEdge().accept(this);
    edgeInsertion.getLabels().stream().forEach(label -> label.accept(this));
    edgeInsertion.getProperties().stream().forEach(setProperty -> setProperty.accept(this));
  }

  @Override
  public void visit(Update update) {
    update.getElement().accept(this);
    update.getSetPropertyExpressions().stream().forEach(setPropertyExpression -> setPropertyExpression.accept(this));
  }

  @Override
  public void visit(SetPropertyExpression setPropertyExpression) {
    setPropertyExpression.getPropertyAccess().accept(this);
    setPropertyExpression.getValueExpression().accept(this);
  }
}
