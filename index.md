---
title: Home
permalink: /index.html
toc: false
keywords: pgql graph query language database analytics social
---

A Query Language for Property Graphs
====================================

PGQL is a SQL-like query language for [Property Graphs](https://github.com/tinkerpop/blueprints/wiki/Property-Graph-Model) - data structures that consist of *nodes* that are connected to other nodes by *edges*, each of which can have ad-hoc key-value pairs (properties) associated with them.

The language is based on the concept of *graph pattern matching*, which allows you to specify patterns that are matched against vertices and edges in a data graph.  This site is the official home of the [PGQL language specification](spec/1.0/).


Topology Constraints
--------------------

Pattern matching is done using *topology constraints*, which describe a pattern of connections between nodes
in the graph;  value constraints (similar to their SQL equivalents) let you further constrain matches by specifying
properties that those connections and nodes must have.

Say we have a graph of TCP/IP connections on a computer network, and you want to detect cases where someone logged into
one machine, from there into another, and from there into still another - you would query for that pattern like this:

```sql
SELECT host1.id(), host2.id(), host3.id()
WHERE                                                               /* choose what to return */
    (host1) -[c1 WITH toPort = 22 and opened = true]-> (host2)      /* topology must match this pattern */
      -[connection2 WITH toPort = 22 and opened = true]-> (host3),
    connection1.bytes > 300,                                        /* meaningful amount of data was exchanged */
    connection2.bytes > 300,
    connection1.start < connection2.start,                          /* second connection within time-frame of first */
    connection2.start + connection2.duration < connection1.start + connection1.duration
GROUP BY host1.id(), host2.id(), host3.id()                         /* aggregate multiple matching connections */
ORDER BY DESC(connection1.when)                                     /* reverse sort chronologically */
```


Constraints Are Directional
---------------------------

A topological constraint has a direction - as edges in graphs do - so `(a) <-[]- (b)`
specifies a case where *b has an edge pointing at a*, whereas `(a) -[]-> (b)` looks
for an edge in the opposite direction.  For example, here we find common friends of
'April' and 'Chris' who are older than both:

```sql
SELECT friend.name, friend.dob
WHERE                              /* note the arrow directions below */
  (p1:person WITH name = 'April') -[:likes]-> (friend) <-[:likes]- (p2:person WITH name = 'Chris'),
  friend.dob > p1.dob AND friend.dob > p2.dob
ORDER BY friend.dob DESC
```

Vertex and Edge labels
----------------------

*Labels* are a way of attaching type information to edges and nodes in a graph, and can be used in
constraints in graphs where not all nodes represent the same thing:

```sql
SELECT p WHERE (p:person) -[e:likes]-> (m:movie WITH title='Star Wars'),
  (p) -[e:likes]-> (m:movie WITH title='Avatar')
```

Regular Path Queries
--------------------

Regular path queries allow for a pattern to be reused - for example, here we
find all of the common ancestors of Mario and Luigi:

```sql
PATH has_parent := () -[:has_father|has_mother]-> ()
SELECT ancestor.name
WHERE
  (:Person WITH name = 'Mario') -/:has_parent*/-> (ancestor:Person),
  (:Person WITH name = 'Luigi') -/:has_parent*/-> (ancestor)
```

The path specification above also shows the use of *anonymous constraints* - no
need to define names for intermediate edges or nodes that will not be used in
additional constraints or query results.  Anonymous elements *can* have constraints,
such as `[:has_father|has_mother]` above - the edge does not get a variable name
(we will not reference it elsewhere), but is constrained.

Aggregation and Sorting
-----------------------

Like SQL, PGQL has support for:

 * `GROUP BY` to create groups of solutions
 * `MIN`, `MAX`, `SUM` and `AVG` aggregations
 * `ORDER BY` to sort results

 and many other familiar SQL constructs.


Resources
---------

 - [PGQL 1.0 Language Specification](spec/1.0/) - the official language specification
 - An [open-sourced parser](https://github.com/oracle/pgql-lang) for PGQL queries on GitHub
 - [White Paper](http://dl.acm.org/citation.cfm?id=2960421) ([pdf](http://event.cwi.nl/grades/2016/07-VanRest.pdf)) that also outlines some future directions of PGQL
   (shortest path finding, graph construction, etc.)
 - [Oracle Labs' Parallel Graph Analytics (PGX)](http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/overview/index.html), an in-memory graph analytics framework with a high-performance PGQL query engine
 - [Oracle Big Data Spatial and Graph](http://www.oracle.com/technetwork/database/database-technologies/bigdata-spatialandgraph/overview/index.html), which supports PGQL by embedding PGX
   (see [the in-memory analyst documentation](http://docs.oracle.com/bigdata/bda45/BDSPA/using-inmem-analytics.htm#BDSPA264)).
 - [(Obsolete) PGQL 0.9 Specification](https://docs.oracle.com/cd/E56133_01/1.2.1/PGQL_Specification.pdf)
