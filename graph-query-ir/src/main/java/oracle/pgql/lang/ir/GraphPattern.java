package oracle.pgql.lang.ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GraphPattern {

  private final Set<QueryVertex> vertices;

  private final Set<QueryEdge> edges;

  private final Set<PathPattern> paths;

  private final Set<QueryExpression> constraints;

  public GraphPattern(Set<QueryVertex> vertices, Set<QueryEdge> edges, Set<PathPattern> paths,
      Set<QueryExpression> constraints) {
    this.vertices = vertices;
    this.edges = edges;
    this.paths = paths;
    this.constraints = constraints;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public Set<QueryEdge> getEdges() {
    return edges;
  }

  public Set<PathPattern> getPaths() {
    return paths;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
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
    
    if (edges.isEmpty() == false && vertexStrings.isEmpty() == false) {
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
