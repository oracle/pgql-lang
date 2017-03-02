# BNF Grammar for PGQL 1.0

The following is a BNF grammar for PGQL 1.0 that follows syntax and naming conventions used in SQL's ISO/IEC 9075 BNF grammar as closely as possible.

```
<query> ::=
        <path pattern>...
        <select clause>
        <where clause>
        [ <group by clause> ]
        [ <order by clause> ]
        [ <limit offset clauses> ]

<where clause> ::= WHERE <graph pattern expression>

<graph pattern expression> ::= { <topology expression> | <value expression> } [ <comma> <graph pattern expression> ]

<topology expression> ::= <vertex> [ <connection vertex>... ]

<connection vertex> ::= <connection> <vertex>

<vertex> ::= <left paren> [ <vertex edge contents> ] <right paren>

<connection> ::=
        <forward edge simple>
    |   <reverse edge simple>
    |   <forward edge left part> <vertex edge contents> <forward edge right part>
    |   <reverse edge left part> <vertex edge contents> <reverse edge right part>
    |   <forward path left part> <path contents> <forward path right part>
    |   <reverse path left part> <path contents> <reverse path right part>

<vertex edge contents> ::=
        [ <variable name> ]
        [ <colon> <graph element label expression> ]
        [ <at sign> <graph element identifier expression> ]
        [ WITH <value expression> ]

<path contents> ::=
        [ <variable name> ]
        [ <colon> <path expression> ]
        [ <Kleene star> ]
         
<variable name> ::= <language identifier>

<graph element label expression> ::=
        <graph element label>
    |   <graph element label expression> OR <graph element label>

<graph element label> ::= <quoted graph element label> | <unquoted graph element label>

<quoted graph element label> ::= <character string literal>

<unquoted graph element label> ::= <language identifier>

<graph element identifier expression> ::=
        <graph element identifier>
    |   <graph element identifier expression> OR <graph element identifier>

<graph element identifier> ::= <literal>

<path expression> ::=
        <path pattern identifier>
    |   <path expression> OR <path pattern identifier>

<Kleene star> ::= <asterisk>

<path pattern> ::= <path pattern identifier> <assign> <topology expression>

<value expression> ::=
        <numeric value expression>
    |   <boolean value expression>
    |   <string value expression>

<numeric value expression> ::=
        <term>
    |   <numeric value expression> <plus sign> <term>
    |   <numeric value expression> <minus sign> <term>

<term> ::=
        <factor>
    |   <term> <asterisk> <factor>
    |   <term> <solidus> <factor>
    |   <term> <percent> <factor>

<factor> ::= [ <sign> ] <numeric primary>

<numeric primary> ::= <value expression primary>

<boolean value expression> ::=
        <boolean term>
    |   <boolean value expression> OR <boolean term>

<boolean term> ::=
        <boolean factor>
    |   <boolean term> AND <boolean factor>

<boolean factor> ::= [ NOT ] <boolean test>

<boolean test> ::= <boolean primary>

<boolean primary> ::=
        <predicate>
    |   <boolean predicand>

<predicate> ::= <comparison predicate>

<boolean predicand> ::=
        <parenthesized boolean value expression>
    |   <nonparenthesized value expression primary>

<parenthesized boolean value expression> ::= <left paren> <boolean value expression> <right paren>

<comparison predicate> ::= <value predicand> <comp op> <value predicand>

<comp op> ::=
        <equals operator>
    |   <not equals operator>
    |   <less than operator>
    |   <greater than operator>
    |   <less than or equals operator>
    |   <greater than or equals operator>
    |   <regex operator>

<value predicand> ::= <boolean predicand> | <nonparenthesized value expression primary>

<value expression primary> ::=
        <parenthesized value expression>
    |   <nonparenthesized value expression primary>

<parenthesized value expression> ::= <left paren> <value expression> <right paren>

<string value expression> ::= <nonparenthesized value expression primary>

<nonparenthesized value expression primary> ::=
        <literal>
    |   <property access>
    |   <variable name>
    |   <aggregate function>
    |   <built-in graph function>

<property access> ::= <variable name> <dot> { <language identifier> | <character string literal> }

!! above for non-inlined version (WHERE); below for inlined expression (WITH)

<property access> ::=
        [ <dot> ] <language identifier>
    |   <dot> <character string literal>

<aggregate function> ::=
        COUNT <left paren> <asterisk> <right paren>
    |   <general set function>

<general set function> ::= <set function type> <left paren> <value expression> <right paren>

<set function type> ::= <computational operation>

<computational operation> ::= AVG | MAX | MIN | SUM | COUNT

<built-in graph function> ::=
        <value expression> <dot> LABEL <left paren> <right paren>
    |   <value expression> <dot> LABELS <left paren> <right paren>
    |   <value expression> <dot> HASLABEL <left paren> <value expression> <right paren>
    |   <value expression> <dot> ID <left paren> <right paren>
    |   <value expression> <dot> HAS <left paren> <value expression>... <right paren>
    |   <value expression> <dot> INDEGREE <left paren> <right paren>
    |   <value expression> <dot> OUTDEGREE <left paren> <right paren>

!! above for non-inlined version (WHERE); below for inlined expression (WITH)

<built-in graph function> ::=
        [ <dot> ] LABEL <left paren> <right paren>
    |   [ <dot> ] LABELS <left paren> <right paren>
    |   [ <dot> ] HASLABEL <left paren> <value expression> <right paren>
    |   [ <dot> ] ID <left paren> <right paren>
    |   [ <dot> ] HAS <left paren> <value expression>... <right paren>
    |   [ <dot> ] INDEGREE <left paren> <right paren>
    |   [ <dot> ] OUTDEGREE <left paren> <right paren>

<group by clause> ::= GROUP BY <grouping element list>

<grouping element list> ::= <grouping element> [ { <comma> <grouping element> }... ]

<grouping element> ::= <value expression>

<order by clause> ::= ORDER BY <sort specification list>

<sort specification list> ::= <sort specification> [ { <comma> <sort specification> }... ]

<sort specification> ::= <sort key> [ <ordering specification> ]

<sort key> ::= <value expression>

<ordering specification> ::= ASC | DESC

<limit offset clauses> ::=
        <limit clause> [ <offset clause> ]
    |   <offset clause> [ <limit clause> ]

<limit clause> ::= LIMIT <unsigned integer>

<offset clause> ::= OFFSET <unsigned integer>
```

