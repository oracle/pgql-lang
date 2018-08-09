---
title: Home
permalink: /index.html
toc: false
keywords: pgql property graph model query language database analytics oracle cypher opencypher sparql
---

Graphs + SQL
====================================

PGQL is a graph pattern matching query language for the [property graph data model](spec/1.1/#property-graph-data-model), inspired by
[SQL](https://en.wikipedia.org/wiki/SQL),
[Cypher](https://homepages.inf.ed.ac.uk/libkin/papers/sigmod18.pdf), and
[G-CORE](https://arxiv.org/pdf/1712.01550.pdf).
PGQL combines Cypher-like [ASCII-art syntax](https://neo4j.com/developer/cypher-query-language/) with familiar constructs from SQL, such as `SELECT`, `FROM` and `WHERE`.
PGQL also provides powerful constructs for matching regular path expressions (i.e. `PATH`).

An example PGQL query is as follows:


```sql
SELECT p2.name AS friend_of_friend
  FROM facebook_graph                             /* In the Facebook graph..   */
 MATCH (p1:Person) -/:friend_of{2}/-> (p2:Person) /* ..match two-hop friends.. */
 WHERE p1.name = 'Mark'                           /* ..of Mark.                */
```

See [PGQL 1.1 Specification](spec/1.1/) for a detailed specification of the language.

Graph Pattern Matching
----------------------

PGQL uses [ASCII-art syntax](https://neo4j.com/developer/cypher-query-language/) for matching vertices, edges, and paths:

 * `(n:Person)` matches a __vertex__ (node) `n` with label `Person`
 * `-[e:friend_of]->` matches an __edge__ `e` with label `friend_of`
 * `-/:friend_of+/->` matches a __path__ consisting of one or more (`+`) edges, each with label `friend_of`

SQL Capabilities
-------------------

PGQL has the following SQL-like capabilities:

 * `DISTINCT` to remove duplicates
 * `GROUP BY` to create groups of solutions, and, `HAVING` to filter out groups of solutions
 * `COUNT`, `MIN`, `MAX`, `AVG` and `SUM` to aggregate over groups of solutions
 * `ORDER BY` to sort results
 * (`NOT`) `EXISTS` subqueries to test whether a graph pattern exists, or, doesn't exists
 * `DATE`, `TIME`, `TIMESTAMP`, `TIME WITH TIME ZONE`, and `TIMESTAMP WITH TIME ZONE` temporal data types

Regular Path Expressions
------------------------

PGQL has __regular path expressions__ (e.g. `*`, `+`, `?`, `{1,4}`) for expressing complex traversals for all sorts of __reachability analyses__:

{% include image.html file="example_graphs/electric_network.png" %}

```sql
    PATH connects_to AS (:Device|Switch) <- (:Connection) -> (d:Device|Switch) /* Devices and switches are connected by two edges. */
                  WHERE d.status IS NULL OR d.status = 'OPEN'                  /* Only consider switches with OPEN status. */
  SELECT d1.name AS source, d2.name AS destination
    FROM electric_network
   MATCH (d1:Device) -/:connects_to+/-> (d2:Device)                            /* We match the connects_to pattern one or more (+) times. */
   WHERE d1.name = 'DS'
ORDER BY d2.name
```

```
+--------+-------------+
| source | destination | /* The result of above query is a table with columns, like in SQL. */
+--------+-------------+
| DN     | D0          | /* First result row. */
| DN     | D5          |
| DN     | D6          |
| DN     | D7          |
| DN     | D8          |
| DN     | D9          | /* Last result row. */
+--------+-------------+
```

Temporal Data Types
-------------------
In addition to numbers, (character) strings, and booleans, PGQL has the following temporal data types:

 - `DATE` (java.time.LocalDate)
 - `TIME` (java.time.LocalTime)
 - `TIMESTAMP` (java.time.LocalDateTime)
 - `TIME WITH TIME ZONE` (java.time.OffsetTime)
 - `TIMESTAMP WITH TIME ZONE` (java.time.OffsetDateTime)

PGQL's Java result set API
(see [ResultSet.java](https://github.com/oracle/pgql-lang/blob/master/graph-query-ir/src/main/java/oracle/pgql/lang/ResultSet.java)
and [ResultAccess.java](https://github.com/oracle/pgql-lang/blob/master/graph-query-ir/src/main/java/oracle/pgql/lang/ResultAccess.java))
 is based on the [__new Java 8 Date and Time Library__](http://www.oracle.com/technetwork/articles/java/jf14-date-time-2125367.html) (`java.time.*`), offering greatly improved safety and functionality for Java developers.

Querying Multiple Graphs
-----------------------
Through subqueries, PGQL allows for __comparing data from different graphs__.

For example, the following query finds people who are on Facebook but not on Twitter:

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
   (e.g. shortest/cheapest path finding, graph construction)
 - __Specifications__
     - [PGQL 1.1 Specification](spec/1.1/) (latest spec.)
     - [PGQL 1.0 Specification](spec/1.0/)
     - [PGQL 0.9 Specification](https://docs.oracle.com/cd/E56133_01/1.2.1/PGQL_Specification.pdf)
 - [Open-sourced __parser__ and __static query validator__](https://github.com/oracle/pgql-lang) on GitHub
 - __Implementations__
     - [Oracle Labs' Parallel Graph AnalytiX (__PGX__)](http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/overview/index.html), an in-memory graph analytics toolkit
     - [Oracle __Big Data__ Spatial and Graph](http://www.oracle.com/technetwork/database/database-technologies/bigdata-spatialandgraph/overview/index.html), which supports PGQL on top of big data workloads on Apache Hadoop and NoSQL database technologies
     - [Oracle Spatial and Graph](https://www.oracle.com/database/spatial/index.html), which supports PGQL on top of the Oracle RDBMS
