/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpressionVisitor;
import oracle.pgql.lang.ir.SchemaQualifiedName;

public class InsertClause implements Modification {

  private SchemaQualifiedName graphName;

  private List<Insertion> insertions;

  public InsertClause(SchemaQualifiedName graphName, List<Insertion> insertions) {
    this.graphName = graphName;
    this.insertions = insertions;
  }

  public SchemaQualifiedName getGraphName() {
    return graphName;
  }

  public void setGraphName(SchemaQualifiedName graphName) {
    this.graphName = graphName;
  }

  public List<Insertion> getInsertions() {
    return insertions;
  }

  public void setInsertions(List<Insertion> insertions) {
    this.insertions = insertions;
  }

  @Override
  public String toString() {
    String intoClause = graphName == null ? "" : "INTO " + graphName + " ";
    return "INSERT " + intoClause + insertions.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
  }

  @Override
  public ModificationType getModificationType() {
    return ModificationType.INSERT;
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
    InsertClause other = (InsertClause) obj;
    if (graphName == null) {
      if (other.graphName != null)
        return false;
    } else if (!graphName.equals(other.graphName))
      return false;
    if (insertions == null) {
      if (other.insertions != null)
        return false;
    } else if (!insertions.equals(other.insertions))
      return false;
    return true;
  }

  @Override
  public void accept(QueryExpressionVisitor v) {
    v.visit(this);
  }
}
