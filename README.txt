=========================================
Project structure
=========================================
Language implementation:
 - Grammar: syntax/*.sdf3
 - AST normalizer: trans/normalize.str
 - Name binding: trans/names.nab
 - Editor services: editor/*.esv
 
Other:
 - Examples: example/*.pgql

=========================================
Notes about the grammar
=========================================
 - It includes layout information such that a pretty-printer and code completion templates can be generated.
 - PGQL keywords are defined as non-terminals, while they should be terminals. Non-terminals are really ugly (they show up in the AST, etc.).
   However, the reason they are non-terminals is because we want allow for case-insensitive keywords, but native support for this has been 
   temporarily removed from Spoofax. Also see http://yellowgrass.org/issue/Spoofax/954

=========================================
Notes about the AST normalizer
=========================================
This is what is does (by example): for the following two 'equivalent' queries, it rewrites their ASTs (which have a completely different structure) into the same "normalized" AST (ASTs not shown here):

SELECT *
WHERE
  x@8374 -[e1 WITH prop1 > 3]-> y
  y -[e2]-> (z:Function)

SELECT *
WHERE
  z <-[e2]- y <-[e1]- x
  e1.prop1 > 3
  z.type = Function
  x.id = 8374

=========================================
Viewing the AST and the normalized AST
=========================================
Open a .pgql file (see /examples/). Then, using the menus in the menu toolbar:
 - Syntax>Show abstract syntax
 - Syntax>Show normalized syntax

=========================================
Setting things up
=========================================

Summary: Install Spoofax, import the project and build it.

Spoofax installation:
 - for installation instructions, see http://metaborg.org/download/
 - use the following update site URL: http://download.spoofax.org/update/stable/
 - don't forget to fix eclipse.ini

Importing the project:
 - File>Import...>General>Existing Projects Into Workspace

Building the project:
 - Project>Build Project

=========================================
Notes about Spoofax version
=========================================
The project is based on Spoofax 1.4.1. A new version of Spoofax with many improvements is in beta stage, but we'll stick with 1.4.1 until the new Spoofax supports SPT testing.
