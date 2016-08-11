---
title:  "PGQL 1.0 Specification"
permalink: pgql-1.0-specification.html
summary: "PGQL is a SQL-like query language for the Property Graph data model.
The language is based on the paradigm of graph pattern matching, which allows you to specify patterns that are matched against vertices and edges in a data graph.
Like SQL, PGQL has support for grouping (GROUP BY), aggregation (e.g. MIN, MAX, AVG), sorting (ORDER BY) and many other familiar SQL constructs.
In addition, PGQL supports regular path queries for applications such as reachability analysis."
toc: false
---

# Introduction

PGQL (Property Graph Query Language) is a query language for the Property Graph (PG) data model. This specification defines the syntax and semantics of PGQL.

Essentially, PGQL is a graph pattern-matching query language. A PGQL query describes a graph pattern with vertices, edges, properties, and their relationships,  When the query is evaluated against a Property Graph instance, the query engine finds all subgraph instances of the graph that match to the specified query pattern. Then the query engine returns the selected data entities from each of the matched subgraph instance.

Consider the following example PGQL query:

```
SELECT m.name, o.name
WHERE (n WITH type = 'Person' AND name = 'John') -[e1 WITH type = 'friendOf']-> (m WITH type = 'Person') <-[e2 WITH type = 'belongs_to']- (o WITH type = 'Car')
```

In the `WHERE` clause, the above query defines the pattern to be found.

- The pattern is composed of three vertices (`n`, `m`, and `o`) and two edges (`e1` and `e2`).
- There is an edge (`e1`) from vertex `n` to vertex `m`.
- There is an edge (`e2`) from vertex `o` to vertex `m`.
- Vertices `n` and `m` have a property `type` with value `'Person'`, while vertex `o` has a property `type` with value `'Car'`.
- Vertex `n` has another property `name` with value `'John'`.
- Edges `e1` and `e2` both have a property `type` whose values are `'friendOf'` and `'belongs_to'` respectively.

In the `SELECT` clause, the above query defines the data entities to be returned.

- For each of the matched subgraph, the query returns the property name of vertex `m` and the property name of vertex `o`.

## Basic Query Structure

The syntax structure of PGQL resembles that of SQL (Standard Query Language) of relational database systems. A basic PGQL query consists of the following three clauses:

```
Query :=
  SelectClause
  WhereClause
  SolutionModifierClause?
```

- The `SelectClause` defines the data entities that are returned in the result.
- The `WhereClause` defines the graph pattern that is matched against the data graph instance.
- The `SolutionModifierClause` defines additional operations for building up the result of the query. The clause is optional.

The detailed syntax and semantic of each clause are explained in following sections.

# WHERE Clause

In a PGQL query, the `WHERE` clause defines the graph pattern to be matched.

Syntactically, a `WHERE` clause is composed of the keyword `WHERE` followed by a comma-separated sequence of constraints.

```
WhereClause         := 'WHERE' {Constraint ','}+
Constraint          := TopologyConstraint |
                       ValueConstraint
TopologyConstraint  := PathPattern
PathPattern         := QueryVertex (QueryConnection QueryVertex)*
QueryVertex         := VariableName |
                       '(' VariableName? LabelConstraint? PropertyConstraints? ')'
QueryConnection     := QueryEdge |
                       QueryPath // see Section 'Path Queries'
QueryEdge           := '->' | '<-' | '-->' | '<--' |
                       '-[' VariableName? LabelConstraint? PropertyConstraints? '->' |
                       '<-[' VariableName? LabelConstraint? PropertyConstraints? '-'
LabelConstraint     := ':' Label ('|' Label)*
InlinedConstraints  := 'WITH' InlinedConstraint*
```

Each constraint is one of the following types:

- A topology constraint describes a partial topology of the subgraph pattern, i.e. vertices and edges in the pattern.
- A value constraint describes a general constraint other than the topology; the constraint takes the form of a Boolean expression which typically involves property values of the vertices and edges.
- An in-lined constraint is a syntactic sugar where value constraints are written inside vertex terms or edge terms of a topology constraint.

There can be multiple constraints in the `WHERE` clause of a PGQL query. Semantically, all constraints are conjunctive – that is, each matched result should satisfy every constraint in the `WHERE` clause.

## Topology Constraint

A topology constraint is a path pattern that describes a partial topology of the subgraph pattern. In other words, a topology constraint describes some connectivity relationships between vertices and edges in the pattern, whereas the whole topology of the pattern is described with one or multiple topology constraints.

A topology constraint is composed of one or more vertices and connections, where a connection is either an edge or a path. In a query, each vertex or edge is (optionally) associated with a variable, which is a symbolic name to refer the vertex or edge in the pattern. For example, consider the following topology constraint:

```
(n)-[e]->(m)
```

The above example defines two vertices (with variable names `n` and `m`), and an edge (with variable name `e`) between them. Also the edge is directed such that the edge `e` is an outgoing edge from vertex `n`.

More specifically, a vertex term is written as a variable name inside a pair of parenthesis `()`. An edge term is written as a variable name inside a square bracket `[]` with two dashes and an inequality symbol attached to it – which makes it look like an arrow drawn in ASCII art. An edge term is always connected with two vertex terms as for the source and destination vertex of the edge; the source vertex is located at the tail of the ASCII arrow and the destination at the head of the ASCII arrow.

### Repeated Variables in Multiple Topology Constraints

