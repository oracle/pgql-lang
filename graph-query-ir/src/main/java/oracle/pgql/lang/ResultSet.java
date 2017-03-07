/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

/**
 * NOTICE:
 * 
 * The ResultSet interface is not yet defined. PGQL implementers should define their own interface for now.
 */
public interface ResultSet extends AutoCloseable {

  /**
   * Releases this result set's resources. Calling the method close on a ResultSet object that is already closed has no
   * effect.
   */
  @Override
  void close() throws PgqlException;
}
