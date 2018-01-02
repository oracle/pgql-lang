/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.util.Map;

import oracle.pgql.lang.ir.CommonPathExpression;
import oracle.pgql.lang.ir.QueryVariable;

public class TranslationContext {

  private final Map<String, QueryVariable> inScopeVars;

  private final Map<String, QueryVariable> inScopeInAggregationVars;

  private final Map<String, CommonPathExpression> commonPathExpressions;

  public TranslationContext(Map<String, QueryVariable> inScopeVars,
      Map<String, QueryVariable> inScopeInAggregationVars, Map<String, CommonPathExpression> commonPathExpressions) {
    this.inScopeVars = inScopeVars;
    this.inScopeInAggregationVars = inScopeInAggregationVars;
    this.commonPathExpressions = commonPathExpressions;
  }

  public Map<String, QueryVariable> getInScopeVars() {
    return inScopeVars;
  }

  public Map<String, QueryVariable> getInScopeInAggregationVars() {
    return inScopeInAggregationVars;
  }

  public Map<String, CommonPathExpression> getCommonPathExpressions() {
    return commonPathExpressions;
  }

}
