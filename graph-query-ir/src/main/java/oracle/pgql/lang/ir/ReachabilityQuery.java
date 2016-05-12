package oracle.pgql.lang.ir;

public class ReachabilityQuery implements VertexPairConnection {

  private final QueryVertex src;
  
  private final QueryVertex dst;
  
  private final PathPattern pathPattern;
  
  public ReachabilityQuery(QueryVertex src, QueryVertex dst, PathPattern pathPattern) {
    this.src = src;
    this.dst = dst;
    this.pathPattern = pathPattern;
  }
  
  @Override
  public QueryVertex getSrc() {
    return src;
  }

  @Override
  public QueryVertex getDst() {
    return dst;
  }

  public PathPattern getPathPattern() {
    return pathPattern;
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.REACHABILITY_QUERY;
  }
}
