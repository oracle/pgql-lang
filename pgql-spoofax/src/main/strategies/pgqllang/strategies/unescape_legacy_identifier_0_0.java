/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class unescape_legacy_identifier_0_0 extends Strategy {

  public static unescape_legacy_identifier_0_0 instance = new unescape_legacy_identifier_0_0();

  public static final CharSequenceTranslator UNESCAPE_LEGACY_IDENTIFIER;
  static {
    final Map<CharSequence, CharSequence> unescapeJavaMap = new HashMap<>();
    unescapeJavaMap.put("\\\\", "\\");
    unescapeJavaMap.put("\\\"", "\"");
    unescapeJavaMap.put("\\'", "'");
    unescapeJavaMap.put("\\", StringUtils.EMPTY);
    unescapeJavaMap.put("\"\"", "\"");
    UNESCAPE_LEGACY_IDENTIFIER = new AggregateTranslator(new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE),
        new LookupTranslator(Collections.unmodifiableMap(unescapeJavaMap)));
  }
  
  @Override
  public IStrategoTerm invoke(Context context, IStrategoTerm current) {
    String escaped = ((IStrategoString) current).stringValue();
    String unescaped = UNESCAPE_LEGACY_IDENTIFIER.translate(escaped);
    return context.getFactory().makeString(unescaped);
  }
}
