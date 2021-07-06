/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import oracle.pgql.lang.metadata.BinaryOperation;
import oracle.pgql.lang.metadata.DataTypeSynonym;
import oracle.pgql.lang.metadata.EdgeLabel;
import oracle.pgql.lang.metadata.FunctionSignature;
import oracle.pgql.lang.metadata.GraphSchema;
import oracle.pgql.lang.metadata.Property;
import oracle.pgql.lang.metadata.UnaryOperation;
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
    knowsProperties.add(new Property("typeConflictProp", "BOOLEAN"));
    edgeLabels.add(new EdgeLabel("knows", knowsProperties));

    List<Property> studyAtProperties = new ArrayList<>();
    studyAtProperties.add(new Property("since", "DATE"));
    studyAtProperties.add(new Property("PROP", "TIME WITH TIME ZONE"));
    studyAtProperties.add(new Property("Typeconflictprop", "INTEGER"));
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
  public Optional<String> getOperationReturnType(UnaryOperation op, String type) {
    switch (op) {
      case NOT:
        return type.equals("BOOLEAN") ? Optional.of("BOOLEAN") : Optional.empty();
      case UMIN:
        switch (type) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
          case "DOUBLE":
            return Optional.of(type);
          default:
            return Optional.empty();
        }
      case SUM:
        switch (type) {
          case "INTEGER":
          case "LONG":
            return Optional.of("LONG");
          case "FLOAT":
          case "DOUBLE":
            return Optional.of("DOUBLE");
          default:
            return Optional.empty();
        }
      case MIN:
      case MAX:
        switch (type) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
          case "DOUBLE":
          case "BOOLEAN":
          case "STRING":
          case "DATE":
          case "TIME":
          case "TIME WITH TIME ZONE":
          case "TIMESTAMP":
          case "TIMESTAMP WITH TIME ZONE":
            return Optional.of(type);
          default:
            return Optional.empty();
        }
      case AVG:
        switch (type) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
          case "DOUBLE":
            return Optional.of("DOUBLE");
          default:
            return Optional.empty();
        }
      case ARRAY_AGG:
        switch (type) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
          case "DOUBLE":
          case "BOOLEAN":
          case "STRING":
          case "DATE":
          case "TIME":
          case "TIME WITH TIME ZONE":
          case "TIMESTAMP":
          case "TIMESTAMP WITH TIME ZONE":
            return Optional.of("ARRAY<" + type + ">");
          default:
            return Optional.empty();
        }
      case LISTAGG:
        switch (type) {
          case "INTEGER":
          case "LONG":
          case "FLOAT":
          case "DOUBLE":
          case "BOOLEAN":
          case "STRING":
          case "DATE":
          case "TIME":
          case "TIME WITH TIME ZONE":
          case "TIMESTAMP":
          case "TIMESTAMP WITH TIME ZONE":
            return Optional.of("STRING");
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
        return getUnionType(typeA, typeB).isPresent() ? Optional.of("BOOLEAN") : Optional.empty();
      case GREATER:
      case GREATER_EQUAL:
      case LESS:
      case LESS_EQUAL:
        if (getUnionTypeForNumerics(typeA, typeB).isPresent() || getUnionTypeForDatetimes(typeA, typeB).isPresent()) {
          return Optional.of("BOOLEAN");
        } else {
          return typeA.equals(typeB) && (typeA.equals("STRING") || typeA.equals("BOOLEAN")) ? Optional.of("BOOLEAN")
              : Optional.empty();
        }
      case AND:
      case OR:
        return typeA.equals("BOOLEAN") && typeB.equals("BOOLEAN") ? Optional.of("BOOLEAN") : Optional.empty();
      case STRING_CONCAT:
        return typeA.equals("STRING") && typeB.equals("STRING") ? Optional.of("STRING") : Optional.empty();
      default:
        return Optional.empty();
    }
  }

  @Override
  public Optional<List<DataTypeSynonym>> getDataTypeSynonyms() {
    List<DataTypeSynonym> synonyms = new ArrayList<>();
    synonyms.add(new DataTypeSynonym("INT", "INTEGER"));
    return Optional.of(synonyms);
  }

  @Override
  public Optional<List<FunctionSignature>> getFunctionSignatures() {
    List<FunctionSignature> functions = new ArrayList<>();

    functions.add(new FunctionSignature(null, "ID", argumentTypes("VERTEX"), "STRING"));
    functions.add(new FunctionSignature(null, "ID", argumentTypes("EDGE"), "STRING"));

    functions.add(new FunctionSignature(null, "LABEL", argumentTypes("VERTEX"), "STRING"));
    functions.add(new FunctionSignature(null, "LABEL", argumentTypes("EDGE"), "STRING"));

    functions.add(new FunctionSignature(null, "LABELS", argumentTypes("VERTEX"), "SET<STRING>"));

    functions.add(new FunctionSignature(null, "has_label", argumentTypes("VERTEX", "STRING"), "BOOLEAN"));
    functions.add(new FunctionSignature(null, "has_label", argumentTypes("EDGE", "STRING"), "BOOLEAN"));

    functions.add(new FunctionSignature(null, "IN_DEGREE", argumentTypes("VERTEX"), "LONG"));
    functions.add(new FunctionSignature(null, "IN_DEGREE", argumentTypes("EDGE"), "LONG"));

    functions.add(new FunctionSignature(null, "OUT_DEGREE", argumentTypes("VERTEX"), "LONG"));
    functions.add(new FunctionSignature(null, "OUT_DEGREE", argumentTypes("EDGE"), "LONG"));

    functions.add(new FunctionSignature(null, "JAVA_REGEXP_LIKE", argumentTypes("STRING", "STRING"), "BOOLEAN"));

    functions.add(new FunctionSignature(null, "UPPER", argumentTypes("STRING"), "STRING"));

    functions.add(new FunctionSignature(null, "LOWER", argumentTypes("STRING"), "STRING"));

    functions.add(new FunctionSignature(null, "OUT_DEGREE", argumentTypes("EDGE"), "STRING"));

    functions.add(new FunctionSignature(null, "ABS", argumentTypes("INTEGER"), "INTEGER"));
    functions.add(new FunctionSignature(null, "ABS", argumentTypes("LONG"), "LONG"));
    functions.add(new FunctionSignature(null, "ABS", argumentTypes("FLOAT"), "FLOAT"));
    functions.add(new FunctionSignature(null, "ABS", argumentTypes("DOUBLE"), "DOUBLE"));

    functions.add(new FunctionSignature(null, "CEIL", argumentTypes("INTEGER"), "INTEGER"));
    functions.add(new FunctionSignature(null, "CEIL", argumentTypes("LONG"), "LONG"));
    functions.add(new FunctionSignature(null, "CEIL", argumentTypes("FLOAT"), "FLOAT"));
    functions.add(new FunctionSignature(null, "CEIL", argumentTypes("DOUBLE"), "DOUBLE"));

    functions.add(new FunctionSignature(null, "CEILING", argumentTypes("INTEGER"), "INTEGER"));
    functions.add(new FunctionSignature(null, "CEILING", argumentTypes("LONG"), "LONG"));
    functions.add(new FunctionSignature(null, "CEILING", argumentTypes("FLOAT"), "FLOAT"));
    functions.add(new FunctionSignature(null, "CEILING", argumentTypes("DOUBLE"), "DOUBLE"));

    functions.add(new FunctionSignature(null, "FLOOR", argumentTypes("INTEGER"), "INTEGER"));
    functions.add(new FunctionSignature(null, "FLOOR", argumentTypes("LONG"), "LONG"));
    functions.add(new FunctionSignature(null, "FLOOR", argumentTypes("FLOAT"), "FLOAT"));
    functions.add(new FunctionSignature(null, "FLOOR", argumentTypes("DOUBLE"), "DOUBLE"));

    functions.add(new FunctionSignature(null, "ROUND", argumentTypes("INTEGER"), "INTEGER"));
    functions.add(new FunctionSignature(null, "ROUND", argumentTypes("LONG"), "LONG"));
    functions.add(new FunctionSignature(null, "ROUND", argumentTypes("FLOAT"), "FLOAT"));
    functions.add(new FunctionSignature(null, "ROUND", argumentTypes("DOUBLE"), "DOUBLE"));

    functions.add(new FunctionSignature("myUdfs", "pi", argumentTypes(), "DOUBLE"));

    functions.add(new FunctionSignature("myUdfs", "numericFunction", argumentTypes("INTEGER"), "INTEGER"));

    functions.add(new FunctionSignature("myUdfs", "ambiguousFunction", argumentTypes("DOUBLE"), "FLOAT"));
    functions.add(new FunctionSignature("myUdfs", "ambiguousFunction", argumentTypes("INTEGER"), "INTEGER"));
    functions.add(new FunctionSignature("MyUdFS", "AmBiguousFuNCtion", argumentTypes("INTEGER"), "DOUBLE"));

    return Optional.of(functions);
  }

  private List<String> argumentTypes(String... args) {
    return Arrays.asList(args);
  }
}
