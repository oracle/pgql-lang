/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.metadata;

import java.util.List;

public class FunctionSignature {

  private final String packageName;

  private final String functionName;

  private final List<String> argumentTypes;

  private final String returnType;

  public FunctionSignature(String packageName, String functionName, List<String> argumentTypes, String returnType) {
    this.packageName = packageName;
    this.functionName = functionName;
    this.argumentTypes = argumentTypes;
    this.returnType = returnType;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getFunctionName() {
    return functionName;
  }

  public List<String> getArgumentTypes() {
    return argumentTypes;
  }

  public String getReturnType() {
    return returnType;
  }
}
