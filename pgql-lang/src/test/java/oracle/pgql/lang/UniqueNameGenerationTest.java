/*
 * Copyright (C) 2013 - 2024 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import oracle.pgql.lang.ir.ExpAsVar;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.QueryEdge;
import oracle.pgql.lang.ir.QueryExpression.Function.Exists;
import oracle.pgql.lang.ir.QueryExpression.PropertyAccess;
import oracle.pgql.lang.ir.QueryExpression.Subquery;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryPath;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.VertexPairConnection;
import oracle.pgql.lang.ir.modify.EdgeInsertion;
import oracle.pgql.lang.ir.modify.InsertClause;
import oracle.pgql.lang.ir.modify.ModifyQuery;
import oracle.pgql.lang.ir.modify.VertexInsertion;
import oracle.pgql.lang.ir.unnest.OneRowPerEdge;
import oracle.pgql.lang.ir.unnest.OneRowPerStep;
import oracle.pgql.lang.ir.unnest.OneRowPerVertex;
import oracle.pgql.lang.util.AbstractQueryExpressionVisitor;

public class UniqueNameGenerationTest extends AbstractPgqlTest {

  @Test
  public void testUniqueVertexNameAndCorrelation() throws Exception {
    PgqlResult result = pgql.parse("SELECT COUNT(*) FROM MATCH (n) WHERE EXISTS ( SELECT 1 FROM MATCH (n) -> (m) )");
    assertTrue(result.isQueryValid());

    GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();
    QueryVertex vertexN1 = graphPattern.getVertices().iterator().next();
    assertEquals("N", vertexN1.getName());
    assertEquals("N_(28,29)", vertexN1.getUniqueName());

    Exists existsSubquery = (Exists) graphPattern.getConstraints().iterator().next();
    Iterator<QueryVertex> it = existsSubquery.getQuery().getGraphPattern().getVertices().iterator();
    QueryVertex vertexN2 = it.next();
    QueryVertex vertexM = it.next();

    assertEquals("N", vertexN2.getName());
    assertEquals("N_(67,68)", vertexN2.getUniqueName());
    assertEquals("N_(28,29)", vertexN2.getCorrelationVertexInOuterQuery().getUniqueName());

    assertEquals("M", vertexM.getName());
    assertEquals("M_(74,75)", vertexM.getUniqueName());
    assertNull(vertexM.getCorrelationVertexInOuterQuery());
  }

  @Test
  public void testUniqueEdgeNameAndCorrelation() throws Exception {
    PgqlResult result = pgql.parse("SELECT COUNT(*) FROM MATCH () -[e1]-> () -[e2]-> ()");
    assertTrue(result.isQueryValid());

    GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();
    Iterator<VertexPairConnection> it = graphPattern.getConnections().iterator();
    QueryEdge edgeE1 = (QueryEdge) it.next();
    QueryEdge edgeE2 = (QueryEdge) it.next();

    assertEquals("E1", edgeE1.getName());
    assertEquals("E1_(32,34)", edgeE1.getUniqueName());

    assertEquals("E2", edgeE2.getName());
    assertEquals("E2_(43,45)", edgeE2.getUniqueName());
  }

  @Test
  public void testUniqueNameInRowsClause() throws Exception {
    PgqlResult result = pgql.parse("SELECT 1 FROM MATCH () ->{1,4} () ONE ROW PER VERTEX (v)");
    assertTrue(result.isQueryValid());

    GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();
    QueryPath path = (QueryPath) graphPattern.getConnections().iterator().next();
    OneRowPerVertex oneRowPerVertex = (OneRowPerVertex) path.getRowsPerMatch();

    assertEquals("V", oneRowPerVertex.getVertex().getName());
    assertEquals("V_(54,55)", oneRowPerVertex.getVertex().getUniqueName());

    result = pgql.parse("SELECT 1 FROM MATCH () ->{1,4} () ONE ROW PER EDGE (e)");
    assertTrue(result.isQueryValid());

    graphPattern = result.getGraphQuery().getGraphPattern();
    path = (QueryPath) graphPattern.getConnections().iterator().next();
    OneRowPerEdge oneRowPerEdge = (OneRowPerEdge) path.getRowsPerMatch();

    assertEquals("E", oneRowPerEdge.getEdge().getName());
    assertEquals("E_(52,53)", oneRowPerEdge.getEdge().getUniqueName());

    result = pgql.parse("SELECT 1 FROM MATCH () ->{1,4} () ONE ROW PER STEP (v1, e, v2)");
    assertTrue(result.isQueryValid());

    graphPattern = result.getGraphQuery().getGraphPattern();
    path = (QueryPath) graphPattern.getConnections().iterator().next();
    OneRowPerStep oneRowPerStep = (OneRowPerStep) path.getRowsPerMatch();

    assertEquals("V1", oneRowPerStep.getVertex1().getName());
    assertEquals("V1_(52,54)", oneRowPerStep.getVertex1().getUniqueName());
    assertEquals("E", oneRowPerStep.getEdge().getName());
    assertEquals("E_(56,57)", oneRowPerStep.getEdge().getUniqueName());
    assertEquals("V2", oneRowPerStep.getVertex2().getName());
    assertEquals("V2_(59,61)", oneRowPerStep.getVertex2().getUniqueName());
  }

  @Test
  public void testUniqueNameInInsert() throws Exception {
    PgqlResult result = pgql.parse("INSERT VERTEX v, EDGE e BETWEEN v AND v");
    assertTrue(result.isQueryValid());

    ModifyQuery modifyQuery = (ModifyQuery) result.getGraphQuery();
    InsertClause insertClause = (InsertClause) modifyQuery.getModifications().iterator().next();

    VertexInsertion vertexInsertion = (VertexInsertion) insertClause.getInsertions().get(0);
    EdgeInsertion edgeInsertion = (EdgeInsertion) insertClause.getInsertions().get(1);

    QueryVertex vertexV = vertexInsertion.getVertex();
    QueryEdge edgeE = edgeInsertion.getEdge();

    assertEquals("V", vertexV.getName());
    assertEquals("V_(14,15)", vertexV.getUniqueName());

    assertEquals("E", edgeE.getName());
    assertEquals("E_(22,23)", edgeE.getUniqueName());
  }

  @Test
  public void testUniqueNameInExpAsVar() throws Exception {
    PgqlResult result = pgql.parse("SELECT n.prop, n.c*2 FROM MATCH (n) GROUP BY n.prop, n.c*2");
    assertTrue(result.isQueryValid());

    SelectQuery selectQuery = (SelectQuery) result.getGraphQuery();
    ExpAsVar expAsVar1 = selectQuery.getProjection().getElements().get(0);
    ExpAsVar expAsVar2 = selectQuery.getProjection().getElements().get(1);
    ExpAsVar expAsVar3 = selectQuery.getGroupBy().getElements().get(0);
    ExpAsVar expAsVar4 = selectQuery.getGroupBy().getElements().get(1);

    assertEquals("PROP", expAsVar1.getName());
    assertEquals("PROP_(9,13)", expAsVar1.getUniqueName());

    assertEquals("n.c*2", expAsVar2.getName());
    assertEquals("n.c*2_(15,20)", expAsVar2.getUniqueName());

    assertEquals("n.prop", expAsVar3.getName());
    assertEquals("n.prop_(45,51)", expAsVar3.getUniqueName());

    assertEquals("n.c*2", expAsVar4.getName());
    assertEquals("n.c*2_(53,58)", expAsVar4.getUniqueName());
  }

  @Test
  public void testQueryCorrelation() throws Exception {
    assertTrue(
        isSubqueryInWhereCorrelated("SELECT 1 FROM MATCH (n) WHERE EXISTS ( SELECT * FROM MATCH (n) -[e]-> (m) )"));
    assertFalse(
        isSubqueryInWhereCorrelated("SELECT 1 FROM MATCH (n) WHERE EXISTS ( SELECT * FROM MATCH (n2) -[e]-> (m) )"));
    assertTrue(isSubqueryInWhereCorrelated(
        "SELECT 1 FROM MATCH (n) WHERE EXISTS ( SELECT * FROM MATCH (n2) -[e]-> (m) WHERE n.prop > 3 )"));
    assertFalse(isSubqueryInWhereCorrelated(
        "SELECT COUNT(*) FROM MATCH (a) WHERE EXISTS ( SELECT COUNT(*) FROM MATCH (b)->() GROUP by b ORDER BY COUNT(*) DESC LIMIT 1 )"));
    assertTrue(isSubqueryInWhereCorrelated(
        "SELECT 1 FROM MATCH () -[e]-> () WHERE EXISTS ( SELECT * FROM MATCH (n) WHERE n IS SOURCE OF e )"));
    assertFalse(isSubqueryInWhereCorrelated(
        "SELECT 1 FROM MATCH () WHERE EXISTS ( SELECT * FROM MATCH (n) -[e]-> () WHERE n IS SOURCE OF e )"));
    assertTrue(isSubqueryInWhereCorrelated("SELECT id(n), n.timeProp FROM MATCH (n)" + //
        "         WHERE (SELECT o.timeWithTimezoneProp FROM MATCH (o)" + //
        "                WHERE d(o) = id(n) ) ORDER BY id(n)"));

    PgqlResult result = pgql.parse("SELECT n.age " + //
        "         MATCH (n) " + //
        "         GROUP BY n.age " + //
        "         HAVING EXISTS ( SELECT 1 " + //
        "                         MATCH (m) -> (o) " + //
        "                         GROUP BY n.age, m " + //
        "                         HAVING COUNT(o) + n.age = 25 )");
    Subquery subquery = (Subquery) result.getGraphQuery().getHaving();
    assertTrue(isCorrelated(subquery.getQuery()));

    result = pgql.parse(
        "SELECT element_number(k), (SELECT element_number(k) FROM MATCH (n2) WHERE id(n2) = element_number(k)) \n"
            + "         FROM MATCH ANY SHORTEST (n) ((src)-[e]->(dst)){2} (m) ONE ROW PER VERTEX (k) WHERE id(k) > 4");
    subquery = (Subquery) ((SelectQuery) result.getGraphQuery()).getProjection().getElements().get(1).getExp();
    assertTrue(isCorrelated(subquery.getQuery()));
  }

  private boolean isSubqueryInWhereCorrelated(String query) throws Exception {
    PgqlResult result = pgql.parse(query);
    assertTrue(result.isQueryValid());
    GraphPattern graphPattern = result.getGraphQuery().getGraphPattern();
    Subquery subquery = (Subquery) graphPattern.getConstraints().iterator().next();
    return isCorrelated(subquery.getQuery());
  }

  private boolean isCorrelated(GraphQuery subquery) {

    Set<QueryVariable> correlationVariables = new HashSet<>();
    Set<String> definedVariables = new HashSet<>();
    Map<String, QueryVariable> referencedVariables = new HashMap<>();

    subquery.accept(new AbstractQueryExpressionVisitor() {

      @Override
      public void visit(QueryVertex queryVertex) {
        definedVariables.add(queryVertex.getUniqueName());

        QueryVariable correlationVariable = queryVertex.getCorrelationVertexInOuterQuery();
        if (correlationVariable != null) {
          correlationVariables.add(correlationVariable);
        }
      }

      @Override
      public void visit(QueryEdge queryEdge) {
        definedVariables.add(queryEdge.getUniqueName());

        QueryVariable correlationVariable = queryEdge.getCorrelationEdgeInOuterQuery();
        if (correlationVariable != null) {
          correlationVariables.add(correlationVariable);
        }
      }

      @Override
      public void visit(ExpAsVar expAsVar) {
        definedVariables.add(expAsVar.getUniqueName());
        super.visit(expAsVar);
      }

      @Override
      public void visit(VarRef varRef) {
        QueryVariable var = varRef.getVariable();
        referencedVariables.put(var.getUniqueName(), var);
      }

      @Override
      public void visit(PropertyAccess propertyAccess) {
        QueryVariable var = propertyAccess.getVariable();
        referencedVariables.put(var.getUniqueName(), var);
      }
    });

    for (String definedVariable : definedVariables) {
      referencedVariables.remove(definedVariable);
    }

    if (definedVariables.contains(null) || referencedVariables.containsKey(null)) {
      throw new IllegalStateException("Unique name should not be null");
    }

    return !(correlationVariables.isEmpty() && referencedVariables.isEmpty());
  }
}