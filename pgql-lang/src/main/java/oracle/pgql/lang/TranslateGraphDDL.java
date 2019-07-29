/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.CreatePropertyGraph;
import oracle.pgql.lang.ddl.propertygraph.EdgeTable;
import oracle.pgql.lang.ddl.propertygraph.Key;
import oracle.pgql.lang.ddl.propertygraph.Label;
import oracle.pgql.lang.ddl.propertygraph.Property;
import oracle.pgql.lang.ddl.propertygraph.VertexTable;
import oracle.pgql.lang.ir.Statement;

import static oracle.pgql.lang.SpoofaxAstToGraphQuery.isNone;
import static oracle.pgql.lang.SpoofaxAstToGraphQuery.getString;
import static oracle.pgql.lang.SpoofaxAstToGraphQuery.getSome;

import java.util.ArrayList;
import java.util.List;

public class TranslateGraphDDL {

  private static int CREATE_PROPERTY_GRAPH_NAME = 0;

  private static int CREATE_PROPERTY_GRAPH_VERTEX_TABLES = 1;

  private static int CREATE_PROPERTY_GRAPH_EDGE_TABLES = 2;

  private static int VERTEX_TABLES_TABLES_LIST = 0;

  private static int EDGE_TABLES_TABLES_LIST = 0;

  private static int VERTEX_TABLE_NAME = 0;

  private static int VERTEX_TABLE_KEY = 1;

  private static int VERTEX_TABLE_LABEL_AND_PROPERTIES = 2;

  private static int EDGE_TABLE_NAME = 0;

  private static int EDGE_TABLE_KEY = 1;

  private static int EDGE_TABLE_SOURCE_VERTEX_TABLE = 2;

  private static int EDGE_TABLE_DESTINATION_VERTEX_TABLE = 3;

  private static int EDGE_TABLE_LABEL_AND_PROPERTIES = 4;

  private static int SOURCE_VERTEX_TABLE_KEY = 0;

  private static int SOURCE_VERTEX_TABLE_NAME = 1;

  private static int DESTINATION_VERTEX_TABLE_KEY = 0;

  private static int DESTINATION_VERTEX_TABLE_NAME = 1;

  private static int LABEL_AND_PROPERTIES_CLAUSE_LABEL_AND_PROPERTIES_LIST = 0;

  private static int LABEL_AND_PROPERTIES_LABEL_CLAUSE = 0;

  private static int LABEL_AND_PROPERTIES_PROPERTIES_CLAUSE = 1;

  private static int PROPERTIES_CLAUSE_PROPERTIES_LIST = 0;

  private static int PROPERTIES_ARE_ALL_COLUMNS_EXCEPT_PART = 1;

  private static int EXCEPT_COLUMNS_LIST = 0;

  private static int EXP_AS_VAR_EXP = 0;

  private static int EXP_AS_VAR_VAR = 1;

  private static int LOCAL_OR_SCHEMA_QUALIFIED_NAME_SCHEMA_NAME = 0;

  private static int LOCAL_OR_SCHEMA_QUALIFIED_NAME_LOCAL_NAME = 1;

  protected static Statement translateCreatePropertyGraph(IStrategoTerm ast) {

    IStrategoTerm graphNameT = ast.getSubterm(CREATE_PROPERTY_GRAPH_NAME);

    String schemaName = getSchemaName(graphNameT);

    String graphName = getLocalName(graphNameT);

    List<VertexTable> vertexTables = getVertexTables(ast.getSubterm(CREATE_PROPERTY_GRAPH_VERTEX_TABLES));

    List<EdgeTable> edgeTables = getEdgeTables(ast.getSubterm(CREATE_PROPERTY_GRAPH_EDGE_TABLES), vertexTables);

    return new CreatePropertyGraph(schemaName, graphName, vertexTables, edgeTables);
  }

  private static List<VertexTable> getVertexTables(IStrategoTerm vertexTablesT) {
    IStrategoTerm vertexTablesListT = vertexTablesT.getSubterm(VERTEX_TABLES_TABLES_LIST);
    List<VertexTable> result = new ArrayList<>();
    for (IStrategoTerm vertexTableT : vertexTablesListT) {
      IStrategoTerm tableNameT = vertexTableT.getSubterm(VERTEX_TABLE_NAME);
      String schemaName = getSchemaName(tableNameT);
      String tableName = getLocalName(tableNameT);

      Key vertexKey = getKey(vertexTableT.getSubterm(VERTEX_TABLE_KEY));
      List<Label> labels = getLabels(vertexTableT.getSubterm(VERTEX_TABLE_LABEL_AND_PROPERTIES));
      result.add(new VertexTable(schemaName, tableName, vertexKey, labels));
    }
    return result;
  }

