/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import oracle.pgql.lang.ir.QueryVariable.VariableType;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

public class PgqlUtils {

  /**
   * @param exp a query expression
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
    });
    return result;
  }

  public static String printConnectionWithSrcAndDst(VertexPairConnection connection) {
    return connection.getSrc() + " " + connection + " " + connection.getDst();
  }

  public static String printReverseConnectionWithSrcAndDst(VertexPairConnection connection) {
    return connection.getDst() + " " + printReverseConnection(connection.toString()) + " " + connection.getSrc();
  }

// HELPER METHODS FOR PRETTY-PRINTING BELOW

  protected static String printPgqlString(String stringLiteral) {
    return "'" + escapeJava(stringLiteral) + "'";
  }
  
  protected static String printPgqlString(GraphQuery graphQuery) {
    GraphPattern graphPattern = graphQuery.getGraphPattern();
    String result = printPathPatterns(graphPattern);
    result += graphQuery.getProjection() + "\n";
    if (graphQuery.getInputGraphName() != null) {
      result += "FROM " + graphQuery.getInputGraphName() + "\n";
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
      return "SELECT " + projection.getElements().stream() //
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
    return expAsVar.isAnonymous() ? exp : exp + " AS " + expAsVar.getName();
  }

  protected static String printPgqlString(GraphPattern graphPattern, List<QueryPath> queryPaths) {
    String result = "WHERE\n";

    Set<QueryVertex> verticesCopy = new HashSet<>(graphPattern.getVertices());

    Iterator<VertexPairConnection> connectionIt = graphPattern.getConnections().iterator();
    Set<QueryExpression> constraints = graphPattern.getConstraints();

    while (connectionIt.hasNext()) {
      VertexPairConnection connection = connectionIt.next();
      QueryVertex src = connection.getSrc();
      QueryVertex dst = connection.getDst();
      result += "  " + deanonymizeIfNeeded(src, constraints) + " " + deanonymizeIfNeeded(connection, constraints) + " " + deanonymizeIfNeeded(dst, constraints);
      if (connectionIt.hasNext()) {
        result += ",\n";
      }
      verticesCopy.remove(src);
      verticesCopy.remove(dst);
    }
    if (!graphPattern.getConnections().isEmpty() && !verticesCopy.isEmpty()) {
      result += ",\n";
    }

    result += verticesCopy.stream() //
        .map(x -> "  " + x.toString()) //
        .collect(Collectors.joining(",\n"));
    
    if (!constraints.isEmpty()) {
      result += ",\n  " + constraints.stream() //
      .map(x -> x.toString()) //
      .collect(Collectors.joining(",\n  "));
    }
    
    return result;
  }

  private static String printPathPatterns(GraphPattern graphPattern) {
    Map<String, QueryPath> queryPaths = new HashMap<>();
    printPathPatternsHelper(queryPaths, graphPattern.getConnections());
    return queryPaths.values().stream() //
        .map(x -> printPathExpression(x)) //
        .collect(Collectors.joining());
  }

  private static void printPathPatternsHelper(Map<String, QueryPath> queryPaths,
      Collection<VertexPairConnection> connections) {
    for (VertexPairConnection connection : connections) {
      if (connection.getVariableType() == VariableType.PATH) {
        QueryPath path = (QueryPath) connection;
        QueryPath result = queryPaths.putIfAbsent(path.getPathExpressionName(), path);
        if (result == null) {
          printPathPatternsHelper(queryPaths, path.getConnections());
        }
      }
    }
  }

  private static String printPathExpression(QueryPath path) {
    String result = "PATH " + path.getPathExpressionName() + " := ";

    Iterator<QueryVertex> vertexIt = path.getVertices().iterator();

    QueryVertex vertex = vertexIt.next();
    result += deanonymizeIfNeeded(vertex, path.getConstraints());
    for (VertexPairConnection connection : path.getConnections()) {
      result += " ";
      switch (connection.getVariableType()) {
        case EDGE:
          QueryEdge edge = (QueryEdge) connection;
          String edgeAsString = deanonymizeIfNeeded(edge, path.getConstraints());
          if (edge.getSrc() == vertex || !edge.isDirected()) {
            result += edgeAsString;
          } else {
            result += printReverseConnection(edgeAsString);
          }
          break;
        case PATH:
          QueryPath nestedPath = (QueryPath) connection;
          String pathAsString = deanonymizeIfNeeded(nestedPath, path.getConstraints());
          if (connection.getSrc() == vertex) {
            result += pathAsString;
          } else {
            result += printReverseConnection(pathAsString);
          }
          break;
        default:
          throw new UnsupportedOperationException("variable type not supported: " + connection.getVariableType());
      }

      vertex = vertexIt.next();
      result += " " + deanonymizeIfNeeded(vertex, path.getConstraints());
    }

    Set<QueryExpression> constraints = path.getConstraints();
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
          return "-[" + var.name + "]->";
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
   * Example 1:  "-[e]->" => "<-[e]-"
   * Example 2:  -/:xyz/-> "<-/:xyz/-"
   */
  private static String printReverseConnection(String connection) {
    return "<" + connection.substring(0, connection.length() - 1);
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
}
