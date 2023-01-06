/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Get methods to access the values in the columns. The parameter indicates the column number or column name. Just like
 * the SQL ResultSet, columns are numbered starting from 1.
 */
public interface ResultAccess {

  /**
   * Retrieves the value of the designated column in the current row as an {@link Object}
   *
   * @param columnIdx
   *          column index
   * @return {@link Object}
   */
  public Object getObject(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link Object}
   *
   * @param columnName
   *          column name
   * @return {@link Object}
   */
  public Object getObject(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link String}
   *
   * @param columnIdx
   *          column index
   * @return {@link String}
   */
  public String getString(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link String}
   *
   * @param columnName
   *          column name
   * @return {@link String}
   */
  public String getString(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link Integer}
   *
   * @param columnIdx
   *          column index
   * @return {@link Integer}
   */
  public Integer getInteger(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link Integer}
   *
   * @param columnName
   *          column name
   * @return {@link Integer}
   */
  public Integer getInteger(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Long}
   *
   * @param columnIdx
   *          column index
   * @return {@link Long}
   */
  public Long getLong(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Long}
   *
   * @param columnName
   *          column name
   * @return {@link Long}
   */
  public Long getLong(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Float}
   *
   * @param columnIdx
   *          column index
   * @return {@link Float}
   */
  public Float getFloat(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Float}
   *
   * @param columnName
   *          column name
   * @return {@link Float}
   */
  public Float getFloat(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Double}
   *
   * @param columnIdx
   *          column index
   * @return {@link Double}
   */
  public Double getDouble(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Double}
   *
   * @param columnName
   *          column name
   * @return {@link Double}
   */
  public Double getDouble(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Boolean}
   *
   * @param columnIdx
   *          column index
   * @return {@link Boolean}
   */
  public Boolean getBoolean(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Boolean}
   *
   * @param columnName
   *          column name
   * @return {@link Boolean}
   */
  public Boolean getBoolean(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as vertex labels
   *
   * @param columnIdx
   *          column index
   * @return vertex labels
   */
  public Set<String> getVertexLabels(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as vertex labels
   *
   * @param columnName
   *          column name
   * @return vertex labels
   */
  public Set<String> getVertexLabels(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link LocalDate}
   *
   * @param columnIdx
   *          column index
   * @return {@link LocalDate}
   */
  public LocalDate getDate(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link LocalDate}
   *
   * @param columnName
   *          column name
   * @return {@link LocalDate}
   */
  public LocalDate getDate(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link LocalTime}
   *
   * @param columnIdx
   *          column index
   * @return {@link LocalTime}
   */
  public LocalTime getTime(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link LocalTime}
   *
   * @param columnName
   *          column name
   * @return {@link LocalTime}
   */
  public LocalTime getTime(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link LocalDateTime}
   *
   * @param columnIdx
   *          column index
   * @return {@link LocalDateTime}
   */
  public LocalDateTime getTimestamp(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link LocalDateTime}
   *
   * @param columnName
   *          column name
   * @return {@link LocalDateTime}
   */
  public LocalDateTime getTimestamp(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link OffsetTime}
   *
   * @param columnIdx
   *          column index
   * @return {@link OffsetTime}
   */
  public OffsetTime getTimeWithTimezone(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link OffsetTime}
   *
   * @param columnName
   *          column name
   * @return {@link OffsetTime}
   */
  public OffsetTime getTimeWithTimezone(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link OffsetDateTime}
   *
   * @param columnIdx
   *          column index
   * @return {@link OffsetDateTime}
   */
  public OffsetDateTime getTimestampWithTimezone(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as an {@link OffsetDateTime}
   *
   * @param columnName
   *          column name
   * @return {@link OffsetDateTime}
   */
  public OffsetDateTime getTimestampWithTimezone(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Date}
   *
   * @param columnIdx
   *          column index
   * @return {@link Date}
   */
  public Date getLegacyDate(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link Date}
   *
   * @param columnName
   *          column name
   * @return {@link Date}
   */
  public Date getLegacyDate(String columnName) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link List}
   *
   * @param columnIdx
   *          column index
   * @return {@link List}
   */
  public <T> List<T> getList(int columnIdx) throws PgqlException;

  /**
   * Retrieves the value of the designated column in the current row as a {@link List}
   *
   * @param columnName
   *          column name
   * @return {@link List}
   */
  public <T> List<T> getList(String columnName) throws PgqlException;
}
