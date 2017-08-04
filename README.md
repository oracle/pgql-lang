# PGQL: a Property Graph Query Language

PGQL is an SQL-like query language for the Property Graph data model.
See [PGQL Home](http://pgql-lang.org/) and [PGQL 1.0 Specification](http://pgql-lang.org/spec/1.0/).

This reposistory contains:

 - The specification of PGQL
 - A parser for PGQL that provides various static error checks to validate queries
 - An intermediate representation of graph queries (see [GraphQuery.java](graph-query-ir/src/main/java/oracle/pgql/lang/ir/GraphQuery.java))
    - GraphQuery objects are returned by the parser and they can be used as a starting point when implementing a graph query engine
 - PGQL compatibility tests (in-progress)

## Build and Install

PGQL can be built on Windows, Linux and Mac OS X and requires Java 1.8 or higher and Maven 3.3.9 or higher.

On Linux / Mac OS X:

 - To build and install to your local Maven repository, run `sh install.sh`

You can easily adapt the scripts to work with Windows.

## Getting Started

First, build and install PGQL like explained above. Then, execute `cd example; sh run.sh` to parse two [example](example/src/main/java/oracle/pgql/lang/example/Main.java) queries:

```java
public class Main {

  public static void main(String[] args) throws PgqlException {

    Pgql pgql = new Pgql();

    // parse query and print graph query
    PgqlResult result1 = pgql.parse("SELECT n WHERE (n) -[e]-> (m)");
    System.out.println(result1.getGraphQuery());

    // parse query with errors and print error messages
    PgqlResult result2 = pgql.parse("SELECT x, y, WHERE (n) -[e]-> (m)");
    System.out.println(result2.getErrorMessages());
  }
}
```

The AST returned by the parser is a [GraphQuery](graph-query-ir/src/main/java/oracle/pgql/lang/ir/GraphQuery.java) object. This would be the input to your query planner.

## Documentation

See [PGQL 1.0 Specification](http://pgql-lang.org/spec/1.0/).

## Development

- Download Eclipse with Spoofax 2.1.0 pre-installed [here](http://www.metaborg.org/en/latest/source/release/note/2.1.0.html)
- Import the following projects into Eclipse (`File>Import...>Maven>Existing Maven Projects>Browse...`):
    - `graph-query-ir`: Java representation of graph queries
    - `pqgl-spoofax`: Spoofax implementation of PGQL (parser + error checks)
    - `pgql-lang`: translation of Spoofax AST into `graph-query-ir`

## Contributions

PGQL is an open source project. See [Contributing](CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to PGQL made by the community.
