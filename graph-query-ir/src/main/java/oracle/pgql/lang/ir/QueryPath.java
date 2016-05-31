package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class QueryPath extends VertexPairConnection {

  public enum Direction {
    OUTGOING,
    INCOMING
  }

  private final List<QueryVertex> vertices;

  private final List<VertexPairConnection> connections;

  private final List<Direction> directions;

  private final Set<QueryExpression> constraints;

  /**
   * Reachability query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections, List<Direction> directions,
      Set<QueryExpression> constraints) {
    super(src, dst);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
  }

  /**
   * Path finding query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections, List<Direction> directions,
      Set<QueryExpression> constraints, String name) {
    super(src, dst, name);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
  }

  public List<QueryVertex> getVertices() {
    return vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }
  
  public List<Direction> getDirection() {
    return directions;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
