/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
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

import oracle.pgql.lang.ir.QueryExpression.Aggregation;
import oracle.pgql.lang.ir.QueryExpression.ConstNull;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrAvg;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrCount;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMax;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrMin;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.AggrSum;
import oracle.pgql.lang.ir.QueryExpression.Aggregation.Star;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Add;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Div;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mod;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Mul;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.Sub;
import oracle.pgql.lang.ir.QueryExpression.ArithmeticExpression.UMin;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstBoolean;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstDecimal;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstInteger;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.Function.EdgeLabel;
import oracle.pgql.lang.ir.QueryExpression.Function.HasLabel;
import oracle.pgql.lang.ir.QueryExpression.Function.HasProp;
import oracle.pgql.lang.ir.QueryExpression.Function.Id;
import oracle.pgql.lang.ir.QueryExpression.Function.InDegree;
import oracle.pgql.lang.ir.QueryExpression.Function.OutDegree;
import oracle.pgql.lang.ir.QueryExpression.Function.Regex;
import oracle.pgql.lang.ir.QueryExpression.Function.VertexLabels;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.And;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Not;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Or;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Equal;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Greater;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.GreaterEqual;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.Less;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.LessEqual;
import oracle.pgql.lang.ir.QueryExpression.RelationalExpression.NotEqual;
import oracle.pgql.lang.ir.QueryPath.Direction;
import oracle.pgql.lang.ir.QueryVariable.VariableType;

public class PgqlUtils {

  /**
   * @param exp
   *          a query expression
   * @return the set of variables used in the query expression
   */
  public static Set<QueryVariable> getVariables(QueryExpression exp) {
    final Set<QueryVariable> result = new HashSet<>();
    exp.accept(new QueryExpressionVisitor() {

      @Override
      public void visit(VarRef varRef) {
        result.add(varRef.getVariable());
      }

      @Override
      public void visit(PropertyAccess propAccess) {
        result.add(propAccess.getVariable());
      }

      @Override
      public void visit(ConstInteger constInteger) {
      }

      @Override
      public void visit(ConstDecimal constDecimal) {
      }

      @Override
      public void visit(ConstString constString) {
      }

      @Override
      public void visit(ConstBoolean constBoolean) {
      }

      @Override
      public void visit(ConstNull constantNull) {
      }

      @Override
      public void visit(Sub sub) {
        sub.getExp1().accept(this);
        sub.getExp2().accept(this);
      }

      @Override
      public void visit(Add add) {
        add.getExp1().accept(this);
        add.getExp2().accept(this);
      }

      @Override
      public void visit(Mul mul) {
        mul.getExp1().accept(this);
        mul.getExp2().accept(this);
      }

      @Override
      public void visit(Div div) {
        div.getExp1().accept(this);
        div.getExp2().accept(this);
      }

      @Override
      public void visit(Mod mod) {
        mod.getExp1().accept(this);
        mod.getExp2().accept(this);
      }

      @Override
      public void visit(UMin uMin) {
        uMin.getExp().accept(this);
      }

      @Override
      public void visit(And and) {
        and.getExp1().accept(this);
        and.getExp2().accept(this);
      }

      @Override
      public void visit(Or or) {
        or.getExp1().accept(this);
        or.getExp2().accept(this);
      }

      @Override
      public void visit(Not not) {
        not.getExp().accept(this);
      }

      @Override
      public void visit(Equal equal) {
        equal.getExp1().accept(this);
        equal.getExp2().accept(this);
      }

      @Override
      public void visit(NotEqual notEqual) {
        notEqual.getExp1().accept(this);
        notEqual.getExp2().accept(this);
      }

      @Override
      public void visit(Greater greater) {
        greater.getExp1().accept(this);
        greater.getExp2().accept(this);
      }

      @Override
      public void visit(GreaterEqual greaterEqual) {
        greaterEqual.getExp1().accept(this);
        greaterEqual.getExp2().accept(this);
      }

      @Override
      public void visit(Less less) {
        less.getExp1().accept(this);
        less.getExp2().accept(this);
      }

      @Override
      public void visit(LessEqual lessEqual) {
        lessEqual.getExp1().accept(this);
        lessEqual.getExp2().accept(this);
      }

      @Override
      public void visit(AggrCount aggrCount) {
        aggrCount.getExp().accept(this);
      }

      @Override
      public void visit(AggrMin aggrMin) {
        aggrMin.getExp().accept(this);
      }

      @Override
      public void visit(AggrMax aggrMax) {
        aggrMax.getExp().accept(this);
      }

      @Override
      public void visit(AggrSum aggrSum) {
        aggrSum.getExp().accept(this);
      }

      @Override
      public void visit(AggrAvg aggrAvg) {
        aggrAvg.getExp().accept(this);
      }

      @Override
      public void visit(Star star) {
      }

      @Override
      public void visit(Regex regex) {
        regex.getExp1().accept(this);
        regex.getExp2().accept(this);
      }

      @Override
      public void visit(Id id) {
        id.getExp().accept(this);
      }

      @Override
      public void visit(HasProp hasProp) {
        hasProp.getExp1().accept(this);
        hasProp.getExp2().accept(this);
      }

      @Override
      public void visit(HasLabel hasLabel) {
        hasLabel.getExp1().accept(this);
        hasLabel.getExp2().accept(this);
      }

      @Override
      public void visit(VertexLabels vertexLabels) {
        vertexLabels.getExp().accept(this);
      }

      @Override
      public void visit(InDegree inDegree) {
        inDegree.getExp().accept(this);
      }

      @Override
      public void visit(OutDegree outDegree) {
        outDegree.getExp().accept(this);
      }

      @Override
      public void visit(EdgeLabel edgeLabel) {
        edgeLabel.getExp().accept(this);
      }
    });
    return result;
  }

