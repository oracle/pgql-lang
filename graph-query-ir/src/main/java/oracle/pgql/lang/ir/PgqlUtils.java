/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryVariable.VariableType;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

public class PgqlUtils {

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

      public void visit(Exists exists ) {
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
    return "'"
        + stringLiteral //
            .replace("\\", "\\\\") //
            .replace("'", "\\'") //
            .replace("\t", "\\t") //
            .replace("\n", "\\n") //
            .replace("\r", "\\r") //
            .replace("\b", "\\b") //
            .replace("\f", "\\f") //
        + "'";
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

  private static String printIdentifier(String identifier) {
    if (identifier.matches("\\w*")) {
      return identifier;
    } else {
      return "\"" + escapeJava(identifier) + "\"";
    }
  }

  protected static String printPgqlString(Projection projection) {
    if (projection.getElements().isEmpty()) {
      return "SELECT *";
    } else {
      return "SELECT " + (projection.hasDistinct() ? "DISTINCT " : "")
          + projection.getElements().stream() //
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

    Set<QueryVertex> verticesCopy = new HashSet<>(graphPattern.getVertices());

    Iterator<VertexPairConnection> connectionIt = graphPattern.getConnections().iterator();
    Set<QueryExpression> constraints = graphPattern.getConstraints();

    if (connectionIt.hasNext()) {
      VertexPairConnection connection1 = connectionIt.next();
      QueryVertex lastVertex = null;

      while (connectionIt.hasNext()) {
        VertexPairConnection connection2 = connectionIt.next();

        QueryVertex src1 = connection1.getSrc();
        QueryVertex dst1 = connection1.getDst();

        QueryVertex src2 = connection2.getSrc();
        QueryVertex dst2 = connection2.getDst();

        QueryVertex vertexOnTheLeft;
        QueryVertex vertexOnTheRight;
        if (src1 == src2 || src1 == dst2) {
          vertexOnTheLeft = dst1;
          vertexOnTheRight = src1;
        } else {
          vertexOnTheLeft = src1;
          vertexOnTheRight = dst1;
        }

        result += printConnection(verticesCopy, constraints, connection1, lastVertex, vertexOnTheLeft, vertexOnTheRight,
            indentation);

        connection1 = connection2;
        lastVertex = vertexOnTheRight;
      }

      QueryVertex src1 = connection1.getSrc();
      QueryVertex dst1 = connection1.getDst();
      QueryVertex vertexOnTheLeft;
      QueryVertex vertexOnTheRight;
      if (connection1.getDst() == lastVertex) {
        vertexOnTheLeft = dst1;
        vertexOnTheRight = src1;
      } else {
        vertexOnTheLeft = src1;
        vertexOnTheRight = dst1;
      }

      result += printConnection(verticesCopy, constraints, connection1, lastVertex, vertexOnTheLeft, vertexOnTheRight,
          indentation);
    }

    // print disconnected vertices
    if (!graphPattern.getConnections().isEmpty() && !verticesCopy.isEmpty()) {
      result += ",\n";
    }
    result += verticesCopy.stream() //
        .map(x -> "  " + x.toString()) //
        .collect(Collectors.joining(",\n"));

    if (!constraints.isEmpty()) {
      result += "\nWHERE " + constraints.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining("\n  AND "));
    }

    return result;
  }

  private static String printConnection(Set<QueryVertex> verticesCopy, Set<QueryExpression> constraints,
      VertexPairConnection connection1, QueryVertex lastVertex, QueryVertex vertexOnTheLeft,
      QueryVertex vertexOnTheRight, int indentation) {
    String result = "";

    if (lastVertex != vertexOnTheLeft) {
      if (lastVertex != null) {
        result += "\n" + printIndentation(indentation - 2) + ", ";
      }
      result += deanonymizeIfNeeded(vertexOnTheLeft, constraints);
    }
    result += " " + printConnection(vertexOnTheLeft, connection1, constraints) + " ";
    result += deanonymizeIfNeeded(vertexOnTheRight, constraints);

    verticesCopy.remove(vertexOnTheLeft);
    verticesCopy.remove(vertexOnTheRight);
    return result;
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

    QueryVertex vertex = vertexIt.next();
    result += deanonymizeIfNeeded(vertex, commonPathExpression.getConstraints());
    for (VertexPairConnection connection : commonPathExpression.getConnections()) {
      result += " " + printConnection(vertex, connection, commonPathExpression.getConstraints());
      vertex = vertexIt.next();
      result += " " + deanonymizeIfNeeded(vertex, commonPathExpression.getConstraints());
    }

    Set<QueryExpression> constraints = commonPathExpression.getConstraints();
    if (!constraints.isEmpty()) {
      result += " WHERE " + constraints.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(" AND "));
    }
    return result + "\n";
  }

  private static String deanonymizeIfNeeded(QueryVariable var, Set<QueryExpression> constraints) {
    Set<QueryVariable> variables = constraints.stream() //
        .map(c -> getVariables(c)) //
        .collect(HashSet::new, Set::addAll, Set::addAll);

    if (variables.contains(var) && var.isAnonymous()) {
      switch (var.getVariableType()) {
        case EDGE:
          String edge = "-[" + var.name + "]-";
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
          return "(" + var.name + ")";
        default:
          throw new UnsupportedOperationException("variable type not supported: " + var.getVariableType());
      }
    } else {
      return var.toString();
    }
  }

  /**
   * Example 1: "-[e]->" => "<-[e]-" Example 2: -/:xyz/-> "<-/:xyz/-"
   */
  private static String printConnection(QueryVertex vertexOnTheLeft, VertexPairConnection connection,
      Set<QueryExpression> constraints) {
    String connectionAsString = deanonymizeIfNeeded(connection, constraints);

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
