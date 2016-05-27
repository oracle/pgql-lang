package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class QueryPath extends VertexPairConnection {

  private final Set<QueryVertex> vertices;

  private final List<VertexPairConnection> connections;

  private final Set<QueryExpression> constraints;

  /**
   * Reachability query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, Set<QueryVertex> vertices,
      List<VertexPairConnection> connections, Set<QueryExpression> constraints) {
    super(src, dst);
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
  }

  /**
   * Path finding query
   */
  public QueryPath(QueryVertex src, QueryVertex dst, Set<QueryVertex> vertices,
      List<VertexPairConnection> connections, Set<QueryExpression> constraints, String name) {
    super(src, dst, name);
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }
  
  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
