---
title: Home
permalink: /index.html
toc: false
keywords: pgql property graph query language database analytics oracle
---

Graphs + SQL
====================================

PGQL is a query language for the [property graph data model](spec/1.1/#property-graph-data-model) and combines the power of __graph pattern matching__ with __SQL__:


```sql
SELECT p2.name AS friend_of_friend
  FROM facebook_graph                             /* In the Facebook graph..   */
 MATCH (p1:Person) -/:friend_of{2}/-> (p2:Person) /* ..match two-hop friends.. */
 WHERE p1.name = 'Mark'                           /* ..of Mark.                */
```

For a detailed specification of PGQL, see [PGQL 1.1 Specification](spec/1.1/).

Graph Pattern Matching
----------------------

PGQL uses ASCII-art syntax for matching __vertices__, __edges__, and __paths__:

 * `(n:Person)` matches a vertex (node) `n` with label `Person`
 * `-[e:friend_of]->` matches an edge `e` with label `friend_of`
 * `-/:friend_of+/->` matches a path consisting of one or more (`+`) edges, each with label `friend_of`

SQL Capabilities
-------------------

PGQL has the following SQL-like capabilities:

 * `DISTINCT` to remove duplicates
 * `GROUP BY` and create groups of solutions and optionally filter groups through `HAVING`
 * `MIN`, `MAX`, `AVG` and `SUM` to aggregate over groups of solutions
 * `ORDER BY` to sort results
 * (`NOT`) `EXISTS` subqueries, to test whether a graph pattern exists, or, doesn't exists

Regular PATH Expressions
------------------------

Regular PATH expressions allow for expressing complex traversals for all sorts of __reachability analysis__ use cases:

{% include image.html file="example_graphs/electric_network.png" %}

```sql
    PATH connects_to AS (:Device) <- (x) -> (:Device)                /* Devices are connected by two edges..                 */
                  WHERE has_label(x, 'Connection')                   /* ..and an intermediate Connection vertex..            */
                     OR has_label(x, 'Switch') AND x.status = 'OPEN' /* ..or an intermediate Switch vertex with OPEN status. */
    FROM electric_network
  SELECT d1.name AS source, d2.name AS destination
   MATCH (d1) -/:connects_to+/-> (d2)                                 /* We match the connect_to pattern one or more times.   */
   WHERE d1.name = 'DS'
ORDER BY d2.name
```

Query output:

```
+--------+-------------+
| source | destination | /* PGQL returns tables with columns, like SQL. */
+--------+-------------+
| DN     | D0          | /* First row. */
| DN     | D5          |
| DN     | D6          |
| DN     | D7          |
| DN     | D8          |
| DN     | D9          | /* Last row. */
+--------+-------------+
```

Multiple Graph Support
-----------------------
The following query finds people who are on Facebook but not on Twitter:

```sql
SELECT p1.name
  FROM facebook_graph
 MATCH (p1:Person)                           /* Match persons in the Facebook graph.. */
 WHERE NOT EXISTS (                          /* ..such that there doesn't exists..    */
                    SELECT p2
                      FROM twitter_graph
                     MATCH (p2:Person)       /* ..a person in the Twitter graph..     */
                     WHERE p1.name = p2.name /* ..with the same name.                 */
                  )
```

Resources
---------

 - [__White Paper__](http://dl.acm.org/citation.cfm?id=2960421) ([pdf](http://event.cwi.nl/grades/2016/07-VanRest.pdf)) that also outlines some future directions of PGQL
   (shortest path finding, graph construction, etc.)
 - __Specifications__
     - [PGQL 1.1 Specification](spec/1.1/) (latest spec.)
     - [PGQL 1.0 Specification](spec/1.0/)
     - [PGQL 0.9 Specification](https://docs.oracle.com/cd/E56133_01/1.2.1/PGQL_Specification.pdf)
 - [Open-sourced __parser__ and __static query validator__](https://github.com/oracle/pgql-lang) on GitHub
 - __Implementations__
     - [Oracle Labs' Parallel Graph Analytics (__PGX__)](http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/overview/index.html), an in-memory graph analytics framework with a high-performance PGQL query engine
     - [Oracle __Big Data__ Spatial and Graph](http://www.oracle.com/technetwork/database/database-technologies/bigdata-spatialandgraph/overview/index.html), which supports PGQL on top of big data workloads on Apache Hadoop and NoSQL database technologies
     - [Oracle Spatial and Graph](https://www.oracle.com/database/spatial/index.html), which supports PGQL on top of the Oracle RDBMS
