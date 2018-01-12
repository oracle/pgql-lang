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
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.And;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.util.SqlDateTimeFormatter;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

public class SpoofaxAstToGraphQuery {

  private static final String GENERATED_VAR_SUBSTR = "<<anonymous>>";

  private static final int POS_COMMON_PATH_EXPRESSIONS = 0;
  private static final int POS_PROJECTION = 1;
  private static final int POS_FROM = 2;
  private static final int POS_WHERE = 3;
  private static final int POS_GROUPBY = 4;
  private static final int POS_HAVING = 5; /* unused */
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

  private static final int POS_EDGE_SRC = 0;
  private static final int POS_EDGE_DST = 2;
  private static final int POS_EDGE_NAME = 1;
  private static final int POS_EDGE_DIRECTION = 3;

  private static final int POS_PATH_SRC = 0;
  private static final int POS_PATH_DST = 1;
  private static final int POS_PATH_LABEL = 2;
  private static final int POS_PATH_QUANTIFIERS = 3;
  private static final int POS_PATH_QUANTIFIERS_MIN_HOPS = 0;
  private static final int POS_PATH_QUANTIFIERS_MAX_HOPS = 1;
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
  private static final int POS_AGGREGATE_DISTINCT = 0;
  private static final int POS_AGGREGATE_EXP = 1;
  private static final int POS_PROPREF_VARNAME = 0;
  private static final int POS_PROPREF_PROPNAME = 1;
  private static final int POS_CAST_EXP = 0;
  private static final int POS_CAST_TARGET_TYPE_NAME = 1;
  private static final int POS_EXISTS_SUBQUERY = 0;
  private static final int POS_FUNCTION_CALL_PACKAGE_NAME = 0;
  private static final int POS_FUNCTION_CALL_ROUTINE_NAME = 1;
  private static final int POS_FUNCTION_CALL_EXPS = 2;

  public static GraphQuery translate(IStrategoTerm ast) throws PgqlException {
    return translate(ast, new TranslationContext(new HashMap<>(), Collections.emptyMap(), new HashMap<>()));
  }

  /**
   *
   * @param ast
   *          abstract syntax tree returned by Spoofax parser
   * @param vars
   *          map from variable name to variable
   */
  public static GraphQuery translate(IStrategoTerm ast, TranslationContext ctx) throws PgqlException {

    if (!((IStrategoAppl) ast).getConstructor().getName().equals("NormalizedQuery")) {
      return null; // failed to parse query
    }

    Map<String, QueryVariable> vars = ctx.getInScopeVars();

    // path patterns
    IStrategoTerm commonPathExpressionsT = getList(ast.getSubterm(POS_COMMON_PATH_EXPRESSIONS));
    List<CommonPathExpression> commonPathExpressions = getCommonPathExpressions(commonPathExpressionsT, ctx);

    // WHERE
    IStrategoTerm graphPatternT = ast.getSubterm(POS_WHERE);

    // vertices
    IStrategoTerm verticesT = getList(graphPatternT.getSubterm(POS_VERTICES));
    Set<QueryVertex> vertices = new HashSet<>(getQueryVertices(verticesT, vars));

    // connections
    IStrategoTerm connectionsT = getList(graphPatternT.getSubterm(POS_CONNECTIONS));
    LinkedHashSet<VertexPairConnection> connections = getConnections(connectionsT, ctx);

    // constraints
    IStrategoTerm constraintsT = getList(graphPatternT.getSubterm(POS_CONSTRAINTS));
    LinkedHashSet<QueryExpression> constraints = getQueryExpressions(constraintsT, ctx);

    // graph pattern
    GraphPattern graphPattern = new GraphPattern(vertices, connections, constraints);

    // GROUP BY
    IStrategoTerm groupByT = ast.getSubterm(POS_GROUPBY);
    Map<String, QueryVariable> groupKeys = new HashMap<>(); // map from variable name to variable
    List<ExpAsVar> groupByElems = getGroupByElems(ctx, groupKeys, groupByT);
    GroupBy groupBy = new GroupBy(groupByElems);
    // create one group when there's no GROUP BY but SELECT has at least one aggregation
    boolean createOneGroup = ((IStrategoAppl) groupByT).getConstructor().getName().equals("CreateOneGroup");
    boolean changeScope = groupByElems.size() > 0 || createOneGroup;
    if (changeScope) {
      ctx = new TranslationContext(groupKeys, vars, ctx.getCommonPathExpressions());
    }

    // SELECT
    IStrategoTerm projectionT = ast.getSubterm(POS_PROJECTION);
    IStrategoTerm distinctT = projectionT.getSubterm(POS_PROJECTION_DISTINCT);
    boolean distinct = isSome(distinctT);
    IStrategoTerm projectionElemsT = projectionT.getSubterm(POS_PROJECTION_ELEMS).getSubterm(0);
    List<ExpAsVar> selectElems;
    if (projectionElemsT.getTermType() == IStrategoTerm.APPL
        && ((IStrategoAppl) projectionElemsT).getConstructor().getName().equals("Star")) {
      // SELECT * in combination with GROUP BY
      selectElems = Collections.emptyList();
    } else {
      selectElems = getSelectElems(ctx, getList(projectionElemsT));
    }
    Projection projection = new Projection(distinct, selectElems);

    // FROM
    IStrategoTerm fromT = ast.getSubterm(POS_FROM);
    String inputGraphName = isNone(fromT) ? null : unescapeJava(getString(fromT));

    // ORDER BY
    IStrategoTerm orderByT = ast.getSubterm(POS_ORDERBY);
    List<OrderByElem> orderByElems = getOrderByElems(ctx, orderByT);
    OrderBy orderBy = new OrderBy(orderByElems);

    // LIMIT OFFSET
    IStrategoTerm limitOffsetT = ast.getSubterm(POS_LIMITOFFSET);
    QueryExpression limit = getLimitOrOffset(limitOffsetT.getSubterm(POS_LIMIT));
    QueryExpression offset = getLimitOrOffset(limitOffsetT.getSubterm(POS_OFFSET));

    return new GraphQuery(commonPathExpressions, projection, inputGraphName, graphPattern, groupBy, orderBy, limit,
        offset);
  }

