/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

public class CompileException extends Exception {

  public CompileException(Throwable cause) {
    super(cause);
  }

  public CompileException(String msg) {
    super(msg);
  }

  public CompileException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
