---
title: Home
permalink: /index.html
toc: false
keywords: pgql graph query language database analytics social
---

Graphs + SQL
====================================

PGQL is a query language for the [property graph data model](spec/1.1/#property-graph-data-model) that combines the power of __graph pattern matching__ with __SQL__:


```sql
SELECT p2.name AS friend_of_friend
  FROM facebook_graph                             /* In the Facebook graph..   */
 MATCH (p1:Person) -/:friend_of{2}/-> (p2:Person) /* ..match two-hop friends.. */
 WHERE p1.name = 'Mark'                           /* ..of Mark.                */
```

For a detailed specification of PGQL, see [PGQL 1.1 Specification](spec/1.1/).

Graph Pattern Matching
----------------------

PGQL uses ASCII-art syntax for matching vertices, edges, and paths:

 * `(n:Person)` matches a vertex (node) `n` with label `Person`
 * `-[e:friend_of]->` matches an edge `e` with label `friend_of`
 * `-/:friend_of+/->` matches a path consisting of one or more (`+`) edges, each with label `friend_of`

SQL Capabilities
-------------------

PGQL has the following SQL-like capabilities:

 * `DISTINCT` to remove duplicates
 * `GROUP BY` and `HAVING` to create groups of solutions and optionally filter such groups
 * `MIN`, `MAX`, `SUM` and `AVG` to aggregate over groups of solutions
 * `ORDER BY` to sort results
 * (`NOT`) `EXISTS` subqueries, to test whether a graph pattern exists, or, doesn't exists

Regular Path Expressions
------------------------

Regular path expressions allow for expressing complex traversals for all sorts of reachability analysis use cases:


{% include image.html file="example_graphs/electric_network.png" %}

```sql
    PATH connects_to AS (:Device) <- (x) -> (:Device)                /* Devices are connected by two edges...                 */
                  WHERE has_label(x, 'Connection')                   /* ...and an intermediate Connection vertex...           */
                     OR has_label(x, 'Switch') AND x.status = 'OPEN' /* ...or an intermediate Switch vertex with OPEN status. */
    FROM electric_network
  SELECT d2.name
   MATCH (d1) -/:connects_to+/-> (d2)                                 /* We match the connect_to pattern one or more times    */
   WHERE d1.name = 'DS'
ORDER BY d2.name
```

Query output:

```
+---------+
| d2.name |
+---------+
| D0      |
| D5      |
| D6      |
| D7      |
| D8      |
| D9      |
+---------+
```

Multiple Graph Support
-----------------------
The following query finds people who are on Facebook but not on Twitter:

```sql
SELECT p1.name
  FROM facebook_graph
 MATCH (p1:Person)
 WHERE NOT EXISTS ( SELECT *
                      FROM twitter_graph
                     MATCH (p2:Person)
                     WHERE p1.name = p2.name )
```

Resources
---------

 - __Specifications__
     - [PGQL 1.1 Specification](spec/1.1/) (latest spec.)
     - [PGQL 1.0 Specification](spec/1.0/)
     - [PGQL 0.9 Specification](https://docs.oracle.com/cd/E56133_01/1.2.1/PGQL_Specification.pdf)
 - [Open-sourced __parser__ and __static query validator__](https://github.com/oracle/pgql-lang) on GitHub
 - [__White Paper__](http://dl.acm.org/citation.cfm?id=2960421) ([pdf](http://event.cwi.nl/grades/2016/07-VanRest.pdf)) that also outlines some future directions of PGQL
   (shortest path finding, graph construction, etc.)
 - __Implementations__
     - [Oracle Labs' Parallel Graph Analytics (__PGX__)](http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/overview/index.html), an in-memory graph analytics framework with a high-performance PGQL query engine
     - [Oracle __Big Data__ Spatial and Graph](http://www.oracle.com/technetwork/database/database-technologies/bigdata-spatialandgraph/overview/index.html), which supports PGQL on top of Big Data workloads on Apache Hadoop and NoSQL database technologies
     - [Oracle Spatial and Graph](https://www.oracle.com/database/spatial/index.html) which supports PGQL on top of the Oracle RDBMS
