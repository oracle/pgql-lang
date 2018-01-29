/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import oracle.pgql.lang.util.AbstractQueryExpressionVisitor;
import oracle.pgql.lang.ir.QueryExpression.Aggregation;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrAvg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrCount;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMax;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMin;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrSum;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Or;
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryVariable.VariableType;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

public class PgqlUtils {

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

      public void visit(Exists exists) {
        // don't visit EXISTS subqueries
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

  protected static String printPgqlString(String stringLiteral) {
    return "'" + escape(stringLiteral) //
        .replace("'", "\\'") //
        + "'";
  }

  protected static String printIdentifier(String identifier) {
    if (identifier.matches("^[a-zA-Z0-9_]*$")) {
      return identifier;
    } else {
      return "\"" + escape(identifier) //
          .replace("\"", "\\\"") //
          + "\"";
    }
  }

  private static String escape(String s) {
    return s //
        .replace("\\", "\\\\") //
        .replace("\t", "\\t") //
        .replace("\n", "\\n") //
        .replace("\r", "\\r") //
        .replace("\b", "\\b") //
        .replace("\f", "\\f");
  }

  protected static String printPgqlDecimal(double val) {
    return DECIMAL_FORMAT.format(val);
  }

  protected static String printPgqlString(GraphQuery graphQuery) {
    String result = printPathPatterns(graphQuery.getCommonPathExpressions());
    GraphPattern graphPattern = graphQuery.getGraphPattern();
    result += graphQuery.getProjection() + "\n";
    if (graphQuery.getInputGraphName() != null) {
      result += "FROM " + printIdentifier(graphQuery.getInputGraphName()) + "\n";
    }
    result += graphPattern;
    GroupBy groupBy = graphQuery.getGroupBy();
    if (groupBy.getElements().isEmpty() == false) {
      result += "\n" + groupBy;
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
      return "SELECT " + (projection.hasDistinct() ? "DISTINCT " : "") + projection.getElements().stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(", "));
    }
  }

  protected static String printPgqlString(QueryVariable variable) {
    if (variable.getVariableType() == VariableType.EXP_AS_VAR) {
      ExpAsVar expAsVar = (ExpAsVar) variable;
      if (expAsVar.isAnonymous()) {
        // e.g. in "SELECT x.inDegree() WHERE (n) GROUP BY x.inDegree()", the SELECT expression "x.inDegree()"
        // is a VarRef to the anonymous GROUP BY expression "x.inDegree()"
        return expAsVar.getExp().toString();
      } else {
        return variable.name;
      }
    }

    return variable.name;
  }

  protected static String printPgqlString(ExpAsVar expAsVar) {
    String exp = expAsVar.getExp().toString();
    // replace expAsVar.getExp().toString() with expAsVar.getName() once we no longer have to pretty print PGQL v1.0
    return expAsVar.isAnonymous() ? expAsVar.getExp().toString() : exp + " AS " + expAsVar.getName();
  }

  protected static String printPgqlString(GraphPattern graphPattern, List<QueryPath> queryPaths) {
    String result = "MATCH ";
    int indentation = result.length();

    Iterator<QueryVertex> vertexIt = graphPattern.getVertices().iterator();
    Set<QueryExpression> constraintsCopy = new HashSet<>(graphPattern.getConstraints());
    QueryVertex lastVertex = null;
    Set<QueryVertex> uncoveredVertices = new LinkedHashSet<>(graphPattern.getVertices());
    Set<VertexPairConnection> uncoveredConnections = new HashSet<>(graphPattern.getConnections());

    // print all vertices and their connections
    while (vertexIt.hasNext()) {
      QueryVertex vertex = vertexIt.next();
      if (!uncoveredVertices.contains(vertex)) {
        continue;
      } else {
        uncoveredVertices.remove(vertex);
      }

      result += printVertex(constraintsCopy, vertex, lastVertex, indentation);
      lastVertex = vertex;

      Iterator<VertexPairConnection> connectionIt = uncoveredConnections.iterator();
      while (connectionIt.hasNext()) {
        VertexPairConnection connection = connectionIt.next();

        if (connection.getSrc() == lastVertex || connection.getDst() == lastVertex) {
          QueryVertex vertexOnTheLeft = lastVertex;
          QueryVertex vertexOnTheRight = connection.getSrc() == lastVertex ? connection.getDst() : connection.getSrc();

          result += printConnection(constraintsCopy, connection, lastVertex, vertexOnTheLeft, vertexOnTheRight,
              indentation);

          uncoveredVertices.remove(vertexOnTheRight);
          connectionIt.remove();
          lastVertex = vertexOnTheRight;
        } else {
          break;
        }
      }
    }

    // print connections for which both vertices are already printed
    Iterator<VertexPairConnection> connectionIt = uncoveredConnections.iterator();
    while (connectionIt.hasNext()) {
      VertexPairConnection connection = connectionIt.next();
      result += printConnection(constraintsCopy, connection, lastVertex, indentation);
      lastVertex = connection.getDst();
    }

    // print filter expressions
    if (!constraintsCopy.isEmpty()) {
      result += "\nWHERE " + constraintsCopy.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining("\n  AND "));
    }