  private static QueryExpression getLimitOrOffset(IStrategoTerm subterm) throws PgqlException {
    if (isNone(subterm)) {
      return null;
    }
    IStrategoAppl expT = (IStrategoAppl) subterm.getSubterm(0).getSubterm(0);
    return translateExp(expT, null);
  }

  private static List<CommonPathExpression> getCommonPathExpressions(IStrategoTerm pathPatternsT,
      TranslationContext ctx)
      throws PgqlException {
    List<CommonPathExpression> result = new ArrayList<>();
    for (IStrategoTerm pathPatternT : pathPatternsT) {
      String name = getString(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_NAME));
      // vertices
      IStrategoTerm verticesT = getList(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_VERTICES));
      Map<String, QueryVariable> varmap = new HashMap<>(); // map from variable name to variable
      List<QueryVertex> vertices = getQueryVertices(verticesT, varmap);

      // connections
      TranslationContext newCtx = new TranslationContext(varmap, null, ctx.getCommonPathExpressions());
      IStrategoTerm connectionsT = getList(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_CONNECTIONS));
      List<VertexPairConnection> connections = new ArrayList<>();
      for (IStrategoTerm connectionT : connectionsT) {
        if (((IStrategoAppl) connectionT).getConstructor().getName().equals("Edge")) {
          connections.add(getQueryEdge(connectionT, varmap));
        } else {
          connections.add(getPath(connectionT, newCtx));
        }
      }

      // constraints
      IStrategoTerm constraintsT = getList(pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_CONSTRAINTS));
      LinkedHashSet<QueryExpression> constraints = getQueryExpressions(constraintsT, newCtx);
      CommonPathExpression commonPathExpression = new CommonPathExpression(name, vertices, connections, constraints);
      result.add(commonPathExpression);
      ctx.getCommonPathExpressions().put(name, commonPathExpression);
    }
    return result;
  }

  private static List<QueryVertex> getQueryVertices(IStrategoTerm verticesT, Map<String, QueryVariable> varmap) {
    List<QueryVertex> vertices = new ArrayList<>(verticesT.getSubtermCount());
    for (IStrategoTerm vertexT : verticesT) {
      String vertexName = getString(vertexT);
      QueryVertex vertex;
      if (varmap.containsKey(vertexName)) {
        vertex = getQueryVertex(varmap, vertexName);
      } else {
        vertex = vertexName.contains(GENERATED_VAR_SUBSTR) ? new QueryVertex(toUniqueName(vertexName, varmap), true)
            : new QueryVertex(vertexName, false);
        varmap.put(vertexName, vertex);
      }
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

  private static LinkedHashSet<VertexPairConnection> getConnections(IStrategoTerm connectionsT, TranslationContext ctx)
      throws PgqlException {

    LinkedHashSet<VertexPairConnection> result = new LinkedHashSet<>();

    for (IStrategoTerm connectionT : connectionsT) {
      String consName = ((IStrategoAppl) connectionT).getConstructor().getName();

      if (consName.equals("Edge")) {
        result.add(getQueryEdge(connectionT, ctx.getInScopeVars()));
      } else {
        assert consName.equals("Path");
        result.add(getPath(connectionT, ctx));
      }
    }

    return result;
  }

  private static QueryEdge getQueryEdge(IStrategoTerm edgeT, Map<String, QueryVariable> varmap) {
    String name = getString(edgeT.getSubterm(POS_EDGE_NAME));
    String srcName = getString(edgeT.getSubterm(POS_EDGE_SRC));
    String dstName = getString(edgeT.getSubterm(POS_EDGE_DST));
    boolean directed = getConstructorName(edgeT.getSubterm(POS_EDGE_DIRECTION)).equals("Undirected") == false;

    QueryVertex src = getQueryVertex(varmap, srcName);
    QueryVertex dst = getQueryVertex(varmap, dstName);

    QueryEdge edge = name.contains(GENERATED_VAR_SUBSTR)
        ? new QueryEdge(src, dst, toUniqueName(name, varmap), true, directed)
        : new QueryEdge(src, dst, name, false, directed);

    varmap.put(name, edge);
    return edge;
  }

  private static QueryVertex getQueryVertex(Map<String, QueryVariable> varmap, String vertexName) {
    QueryVariable queryVariable = varmap.get(vertexName);
    if (queryVariable.getVariableType() == VariableType.VERTEX) {
      return (QueryVertex) queryVariable;
    } else {
      // query has syntactic error and although Spoofax generates a grammatically correct AST, the AST is not
      // semantically correct
      QueryVertex dummyVertex = new QueryVertex(vertexName, false);
      return dummyVertex;
    }
  }

  private static QueryPath getPath(IStrategoTerm pathT, TranslationContext ctx) throws PgqlException {
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
    QueryVertex src = getQueryVertex(ctx.getInScopeVars(), srcName);
    QueryVertex dst = getQueryVertex(ctx.getInScopeVars(), dstName);

    QueryPath path = name.contains(GENERATED_VAR_SUBSTR)
        ? new QueryPath(src, dst, toUniqueName(name, ctx.getInScopeVars()), commonPathExpression, true, minHops,
            maxHops)
        : new QueryPath(src, dst, name, commonPathExpression, false, minHops, maxHops);

    return path;
  }

  private static String toUniqueName(String generatedAnonymousName, Map<String, QueryVariable> varmap) {
    String name = generatedAnonymousName.replace(GENERATED_VAR_SUBSTR, "anonymous");
    while (varmap.containsKey(name)) {
      name += "_2";
    }
    return name;
  }

  private static List<ExpAsVar> getGroupByElems(TranslationContext ctx, Map<String, QueryVariable> outputVars,
      IStrategoTerm groupByT)
      throws PgqlException {
    if (isSome(groupByT)) { // has GROUP BY
      IStrategoTerm groupByElemsT = getList(groupByT);
      return getExpAsVars(ctx, outputVars, groupByElemsT);
    }
    return Collections.emptyList();
  }

  private static List<ExpAsVar> getSelectElems(TranslationContext ctx, IStrategoTerm selectElemsT)
      throws PgqlException {
    Map<String, QueryVariable> outputVars = new HashMap<>();
    List<ExpAsVar> result = getExpAsVars(ctx, outputVars, selectElemsT);
    ctx.getInScopeVars().putAll(outputVars);
    return result;
  }

  private static List<ExpAsVar> getExpAsVars(TranslationContext ctx, Map<String, QueryVariable> outputVars,
      IStrategoTerm expAsVarsT)
      throws PgqlException {
    List<ExpAsVar> expAsVars = new ArrayList<>(expAsVarsT.getSubtermCount());
    for (IStrategoTerm expAsVarT : expAsVarsT) {
      String varName = getString(expAsVarT.getSubterm(POS_EXPASVAR_VAR));
      QueryExpression exp = translateExp(expAsVarT.getSubterm(POS_EXPASVAR_EXP), ctx);

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
          OffsetDateTime timestampWithTimezone = OffsetDateTime.parse(s,
              SqlDateTimeFormatter.SQL_TIMESTAMP_WITH_TIMEZONE);
          return new QueryExpression.Constant.ConstTimestampWithTimezone(timestampWithTimezone);
        }
      case "VarRef":
      case "GroupRef":
      case "SelectOrGroupRef":
      case "VarOrSelectRef":
        String varName = getString(t);
        QueryVariable var = getVariable(ctx.getInScopeVars(), varName);
        return new QueryExpression.VarRef(var);
      case "BindVariable":
        int parameterIndex = getInt(t);
        return new QueryExpression.BindVariable(parameterIndex);
      case "PropRef":
        varName = getString(t.getSubterm(POS_PROPREF_VARNAME));
        var = getVariable(ctx.getInScopeVars(), varName);
        String propname = getString(t.getSubterm(POS_PROPREF_PROPNAME));
        return new QueryExpression.PropertyAccess(var, propname);
      case "Cast":
        exp = translateExp(t.getSubterm(POS_CAST_EXP), ctx);
        String targetTypeName = getString(t.getSubterm(POS_CAST_TARGET_TYPE_NAME));
        return new QueryExpression.Function.Cast(exp, targetTypeName);
      case "Exists":
        IStrategoTerm subqueryT = t.getSubterm(POS_EXISTS_SUBQUERY);
        Map<String, QueryVariable> inScopeVars = new HashMap<>(ctx.getInScopeVars());
        Map<String, QueryVariable> inScopeInAggregationVars = ctx.getInScopeInAggregationVars() == null ? null
            : new HashMap<>(ctx.getInScopeInAggregationVars());
        Map<String, CommonPathExpression> commonPathExpressions = new HashMap<>(ctx.getCommonPathExpressions());
        TranslationContext newCtx = new TranslationContext(inScopeVars, inScopeInAggregationVars,
            commonPathExpressions);
        GraphQuery subquery = translate(subqueryT, newCtx);
        return new QueryExpression.Function.Exists(subquery);
      case "CallStatement":
      case "FunctionCall":
        IStrategoTerm packageDeclT = t.getSubterm(POS_FUNCTION_CALL_PACKAGE_NAME);
        String packageName = isNone(packageDeclT) ? null : getString(packageDeclT);
        String functionName = getString(t.getSubterm(POS_FUNCTION_CALL_ROUTINE_NAME));
        IStrategoTerm argsT = getList(t.getSubterm(POS_FUNCTION_CALL_EXPS));
        List<QueryExpression> args = varArgsToExps(ctx, argsT);
        return new QueryExpression.FunctionCall(packageName, functionName, args);
      case "COUNT":
      case "MIN":
      case "MAX":
      case "SUM":
      case "AVG":
        newCtx = new TranslationContext(ctx.getInScopeInAggregationVars(), null, ctx.getCommonPathExpressions());
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), newCtx);
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
          default:
            throw new IllegalStateException();
        }

      case "Star":
        return new QueryExpression.Star();
      default:
        throw new UnsupportedOperationException("Expression unsupported: " + t);
    }
  }

  private static boolean aggregationHasDistinct(IStrategoTerm t) {
    return isSome(t.getSubterm(POS_AGGREGATE_DISTINCT));
  }

  private static QueryVariable getVariable(Map<String, QueryVariable> inScopeVars, String varName) {
    QueryVariable var = inScopeVars.get(varName);
    if (var == null) {
      // dangling reference
      var = new QueryVertex(varName, false);
    }
    return var;
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
