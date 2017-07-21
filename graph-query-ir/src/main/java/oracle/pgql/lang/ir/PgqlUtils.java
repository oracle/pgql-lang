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
    });
    return result;
  }

  public static String printPgqlString(String stringLiteral) {
    return "'" + escapeJava(stringLiteral) + "'";
  }
  
  public static String printPgqlString(GraphQuery graphQuery) {
    GraphPattern graphPattern = graphQuery.getGraphPattern();
    String result = printPathPatterns(graphPattern) + graphQuery.getProjection() + "\n" + graphPattern;
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

  public static String printPgqlString(Projection projection) {
    if (projection.getElements().isEmpty()) {
      return "SELECT *";
    } else {
      return "SELECT " + projection.getElements().stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(", "));
    }
  }

  public static String printPgqlString(QueryVariable variable) {
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

    return variable.isAnonymous() ? "_anonymous_" : variable.name;
  }

  public static String printPgqlString(ExpAsVar expAsVar) {
    String exp = expAsVar.getExp().toString();
    return expAsVar.isAnonymous() ? exp : exp + " AS " + expAsVar.getName();
  }

  public static String printPgqlString(GraphPattern graphPattern, List<QueryPath> queryPaths) {
    String result = "WHERE\n";

    Set<QueryVertex> verticesCopy = new HashSet<>(graphPattern.getVertices());

    Iterator<VertexPairConnection> connectionIt = graphPattern.getConnections().iterator();

    while (connectionIt.hasNext()) {
      VertexPairConnection connection = connectionIt.next();
      QueryVertex src = connection.getSrc();
      QueryVertex dst = connection.getDst();
      result += "  " + src + " " + connection + " " + dst;
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
    
    
    Set<QueryExpression> constraints = graphPattern.getConstraints();
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
    result += vertex;
    for (VertexPairConnection connection : path.getConnections()) {
      result += " ";
      switch (connection.getVariableType()) {
        case EDGE:
          QueryEdge edge = (QueryEdge) connection;
          if (edge.getSrc() == vertex || !edge.isDirected()) {
            result += edge.toString();
          } else {
            result += printReverseConnection(edge);
          }
          break;
        case PATH:
          if (connection.getSrc() == vertex) {
            result += connection.toString();
          } else {
            result += printReverseConnection(connection);
          }
          break;
        default:
          throw new UnsupportedOperationException("variable type not supported: " + connection.getVariableType());
      }

      vertex = vertexIt.next();
      result += " " + vertex;
    }

    Set<QueryExpression> constraints = path.getConstraints();
    if (!constraints.isEmpty()) {
      result += " WHERE " + constraints.stream() //
      .map(x -> x.toString()) //
      .collect(Collectors.joining(" AND "));
    }
    return result + "\n";
  }

  /**
   * Example 1:  "-[e]->" => "<-[e]-"
   * Example 2:  -/:xyz/-> "<-/:xyz/-"
   */
  private static String printReverseConnection(VertexPairConnection connection) {
    String s = connection.toString();
    return "<" + s.substring(0, s.length() - 1);
  }

  public static String printConnectionWithSrcAndDst(VertexPairConnection connection) {
    return connection.getSrc() + " " + connection + " " + connection.getDst();
  }

  public static String printHops(QueryPath path) {
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

  public static String printPgqlString(GroupBy groupBy) {
    return "GROUP BY " + groupBy.getElements().stream() //
        .map(x -> x.toString()) //
        .collect(Collectors.joining(", "));
  }

  public static String printPgqlString(OrderBy orderBy) {
    return "ORDER BY " + orderBy.getElements().stream() //
        .map(orderByElem -> printPgqlString(orderByElem)) //
        .collect(Collectors.joining(", "));
  }

  public static String printPgqlString(OrderByElem orderByElem) {
    return orderByElem.getExp() + " " + (orderByElem.isAscending() ? "" : "DESC");
  }
}
