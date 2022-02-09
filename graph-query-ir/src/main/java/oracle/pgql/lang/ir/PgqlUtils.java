/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import oracle.pgql.lang.util.AbstractQueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression.Aggregation;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrArrayAgg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrAvg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrCount;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrListagg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMax;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMin;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrSum;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Or;
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.modify.ModifyQuery;
import oracle.pgql.lang.ir.unnest.OneRowPerEdge;
import oracle.pgql.lang.ir.unnest.OneRowPerStep;
import oracle.pgql.lang.ir.unnest.OneRowPerVertex;
import oracle.pgql.lang.ir.unnest.RowsPerMatch;

public class PgqlUtils {

  private final static Pattern ALL_UPPERCASED_IDENTIFIER = Pattern.compile("^[A-Z][A-Z0-9_]*$");

  public final static String GENERATED_VAR_PREFIX = "<<anonymous>>_";

  // make sure to keep in sync with list of reserved words in pgql-spoofax/syntax/Names.sdf3
  private final static Set<String> RESERVED_WORDS = new HashSet<>(
      Arrays.asList("true", "false", "null", "not", "distinct"));

  static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

  static {
    DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(true);
    DECIMAL_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
  }

  /**
   * @param exp
   *          a query expression
   * @return the set of variables used in the query expression
   */
  public static Set<QueryVariable> getVariables(QueryExpression exp) {
    final Set<QueryVariable> result = new HashSet<>();
    exp.accept(new AbstractQueryExpressionVisitor() {

      @Override
      public void visit(VarRef varRef) {
        result.add(varRef.getVariable());
      }

      @Override
      public void visit(PropertyAccess propAccess) {
        result.add(propAccess.getVariable());
      }

      @Override
      public void visit(QueryVertex queryVertex) {
        result.add(queryVertex);
      }

      @Override
      public void visit(QueryEdge queryEdge) {
        result.add(queryEdge);
      }

      @Override
      public void visit(QueryPath queryPath) {
        result.add(queryPath);
        RowsPerMatch rowsPerMatch = queryPath.getRowsPerMatch();
        switch (queryPath.getRowsPerMatch().getRowsPerMatchType()) {
          case ONE_ROW_PER_VERTEX:
            result.add(((OneRowPerVertex) rowsPerMatch).getVertex());
            break;
          case ONE_ROW_PER_EDGE:
            result.add(((OneRowPerEdge) rowsPerMatch).getEdge());
            break;
          case ONE_ROW_PER_MATCH:
            break;
          case ONE_ROW_PER_STEP:
            OneRowPerStep oneRowPerStep = (OneRowPerStep) rowsPerMatch;
            result.add(oneRowPerStep.getVertex1());
            result.add(oneRowPerStep.getEdge());
            result.add(oneRowPerStep.getVertex2());
            break;
          default:
            throw new UnsupportedOperationException(rowsPerMatch.getRowsPerMatchType() + " not supported");
        }
      }
    });
    return result;
  }

  public static Set<Aggregation> getAggregations(QueryExpression exp) {
    final Set<Aggregation> result = new HashSet<>();
    exp.accept(new AbstractQueryExpressionVisitor() {

      @Override
      public void visit(AggrCount aggrCount) {
        result.add(aggrCount);
      }

      @Override
      public void visit(AggrListagg aggrListagg) {
        result.add(aggrListagg);
      }

      @Override
      public void visit(AggrMin aggrMin) {
        result.add(aggrMin);
      }

      @Override
      public void visit(AggrMax aggrMax) {
        result.add(aggrMax);
      }

      @Override
      public void visit(AggrSum aggrSum) {
        result.add(aggrSum);
      }

      @Override
      public void visit(AggrAvg aggrAvg) {
        result.add(aggrAvg);
      }

      @Override
      public void visit(AggrArrayAgg aggrArrayAgg) {
        result.add(aggrArrayAgg);
      }

      @Override
      public void visit(Exists exists) {
        // don't visit EXISTS subqueries
      }

      @Override
      public void visit(ScalarSubquery subquery) {
        // don't visit scalar subqueries
      }
    });
    return result;
  }

