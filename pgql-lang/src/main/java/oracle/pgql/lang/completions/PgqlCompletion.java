/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

public class PgqlCompletion {

  private final String name;

  private final String value;

  private final String meta;

  public PgqlCompletion(String name, String value, String meta) {
    this.name = name;
    this.value = value;
    this.meta = meta;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getMeta() {
    return meta;
  }

  @Override
  public String toString() {
    return name + "\n  " + value + "\n  " + meta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((meta == null) ? 0 : meta.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PgqlCompletion other = (PgqlCompletion) obj;
    if (meta == null) {
      if (other.meta != null)
        return false;
    } else if (!meta.equals(other.meta))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
