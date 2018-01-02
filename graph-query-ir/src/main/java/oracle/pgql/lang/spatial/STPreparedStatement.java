/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.spatial;

import oracle.pgql.lang.PgqlException;

public interface STPreparedStatement {

  /**
   * Sets the designated parameter to the given Point2D value.
   *
   * @param parameterIndex
   *          the first parameter is 1, the second is 2, ...
   * @param x
   *          the parameter value
   */
  void setPoint2D(int parameterIndex, Point2D x) throws PgqlException;
}
