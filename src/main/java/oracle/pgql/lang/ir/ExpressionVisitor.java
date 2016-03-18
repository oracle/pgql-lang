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

public abstract class ExpressionVisitor {

  protected final void visit(QueryExpression queryExpression) {
    switch (queryExpression.getExpType()) {
      case ConstInteger:
        visit ((ConstInteger) queryExpression);
        break;
      case ConstDecimal:
        visit ((ConstDecimal) queryExpression);
        break;
      case ConstString:
        visit ((ConstString) queryExpression);
        break;
      case ConstBoolean:
        visit ((ConstBoolean) queryExpression);
        break;
      case ConstNull:
        visit ((ConstNull) queryExpression);
        break;
      case Sub:
        visit ((Sub) queryExpression);
        break;
      case Add:
        visit ((Add) queryExpression);
        break;
      case Mul:
        visit ((Mul) queryExpression);
        break;
      case Div:
        visit ((Div) queryExpression);
        break;
      case Mod:
        visit ((Mod) queryExpression);
        break;
      case UMin:
        visit ((UMin) queryExpression);
        break;
      case And:
        visit ((And) queryExpression);
        break;
      case Or:
        visit ((Or) queryExpression);
        break;
      case Not:
        visit ((Not) queryExpression);
        break;
      case Equal:
        visit ((Equal) queryExpression);
        break;
      case NotEqual:
        visit ((NotEqual) queryExpression);
        break;
      case Greater:
        visit ((Greater) queryExpression);
        break;
      case GreaterEqual:
        visit ((GreaterEqual) queryExpression);
        break;
      case Less:
        visit ((Less) queryExpression);
        break;
      case LessEqual:
        visit ((LessEqual) queryExpression);
        break;
      case Aggr_count:
        visit ((AggrCount) queryExpression);
        break;
      case Aggr_min:
        visit ((AggrMin) queryExpression);
        break;
      case Aggr_max:
        visit ((AggrMax) queryExpression);
        break;
      case Aggr_sum:
        visit ((AggrSum) queryExpression);
        break;
      case Aggr_avg:
        visit ((AggrAvg) queryExpression);
        break;
      case VarRef:
        visit ((VarRef) queryExpression);
        break;
      case Star:
        visit ((Star) queryExpression);
        break;
      case Regex:
        visit ((Regex) queryExpression);
        break;
      case Id:
        visit ((Id) queryExpression);
        break;
      case PropAccess:
        visit ((PropAccess) queryExpression);
        break;
      case HasProp:
        visit ((HasProp) queryExpression);
        break;
      case HasLabel:
        visit ((HasLabel) queryExpression);
        break;
      case VertexLabels:
        visit ((VertexLabels) queryExpression);
        break;
      case InDegree:
        visit ((InDegree) queryExpression);
        break;
      case OutDegree:
        visit ((OutDegree) queryExpression);
        break;
      case EdgeLabel:
        visit ((EdgeLabel) queryExpression);
        break;
      default:
        throw new UnsupportedOperationException("Expression of type " + queryExpression.getExpType()
          + " is not supported");
    }
  }

  public void visit(ConstInteger constInteger) { }

  public void visit(ConstDecimal constDecimal) { }

  public void visit(ConstString constString) { }

  public void visit(ConstBoolean constBoolean) { }

  public void visit(ConstNull constantNull) { }

  public void visit(Sub sub) { }

  public void visit(Add add) { }

  public void visit(Mul mul) { }

  public void visit(Div div) { }

  public void visit(Mod mod) { }

  public void visit(UMin uMin) { }

  public void visit(And and) { }

  public void visit(Or or) { }

  public void visit(Not not) { }

  public void visit(Equal equal) { }

  public void visit(NotEqual notEqual) { }

  public void visit(Greater greater) { }

  public void visit(GreaterEqual greaterEqual) { }

  public void visit(Less less) { }

  public void visit(LessEqual lessEqual) { }

  public void visit(AggrCount aggrCount) { }

  public void visit(AggrMin aggrMin) { }

  public void visit(AggrMax aggrMax) { }

  public void visit(AggrSum aggrSum) { }

  public void visit(AggrAvg aggrAvg) { }

  public void visit(VarRef varRef) { }

  public void visit(Star star) { }

  public void visit(Regex regex) { }

  public void visit(Id id) { }

  public void visit(PropAccess propAccess) { }

  public void visit(HasProp hasProp) { }

  public void visit(HasLabel hasLabel) { }

  public void visit(VertexLabels vertexLabels) { }

  public void visit(InDegree inDegree) { }

  public void visit(OutDegree outDegree) { }

  public void visit(EdgeLabel edgeLabel) { }
}