package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner
 * Manages the lexical analysis
 * TODO: add support for comma separated expressions, (aside function argument list)
 * TODO: add support for ternary operator
 *
 */
import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {

  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  /**
   * Hashmap for the reserved words
   */
  static {
    keywords = new HashMap<>();
    keywords.put("and", AND);
    keywords.put("class", CLASS);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("for", FOR);
    keywords.put("fun", FUN);
    keywords.put("if", IF);
    keywords.put("nil", NIL);
    keywords.put("or", OR);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("this", THIS);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  /**
   * Starts the scanning loop
   * 
   * @return
   */
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  /**
   * Scans tokens based of a characters from the source
   */
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd())
            advance();
        } else if (match('*')) {
          scanBlockComment();
        } else {
          addToken(SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }

  }

  /**
   * Helper to know if a character is part of the alphabeth or _
   * 
   * @param c Character
   * @return
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  /**
   * Helper to know if a characterr is alphanumeric
   * 
   * @param c Character
   * @return
   */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  /**
   * Cosumes, scans and ads token for identifiers and reserved keywords
   */
  private void identifier() {
    while (isAlphaNumeric(peek()))
      advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = IDENTIFIER;
    addToken(type);

  }

  /**
   * Consumes, scans and ads token for numeric literals
   */
  private void number() {
    while (isDigit(peek()))
      advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance();

      while (isDigit(peek()))
        advance();
    }
    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  /**
   * Peeks into the next character
   * 
   * @return
   */
  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Helper to scan, consume and add token of string literals
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  /**
   * Helper to scan a block comment, taking into account new lines and nested
   * block comments
   */
  private void scanBlockComment() {
    int depth = 1;

    while (!isAtEnd()) {
      char c = advance();

      if (c == '\n') {
        line++;
      } else if (c == '/' && match('*')) {
        depth++;
      } else if (c == '*' && match('/')) {
        depth--;
        if (depth == 0)
          return;
      }
    }

    Lox.error(line, "Unterminated block comment.");
  }

  /**
   * Peeks into the current character without consuming it
   * 
   * @return next character
   */
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  /**
   * Helper to consume next character if it matches an expected one
   * 
   * @param expected
   * @return Whether it matched or not
   */
  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }
    if (source.charAt(current) != expected) {
      return false;
    }
    current++;
    return true;
  }

  /**
   * Consumes characters from the source
   * 
   * @return character at next position
   */
  private char advance() {
    return source.charAt(current++);
  }

  /**
   * Add token to the list of tokens of the current scanned source without a
   * literal value
   * 
   * @param type Token type
   */
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  /**
   * Add token to the list of tokens of the current scanned source
   * 
   * @param type    Token type
   * @param literal Explicit value
   */
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  /**
   * Helper to check if the scanner is at the end of the source
   * 
   * @return true/false if the current scanned character is greater or equal than
   *         the length of the source
   */
  private boolean isAtEnd() {
    return current >= source.length();
  }

}
