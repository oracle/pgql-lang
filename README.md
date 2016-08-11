# PGQL: a Property Graph Query Language

PGQL is a SQL-like graph query language for the Property Graph data model.

This repository contains PGQL's reference implementation:

 - Parser
 - Graph Query IR
 - Tests

## Build and Install

PGQL can be built on Windows, Linux and Mac OS X and requires Java 1.8 or higher and Maven 3.3.9 or higher. Note that although Java 1.8 or higher is required to *build* PGQL, Java 1.7 or higher can be used to *run* PGQL.

On Linux / Mac OS X:

 - To build, run `sh build.sh`
 - To install to your local Maven repository, run `sh install.sh`

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

The PGQL 1.0 language specification can be found [here](https://oracle.github.io/pgql-lang/pgql-1.0-specification.html).

## Development

- Download Eclipse with Spoofax pre-installed [here](https://spoofax.readthedocs.io/en/latest/source/langdev/start.html)
- Import the following projects into Eclipse (`File>Import...>Maven>Existing Maven Projects>Browse...`):
    - `graph-query-ir`: Java representation of graph queries
    - `pqgl-spoofax`: Spoofax implementation of PGQL (parser + error checks)
    - `pgql-lang`: translation of Spoofax AST into `graph-query-ir`

## Contributions

PGQL is an open source project. See [Contributing](CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to PGQL made by the community.
