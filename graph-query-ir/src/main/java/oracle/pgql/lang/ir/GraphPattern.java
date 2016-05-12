package oracle.pgql.lang.ir;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GraphPattern {
  
  private final Set<QueryVertex> vertices;
  
  private final Set<QueryEdge> edges;
  
  private final Set<QueryExpression> constraints;
  
  private final Set<ReachabilityQuery> reachabilityQueries;

  private final Set<PathFindingQuery> pathFindingQueries;
  
  public GraphPattern(Set<QueryVertex> vertices, Set<QueryEdge> edges, Set<QueryExpression> constraints,
      Set<ReachabilityQuery> reachabilityQueries, Set<PathFindingQuery> pathFindingQueries) {
    this.vertices = vertices;
    this.edges = edges;
    this.constraints = constraints;
    this.reachabilityQueries = reachabilityQueries;
    this.pathFindingQueries = pathFindingQueries;
  }
  
  public Set<QueryVertex> getVertices() {
    return vertices;
  }

  public Set<QueryEdge> getEdges() {
    return edges;
  }


  public Set<QueryExpression> getConstraints() {
    return constraints;
  }

  public Set<ReachabilityQuery> getReachabilityQueries() {
    return reachabilityQueries;
  }

  public Set<PathFindingQuery> getPathFindingQueries() {
    return pathFindingQueries;
  }

  @Override
  public String toString() {
    Set<Object> elements = new HashSet<>();
    elements.addAll(edges);
    elements.addAll(constraints);
    
    String result = "WHERE\n";
    Iterator<Object> it = elements.iterator();
    while (it.hasNext()) {
      result += "  " + it.next();
      if (it.hasNext()) {
        result += ",\n";
      }
    }
    return result;
  }
}
