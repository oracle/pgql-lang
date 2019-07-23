/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.CreatePropertyGraph;
import oracle.pgql.lang.ddl.propertygraph.EdgeTable;
import oracle.pgql.lang.ddl.propertygraph.Key;
import oracle.pgql.lang.ddl.propertygraph.Label;
import oracle.pgql.lang.ddl.propertygraph.VertexTable;
import oracle.pgql.lang.ir.Statement;

import static oracle.pgql.lang.SpoofaxAstToGraphQuery.getString;
import static oracle.pgql.lang.SpoofaxAstToGraphQuery.getSome;

import java.util.ArrayList;
import java.util.List;

public class TranslateGraphDDL {

  private static int CREATE_PROPERTY_GRAPH_NAME = 0;

  private static int CREATE_PROPERTY_GRAPH_VERTEX_TABLES = 1;

  private static int CREATE_PROPERTY_GRAPH_EDGE_TABLES = 2;

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

  private static int LABEL_AND_PROPERTIES_LABEL_CLAUSE = 0;

  private static int LABEL_AND_PROPERTIES_PROPERTIES_CLAUSE = 1;

  private static int PROPERTIES_CLAUSE_PROPERTIES = 0;

  private static int PROPERTY_COLUMN_NAME = 0;

  private static int PROPERTY_PROPERTY_NAME = 1;

  protected static Statement translateCreatePropertyGraph(IStrategoTerm ast) {

    String graphName = getString(ast.getSubterm(CREATE_PROPERTY_GRAPH_NAME));

    List<VertexTable> vertexTables = getVertexTables(ast.getSubterm(CREATE_PROPERTY_GRAPH_VERTEX_TABLES));

    List<EdgeTable> edgeTables = getEdgeTables(ast.getSubterm(CREATE_PROPERTY_GRAPH_EDGE_TABLES), vertexTables);

    return new CreatePropertyGraph(graphName, vertexTables, edgeTables);
  }

  private static List<VertexTable> getVertexTables(IStrategoTerm vertexTablesT) {
    List<VertexTable> result = new ArrayList<>();
    for (IStrategoTerm vertexTableT : vertexTablesT) {
      String tableName = getString(vertexTableT.getSubterm(VERTEX_TABLE_NAME));
      Key vertexKey = getKey(vertexTableT.getSubterm(VERTEX_TABLE_KEY));
      List<Label> labels = getLabels(vertexTableT.getSubterm(VERTEX_TABLE_LABEL_AND_PROPERTIES));
      result.add(new VertexTable(tableName, vertexKey, labels));
    }
    return result;
  }

  private static List<EdgeTable> getEdgeTables(IStrategoTerm edgeTablesT, List<VertexTable> vertexTables) {
    List<EdgeTable> result = new ArrayList<>();
    for (IStrategoTerm edgeTableT : edgeTablesT) {
      String tableName = getString(edgeTableT.getSubterm(EDGE_TABLE_NAME));
      // Key edgeKey = getKey(edgeTableT.getSubterm(EDGE_TABLE_KEY)); not used for now

      IStrategoTerm sourceVertexTableT = edgeTableT.getSubterm(EDGE_TABLE_SOURCE_VERTEX_TABLE);
      Key sourceVertexTableKey = getKey(sourceVertexTableT.getSubterm(SOURCE_VERTEX_TABLE_KEY));
      String sourceVertexTableName = getString(sourceVertexTableT.getSubterm(SOURCE_VERTEX_TABLE_NAME));
      VertexTable sourceVertexTable = getVertexTable(vertexTables, sourceVertexTableName);

      IStrategoTerm destinationVertexTableT = edgeTableT.getSubterm(EDGE_TABLE_DESTINATION_VERTEX_TABLE);
      Key destinationVertexTableKey = getKey(destinationVertexTableT.getSubterm(DESTINATION_VERTEX_TABLE_KEY));
      String destinationVertexTableName = getString(destinationVertexTableT.getSubterm(DESTINATION_VERTEX_TABLE_NAME));
      VertexTable destinationVertexTable = getVertexTable(vertexTables, destinationVertexTableName);

      List<Label> labels = getLabels(edgeTableT.getSubterm(EDGE_TABLE_LABEL_AND_PROPERTIES));
      result.add(new EdgeTable(tableName, sourceVertexTable, sourceVertexTableKey, destinationVertexTable,
          destinationVertexTableKey, labels));
    }
    return result;
  }

  private static VertexTable getVertexTable(List<VertexTable> vertexTables, String tableName) {
    for (VertexTable vertexTable : vertexTables) {
      if (vertexTable.getTableName().equals(tableName)) {
        return vertexTable;
      }
    }
    throw new IllegalStateException("vertex table not found");
  }

  private static Key getKey(IStrategoTerm keyClauseT) {
    keyClauseT = getSome(keyClauseT);
    List<String> columnNames = new ArrayList<>();
    for (IStrategoTerm columnReference : keyClauseT) {
      columnNames.add(getString(columnReference));
    }
    return new Key(columnNames);
  }

  private static List<Label> getLabels(IStrategoTerm subterm) {
    // TODO Auto-generated method stub
    return null;
  }

}
