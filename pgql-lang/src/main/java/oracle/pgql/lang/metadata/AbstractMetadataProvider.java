/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * All method in this class return an Optional so that implementations can choose to only implement a subset and
 * incrementally provide more metadata over time.
 */
public abstract class AbstractMetadataProvider {

  /**
   * Used to check existence of vertex labels in label predicates. In the future may be used to provide suggestions when
   * labels are misspelled.
   * 
   * @param graphName
   *          the name of the graph; null when no graph name is specified as part of the query
   * @return the vertex labels in the graph
   */
  public Optional<Set<String>> getVertexLabels(String graphName) {
    return Optional.empty();
  }

  /**
   * Used to check existence of edge labels in label predicates. In the future may be used to provide suggestions when
   * labels are misspelled.
   *
   * @param graphName
   *          the name of the graph; null when no graph name is specified as part of the query
   * @return the edge labels in the graph
   */
  public Optional<Set<String>> getEdgeLabels(String graphName) {
    return Optional.empty();
  }

  /**
   * Used to check the existence of vertex properties in value expressions. In the future may be used to provide
   * suggestions when property names are misspelled.
   *
   * @param graphName
   *          the name of the graph; null when no graph name is specified as part of the query
   * @return the edge labels in the graph
   * @return the vertex labels that have the given property
   */
  public Optional<Set<String>> getVertexPropertyNames(String graphName, String label) {
    return Optional.empty();
  }

  /**
   * Used to check the existence of edge properties in value expressions. In the future may be used to provide
   * suggestions when property names are misspelled.
   *
   * @param graphName
   *          the name of the graph; null when no graph name is specified as part of the query
   * @return the edge labels in the graph
   * @return the edge labels that have the given property
   */
  public Optional<Set<String>> getEdgePropertyNames(String graphName, String label) {
    return Optional.empty();
  }

  /**
   * Get the type of a vertex property given a graph name and a vertex label.
   *
   * @param graphName
   *          the name of the graph; null when no graph name is specified as part of the query
   * @return the edge labels in the graph
   * @param label
   * @param propertyName
   * @return the vertex property type name
   */
  public Optional<String> getVertexPropertyType(String graphName, String label, String propertyName) {
    return Optional.empty();
  }

  /**
   * Get the type of a edge property given a graph name and a edge label.
   *
   * @param graphName
   *          the name of the graph; null when no graph name is specified as part of the query
   * @return the edge labels in the graph
   * @param label
   * @param propertyName
   * @return the edge property type name
   */
  public Optional<String> getEdgePropertyType(String graphName, String label, String propertyName) {
    return Optional.empty();
  }

  /**
   * Get the return type of a unary operation.
   * 
   * @param op
   *          e.g. NOT, - (unary minus)
   * @param type
   *          e.g. BOOLEAN
   * @return the return type of the operation (e.g. LONG) or null if the operation is not defined for the input type
   */
  public Optional<Boolean> getOperationReturnType(UnaryOperation op, String type) {
    return Optional.empty();
  }

  /**
   * Get the return type of a binary operation.
   * 
   * @param op
   *          e.g. multiplication
   * @param typeA
   *          e.g. LONG
   * @param typeB
   *          e.g. INTEGER
   * @return the return type of the operation (e.g. LONG) or null if the operation is not defined for the two input
   *         types
   */
  public Optional<Boolean> getOperationReturnType(BinaryOperation op, String typeA, String typeB) {
    return Optional.empty();
  }

  /**
   * Get a function's return type.
   * 
   * @param packageName
   *          can be null
   * @param functionName
   * @param argumentTypes
   *          e.g. ["STRING", "BOOLEAN"]
   * @return the return type of the function (e.g. "LONG") or null if the function does not exist
   */
  public Optional<String> getFunctionReturnType(String packageName, String functionName, List<String> argumentTypes) {
    return Optional.empty();
  }

  /**
   * Get the union type of two data types.
   * 
   * This is used to decide on the column type of a property access when multiple vertex/edge tables have the same
   * property but with different property types. Typically, the widest type is chosen.
   * 
   * Examples:
   * 
   * - LONG, INTEGER ==> LONG
   * 
   * - VARCHAR(10), VARCHAR(20) ==> VARCHAR(20)
   * 
   * @param packageName
   * @param functionName
   * @param argumentTypes
   * @return the union type of the two type
   */
  public Optional<String> getUnionType(String typeA, String typeB) {
    return Optional.empty();
  }
}
