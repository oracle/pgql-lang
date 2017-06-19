/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.spatial;

/**
 * A point in a 3-dimensional space
 */
public class Point3D {

  private final double x;

  private final double y;

  private final double z;

  private final double m;


  /**
   * Create a point in a 3-dimensional space
   *
   * @param x longitude
   * @param y latitude
   * @param z altitude
   */
  public Point3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = Double.NaN;
  }

  /**
   * Create a point in a 3-dimensional space with a linear reference
   *
   * @param x longitude
   * @param y latitude
   * @param z altitude
   * @param m linear reference
   */
  public Point3D(double x, double y, double z, double m) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = m;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Point3D)) {
      return false;
    }

    Point3D point = (Point3D) o;

    if (Double.compare(point.getX(), x) != 0) {
      return false;
    }
    if (Double.compare(point.getY(), y) != 0) {
      return false;
    }
    if (Double.compare(point.getZ(), z) != 0) {
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
    temp = Double.doubleToLongBits(getZ());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getM());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * @return the x value (longitude) of this point
   */
  public double getX() {
    return x;
  }

  /**
   * @return the y value (latitude) of this point
   */
  public double getY() {
    return y;
  }

  /**
   * @return the z value (altitude) of this point
   */
  public double getZ() {
    return z;
  }

  /**
   * @return the m value (linear reference) of this point
   */
  public double getM() {
    return m;
  }
}
