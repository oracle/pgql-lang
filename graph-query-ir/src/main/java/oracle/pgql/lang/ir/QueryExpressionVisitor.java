/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrAvg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrCount;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrJsonArrayagg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrListagg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMax;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMin;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrSum;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrArrayAgg;
import oracle.pgql.lang.ir.QueryExpression.AllProperties;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Add;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Div;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mod;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mul;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Sub;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.UMin;
import oracle.pgql.lang.ir.QueryExpression.BetweenPredicate;
import oracle.pgql.lang.ir.QueryExpression.BindVariable;
import oracle.pgql.lang.ir.QueryExpression.ConcatExpression;
import oracle.pgql.lang.ir.QueryExpressionVisitor;
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
import oracle.pgql.lang.ir.QueryExpression.Interval;
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
import oracle.pgql.lang.ir.QueryExpression.SubstringExpression;
import oracle.pgql.lang.ir.modify.ModifyQuery;
import oracle.pgql.lang.ir.modify.SetPropertyExpression;
import oracle.pgql.lang.ir.modify.Update;
import oracle.pgql.lang.ir.modify.UpdateClause;
import oracle.pgql.lang.ir.modify.VertexInsertion;
import oracle.pgql.lang.ir.modify.DeleteClause;
import oracle.pgql.lang.ir.modify.EdgeInsertion;
import oracle.pgql.lang.ir.modify.InsertClause;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.QueryExpression.SimpleCase;
import oracle.pgql.lang.ir.QueryExpression.Star;
import oracle.pgql.lang.ir.QueryExpression.VarRef;

public interface QueryExpressionVisitor {

  public void visit(ConstInteger constInteger);

  public void visit(ConstDecimal constDecimal);

  public void visit(ConstString constString);

  public void visit(ConstBoolean constBoolean);

  public void visit(ConstDate constDate);

  public void visit(ConstTime constTime);

  public void visit(ConstTimestamp constTimestamp);

  public void visit(ConstTimeWithTimezone constTimeWithTimezone);

  public void visit(ConstTimestampWithTimezone constTimestampWithTimezone);

  public void visit(Interval interval);

  public void visit(Sub sub);

  public void visit(Add add);

  public void visit(Mul mul);

  public void visit(Div div);

  public void visit(Mod mod);

  public void visit(UMin uMin);

  public void visit(And and);

  public void visit(Or or);

  public void visit(Not not);

  public void visit(Equal equal);

  public void visit(NotEqual notEqual);

  public void visit(Greater greater);

  public void visit(GreaterEqual greaterEqual);

  public void visit(Less less);

  public void visit(LessEqual lessEqual);

  public void visit(ConcatExpression concat);

  public void visit(AggrCount aggrCount);

  public void visit(AggrListagg aggrListagg);

  public void visit(AggrMin aggrMin);

  public void visit(AggrMax aggrMax);

  public void visit(AggrSum aggrSum);

  public void visit(AggrAvg aggrAvg);

  public void visit(AggrArrayAgg aggrArrayAgg);

  public void visit(AggrJsonArrayagg aggrJsonArrayagg);

  public void visit(VarRef varRef);

  public void visit(BindVariable bindVariable);

  public void visit(Star star);

  public void visit(AllProperties allProperties);

  public void visit(PropertyAccess propAccess);

  public void visit(Cast cast);

  public void visit(Exists exists);

  public void visit(FunctionCall functionCall);

  public void visit(ExtractExpression extractExpression);

  public void visit(InPredicate inPredicate);

  public void visit(InValueList inValueList);

  public void visit(IsNull isNull);

  public void visit(IfElse ifElse);

  public void visit(SimpleCase simpleCase);

  public void visit(SubstringExpression substringExpression);

  public void visit(BetweenPredicate betweenPredicate);

  public void visit(ScalarSubquery sclarSubquery);

  public void visit(SelectQuery selectQuery);

  public void visit(GraphPattern graphPattern);

  public void visit(Projection projection);

  public void visit(ExpAsVar expAsVar);

  public void visit(QueryVertex queryVertex);

  public void visit(QueryEdge queryEdge);

  public void visit(QueryPath queryPath);

  public void visit(GroupBy groupBy);

  public void visit(OrderBy orderBy);

  public void visit(OrderByElem orderByElem);

  public void visit(ModifyQuery modifyQuery);

  public void visit(InsertClause insertClause);

  public void visit(UpdateClause updateClause);

  public void visit(DeleteClause deleteClause);

  public void visit(VertexInsertion vertexInsertion);

  public void visit(EdgeInsertion edgeInsertion);

  public void visit(Update update);

  public void visit(SetPropertyExpression setPropertyExpression);

  public void visit(DerivedTable derivedTable);
}
