/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.util.SqlDateTimeFormatter;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

public class SpoofaxAstToGraphQuery {

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
  private static final int POS_PATH_HOP_DISTANCE = 3;
  private static final int POS_PATH_HOP_DISTANCE_MIN = 0;
  private static final int POS_PATH_HOP_DISTANCE_MAX = 1;
  private static final int POS_PATH_NAME = 4;

  private static final int POS_ORDERBY_EXP = 0;
  private static final int POS_ORDERBY_ORDERING = 1;
  private static final int POS_LIMIT = 0;
  private static final int POS_OFFSET = 1;

  private static final int POS_EXPASVAR_EXP = 0;
  private static final int POS_EXPASVAR_VAR = 1;
  private static final int POS_BINARY_EXP_LEFT = 0;
  private static final int POS_BINARY_EXP_RIGHT = 1;
  private static final int POS_UNARY_EXP = 0;
  private static final int POS_AGGREGATE_EXP = 1;
  private static final int POS_PROPREF_VARNAME = 0;
  private static final int POS_PROPREF_PROPNAME = 1;
  private static final int POS_CAST_EXP = 0;
  private static final int POS_CAST_TARGET_TYPE_NAME = 1;
  private static final int POS_ALL_DIFFERENT_EXPS = 0;

  public static GraphQuery translate(IStrategoTerm ast) throws PgqlException {

    // path patterns
    IStrategoTerm pathPatternsT = getList(ast.getSubterm(POS_PATH_PATTERNS));
    Map<String, IStrategoTerm> pathPatternMap = getPathPatterns(pathPatternsT); // map from path pattern name to path
    // pattern term

    // WHERE
    IStrategoTerm graphPatternT = ast.getSubterm(POS_WHERE);

    // vertices
    IStrategoTerm verticesT = getList(graphPatternT.getSubterm(POS_VERTICES));
    Map<String, QueryVariable> vars = new HashMap<>(); // map from variable name to variable
    Set<QueryVertex> vertices = new HashSet<>(getQueryVertices(verticesT, vars));

    // edges
    IStrategoTerm edgesT = getList(graphPatternT.getSubterm(POS_EDGES));
    Set<QueryEdge> edges = new HashSet<>(getQueryEdges(edgesT, vars));

    // paths
    IStrategoTerm pathsT = getList(graphPatternT.getSubterm(POS_PATHS));
    Set<QueryPath> paths = getPaths(pathsT, pathPatternMap, vars);

    // connections
    Set<VertexPairConnection> connections = new HashSet<>();
    connections.addAll(edges);
    connections.addAll(paths);

    // constraints
    IStrategoTerm constraintsT = getList(graphPatternT.getSubterm(POS_CONSTRAINTS));
    Set<QueryExpression> constraints = getQueryExpressions(constraintsT, vars);

    // graph pattern
    GraphPattern graphPattern = new GraphPattern(vertices, connections, constraints);

    // GROUP BY
    IStrategoTerm groupByT = ast.getSubterm(POS_GROUPBY);
    Map<String, QueryVariable> groupKeys = new HashMap<>(); // map from variable name to variable
    List<ExpAsVar> groupByElems = getGroupByElems(vars, groupKeys, groupByT);
    GroupBy groupBy = new GroupBy(groupByElems);
    boolean createOneGroup = ((IStrategoAppl) groupByT).getConstructor().getName().equals("CreateOneGroup"); // create
                                                                                                             // one
                                                                                                             // group
                                                                                                             // when
                                                                                                             // there's
                                                                                                             // no GROUP
                                                                                                             // BY but
                                                                                                             // SELECT
                                                                                                             // has at
                                                                                                             // least
                                                                                                             // one
                                                                                                             // aggregation
    boolean changeScope = groupByElems.size() > 0 || createOneGroup;
    Map<String, QueryVariable> inScopeVars = changeScope ? groupKeys : vars;
    Map<String, QueryVariable> inScopeInAggregationVars = changeScope ? vars
        : Collections.<String, QueryVariable> emptyMap();

    // SELECT
    IStrategoTerm projectionT = ast.getSubterm(POS_PROJECTION);
    HashMap<String, QueryVariable> inScopeVarsForOrderBy = new HashMap<String, QueryVariable>();
    List<ExpAsVar> selectElems = getSelectElems(inScopeVars, inScopeInAggregationVars, inScopeVarsForOrderBy,
        projectionT);
    Projection projection = new Projection(selectElems);

    // ORDER BY
    IStrategoTerm orderByT = ast.getSubterm(POS_ORDERBY);
    List<OrderByElem> orderByElems = getOrderByElems(inScopeVarsForOrderBy, inScopeInAggregationVars, orderByT);
    OrderBy orderBy = new OrderBy(orderByElems);

    // LIMIT OFFSET
    IStrategoTerm limitOffsetT = ast.getSubterm(POS_LIMITOFFSET);
    QueryExpression limit = getLimitOrOffset(limitOffsetT.getSubterm(POS_LIMIT));
    QueryExpression offset = getLimitOrOffset(limitOffsetT.getSubterm(POS_OFFSET));

    return new GraphQuery(projection, graphPattern, groupBy, orderBy, limit, offset);
  }

