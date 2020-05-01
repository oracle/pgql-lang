/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.DropExternalSchema;
import oracle.pgql.lang.ir.Statement;

import static oracle.pgql.lang.CommonTranslationUtil.getString;

public class TranslateDropPropertyGraph {

  private static int EXTERNAL_SCHEMA_NAME = 0;

  protected static Statement translateDropPropertyGraph(IStrategoTerm ast) {

    IStrategoTerm schemaNameT = ast.getSubterm(EXTERNAL_SCHEMA_NAME);

    String schemaName = getString(schemaNameT);

    return new DropExternalSchema(schemaName);
  }
}
