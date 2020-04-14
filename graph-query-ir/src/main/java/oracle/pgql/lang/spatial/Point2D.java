/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.spatial;

import java.io.Serializable;

/**
 * A point in a 2-dimensional space
 */
public class Point2D implements Serializable {

  private final double x;

  private final double y;

  private final double m;

  /**
   * Create a point in a 2-dimensional space
   *
   * @param x
   *          longitude
   * @param y
   *          latitude
   */
  public Point2D(double x, double y) {
    this.x = x;
    this.y = y;
    this.m = Double.NaN;
  }

  /**
   * Create a point in a 2-dimensional space with a linear reference
   *
   * @param x
   *          longitude
   * @param y
   *          latitude
   * @param m
   *          linear reference
   */
  public Point2D(double x, double y, double m) {
    this.x = x;
    this.y = y;
    this.m = m;
  }

  /**
   * Create a Point2D object from a well-known text representation
   *
   * @param wktPoint
   *          well-known text representation of a point value
   * @return Point2D object
   */
  public static Point2D fromWkt(String wktPoint) {
    if (wktPoint.indexOf('(') == -1 || wktPoint.indexOf(')') == -1 || wktPoint.indexOf('(') > wktPoint.indexOf(')')) {
      throw new WktParseException(wktPoint + " is not in the right format");
    }
    String prefix = wktPoint.substring(0, wktPoint.indexOf('('));
    String values = wktPoint.substring(wktPoint.indexOf('(') + 1, wktPoint.indexOf(')'));

    int xValEndPos = values.indexOf(' ');
    if (xValEndPos == -1) {
      throw new WktParseException(wktPoint + " is not in the right format");
    }
    Double xVal = Double.parseDouble(values.substring(0, xValEndPos));

    int yValStartPos = values.indexOf(' ') + 1;
    switch (prefix.trim()) {
      case "POINT":
        Double yVal = Double.parseDouble(values.substring(yValStartPos, values.length()));
        return new Point2D(xVal, yVal);
      case "POINT M":
        int yValEndPos = values.substring(yValStartPos).indexOf(' ') + yValStartPos + 1;
        yVal = Double.parseDouble(values.substring(yValStartPos, yValEndPos));
        Double mVal = Double.parseDouble(values.substring(yValEndPos));
        return new Point2D(xVal, yVal, mVal);
      default:
        throw new WktParseException(wktPoint + " is not in the right format");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Point2D)) {
      return false;
    }

    Point2D point = (Point2D) o;

    if (Double.compare(point.getX(), x) != 0) {
      return false;
    }
    if (Double.compare(point.getY(), y) != 0) {
      return false;
    }
    return Double.compare(point.getM(), m) == 0;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(getX());
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getY());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getM());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * @return the x value of this point
   */
  public double getX() {
    return x;
  }

  /**
   * @return the y value of this point
   */
  public double getY() {
    return y;
  }

  /**
   * @return the m value of this point
   */
  public double getM() {
    return m;
  }
}
