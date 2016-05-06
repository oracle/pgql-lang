package oracle.pgql.lang.ir;

import java.util.List;
import java.util.Set;

public class PathPattern {
  
  private final Set<QueryVertex> vertices;
  
  private final List<VertexPairConnection> connections;
  
  private final int minHopCount;
  
  private final int maxHopCount;
  
  public PathPattern(Set<QueryVertex> vertices, List<VertexPairConnection> connections,
      int minHopCount, int maxHopCount) {
    this.vertices = vertices;
    this.connections = connections;
    this.minHopCount = minHopCount;
    this.maxHopCount = maxHopCount;
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
  
  class PathPatternElem {
    
    
    
    
  }
}
