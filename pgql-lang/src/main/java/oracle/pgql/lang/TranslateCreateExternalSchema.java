/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.propertygraph.CreateExternalSchema;
import oracle.pgql.lang.ir.Statement;

import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.isNone;

public class TranslateCreateExternalSchema {

  private static int LOCAL_SCHEMA_NAME = 0;

  private static int DATABASE_CONNECTION_DETAILS = 1;

  private static int URL = 0;

  private static int USER_NAME = 1;

  private static int KEYSTORE_ALIAS = 2;

  private static int DATA_SOURCE_NAME = 0;

  protected static Statement translateCreateExternalSchema(IStrategoTerm ast) {

    IStrategoTerm localSchemaNameT = ast.getSubterm(LOCAL_SCHEMA_NAME);
    String localSchemaName = getString(localSchemaNameT);

    IStrategoAppl databaseConnectionDetails = (IStrategoAppl) ast.getSubterm(DATABASE_CONNECTION_DETAILS);
    String connectionDetailsType = databaseConnectionDetails.getConstructor().getName();

    switch (connectionDetailsType) {
      case "JdbcConnectionDetails":

        IStrategoTerm urlT = databaseConnectionDetails.getSubterm(URL);
        String url = getString(urlT);

        IStrategoTerm userNameT = databaseConnectionDetails.getSubterm(USER_NAME);
        String userName = isNone(userNameT) ? null : getString(userNameT);

        IStrategoTerm keystoreAliasT = databaseConnectionDetails.getSubterm(KEYSTORE_ALIAS);
        String keystoreAlias = isNone(keystoreAliasT) ? null : getString(keystoreAliasT);

        return new CreateExternalSchema(localSchemaName, url, userName, keystoreAlias);
      case "DataSource":
        IStrategoTerm dataSourceNameT = databaseConnectionDetails.getSubterm(DATA_SOURCE_NAME);
        String dataSourceName = getString(dataSourceNameT);
        return new CreateExternalSchema(localSchemaName, dataSourceName);
      default:
        throw new IllegalArgumentException(connectionDetailsType);
    }
  }
}