There can be multiple topology constraints in the `WHERE` clause of a PGQL query. In such a case, vertex terms that have the same variable name correspond to the same vertex entity. For example, consider the following two lines of topology constraints:

```
(n)-[e1]->(m1),
(n)-[e2]->(m2)
```

Here, the vertex term `(n)` in the first constraint indeed refers to the same vertex as the vertex term `(n)` in the second constraint. It is an error, however, if two edge terms have the same variable name, or, if the same variable name is assigned to an edge term as well as to a vertex term in a single query.

### Syntactic Sugars for Topology Constraints

For user's convenience, PGQL provides several syntactic sugars (short-cuts) for topology constraints.

First, a single topology constraint can be written as a chain of edge terms such that two consecutive edge terms share the common vertex term in between. For instance, the following topology constraint is valid in PGQL:

```
(n1)-[e1]->(n2)-[e2]->(n3)-[e3]->(n4)
```

In fact, the above constraint is equivalent to the following set of comma-separated constraints:

```
(n1)-[e1]->(n2),
(n2)-[e2]->(n3),
(n3)-[e3]->(n4)
```

Second, PGQL syntax allows to reverse the direction of an edge in the query, i.e. right-to-left instead of left-to-right. Therefore, the following is a valid topology constraint in PGQL:

```
(n1)-[e1]->(n2)<-[e2]-(n3)
```

Please mind the edge directions in the above query – vertex `n2` is a common outgoing neighbor of both vertex `n1` and vertex `n3`.

Third, PGQL allows to omit not-interesting variable names in the query. A variable name is not interesting if that name would not appear in any other constraint, nor in other clauses (`SelectClause`, `SolutionModifierClause`). As for a vertex term, only the variable name is omitted, resulting in an empty parenthesis pair. In case of an edge term, the whole square bracket is omitted in addition to the variable name. In this case, the remaining ASCII arrow can have either one dash or two dashes.

The following table summarizes these short cuts.

Syntax form | Example
--- | ---
Basic form | `(n)-[e]->(m)`
Omit variable name of the source vertex | `()-[e]->(m)`
Omit variable name of the destination vertex | `(n)-[e]->()`
Omit variable names in both vertices | `()-[e]->()`
Omit variable name in edge | `(n)-->(m)`
Omit variable name in edge (alternative,  one dash) | `(n)->(m)`
Omitting variables in both vertex and edge | `k1->()->()->k2`

Finally, the parenthesis in the vertex term can be omitted, if the vertex term is attached to an edge term and there is no in-lined value constraints. Therefore the following syntax is a valid topology constraint in PGQL: `x->y->z<-w-[e1]->q`.

### Disconnected Topology Constraints

In the case the topology constraints form multiple groups of vertices and edges that a not connected to each other, the semantic is that the different groups are matched independently and that final result is produced by taking the Cartesian product of the result sets of the different groups. The following is an example of a query that will result in a Cartesian product.

```
SELECT *
WHERE
  n1 -> m1
  n2 -> m2 // vertices {n2, m2} are not connected to vertices {n1, m1}, resulting in a Cartesian product
```

## Label Matching

In the Property Graph model, vertices have a set of labels, while edges have a single label. PGQL provides a convenient syntax for matching labels by attaching the label to the corresponding vertex or edge using a colon (`:`) followed by the label. Take the following example:

```
SELECT *
WHERE (x:Person) -[e:likes]-> (y:Person)
```

Here, we specify that vertices `x` and `y` have the label `'Person'` and that the edge `e` has the label `'likes'`.

Labels can still be specified when variables are omitted. The following is an example:

```
SELECT *
WHERE (:Person) -[:likes]-> (:Person)
```

### Labels and Quotes

Note that even though labels are Strings, we have omitted the quotes in the example above. Omitting quotes is optional only if the label is an alphanumeric character followed by zero or more alphanumeric or underscore characters. Otherwise, the label needs to be quoted and Syntax for Strings needs to be followed. This is explained by the following grammar constructs:

```
Label          := String | UnquotedString
UnquotedString := [a-zA-Z][a-zA-Z0-9\_]*
```

Take the following example:

```
SELECT *
WHERE (x:Person) -[e:'has friend']-> (y:Person)
```

Here, because the label `'has friend'` contains a white space, the quotes cannot be omitted and syntax for quoted Strings need to be followed.

### Label Alternatives

One can specify label alternatives, such that the pattern matches if the vertex or edge has one of the specified labels. Syntax-wise, label alternatives are separated by a `|` character, as follows:

```
SELECT *
WHERE (x:Student|Professor) -[e:likes|knows]-> (y:Student|Professor)
```

Here, vertices `x` and `y` match if they have either or both of labels `'Student'` and `'Professor'`. Edge `e` matches if it has either label `'likes'` or label `'knows'`.


### Built-in Functions for Labels

There are also built-in functions available for labels:

- `hasLabel(String label)` which returns `true` if the vertex or edge has the specified label.
- `labels()` which returns the set of labels of a vertex.
- `label()` which returns the label of an edge.

## Value Constraint

The value constraint describes a general constraint other than the topology. A value constraint takes the form of a Boolean expression which typically involves certain property values of the vertices and edges that are defined in topology constraints in the same query. For instance, the following example consists of three constraints – one topology constraint followed by two value constraints.

```
x -> y,
x.name = 'John',
y.age > 25
```

