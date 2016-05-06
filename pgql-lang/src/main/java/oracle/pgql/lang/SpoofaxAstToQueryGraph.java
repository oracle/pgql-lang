/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.QueryConnection;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryGraph;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.QueryVar;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpoofaxAstToQueryGraph {

  private static final int POS_SELECT = 3;
  private static final int POS_PROJECTION = 1;

  private static final int POS_WHERE = 1;
  private static final int POS_NODES = 0;
  private static final int POS_EDGES = 1;
  private static final int POS_EDGE_NAME = 1;
  private static final int POS_EDGE_SRC = 0;
  private static final int POS_EDGE_DST = 2;
  private static final int POS_EDGE_CONSTRAINTS = 3;
  private static final int POS_PATHLENGTH_MIN = 0;
  private static final int POS_PATHLENGTH_MAX = 1;
  private static final int POS_CONSTRAINTS = 2;

  private static final int POS_GROUPBY = 2;
  private static final int POS_ORDERBY = 4;
  private static final int POS_ORDERBY_EXP = 0;
  private static final int POS_ORDERBY_ORDERING = 1;
  private static final int POS_LIMITOFFSET = 5;
  private static final int POS_LIMIT = 0;
  private static final int POS_OFFSET = 1;

  private static final int POS_EXPASVAR_EXP = 1;
  private static final int POS_EXPASVAR_VAR = 0;
  private static final int POS_BINARY_EXP_LEFT = 0;
  private static final int POS_BINARY_EXP_RIGHT = 1;
  private static final int POS_UNARY_EXP = 0;
  private static final int POS_AGGREGATE_EXP = 1; // FIXME: do we support e.g. AVG(DISTINCT x) ? (parser does)
  private static final int POS_PROPREF_VARNAME = 0;
  private static final int POS_PROPREF_PROPNAME = 1;

  public static QueryGraph translate(IStrategoTerm ast) throws PgqlException {

    // WHERE
    IStrategoTerm graphPatternT = ast.getSubterm(POS_WHERE);

    // nodes
    IStrategoTerm nodesT = getList(graphPatternT.getSubterm(POS_NODES));
    Map<String, QueryVar> varmap = new HashMap<>(); // map from variable name to variable
    List<QueryVertex> nodes = getQueryVertexs(nodesT, varmap);

    // edges
    IStrategoTerm edgesT = getList(graphPatternT.getSubterm(POS_EDGES));
    List<QueryConnection> connections = getQueryConnections(edgesT, varmap);

    // constraints
    IStrategoTerm constraintsT = getList(graphPatternT.getSubterm(POS_CONSTRAINTS));
    List<QueryExpression> constraints = getQueryExpressions(constraintsT, varmap);

    IStrategoTerm selectT = ast.getSubterm(POS_SELECT);

    // GROUP BY
    IStrategoTerm groupByT = selectT.getSubterm(POS_GROUPBY);
    List<ExpAsVar> groupByElems = getGroupByElems(varmap, groupByT);

    // SELECT
    IStrategoTerm projectionT = selectT.getSubterm(POS_PROJECTION);
    List<ExpAsVar> selectElems = getSelectElems(varmap, projectionT);

    // ORDER BY
    IStrategoTerm orderByT = selectT.getSubterm(POS_ORDERBY);
    List<OrderByElem> orderByElems = getOrderByElems(varmap, orderByT);

    // LIMIT OFFSET
    IStrategoTerm limitOffsetT = selectT.getSubterm(POS_LIMITOFFSET);
    long limit = getLimitOrOffset(limitOffsetT.getSubterm(POS_LIMIT));
    long offset = getLimitOrOffset(limitOffsetT.getSubterm(POS_OFFSET));

    return new QueryGraph(selectElems, nodes, connections, constraints, groupByElems, orderByElems, limit, offset);
  }

  private static List<QueryVertex> getQueryVertexs(IStrategoTerm nodesT, Map<String, QueryVar> varmap) {
    List<QueryVertex> nodes = new ArrayList<>(nodesT.getSubtermCount());
    for (IStrategoTerm nodeT : nodesT) {
      String nodeName = getString(nodeT);
      QueryVertex node = new QueryVertex(nodeName);
      nodes.add(node);
      varmap.put(nodeName, node);
    }
    return nodes;
  }

  private static List<QueryExpression> getQueryExpressions(IStrategoTerm constraintsT, Map<String, QueryVar> varmap)
      throws PgqlException {
    List<QueryExpression> constraints = new ArrayList<>(constraintsT.getSubtermCount());
    for (IStrategoTerm constraintT : constraintsT) {
      QueryExpression exp = translateExp(constraintT, varmap);
      constraints.add(exp);
    }
    return constraints;
  }

  private static List<QueryConnection> getQueryConnections(IStrategoTerm edgesT, Map<String, QueryVar> varmap) {
    List<QueryConnection> connections = new ArrayList<>(edgesT.getSubtermCount());
    for (IStrategoTerm edgeT : edgesT) {
      String name = getString(edgeT.getSubterm(POS_EDGE_NAME));
      String srcName = getString(edgeT.getSubterm(POS_EDGE_SRC));
      String dstName = getString(edgeT.getSubterm(POS_EDGE_DST));

      QueryVertex src = (QueryVertex) varmap.get(srcName);
      QueryVertex dst = (QueryVertex) varmap.get(dstName);

      QueryConnection conn = null;
      IStrategoTerm edgeConstraints = edgeT.getSubterm(POS_EDGE_CONSTRAINTS);
      if (isNone(edgeConstraints)) {
        conn = new QueryConnection.QueryEdge(name, src, dst);
      } else {
        edgeConstraints = getSome(edgeConstraints);

        if (((IStrategoAppl) edgeConstraints).getName().equals("Shortest")) {
          conn = new QueryConnection.QueryPath.ShortestQueryPath(name, src, dst, false);
        } else {

          IStrategoTerm minLengthT = edgeConstraints.getSubterm(POS_PATHLENGTH_MIN);
          long minLength = -1;
          if (!isNone(minLengthT)) {
            minLength = Long.parseLong(getString(minLengthT));
          }

          IStrategoTerm maxLengthT = edgeConstraints.getSubterm(POS_PATHLENGTH_MAX);
          long maxLength = -1;
          if (!isNone(maxLengthT)) {
            maxLength = Long.parseLong(getString(maxLengthT));
          }

          conn = new QueryConnection.QueryPath.SimpleQueryPath(name, src, dst, minLength, maxLength);
        }
      }

      connections.add(conn);
      varmap.put(name, conn);
    }
    return connections;
  }

  private static List<ExpAsVar> getGroupByElems(Map<String, QueryVar> varmap, IStrategoTerm groupByT)
      throws PgqlException {
    if (!isNone(groupByT)) { // has GROUP BY
      IStrategoTerm groupByElemsT = getList(groupByT);
      return getExpAsVars(varmap, groupByElemsT);
    }
    return new ArrayList<>();
  }

  private static List<ExpAsVar> getSelectElems(Map<String, QueryVar> varmap, IStrategoTerm projectionT)
      throws PgqlException {
    return getExpAsVars(varmap, getList(projectionT));
  }

  private static List<ExpAsVar> getExpAsVars(Map<String, QueryVar> varmap, IStrategoTerm expAsVarsT)
      throws PgqlException {
    List<ExpAsVar> expAsVars = new ArrayList<>(expAsVarsT.getSubtermCount());
    for (IStrategoTerm expAsVarT : expAsVarsT) {
      String varName = getString(expAsVarT.getSubterm(POS_EXPASVAR_VAR));
      QueryExpression exp = translateExp(expAsVarT.getSubterm(POS_EXPASVAR_EXP), varmap);

      ExpAsVar expAsVar = new ExpAsVar(exp, varName);
      expAsVars.add(expAsVar);
      varmap.put(varName, expAsVar);
    }
    return expAsVars;
  }

  private static List<OrderByElem> getOrderByElems(Map<String, QueryVar> varmap, IStrategoTerm orderByT)
      throws PgqlException {
    List<OrderByElem> orderByElems = new ArrayList<>();
    if (!isNone(orderByT)) { // has ORDER BY
      IStrategoTerm orderByElemsT = getList(orderByT);
      for (IStrategoTerm orderByElemT : orderByElemsT) {
        QueryExpression exp = translateExp(orderByElemT.getSubterm(POS_ORDERBY_EXP), varmap);
        boolean ascending = ((IStrategoAppl) orderByElemT.getSubterm(POS_ORDERBY_ORDERING)).getConstructor().getName()
            .equals("Asc");
        orderByElems.add(new OrderByElem(exp, ascending));
      }
    }
    return orderByElems;
  }

  private static long getLimitOrOffset(IStrategoTerm getLimitOrOffsetT) {
    long offset = -1;
    if (!isNone(getLimitOrOffsetT)) {
      offset = Long.parseLong(getString(getLimitOrOffsetT));
    }
    return offset;
  }

  private static QueryExpression translateExp(IStrategoTerm t, Map<String, QueryVar> varmap) throws PgqlException {
    String cons = ((IStrategoAppl) t).getConstructor().getName();

    switch (cons) {
      case "Sub":
        QueryExpression exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        QueryExpression exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.ArithmeticExpression.Sub(exp1, exp2);
      case "Add":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.ArithmeticExpression.Add(exp1, exp2);
      case "Mul":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.ArithmeticExpression.Mul(exp1, exp2);
      case "Div":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.ArithmeticExpression.Div(exp1, exp2);
      case "Mod":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.ArithmeticExpression.Mod(exp1, exp2);
      case "UMin":
        QueryExpression exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.ArithmeticExpression.UMin(exp);
      case "And":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.LogicalExpression.And(exp1, exp2);
      case "Or":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.LogicalExpression.Or(exp1, exp2);
      case "Not":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.LogicalExpression.Not(exp);
      case "Eq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.RelationalExpression.Equal(exp1, exp2);
      case "Neq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.RelationalExpression.NotEqual(exp1, exp2);
      case "Gt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.RelationalExpression.Greater(exp1, exp2);
      case "Gte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.RelationalExpression.GreaterEqual(exp1, exp2);
      case "Lt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.RelationalExpression.Less(exp1, exp2);
      case "Lte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.RelationalExpression.LessEqual(exp1, exp2);
      case "Integer":
        try {
          long l = Long.parseLong(getString(t));
          return new QueryExpression.Constant.ConstInteger(l);
        } catch (NumberFormatException e) {
          throw new PgqlException(getString(t) + " is too large to be represented as Long");
        }
      case "Decimal":
        double d = Double.parseDouble(getString(t));
        return new QueryExpression.Constant.ConstDecimal(d);
      case "String":
        String s = getString(t);
        return new QueryExpression.Constant.ConstString(s);
      case "True":
        return new QueryExpression.Constant.ConstBoolean(true);
      case "False":
        return new QueryExpression.Constant.ConstBoolean(false);
      case "Null":
        return new QueryExpression.Constant.ConstNull();
      case "VarRef":
      case "GroupRef":
      case "SelectOrGroupRef":
        String varName = getString(t);
        QueryVar var = varmap.get(varName);
        if (var == null) {
          throw new PgqlException("Variable " + varName + " undefined");
        }
        return new QueryExpression.VarRef(var);
      case "PropRef":
        varName = getString(t.getSubterm(POS_PROPREF_VARNAME));
        var = varmap.get(varName);
        if (var == null) {
          throw new PgqlException("Variable " + varName + " undefined");
        }
        String propname = getString(t.getSubterm(POS_PROPREF_PROPNAME));
        return new QueryExpression.PropAccess(var, propname);
      case "Regex":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.Function.Regex(exp1, exp2);
      case "Label":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.Function.EdgeLabel(exp);
      case "Labels":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.Function.VertexLabels(exp);
      case "Id":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.Function.Id(exp);
      case "Has":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.Function.HasProp(exp1, exp2);
      case "HasLabel":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), varmap);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), varmap);
        return new QueryExpression.Function.HasLabel(exp1, exp2);
      case "InDegree":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.Function.InDegree(exp);
      case "OutDegree":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), varmap);
        return new QueryExpression.Function.OutDegree(exp);
      case "COUNT":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), varmap);
        return new QueryExpression.Aggregation.AggrCount(exp);
      case "MIN":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), varmap);
        return new QueryExpression.Aggregation.AggrMin(exp);
      case "MAX":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), varmap);
        return new QueryExpression.Aggregation.AggrMax(exp);
      case "SUM":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), varmap);
        return new QueryExpression.Aggregation.AggrSum(exp);
      case "AVG":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), varmap);
        return new QueryExpression.Aggregation.AggrAvg(exp);
      case "Star":
        return new QueryExpression.Aggregation.Star();
      default:
        throw new UnsupportedOperationException("Expression unsupported: " + t);
    }
  }

  // helper method
  private static String getString(IStrategoTerm t) {
    while (t.getTermType() != IStrategoTerm.STRING) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(LimitClause("10"))
    }
    return ((IStrategoString) t).stringValue();
  }

  // helper method
  private static IStrategoTerm getList(IStrategoTerm t) {
    while (t.getTermType() != IStrategoTerm.LIST) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(OrderElems([...]))
    }
    return t;
  }

  // helper method
  private static boolean isNone(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName().equals("None");
  }

  // helper method
  private static IStrategoTerm getSome(IStrategoTerm t) {
    if (((IStrategoAppl) t).getConstructor().getName().equals("Some")) {
      return t.getSubterm(0);
    }
    return null;
  }
}
