/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

/**
 * ANY is not a path finding goal because it is translated by the parser either into REACHES (when no data along path is
 * retrieved) or SHORTEST (when data along path is retrieved)
 */
public enum PathFindingGoal {

  ALL,
  REACHES,
  SHORTEST,
  CHEAPEST
}
