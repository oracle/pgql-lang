/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.CallStatement;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.QueryExpression;

import static oracle.pgql.lang.CommonTranslationUtil.getSomeValue;
import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.isSome;
import static oracle.pgql.lang.CommonTranslationUtil.translateExp;

import java.util.ArrayList;
import java.util.List;

public class TranslateCallStatement {

  private static final int POS_PACKAGE_NAME = 0;
  private static final int POS_ROUTINE_NAME = 1;
  private static final int POS_PACKAGE_NAME_SCHEMA_PART = 0;
  private static final int POS_PACKAGE_NAME_PACKAGE_PART = 1;
  private static final int POS_ARGUMENT_LIST = 2;

  protected static PgqlStatement translateCallStatement(IStrategoTerm ast) throws PgqlException {
    String schemaName = null;
    String packageName = null;
    IStrategoTerm optionalPackageDeclT = ast.getSubterm(POS_PACKAGE_NAME);
    if (isSome(optionalPackageDeclT)) {
      IStrategoTerm packageDeclT = getSomeValue(optionalPackageDeclT);
      IStrategoTerm schemaT = packageDeclT.getSubterm(POS_PACKAGE_NAME_SCHEMA_PART);
      if (isSome(schemaT)) {
        schemaName = getString(schemaT);
      }
      packageName = getString(packageDeclT.getSubterm(POS_PACKAGE_NAME_PACKAGE_PART));
    }
    String routineName = getString(ast.getSubterm(POS_ROUTINE_NAME));

    IStrategoTerm argumentListT = ast.getSubterm(POS_ARGUMENT_LIST);
    List<QueryExpression> argumentList = new ArrayList<>(argumentListT.getSubtermCount());
    TranslationContext ctx = new TranslationContext();
    for (IStrategoTerm argumentT : argumentListT.getSubterms()) {
      argumentList.add(translateExp(argumentT, ctx));
    }

    return new CallStatement(schemaName, packageName, routineName, argumentList);
  }
}
