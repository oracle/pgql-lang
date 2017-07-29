/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

/**
 * A table of data representing the result of a PGQL query. The cursor is positioned before the first row. After the
 * first call to the function `next()`, the cursor will be located at the first row and you can retrieve the data in
 * the columns with one of suitable get methods.
 */
public interface ResultSet extends AutoCloseable, ResultAccess {

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
   * Releases this result set's resources. Calling the method close on a ResultSet object that is already closed has no
   * effect.
   */
  @Override
  void close() throws PgqlException;
}
