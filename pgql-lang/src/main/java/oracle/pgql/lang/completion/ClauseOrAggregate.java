/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completion;

public enum ClauseOrAggregate implements Keyword {
  SELECT("SELECT", "SELECT", "", "select clause"),
  FROM("FROM", "FROM", "", "from clause"),
  MATCH("MATCH", "MATCH", "", "match clause"),
  WHERE("WHERE", "WHERE", "", "where clause"),
  ORDER_BY("ORDER BY", "ORDER BY", "", "order by clause"),
  GROUP_BY("GROUP BY", "GROUP BY", "", "group by clause"),
  HAVING("HAVING", "HAVING", "", "having clause"),
  LIMIT("LIMIT", "LIMIT", "", "limit clause"),
  OFFSET("OFFSET", "OFFSET", "", "offset clause"),
  COUNT("COUNT", "COUNT(exp)", "", "count the number of times the expression evaluates to a non-null value"),
  LISTAGG("LISTAGG", "LISTAGG(exp [, separator])", "", "concatenation of values"),
  MIN("MIN", "MIN(exp)", "", "minimum"),
  MAX("MAX", "MAX(exp)", "", "maximum"),
  AVG("AVG", "AVG(exp)", "", "average"),
  SUM("SUM", "SUM(exp)", "", "sum");

  private final Fields fields;

  ClauseOrAggregate(String... parameters) {
    this.fields = new Fields(parameters);
  }

  @Override
  public Fields getFields() {
    return fields;
  }
}
