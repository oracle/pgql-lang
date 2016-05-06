package oracle.pgql.lang.ir;

public interface VertexPairConnection {

  public enum ConnectionType {
    EDGE,
    REACHABILITY_QUERY,
    PATH_FINDING_QUERY
  }
  
  public QueryVertex getSrc();

  public QueryVertex getDst();
  
  public ConnectionType getType();
}
