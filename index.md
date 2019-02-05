---
title: PGQL
permalink: /index.html
toc: false
keywords: pgql property graph query language database analytics oracle sql standard gql cypher opencypher sparql gsql pgx big data spatial
---

<br/><br/>
{% include note.html content="Find our statement on __GQL__, a __standard__ for property graph querying, [__here__](gql-statement.html)." %}

Graphs & SQL
====================================

PGQL is a graph pattern-matching query language for the [property graph data model](spec/1.1/#property-graph-data-model), inspired by
[SQL](https://en.wikipedia.org/wiki/SQL),
[openCypher](http://www.opencypher.org/),
[G-CORE](http://g-core.org/),
[GSQL](https://doc.tigergraph.com/GSQL-101.html),
and [SPARQL](https://www.w3.org/TR/sparql11-query/).
PGQL combines graph pattern matching with familiar constructs from SQL, such as `SELECT`, `FROM` and `WHERE`.
PGQL also provides powerful constructs for matching regular path expressions (e.g. `PATH`).

An example is as follows:

{% include image.html file="example_graphs/financial_transactions.png" style="width:680px;" %}

```sql
  SELECT owner.name AS account_holder, SUM(t.amount) AS total_transacted_with_Nikita
    FROM financial_transactions
   MATCH (p:Person) -[:ownerOf]-> (:Account) -[t:transaction]- (:Account) <-[:ownerOf]- (owner:Person|Company)
   WHERE p.name = 'Nikita'
GROUP BY owner
```

```
+----------------+------------------------------+
| account_holder | total_transacted_with_Nikita |
+----------------+------------------------------|
| Camille        | 1000.00                      |
| Oracle         | 4501.00                      |
+----------------+------------------------------+
```

See [PGQL 1.1 Specification](spec/1.1/) for a detailed specification of the language.

Graph Pattern Matching
----------------------

PGQL uses [ASCII-art syntax](https://neo4j.com/developer/cypher-query-language/) for matching vertices, edges, and paths:

 * `(n:Person)` matches a __vertex__ (or node) `n` with label `Person`.
 * `-[e:friend_of]->` matches an __edge__ (or relationship) `e` with label `friend_of`.
 * `-/:friend_of+/->` matches a __path__ consisting of one or more (`+`) edges, each with label `friend_of`.

SQL Capabilities
-------------------

PGQL has the following SQL-like capabilities:

 * `DISTINCT` to remove duplicates.
 * `GROUP BY` to create groups of solutions and `HAVING` to filter out groups of solutions.
 * `COUNT`, `MIN`, `MAX`, `AVG` and `SUM` to aggregate over groups of solutions.
 * `ORDER BY` to sort results.
 * (`NOT`) `EXISTS` subqueries to test whether a graph pattern exists, or, does not exist.
 * `DATE`, `TIME`, `TIMESTAMP`, `TIME WITH TIME ZONE`, and `TIMESTAMP WITH TIME ZONE` temporal data types.

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

Datetime data types
-------------------
In addition to numerics, (character) strings, and booleans, PGQL has the following temporal data types:

 - `DATE` (java.time.LocalDate)
 - `TIME` (java.time.LocalTime)
 - `TIMESTAMP` (java.time.LocalDateTime)
 - `TIME WITH TIME ZONE` (java.time.OffsetTime)
 - `TIMESTAMP WITH TIME ZONE` (java.time.OffsetDateTime)

PGQL's Java ResultSet API
(see [ResultSet.java](https://github.com/oracle/pgql-lang/blob/master/graph-query-ir/src/main/java/oracle/pgql/lang/ResultSet.java)
and [ResultAccess.java](https://github.com/oracle/pgql-lang/blob/master/graph-query-ir/src/main/java/oracle/pgql/lang/ResultAccess.java))
 is based on the [__new Java 8 Date and Time Library__](http://www.oracle.com/technetwork/articles/java/jf14-date-time-2125367.html) (`java.time.*`), offering greatly improved safety and functionality for Java developers.

Resources
---------

 - __Specifications__
     - [PGQL 1.1 Specification](spec/1.1/)
     - [PGQL 1.0 Specification](spec/1.1/)
     - [PGQL 0.9 Specification](https://docs.oracle.com/cd/E56133_01/1.2.1/PGQL_Specification.pdf)
 - __Parser__
     - [Open-sourced parser and static query validator](https://github.com/oracle/pgql-lang) on GitHub
 - __Oracle__
     - [Oracle Spatial and Graph](https://www.oracle.com/database/spatial/index.html)
         - [Spatial and Graph Property Graph Developer's Guide for Oracle Database 18c](https://docs.oracle.com/en/database/oracle/oracle-database/18/spgdg/index.html)
         - [Property Graph Query Language (PGQL)](https://docs.oracle.com/en/database/oracle/oracle-database/18/spgdg/sql-based-property-graph-query-analytics.html#GUID-301FF092-1A07-43D2-91E5-0C5AFF3467CC)
     - [Oracle Big Data Spatial and Graph](https://www.oracle.com/technetwork/database/database-technologies/bigdata-spatialandgraph/overview/index.html)
         - [Using Pattern-Matching Queries with Graphs](https://docs.oracle.com/en/bigdata/big-data-spatial-graph/2.5/bdspa/using-in-memory-analyst.html#GUID-96D9C0AA-CE52-48E6-A09E-D97E872A79A1)
     - [PGX - Parallel Graph AnalytiX](https://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytix/overview/index.html)
         - [PGQL extensions and limitations in PGX](https://docs.oracle.com/cd/E56133_01/latest/reference/pgql-specification.html)
         - [Relevant Java API documentation](https://docs.oracle.com/cd/E56133_01/latest/reference/api/graph-pattern-matching.html)
         - [Use case: Analyzing Superhero Network with Patterns and Computations](https://docs.oracle.com/cd/E56133_01/latest/use-cases/superhero/index.html)
         - [Tutorial on invoking PGQL queries from the Java API or Groovy shell](https://docs.oracle.com/cd/E56133_01/latest/tutorials/graph-pattern-matching.html)
         - [Tutorial on working with datetime data types](https://docs.oracle.com/cd/E56133_01/latest/tutorials/datetime-data-types.html)
 - __Blog Posts__
     - [Using PGQL in Python](https://blogs.oracle.com/bigdataspatialgraph/using-pgql-in-python) (_2018-05-22_)
     - [How Many Ways to Run Property Graph Query Language (PGQL) in BDSG? (II)](https://blogs.oracle.com/bigdataspatialgraph/how-many-ways-to-run-property-graph-query-language-pgql-in-bdsg-ii) (_2017-03-23_)
     - [How Many Ways to Run Property Graph Query Language (PGQL) in BDSG? (I)](https://blogs.oracle.com/bigdataspatialgraph/how-many-ways-to-run-property-graph-query-language-pgql-in-bdsg-i) (_2017-03-14_)
     - [Property Graph Query Language (PGQL) support has been added to Oracle Database 12.2.0.1!](https://blogs.oracle.com/oraclespatial/property-graph-query-language-pgql-support-has-been-added-to-oracle-database-12201) (_2017-03-10_)
 - __Training Videos__
     - [PGQL: A Query Language for Graphs](https://asktom.oracle.com/pls/apex/f?p=100:551:::NO:551:P551_CLASS_ID:4197&cs=1F6BF819D61CFBE3F44500E3F8E156C5C) (_2018-10-02_)
 - __White Paper__
     - [White Paper](http://dl.acm.org/citation.cfm?id=2960421) ([pdf](http://event.cwi.nl/grades/2016/07-VanRest.pdf)) (_2016-06-24_)
 - __Standardization__
     - See our [statement on GQL](gql-statement.html) (_2019-01-04_)
