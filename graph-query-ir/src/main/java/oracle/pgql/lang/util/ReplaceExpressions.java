/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.DerivedTable;
import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.QueryExpression.SourceDestinationPredicate;
import oracle.pgql.lang.ir.modify.DeleteClause;
import oracle.pgql.lang.ir.modify.EdgeInsertion;
import oracle.pgql.lang.ir.modify.InsertClause;
import oracle.pgql.lang.ir.modify.ModifyQuery;
import oracle.pgql.lang.ir.modify.SetPropertyExpression;
import oracle.pgql.lang.ir.modify.Update;
import oracle.pgql.lang.ir.modify.UpdateClause;
import oracle.pgql.lang.ir.modify.VertexInsertion;

/**
 * A visitor that replaces some expressions with another expression. This visitor is only intended to operate on
 * {@code SELECT} clauses. Implementations of this class need to provide implementations for two functions: The first
 * function is used to identify expressions that are to be replaced. The second function is used to construct the
 * replacements for those expressions.
 */
public abstract class ReplaceExpressions implements QueryExpressionVisitor {

  // API

  /**
   * Check if an expression should be replaced by this operator.
   *
   * @param expression
   *          A query expression.
   * @return {@code true}, iff this expression should be replaced.
   */
  protected abstract boolean matches(QueryExpression expression);

  /**
   * Construct a replacement for a given expression. Only matching expressions, i.e., those where
   * {@link #matches(QueryExpression)} returns {@code true}, are passed to this function for replacement.
   *
   * @param expression
   *          The expression that is to be replaced in the query.
   * @return A new expression that is used as the replacement.
   */
  public abstract QueryExpression replace(QueryExpression expression);

  private QueryExpression replaceMatching(QueryExpression expression) {
    if (expression == null) {
      return null;
    } else if (matches(expression)) {
      return replace(expression);
    } else {
      expression.accept(this);
      return expression;
    }
  }

  // Unary expressions

  @Override
  public void visit(QueryExpression.ArithmeticExpression.UMin uMin) {
    replaceInUnaryExpression(uMin);
  }

  @Override
  public void visit(QueryExpression.LogicalExpression.Not not) {
    replaceInUnaryExpression(not);
  }

  // Binary expressions

  @Override
  public void visit(QueryExpression.ArithmeticExpression.Sub sub) {
    replaceInBinaryExpression(sub);
  }

  @Override
  public void visit(QueryExpression.ArithmeticExpression.Add add) {
    replaceInBinaryExpression(add);
  }

  @Override
  public void visit(QueryExpression.ArithmeticExpression.Mul mul) {
    replaceInBinaryExpression(mul);
  }

  @Override
  public void visit(QueryExpression.ArithmeticExpression.Div div) {
    replaceInBinaryExpression(div);
  }

  @Override
  public void visit(QueryExpression.ArithmeticExpression.Mod mod) {
    replaceInBinaryExpression(mod);
  }

  @Override
  public void visit(QueryExpression.LogicalExpression.And and) {
    replaceInBinaryExpression(and);
  }

  @Override
  public void visit(QueryExpression.LogicalExpression.Or or) {
    replaceInBinaryExpression(or);
  }

  @Override
  public void visit(QueryExpression.RelationalExpression.Equal equal) {
    replaceInBinaryExpression(equal);
  }

  @Override
  public void visit(QueryExpression.RelationalExpression.NotEqual notEqual) {
    replaceInBinaryExpression(notEqual);
  }

  @Override
  public void visit(QueryExpression.RelationalExpression.Greater greater) {
    replaceInBinaryExpression(greater);
  }

  @Override
  public void visit(QueryExpression.RelationalExpression.GreaterEqual greaterEqual) {
    replaceInBinaryExpression(greaterEqual);
  }

  @Override
  public void visit(QueryExpression.RelationalExpression.Less less) {
    replaceInBinaryExpression(less);
  }

  @Override
  public void visit(QueryExpression.RelationalExpression.LessEqual lessEqual) {
    replaceInBinaryExpression(lessEqual);
  }

  @Override
  public void visit(QueryExpression.ConcatExpression concat) {
    replaceInBinaryExpression(concat);
  }

  // Aggregations

