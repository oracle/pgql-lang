=========================================
Project structure
=========================================
Language implementation:
 - Grammar: syntax/*.sdf3
 - AST normalizer: trans/normalize.str
 - Name binding: trans/names.nab
 - Editor services: editor/*.esv
 
Other:
 - Examples: example/**/.pgql
 - Tests: test/*.spt
 - Schema used for examples and tests: trans/example-schema.str

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

MATCH
  x@8374 -[e1 WITH prop1 > 3]-> y
  y -[e2]-> (z:Function)
SELECT *

MATCH
  z <-[e2]- y <-[e1]- x
  e1.prop1 > 3
  z.type = Function
  x.id = 8374
SELECT *

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
 - for installation instructions, see http://metaborg.org/download/.
 - use the following update site URL: http://metaborg.org/spoofax/spoofax-1-4-0/.
   currently this points to the same download as http://download.spoofax.org/update/stable/, but this may change in the future. 
 - don't forget to fix eclipse.ini.

Importing the project:
 - File>Import...>General>Existing Projects Into Workspace

Building the project:
 - Project>Build Project

=========================================
Notes about Spoofax version
=========================================
The project is based on Spoofax 1.4, which is much less stable and less fast than the new alpha1 release that has been built from the ground up: http://metaborg.org/spoofax/new-spoofax-plugin-alpha1-release/
However, we will stick with this version until the new version supports:
 - SPT testing
 - editor service: reference resolution
 - editor service: content completion
