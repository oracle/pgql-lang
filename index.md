---
title: Getting started with PGQL
keywords: pgql graph query language
sidebar: mydoc_sidebar
permalink: index.html
toc: false
---

PGQL is a SQL-like query language for the [Property Graph data model](https://github.com/tinkerpop/blueprints/wiki/Property-Graph-Model).
The language is based on the paradigm of _graph pattern matching_, which allows you to specify patterns that are matched against vertices and edges in a data graph.
Like SQL, PGQL has support for grouping (`GROUP BY`), aggregation (e.g. `MIN`, `MAX`, `AVG`), sorting (`ORDER BY`) and many other familiar SQL constructs.
In addition, PGQL supports _regular path queries_ for applications such as reachability analysis.

Here are some resources for PGQL:

 - The [PGQL 1.0 Specification](pgql-1.0-specification.html)
 - A reusable [open-sourced parser](https://github.com/oracle/pgql-lang) for PGQL queries
 - The [paper](http://dl.acm.org/citation.cfm?id=2960421) ([pdf](http://event.cwi.nl/grades/2016/07-VanRest.pdf)), which also outlines some future directions of PGQL,
   (shortest path finding, graph construction, etc.)
 - The in-memory graph analytics framework [PGX](http://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytics/overview/index.html), which comes with a high-performance PGQL query engine 

## Example Queries

#### Find all ancestors of 'Mario' and 'Luigi' 

```
PATH has_parent := () -[:has_father|has_mother]-> ()
SELECT ancestor.name
WHERE
  (:Person WITH name = 'Mario') -/:has_parent*/-> (ancestor:Person),
  (:Person WITH name = 'Luigi') -/:has_parent*/-> (ancestor:Person)
```

#### Find common friends of 'April' and 'Chris' that are older than both 

```
SELECT friend.name, friend.age
WHERE
  (p1:Person WITH name = 'April') -[:likes]-> (friend),
  (p2:Person WITH name = 'Chris') -[:likes]-> (friend),
  friend.dob > p1.dob AND friend.dob > p2.dob
ORDER BY friend.age DESC
```