  private static List<EdgeTable> getEdgeTables(IStrategoTerm edgeTablesT, List<VertexTable> vertexTables) {
    IStrategoTerm edgeTablesListT = edgeTablesT.getSubterm(EDGE_TABLES_TABLES_LIST);
    List<EdgeTable> result = new ArrayList<>();
    for (IStrategoTerm edgeTableT : edgeTablesListT) {
      IStrategoTerm tableNameT = edgeTableT.getSubterm(EDGE_TABLE_NAME);
      String schemaName = getSchemaName(tableNameT);
      String tableName = getLocalName(tableNameT);

      // Key edgeKey = getKey(edgeTableT.getSubterm(EDGE_TABLE_KEY)); not used for now

      IStrategoTerm sourceVertexTableT = edgeTableT.getSubterm(EDGE_TABLE_SOURCE_VERTEX_TABLE);
      Key sourceVertexTableKey = getKey(sourceVertexTableT.getSubterm(SOURCE_VERTEX_TABLE_KEY));
      IStrategoTerm sourceVertexTableNameT = sourceVertexTableT.getSubterm(SOURCE_VERTEX_TABLE_NAME);
      String sourceVertexTableSchemaName = getSchemaName(sourceVertexTableNameT);
      String sourceVertexTableName = getLocalName(sourceVertexTableNameT);
      VertexTable sourceVertexTable = getVertexTable(vertexTables, sourceVertexTableSchemaName, sourceVertexTableName);

      IStrategoTerm destinationVertexTableT = edgeTableT.getSubterm(EDGE_TABLE_DESTINATION_VERTEX_TABLE);
      Key destinationVertexTableKey = getKey(destinationVertexTableT.getSubterm(DESTINATION_VERTEX_TABLE_KEY));
      IStrategoTerm destinationVertexTableNameT = destinationVertexTableT.getSubterm(DESTINATION_VERTEX_TABLE_NAME);
      String destinationVertexTableSchemaName = getSchemaName(destinationVertexTableNameT);
      String destinationVertexTableName = getLocalName(destinationVertexTableNameT);
      VertexTable destinationVertexTable = getVertexTable(vertexTables, destinationVertexTableSchemaName,
          destinationVertexTableName);

      List<Label> labels = getLabels(edgeTableT.getSubterm(EDGE_TABLE_LABEL_AND_PROPERTIES));
      result.add(new EdgeTable(schemaName, tableName, sourceVertexTable, sourceVertexTableKey, destinationVertexTable,
          destinationVertexTableKey, labels));
    }
    return result;
  }

  private static VertexTable getVertexTable(List<VertexTable> vertexTables, String schemaName, String tableName) {
    for (VertexTable vertexTable : vertexTables) {
      boolean schemaEquals = (vertexTable.getSchemaName() == null && schemaName == null)
          || vertexTable.getSchemaName().equals(schemaName);
      if (schemaEquals && vertexTable.getTableName().equals(tableName)) {
        return vertexTable;
      }
    }
    throw new IllegalStateException("vertex table not found");
  }

  private static Key getKey(IStrategoTerm keyClauseT) {
    if (isNone(keyClauseT)) {
      return null;
    }
    keyClauseT = getSome(keyClauseT);
    List<String> columnNames = new ArrayList<>();
    for (IStrategoTerm columnReference : keyClauseT) {
      columnNames.add(getString(columnReference));
    }
    return new Key(columnNames);
  }

  private static List<Label> getLabels(IStrategoTerm labelAndPropertiesClauseT) {
    IStrategoTerm labelAndPropertiesListT = labelAndPropertiesClauseT
        .getSubterm(LABEL_AND_PROPERTIES_CLAUSE_LABEL_AND_PROPERTIES_LIST);
    List<Label> result = new ArrayList<>();
    for (IStrategoTerm labelAndPropertiesT : labelAndPropertiesListT) {
      String labelName = getString(labelAndPropertiesT.getSubterm(LABEL_AND_PROPERTIES_LABEL_CLAUSE));
      Label label = getLabel(labelName, labelAndPropertiesT.getSubterm(LABEL_AND_PROPERTIES_PROPERTIES_CLAUSE));
      result.add(label);
    }
    return result;
  }

  private static Label getLabel(String labelName, IStrategoTerm propertiesClauseT) {
    if (isNone(propertiesClauseT)) {
      return null;
    } else {
      IStrategoTerm propertiesSpecificationT = getSome(propertiesClauseT);
      String propertiesSpecificationType = ((IStrategoAppl) propertiesSpecificationT).getConstructor().getName();
      switch (propertiesSpecificationType) {
        case "PropertyExpressions":
          IStrategoTerm propertiesListT = propertiesSpecificationT.getSubterm(PROPERTIES_CLAUSE_PROPERTIES_LIST);
          List<Property> properties = new ArrayList<>();
          for (IStrategoTerm expAsVarT : propertiesListT) {
            String columnName = getString(expAsVarT.getSubterm(EXP_AS_VAR_EXP));
            String propertyName = getString(expAsVarT.getSubterm(EXP_AS_VAR_VAR));
            properties.add(new Property(columnName, propertyName));
          }
          return new Label(labelName, properties);
        case "PropertiesAreAllColumns":
          IStrategoTerm exceptColumnsT = propertiesSpecificationT.getSubterm(PROPERTIES_ARE_ALL_COLUMNS_EXCEPT_PART);
          boolean propertiesAreAllColumns = true;
          if (isNone(exceptColumnsT)) {
            return new Label(labelName, propertiesAreAllColumns);
          } else {
            IStrategoTerm exceptColumnsListT = getSome(exceptColumnsT).getSubterm(EXCEPT_COLUMNS_LIST);
            List<String> columnNames = new ArrayList<>();
            for (IStrategoTerm columnNameT : exceptColumnsListT) {
              columnNames.add(getString(columnNameT));
            }
            return new Label(labelName, propertiesAreAllColumns, columnNames);
          }
        default:
          throw new IllegalArgumentException(propertiesSpecificationType);
      }
    }
  }

  private static String getLocalName(IStrategoTerm localOrSchemaQualifiedNameT) {
    String tableName = getString(localOrSchemaQualifiedNameT.getSubterm(LOCAL_OR_SCHEMA_QUALIFIED_NAME_LOCAL_NAME));
    return tableName;
  }

  private static String getSchemaName(IStrategoTerm localOrSchemaQualifiedNameT) {
    IStrategoTerm schemaNameT = localOrSchemaQualifiedNameT.getSubterm(LOCAL_OR_SCHEMA_QUALIFIED_NAME_SCHEMA_NAME);
    if (isNone(schemaNameT)) {
      return null;
    } else {
      return getString(schemaNameT);
    }
  }
}
