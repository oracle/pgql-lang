/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.Set;

/**
 * A table of data representing the result of a PGQL query. The cursor is positioned before the first row. After the
 * first call to the function `next()`, the cursor will be located at the first row and you can retrieve the data in
 * the columns with one of suitable get methods. Just like the SQL ResultSet, columns are numbered from 1.
 */
public interface ResultSet extends AutoCloseable {

  /**
   * Moves the cursor forward one row from its current position.
   *
   * @return <code>true</code> if the new current row is valid; <code>false</code> if the new cursor is positioned
   * after the last row
   */
  public boolean next() throws PgqlException;

  /**
   * Moves the cursor to the previous row from its current position.
   *
   * @return <code>true</code> if the new current row is valid; <code>false</code> if the new cursor is positioned
   * before the first row
   */
  public boolean previous() throws PgqlException;

  /**
   * Place the cursor before the first row.
   */
  public void beforeFirst() throws PgqlException;

  /**
   * Place the cursor after the last row.
   */
  public void afterLast() throws PgqlException;

  /**
   * Place the cursor to the given row number in this ResultSet object.
   *
   * @return <code>true</code> if the cursor is moved to a position in the ResultSet object; <code>false</code> if
   * the cursor is moved before the first or after the last row
   * @throws PgqlException if a connection error occurs or when this method is called on a closed result set
   */
  public boolean absolute(int row) throws PgqlException;

  /**
   * Moves the cursor a relative number of row with repect to the current position. A negative number will move the
   * cursor backwards.
   *
   * @return <code>true</code> if the cursor is moved to a position in the ResultSet object; <code>false</code> if
   * the cursor is moved before the first or after the last row
   */
  public boolean relative(int rows) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link Object}
   *
   * @param elementIdx element index
   * @return {@link Object}
   */
  public Object getObject(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link Object}
   *
   * @param elementName element name
   * @return {@link Object}
   */
  public Object getObject(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link String}
   *
   * @param elementIdx element index
   * @return {@link String}
   */
  public String getString(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link String}
   *
   * @param elementName element name
   * @return {@link String}
   */
  public String getString(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as an {@link Integer}
   *
   * @param elementIdx element index
   * @return {@link Integer}
   */
  public Integer getInteger(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link Integer}
   *
   * @param elementName element name
   * @return {@link Integer}
   */
  public Integer getInteger(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Long}
   *
   * @param elementIdx element index
   * @return {@link Long}
   */
  public Long getLong(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Long}
   *
   * @param elementName element name
   * @return {@link Long}
   */
  public Long getLong(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Float}
   *
   * @param elementIdx element index
   * @return {@link Float}
   */
  public Float getFloat(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Float}
   *
   * @param elementName element name
   * @return {@link Float}
   */
  public Float getFloat(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Double}
   *
   * @param elementIdx element index
   * @return {@link Double}
   */
  public Double getDouble(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Double}
   *
   * @param elementName element name
   * @return {@link Double}
   */
  public Double getDouble(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Boolean}
   *
   * @param elementIdx element index
   * @return {@link Boolean}
   */
  public Boolean getBoolean(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Boolean}
   *
   * @param elementName element name
   * @return {@link Boolean}
   */
  public Boolean getBoolean(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as vertex labels
   *
   * @param elementIdx element index
   * @return vertex labels
   */
  public Set<String> getVertexLabels(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as vertex labels
   *
   * @param elementName element name
   * @return vertex labels
   */
  public Set<String> getVertexLabels(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link LocalDate}
   *
   * @param elementIdx element index
   * @return {@link LocalDate}
   */
  public LocalDate getDate(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link LocalDate}
   *
   * @param elementName element name
   * @return {@link LocalDate}
   */
  public LocalDate getDate(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link LocalTime}
   *
   * @param elementIdx element index
   * @return {@link LocalTime}
   */
  public LocalTime getTime(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link LocalTime}
   *
   * @param elementName element name
   * @return {@link LocalTime}
   */
  public LocalTime getTime(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link LocalDateTime}
   *
   * @param elementIdx element index
   * @return {@link LocalDateTime}
   */
  public LocalDateTime getTimestamp(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link LocalDateTime}
   *
   * @param elementName element name
   * @return {@link LocalDateTime}
   */
  public LocalDateTime getTimestamp(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as an {@link OffsetTime}
   *
   * @param elementIdx element index
   * @return {@link OffsetTime}
   */
  public OffsetTime getTimeWithTimezone(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link OffsetTime}
   *
   * @param elementName element name
   * @return {@link OffsetTime}
   */
  public OffsetTime getTimeWithTimezone(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as an {@link OffsetDateTime}
   *
   * @param elementIdx element index
   * @return {@link OffsetDateTime}
   */
  public OffsetDateTime getTimestampWithTimezone(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link OffsetDateTime}
   *
   * @param elementName element name
   * @return {@link OffsetDateTime}
   */
  public OffsetDateTime getTimestampWithTimezone(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Date}
   *
   * @param elementIdx element index
   * @return {@link Date}
   */
  public Date getLegacyDate(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Date}
   *
   * @param elementName element name
   * @return {@link Date}
   */
  public Date getLegacyDate(String elementName) throws PgqlException;

  /**
   * Releases this result set's resources. Calling the method close on a ResultSet object that is already closed has no
   * effect.
   */
  @Override
  void close() throws PgqlException;
}