  public static String printConnectionWithSrcAndDst(VertexPairConnection connection) {
    return connection.getSrc() + " " + connection + " " + connection.getDst();
  }

  public static String printReverseConnectionWithSrcAndDst(VertexPairConnection connection) {
    QueryVertex dst = connection.getDst();
    QueryVertex src = connection.getSrc();
    Set<QueryExpression> constraints = Collections.emptySet();
    return dst + " " + printConnection(dst, connection, constraints) + " " + src;
  }

  // HELPER METHODS FOR PRETTY-PRINTING BELOW

  public static String printIdentifier(String identifier) {
    return "\"" + identifier.replace("\"", "\"\"") + "\"";
  }

  public static String printIdentifier(String identifier, boolean alwaysQuote) {
    String lowerCasedIdentifier = identifier.toLowerCase();
    if (!alwaysQuote && ALL_UPPERCASED_IDENTIFIER.matcher(identifier).matches()
        && !RESERVED_WORDS.contains(lowerCasedIdentifier)) {
      // we don't double-quote all-uppercased identifiers, and also make them all-lowercase, to make the pretty-printed
      // queries easier to read
      return lowerCasedIdentifier;
    } else {
      return printIdentifier(identifier);
    }
  }

  protected static String printPgqlString(GraphQuery graphQuery) {
    String result = printPathPatterns(graphQuery.getCommonPathExpressions());

    switch (graphQuery.getQueryType()) {
      case SELECT:
        result += ((SelectQuery) graphQuery).getProjection();
        break;
      case MODIFY:
        ModifyQuery modifyQuery = (ModifyQuery) graphQuery;
        result += modifyQuery.getModifications().stream() //
            .map(x -> x.toString()) //
            .collect(Collectors.joining("\n"));
        break;
      default:
        throw new IllegalArgumentException(graphQuery.getQueryType().toString());
    }

    if (graphQuery.getGraphPattern() != null) {
      result += "\nFROM ";
      result += printPgqlString(graphQuery.getGraphPattern(), graphQuery.getGraphName());
    }
    GroupBy groupBy = graphQuery.getGroupBy();
    if (groupBy != null && groupBy.getElements().isEmpty() == false) {
      result += "\n" + groupBy;
    }
    QueryExpression having = graphQuery.getHaving();
    if (having != null) {
      result += "\nHAVING " + having;
    }
    OrderBy orderBy = graphQuery.getOrderBy();
    if (orderBy.getElements().isEmpty() == false) {
      result += "\n" + orderBy;
    }
    QueryExpression limit = graphQuery.getLimit();
    if (limit != null) {
      result += "\nLIMIT " + limit;
    }
    QueryExpression offset = graphQuery.getOffset();
    if (offset != null) {
      result += "\nOFFSET " + offset;
    }
    return result;
  }

