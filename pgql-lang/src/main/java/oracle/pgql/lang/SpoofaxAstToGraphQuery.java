/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.Direction;
import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.PathFindingGoal;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.And;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
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
import oracle.pgql.lang.ir.QueryExpression.ExtractExpression.ExtractField;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.InPredicate;
import oracle.pgql.lang.ir.QueryExpression.InPredicate.InValueList;
import oracle.pgql.lang.ir.QueryExpression.IsNull;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryType;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.ir.update.GraphUpdate;
import oracle.pgql.lang.ir.update.GraphUpdateQuery;
import oracle.pgql.lang.ir.update.PropertyUpdate;
import oracle.pgql.lang.util.SqlDateTimeFormatter;

public class SpoofaxAstToGraphQuery {

  private static final String GENERATED_VAR_SUBSTR = "<<anonymous>>";

  private static final int POS_COMMON_PATH_EXPRESSIONS = 0;
  private static final int POS_SELECT_OR_UPDATE = 1;
  private static final int POS_GRAPH_NAME = 2;
  private static final int POS_GRAPH_PATTERN = 3;
  private static final int POS_GROUPBY = 4;
  private static final int POS_HAVING = 5;
  private static final int POS_ORDERBY = 6;
  private static final int POS_LIMITOFFSET = 7;

  private static final int POS_COMMON_PATH_EXPRESSION_NAME = 0;
  private static final int POS_COMMON_PATH_EXPRESSION_VERTICES = 1;
  private static final int POS_COMMON_PATH_EXPRESSION_CONNECTIONS = 2;
  private static final int POS_COMMON_PATH_EXPRESSION_CONSTRAINTS = 3;

  private static final int POS_PROJECTION_DISTINCT = 0;
  private static final int POS_PROJECTION_ELEMS = 1;

  private static final int POS_UPDATE_ELEMS = 0;
  private static final int POS_PROPERTY_UPDATE_PROPERTY_REFERENCE = 0;
  private static final int POS_PROPERTY_UPDATE_VALUE_EXPRESSION = 1;

  private static final int POS_VERTICES = 0;
  private static final int POS_CONNECTIONS = 1;
  private static final int POS_CONSTRAINTS = 2;

  private static final int POS_VERTEX_NAME = 0;
  private static final int POS_VERTEX_ORIGIN_OFFSET = 1;

  private static final int POS_EDGE_SRC = 0;
  private static final int POS_EDGE_NAME = 1;
  private static final int POS_EDGE_DST = 2;
  private static final int POS_EDGE_DIRECTION = 3;
  private static final int POS_EDGE_ORIGIN_OFFSET = 4;

  private static final int POS_PATH_SRC = 0;
  private static final int POS_PATH_DST = 1;
  private static final int POS_PATH_EXPRESSION = 2;
  private static final int POS_PATH_QUANTIFIERS = 3;
  private static final int POS_PATH_QUANTIFIERS_MIN_HOPS = 0;
  private static final int POS_PATH_QUANTIFIERS_MAX_HOPS = 1;
  private static final int POS_PATH_NAME = 4;
  private static final int POS_PATH_DIRECTION = 5;
  private static final int POS_PATH_FINDING_GOAL = 6;
  private static final int POS_PATH_K_VALUE = 7;

  private static final int POS_ORDERBY_EXP = 0;
  private static final int POS_ORDERBY_ORDERING = 1;
  private static final int POS_LIMIT = 0;
  private static final int POS_OFFSET = 1;

  private static final int POS_EXPASVAR_EXP = 0;
  private static final int POS_EXPASVAR_VAR = 1;
  private static final int POS_EXPASVAR_ANONYMOUS = 2;
  private static final int POS_EXPASVAR_ORIGIN_OFFSET = 3;

  private static final int POS_BINARY_EXP_LEFT = 0;
  private static final int POS_BINARY_EXP_RIGHT = 1;
  private static final int POS_UNARY_EXP = 0;
  private static final int POS_AGGREGATE_DISTINCT = 0;
  private static final int POS_AGGREGATE_EXP = 1;
  private static final int POS_VARREF_VARNAME = 0;
  private static final int POS_VARREF_ORIGIN_OFFSET = 1;
  private static final int POS_PROPREF_VARREF = 0;
  private static final int POS_PROPREF_PROPNAME = 1;
  private static final int POS_CAST_EXP = 0;
  private static final int POS_CAST_TARGET_TYPE_NAME = 1;
  private static final int POS_EXISTS_SUBQUERY = 0;
  private static final int POS_SCALARSUBQUERY_SUBQUERY = 0;
  private static final int POS_SUBQUERY = 0;
  private static final int POS_FUNCTION_CALL_PACKAGE_NAME = 0;
  private static final int POS_FUNCTION_CALL_ROUTINE_NAME = 1;
  private static final int POS_FUNCTION_CALL_EXPS = 2;
  private static final int POS_EXTRACT_FIELD = 0;
  private static final int POS_EXTRACT_EXP = 1;
  private static final int POS_IN_PREDICATE_EXP = 0;
  private static final int POS_IN_PREDICATE_VALUES = 1;
  private static final int POS_IS_NULL_EXP = 0;

