package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class PathPattern extends QueryVariable implements VertexPairConnection {

  private final QueryVertex src;
  
  private final QueryVertex dst;

  private final Set<QueryVertex> vertices;
  
  private final List<VertexPairConnection> connections;
  
  private final int minHopCount;
  
  private final int maxHopCount;

  private final int k; // number of shortest paths to find; 0 for reachability queries
  
  /**
   * Reachability query
   */
  public PathPattern(QueryVertex src, QueryVertex dst, Set<QueryVertex> vertices, List<VertexPairConnection> connections, int minHopCount, int maxHopCount) {
    super();
    this.src = src;
    this.dst = dst;
    this.vertices = vertices;
    this.connections = connections;
    this.minHopCount = minHopCount;
    this.maxHopCount = maxHopCount;
    k = 0;
  }
  
  /**
   * Path finding query
   */
  public PathPattern(QueryVertex src, QueryVertex dst, Set<QueryVertex> vertices, List<VertexPairConnection> connections, int minHopCount, int maxHopCount, String name, int k) {
    super(name);
    this.src = src;
    this.dst = dst;
    this.vertices = vertices;
    this.connections = connections;
    this.minHopCount = minHopCount;
    this.maxHopCount = maxHopCount;
    this.k = k;
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

  public int getMinHopCount() {
    return minHopCount;
  }

  public int getMaxHopCount() {
    return maxHopCount;
  }

  public int getK() {
    return k;
  }

  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
