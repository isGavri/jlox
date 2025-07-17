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

