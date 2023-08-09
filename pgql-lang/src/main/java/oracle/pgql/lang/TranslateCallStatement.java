/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ddl.CallStatement;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.QueryExpression;

import static oracle.pgql.lang.CommonTranslationUtil.getSchemaQualifiedName;
import static oracle.pgql.lang.CommonTranslationUtil.translateExp;

import java.util.ArrayList;
import java.util.List;

public class TranslateCallStatement {

  private static int ROUTINE_NAME = 0;
  
  private static int ARGUMENT_LIST = 1;

  protected static PgqlStatement translateCallStatement(IStrategoTerm ast) throws PgqlException {
    IStrategoTerm routineNameT = ast.getSubterm(ROUTINE_NAME);
    SchemaQualifiedName routineName = getSchemaQualifiedName(routineNameT);

    IStrategoTerm argumentListT = ast.getSubterm(ARGUMENT_LIST);
    List<QueryExpression> argumentList = new ArrayList<>(argumentListT.getSubtermCount());
    TranslationContext ctx = new TranslationContext();
    for (IStrategoTerm argumentT : argumentListT.getSubterms()) {
      argumentList.add(translateExp(argumentT, ctx));
    }
    
    return new CallStatement(routineName, argumentList);
  }
}
