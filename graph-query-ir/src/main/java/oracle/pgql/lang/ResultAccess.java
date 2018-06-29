/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
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
   * Gets the value of the designated element by element name as an {@link Object}
   *
   * @param elementIdx
   *          element index
   * @return {@link Object}
   */
  public Object getObject(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link Object}
   *
   * @param elementName
   *          element name
   * @return {@link Object}
   */
  public Object getObject(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link String}
   *
   * @param elementIdx
   *          element index
   * @return {@link String}
   */
  public String getString(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link String}
   *
   * @param elementName
   *          element name
   * @return {@link String}
   */
  public String getString(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as an {@link Integer}
   *
   * @param elementIdx
   *          element index
   * @return {@link Integer}
   */
  public Integer getInteger(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link Integer}
   *
   * @param elementName
   *          element name
   * @return {@link Integer}
   */
  public Integer getInteger(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Long}
   *
   * @param elementIdx
   *          element index
   * @return {@link Long}
   */
  public Long getLong(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Long}
   *
   * @param elementName
   *          element name
   * @return {@link Long}
   */
  public Long getLong(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Float}
   *
   * @param elementIdx
   *          element index
   * @return {@link Float}
   */
  public Float getFloat(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Float}
   *
   * @param elementName
   *          element name
   * @return {@link Float}
   */
  public Float getFloat(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Double}
   *
   * @param elementIdx
   *          element index
   * @return {@link Double}
   */
  public Double getDouble(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Double}
   *
   * @param elementName
   *          element name
   * @return {@link Double}
   */
  public Double getDouble(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Boolean}
   *
   * @param elementIdx
   *          element index
   * @return {@link Boolean}
   */
  public Boolean getBoolean(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Boolean}
   *
   * @param elementName
   *          element name
   * @return {@link Boolean}
   */
  public Boolean getBoolean(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as vertex labels
   *
   * @param elementIdx
   *          element index
   * @return vertex labels
   */
  public Set<String> getVertexLabels(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as vertex labels
   *
   * @param elementName
   *          element name
   * @return vertex labels
   */
  public Set<String> getVertexLabels(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link LocalDate}
   *
   * @param elementIdx
   *          element index
   * @return {@link LocalDate}
   */
  public LocalDate getDate(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link LocalDate}
   *
   * @param elementName
   *          element name
   * @return {@link LocalDate}
   */
  public LocalDate getDate(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link LocalTime}
   *
   * @param elementIdx
   *          element index
   * @return {@link LocalTime}
   */
  public LocalTime getTime(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link LocalTime}
   *
   * @param elementName
   *          element name
   * @return {@link LocalTime}
   */
  public LocalTime getTime(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link LocalDateTime}
   *
   * @param elementIdx
   *          element index
   * @return {@link LocalDateTime}
   */
  public LocalDateTime getTimestamp(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link LocalDateTime}
   *
   * @param elementName
   *          element name
   * @return {@link LocalDateTime}
   */
  public LocalDateTime getTimestamp(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as an {@link OffsetTime}
   *
   * @param elementIdx
   *          element index
   * @return {@link OffsetTime}
   */
  public OffsetTime getTimeWithTimezone(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link OffsetTime}
   *
   * @param elementName
   *          element name
   * @return {@link OffsetTime}
   */
  public OffsetTime getTimeWithTimezone(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as an {@link OffsetDateTime}
   *
   * @param elementIdx
   *          element index
   * @return {@link OffsetDateTime}
   */
  public OffsetDateTime getTimestampWithTimezone(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as an {@link OffsetDateTime}
   *
   * @param elementName
   *          element name
   * @return {@link OffsetDateTime}
   */
  public OffsetDateTime getTimestampWithTimezone(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link Date}
   *
   * @param elementIdx
   *          element index
   * @return {@link Date}
   */
  public Date getLegacyDate(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Date}
   *
   * @param elementName
   *          element name
   * @return {@link Date}
   */
  public Date getLegacyDate(String elementName) throws PgqlException;

  /**
   * Gets the value of the designated element by element index as a {@link List}
   *
   * @param elementIdx
   *          element index
   * @return {@link List}
   */
  public <T> List<T> getList(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link List}
   *
   * @param elementName
   *          element name
   * @return {@link List}
   */
  public <T> List<T> getList(String elementName) throws PgqlException;
}