  @Override
  public void visit(QueryExpression.Aggregation.AggrCount aggrCount) {
    replaceInUnaryExpression(aggrCount);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrListagg aggrListagg) {
    replaceInUnaryExpression(aggrListagg);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrMin aggrMin) {
    replaceInUnaryExpression(aggrMin);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrMax aggrMax) {
    replaceInUnaryExpression(aggrMax);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrSum aggrSum) {
    replaceInUnaryExpression(aggrSum);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrAvg aggrAvg) {
    replaceInUnaryExpression(aggrAvg);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrArrayAgg aggrArrayAgg) {
    replaceInUnaryExpression(aggrArrayAgg);
  }

  @Override
  public void visit(QueryExpression.Aggregation.AggrJsonArrayagg aggrJsonArrayagg) {
    replaceInUnaryExpression(aggrJsonArrayagg);
  }

  // Other expressions

  @Override
  public void visit(QueryExpression.Function.Cast cast) {
    cast.setExp(replaceMatching(cast.getExp()));
  }

  @Override
  public void visit(QueryExpression.FunctionCall functionCall) {
    functionCall.getArgs().replaceAll(this::replaceMatching);
  }

  @Override
  public void visit(QueryExpression.ExtractExpression extractExpression) {
    extractExpression.setExp(replaceMatching(extractExpression.getExp()));
  }

  @Override
  public void visit(QueryExpression.InPredicate inPredicate) {
    inPredicate.setExp(replaceMatching(inPredicate.getExp()));
    inPredicate.setInValueList(replaceMatching(inPredicate.getInValueList()));
  }

  @Override
  public void visit(QueryExpression.IsNull isNull) {
    isNull.setExp(replaceMatching(isNull.getExp()));
  }

  @Override
  public void visit(QueryExpression.IfElse ifElse) {
    ifElse.setExp1(replaceMatching(ifElse.getExp1()));
    ifElse.setExp2(replaceMatching(ifElse.getExp2()));
    ifElse.setExp3(replaceMatching(ifElse.getExp3()));
  }

  @Override
  public void visit(QueryExpression.SimpleCase simpleCase) {
    simpleCase.setCaseOperand(replaceMatching(simpleCase.getCaseOperand()));
    simpleCase.setElseExp(replaceMatching(simpleCase.getElseExp()));
    for (QueryExpression.WhenThenExpression whenThenExp : simpleCase.getWhenThenExps()) {
      whenThenExp.setWhen(replaceMatching(whenThenExp.getWhen()));
      whenThenExp.setThen(replaceMatching(whenThenExp.getThen()));
    }
  }

  @Override
  public void visit(QueryExpression.SubstringExpression substringExpression) {
    substringExpression.setExp(replaceMatching(substringExpression.getExp()));
    substringExpression.setStartPosition(replaceMatching(substringExpression.getStartPosition()));
    substringExpression.setStringLength(replaceMatching(substringExpression.getStringLength()));
  }

  @Override
  public void visit(QueryExpression.BetweenPredicate betweenPredicate) {
    betweenPredicate.setExp1(replaceMatching(betweenPredicate.getExp1()));
    betweenPredicate.setExp2(replaceMatching(betweenPredicate.getExp2()));
    betweenPredicate.setExp3(replaceMatching(betweenPredicate.getExp3()));
  }

  @Override
  public void visit(SourceDestinationPredicate sourceDestinationPredicate) {
  }

  @Override
  public void visit(Projection projection) {
    projection.getElements().forEach(this::visit);
  }

  // General replacement functions

  private void replaceInUnaryExpression(QueryExpression.UnaryExpression unaryExpression) {
    unaryExpression.setExp(replaceMatching(unaryExpression.getExp()));
  }

  private void replaceInBinaryExpression(QueryExpression.BinaryExpression binaryExpression) {
    binaryExpression.setExp1(replaceMatching(binaryExpression.getExp1()));
    binaryExpression.setExp2(replaceMatching(binaryExpression.getExp2()));
  }

  private Set<QueryExpression> replaceInSet(Set<QueryExpression> expressions) {
    return expressions.stream() //
        .map(this::replaceMatching) //
        .collect(Collectors.toSet());
  }

  private List<QueryExpression> replaceInList(List<QueryExpression> expressions) {
    return expressions.stream() //
        .map(this::replaceMatching) //
        .collect(Collectors.toList());
  }

  // Other expression types