Productions for terminals:

```
<double quote> ::= "

<percent> ::= %

<quote> ::= '

<left paren> ::= (

<right paren> ::= )

<asterisk> ::= *

<plus sign> ::= +

<comma> ::= ,

<minus sign> ::= -

<period> ::= .

<solidus> ::= /

<colon> ::= :

<less than operator> ::= <

<equals operator> ::= =

<greater than operator> ::= >

<not equals operator> ::= !=

<less than or equals operator> ::= <=

<greater than or equals operator> ::= >=

<regex operator> ::= !=

<left bracket> ::= [

<right bracket> ::= ]

<underscore> ::= _

<vertical bar> ::= |

<left brace> ::= {

<right brace> ::= }

<backslash> ::= \

<exclamation mark> ::= !

<at sign> ::= @

<assign> ::= :=

<forward edge simple> ::= ->

<reverse edge simple> ::= <-

<forward edge left part> ::= -[

<forward edge right part> ::= ]->

<reverse edge left part> ::= <-[

<reverse edge right part> ::= ]-

<forward path left part> ::= -/

<forward path right part> ::= /->

<reverse path left part> ::= <-/

<reverse path right part> ::= /-

<simple Latin letter> ::= <simple Latin upper case letter> | <simple Latin lower case letter>

<simple Latin upper case letter> ::=
        A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S | T | U | V | W | X | Y | Z

<simple Latin lower case letter> ::=
        a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z

<digit> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

<alphanumeric character> ::= <simple Latin letter> | <digit>

<language identifier> ::=
        <language identifier start> [ { <underscore> | <language identifier part> }... ]

<language identifier start> ::= <simple Latin letter>

<language identifier part> ::= <simple Latin letter> | <digit>

<literal> ::= <signed numeric literal> | <general literal>

<unsigned literal> ::= <unsigned numeric literal> | <general literal>

<general literal> ::=
        <character string literal>
    |   <boolean literal>

<character string literal> ::= <single-quoted string literal> | <double-quoted string literal>

<single-quoted string literal> ::= <quote> [ { <character no quote> | <escaped character> }... ] <quote>

<double-quoted string literal> ::= <double quote> [ { <character no double quote> | <escaped character> }... ] <double quote>

<character no quote> ::= !! any Unicode character except for quote

<character no double quote> ::= !! any Unicode character except for double quote

<escaped character> ::= <backslash> <escape value>

<escape value> ::= <quote> | t | b | n | r | f

<signed numeric literal> ::= [ <sign> ] <unsigned numeric literal>

<unsigned numeric literal> ::= <exact numeric literal>

<exact numeric literal> ::=
        <unsigned integer> [ <period> [ <unsigned integer> ] ]
    |   <period> <unsigned integer>

<sign> ::= <minus sign>

<signed integer> ::= [ <sign> ] <unsigned integer>

<unsigned integer> ::= <digit> ...

<boolean literal> ::= TRUE | FALSE
```

