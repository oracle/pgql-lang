# PGQL : a Property Graph Query Language

This repository contains PGQL's reference implementation:

 - Parser
 - Tests

## Building the parser

PGQL can be build on Windows, Linux and Mac OS X and requires Java 1.7 or higher and Maven 3.3.9 or higher.

To build, run `sh build.sh`.

## Deploying the parser to a Maven repository

See `deploy_oracle-internal.sh` for an example.

## Using the parser

First, build and deploy the parser. Then, use it like in the [example](example/src/main/java/oracle/pgql/lang/example/Main.java):

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
