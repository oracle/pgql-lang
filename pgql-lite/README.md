# Using the parser from a Spoofax project in Java

Use `./copy-parsetable.sh` in this directory to copy the parse table file from the PGQL Spoofax project. _This requires that you've built the PGQL Spoofax project first!_

Then build with Maven, and run `java -jar target/pgql-lite-0.0.0-SNAPSHOT.jar test.pgql` to see the parser parse and output an abstract syntax tree to STDOUT.