  public static Set<Aggregation> getAggregations(QueryExpression exp) {
    final Set<Aggregation> result = new HashSet<>();
    exp.accept(new QueryExpressionVisitor() {

      @Override
      public void visit(VarRef varRef) {
      }

      @Override
      public void visit(PropertyAccess propAccess) {
      }

      @Override
      public void visit(ConstInteger constInteger) {
      }

      @Override
      public void visit(ConstDecimal constDecimal) {
      }

      @Override
      public void visit(ConstString constString) {
      }

      @Override
      public void visit(ConstBoolean constBoolean) {
      }

      @Override
      public void visit(ConstNull constantNull) {
      }

      @Override
      public void visit(Sub sub) {
        sub.getExp1().accept(this);
        sub.getExp2().accept(this);
      }

      @Override
      public void visit(Add add) {
        add.getExp1().accept(this);
        add.getExp2().accept(this);
      }

      @Override
      public void visit(Mul mul) {
        mul.getExp1().accept(this);
        mul.getExp2().accept(this);
      }

      @Override
      public void visit(Div div) {
        div.getExp1().accept(this);
        div.getExp2().accept(this);
      }

      @Override
      public void visit(Mod mod) {
        mod.getExp1().accept(this);
        mod.getExp2().accept(this);
      }

      @Override
      public void visit(UMin uMin) {
        uMin.getExp().accept(this);
      }

      @Override
      public void visit(And and) {
        and.getExp1().accept(this);
        and.getExp2().accept(this);
      }

      @Override
      public void visit(Or or) {
        or.getExp1().accept(this);
        or.getExp2().accept(this);
      }

      @Override
      public void visit(Not not) {
        not.getExp().accept(this);
      }

      @Override
      public void visit(Equal equal) {
        equal.getExp1().accept(this);
        equal.getExp2().accept(this);
      }

      @Override
      public void visit(NotEqual notEqual) {
        notEqual.getExp1().accept(this);
        notEqual.getExp2().accept(this);
      }

      @Override
      public void visit(Greater greater) {
        greater.getExp1().accept(this);
        greater.getExp2().accept(this);
      }

      @Override
      public void visit(GreaterEqual greaterEqual) {
        greaterEqual.getExp1().accept(this);
        greaterEqual.getExp2().accept(this);
      }

      @Override
      public void visit(Less less) {
        less.getExp1().accept(this);
        less.getExp2().accept(this);
      }

      @Override
      public void visit(LessEqual lessEqual) {
        lessEqual.getExp1().accept(this);
        lessEqual.getExp2().accept(this);
      }

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

      @Override
      public void visit(Star star) {
      }

      @Override
      public void visit(Regex regex) {
        regex.getExp1().accept(this);
        regex.getExp2().accept(this);
      }

      @Override
      public void visit(Id id) {
        id.getExp().accept(this);
      }

      @Override
      public void visit(HasProp hasProp) {
        hasProp.getExp1().accept(this);
        hasProp.getExp2().accept(this);
      }

      @Override
      public void visit(HasLabel hasLabel) {
        hasLabel.getExp1().accept(this);
        hasLabel.getExp2().accept(this);
      }

      @Override
      public void visit(VertexLabels vertexLabels) {
        vertexLabels.getExp().accept(this);
      }

      @Override
      public void visit(InDegree inDegree) {
        inDegree.getExp().accept(this);
      }

      @Override
      public void visit(OutDegree outDegree) {
        outDegree.getExp().accept(this);
      }

      @Override
      public void visit(EdgeLabel edgeLabel) {
        edgeLabel.getExp().accept(this);
      }
    });
    return result;
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
    long limit = graphQuery.getLimit();
    if (limit > -1) {
      result += "\nLIMIT " + limit;
    }
    long offset = graphQuery.getOffset();
    if (offset > -1) {
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

  public static String printPgqlString(VarRef varRef) {
    QueryVariable variable = varRef.getVariable();
    if (variable.getVariableType() == VariableType.EXP_AS_VAR) {
      ExpAsVar expAsVar = (ExpAsVar) variable;
      if (expAsVar.isAnonymous()) {
        // e.g. in "SELECT x.inDegree() WHERE (n) GROUP BY x.inDegree()", the SELECT expression "x.inDegree()"
        // is a VarRef to the anonymous GROUP BY expression "x.inDegree()"
        return expAsVar.getExp().toString();
      }
      else {
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
    if (a.isKleenePlus() != b.isKleenePlus()) {
      return false;
    }
    if (a.getMaxRepetition() != b.getMaxRepetition()) {
      return false;
    }
    if (!a.getVertices().equals(b.getVertices())) {
      return false;
    }
    if (!a.getConnections().equals(b.getConnections())) {
      return false;
    }
    if (!a.getDirections().equals(b.getDirections())) {
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

  public static String printPgqlString(GraphPattern graphPattern) {
    String result = "WHERE\n";

    Set<QueryExpression> constraints = new HashSet<>(graphPattern.getConstraints());

    Map<QueryVertex, String> vertexStrings = getStringsForVerticesWithInlinedConstraints(graphPattern.getVertices(),
        constraints);

    Iterator<VertexPairConnection> it2 = graphPattern.getConnections().iterator();

    List<QueryPath> queryPaths = new ArrayList<>();
    while (it2.hasNext()) {
      VertexPairConnection connection = it2.next();

      result += "  ";
      result += printVertex(connection.getSrc(), vertexStrings);

      switch (connection.getVariableType()) {
        case EDGE:
          QueryEdge edge = (QueryEdge) connection;
          result += " -[";
          result += printInlinedConstraints(constraints, edge);
          result += "]-> ";
          break;
        case PATH:
          QueryPath path = (QueryPath) connection;
          result += " -/:type";
          result += getPathId(path, queryPaths);
          result += "*/-> ";
          break;
        default:
          break;
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
    for (VertexPairConnection connection : graphPattern.getConnections()) {
      if (connection.getVariableType() == VariableType.PATH) {
        QueryPath path = (QueryPath) connection;
        int numPathPatternsBefore = queryPaths.size();
        int pathId = getPathId(path, queryPaths);
        int numPathPatternsAfter = queryPaths.size();
        if (numPathPatternsBefore != numPathPatternsAfter) {
          // the path has already been printed
          continue;
        }

        result += "PATH type" + pathId + " := ";

        Set<QueryExpression> constraints = new HashSet<>(path.getConstraints());

        Map<QueryVertex, String> vertexStrings = getStringsForVerticesWithInlinedConstraints(path.getVertices(),
            constraints);

        Iterator<Direction> directionsIt = path.getDirections().iterator();
        Iterator<QueryVertex> verticesIt = path.getVertices().iterator();

        QueryVertex vertex = verticesIt.next();
        result += printVertex(vertex, vertexStrings);
        for (VertexPairConnection connection2 : path.getConnections()) {
          Direction direction = directionsIt.next();

          switch (connection2.getVariableType()) {
            case EDGE:
              QueryEdge edge = (QueryEdge) connection2;
              result += direction == Direction.OUTGOING ? " -[" : " <-[";
              result += printInlinedConstraints(constraints, edge);
              result += direction == Direction.OUTGOING ? "]-> " : "]- ";
              break;
            case PATH:
              throw new UnsupportedOperationException("nested Kleene star not yet supported");
            default:
              throw new UnsupportedOperationException("variable type not supported: " + connection2.getVariableType());
          }

          vertex = verticesIt.next();
          result += printVertex(vertex, vertexStrings);
        }

        result += "\n";
      }
    }

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