  protected static String printPgqlString(Projection projection) {
    if (projection.getElements().isEmpty()) {
      return "SELECT *";
    } else {
      return "SELECT " + (projection.isDistinct() ? "DISTINCT " : "") + projection.getElements().stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(", "));
    }
  }

  protected static String printPgqlString(QueryVariable variable) {
    if (variable.getVariableType() == VariableType.EXP_AS_VAR) {
      ExpAsVar expAsVar = (ExpAsVar) variable;
      if (expAsVar.isAnonymous()) {
        // e.g. in "SELECT x.prop1 + x.prop2 FROM g MATCH( (n) ) ORDER BY x.prop1 + x.prop2", the ORDER BY expression
        // "x.prop1 + x.prop2" is a VarRef to the anonymous SELECT expression "x.prop1 + x.prop2"
        return expAsVar.getExp().toString();
      }
    }

    return printIdentifier(variable.name, false);
  }

  protected static String printPgqlString(ExpAsVar expAsVar) {
    if (expAsVar.isAnonymous()) {
      return expAsVar.getExp().toString();
    } else {
      return expAsVar.getExp() + " AS " + printIdentifier(expAsVar.getName(), false);
    }
  }

  protected static String printPgqlString(GraphPattern graphPattern) {
    return printPgqlString(graphPattern, null);
  }

  private static String printPgqlString(GraphPattern graphPattern, SchemaQualifiedName graphName) {
    Set<QueryVertex> uncoveredVertices = new LinkedHashSet<>(graphPattern.getVertices());
    List<String> graphPatternMatches = new ArrayList<String>();

    Iterator<VertexPairConnection> connectionIt = graphPattern.getConnections().iterator();
    while (connectionIt.hasNext()) {
      VertexPairConnection connection = connectionIt.next();
      uncoveredVertices.remove(connection.getSrc());
      uncoveredVertices.remove(connection.getDst());
      if (isVariableLengthPathPatternNotReaches(connection)) {
        graphPatternMatches.add("MATCH " + connection.toString() + printOnClause(graphName));
      } else {
        graphPatternMatches.add(
            "MATCH " + connection.getSrc() + " " + connection + " " + connection.getDst() + printOnClause(graphName));
      }
    }

    // print remaining vertices that are not part of any connection
    Iterator<QueryVertex> vertexIt = uncoveredVertices.iterator();
    while (vertexIt.hasNext()) {
      graphPatternMatches.add("MATCH " + vertexIt.next() + printOnClause(graphName));
    }

    String result = graphPatternMatches.stream().collect(Collectors.joining("\n   , "));

    // print filter expressions
    if (!graphPattern.getConstraints().isEmpty()) {
      result += "\nWHERE " + graphPattern.getConstraints().stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining("\n  AND "));
    }

    return result;
  }

  private static boolean isVariableLengthPathPatternNotReaches(VertexPairConnection connection) {
    if (connection.getVariableType() != VariableType.PATH) {
      return false;
    }

    QueryPath queryPath = (QueryPath) connection;
    PathFindingGoal goal = queryPath.getPathFindingGoal();
    return goal != PathFindingGoal.REACHES || queryPath.getPathExpressionName().startsWith(GENERATED_VAR_PREFIX);
  }

  private static String printOnClause(SchemaQualifiedName graphName) {
    if (graphName == null) {
      return "";
    } else {
      return " ON " + graphName.toString();
    }
  }

  private static String printPathPatterns(List<CommonPathExpression> commonPathExpressions) {
    return commonPathExpressions.stream() //
        .map(x -> printCommonPathExpression(x)) //
        .collect(Collectors.joining());
  }

  private static String printCommonPathExpression(CommonPathExpression commonPathExpression) {
    String result = "PATH " + printIdentifier(commonPathExpression.getName(), false) + " AS ";
    result += printPathExpression(commonPathExpression, false);
    return result + "\n";
  }

  protected static String printPathExpression(CommonPathExpression commonPathExpression, boolean tryOmitSrcAndDst) {
    Iterator<QueryVertex> vertexIt = commonPathExpression.getVertices().iterator();
    Set<QueryExpression> constraintsCopy = new HashSet<>(commonPathExpression.getConstraints());

    QueryVertex vertex = vertexIt.next();
    String result = deanonymizeIfNeeded(vertex, constraintsCopy);
    for (VertexPairConnection connection : commonPathExpression.getConnections()) {
      result += " " + printConnection(vertex, connection, constraintsCopy);
      vertex = vertexIt.next();
      result += " " + deanonymizeIfNeeded(vertex, constraintsCopy);
    }

    if (tryOmitSrcAndDst) {
      if (result.startsWith("()")) {
        result = result.substring(3);
      }
      if (result.endsWith("()")) {
        result = result.substring(0, result.length() - 3);
      }
    }

    if (!constraintsCopy.isEmpty()) {
      result += " WHERE " + constraintsCopy.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(" AND "));
    }

    if (commonPathExpression.getCost() != null) {
      result += " COST " + commonPathExpression.getCost();
    }

    return result;
  }

  private static String deanonymizeIfNeeded(QueryVariable var, Set<QueryExpression> constraintsCopy) {

    QueryExpression labelPredicate = null;
    Iterator<QueryExpression> it = constraintsCopy.iterator();
    while (it.hasNext()) {
      QueryExpression exp = it.next();
      if (isHasLabelFunctionForVar(exp, var)) {
        labelPredicate = exp;
        it.remove();
        break;
      }
    }

    Set<QueryVariable> variables = constraintsCopy.stream() //
        .map(c -> getVariables(c)) //
        .collect(HashSet::new, Set::addAll, Set::addAll);
    boolean printVariableName = !var.isAnonymous() || (var.isAnonymous() && variables.contains(var));

    switch (var.getVariableType()) {
      case EDGE:
        String edge;
        if (printVariableName == false && labelPredicate == null) {
          edge = "-";
        } else {
          edge = "-[" + printVariableAndLabelPredicate(var, printVariableName, labelPredicate) + "]-";
        }
        QueryEdge queryEdge = (QueryEdge) var;
        if (queryEdge.isDirected()) {
          return edge + ">";
        } else {
          return edge;
        }
      case PATH:
        QueryPath queryPath = (QueryPath) var;
        return "-/" + (queryPath.isAnonymous() ? "" : printIdentifier(var.name, false)) + ":"
            + printIdentifier(queryPath.getPathExpressionName(), false) + printHops(queryPath) + "/->";
      case VERTEX:
        return "(" + printVariableAndLabelPredicate(var, printVariableName, labelPredicate) + ")";
      default:
        throw new UnsupportedOperationException("variable type not supported: " + var.getVariableType());
    }
  }

  private static String printVariableAndLabelPredicate(QueryVariable var, boolean printVariableName,
      QueryExpression labelPredicate) {
    String result = printVariableName ? printIdentifier(var.getName(), false) : "";
    if (labelPredicate != null) {
      result += ":" + printLabelPredicate(labelPredicate);
    }
    return result;
  }

  public static boolean isHasLabelFunctionForVar(QueryExpression exp, QueryVariable var) {
    switch (exp.getExpType()) {
      case FUNCTION_CALL: {
        FunctionCall functionCall = (FunctionCall) exp;
        boolean hasLabelFunctionName = functionCall.getFunctionName().equals("has_label")
            || functionCall.getFunctionName().equals("HAS_LABEL");

        if (functionCall.getPackageName() == null && hasLabelFunctionName && functionCall.getArgs().size() == 2) {
          QueryExpression arg0 = functionCall.getArgs().get(0);
          QueryExpression arg1 = functionCall.getArgs().get(1);
          if (arg0.getExpType() == ExpressionType.VARREF && arg1.getExpType() == ExpressionType.STRING) {
            if (((VarRef) arg0).getVariable() == var) {
              return true;
            }
          }
        }

        return false;
      }
      case OR: {
        Or or = (Or) exp;
        return isHasLabelFunctionForVar(or.getExp1(), var) && isHasLabelFunctionForVar(or.getExp2(), var);
      }
      default:
        return false;
    }
  }

  private static String printLabelPredicate(QueryExpression labelPredicate) {
    switch (labelPredicate.getExpType()) {
      case FUNCTION_CALL: {
        FunctionCall hasLabelPredicate = (FunctionCall) labelPredicate;
        ConstString constString = (ConstString) hasLabelPredicate.getArgs().get(1);
        return printIdentifier(constString.getValue(), false);
      }
      case OR: {
        Or or = (Or) labelPredicate;
        return printLabelPredicate(or.getExp1()) + "|" + printLabelPredicate(or.getExp2());
      }
      default:
        throw new IllegalArgumentException("unexpected expression type: " + labelPredicate.getExpType());
    }
  }

  /**
   * Example 1: "-[e]->" => "<-[e]-" Example 2: -/:xyz/-> "<-/:xyz/-"
   */
  private static String printConnection(QueryVertex vertexOnTheLeft, VertexPairConnection connection,
      Set<QueryExpression> constraintsCopy) {
    String connectionAsString = deanonymizeIfNeeded(connection, constraintsCopy);

    boolean isUndirectedEdge = connection.getVariableType() == VariableType.EDGE
        && ((QueryEdge) connection).isDirected() == false;

    if (isUndirectedEdge || connection.getSrc() == vertexOnTheLeft) {
      return connectionAsString;
    } else {
      return "<" + connectionAsString.substring(0, connectionAsString.length() - 1);
    }
  }

  protected static String printHops(QueryPath path) {
    long minHops = path.getMinHops();
    long maxHops = path.getMaxHops();
    if (minHops == 1 && maxHops == 1 && path.getPathFindingGoal() == PathFindingGoal.REACHES) {
      return "";
    } else if (minHops == 0 && maxHops == -1) {
      return "*";
    } else if (minHops == 1 && maxHops == -1) {
      return "+";
    } else if (minHops == maxHops) {
      return "{" + minHops + "}";
    } else if (maxHops == -1) {
      return "{" + minHops + ",}";
    } else if (minHops == 0) {
      return "{," + maxHops + "}";
    } else {
      return "{" + minHops + "," + maxHops + "}";
    }
  }

  protected static String printPgqlString(GroupBy groupBy) {
    return "GROUP BY " + groupBy.getElements().stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(", "));
  }

  protected static String printPgqlString(OrderBy orderBy) {
    return "ORDER BY " + orderBy.getElements().stream() //
        .map(orderByElem -> printPgqlString(orderByElem)) //
        .collect(Collectors.joining(", "));
  }

  protected static String printPgqlString(OrderByElem orderByElem) {
    return orderByElem.getExp() + (orderByElem.isAscending() ? "" : " DESC");
  }

  private static String printTime(LocalTime time) {
    StringBuilder buf = new StringBuilder(18);
    int hourValue = time.getHour();
    int minuteValue = time.getMinute();
    int secondValue = time.getSecond();
    int nanoValue = time.getNano();
    buf.append(hourValue < 10 ? "0" : "").append(hourValue).append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
    buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
    if (nanoValue > 0) {
      buf.append('.');
      if (nanoValue % 1000_000 == 0) {
        buf.append(Integer.toString((nanoValue / 1000_000) + 1000).substring(1));
      } else if (nanoValue % 1000 == 0) {
        buf.append(Integer.toString((nanoValue / 1000) + 1000_000).substring(1));
      } else {
        buf.append(Integer.toString((nanoValue) + 1000_000_000).substring(1));
      }
    }
    return buf.toString();
  }

  public static String printLiteral(double val) {
    return DECIMAL_FORMAT.format(val);
  }

  public static String printLiteral(String val) {
    return "'" + val.replace("'", "''") + "'";
  }

  public static String printLiteral(LocalDate val) {
    return "DATE '" + val + "'";
  }

  public static String printLiteral(LocalTime val) {
    return "TIME '" + printTime(val) + "'";
  }

  public static String printLiteral(LocalDateTime val) {
    return "TIMESTAMP '" + val.toLocalDate() + " " + printTime(val.toLocalTime()) + "'";
  }

  public static String printLiteral(OffsetTime val) {
    return "TIME '" + printTime(val.toLocalTime()) + val.getOffset() + "'";
  }

  public static String printLiteral(OffsetDateTime val) {
    return "TIMESTAMP '" + val.toLocalDate() + " " + printTime(val.toLocalTime()) + val.getOffset() + "'";
  }
}
