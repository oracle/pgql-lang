/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.DropPropertyGraph;
import oracle.pgql.lang.ir.Statement;

import static oracle.pgql.lang.CommonTranslationUtil.getLocalName;
import static oracle.pgql.lang.CommonTranslationUtil.getSchemaName;

public class TranslateDropPropertyGraph {

  private static int DROP_PROPERTY_GRAPH_NAME = 0;

  protected static Statement translateDropPropertyGraph(IStrategoTerm ast) {

    IStrategoTerm graphNameT = ast.getSubterm(DROP_PROPERTY_GRAPH_NAME);

    String schemaName = getSchemaName(graphNameT);

    String graphName = getLocalName(graphNameT);

    return new DropPropertyGraph(schemaName, graphName);
  }
}
