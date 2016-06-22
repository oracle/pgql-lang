# PGQL: a Property Graph Query Language

PGQL is a SQL-like graph query language for the Property Graph data model.

This repository contains PGQL's reference implementation:

 - Parser
 - Graph Query IR
 - Tests

## Build and Install

PGQL can be build on Windows, Linux and Mac OS X and requires Java 1.7 or higher and Maven 3.3.9 or higher.

 - To build, run `sh build.sh`
 - To install to your local Maven repository, run `sh install.sh`

## Getting started

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

The language specification can be found [here](https://docs.oracle.com/cd/E56133_01/1.2.0/PGQL_Specification.pdf).

## Contributions

PGQL is an open source project. See [Contributing](CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to PGQL made by the community.
