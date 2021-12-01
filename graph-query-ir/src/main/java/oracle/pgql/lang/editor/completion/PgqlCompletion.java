/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.editor.completion;

public class PgqlCompletion {

  private final String value;

  private final String meta;

  public PgqlCompletion(String value, String meta) {
    this.value = value;
    this.meta = meta;
  }

  public String getValue() {
    return value;
  }

  public String getMeta() {
    return meta;
  }

  @Override
  public String toString() {
    return value + "\t\t" + meta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((meta == null) ? 0 : meta.hashCode());
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
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
