/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.DropExternalSchema;
import oracle.pgql.lang.ir.PgqlStatement;

import static oracle.pgql.lang.CommonTranslationUtil.getString;

public class TranslateDropExternalSchema {

  private static int DROP_EXTERNAL_SCHEMA_NAME = 0;

  protected static PgqlStatement translateDropExternalSchema(IStrategoTerm ast) {

    String schemaName = getString(ast.getSubterm(DROP_EXTERNAL_SCHEMA_NAME));

    return new DropExternalSchema(schemaName);
  }
}