In the above example, the first value constraint demands that the vertex `x` has a property `name` and its value to be `'John'`. Similarly, the second value constrain demands that the vertex `y` has a numeric property `age` and its value to be larger than `25`. Here, in the value constraint expressions, the dot (`.`) operator is used for property access. For the detailed syntax and semantic of expressions, please refer to the corresponding section in this specification.

Note that in PGQL the ordering of constraints does not has any effect on the result. Therefore, the previous example is equivalent to the following:

```
x.name = 'John',
x -> y,
y.age > 25
```

## In-lined Constraint

An in-lined constraint is a syntactic sugar where value constraints are written directly inside a topology constraint. More specifically, expressions that access the property values of a certain vertex (or edge) are put directly inside the parenthesis (or the square bracket) of the corresponding vertex (or edge) term. Consider the following set of constraints.

```
n-[e]->(),
n.name = 'John' OR n.name = 'James',
n.type = 'Person'
e.type = 'workAt',
e.workHours < 40
```

The above constraints can re-written with in-lined constraint as follows:

```
(n WITH name = 'John' OR name = 'James', type = 'Person') -[e WITH type = 'workAt', workHours < 40]-> ()
```

Note that the property-accessing expressions in the original value constraints are in-lined into the topology constraint. More specifically, the expressions are in-lined inside the parenthesis or square bracket after the `WITH` keyword. Moreover, the syntax for property access gets simplified in the in-lined expressions. See the discussion in the following section.

### Simplified Property Access in the In-lined Expressions

Syntax for property access is further simplified in the in-lined expressions. In normal value constraint, a property access takes the form of dot expression (i.e. variable_name.property_name). In an in-lined expression, on the other hand, the variable name can be omitted since it is clear from the context. Moreover, if the property name is properly alpha-numeric, even the leading dot can be omitted. The following table summarizes this short-cut rules.

Normal Value Constraint | In-lined Constraint | In-lined Constraint (alternative)
--- | --- | ---
`n.name = 'John'` | `(n WITH .name = 'John')` | `(n WITH name = 'John')`
`n.'middle name' = 'John'` | `(n WITH .'middle name' = 'John')` |

Note that in the above table, we cannot omit the leading dot nor the quotes for property access '.middle name' since the name contains a space and is thus not an alpha-numeric.

### Vertices/edges without Variable Name but with In-lined Constraints

If a not-interesting variable name is omitted for a vertex or edge term, it is still possible to specify in-lined constraints without having to introduce a variable name. This can be achieved by omitting the variable name and by directly using the `WITH` keyword followed by the constraints. The following table summarizes this short-cut rule.

In-lined Constraint | In-lined Constraint w/o variable name
--- | ---
`(n WITH name = 'John')` | `(WITH name = 'John')`

### Limitation on the In-lined Expressions

Expressions that contain property accesses from multiple variables (a.k.a. cross-constraints) cannot be in-lined. Consider the following constraint:

```
n->m
n.name = m.name
```

This constraint cannot be inlined. The following is syntatcially not valid:

```
(n WITH name = m.name) -> m   // this is a syntax error
```

### Identifier short-cut for in-lined expressions

In property graphs, vertices and edges can have unique identifiers (IDs). PGQL expression provides a special function `id()` for accessing the indentifier of a vertex or edge. However, there is another short-cut syntax for an in-lined expression, if the vertex (or edge) is constrained to have a specific ID value. Specifically, the variable name followed by `@` and a certain value means that the vertex (or edge) should have the ID of the specified value. The following is an example.

Original Syntax | Shortcut Syntax
--- | ---
`(n WITH id() = 123)` | `(n@123)`
`(n:Person WITH id() = 123)` | `(n:Person@123)`
`()-[e WITH id()=1234)->[]` | `() -[e@1234]-> ()`

## Graph Pattern Matching Semantic

There are two popular graph pattern matching semantics: homomorphic pattern matching and isomorphic pattern matching. The semantic of PGQL is that of homomorphic pattern matching, which gives more expressive power.

### Homomorphic Pattern Matching

Under graph homomorphism, multiple vertices (or edges) in the query pattern may match with the same vertex (or edge) in the data graph as long as all topology and value constraints of the different query vertices (or edges) are satisfied by the data vertex (or edge).

Consider the following example graph and query:

```
Vertex 0
Vertex 1
Edge 0: 0 -> 0
Edge 1: 0 -> 1
```

```
SELECT x, y
WHERE x -> y
```

Under graph homomorphism semantic the output of this query is as follows:

x | y
--- | ---
0 | 0
0 | 1

Note that in case of the first result, both query vertex `x` and query vertex `y` bind to the same data vertex `0`.

### Isomorphic Pattern Matching

Under graph isomorphism, two query vertices may not match with the same data vertex.

Consider the example from above. Under isomorphic semantic, only the second solution is a valid one since the first solution binds both query vertices `x` and `y` to the same data vertex.

In PGQL, to specify that a pattern should be matched in an isomorphic way, one can introduce non-equality constraints:

```
SELECT x, y
WHERE x -> y, x != y
```

The output of this query is as follows:

x | y
--- | ---
0 | 1

# SELECT Clause

In a PGQL query, the SELECT clause defines the data entities to be returned in the result. In other words, the select clause defines the columns of the result table.

The following explains the syntactic structure of SELECT clause.

```
SelectClause := 'SELECT' {SelectElem ','}* |
                'SELECT' '*'
SelectElem   := Expression ('AS' Variable)?
```

A `SELECT` clause consists of the keyword `SELECT` followed by a comma-separated sequence of select element, or a special character star `*`. A select element consists of:

- An expression.
- An optional variable definition that is specified by appending the keyword AS and the name of the variable.

## SELECT Expressions

A PGQL query can dictate the data entities to be returned in the `SELECT` clause, by putting a comma-separated list of expressions after the `SELECT` keyword. Per every matched subgraph (i.e. row), each `SELECT` expression (i.e. column) is computed and stored in the result set. For instance, consider the following example:

```
SELECT n, m, n.age
WHERE
   (n WITH type = 'Person') -[e WITH type='friendOf']-> (m WITH type = 'Person')
```

Per each matched subgraph, the query returns two vertices `n` and `m` and the value for property age of vertex `n`.  Note that edge `e` is omitted from the result even though it is used for describing the pattern.

### Assigning Variable Name to Select Expression

It is possible to assign a variable name to any of the selection expression, by appending the keyword `AS` and a variable name. The variable name is used as the column name of the result set. In addition, the variable name can be later used in the `ORDER BY` clause. See the related section later in this document.

```
SELECT n.age*2 - 1 AS pivot, n.name, n
WHERE
   (n WITH type = 'Person') -> (m WITH type = 'Car')
ORDER BY pivot
```

## SELECT *

`SELECT *` is a special `SELECT` clause. The semantic of `SELECT *` is to select all the variables or group keys in-scope. If the query has no `GROUP BY`, the selected variables are all the vertex and edge variables from the `WHERE` clause. If the query does have a `GROUP BY`, the selected elements are all the group keys.

Consider the following query:

```SELECT *
WHERE
  (n WITH type = 'Person') -> (m) -> (w)
  (n) -> (w) -> (m)
```

Since this query does not have a `GROUP BY`, all the variables in the `WHERE` are returned: `n`, `m` and `w`. However, the order of variables selected by `SELECT *` is not defined by the specification. Therefore the result of `SELECT *` in the above query can be any combination of (`n`, `m`, `w`).

Now consider the following query, which has a `GROUP BY`:

```
SELECT *
WHERE
  (n WITH type = 'Person') -> (m) -> (w)
  (n) -> (w) -> (m)
GROUP BY n.name, m
```

Because the query has a `GROUP BY`, all group keys are returned: `n.name` and `m`. The order of the variables selected is the order in which the group keys appear in the `GROUP BY`.

### SELECT * with no variables in the WHERE clause

It is semantically valid to have a `SELECT *` in combination with a `WHERE` clause that has not a single variable definition. In such a case, the result set will still contain as many results (i.e. rows) as there are matches of the subgraph defined by the `WHERE` clause. However, each result (i.e. row) will have zero elements (i.e. columns). The following is an example of such a query.

``
SELECT *
WHERE
  (WITH type = 'Person') -> () -> ()
```

## Aggregation

Instead of retrieving all the matched results, a PGQL query can choose to get only some aggregated information about the result. This is done by putting aggregations in SELECT clause, instead of normal expressions. Consider the following example query which returns the average value of property age over all the matched vertex m.

```
SELECT AVG(m.age) WHERE (m WITH type = 'Person')
```

Syntactically, an aggregation takes the form of Aggregate operator followed by expression inside a parenthesis. The following table is the list of Aggregate operators and their required input type.

Aggregate Operator | Semantic | Required Input Type
--- | --- | ---
`COUNT` | counts the number of times the given expression has a bound. | not null
`MIN` | takes the minimum of the values for the given expression. | numeric
`MAX` | takes the minimum of the values for the given expression. | numeric
`SUM` | sums over the values for the given expression. | numeric
`AVG` | takes the average of the values for the given | numeric

`COUNT(*)` is a special syntax to count the number of pattern matches, without specifying an expressions. Consider the following example:

```
SELECT COUNT(*)
WHERE (m WITH type='Person') -> (k WITH type = 'Car') <- (n WITH type = 'Person')
```

The above query simply returns the number of matches to the pattern.

### Aggregation and Required Input Type

In PGQL, aggregation is performed only for the matched results where the type of the target expression matches with the required input type. Consider an example graph instance which has the following four vertex entities.

```
{"id": 3048,  "name":"John",  "age":30}
{"id": 1197,  "name":"Peter", "age":20}
{"id": 20487, "name":"Paul",  "age":"thirty five"}
{"id": 2019,  "name":"James"}
```

Now suppose the following query is applied on this data set.

```
SELECT AVG(n.age), COUNT(*) WHERE (n)
```

Note that all the vertices are matched by the `WHERE` clause. However, the aggregation result from `SELECT` clause is `25` and `4`. For `AVG(n.age)` aggregation, only two vertices get aggregated (`"John"` and `"Peter"`) – the vertex for `"Paul"` is not applied because `'age'` is not numeric type, and the vertex for `"James"` does not have `'age'` property at all. For `COUNT(*)` aggregation, on the other hand, all the four matched vertices are applied to the aggregation.

### Aggregation and Solution Modifier

Aggregation is applied only afterthe  `GROUP BY` operator is applied, but before the `OFFSET` and `LIMIT` operators are applied.

- If there is no GROUP BY operator, the aggregation is performed over the whole match results.
- If there is a GROUP BY operator, the aggregation is applied over each group.

See the detailed syntax and semantics of `SolutionModifierClause` in the related section of this specification.

### Assigning Variable Name to Aggregation

Like normal selection expression, it is also possible to assign variable name to aggregations. Again this is done by appending the key word `AS` and a variable name next to the aggregation. The variable name is used as the column name of the result set. In addition, the variable name can be later used in the `ORDER BY` clause. See the related section later in this document.

```
SELECT AVG(n.age) AS pivot, COUNT(n)
WHERE
   (n WITH type = 'Person') -> (m WITH type = 'Car')