  private static QueryExpression getLimitOrOffset(IStrategoTerm subterm) throws PgqlException {
    if (isNone(subterm)) {
      return null;
    }
    IStrategoAppl expT = (IStrategoAppl) subterm.getSubterm(0).getSubterm(0);
    return translateExp(expT, Collections.<String, QueryVariable> emptyMap(),
        Collections.<String, QueryVariable> emptyMap());
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
      QueryVertex vertex = vertexName.contains(GENERATED_VAR_SUBSTR) ? new QueryVertex(vertexName, true)
          : new QueryVertex(vertexName, false);
      vertices.add(vertex);
      varmap.put(vertexName, vertex);
    }
    return vertices;
  }

  private static Set<QueryExpression> getQueryExpressions(IStrategoTerm constraintsT, Map<String, QueryVariable> varmap)
      throws PgqlException {
    Set<QueryExpression> constraints = new HashSet<>(constraintsT.getSubtermCount());
    for (IStrategoTerm constraintT : constraintsT) {
      QueryExpression exp = translateExp(constraintT, varmap, Collections.<String, QueryVariable> emptyMap());
      constraints.add(exp);
    }
    return constraints;
  }

  private static List<QueryEdge> getQueryEdges(IStrategoTerm edgesT, Map<String, QueryVariable> varmap) {
    List<QueryEdge> edges = new ArrayList<>(edgesT.getSubtermCount());
    for (IStrategoTerm edgeT : edgesT) {
      edges.add(getQueryEdge(edgeT, varmap));
    }
    return edges;
  }

  private static QueryEdge getQueryEdge(IStrategoTerm edgeT, Map<String, QueryVariable> varmap) {
    String name = getString(edgeT.getSubterm(POS_EDGE_NAME));
    String srcName = getString(edgeT.getSubterm(POS_EDGE_SRC));
    String dstName = getString(edgeT.getSubterm(POS_EDGE_DST));
    boolean directed = getConstructorName(edgeT.getSubterm(POS_EDGE_DIRECTION)).equals("Undirected") == false;

    QueryVertex src = (QueryVertex) varmap.get(srcName);
    QueryVertex dst = (QueryVertex) varmap.get(dstName);

    QueryEdge edge = name.contains(GENERATED_VAR_SUBSTR) ? new QueryEdge(src, dst, name, true, directed)
        : new QueryEdge(src, dst, name, false, directed);

    varmap.put(name, edge);
    return edge;
  }

  private static Set<QueryPath> getPaths(IStrategoTerm pathsT, Map<String, IStrategoTerm> pathPatternMap,
      Map<String, QueryVariable> varmap) throws PgqlException {
    Set<QueryPath> result = new HashSet<>();

    for (IStrategoTerm pathT : pathsT) {
      result.add(getPath(pathT, pathPatternMap, varmap));
    }

    return result;
  }

  private static QueryPath getPath(IStrategoTerm pathT, Map<String, IStrategoTerm> pathPatternMap,
      Map<String, QueryVariable> varmap) throws PgqlException {
    String pathPatternName = getString(pathT.getSubterm(POS_PATH_PATH_PATTERN));
    IStrategoTerm hopDistanceT = pathT.getSubterm(POS_PATH_HOP_DISTANCE);
    long minHopDistance;
    long maxHopDistance;
    if (isSome(hopDistanceT)) {
      minHopDistance = parseLong(hopDistanceT.getSubterm(POS_PATH_HOP_DISTANCE_MIN));
      maxHopDistance = parseLong(hopDistanceT.getSubterm(POS_PATH_HOP_DISTANCE_MAX));
    } else {
      minHopDistance = 1;
      maxHopDistance = 1;
    }

    IStrategoTerm pathPatternT = pathPatternMap.get(pathPatternName);

    // vertices
    IStrategoTerm verticesT = getList(pathPatternT.getSubterm(POS_PATH_PATTERN_VERTICES));
    Map<String, QueryVariable> pathPatternVarmap = new HashMap<>(); // map from variable name to variable
    List<QueryVertex> vertices = getQueryVertices(verticesT, pathPatternVarmap);

    // connections
    IStrategoTerm connectionsT = getList(pathPatternT.getSubterm(POS_PATH_PATTERN_CONNECTIONS));
    List<VertexPairConnection> connections = new ArrayList<>();
    for (IStrategoTerm connectionT : connectionsT) {
      if (((IStrategoAppl) connectionT).getConstructor().getName().equals("Edge")) {
        connections.add(getQueryEdge(connectionT, pathPatternVarmap));
      } else {
        connections.add(getPath(connectionT, pathPatternMap, pathPatternVarmap));
      }
    }

    // constraints
    IStrategoTerm constraintsT = getList(pathPatternT.getSubterm(POS_PATH_PATTERN_CONSTRAINTS));
    Set<QueryExpression> constraints = getQueryExpressions(constraintsT, pathPatternVarmap);

    String srcName = getString(pathT.getSubterm(POS_PATH_SRC));
    String dstName = getString(pathT.getSubterm(POS_PATH_DST));
    String name = getString(pathT.getSubterm(POS_PATH_NAME));
    QueryVertex src = (QueryVertex) varmap.get(srcName);
    QueryVertex dst = (QueryVertex) varmap.get(dstName);

    QueryPath pathPattern = name.contains(GENERATED_VAR_SUBSTR)
        ? new QueryPath(src, dst, vertices, connections, constraints, name, true, minHopDistance, maxHopDistance)
        : new QueryPath(src, dst, vertices, connections, constraints, name, false, minHopDistance, maxHopDistance);

    return pathPattern;
  }

  private static List<ExpAsVar> getGroupByElems(Map<String, QueryVariable> inputVars,
      Map<String, QueryVariable> outputVars, IStrategoTerm groupByT) throws PgqlException {
    if (isSome(groupByT)) { // has GROUP BY
      IStrategoTerm groupByElemsT = getList(groupByT);
      return getExpAsVars(inputVars, Collections.<String, QueryVariable> emptyMap(), outputVars, groupByElemsT);
    }
    return Collections.emptyList();
  }

  private static List<ExpAsVar> getSelectElems(Map<String, QueryVariable> inScopeVars,
      Map<String, QueryVariable> inScopeInAggregationVars, Map<String, QueryVariable> outputVars,
      IStrategoTerm projectionT) throws PgqlException {
    outputVars.putAll(inScopeVars);
    return getExpAsVars(inScopeVars, inScopeInAggregationVars, outputVars, getList(projectionT));
  }

  private static List<ExpAsVar> getExpAsVars(Map<String, QueryVariable> inScopeVars,
      Map<String, QueryVariable> inScopeInAggregationVars, Map<String, QueryVariable> outputVars,
      IStrategoTerm expAsVarsT) throws PgqlException {
    List<ExpAsVar> expAsVars = new ArrayList<>(expAsVarsT.getSubtermCount());
    for (IStrategoTerm expAsVarT : expAsVarsT) {
      String varName = getString(expAsVarT.getSubterm(POS_EXPASVAR_VAR));
      QueryExpression exp = translateExp(expAsVarT.getSubterm(POS_EXPASVAR_EXP), inScopeVars, inScopeInAggregationVars);

      ExpAsVar expAsVar;
      switch (getConstructorName(expAsVarT)) {
        case "ExpAsVar":
        case "ExpAsGroupVar":
        case "ExpAsSelectVar":
          expAsVar = new ExpAsVar(exp, varName, false);
          break;
        case "AnonymousExpAsVar":
        case "AnonymousExpAsGroupVar":
          expAsVar = new ExpAsVar(exp, varName, true);
          break;
        default:
          throw new IllegalArgumentException("Unexpected term: " + expAsVarT);
      }

      expAsVars.add(expAsVar);
      outputVars.put(varName, expAsVar);
    }
    return expAsVars;
  }

  private static List<OrderByElem> getOrderByElems(Map<String, QueryVariable> inScopeVars,
      Map<String, QueryVariable> inScopeInAggregationVars, IStrategoTerm orderByT) throws PgqlException {
    List<OrderByElem> orderByElems = new ArrayList<>();
    if (!isNone(orderByT)) { // has ORDER BY
      IStrategoTerm orderByElemsT = getList(orderByT);
      for (IStrategoTerm orderByElemT : orderByElemsT) {
        QueryExpression exp = translateExp(orderByElemT.getSubterm(POS_ORDERBY_EXP), inScopeVars,
            inScopeInAggregationVars);
        boolean ascending = ((IStrategoAppl) orderByElemT.getSubterm(POS_ORDERBY_ORDERING)).getConstructor().getName()
            .equals("Asc");
        orderByElems.add(new OrderByElem(exp, ascending));
      }
    }
    return orderByElems;
  }

  private static QueryExpression translateExp(IStrategoTerm t, Map<String, QueryVariable> inScopeVars,
      Map<String, QueryVariable> inScopeInAggregationVars) throws PgqlException {
    String cons = ((IStrategoAppl) t).getConstructor().getName();

    switch (cons) {
      case "Sub":
        QueryExpression exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        QueryExpression exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.ArithmeticExpression.Sub(exp1, exp2);
      case "Add":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.ArithmeticExpression.Add(exp1, exp2);
      case "Mul":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.ArithmeticExpression.Mul(exp1, exp2);
      case "Div":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.ArithmeticExpression.Div(exp1, exp2);
      case "Mod":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.ArithmeticExpression.Mod(exp1, exp2);
      case "UMin":
        QueryExpression exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.ArithmeticExpression.UMin(exp);
      case "And":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.LogicalExpression.And(exp1, exp2);
      case "Or":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.LogicalExpression.Or(exp1, exp2);
      case "Not":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.LogicalExpression.Not(exp);
      case "Eq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.RelationalExpression.Equal(exp1, exp2);
      case "Neq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.RelationalExpression.NotEqual(exp1, exp2);
      case "Gt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.RelationalExpression.Greater(exp1, exp2);
      case "Gte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.RelationalExpression.GreaterEqual(exp1, exp2);
      case "Lt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.RelationalExpression.Less(exp1, exp2);
      case "Lte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.RelationalExpression.LessEqual(exp1, exp2);
      case "Integer":
        long l = parseLong(t);
        return new QueryExpression.Constant.ConstInteger(l);
      case "Decimal":
        double d = Double.parseDouble(getString(t));
        return new QueryExpression.Constant.ConstDecimal(d);
      case "String":
        String s = unescapeJava(getString(t));
        return new QueryExpression.Constant.ConstString(s);
      case "True":
        return new QueryExpression.Constant.ConstBoolean(true);
      case "False":
        return new QueryExpression.Constant.ConstBoolean(false);
      case "Date":
        s = getString(t);
        LocalDate date = LocalDate.parse(s, SqlDateTimeFormatter.SQL_DATE);
        return new QueryExpression.Constant.ConstDate(date);
      case "Time":
        s = getString(t);
        try {
          LocalTime time = LocalTime.parse(s, SqlDateTimeFormatter.SQL_TIME);
          return new QueryExpression.Constant.ConstTime(time);
        } catch (DateTimeParseException e) {
          OffsetTime timeWithTimezone = OffsetTime.parse(s, SqlDateTimeFormatter.SQL_TIME_WITH_TIMEZONE);
          return new QueryExpression.Constant.ConstTimeWithTimezone(timeWithTimezone);
        }
      case "Timestamp":
        s = getString(t);
        try {
          LocalDateTime timestamp = LocalDateTime.parse(s, SqlDateTimeFormatter.SQL_TIMESTAMP);
          return new QueryExpression.Constant.ConstTimestamp(timestamp);
        } catch (DateTimeParseException e) {
          OffsetDateTime timestampWithTimezone = OffsetDateTime.parse(s, SqlDateTimeFormatter.SQL_TIMESTAMP_WITH_TIMEZONE);
          return new QueryExpression.Constant.ConstTimestampWithTimezone(timestampWithTimezone);
        }
      case "Null":
        return new QueryExpression.ConstNull();
      case "VarRef":
      case "GroupRef":
      case "SelectOrGroupRef":
      case "VarOrSelectRef":
        String varName = getString(t);
        QueryVariable var = inScopeVars.get(varName);
        if (var == null) {
          throw new PgqlException("Variable " + varName + " undefined");
        }
        return new QueryExpression.VarRef(var);
      case "BindVariable":
        int parameterIndex = getInt(t);
        return new QueryExpression.BindVariable(parameterIndex);
      case "PropRef":
        varName = getString(t.getSubterm(POS_PROPREF_VARNAME));
        var = inScopeVars.get(varName);
        if (var == null) {
          throw new PgqlException("Variable " + varName + " undefined");
        }
        String propname = getString(t.getSubterm(POS_PROPREF_PROPNAME));
        return new QueryExpression.PropertyAccess(var, propname);
      case "Regex":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.Regex(exp1, exp2);
      case "Label":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.EdgeLabel(exp);
      case "Labels":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.VertexLabels(exp);
      case "Id":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.Id(exp);
      case "Has":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.HasProp(exp1, exp2);
      case "HasLabel":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), inScopeVars, inScopeInAggregationVars);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.HasLabel(exp1, exp2);
      case "InDegree":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.InDegree(exp);
      case "OutDegree":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), inScopeVars, inScopeInAggregationVars);
        return new QueryExpression.Function.OutDegree(exp);
      case "Cast":
        exp = translateExp(t.getSubterm(POS_CAST_EXP), inScopeVars, inScopeInAggregationVars);
        String targetTypeName = getString(t.getSubterm(POS_CAST_TARGET_TYPE_NAME));
        return new QueryExpression.Function.Cast(exp, targetTypeName);
      case "AllDifferent":
        IStrategoTerm[] expsT = t.getSubterm(POS_ALL_DIFFERENT_EXPS).getAllSubterms();
        List<QueryExpression> exps = new ArrayList<>();
        for (IStrategoTerm expT : expsT) {
          exps.add(translateExp(expT, inScopeVars, inScopeInAggregationVars));
        }
        return new QueryExpression.Function.AllDifferent(exps);
      case "COUNT":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), inScopeInAggregationVars, inScopeInAggregationVars);
        return new QueryExpression.Aggregation.AggrCount(exp);
      case "MIN":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), inScopeInAggregationVars, inScopeInAggregationVars);
        return new QueryExpression.Aggregation.AggrMin(exp);
      case "MAX":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), inScopeInAggregationVars, inScopeInAggregationVars);
        return new QueryExpression.Aggregation.AggrMax(exp);
      case "SUM":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), inScopeInAggregationVars, inScopeInAggregationVars);
        return new QueryExpression.Aggregation.AggrSum(exp);
      case "AVG":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), inScopeInAggregationVars, inScopeInAggregationVars);
        return new QueryExpression.Aggregation.AggrAvg(exp);
      case "Star":
        return new QueryExpression.Star();
      default:
        throw new UnsupportedOperationException("Expression unsupported: " + t);
    }
  }

  // helper method
  private static long parseLong(IStrategoTerm t) throws PgqlException {
    try {
      return Long.parseLong(getString(t));
    } catch (NumberFormatException e) {
      throw new PgqlException(getString(t) + " is too large to be stored as Long");
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
  private static int getInt(IStrategoTerm t) {
    while (t.getTermType() != IStrategoTerm.INT) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(LimitClause("10"))
    }
    return ((IStrategoInt) t).intValue();
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
  private static boolean isSome(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName().equals("Some");
  }

  // helper method
  private static String getConstructorName(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName();
  }
}
