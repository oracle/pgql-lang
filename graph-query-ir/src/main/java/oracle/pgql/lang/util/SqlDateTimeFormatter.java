/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.util;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

public class SqlDateTimeFormatter {

  public static final DateTimeFormatter SQL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  /**
   * Differences with DateTimeFormatter.ISO_LOCAL_TIME:
   * - seconds are optional in ISO but not in SQL
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
   * Differences with DateTimeFormatter.ISO_LOCAL_DATE_TIME:
   * - seconds are optional in ISO but not in SQL
   * - date and time are separated by 'T' in ISO but by ' ' in SQL
   */
  public static final DateTimeFormatter SQL_TIMESTAMP = new DateTimeFormatterBuilder() //
      .append(SQL_DATE) //
      .appendLiteral(' ') //
      .append(SQL_TIME).toFormatter().withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);

  /**
   * Differences with DateTimeFormatter.ISO_OFFSET_TIME:
   * - seconds are optional in ISO but not in SQL
   * - a zero offset in ISO can optionally be denoted by appending 'Z' at the end, but this is not allowed in SQL
   */
  public static final DateTimeFormatter SQL_TIME_WITH_TIMEZONE = new DateTimeFormatterBuilder() //
      .append(SQL_TIME) //
      .appendOffset("+HH:MM", "").toFormatter();

  /**
   * Differences with DateTimeFormatter.ISO_OFFSET_DATE_TIME:
   * - seconds are optional in ISO but not in SQL
   * - date and time are separated by 'T' in ISO but by ' ' in SQL
   * - a zero offset in ISO can optionally be denoted by appending 'Z' at the end, but this is not allowed in SQL
   */
  public static final DateTimeFormatter SQL_TIMESTAMP_WITH_TIMEZONE = new DateTimeFormatterBuilder() //
      .append(SQL_TIMESTAMP) //
      .appendOffset("+HH:MM", "").toFormatter();
}