  public static GraphQuery translate(IStrategoTerm ast) throws PgqlException {
    return translate(ast, new TranslationContext(new HashMap<>(), new HashSet<>(), new HashMap<>()));
  }

  /**
   *
   * @param ast
   *          abstract syntax tree returned by Spoofax parser
   * @param vars
   *          map from variable name to variable
   */
  private static GraphQuery translate(IStrategoTerm ast, TranslationContext ctx) throws PgqlException {

    if (!((IStrategoAppl) ast).getConstructor().getName().equals("NormalizedQuery")) {
      return null; // failed to parse query
    }

    // path patterns
    IStrategoTerm commonPathExpressionsT = getList(ast.getSubterm(POS_COMMON_PATH_EXPRESSIONS));
    List<CommonPathExpression> commonPathExpressions = getCommonPathExpressions(commonPathExpressionsT, ctx);

    // graph pattern
    IStrategoTerm graphPatternT = ast.getSubterm(POS_GRAPH_PATTERN);

    // vertices
    IStrategoTerm verticesT = getList(graphPatternT.getSubterm(POS_VERTICES));
    Set<QueryVertex> vertices = new HashSet<>(getQueryVertices(verticesT, ctx));
    Map<String, QueryVertex> vertexMap = new HashMap<>();
    vertices.stream().forEach(vertex -> vertexMap.put(vertex.getName(), vertex));

    // connections
    IStrategoTerm connectionsT = getList(graphPatternT.getSubterm(POS_CONNECTIONS));
    LinkedHashSet<VertexPairConnection> connections = getConnections(connectionsT, ctx, vertexMap);

    giveAnonymousVariablesUniqueHiddenName(vertices, ctx);
    giveAnonymousVariablesUniqueHiddenName(connections, ctx);

    // constraints
    IStrategoTerm constraintsT = getList(graphPatternT.getSubterm(POS_CONSTRAINTS));
    LinkedHashSet<QueryExpression> constraints = getQueryExpressions(constraintsT, ctx);

    // graph pattern
    GraphPattern graphPattern = new GraphPattern(vertices, connections, constraints);

    // GROUP BY
    IStrategoTerm groupByT = ast.getSubterm(POS_GROUPBY);
    GroupBy groupBy = getGroupBy(ctx, groupByT);

    // SELECT or UPDATE

    Projection projection = null;
    GraphUpdate graphUpdate = null;
    IStrategoTerm selectOrUpdateT = ast.getSubterm(POS_SELECT_OR_UPDATE);
    String selectOrUpdate = ((IStrategoAppl) selectOrUpdateT).getConstructor().getName();
    switch (selectOrUpdate) {
      case "SelectClause":
        projection = translateSelectClause(ctx, selectOrUpdateT);
        break;
      case "UpdateClause":
        graphUpdate = translateUpdateClause(ctx, selectOrUpdateT);
        break;
      default:
        throw new IllegalStateException(selectOrUpdate);
    }

    // input graph name
    IStrategoTerm fromT = ast.getSubterm(POS_GRAPH_NAME);
    String inputGraphName = isNone(fromT) ? null : getString(fromT);

    // HAVING
    QueryExpression having = tryGetExpression(ast.getSubterm(POS_HAVING), ctx);

    // ORDER BY
    IStrategoTerm orderByT = ast.getSubterm(POS_ORDERBY);
    List<OrderByElem> orderByElems = getOrderByElems(ctx, orderByT);
    OrderBy orderBy = new OrderBy(orderByElems);

    // LIMIT OFFSET
    IStrategoTerm limitOffsetT = ast.getSubterm(POS_LIMITOFFSET);
    QueryExpression limit = tryGetExpression(limitOffsetT.getSubterm(POS_LIMIT), ctx);
    QueryExpression offset = tryGetExpression(limitOffsetT.getSubterm(POS_OFFSET), ctx);

    switch (selectOrUpdate) {
      case "SelectClause":
        return new SelectQuery(commonPathExpressions, projection, inputGraphName, graphPattern, groupBy, having,
            orderBy, limit, offset);
      case "UpdateClause":
        return new GraphUpdateQuery(commonPathExpressions, graphUpdate, inputGraphName, graphPattern, groupBy, having,
            orderBy, limit, offset);
      default:
        throw new IllegalStateException(selectOrUpdate);
    }
  }