GROUP BY n.hometown
ORDER BY pivot
```

# Path Queries

Path queries test for the existence of arbitrary-length paths between pairs of vertices, or, retrieve actual paths between pairs of vertices. PGQL 1.0 supports testing for path existence ("reachability testing") only, while retrieval of actual paths between reachable pairs of vertices is planned for future PGQL versions.

## Regular Path Patterns

In addition to query vertices and query edge, a graph pattern in PGQL may be composed of query paths. Such paths define a regular path pattern between a pair of query vertices. During querying, bindings for query vertices are only obtained for those vertices in the graph that are reachable by at least one path that satisfies the regular path pattern.
The syntactic structure of a query path is similar to a query edge, but it uses forward slashes (-/.../->) instead of square brackets (-[...]->) to clearly distinguishes the two types of connection. Inside the forward slashes, there must be a colon (':') followed by a regular pattern.

```
QueryPath       := '-/' ':' RegularPattern '/->' |
                   '<-/' ':' RegularPattern '/-'
RegularPattern  := PathPatternName |
                   Label |
                   ZeroOrMorePath |
                   AlternativePath |
                   GroupPath
ZeroOrMorePath  := RegularPattern '*'
ZeroOrMorePath  := RegularPattern
AlternativePath := {RegularPattern '|'}
GroupPath       := '(' RegularPattern ')'
```

A regular pattern is one of the following:

- `PathPatternName`: matches a path using a path pattern that is declared at the beginning of the query
- `Label`: matches a path of length one such that the edge on the path has the specified label
- `ZeroOrMorePath`: matches a path by repeatedly matching the pattern zero or more times
- `AlternativePath`: matches an alternative pattern (all possibilities are tried)
- `GroupPath`: matches a path (brackets help control precedence)

An example is as follows:

```
SELECT c
WHERE
  (c:Class) -/:subclass_of*/-> (:Class WITH name = 'ArrayList')
```

Here, we find all classes that are a subclass of `'ArrayList'`. The regular pattern `subclass_of*` matches a path consisting of zero or more edges with the label `subclass_of`. Because the pattern may match a path with zero edges, it is allowed for the two query vertices to bind to the same data vertex if the data vertex satisfies the value constraints specified for path vertices (i.e. the vertex has a label `'Class'` and a property name with a value `'ArrayList'`.

## Path Pattern Composition

Path patterns may be declared outside of the `WHERE` clause at the beginning of the query. Such patterns can then be used to construct more complex regular path patterns via path pattern composition.

The syntactic structure is as follows:

```
PathPatternDecl := 'PATH' PathPatternName ':=' PathPattern
PathPatternName := [a-zA-Z][a-zA-Z0-9\_]*
```

A path pattern declaration starts with the keyword `PATH` and is followed by the name for the path pattern, the assignment operator `:=` and a path pattern. The syntactic structure of the path pattern is the same as a path pattern in the `WHERE` clause.

An example is as follows:

```
PATH has_parent := () -[:has_father|has_mother]-> ()
SELECT ancestor
WHERE
  (:Person WITH name = 'Mario') -/:has_parent*/-> (ancestor:Person),
  (:Person WITH name = 'Luigi') -/:has_parent*/-> (ancestor:Person)
```

The above query finds common ancestors of `'Mario'` and `'Luigi'`.

Another example is as follows:

```
PATH connects_to := (:Generator) -[:has_connector]-> (:Connector WITH status = 'OPERATIVE') <-[:has_connector]- (:Generator)
SELECT generatorA.location, generatorB.location
WHERE
  (generatorA) -/:connects_to*/-> (generatorB),
  generatorA != generatorB
```

The above query outputs all generators that are connected to each other via one or more connectors that are all operative.

# Solution Modifier Clause

The solution modifier clause defines additional operations for building up the result of the query.  A solution modifier clause consists of four (sub-)clauses– `GroupByClause`, `OrderByClause`, <LIMIT clause> and <OFFSET clause>. Note that all these clauses are optional; therefore the entire solution modifier clause is optional.

```
SolutionModifierClause := GroupByClause? OrderByClause? LimitOffsetClauses?
```

## ORDER BY

When there are multiple matched subgraph instances to a given query, in general, the ordering between those instances are not defined; the query execution engine can present the result in any order. Still, the user can specify the ordering between the answers in the result using `ORDER BY` clause.

The following explains the syntactic structure of `ORDER BY` clause.

```
OrderByClause := 'ORDER' 'BY' {OrderTerm ','}+
OrderTerm     := Expression ('ASC'|'DESC')? |
                 ('ASC'|'DESC')? '(' Expression ')'
