---
title:  "PGQL 1.1 Specification"
permalink: /spec/1.1/
summary: "PGQL is an SQL-like query language for the property graph data model and is based on the paradigm of graph pattern matching,
allowing you to specify patterns that are then matched against vertices and edges in a graph.
Like SQL, PGQL has support for grouping (GROUP BY), aggregation (e.g. MIN, MAX, AVG, SUM), sorting (ORDER BY) and many other familiar constructs.
In addition, PGQL has regular path expressions for applications such as reachability analysis."
sidebar: spec_1_1_sidebar
toc: true
---

# Changelog

The following are the changes since PGQL 1.0:

## New Querying Capabilities in PGQL 1.1

 - Input graph definition through `FROM` clause (see [Input Graph (FROM)](#input-graph-from)).
 - Matching of edges in either incoming or outgoing direction through undirected query edges (see [Undirected Query Edges](#undirected-query-edges)).
 - Quantifiers for specifying minimal and maximal number of applications in regular path expressions (see [Min and Max Quantifiers](#min-and-max-quantifiers)).
 - `DISTINCT` in aggregations (see [Aggregation](#aggregation)).
 - `SELECT DISTINCT` (see [Projections (SELECT)](#projection-select)).
 - Filtering of groups through `HAVING` clause (see [Filtering of Groups (HAVING)](#filtering-of-groups-having))
 - Temporal data types `DATE`, `TIME`, `TIMESTAMP`, `TIME WITH TIME ZONE`, and `TIMESTAMP WITH TIME ZONE` (see [Temporal Types](#temporal-types))
 - `IS NULL` and `IS NOT NULL` testing (see [IS NULL and IS NOT NULL](#is-null-and-is-not-null))
 - Explicit type conversion through `CAST` specification (see [Explicit Type Conversion (CAST)](#explicit-type-conversion-cast))
 - Built-in function `all_different(val1, val2, .., valn)` (see [Built-In Functions](#built-in-functions))
 - User-defined functions (see [User-Defined Functions](#user-defined-functions))
 - Existential (`EXISTS`) subqueries (see [Existential Subqueries (EXISTS)](#existential-subqueries-exists))
 - Bind variables (`?`) for creating parameterized queries (see [Bind Variables](#bind-variables))

## Breaking Syntax Changes since PGQL 1.0

 - The `WHERE` clause is changed into a `MATCH` clause and an optional `WHERE` clause such that the `MATCH` contains the pattern (vertices and edges) while the `WHERE` contains the filters if there are any.
   The inlined filters (`WITH` construct) should also be specified in the `WHERE` clause.
   For example, the following is a query in PGQL 1.0 and PGQL 1.1 syntax:

   ```sql
   /* PGQL 1.0 */
   SELECT n.name
    WHERE (n:Person WITH age > 25)
        , n.age <= 35
   ```

   ```sql
   /* PGQL 1.1 */
   SELECT n.name
     FROM g MATCH (n:Person)
    WHERE n.age > 25
      AND n.age <= 35
   ```

 - The syntax for common path expressions (see [Common Path Expressions](#common-path-expressions)) has changed as follows:

   ```bash
   # PGQL 1.0
   'PATH' IDENTIFIER ':=' PathPattern
   ```

   ```bash
   # PGQL 1.1
   'PATH' <IDENTIFIER> 'AS' <PathPattern> <WhereClause>?
   ```

   The changes are:

   - The symbol `:=` has changed into the keyword `AS`.
   - The inlined expressions (`WITH` construct) are moved to an optional `WHERE` clause.

   For example, the following is a query in both PGQL 1.0 (top) and PGQL 1.1 (bottom):

   ```sql
     /* PGQL 1.0 */
     PATH close_friend := () -[WITH weight >= 9]-> (:Person)
   SELECT m.name
    WHERE (n:Person) -/:close_friend*/-> (m)
        , n.name = 'Amber' 
   ```

   ```sql
     /* PGQL 1.1 */
     PATH close_friend AS () -[e]-> (:Person) WHERE e.weight >= 9
   SELECT m.name
     FROM g MATCH (n:Person) -/:close_friend*/-> (m)
    WHERE n.name = 'Amber'
   ```

 - Double-quoted string literals are no longer allowed; string literals should be single-quoted.

 - OO-style function call syntax has been replaced with SQL-style function call syntax:

   - `x.label()` => `label(x)`
   - `x.labels()` => `labels(x)`
   - `x.hasLabel(y)` => `has_label(x, y)`
   - `x.id()` => `id(x)`
   - `x.inDegree()` => `in_degree(x)`
   - `x.outDegree()` => `out_degree(x)`

 - The constructs `-->` (match any outgoing edge) and `<--` (match any incoming edge) are no longer allowed. Instead, use `->` and `<-`.

 - The infix Java RegExp operator `=~` has been removed. Instead, use the built-in function `java_regexp_like` (see [Built-in Functions](#built-in-functions)).

 - The `!` (logical not) operator has been removed. Instead, use `NOT` (logical not).

 - The `!=` (not equals) operator has been removed. Instead, use `<>` (not equals).

 - The `ASC(x)` (sort in ascending order) and `DESC(x)` (sort in descending order) functions have been removed. Instead, use the `x ASC` and `x DESC` constructs.

 - Direct sorting of vertices and edges (e.g. `ORDER BY v1, e1`) is no longer allowed. Instead, sort using _properties_ of vertices and edges (e.g. `ORDER BY v1.propX, e1.propY`).

# Introduction

PGQL (Property Graph Query Language) is a query language for the [property graph data model](#property-graph-data-model). This specification defines the syntax and semantics of PGQL.

Essentially, PGQL is a graph pattern-matching query language. A PGQL query describes a graph pattern consisting of vertices and edges. When the query is evaluated against a property graph, all the possible subgraphs that match the pattern are returned.

Consider the following example PGQL query:

```sql
SELECT m.name, o.name
  FROM sn_graph 
 MATCH (n:Person) -[e1:friend_of]-> (m:Person) <-[e2:belongs_to]- (o:Car)
 WHERE n.name = 'John'
```

In the `FROM` clause, we specify the graph that is queried:

 - The input graph is named `sn_graph`

In the `MATCH` clause, the above query defines the pattern to be found.

 - The pattern has three vertices, `n`, `m` and `o`, and two edges, `e1` and `e2`.
 - The edge `e1` goes from `n` to `m` and the edge `e2` goes from `o` to `m`.
 - Vertices `n` and `m` have a label `Person`, while vertex `o` has a label `Car`.
 - Edges `e1` and `e2` have labels `friend_of` and `belongs_to` respectively.

The `WHERE` clause contains filters:

 - Vertex `n` has a property `name` with the value `John`.

The `SELECT` clause specifies what should be projected out from the query:

 - For each of the matched subgraphs, we project the property `name` of vertex `m` and the property `name` of vertex `o`.

## Property Graph Data Model

A property graph has a name, which is a (character) string, and contains:

 - A set of vertices (or nodes).

   - Each vertex has zero or more labels.
   - Each vertex has zero or more properties, which are arbitrary key-value pairs.

 - A set of edges.

   - Each edge has a source and a destination vertex.
   - Each edge has zero or more labels.
   - Each edge has zero or more properties, which are arbitrary key-value pairs.

Labels as well as property names are strings. Property values are scalars such as numbers, strings or booleans.

Note: the property graph model in PGQL 1.1 does not support multi-valued properties like in [TinkerPop](http://tinkerpop.apache.org/docs/current/reference/#vertex-properties).

## Basic Query Structure

The syntax of PGQL resembles that of SQL (Standard Query Language) of relational database systems. A basic PGQL query consists of the following clauses:

```bash
Query ::=
  <CommonPathExpressions>?
  <SelectClause>
  <FromClause>? <MatchClause>
  <WhereClause>?
  <GroupByClause>? <HavingClause>?
  <OrderByClause>?
  <LimitOffsetClauses>?
```

The most important ones are as follows:

- The `SelectClause` defines the data entities that are returned in the result.
- The `MatchClause` defines the graph pattern that is matched against the data graph instance.
- The `WhereClause` defines the filters. 

The detailed syntax and semantic of each clause are explained in following sections.

# Graph Pattern Matching

## Input Graph (FROM)

The `FROM` clause specifies the name of the input graph to be queried:

```bash
FromClause ::= 'FROM' <GraphName>

GraphName  ::= <IDENTIFIER>
```

The `FROM` clause may be omitted if the system does not require the specification of an input graph for reasons such as:

 - The input graph is implicit because the system only handles single graphs.
 - The system has a notion of a "default graph" like in certain SPARQL systems.
 - The system provides an API such as `Graph.queryPgql(..)`, such that it is already clear from the context what the input graph is.

Subqueries may have their own `FROM` clause (see [Querying Multiple Graphs](#querying-multiple-graphs)). Subqueries may also omit the `FROM` clause (see [Subqueries without FROM Clause](#subqueries-without-from-clause)).

## Graph Pattern Specification (MATCH)

In a PGQL query, the `MATCH` clause defines the graph pattern to be matched.

Syntactically, a `MATCH` clause is composed of the keyword `MATCH` followed by a comma-separated sequence of path patterns:

```bash
MatchClause           ::= 'MATCH' <GraphPattern>

GraphPattern          ::= <PathPattern> ( ',' <PathPattern> )*

PathPattern           ::= <Vertex> ( <Relation> <Vertex> )*

Vertex                ::= '(' <VariableSpecification> ')'

Relation              ::= <Edge>
                        | <Path>

Edge                  ::= <OutgoingEdge>
                        | <IncomingEdge>
                        | <UndirectedEdge>

OutgoingEdge          ::= '->'
                        | '-[' <VariableSpecification> ']->'

IncomingEdge          ::= '<-'
                        | '<-[' <VariableSpecification> ']-'

VariableSpecification ::= <VariableName>? <LabelPredicate>?

VariableName          ::= <IDENTIFIER>
```

A path pattern that describes a partial topology of the subgraph pattern. In other words, a topology constraint describes some connectivity relationships between vertices and edges in the pattern, whereas the whole topology of the pattern is described with one or multiple topology constraints.

A topology constraint is composed of one or more vertices and relations, where a relation is either an edge or a path. In a query, each vertex or edge is (optionally) associated with a variable, which is a symbolic name to reference the vertex or edge in other clauses. For example, consider the following topology constraint:

```sql
(n) -[e]-> (m)
```

The above example defines two vertices (with variable names `n` and `m`), and an edge (with variable name `e`) between them. Also the edge is directed such that the edge `e` is an outgoing edge from vertex `n`.

More specifically, a vertex term is written as a variable name inside a pair of parenthesis `()`. An edge term is written as a variable name inside a square bracket `[]` with two dashes and an inequality symbol attached to it – which makes it look like an arrow drawn in ASCII art. An edge term is always connected with two vertex terms as for the source and destination vertex of the edge; the source vertex is located at the tail of the ASCII arrow and the destination at the head of the ASCII arrow.

There can be multiple path patterns in the `MATCH` clause of a PGQL query. Semantically, all constraints are conjunctive – that is, each matched result should satisfy every constraint in the `MATCH` clause.

### Repeated Variables

There can be multiple topology constraints in the `WHERE` clause of a PGQL query. In such a case, vertex terms that have the same variable name correspond to the same vertex entity. For example, consider the following two lines of topology constraints:

```sql
(n) -[e1]-> (m1),
(n) -[e2]-> (m2)
```

Here, the vertex term `(n)` in the first constraint indeed refers to the same vertex as the vertex term `(n)` in the second constraint. It is an error, however, if two edge terms have the same variable name, or, if the same variable name is assigned to an edge term as well as to a vertex term in a single query.

### Alternatives for Specifying Graph Patterns

There are various ways in which a particular graph pattern can be specified.

First, a single path pattern can be written as a chain of edge terms such that two consecutive edge terms share the common vertex term in between. For example:

```sql
(n1) -[e1]-> (n2) -[e2]-> (n3) -[e3]-> (n4)
```

The above graph pattern is equivalent to the graph pattern specified by the following set of comma-separate path patterns:

```sql
(n1) -[e1]-> (n2),
(n2) -[e2]-> (n3),
(n3) -[e3]-> (n4)
```

Second, it is allowed to reverse the direction of an edge in the pattern, i.e. right-to-left instead of left-to-right. Therefore, the following is a valid graph pattern:

```sql
(n1) -[e1]-> (n2) <-[e2]- (n3)
```

Please mind the edge directions in the above query – vertex `n2` is a common outgoing neighbor of both vertex `n1` and vertex `n3`.

Third, it is allowed to ommitg variable names if the particular vertex or edge does not need to be referenced in any of the other clauses (e.g. `SELECT` or `ORDER BY`). When the variable name is omitted, the vertex or edge is an "anonymous" vertex or edge.

Syntactically, for vertices, this result in an empty pair of parenthesis. In case of edges, the whole square bracket is omitted in addition to the variable name.

The following table summarizes these short cuts.

Syntax form | Example
--- | ---
Basic form | `(n) -[e]-> (m)`
Omit variable name of the source vertex | `() -[e]-> (m)`
Omit variable name of the destination vertex | `(n) -[e]-> ()`
Omit variable names in both vertices | `() -[e]-> ()`
Omit variable name in edge | `(n) -> (m)`

### Disconnected Graph Patterns

In the case the `MATCH` clause contains two or more disconnected graph patterns (i.e. groups of vertices and relations that are not connected to each other), the different groups are matched independently and the final result is produced by taking the Cartesian product of the result sets of the different groups. The following is an example:

```sql
SELECT *
  FROM g MATCH (n1) -> (m1), (n2) -> (m2)
```

Here, vertices `n2` and `m2` are not connected to vertices `n1` and `m1`, resulting in a Cartesian product.

## Label Predicates

In the property graph model, vertices and edge may have labels, which are arbitrary (character) strings. Typically, labels are used to encode types of entities. For example, a graph may contain a set of vertices with the label `Person`, a set of vertices with the label `Movie`, and, a set of edges with the label `likes`. A label predicate specifies that a vertex or edge only matches if it has ony of the specified labels. The syntax for specifying a label predicate is through a (`:`) followed by one or more labels that are separate by a vertical bar (`|`).

This is explained by the following grammar constructs:

```bash
LabelPredicate ::= ':' <Label> ( '|' <Label> )*

Label          ::= <IDENTIFIER>
```

Take the following example:

```sql
SELECT *
  FROM g MATCH (x:Person) -[e:likes|knows]-> (y:Person)
```

Here, we specify that vertices `x` and `y` have the label `Person` and that the edge `e` has the label `likes` or the label `knows`.

A label predicate can be specified even when a variable is omitted. For example:

```sql
SELECT *
  FROM g MATCH (:Person) -[:likes|knows]-> (:Person)
```

There are also built-in functions available for labels (see [Built-in Functions](#built-in-functions)):

 - `has_label(element, string)` returns `true` if the vertex or edge (first argument) has the specified label (second argument).
 - `labels(element)` returns the set of labels of a vertex or edge in the case the vertex/edge has multiple labels.
 - `label(element)` returns the label of a vertex or edge in the case the vertex/edge has only a single label.

## Filters (WHERE)

Filters are applied after pattern matching to remove certain solutions. A filter takes the form of a boolean value expression which typically involves certain property values of the vertices and edges in the graph pattern. The syntactic structure is as follows:


```bash
WhereClause ::= 'WHERE' <ValueExpression>
```

For example:

```sql
SELECT y.name
  FROM g MATCH (x) -> (y)
 WHERE x.name = 'John'
   AND y.age > 25
```

Here, the first filter describes that the vertex `x` has a property `name` and its value is `John`. Similarly, the second filter describes that the vertex `y` has a property `age` and its value is larger than `25`. Here, in the filter, the dot (`.`) operator is used for property access. For the detailed syntax and semantic of expressions, see [Value Expressions](#value-expressions).

Note that the ordering of constraints does not have an affect on the result, such that query from the previous example is equivalent to:

```sql
SELECT y.name
 FROM g MATCH (x) -> (y)
WHERE y.age > 25
  AND x.name = 'John'
```

## Graph Pattern Matching Semantic

There are two popular graph pattern matching semantics: graph homomorphism and graph isomorphism. The built-in semantic of PGQL is based on graph homomorphism, but patterns can still be matched in an isomorphic manner by specifying non-equality constraints between vertices and/or edges, or, by using the built-in function `all_different(exp1, exp2, .., expN)` (see [Built-in Functions](#built-in-functions)).

### Subgraph Homomorphism

Under graph homomorphism, multiple vertices (or edges) in the query pattern may match with the same vertex (or edge) in the data graph as long as all topology and value constraints of the different query vertices (or edges) are satisfied by the data vertex (or edge).

Consider the following example graph and query:

```
Vertex 0
Vertex 1
Edge 0: 0 -> 0
Edge 1: 0 -> 1
```

```sql
SELECT x, y
  FROM g MATCH (x) -> (y)
```

Under graph homomorphism the output of this query is as follows:

x | y
--- | ---
0 | 0
0 | 1

Note that in case of the first result, both query vertex `x` and query vertex `y` are bound to the same data vertex `0`.

### Subgraph Isomorphism

Under graph isomorphism, two distinct query vertices must not match with the same data vertex.

Consider the example from above. Under graph isomorphism, only the second solution is a valid one since the first solution binds both query vertices `x` and `y` to the same data vertex.

In PGQL, to specify that a pattern should be matched in an isomorphic way, one can introduce non-equality constraints:

```sql
SELECT x, y
  FROM g MATCH (x) -> (y)
 WHERE x <> y
```

The output of this query is as follows:

x | y
--- | ---
0 | 1

Alternatively, one can use the built-in function `all_different(exp1, exp2, .., expN)` (see [Built-in Functions](#built-in-functions)), which takes an arbitrary number of vertices or edges as input, and automatically applies non-equality constraints between all of them:

```sql
SELECT x, y
  FROM g MATCH (x) -> (y)
 WHERE all_different(x, y)
```

## Undirected Query Edges

Undirected query edges match with both incoming and outgoing data edges.

The syntactic structure is as follows:

```bash
UndirectedEdge ::= '-'
                 | '-[' <VariableSpecification> ']-'
```

An example PGQL query with undirected edges is as follows:

```sql
SELECT *
  FROM g MATCH (n) -[e1]- (m) -[e2]- (o)
```

Note that in case there are both incoming and outgoing data edges between two data vertices, there will be separate result bindings for each of the edges.

Undirected edges may also be used inside [common path expressions](#common-path-expressions):

```sql
  PATH two_hops AS () -[e1]- () -[e2]- ()
SELECT *
  FROM g MATCH (n) -/:two_hops*/-> (m)
```

The above query will return all pairs of vertices `n` and `m` that are reachable via a multiple of two edges, each edge being either an incoming or an outgoing edge.

# Table Operations

## Projection (SELECT)

In a PGQL query, the SELECT clause defines the data entities to be returned in the result. In other words, the select clause defines the columns of the result table.

The following explains the syntactic structure of SELECT clause.

```bash
SelectClause ::= 'SELECT' 'DISTINCT'? <ExpAsVar> ( ',' <ExpAsVar> )*
               | 'SELECT' '*'

ExpAsVar     ::= <ValueExpression> ( 'AS' <VariableName> )?
```

A `SELECT` clause consists of the keyword `SELECT` followed by either an optional `DISTINCT` modifier and comma-separated sequence of `<ExpAsVar>` ("expression as variable") elements, or, a special character star `*`. An `<ExpAsVar>` consists of:

- A `<ValueExpression>`.
- An optional `<VariableName>`, specified by appending the keyword `AS` and the name of the variable.

Consider the following example:

```sql
SELECT n, m, n.age AS age
  FROM g MATCH (n:Person) -[e:friend_of]-> (m:Person)
```

Per each matched subgraph, the query returns two vertices `n` and `m` and the value for property age of vertex `n`.  Note that edge `e` is omitted from the result even though it is used for describing the pattern.

The `DISTINCT` modifier allows for filtering out duplicate results. The operation applies to an entire result row, such that rows are only considered duplicates of each other if they contain the same set of values.

### Assigning Variable Name to Select Expression

It is possible to assign a variable name to any of the selection expression, by appending the keyword `AS` and a variable name. The variable name is used as the column name of the result set. In addition, the variable name can be later used in the `ORDER BY` clause. See the related section later in this document.

```sql
  SELECT n.age * 2 - 1 AS pivot, n.name, n
    FROM g MATCH (n:Person) -> (m:Car)
ORDER BY pivot
```

### SELECT *

`SELECT *` is a special `SELECT` clause. The semantic of `SELECT *` is to select all the variables in the graph pattern.

Consider the following query:

```sql
SELECT *
  FROM g MATCH (n:Person) -> (m) -> (w)
     , (n) -> (w) -> (m)
```

This query is semantically equivalent to:

```sql
SELECT n, m, w
  FROM g MATCH (n:Person) -> (m) -> ()
     , (n) -> (w) -> (m)
```

`SELECT *` is not allowed when the graph pattern has zero variables. This is the case when all the vertices and edges in the pattern are anonymous (e.g. `MATCH () -> (:Person)`).
Futhermore, `SELECT *` in combination with `GROUP BY` is not allowed.

## Sorting (ORDER BY)

When there are multiple matched subgraph instances to a given query, in general, the ordering between those instances are not defined; the query execution engine can present the result in any order. Still, the user can specify the ordering between the answers in the result using `ORDER BY` clause.

The following explains the syntactic structure of `ORDER BY` clause.

```bash
OrderByClause ::= 'ORDER' 'BY' <OrderTerm> ( ',' <OrderTerm> )*

OrderTerm     ::= <ValueExpression> ( 'ASC' | 'DESC' )?
```

The `ORDER BY` clause starts with the keywords `ORDER BY` and is followed by comma separated list of order terms. An order term consists of the following parts:

 - An expression.
 - An optional ASC or DESC decoration to specify that ordering should be ascending or descending.
     - If no keyword is given, the default is ascending order.

The following is an example in which the results are ordered by property access `n.age` in ascending order:

```sql
  SELECT n.name
    FROM g MATCH (n:Person)
ORDER BY n.age ASC
```

### Multiple Terms in ORDER BY

It is possible that `ORDER BY` clause consists of multiple terms. In such a case, these terms are evaluated from left to right. That is, (n+1)th ordering term is used only for the tie-break rule for n-th ordering term. Note that each term can have different ascending or descending decorator.

```sql
  SELECT f.name
    FROM g MATCH (f:Person)
ORDER BY f.age ASC, f.salary DESC
```

### Data Types for ORDER BY

A partial ordering is defined for the different data types as follows:

- Numeric data values are ordered from small to large.
- Strings are ordered lexicographically.
- Boolean values are ordered such that `false` comes before `true`
- Temporal data types (dates, time, timestamps) are ordered such that earlier points in time come before later points in time.

Vertices and edges cannot be ordered.

## Pagination (LIMIT and OFFSET)

The `LIMIT` puts an upper bound on the number of solutions returned, whereas the `OFFSET` specifies the start of the first solution that should be returned.

The following explains the syntactic structure for the LIMIT and OFFSET clauses:

```bash
LimitOffsetClauses ::= 'LIMIT' <LimitOffsetValue> ( 'OFFSET' <LimitOffsetValue> )?
                     | 'OFFSET' <LimitOffsetValue> ( 'LIMIT' <LimitOffsetValue> )?

LimitOffsetValue   ::= <UNSIGNED_INTEGER>
                     | <BindVariable>
```

The `LIMIT` clause starts with the keyword `LIMIT` and is followed by an integer that defines the limit. Similarly, the `OFFSET` clause starts with the keyword `OFFSET` and is followed by an integer that defines the offset. Furthermore:
The `LIMIT` and `OFFSET` clauses can be defined in either order.
The limit and offset may not be negatives.
The following semantics hold for the `LIMIT` and `OFFSET` clauses:
The `OFFSET` clause is always applied first, even if the `LIMIT` clause is placed before the `OFFSET` clause inside the query.
An `OFFSET` of zero has no effect and gives the same result as if the `OFFSET` clause was omitted.
If the number of actual solutions after `OFFSET` is applied is greater than the limit, then at most the limit number of solutions will be returned.

In the following query, the first 5 intermediate solutions are pruned from the result (i.e. `OFFSET 5`). The next 10 intermediate solutions are returned and become final solutions of the query (i.e. `LIMIT 10`).

```sql
SELECT n
  FROM g MATCH (n)
 LIMIT 10
OFFSET 5
```

# Regular Path Expressions

Path queries test for the existence of arbitrary-length paths between pairs of vertices, or, retrieve actual paths between pairs of vertices.
PGQL 1.1 supports testing for path existence ("reachability testing") only, while retrieval of actual paths between reachable pairs of vertices is planned for a future version.

The syntactic structure of a query path is similar to a query edge, but it uses forward slashes (`-/` and `/->`) instead of square brackets (`-[` and `]->`). The syntax rules are as follows:

```bash
Path                 ::= <OutgoingPath>
                       | <IncomingPath>

OutgoingPath         ::= '-/' <PathSpecification> '/->'

IncomingPath         ::= '<-/' <PathSpecification> '/-'

PathSpecification    ::= <LabelPredicate>
                       | <PathPredicate>

PathPredicate        ::= ':' <Label> <RepetitionQuantifier>

RepetitionQuantifier ::= <ZeroOrMore>
                       | <OneOrMore>
                       | <Optional>
                       | <ExactlyN>
                       | <NOrMore>
                       | <BetweenNAndM>
                       | <BetweenZeroAndM>

ZeroOrMore           ::= '*'

OneOrMore            ::= '+'

Optional             ::= '?'

ExactlyN             ::= '{' <UNSIGNED_INTEGER> '}'

NOrMore              ::= '{' <UNSIGNED_INTEGER> ',' '}'

BetweenNAndM         ::= '{' <UNSIGNED_INTEGER> ',' <UNSIGNED_INTEGER> '}'

BetweenZeroAndM      ::= '{' ',' <UNSIGNED_INTEGER> '}'
```

An example is as follows:

```sql
SELECT c.name
  FROM g MATCH (c:Class) -/:subclass_of*/-> (arrayList:Class)
 WHERE arrayList.name = 'ArrayList'
```

Here, we find all classes that are a subclass of `'ArrayList'`. The regular path pattern `subclass_of*` matches a path consisting of zero or more edges with the label `subclass_of`. Because the pattern may match a path with zero edges, the two query vertices can be bound to the same data vertex if the data vertex satisfies the constraints specified in both source and destination vertices (i.e. the vertex has a label `Class` and a property `name` with a value `ArrayList`).

## Min and Max Quantifiers

Quantifiers in regular path expressions allow for specifying lower and upper limits on the number of times a pattern should match.

| quantifier | meaning                              | matches                                                                                                                             | example path        |
|------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| *          | zero (0) or more                     | A path that connects the source and destination of the path by zero or more matches of a given pattern.                             | `-/:lbl*/->`        |
| +          | one (1) or more                      | A path that connects the source and destination of the path by one or more matches of a given pattern.                              | `-/:lbl+/->`        |
| ?          | zero or one (1), i.e. "optional"     | A path that connects the source and destination of the path by zero or one matches of a given pattern.                              | `-/:lbl?/->`        |
| { n }      | exactly _n_                          | A path that connects the source and destination of the path by exactly _n_ matches of a given pattern.                              | `-/:lbl{2}/->`      |
| { n, }     | _n_ or more                          | A path that connects the source and destination of the path by at least _n_ matches of a given pattern.                             | `-/:lbl{2,}/->`     |
| { n, m }   | between _n_ and _m_ (inclusive)      | A path that connects the source and destination of the path by at least _n_ and at most _m_ (inclusive) matches of a given pattern. | `-/:lbl{2,3}/->                      ` |
| { , m }    | between zero (0) and _m_ (inclusive) | A path that connects the source and destination of the path by at least 0 and at most _m_ (inclusive) matches of a given pattern.   | `-/:lbl{,3}/->`     |

Paths considered include those that repeat the same vertices and/or edges multiple times. This means that even cycles are considered. However, because the semantic is to test for the existence of paths between pairs of vertices, there is only at most one result per pair of vertices. Thus, even though an unbounded number of paths may exist between a pair of vertices (because of cycles), the result is always bounded.

Take the following graph as example:

{% include image.html file="example_graphs/pgql_min_max_hop.png" %}

### Zero or more

The following query finds all vertices `y` that can be reached from `Amy` by following zero or more `likes` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:likes*/-> (y)
 WHERE x.name = 'Amy'
```

```
+--------+
| y.name |
+--------+
| Amy    |
| John   |
| Albert |
| Judith |
+--------+
```

Note that here, `Amy` is returned since `Amy` connects to `Amy` by following zero `likes` edges. In other words, there exists an empty path for the vertex pair.
For `Judith`, there exist two paths (`100 -> 200 -> 300 -> 400` and `100 -> 400`). However, `Judith` is still only returned once.

### One or more

The following query finds all people that can be reached from `Amy` by following one or more `likes` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:likes+/-> (y)
 WHERE x.name = 'Amy'
```

```
+--------+
| y.name |
+--------+
| John   |
| Albert |
| Judith |
+--------+
```

This time, `Amy` is not returned since there does not exist a path that connects `Amy` to `Amy` that has a length greater than zero.

Another example is a query that finds all people that can be reached from `Judith` by following one or more `knows` edges:

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:knows+/-> (y)
 WHERE x.name = 'Judith'
```

```
+--------+
| y.name |
+--------+
| Jonas  |
| Judith |
+--------+
```

Here, in addition to `Jonas`, `Judith` is returned since there exist paths from `Judith` back to `Judith` that has a length greater than zero. Examples of such paths are `400 -> 500 -> 400` and `400 -> 500 -> 400 -> 500 -> 400`.

### Optional

The following query finds all people that can be reached from `Judith` by following zero or one `knows` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:knows?/-> (y)
 WHERE x.name = 'Judith'
```

```
+--------+
| y.name |
+--------+
| Judith |
| Jonas  |
+--------+
```

Here, `Judith` is returned since there exists the empty path that starts in `400` and ends in `400`. `Jonas` is returned because of the following path that has length one: `400 -> 500`.

### Exactly n

The following query finds all people that can be reached from `Amy` by following exactly two `likes` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:likes{2}/-> (y)
 WHERE x.name = 'Amy'
```

```
+--------+
| y.name |
+--------+
| Albert |
+--------+
```

Here, `Albert` is returned since there exists the following path that has `likes` edges only: `100 -> 200 -> 300`.

### n or more

The following query finds all people that can be reached from `Amy` by following 2 or more `likes` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:likes{2,}/-> (y)
 WHERE x.name = 'Amy'
```

```
+--------+
| y.name |
+--------+
| Albert |
| Judith |
+--------+
```
Here, `Albert` is returned since there exists the following path of length two: `100 -> 200 -> 300`. `Judith` is returned since there exists a path of length three: `100 -> 200 -> 300 -> 400`.

### Between n and m

The following query finds all people that can be reached from `Amy` by following between 1 and 2 `likes` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:likes{1,2}/-> (y)
 WHERE x.name = 'Amy'
```

```
+--------+
| y.name |
+--------+
| John   |
| Albert |
| Judith |
+--------+
```

Here, `John` is returned since there exists a path of length one (i.e. `100 -> 200`);
`Albert` is returned since there exists a path of length two (i.e. `100 -> 200 -> 300`);
`Judith` is returned since there exists a path of length one (i.e. `100 -> 400`).

### Between zero and m

The following query finds all people that can be reached from `Judith` by following at most 2 `knows` edges.

```sql
SELECT y.name
  FROM g MATCH (x:Person) -/:knows{,2}/-> (y)
 WHERE x.name = 'Judith'
```

```
+--------+
| y.name |
+--------+
| Jonas  |
| Judith |
+--------+
```

Here, `Jonas` is returned since there exists a path of length one (i.e. `400 -> 500`).
For `Judith`, there exists an empty path of length zero (i.e. `400`) as well as a non-empty path of length two (i.e. `400 -> 500 -> 400`).
Yet, `Judith` is only returned once.

## Common Path Expressions

One or more "common path expression" may be declared at the beginning of the query. These can be seen as macros that allow for expressing complex regular expressions.

```bash
CommonPathExpressions ::= <CommonPathExpression>+

CommonPathExpression  ::= 'PATH' <IDENTIFIER> 'AS' <PathPattern> <WhereClause>?
```

A path pattern declaration starts with the keyword `PATH`, followed by an expression name, the assignment operator `AS`, and a path pattern consisting of at least one vertex. The syntactic structure of the path pattern is the same as a path pattern in the `MATCH` clause.

An example is as follows:

```sql
  PATH has_parent AS () -[:has_father|has_mother]-> (:Person)
SELECT ancestor.name
  FROM g MATCH (p1:Person) -/:has_parent+/-> (ancestor)
             , (p2:Person) -/:has_parent+/-> (ancestor)
 WHERE p1.name = 'Mario'
   AND p2.name = 'Luigi'
```

The above query finds common ancestors of `Mario` and `Luigi`.

Another example is as follows:

```sql
  PATH connects_to AS (:Generator) -[:has_connector]-> (c:Connector) <-[:has_connector]- (:Generator)
                WHERE c.status = 'OPERATIONAL'
SELECT generatorA.location, generatorB.location
  FROM g MATCH (generatorA) -/:connects_to+/-> (generatorB)
```

The above query outputs all generators that are connected to each other via one or more connectors that are all operational.

# Grouping and Aggregation

## Grouping

`GROUP BY` allows for grouping of solutions and is typically used in combination with aggregation to aggregate over groups of solutions instead of over the total set of solutions.

The following explains the syntactic structure of the `GROUP BY` clause:

```bash
GroupByClause ::= 'GROUP' 'BY' <ExpAsVar> ( ',' <ExpAsVar> )*
```

The `GROUP BY` clause starts with the keywords GROUP BY and is followed by a comma-separated list of group terms. Each group term consists of:

- An expression.
- An optional variable definition that is specified by appending the keyword `AS` and the name of the variable.

Consider the following query:

```sql
  SELECT n.first_name, COUNT(*), AVG(n.age)
    FROM g MATCH (n:Person)
GROUP BY n.first_name
```

Matches are grouped by their values for `n.first_name`. For each group, the query selects `n.first_name` (i.e. the group key), the number of solutions in the group (i.e. `COUNT(*)`), and the average value of the property age for vertex n (i.e. `AVG(n.age)`).

### Assigning Variable Name to Group Expression

It is possible to assign a variable name to any of the group expression, by appending the keyword `AS` and a variable name. The variable name can be used in the `SELECT` to select a group key, or in the `ORDER BY` to order by a group key. See the related section later in this document.

```sql
  SELECT nAge, COUNT(*)
    FROM g MATCH (n:Person)
GROUP BY n.age AS nAge
ORDER BY nAge
```

### Multiple Terms in GROUP BY

It is possible that the `GROUP BY` clause consists of multiple terms. In such a case, matches are grouped together only if they hold the same result for each of the group expressions.

Consider the following query:

```sql
  SELECT n.first_name, n.last_name, COUNT(*)
    FROM g MATCH (n:Person)
GROUP BY n.first_name, n.last_name
```

Matches will be grouped together only if they hold the same values for `n.first_name` and the same values for `n.last_name`.

### GROUP BY and NULL values

The group for which all the group keys are null is a valid group and takes part in further query processing.

To filter out such a group, use a `HAVING` clause (see [Filtering of Groups (HAVING)](#filtering-of-groups-having)), for example:

```sql
  SELECT n.prop1, n.prop2, COUNT(*)
    FROM g MATCH (n)
GROUP BY n.prop1, n.prop2
  HAVING n.prop1 IS NOT NULL
     AND n.prop2 IS NOT NULL
```

### Repetition of Group Expression in Select or Order Expression

Group expressions that are variable accesses, property accesses, or built-in function calls may be repeated in select or order expressions. This is a short-cut that allows you to neglect introducing new variables for simple expressions.

Consider the following query:

```sql
  SELECT n.age, COUNT(*)
    FROM g MATCH (n)
GROUP BY n.age
ORDER BY n.age
```

Here, the group expression `n.age` is repeated as select and order expressions.

This repetition of group expressions introduces an exception to the variable visibility rules described above, since variable n is not inside an aggregation in the select/order expression. However, semantically, the query is treated as if there were a variable for the group expression:

```sql
  SELECT nAge, COUNT(*)
    FROM g MATCH (n)
GROUP BY n.age AS nAge
ORDER BY nAge
```

## Aggregation

Aggregates `COUNT`, `MIN`, `MAX`, `AVG` and `SUM` can aggregate over groups of solutions.

The syntax is as follows:

```bash
Aggregation      ::= <CountAggregation>
                   | <MinAggregation>
                   | <MaxAggregation>
                   | <AvgAggregation>
                   | <SumAggregation>

CountAggregation ::= 'COUNT' '(' '*' ')'
                   | 'COUNT' '(' 'DISTINCT'? <ValueExpression> ')'

MinAggregation   ::= 'MIN' '(' 'DISTINCT'? <ValueExpression> ')'

MaxAggregation   ::= 'MAX' '(' 'DISTINCT'? <ValueExpression> ')'

AvgAggregation   ::= 'AVG' '(' 'DISTINCT'? <ValueExpression> ')'

SumAggregation   ::= 'SUM' '(' 'DISTINCT'? <ValueExpression> ')'
```

Syntactically, an aggregation takes the form of aggregate followed by an optional `DISTINCT` modifier and a `<ValueExpression>`.

The following table gives an overview of the different aggregates and their supported input types.

Aggregate Operator | Semantic | Required Input Type
--- | --- | ---
`COUNT` | counts the number of times the given expression has a bound (i.e. is not null). | any type, including vertex and edge
`MIN` | takes the minimum of the values for the given expression. | numeric, string, boolean, date, time (with time zone), or, timestamp (with time zone)
`MAX` | takes the maximum of the values for the given expression. | numeric, string, boolean, date, time (with time zone), or, timestamp (with time zone)
`SUM` | sums over the values for the given expression. | numeric
`AVG` | takes the average of the values for the given. | numeric

### Aggregation with GROUP BY

If a `GROUP BY` is specified, aggregations are applied to each individual group of solutions.

An example is as follows:

```sql
  SELECT AVG(m.salary)
    FROM g MATCH (m:Person)
GROUP BY m.age
```

Here, we group people by their age and compute the average salary for each such a group.

### Aggregation without GROUP BY

If _no_ `GROUP BY` is specified, aggregations are applied to the entire set of solutions.

An example is as follows:

```sql
SELECT AVG(m.salary)
  FROM g MATCH (m:Person)
```

Here, we aggregate over the entire set of vertices with label `Person`, to compute the average salary.

### COUNT(*)

`COUNT(*)` is a special construct that simply counts the number of solutions without evaluating an expression. An example is as follows:

```sql
SELECT COUNT(*)
  FROM g MATCH (m:Person)
```

### DISTINCT Aggregation

The `DISTINCT` modifier specifies that duplicate values should be removed before performing aggregation.

For example:

```sql
SELECT AVG(DISTINCT m.age)
  FROM g MATCH (m:Person)
```

Here, we aggregate only over distinct `m.age` values.

## Filtering of Groups (HAVING)

The `HAVING` clause is an optional clause that can be placed after a `GROUP BY` clause to filter out particular groups of solutions.
The syntactic structure is as follows:

```bash
HavingClause ::= 'HAVING' <ValueExpression>
```

An example is as follows:

```sql
  SELECT n.name
    FROM g MATCH (n) -[:has_friend]-> (m)
GROUP BY n
  HAVING COUNT(m) > 10
```

This query returns the names of people who have more than 10 friends.

# Value Expressions

Value expressions are used in various parts of the language, for example, to filter solutions (`WHERE` and `HAVING`), to project out computed values (`SELECT`), or, to group by or order by computed values (`GROUP BY` and `ORDER BY`).

The following are the relevant grammar rules:

```bash
ValueExpression          ::= <VariableReference>
                           | <PropertyAccess>
                           | <Literal>
                           | <BindVariable>
                           | <ArithmeticExpression>
                           | <RelationalExpression>
                           | <LogicalExpression>
                           | <BracketedValueExpression>
                           | <CastSpecification>
                           | <FunctionCall>
                           | <IsNullPredicate>
                           | <IsNotNullPredicate>
                           | <ExistsPredicate>
                           | <Aggregation>

VariableReference        ::= <VariableName>

PropertyAccess           ::= <VariableReference> '.' <PropertyName>

PropertyName             ::= <IDENTIFIER>

BracketedValueExpression ::= '(' <ValueExpression> ')'
```

A value expression is one of:

 - A variable reference, being either a reference to a `<Vertex>`, an `<Edge>`, or, an `<ExpAsVar>`.
 - A property access, which syntactically takes the form of a variable reference, followed by a dot (`.`) and the name of a property.
 - A literal (see [Literals](#literals)).
 - A bind variable (see [Bind Variables](#bind-variables)).
 - An arithmetic, relational, or, logical expression (see [Operators](#operators)).
 - A bracketed value expression, which syntactically takes the form of a value expression between rounded brackets. The brackets allow for controlling precedence.
 - A function call (see [Functions](#functions)).
 - The `IS NULL` and `IS NOT NULL` predicates (see [IS NULL and IS NOT NULL](#is-null-and-is-not-null)).
 - The `EXISTS` predicate (see [Existential Subqueries (EXISTS)](#existential-subqueries-exists)).
 - An aggregation (see [Aggregation](#aggregation)).

## Operators

The following table is an overview of the operators:

Operator kind | Operator
------------- | --------
Arithmetic    | `+`, `-`, `*`, `/`, `%`, `-` (unary minus)
Relational    | `=`, `<>`, `<`, `>`, `<=`, `>=`
Logical       | `AND`, `OR`, `NOT`

The corresponding grammar rules are:

```bash
ArithmeticExpression ::= <UnaryMinus>
                       | <Multiplication>
                       | <Division>
                       | <Modulo>
                       | <Addition>
                       | <Subtraction>

UnaryMinus           ::= '-' <ValueExpression>

Multiplication       ::= <ValueExpression> '*' <ValueExpression>

Division             ::= <ValueExpression> '/' <ValueExpression>

Modulo               ::= <ValueExpression> '%' <ValueExpression>

Addition             ::= <ValueExpression> '+' <ValueExpression>

Subtraction          ::= <ValueExpression> '-' <ValueExpression>

RelationalExpression ::= <Equals>
                       | <NotEquals>
                       | <Greater>
                       | <Less>
                       | <GreaterEqual>
                       | <LessEquals>

Equals               ::= <ValueExpression> '=' <ValueExpression>

NotEquals            ::= <ValueExpression> '<>' <ValueExpression>

Greater              ::= <ValueExpression> '>' <ValueExpression>

Less                 ::= <ValueExpression> '<' <ValueExpression>

GreaterEqual         ::= <ValueExpression> '>=' <ValueExpression>

LessEquals           ::= <ValueExpression> '<=' <ValueExpression>

LogicalExpression    ::= <Not>
                       | <And>
                       | <Or>

Not                  ::= 'NOT' <ValueExpression>

And                  ::= <ValueExpression> 'AND' <ValueExpression>

Or                   ::= <ValueExpression> 'OR' <ValueExpression>
```

The supported input types and corresponding return types are as follows:

Operator                                            | type of A (and B)                                                                                     | Return Type
--------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | -----------
A `+` B<br>A `-` B<br>A `*` B<br>A `/` B<br>A `%` B | numeric                                                                                               | numeric*
`-`A (unary minus)                                  | numeric                                                                                               | type of A
A `=` B<br>A `<>` B                                 | numeric, string, boolean,<br>date, time (with time zone), timestamp (with time zone),<br>vertex, edge | boolean
A `<` B<br>A `>` B<br>A `<=` B<br>A `>=` B          | numeric, string, boolean,<br>date, time (with time zone), timestamp (with time zone)                  | boolean
`NOT` A<br>A `AND` B<br>A `OR` B                    | boolean                                                                                               | boolean

*For precision and scale, see [Implicit Type Conversion](#implicit-type-conversion). 

### Comparison of Temporal Values with Time Zones

Binary operations are only allowed if both operands are of the same type, with the following two exceptions:

- _time_ values can be compared to _time with time zone_ values
- _timestamp_ values can be compared to _timestamp with time zone_ values

To compare such _time(stamp) with time zone_ values to other time(stamp) values (with or without time zone), values are first normalized to have the same time zone, before they are compared.
Comparison with other operand type combinations, such as dates and timestamp, is not possible. However, it is possible to cast between e.g. dates and timestamps (see [Explicit Type Conversion (CAST)](#explicit-type-conversion-cast)).

### Operator Precedence

Operator precedences are shown in the following list, from the highest precedence to the lowest. An operator on a higher level (e.g. level 1) is evaluated before an operator on a lower level (e.g. level 2).

Level | Operator Precedence
----- | ---
1     | `-` (unary minus)
2     | `*`, `/`, `%`
3     | `+`, `-`
4     | `=`, `<>`, `>`, `<`, `>=`, `<=`
5     | `NOT`
6     | `AND`
7     | `OR`

## Null Values

The property graph data model does not allow properties with `null` value. Instead, missing or undefined data can be modeled through the _absence_ of properties.
A `null` value is generated when trying to access a property of a vertex or edge wile the property appears to be missing.
Three-valued logic applies when `null` values appear in computation.

### Three-Valued Logic

An operator returns `null` if one of its operands yields `null`, with an exception for `AND` and `OR`. This is shown in the following table:

Operator                        | Result when A is null                         | Result when B is null                          | Result when A and B are null
------------------------------- | --------------------------------------------- | ---------------------------------------------- | ----------------------------
A `+` `-` `*` `/` `%` B         | `null`                                        | `null`                                         | `null`
`-` A                           | `null`                                        | N/A                                            | N/A
A `=` `<>` `>` `<` `>=` `<=` B  | `null`                                        | `null`                                         | `null`
A `AND` B                       | `false` if B yields `false`, `null` otherwise | `false` if A yields `false`, `null` otherwise  | `null`
A `OR` B                        | `true` if B yields `true`, `null` otherwise   | `true` if A yields `true`, `null` otherwise    | `null`
`NOT` A                         | `null`                                        | N/A                                            | N/A

Note that from the table it follows that `null = null` yields `null` and not `true`.

### IS NULL and IS NOT NULL

To test whether a value exists or not, one can use the `IS NULL` and `IS NOT NULL` constructs.

```bash
IsNullPredicate    ::= <ValueExpression> 'IS' 'NULL'

IsNotNullPredicate ::= <ValueExpression> 'IS' 'NOT' 'NULL'
```

An example is as follows:

```sql
SELECT n.name
  FROM g MATCH (n)
 WHERE n.name IS NOT NULL
```

Here, we find all the vertices in the graph that have the property `name` and then return the property.

## Literals

The following are the available literals in PGQL:

```bash
Literal                      ::= <StringLiteral>
                               | <NumericLiteral>
                               | <BooleanLiteral>
                               | <DateLiteral>
                               | <TimeLiteral>
                               | <TimestampLiteral>
                               | <TimeWithTimeZoneLiteral>
                               | <TimestampWithTimeZoneLiteral>

StringLiteral                ::= <SINGLE_QUOTED_STRING>

NumericLiteral               ::= <UNSIGNED_INTEGER>
                               | <UNSIGNED_DECIMAL>

BooleanLiteral               ::= 'true'
                               | 'false'

DateLiteral                  ::= 'DATE' "'" <yyyy-MM-dd> "'"

TimeLiteral                  ::= 'TIME' "'" <HH:mm:ss> "'"

TimestampLiteral             ::= 'TIMESTAMP' "'" <yyyy-MM-dd HH:mm:ss> "'"

TimeWithTimeZoneLiteral      ::= 'TIME' "'" <HH:mm:ss+HH:MM> "'"

TimestampWithTimeZoneLiteral ::= 'TIMESTAMP' "'" <yyyy-MM-dd HH:mm:ss+HH:MM> "'"
```

| Literal type             | Example literal                         |
|--------------------------|-----------------------------------------|
| string                   | `'Clara'`                               |
| integer                  | `12`                                    |
| decimal                  | `12.3`                                  |
| boolean                  | `true`                                  |
| date                     | `DATE '2017-09-21'`                     |
| time                     | `TIME '16:15:00'`                       |
| timestamp                | `TIMESTAMP '2017-09-21 16:15:00'`       |
| time with time zone      | `TIME '16:15:00+01:00'`                 |
| timestamp with time zone | `TIMESTAMP '2017-09-21 16:15:00-03:00'` |

Note that the numeric literals (integer and decimal) are unsigned. However, signed values can be generated by using the unary minus operator (`-`).

## Bind Variables

In place of a literal, one may specify a bind variable (`?`). This allows for specifying parameterized queries.

```bash
BindVariable ::= '?'
```

An example query with two bind variables is as follows:

```sql
SELECT n.age
  FROM g MATCH (n)
 WHERE n.name = ?
    OR n.age > ?
```

In the following query, bind variables are used in `LIMIT` and `OFFSET`:

```sql
  SELECT n.name, n.age
    FROM g MATCH (n)
ORDER BY n.age
   LIMIT ?
  OFFSET ?
```

The following example shows a bind variable in the position of a label:

```sql
  SELECT n.name
    FROM g MATCH (n)
   WHERE has_label(n, ?)
```

 
## Functions

PGQL has a set of built-in functions (see [Built-in Functions](#built-in-functions)), and, provides language extension through user-defined functions (see [User-Defined Functions](#user-defined-functions)).

The syntactic structure for function calls is as follows:

```bash
FunctionCall         ::= <PackageSpecification>? <FunctionName> '(' <ArgumentList> ')'

PackageSpecification ::= <PackageName> '.'

PackageName          ::= <IDENTIFIER>

FunctionName         ::= <IDENTIFIER>

ArgumentList         ::= ( <ValueExpression> ( ',' <ValueExpression> )* )?
```

A function call has an optional package name, a function name, and, zero or more arguments which are arbitrary value expressions.

Function and package names are case-insensitive such that e.g. `in_degree(..)` is the same function as `In_Degree(..)` or `IN_DEGREE(..)`.

### Built-In Functions

The following is an overview of the built-in functions:

Signature | Return value | Description
`id(element)` | `object` | returns an identifier for the vertex/edge, if one exists.
`has_label(element, string)` | boolean | returns true if the vertex or edge (first argument) has the given label (second argument).
`labels(element)` | `set<string>` | returns the labels of the vertex or edge in the case it has multiple labels.
`label(element)` | string | returns the label of the vertex or edge in the case it has a single label.
`all_different(val1, val2, .., valn)` | boolean | returns true if the values are all different, a function typically used for specifying isomorphic constraints (see [Subgraph Isomorphism](#subgraph-isomorphism)).
`in_degree(vertex)` | exact numeric | returns the number of incoming neighbors.
`out_degree(vertex)` | exact numeric | returns the number of outgoing neighbors.
`java_regexp_like(string, pattern)` | boolean | returns whether the string matches the [pattern](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html)

Consider the following query:

```sql
SELECT id(y)
  FROM g MATCH (x) -> (y)
 WHERE in_degree(x) > 10
```

Here, `in_degree(x)` returns the number of incoming neighbors of `x`, whereas `id(y)` returns the identifier of the vertex `y`.

### User-Defined Functions

PGQL does not specify how user-defined functions (UDFs) are registered to a database system and only considers function _invocation_:

UDFs are invoked similarly to built-in functions. For example, a user may have registered a function `math.tan` that returns the tangent of a given angle.
An example invocation of this function is then:

```sql
  SELECT math.tan(n.angle) AS tangent
    FROM g MATCH (n)
ORDER BY tangent
```

If a UDF is registered that has the same name as a built-in function, then, upon function invocation, the UDF is invoked and not the built-in function. UDFs can thus override built-ins.

## Type Conversion

Implicit type conversion is supported for numeric types (see [Implicit Type Conversion](#implicit-type-conversion)). Other type conversions require explicit type conversion (see [Explicit Type Conversion (CAST)](#explicit-type-conversion-cast)).

### Implicit Type Conversion

Performing arithmetic operations with different numeric types will lead to implicit type conversion (i.e. coercion).
Coercion is only defined for numeric types. Given a binary arithmetic operation (i.e. `+`, `-`, `*`, `/`, `%`), the rules are as follows:

 - If both operands are exact numerics (e.g. integer or long), then the result is also an exact numeric with a scale that is at least as large as the scales of each operand.
 - If one or both of the operands is approximate numeric (e.g. float, double), the result is an approximate numeric with a scale that is at least as large as the scales of each operand. The precision will also be at least as high as the precision of each operand.

### Explicit Type Conversion (CAST)

Explicit type conversion is supported through type "casting".

The syntax is as follows: 

```bash
CastSpecification ::= 'CAST' '(' <ValueExpression> 'AS' <DataType> ')'

DataType          ::= 'STRING'
                    | 'INTEGER' | 'INT' | 'LONG' | 'FLOAT' | 'DOUBLE'
                    | 'BOOLEAN'
                    | 'DATE' | 'TIME' | 'TIME WITH TIME ZONE' | 'TIMESTAMP' | 'TIMESTAMP WITH TIME ZONE'
```

Note that the syntax of a data type is one or more identifiers separated by a space, allowing the encoding of data types such as `STRING` and `TIME WITH TIME ZONE`.

For example:

```sql
SELECT CAST(n.age AS STRING), CAST('123' AS INTEGER), CAST('09:15:00+01:00' AS TIME WITH TIME ZONE)
 MATCH (n:Person)
```

Casting is allowed between the following data types:

| From \ To                | string | exact numeric | approximate numeric | boolean | time | time with time zone | date | timestamp | timestamp with time zone |
|--------------------------|--------|---------------|---------------------|---------|------|--------------------|------|-----------|-------------------------|
| string                   | Y      | Y             | Y                   | Y       | Y    | Y                  | Y    | Y         | Y                       |
| exact numeric            | Y      | M             | M                   | N       | N    | N                  | N    | N         | N                       |
| approximate numeric      | Y      | M             | M                   | N       | N    | N                  | N    | N         | N                       |
| boolean                  | Y      | N             | N                   | Y       | N    | N                  | N    | N         | N                       |
| date                     | Y      | N             | N                   | N       | N    | N                  | Y    | Y         | Y                       |
| time                     | Y      | N             | N                   | N       | Y    | Y                  | N    | Y         | Y                       |
| timestamp                | Y      | N             | N                   | N       | Y    | Y                  | Y    | Y         | Y                       |
| time with time zone      | Y      | N             | N                   | N       | Y    | Y                  | N    | Y         | Y                       |
| timestamp with time zone | Y      | N             | N                   | N       | Y    | Y                  | Y    | Y         | Y                       |

In the table above, `Y` indicates that casting is supported, `N` indicates that casting is not supported, and `M` indicates that casting is supported only if the numeric value is within the precision bounds of the specified target type.

## Temporal Types

PGQL has five temporal data types: `DATE`, `TIME`, `TIMESTAMP`, `TIME WITH TIME ZONE` and `TIMESTAMP WITH TIME ZONE`.
For each of the data types, there exists a corresponding literal (see [Literals](#literals)).

In PGQL 1.1, the supported operations on temporal values are limited to comparison (see [Operators](#operators) and [Comparison of Temporal Values with Time Zones](#comparison-of-temporal-values-with-time-zones)).

# Subqueries

Subqueries in PGQL 1.1 are limited to existential subqueries.

## Existential Subqueries (EXISTS)

`EXISTS` returns true/false depending on whether the subquery produces at least one result, given the bindings obtained in the current (outer) query. No additional binding of variables occurs.

The syntax is as follows:

```bash
ExistsPredicate ::= 'EXISTS' <Subquery>

Subquery        ::= '(' <Query> ')'
```
An example is to find friend of friends, and, for each friend of friend, return the number of common friends:

```sql
SELECT fof.name, COUNT(friend) AS num_common_friends
  FROM g MATCH (p:Person) -[:has_friend]-> (friend:Person) -[:has_friend]-> (fof:Person)
 WHERE NOT EXISTS (
                    SELECT *
                     MATCH (p) -[:has_friend]-> (fof)
                  )
```

Here, vertices `p` and `fof` are passed from the outer query to the inner query. The `EXISTS` returns true if there is at least one `has_friend` edge between vertices `p` and `fof`.

## Subqueries without FROM Clause

If the `FROM` clause is omitted from a subquery, then the graph to process the subquery against, is the same graph as used for the outer query.

## Querying Multiple Graphs

Through subqueries, PGQL allows for comparing data from different graphs.

For example, the following query finds people who are on Facebook but not on Twitter:

```sql
SELECT p1.name
  FROM facebook_graph
 MATCH (p1:Person)                           /* Match persons in the Facebook graph.. */
 WHERE NOT EXISTS (                          /* ..such that there does not exist..    */
                    SELECT p2
                      FROM twitter_graph
                     MATCH (p2:Person)       /* ..a person in the Twitter graph..     */
                     WHERE p1.name = p2.name /* ..with the same name.                 */
                  )
```

Above, we compare two string properties from different graphs. Besides properties, it is also possible to compare vertices and edges from different graphs. However, because PGQL 1.1 does not have concepts like graph views, base graphs, or sharing of vertices/edges between graphs, such comparisons will always yield `false`.

## Subqueries inside PATH Clause

Users can add a sub-query in the `WHERE` clause of the `PATH` definition. One might be interested in asserting for specific properties for a vertex in the `PATH`. The following example defines a path ending in a vertex which is not the oldest in the graph:

```sql
  PATH p AS (a) -> (b) WHERE EXISTS ( SELECT * FROM g MATCH (x) WHERE x.age > b.age )
SELECT ...
  FROM ...
```

Topology related constraints can be also imposed. The following example defines a path ending in a vertex which has at least one out-neighbor:

```sql
  PATH p AS (a) -> (b) WHERE EXISTS ( SELECT * FROM g MATCH (b) -> (c) )
SELECT ...
  FROM ...
```

# Other Syntactic Rules

## Lexical Constructs

The following are the lexical grammar constructs: 

```bash
IDENTIFIER           ::= [a-zA-Z][a-zA-Z0-9\_]*

SINGLE_QUOTED_STRING ::= "'" ( ~[\'\n\\] | <ESCAPED_CHARACTER> )* "'"

UNSIGNED_INTEGER     ::= [0-9]+

UNSIGNED_DECIMAL     ::= ( [0-9]* '.' [0-9]+ ) | ( [0-9]+ '.' )
```

These rules describe the following:

 - Identifiers (used for e.g. graph names, property names, etc.) take the form of an alphabetic character followed by zero or more alphanumeric or underscore (i.e. `_`) characters.
 - Single quoted strings (used for string literals) consist of:
     - A starting single quote.
     - Any number of characters that are either:
         - Not single quote characters, new line characters, or backslash characters.
         - Escaped characters.
     - An ending single quote.
 - Unsigned integers consist of one or more digits.
 - Unsigned decimals consist of zero or more digits followed by a dot (`.`) and one or more digits, or, one or more digits followed by only a dot (`.`).

## Escaped Characters in Strings

Escaping in string literals is necessary to support having white space, quotation marks and the backslash character as a part of the literal value. The following explains the syntax of an escaped character.

```bash
ESCAPED_CHARACTER ::= '\\' [tnr\"\'\\]
```

Note that an escaped character is either a tab (`\t`), a line feed (`\n`), a carriage return (`\r`), a single (`\'`) or double quote (`\"`), or a backslash (`\\`). Corresponding Unicode code points are shown in the table below.

Escape | Unicode code point
--- | ---
`\t` | U+0009 (tab)
`\n` | U+000A (line feed)
`\r` | U+000D (carriage return)
`\"` | U+0022 (quotation mark, double quote mark)
`\'` | U+0027 (apostrophe-quote, single quote mark)
`\\` | U+005C (backslash)

In string literals, it is optional to escape double quotes. For example, the following expression yields `true`:

```sql
'abc\"d\"efg' = 'abc"d"efg' /* this expression yields true */
```

## Keywords

The following is a list of keywords in PGQL.

```
PATH, SELECT, AS, MATCH, WHERE, GROUP, BY,
HAVING, ORDER, ASC, DESC, LIMIT, OFFSET,
AND, OR, NOT, true, false, IS, NULL,
DATE, TIME, TIMESTAMP, WITH, ZONE,
COUNT, MIN, MAX, AVG, SUM, EXISTS, CAST
```

Keywords are case-insensitive and variations such as `SELECT`, `Select` and `sELeCt` can be used interchangeably.

Keywords are reserved names such that an `<IDENTIFIER>` (e.g. variable name or property name) may not correspond to one of the keywords.

## Comments

Comments are delimited by `/*` and `*/`. The following is the syntactic structure:

```bash
COMMENT ::= '/*' ~[\*]* '*/'
```

An example query with both single-line and multi-line comments is as follows:

```sql
/* This is a
   multi-line
   comment. */
SELECT n.name, n.age
  FROM g MATCH (n:Person) /* this is a single-line comment */
```
