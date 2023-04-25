/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.DerivedTable;
import oracle.pgql.lang.ir.Direction;
import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.GroupBy;
import oracle.pgql.lang.ir.OrderBy;
import oracle.pgql.lang.ir.OrderByElem;
import oracle.pgql.lang.ir.PathFindingGoal;
import oracle.pgql.lang.ir.PathMode;
import oracle.pgql.lang.ir.Projection;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.AllProperties;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.And;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.TableExpression;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.ir.modify.DeleteClause;
import oracle.pgql.lang.ir.modify.EdgeInsertion;
import oracle.pgql.lang.ir.modify.InsertClause;
import oracle.pgql.lang.ir.modify.Insertion;
import oracle.pgql.lang.ir.modify.Modification;
import oracle.pgql.lang.ir.modify.ModifyQuery;
import oracle.pgql.lang.ir.modify.SetPropertyExpression;
import oracle.pgql.lang.ir.modify.Update;
import oracle.pgql.lang.ir.modify.UpdateClause;
import oracle.pgql.lang.ir.modify.VertexInsertion;
import oracle.pgql.lang.ir.unnest.OneRowPerEdge;
import oracle.pgql.lang.ir.unnest.OneRowPerMatch;
import oracle.pgql.lang.ir.unnest.OneRowPerStep;
import oracle.pgql.lang.ir.unnest.OneRowPerVertex;
import oracle.pgql.lang.ir.unnest.RowsPerMatch;

import static oracle.pgql.lang.TranslateCreateExternalSchema.translateCreateExternalSchema;
import static oracle.pgql.lang.TranslateDropExternalSchema.translateDropExternalSchema;
import static oracle.pgql.lang.TranslateCreatePropertyGraph.translateCreatePropertyGraph;
import static oracle.pgql.lang.TranslateDropPropertyGraph.translateDropPropertyGraph;
import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.getList;
import static oracle.pgql.lang.CommonTranslationUtil.getSchemaQualifiedName;
import static oracle.pgql.lang.CommonTranslationUtil.isNone;
import static oracle.pgql.lang.CommonTranslationUtil.isSome;
import static oracle.pgql.lang.CommonTranslationUtil.getSomeValue;
import static oracle.pgql.lang.CommonTranslationUtil.getConstructorName;
import static oracle.pgql.lang.CommonTranslationUtil.getVariable;
import static oracle.pgql.lang.CommonTranslationUtil.translateExp;
import static oracle.pgql.lang.CommonTranslationUtil.parseLong;
import static oracle.pgql.lang.CommonTranslationUtil.parseInt;

public class SpoofaxAstToGraphQuery {

  private static final String GENERATED_VAR_SUBSTR = "<<anonymous>>";
  private static final String ALL_PROPERTIES_CONSTRUCTOR = "AllProperties";

  private static final int POS_COMMON_PATH_EXPRESSIONS = 0;
  private static final int POS_SELECT_OR_MODIFY = 1;
  private static final int POS_GRAPH_NAME = 2;
  private static final int POS_TABLE_EXPRESSIONS = 3;
  @SuppressWarnings("unused")
  private static final int POS_NON_PUSHED_DOWN_PREDICATES = 4;
  private static final int POS_GROUPBY = 5;
  private static final int POS_HAVING = 6;
  private static final int POS_ORDERBY = 7;
  private static final int POS_LIMITOFFSET = 8;

  private static final int POT_GRAPH_NAME_NAME = 0;

  private static final int POS_COMMON_PATH_EXPRESSION_NAME = 0;
  private static final int POS_COMMON_PATH_EXPRESSION_VERTICES = 1;
  private static final int POS_COMMON_PATH_EXPRESSION_CONNECTIONS = 2;
  private static final int POS_COMMON_PATH_EXPRESSION_CONSTRAINTS = 3;
  private static final int POS_COMMON_PATH_EXPRESSION_COST = 4;

  private static final int POS_COST_EXP = 0;

  private static final int POS_PROJECTION_DISTINCT = 0;
  private static final int POS_PROJECTION_ELEMS = 1;

  private static final int POS_MODIFY_MODIFICATIONS = 0;