```

The `ORDER BY` clause starts with the keywords `ORDER BY` and is followed by comma separated list of order terms. An order term consists of the following parts:

- An expression.
- An optional ASC or DESC decoration to specify that ordering should be ascending or descending.
    - If no keyword is given, the default is ascending order.

The following is an example in which the results are ordered by property access `n.age` in ascending order:

```
SELECT n.name
WHERE (n WITH type = 'Person')
ORDER BY n.age ASC
```

### Multiple Terms in ORDER BY

It is possible that `ORDER BY` clause consists of multiple terms. In such a case, these terms are evaluated from left to right. That is, (n+1)th ordering term is used only for the tie-break rule for n-th ordering term. Note that each term can have different ascending or descending decorator.

```
SELECT f.name
WHERE (f WITH type = 'Person')
ORDER BY ASC(f.age), f.salary DESC
```

### Data Types for ORDER BY

A partial ordering is defined for the different data types as follows:

- Numeric data values are ordered from small to large.
- Strings are ordered lexicographically.
- Vertices and edges are ordered by their identifier (small to larger if numeric, lexicographically if String)

In the case a property access holds multiple types of data values, the following ordering is applied between values of different types:

- Numeric < String < Boolean 'false' < Boolean 'true' < 'null'

Consider the following data values:

```
['Mary', 25, null, false, true, 'John', 3.5, 27.5]
```

Applying the above rules to the values, will result in the following ordering:

```
[3.5, 25, 27.5, 'John', 'Mary', true, false, null]
```

## LIMIT and OFFSET

The `LIMIT` puts an upper bound on the number of solutions returned, whereas the `OFFSET` specifies the start of the first solution that should be returned.

The following explains the syntactic structure for the LIMIT and OFFSET clauses:

```
LimitOffsetClauses := 'LIMIT' Integer ('OFFSET' Integer)? |
                      'OFFSET' Integer ('LIMIT' Integer)?
```

The `LIMIT` clause starts with the keyword `LIMIT` and is followed by an integer that defines the limit. Similarly, the `OFFSET` clause starts with the keyword `OFFSET` and is followed by an integer that defines the offset. Furthermore:
The `LIMIT` and `OFFSET` clauses can be defined in either order.
The limit and offset may not be negatives.
The following semantics hold for the `LIMIT` and `OFFSET` clauses:
The `OFFSET` clause is always applied first, even if the `LIMIT` clause is placed before the `OFFSET` clause inside the query.
An `OFFSET` of zero has no effect and gives the same result as if the `OFFSET` clause was omitted.
If the number of actual solutions after `OFFSET` is applied is greater than the limit, then at most the limit number of solutions will be returned.

In the following query, the first 5 intermediate solutions are pruned from the result (i.e. `OFFSET 5`). The next 10 intermediate solutions are returned and become final solutions of the query (i.e. `LIMIT 10`).

```
SELECT n WHERE (n) LIMIT 10 OFFSET 5
```

## Grouping and Aggregation

GROUP BY allows for grouping of solutions and is typically used in combination with aggregation to aggregate over groups of solutions instead of over the total set of solutions.

The following explains the syntactic structure of the `GROUP BY` clause:

```
GroupByClause := 'GROUP' 'BY' {GroupTerm ','}+
GroupTerm     := Expression ('AS' Variable)?
```

The `GROUP BY` clause starts with the keywords GROUP BY and is followed by a comma-separated list of group terms. Each group term consists of:

- An expression.
- An optional variable definition that is specified by appending the keyword AS and the name of the variable.

Consider the following query:

```
SELECT n.firstName, COUNT(*), AVG(n.age)
WHERE (n WITH type = 'Person')
GROUP BY n.firstName
```

Matches are grouped by their values for `n.firstName`. For each group, the query selects `n.firstName` (i.e. the group key), the number of solutions in the group (i.e. `COUNT(*)`), and the average value of the property age for vertex n (i.e. `AVG(n.age)`).

### Assigning Variable Name to Group Expression

It is possible to assign a variable name to any of the group expression, by appending the keyword `AS` and a variable name. The variable name can be used in the `SELECT` to select a group key, or in the `ORDER BY` to order by a group key. See the related section later in this document.

```
SELECT nAge, COUNT(*)
WHERE
   (n WITH type = 'Person')
GROUP BY n.age AS nAge
ORDER BY nAge
```

### Multiple Terms in GROUP BY

It is possible that the `GROUP BY` clause consists of multiple terms. In such a case, matches are grouped together only if they hold the same result for each of the group expressions.

Consider the following query:

```
SELECT n.firstName, n.lastName, COUNT(*)
WHERE (n WITH type = 'Person')
GROUP BY n.firstName, n.lastName
```

Matches will be grouped together only if they hold the same values for `n.firstName` and the same values for `n.lastName`.

### GROUP BY and NULL values

The group for which all the group by expressions evaluate to null is ignored and does not take part in further query processing. However, a group for which some expressions evaluate to null but at least one expression evaluates to a non-null value, is not ignored and takes part in further query processing.

## Repetition of Group Expression in Select or Order Expression

Group expressions that are variable accesses, property accesses, or built-in function calls may be repeated in select or order expressions. This is a short-cut that allows you to neglect introducing new variables for simple expressions.

Consider the following query:

```
SELECT n.age, COUNT(*)
WHERE
  n
GROUP BY n.age
ORDER BY n.age
```

Here, the group expression n.age is repeated as select and order expressions.

This repetition of group expressions introduces an exception to the variable visibility rules described above, since variable n is not inside an aggregation in the select/order expression. However, semantically, the query is treated as if there were a variable for the group expression:

```
SELECT nAge, COUNT(*)
WHERE
  n
