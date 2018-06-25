/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
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
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.ExtractExpression;
import oracle.pgql.lang.ir.QueryExpression.ExtractExpression.ExtractField;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.util.SqlDateTimeFormatter;

public class SpoofaxAstToGraphQuery {

  private static final String GENERATED_VAR_SUBSTR = "<<anonymous>>";

  private static final int POS_COMMON_PATH_EXPRESSIONS = 0;
  private static final int POS_PROJECTION = 1;
  private static final int POS_FROM = 2;
  private static final int POS_WHERE = 3;
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
  private static final int POS_PATH_LABEL = 2;
  private static final int POS_PATH_QUANTIFIERS = 3;
  private static final int POS_PATH_QUANTIFIERS_MIN_HOPS = 0;
  private static final int POS_PATH_QUANTIFIERS_MAX_HOPS = 1;
  private static final int POS_PATH_NAME = 4;
  // FIXME: where is 5?
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

    // WHERE
    IStrategoTerm graphPatternT = ast.getSubterm(POS_WHERE);

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

    // SELECT
    IStrategoTerm projectionT = ast.getSubterm(POS_PROJECTION);
    IStrategoTerm distinctT = projectionT.getSubterm(POS_PROJECTION_DISTINCT);
    boolean distinct = isSome(distinctT);

    IStrategoTerm projectionElemsT = projectionT.getSubterm(POS_PROJECTION_ELEMS);
    List<ExpAsVar> selectElems;
    if (projectionElemsT.getTermType() == IStrategoTerm.APPL
        && ((IStrategoAppl) projectionElemsT).getConstructor().getName().equals("Star")) {
      // GROUP BY in combination with SELECT *. Even though the parser will generate an error for it, the translation to
      // GraphQuery should succeed (error recovery)
      selectElems = new ArrayList<>();
    } else {
      projectionElemsT = projectionElemsT.getSubterm(0);
      selectElems = getExpAsVars(ctx, projectionElemsT);
    }
    Projection projection = new Projection(distinct, selectElems);

    // FROM
    IStrategoTerm fromT = ast.getSubterm(POS_FROM);
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

    return new GraphQuery(commonPathExpressions, projection, inputGraphName, graphPattern, groupBy, having, orderBy,
        limit, offset);
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
      CommonPathExpression commonPathExpression = new CommonPathExpression(name, vertices, connections, constraints);
      result.add(commonPathExpression);
      ctx.getCommonPathExpressions().put(name, commonPathExpression);
    }
    return result;
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
    boolean directed = getConstructorName(edgeT.getSubterm(POS_EDGE_DIRECTION)).equals("Undirected") == false;
    IStrategoTerm originPosition = edgeT.getSubterm(POS_EDGE_ORIGIN_OFFSET);

    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);

    QueryEdge edge = name.contains(GENERATED_VAR_SUBSTR) ? new QueryEdge(src, dst, name, true, directed)
        : new QueryEdge(src, dst, name, false, directed);

    ctx.addVar(edge, name, originPosition);
    return edge;
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
    String label = getString(pathT.getSubterm(POS_PATH_LABEL));
    IStrategoTerm pathQuantifiersT = pathT.getSubterm(POS_PATH_QUANTIFIERS);
    long minHops;
    long maxHops;
    if (isSome(pathQuantifiersT)) {
      pathQuantifiersT = getSome(pathQuantifiersT);
      minHops = parseLong(pathQuantifiersT.getSubterm(POS_PATH_QUANTIFIERS_MIN_HOPS));
      maxHops = parseLong(pathQuantifiersT.getSubterm(POS_PATH_QUANTIFIERS_MAX_HOPS));
    } else {
      minHops = 1;
      maxHops = 1;
    }

    CommonPathExpression commonPathExpression = ctx.getCommonPathExpressions().get(label);

    if (commonPathExpression == null) { // no path expression defined for the label; generate one here
      QueryVertex src = new QueryVertex("n", true);
      QueryVertex dst = new QueryVertex("m", true);
      VertexPairConnection edge = new QueryEdge(src, dst, "e", true, true);

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
    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);
    PathFindingGoal goal = PathFindingGoal.REACHES;
    long kValue = -1;

    QueryPath path = name.contains(GENERATED_VAR_SUBSTR)
        ? new QueryPath(src, dst, name, commonPathExpression, true, minHops, maxHops, goal, kValue)
        : new QueryPath(src, dst, name, commonPathExpression, false, minHops, maxHops, goal, kValue);

    return path;
  }

  private static QueryPath getShortest(IStrategoTerm pathT, TranslationContext ctx,
      Map<String, QueryVertex> vertexMap) {
    String srcName = getString(pathT.getSubterm(POS_PATH_SRC));
    String dstName = getString(pathT.getSubterm(POS_PATH_DST));
    String name = "<<anonymous>>dummy";
    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);

    String edgeName = getString(pathT.getSubterm(POS_PATH_LABEL).getSubterm(1));
    IStrategoTerm originPosition = pathT.getSubterm(POS_PATH_LABEL).getSubterm(4);
    QueryVertex v1 = new QueryVertex("generated1", true);
    QueryVertex v2 = new QueryVertex("generated2", true);
    QueryEdge edge = new QueryEdge(v1, v2, edgeName, false, true);

    List<QueryVertex> vertices = new ArrayList<>();
    vertices.add(v1);
    vertices.add(v2);
    List<VertexPairConnection> connections = new ArrayList<>();
    connections.add(edge);
    Set<QueryExpression> constraints = Collections.emptySet();
    ctx.addVar(edge, edgeName, originPosition);
    PathFindingGoal goal = PathFindingGoal.SHORTEST;

    CommonPathExpression pathExpression = new CommonPathExpression("shortest( " + edgeName + "*)", vertices,
        connections, constraints);

    long kValue = Long.parseLong(getString(pathT.getSubterm(POS_PATH_K_VALUE)));

    QueryPath path = new QueryPath(src, dst, name, pathExpression, true, 0, -1, goal, kValue);

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
          OffsetDateTime timestampWithTimezone = OffsetDateTime.parse(s,
              SqlDateTimeFormatter.SQL_TIMESTAMP_WITH_TIMEZONE);
          return new QueryExpression.Constant.ConstTimestampWithTimezone(timestampWithTimezone);
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
        GraphQuery query = translateSubquery(ctx, subqueryT);
        return new QueryExpression.Function.Exists(query);
      case "ScalarSubquery":
        subqueryT = t.getSubterm(POS_SCALARSUBQUERY_SUBQUERY);
        query = translateSubquery(ctx, subqueryT);
        return new ScalarSubquery(query);
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

  private static GraphQuery translateSubquery(TranslationContext ctx, IStrategoTerm t) throws PgqlException {
    IStrategoTerm subqueryT = t.getSubterm(POS_SUBQUERY);
    return translate(subqueryT, ctx);
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
  private static IStrategoTerm getSome(IStrategoTerm t) {
    return t.getSubterm(0);
  }

  // helper method
  private static String getConstructorName(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName();
  }
}
