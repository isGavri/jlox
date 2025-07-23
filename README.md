# Jlox

## Crafting compilers java interpreter by Robert Nymstrong

### Running

Run by typing

`mvn compile`
`mvn -q exec:java`

Run your own file by typing

*You need to modify the 39 line on the pom.xml, deleting the arguments or setting you file name*

#### Command

`mvn -q exec:java -Dexec.args="filename"`

#### pom.xml

```
    <arguments>
        <argument>code.jlox</argument>
    </arguments>
```

### Scanner/Lexer

This is the first phase of our interpreter

We start the interpreter by scanning the source (file or REPL) we implement a regular gramma (type 3) or finite automata to validate the input of the language, this only on a lexical level, this means only validates lexemes ("words"). We also assing the lexemes to tokens (so it has a higher abstraction) and handle errors that could occur on this phase (unterminated strings, unexpected characters), basically when the grammar fails.

Basically the definition of our regular grammar (and thus of our language) turns out like so


```
S → ID | NUM | STR | OP | WS | COMMENT | SYMBOL

ID → L ID'  
ID' → L ID' | D ID' | ε

NUM → D NUM'  
NUM' → D NUM' | DOT D D* | ε  
DOT → .

STR → " CHARS "

CHARS → CHAR CHARS | ε
CHAR → any character except " and \n (handle escaping in code)

OP → '=' '=' | '!' '=' | '<' '=' | '>' '=' | '+' | '-' | '*' | '/' | '=' | '<' | '>'

COMMENT → '/' '/' COMMENT_CHARS  
COMMENT_CHARS → CHAR COMMENT_CHARS | ε

WS → ' ' WS | '\t' WS | '\n' WS | '\r' WS | ε

SYMBOL → '(' | ')' | '{' | '}' | ';' | ',' | '.'

L → a | b | ... | z | A | B | ... | Z | _
D → 0 | 1 | ... | 9
```

### Parser/Syntactic analysis

Second phase of the interpreter.

Now this generally represents a context-free grammar (type 2) and/or a push-down automata. This produces valid sequences of tokens given a set of rules. This sequence is an expression of our language.

*Rules are expressed in a kind of BNF syntax*

Right now this rules only account for literal, unary, binary and grouping expressions. And right now is ambiguous/not deterministic.

```
expression     → literal
                 | unary
                 | binary
                 | grouping ;

literal        → NUMBER | STRING | "true" | "false" | "nil" ;
grouping       → "(" expression ")" ;
unary          → ( "-" | "!" ) expression ;
binary         → expression operator expression ;
operator       → "==" | "!=" | "<" | "<=" | ">" | ">="
               | "+"  | "-"  | "*" | "/" ;
```
For this part we have the class AstPrinter that prints the way our syntax is being validated. You can change the main class on the pom.xml so it runs it once you pass it some code

Finally our grammar ends up like this. All of the binary expression share the same structure.

```

expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
               | "(" expression ")" ;
```
For our parser we wil use the recursive descent technique. What this means is that we will start parsing from the highest-level rule (in this case expression) and works downwards, when we match a non-terminal we call the function associated with that rule recursively and when we encounter a terminal it checks if the TokenType matches the expected terminal.

Now we can parse a single expression

Added support to comma and ternary operators. We added two new non-terminals, comma and ternary
```
comma          → conditional ( "," conditional )* ;
conditional    → equality ( "?" expression ":" conditional )? ;
```
This come right after the expression call as they have the lowest precedence when evaluation, so the highest when parsed

And we also need to add new expressions to handle this operators
