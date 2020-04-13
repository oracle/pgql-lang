/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.CreateExternalSchema;
import oracle.pgql.lang.ir.Statement;

import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.isNone;

public class TranslateCreateExternalSchema {

  private static int LOCAL_SCHEMA_NAME = 0;

  private static int DATA_SOURCE_NAME = 1;

  private static int URL = 2;

  private static int USER_NAME = 3;

  private static int KEYSTORE_ALIAS = 4;

  protected static Statement translateCreateExternalSchema(IStrategoTerm ast) {

    IStrategoTerm localSchemaNameT = ast.getSubterm(LOCAL_SCHEMA_NAME);
    String localSchemaName = getString(localSchemaNameT);

    IStrategoTerm dataSourceNameT = ast.getSubterm(DATA_SOURCE_NAME);
    String dataSourceName = isNone(dataSourceNameT) ? null : getString(dataSourceNameT);

    IStrategoTerm urlT = ast.getSubterm(URL);
    String url = isNone(urlT) ? null : getString(urlT);

    IStrategoTerm userNameT = ast.getSubterm(USER_NAME);
    String userName = isNone(userNameT) ? null : getString(userNameT);

    IStrategoTerm keystoreAliasT = ast.getSubterm(KEYSTORE_ALIAS);
    String keystoreAlias = isNone(keystoreAliasT) ? null : getString(keystoreAliasT);

    return new CreateExternalSchema(localSchemaName, dataSourceName, url, userName, keystoreAlias);
  }
}
