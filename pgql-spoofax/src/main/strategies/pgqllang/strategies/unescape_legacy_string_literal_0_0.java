/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class unescape_legacy_string_literal_0_0 extends Strategy {

	public static unescape_legacy_string_literal_0_0 instance = new unescape_legacy_string_literal_0_0();

	public static final CharSequenceTranslator UNESCAPE_PGQL_STRING_LITERAL = new AggregateTranslator(
			new CharSequenceTranslator[] { //
					new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()), //
					new LookupTranslator(new String[][] { { "\\\\", "\\" }, { "\\\"", "\"" }, { "\\'", "'" },
							{ "\\", "" }, { "''", "'" } }) });

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {
		String escaped = ((IStrategoString) current).stringValue();
		String unescaped = UNESCAPE_PGQL_STRING_LITERAL.translate(escaped);
		return context.getFactory().makeString(unescaped);
	}
}
