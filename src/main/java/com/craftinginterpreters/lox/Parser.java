package com.craftinginterpreters.lox;
// "Variable : Token name"

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * Parser
 */
public class Parser {

  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  // Points to the next token to be parsed
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // Parses a singular expression
  // Expr parse() {
  // try {
  // return expression();
  // } catch (ParseError e) {
  // return null;
  // }
  // }
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  // This functions represents this rule:
  // expression → equality
  private Expr expression() {
    return comma();
  }

  private Stmt declaration() {
    try {
      if (match(VAR))
        varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt statement() {
    if (match(PRINT))
      return printStatement();

    return expressionStatement();
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  // comma → conditional ( "," conditional )* ;
  private Expr comma() {

    Expr expr = ternary();
    if (!check(COMMA)) {
      return expr;
    }
    List<Expr> exprs = new ArrayList<>();
    exprs.add(expr);
    while (match(COMMA)) {
      exprs.add(ternary());
    }

    return new Expr.Comma(exprs);
  }

  // ternary → equality ( "?" expression ":" ternary )? ;
  private Expr ternary() {
    Expr condition = equality();
    if (match(Q_MARK)) {
      Token operator = previous();
      Expr then = expression();
      if (!match(COLON)) {
        throw error(peek(), "Missing ':' for ternary operation");
      }
      Expr elseThen = expression();
      return new Expr.Ternary(condition, then, elseThen, operator);

    }
    return condition;
  }

  // This function represents this rule:
  // equality → comparison ( ( "!=" | "==" ) comparison )*
  private Expr equality() {

    // Left recursive call to comparison expression
    // equality → comparison ...
    Expr expr = comparison();

    // If we get to match either a bang_equal or a equal_equal then we get into the
    // recursive call for this expression
    // ... ( ( "!=" | "==" ) comparison )*
    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous(); // We collect the previous token (the one we matched)
      Expr right = comparison(); // recursive call to the right comparison expression
      expr = new Expr.Binary(expr, operator, right);
      // If we get to encounter another BANG_EQUAL, EQUAL_EQUAL this
      // converts into a bigger expr and adds leaves to the AST, but
      // still the same way of handling it. How this works? the left part, orignal
      // expr call will have the new Binary expr, so in the next assign that
      // old expr becomes the new lext expression and so on

    }
    return expr;
  }

  // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // term → factor ( ( "-" | "+" ) factor )* ;
  private Expr term() {
    Expr expr = factor();
    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // factor → unary ( ( "/" | "*" ) unary )* ;
  private Expr factor() {
    Expr expr = unary();
    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // unary → ( "!" | "-" ) unary | primary ;
  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return primary();
  }

  // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;

  private Expr primary() {
    if (match(TRUE)) {
      return new Expr.Literal(true);
    }
    if (match(FALSE)) {
      return new Expr.Literal(false);
    }
    if (match(NIL)) {
      return new Expr.Literal(null);
    }

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    // Binary operator appearing at the start of a expression

    // Equality
    if (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Expr discarded = comparison();
      throw error(isAtEnd() ? previous() : advance(), "Missing operand for equality '!='/'==' expression");
    }
    // Comparison
    if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Expr discarded = term();
      throw error(isAtEnd() ? previous() : advance(), "Missing operand for comparison '>'/'>='/'<'/'<=' expression");
    }
    // Term
    if (match(PLUS, MINUS)) {
      Expr discarded = factor();
      throw error(isAtEnd() ? previous() : advance(), "Missing operand for term '+'/'-' expression");
    }
    // Factor
    if (match(STAR, SLASH)) {
      Expr discarded = unary();
      throw error(isAtEnd() ? previous() : advance(), "Missing operand for factor '*'/'/' expression");
    }

    throw error(peek(), "Expected expression");

  }

  // Takes a look into next token to be consumed and consumes it
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();
    throw error(peek(), message);
  }

  // Takes a look into next token to be consumed without consuming it
  private boolean check(TokenType type) {
    if (isAtEnd())
      return false;
    return peek().type == type;
  }

  // Consumes next token and returns it
  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  // Helpers
  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON)
        return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }

}