  private static Projection translateSelectClause(TranslationContext ctx, IStrategoTerm selectOrUpdateT)
      throws PgqlException {
    Projection projection;
    IStrategoTerm distinctT = selectOrUpdateT.getSubterm(POS_PROJECTION_DISTINCT);
    boolean distinct = isSome(distinctT);

    IStrategoTerm projectionElemsT = selectOrUpdateT.getSubterm(POS_PROJECTION_ELEMS);
    List<ExpAsVar> selectElems;
    if (projectionElemsT.getTermType() == IStrategoTerm.APPL
        && ((IStrategoAppl) projectionElemsT).getConstructor().getName().equals("Star")) {
      // GROUP BY in combination with SELECT *. Even though the parser will generate an error for it, the
      // translation to
      // GraphQuery should succeed (error recovery)
      selectElems = new ArrayList<>();
    } else {
      projectionElemsT = projectionElemsT.getSubterm(0);
      selectElems = getExpAsVars(ctx, projectionElemsT);
    }
    projection = new Projection(distinct, selectElems);
    return projection;
  }

  private static GraphUpdate translateUpdateClause(TranslationContext ctx, IStrategoTerm selectOrUpdateT)
      throws PgqlException {
    GraphUpdate graphUpdate;
    IStrategoTerm propertyUpdatesT = selectOrUpdateT.getSubterm(POS_UPDATE_ELEMS);
    List<PropertyUpdate> propertyUpdates = new ArrayList<>();
    for (IStrategoTerm propertyUpdateT : propertyUpdatesT) {
      IStrategoTerm propertyAccessT = propertyUpdateT.getSubterm(POS_PROPERTY_UPDATE_PROPERTY_REFERENCE);
      QueryExpression.PropertyAccess propertyAccess = (PropertyAccess) translateExp(propertyAccessT, ctx);

      IStrategoTerm valueExpressionT = propertyUpdateT.getSubterm(POS_PROPERTY_UPDATE_VALUE_EXPRESSION);
      QueryExpression valueExpression = translateExp(valueExpressionT, ctx);

      propertyUpdates.add(new PropertyUpdate(propertyAccess, valueExpression));
    }
    graphUpdate = new GraphUpdate(propertyUpdates);
    return graphUpdate;
  }

  private static QueryExpression tryGetExpression(IStrategoTerm term, TranslationContext ctx) throws PgqlException {
    if (isNone(term)) {
      return null;
    }
    IStrategoAppl expT = (IStrategoAppl) term.getSubterm(0).getSubterm(0);
    return translateExp(expT, ctx);
  }

  private static List<CommonPathExpression> getCommonPathExpressions(IStrategoTerm pathPatternsT,
      TranslationContext ctx)
      throws PgqlException {
    List<CommonPathExpression> result = new ArrayList<>();
    for (IStrategoTerm pathPatternT : pathPatternsT) {
      CommonPathExpression commonPathExpression = getPathExpression(pathPatternT, ctx);
      ctx.getCommonPathExpressions().put(commonPathExpression.getName(), commonPathExpression);
      result.add(commonPathExpression);
    }
    return result;
  }

