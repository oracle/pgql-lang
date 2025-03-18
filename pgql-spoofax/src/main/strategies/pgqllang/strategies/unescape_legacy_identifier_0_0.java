/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class unescape_legacy_identifier_0_0 extends Strategy {

  public static unescape_legacy_identifier_0_0 instance = new unescape_legacy_identifier_0_0();

  @Override
  public IStrategoTerm invoke(Context context, IStrategoTerm current) {
    String escaped = ((IStrategoString) current).stringValue();

    // first replace "" by \"
    String prepared = escaped.replaceAll("\\\"", "\"").replaceAll("\"\"", "\"").replaceAll("\"", "\\\"");

    // then perform Java unescaping
    Properties props = new Properties();
    try {
      props.load(new StringReader("key=" + prepared));
    } catch (IOException e) {
      e.printStackTrace();
    }
    String unescaped = props.getProperty("key");

    return context.getFactory().makeString(unescaped);
  }
}
