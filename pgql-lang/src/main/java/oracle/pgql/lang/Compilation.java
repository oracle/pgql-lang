/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import oracle.pgql.lang.ir.QueryGraph;

public class Compilation {

  private final String queryString;
  private final String messages;
  private final boolean queryValid;
  private final QueryGraph queryGraph;

  public Compilation(String queryString, boolean queryValid, String messages, QueryGraph queryGraph) {
    this.queryString = queryString;
    this.messages = messages;
    this.queryValid = queryValid;
    this.queryGraph = queryGraph;
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
  public String getMessages() {
    return messages;
  }

  /**
   * @return a QueryGraph object if the query is valid; null otherwise
   */
  public QueryGraph getQueryGraph() {
    return queryGraph;
  }
}
