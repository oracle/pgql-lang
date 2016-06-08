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
  
  private final long minRepetition;
  
  private final long maxRepetition; // -1 for unbounded repetition

  /**
   * Reachability query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections, List<Direction> directions,
      Set<QueryExpression> constraints, long minRepetition, long maxRepetition) {
    super(src, dst);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
    this.minRepetition = minRepetition;
    this.maxRepetition = maxRepetition;
  }

  /**
   * Path finding query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections, List<Direction> directions,
      Set<QueryExpression> constraints, long minRepetition, long maxRepetition, String name) {
    super(src, dst, name);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
    this.minRepetition = minRepetition;
    this.maxRepetition = maxRepetition;
  }

  public List<QueryVertex> getVertices() {
    return vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }
  
  public List<Direction> getDirections() {
    return directions;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }
  
  public long getMinRepetition() {
    return minRepetition;
  }
  
  public long getMaxRepetition() {
    return maxRepetition;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
