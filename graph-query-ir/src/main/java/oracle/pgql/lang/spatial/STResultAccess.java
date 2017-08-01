/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.spatial;

import oracle.pgql.lang.PgqlException;

/**
 * Get methods to access spatial type values in the result set. The parameter indicates the column number or column
 * name. Just like the SQL ResultSet, columns are numbered from 1.
 */
public interface STResultAccess {

  /**
   * Gets the value of the designated element by element index as a {@link Point2D}
   *
   * @param elementIdx element index
   * @return {@link Point2D}
   */
  public Point2D getPoint2D(int elementIdx) throws PgqlException;

  /**
   * Gets the value of the designated element by element name as a {@link Point2D}
   *
   * @param elementName element name
   * @return {@link Point2D}
   */
  public Point2D getPoint2D(String elementName) throws PgqlException;
}
