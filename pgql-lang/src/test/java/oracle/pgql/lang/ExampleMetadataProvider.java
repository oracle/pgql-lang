/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import oracle.pgql.lang.metadata.BinaryOperation;
import oracle.pgql.lang.metadata.EdgeLabel;
import oracle.pgql.lang.metadata.GraphSchema;
import oracle.pgql.lang.metadata.Property;
import oracle.pgql.lang.metadata.VertexLabel;

public class ExampleMetadataProvider extends AbstractMetadataProvider {

  @Override
  public Optional<GraphSchema> getGraphSchema() {
    List<VertexLabel> vertexLabels = new ArrayList<>();

    List<Property> personProperties = new ArrayList<>();
    personProperties.add(new Property("firstName", "STRING"));
    personProperties.add(new Property("dob", "DATE"));
    personProperties.add(new Property("numericProp", "INTEGER"));
    personProperties.add(new Property("typeConflictProp", "BOOLEAN"));
    vertexLabels.add(new VertexLabel("Person", personProperties));

    List<Property> universityProperties = new ArrayList<>();
    universityProperties.add(new Property("name", "STRING"));
    universityProperties.add(new Property("numericProp", "DOUBLE"));
    universityProperties.add(new Property("typeConflictProp", "DATE"));
    vertexLabels.add(new VertexLabel("University", universityProperties));

    List<EdgeLabel> edgeLabels = new ArrayList<>();

    List<Property> knowsProperties = new ArrayList<>();
    knowsProperties.add(new Property("since", "DATE"));
    knowsProperties.add(new Property("prop", "TIME"));
    edgeLabels.add(new EdgeLabel("knows", knowsProperties));

    List<Property> studyAtProperties = new ArrayList<>();
    studyAtProperties.add(new Property("since", "DATE"));
    studyAtProperties.add(new Property("PROP", "TIME WITH TIME ZONE"));
    edgeLabels.add(new EdgeLabel("studyAt", studyAtProperties));

    GraphSchema graphSchema = new GraphSchema(vertexLabels, edgeLabels);
    return Optional.of(graphSchema);
  }

  @Override
  public Optional<GraphSchema> getGraphSchema(SchemaQualifiedName graphName) {
    if (graphName.getName().equals("financialNetwork")
        || graphName.getName().equals("financialNetwork".toUpperCase())) {
      List<VertexLabel> vertexLabels = new ArrayList<>();

      List<Property> accountProperties = new ArrayList<>();
      accountProperties.add(new Property("number", "LONG"));
      vertexLabels.add(new VertexLabel("Account", accountProperties));

      List<Property> companyProperties = new ArrayList<>();
      companyProperties.add(new Property("name", "STRING"));
      vertexLabels.add(new VertexLabel("Company", companyProperties));

      List<Property> personProperties = new ArrayList<>();
      personProperties.add(new Property("name", "STRING"));
      vertexLabels.add(new VertexLabel("Person", personProperties));

      List<EdgeLabel> edgeLabels = new ArrayList<>();

      List<Property> transactionProperties = new ArrayList<>();
      transactionProperties.add(new Property("amount", "DOUBLE"));
      edgeLabels.add(new EdgeLabel("transaction", transactionProperties));

      List<Property> worksForProperties = Collections.emptyList();
      edgeLabels.add(new EdgeLabel("worksFor", worksForProperties));

      List<Property> ownerProperties = Collections.emptyList();
      edgeLabels.add(new EdgeLabel("owner", ownerProperties));

      GraphSchema graphSchema = new GraphSchema(vertexLabels, edgeLabels);
      return Optional.of(graphSchema);
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> getDefaultStringType() {
    return Optional.of("STRING");
  }

  @Override
  public Optional<String> getDefaultShortIntegerType() {
    return Optional.of("INTEGER");
  }

  @Override
  public Optional<String> getDefaultLongIntegerType() {
    return Optional.of("LONG");
  }

  @Override
  public Optional<String> getDefaultDecimalType() {
    return Optional.of("DOUBLE");
  }

  @Override
  public Optional<String> getUnionType(String typeA, String typeB) {
    Optional<String> resultForNumerics = getUnionTypeForNumerics(typeA, typeB);
    if (resultForNumerics.isPresent()) {
      return resultForNumerics;
    }

    Optional<String> resultForDatetimes = getUnionTypeForDatetimes(typeA, typeB);
    if (resultForDatetimes.isPresent()) {
      return resultForDatetimes;
    }

    return typeA.equals(typeB) ? Optional.of(typeA) : Optional.empty();
  }

  public Optional<String> getUnionTypeForNumerics(String typeA, String typeB) {
    switch (typeA) {
      case "INTEGER":
        switch (typeB) {
          case "INTEGER":
            return Optional.of("INTEGER");
          case "LONG":
            return Optional.of("LONG");
          case "FLOAT":
            return Optional.of("FLOAT");
          case "DOUBLE":
            return Optional.of("DOUBLE");
          default:
            return Optional.empty();
        }
      case "LONG":
        switch (typeB) {
          case "INTEGER":
          case "LONG":
            return Optional.of("LONG");
          case "FLOAT":
            return Optional.of("FLOAT");
          case "DOUBLE":
            return Optional.of("DOUBLE");
          default:
            return Optional.empty();
        }
      case "FLOAT":
        switch (typeB) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
            return Optional.of("FLOAT");
          case "DOUBLE":
            return Optional.of("DOUBLE");
          default:
            return Optional.empty();
        }
      case "DOUBLE":
        switch (typeB) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
          case "DOUBLE":
            return Optional.of("DOUBLE");
          default:
            return Optional.empty();
        }
      default:
        return Optional.empty();
    }
  }

