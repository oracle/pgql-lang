/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.FunctionCall;
import oracle.pgql.lang.ir.QueryExpression.LogicalExpression.Or;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVariable.VariableType;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;
import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import oracle.pgql.lang.metadata.GraphSchema;
import oracle.pgql.lang.metadata.Label;

import static oracle.pgql.lang.ir.PgqlUtils.isHasLabelFunctionForVar;

public class PgqlResult {

  private final String queryString;

  private final String errorMessages;

  private final boolean queryValid;

  private final PgqlStatement pgqlStatement;

  private final PgqlVersion pgqlVersion;

  private final int bindVariableCount;

  private final boolean querySelectsAllProperties;

  private final AbstractMetadataProvider metadataProvider;

  public PgqlResult(String queryString, boolean queryValid, String messages, PgqlStatement pgqlStatement,
      PgqlVersion pgqlVersion, int bindVariableCount, boolean querySelectsAllProperties,
      AbstractMetadataProvider metadataProvider) {
    this.queryString = queryString;
    this.errorMessages = messages;
    this.queryValid = queryValid;
    this.pgqlStatement = pgqlStatement;
    this.pgqlVersion = pgqlVersion;
    this.bindVariableCount = bindVariableCount;
    this.querySelectsAllProperties = querySelectsAllProperties;
    this.metadataProvider = metadataProvider;
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
   * @return a GraphQuery object if the query is valid (check {@link #isQueryValid()}). In case the query is invalid and
   *         contains syntax or semantic errors then this method _may_ return null depending on how recoverable the
   *         query text is.
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

  /**
   * @return a PgqlStatement object if the query is valid (check {@link #isQueryValid()}). In case the statement is
   *         invalid and contains syntax or semantic errors then this method _may_ return null depending on how
   *         recoverable the statement text is.
   */
  public PgqlStatement getPgqlStatement() {
    return pgqlStatement;
  }

  public PgqlVersion getPgqlVersion() {
    return pgqlVersion;
  }

  public int getBindVariableCount() {
    return bindVariableCount;
  }

  public boolean querySelectsAllProperties() {
    return querySelectsAllProperties;
  }

  /**
   * Given a vertex or edge variable that appears in the graph pattern, returns a list of visible properties taking into
   * account the label expressions defined for the vertex or edge.
   *
   * @param vertexOrEdge
   *          the vertex or edge from the graph pattern
   * @return the list of properties
   */
  public List<String> getAllProperties(QueryVariable vertexOrEdge) {
    if (pgqlStatement.getStatementType() != StatementType.SELECT) {
      throw new IllegalStateException("SELECT query expected but was " + pgqlStatement.getStatementType());
    }
    if (metadataProvider == null || metadataProvider.getGraphSchema() == null) {
      throw new IllegalStateException("Graph schema was not provided to the parser");
    }

    boolean isVertex = vertexOrEdge.getVariableType() == VariableType.VERTEX;

    List<String> result = null;
    for (QueryExpression exp : getGraphQuery().getGraphPattern().getConstraints()) {
      if (isHasLabelFunctionForVar(exp, vertexOrEdge)) {
        List<String> properties = getAllProperties(exp, isVertex);
        if (result == null) {
          result = properties;
        } else {
          result.retainAll(properties); // compute the intersection since this is a conjunction (AND)
        }
      }
    }

    // no label expression exist, so we return the union of properties of all labels
    if (result == null) {
      List<? extends Label> labels = getLabels(isVertex);
      LinkedHashSet<String> properties = new LinkedHashSet<>();
      for (Label l : labels) {
        properties.addAll(l.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList()));
      }
      result = new ArrayList<String>(properties);
    }

    return result;
  }

  private List<String> getAllProperties(QueryExpression exp, boolean isVertex) {
    switch (exp.getExpType()) {
      case FUNCTION_CALL:
        return getAllProperties((FunctionCall) exp, isVertex);
      case OR:
        return getAllProperties((Or) exp, isVertex);
      default:
        throw new IllegalStateException("Unsupported expression type " + pgqlStatement.getStatementType());
    }
  }

  private List<String> getAllProperties(FunctionCall functionCall, boolean isVertex) {
    String label = ((ConstString) functionCall.getArgs().get(1)).getValue();
    List<? extends Label> labels = getLabels(isVertex);

    // first try to find an exact match
    for (Label l : labels) {
      if (l.getLabel().equals(label)) {
        return l.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList());
      }
    }

    // now try to find an inexact match
    for (Label l : labels) {
      if (l.getLabel().toUpperCase().equals(label)) {
        return l.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList());
      }
    }

    throw new IllegalStateException("Label " + label + " does not exist");
  }

  private List<String> getAllProperties(Or or, boolean isVertex) {
    List<String> result = getAllProperties(or.getExp1(), isVertex);
    result.addAll(getAllProperties(or.getExp2(), isVertex)); // compute the union since this is a disjunction (OR)
    return result;
  }

  private List<? extends Label> getLabels(boolean isVertex) {
    SchemaQualifiedName graphName = getGraphQuery().getGraphName();
    GraphSchema graphSchema = graphName == null ? metadataProvider.getGraphSchema().get()
        : metadataProvider.getGraphSchema(graphName).get();
    List<? extends Label> labels = isVertex ? graphSchema.getVertexLabels() : graphSchema.getEdgeLabels();
    return labels;
  }
}
