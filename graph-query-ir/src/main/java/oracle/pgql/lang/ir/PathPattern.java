package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class PathPattern extends QueryVariable implements VertexPairConnection {

  private final QueryVertex src;
  
  private final QueryVertex dst;

  private final Set<QueryVertex> vertices;
  
  private final List<VertexPairConnection> connections;
  
  /**
   * Reachability query
   */
  public PathPattern(QueryVertex src, QueryVertex dst, Set<QueryVertex> vertices, List<VertexPairConnection> connections) {
    super();
    this.src = src;
    this.dst = dst;
    this.vertices = vertices;
    this.connections = connections;
  }
  
  /**
   * Path finding query
   */
  public PathPattern(QueryVertex src, QueryVertex dst, Set<QueryVertex> vertices, List<VertexPairConnection> connections, String name) {
    super(name);
    this.src = src;
    this.dst = dst;
    this.vertices = vertices;
    this.connections = connections;
  }

  public QueryVertex getSrc() {
    return src;
  }

  public QueryVertex getDst() {
    return dst;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public List<VertexPairConnection> getConnections() {
    return connections;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