GROUP BY n.age AS nAge
ORDER BY nAge
```

# Expressions

Expressions are used in value constraints, in-lined constraints, and select/group/order terms. This section of the document defines the operators and built-in functions that can be used as part of an expression.

## Operators

The following table is an overview of the operators in PGQL.

Operator type | Operator | Example
--- | --- | ---
Arithmetic | `+`, `-`, `*`, `/`, `%` | `SELECT * WHERE n -> (m WITH start_line_num < end-line-num - 10)`
Relational | `=`, `!=`, `<`, `>`, `<=`, `>=`, `=~` | `SELECT * WHERE n --> m, n.start_line_num < m.start_line_num`
Logical | `AND`, `OR`, `NOT`, `!` | `SELECT * WHERE n --> m, n.start_line_num > 500 AND m.start_line_num > 500`

### Operator Precedence

Operator precedences are shown in the following list, from highest precedence to the lowest. An operator on a higher level is evaluated before an operator on a lower level.

Level | Operator Precedence
--- | ---
1 | `-` (unary minus), `!`
2 | `*`, `/`, `%`
3 | `+`, `-`
4 | `=`, `!=`, `<`, `>`, `<=`, `>=`, `=~`
5 | `NOT`
6 | `AND`
7 | `OR`

### Operator and Operand Types

The following table specifies operand types and operator return types.

Operator | Type(A) | Type(B) | Result Type
--- | --- | --- | ---
A `+` B<br>A `-` B<br>A `*` B<br>A `/` B<br>A `%` B | numeric | numeric | numeric
A `=` B<br>A `!=` B<br>A `<` B<br>A `>` B<br>A `<=` B<br>A `>=` B | numeric | numeric | boolean
A `=` B<br>A `=~` B | String | String | boolean
A `=` B | boolean | boolean | boolean
A `AND` B<br>A `OR` B | boolean | boolean | boolean
`NOT` A<br>`!`A | boolean | | boolean


If the value for an operand is of a type that is not defined for the operator, the operation yields `null`. There is one exception to this rule, which is that the `OR` operator yields true if either of the operands yield `true` (see the section on `null` values and operators).

### Data Type Conversion

Numeric values are automatically converted (coerced) when compared against each other. An example is as follows:

```
3 = 3.0 // this expression yields TRUE
```

Comparing between Numeric, String, or Boolean values yields `null`  (see the corresponding section for more details on the handling of `null` values).

## Regular Expression String Matching

Regular expressions for String matching is supported using the `=~` operator, which returns true if the String on the left-hand side matches the String pattern on the right-hand side.

An example is as follows:

```
n.name =~ 'ar' // this expression yields TRUE if the String n.name contains String 'ar'
'Carl' =~ 'ar' // this expression yields TRUE
'Carl' =~ 'lm' // this expression yields FALSE
```

The syntax followed for the pattern on the right-hand side, is that of Java REGEX.

## Null Values

`null` is used to represent a missing or undefined value. There are three ways in which a null value can come into existence:

- A property access (i.e. `var_name.prop_name`) returns `null` if the property is missing for a vertex or edge in the data graph.
- An expression returns `null` if any operand or function argument is `null` (with an exception for the `OR` operator, see below).
- In a query, a `null` value may be used in the place of a literal value (e.g. `n.name = null`).

### Null Values and Operators

An operator returns `null` if one of its operands yields `null`, with an exception for the `OR` operator: if the left-hand side or right-hand side of the `OR` operations returns `true`, the operation itself yields `true`. Otherwise, the operation yields `null`. The table below summarizes these rules.

Operator | Result {A = NULL} | Result {B = NULL} | Result {A = NULL, B = NULL}
--- | --- | --- | ---
A `+` `-` `*` `/` `%` B<br>A `=` `!=` `<` `>` `<=` `>=` B  | `null`  | `null`  | `null`
A `AND` B  | `null`  | `null`  | `null`
A `OR` B | `true` if B yields `true`, `null` otherwise | true if A yields `true`, `null` otherwise | `null`
`NOT` A<br>`!`A | `null` | |

Note that from the table it follows that `null = null` yields `null` and not `true`.

### Null Values as Function Argument

If any of the arguments of a function is `null`, the function itself yields `null`. For example, `x.has(null)` yields `null`.

## Built-in Functions

Built-in functions can be used in a value constraint or an in-lined constraint, or in a select/group/order expression. The following table lists the built-in functions of PGQL.

Object type | Signature | Return value | Description
vertex/edge | `id()` | numeric/string | returns the vertex/edge identifier. |
vertex/edge | `has(String prop1, String prop2, ...)` | boolean | returns `true` if the vertex or edge has the given (comma-separated) properties.
vertex | `inDegree()` | decimal | returns the number of incoming neighbors.
vertex | `outDegree()` | decimal | returns the number of outgoing neighbors.
vertex/edge | `hasLabel(String lbl)` | boolean | returns true if the vertex or edge has the given label.
vertex | `labels()` | Set<String> | returns the labels of the vertex.
edge | `label()` | String | returns the label of the edge.

The syntactic structure of a built-in function call is as follows:

```
FunctionCall := 'id' '(' ')' |
                'has' '(' {String ','}+ ')' |
                'inDegree' '(' ')' |
                'outDegree' '(' ')' |
                'hasLabel' '(' ')' |
                'labels' '(' ')' |
                'label' '(' ')'
```

A build-in function call is a function name followed by zero or more function arguments. The function arguments are in between rounded brackets. Furthermore, function names are not case-sensitive.

In contrast to SQL, the vertex or edge to which the function applies (i.e. the object), is not passed as one of the function arguments. Instead, the same dot expression syntax that is used for a property access, is also used for a function call: `variable_name.function_name(function arguments)`.

Consider the following query:

```
SELECT y.id()
WHERE
  x -> y,
  x.inDegree() > 10
