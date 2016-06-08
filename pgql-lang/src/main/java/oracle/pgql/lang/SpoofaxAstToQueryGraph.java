/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.ir.QueryPath.Direction;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVariable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpoofaxAstToQueryGraph {

  private static final String GENERATED_VAR_SUBSTR = "<<generated>>";

  private static final int POS_PATH_PATTERNS = 0;
  private static final int POS_PROJECTION = 1;
  private static final int POS_FROM = 2; // not yet parsed
  private static final int POS_WHERE = 3;
  private static final int POS_GROUPBY = 4;
  private static final int POS_ORDERBY = 5;
  private static final int POS_LIMITOFFSET = 6;

  private static final int POS_PATH_PATTERN_NAME = 0;
  private static final int POS_PATH_PATTERN_VERTICES = 1;
  private static final int POS_PATH_PATTERN_CONNECTIONS = 2;
  private static final int POS_PATH_PATTERN_CONSTRAINTS = 3;

  private static final int POS_VERTICES = 0;
  private static final int POS_EDGES = 1;
  private static final int POS_PATHS = 2;
  private static final int POS_CONSTRAINTS = 3;

  private static final int POS_EDGE_SRC = 0;
  private static final int POS_EDGE_DST = 2;
  private static final int POS_EDGE_NAME = 1;
  private static final int POS_EDGE_DIRECTION = 3;

  private static final int POS_PATH_SRC = 0;
  private static final int POS_PATH_DST = 1;
  private static final int POS_PATH_PATH_PATTERN = 2;
  private static final int POS_PATH_KLEENE_STAR = 3;
  private static final int POS_PATH_NAME = 4;
  
  private static final int POS_KLEENE_STAR_MIN_MAX_REPETITION = 0;
  private static final int POS_MIN_REPETITION = 0;
  private static final int POS_MAX_REPETITION = 1;

  private static final int POS_ORDERBY_EXP = 0;
  private static final int POS_ORDERBY_ORDERING = 1;
  private static final int POS_LIMIT = 0;
  private static final int POS_OFFSET = 1;

  private static final int POS_EXPASVAR_EXP = 1;
  private static final int POS_EXPASVAR_VAR = 0;
  private static final int POS_BINARY_EXP_LEFT = 0;
  private static final int POS_BINARY_EXP_RIGHT = 1;
  private static final int POS_UNARY_EXP = 0;
  private static final int POS_AGGREGATE_EXP = 1;
  private static final int POS_PROPREF_VARNAME = 0;
  private static final int POS_PROPREF_PROPNAME = 1;

  public static GraphQuery translate(IStrategoTerm ast) throws PgqlException {

    // path patterns
    IStrategoTerm pathPatternsT = getList(ast.getSubterm(POS_PATH_PATTERNS));
    Map<String, IStrategoTerm> pathPatternMap = getPathPatterns(pathPatternsT); // map from path pattern name to path
                                                                                // pattern term

    // WHERE
    IStrategoTerm graphPatternT = ast.getSubterm(POS_WHERE);

    // vertices
    IStrategoTerm verticesT = getList(graphPatternT.getSubterm(POS_VERTICES));
    Map<String, QueryVariable> varmap = new HashMap<>(); // map from variable name to variable
    Set<QueryVertex> vertices = new HashSet<>(getQueryVertices(verticesT, varmap));

    // edges
    IStrategoTerm edgesT = getList(graphPatternT.getSubterm(POS_EDGES));
    Set<QueryEdge> edges = new HashSet<>(getQueryEdges(edgesT, varmap));

    // paths
    IStrategoTerm pathsT = getList(graphPatternT.getSubterm(POS_PATHS));
    Set<QueryPath> paths = getPaths(pathsT, pathPatternMap, varmap);

    // connections
    Set<VertexPairConnection> connections = new HashSet<>();
    connections.addAll(edges);
    connections.addAll(paths);

    // constraints
    IStrategoTerm constraintsT = getList(graphPatternT.getSubterm(POS_CONSTRAINTS));
    Set<QueryExpression> constraints = getQueryExpressions(constraintsT, varmap);

    // graph pattern
    GraphPattern graphPattern = new GraphPattern(vertices, connections, constraints);

    // GROUP BY
    IStrategoTerm groupByT = ast.getSubterm(POS_GROUPBY);
    List<ExpAsVar> groupByElems = getGroupByElems(varmap, groupByT);
    GroupBy groupBy = new GroupBy(groupByElems);

    // SELECT
    IStrategoTerm projectionT = ast.getSubterm(POS_PROJECTION);
    List<ExpAsVar> selectElems = getSelectElems(varmap, projectionT);
    Projection projection = new Projection(selectElems);

    // ORDER BY
    IStrategoTerm orderByT = ast.getSubterm(POS_ORDERBY);
    List<OrderByElem> orderByElems = getOrderByElems(varmap, orderByT);
    OrderBy orderBy = new OrderBy(orderByElems);

    // LIMIT OFFSET
    IStrategoTerm limitOffsetT = ast.getSubterm(POS_LIMITOFFSET);
    long limit = getLimitOrOffset(limitOffsetT.getSubterm(POS_LIMIT));
    long offset = getLimitOrOffset(limitOffsetT.getSubterm(POS_OFFSET));

    return new GraphQuery(projection, graphPattern, groupBy, orderBy, limit, offset);
  }

  private static Map<String, IStrategoTerm> getPathPatterns(IStrategoTerm pathPatternsT) {
    Map<String, IStrategoTerm> result = new HashMap<>();
    for (IStrategoTerm pathPatternT : pathPatternsT) {
      String name = getString(pathPatternT.getSubterm(POS_PATH_PATTERN_NAME));
      result.put(name, pathPatternT);
    }
    return result;
  }

  private static List<QueryVertex> getQueryVertices(IStrategoTerm verticesT, Map<String, QueryVariable> varmap) {
    List<QueryVertex> vertices = new ArrayList<>(verticesT.getSubtermCount());
    for (IStrategoTerm vertexT : verticesT) {
      String vertexName = getString(vertexT);
      QueryVertex vertex = vertexName.contains(GENERATED_VAR_SUBSTR) ? new QueryVertex() : new QueryVertex(vertexName);
      vertices.add(vertex);
      varmap.put(vertexName, vertex);
    }
    return vertices;
  }

  private static Set<QueryExpression> getQueryExpressions(IStrategoTerm constraintsT, Map<String, QueryVariable> varmap)
      throws PgqlException {
    Set<QueryExpression> constraints = new HashSet<>(constraintsT.getSubtermCount());
    for (IStrategoTerm constraintT : constraintsT) {
      QueryExpression exp = translateExp(constraintT, varmap);
      constraints.add(exp);
    }
    return constraints;
  }

  private static List<QueryEdge> getQueryEdges(IStrategoTerm edgesT, Map<String, QueryVariable> varmap) {
    List<QueryEdge> edges = new ArrayList<>(edgesT.getSubtermCount());
    for (IStrategoTerm edgeT : edgesT) {
      String name = getString(edgeT.getSubterm(POS_EDGE_NAME));
      String srcName = getString(edgeT.getSubterm(POS_EDGE_SRC));
      String dstName = getString(edgeT.getSubterm(POS_EDGE_DST));

      QueryVertex src = (QueryVertex) varmap.get(srcName);
      QueryVertex dst = (QueryVertex) varmap.get(dstName);

      QueryEdge edge = name.contains(GENERATED_VAR_SUBSTR) ? new QueryEdge(src, dst) : new QueryEdge(src, dst, name);

      edges.add(edge);
      varmap.put(name, edge);
    }
    return edges;
  }

  private static Set<QueryPath> getPaths(IStrategoTerm pathsT, Map<String, IStrategoTerm> pathPatternMap,
      Map<String, QueryVariable> varmap) throws PgqlException {
    Set<QueryPath> result = new HashSet<>();

    // for now, assume every RPQ has a Kleene star and that there is no nested Kleene star
    for (IStrategoTerm pathT : pathsT) {

      String pathPatternName = getString(pathT.getSubterm(POS_PATH_PATH_PATTERN));
      IStrategoTerm kleeneStarT = pathT.getSubterm(POS_PATH_KLEENE_STAR);
      long minRepetition = 0;
      long maxRepetition = -1;
      if (isNone(kleeneStarT) == false) {
        IStrategoTerm minMaxRepetitionT = getSome(kleeneStarT).getSubterm(POS_KLEENE_STAR_MIN_MAX_REPETITION);
        if (isNone(minMaxRepetitionT) == false) {
          IStrategoTerm minRepetitionT = getSome(minMaxRepetitionT).getSubterm(POS_MIN_REPETITION);
          if (isNone(minRepetitionT) == false) {
            minRepetition = Integer.parseInt(getString(minRepetitionT));
          }
          
          IStrategoTerm maxRepetitionT = getSome(minMaxRepetitionT).getSubterm(POS_MAX_REPETITION);
          if (isNone(maxRepetitionT) == false) {
            maxRepetition = Integer.parseInt(getString(maxRepetitionT));
          }
        }
      }
      
      IStrategoTerm pathPatternT = pathPatternMap.get(pathPatternName);

      // vertices
      IStrategoTerm verticesT = getList(pathPatternT.getSubterm(POS_PATH_PATTERN_VERTICES));
      Map<String, QueryVariable> pathPatternVarmap = new HashMap<>(); // map from variable name to variable
      List<QueryVertex> vertices = getQueryVertices(verticesT, pathPatternVarmap);

      // edges
      IStrategoTerm edgesT = getList(pathPatternT.getSubterm(POS_PATH_PATTERN_CONNECTIONS));
      List<QueryEdge> edges = getQueryEdges(edgesT, pathPatternVarmap);
      List<VertexPairConnection> connections = new ArrayList<>();
      connections.addAll(edges);

      // constraints
      IStrategoTerm constraintsT = getList(pathPatternT.getSubterm(POS_PATH_PATTERN_CONSTRAINTS));
      Set<QueryExpression> constraints = getQueryExpressions(constraintsT, pathPatternVarmap);

      String srcName = getString(pathT.getSubterm(POS_PATH_SRC));
      String dstName = getString(pathT.getSubterm(POS_PATH_DST));
      String name = getString(pathT.getSubterm(POS_PATH_NAME));
      QueryVertex src = (QueryVertex) varmap.get(srcName);
      QueryVertex dst = (QueryVertex) varmap.get(dstName);

      List<Direction> directions = new ArrayList<Direction>();
      for (IStrategoTerm edgeT : edgesT) {
        Direction direction = ((IStrategoAppl) edgeT.getSubterm(POS_EDGE_DIRECTION)).getConstructor().getName()
            .equals("Outgoing") ? Direction.OUTGOING : Direction.INCOMING;
        directions.add(direction);
      }

      QueryPath pathPattern = name.contains(GENERATED_VAR_SUBSTR)
          ? new QueryPath(src, dst, vertices, connections, directions, constraints, minRepetition, maxRepetition)
          : new QueryPath(src, dst, vertices, connections, directions, constraints, minRepetition, maxRepetition, name);

      result.add(pathPattern);
    }

    return result;
  }

  private static List<ExpAsVar> getGroupByElems(Map<String, QueryVariable> varmap, IStrategoTerm groupByT)
      throws PgqlException {
    if (!isNone(groupByT)) { // has GROUP BY
      IStrategoTerm groupByElemsT = getList(groupByT);
      return getExpAsVars(varmap, groupByElemsT);
    }
    return new ArrayList<>();
  }

  private static List<ExpAsVar> getSelectElems(Map<String, QueryVariable> varmap, IStrategoTerm projectionT)
      throws PgqlException {
    return getExpAsVars(varmap, getList(projectionT));
  }

  private static List<ExpAsVar> getExpAsVars(Map<String, QueryVariable> varmap, IStrategoTerm expAsVarsT)
      throws PgqlException {
    List<ExpAsVar> expAsVars = new ArrayList<>(expAsVarsT.getSubtermCount());
    for (IStrategoTerm expAsVarT : expAsVarsT) {
      String varName = getString(expAsVarT.getSubterm(POS_EXPASVAR_VAR));
      QueryExpression exp = translateExp(expAsVarT.getSubterm(POS_EXPASVAR_EXP), varmap);

      ExpAsVar expAsVar;
      if (varName.contains(GENERATED_VAR_SUBSTR)) {
        expAsVar = new ExpAsVar(exp, varName.replaceFirst(GENERATED_VAR_SUBSTR + ".*", ""), true);
      } else {
        expAsVar = new ExpAsVar(exp, varName, false);
      }
      expAsVars.add(expAsVar);
      varmap.put(varName, expAsVar);
    }
    return expAsVars;
  }

  private static List<OrderByElem> getOrderByElems(Map<String, QueryVariable> varmap, IStrategoTerm orderByT)
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

  private static QueryExpression translateExp(IStrategoTerm t, Map<String, QueryVariable> varmap) throws PgqlException {
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
          throw new PgqlException(getString(t) + " is too large to be stored as Long");
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
        return new QueryExpression.ConstNull();
      case "VarRef":
      case "GroupRef":
      case "SelectOrGroupRef":
        String varName = getString(t);
        QueryVariable var = varmap.get(varName);
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
        return new QueryExpression.PropertyAccess(var, propname);
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
    return t.getSubterm(0);
  }
}
