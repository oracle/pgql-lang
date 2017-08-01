/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

/**
 * A result of a pattern matching query. The get methods can be used to access the values in the columns.
 * The parameter indicates the column name or number. Just like the SQL ResultSet, columns are numbered from 1.
 */
public interface Result extends ResultAccess {

}
