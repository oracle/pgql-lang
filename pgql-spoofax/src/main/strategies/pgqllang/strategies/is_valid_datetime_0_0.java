/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class is_valid_datetime_0_0 extends Strategy {

  public static is_valid_datetime_0_0 instance = new is_valid_datetime_0_0();

  public static final DateTimeFormatter SQL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  /**
   * Differences with DateTimeFormatter.ISO_LOCAL_TIME: - seconds are optional in ISO but not in SQL
   */
  public static final DateTimeFormatter SQL_TIME = new DateTimeFormatterBuilder() //
      .appendValue(HOUR_OF_DAY, 2) //
      .appendLiteral(':') //
      .appendValue(MINUTE_OF_HOUR, 2) //
      .appendLiteral(':') //
      .appendValue(SECOND_OF_MINUTE, 2) //
      .optionalStart() //
      .appendFraction(NANO_OF_SECOND, 0, 9, true).toFormatter().withResolverStyle(ResolverStyle.STRICT);

  /**
   * Differences with DateTimeFormatter.ISO_LOCAL_DATE_TIME: - seconds are optional in ISO but not in SQL - date and
   * time are separated by 'T' in ISO but by ' ' in SQL
   */
  public static final DateTimeFormatter SQL_TIMESTAMP = new DateTimeFormatterBuilder() //
      .append(SQL_DATE) //
      .appendLiteral(' ') //
      .append(SQL_TIME).toFormatter().withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);

  /**
   * Differences with DateTimeFormatter.ISO_OFFSET_TIME: - seconds are optional in ISO but not in SQL - a zero offset in
   * ISO can optionally be denoted by appending 'Z' at the end, but this is not allowed in SQL
   */
  public static final DateTimeFormatter SQL_TIME_WITH_TIMEZONE = new DateTimeFormatterBuilder() //
      .append(SQL_TIME) //
      .appendOffset("+HH:MM", "").toFormatter();

  /**
   * Differences with DateTimeFormatter.ISO_OFFSET_DATE_TIME: - seconds are optional in ISO but not in SQL - date and
   * time are separated by 'T' in ISO but by ' ' in SQL - a zero offset in ISO can optionally be denoted by appending
   * 'Z' at the end, but this is not allowed in SQL
   */
  public static final DateTimeFormatter SQL_TIMESTAMP_WITH_TIMEZONE = new DateTimeFormatterBuilder() //
      .append(SQL_TIMESTAMP) //
      .appendOffset("+HH:MM", "").toFormatter();

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
          return createErrorMessage(context, message);
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
            return createErrorMessage(context, message);
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
                + ". Examples of valid datetimes are '2018-01-15 16:30:00' and '2018-01-15 15:30:00+01:00'.";
            return createErrorMessage(context, message);
          }
        }
      default:
        return null;
    }
  }

  private static IStrategoTerm createErrorMessage(Context context, String message) {
    ITermFactory f = context.getFactory();
    return f.makeAppl(f.makeConstructor("ErrorMessage", 1), f.makeString(message));

  }
}