  private static final int POS_INSERT_CLAUSE_INTO_CLAUSE = 0;
  private static final int POS_INSERT_CLAUSE_INSERTIONS = 1;
  private static final int POS_INSERT_CLAUSE_INTO_CLAUSE_GRAPH_NAME = 0;
  private static final int POS_VERTEX_INSERTION_NAME = 0;
  private static final int POS_VERTEX_INSERTION_ORIGIN_OFFSET = 1;
  private static final int POS_VERTEX_INSERTION_LABELS = 2;
  private static final int POS_VERTEX_INSERTION_PROPERTIES = 3;
  private static final int POS_EDGE_INSERTION_NAME = 0;
  private static final int POS_EDGE_INSERTION_ORIGIN_OFFSET = 1;
  private static final int POS_EDGE_INSERTION_SRC = 2;
  private static final int POS_EDGE_INSERTION_DST = 3;
  private static final int POS_EDGE_INSERTION_LABELS = 4;
  private static final int POS_EDGE_INSERTION_PROPERTIES = 5;
  private static final int POS_LABELS_LIST = 0;

  private static final int POS_UPDATE_CLAUSE_ELEMENTS = 0;
  private static final int POS_UPDATE_NAME = 0;
  private static final int POS_UPDATE_SET_PROPERTIES = 1;
  private static final int POS_SET_PROPERTIES_EXPRESSIONS = 0;
  private static final int POS_SET_PROPERTY_EXPRESSION_PROPERTY_ACCESS = 0;
  private static final int POS_SET_PROPERTY_EXPRESSION_VALUE_EXPRESSION = 1;

  private static final int POS_DELETION_ELEMENTS = 0;

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
  private static final int POS_PATH_LABEL_EXPRESSION = 2;
  private static final int POS_PATH_LABEL_EXPRESSION_LABEL = 1;
  private static final int POS_PATH_QUANTIFIERS = 3;
  private static final int POS_PATH_QUANTIFIERS_MIN_HOPS = 0;
  private static final int POS_PATH_QUANTIFIERS_MAX_HOPS = 1;
  private static final int POS_PATH_NAME = 4;
  private static final int POS_PATH_DIRECTION = 5;
  private static final int POS_PATH_FINDING_GOAL = 6;
  private static final int POS_PATH_TOP_K_ANY_ALL = 7;
  private static final int POS_PATH_PATH_MODE = 8;
  private static final int POS_PATH_ROWS_PER_MATCH = 9;

  private static final int POS_ONE_ROW_PER_VERTEX_VERTEX = 0;
  private static final int POS_ONE_ROW_PER_EDGE_EDGE = 0;
  private static final int POS_ONE_ROW_PER_STEP_VERTEX_1 = 0;
  private static final int POS_ONE_ROW_PER_STEP_EDGE = 1;
  private static final int POS_ONE_ROW_PER_STEP_VERTEX_2 = 2;
  private static final int POS_ROWS_PER_MATCH_VARIABLE_NAME = 0;
  private static final int POS_ROWS_PER_MATCH_VARIABLE_ORIGIN_OFFSET = 1;

  private static final int POS_ORDERBY_EXP = 0;
  private static final int POS_ORDERBY_ORDERING = 1;
  private static final int POS_LIMIT = 0;
  private static final int POS_OFFSET = 1;

  private static final int POS_EXPASVAR_EXP = 0;
  private static final int POS_EXPASVAR_VAR = 1;
  private static final int POS_EXPASVAR_ANONYMOUS = 2;
  private static final int POS_EXPASVAR_ORIGIN_OFFSET = 3;

  private static final int IDENTIFIER_NAME = 0;
  private static final int IDENTIFIER_ORIGINNAME = 1;

  private static final int POS_EXP_PLUS_TYPE_EXP = 0;

  private static final int POS_ALLPROPERTIES_VARREF = 0;
  private static final int POS_ALLPROPERTIES_PREFIX = 1;

  private static final int POS_DERIVED_TABLE_LATERAL = 0;
  private static final int POS_DERIVED_TABLE_SUBQUERY = 1;
  private static final int POS_SUBQUERY_QUERY = 0;

