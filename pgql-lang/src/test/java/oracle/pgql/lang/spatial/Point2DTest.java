/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.spatial;

import static oracle.pgql.lang.spatial.Point2D.fromWkt;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Point2DTest {

  @Test
  public void testParsePointFromWkt() throws Exception {
    Point2D point = fromWkt("POINT (1 2)");
    assertTrue(point.getX() == 1);
    assertTrue(point.getY() == 2);
  }
  
  @Test
  public void testParsePointMFromWkt() throws Exception {
    Point2D point = fromWkt("POINT M (1 2 80)");
    assertTrue(point.getX() == 1);
    assertTrue(point.getY() == 2);
    assertTrue(point.getM() == 80);
  }
}