```

Here, `x.inDegree()` returns the number of incoming neighbors of `x`, whereas `y.id()` returns the identifier of the vertex `y`. Variables `x` and `y` are the objects of the two function calls.

### Simplified Function Calls in the In-lined Expressions

The same syntactic structure rules that apply to a simplified property access, also apply to a function call in an in-lined expression. That is, the object of the function call can be omitted since it is clear from the context. Moreover, the leading dot can be omitted too. The following table summarizes these short-cut rules.

Normal Function Call | In-lined Function Call | In-lined Function Call (alternative)
--- | --- | ---
`n.outDegree() > 10` | `(n WITH .outDegree() > 10)` | `(n WITH outDegree() > 10)`

# Other Syntactic Rules

## Syntax for Variables

The syntactic structure of a variable name is an alphabetic character followed by zero or more alphanumeric or underscore (i.e. `_`) characters:

```
Variable := [a-zA-Z][a-zA-Z0-9\_]*
```

## Syntax for Properties

Property names may be quoted or unquoted. Quoted and unquotes property names may be used interchangeably. If unquoted, the syntactic structure of a property name is the same as for a variable name. That is, an alphabetic character followed by zero or more alphanumeric or underscore (i.e. _) characters. If quoted, the syntactic structure is that of a String (for the syntactic structure, see String literal).

```
PropertyName := String |
                UnquotedString
```

## Literals

The literal types are String, Integer, Decimal, and Boolean. The following shows the syntactic structure of the different types of literals.

```
String  := "'" (~[\'\n\\] | EscapedCharacter)* "'" |
           '"' (~[\"\n\\] | EscapedCharacter)* '"'
Integer := '-'? [0-9]+
Decimal := '-'? [0-9]* '.' [0-9]+
Boolean := 'true' |
           'false'
```

Just like `null`, `true` and `false` are case-insensitive.

### Single-quoted and Double-quoted Strings

A String literal may either be single or double quoted. Single and double quoted Strings can be used interchangeably. For example, the following expression evaluates to true.

```
"Person" = 'Person' // this expression evaluates to TRUE
```

### Escaped Characters in Strings

Escaping in String literals is necessary to support having white space, quotation marks and the backslash character as a part of the literal value. The following explains the syntax of an escaped character.

```
EscapedCharacter := '\' [tnr\"']
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

### Optional Escaping of Quotes in Strings

In single quoted String literals, it is optional to escape double quotes, while in double quoted String literals, it is optional to escape single quotes. The following table provides examples of String literals with escaped quotes, and corresponding String literals in which quotes are not escaped.

With escape | Without escape
--- | ---
`'single quoted string literal with \"double\" quotes inside'` | `'single quoted string literal with "double" quotes inside'`
`"double quoted string literal with \'single\' quotes inside"` | `"double quoted string literal with 'single' quotes inside"`

Note that the value of the literal is the same no matter if quotes are escaped or not. This means that, for example, the following expression evaluates to `true`.

```
'\"double" quotes and \'single\' quotes' = "\"double\" quotes and \'single' quotes" // this expression evaluates to TRUE
```

## Keywords

The following is the list of keywords in PGQL.

```
PATH, SELECT, WHERE, AS, WITH, ORDER, GROUP, BY, ASC, DESC, LIMIT, OFFSET, AND, OR, true, false, null
```

There are certain restrictions when using keywords as variable or property name:

- Keywords cannot be used as a variable name.
- Keywords can only be used as a property name, if quotations are used when accessing the property: `SELECT * WHERE n -> m, n.'GROUP' = 'managers'`

Finally, keywords are not case-sensitive. For example, `SELECT`, `Select` and `sELeCt`, are all valid.

## Comments

There are two kinds of comments: single-line comments and multi-line comments. Single-line comments start with double backslashes (`\\`), while multi-line comments are delimited by `/*` and `*/`. The following shows the syntactic structure of the two forms.

```
Comment           := SingleLineComment |
                     MultiLineComment
SingleLineComment := '//' ~[\n]*
MultiLineComment  := '/*' ~[\*]* '*/'
```

An example query with both single-line and multi-line comments is as follows:

```
/* This is a
   multi-line
   comment */
SELECT n.name, n.age
WHERE
  (n WITH type = 'Person') // this is a single-line comment
```

## White Space

White space consists of spaces, new lines and tabs. White space is significant in String literals, as the white space is part of the literal value and taken into account when comparing against data values. Outside of String literals, white space is ignored. However, for readability consideration and ease of parser implementation, the following rules should be followed when writing a query:

- A keyword should not be followed directly by a variable or property name.
- A variable or property name should not be followed directly by a keyword.

If these rules are not followed, a PGQL parser may or may not treat it as an error.

Consider the following query:

```
SELECT n.name, m.name
WHERE
  (n WITH type = 'Person', name = 'Ron Weasley') -> m
```

This query can be reformatted with minimal white space, while guaranteeing compatibility with different parser implementations, as follows:

```
SELECT n.name,m.name WHERE(n WITH type='Person',name='Ron Weasley')->m
```

Note that the white space after the `SELECT` keyword, in front of the `WHERE` keyword, before and after the `WITH` keyword and in the String literal `'Ron Weasley'` cannot be omitted.
















































