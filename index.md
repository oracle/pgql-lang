---
title: PGQL
permalink: /index.html
toc: false
keywords: pgql property graph query language database analytics oracle sql standard gql cypher opencypher sparql gsql pgx big data spatial
---

Graph pattern matching
====================================

PGQL is a graph query language built on top of SQL, bringing graph pattern matching capabilities to existing SQL users as well as to new users who are interested in graph technology but who do not have an SQL background.

A high-level overview of PGQL
-----------------------------

Alongside SQL constructs like `SELECT`, `FROM`, `WHERE`, `GROUP BY` and `ORDER BY`, PGQL allows for matching fixed-length graph patterns and variable-length graph patterns.
Fixed-length graph patterns match a fixed number of vertices and edges per solution.
The types of the vertices and edges can be defined through arbitrary label expressions such as `friend_of|sibling_of`, for example to match edges that have either the label `friend_of` or the label `sibling_of`.
This means that edge patterns are higher-level joins that can relate different types of entities at once.
Variable-length graph patterns, on the other hand, contain one or more quantifiers like `*`, `+` or `{2,4}` for matching vertices and edges in a recursive fashion.
This allows for encoding graph reachability (transitive closure) queries as well as shortest and cheapest path finding queries.

PGQL is an [open-sourced project](https://github.com/oracle/pgql-lang), and we welcome contributions or suggestions from anyone and in any form.

A basic example
----------

An example property graph is:

{% include image.html file="example_graphs/financial_transactions.png" %}

Above, `Person`, `Company` and `Account` are vertex labels while `owner`, `worksFor` and `transaction` are edge labels.
Furthermore, `name` and `number` are vertex properties while `amount` is an edge property.

Assume that this graph is stored in the following tables in a database:

{% include image.html file="example_graphs/financial_transactions_schema.png" %}

From these tables we can create the desired graph as follows:

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

After we created the graph, we can run a `SELECT` query to "produce an overview of account holders that have transacted with a person named Nikita":

```sql
  SELECT owner.name AS account_holder, SUM(t.amount) AS total_transacted_with_Nikita
    FROM MATCH (p:Person) <-[:owner]- (account1:Account)
       , MATCH (account1) -[t:transaction]- (account2) /* match both incoming and outgoing transactions */
       , MATCH (account2:Account) -[:owner]-> (owner:Person|Company)
   WHERE p.name = 'Nikita'
GROUP BY owner
```

Or, using PGQL with SQL Standard syntax:

```sql
SELECT account_holder, SUM(amount) AS total_transacted_with_Nikita
FROM
  GRAPH_TABLE ( financial_transactions
    MATCH
      (p IS Person) <-[IS owner]- (account1 IS Account),
      (account1) -[t IS transaction]- (account2), /* match both incoming and outgoing transactions */
      (account2 IS Account) -[IS owner]-> (owner IS Person|Company)
    WHERE p.name = 'Nikita'
    COLUMNS ( owner.name AS account_holder, t.amount )
  )
GROUP BY account_holder
```

The result is:

```
+----------------+------------------------------+
| account_holder | total_transacted_with_Nikita |
+----------------+------------------------------|
| Camille        | 1000.00                      |
| Oracle         | 4501.00                      |
+----------------+------------------------------+
```

Please see the [PGQL Specification](spec/latest/) for more examples and a detailed specification of the language.

Relationship to the SQL Standard
----------------------------

In its latest revision, the SQL Standard added support for property graphs ([SQL:2023 Part 16: SQL/PGQ – Property Graph Queries](https://www.iso.org/standard/79473.html)).
SQL/PGQ allows the user to create property graphs on top of one or more existing relational tables,
and query these graphs natively using a powerful new operator, called GRAPH_TABLE, which provides a graph pattern matching language fully integrated into SQL.

The plan for PGQL is to align it with the SQL Standard where possible.
As such, the GRAPH_TABLE operator as well as SQL's CREATE PROPERTY GRAPH statement were previously added to PGQL.

PGQL also has a custom syntax that was developed before SQL:2023 was finalized, which is referred to in the PGQL Specification as 'PGQL with custom syntax'.
Queries with custom syntax can be easily distinguished from queries with SQL Standard syntax as they do not use the GRAPH_TABLE operator.
The plan is to maintain the custom syntax over time and give users the choice to use whichever syntax they prefer.

Property Graphs in Oracle Database
----------------------------

PGQL has been part of the Oracle Database as a standalone language since Oracle Database 12.2.
However, in Oracle Database 23ai, property graphs were added directly into SQL as a more integrated feature of the converged database.
Nevertheless, PGQL remains available in Oracle Database 23ai and users can [choose](https://blogs.oracle.com/database/post/querying-graphs-with-sql-and-pgql-what-is-the-difference) the language that best suits their needs and can easily migrate between them.

{::nomarkdown}
<a href="https://www.oracle.com/database/graph/">
  <img src="images/oracle_graph_database.png" alt="Integrated Graph Database" style="width:500px;">
</a>
{:/}
