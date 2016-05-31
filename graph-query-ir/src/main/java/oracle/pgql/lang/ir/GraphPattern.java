package oracle.pgql.lang.ir;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import oracle.pgql.lang.ir.QueryPath.Direction;
import oracle.pgql.lang.ir.QueryVariable.VariableType;

public class GraphPattern {

  private final Set<QueryVertex> vertices;

  private final Set<VertexPairConnection> connections;

  private final Set<QueryExpression> constraints;

  public GraphPattern(Set<QueryVertex> vertices, Set<VertexPairConnection> connections,
      Set<QueryExpression> constraints) {
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public Set<VertexPairConnection> getConnections() {
    return connections;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    String result = "WHERE\n";

    Set<QueryExpression> constraints = new HashSet<>(this.constraints);

    Map<QueryVertex, String> vertexStrings = getStringsForVerticesWithInlinedConstraints(vertices, constraints);

    Iterator<VertexPairConnection> it2 = connections.iterator();
    int pathCounter = 0;
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
          result += " -/:type";
          result += pathCounter;
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

    if (connections.isEmpty() == false && vertexStrings.isEmpty() == false) {
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

  protected String printPathPatterns() {
    String result = "";
    int counter = 0;

    for (VertexPairConnection connection : connections) {
      if (connection.getVariableType() == VariableType.PATH) {
        QueryPath path = (QueryPath) connection;
        result += "PATH type" + counter++ + " := ";

        Map<QueryVertex, String> vertexStrings = getStringsForVerticesWithInlinedConstraints(path.getVertices(),
            path.getConstraints());

        Iterator<Direction> directionsIt = path.getDirection().iterator();
        Iterator<QueryVertex> verticesIt = path.getVertices().iterator();
        
        QueryVertex vertex = verticesIt.next();
        result += printVertex(vertex, vertexStrings);
        for (VertexPairConnection connection2 : path.getConnections()) {
          Direction direction = directionsIt.next();

          switch (connection2.getVariableType()) {
            case EDGE:
              QueryEdge edge = (QueryEdge) connection2;
              result += direction == Direction.OUTGOING ? " -[" : " <-[";
              result += printInlinedConstraints(path.getConstraints(), edge);
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

  private String printVertex(QueryVertex vertex, Map<QueryVertex, String> stringForVerticesWithInlinedConstraints) {
    if (stringForVerticesWithInlinedConstraints.containsKey(vertex)) {
      String result = stringForVerticesWithInlinedConstraints.get(vertex);
      stringForVerticesWithInlinedConstraints.remove(vertex);
      return result;
    } else {
      return vertex.isAnonymous() ? "()" : vertex.name;
    }
  }

  private HashMap<QueryVertex, String> getStringsForVerticesWithInlinedConstraints(Collection<QueryVertex> vertices,
      Set<QueryExpression> constraints) {
    HashMap<QueryVertex, String> result = new HashMap<>();
    for (QueryVertex vertex : vertices) {
      String vertexString = "(";
      vertexString += printInlinedConstraints(constraints, vertex);
      vertexString += ")";
      result.put(vertex, vertexString);
    }
    return result;
  }

  private String printInlinedConstraints(Set<QueryExpression> constraints, QueryVariable variable) {
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
