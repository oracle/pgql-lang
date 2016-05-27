package oracle.pgql.lang.ir;

public abstract class VertexPairConnection extends QueryVariable {

  private final QueryVertex src;
  
  private final QueryVertex dst;

  public VertexPairConnection(QueryVertex src, QueryVertex dst, String name) {
    super(name);
    this.src = src;
    this.dst = dst;
  }

  public VertexPairConnection(QueryVertex src, QueryVertex dst) {
    super();
    this.src = src;
    this.dst = dst;
  }

  public QueryVertex getSrc() {
    return src;
  }

  public QueryVertex getDst() {
    return dst;
  }
}
