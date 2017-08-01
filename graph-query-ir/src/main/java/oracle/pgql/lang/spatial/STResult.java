/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.spatial;

/**
 * A result of a pattern matching query with spatial types. The get methods can be used to access the values in the
 * columns. The parameter indicates the column number or column name. Just like the SQL ResultSet, columns are
 * numbered from 1.
 */
public interface STResult extends STResultAccess {

}
