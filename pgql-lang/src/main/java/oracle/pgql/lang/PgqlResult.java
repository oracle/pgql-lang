/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.Statement;
import oracle.pgql.lang.ir.StatementType;

public class PgqlResult {

  private final String queryString;

  private final String errorMessages;

  private final boolean queryValid;

  private final Statement statement;

  private final ISpoofaxParseUnit spoofaxParseUnit;

  public PgqlResult(String queryString, boolean queryValid, String messages, Statement statement,
      ISpoofaxParseUnit spoofaxParseUnit) {
    this.queryString = queryString;
    this.errorMessages = messages;
    this.queryValid = queryValid;
    this.statement = statement;
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
    if (statement == null) {
      return null;
    } else if (statement.getStatementType() == StatementType.SELECT
        || statement.getStatementType() == StatementType.GRAPH_MODIFY) {
      return (GraphQuery) statement;
    } else {
      throw new IllegalStateException(
          "Use getStatement() instead of getGraphQuery() if statment is not a SELECT or graph INSERT/UPDATE/DELETE query");
    }
  }

  public Statement getStatement() {
    return statement;
  }

  protected ISpoofaxParseUnit getSpoofaxParseUnit() {
    return spoofaxParseUnit;
  }
}
