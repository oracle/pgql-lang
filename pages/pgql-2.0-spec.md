---
title: "PGQL 2.0 Specification"
date: "19 May 2023"
permalink: /spec/2.0/
summary: "PGQL is an SQL-based query language for the property graph data model that allows
you to specify high-level graph patterns which are matched against vertices and edges in a graph.
PGQL has support for grouping (GROUP BY), aggregation (e.g. MIN, MAX, AVG, SUM), sorting (ORDER BY) and many other familiar SQL constructs.
Furthermore, PGQL has powerful regular expression constructs for graph reachability (transitive closure), shortest path finding and
cheapest path finding."
sidebar: spec_2_0_sidebar
toc: false
---

# Introduction

PGQL is a graph pattern-matching query language for the [property graph data model](#property-graph-data-model). This document specifies the syntax and semantics of the language.

## Changelog

The following are the changes since PGQL 1.5:

### New features in PGQL 2.0

The new (and fully SQL-compatible) features are:

 - [GRAPH_TABLE Operator](#graph_table-operator)
 - [LATERAL Subquery](#lateral-subqueries)
 - [Path Modes](#path-modes) (`WALK`, `ACYCLIC`, `SIMPLE`, `TRAIL`)
 - [LABELED Predicate](#labeled-predicate), [SOURCE/DESTINATION Predicate](#source--destination-predicate), [MATCHNUM Function](#matchnum-function) and [VERTEX_ID/EDGE_ID Function](#vertex_idedge_id-function)
 - [FETCH FIRST Clause](#fetch-first-clause)

## A note on the Grammar

This document contains a complete grammar definition of PGQL, spread throughout the different sections. There is a single entry point into the grammar: <PgqlStatement>.

## Document Outline

 - [Introduction](#introduction) contains a changelog, a note on the grammar, this outline and an introduction to the property graph data model.
 - [Creating a Property Graph](#creating-a-property-graph) describes how to create a property graph from an existing set of tables in a relational database.
 - [Graph Pattern Matching](#graph-pattern-matching) introduces the basic concepts of graph querying.
 - [Variable-Length Paths](#variable-length-paths) introduces the constructs for matching variable-length paths such as shortest paths or cheapests paths.
 - [Number of Rows Per Match](#number-of-rows-per-match) describes how to specify the number of rows per match, for example to obtain one row per vertex in a shortest path.
 - [Grouping and Aggregation](#grouping-and-aggregation) describes the mechanism to group and aggregate results.
 - [Sorting and Row Limiting](#sorting-and-row-limiting) describes the ability to sort and paginate results.
 - [Functions and Expressions](#functions-and-expressions) describes the supported data types and corresponding functions and operations.
 - [Subqueries](#subqueries) describes the syntax and semantics of subqueries for creating more complex queries that nest other queries.
 - [Graph Modification](#graph-modification) describes `INSERT`, `UPDATE` and `DELETE` statements for inserting, updating and deleting vertices and edges in a graph.
 - [Other Syntactic rules](#other-syntactic-rules) describes additional syntactic rules that are not covered by the other sections, such as syntax for identifiers and comments.

## Property graph data model

A property graph has a name, which is a (character) string, and contains:

 - A set of vertices (or nodes).

   - Each vertex has zero or more labels.
   - Each vertex has zero or more properties (or attributes), which are arbitrary key-value pairs.

 - A set of edges (or relationships).

   - Each edge is directed.
   - Each edge has zero or more labels.
   - Each edge has zero or more properties (or attributes), which are arbitrary key-value pairs.

Labels as well as names of properties are strings. Property values are scalars such as numerics, strings or booleans.

### Example 1: Student Network

An example graph is:

{% include image.html file="example_graphs/student_network.png"  %}

Here, `student_network` is the name of the graph. The graph has three vertices labeled `Person` and one vertex labeled `University`. There are six directed edges that connect the vertices. Three of them go from person to person vertices, and they have the label `knows`. Three others go from person to university vertices and are labeled `studentOf`. The person vertices have two properties, namely `name` for encoding the name of the person and `dob` for encoding the date of birth of the person. The university vertex has only a single property `name` for encoding the name of the university. The edges have no properties.

### Example 2: Financial Transactions

An example graph with financial transactions is:

{% include image.html file="example_graphs/financial_transactions.png" %}

Here, `financial_transactions` is the name of the graph. The graph has three types of vertices. Vertices labeled `Person` or `Company` have a property `name`, while vertices labeled `Account` have a property `number`. There are edges labeled `owner` from accounts to persons as well as from accounts to companies, and there are edges labeled `transaction` from accounts to accounts. Note that only `transaction` edges have a property (`amount`) while other edges do not have any properties.


# Creating a Property Graph

The [CREATE PROPERTY GRAPH](#create-property-graph) statement allows for creating a property graph from a set of existing database tables,
while the [DROP PROPERTY GRAPH](#drop-property-graph) statements allows for dropping a graph.

## CREATE PROPERTY GRAPH

The `CREATE PROPERTY GRAPH` statement starts with a graph name and is followed by a non-empty set of vertex tables and an optional set of edge tables.

The syntax is:

```bash
CreatePropertyGraph  ::= 'CREATE' 'PROPERTY' 'GRAPH' <GraphName>
                         <VertexTables>
                         <EdgeTables>?

GraphName            ::= <SchemaQualifiedName>

SchemaQualifiedName  ::= <SchemaIdentifierPart>? <Identifier>

SchemaIdentifierPart ::= <Identifier> '.'

VertexTables         ::= 'VERTEX' 'TABLES' '(' <VertexTable> ( ',' <VertexTable> )* ')'

EdgeTables           ::= 'EDGE' 'TABLES' '(' <EdgeTable> ( ',' <EdgeTable> )* ')'
```

It is possible to have no edge tables such that the resulting graph only has vertices that are all disconnected from each other.
However, it is not possible to have a graph with edge tables but no vertex tables.

The following example shows a schema with a set of tables. Each table has a name and a list of columns, some of which form the primary key for the table (in red) while others form foreign keys that reference rows of other tables.

{% include image.html file="example_graphs/financial_transactions_schema.png"  %}

The following is a complete example of how a graph can be created from these tables:

```sql
CREATE PROPERTY GRAPH financial_transactions
  VERTEX TABLES (
    Persons LABEL Person PROPERTIES ( name ),
    Companies LABEL Company PROPERTIES ( name ),
    Accounts LABEL Account PROPERTIES ( number )
  )
  EDGE TABLES (
    Transactions
      SOURCE KEY ( from_account ) REFERENCES Accounts ( number )
      DESTINATION KEY ( to_account ) REFERENCES Accounts ( number )
      LABEL transaction PROPERTIES ( amount ),
    Accounts AS PersonOwner
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION Persons
      LABEL owner NO PROPERTIES,
    Accounts AS CompanyOwner
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION Companies
      LABEL owner NO PROPERTIES,
    Persons AS worksFor
      SOURCE KEY ( id ) REFERENCES Persons ( id )
      DESTINATION Companies
      NO PROPERTIES
  )
```

Above, `financial_transactions` is the name of the graph.
The graph has three vertex tables: `Persons`, `Companies` and `Accounts`.
The graph also has four edge tables: `Transactions`, `PersonOwner`, `CompanyOwner` and `worksFor`.

Underlying foreign keys are used to establish the connections between the two endpoints of the edges and the corresponding vertices.
Note that the "source" of an edge is the vertex where the edge points _from_ while the "destination" of an edge is the vertex where the edge point _to_.

If foreign keys cannot be used or are not present, the necessary keys can be defined as part of the `CREATE PROPERTY GRAPH` statement.
Labels and properties can also be defined, all of which is explained in more detail in the next sections.

### Vertex tables

A vertex table provides a vertex for each row of the underlying table.

The syntax is:

```bash
VertexTable              ::= <TableName> <TableAlias>? <KeyClause>? <LabelAndPropertiesClause>?

LabelAndPropertiesClause ::= <LabelClause>? <PropertiesClause>?

TableName                ::= <SchemaQualifiedName>
```

The [table alias](#table-aliases) is required only if the underlying table is used as vertex table more than once, to provide a unique name for each table.
It can be used for specifying a [label](#labels) for the vertices too.

The key of the vertex table uniquely identifies a row in the table.
If a key is not explicitly specified then it defaults to the primary key of the underlying table.
A key is always required so a primary key needs to exist if no key is specified.
See the section on [keys](#keys) for more details.

The label clause provides a label for the vertices.
If a label is not defined, the label defaults to the alias.
Since the alias defaults to the name of the underlying table, if no alias is provided, the label defaults to the name of the underlying table.
See the section on [labels](#labels) for details.

The properties clause defines the mapping from columns of the underlying table into properties of the vertices.
See the section on [properties](#properties) for more details.

### Edge tables

An edge table provides an edge for each row of the underlying table.

```bash
EdgeTable                      ::= <TableName> <TableAlias>? <KeyClause>?
                                   <SourceVertexTable> <DestinationVertexTable>
                                   <LabelAndPropertiesClause>?

SourceVertexTable              ::= 'SOURCE' <ReferencedVertexTableKeyClause>? <TableName>

DestinationVertexTable         ::= 'DESTINATION' <ReferencedVertexTableKeyClause>? <TableName>

ReferencedVertexTableKeyClause ::= <KeyClause> 'REFERENCES'
```

The [table alias](#table-aliases) is required only if the underlying table is used as edge table more than once, to provide a unique name for each table.
It can be used for specifying a [label](#labels) for the edges too.

The source vertex table and destination vertex table are mandatory for defining the two endpoints of the edge.
A key is optional if there is a single foreign key from the edge table to the source or destination vertex table.
If a key is not provided, it will default to the existing foreign key.

Take the following example from before:

{% include image.html file="example_graphs/financial_transactions_schema.png"  %}

```sql
CREATE PROPERTY GRAPH financial_transactions
  VERTEX TABLES (
    Persons LABEL Person PROPERTIES ( name ),
    Companies LABEL Company PROPERTIES ( name ),
    Accounts LABEL Account PROPERTIES ( number )
  )
  EDGE TABLES (
    Transactions
      SOURCE KEY ( from_account ) REFERENCES Accounts ( number )
      DESTINATION KEY ( to_account ) REFERENCES Accounts ( number )
      LABEL transaction PROPERTIES ( amount ),
    Accounts AS PersonOwner
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION Persons
      LABEL owner NO PROPERTIES,
    Accounts AS CompanyOwner
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION Companies
      LABEL owner NO PROPERTIES,
    Persons AS worksFor
      SOURCE KEY ( id ) REFERENCES Persons ( id )
      DESTINATION Companies
      NO PROPERTIES
  )
```

The key of the edge table uniquely identifies a row in the table.
If a key is not explicitly specified (in case of all four edge tables above) then it defaults to the primary key of the underlying table.
A key is always required so a primary key needs to exist if no key is specified.
See the section on [keys](#keys) for more details.

In case of edge tables `PersonOwner`, `CompanyOwner` and `worksFor`, the destination vertex table is the same table as the edge table itself.
This means that rows in the table are mapped into both vertices and edges. It is also possible that the source vertex table is the edge table itself or that both the source and destination tables are the edge table itself.
This is explained in more detail in [Source or destination is self](#source-or-destination-is-self).

Keys for the destinations of `PersonOwner`, `CompanyOwner` and `worksFor` are omitted because we can default to the existing foreign keys.
Keys for their sources cannot be omitted because there exist no foreign key to default to (e.g. in case of `PersonOwner` there are zero foreign keys from `Accounts` to `Accounts` hence `SOURCE KEY ( number ) REFERENCES Accounts ( number )` needs to be specified).
Furthermore, keys for the source and destination of `Transactions` cannot be omitted because _two_ foreign keys exist between `Transactions` and `Accounts` so it is necessary to specify which one to use.

If a row in an edge table has a NULL value for any of its source key columns or its destination key columns then no edge is created.
Note that in case of the `Accounts` table from the example, it is assumed that either the `person_id` or the `company_id` is NULL, so that each time the row is mapped into either a "company owner" or a "person owner" edge but never into two types of edges at once.

The label clause provides a label for the edges.
If a label is not defined, the label defaults to the alias.
Since the alias defaults to the name of the underlying table, if no alias is provided, the label defaults to the name of the underlying table.
See the section on [labels](#labels) for details.

The properties clause defines the mapping from columns of the underlying table to properties of the edges.
See the section on [properties](#properties) for more details

### Table aliases

Vertex and edge tables can have aliases for uniquely naming the tables.
If no alias is defined, then the alias defaults to the name of the underlying database table of the vertex or edge table.

The syntax is:

```bash
TableAlias ::= ( 'AS' )? <Identifier>
```

For example:

```sql
...
  EDGE TABLES ( Persons AS worksFor ... )
...
```

Above, the underlying table of the edge table is `Persons`, while the alias is `worksFor`.

All vertex and edge tables are required to have unique names.
Therefore, if multiple vertex tables use the same underlying table, then at least one of them requires an alias.
Similarly, if multiple edge tables use the same underlying table, then at least one of them requires an alias.
The restriction does not apply across vertex and edge tables, so, there may exist a vertex table with the same name as an edge table,
but there may not exist two vertex tables with the same name, or two edge tables with the same name.

If the alias is not provided then it defaults to the name of the underlying table.
For example:

```sql
...
  VERTEX TABLES ( Person )
...
```

Above is equivalent to:

```sql
...
  VERTEX TABLES ( Person AS Person )
...
```

Finally, in addition to providing unique names for vertex and edge tables, the aliases can also serve as a means to provide [labels](#labels) for vertices and edges:
if no label is defined then the label defaults to the table alias.
Note that although table aliases are required to be unique, labels are not.
In other words, multiple vertex tables and multiple edge tables can have the same label.

### Keys

By default, existing primary and foreign keys of underlying tables are used to connect the end points of the edges to the appropriate vertices, but the following scenarios
require manual specification of keys:

 - Multiple foreign keys exists between an edge table and its source vertex table or its destination vertex tables such that it would be ambiguous which foreign key to use.
 - Primary and/or foreign keys on underlying tables were not defined or the underlying tables are views which means that primary and foreign keys cannot be defined.

The syntax for keys is:

```bash
KeyClause  ::= '(' <ColumnName> ( ',' <ColumnName> )* ')'

ColumnName ::= <Identifier>
```

Take the example from before:

{% include image.html file="example_graphs/financial_transactions_schema.png"  %}

```sql
CREATE PROPERTY GRAPH financial_transactions
  VERTEX TABLES (
    ...
  )
  EDGE TABLES (
    Transactions
      SOURCE KEY ( from_account ) REFERENCES Accounts ( number )
      DESTINATION KEY ( to_account ) REFERENCES Accounts ( number )
      LABEL transaction PROPERTIES ( amount ),
    Accounts AS PersonOwner
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION Persons
      LABEL owner NO PROPERTIES,
    ...
  )
```

Above, a key is defined for the source and destination of `Transactions` because two foreign keys exist between `Transactions` and `Accounts` so it would be ambiguous which one to use without explicit specification.
In case of `PersonOwner`, no foreign key exists between `Accounts` and `Accounts` so a key for the source (`KEY ( number )`) has to be explicitly specified. However, for the destination it is possible to omit the key and default to the existing foreign key between `Accounts` and `Persons`.

The keys for source and destination vertex tables consist of one or more columns of the underlying edge table that uniquely identify a vertex in the corresponding vertex table. If no key is defined for the vertex table, the key defaults to the underlying primary key, which is required to exist in such a case.

The following example has a schema that has no primary and foreign keys defined at all:

{% include image.html file="example_graphs/financial_transactions_schema_no_keys.png"  %}

Note that above, we have the same schema as before, but this time the primary and foreign keys are missing.

Even though primary and foreign keys are missing, the graph can still be created by specifying the necessary keys in the `CREATE PROPERTY GRAPH` statement itself:

```sql
CREATE PROPERTY GRAPH financial_transactions
  VERTEX TABLES (
    Persons
      KEY ( id )
      LABEL Person
      PROPERTIES ( name ),
    Companies
      KEY ( id )
      LABEL Company
      PROPERTIES ( name ),
    Accounts
      KEY ( number )
      LABEL Account
      PROPERTIES ( number )
  )
  EDGE TABLES (
    Transactions
      KEY ( from_account, to_account, date )
      SOURCE KEY ( from_account ) REFERENCES Accounts ( number )
      DESTINATION KEY ( to_account ) REFERENCES Accounts ( number )
      LABEL transaction PROPERTIES ( amount ),
    Accounts AS PersonOwner
      KEY ( number )
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION KEY ( person_id ) REFERENCES Persons ( id )
      LABEL owner NO PROPERTIES,
    Accounts AS CompanyOwner
      KEY ( number )
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION KEY ( company_id ) REFERENCES Companies ( id )
      LABEL owner NO PROPERTIES,
  Persons AS worksFor
      KEY ( id )
      SOURCE KEY ( id ) REFERENCES Persons ( id )
      DESTINATION KEY ( company_id ) REFERENCES Companies ( id )
      NO PROPERTIES
  )
```

Above, keys were defined for each vertex table (e.g. `KEY ( id )`), edge table (e.g. `KEY ( from_account, to_account, date )`), source vertex table reference (e.g. `KEY ( from_account )`) and destination table reference (e.g. `KEY ( to_account )`).

Each vertex and edge table is required to have a key so that if a key is not explicitly specified then the underlying table needs to have a primary key defined.

### Labels

In graphs created through `CREATE PROPERTY GRAPH`, each vertex has exactly one label and each edge has exactly one label.
This restriction may be lifted in future PGQL version.

The syntax for labels is:

```bash
LabelClause ::= 'LABEL' <Label>

Label       ::= <Identifier>
```

The label clause is optional. If it is omitted, then the label defaults to the [table alias](#table-aliases).
Note that also the table alias is optional and defaults to the table name.
Thus, if no label is specified and no table alias is specified, then both the table alias and the label defaults to the table name.

For example:

```sql
...
  VERTEX TABLES ( Person )
...
```

Above is equivalent to:

```sql
...
  VERTEX TABLES ( Person AS Person )
...
```

Which is equivalent to:

```sql
...
  VERTEX TABLES ( Person AS Person LABEL Person )
...
```

### Properties

By default, properties are all columns such that a vertex or edge property is created for each column of the underlying table.
However, there are different ways to customize this behavior as described below.

The syntax is:

```bash
PropertiesClause ::=   <PropertiesAreAllColumns>
                     | <PropertyExpressions>
                     | <NoProperties>
```

Note that the properties clause is optional and if the clause is omitted then it defaults to `PROPERTIES ARE ALL COLUMNS`.

#### PROPERTIES ARE ALL COLUMNS

Although by default a property is created for each columns implicitly, this can also be made explicit through `PROPERTIES ARE ALL COLUMNS`.

The syntax is:

```bash
PropertiesAreAllColumns ::= 'PROPERTIES' <AreKeyword>? 'ALL' 'COLUMNS' <ExceptColumns>?

AreKeyword              ::= 'ARE'
```

An example is:

```sql
...
  VERTEX TABLES ( Person PROPERTIES ARE ALL COLUMNS )
...
```

Because of the default, the above is equivalent to:

```sql
...
  VERTEX TABLES ( Person )
...
```

#### PROPERTIES ARE ALL COLUMNS EXCEPT ( .. )

One can exclude columns by adding an `EXCEPT` clause.
The columns that are excluded will not become properties while all the other columns do.

The syntax is:

```bash
ExceptColumns ::= 'EXCEPT' '(' <ColumnReference> ( ',' <ColumnReference> )* ')'
```

#### PROPERTIES ( .. )

Instead of excluding columns (see above), "property expressions" allow for specifying exactly which columns should be included.
The property expressions also allow you to use a `CAST` expression to map the column into a property of a different data type.

The syntax is:

```bash
PropertyExpressions                ::= 'PROPERTIES' '(' <PropertyExpression> ( ',' <PropertyExpression> )* ')'

PropertyExpression                 ::= <ColumnReferenceOrCastSpecification> ( 'AS' <PropertyName> )?

ColumnReferenceOrCastSpecification ::=   <ColumnReference>
                                       | <CastSpecification>

PropertyName                       ::= <Identifier>

ColumnReference                    ::= <Identifier>
```

For example:

```sql
...
  VERTEX TABLES (
    Employees
      LABEL Employee
      PROPERTIES ( first_name ),
...
```

Above, even though table `Employees` may have many columns, only the column `first_name` is used as a property. The name of the property defaults to the name of the column: `first_name`.

If a different property name is desired then an alias can be used:

```sql
...
  VERTEX TABLES (
    Employees
      LABEL Employee
      PROPERTIES ( first_name AS firstName ),
...
```

Above, the column name `first_name` becomes a property with name `firstName` (notice the missing underscore character in the property name).

Property names may also be `CAST` expressions, which allows the values in the column to be converted into properties of a different data type.

For example:

```sql
...
  VERTEX TABLES (
    Employees
      LABEL Employee
      PROPERTIES ( CAST(salary AS INTEGER) AS salary ),
...
```

#### NO PROPERTIES

If no properties are desired for the vertices or edges, then one can use the `NO PROPERTIES` syntax:

```bash
NoProperties ::= 'NO' 'PROPERTIES'
```

An example of an edge table with no properties is:

```sql
...
  EDGE TABLES (
    ...
    Accounts AS PersonOwner
      SOURCE KEY ( number ) REFERENCES Accounts ( number )
      DESTINATION Persons
      LABEL owner NO PROPERTIES
    ...
```

#### Relation between labels and properties

Vertex tables that have the same label are required to have the same properties such that the properties have the same name and compatible data types.
Similarly, edge tables that have the same label are required to have the same properties such that the properties have the same name and compatible data types.

Take the following example:

```sql
...
  VERTEX TABLES (
    /* ERROR: it is not allowed to have tables with the same labels but different properties */
    Country LABEL Place PROPERTIES ( country_name ),
    City LABEL Place PROPERTIES ( city_name )
  )
...
```

The statement above is illegal because both `Country` and `City` have label `Place` but their properties are inconsistent. To make this example work, the same property names have to be assigned:

```sql
...
  VERTEX TABLES (
    Country LABEL Place PROPERTIES ( country_name AS name ),
    City LABEL Place PROPERTIES ( city_name AS name )
  )
...
```

### Source or destination is self

A source and/or a destination vertex table of an edge may be the edge table itself.
In such a case, the underlying table provides both vertices and edges at the same time.

Take the following schema as example:

{% include image.html file="example_graphs/hr_schema_simplified.png" %}

Here, both tables are clear candidates for vertex tables, but it is not immediately clear which are the edge tables corresponding to the
"employee works for employee" and "department managed by employee" relationships.

These edge tables are in fact the `Employees` and `Departments` tables themselves.

The graph can be created as follows:

```sql
CREATE PROPERTY GRAPH hr_simplified
  VERTEX TABLES (
    employees LABEL employee
      PROPERTIES ARE ALL COLUMNS EXCEPT ( job_id, manager_id, department_id ),
    departments LABEL department
      PROPERTIES ( department_id, department_name )
  )
  EDGE TABLES (
    employees AS works_for
      SOURCE KEY ( employee_id ) REFERENCES employees ( employee_id )
      DESTINATION KEY ( manager_id ) REFERENCES employees ( employee_id )
      NO PROPERTIES,
    departments AS managed_by
      SOURCE KEY ( department_id ) REFERENCES departments ( department_id )
      DESTINATION employees
      NO PROPERTIES
  )
```

As you can see, the `employee` vertices are created from the `employees` table, but so are the `works_for` edges that represent the managers of employees.
The source key is the primary key of the table, while the destination key corresponds to the foreign key.
It is optional to simplify `DESTINATION KEY ( manager_id ) REFERENCES employees ( employee_id )` to `DESTINATION employees` to make use of the existing foreign key.
This is possible only because there exists exactly one foreign key between the `employees` table and itself.
Do note that in this example, we cannot default the _source_ vertex to the foreign key,
so we need to explicitly specify it (`KEY ( employee_id )`).

Similarly, the `department` vertices are created from the `departments` table, but so are the `managed_by` edges that represent the managers of departments.
The source of the edge table is again the table itself and therefore references the primary key.
The destination, on the other hand, is the `employees` table. Here, because there exists a (single) foreign key between `departments` and `employees`,
the destination key `KEY ( manager_id )` was omitted to make it default to the foreign key.

Furthermore, even though the edges are embedded in the vertex tables, it is still the case that by default a property is created for each of the columns of the table.
Therefore, we specify `NO PROPERTIES` for the edge tables as we already place the necessary properties on the vertex tables.

### Example: HR schema

A more complex example is the Human Resources (HR) schema:

{% include image.html file="example_graphs/hr_schema.png"  %}

The following statement maps the schema into a graph:

```sql
CREATE PROPERTY GRAPH hr
  VERTEX TABLES (
    employees LABEL employee
      PROPERTIES ARE ALL COLUMNS EXCEPT ( job_id, manager_id, department_id ),
    departments LABEL department
      PROPERTIES ( department_id, department_name ),
    jobs LABEL job
      PROPERTIES ARE ALL COLUMNS,
    job_history
      PROPERTIES ( start_date, end_date ),
    locations LABEL location
      PROPERTIES ARE ALL COLUMNS EXCEPT ( country_id ),
    countries LABEL country
      PROPERTIES ARE ALL COLUMNS EXCEPT ( region_id ),
    regions LABEL region
  )
  EDGE TABLES (
    employees AS works_for
      SOURCE KEY ( employee_id ) REFERENCES employees ( employee_id )
      DESTINATION KEY ( manager_id ) REFERENCES employees ( employee_id )
      NO PROPERTIES,
    employees AS works_at
      SOURCE KEY ( employee_id ) REFERENCES employees ( employee_id )
      DESTINATION departments
      NO PROPERTIES,
    employees AS works_as
      SOURCE KEY ( employee_id ) REFERENCES employees ( employee_id )
      DESTINATION jobs
      NO PROPERTIES,
    departments AS managed_by
      SOURCE KEY ( department_id ) REFERENCES departments ( department_id )
      DESTINATION employees
      NO PROPERTIES,
    job_history AS for_employee
      SOURCE KEY ( employee_id, start_date ) REFERENCES job_history ( employee_id, start_date)
      DESTINATION employees
      NO PROPERTIES,
    job_history AS for_department
      SOURCE KEY ( employee_id, start_date ) REFERENCES job_history ( employee_id, start_date)
      DESTINATION departments
      NO PROPERTIES,
    job_history AS for_job
      SOURCE KEY ( employee_id, start_date ) REFERENCES job_history ( employee_id, start_date)
      DESTINATION jobs
      NO PROPERTIES,
    departments AS department_located_in
      SOURCE KEY ( department_id ) REFERENCES departments ( department_id )
      DESTINATION locations
      LABEL located_in
      NO PROPERTIES,
    locations AS location_located_in
      SOURCE KEY ( location_id ) REFERENCES locations ( location_id )
      DESTINATION countries
      LABEL located_in
      NO PROPERTIES,
    countries AS country_located_in
      SOURCE KEY ( country_id ) REFERENCES countries ( country_id )
      DESTINATION regions
      LABEL located_in
      NO PROPERTIES
  )
```

In this example, all the edge tables have a source vertex table that is the edge table itself.
This scenario was explained in more detail in [Source or destination is self](#source-or-destination-is-self).
Also note that the graph only has vertex properties, but no edge properties, which is typical for such a scenario.

After the graph is created it can be queried.
For example, we may want to see an overview of the vertex and edge labels and their frequencies.
Therefore, we first perform a `SELECT` query to create such an overview for the vertex labels:

```sql
  SELECT label(n) AS lbl, COUNT(*)
    FROM MATCH (n)
GROUP BY lbl
ORDER BY COUNT(*) DESC
```

```
+------------------------+
| lbl         | COUNT(*) |
+------------------------+
| EMPLOYEE    | 107      |
| DEPARTMENT  | 27       |
| COUNTRY     | 25       |
| LOCATION    | 23       |
| JOB         | 19       |
| JOB_HISTORY | 10       |
| REGION      | 4        |
+------------------------+
```

Note that above, labels are uppercased since [unquoted identifiers](#unquoted-identifiers) were used in the `CREATE PROPERTY GRAPH` statement.
Like in SQL, [quoted identifiers](#quoted-identifiers) can be used if such implicit upper casing of identifiers is not desired.

Then, we create an overview of labels of edges and labels of their source and destination vertices, again with frequencies for each combination:

```sql
  SELECT label(n) AS srcLbl, label(e) AS edgeLbl, label(m) AS dstLbl, COUNT(*)
    FROM MATCH (n) -[e]-> (m)
GROUP BY srcLbl, edgeLbl, dstLbl
ORDER BY COUNT(*) DESC
```

```
+--------------------------------------------------+
| srcLbl      | edgeLbl    | dstLbl     | COUNT(*) |
+--------------------------------------------------+
| EMPLOYEE    | WORKS_AS   | JOB        | 107      |
| EMPLOYEE    | WORKS_AT   | DEPARTMENT | 106      |
| EMPLOYEE    | WORKS_FOR  | EMPLOYEE   | 106      |
| DEPARTMENT  | LOCATED_IN | LOCATION   | 27       |
| COUNTRY     | LOCATED_IN | REGION     | 25       |
| LOCATION    | LOCATED_IN | COUNTRY    | 23       |
| DEPARTMENT  | MANAGED_BY | EMPLOYEE   | 11       |
| JOB_HISTORY | FOR        | JOB        | 10       |
| JOB_HISTORY | FOR        | EMPLOYEE   | 10       |
| JOB_HISTORY | FOR        | DEPARTMENT | 10       |
+--------------------------------------------------+
```

### Multiple schemas

Vertex and edge tables of a graph can come from different database schemas.
This can be achieved by qualifying the vertex and edge table names with a schema name.

For example:

```sql
CREATE PROPERTY GRAPH
  VERTEX TABLES (
    SocialNetwork.Person,
    HR.Employees LABEL Employee
  )
  EDGE TABLES (
    MySchema.SameAs
      SOURCE KEY ( firstName, lastName ) REFERENCES Person ( firstName, lastName )
      DESTINATION KEY ( first_name, last_name ) REFERENCES Employee ( first_name, last_name )
  )
```

Above, the vertex table `Person` is part of schema `SocialNetwork`,
the vertex table `Employee` is part of schema `HR`
and the edge table `SameAs` is part of schema `MySchema`.

Note that for the edge table, the source and destination vertex tables are referenced by table name without schema name (e.g. `Person` instead of `SocialNetwork.Person`).
Also note that if no table aliases or labels are defined, then they default to the table name without the schema name.

## DROP PROPERTY GRAPH

To drop a property graph use `DROP PROPERTY GRAPH` followed by the name of the graph to drop.

The syntax is:

```bash
DropPropertyGraph ::= 'DROP' 'PROPERTY' 'GRAPH' <GraphReference>
```

For example:

```sql
DROP PROPERTY GRAPH financial_transactions
```

# Graph Pattern Matching

## Writing simple queries

This section is mostly example-based and is meant for beginning users.

### Vertex patterns

The following query matches all the vertices with the label `Person` and retrieves their properties `name` and `dob`:

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT n.name, n.dob
  FROM MATCH (n:Person)
```

```
+-----------------------+
| name     | dob        |
+-----------------------+
| Riya     | 1995-03-20 |
| Kathrine | 1994-01-15 |
| Lee      | 1996-01-29 |
+-----------------------+
```

In the query above:
 - `(n:Person)` is a vertex pattern in which `n` is a variable name and `:Person` a label expression.
 - Variable names like `n` can be freely chosen by the user. The vertices that match the pattern are said to "bind to the variable".
 - The label expression `:Person` specifies that we match only vertices that have the label `Person`.
 - `n.name` and `n.dob` are property references, accessing the properties `name` and `dob` of the vertex `n` respectively.

The query produces three results, which are returned as a table. The results are unordered.

### Edge patterns

Edge patterns take the form of arrows like `-[e]->` (match an outgoing edge) and `<-[e]-` (match an incoming edge).

For example:

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT a.name AS a, b.name AS b
  FROM MATCH (a:Person) -[e:knows]-> (b:Person)
```

```
+---------------------+
| a        | b        |
+---------------------+
| Kathrine | Riya     |
| Kathrine | Lee      |
| Lee      | Kathrine |
+---------------------+
```

In the above query:
 - `-[e:knows]->` is an edge pattern in which `e` is a variable name and `:knows` a label expression.
 - The arrowhead `->` specifies that the pattern matches edges that are outgoing from `a` and incoming to `b`.

### Label expressions

More complex label expressions are supported through [label disjunction](#label-disjunction). Furthermore, it is possible to [omit a label expression](#omitting-a-label-expression).

#### Label disjunction

The bar operator (`|`) is a logical OR for specifying that a vertex or edge should match as long as it has at least one of the specified labels.

For example:

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT n.name, n.dob
  FROM MATCH (n:Person|University)
```

```
+--------------------------+
| name        | dob        |
+--------------------------+
| Riya        | 1995-03-20 |
| Kathrine    | 1994-01-15 |
| Lee         | 1996-01-29 |
| UC Berkeley | <null>     |
+--------------------------+
```

In the query above, `(n:Person|University)` matches vertices that have either the label `Person` or the label `University`. Note that in the result, there is a `<null>` value in the last row because the corresponding vertex does not have a property `dob`.

#### Omitting a label expression

Label expressions may be omitted so that the vertex or edge pattern will then match any vertex or edge.

For example:

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT n.name, n.dob
  FROM MATCH (n)
```

```
+--------------------------+
| name        | dob        |
+--------------------------+
| Riya        | 1995-03-20 |
| Kathrine    | 1994-01-15 |
| Lee         | 1996-01-29 |
| UC Berkeley | <null>     |
+--------------------------+
```

Note that the query gives the same results as before since both patterns `(n)` and `(n:Person|University)` match all the vertices in the example graph.

### Filter predicates

Filter predicates provide a way to further restrict which vertices or edges may bind to patterns.
A filter predicate is a boolean value expression and is placed in a [WHERE clause](#where-clause).

For example, "find all persons that have a date of birth (dob) greater than 1995-01-01":

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT n.name, n.dob
  FROM MATCH (n)
 WHERE n.dob > DATE '1995-01-01'
```

```
+---------------------+
| name   | dob        |
+---------------------+
| Riya   | 1995-03-20 |
| Lee    | 1996-01-29 |
+---------------------+
```

Above, the vertex pattern `(n)` initially matches all three Person vertices in the graph as well as the University vertex, since no label expression is specified.
However, the filter predicate `n.dob > DATE '1995-01-01'` filters out Kathrine because her date of birth is before 1995-01-01.
It also filters out UC Berkeley because the vertex does not have a property `dob` so that the reference `n.dob` returns null and since `null > DATE '1995-01-01'` is null (see [three-valued logic](#three-valued-logic)) the final result is null, which has the same affect as `false` and thus this candidate solution gets filtered out.

Another example is to "find people that Kathrine knows and that are old than her":

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT m.name AS name, m.dob AS dob
  FROM MATCH (n) -[e]-> (m)
 WHERE n.name = 'Kathrine' AND n.dob <= m.dob
```

```
+-------------------+
| name | dob        |
+-------------------+
| Riya | 1995-03-20 |
| Lee  | 1996-01-29 |
+-------------------+
```

Here, the pattern `(n) -[e]-> (m)` initially matches all the edges in the graph since it does not have any label expression.
However, the filter expression `n.name = 'Kathrine' AND n.dob <= m.dob` specifies that the source of the edge has a property `name` with the value `Kathrine` and that both the source and destination of the edge have properties `dob` such that the value for the source is smaller than or equal to the value for the destination.
Only two out of six edges satisfy this filter predicate.

### More complex patterns

More complex patterns are formed either by forming longer path patterns that consist of multiple edge patterns, or by specifying multiple comma-separated path patterns that share one or more vertex variables.

For example, "find people that Lee knows and that are a student at the same university as Lee":

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT p2.name AS friend, u.name AS university
  FROM MATCH (u:University) <-[:studentOf]- (p1:Person) -[:knows]-> (p2:Person) -[:studentOf]-> (u)
 WHERE p1.name = 'Lee'
```

```
+------------------------+
| friend   | university  |
+------------------------+
| Kathrine | UC Berkeley |
+------------------------+
```

Above, in the `MATCH` clause there is only one path pattern that consists of four vertex patterns and three edge patterns.
Note that the first and last vertex pattern both have the variable `u`. This means that they are the same variable rather than two different variables. Label expressions may be specified for neither, one, or both of the vertex patterns such that if there are multiple label expressions specified then they are simply evaluated in conjunction such that all expressions need to satisfy for a vertex to bind to the variable.

The same query as above may be expressed through multiple comma-separated path patterns, like this:

```sql
SELECT p2.name AS friend, u.name AS university
  FROM MATCH (p1:Person) -[:knows]-> (p2:Person)
     , MATCH (p1) -[:studentOf]-> (u:University)
     , MATCH (p2) -[:studentOf]-> (u)
 WHERE p1.name = 'Lee'
```

```
+------------------------+
| friend   | university  |
+------------------------+
| Kathrine | UC Berkeley |
+------------------------+
```

Here again, both occurrences of `u` are the same variable, as well as both occurrences of `p1` and both occurrences of `p2`.

### Binding an element multiple times

In a single solution it is allowed for a vertex or an edge to be bound to multiple variables at the same time.

For example, "find friends of friends of Lee" (friendship being defined by the presence of a 'knows' edge):

{% include image.html file="example_graphs/student_network.png"  %}

```sql
SELECT p1.name AS p1, p2.name AS p2, p3.name AS p3
  FROM MATCH (p1:Person) -[:knows]-> (p2:Person) -[:knows]-> (p3:Person)
 WHERE p1.name = 'Lee'
```

```
+-----------------------+
| p1  | p2       | p3   |
+-----------------------+
| Lee | Kathrine | Riya |
| Lee | Kathrine | Lee  |
+-----------------------+
````

Above, in the second solution, Lee is bound to both the variable `p1` and the variable `p3`. This solution is obtained since we can hop from Lee to Kathrine via the edge that is outgoing from Lee, and then we can hop back from Kathrine to Lee via the edge that is incoming to Lee.

If such binding of vertices to multiple variables is not desired, one can use either non-equality constraints or the [ALL_DIFFERENT predicate](#all_different-predicate).

For example, the predicate `p1 <> p3` in the query below adds the restriction that Lee, which has to bind to variable `p1`, cannot also bind to variable `p3`:

```sql
SELECT p1.name AS p1, p2.name AS p2, p3.name AS p3
  FROM MATCH (p1:Person) -[:knows]-> (p2:Person) -[:knows]-> (p3:Person)
 WHERE p1.name = 'Lee' AND p1 <> p3
```

```
+-----------------------+
| p1  | p2       | p3   |
+-----------------------+
| Lee | Kathrine | Riya |
+-----------------------+
```

An alternative is to use the [ALL_DIFFERENT predicate](#all_different-predicate), which can take any number of vertices or edges as input and specifies non-equality between all of them:

```sql
SELECT p1.name AS p1, p2.name AS p2, p3.name AS p3
  FROM MATCH (p1:Person) -[:knows]-> (p2:Person) -[:knows]-> (p3:Person)
 WHERE p1.name = 'Lee' AND ALL_DIFFERENT(p1, p3)
```

```
+-----------------------+
| p1  | p2       | p3   |
+-----------------------+
| Lee | Kathrine | Riya |
+-----------------------+
```

Besides vertices binding to multiple variables, it is also possible for edges to bind to multiple variables.

For example, "find two people that both know Riya":

```sql
SELECT p1.name AS p1, p2.name AS p2, e1 = e2
  FROM MATCH (p1:Person) -[e1:knows]-> (riya:Person)
     , MATCH (p2:Person) -[e2:knows]-> (riya)
 WHERE riya.name = 'Riya'
```

```
+-------------------------------+
| p1       | p2       | e1 = e2 |
+-------------------------------+
| Kathrine | Kathrine | true    |
+-------------------------------+
```

Above, the only solution has Kathrine bound to both variables `p1` and `p2` and the single edge between Kathrine and Riya is bound to both `e1` and `e2`, which is why `e1 = e2` in the `SELECT` clause returns `true`.

Again, if such bindings are not desired then one should add constraints like `e1 <> e2` or `ALL_DIFFERENT(e1, e2)` to the `WHERE` clause.

### Matching edges in any direction

Any-directed edge patterns match edges in the graph no matter if they are incoming or outgoing.

An example query with two any-directed edge patterns is:

```sql
SELECT *
  FROM MATCH (n) -[e1]- (m) -[e2]- (o)
```

Note that in case there are both incoming and outgoing data edges between two data vertices, there will be separate result bindings for each of the edges.

## Main query structure

The previous section on [writing simple queries](#writing-simple-queries) provided a basic introduction to graph pattern matching. The rest of this document introduces the different functionalities in more detail.

The following is the syntax of the main query structure:

```bash
PgqlStatement   ::=   <CreatePropertyGraph>
                    | <DropPropertyGraph>
                    | <Query>

Query           ::=   <SelectQuery>
                    | <ModifyQuery>

SelectQuery     ::= <SelectClause>
                    <FromClause>
                    <WhereClause>?
                    <GroupByClause>?
                    <HavingClause>?
                    <OrderByClause>?
                    <OffsetClause>?
                    ( <FetchFirstClause> | <LimitClause> )?
```

Details of the different clauses of a query can be found in the following sections:

 - The [SELECT clause](#select-clause) specifies what should be returned.
 - The [FROM clause](#from-clause) defines the graph pattern that is to be matched.
 - The [WHERE clause](#where-clause) specifies filters.
 - The [GROUP BY clause](#group-by-clause) allows for creating groups of results.
 - The [HAVING clause](#having-clause) allows for filtering entire groups of results.
 - The [ORDER BY clause](#order-by-clause) allows for sorting of results.
 - The [OFFSET clause](#offset-clause) specifies the number of rows to skip.
 - The [FETCH FIRST clause](#fetch-first-clause) and the [LIMIT clause](#limit-clause) are syntactic variations for limiting the number of rows.

## SELECT Clause

In a PGQL query, the SELECT clause defines the data entities to be returned in the result. In other words, the select clause defines the columns of the result table.

The following explains the syntactic structure of SELECT clause.

```bash
SelectClause        ::=   'SELECT' 'DISTINCT'? <SelectElement> ( ',' <SelectElement> )*
                        | 'SELECT' '*'

SelectElement       ::=   ExpAsVar
                        | AllProperties

ExpAsVar            ::= <ValueExpression> ( 'AS' <VariableName> )?

AllProperties       ::= <ElementReference> '.*' <AllPropertiesPrefix>?

AllPropertiesPrefix ::= 'PREFIX' <StringLiteral>
```

A `SELECT` clause consists of the keyword `SELECT` followed by either an optional `DISTINCT` modifier and comma-separated sequence of `<ExpAsVar>` ("expression as variable") elements, or, a special character star `*`. An `<ExpAsVar>` consists of:

- A `<ValueExpression>`.
- An optional `<VariableName>`, specified by appending the keyword `AS` and the name of the variable.

Consider the following example:

```sql
SELECT n, m, n.age AS age
  FROM MATCH (n:Person) -[e:friend_of]-> (m:Person)
```

Per each matched subgraph, the query returns two vertices `n` and `m` and the value for property age of vertex `n`.  Note that edge `e` is omitted from the result even though it is used for describing the pattern.

The `DISTINCT` modifier allows for filtering out duplicate results. The operation applies to an entire result row, such that rows are only considered duplicates of each other if they contain the same set of values.

### Assigning variable name to Select Expression

It is possible to assign a variable name to any of the selection expression, by appending the keyword `AS` and a variable name. The variable name is used as the column name of the result set. In addition, the variable name can be later used in the `ORDER BY` clause. See the related section later in this document.

```sql
  SELECT n.age * 2 - 1 AS pivot, n.name, n
    FROM MATCH (n:Person) -> (m:Car)
ORDER BY pivot
```

### SELECT *

`SELECT *` is a special `SELECT` clause. The semantic of `SELECT *` is to select all the variables in the graph pattern.

Consider the following query:

```sql
SELECT *
FROM MATCH (n:Person) -> (m) -> (w),
     MATCH (n) -> (w) -> (m)
```

This query is semantically equivalent to:

```sql
SELECT n, m, w
FROM MATCH (n:Person) -> (m) -> (w),
     MATCH (n) -> (w) -> (m)
```

`SELECT *` is not allowed when the graph pattern has zero variables. This is the case when all the vertices and edges in the pattern are anonymous (e.g. `MATCH () -> (:Person)`).
Furthermore, `SELECT *` in combination with `GROUP BY` is not allowed.

### Selecting All Properties

Through `SELECT v.*` one can select all properties of the vertices or edges that bind to the variable `v`.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT label(n), n.*
    FROM MATCH (n)
ORDER BY "number", "name"
```

```
+-----------------------------+
| label(n) | number | name    |
+-----------------------------+
| Account  | 1001   | <null>  |
| Account  | 2090   | <null>  |
| Account  | 8021   | <null>  |
| Account  | 10039  | <null>  |
| Person   | <null> | Camille |
| Person   | <null> | Liam    |
| Person   | <null> | Nikita  |
| Company  | <null> | Oracle  |
+-----------------------------+
```

Label expressions are taken into account such that only properties are selected that belong to the specified vertex or
edge labels:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT label(n), n.*
FROM MATCH (n:Person)
ORDER BY "name"
```

```
+--------------------+
| label(n) | name    |
+--------------------+
| Person   | Camille |
| Person   | Liam    |
| Person   | Nikita  |
+--------------------+
```

A `PREFIX` can be specified to avoid duplicate column names in case all properties of multiple vertex or edge variables are selected.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT n.* PREFIX 'n_', e.* PREFIX 'e_', m.* PREFIX 'm_'
FROM MATCH (n:Account) -[e:transaction]-> (m:Account)
ORDER BY "e_amount"
```

```
+--------------------------------+
| n_number | e_amount | m_number |
+--------------------------------+
| 10039    | 1000.0   | 8021     |
| 8021     | 1500.3   | 1001     |
| 8021     | 3000.7   | 1001     |
| 2090     | 9900.0   | 10039    |
| 1001     | 9999.5   | 2090     |
+--------------------------------+
```

## FROM Clause

In a PGQL query, the `FROM` clause defines the graph pattern to be matched.

Syntactically, a `FROM` clause is composed of the keyword `FROM` followed by a comma-separated sequence of table expressions.
Each table expression is either a [MATCH clause](#match-clause), a [GRAPH_TABLE operator](#graph_table-operator) or a [LATERAL subquery](#lateral-subqueries).

```bash
FromClause             ::= 'FROM' <TableExpression> ( ',' <TableExpression> )*

TableExpression        ::=   <MatchClause>
                           | <GraphTable>
                           | <LateralSubquery>
```

## MATCH Clause

```bash
MatchClause               ::= 'MATCH' ( <PathPattern> | <ParenthesizedGraphPattern> ) <OnClause>? <RowsPerMatch>?

ParenthesizedGraphPattern ::= '(' <GraphPattern> ')'

GraphPattern              ::= <PathPattern> ( ',' <PathPattern> )*

PathPattern               ::=   <BasicPathPattern>
                              | <AnyPathPattern>
                              | <AnyShortestPathPattern>
                              | <AllShortestPathPattern>
                              | <ShortestKPathPattern>
                              | <AnyCheapestPathPattern>
                              | <CheapestKPathPattern>
                              | <AllPathPattern>

BasicPathPattern          ::= <VertexPattern> ( <EdgePattern> <VertexPattern> )*

VertexPattern             ::= '(' <VariableSpecification> ')'

EdgePattern               ::=   <OutgoingEdgePattern>
                              | <IncomingEdgePattern>
                              | <AnyDirectedEdgePattern>

OutgoingEdgePattern       ::=   '->'
                              | '-[' <VariableSpecification> ']->'

IncomingEdgePattern       ::=   '<-'
                              | '<-[' <VariableSpecification> ']-'

AnyDirectedEdgePattern ::=      '-'
                              | '-[' <VariableSpecification> ']-'

VariableSpecification     ::= <VariableName>? <LabelPredicate>?

VariableName              ::= <Identifier>
```

A path pattern that describes a partial topology of the subgraph pattern. In other words, a topology constraint describes some connectivity relationships between vertices and edges in the pattern, whereas the whole topology of the pattern is described with one or multiple topology constraints.

A topology constraint is composed of one or more vertices and relations, where a relation is either an edge or a path. In a query, each vertex or edge is (optionally) associated with a variable, which is a symbolic name to reference the vertex or edge in other clauses. For example, consider the following topology constraint:

```sql
(n) -[e]-> (m)
```

The above example defines two vertices (with variable names `n` and `m`), and an edge (with variable name `e`) between them. Also the edge is directed such that the edge `e` is an outgoing edge from vertex `n`.

More specifically, a vertex term is written as a variable name inside a pair of parenthesis `()`. An edge term is written as a variable name inside a square bracket `[]` with two dashes and an inequality symbol attached to it – which makes it look like an arrow drawn in ASCII art. An edge term is always connected with two vertex terms as for the source and destination vertex of the edge; the source vertex is located at the tail of the ASCII arrow and the destination at the head of the ASCII arrow.

There can be multiple path patterns in the `FROM` clause of a PGQL query. Semantically, all constraints are conjunctive – that is, each matched result should satisfy every constraint in the `FROM` clause.

### ON clause

The `ON` clause is an optional clause that belongs to the `MATCH` clause and specifies the name of the graph to match the pattern on.

The syntax is:

```bash
OnClause       ::= 'ON' <GraphReference>

GraphReference ::= <GraphName>
```

For example:

```sql
SELECT p.first_name, p.last_name
FROM MATCH (p:Person) ON my_graph
ORDER BY p.first_name, p.last_name
```

Above, the pattern `(p:Person)` is matched on graph `my_graph`.

#### Default graphs

The `ON` clauses may be omitted if a "default graph" has been provided.
PGQL itself does not (yet) provide syntax for specifying a default graph, but Java APIs for invoking PGQL queries typically provide mechanisms for it:
 - Oracle's in-memory analytics engine PGX has the API `PgxGraph.queryPgql("SELECT ...")` such that the default graph corresponds to `PgxGraph.getName()` such that `ON` clauses can be omitted from queries.
 - Oracle's PGQL-on-RDBMS provides the API `PgqlConnection.setGraph("myGraph")` for setting the default graph such that the `ON` clauses can be omitted from queries.

If a default graph is provided then the `ON` clause can be omitted:

```sql
SELECT p.first_name, p.last_name
FROM MATCH (p:Person)
ORDER BY p.first_name, p.last_name
```

#### Querying multiple graphs

Although each `MATCH` clause can have its own `ON` clause, PGQL does not support querying of multiple graphs in a single query.
Therefore, it is not possible for two `MATCH` clauses to have `ON` clauses with different graph names.

### Repeated variables

There can be multiple topology constraints in the `FROM` clause of a PGQL query. In such a case, vertex terms that have the same variable name correspond to the same vertex entity. For example, consider the following two lines of topology constraints:

```sql
SELECT *
 FROM MATCH (n) -[e1]-> (m1),
      MATCH (n) -[e2]-> (m2)
```

Here, the vertex term `(n)` in the first constraint indeed refers to the same vertex as the vertex term `(n)` in the second constraint. It is an error, however, if two edge terms have the same variable name, or, if the same variable name is assigned to an edge term as well as to a vertex term in a single query.

### Alternatives for specifying graph patterns

There are various ways in which a particular graph pattern can be specified.

First, a single path pattern can be written as a chain of edge terms such that two consecutive edge terms share the common vertex term in between. For example:

```sql
SELECT *
FROM MATCH (n1) -[e1]-> (n2) -[e2]-> (n3) -[e3]-> (n4)
```

The above graph pattern is equivalent to the graph pattern specified by the following set of comma-separate path patterns:

```sql
SELECT *
FROM MATCH (n1) -[e1]-> (n2),
     MATCH (n2) -[e2]-> (n3),
     MATCH (n3) -[e3]-> (n4)
```

Second, it is allowed to reverse the direction of an edge in the pattern, i.e. right-to-left instead of left-to-right. Therefore, the following is a valid graph pattern:

```sql
SELECT *
FROM MATCH (n1) -[e1]-> (n2) <-[e2]- (n3)
```

Please mind the edge directions in the above query – vertex `n2` is a common outgoing neighbor of both vertex `n1` and vertex `n3`.

Third, it is allowed to ommitg variable names if the particular vertex or edge does not need to be referenced in any of the other clauses (e.g. `SELECT` or `ORDER BY`). When the variable name is omitted, the vertex or edge is an "anonymous" vertex or edge.

Syntactically, for vertices, this result in an empty pair of parenthesis. In case of edges, the whole square bracket is omitted in addition to the variable name.

The following table summarizes these short cuts.

syntax form | example
--- | ---
basic form | `(n) -[e]-> (m)`
omit variable name of the source vertex | `() -[e]-> (m)`
omit variable name of the destination vertex | `(n) -[e]-> ()`
omit variable names in both vertices | `() -[e]-> ()`
omit variable name in edge | `(n) -> (m)`

### Disconnected graph patterns

In the case the `MATCH` clause contains two or more disconnected graph patterns (i.e. groups of vertices and relations that are not connected to each other), the different groups are matched independently and the final result is produced by taking the Cartesian product of the result sets of the different groups. The following is an example:

```sql
SELECT *
FROM MATCH (n1) -> (m1),
     MATCH (n2) -> (m2)
```

Here, vertices `n2` and `m2` are not connected to vertices `n1` and `m1`, resulting in a Cartesian product.

### Label expression

In the property graph model, vertices and edge may have labels, which are arbitrary (character) strings. Typically, labels are used to encode types of entities. For example, a graph may contain a set of vertices with the label `Person`, a set of vertices with the label `Movie`, and, a set of edges with the label `likes`. A label expression specifies that a vertex or edge only matches if it has ony of the specified labels. A label expression starts with either a colon (`:`) or the keyword `IS`, followed by one or more labels that are separate by a vertical bar (`|`).

The corresponding grammar is:

```bash
LabelExpression  ::= <ColonOrIsKeyword> <Label> ( '|' <Label> )*

ColonOrIsKeyword ::=    ':'
                     || 'IS'
```

Colons and `IS` keywords can be used interchangeably.

Take the following example:

```sql
SELECT *
FROM MATCH (x:Person) -[e IS likes|knows]-> (y:Person)
```

Here, we specify that vertices `x` and `y` have the label `Person` and that the edge `e` has the label `likes` or the label `knows`.

A label expression can be specified even when a variable is omitted. For example:

```sql
SELECT *
FROM MATCH (IS Person) -[:likes|knows]-> (IS Person)
```

There are also built-in functions and predicates available for labels:

 - [label(element)](#label-function) returns the label of a vertex or edge in the case the vertex/edge has only a single label.
 - [labels(element)](#labels-function) returns the set of labels of a vertex or edge in the case the vertex/edge has multiple labels.
 - [element IS [NOT] LABELED label)](#labeled-predicate) returns `true` or `false` depending on if the vertex or edge has the specified label.

## WHERE Clause

Filters are applied after pattern matching to remove certain solutions. A filter takes the form of a boolean value expression which typically involves certain property values of the vertices and edges in the graph pattern.

The syntax is:


```bash
WhereClause ::= 'WHERE' <ValueExpression>
```

For example:

```sql
SELECT y.name
  FROM MATCH (x) -> (y)
 WHERE x.name = 'Jake'
   AND y.age > 25
```

Here, the first filter describes that the vertex `x` has a property `name` and its value is `Jake`. Similarly, the second filter describes that the vertex `y` has a property `age` and its value is larger than `25`. Here, in the filter, the dot (`.`) operator is used for property access. For the detailed syntax and semantic of expressions, see [Functions and Expressions](#functions-and-expressions).

Note that the ordering of constraints does not have an affect on the result, such that query from the previous example is equivalent to:

```sql
SELECT y.name
 FROM MATCH (x) -> (y)
WHERE y.age > 25
  AND x.name = 'Jake'
```

## GRAPH_TABLE Operator

The `GRAPH_TABLE` operator provides a SQL-compatible way to express graph queries, conforming to the [SQL extension for property graph queries](https://www.iso.org/standard/79473.html).

The syntax is:

```bash
GraphTable      ::= 'GRAPH_TABLE' '(' <GraphReference> 'MATCH' <GraphPattern> <WhereClause>? <GraphTableShape> ')'

GraphTableShape ::= <RowsPerMatch>? <ColumnsClause>

ColumnsClause   ::= 'COLUMNS' '(' <ExpAsVar> ( ',' <ExpAsVar> )* ')'
```

A `GRAPH_TABLE` has the following parts:
 - A graph reference that references the graph to perform the pattern matching on.
 - A `MATCH` keyword.
 - A graph pattern, which is a comma-separted list of path patterns.
 - An optional [WHERE clause](#where-clause).
 - A graph table shape.

The graph table shape defines how the result of pattern matching should be transformed into tabular form.
It has two parts:
 - An optional [Number of rows per match](#number-of-rows-per-match).
 - A mandatory `COLUMNS` clause. The `COLUMNS` clause allows for defining a projection that transforms the result of graph pattern matching into a regular table that no longer contains graph objects like vertices and edges but instead regular data values only.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT *
FROM GRAPH_TABLE ( financial_transactions
       MATCH (n IS Person) <-[IS owner]- (a1 IS Account),
             (a1) -[e IS transaction]- (a2),
             (a2) -[IS owner]-> (m IS person)
       WHERE n.name = 'Camille'
       COLUMNS ( m.name, e.amount,
                 CASE
                   WHEN a1 IS SOURCE OF e THEN 'Outgoing transaction'
                   ELSE 'Incoming transaction'
                 END AS transaction_type )
     )
ORDER BY amount DESC
```

```
+----------------------------------------+
| name   | amount | transaction_type     |
+----------------------------------------+
| Liam   | 9900.0 | Incoming transaction |
| Nikita | 1000.0 | Outgoing transaction |
+----------------------------------------+
```

An example with [horizontal aggregation](#horizontal-aggregation) is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT *
FROM GRAPH_TABLE ( financial_transactions
       MATCH ALL SIMPLE PATHS (a IS Account) -[e IS transaction]->+ (a)
       WHERE a.number = 10039
       COLUMNS ( LISTAGG(e.amount, ', ') AS amounts_along_path,
                 SUM(e.amount) AS total_amount )
     )
ORDER BY total_amount DESC
```

```
+-----------------------------------------------+
| amounts_along_path             | total_amount |
+-----------------------------------------------+
| 1000.0, 3000.7, 9999.5, 9900.0 | 23900.2      |
| 1000.0, 1500.3, 9999.5, 9900.0 | 22399.8      |
+-----------------------------------------------+
```

An example with [ONE ROW PER STEP](#one-row-per-step) is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT *
FROM GRAPH_TABLE ( financial_transactions
       MATCH ALL SIMPLE PATHS (a IS Account) -[IS transaction]->+ (a)
       WHERE a.number = 10039
       ONE ROW PER STEP ( v1, e, v2 )
       COLUMNS ( MATCHNUM() AS match_num, ELEMENT_NUMBER(e) AS elem_num,
                 v1.number AS account1, e.amount, v2.number AS account2 )
     )
ORDER BY match_num, elem_num
```

```
+-----------------------------------------------------+
| match_num | elem_num | account1 | amount | account2 |
+-----------------------------------------------------+
| 0         | 2        | 10039    | 1000.0 | 8021     |
| 0         | 4        | 8021     | 1500.3 | 1001     |
| 0         | 6        | 1001     | 9999.5 | 2090     |
| 0         | 8        | 2090     | 9900.0 | 10039    |
| 1         | 2        | 10039    | 1000.0 | 8021     |
| 1         | 4        | 8021     | 3000.7 | 1001     |
| 1         | 6        | 1001     | 9999.5 | 2090     |
| 1         | 8        | 2090     | 9900.0 | 10039    |
+-----------------------------------------------------+
```

The following features are disallowed if `GRAPH_TABLE` is used anywhere in a PGQL query:

| Disallowed in combination with `GRAPH_TABLE`                        | What to use instead                                                                |
|---------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [MATCH clause](#match-clause) as top-level element in a FROM clause | `GRAPH_TABLE` operator with MATCH clause inside                                    |
| Colon (`:`) in [label expressions](#label-expressions)              | `IS` keyword                                                                       |
| [LIMIT clause](#limit-clause)                                       | [FETCH FIRST clause](#fetch-first-clause)                                          |
| [ID function](#id-function)                                         | [VERTEX_ID/EDGE_ID function](#vertex-idedge-id-function)                           |
| [LABEL](#label-function) and [LABELS](#labels-function) functions   | [LABELED predicate](#labeled-predicate)                                            |
| Vertex [not] equals or edge [not] equals (e.g. `v1 <> v2`)          | [ALL_DIFFERENT Predicate](#all_different-predicate) (e.g. `ALL_DIFFERENT(v1, v2)`  |
| Aggregation with vertex/edge input (e.g. `COUNT(e)`)                | [VERTEX_ID/EDGE_ID function](#vertex_idedge_id-function) (e.g. COUNT(edge_id(e))   |
| [JAVA_REGEXP_LIKE Function](#java_regexp_like-function)             | `REGEXP_LIKE`, `REGEXP_INSTR` or user-defined function                             |
| [All properties PREFIX](#AllPropertiesPrefix)                       | To obtain unique column names, write out all properties and provide unique aliases |
| [Graph Modification](#graph-modification)                           | PGQL query without `GRAPH_TABLE`                                                   |

# Variable-Length Paths

[Graph Pattern Matching](#graph-pattern-matching) introduced how "fixed-length" patterns can be matched.
Fixed-length patterns match a fixed number of vertices and edges such that every solution (every row) has the same number of vertices and edges.

However, through the use of quantifiers (introduced below) it is is possible to match "variable-length" paths such as shortest paths.
Variable-length path patterns match a variable number of vertices and edges such that different solutions (different rows) potentially have different numbers of vertices and edges.

## Overview of Path Finding Goals

| goal         | matches              | limitations on quantifier                  |
|--------------|----------------------|--------------------------------------------|
| ANY          | any path             | no limitations                             |
| ANY SHORTEST | any shortest path    | no limitations                             |
| ALL SHORTEST | all shortest paths   | no limitations                             |
| SHORTEST k   | shortest k paths     | no limitations                             |
| ANY CHEAPEST | any cheapest path    | no limitations                             |
| CHEAPEST k   | cheapest k paths     | no limitations                             |
| ALL          | all paths            | requires an upper bound on the path length |

## Quantifiers

Quantifiers allow for matching variable-length paths by specifying lower and upper limits on the number of times a pattern is allowed to match.

The syntax is:

```bash
GraphPatternQuantifier     ::=   <ZeroOrMore>
                               | <OneOrMore>
                               | <Optional>
                               | <ExactlyN>
                               | <NOrMore>
                               | <BetweenNAndM>
                               | <BetweenZeroAndM>

ZeroOrMore                 ::= '*'

OneOrMore                  ::= '+'

Optional                   ::= '?'

ExactlyN                   ::= '{' <UNSIGNED_INTEGER> '}'

NOrMore                    ::= '{' <UNSIGNED_INTEGER> ',' '}'

BetweenNAndM               ::= '{' <UNSIGNED_INTEGER> ',' <UNSIGNED_INTEGER> '}'

BetweenZeroAndM            ::= '{' ',' <UNSIGNED_INTEGER> '}'
```

The meaning of the different quantifiers is:

| quantifier | meaning                              | matches                                                                                                                             |
|------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| *          | zero (0) or more                     | a path that connects the source and destination of the path by zero or more matches of a given pattern                             |
| +          | one (1) or more                      | a path that connects the source and destination of the path by one or more matches of a given pattern                              |
| ?          | zero or one (1), i.e. "optional"     | a path that connects the source and destination of the path by zero or one matches of a given pattern                              |
| { n }      | exactly _n_                          | a path that connects the source and destination of the path by exactly _n_ matches of a given pattern                              |
| { n, }     | _n_ or more                          | a path that connects the source and destination of the path by at least _n_ matches of a given pattern                             |
| { n, m }   | between _n_ and _m_ (inclusive)      | a path that connects the source and destination of the path by at least _n_ and at most _m_ (inclusive) matches of a given pattern |
| { , m }    | between zero (0) and _m_ (inclusive) | a path that connects the source and destination of the path by at least 0 and at most _m_ (inclusive) matches of a given pattern   |

All paths are considered, even the ones that contain a vertex or edge multiple times. In other words, cycles are permitted.

An example is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT a.number AS a,
       b.number AS b,
       COUNT(e) AS pathLength,
       ARRAY_AGG(e.amount) AS amounts
  FROM MATCH ANY SHORTEST (a:Account) -[e:transaction]->* (b:Account)
 WHERE a.number = 10039 AND b.number = 2090
```

```
+------------------------------------------------------+
| a     | b    | pathLength | amounts                  |
+------------------------------------------------------+
| 10039 | 2090 | 3          | [1000.0, 1500.3, 9999.5] |
+------------------------------------------------------+
```

Above, we use the quantifier `*` to find a shortest path from account `10039` to account `2090`, following only `transaction` edges.
Shortest path finding is explained in more detail in [Shortest Path](#shortest-path). `COUNT(e)` and `ARRAY_AGG(e.amount)` are horizontal aggregations which are explained in [Horizontal Aggregation](#horizontal-aggregation).

Another example is:

```sql
  SELECT LISTAGG(x.number, ', ') AS account_numbers, SUM(e.amount) AS total_amount
    FROM MATCH SHORTEST 4 PATHS (a:Account) ((x:Account) <-[e:transaction]-)+ (a)
   WHERE a.number = 10039
ORDER BY SUM(e.amount)
```

```
+-----------------------------------------------------------------+
| account_numbers                                  | total_amount |
+-----------------------------------------------------------------+
| 10039, 2090, 1001, 8021                          | 22399.8      |
| 10039, 2090, 1001, 8021                          | 23900.2      |
| 10039, 2090, 1001, 8021, 10039, 2090, 1001, 8021 | 44799.6      |
| 10039, 2090, 1001, 8021, 10039, 2090, 1001, 8021 | 46300.0      |
+-----------------------------------------------------------------+
```

Above, we use the quantifier `+` to find the shortest 4 paths from account `10039` back to itself, following only incoming `transaction` edges.
Quantifier `+` will make sure not to include the empty path, which is the path with zero edges that only contains the vertex corresponding to account `10039`.
We use the `LISTAGG` aggregate to retrieve the account numbers and the `SUM` aggregate to retrieve the total of the transaction amounts along each path.

## Any Path

`ANY` is used to find any (arbitrary) path between a pair of source-destination vertices.

Two typical uses are:

 - Testing for the _existence_ of a path between a pair of vertices without caring about the actual data along the paths.
 - Matching a path in case of tree-structured graphs or other types of graph structures for which it is known that only single paths exist between pairs of vertices.

The syntax for matching any path is:

```bash
AnyPathPattern ::=                       'ANY' ( 'PATH' | 'PATHS' )?
                                           <SourceVertexPattern>
                                             <QuantifiedPathPatternPrimary>
                                               <DestinationVertexPattern>
                                       | 'ANY' ( 'PATH' | 'PATHS' )? '('
                                           <SourceVertexPattern>
                                             <QuantifiedPathPatternPrimary>
                                               <DestinationVertexPattern> ')'

PathOrPaths                        ::=   'PATH'
                                       | 'PATHS'

SourceVertexPattern                ::= <VertexPattern>

DestinationVertexPattern           ::= <VertexPattern>

QuantifiedPathPatternPrimary       ::= <PathPatternPrimary> <GraphPatternQuantifier>?

PathPatternPrimary                 ::=   <EdgePattern>
                                       | <ParenthesizedPathPatternExpression>

ParenthesizedPathPatternExpression ::= '(' <VertexPattern>? <EdgePattern> <VertexPattern>?
                                             <WhereClause>?
                                               <CostClause>? ')'
```

An example where we test for path existence is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT dst.number
    FROM MATCH ANY (src:Account) -[e]->+ (dst:Account)
   WHERE src.number = 8021
ORDER BY dst.number
```

```
+--------+
| number |
+--------+
| 1001   |
| 2090   |
| 8021   |
| 10039  |
+--------+
```

An example where we return data along the path is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT dst.number, LISTAGG(e.amount, ' + ') || ' = ', SUM(e.amount)
    FROM MATCH ANY (src:Account) -[e]->+ (dst:Account)
   WHERE src.number = 8021
ORDER BY dst.number
```

```
+---------------------------------------------------------------+
| number | LISTAGG(e.amount, ' + ') || ' = '    | SUM(e.amount) |
+---------------------------------------------------------------+
| 1001   | 1500.3 =                             | 1500.3        |
| 2090   | 1500.3 + 9999.5 =                    | 11499.8       |
| 8021   | 1500.3 + 9999.5 + 9900.0 + 1000.0 =  | 22399.8       |
| 10039  | 1500.3 + 9999.5 + 9900.0 =           | 21399.8       |
+---------------------------------------------------------------+
```

Note that above, there is always only a single path per source-destination pair (there are four such pairs).
And it is arbitrary which path is match.
In this example, all four paths happen to contain the transaction edge with amount `1500.30` instead of the one with amount `3000.80`.

## Shortest Path

Shortest path finding allows for finding paths with a minimal number of hops.
Given a pair of vertices, there are different kinds of shortest paths that can be obtained:

 - [any shortest path](#any-shortest-path)
 - [all shortest paths](#all-shortest-paths)
 - [shortest-k-paths](#shortest-k-paths)

### Any Shortest Path

`ANY SHORTEST` allows for matching a shortest path (i.e. minimal number of edges) between a source vertex and a destination vertex. In case multiple shortest paths exist, an arbitrary one is retrieved.

The syntax is:

```bash
AnyShortestPathPattern ::=   'ANY' 'SHORTEST' ( 'PATH' | 'PATHS' )?
                               <SourceVertexPattern>
                                 <QuantifiedPathPatternPrimary>
                                   <DestinationVertexPattern>
                           | 'ANY' 'SHORTEST' ( 'PATH' | 'PATHS' )? '('
                               <SourceVertexPattern>
                                 <QuantifiedPathPatternPrimary>
                                   <DestinationVertexPattern> ')'
```

For example:

```sql
SELECT src, SUM(e.weight), dst
  FROM MATCH ANY SHORTEST (src) -[e]->* (dst)
 WHERE src.age < dst.age
```

Another example is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT COUNT(e) AS num_hops
       , p1.name AS start
       , ARRAY_AGG ( CASE
                       WHEN dst IS LABELED Account
                         THEN CAST(dst.number AS STRING)
                       ELSE dst.name
                     END
                   ) AS path
    FROM MATCH ANY SHORTEST (p1:Person) (-[e]- (dst))* (p2:Person)
   WHERE p1.name = 'Camille' AND p2.name = 'Liam'
ORDER BY num_hops
```

```
+------------------------------------------+
| num_hops | start   | path                |
+------------------------------------------+
| 3        | Camille | [10039, 2090, Liam] |
+------------------------------------------+
```

Filters on vertices and edges along paths can be specified by adding a `WHERE` clause inside the quantified pattern.

For example, the following query matches a shortest path (if one exists) such that each edge along the path has a property `weight` with a value greater than `10`:

```sql
SELECT src, ARRAY_AGG(e.weight), dst
  FROM MATCH ANY SHORTEST (src) (-[e]-> WHERE e.weight > 10)* (dst)
```

Note that this is different from a `WHERE` clause that is placed outside of the quantified pattern:

```sql
SELECT src, ARRAY_AGG(e.weight), dst
  FROM MATCH ANY SHORTEST (src) -[e]->* (dst) WHERE SUM(e.cost) < 100
```

Here, the filter is applied only _after_ a shortest path is matched such that if the `WHERE` condition is not satisfied, the path is filtered out and no other path is considered even though another path may exist that does satisfy the `WHERE` condition.

### All Shortest Path

Given a pair of source-destination vertices, `ALL SHORTEST` path matches all shortest paths between the two vertices.
In contrast to `ANY SHORTEST`, `ALL SHORTEST` will return a deterministic result as it will include all shortest paths instead of an arbitrary shortest path.

The syntax is:

```bash
AllShortestPathPattern ::=   'ALL' 'SHORTEST' ( 'PATH' | 'PATHS' )?
                               <SourceVertexPattern>
                                 <QuantifiedPathPatternPrimary>
                                   <DestinationVertexPattern>
                           | 'ALL' 'SHORTEST' ( 'PATH' | 'PATHS' )?
                               '(' <SourceVertexPattern>
                                  <QuantifiedPathPatternPrimary>
                                    <DestinationVertexPattern> ')'
```

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT LISTAGG(e.amount, ' + ') || ' = ', SUM(e.amount) AS total_amount
    FROM MATCH ALL SHORTEST (a:Account) -[e:transaction]->* (b:Account)
   WHERE a.number = 10039 AND b.number = 2090
ORDER BY total_amount
```

```
+--------------------------------------------------+
| LISTAGG(e.amount, ' + ') || ' = ' | total_amount |
+--------------------------------------------------+
| 1000.0 + 1500.3 + 9999.5 =        | 12499.8      |
| 1000.0 + 3000.7 + 9999.5 =        | 14000.2      |
+--------------------------------------------------+
```

### Shortest K Paths

`SHORTEST k PATHS` matches the shortest k paths for each pair of source and destination vertices. Aggregations can then be computed over their vertices/edges.

The syntax is:

```bash
ShortestKPathPattern ::=   'SHORTEST' <KValue> ( 'PATH' | 'PATHS' )?
                             <SourceVertexPattern>
                               <QuantifiedPathPatternPrimary>
                                 <DestinationVertexPattern>
                         | 'SHORTEST' <KValue> ( 'PATH' | 'PATHS' )? '('
                             <SourceVertexPattern>
                               <QuantifiedPathPatternPrimary>
                                 <DestinationVertexPattern> ')'

KValue                  ::= <UNSIGNED_INTEGER>
```

For example the following query will output the sum of the edge weights along each of the shortest 3 paths between
each of the matched source and destination pairs:

```sql
SELECT src, SUM(e.weight), dst
  FROM MATCH SHORTEST 3 PATHS (src) -[e]->* (dst)
 WHERE src.age < dst.age
```

Notice that the sum aggregation is computed for each matching path. In other words, the number of rows returned by the
query is equal to the number of paths that match, which is at most three times the number of possible source-destination pairs.


The `ARRAY_AGG` construct allows users to output properties of edges/vertices along the path. For example, in the following query:

```sql
SELECT src, ARRAY_AGG(e.weight), ARRAY_AGG(v1.age), ARRAY_AGG(v2.age), dst
  FROM MATCH SHORTEST 3 PATHS (src) ((v1) -[e]-> (v2))* (dst)
 WHERE src.age < dst.age
```

the `ARRAY_AGG(e.weight)` outputs a list containing the weight property of all the edges along the path,

the `ARRAY_AGG(v1.cost)` outputs a list containing the age property of all the vertices along the path except the last one,

the `ARRAY_AGG(v2.cost)` outputs a list containing the age property of all the vertices along the path except the first one.


Users can also compose shortest path constructs with other matching operators:

```sql
SELECT ARRAY_AGG(e1.weight), ARRAY_AGG(e2.weight)
  FROM MATCH (start) -> (src)
     , MATCH SHORTEST 3 PATHS (src) (-[e1]->)* (mid)
     , MATCH ANY SHORTEST (mid) (-[e2]->)* (dst)
     , MATCH (dst) -> (end)
```

Another example is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT COUNT(e) AS num_hops
       , SUM(e.amount) AS total_amount
       , ARRAY_AGG(e.amount) AS amounts_along_path
    FROM MATCH SHORTEST 7 PATHS (a:Account) -[e:transaction]->* (b:Account)
   WHERE a.number = 10039 AND a = b
ORDER BY num_hops, total_amount
```

```
+--------------------------------------------------------------------------------------------+
| num_hops | total_amount | amounts_along_path                                               |
+--------------------------------------------------------------------------------------------+
| 0        | <null>       | <null>                                                           |
| 4        | 22399.8      | [1000.0, 1500.3, 9999.5, 9900.0]                                 |
| 4        | 23900.2      | [1000.0, 3000.7, 9999.5, 9900.0]                                 |
| 8        | 44799.6      | [1000.0, 1500.3, 9999.5, 9900.0, 1000.0, 1500.3, 9999.5, 9900.0] |
| 8        | 46300.0      | [1000.0, 1500.3, 9999.5, 9900.0, 1000.0, 3000.7, 9999.5, 9900.0] |
| 8        | 46300.0      | [1000.0, 3000.7, 9999.5, 9900.0, 1000.0, 1500.3, 9999.5, 9900.0] |
| 8        | 47800.4      | [1000.0, 3000.7, 9999.5, 9900.0, 1000.0, 3000.7, 9999.5, 9900.0] |
+--------------------------------------------------------------------------------------------+
```

Note that above, we matched a path with zero edges (the first result) and we also matched four paths (the last four results) that visit the same edges multiple times.
The following example shows how such paths could be filtered out, such that we only keep paths that have at least one edge and that do not visit an edge multiple times:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT COUNT(e) AS num_hops
       , SUM(e.amount) AS total_amount
       , ARRAY_AGG(e.amount) AS amounts_along_path
    FROM MATCH SHORTEST 7 PATHS (a:Account) -[e:transaction]->* (b:Account)
   WHERE a.number = 10039 AND a = b AND COUNT(DISTINCT e) = COUNT(e) AND COUNT(e) > 0
ORDER BY num_hops, total_amount
```

```
+------------------------------------------------------------+
| num_hops | total_amount | amounts_along_path               |
+------------------------------------------------------------+
| 4        | 22399.8      | [1000.0, 1500.3, 9999.5, 9900.0] |
| 4        | 23900.2      | [1000.0, 3000.7, 9999.5, 9900.0] |
+------------------------------------------------------------+
```

## Cheapest Path

Cheapest path finding allows for finding paths based on a cost function.
Given a pair of vertices, [single cheapest path finding](#single-cheapest-path) allows for finding a single cheapest path,
While [cheapest k path finding](#cheapest-k-paths) allows for finding K cheapest paths where paths for which paths with increasing cost are matched.

### Any Cheapest Path

The `CHEAPEST` construct allows for finding a cheapest path based on an arbitrary `COST` function.

The syntax is:

```
AnyCheapestPathPattern ::=   'ANY' 'CHEAPEST' ( 'PATH' | 'PATHS' )?
                               <SourceVertexPattern>
                                 <QuantifiedPathPatternPrimary>
                                   <DestinationVertexPattern>
                           | 'ANY' 'CHEAPEST' ( 'PATH' | 'PATHS' )? '('
                               <SourceVertexPattern>
                                 <QuantifiedPathPatternPrimary>
                                   <DestinationVertexPattern> ')'

CostClause          ::= 'COST' ValueExpression
```

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT COUNT(e) AS num_hops
     , SUM(e.amount) AS total_amount
     , ARRAY_AGG(e.amount) AS amounts_along_path
  FROM MATCH ANY CHEAPEST (a:Account) (-[e:transaction]-> COST e.amount)* (b:Account)
 WHERE a.number = 10039 AND b.number = 2090
```

```
+----------------------------------------------------+
| num_hops | total_amount | amounts_along_path       |
+----------------------------------------------------+
| 3        | 12499.8      | [1000.0, 1500.3, 9999.5] |
+----------------------------------------------------+
```

The following example with `CHEAPEST` contains an any-directed edge pattern (`-[e:transaction]-`):

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT COUNT(e) AS num_hops
     , SUM(e.amount) AS total_amount
     , ARRAY_AGG(e.amount) AS amounts_along_path
  FROM MATCH ANY CHEAPEST (a:Account) (-[e:transaction]- COST e.amount)* (b:Account)
 WHERE a.number = 10039 AND b.number = 2090
```

```
+----------------------------------------------+
| num_hops | total_amount | amounts_along_path |
+----------------------------------------------+
| 1        | 9900.0       | [9900.0]           |
+----------------------------------------------+
```

Note that above, because edges are matched in any direction, the cheapest path between accounts `10039` and `2090` is the one that contains a single incoming edge.

The cost function is not limited to edge properties, it can be an arbitrary expression.
The following example has a `CASE` statement that defines a different cost for different types of edges:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT COUNT(e) AS num_hops
     , SUM(e.amount) AS total_amount
     , ARRAY_AGG(e.amount) AS amounts_along_path
  FROM MATCH ANY CHEAPEST (p1:Person) (-[e:owner|transaction]-
                                      COST CASE
                                             WHEN e.amount IS NULL THEN 1
                                             ELSE e.amount
                                           END)* (p2:Person)
 WHERE p1.name = 'Nikita' AND p2.name = 'Liam'
```

```
+----------------------------------------------+
| num_hops | total_amount | amounts_along_path |
+----------------------------------------------+
| 4        | 10900.0      | [1000.0, 9900.0]   |
+----------------------------------------------+
```

Note that above, when the edge is an `owner` edge, `e.amount` will return NULL resulting in a cost of `1` (`WHEN e.amount IS NULL THEN 1`).

### Cheapest K Paths

PGQL offers a `CHEAPEST k PATHS` clause, which returns the `k` paths that match a given pattern with the lowest cost,
computed with a user-defined cost function. If the user-defined cost function returns a constant, the `CHEAPEST k PATHS`
 clause is equivalent to `SHORTEST k PATHS`.

The syntax of the queries is extended the following way:

```
CheapestKPathPattern  ::=   'CHEAPEST' <KValue> ( 'PATH' | 'PATHS' )?
                              <SourceVertexPattern>
                                <QuantifiedPathPatternPrimary>
                                  <DestinationVertexPattern>
                          | 'CHEAPEST' <KValue>  ( 'PATH' | 'PATHS' )? '('
                              <SourceVertexPattern>
                                <QuantifiedPathPatternPrimary>
                                  <DestinationVertexPattern> ')'
```

The cost function must evaluate to a number.

Over paths returned by a `CHEAPEST` query the same aggregations are defined as over paths returned by a `SHORTEST` query.

The `CHEAPEST` queries represent paths the same way as `SHORTEST`, allowing the same path aggregations.

For example, the following query returns the cheapest 3 paths from account 10039 to itself:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT COUNT(e) AS num_hops
       , SUM(e.amount) AS total_amount
       , ARRAY_AGG(e.amount) AS amounts_along_path
    FROM MATCH CHEAPEST 3 PATHS (a:Account) (-[e:transaction]-> COST e.amount)* (a)
   WHERE a.number = 10039
ORDER BY total_amount
```

```
+------------------------------------------------------------+
| num_hops | total_amount | amounts_along_path               |
+------------------------------------------------------------+
| 0        | <null>       | <null>                           |
| 4        | 22399.8      | [1000.0, 1500.3, 9999.5, 9900.0] |
| 4        | 23900.2      | [1000.0, 3000.7, 9999.5, 9900.0] |
+------------------------------------------------------------+
```

The following is a more complex query that involves a cost function based on the labels of the vertices in the cheapest path.
It finds the 4 cheapest paths between account `10039` and company `Oracle` such that `Person` vertices contribute `3` towards the total cost,
while `Account` or `Company` vertices contribute `1` to the total cost.

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT COUNT(e) AS num_hops
       , ARRAY_AGG( CASE label(n_x)
                      WHEN 'Person' THEN n_x.name
                      WHEN 'Company' THEN n_x.name
                      WHEN 'Account' THEN CAST(n_x.number AS STRING)
                    END ) AS names_or_numbers
       , SUM( CASE label(n_x) WHEN 'Person' THEN 8 ELSE 1 END ) AS total_cost
    FROM MATCH CHEAPEST 4 PATHS
          (a:Account)
            (-[e]- (n_x) COST CASE label(n_x) WHEN 'Person' THEN 3 ELSE 1 END)*
              (c:Company)
   WHERE a.number = 10039 AND c.name = 'Oracle'
ORDER BY total_cost
```

```
+----------------------------------------------+
| num_hops | names_or_numbers     | total_cost |
+----------------------------------------------+
| 3        | [2090, 1001, Oracle] | 3          |
| 3        | [8021, 1001, Oracle] | 3          |
| 3        | [8021, 1001, Oracle] | 3          |
| 2        | [Camille, Oracle]    | 9          |
+----------------------------------------------+
```

As you can see, even though the path returned in the fourth row is shorter than the other three paths,
it has a higher cost because it includes a `Person` vertex (`Camille`), which adds `4` to the total cost.

## All Path

`ALL` path returns all paths between source and destination vertices.
Cycles are included. Therefore, it is required to always specify an upper bound on the path length as a way to avoid endless cycling.

Thus, only the following [quantifiers](#quantifiers) are allowed:

- `?`
- `{ n }`
- `{ n, m }`
- `{ , m }`

Whereas these quantifiers are forbidden:

- `*`
- `+`
- `{ n, }`

The syntax is:

```bash
AllPathPattern ::=   'ALL' ( 'PATH' | 'PATHS' )?
                       <SourceVertexPattern>
                         <QuantifiedPathPatternPrimary>
                           <DestinationVertexPattern>
                   | 'ALL' ( 'PATH' | 'PATHS' )? '('
                       <SourceVertexPattern>
                         <QuantifiedPathPatternPrimary>
                           <DestinationVertexPattern> ')'
```

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT LISTAGG(e.amount, ' + ') || ' = ', SUM(e.amount) AS total_amount
    FROM MATCH ALL (a:Account) -[e:transaction]->{,7} (b:Account)
   WHERE a.number = 10039 AND b.number = 2090
ORDER BY total_amount
```

```
+--------------------------------------------------------------------------------+
| LISTAGG(e.amount, ' + ') || ' = '                               | total_amount |
+--------------------------------------------------------------------------------+
| 1000.0 + 1500.3 + 9999.5 =                                      | 12499.8      |
| 1000.0 + 3000.7 + 9999.5 =                                      | 14000.2      |
| 1000.0 + 1500.3 + 9999.5 + 9900.0 + 1000.0 + 1500.3 + 9999.5 =  | 34899.6      |
| 1000.0 + 1500.3 + 9999.5 + 9900.0 + 1000.0 + 3000.7 + 9999.5 =  | 36400.0      |
| 1000.0 + 3000.7 + 9999.5 + 9900.0 + 1000.0 + 1500.3 + 9999.5 =  | 36400.0      |
| 1000.0 + 3000.7 + 9999.5 + 9900.0 + 1000.0 + 3000.7 + 9999.5 =  | 37900.4      |
+--------------------------------------------------------------------------------+
```

## Horizontal Aggregation

Aggregations are either applied in a _vertical_ or a _horizontal_ fashion.

### Recap of vertical aggregation

Vertical aggregation was introduced in [Aggregation](#aggregation). This kind of aggregation is what people usually learn first when they start using PGQL or SQL.
Vertical aggregation takes a group of values from different rows and aggregates the values into a single value, for example by taking the minimum or maximum. If a `GROUP BY` is specified then the output of a query is as many rows as there are groups, while if no `GROUP BY` is specified then the output is a single row. For more details, see [Grouping and Aggregation](#grouping-and-aggregation).

Given the pattern `(n) -[e]-> (m)`, examples of vertical aggregation are:

 - `SUM(e.prop)`
 - `COUNT(e.prop)`
 - `SUM(n.prop + m.prop / 2)`

### Group Variables

To understand _horizontal_ aggregation, however, it is neccesary to know the difference between "singleton variables" and "group variables".
A singleton variable is a variable that binds to only one vertex or edge, whereas a group variable is a variable that may bind to multiple vertices or edges.

Consider the pattern `(n) -[e1]-> (m) -[e2]->* (o)`.
Here, `e1` is a singleton variable because within a single match of the pattern there is always a single edge bound to `e1`, whereas `e2` is a group variable because within a single match of the pattern there may be multiple edges bound to `e2` because of the quantifier `*`.
Variables are thus either singleton variables or group variables depending on whether they are enclosed by a quantifier with an upper bound greater than 1.

Here are examples with singleton variables:

 - `-[e]->`
 - `-[e]->?`

Here are examples with group variables:

 - `-[e]->*`
 - `-[e]->+`
 - `-[e]->{1,4}`

Quantifiers with curly braces _always_ introduce group variables, so the following are also examples with group variables:

- `-[e]->{1,1}` (notice that this is not the same as `-[e]->`)
- `-[e]->{0,1}` (notice that this is not the same as `-[e]->?`)

Group variables thus form implicit groups without a need to explicitly specify a `GROUP BY`.

### Horizontal aggregation using group variables

Group variables can be used to perform _horizontal_ aggregation. To be precise, an aggregation is applied in a horizontal manner if the expression that is input to the aggregation contains at least one group variable.
The input values for the aggregation are obtained by evaluating the expression once for each binding of the group variable(s) within the particular match.
A separate output is generated for each match of the pattern rather than that a single output is generated for an entire group of matches like in case of vertical aggregation.

The same aggregates (`MIN`, `MAX`, `AVG`, etc.) that are used for vertical aggregation are also used for horizontal aggregation.
Given the pattern `( (n) -[e]-> (m) )*`, examples of horizontal aggregations are:

 - `SUM(e.prop * 2)`
 - `COUNT(e.prop)`
 - `ARRAY_AGG(n.prop)`

Aggregations with multiple group variables such as `SUM(n.prop + m.prop / 2)` are not supported in PGQL 2.0 and are planned for a future version.

It is possible to mix vertical and horizontal aggregation in a single query. For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT SUM(COUNT(e)) AS sumOfPathLengths
  FROM MATCH ANY SHORTEST (a:Account) -[e:transaction]->* (b:Account)
 WHERE a.number = 10039 AND (b.number = 1001 OR b.number = 2090)
```

```
+------------------+
| sumOfPathLengths |
+------------------+
| 5                |
+------------------+
```

Above, we first match a shortest path between accounts 10039 and 1001. Notice that the length of this path is 2.
We also match a shortest path between accounts 10039 and 2090. Notice that the length of this path is 3.
In the SELECT clause, the aggregation `COUNT(e)` is a horizontal aggregation since `e` is a group variable. For each of the two shortest paths, `COUNT(e)` computes the length by counting the number of edges. The output will be 2 for one of the two paths, and 3 for the other.
Then it takes the `SUM` to compute the total length of the two paths, which is 5.

### Horizontal aggregation in WHERE and GROUP BY

While vertical aggregation is only possible in the `SELECT`, `HAVING` and `ORDER BY` clauses, horizontal aggregation is also possible in the `WHERE` and `GROUP BY` clauses.

An example of a horizontal aggregation in `WHERE` is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT b.number AS b,
         COUNT(e) AS pathLength,
         ARRAY_AGG(e.amount) AS transactions
    FROM MATCH ANY SHORTEST (a:Account) -[e:transaction]->* (b:Account)
   WHERE a.number = 10039 AND
         (b.number = 8021 OR b.number = 1001 OR b.number = 2090) AND
         COUNT(e) <= 2
ORDER BY pathLength
```

```
+--------------------------------------+
| b    | pathLength | transactions     |
+--------------------------------------+
| 8021 | 1          | [1000.0]         |
| 1001 | 2          | [1000.0, 1500.3] |
+--------------------------------------+
```

Above, we compute a shortest path from account 10039 to accounts 8021, 1001, and 2090. So three paths in total.
However, in the `WHERE` clause we only keep paths that have at most two edges (`COUNT(e) <= 2`) such that only the paths to accounts 8021 and 1001 are kept since the path to 2090 has three edges.

An example of a horizontal aggregation in `GROUP BY` is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT COUNT(e) AS pathLength,
         COUNT(*) AS cnt
    FROM MATCH ANY SHORTEST (a:Account) -[e:transaction]->* (b:Account)
   WHERE (a.number = 10039 OR a.number = 8021) AND
         (b.number = 1001 OR b.number = 2090)
GROUP BY COUNT(e)
ORDER BY pathLength
```

```
+------------------+
| pathLength | cnt |
+------------------+
| 1          | 1   |
| 2          | 2   |
| 3          | 1   |
+------------------+
```

Above, we first match shortest paths between four pairs of vertices and then we group by the length of the paths (`GROUP BY COUNT(e)`) by means of horizontal aggregation. Then we perform a vertical aggregation `COUNT(*)` to compute the number of paths that have the particular path length. The result shows that one path has length 1, two paths have length 2, and one path as length 3.

## Path modes

The following path modes are available in combination with `ANY`, `ALL`, `ANY SHORTEST`, `SHORTEST k`, `ALL SHORTEST`
`ANY CHEAPEST` and `CHEAPEST k`:

- `WALK`, the default path mode, where no filtering of paths happen.
- `TRAIL`, where path bindings with repeated edges are not returned.
- `ACYCLIC`, where path bindings with repeated vertices are not returned.
- `SIMPLE`, where path bindings with repeated vertices are not returned unless the repeated vertex is the
  first and the last in the path

Syntactically, the path mode is placed directly after `ANY`, `ALL`, `ANY SHORTEST`, `SHORTEST k`, `ALL SHORTEST`,
`ANY CHEAPEST` or `CHEAPEST k`.
The path mode is optionally followed by a `PATH` or `PATHS` keyword.

An example with `WALK` is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT LISTAGG(e.amount, ', ') AS amounts_along_path, SUM(e.amount) AS total_cost
FROM MATCH CHEAPEST 4 WALK (a:account) (-[e:transaction]-> COST e.amount)* (a)
WHERE a.number = 10039
ORDER BY total_cost
```

```
+-----------------------------------------------------------------------------+
| amounts_along_path                                             | total_cost |
+-----------------------------------------------------------------------------+
| 1000.0, 1500.3, 9999.5, 9900.0                                 | 22399.8    |
| 1000.0, 3000.7, 9999.5, 9900.0                                 | 23900.2    |
| 1000.0, 1500.3, 9999.5, 9900.0, 1000.0, 1500.3, 9999.5, 9900.0 | 44799.6    |
| <null>                                                         | <null>     |
+-----------------------------------------------------------------------------+
```

Above, although the first two results are simple paths, the third result is neither acyclic nor simple and is also not a
trail since various vertices and edges are repeated along the path.
However, the path is a walk given that walks allow for repeated vertices as well as repeated edges.
Furthermore, the last result indicates that an empty path was matched, which is possible because quantifier `*` matches
patterns _zero_ or more times.

An example with `TRAIL` is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT CAST(a.number AS STRING) || ' -> ' || LISTAGG(x.number, ' -> ') AS accounts_along_path
FROM MATCH ALL TRAIL PATHS (a:account) (-[:transaction]-> (x)){2,} (b:Account)
WHERE a.number = 8021 AND b.number = 1001
```

```
+-----------------------------------------------+
| accounts_along_path                           |
+-----------------------------------------------+
| 8021 -> 1001 -> 2090 -> 10039 -> 8021 -> 1001 |
| 8021 -> 1001 -> 2090 -> 10039 -> 8021 -> 1001 |
+-----------------------------------------------+
```

Above, both paths contain the vertices 8021 and 1001 twice but they are still valid trails as long as no edges are repeated.

An example with `ACYCLIC` is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT CAST(a.number AS STRING) || ' -> ' || LISTAGG(x.number, ' -> ') AS accounts_along_path
FROM MATCH SHORTEST 10 ACYCLIC PATHS (a:account) (-[:transaction]-> (x))+ (b)
WHERE a.number = 10039 AND b.number = 1001
```

```
+-----------------------+
| accounts_along_path   |
+-----------------------+
| 10039 -> 8021 -> 1001 |
| 10039 -> 8021 -> 1001 |
+-----------------------+
```

Above, we requested 10 shortest paths but only two were returned since all the other paths are cyclic.

An example with `SIMPLE` is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT CAST(a.number AS STRING) || ' -> ' || LISTAGG(x.number, ' -> ') AS accounts_along_path
FROM MATCH ANY SIMPLE PATH (a:account) (-[:transaction]-> (x))+ (a)
WHERE a.number = 10039
```

```
+----------------------------------------+
| accounts_along_path                    |
+----------------------------------------+
| 10039 -> 8021 -> 1001 -> 2090 -> 10039 |
+----------------------------------------+
```

Above, a cyclic path is returned.
This path is a valid simple path since it starts and ends in the same vertex and there is no other cycle in the path.

# Number of Rows Per Match

Each `MATCH` clause has an optional number of rows per match that determines the number of rows in the output of the pattern match.
There are three options:

 - [ONE ROW PER MATCH](#one-row-per-match) (default)
 - [ONE ROW PER VERTEX](#one-row-per-vertex)
 - [ONE ROW PER STEP](#one-row-per-step)

The default is `ONE ROW PER MATCH`. This option can be used for both fixed-length and variable-length graph patterns. The other two options are typically used in combination with variable-length graph pattern but can be used for fixed-length graph patterns too.

Syntactically, the number of rows per match are placed after the optional `ON` clause, see [`MatchClause`](#MatchClause).

The syntax of [`RowsPerMatch`](#RowsPerMatch) is:

```bash
RowsPerMatch    ::= <OneRowPerMatch>
                  | <OneRowPerVertex>
                  | <OneRowPerStep>

OneRowPerMatch  ::= 'ONE' 'ROW' 'PER' 'MATCH'

OneRowPerVertex ::= 'ONE' 'ROW' 'PER' 'VERTEX' '(' <VertexVariable> ')'

VertexVariable  ::= <VariableName>

OneRowPerStep   ::= 'ONE' 'ROW' 'PER' 'STEP' '(' <VertexVariable1> ',' <EdgeVariable> ',' <VertexVariable2> ')'

VertexVariable1 ::= <VariableName>

EdgeVariable    ::= <VariableName>

VertexVariable2 ::= <VariableName>
```

## ONE ROW PER MATCH

`ONE ROW PER MATCH` is the default option. [Graph Pattern Matching](#graph-pattern-matching) shows various examples where this option is implicitly used.

An example where the keywords `ONE ROW PER MATCH` are explicitly specified is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT a.number, p.name
    FROM MATCH (a:Account) -[:owner]-> (p:Person)
           ON financial_transactions
           ONE ROW PER MATCH
ORDER BY a.number
```

```
+------------------+
| number | name    |
+------------------+
| 2090   | Liam    |
| 8021   | Nikita  |
| 10039  | Camille |
+------------------+
```

Above, `ONE ROW PER MATCH` is used for a fixed-length graph pattern.
Since there are three matches to the pattern, three rows are returned.

`ONE ROW PER MATCH` can be used in combination with variable-length graph pattern too.
In that case, data can be returned only from the begin and end points of variable-length paths unless data along paths is aggregated through [horizontal aggregation](#horizontal-aggregation).

For example:


{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT a1.number AS account1, a2.number AS account2
       , LISTAGG(t.amount, ' + ') || ' = ', SUM(t.amount) AS total_amount
    FROM MATCH (p1:Person) <-[:owner]- (a1:Account) ONE ROW PER MATCH
       , MATCH (p2:Person) <-[:owner]- (a2:Account) ONE ROW PER MATCH
       , MATCH ALL (a1) -[t:transaction]->{,4} (a2) ONE ROW PER MATCH
   WHERE p1.name = 'Camille' AND p2.name = 'Liam'
ORDER BY total_amount
```

```
+------------------------------------------------------------------------+
| account1 | account2 | LISTAGG(t.amount, ' + ') || ' = ' | total_amount |
+------------------------------------------------------------------------+
| 10039    | 2090     | 1000.0 + 1500.3 + 9999.5 =        | 12499.8      |
| 10039    | 2090     | 1000.0 + 3000.7 + 9999.5 =        | 14000.2      |
+------------------------------------------------------------------------+
```

Above, since the pattern matches two `transaction` paths between the accounts of `Camille` and `Liam` and since `ONE ROW PER MATCH` is specified,
there are two rows returned from the query.
The values along the paths are aggregated via `LISTAGG` and `SUM` aggregations.
`ONE ROW PER MATCH` does not allow for returning values along paths as individual elements but [ONE ROW PER VERTEX](#one-row-per-vertex) and [ONE ROW PER STEP](#one-row-per-step) can be used for that.

## ONE ROW PER VERTEX

`ONE ROW PER VERTEX` is used to retrieve information from vertices along paths.
The output will be one row per vertex.
This option is typically used in combination with variable-length path patterns.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v.number AS account_nr, ELEMENT_NUMBER(v) AS elem_nr
    FROM MATCH ANY (a1:Account) -[:transaction]->* (a2:Account)
           ON financial_transactions
           ONE ROW PER VERTEX ( v )
   WHERE a1.number = 1001 AND a2.number = 8021
ORDER BY ELEMENT_NUMBER(v)
```

```
+----------------------+
| account_nr | elem_nr |
+----------------------+
| 1001       | 1       |
| 2090       | 3       |
| 10039      | 5       |
| 8021       | 7       |
+----------------------+
```

Above, although only a single path matched the pattern, four rows were returned because the path has four vertices.
We return the account numbers of the four vertices together with the element numbers (see [ELEMENT_NUMBER function](#element_number-function)).
Note that the element numbers are odd since the even numbers are taken by the edges that connect the vertices.

Another example is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v.number AS account_nr, MATCHNUM(v) AS match_nr, ELEMENT_NUMBER(v) AS elem_nr
       , LISTAGG(t.amount, ' + ') || ' = ', SUM(t.amount) AS total_amount
    FROM MATCH (p1:Person) <-[:owner]- (a1:Account) ONE ROW PER MATCH
       , MATCH (p2:Person) <-[:owner]- (a2:Account) ONE ROW PER MATCH
       , MATCH ALL (a1) -[t:transaction]->{,4} (a2) ONE ROW PER VERTEX (v)
   WHERE p1.name = 'Camille' AND p2.name = 'Liam'
ORDER BY MATCHNUM(v), ELEMENT_NUMBER(v)
```

```
+------------------------------------------------------------------------------------+
| account_nr | match_nr | elem_nr | LISTAGG(t.amount, ' + ') || ' = ' | total_amount |
+------------------------------------------------------------------------------------+
| 10039      | 4        | 1       | 1000.0 + 1500.3 + 9999.5 =        | 12499.8      |
| 8021       | 4        | 3       | 1000.0 + 1500.3 + 9999.5 =        | 12499.8      |
| 1001       | 4        | 5       | 1000.0 + 1500.3 + 9999.5 =        | 12499.8      |
| 2090       | 4        | 7       | 1000.0 + 1500.3 + 9999.5 =        | 12499.8      |
| 10039      | 5        | 1       | 1000.0 + 3000.7 + 9999.5 =        | 14000.2      |
| 8021       | 5        | 3       | 1000.0 + 3000.7 + 9999.5 =        | 14000.2      |
| 1001       | 5        | 5       | 1000.0 + 3000.7 + 9999.5 =        | 14000.2      |
| 2090       | 5        | 7       | 1000.0 + 3000.7 + 9999.5 =        | 14000.2      |
+------------------------------------------------------------------------------------+
```

There are a couple things to observe from this example:
 - `ONE ROW PER MATCH` is used for the two fixed-length patterns while `ONE ROW PER VERTEX` is used for the variable-length pattern. So, each `MATCH` has its own number of rows per match.
 - Even though we specified `ONE ROW PER VERTEX` for the third pattern, the variable `t` is still available for [horizontal aggregations](#horizontal-aggregation) like the `LISTAGG` and `SUM` aggregations in the `SELECT`.
 - If there are multiple matches (here there are two matches to the pattern), then the [MATCHNUM function](#matchnum-function) can be used to identify them.

## ONE ROW PER STEP

`ONE ROW PER STEP` is used to retrieve information from edges on paths as well as from the two endpoints of those edges.
This option is typically used in combination with variable-length path patterns.

A step is a vertex-edge-vertex triple and `ONE ROW PER STEP` therefore defines three variables:
 - A first vertex variable
 - An edge variable
 - A second vertex variable

When iterating through the steps of a path, the second vertex variable of the current step binds to the same vertex as the first vertex variable of the next step, unless there is no next step.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v1.number AS v1_account_nr, e.amount, v2.number AS v2_account_nr
       , ELEMENT_NUMBER(v1) AS v1_elem_nr, ELEMENT_NUMBER(e) AS e_elem_nr
       , ELEMENT_NUMBER(v2) AS v2_elem_nr
    FROM MATCH ANY (a1:Account) -[:transaction]->+ (a2:Account)
           ON financial_transactions
           ONE ROW PER STEP ( v1, e, v2 )
   WHERE a1.number = 1001 AND a2.number = 8021
ORDER BY ELEMENT_NUMBER(e)
```

```
+------------------------------------------------------------------------------+
| v1_account_nr | amount | v2_account_nr | v1_elem_nr | e_elem_nr | v2_elem_nr |
+------------------------------------------------------------------------------+
| 1001          | 9999.5 | 2090          | 1          | 2         | 3          |
| 2090          | 9900.0 | 10039         | 3          | 4         | 5          |
| 10039         | 1000.0 | 8021          | 5          | 6         | 7          |
+------------------------------------------------------------------------------+
```

Above, although only a single path matched the pattern, three rows were returned because the path has three steps.
In each step, we return the account numbers of the two end points (`v1_account_nr` and `v2_account_nr`), the `amount` of the `transaction` edges, and the [element numbers](#element_number-function) of the vertices and edges on the path (`v1_elem_nr`, `e_elem_nr` and `v2_elem_nr`).
Note that vertices always have odd element numbers while edges always have even element numbers since paths always start with a vertex, then an edge, then another vertex, then another edge, etc.

In the example above, the first vertex variable of a step each time binds to the source of an edge while the second vertex variable binds to the destination of an edge. However, it is not always the case that the first variable binds to the sources of edges: if the edge pattern points from right-to-left instead of left-to-right then the first vertex variable binds to destinations of edges while the second vertex variable binds to sources of edges.

The following example is the same as above but with the direction of the edge pattern and the start and end of the path pattern reversed:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v1.number AS v1_account_nr, e.amount, v2.number AS v2_account_nr
       , ELEMENT_NUMBER(v1) AS v1_elem_nr, ELEMENT_NUMBER(e) AS e_elem_nr
       , ELEMENT_NUMBER(v2) AS v2_elem_nr
    FROM MATCH ANY (a2:Account) <-[:transaction]-+ (a1:Account)
           ON financial_transactions
           ONE ROW PER STEP ( v1, e, v2 )
   WHERE a1.number = 1001 AND a2.number = 8021
ORDER BY ELEMENT_NUMBER(e)
```

```
+------------------------------------------------------------------------------+
| v1_account_nr | amount | v2_account_nr | v1_elem_nr | e_elem_nr | v2_elem_nr |
+------------------------------------------------------------------------------+
| 8021          | 1000.0 | 10039         | 1          | 2         | 3          |
| 10039         | 9900.0 | 2090          | 3          | 4         | 5          |
| 2090          | 9999.5 | 1001          | 5          | 6         | 7          |
+------------------------------------------------------------------------------+
```

Note the difference with before: the first variable `v1` this time binds to destinations of the edges rather than to sources, while the second vertex variable `v2` binds to sources rather than to destinations.

Another example is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v1.number AS v1_account_nr, e.amount, v2.number AS v2_account_nr, MATCHNUM(e) AS match_nr
       , ELEMENT_NUMBER(v1) AS v1_elem_nr, ELEMENT_NUMBER(e) AS e_elem_nr
       , ELEMENT_NUMBER(v2) AS v2_elem_nr, SUM(t.amount) AS total_amount
    FROM MATCH (p1:Person) <-[:owner]- (a1:Account) ONE ROW PER MATCH
       , MATCH (p2:Person) <-[:owner]- (a2:Account) ONE ROW PER MATCH
       , MATCH ALL (a1) -[t:transaction]->{1,4} (a2) ONE ROW PER STEP (v1, e, v2)
   WHERE p1.name = 'Camille' AND p2.name = 'Liam'
ORDER BY MATCHNUM(e), ELEMENT_NUMBER(e)
```

```
+--------------------------------------------------------------------------------------------------------+
| v1_account_nr | amount | v2_account_nr | match_nr | v1_elem_nr | e_elem_nr | v2_elem_nr | total_amount |
+--------------------------------------------------------------------------------------------------------+
| 10039         | 1000.0 | 8021          | 3        | 1          | 2         | 3          | 12499.8      |
| 8021          | 1500.3 | 1001          | 3        | 3          | 4         | 5          | 12499.8      |
| 1001          | 9999.5 | 2090          | 3        | 5          | 6         | 7          | 12499.8      |
| 10039         | 1000.0 | 8021          | 4        | 1          | 2         | 3          | 14000.2      |
| 8021          | 3000.7 | 1001          | 4        | 3          | 4         | 5          | 14000.2      |
| 1001          | 9999.5 | 2090          | 4        | 5          | 6         | 7          | 14000.2      |
+--------------------------------------------------------------------------------------------------------+
```

There are a couple things to observe from this example:
 - `ONE ROW PER MATCH` is used for the two fixed-length patterns while `ONE ROW PER STEP` is used for the variable-length pattern. So, each `MATCH` has its own number of rows per match.
 - Even though we specified `ONE ROW PER STEP` for the third pattern, the variable `t` is still available for [horizontal aggregations](#horizontal-aggregation) like the the `SUM` aggregation in the `SELECT`.
 - If there are multiple matches (here there are two matches to the pattern), then the [MATCHNUM function](#matchnum_function) can be used to identify them.

Finally, it is worth noting that if a path is empty (i.e. has length zero) then it has a single step such that the first vertex variable is bound but the edge variable and the second vertex variable are unbound.
Therefore, the number of steps does not always equal the number of edges on a path.

# Grouping and Aggregation

## GROUP BY Clause

`GROUP BY` allows for grouping of solutions and is typically used in combination with aggregates like `MIN` and `MAX` to compute aggregations over groups of solutions.

The following explains the syntactic structure of the `GROUP BY` clause:

```bash
GroupByClause ::= 'GROUP' 'BY' <ExpAsVar> ( ',' <ExpAsVar> )*
```

The `GROUP BY` clause starts with the keywords GROUP BY and is followed by a comma-separated list of value expressions that can be of any type.

Consider the following query:

```sql
  SELECT n.first_name, COUNT(*), AVG(n.age)
    FROM MATCH (n:Person)
GROUP BY n.first_name
```

Matches are grouped by their values for `n.first_name`. For each group, the query selects `n.first_name` (i.e. the group key), the number of solutions in the group (i.e. `COUNT(*)`), and the average value of the property age for vertex n (i.e. `AVG(n.age)`).

### Multiple Terms in GROUP BY

It is possible that the `GROUP BY` clause consists of multiple terms. In such a case, matches are grouped together only if they hold the same result for each of the group expressions.

Consider the following query:

```sql
  SELECT n.first_name, n.last_name, COUNT(*)
    FROM MATCH (n:Person)
GROUP BY n.first_name, n.last_name
```

Matches will be grouped together only if they hold the same values for `n.first_name` and the same values for `n.last_name`.

### Aliases in GROUP BY

Each expression in `GROUP BY` can have an alias (e.g. `GROUP BY n.prop AS myAlias`). The alias can be referenced from the `HAVING`, `ORDER BY` and `SELECT` clauses so that repeated specification of the same expression can be avoided.

Note, however, that `GROUP BY` can also reference aliases from `SELECT` but it is not allowed to create a circular dependency such that an expression in the `SELECT` references an expression in the `GROUP BY` that in its turn references that same expression in the `SELECT`.

### GROUP BY and NULL values

The group for which all the group keys are null is a valid group and takes part in further query processing.

To filter out such a group, use the [HAVING clause](#having-clause).
Foror example:

```sql
  SELECT n.prop1, n.prop2, COUNT(*)
    FROM MATCH (n)
GROUP BY n.prop1, n.prop2
  HAVING n.prop1 IS NOT NULL AND n.prop2 IS NOT NULL
```

### Repetition of Group Expression in Select or Order Expression

Group expressions may be repeated in select or order expressions.

Consider the following query:

```sql
  SELECT n.age, COUNT(*)
    FROM MATCH (n)
GROUP BY n.age
ORDER BY n.age
```

Here, the group expression `n.age` is repeated in the SELECT and ORDER BY.

## Aggregation

Aggregates `COUNT`, `MIN`, `MAX`, `AVG` and `SUM` can aggregate over groups of solutions.

The syntax is:

```bash
Aggregation       ::=   <CountAggregation>
                       | <MinAggregation>
                       | <MaxAggregation>
                       | <AvgAggregation>
                       | <SumAggregation>
                       | <ArrayAggregation>
                       | <ListaggAggregation>

CountAggregation   ::=   'COUNT' '(' '*' ')'
                       | 'COUNT' '(' 'DISTINCT'? <ValueExpression> ')'

MinAggregation     ::= 'MIN' '(' 'DISTINCT'? <ValueExpression> ')'

MaxAggregation     ::= 'MAX' '(' 'DISTINCT'? <ValueExpression> ')'

AvgAggregation     ::= 'AVG' '(' 'DISTINCT'? <ValueExpression> ')'

SumAggregation     ::= 'SUM' '(' 'DISTINCT'? <ValueExpression> ')'

ArrayAggregation   ::= 'ARRAY_AGG' '(' 'DISTINCT'? <ValueExpression> ')'

ListaggAggregation ::= 'LISTAGG' '(' 'DISTINCT'? <ValueExpression> <ListaggSeparator>? ')'

ListaggSeparator   ::= ',' <StringLiteral>
```

Syntactically, an aggregation takes the form of aggregate followed by an optional `DISTINCT` modifier and a `<ValueExpression>`.

The following table gives an overview of the different aggregates and their supported input types.

aggregate operator | semantic | required input type
--- | --- | ---
`COUNT` | counts the number of times the given expression has a bound (i.e. is not null). | any type, including vertex and edge
`MIN` | takes the minimum of the values for the given expression. | numeric, string, boolean, date, time [with time zone], or, timestamp [with time zone]
`MAX` | takes the maximum of the values for the given expression. | numeric, string, boolean, date, time [with time zone], or, timestamp [with time zone]
`SUM` | sums over the values for the given expression. | numeric
`AVG` | takes the average of the values for the given expression. | numeric
`ARRAY_AGG` | constructs an array/list of the values for the given expression. | numeric, string, boolean, date, time [with time zone], or, timestamp [with time zone]
`LISTAGG` | constructs a concatenation of the values for the given expression; an optional separator can be specified to delimit the values. | numeric, string, boolean, date, time [with time zone], or, timestamp [with time zone]

All aggregate functions ignore nulls. `COUNT` never returns null, but instead returns zero if all input values to the aggregate function are null.
For all the remaining aggregate functions, if there are no inputs or all input values to the aggregate function are null, then the function returns null.

For example, the average of `2`, `4` and `null` is `3`, while the average of `null` and `null` is `null`.
The count of `2`, `4` and `null` is `2` (there are two non-null values), while the count of `null` and `null` is `0`.

### Aggregation with GROUP BY

If a `GROUP BY` is specified, aggregations are applied to each individual group of solutions.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT label(owner),
       COUNT(*) AS numTransactions,
       SUM(out.amount) AS totalOutgoing,
       LISTAGG(out.amount, ', ') AS amounts
  FROM MATCH (a:Account) -[:owner]-> (owner:Person|Company)
     , MATCH (a) -[out:transaction]-> (:Account)
GROUP BY label(owner)
ORDER BY label(owner)
```

```
+---------------------------------------------------------------------------------+
| label(owner) | numTransactions | totalOutgoing | amounts                        |
+---------------------------------------------------------------------------------+
| Company      | 1               | 9999.5        | 9999.5                         |
| Person       | 4               | 15401.0       | 1000.0, 9900.0, 1500.3, 3000.7 |
+---------------------------------------------------------------------------------+
```

Here, we match accounts, their owner (either a person or a company) and their outgoing transactions.
Then we group by the owner's label (either `Person` or `Company`) and compute the total number of outgoing transactions,
the total amount transacted, and a comma-separated list of transaction amounts for each group.

### Aggregation without GROUP BY

If _no_ `GROUP BY` is specified, aggregations are applied to the entire set of solutions.

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT COUNT(*) AS numTransactions,
       SUM(out.amount) AS totalOutgoing,
       LISTAGG(out.amount, ', ') AS amounts
  FROM MATCH (a:Account) -[:owner]-> (owner:Person|Company)
     , MATCH (a) -[out:transaction]-> (:Account)
```

```
+--------------------------------------------------------------------------+
| numTransactions | totalOutgoing | amounts                                |
+--------------------------------------------------------------------------+
| 5               | 25400.5       | 1000.0, 9900.0, 1500.3, 3000.7, 9999.5 |
+--------------------------------------------------------------------------+
```

Note that the result will always be a single row, unless nothing was matched in which case zero rows are returned.

### COUNT(*)

`COUNT(*)` is a special construct that simply counts the number of solutions without evaluating an expression.

For example:

```sql
SELECT COUNT(*)
  FROM MATCH (m:Person)
```

### DISTINCT in aggregation

The `DISTINCT` modifier specifies that duplicate values should be removed before performing aggregation.

For example:

```sql
SELECT AVG(DISTINCT m.age)
  FROM MATCH (m:Person)
```

Here, we aggregate only over distinct `m.age` values.

## HAVING Clause

The `HAVING` clause is an optional clause that can be placed after a `GROUP BY` clause to filter out particular groups of solutions.

The syntax is:

```bash
HavingClause ::= 'HAVING' <ValueExpression>
```

The value expression needs to be a boolean expression.

For example:

```sql
  SELECT n.name
    FROM MATCH (n) -[:has_friend]-> (m)
GROUP BY n
  HAVING COUNT(m) > 10
```

This query returns the names of people who have more than 10 friends.


# Sorting and Row Limiting


## ORDER BY Clause

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
    FROM MATCH (n:Person)
ORDER BY n.age ASC
```

### Data types for ORDER BY

A partial ordering for the different data types is defined as follows:

- Numeric values are ordered from small to large.
- String values are ordered lexicographically.
- Boolean values are ordered such that `false` comes before `true`.
- Datetime values (i.e. dates, times, or timestamps) are ordered such that earlier points in time come before later points in time.

Vertices, edges and arrays cannot be ordered directly.

### Multiple expressions in ORDER BY

An `ORDER BY` may contain more than one expression, in which case the expresisons are evaluated from left to right. That is, (n+1)th ordering term is used only for the tie-break rule for n-th ordering term. Note that different expressions can have different ascending or descending decorators.

```sql
  SELECT f.name
    FROM MATCH (f:Person)
ORDER BY f.age ASC, f.salary DESC
```

## OFFSET Clause

The `OFFSET` specifies the start of the first solution that should be returned.

The syntax is:

```bash
OffsetClause   ::= 'OFFSET' <OffsetRowCount> ( 'ROW' | 'ROWS' )?

OffsetRowCount ::=   <UNSIGNED_INTEGER>
                   | <BindVariable>
```

The `OFFSET` clause starts with the keyword `OFFSET` and is followed by an offset row count.
The offset row count needs to be greater than or equal to 0.
If it is larger than the number of rows in the result, no rows are returned.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT n.name
FROM MATCH (n:Person)
ORDER BY n.name
OFFSET 1
```

```
+--------+
| name   |
+--------+
| Liam   |
| Nikita |
| Oracle |
+--------+
```

## FETCH FIRST Clause

The `FETCH FIRST` clause provides a limit to the number of solutions to be kept.

The syntax is:

```bash
FetchFirstClause   ::= 'FETCH' ( 'FIRST' | 'NEXT' )? <FetchFirstQuantity> ( 'FIRST' | 'NEXT' )? 'ONLY'

FetchFirstQuantity ::=   <UNSIGNED_INTEGER>
                       | <BindVariable>
```


The `FETCH FIRST` clause is applied after the [OFFSET clause](#offset-clause).
If there are fewer solutions than the fetch quantity, all solutions are returned.

For example, in the following query the first solution is pruned from the result (`OFFSET 1`) and the next two solutions are fetched (`FETCH FIRST 2 ROWS ONLY`).

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT n.name
FROM MATCH (n:Person)
ORDER BY n.name
OFFSET 1
FETCH FIRST 2 ROWS ONLY
```

```
+--------+
| name   |
+--------+
| Liam   |
| Nikita |
+--------+
```

## LIMIT Clause

The `LIMIT` clause provides a syntactic alternative to the [FETCH FIRST clause](#fetch-first-clause).

The syntax is:

```bash
LimitClause   ::= 'LIMIT' <LimitQuantity>

LimitQuantity ::=   <UNSIGNED_INTEGER>
                  | <BindVariable>
```

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT n.name
FROM MATCH (n:Person)
ORDER BY n.name
OFFSET 1
LIMIT 2
```

```
+--------+
| name   |
+--------+
| Liam   |
| Nikita |
+--------+
```

# Functions and Expressions

Value expressions are used in various parts of the language, for example, to filter solutions (`WHERE` and `HAVING`), to project out computed values (`SELECT`), or, to group by or order by computed values (`GROUP BY` and `ORDER BY`).

The following are the relevant grammar rules:

```bash
ValueExpression          ::=   <VariableReference>
                             | <PropertyReference>
                             | <Literal>
                             | <BindVariable>
                             | <ArithmeticExpression>
                             | <RelationalExpression>
                             | <LogicalExpression>
                             | <StringConcat>
                             | <BracketedValueExpression>
                             | <FunctionInvocation>
                             | <CharacterSubstring>
                             | <Aggregation>
                             | <ExtractFunction>
                             | <NullPredicate>
                             | <InPredicate>
                             | <LabeledPredicate>
                             | <CastSpecification>
                             | <CaseExpression>
                             | <ExistsPredicate>
                             | <ScalarSubquery>

VariableReference        ::= <VariableName>

PropertyReference        ::= <ElementReference> '.' <PropertyName>

ElementReference         ::= <VariableName>

BracketedValueExpression ::= '(' <ValueExpression> ')'
```

A value expression is one of:

 - A variable reference, being either a reference to a `<VertexPattern>`, an `<EdgePattern>`, or an `<ExpAsVar>`.
 - A property access, which syntactically takes the form of a variable reference, followed by a dot (`.`) and the name of a property.
 - A literal (see [Literals](#literals)).
 - A bind variable (see [Bind Variables](#bind-variables)).
 - An arithmetic expression, relational expression, logical expression or string concatenation (see [Operators](#operators)).
 - A bracketed value expression, which syntactically takes the form of a value expression between rounded brackets. The brackets allow for controlling precedence.
 - A function invocation (see [String functions](#string-functions), [Numeric functions](#numeric-functions), [Datetime functions](#datetime-functions) and [Vertex and Edge functions](#vertex-and-edge-functions)).
 - A character substring function (see [Substring](#substring)).
 - An aggregation (see [Aggregation](#aggregation)).
 - An `EXTRACT` function (see [EXTRACT](#extract)).
 - A [NULL](#null-predicate)), [IN](#in-predicate)) or [LABELED](#labeled-predicate) predicate.
 - A [CAST](#cast) specification.
 - An `EXISTS` predicate (see [EXISTS and NOT EXISTS Subqueries](#exists-and-not-exists-subqueries)).
 - A scalar subquery (see [Scalar Subqueries](#scalar-subqueries)).

## Data Types and Literals

### Data Types

PGQL has the following data types:

 - `STRING`
 - `NUMERIC` (e.g. `INT`/`INTEGER`, `LONG`, `FLOAT`, `DOUBLE`)
 - `BOOLEAN`
 - `DATE`
 - `TIME`
 - `TIMESTAMP`
 - `TIME WITH TIME ZONE`
 - `TIMESTAMP WITH TIME ZONE`
 - `INTERVAL`

### Literals

The syntax is:

```bash
Literal                      ::=   <StringLiteral>
                                 | <NumericLiteral>
                                 | <BooleanLiteral>
                                 | <DateLiteral>
                                 | <TimeLiteral>
                                 | <TimestampLiteral>
                                 | <TimeWithTimeZoneLiteral>
                                 | <TimestampWithTimeZoneLiteral>
                                 | <IntervalLiteral>

StringLiteral                ::= <STRING_LITERAL>

NumericLiteral               ::=   <UNSIGNED_INTEGER>
                                 | <UNSIGNED_DECIMAL>

BooleanLiteral               ::=   'true'
                                 | 'false'

DateLiteral                  ::= 'DATE' "'" <yyyy-MM-dd> "'"

TimeLiteral                  ::= 'TIME' "'" <HH:mm:ss> "'"

TimestampLiteral             ::= 'TIMESTAMP' "'" <yyyy-MM-dd HH:mm:ss> "'"

TimeWithTimeZoneLiteral      ::= 'TIME' "'" <HH:mm:ss+HH:MM> "'"

TimestampWithTimeZoneLiteral ::= 'TIMESTAMP' "'" <yyyy-MM-dd HH:mm:ss+HH:MM> "'"

IntervalLiteral              ::= 'INTERVAL' <StringLiteral> <DateTimeField>

DateTimeField                ::=   'YEAR'
                                 | 'MONTH'
                                 | 'DAY'
                                 | 'HOUR'
                                 | 'MINUTE'
                                 | 'SECOND'
```

For example:

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
| interval                 | `INTERVAL '1' YEAR`                     |

Note that numeric literals are unsigned, but signed values can be generated by means of the unary minus operator (`-`).

### Bind Variables

In place of a literal, one may specify a bind variable (`?`). This allows for specifying parameterized queries.

```bash
BindVariable ::= '?'
```

An example query with two bind variables is as follows:

```sql
SELECT n.age
  FROM MATCH (n)
 WHERE n.name = ?
    OR n.age > ?
```

In the following query, bind variables are used in `LIMIT` and `OFFSET`:

```sql
  SELECT n.name, n.age
    FROM MATCH (n)
ORDER BY n.age
   LIMIT ?
  OFFSET ?
```

The following example shows a bind variable in the position of a label:

```sql
  SELECT n.name
    FROM MATCH (n)
   WHERE label(n) = ?
```


## Operators

### Overview of Operators

The following table is an overview of the operators:

operator type | operator
------------- | --------
arithmetic    | `+`, `-`, `*`, `/`, `%`, `-` (unary minus)
relational    | `=`, `<>`, `<`, `>`, `<=`, `>=`
logical       | `AND`, `OR`, `NOT`
string        | `||` (concat)

The corresponding grammar rules are:

```bash
ArithmeticExpression ::=   <UnaryMinus>
                         | <Multiplication>
                         | <Division>
                         | <Modulo>
                         | <Addition>
                         | <Subtraction>

UnaryMinus           ::= '-' <ValueExpression>

StringConcat         ::= <ValueExpression> '||' <ValueExpression>

Multiplication       ::= <ValueExpression> '*' <ValueExpression>

Division             ::= <ValueExpression> '/' <ValueExpression>

Modulo               ::= <ValueExpression> '%' <ValueExpression>

Addition             ::= <ValueExpression> '+' <ValueExpression>

Subtraction          ::= <ValueExpression> '-' <ValueExpression>

RelationalExpression ::=   <Equal>
                         | <NotEqual>
                         | <Greater>
                         | <Less>
                         | <GreaterOrEqual>
                         | <LessOrEqual>

Equal                ::= <ValueExpression> '=' <ValueExpression>

NotEqual             ::= <ValueExpression> '<>' <ValueExpression>

Greater              ::= <ValueExpression> '>' <ValueExpression>

Less                 ::= <ValueExpression> '<' <ValueExpression>

GreaterOrEqual       ::= <ValueExpression> '>=' <ValueExpression>

LessOrEqual          ::= <ValueExpression> '<=' <ValueExpression>

LogicalExpression    ::=   <Not>
                         | <And>
                         | <Or>

Not                  ::= 'NOT' <ValueExpression>

And                  ::= <ValueExpression> 'AND' <ValueExpression>

Or                   ::= <ValueExpression> 'OR' <ValueExpression>
```

The supported input types and corresponding return types are as follows:

operator                       | type of A                  | type of B                  | return type
------------------------------ | ------------------------------------------------------- | -----------
`-`A (unary minus)             | numeric                    |                            | numeric
A `||` B                       | string                     | string                     | string
A `+` `-` B                    | numeric<br>date<br>time [with time zone]<br>timestamp [with time zone] | numeric<br>interval<br>interval<br>interval | numeric<br>date<br>time [with time zone]<br>timestamp [with time zone]
A `*` `/` `%` B                | numeric                    | numeric                    | numeric
A `=` `<>` B                   | numeric<br>string<br>boolean<br>date<br>time [with time zone]<br>timestamp [with time zone]<br>vertex<br>edge<br>array | numeric<br>string<br>boolean<br>date<br>time [with time zone]<br>timestamp [with time zone]<br>vertex<br>edge<br>array | boolean<br>boolean<br>boolean<br>boolean<br>boolean<br>boolean<br>boolean<br>boolean<br>boolean
A `<` `>` `<=` `>=` B | numeric<br>string<br>boolean<br>date<br>time [with time zone]<br>timestamp [with time zone] | numeric<br>string<br>boolean<br>date<br>time [with time zone]<br>timestamp [with time zone] | boolean<br>boolean<br>boolean<br>boolean<br>boolean<br>boolean
`NOT` A                        | boolean                    |                            | boolean
A `AND` `OR` B                 | boolean                    | boolean                    | boolean

Binary operations are only allowed if both operands are of the same type, with the following two exceptions:

- _time_ values can be compared to _time with time zone_ values
- _timestamp_ values can be compared to _timestamp with time zone_ values

To compare such _time(stamp) with time zone_ values to other time(stamp) values (with or without time zone), values are first normalized to have the same time zone, before they are compared.
Comparison with other operand type combinations, such as dates and timestamp, is not possible. However, it is possible to cast between e.g. dates and timestamps (see [CAST](#cast)).

### Operator Precedence

Operator precedences are shown in the following list, from the highest precedence to the lowest. An operator on a higher level (e.g. level 1) is evaluated before an operator on a lower level (e.g. level 2).

level | operator precedence
----- | ---
1     | `-` (unary minus)
2     | `||` (string concat)
3     | `*`, `/`, `%`
4     | `+`, `-`
5     | `=`, `<>`, `>`, `<`, `>=`, `<=`
6     | `NOT`
7     | `AND`
8     | `OR`

### Implicit Type Conversion

Performing arithmetic operations with different numeric types will lead to implicit type conversion (i.e. coercion).
Coercion is only defined for numeric types. Given a binary arithmetic operation (i.e. `+`, `-`, `*`, `/`, `%`), the rules are as follows:

 - If both operands are exact numerics (e.g. integer or long), then the result is also an exact numeric with a scale that is at least as large as the scales of each operand.
 - If one or both of the operands is approximate numeric (e.g. float, double), the result is an approximate numeric with a scale that is at least as large as the scales of each operand. The precision will also be at least as high as the precision of each operand.

## Null values

The property graph data model does not allow properties with `null` value. Instead, missing or undefined data can be modeled through the _absence_ of properties.
A `null` value is generated when trying to access a property of a vertex or an edge while the property appears to be missing.
Three-valued logic applies when `null` values appear in computation.

### Three-Valued Logic

An operator returns `null` if one of its operands yields `null`, with an exception for `AND` and `OR`. This is shown in the following table:

operator                        | result when A is null                         | result when B is null                          | result when A and B are null
------------------------------- | --------------------------------------------- | ---------------------------------------------- | ----------------------------
A `+` `-` `*` `/` `%` B         | `null`                                        | `null`                                         | `null`
`-` A                           | `null`                                        | N/A                                            | N/A
A `=` `<>` `>` `<` `>=` `<=` B  | `null`                                        | `null`                                         | `null`
A `AND` B                       | `false` if B yields `false`, `null` otherwise | `false` if A yields `false`, `null` otherwise  | `null`
A `OR` B                        | `true` if B yields `true`, `null` otherwise   | `true` if A yields `true`, `null` otherwise    | `null`
`NOT` A                         | `null`                                        | N/A                                            | N/A

Note that from the table it follows that `null = null` yields `null` and not `true`.

### NULL Predicate

To test whether a value exists or not, one can use the `IS NULL` and `IS NOT NULL` constructs.

```bash
NullPredicate ::= <ValueExpression> 'IS' ('NOT')? 'NULL'
```

For example:

```sql
SELECT n.name
  FROM MATCH (n)
 WHERE n.name IS NOT NULL
```

Here, we find all the vertices in the graph that have the property `name` and then return the property.


## Vertex and Edge functions

### VERTEX_ID/EDGE_ID Function

The `VERTEX_ID` and `EDGE_ID` functions return a system-generated identifier for the vertex/edge.

The syntax is:

```
VERTEX_ID( vertex )
EDGE_ID( edge )
```

#### ID Function

The `ID` function provides a syntactic alternative for the [VERTEX_ID/EDGE_ID function](#vertex_idedge_id-function).

The syntax is:

```
ID( vertex/edge )
```

### LABEL Function

The `LABEL` function returns the label of a vertex or an edge. It is an error if the vertex or edge does not have a label, or, has more than one label.
The return type of the function is a string.

The syntax is:

```
LABEL( vertex/edge )
```

For example:

```sql
SELECT label(e)
FROM MATCH (n:Person) -[e]-> (m:Person)
```

```
+----------+
| label(e) |
+----------+
| likes    |
| knows    |
| likes    |
+----------+
```

### LABELS Function

The `labels` function returns the set of labels of a vertex or an edge. If the vertex or edge does not have a label, an empty set is returned.
The return type of the function is a set of strings.

The syntax is:

```
LABELS( vertex/edge )
```

For example:

```sql
SELECT labels(n)
  FROM MATCH (n:Employee|Manager)
```

```
+---------------------+
| labels(n)           |
+---------------------+
| [Employee]          |
| [Manager]           |
| [Employee, Manager] |
+---------------------+
```

### LABELED Predicate

The `IS [NOT] LABELED` predicate returns true or false depending on whether the vertex or edge has the specified label.

The syntax is:

```bash
LabeledPredicate ::= <ElementReference> 'IS' ('NOT')? 'LABELED' <Label>
```

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT a.number,
       CASE WHEN n IS LABELED Person THEN 'Personal Account' ELSE 'Business Account' END AS accountType
FROM MATCH (n:Person|Company) <-[:owner]- (a:Account)
```

```
+---------------------------+
| number | accountType      |
+---------------------------+
| 10039  | Personal Account |
| 2090   | Personal Account |
| 8021   | Personal Account |
| 1001   | Business Account |
+---------------------------+
```

### SOURCE / DESTINATION Predicate

The `SOURCE` / `DESTINATION` predicate allows for testing if a vertex is the source or the destination of an edge.
This is in particularly useful when edges were matched through [any-directed edge patterns](##matching-edges-in-any-direction).

The syntax is:

```bash
SourceDestinationPredicate ::=   <VertexReference> 'IS' ( 'NOT' )? 'SOURCE' 'OF' <EdgeReference>
                               | <VertexReference> 'IS' ( 'NOT' )? 'DESTINATION' 'OF' <EdgeReference>

VertexReference            ::= <VariableReference>

EdgeReference              ::= <VariableReference>
```

For example:

```sql
SELECT e.amount, CASE WHEN n IS SOURCE OF e THEN 'Outgoing transaction' ELSE 'Incoming transaction' END AS transaction_type
FROM MATCH (n:Account) -[e:transaction]- (m:Account)
WHERE n.number = 8021
ORDER BY transaction_type, e.amount
```

```
+-------------------------------+
| amount | transaction_type     |
+-------------------------------+
| 1000.0 | Incoming transaction |
| 1500.3 | Outgoing transaction |
| 3000.7 | Outgoing transaction |
+-------------------------------+
```

Another example is:

```sql
SELECT n.number, n.name,
       SUM(CASE WHEN n IS DESTINATION OF e THEN 1 ELSE 0 END) AS num_incoming_edges,
       SUM(CASE WHEN n IS SOURCE OF e THEN 1 ELSE 0 END) AS num_outgoing_edges
FROM MATCH (n) -[e]- (m)
GROUP BY number, name
ORDER BY num_incoming_edges + num_outgoing_edges DESC, number, name
```

```
+------------------------------------------------------------+
| number | name    | num_incoming_edges | num_outgoing_edges |
+------------------------------------------------------------+
| 1001   | <null>  | 2                  | 2                  |
| 8021   | <null>  | 1                  | 3                  |
| 2090   | <null>  | 1                  | 2                  |
| 10039  | <null>  | 1                  | 2                  |
| <null> | Camille | 1                  | 1                  |
| <null> | Oracle  | 2                  | 0                  |
| <null> | Liam    | 1                  | 0                  |
| <null> | Nikita  | 1                  | 0                  |
+------------------------------------------------------------+
```

### MATCHNUM Function

The `MATCHNUM` function allows for obtaining a unique identifier for each match to a graph pattern.

Inside [GRAPH_TABLE](#graph_table-operator), the `MATCHNUM` function takes no arguments as it is clear which `MATCH` it applies to. This because `GRAPH_TABLE` only has a single `MATCH`.

Outside of `GRAPH_TABLE`, the `MATCHNUM` function takes a vertex or an edge as argument.
The vertex or edge identifies the `MATCH` clause for which to obtain the match number for.
It is required that the vertex or edge is defined inside a single `MATCH` clause only.

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
SELECT account_num, match_num, elem_num
FROM GRAPH_TABLE ( financial_transactions
       MATCH ALL (a1 IS Account) -[IS transaction]->{,4} (a2 IS Account)
       WHERE a1.number = 10039 AND a2.number = 2090
       ONE ROW PER VERTEX ( v )
       COLUMNS ( v.number AS account_num,
                 MATCHNUM() AS match_num,
                 ELEMENT_NUMBER(v) AS elem_num )
     )
ORDER BY match_num, elem_num
```

```
+------------------------------------+
| account_num | match_num | elem_num |
+------------------------------------+
| 10039       | 0         | 1        |
| 8021        | 0         | 3        |
| 1001        | 0         | 5        |
| 2090        | 0         | 7        |
| 10039       | 1         | 1        |
| 8021        | 1         | 3        |
| 1001        | 1         | 5        |
| 2090        | 1         | 7        |
+------------------------------------+
```

```sql
SELECT v.number AS account_number, MATCHNUM(v), ELEMENT_NUMBER(v)
FROM MATCH ALL (a1:Account) -[:transaction]->{,4} (a2:Account)
       ONE ROW PER VERTEX ( v )
WHERE a1.number = 10039 AND a2.number = 2090
ORDER BY MATCHNUM(v), ELEMENT_NUMBER(v)
```

```
+--------------------------------------------------+
| account_number | MATCHNUM(v) | ELEMENT_NUMBER(v) |
+--------------------------------------------------+
| 10039          | 0           | 1                 |
| 8021           | 0           | 3                 |
| 1001           | 0           | 5                 |
| 2090           | 0           | 7                 |
| 10039          | 1           | 1                 |
| 8021           | 1           | 3                 |
| 1001           | 1           | 5                 |
| 2090           | 1           | 7                 |
+--------------------------------------------------+
```

The numbers returned by the function are unique but not necessarily incremental (0, 1, 2, 3, 4, ...) and gaps between numbers are possible (1, 5, 18, 101) depending on the (multi-threaded) implementation.

### ELEMENT_NUMBER Function

The `ELEMENT_NUMBER` function allows for obtaining a unique identifier for each vertex and edge within a solution to a graph pattern.

Vertices and edges are numbered from top-to-bottom and from left-to-right.
Therefore, vertices have odd numbers (1, 3, 5, ...) while edges have even numbers (2, 4, 6, ...).

For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v1.number AS v1_account_nr, e.amount, v2.number AS v2_account_nr
       , ELEMENT_NUMBER(v1) AS v1_elem_nr, ELEMENT_NUMBER(e) AS e_elem_nr
       , ELEMENT_NUMBER(v2) AS v2_elem_nr
    FROM MATCH ANY (a1:Account) -[:transaction]->+ (a2:Account)
           ONE ROW PER STEP ( v1, e, v2 )
   WHERE a1.number = 1001 AND a2.number = 8021
ORDER BY e_elem_nr
```

```
+------------------------------------------------------------------------------+
| v1_account_nr | amount | v2_account_nr | v1_elem_nr | e_elem_nr | v2_elem_nr |
+------------------------------------------------------------------------------+
| 1001          | 9999.5 | 2090          | 1          | 2         | 3          |
| 2090          | 9900.0 | 10039         | 3          | 4         | 5          |
| 10039         | 1000.0 | 8021          | 5          | 6         | 7          |
+------------------------------------------------------------------------------+
```

The direction of the edge patterns does not affect the left-to-right numbering.
For example:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT v1.number AS v1_account_nr, e.amount, v2.number AS v2_account_nr
       , ELEMENT_NUMBER(v1) AS v1_elem_nr, ELEMENT_NUMBER(e) AS e_elem_nr, ELEMENT_NUMBER(v2) AS v2_elem_nr
    FROM MATCH ANY (a2:Account) <-[:transaction]-+ (a1:Account)
           ONE ROW PER STEP ( v1, e, v2 )
   WHERE a1.number = 1001 AND a2.number = 8021
ORDER BY e_elem_nr
```

```
+------------------------------------------------------------------------------+
| v1_account_nr | amount | v2_account_nr | v1_elem_nr | e_elem_nr | v2_elem_nr |
+------------------------------------------------------------------------------+
| 8021          | 1000.0 | 10039         | 1          | 2         | 3          |
| 10039         | 9900.0 | 2090          | 3          | 4         | 5          |
| 2090          | 9999.5 | 1001          | 5          | 6         | 7          |
+------------------------------------------------------------------------------+
```

Above, we reversed the direction of the edge pattern so that it points from right-to-left instead of left-to-right.
Therefore, the first variable `v1` now binds to destinations rather than sources of edges.

### ALL_DIFFERENT Predicate

The `ALL_DIFFERENT` predicate returns true if the provided values are all different from each other, and false otherwise. The function is typically used for specifying that a particular set of vertices or edges are all different from each other. However, the function can be used for values of any data type, as long as the provided values can be compared for equality.

The syntax is:

```
ALL_DIFFERENT( val1, val2, val3, ..., valN )
```

For example:

```sql
SELECT *
  FROM MATCH (n) -> (m) -> (o)
 WHERE ALL_DIFFERENT( n, m, o )
```

Note that the above query can be rewritten using non-equality constraints as follows:

```sql
SELECT *
  FROM MATCH (n) -> (m) <- (o) -> (n)
 WHERE n <> m AND n <> o AND m <> o
```

Another example is:

```sql
ALL_DIFFERENT( 1, 2, 3 )
Result: true

ALL_DIFFERENT( 1, 1.0 )
Result: false
```

## String functions

In addition to the (character) string functions in this section, please also notice the string concatenation operator (`||`) documented in [Operators](#operators).

### JAVA_REGEXP_LIKE Function

The `JAVA_REGEXP_LIKE` returns whether the string matches the given [Java regular expression pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

The syntax is:

```
JAVA_REGEXP_LIKE( string, pattern )
```

For example:

```sql
JAVA_REGEXP_LIKE('aaaaab', 'a*b')
Result: true
```

### LOWER

The `LOWER` function transforms a string to lowercase. The case of each character is defined by the rules of the default locale.

The syntax is:

```
LOWER( string )
```

For example:

```sql
LOWER('A string')
Result: a string
```

### SUBSTRING

The `SUBSTRING` function returns a portion of the given string, starting from the specified index in `FROM` clause. If a `FOR` clause is provided, the substring returned is limited to the given length.

The syntax is:

```bash
CharacterSubstring ::= 'SUBSTRING' '(' <ValueExpression> 'FROM' <StartPosition> ( 'FOR' <StringLength> )? ')'

StartPosition      ::= <ValueExpression>

StringLength       ::= <ValueExpression>
```

For example:

```sql
SUBSTRING('A string' FROM 1)
Result: A string

SUBSTRING('A string'  FROM 3 FOR 2)
Result: st
```

The following table gives more examples for different values of `FROM` and `FOR`:

input string | FROM  | FOR          | output string
------------ | ----- | ------------ | -------------
`hello`      | `3`   | not provided | `llo`
`hello`      | `-10` | not provided | `hello`
`hello`      | `7`   | not provided | (empty string)
`hello`      | `3`   | not provided | `llo`
`hello`      | `3`   | `-1`         | exception is raised, FOR must not be negative
`hello`      | `3`   | `2`          | `ll`
`hello`      | `3`   | `10`         | `llo`
`hello`      | `-10` | `2`          | (empty string)
`hello`      | `-10` | `13`         | `he`
`hello`      | `-10` | `18`         | `hello`
`hello`      | `7`   | `2`          | (empty string)

### UPPER

The `UPPER` function transforms a string to uppercase. The case of each character is defined by the rules of the default locale.

The syntax is:

```
UPPER( string )
```

For example:

```sql
UPPER('A string')
Result: A STRING
```

## Numeric functions

### ABS

The `ABS` function returns the absolute value of a number.
The output value will have the same data type as the input value.

The syntax is:

```
ABS( number )
```

For example:

```sql
ABS(-23)
Result: 23

ABS(-23.6)
Result: 23.6

ABS(-23.65)
Result: 23.65

ABS(23.65)
Result: 23.65

ABS(23.65 * -1)
Result: 23.65
```

### CEIL or CEILING

The `CEIL` (or `CEILING`) function rounds the specified number up and returns the smallest number that is greater than or equal to the specified number.
The output value will have the same data type as the input value.

The syntax is:

```
CEIL ( number )
CEILING ( number )
```

For example:

```sql
CEIL(3.2)
Result: 4.0

CEIL(2.8)
Result: 3.0

CEIL(3)
Result: 3
```

### FLOOR

The `FLOOR` function returns the largest integer value that is smaller than or equal to the given argument.
The output value will have the same data type as the input value.

The syntax is:

```
FLOOR( number )
```

For example:

```sql
FLOOR(3.2)
Result: 3.0

FLOOR(2.8)
Result: 2.0

FLOOR(3)
Result: 3
```

### ROUND

The `ROUND` function returns the integer closest to the given argument.
The output value will have the same data type as the input value.

The syntax is:

```
ROUND ( number )
```

For example:

```sql
ROUND(3.2)
Result: 3.0

ROUND(2.8)
Result: 3.0

ROUND(3)
Result: 3
```

## Datetime functions

### EXTRACT

The `EXTRACT` function allows for extracting a datetime field, such as a year, month or day, from a datetime value.

The syntax is:

```bash
ExtractFunction ::= 'EXTRACT' '(' <ExtractField> 'FROM' <ValueExpression> ')'

ExtractField    ::=   'YEAR'
                    | 'MONTH'
                    | 'DAY'
                    | 'HOUR'
                    | 'MINUTE'
                    | 'SECOND'
                    | 'TIMEZONE_HOUR'
                    | 'TIMEZONE_MINUTE'
```

The fields `YEAR`, `MONTH` and `DAY` can be extracted from a date, a timestamp, or a timestamp with time zone.

For example:

```sql
EXTRACT(YEAR FROM DATE '2017-02-13')
Result: 2017

EXTRACT(MONTH FROM DATE '2017-02-13')
Result: 2

EXTRACT(DAY FROM DATE '2017-02-13')
Result: 13
```

The fields `HOUR`, `MINUTE` and `SECOND` can be extracted from a time, a timestamp, a time with time zone, or a timestamp with time zone.

For example:

```sql
EXTRACT(HOUR FROM TIME '12:05:03.201')
Result: 12

EXTRACT(MINUTE FROM TIME '12:05:03.201')
Result: 5

EXTRACT(SECOND FROM TIME '12:05:03.201')
Result: 3.201
```

The fields `TIMEZONE_HOUR` and `TIMEZONE_MINUTE` can be extracted from a time with time zone or a timestamp with time zone.

For example:

```sql
EXTRACT(TIMEZONE_HOUR FROM TIMESTAMP '2018-01-01 12:30:00-02:30')
Result: -2

EXTRACT(TIMEZONE_MINUTE FROM TIMESTAMP '2018-01-01 12:30:00-02:30')
Result: -30
```

## User-Defined functions

User-defined functions (UDFs) are invoked similarly to built-in functions. For example, a user may have registered a function `math.tan` that returns the tangent of a given angle.
An example invocation of this function is then:

```sql
  SELECT math.tan(n.angle) AS tangent
    FROM MATCH (n)
ORDER BY tangent
```

The syntax is:

```bash
FunctionInvocation   ::= <PackageSpecification>? <FunctionName> '(' <ArgumentList>? ')'

PackageSpecification ::= <PackageName> '.'

PackageName          ::= <Identifier>

FunctionName         ::= <Identifier>

ArgumentList         ::= <ValueExpression> ( ',' <ValueExpression> )*
```

Note that a function invocation has an optional package name, a (non-optional) function name, and, zero or more arguments which are arbitrary value expressions.

Function and package names are case-insensitive such that e.g. `in_degree(..)` is the same as `In_Degree(..)` or `IN_DEGREE(..)`.

If a UDF is registered that has the same name as a built-in function, then, upon function invocation, the UDF is invoked and not the built-in function. UDFs can thus override built-ins.

## CAST

While [_implicit_ type conversion](#implicit-type-conversion) is supported between numeric types, between time types, and between timezone types, other type conversions require _explicit_ conversion through casting (`CAST`).

The syntax is:

```bash
CastSpecification ::= 'CAST' '(' <ValueExpression> 'AS' <DataType> ')'

DataType          ::=   'STRING'
                      | 'BOOLEAN'
                      | 'INTEGER'
                      | 'INT'
                      | 'LONG'
                      | 'FLOAT'
                      | 'DOUBLE'
                      | 'DATE'
                      | 'TIME'
                      | 'TIME' 'WITH' 'TIME' 'ZONE'
                      | 'TIMESTAMP'
                      | 'TIMESTAMP' 'WITH' 'TIME' 'ZONE'
```

For example:

```sql
SELECT CAST(n.age AS STRING), CAST('123' AS INTEGER), CAST('09:15:00+01:00' AS TIME WITH TIME ZONE)
  FROM MATCH (n:Person)
```

Casting is allowed between the following data types:

| from \ to                | string | exact numeric | approximate numeric | boolean | time | time with time zone | date | timestamp | timestamp with time zone |
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

In the table above, `Y` indicates that casting is supported, `N` indicates that casting is not supported, and `M` indicates that casting is supported only if the numeric value is between the minimum and maximum values (inclusive) that can be represented by the specified target type.

## CASE

The `CASE` predicate returns an expression based on the evaluation of some given Boolean conditions.

There are two types of `CASE` expressions: "simple case" and "searched case".

The syntax is:

```bash
CaseExpression ::= <SimpleCase> | <SearchedCase>

SimpleCase     ::= 'CASE' <ValueExpression> <WhenClause>+ <ElseClause>? 'END'

SearchedCase   ::= 'CASE' <WhenClause>+ <ElseClause>? 'END'

WhenClause     ::= 'WHEN' <ValueExpression> 'THEN' <ValueExpression>

ElseClause     ::= 'ELSE' <ValueExpression>
```

The simple case provides a list of pairs (`WHEN` compare value, `THEN` return value) and optionally
an else clause (`ELSE` return value). PGQL compares a given expression to each compare value and
returns the corresponding return value when compared expressions are equal. If no equal expression
is found and an `ELSE` clause exists, then PGQL returns the given else value. If no `ELSE` clause
exists, null is returned.

For example:

```sql
CASE n.age
 WHEN 1 THEN 'One'
 WHEN 2 THEN 'Two'
 WHEN 3 THEN 'Three'
 ELSE 'Older than three'
END
```

The searched case provides a list of pairs (`WHEN` boolean expression, `THEN` return value) and optionally
an `else` clause (`ELSE` return value). PGQL evaluates each boolean expression until one of them evaluates
to true, and returns the corresponding return value. If no expression evaluates to true, and an `ELSE`
clause exists, then PGQL returns the given else value. If no `ELSE` clause exists, null is returned.

For example:

```sql
CASE
 WHEN n.level = 'user' THEN 0
 WHEN n.authorized THEN 1
 ELSE -1
END
```

## IN Predicate

The `[NOT] IN` predicate tests a value for membership in a list of values.
The PGQL literal types `INTEGER`, `DECIMAL`, `BOOLEAN`, `STRING`, `DATE`, `TIME [WITH TIME ZONE]`, `TIMESTAMP [WITH TIME ZONE]` are allowed in the list.

The syntax is:

```bash
InPredicate ::= <ValueExpression> ('NOT')? 'IN' <InValueList>

InValueList ::=   '(' <ValueExpression> ( ',' <ValueExpression> )* ')'
                | <BindVariable>
```

For example:

```sql
2 IN (2, 3, 5)
Result: true

3.2 IN (5, 4.8, 3.2)
Result: true

false IN (true, true)
Result: false

'Emily' IN ('Emily', 'Carl')
Result: true

DATE '1990-07-03' IN (DATE '1990-07-03', DATE '1993-05-28')
Result: true

TIME '12:00:10' IN (TIME '11:55:10', TIME '06:50:00.999+05:00')
Result: false

TIMESTAMP '2016-03-20 22:09:59.999' IN (TIMESTAMP '2016-03-20 23:09:59')
Result: false
```

Bind variables are also supported in the position of the list. For example:

```sql
SELECT n.date_of_birth
  FROM MATCH (n:Person)
 WHERE n.date_of_birth IN ? /* use PreparedStatement.setArray(int, java.util.List) */
```

# Subqueries

There are two types of subqueries:

 - [EXISTS and NOT EXISTS Subqueries](#exists-and-not-exists-subqueries).
 - [Scalar Subqueries](#scalar-subqueries).
 - [LATERAL Subqueries](#lateral-subqueries).

Both types of subqueries can be used as a value expression in a `SELECT`, `WHERE`, `GROUP BY`, `HAVING` and `ORDER BY` clauses (including `WHERE` clauses of `PATH` expressions). An `EXISTS` or `NOT EXISTS` subquery returns a boolean while a scalar subquery returns a value of any of the supported [data types](#data-types-and-literals).

## EXISTS and NOT EXISTS Subqueries

`EXISTS` returns true/false depending on whether the subquery produces at least one result, given the bindings obtained in the current (outer) query. No additional binding of variables occurs.

The syntax is:

```bash
ExistsPredicate ::= 'EXISTS' <Subquery>

Subquery        ::= '(' <Query> ')'
```
An example is to find friend of friends, and, for each friend of friend, return the number of common friends:

```sql
SELECT fof.name, COUNT(friend) AS num_common_friends
  FROM MATCH (p:Person) -[:has_friend]-> (friend:Person) -[:has_friend]-> (fof:Person)
 WHERE NOT EXISTS ( SELECT * FROM MATCH (p) -[:has_friend]-> (fof) )
```

Here, vertices `p` and `fof` are passed from the outer query to the inner query. The `EXISTS` returns true if there is at least one `has_friend` edge between vertices `p` and `fof`.

## Scalar Subqueries

Scalar subqueries are queries that return a scalar value (exactly one row and exactly one column) such that they can be part of an expression in a `SELECT`, `WHERE`, `GROUP BY`, `HAVING` or `ORDER BY` clause.

The syntax is:

```bash
ScalarSubquery ::= <Subquery>
```

For example:

```sql
SELECT a.name
  FROM MATCH (a)
 WHERE a.age > ( SELECT AVG(b.age) FROM MATCH (a) -[:friendOf]-> (b) )
```

Another example is:

{% include image.html file="example_graphs/financial_transactions.png" %}

```sql
  SELECT p.name AS name
       , ( SELECT SUM(t.amount)
             FROM MATCH (a) <-[t:transaction]- (:Account)
                     ON financial_transactions
         ) AS sum_incoming
       , ( SELECT SUM(t.amount)
             FROM MATCH (a) -[t:transaction]-> (:Account)
                     ON financial_transactions
         ) AS sum_outgoing
       , ( SELECT COUNT(DISTINCT p2)
             FROM MATCH (a) -[t:transaction]- (:Account) -[:owner]-> (p2:Person)
                     ON financial_transactions
            WHERE p2 <> p
         ) AS num_persons_transacted_with
       , ( SELECT COUNT(DISTINCT c)
             FROM MATCH (a) -[t:transaction]- (:Account) -[:owner]-> (c:Company)
                     ON financial_transactions
         ) AS num_companies_transacted_with
    FROM MATCH (p:Person) <-[:owner]- (a:Account) ON financial_transactions
ORDER BY sum_outgoing + sum_incoming DESC
```

```
+-----------------------------------------------------------------------------------------------------+
| name    | sum_incoming | sum_outgoing | num_persons_transacted_with | num_companies_transacted_with |
+-----------------------------------------------------------------------------------------------------+
| Liam    | 9999.5       | 9900.0       | 1                           | 1                             |
| Camille | 9900.0       | 1000.0       | 2                           | 0                             |
| Nikita  | 1000.0       | 4501.0       | 1                           | 1                             |
+-----------------------------------------------------------------------------------------------------+
```

Note that in the query, the graph name `financial_transactions` is repeatedly specified. Such repetition can be avoided by using a [default graph](#default-graphs), which simplifies the query:

```sql
  SELECT p.name AS name
       , ( SELECT SUM(t.amount)
             FROM MATCH (a) <-[t:transaction]- (:Account)
         ) AS sum_incoming
       , ( SELECT SUM(t.amount)
             FROM MATCH (a) -[t:transaction]-> (:Account)
         ) AS sum_outgoing
       , ( SELECT COUNT(DISTINCT p2)
             FROM MATCH (a) -[t:transaction]- (:Account) -[:owner]-> (p2:Person)
            WHERE p2 <> p
         ) AS num_persons_transacted_with
       , ( SELECT COUNT(DISTINCT c)
             FROM MATCH (a) -[t:transaction]- (:Account) -[:owner]-> (c:Company)
         ) AS num_companies_transacted_with
    FROM MATCH (p:Person) <-[:owner]- (a:Account)
ORDER BY sum_outgoing + sum_incoming DESC
```

## LATERAL Subqueries

A `LATERAL` subquery can be any PGQL SELECT query and all functions of PGQL SELECT queries are supported inside a `LATERAL` subquery.
A `LATERAL` subquery can project any number of columns and it can have an arbitrary number of result rows.
All projected elements are available outside a `LATERAL` subquery, and only projected variables are visible to the outer query.

The syntax is:

```bash
LateralSubquery ::= 'LATERAL' <Subquery>
```

In the following query, the `LATERAL` subquery projects the two vertices `a` and `p`, while the outer accesses properties of those vertices.

```sql
SELECT p.name, a.number
FROM LATERAL ( SELECT a, p
               FROM MATCH (a:Account) -> (p:Person)
             )
```

Alternatively, properties can also be projected by the `LATERAL` subquery and then be referenced by the outer query.

```sql
SELECT name, number
FROM LATERAL ( SELECT p.name as name, a.number as number
               FROM MATCH (a:Account) -> (p:Person)
             )
```

```sql
SELECT p.name, number
FROM LATERAL ( SELECT p, a.number as number
               FROM MATCH (a:Account) -> (p:Person)
             )
```

### Variable Renaming

Variables can be renamed in the projection of a `LATERAL` subquery. In this case, the new name has to be used to reference a variable.

```sql
SELECT account.name, person.number
FROM LATERAL ( SELECT a as account, p as person
               FROM MATCH (a:Account) -> (p:Person)
             )
```

### Nesting Of LATERAL Subqueries

`LATERAL` subqueries can be nested. There is no limit on the nesting level.

For example:

```sql
SELECT name, number
FROM LATERAL ( SELECT a.name, p.number
               FROM LATERAL ( SELECT a,p
                              FROM MATCH (a:Account) -> (p:Person)
                            )
             )
```

### LATERAL Followed By MATCH

A `LATERAL` subquery can be followed by one or more `MATCH` clauses. Vertices projected by a `LATERAL` subquery can be used in subsequent `MATCH` clauses.

In the example below `(a)` in the outer `MATCH` clause, is the same `(a)` projected in the `LATERAL` subquery.
```sql
SELECT p.name as pName, p1.name as p1Name
FROM LATERAL ( SELECT a, p
               FROM MATCH (a:Account) -> (p:Person)
             ),
     MATCH (a) -> (a1:Account) -> (p1:Person)
```

The `WHERE` clause of the outer query can contain all variables projected in the `LATERAL` subquery and variables in the outer `MATCH` clause.

```sql
SELECT p.name as pName, p1.name as p1Name
FROM LATERAL ( SELECT a, p
               FROM MATCH (a:Account) -> (p:Person)
             ),
     MATCH (a) -> (a1:Account) -> (p1:Person)
```


### Reusing Of Variable Names

Variables not projected in the `LATERAL` subquery are not visible outside the `LATERAL` subquery.
Therefore, variables in the outer query with the same name are new variables and independent of the variable in the `LATERAL` subquery with the same name.


```sql
SELECT p.name
FROM LATERAL ( SELECT a
               FROM MATCH (a:Account) -> (p:Person)
             ),
     MATCH (a) -> (a1:Account) -> (p:Person)
```

#### GROUP BY in `LATERAL` subquery

`GROUP BY` is (like every other function of PGQL SELECT queries) supported inside a `LATERAL` subquery.
If a vertex is projected after a `LATERAL` subquery, it can be used in a subsequent `MATCH` clause.

```sql
SELECT p.name, ARRAY_AGG(a.number)
FROM LATERAL ( SELECT p, SUM(t.amount) AS sum
               FROM MATCH (a1:Account) -[t:transaction] - (a:Account) -> (p:Person)
               GROUP BY p
               HAVING sum > 5000
             ),
     MATCH (p) <- (a:Account)
     GROUP BY p
```

### Unnesting In LATERAL Subquery
Variables from path unnesting with `ONE ROW PER STEP` or `ONE ROW PER VERTEX` can be used like any other variable projected in a `LATERAL` subquery.
This includes using unnested vertices being used in a subsquent `MATCH` clause

```sql
SELECT p.name as pName, p1.name as p1Name
FROM LATERAL (SELECT p, v
              FROM MATCH (p:Person) <- (a:Account),
                   MATCH ANY (a) ->* (a1:Account) ONE ROW PER VERTEX (v)),
     MATCH (v) -> (p1:Person)
```

# Graph Modification

```bash
ModifyQuery       ::=   <ModifyQuerySimple>
                      | <ModifyQueryFull>

ModifyQuerySimple ::= <InsertClause>

ModifyQueryFull   ::= <Modification>+
                      <FromClause>
                      <WhereClause>?
                      <GroupByClause>?
                      <HavingClause>?
                      <OrderByClause>?
                      <OffsetClause>?
                      ( <FetchFirstClause> | <LimitClause> )?

Modification      ::=   <InsertClause>
                      | <UpdateClause>
                      | <DeleteClause>
```

Modifications follow snapshot isolation semantics, meaning that insertions, updates and deletions within the same query do not see each other's results.

## INSERT

```bash
InsertClause            ::= 'INSERT' <IntoClause>? <GraphElementInsertion> ( ',' <GraphElementInsertion> )*

IntoClause              ::= 'INTO' <GraphReference>

GraphElementInsertion   ::=   'VERTEX' <VariableName>? <LabelsAndProperties>
                            | 'EDGE' <VariableName>? 'BETWEEN' <VertexReference> 'AND' <VertexReference>
                                     <LabelsAndProperties>

VertexReference         ::= <Identifier>

LabelsAndProperties     ::= <LabelSpecification>? <PropertiesSpecification>?

LabelSpecification      ::= 'LABELS' '(' <Label> ( ',' <Label> )* ')'

PropertiesSpecification ::= 'PROPERTIES' '(' <PropertyAssignment> ( ',' <PropertyAssignment> )* ')'

PropertyAssignment      ::= <PropertyReference> '=' <ValueExpression>
```


PGQL supports the insertions of edges and vertices into a graph.
In the same query, multiple vertices and edges can be inserted by enumerating them after the `INSERT` keyword.
All inserted entities must be identified with a variable name that has to be unique for the whole modification query.

So the following query should fail, because the variable `x` is not only local to the vertex insertion term:

```sql
INSERT VERTEX x, VERTEX x
```

The id values for the inserted entities are automatically generated.

### Inserting vertices

Vertices can be inserted with or without a match.

If the match is missing, one unconnected vertex is inserted to the graph. For example in case of the following query

```sql
INSERT VERTEX x LABELS ( Male ) PROPERTIES ( x.age = 22 )
```

In the presence of a match, as many vertices are inserted as many rows are matched.
So the following query inserts a new vertex for every vertex in the graph that is labelled `Male`.


```sql
INSERT VERTEX x LABELS ( Male ) PROPERTIES ( x.age = y.age )
  FROM MATCH (y:Male)
```

In the presence of a `GROUP BY` expression, as many vertices are inserted, as many groups are matched.
For example the following query inserts a new vertex for every profession in the graph.

```sql
  INSERT VERTEX x LABELS ( Profession ) PROPERTIES ( x.name = y.profession )
    FROM MATCH (y:Person)
GROUP BY y.profession
```

### Inserting edges

Edges can be inserted by specifying the source and destination vertices.
Only the insertion of directed edges are supported.

For example the following query inserts a vertex with source `x` and destination `y`:

```sql
INSERT EDGE e BETWEEN x AND y
  FROM MATCH (x)
     , MATCH (y)
 WHERE id(x) = 1 AND id(y) = 2
```

### Labels

Labels for the inserted entities can be specified between braces after the `LABELS` keyword.

For example:

```sql
INSERT EDGE e BETWEEN x AND y LABELS ( knows )
  FROM MATCH (x:Person)
     , MATCH (y:Person)
 WHERE id(x) = 1 AND id(y) = 2
```

### Properties

Properties can be specified between braces after the `PROPERTIES` keyword.
On the right-hand-side of the expression, the property name must be preceded by the variable name and a dot.
Property assignments can be arbitrary expressions with similar restrictions as property assignments in case of update queries.
Property expressions cannot refer to other entities that are inserted at the same time.

For example, the following query inserts a new vertex with `age = 22`:

```sql
INSERT VERTEX v PROPERTIES ( v.age = 22 )
```

Edge properties can be specified in the same manner:

```sql
INSERT EDGE e BETWEEN x AND y LABELS ( knows ) PROPERTIES ( e.since = DATE '2017-09-21' )
  FROM MATCH (x:Person)
     , MATCH (y:Person)
 WHERE id(x) = 1 AND id(y) = 2
```

In case of partitioned schema, only those properties can be assigned that are defined for the type of the entity.
Note that the entity type is determined by the label(s).

### Multiple inserts in the same INSERT clause

One insert clause can contain multiple inserts.

For example, the query below inserts two vertices into the graph:

```sql
INSERT
  VERTEX v LABELS ( Male ) PROPERTIES ( v.age = 23, v.name = 'John' ),
  VERTEX u LABELS ( Female ) PROPERTIES ( u.age = 24, u.name = 'Jane' )
```

Multiple insertions under the same `INSERT` can be used to set a newly inserted vertex as source or destination for a newly inserted edge.

For example, the following query inserts a vertex and an edge that connects it to the matched vertex `y`:

```sql
INSERT VERTEX x LABELS ( Person ) PROPERTIES ( x.name = 'John' )
     , EDGE e BETWEEN x AND y LABELS ( knows ) PROPERTIES ( e.since = DATE '2017-09-21' )
  FROM MATCH (y)
 WHERE y.name = 'Jane'
```

Note that the properties of `x` cannot be accessed in the property assignments of `e`, only the variable itself is visible as source of the edge.
For this reason setting `e.since` to `x.graduation_date` would cause the query to fail.


In the presence of a match, as many edges are inserted as many (not necessarily unique) vertex pairs are matched.
If a vertex pair is matched more than once, multiple edges will be inserted between the vertices.

For example consider the following query:
```sql
INSERT EDGE e BETWEEN x AND y
  FROM MATCH (x)
     , MATCH (y) -> (z)
 WHERE id(x) = 1
```

{% include image.html file="example_graphs/pgql_modify_example_before.png" %}

If the query is executed on the graph above, the following vertices will be matched

x | y | z |
--- | --- | ---
V1 | V2 | V4 |
V1 | V3 | V2
V1 | V3 | V4


In that case, three edges will be inserted, one connecting `V1` and `V2` and two different edges, both connecting `V1` and `V3` as it is shown below.

{% include image.html file="example_graphs/pgql_modify_example_after.png" %}

## UPDATE

The `UPDATE` clause allows for setting the properties of one or more vertices and edges.

The syntax is:

```bash
UpdateClause       ::= 'UPDATE' <GraphElementUpdate> ( ',' <GraphElementUpdate> )*

GraphElementUpdate ::= <ElementReference> 'SET' '(' <PropertyAssignment> ( ',' <PropertyAssignment> )* ')'
```

For example, the following query sets the property `age` of every person named "John" to the value `42`:

```sql
UPDATE x SET ( x.age = 42 )
  FROM MATCH (x:Person)
 WHERE x.name = 'John'
```

An example in which properties of multiple vertices and edges are update is:

```sql
UPDATE v SET ( v.carOwner = true )
     , u SET ( u.weight = 3500 )
     , e SET ( e.since = DATE '2010-01-03' )
  FROM MATCH (v:Person) <-[e:belongs_to]- (u:Car)
 WHERE v.name = 'John'
```

Above, we match a person named John and the car that belongs to John. We then set the property `carOwner` of John to true, we set the property `weight` of the car to 3500, and we set the property `since` of the `belongs_to` edge to the date 2010-01-03.

### Handling read after write conflicts

During the update, the assigned values (right-hand-side of assignments) correspond to the graph property values
before the beginning of the update. This aligns with the snapshot isolation semantics defined between modifications in the same query.

For example consider the following update:

```sql
UPDATE x SET ( x.a = y.b, x.b = 12 )
  FROM MATCH (x) -> (y)
```

It is possible, that a vertex is matched by both `(x)` and `(y)` for example

x | y
--- | ---
V1 | V2
V3 | V1

Supposing that `V1.b` was `20` before executing the update, `V1.b` will be assigned 12 `V3.a` will be assigned `20` no
matter in which order the updates are executed.

### Handling write after write conflicts

Multiple writes to the same property of the same entity are not allowed, in such cases the execution terminates with
an error.

For example consider the following query:

```sql
UPDATE x SET ( x.a = y.a )
  FROM MATCH (x) -> (y)
```

If the following vertices are matched

x | y
--- | ---
V1 | V2
V1 | V3

a runtime exception will be thrown, because the value assigned to `V1.a` could be ambiguous.

As an extension to this semantics, PGX implements a more relaxed version for conflicting write checks.
If the assigned value can be statically guaranteed to be only depending on property values of the entity it is
assigned to, then even in case of multiple assignments, (since the assigned value is always the same) the update
succeeds.

For example, in the following case, multiple writes to `v.a` are allowed, because in this case no matter how many
times `v.a` is written, it is always assigned the same value (65 minus its age property).

```sql
UPDATE v SET ( v.a = 65 - v.age )
  FROM MATCH (v:Person) -> (u:Person)
 WHERE v.name = 'John'
```

In the following case, however, multiple writes to `v.a` are not allowed, because the value of the property would be
ambiguous, 65 minus the other vertex's age property, that can be different for different matched `u`'s.

```sql
UPDATE v SET ( v.a = 65 - u.age )
  FROM MATCH (v:Person) -> (u:Person)
 WHERE v.name = 'John'
```

## DELETE

```bash
DeleteClause ::= 'DELETE' <ElementReference> ( ',' <ElementReference> )*
```


Entities can be deleted by enumerating them after the `DELETE` keyword. The order of enumeration does not affect the result of the execution.

For example, one can delete all edges from a graph using the following query

```sql
DELETE e
  FROM MATCH () -[e]-> ()
```

Multiple deletes to the same entity are not considered conflicting. For example consider the following query:

```sql
DELETE x, y
  FROM MATCH (x) -> (y)
```

In that case, even if a vertex is matched multiple times by `(x)` or `(y)`, and deleted multiple times, the query will complete without an exception.


If a vertex is deleted, all its incoming and outgoing edges are deleted as well, thus there are no dangling edges left after a query.
So the following query not only deletes the vertex with id `11` but also all edges for which it is source or destination.

```sql
DELETE x
  FROM MATCH (x)
 WHERE id(x) = 11
```

Because of implicit deletion of edges, the following query can be used to delete all edges as well as all vertices from a graph:

```sql
DELETE x
  FROM MATCH (x)
```

## Combining INSERT, UPDATE and DELETE

Multiple modifications can be executed in the same query.
For example, to update a vertex and also insert an edge with the same vertex as source, the following query can be used:

```sql
INSERT EDGE e BETWEEN x AND y
UPDATE y SET ( y.a = 12 )
  FROM MATCH (x), MATCH (y)
 WHERE id(x) = 1 AND id(y) = 2
```

### Isolation semantics of modification queries

Modify queries follow snapshot isolation, which means all modifications see a consistent state of the graph, that is its state before the execution of the update.
For this reason, property assignments can come from updated and deleted vertices, but they cannot refer to inserted vertices.

For example, the query below succeeds, because `y.age` is evaluated based on the graph's status before the query.

```sql
INSERT VERTEX x PROPERTIES ( x.age = y.age )
DELETE y
  FROM MATCH (y)
```

Please note, that for the same reason, properties of newly inserted vertices cannot be referenced in the right-hand-side expressions.
For example, the following query would fail as `x` is not yet in the graph, and `x.age` cannot be evaluated:

```sql
INSERT VERTEX x PROPERTIES ( v.age = 24 )
     , VERTEX y PROPERTIES ( y.age = x.age )
```

### Handling conflicting modifications

Multiple modifications on the same entity are not allowed, in such cases the execution terminates with
an error. This section only addresses conflicts between different modifications under the same query.
For the conflicts within the same modification, please refer to the corresponding sections.

One example for such conflict would be the UPDATE-DELETE conflicts.
The same entity cannot be updated and deleted in the same query.

For example, let us consider the following query:

```sql
UPDATE x SET ( x.a = 11 )
DELETE x
  FROM MATCH (x)
```

There the conflict is trivial between the deleted and the updated vertex.
However, the conflict is not always straightforward, for example,
the following query can also fail due to conflicting update and delete:

```sql
UPDATE x SET ( x.a = 11 )
DELETE y
  FROM MATCH (x) -> (y)
```

If the vertices matched by `x` are distinct to the ones matched by `y` the query should pass, however, if there is a vertex that is matched by both `x` and `y` the query will fail with an exception.
Note that the order of modifications does not matter, the query will fail in any case.

Similar behavior is expected upon INSERT-DELETE conflicts, where the inserted entity depends on an entity that is being deleted.
Note that because of the snapshot semantics, this is only possible if an edge is inserted, and at the same time its source or destination vertex is deleted.

For example, consider the following, not trivial case:

```sql
INSERT EDGE e BETWEEN x AND y
DELETE z
  FROM MATCH (x) -> (y), MATCH (z)
 WHERE id(z) = 11
```

If any vertex is matched by `z` and either `x` or `z` then after executing the query the inserted edge would not have a source or destination.
Thus in that case the execution fails.

# Other Syntactic rules

## Identifiers

Graph names, property names, labels, etc. are identifiers that can appear in either unquoted form or double quoted form.

The syntax is:

```bash
Identifier           ::= <UNQUOTED_IDENTIFIER> | <QUOTED_IDENTIFIER>
```

### Unquoted identifiers

Unquoted identifiers take the form of an alphabetic character followed by zero or more alphanumeric or underscore (i.e. `_`) characters:

```bash
UNQUOTED_IDENTIFIER  ::= [a-zA-Z][a-zA-Z0-9\_]*
```

Unquoted identifiers are automatically uppercased.

For example, the following two queries are equivalent:

```sql
SELECT n.dob AS name
  FROM MATCH (n:Person) ON myGraph
 WHERE n.firstName = 'Nikita'
```

```sql
SELECT "N"."DOB"
  FROM MATCH ("N":"PERSON") ON "MYGRAPH"
 WHERE "N"."FIRSTNAME" = 'Nikita'
```

Note that this is aligned to SQL, which also automatically uppercases unquoted identifiers.
However, as an extension to SQL — which matches uppercased references in exact manner — PGQL matches uppercased references to graphs, labels and properties in case-insensitive manner if no exact match exists.

For example, a property `firstName` in the graph can be referenced in PGQL either through `firstName`, `"firstName"`, `"FIRSTNAME"` or `fIrStNaMe`, but not through `"FirstName"`.

### Quoted identifiers

Quoted identifiers are delimited with double quotes and support the full range of Unicode characters:

```bash
QUOTED_IDENTIFIER            ::= '"' ( ~[\"] | <ESCAPED_IDENTIFIER_CHARACTER> )* '"'

ESCAPED_IDENTIFIER_CHARACTER ::= '""'
```

Above says that a quoted identifier starts and ends with double quotes and in between has any number of:
 - Unicode characters except for the double quote character
 - An escaped double quote in the form of two double quotes

Note that the syntax of a PGQL identifier is different from a string literal in languages like Java or C++,
because unlike in Java and C++, characters like a new line or a backslash are not escaped in PGQL;
in identifiers in PGQL, only double quotes are escaped.

For example, take the following string:

```
My string with single quotes ', double quotes ", backslashes \
new lines and tabs	.
```

Here is an example of how to use such a string as a property name in PGQL:

```sql
SELECT *
  FROM MATCH (n)
 WHERE n."My string with single quotes ', double quotes "", backslashes \
new lines and tabs	." = 123
```

As you can see, only the double quote (`"`) was escaped (`""`).

## String literals

The syntax for string literals is:

```bash
STRING_LITERAL                   ::= "'" ( ~[\'] | <ESCAPED_STRING_LITERAL_CHARACTER> )* "'"

ESCAPED_STRING_LITERAL_CHARACTER ::= "''"
```

Above says that a string literal starts and ends with single quotes and in between has any number of:
 - Unicode characters except for the single quote character
 - An escaped single quote in the form of two single quotes

Note that this is different from string literals in languages like Java or C++.
First of all, PGQL string literals are single-quoted instead of double-quoted.
Second, unlike in Java and C++, characters like a new line or a backslash are not escaped in PGQL;
in string literals in PGQL, only single quotes are escaped.

For example, take the following string:

```
My string with single quotes ', double quotes ", backslashes \
new lines and tabs	.
```

Here is an example of how to use such a string as literal in PGQL:

```sql
SELECT *
  FROM MATCH (n)
 WHERE n.prop = 'My string with single quotes '', double quotes ", backslashes \
new lines and tabs	.'
```

As you can see, only the single quote (`'`) was escaped (`''`).

## Keywords

The following is a list of keywords in PGQL.

```sql
SELECT, FROM, MATCH, ON, WHERE, GROUP,
BY, HAVING, ORDER, ASC, DESC, OFFSET,
AND, OR, NOT, true, false, IS, NULL, AS,
DATE, TIME, TIMESTAMP, WITH, ZONE, DISTINCT,
COUNT, MIN, MAX, AVG, SUM, ARRAY_AGG, LISTAGG,
IN, EXISTS, CAST, CASE, WHEN, THEN, ELSE, END,
EXTRACT, YEAR, MONTH, DAY, HOUR, MINUTE,
SECOND, TIMEZONE_HOUR, TIMEZONE_MINUTE,
SHORTEST, CHEAPEST, COST, CREATE, DROP,
PROPERTY, GRAPH, VERTEX, EDGE, TABLES, KEY,
SOURCE, DESTINATION, OF, REFERENCES, PROPERTIES,
LABELED, FETCH, FIRST, NEXT, ROWS, ONLY,
LABEL, PROPERTIES, ARE, ALL, COLUMNS,
EXCEPT, NO, INSERT, UPDATE, DELETE, INTO,
LABELS, SET, BETWEEN, INTERVAL, ONE, ROW,
PER, STEP, PREFIX, WALK, ACYCLIC, SIMPLE, TRAIL,
GRAPH_TABLE, COLUMNS, LATERAL
```

Keywords are case-insensitive and variations such as `SELECT`, `Select` and `sELeCt` can be used interchangeably.

## Integers and Decimals

Lexical grammar for integers and decimals is:

```bash
UNSIGNED_INTEGER     ::= [0-9]+

UNSIGNED_DECIMAL     ::= ( [0-9]* '.' [0-9]+ ) | ( [0-9]+ '.' )
```

These rules describe the following:

 - Unsigned integers consist of one or more digits.
 - Unsigned decimals either consist of zero or more digits followed by a dot (`.`) and one or more digits, or, the conceit of one or more digits followed by only a dot (`.`).

## Comments

Comments are delimited by `/*` and `*/`.

The syntax is:

```bash
COMMENT ::= '/*' ~[\*]* '*/'
```

For example:

```sql
/* This is a
   multi-line
   comment. */
SELECT n.name, n.age
  FROM MATCH (n:Person) /* this is a single-line comment */
```
