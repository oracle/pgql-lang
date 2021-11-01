/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

public interface ResultSetMetaData {

  /**
   * Returns the number of columns in this <code>ResultSet</code> object.
   * 
   * @return the number of columns
   */
  public int getColumnCount();
  
  /**
   * Get the designated column's name.
   * 
   * @param column the first column is 1, the second is 2, ...
   * @return column name
   */
  public String getColumnName(int column);
}
