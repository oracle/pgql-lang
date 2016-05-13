package oracle.pgql.lang.ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GraphPattern {

  private final Set<QueryVertex> vertices;

  private final Set<QueryEdge> edges;

  private final Set<QueryExpression> constraints;

  private final Set<ReachabilityQuery> reachabilityQueries;

  private final Set<PathFindingQuery> pathFindingQueries;

  public GraphPattern(Set<QueryVertex> vertices, Set<QueryEdge> edges, Set<QueryExpression> constraints,
      Set<ReachabilityQuery> reachabilityQueries, Set<PathFindingQuery> pathFindingQueries) {
    this.vertices = vertices;
    this.edges = edges;
    this.constraints = constraints;
    this.reachabilityQueries = reachabilityQueries;
    this.pathFindingQueries = pathFindingQueries;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public Set<QueryEdge> getEdges() {
    return edges;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  public Set<ReachabilityQuery> getReachabilityQueries() {
    return reachabilityQueries;
  }

  public Set<PathFindingQuery> getPathFindingQueries() {
    return pathFindingQueries;
  }

  @Override
  public String toString() {
    String result = "WHERE\n";

    Set<QueryExpression> constraints = new HashSet<>(this.constraints);
    
    HashMap<QueryVertex, String> vertexStrings = new HashMap<>();
    for (QueryVertex vertex : vertices) {
      String vertexString = "(";
      vertexString += toStringHelperForInlinedConstraints(constraints, vertex);
      vertexString += ")";
      vertexStrings.put(vertex, vertexString);
    }
    
    Iterator<QueryEdge> it2 = edges.iterator();
    while (it2.hasNext()) {
      QueryEdge edge = it2.next();
      result += "  ";
      if (vertexStrings.containsKey(edge.getSrc())) {
        result += vertexStrings.get(edge.getSrc());
        vertexStrings.remove(edge.getSrc());
      } else {
        result += edge.isAnonymous() ? "()" : edge.getSrc().name;
      }
      result += " -[";
      result += toStringHelperForInlinedConstraints(constraints, edge);
      result += "]-> ";
      if (vertexStrings.containsKey(edge.getDst())) {
        result += vertexStrings.get(edge.getDst());
        vertexStrings.remove(edge.getDst());
      } else {
        result += edge.isAnonymous() ? "()" : edge.getDst().name;
      }
      if (it2.hasNext()) {
        result += ",\n";
      }
    }
    
    if (vertexStrings.isEmpty() == false) {
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
    result += "\n";
    return result;
  }
  
  private String toStringHelperForInlinedConstraints(Set<QueryExpression> constraints,
      QueryVariable variable) {
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
}
