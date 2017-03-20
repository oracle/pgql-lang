/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    return "'" + stringLiteral.replace("\\", "\\\\").replace("'", "\\'") + "'";
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
    String result = "SELECT ";
    if (projection.getElements().isEmpty()) {
      result += "*";
    } else {
      Iterator<ExpAsVar> it = projection.getElements().iterator();
      while (it.hasNext()) {
        result += it.next();
        if (it.hasNext()) {
          result += ", ";
        }
      }
    }
    return result;
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

    return variable.isAnonymous() ? "" : variable.name;
  }

  public static String printPgqlString(ExpAsVar expAsVar) {
    String exp = expAsVar.getExp().toString();
    return expAsVar.isAnonymous() ? exp : exp + " AS " + expAsVar.getName();
  }

  /**
   * Returns whether QueryPath a equals QueryPath b, ignoring source, destination vertices and path name.
   */
  private static boolean patternsEqual(QueryPath a, QueryPath b) {

    if (a.isAnonymous() != b.isAnonymous()) {
      return false;
    }
    if (!a.getVertices().equals(b.getVertices())) {
      return false;
    }
    if (!a.getConnections().equals(b.getConnections())) {
      return false;
    }
    return a.getConstraints().equals(b.getConstraints());
  }

  private static int getPathId(QueryPath path, List<QueryPath> queryPaths) {
    for (int i = 0; i < queryPaths.size(); i++) {
      if (patternsEqual(path, queryPaths.get(i))) {
        return i;
      }
    }
    queryPaths.add(path);
    return queryPaths.size() - 1;
  }

  public static String printPgqlString(GraphPattern graphPattern, List<QueryPath> queryPaths) {
    String result = "WHERE\n";

    Set<QueryExpression> constraints = new HashSet<>(graphPattern.getConstraints());

    Map<QueryVertex, String> vertexStrings = getStringsForVerticesWithInlinedConstraints(graphPattern.getVertices(),
        constraints);

    Iterator<VertexPairConnection> it2 = graphPattern.getConnections().iterator();

    while (it2.hasNext()) {
      VertexPairConnection connection = it2.next();

      result += "  ";
      result += printVertex(connection.getSrc(), vertexStrings);

      switch (connection.getVariableType()) {
        case EDGE:
          QueryEdge edge = (QueryEdge) connection;
          result += " -[";
          result += printInlinedConstraints(constraints, edge);
          result += "]-";
          result += edge.isDirected() ? "> " : " ";
          break;
        case PATH:
          QueryPath path = (QueryPath) connection;
          result += " -/:type";
          result += getPathId(path, queryPaths);
          switch (path.getRepetition()) {
            case KLEENE_STAR:
              result += "*";
              break;
            case KLEENE_PLUS:
              result += "+";
              break;
            case NONE:
              break;
            default:
              throw new UnsupportedOperationException();
          }
          result += "/-> ";
          break;
        default:
          throw new UnsupportedOperationException();
      }

      result += printVertex(connection.getDst(), vertexStrings);

      if (it2.hasNext()) {
        result += ",\n";
      }
    }

    if (graphPattern.getConnections().isEmpty() == false && vertexStrings.isEmpty() == false) {
      result += ",\n";
    }

    Iterator<String> it = vertexStrings.values().iterator();
    while (it.hasNext()) {
      String vertexString = it.next();
      result += vertexString;
      if (it.hasNext()) {
        vertexString += ",\n";
      }
    }

    if (constraints.isEmpty() == false) {
      result += ",\n";
    }

    Iterator<QueryExpression> it4 = constraints.iterator();
    while (it4.hasNext()) {
      result += "  " + it4.next();
      if (it4.hasNext()) {
        result += ",\n";
      }
    }
    return result;
  }

  private static String printVertex(QueryVertex vertex,
      Map<QueryVertex, String> stringForVerticesWithInlinedConstraints) {
    if (stringForVerticesWithInlinedConstraints.containsKey(vertex)) {
      String result = stringForVerticesWithInlinedConstraints.get(vertex);
      stringForVerticesWithInlinedConstraints.remove(vertex);
      return result;
    } else {
      return "(" + (vertex.isAnonymous() ? "" : vertex.name) + ")";
    }
  }

  private static HashMap<QueryVertex, String> getStringsForVerticesWithInlinedConstraints(
      Collection<QueryVertex> vertices, Set<QueryExpression> constraints) {
    HashMap<QueryVertex, String> result = new HashMap<>();
    for (QueryVertex vertex : vertices) {
      String vertexString = "(";
      vertexString += printInlinedConstraints(constraints, vertex);
      vertexString += ")";
      result.put(vertex, vertexString);
    }
    return result;
  }

  private static String printInlinedConstraints(Set<QueryExpression> constraints, QueryVariable variable) {
    if (variable.isAnonymous() == false) {
      return variable.name;
    }

    String result = "";
    Set<QueryExpression> constraintsForVariable = new HashSet<>();
    for (QueryExpression exp : constraints) {
      Set<QueryVariable> varsInExp = PgqlUtils.getVariables(exp);
      if (varsInExp.size() == 1 && varsInExp.contains(variable)) {
        constraintsForVariable.add(exp);
      }
    }
    if (constraintsForVariable.size() >= 1) {
      constraints.removeAll(constraintsForVariable);
      result += "WITH ";
      Iterator<QueryExpression> it = constraintsForVariable.iterator();
      while (it.hasNext()) {
        result += it.next();
        if (it.hasNext()) {
          result += ", ";
        }
      }
    }
    return result;
  }

  private static String printPathPatterns(GraphPattern graphPattern) {
    String result = "";

    List<QueryPath> queryPaths = new ArrayList<>();
    printPathPatternsHelper(queryPaths, graphPattern.getConnections());

    List<QueryPath> queryPathsList = new ArrayList<QueryPath>(queryPaths);
    for (QueryPath path : queryPaths) {
      result += printPathPattern(path, queryPathsList);
    }

    return result;
  }

  private static void printPathPatternsHelper(List<QueryPath> queryPaths,
      Collection<VertexPairConnection> connections) {
    for (VertexPairConnection connection : connections) {
      if (connection.getVariableType() == VariableType.PATH) {
        QueryPath path = (QueryPath) connection;

        boolean found = false;
        for (int i = 0; i < queryPaths.size(); i++) {
          if (patternsEqual(path, queryPaths.get(i))) {
            found = true;
          }
        }
        if (found == false) {
          queryPaths.add(path);
          printPathPatternsHelper(queryPaths, path.getConnections());
        }
      }
    }
  }

  private static String printPathPattern(QueryPath path, List<QueryPath> queryPaths) {
    int pathId = getPathId(path, queryPaths);
    String result = "PATH type" + pathId + " := ";

    Set<QueryExpression> constraints = new HashSet<>(path.getConstraints());

    Map<QueryVertex, String> vertexStrings = getStringsForVerticesWithInlinedConstraints(path.getVertices(),
        constraints);

    Iterator<QueryVertex> verticesIt = path.getVertices().iterator();

    QueryVertex vertex = verticesIt.next();
    result += printVertex(vertex, vertexStrings);
    for (VertexPairConnection connection2 : path.getConnections()) {

      switch (connection2.getVariableType()) {
        case EDGE:
          QueryEdge edge = (QueryEdge) connection2;
          if (edge.isDirected()) {
            result += (edge.getSrc() == vertex) ? " -[" : " <-[";
          } else {
            result += " -[";
          }

          result += printInlinedConstraints(constraints, edge);

          if (edge.isDirected()) {
            result += (edge.getSrc() == vertex) ? "]-> " : "]- ";
          } else {
            result += "]- ";
          }
          break;
        case PATH:
          QueryPath queryPath = (QueryPath) connection2;
          result += (queryPath.getSrc() == vertex) ? " -/" : " <-/";
          result += "type" + getPathId(queryPath, queryPaths);
          switch (path.getRepetition()) {
            case KLEENE_STAR:
              result += "*";
              break;
            case KLEENE_PLUS:
              result += "+";
              break;
            case NONE:
              break;
            default:
              throw new UnsupportedOperationException();
          }
          result += (queryPath.getSrc() == vertex) ? "/-> " : "/- ";
          break;
        default:
          throw new UnsupportedOperationException("variable type not supported: " + connection2.getVariableType());
      }

      vertex = verticesIt.next();
      result += printVertex(vertex, vertexStrings);
    }
    result += "\n";
    return result;
  }

  public static String printPgqlString(GroupBy groupBy) {
    String result = "GROUP BY ";
    Iterator<ExpAsVar> it = groupBy.getElements().iterator();
    while (it.hasNext()) {
      result += it.next();
      if (it.hasNext()) {
        result += ", ";
      }
    }
    return result;
  }

  public static String printPgqlString(OrderBy orderBy) {
    String result = "ORDER BY ";
    Iterator<OrderByElem> it = orderBy.getElements().iterator();
    while (it.hasNext()) {
      result += it.next();
      if (it.hasNext()) {
        result += ", ";
      }
    }
    return result;
  }

  public static String printPgqlString(OrderByElem orderByElem) {
    return (orderByElem.isAscending() ? "ASC" : "DESC") + "(" + orderByElem.getExp() + ")";
  }
}
