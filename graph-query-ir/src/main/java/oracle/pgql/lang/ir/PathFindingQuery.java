package oracle.pgql.lang.ir;

import oracle.pgql.lang.ir.QueryVariable.VariableType;

public class PathFindingQuery extends QueryVariable implements VertexPairConnection {
 
  private final QueryVertex src;
  
  private final QueryVertex dst;
  
  private final PathPattern pathPattern;

  private final int k; // number of shortest paths to find

  public PathFindingQuery(String name, QueryVertex src, QueryVertex dst, PathPattern pathPattern,
      int k) {
    super(name);
    this.src = src;
    this.dst = dst;
    this.k = k;
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
  
  public int getK() {
    return k;
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.PATH_FINDING_QUERY;
  }


  @Override
  public VariableType getVariableType() {
    return VariableType.PATH;
  }
}
