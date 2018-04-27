/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.QueryVariable;

public class TranslationContext {

  private final Map<IStrategoTerm, QueryVariable> vars;

  private final Set<String> varNames; // used variable names in the query (globally)

  private final Map<String, CommonPathExpression> commonPathExpressions;

  public TranslationContext(Map<IStrategoTerm, QueryVariable> vars, Set<String> varNames,
      Map<String, CommonPathExpression> commonPathExpressions) {
    this.vars = vars;
    this.varNames = varNames;
    this.commonPathExpressions = commonPathExpressions;
  }

  public void addVar(QueryVariable var, String varName, IStrategoTerm originPosition) {
    vars.put(originPosition, var);
    varNames.add(varName);
  }

  public QueryVariable getVariable(IStrategoTerm originPosition) {
    return vars.get(originPosition);
  }

  public boolean isVariableNameInUse(String varName) {
    return varNames.contains(varName);
  }

  public Map<String, CommonPathExpression> getCommonPathExpressions() {
    return commonPathExpressions;
  }
}
