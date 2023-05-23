/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;

public class CreateSuperPropertyGraph extends CreatePropertyGraph {

  private List<BaseGraph> baseGraphs;

  public CreateSuperPropertyGraph(SchemaQualifiedName graphName, List<BaseGraph> baseGraphs,
      List<VertexTable> vertexTables, List<EdgeTable> edgeTables) {
    super(graphName, vertexTables, edgeTables);
    this.baseGraphs = baseGraphs;
  }

  public List<BaseGraph> getBaseGraphs() {
    return baseGraphs;
  }

  public void setBaseGraphs(List<BaseGraph> baseGraphs) {
    this.baseGraphs = baseGraphs;
  }

  @Override
  public String toString() {
    return "CREATE PROPERTY GRAPH " + graphName.toString(true) //
        + "\n  BASE GRAPHS (\n    " + baseGraphs.stream().map(x -> x.toString()).collect(Collectors.joining(",\n    ")) + "\n  )" //
        + printVertexTables() + printEdgeTables() + printOptions();
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.CREATE_SUPER_PROPERTY_GRAPH;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    CreateSuperPropertyGraph other = (CreateSuperPropertyGraph) obj;
    if (baseGraphs == null) {
      if (other.baseGraphs != null)
        return false;
    } else if (!baseGraphs.equals(other.baseGraphs))
      return false;
    return true;
  }
}