    return result;
  }

  private static String printConnection(Set<QueryExpression> constraintsCopy, VertexPairConnection connection,
      QueryVertex lastVertex, int indentation) {
    return printConnection(constraintsCopy, connection, lastVertex, connection.getSrc(), connection.getDst(),
        indentation);
  }

  private static String printConnection(Set<QueryExpression> constraintsCopy, VertexPairConnection connection,
      QueryVertex lastVertex, QueryVertex vertexOnTheLeft, QueryVertex vertexOnTheRight, int indentation) {
    String result = "";

    if (lastVertex != vertexOnTheLeft) {
      if (lastVertex != null) {
        result += "\n" + printIndentation(indentation - 2) + ", ";
      }
      result += deanonymizeIfNeeded(vertexOnTheLeft, constraintsCopy);
    }
    result += " " + printConnection(vertexOnTheLeft, connection, constraintsCopy) + " ";
    result += deanonymizeIfNeeded(vertexOnTheRight, constraintsCopy);

    return result;
  }

  private static String printVertex(Set<QueryExpression> constraintsCopy, QueryVertex vertex, QueryVertex lastVertex,
      int indentation) {
    String result = (lastVertex == null) ? "" : "\n" + printIndentation(indentation - 2) + ", ";
    return result + deanonymizeIfNeeded(vertex, constraintsCopy);
  }

  private static String printIndentation(int indentation) {
    return String.join("", Collections.nCopies(indentation, " "));
  }

  private static String printPathPatterns(List<CommonPathExpression> commonPathExpressions) {
    return commonPathExpressions.stream() //
        .map(x -> printPathExpression(x)) //
        .collect(Collectors.joining());
  }

  private static String printPathExpression(CommonPathExpression commonPathExpression) {
    String result = "PATH " + commonPathExpression.getName() + " AS ";

    Iterator<QueryVertex> vertexIt = commonPathExpression.getVertices().iterator();
    Set<QueryExpression> constraintsCopy = new HashSet<>(commonPathExpression.getConstraints());

    QueryVertex vertex = vertexIt.next();
    result += deanonymizeIfNeeded(vertex, constraintsCopy);
    for (VertexPairConnection connection : commonPathExpression.getConnections()) {
      result += " " + printConnection(vertex, connection, constraintsCopy);
      vertex = vertexIt.next();
      result += " " + deanonymizeIfNeeded(vertex, constraintsCopy);
    }

    if (!constraintsCopy.isEmpty()) {
      result += " WHERE " + constraintsCopy.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(" AND "));
    }
    return result + "\n";
  }

  private static String deanonymizeIfNeeded(QueryVariable var, Set<QueryExpression> constraintsCopy) {

    QueryExpression labelPredicate = null;
    Iterator<QueryExpression> it = constraintsCopy.iterator();
    while (it.hasNext()) {
      QueryExpression exp = it.next();
      if (isLabelPredicate(var, exp)) {
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
        return "-/" + var.name + ":" + queryPath.getPathExpressionName() + printHops(queryPath) + "/->";
      case VERTEX:
        return "(" + printVariableAndLabelPredicate(var, printVariableName, labelPredicate) + ")";
      default:
        throw new UnsupportedOperationException("variable type not supported: " + var.getVariableType());
    }
  }

  private static String printVariableAndLabelPredicate(QueryVariable var, boolean printVariableName,
      QueryExpression labelPredicate) {
    String result = printVariableName ? var.getName() : "";
    if (labelPredicate != null) {
      result += ":" + printLabelPredicate(labelPredicate);
    }
    return result;
  }

  private static boolean isLabelPredicate(QueryVariable var, QueryExpression exp) {
    switch (exp.getExpType()) {
      case FUNCTION_CALL: {
        FunctionCall functionCall = (FunctionCall) exp;
        if (functionCall.getPackageName() == null && functionCall.getFunctionName().toLowerCase().equals("has_label")
            && functionCall.getArgs().size() == 2) {
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
        return isLabelPredicate(var, or.getExp1()) && isLabelPredicate(var, or.getExp2());
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
        return printIdentifier(constString.getValue());
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
    if (minHops == 1 && maxHops == 1) {
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

  protected static String printTime(LocalTime time) {
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
}
