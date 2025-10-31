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

```xml
    <arguments>
        <argument>code.jlox</argument>
    </arguments>
```

### Scanner/Lexer

This is the first phase of our interpreter

We start the interpreter by scanning the source (file or REPL) we implement a regular gramma (type 3) or finite automata to validate the input of the language, this only on a lexical level, this means only validates lexemes ("words"). We also assing the lexemes to tokens (so it has a higher abstraction) and handle errors that could occur on this phase (unterminated strings, unexpected characters), basically when the grammar fails.

Basically the definition of our regular grammar (and thus of our language) turns out like so

*Needs fix to include new tokens added in the challenges and own features ternary operator and i dont remeber what else*


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

For our parser we will use the recursive descent technique. What this means is that we will start parsing from the highest-level rule (in this case expression) and works downwards, when we match a non-terminal we call the function associated with that rule recursively and when we encounter a terminal it checks if the TokenType matches the expected terminal.

Added support to comma and ternary operators. We added two new non-terminals, comma and ternary
```
comma          → conditional ( "," conditional )* ;
conditional    → equality ( "?" expression ":" conditional )? ;
```
This come right after the expression call as they have the lowest precedence when evaluation, so the highest when parsed

*TODO: needs to be included in the full grammar definition*
Added grammar for statements
```
program        → statement* EOF ;

statement      → exprStmt
               | printStmt ;

exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;
```

Then we added this for variable declarations (and also for functions later on i believe)

```bnf
program        → declaration* EOF ;

declaration    → varDecl
               | statement ;

statement      → exprStmt
               | printStmt ;
```

New rule for declaring a variable
```bnf
varDecl         → "var" IDENTIFIER ( "=" expression )? ";";
```
And we also define a new primary expression which just generates an identifier
```
primary         → "true" | "false" | "nil"
                | NUMBER | STRING
                | "(" expression ")"
                | IDENTIFIER ;
```
### Interpreter

We just return the value of our leaf nodes that are literals, and run each type of expression
