package oracle.pgql.lang.ir;

import java.util.LinkedHashSet;
import java.util.Set;

public class OptionalGraphPattern extends GraphPattern {

  public OptionalGraphPattern(Set<QueryVertex> vertices, LinkedHashSet<VertexPairConnection> connections,
      LinkedHashSet<QueryExpression> constraints) {
    super(vertices, connections, constraints);
  }

  @Override
  public TableExpressionType getTableExpressionType() {
    return TableExpressionType.OPTIONAL_GRAPH_PATTERN;
  }

  @Override
  public String toString() {
    return "OPTIONAL " + super.toString();
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
