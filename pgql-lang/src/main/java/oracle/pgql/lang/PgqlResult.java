/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import oracle.pgql.lang.ir.GraphQuery;

public class PgqlResult {

  private final String queryString;
  private final String errorMessages;
  private final boolean queryValid;
  private final GraphQuery graphQuery;
  private final ISpoofaxParseUnit spoofaxParseUnit;

  public PgqlResult(String queryString, boolean queryValid, String messages, GraphQuery graphQuery,
      ISpoofaxParseUnit spoofaxParseUnit) {
    this.queryString = queryString;
    this.errorMessages = messages;
    this.queryValid = queryValid;
    this.graphQuery = graphQuery;
    this.spoofaxParseUnit = spoofaxParseUnit;
  }

  /**
   * @return the original query as String
   */
  public String getQueryString() {
    return queryString;
  }

  /**
   * @return true if the query is valid
   */
  public boolean isQueryValid() {
    return queryValid;
  }

  /**
   * @return a String with error messages if the query is not valid (see {@link #isQueryValid()}); null otherwise
   */
  public String getErrorMessages() {
    return errorMessages;
  }

  /**
   * @return a GraphQuery object if the query is valid; null otherwise
   */
  public GraphQuery getGraphQuery() {
    return graphQuery;
  }

  protected ISpoofaxParseUnit getSpoofaxParseUnit() {
    return spoofaxParseUnit;
  }
}
