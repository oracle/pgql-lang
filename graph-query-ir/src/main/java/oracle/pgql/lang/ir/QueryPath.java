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
  
  private final boolean kleenePlus; // Kleene star if false
  
  private final long maxRepetition; // -1 for unbounded repetition

  /**
   * Reachability query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections, List<Direction> directions,
      Set<QueryExpression> constraints, boolean kleenePlus, long maxRepetition) {
    super(src, dst);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
    this.kleenePlus = kleenePlus;
    this.maxRepetition = maxRepetition;
  }

  /**
   * Path finding query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, List<QueryVertex> vertices, List<VertexPairConnection> connections, List<Direction> directions,
      Set<QueryExpression> constraints, boolean kleenePlus, long maxRepetition, String name) {
    super(src, dst, name);
    this.vertices = vertices;
    this.connections = connections;
    this.directions = directions;
    this.constraints = constraints;
    this.kleenePlus = kleenePlus;
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
  
  public boolean isKleenePlus() {
    return kleenePlus;
  }
  
  public long getMaxRepetition() {
    return maxRepetition;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
