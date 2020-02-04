/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir.modify;

import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;

public abstract class AbstractInsertion implements Insertion {

  protected List<QueryExpression> labels;

  protected List<SetPropertyExpression> properties;

  public AbstractInsertion(List<QueryExpression> labels, List<SetPropertyExpression> properties) {
    this.labels = labels;
    this.properties = properties;
  }

  public List<QueryExpression> getLabels() {
    return labels;
  }

  public void setLabels(List<QueryExpression> labels) {
    this.labels = labels;
  }

  public List<SetPropertyExpression> getProperties() {
    return properties;
  }

  public void setProperties(List<SetPropertyExpression> properties) {
    this.properties = properties;
  }

  protected String printLabels() {
    if (labels.isEmpty()) {
      return "";
    } else {
      return " LABELS ( " + labels.stream() //
          .map(x -> printIdentifier(((ConstString) x).getValue())) //
          .collect(Collectors.joining(", ")) + " )";
    }
  }

  protected String printProperties() {
    if (properties.isEmpty()) {
      return "";
    } else {
      return " PROPERTIES ( " + properties.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(", ")) + " )";
    }
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
    AbstractInsertion other = (AbstractInsertion) obj;
    if (labels == null) {
      if (other.labels != null)
        return false;
    } else if (!labels.equals(other.labels))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    return true;
  }
}
