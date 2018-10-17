/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completion;

import static oracle.pgql.lang.completion.PgqlCompletionGenerator.completion;

import oracle.pgql.lang.editor.completion.PgqlCompletion;

public interface Keyword {

  public Fields getFields();
  
  default public String getStringExpression() {
    return getFields().stringExpression;
  }
  
  default public String getValue() {
    return getFields().value;
  }

  default public String getMeta() {
    return getFields().meta;
  }

  default public String getValueSuffix() {
    return getFields().valueSuffix;
  }

  default public PgqlCompletion getCompletion() {
    return getCompletion(getValue());
  }
  
  default public PgqlCompletion getCompletion(String valueSuffix) {
    return completion(valueSuffix + getValueSuffix(), getMeta());
  }
  
  public static final class Fields {

    private final String stringExpression;
    private final String value;
    private final String meta;
    private final String valueSuffix;
    
    Fields(String... fields) {
      int idx = 0;
      this.stringExpression = fields[idx++];
      this.value = fields[idx++];
      this.valueSuffix = fields[idx++];
      this.meta = fields[idx++];
    }
  }
}
