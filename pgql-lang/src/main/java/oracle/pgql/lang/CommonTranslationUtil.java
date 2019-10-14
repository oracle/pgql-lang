/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ir.SchemaQualifiedName;

public class CommonTranslationUtil {

  private static int LOCAL_OR_SCHEMA_QUALIFIED_NAME_SCHEMA_NAME = 0;

  private static int LOCAL_OR_SCHEMA_QUALIFIED_NAME_LOCAL_NAME = 1;

  protected static String getString(IStrategoTerm t) {
    while (t.getTermType() != IStrategoTerm.STRING) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(LimitClause("10"))
    }
    return ((IStrategoString) t).stringValue();
  }

  protected static int getInt(IStrategoTerm t) {
    while (t.getTermType() != IStrategoTerm.INT) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(LimitClause("10"))
    }
    return ((IStrategoInt) t).intValue();
  }

  protected static IStrategoTerm getList(IStrategoTerm t) {
    while (t.getTermType() != IStrategoTerm.LIST) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(OrderElems([...]))
    }
    return t;
  }

  protected static boolean isNone(IStrategoTerm t) {
    return t.getTermType() == IStrategoTerm.APPL && ((IStrategoAppl) t).getConstructor().getName().equals("None");
  }

  protected static boolean isSome(IStrategoTerm t) {
    return t.getTermType() == IStrategoTerm.APPL && ((IStrategoAppl) t).getConstructor().getName().equals("Some");
  }

  protected static IStrategoTerm getSome(IStrategoTerm t) {
    return t.getSubterm(0);
  }

  protected static String getConstructorName(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName();
  }

  protected static SchemaQualifiedName getSchemaQualifiedName(IStrategoTerm schemaQualifiedNameT) {
    IStrategoTerm schemaNameT = schemaQualifiedNameT.getSubterm(LOCAL_OR_SCHEMA_QUALIFIED_NAME_SCHEMA_NAME);
    String schemaName = null;
    if (isSome(schemaNameT)) {
      schemaName = getString(schemaNameT);
    }
    String localName = getString(schemaQualifiedNameT.getSubterm(LOCAL_OR_SCHEMA_QUALIFIED_NAME_LOCAL_NAME));
    return new SchemaQualifiedName(schemaName, localName);
  }
}