  private static CommonPathExpression getPathExpression(IStrategoTerm pathPatternT, TranslationContext ctx)
      throws PgqlException {
    String name = getString(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_NAME));
    // vertices
    IStrategoTerm verticesT = getList(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_VERTICES));
    List<QueryVertex> vertices = getQueryVertices(verticesT, ctx);
    Map<String, QueryVertex> vertexMap = new HashMap<>();
    vertices.stream().forEach(vertex -> vertexMap.put(vertex.getName(), vertex));

    // connections
    IStrategoTerm connectionsT = getList(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_CONNECTIONS));
    List<VertexPairConnection> connections = new ArrayList<>();
    for (IStrategoTerm connectionT : connectionsT) {
      if (((IStrategoAppl) connectionT).getConstructor().getName().equals("Edge")) {
        connections.add(getQueryEdge(connectionT, ctx, vertexMap));
      } else {
        connections.add(getPath(connectionT, ctx, vertexMap));
      }
    }

    // constraints
    IStrategoTerm constraintsT = getList(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_CONSTRAINTS));
    LinkedHashSet<QueryExpression> constraints = getQueryExpressions(constraintsT, ctx);
    return new CommonPathExpression(name, vertices, connections, constraints);
  }

  private static List<QueryVertex> getQueryVertices(IStrategoTerm verticesT, TranslationContext ctx) {
    List<QueryVertex> vertices = new ArrayList<>(verticesT.getSubtermCount());
    for (IStrategoTerm vertexT : verticesT) {
      String vertexName = getString(vertexT.getSubterm(POS_VERTEX_NAME));
      IStrategoTerm originPosition = vertexT.getSubterm(POS_VERTEX_ORIGIN_OFFSET);
      boolean anonymous = vertexName.contains(GENERATED_VAR_SUBSTR);

      QueryVertex vertex = new QueryVertex(vertexName, anonymous);
      ctx.addVar(vertex, vertexName, originPosition);
      vertices.add(vertex);
    }
    return vertices;
  }

  private static LinkedHashSet<QueryExpression> getQueryExpressions(IStrategoTerm constraintsT, TranslationContext ctx)
      throws PgqlException {
    LinkedHashSet<QueryExpression> constraints = new LinkedHashSet<>(constraintsT.getSubtermCount());
    for (IStrategoTerm constraintT : constraintsT) {
      QueryExpression exp = translateExp(constraintT, ctx);
      addQueryExpressions(exp, constraints);
    }
    return constraints;
  }

  private static void addQueryExpressions(QueryExpression exp, Set<QueryExpression> constraints) {
    if (exp.getExpType() == ExpressionType.AND) {
      And and = (And) exp;
      addQueryExpressions(and.getExp1(), constraints);
      addQueryExpressions(and.getExp2(), constraints);
    } else {
      constraints.add(exp);
    }
  }

  private static LinkedHashSet<VertexPairConnection> getConnections(IStrategoTerm connectionsT, TranslationContext ctx,
      Map<String, QueryVertex> vertexMap)
      throws PgqlException {

    LinkedHashSet<VertexPairConnection> result = new LinkedHashSet<>();

    for (IStrategoTerm connectionT : connectionsT) {
      String consName = ((IStrategoAppl) connectionT).getConstructor().getName();

      if (consName.equals("Edge")) {
        result.add(getQueryEdge(connectionT, ctx, vertexMap));
      } else {
        assert consName.equals("Path");
        result.add(getPath(connectionT, ctx, vertexMap));
      }
    }

    return result;
  }

  private static QueryEdge getQueryEdge(IStrategoTerm edgeT, TranslationContext ctx,
      Map<String, QueryVertex> vertexMap) {
    String name = getString(edgeT.getSubterm(POS_EDGE_NAME));
    String srcName = getString(edgeT.getSubterm(POS_EDGE_SRC));
    String dstName = getString(edgeT.getSubterm(POS_EDGE_DST));
    Direction direction = getDirection(edgeT.getSubterm(POS_EDGE_DIRECTION));
    IStrategoTerm originPosition = edgeT.getSubterm(POS_EDGE_ORIGIN_OFFSET);

    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);

    QueryEdge edge = name.contains(GENERATED_VAR_SUBSTR) ? new QueryEdge(src, dst, name, true, direction)
        : new QueryEdge(src, dst, name, false, direction);

    ctx.addVar(edge, name, originPosition);
    return edge;
  }

  private static Direction getDirection(IStrategoTerm directionT) {
    String constructorName = getConstructorName(directionT);
    switch (constructorName) {
      case "Outgoing":
        return Direction.OUTGOING;
      case "Incoming":
        return Direction.INCOMING;
      case "Undirected":
        return Direction.ANY;
      default:
        throw new IllegalArgumentException(constructorName);
    }
  }

  private static QueryVertex getQueryVertex(Map<String, QueryVertex> vertexMap, String vertexName) {
    QueryVariable queryVariable = vertexMap.get(vertexName);
    if (queryVariable.getVariableType() == VariableType.VERTEX) {
      return (QueryVertex) queryVariable;
    } else {
      // query has syntactic error and although Spoofax generates a grammatically correct AST, the AST is not
      // semantically correct
      QueryVertex dummyVertex = new QueryVertex(vertexName, false);
      return dummyVertex;
    }
  }

  private static QueryPath getPath(IStrategoTerm pathT, TranslationContext ctx, Map<String, QueryVertex> vertexMap)
      throws PgqlException {

    String pathFindingGoal = ((IStrategoAppl) pathT.getSubterm(POS_PATH_FINDING_GOAL)).getConstructor().getName();
    switch (pathFindingGoal) {
      case "Reaches":
        return getReaches(pathT, ctx, vertexMap);
      case "Shortest":
        return getShortest(pathT, ctx, vertexMap);
      default:
        throw new UnsupportedOperationException(pathFindingGoal);
    }
  }

  private static QueryPath getReaches(IStrategoTerm pathT, TranslationContext ctx, Map<String, QueryVertex> vertexMap)
      throws PgqlException {
    String label = getString(pathT.getSubterm(POS_PATH_EXPRESSION));
    long minHops = getMinHops(pathT);
    long maxHops = getMaxHops(pathT);

    CommonPathExpression commonPathExpression = ctx.getCommonPathExpressions().get(label);

    if (commonPathExpression == null) { // no path expression defined for the label; generate one here
      QueryVertex src = new QueryVertex("n", true);
      QueryVertex dst = new QueryVertex("m", true);
      VertexPairConnection edge = new QueryEdge(src, dst, "e", true, Direction.OUTGOING);

      List<QueryExpression> args = new ArrayList<>();
      args.add(new VarRef(edge));
      args.add(new ConstString(label));
      QueryExpression labelExp = new QueryExpression.FunctionCall("has_label", args);

      List<QueryVertex> vertices = new ArrayList<>();
      vertices.add(src);
      vertices.add(dst);
      List<VertexPairConnection> connections = new ArrayList<>();
      connections.add(edge);
      Set<QueryExpression> constraints = new HashSet<>();
      constraints.add(labelExp);

      commonPathExpression = new CommonPathExpression(label, vertices, connections, constraints);
    }

    String srcName = getString(pathT.getSubterm(POS_PATH_SRC));
    String dstName = getString(pathT.getSubterm(POS_PATH_DST));
    String name = getString(pathT.getSubterm(POS_PATH_NAME));
    Direction direction = getDirection(pathT.getSubterm(POS_PATH_DIRECTION));
    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);
    PathFindingGoal goal = PathFindingGoal.REACHES;
    int kValue = -1;

    QueryPath path = name.contains(GENERATED_VAR_SUBSTR)
        ? new QueryPath(src, dst, name, commonPathExpression, true, minHops, maxHops, goal, kValue, direction)
        : new QueryPath(src, dst, name, commonPathExpression, false, minHops, maxHops, goal, kValue, direction);

    return path;
  }

  private static long getMinHops(IStrategoTerm pathT) throws PgqlException {
    return getMinMaxHops(pathT, true);
  }

  private static long getMaxHops(IStrategoTerm pathT) throws PgqlException {
    return getMinMaxHops(pathT, false);
  }

  private static long getMinMaxHops(IStrategoTerm pathT, boolean min) throws PgqlException {
    IStrategoTerm pathQuantifiersT = pathT.getSubterm(POS_PATH_QUANTIFIERS);
    if (isSome(pathQuantifiersT)) {
      pathQuantifiersT = getSome(pathQuantifiersT);
      int position = min ? POS_PATH_QUANTIFIERS_MIN_HOPS : POS_PATH_QUANTIFIERS_MAX_HOPS;
      return parseLong(pathQuantifiersT.getSubterm(position));
    } else {
      return 1;
    }
  }

  private static QueryPath getShortest(IStrategoTerm pathT, TranslationContext ctx, Map<String, QueryVertex> vertexMap)
      throws PgqlException {
    String srcName = getString(pathT.getSubterm(POS_PATH_SRC));
    String dstName = getString(pathT.getSubterm(POS_PATH_DST));
    long minHops = getMinHops(pathT);
    long maxHops = getMaxHops(pathT);
    String name = getString(pathT.getSubterm(POS_PATH_NAME));
    Direction direction = getDirection(pathT.getSubterm(POS_PATH_DIRECTION));
    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);

    IStrategoTerm pathExpressionT = pathT.getSubterm(POS_PATH_EXPRESSION);
    CommonPathExpression pathExpression = getPathExpression(pathExpressionT, ctx);

    PathFindingGoal goal = PathFindingGoal.SHORTEST;

    int kValue = parseInt(pathT.getSubterm(POS_PATH_K_VALUE));

    QueryPath path = new QueryPath(src, dst, name, pathExpression, true, minHops, maxHops, goal, kValue, direction);

    return path;
  }

  private static void giveAnonymousVariablesUniqueHiddenName(Collection<? extends QueryVariable> variables,
      TranslationContext ctx) {

    for (QueryVariable var : variables) {
      if (var.isAnonymous()) {
        String name = var.getName().replace(GENERATED_VAR_SUBSTR, "anonymous");
        while (ctx.isVariableNameInUse(name)) {
          name += "_2";
        }
        var.setName(name);
      }

      // recurse for regular path expressions
      if (var.getVariableType() == VariableType.PATH) {
        QueryPath path = (QueryPath) var;
        giveAnonymousVariablesUniqueHiddenName(path.getConnections(), ctx);
        giveAnonymousVariablesUniqueHiddenName(path.getVertices(), ctx);
      }
    }
  }

  private static GroupBy getGroupBy(TranslationContext ctx, IStrategoTerm groupByT) throws PgqlException {
    String consName = ((IStrategoAppl) groupByT).getConstructor().getName();
    switch (consName) {
      case "Some": // explicit GROUP BY
        IStrategoTerm groupByElemsT = getList(groupByT);
        return new GroupBy(getExpAsVars(ctx, groupByElemsT));
      case "CreateOneGroup": // implicit GROUP BY (e.g. SELECT has aggregation)
        return new GroupBy(Collections.emptyList());
      case "None": // no GROUP BY
        return null;
      default:
        throw new IllegalArgumentException(consName);
    }
  }

  private static List<ExpAsVar> getExpAsVars(TranslationContext ctx, IStrategoTerm expAsVarsT) throws PgqlException {
    List<ExpAsVar> expAsVars = new ArrayList<>(expAsVarsT.getSubtermCount());
    for (IStrategoTerm expAsVarT : expAsVarsT) {
      QueryExpression exp = translateExp(expAsVarT.getSubterm(POS_EXPASVAR_EXP), ctx);
      String varName = getString(expAsVarT.getSubterm(POS_EXPASVAR_VAR));
      boolean anonymous = ((IStrategoAppl) expAsVarT.getSubterm(POS_EXPASVAR_ANONYMOUS)).getConstructor().getName()
          .equals("Anonymous");
      IStrategoTerm originPosition = expAsVarT.getSubterm(POS_EXPASVAR_ORIGIN_OFFSET);

      ExpAsVar expAsVar = new ExpAsVar(exp, varName, anonymous);
      expAsVars.add(expAsVar);
      ctx.addVar(expAsVar, varName, originPosition);
    }
    return expAsVars;
  }

  private static List<OrderByElem> getOrderByElems(TranslationContext ctx, IStrategoTerm orderByT)
      throws PgqlException {
    List<OrderByElem> orderByElems = new ArrayList<>();
    if (!isNone(orderByT)) { // has ORDER BY
      IStrategoTerm orderByElemsT = getList(orderByT);
      for (IStrategoTerm orderByElemT : orderByElemsT) {
        QueryExpression exp = translateExp(orderByElemT.getSubterm(POS_ORDERBY_EXP), ctx);
        boolean ascending = ((IStrategoAppl) orderByElemT.getSubterm(POS_ORDERBY_ORDERING)).getConstructor().getName()
            .equals("Asc");
        orderByElems.add(new OrderByElem(exp, ascending));
      }
    }
    return orderByElems;
  }

  private static QueryExpression translateExp(IStrategoTerm t, TranslationContext ctx) throws PgqlException {

    String cons = ((IStrategoAppl) t).getConstructor().getName();

    switch (cons) {
      case "Sub":
        QueryExpression exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        QueryExpression exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Sub(exp1, exp2);
      case "Add":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Add(exp1, exp2);
      case "Mul":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Mul(exp1, exp2);
      case "Div":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Div(exp1, exp2);
      case "Mod":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Mod(exp1, exp2);
      case "UMin":
        QueryExpression exp = translateExp(t.getSubterm(POS_UNARY_EXP), ctx);
        return new QueryExpression.ArithmeticExpression.UMin(exp);
      case "And":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.LogicalExpression.And(exp1, exp2);
      case "Or":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.LogicalExpression.Or(exp1, exp2);
      case "Not":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), ctx);
        return new QueryExpression.LogicalExpression.Not(exp);
      case "Eq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.Equal(exp1, exp2);
      case "Neq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.NotEqual(exp1, exp2);
      case "Gt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.Greater(exp1, exp2);
      case "Gte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.GreaterEqual(exp1, exp2);
      case "Lt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.Less(exp1, exp2);
      case "Lte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.LessEqual(exp1, exp2);
      case "Integer":
        long l = parseLong(t);
        return new QueryExpression.Constant.ConstInteger(l);
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
      case "Date":
        s = getString(t);
        LocalDate date;
        try {
          date = LocalDate.parse(s, SqlDateTimeFormatter.SQL_DATE);
        } catch (DateTimeParseException e) {
          // can return any date here; parser already generated an error message for it
          date = LocalDate.MIN;
        }
        return new QueryExpression.Constant.ConstDate(date);
      case "Time":
        s = getString(t);
        try {
          LocalTime time = LocalTime.parse(s, SqlDateTimeFormatter.SQL_TIME);
          return new QueryExpression.Constant.ConstTime(time);
        } catch (DateTimeParseException e) {
          try {
            OffsetTime timeWithTimezone = OffsetTime.parse(s, SqlDateTimeFormatter.SQL_TIME_WITH_TIMEZONE);
            return new QueryExpression.Constant.ConstTimeWithTimezone(timeWithTimezone);
          } catch (DateTimeParseException e2) {
            // can return any time here; parser already generated an error message for it
            return new QueryExpression.Constant.ConstTime(LocalTime.MIN);
          }
        }
      case "Timestamp":
        s = getString(t);
        try {
          LocalDateTime timestamp = LocalDateTime.parse(s, SqlDateTimeFormatter.SQL_TIMESTAMP);
          return new QueryExpression.Constant.ConstTimestamp(timestamp);
        } catch (DateTimeParseException e) {
          try {
            OffsetDateTime timestampWithTimezone = OffsetDateTime.parse(s,
                SqlDateTimeFormatter.SQL_TIMESTAMP_WITH_TIMEZONE);
            return new QueryExpression.Constant.ConstTimestampWithTimezone(timestampWithTimezone);
          } catch (DateTimeParseException e2) {
            // can return any timestamp here; parser already generated an error message for it
            return new QueryExpression.Constant.ConstTimestamp(LocalDateTime.MIN);
          }
        }
      case "VarRef":
        String varName = getString(t.getSubterm(POS_VARREF_VARNAME));
        IStrategoTerm originPosition = null;
        if (t.getSubtermCount() > 1) {
          originPosition = t.getSubterm(POS_VARREF_ORIGIN_OFFSET);
        }
        QueryVariable var = getVariable(ctx, originPosition, varName);
        return new QueryExpression.VarRef(var);
      case "BindVariable":
        int parameterIndex = getInt(t);
        return new QueryExpression.BindVariable(parameterIndex);
      case "PropRef":
        IStrategoTerm varRefT = t.getSubterm(POS_PROPREF_VARREF);
        VarRef varRef = (VarRef) translateExp(varRefT, ctx);
        String propname = getString(t.getSubterm(POS_PROPREF_PROPNAME));
        return new QueryExpression.PropertyAccess(varRef.getVariable(), propname);
      case "Cast":
        exp = translateExp(t.getSubterm(POS_CAST_EXP), ctx);
        String targetTypeName = getString(t.getSubterm(POS_CAST_TARGET_TYPE_NAME));
        return new QueryExpression.Function.Cast(exp, targetTypeName);
      case "Exists":
        IStrategoTerm subqueryT = t.getSubterm(POS_EXISTS_SUBQUERY);
        SelectQuery selectQuery = translateSubquery(ctx, subqueryT);
        return new QueryExpression.Function.Exists(selectQuery);
      case "ScalarSubquery":
        subqueryT = t.getSubterm(POS_SCALARSUBQUERY_SUBQUERY);
        selectQuery = translateSubquery(ctx, subqueryT);
        return new ScalarSubquery(selectQuery);
      case "CallStatement":
      case "FunctionCall":
        IStrategoTerm packageDeclT = t.getSubterm(POS_FUNCTION_CALL_PACKAGE_NAME);
        String packageName = isNone(packageDeclT) ? null : getString(packageDeclT);
        String functionName = getString(t.getSubterm(POS_FUNCTION_CALL_ROUTINE_NAME));
        IStrategoTerm argsT = getList(t.getSubterm(POS_FUNCTION_CALL_EXPS));
        List<QueryExpression> args = varArgsToExps(ctx, argsT);
        return new QueryExpression.FunctionCall(packageName, functionName, args);
      case "ExtractExp":
        IStrategoAppl fieldT = (IStrategoAppl) t.getSubterm(POS_EXTRACT_FIELD);
        ExtractField field;
        switch (fieldT.getConstructor().getName()) {
          case "Year":
            field = ExtractField.YEAR;
            break;
          case "Month":
            field = ExtractField.MONTH;
            break;
          case "Day":
            field = ExtractField.DAY;
            break;
          case "Hour":
            field = ExtractField.HOUR;
            break;
          case "Minute":
            field = ExtractField.MINUTE;
            break;
          case "Second":
            field = ExtractField.SECOND;
            break;
          case "TimezoneHour":
            field = ExtractField.TIMEZONE_HOUR;
            break;
          case "TimezoneMinute":
            field = ExtractField.TIMEZONE_MINUTE;
            break;
          default:
            throw new IllegalArgumentException();
        }
        IStrategoTerm expT = t.getSubterm(POS_EXTRACT_EXP);
        exp = translateExp(expT, ctx);
        return new ExtractExpression(field, exp);
      case "InPredicate":
        expT = t.getSubterm(POS_IN_PREDICATE_EXP);
        exp = translateExp(expT, ctx);
        IStrategoTerm inValueListT = t.getSubterm(POS_IN_PREDICATE_VALUES);
        QueryExpression inValueList = translateExp(inValueListT, ctx);
        return new InPredicate(exp, inValueList);
      case "IsNull":
        expT = t.getSubterm(POS_IS_NULL_EXP);
        exp = translateExp(expT, ctx);
        return new IsNull(exp);
      case "Array":
        IStrategoTerm arrayValues = t.getSubterm(0);
        int size = arrayValues.getSubtermCount();

        long[] integerValues = new long[size];
        double[] decimalValues = new double[size];
        boolean[] booleanValues = new boolean[size];
        String[] stringValues = new String[size];
        LocalDate[] dateValues = new LocalDate[size];
        LocalTime[] timeValues = new LocalTime[size];
        LocalDateTime[] timestampValues = new LocalDateTime[size];

        ExpressionType arrayElementType = null;

        for (int i = 0; i < size; i++) {
          QueryExpression literal = translateExp(arrayValues.getSubterm(i), ctx);
          switch (literal.getExpType()) {
            case INTEGER:
              if (arrayElementType == null) {
                arrayElementType = ExpressionType.INTEGER;
              }
              long integerValue = ((ConstInteger) literal).getValue();
              integerValues[i] = integerValue;
              decimalValues[i] = (double) integerValue;
              break;
            case DECIMAL:
              arrayElementType = ExpressionType.DECIMAL;
              decimalValues[i] = ((ConstDecimal) literal).getValue();
              break;
            case BOOLEAN:
              arrayElementType = ExpressionType.BOOLEAN;
              booleanValues[i] = ((ConstBoolean) literal).getValue();
              break;
            case STRING:
              arrayElementType = ExpressionType.STRING;
              stringValues[i] = ((ConstString) literal).getValue();
              break;
            case DATE:
              arrayElementType = ExpressionType.DATE;
              dateValues[i] = ((ConstDate) literal).getValue();
              break;
            case TIME:
              arrayElementType = ExpressionType.TIME;
              timeValues[i] = ((ConstTime) literal).getValue();
              break;
            case TIMESTAMP:
              arrayElementType = ExpressionType.TIMESTAMP;
              timestampValues[i] = ((ConstTimestamp) literal).getValue();
              break;
            case TIME_WITH_TIMEZONE:
              arrayElementType = ExpressionType.TIME;
              timeValues[i] = ((ConstTimeWithTimezone) literal).getValue().withOffsetSameInstant(ZoneOffset.UTC)
                  .toLocalTime();
              break;
            case TIMESTAMP_WITH_TIMEZONE:
              arrayElementType = ExpressionType.TIMESTAMP;
              timestampValues[i] = ((ConstTimestampWithTimezone) literal).getValue()
                  .withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
              break;
            default:
              throw new IllegalArgumentException(literal.getExpType().toString());
          }
        }

        switch (arrayElementType) {
          case INTEGER:
            return new InValueList(integerValues);
          case DECIMAL:
            return new InValueList(decimalValues);
          case BOOLEAN:
            return new InValueList(booleanValues);
          case STRING:
            return new InValueList(stringValues);
          case DATE:
            return new InValueList(dateValues);
          case TIME:
            return new InValueList(timeValues);
          case TIMESTAMP:
            return new InValueList(timestampValues);
          default:
            throw new IllegalArgumentException(arrayElementType.toString());
        }
      case "COUNT":
      case "MIN":
      case "MAX":
      case "SUM":
      case "AVG":
      case "ARRAY-AGG":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), ctx);
        boolean distinct = aggregationHasDistinct(t);
        switch (cons) {
          case "COUNT":
            return new QueryExpression.Aggregation.AggrCount(distinct, exp);
          case "MIN":
            return new QueryExpression.Aggregation.AggrMin(distinct, exp);
          case "MAX":
            return new QueryExpression.Aggregation.AggrMax(distinct, exp);
          case "SUM":
            return new QueryExpression.Aggregation.AggrSum(distinct, exp);
          case "AVG":
            return new QueryExpression.Aggregation.AggrAvg(distinct, exp);
          case "ARRAY-AGG":
            return new QueryExpression.Aggregation.AggrArrayAgg(distinct, exp);
          default:
            throw new IllegalArgumentException(cons);
        }
      case "Star":
        return new QueryExpression.Star();
      default:
        throw new UnsupportedOperationException("Expression unsupported: " + t);
    }
  }

  private static SelectQuery translateSubquery(TranslationContext ctx, IStrategoTerm t) throws PgqlException {
    IStrategoTerm subqueryT = t.getSubterm(POS_SUBQUERY);
    GraphQuery query = translate(subqueryT, ctx);
    if (query.getQueryType() == QueryType.SELECT) {
      return (SelectQuery) query;
    } else {
      return null; // error recovery (translation should succeed even for syntactically invalid queries)
    }
  }

  private static boolean aggregationHasDistinct(IStrategoTerm t) {
    return isSome(t.getSubterm(POS_AGGREGATE_DISTINCT));
  }

  private static QueryVariable getVariable(TranslationContext ctx, IStrategoTerm originPosition, String varName) {
    if (originPosition == null) {
      // dangling reference
      return new QueryVertex(varName, false);
    }

    return ctx.getVariable(originPosition);
  }

  private static List<QueryExpression> varArgsToExps(TranslationContext ctx, IStrategoTerm expsT) throws PgqlException {
    List<QueryExpression> exps = new ArrayList<>();
    for (IStrategoTerm expT : expsT) {
      exps.add(translateExp(expT, ctx));
    }
    return exps;
  }

  // helper method
  private static long parseLong(IStrategoTerm t) throws PgqlException {
    try {
      return Long.parseLong(getString(t));
    } catch (NumberFormatException e) {
      throw new PgqlException(getString(t) + " is too large to be stored as long");
    }
  }

  // helper method
  private static int parseInt(IStrategoTerm t) throws PgqlException {
    try {
      return Integer.parseInt(getString(t));
    } catch (NumberFormatException e) {
      throw new PgqlException(getString(t) + " is too large to be stored as int");
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
  private static IStrategoTerm getSome(IStrategoTerm t) {
    return t.getSubterm(0);
  }

  // helper method
  private static String getConstructorName(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName();
  }
}
