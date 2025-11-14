package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);

    R visitPrintStmt(Print stmt);
  }

  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expr = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expr;
  }

  static class Print extends Stmt {
    Print(Expr expresssion) {
      this.expr = expresssion;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expr;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
