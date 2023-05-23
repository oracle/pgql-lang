/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.SchemaQualifiedName;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public class BaseGraph {

  private SchemaQualifiedName graphName;

  /**
   * This field is null unless the user specified ELEMENT TABLES ( .. )
   */
  private List<BaseElementTable> baseElementTables;

  /**
   * This field is null unless the user specified ALL ELEMENT TABLES EXCEPT ( .. )
   */
  private List<String> allElementTablesExcept;

  public BaseGraph(SchemaQualifiedName graphName, List<BaseElementTable> baseElementTables,
      List<String> allElementTablesExcept) {
    this.graphName = graphName;
    this.baseElementTables = baseElementTables;
    this.allElementTablesExcept = allElementTablesExcept;
  }

  public SchemaQualifiedName getGraphName() {
    return graphName;
  }

  public void setGraphName(SchemaQualifiedName graphName) {
    this.graphName = graphName;
  }

  public List<BaseElementTable> getBaseElementTables() {
    return baseElementTables;
  }

  public void setBaseElementTables(List<BaseElementTable> baseElementTables) {
    this.baseElementTables = baseElementTables;
  }

  public List<String> getAllElementTablesExcept() {
    return allElementTablesExcept;
  }

  public void setAllElementTablesExcept(List<String> allElementTablesExcept) {
    this.allElementTablesExcept = allElementTablesExcept;
  }

  @Override
  public String toString() {
    String result = graphName.toString(true);
    if (baseElementTables != null) {
      result += " ELEMENT TABLES ( " //
          + baseElementTables.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) //
          + ")";
    } else if (allElementTablesExcept != null) {
      result += " ALL ELEMENT TABLES EXCEPT ( " //
          + allElementTablesExcept.stream().map(x -> printIdentifier(x)).collect(Collectors.joining(", ")) //
          + " )";
    } else {
      result += " ALL ELEMENT TABLES";
    }

    return result;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BaseGraph other = (BaseGraph) obj;
    if (allElementTablesExcept == null) {
      if (other.allElementTablesExcept != null)
        return false;
    } else if (!allElementTablesExcept.equals(other.allElementTablesExcept))
      return false;
    if (baseElementTables == null) {
      if (other.baseElementTables != null)
        return false;
    } else if (!baseElementTables.equals(other.baseElementTables))
      return false;
    if (graphName == null) {
      if (other.graphName != null)
        return false;
    } else if (!graphName.equals(other.graphName))
      return false;
    return true;
  }
}
