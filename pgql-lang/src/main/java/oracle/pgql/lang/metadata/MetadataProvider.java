/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * All method in this class return an Optional so that implementations can choose to only implement a subset and/or
 * incrementally provide more metadata
 */
public abstract class MetadataProvider {

  /**
   * Used to check existence of vertex labels in label predicates. In the future may be used to provide suggestions when
   * labels are misspelled.
   * 
   * @return the vertex labels in the graph
   */
  public Optional<Set<String>> getVertexLabels() {
    return Optional.empty();
  }

  /**
   * Used to check existence of edge labels in label predicates. In the future may be used to provide suggestions when
   * labels are misspelled.
   * 
   * @return the edge labels in the graph
   */
  public Optional<Set<String>> getEdgeLabels() {
    return Optional.empty();
  }

  /**
   * Used to check the existence of vertex properties in value expressions. In the future may be used to provide
   * suggestions when property names are misspelled.
   * 
   * @return the vertex labels that have the given property
   */
  public Optional<Set<String>> getVertexPropertyNames(String label) {
    return Optional.empty();
  }

  /**
   * Used to check the existence of edge properties in value expressions. In the future may be used to provide
   * suggestions when property names are misspelled.
   * 
   * @return the edge labels that have the given property
   */
  public Optional<Set<String>> getEdgePropertyNames(String label) {
    return Optional.empty();
  }

  /**
   * @param label
   * @param propertyName
   * @return the vertex property type name
   */
  public Optional<String> getVertexPropertyType(String label, String propertyName) {
    return Optional.empty();
  }

  /**
   * @param label
   * @param propertyName
   * @return the edge property type name
   */
  public Optional<String> getEdgePropertyType(String label, String propertyName) {
    return Optional.empty();
  }

  /**
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
   * Get the data type of a function's return type.
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
}