  public Optional<String> getUnionTypeForDatetimes(String typeA, String typeB) {
    switch (typeA) {
      case "DATE":
        return typeB.equals("DATE") ? Optional.of("DATE") : Optional.empty();
      case "TIME":
        switch (typeB) {
          case "TIME":
            return Optional.of("TIME");
          case "TIME WITH TIME ZONE":
            return Optional.of("TIME WITH TIME ZONE");
          default:
            return Optional.empty();
        }
      case "TIME WITH TIME ZONE":
        switch (typeB) {
          case "TIME":
          case "TIME WITH TIME ZONE":
            return Optional.of("TIME WITH TIME ZONE");
          default:
            return Optional.empty();
        }
      case "TIMESTAMP":
        switch (typeB) {
          case "TIMESTAMP":
            return Optional.of("TIMESTAMP");
          case "TIMESTAMP WITH TIME ZONE":
            return Optional.of("TIMESTAMP WITH TIME ZONE");
          default:
            return Optional.empty();
        }
      case "TIMESTAMP WITH TIME ZONE":
        switch (typeB) {
          case "TIMESTAMP":
          case "TIMESTAMP WITH TIME ZONE":
            return Optional.of("TIMESTAMP WITH TIME ZONE");
          default:
            return Optional.empty();
        }
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<String> getOperationReturnType(BinaryOperation op, String typeA, String typeB) {
    switch (op) {
      case ADD:
      case SUB:
      case MUL:
      case DIV:
      case MOD:
        return getUnionTypeForNumerics(typeA, typeB);
      case EQUAL:
      case NOT_EQUAL:
        if (typeA.startsWith("ARRAY")) {
          return Optional.empty();
        }
        return getUnionType(typeA, typeB);
      case GREATER:
      case GREATER_EQUAL:
      case LESS:
      case LESS_EQUAL:
        if (getUnionTypeForNumerics(typeA, typeB).isPresent() || getUnionTypeForDatetimes(typeA, typeB).isPresent()) {
          return Optional.of("BOOLEAN");
        } else if (typeA.equals(typeB) && typeA.equals("STRING") && typeA.equals("BOOLEAN")) {
          return Optional.of("BOOLEAN");
        } else {
          return Optional.empty();
        }
      default:
        return Optional.empty();
    }
  }
}
