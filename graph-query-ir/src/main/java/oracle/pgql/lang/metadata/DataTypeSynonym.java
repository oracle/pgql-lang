/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

public class DataTypeSynonym {

  private final String synonym;

  private final String dataType;

  public DataTypeSynonym(String synonym, String dataType) {
    this.synonym = synonym;
    this.dataType = dataType;
  }

  public String getSynonym() {
    return synonym;
  }

  public String getDataType() {
    return dataType;
  }
}
