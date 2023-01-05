/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import java.util.regex.Pattern;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class is_invalid_datetime_field_0_0 extends Strategy {

  public static is_invalid_datetime_field_0_0 instance = new is_invalid_datetime_field_0_0();

  private static Pattern decimalPattern = Pattern.compile("[0-9]*['.']?[0-9]*");;

  @Override
  public IStrategoTerm invoke(Context context, IStrategoTerm current) {

    IStrategoString valueTerm = (IStrategoString) current.getSubterm(0);
    String field = ((IStrategoString) current.getSubterm(1)).stringValue();

    switch (field) {
      case "YEAR":
        return parseIntAndCheckBounds(context, valueTerm, Long.MIN_VALUE, Long.MAX_VALUE);
      case "MONTH":
        return parseIntAndCheckBounds(context, valueTerm, 0, 11);
      case "DAY":
        return parseIntAndCheckBounds(context, valueTerm, Long.MIN_VALUE, Long.MAX_VALUE);
      case "HOUR":
        return parseIntAndCheckBounds(context, valueTerm, 0, 23);
      case "MINUTE":
        return parseIntAndCheckBounds(context, valueTerm, 0, 59);
      case "SECOND":
        String message = "Number between 0 and 59.999... expected";
        if (decimalPattern.matcher(valueTerm.stringValue()).find() == false) {
          return createErrorMessage(context, valueTerm, message);
        }
        try {
          double value = Double.parseDouble(valueTerm.stringValue());
          if (value < 0 || value >= 60) {
            return createErrorMessage(context, valueTerm, message);
          }
        } catch (NumberFormatException e) {
          return createErrorMessage(context, valueTerm, message);
        }
        return null;
      default:
        return null;
    }
  }

  private static IStrategoTerm parseIntAndCheckBounds(Context context, IStrategoString valueTerm, long lowerBound,
      long upperBound) {
    String message;
    if (upperBound == Long.MAX_VALUE) {
      message = "Integer expected";
    } else {
      message = "Integer between " + lowerBound + " and " + upperBound + " expected";
    }
    try {
      long value = Long.parseLong(valueTerm.stringValue());
      if (value < lowerBound || value > upperBound) {
        return createErrorMessage(context, valueTerm, message);
      }
    } catch (NumberFormatException e) {
      return createErrorMessage(context, valueTerm, message);
    }

    return null;
  }

  private static IStrategoTerm createErrorMessage(Context context, IStrategoTerm errorTerm, String message) {
    ITermFactory f = context.getFactory();
    return f.makeAppl(f.makeConstructor("ErrorMessage", 2), errorTerm, f.makeString(message));
  }
}
