package oracle.pgql.lang.ir;

import java.util.Set;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class GraphPattern {

  private final Set<QueryVertex> vertices;

  private final Set<VertexPairConnection> connections;

  private final Set<QueryExpression> constraints;

  public GraphPattern(Set<QueryVertex> vertices, Set<VertexPairConnection> connections,
      Set<QueryExpression> constraints) {
    this.vertices = vertices;
    this.connections = connections;
    this.constraints = constraints;
  }

  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public Set<VertexPairConnection> getConnections() {
    return connections;
  }

  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
  }
}
