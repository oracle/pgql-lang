/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import static pgqllang.strategies.DateTimeFormatters.SQL_DATE;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIME;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIMESTAMP;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIME_WITH_TIMEZONE;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIMESTAMP_WITH_TIMEZONE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class is_valid_datetime_0_0 extends Strategy {

  public static is_valid_datetime_0_0 instance = new is_valid_datetime_0_0();

  @Override
  public IStrategoTerm invoke(Context context, IStrategoTerm current) {

    IStrategoAppl appl = (IStrategoAppl) current;
    String s = ((IStrategoString) appl.getSubterm(0)).stringValue();

    switch (appl.getConstructor().getName()) {
      case "Date":
        try {
          LocalDate.parse(s, SQL_DATE);
          return current;
        } catch (DateTimeParseException e) {
          String message = "Not a valid date: " + e.getMessage() + ". An example of a valid date is '2018-01-15'.";
          return createErrorMessage(context, current, message);
        }
      case "Time":
        try {
          LocalTime.parse(s, SQL_TIME);
          return current;
        } catch (DateTimeParseException e) {
          try {
            OffsetTime.parse(s, SQL_TIME_WITH_TIMEZONE);
            return current;
          } catch (DateTimeParseException e2) {
            String message = "Not a valid time: " + e.getMessage()
                + ". Examples of valid times are '16:30:00' and '15:30:00+01:00'.";
            return createErrorMessage(context, current, message);
          }
        }
      case "Timestamp":
        try {
          LocalDateTime.parse(s, SQL_TIMESTAMP);
          return current;
        } catch (DateTimeParseException e) {
          try {
            OffsetDateTime.parse(s, SQL_TIMESTAMP_WITH_TIMEZONE);
            return current;
          } catch (DateTimeParseException e2) {
            String message = "Not a valid timestamp: " + e.getMessage()
                + ". Examples of valid timestamps are '2018-01-15 16:30:00' and '2018-01-15 15:30:00+01:00'.";
            return createErrorMessage(context, current, message);
          }
        }
      default:
        return null;
    }
  }

  private static IStrategoTerm createErrorMessage(Context context, IStrategoTerm current, String message) {
    ITermFactory f = context.getFactory();
    return f.makeAppl(f.makeConstructor("ErrorMessage", 2), current, f.makeString(message));

  }
}
