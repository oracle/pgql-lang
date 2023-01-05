/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.DropPropertyGraph;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.PgqlStatement;

import static oracle.pgql.lang.CommonTranslationUtil.getSchemaQualifiedName;

public class TranslateDropPropertyGraph {

  private static int DROP_PROPERTY_GRAPH_NAME = 0;

  protected static PgqlStatement translateDropPropertyGraph(IStrategoTerm ast) {

    IStrategoTerm graphNameT = ast.getSubterm(DROP_PROPERTY_GRAPH_NAME);

    SchemaQualifiedName graphName = getSchemaQualifiedName(graphNameT);

    return new DropPropertyGraph(graphName);
  }
}