  @Override
  public void visit(QueryExpression.Constant.ConstInteger constInteger) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstDecimal constDecimal) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstString constString) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstBoolean constBoolean) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstDate constDate) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstTime constTime) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstTimestamp constTimestamp) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstTimeWithTimezone constTimeWithTimezone) {
  }

  @Override
  public void visit(QueryExpression.Constant.ConstTimestampWithTimezone constTimestampWithTimezone) {
  }

  @Override
  public void visit(QueryExpression.Interval interval) {
  }

  @Override
  public void visit(QueryExpression.VarRef varRef) {
  }

  @Override
  public void visit(QueryExpression.BindVariable bindVariable) {
  }

  @Override
  public void visit(QueryExpression.Star star) {
  }

  @Override
  public void visit(QueryExpression.AllProperties allProperties) {
    visit(allProperties.getVarRef());
  }

  @Override
  public void visit(QueryExpression.PropertyAccess propertyAccess) {
  }

  @Override
  public void visit(QueryExpression.Function.Exists exists) {
    // SELECT sub-queries are not QueryExpressions, just continue
    visit(exists.getQuery());
  }

  @Override
  public void visit(QueryExpression.InPredicate.InValueList inValueList) {
  }

  @Override
  public void visit(QueryExpression.ScalarSubquery scalarSubquery) {
    visit(scalarSubquery.getQuery());
  }

  @Override
  public void visit(SelectQuery selectQuery) {
    visit(selectQuery.getProjection());
    visitQuery(selectQuery);
  }

  public void visitQuery(GraphQuery graphQuery) {
    graphQuery.getTableExpressions().forEach(e -> e.accept(this));
    if (graphQuery.getGroupBy() != null) {
      visit(graphQuery.getGroupBy());
    }
    if (graphQuery.getHaving() != null) {
      graphQuery.setHaving(replaceMatching(graphQuery.getHaving()));
    }
    visit(graphQuery.getOrderBy());
    graphQuery.setLimit(replaceMatching(graphQuery.getLimit()));
    graphQuery.setOffset(replaceMatching(graphQuery.getOffset()));
  }

  @Override
  public void visit(GraphPattern graphPattern) {
    graphPattern.setConstraints(replaceInSet(graphPattern.getConstraints()));
  }

  @Override
  public void visit(ExpAsVar expAsVar) {
    expAsVar.setExp(replaceMatching(expAsVar.getExp()));
  }

  @Override
  public void visit(QueryVertex queryVertex) {
  }

  @Override
  public void visit(QueryEdge queryEdge) {
  }

  @Override
  public void visit(QueryPath queryPath) {
    queryPath.setConstraints(replaceInSet(queryPath.getConstraints()));
  }

  @Override
  public void visit(GroupBy groupBy) {
    groupBy.getElements().forEach(this::visit);
  }

  @Override
  public void visit(OrderBy orderBy) {
    orderBy.getElements().forEach(this::visit);
  }

  @Override
  public void visit(OrderByElem orderByElem) {
    orderByElem.setExp(replaceMatching(orderByElem.getExp()));
  }

  @Override
  public void visit(DerivedTable derivedTable) {
    visit(derivedTable.getQuery());
  }

  @Override
  public void visit(ModifyQuery modifyQuery) {
    modifyQuery.getModifications().forEach(modification -> modification.accept(this));
    visitQuery(modifyQuery);
  }

  @Override
  public void visit(SetPropertyExpression setPropertyExpression) {
    setPropertyExpression.setValueExpression(replaceMatching(setPropertyExpression.getValueExpression()));
  }

  @Override
  public void visit(InsertClause insertClause) {
    insertClause.getInsertions().forEach(insertion -> insertion.accept(this));
  }

  @Override
  public void visit(UpdateClause updateClause) {
    updateClause.getUpdates().forEach(update -> update.accept(this));
  }

  @Override
  public void visit(DeleteClause deleteClause) {
  }

  @Override
  public void visit(VertexInsertion vertexInsertion) {
    vertexInsertion.setLabels(replaceInList(vertexInsertion.getLabels()));
    vertexInsertion.getProperties()
        .forEach(property -> property.setValueExpression(replaceMatching(property.getValueExpression())));
  }

  @Override
  public void visit(EdgeInsertion edgeInsertion) {
    edgeInsertion.setLabels(replaceInList(edgeInsertion.getLabels()));
    edgeInsertion.getProperties()
        .forEach(property -> property.setValueExpression(replaceMatching(property.getValueExpression())));
  }

  @Override
  public void visit(Update update) {
    update.getSetPropertyExpressions().forEach(propertyExpression -> propertyExpression
        .setValueExpression(replaceMatching(propertyExpression.getValueExpression())));
  }
}
