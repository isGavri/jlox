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

