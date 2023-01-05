/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class AbstractPgqlTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected static Pgql pgql;

  @BeforeClass
  public static void setUp() throws Exception {
    pgql = new Pgql();
  }

  @AfterClass
  public static void tearDown() {
    pgql.close();
  }
}
