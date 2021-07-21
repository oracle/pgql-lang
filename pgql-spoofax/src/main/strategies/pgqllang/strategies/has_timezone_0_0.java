/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import static pgqllang.strategies.DateTimeFormatters.SQL_TIME;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIMESTAMP;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIME_WITH_TIMEZONE;
import static pgqllang.strategies.DateTimeFormatters.SQL_TIMESTAMP_WITH_TIMEZONE;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class has_timezone_0_0 extends Strategy {

  public static has_timezone_0_0 instance = new has_timezone_0_0();

  @Override
  public IStrategoTerm invoke(Context context, IStrategoTerm current) {

    IStrategoAppl appl = (IStrategoAppl) current;
    String s = ((IStrategoString) appl.getSubterm(0)).stringValue();

    switch (appl.getConstructor().getName()) {
      case "Time":
        try {
          LocalTime.parse(s, SQL_TIME);
          return null;
        } catch (DateTimeParseException e) {
          try {
            OffsetTime.parse(s, SQL_TIME_WITH_TIMEZONE);
            return current;
          } catch (DateTimeParseException e2) {
            return null;
          }
        }
      case "Timestamp":
        try {
          LocalDateTime.parse(s, SQL_TIMESTAMP);
          return null;
        } catch (DateTimeParseException e) {
          try {
            OffsetDateTime.parse(s, SQL_TIMESTAMP_WITH_TIMEZONE);
            return current;
          } catch (DateTimeParseException e2) {
            return null;
          }
        }
      default:
        return null;
    }
  }
}