  public static PgqlStatement translate(IStrategoTerm ast) throws PgqlException {

    String constructorName = ((IStrategoAppl) ast).getConstructor().getName();

    switch (constructorName) {
      case "NormalizedQuery":
        return translate(ast, new TranslationContext(new HashMap<>(), new HashSet<>(), new HashMap<>()));
      case "CreatePropertyGraph":
        return translateCreatePropertyGraph(ast);
      case "DropPropertyGraph":
        return translateDropPropertyGraph(ast);
      case "CreateExternalSchema":
        return translateCreateExternalSchema(ast);
      case "DropExternalSchema":
        return translateDropExternalSchema(ast);
      default:
        return null; // failed to parse query
    }
  }

  /**
   *
   * @param ast
   *          abstract syntax tree returned by Spoofax parser
   * @param vars
   *          map from variable name to variable
   */
  protected static GraphQuery translate(IStrategoTerm ast, TranslationContext ctx) throws PgqlException {

    // path patterns
    IStrategoTerm commonPathExpressionsT = getList(ast.getSubterm(POS_COMMON_PATH_EXPRESSIONS));
    List<CommonPathExpression> commonPathExpressions = getCommonPathExpressions(commonPathExpressionsT, ctx);

    // graph pattern
    IStrategoList tableExpressionsT = (IStrategoList) ast.getSubterm(POS_TABLE_EXPRESSIONS);

    List<TableExpression> tableExpressions = new ArrayList<>();
    Iterator<IStrategoTerm> it = tableExpressionsT.iterator();
    while (it.hasNext()) {
      IStrategoAppl tableExpressionT = (IStrategoAppl) it.next();
      String constructorName = tableExpressionT.getConstructor().getName();
      switch (constructorName) {
        case "GraphPattern":
          GraphPattern graphPattern = translateGraphPattern(ctx, tableExpressionT);
          tableExpressions.add(graphPattern);
          break;
        case "DerivedTable":
          boolean lateral = isSome(tableExpressionT.getSubterm(POS_DERIVED_TABLE_LATERAL));
          SelectQuery query = (SelectQuery) translate(
              tableExpressionT.getSubterm(POS_DERIVED_TABLE_SUBQUERY).getSubterm(POS_SUBQUERY_QUERY), ctx);
          DerivedTable derivedTable = new DerivedTable(query, lateral);
          tableExpressions.add(derivedTable);
          break;
        default:
          throw new IllegalStateException(constructorName + " not supported");
      }
    }

    // GROUP BY
    IStrategoTerm groupByT = ast.getSubterm(POS_GROUPBY);
    GroupBy groupBy = getGroupBy(ctx, groupByT);

    // SELECT or UPDATE or MODIFY
    Projection projection = null;
    IStrategoTerm selectOrModifyT = ast.getSubterm(POS_SELECT_OR_MODIFY);
    String selectOrUpdate = ((IStrategoAppl) selectOrModifyT).getConstructor().getName();
    List<Modification> modifications = null;
    switch (selectOrUpdate) {
      case "SelectClause":
        projection = translateSelectClause(ctx, selectOrModifyT);
        break;
      case "ModifyClause":
        IStrategoTerm modificationsT = selectOrModifyT.getSubterm(POS_MODIFY_MODIFICATIONS);
        modifications = translateModifications(ctx, modificationsT);
        break;
      default:
        throw new IllegalStateException(selectOrUpdate);
    }

    // input graph name
    IStrategoTerm fromT = ast.getSubterm(POS_GRAPH_NAME);
    SchemaQualifiedName graphName = null;
    if (isSome(fromT)) {
      IStrategoTerm graphNameT = getSomeValue(fromT).getSubterm(POT_GRAPH_NAME_NAME);
      graphName = getSchemaQualifiedName(graphNameT);
    }

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
        return new SelectQuery(commonPathExpressions, projection, graphName, tableExpressions, groupBy, having, orderBy,
            limit, offset);
      case "ModifyClause":
        return new ModifyQuery(commonPathExpressions, modifications, graphName, tableExpressions, groupBy, having,
            orderBy, limit, offset);
      default:
        throw new IllegalStateException(selectOrUpdate);
    }
  }

  private static GraphPattern translateGraphPattern(TranslationContext ctx, IStrategoTerm graphPatternT)
      throws PgqlException {
    GraphPattern graphPattern;
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
    graphPattern = new GraphPattern(vertices, connections, constraints);
    return graphPattern;
  }

  private static Projection translateSelectClause(TranslationContext ctx, IStrategoTerm selectOrUpdateT)
      throws PgqlException {
    Projection projection;
    IStrategoTerm distinctT = selectOrUpdateT.getSubterm(POS_PROJECTION_DISTINCT);
    boolean distinct = isSome(distinctT);

    IStrategoTerm projectionElemsT = selectOrUpdateT.getSubterm(POS_PROJECTION_ELEMS);
    List<ExpAsVar> selectElems;
    if (projectionElemsT.getType() == TermType.APPL
        && ((IStrategoAppl) projectionElemsT).getConstructor().getName().equals("Star")) {
      // GROUP BY in combination with SELECT *. Even though the parser will generate
      // an error for it, the
      // translation to GraphQuery should succeed (error recovery)
      selectElems = new ArrayList<>();
    } else {
      projectionElemsT = projectionElemsT.getSubterm(0);
      selectElems = getExpAsVars(ctx, projectionElemsT);
    }
    projection = new Projection(distinct, selectElems);
    return projection;
  }

  private static List<Modification> translateModifications(TranslationContext ctx, IStrategoTerm modificationsT)
      throws PgqlException {
    List<Modification> result = new ArrayList<>();
    for (IStrategoTerm modificationT : modificationsT) {
      String constructorName = ((IStrategoAppl) modificationT).getConstructor().getName();
      switch (constructorName) {
        case "InsertClause": {

          IStrategoTerm optionalIntoClauseT = modificationT.getSubterm(POS_INSERT_CLAUSE_INTO_CLAUSE);
          SchemaQualifiedName graphName = null;
          if (isSome(optionalIntoClauseT)) {
            IStrategoTerm intoClauseT = getSomeValue(optionalIntoClauseT);
            graphName = getSchemaQualifiedName(intoClauseT.getSubterm(POS_INSERT_CLAUSE_INTO_CLAUSE_GRAPH_NAME));
          }

          IStrategoTerm insertionsT = modificationT.getSubterm(POS_INSERT_CLAUSE_INSERTIONS);

          List<Insertion> insertions = new ArrayList<>();
          for (IStrategoTerm insertionT : insertionsT) {
            Insertion insertion = translateInsertion(ctx, insertionT);
            insertions.add(insertion);
          }

          result.add(new InsertClause(graphName, insertions));

          break;
        }
        case "UpdateClause": {
          IStrategoTerm updatesT = modificationT.getSubterm(POS_UPDATE_CLAUSE_ELEMENTS);

          List<Update> updates = new ArrayList<>();
          for (IStrategoTerm updateT : updatesT) {
            IStrategoTerm elementT = updateT.getSubterm(POS_UPDATE_NAME);
            VarRef element = (VarRef) translateExp(elementT, ctx);

            IStrategoTerm setPropertiesT = updateT.getSubterm(POS_UPDATE_SET_PROPERTIES);
            List<SetPropertyExpression> setPropertyExpressions = getSetPropertyExpressions(ctx, setPropertiesT);

            updates.add(new Update(element, setPropertyExpressions));
          }

          result.add(new UpdateClause(updates));

          break;
        }
        case "DeleteClause": {
          IStrategoTerm elementsT = modificationT.getSubterm(POS_DELETION_ELEMENTS);
          List<VarRef> deletions = new ArrayList<>();
          for (IStrategoTerm elementT : elementsT) {
            deletions.add((VarRef) translateExp(elementT, ctx));
          }

          result.add(new DeleteClause(deletions));

          break;
        }
        default:
          throw new IllegalArgumentException(constructorName);
      }
    }
    return result;
  }

  private static Insertion translateInsertion(TranslationContext ctx, IStrategoTerm insertionT) throws PgqlException {
    String constructorName = ((IStrategoAppl) insertionT).getConstructor().getName();
    switch (constructorName) {
      case "VertexInsertion": {
        String vertexName = getString(insertionT.getSubterm(POS_VERTEX_INSERTION_NAME));
        QueryVertex vertex = new QueryVertex(vertexName, false);
        IStrategoTerm originOffset = insertionT.getSubterm(POS_VERTEX_INSERTION_ORIGIN_OFFSET);
        ctx.addVar(vertex, vertexName, originOffset);

        IStrategoTerm labelsT = insertionT.getSubterm(POS_VERTEX_INSERTION_LABELS);
        List<QueryExpression> labels = getLabels(ctx, labelsT);

        IStrategoTerm propertiesT = insertionT.getSubterm(POS_VERTEX_INSERTION_PROPERTIES);
        List<SetPropertyExpression> properties = getProperties(ctx, propertiesT);

        return new VertexInsertion(vertex, labels, properties);
      }
      case "DirectedEdgeInsertion": {
        String edgeName = getString(insertionT.getSubterm(POS_EDGE_INSERTION_NAME));

        IStrategoTerm srcVarRefT = insertionT.getSubterm(POS_EDGE_INSERTION_SRC).getSubterm(POS_EXP_PLUS_TYPE_EXP);
        QueryVertex src = dereferenceQueryVertex(getVariable(ctx, srcVarRefT));

        IStrategoTerm dstVarRefT = insertionT.getSubterm(POS_EDGE_INSERTION_DST).getSubterm(POS_EXP_PLUS_TYPE_EXP);
        QueryVertex dst = dereferenceQueryVertex(getVariable(ctx, dstVarRefT));

        QueryEdge edge = new QueryEdge(src, dst, edgeName, false, Direction.OUTGOING);
        IStrategoTerm originOffset = insertionT.getSubterm(POS_EDGE_INSERTION_ORIGIN_OFFSET);
        ctx.addVar(edge, edgeName, originOffset);

        IStrategoTerm labelsT = insertionT.getSubterm(POS_EDGE_INSERTION_LABELS);
        List<QueryExpression> labels = getLabels(ctx, labelsT);

        IStrategoTerm propertiesT = insertionT.getSubterm(POS_EDGE_INSERTION_PROPERTIES);
        List<SetPropertyExpression> properties = getProperties(ctx, propertiesT);
        return new EdgeInsertion(edge, labels, properties);
      }

      default:
        throw new IllegalArgumentException(constructorName);
    }
  }

  private static QueryVertex dereferenceQueryVertex(QueryVariable var) throws PgqlException {
    switch (var.getVariableType()) {
      case VERTEX:
        return (QueryVertex) var;
      case EXP_AS_VAR:
        QueryExpression exp = ((ExpAsVar) var).getExp();
        if (exp.getExpType() != ExpressionType.VARREF) {
          throw new PgqlException("Not a vertex reference: " + exp);
        }
        QueryVariable nestedVar = ((VarRef) exp).getVariable();
        return dereferenceQueryVertex(nestedVar);
      default:
        throw new PgqlException("Not a vertex reference: " + var);
    }
  }

  private static List<QueryExpression> getLabels(TranslationContext ctx, IStrategoTerm labelsT) throws PgqlException {
    List<QueryExpression> result = new ArrayList<>();
    if (isSome(labelsT)) {
      IStrategoTerm labelsListT = getSomeValue(labelsT).getSubterm(POS_LABELS_LIST);
      for (IStrategoTerm labelT : labelsListT) {
        String label = getString(labelT.getSubterm(IDENTIFIER_NAME));
        result.add(new ConstString(label));
      }
    }
    return result;
  }

  private static List<SetPropertyExpression> getProperties(TranslationContext ctx, IStrategoTerm propertiesT)
      throws PgqlException {
    if (isSome(propertiesT)) {
      return getSetPropertyExpressions(ctx, getSomeValue(propertiesT));
    } else {
      return new ArrayList<>();
    }
  }

  private static List<SetPropertyExpression> getSetPropertyExpressions(TranslationContext ctx,
      IStrategoTerm setPropertiesT)
      throws PgqlException {
    List<SetPropertyExpression> result = new ArrayList<>();

    IStrategoTerm expressionsT = setPropertiesT.getSubterm(POS_SET_PROPERTIES_EXPRESSIONS);
    for (IStrategoTerm expressionT : expressionsT) {
      IStrategoTerm propertyAccessT = expressionT.getSubterm(POS_SET_PROPERTY_EXPRESSION_PROPERTY_ACCESS);
      QueryExpression propertyAccess = translateExp(propertyAccessT, ctx);
      IStrategoTerm valueExpressionT = expressionT.getSubterm(POS_SET_PROPERTY_EXPRESSION_VALUE_EXPRESSION);
      QueryExpression valueExpression = translateExp(valueExpressionT, ctx);
      if (propertyAccess.getExpType() == ExpressionType.PROP_ACCESS) {
        // error recovery: if it is a reference to a group by key it is not a property access
        result.add(new SetPropertyExpression((PropertyAccess) propertyAccess, valueExpression));
      }
    }

    return result;
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

    // COST clause
    IStrategoTerm costT = pathPatternT.getSubterm(POS_COMMON_PATH_EXPRESSION_COST);
    QueryExpression cost;
    if (isNone(costT)) {
      cost = null;
    } else {
      cost = translateExp(getSomeValue(costT).getSubterm(POS_COST_EXP), ctx);
    }

    return new CommonPathExpression(name, vertices, connections, constraints, cost);
  }

  private static List<QueryVertex> getQueryVertices(IStrategoTerm verticesT, TranslationContext ctx) {
    List<QueryVertex> vertices = new ArrayList<>(verticesT.getSubtermCount());
    for (IStrategoTerm vertexT : verticesT) {
      String vertexName = getString(vertexT.getSubterm(POS_VERTEX_NAME));
      IStrategoTerm originOffset = vertexT.getSubterm(POS_VERTEX_ORIGIN_OFFSET);
      boolean anonymous = vertexName.contains(GENERATED_VAR_SUBSTR);

      QueryVertex vertex = new QueryVertex(vertexName, anonymous);
      ctx.addVar(vertex, vertexName, originOffset);
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

      switch (consName) {
        case "Edge":
          result.add(getQueryEdge(connectionT, ctx, vertexMap));
          break;
        case "Path":
          result.add(getPath(connectionT, ctx, vertexMap));
          break;
        case "ComplexRegularExpressionNotSupported":
        case "ComplexParenthesizedRegularExpressionNotSupported":
          // ignore it
          break;
        default:
          throw new IllegalArgumentException(consName);
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
    IStrategoTerm originOffset = edgeT.getSubterm(POS_EDGE_ORIGIN_OFFSET);

    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);

    QueryEdge edge = name.contains(GENERATED_VAR_SUBSTR) ? new QueryEdge(src, dst, name, true, direction)
        : new QueryEdge(src, dst, name, false, direction);

    ctx.addVar(edge, name, originOffset);
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
      // query has syntactic error and although Spoofax generates a grammatically
      // correct AST, the AST is not
      // semantically correct
      QueryVertex dummyVertex = new QueryVertex(vertexName, false);
      return dummyVertex;
    }
  }

  private static QueryPath getPath(IStrategoTerm pathT, TranslationContext ctx, Map<String, QueryVertex> vertexMap)
      throws PgqlException {

    String pathFindingGoal = ((IStrategoAppl) pathT.getSubterm(POS_PATH_FINDING_GOAL)).getConstructor().getName();
    CommonPathExpression commonPathExpression;
    IStrategoAppl pathExpressionT = (IStrategoAppl) pathT.getSubterm(POS_PATH_EXPRESSION);
    if (pathExpressionT.getConstructor().getName().equals("CommonPathExpression")) {
      commonPathExpression = getPathExpression(pathExpressionT, ctx);
    } else {
      commonPathExpression = getCommonPathExpressionFromReaches(pathT, ctx);
    }

    switch (pathFindingGoal) {
      case "Reaches":
        return getQueryPath(pathT, ctx, vertexMap, PathFindingGoal.REACHES, commonPathExpression);
      case "Shortest":
        return getQueryPath(pathT, ctx, vertexMap, PathFindingGoal.SHORTEST, commonPathExpression);
      case "Cheapest":
        return getQueryPath(pathT, ctx, vertexMap, PathFindingGoal.CHEAPEST, commonPathExpression);
      case "All":
        return getQueryPath(pathT, ctx, vertexMap, PathFindingGoal.ALL, commonPathExpression);
      default:
        throw new UnsupportedOperationException(pathFindingGoal);
    }
  }

  private static CommonPathExpression getCommonPathExpressionFromReaches(IStrategoTerm pathT, TranslationContext ctx) {
    CommonPathExpression commonPathExpression;
    String label = getString(pathT.getSubterm(POS_PATH_LABEL_EXPRESSION).getSubterm(POS_PATH_LABEL_EXPRESSION_LABEL));
    commonPathExpression = ctx.getCommonPathExpressions().get(label);
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
    return commonPathExpression;
  }

  private static QueryPath getQueryPath(IStrategoTerm pathT, TranslationContext ctx, Map<String, QueryVertex> vertexMap,
      PathFindingGoal goal, CommonPathExpression commonPathExpression)
      throws PgqlException {
    String srcName = getString(pathT.getSubterm(POS_PATH_SRC));
    String dstName = getString(pathT.getSubterm(POS_PATH_DST));
    long minHops = getMinHops(pathT);
    long maxHops = getMaxHops(pathT);
    String name = getString(pathT.getSubterm(POS_PATH_NAME));
    Direction direction = getDirection(pathT.getSubterm(POS_PATH_DIRECTION));
    QueryVertex src = getQueryVertex(vertexMap, srcName);
    QueryVertex dst = getQueryVertex(vertexMap, dstName);

    int kValue = -1;
    boolean withTies = false;

    switch (goal) {
      case ALL:
        break;
      case REACHES:
        kValue = 1;
        break;
      case SHORTEST:
      case CHEAPEST:
        IStrategoTerm topKAnyAllT = pathT.getSubterm(POS_PATH_TOP_K_ANY_ALL);
        if (isSome(topKAnyAllT)) {
          IStrategoAppl topKAnyAllContent = (IStrategoAppl) getSomeValue(topKAnyAllT);
          switch (topKAnyAllContent.getName()) {
            case "TopK": // SHORTEST k or CHEAPEST k
              kValue = parseInt(topKAnyAllContent.getSubterm(0));
              break;
            case "Any": // ANY SHORTEST or ANY CHEAPEST
              kValue = 1;
              break;
            case "All": // ALL SHORTEST or ALL CHEAPEST
              kValue = 1;
              withTies = true;
              break;
            default:
              throw new IllegalArgumentException(topKAnyAllContent.getName());
          }
        }
        break;
      default:
        throw new UnsupportedOperationException(goal.name());
    }

    PathMode pathMode = getPathMode(pathT.getSubterm(POS_PATH_PATH_MODE));

    RowsPerMatch rowsPerMatch = getRowsPerMatch(pathT.getSubterm(POS_PATH_ROWS_PER_MATCH), ctx);

    QueryPath path = new QueryPath(src, dst, name, commonPathExpression, true, minHops, maxHops, goal, kValue, withTies,
        pathMode, direction, rowsPerMatch);

    return path;
  }

  private static PathMode getPathMode(IStrategoTerm pathModeT) {
    String constructorName = ((IStrategoAppl) pathModeT).getConstructor().getName();
    switch (constructorName) {
      case "Walk":
        return PathMode.WALK;
      case "Trail":
        return PathMode.TRAIL;
      case "Acyclic":
        return PathMode.ACYCLIC;
      case "Simple":
        return PathMode.SIMPLE;
      default:
        throw new IllegalArgumentException(constructorName);
    }
  }

  private static RowsPerMatch getRowsPerMatch(IStrategoTerm optionalRowsPerMatchT, TranslationContext ctx) {
    if (isNone(optionalRowsPerMatchT)) {
      return new OneRowPerMatch();
    }

    IStrategoAppl rowsPerMatchT = (IStrategoAppl) getSomeValue(optionalRowsPerMatchT);
    String constructorName = rowsPerMatchT.getConstructor().getName();
    switch (constructorName) {
      case "OneRowPerMatch":
        return new OneRowPerMatch();
      case "OneRowPerVertex": {
        QueryVertex vertex = getRowsPerMatchVertex(rowsPerMatchT, POS_ONE_ROW_PER_VERTEX_VERTEX, ctx);
        return new OneRowPerVertex(vertex);
      }
      case "OneRowPerEdge": {
        QueryEdge edge = getRowsPerMatchEdge(rowsPerMatchT, POS_ONE_ROW_PER_EDGE_EDGE, ctx);
        return new OneRowPerEdge(edge);
      }
      case "OneRowPerStep": {
        QueryVertex vertex1 = getRowsPerMatchVertex(rowsPerMatchT, POS_ONE_ROW_PER_STEP_VERTEX_1, ctx);
        QueryEdge edge = getRowsPerMatchEdge(rowsPerMatchT, POS_ONE_ROW_PER_STEP_EDGE, ctx);
        QueryVertex vertex2 = getRowsPerMatchVertex(rowsPerMatchT, POS_ONE_ROW_PER_STEP_VERTEX_2, ctx);
        return new OneRowPerStep(vertex1, edge, vertex2);
      }
      default:
        throw new IllegalArgumentException(constructorName);
    }
  }

  private static QueryVertex getRowsPerMatchVertex(IStrategoAppl rowsPerMatchT, int variablePosition,
      TranslationContext ctx) {
    IStrategoTerm vertexVariableT = rowsPerMatchT.getSubterm(variablePosition);
    String vertexName = getString(vertexVariableT.getSubterm(POS_ROWS_PER_MATCH_VARIABLE_NAME));
    QueryVertex vertex = new QueryVertex(vertexName, false);
    IStrategoTerm originOffset = vertexVariableT.getSubterm(POS_ROWS_PER_MATCH_VARIABLE_ORIGIN_OFFSET);
    ctx.addVar(vertex, vertexName, originOffset);
    return vertex;
  }

  private static QueryEdge getRowsPerMatchEdge(IStrategoAppl rowsPerMatchT, int variablePosition,
      TranslationContext ctx) {
    IStrategoTerm edgeVariableT = rowsPerMatchT.getSubterm(variablePosition);
    String edgeName = getString(edgeVariableT.getSubterm(POS_ROWS_PER_MATCH_VARIABLE_NAME));
    QueryEdge edge = new QueryEdge(null, null, edgeName, false, null);
    IStrategoTerm originOffset = edgeVariableT.getSubterm(POS_ROWS_PER_MATCH_VARIABLE_ORIGIN_OFFSET);
    ctx.addVar(edge, edgeName, originOffset);
    return edge;
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
      pathQuantifiersT = getSomeValue(pathQuantifiersT);
      int position = min ? POS_PATH_QUANTIFIERS_MIN_HOPS : POS_PATH_QUANTIFIERS_MAX_HOPS;
      return parseLong(pathQuantifiersT.getSubterm(position));
    } else {
      return 1;
    }
  }

  private static void giveAnonymousVariablesUniqueHiddenName(Collection<? extends QueryVariable> variables,
      TranslationContext ctx) {

    for (QueryVariable var : variables) {
      if (var.getName().contains(GENERATED_VAR_SUBSTR)) {
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
      if (((IStrategoAppl) expAsVarT).getConstructor().getName().equals(ALL_PROPERTIES_CONSTRUCTOR)) {
        VarRef varRef = (VarRef) translateExp(expAsVarT.getSubterm(POS_ALLPROPERTIES_VARREF), ctx);
        String expAsVarName = GENERATED_VAR_SUBSTR + "_" + varRef + ".*"; // this just needs to be some unique name;
                                                                          // doesn't matter what
        IStrategoTerm prefixT = expAsVarT.getSubterm(POS_ALLPROPERTIES_PREFIX);
        String prefix = isSome(prefixT) ? getString(prefixT) : null;
        ExpAsVar expAsVar = new ExpAsVar(new AllProperties(varRef, prefix), expAsVarName, true, expAsVarName);
        expAsVars.add(expAsVar);
        continue;
      }

      QueryExpression exp = translateExp(expAsVarT.getSubterm(POS_EXPASVAR_EXP), ctx);
      IStrategoTerm columnName = expAsVarT.getSubterm(POS_EXPASVAR_VAR);
      String varName = getString(columnName.getSubterm(IDENTIFIER_NAME));
      IStrategoTerm originNameT = columnName.getSubterm(IDENTIFIER_ORIGINNAME);
      String originName = isNone(originNameT) ? null : getString(originNameT);
      boolean anonymous = ((IStrategoAppl) expAsVarT.getSubterm(POS_EXPASVAR_ANONYMOUS)).getConstructor().getName()
          .equals("Anonymous");
      IStrategoTerm originOffset = expAsVarT.getSubterm(POS_EXPASVAR_ORIGIN_OFFSET);

      ExpAsVar expAsVar = new ExpAsVar(exp, varName, anonymous, originName);
      expAsVars.add(expAsVar);
      ctx.addVar(expAsVar, varName, originOffset);
    }
    giveAnonymousVariablesUniqueHiddenName(expAsVars, ctx);
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
}
