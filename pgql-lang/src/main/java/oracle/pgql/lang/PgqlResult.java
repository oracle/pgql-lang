/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.StatementType;

public class PgqlResult {

  private final String queryString;

  private final String errorMessages;

  private final boolean queryValid;

  private final PgqlStatement pgqlStatement;

  private final ISpoofaxParseUnit spoofaxParseUnit;

  private final PgqlVersion pgqlVersion;

  private final int bindVariableCount;

  public PgqlResult(String queryString, boolean queryValid, String messages, PgqlStatement pgqlStatement,
      ISpoofaxParseUnit spoofaxParseUnit, PgqlVersion pgqlVersion, int bindVariableCount) {
    this.queryString = queryString;
    this.errorMessages = messages;
    this.queryValid = queryValid;
    this.pgqlStatement = pgqlStatement;
    this.spoofaxParseUnit = spoofaxParseUnit;
    this.pgqlVersion = pgqlVersion;
    this.bindVariableCount = bindVariableCount;
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
    if (pgqlStatement == null) {
      return null;
    } else if (pgqlStatement.getStatementType() == StatementType.SELECT
        || pgqlStatement.getStatementType() == StatementType.GRAPH_MODIFY) {
      return (GraphQuery) pgqlStatement;
    } else {
      throw new IllegalStateException(
          "Use getStatement() instead of getGraphQuery() if statment is not a SELECT or graph INSERT/UPDATE/DELETE query");
    }
  }

  public PgqlStatement getPgqlStatement() {
    return pgqlStatement;
  }

  public PgqlVersion getPgqlVersion() {
    return pgqlVersion;
  }

  public int getBindVariableCount() {
    return bindVariableCount;
  }

  protected ISpoofaxParseUnit getSpoofaxParseUnit() {
    return spoofaxParseUnit;
  }
}
