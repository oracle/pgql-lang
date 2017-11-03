---
title: Home
permalink: /index.html
toc: false
keywords: pgql graph query language database analytics social
---

A Query Language for Property Graphs
====================================

PGQL is a SQL-like query language for [Property Graphs](spec/1.1/#property-graph-data-model) - data structures that consist of *vertices* that are connected to other vertices by *edges*, each of which can have ad-hoc key-value pairs (properties) associated with them.

The language is based on the concept of *graph pattern matching*, which allows you to specify patterns that are matched against vertices and edges in a data graph.  This site is the official home of the [PGQL language specification](spec/1.0/).


Topology Constraints
--------------------

Pattern matching is done using *topology constraints*, which describe a pattern of connections between vertices
in the graph;  value constraints (similar to their SQL equivalents) let you further constrain matches by specifying
properties that those connections and vertices must have.

Say we have a graph of TCP/IP connections on a computer network, and you want to detect cases where someone logged into
one machine, from there into another, and from there into still another - you would query for that pattern like this:

```sql
  SELECT host1.name, host2.name, host3.name                       /* choose what to return */
    FROM computer_network_graph
   MATCH (host1) -[c1]-> (host2) -[c2]-> (host3)                  /* topology must match this pattern */
   WHERE c1.to_port = 22 AND c1.opened = true
     AND c2.to_port = 22 AND c2.opened = true
     AND c1.bytes > 300 AND 2.bytes > 300                         /* meaningful amount of data was exchanged */
     AND c1.start < connection2.start,                            /* second connection within time-frame of first */
     AND c2.start + connection2.duration < c1.start + c1.duration
GROUP BY host1, host2, host3                                      /* aggregate multiple matching connections */
ORDER BY DESC(c1.when)                                            /* reverse sort chronologically */
```


Constraints Are Directional
---------------------------

A topological constraint has a direction - as edges in graphs do - so `(a) <-[e]- (b)`
specifies a case where *b has an edge pointing at a*, whereas `(a) -[e]-> (b)` looks
for an edge in the opposite direction.  For example, here we find common friends of
'April' and 'Chris' who are older than both:

```sql
  SELECT friend.name, friend.dob
    FROM social_network_graph
   MATCH (p1:Person) -[:likes]-> (friend) <-[:likes]- (p2:Person) /* note the arrow directions */
   WHERE p1.name = 'April'
     AND p2.name = 'Chris'
     AND friend.dob > p1.dob
     AND friend.dob > p2.dob
ORDER BY friend.dob DESC
```

Vertex and Edge labels
----------------------

*Labels* are a way of attaching type information to edges and vertices in a graph, and can be used in
constraints in graphs where not all vertices represent the same thing:

```sql
SELECT p.name
  FROM imdb_graph
 MATCH (p:Person) -[e:likes]-> (m1:Movie)
     , (p) -[e:likes]-> (m2:Movie)
 WHERE m1.title = 'Star Wars'
   AND m2.title = 'Avatar'
```

Regular Path Expressions
--------------------

Regular path expressions allow for a pattern to be reused - for example, here we
find all of the common ancestors of Mario and Luigi:

```sql
  PATH has_parent AS () -[:has_father|has_mother]-> ()
  FROM family_graph
SELECT ancestor.name
 MATCH (p1:Person) -/:has_parent*/-> (ancestor:Person)
     , (p2:Person) -/:has_parent*/-> (ancestor)
 WHERE p1.name = 'Mario'
   AND p2.name = 'Luigi'
```

The path specification above also shows the use of *anonymous constraints* - no
need to define names for intermediate edges or vertices that will not be used in
additional constraints or query results.  Anonymous elements *can* have constraints,
such as `[:has_father|has_mother]` above - the edge does not get a variable name
(we will not reference it elsewhere), but is constrained.

Aggregation, Sorting, and Subqueries
-----------------------

Like SQL, PGQL has support for:

 * `GROUP BY` to create groups of solutions
 * `MIN`, `MAX`, `SUM` and `AVG` aggregations
 * `ORDER BY` to sort results
 * `EXISTS` subqueries, to test whether a pattern exists or doesn't exists

 and many other familiar SQL constructs.

Resources
---------

 - [PGQL 1.1 Specification](spec/1.1/) (latest)
 - [PGQL 1.0 Specification](spec/1.0/)
 - [PGQL 0.9 Specification](https://docs.oracle.com/cd/E56133_01/1.2.1/PGQL_Specification.pdf)
 - An [open-sourced parser](https://github.com/oracle/pgql-lang) for PGQL queries on GitHub
 - [White Paper](http://dl.acm.org/citation.cfm?id=2960421) ([pdf](http://event.cwi.nl/grades/2016/07-VanRest.pdf)) that also outlines some future directions of PGQL
   (shortest path finding, graph construction, etc.)
 - [Oracle Labs' Parallel Graph Analytics (PGX)](http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/overview/index.html), an in-memory graph analytics framework with a high-performance PGQL query engine
 - [Oracle Big Data Spatial and Graph](http://www.oracle.com/technetwork/database/database-technologies/bigdata-spatialandgraph/overview/index.html), which supports PGQL by embedding PGX
   (see [the in-memory analyst documentation](http://docs.oracle.com/bigdata/bda45/BDSPA/using-inmem-analytics.htm#BDSPA264)).
