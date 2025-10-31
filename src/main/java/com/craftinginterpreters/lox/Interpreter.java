
package com.craftinginterpreters.lox;

import java.util.List;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Comma;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Ternary;
import com.craftinginterpreters.lox.Expr.Unary;

/**
 * Interpreter
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError e) {
      Lox.runtimeError(e);
    }
  }

  private String stringify(Object object) {
    if (object == null)
      return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  @Override
  public Object visitBinaryExpr(Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      // Arithmetic
      case MINUS:
        checkNumberOperands(expr.operator, left, right);
        return (double) left - (double) right;
      case SLASH:
        checkNumberOperands(expr.operator, left, right);
        if ((Double) right == 0) {
          throw new RuntimeError(expr.operator, "You are trying to divide by zero.");
        }
        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(expr.operator, left, right);
        return (double) left * (double) right;
      case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }

        if (left instanceof String && right instanceof String) {
          return (String) left + (String) right;
        }

        if ((left instanceof String && right instanceof Double)
            || (left instanceof Double && right instanceof String)) {
          return stringify(left) + stringify(right);
        }
        throw new RuntimeError(expr.operator, "The operands must be numbers or strings");
      // ) Comparison
      case GREATER:
        if (left instanceof String && right instanceof Double) {
          return left.toString().length() > (Double) right;
        }
        if (left instanceof Double && right instanceof String) {
          return (Double) left > right.toString().length();
        }
        if (left instanceof Double && right instanceof Double) {
          return (Double) left > (Double) right;
        }
        if (left instanceof String && right instanceof String) {
          return left.toString().length() > right.toString().length();
        }
        throw new RuntimeError(expr.operator, "Unsuported comparison.");
      case GREATER_EQUAL:

        if (left instanceof String && right instanceof Double) {
          return left.toString().length() >= (Double) right;
        }
        if (left instanceof Double && right instanceof String) {
          return (Double) left >= right.toString().length();
        }
        if (left instanceof Double && right instanceof Double) {
          return (Double) left >= (Double) right;
        }
        if (left instanceof String && right instanceof String) {
          return left.toString().length() >= right.toString().length();
        }
        throw new RuntimeError(expr.operator, "Unsuported comparison.");
      case LESS:

        if (left instanceof String && right instanceof Double) {
          return left.toString().length() < (Double) right;
        }
        if (left instanceof Double && right instanceof String) {
          return (Double) left < right.toString().length();
        }
        if (left instanceof Double && right instanceof Double) {
          return (Double) left < (Double) right;
        }
        if (left instanceof String && right instanceof String) {
          return left.toString().length() < right.toString().length();
        }
        throw new RuntimeError(expr.operator, "Unsuported comparison.");
      case LESS_EQUAL:
        if (left instanceof String && right instanceof Double) {
          return left.toString().length() <= (Double) right;
        }
        if (left instanceof Double && right instanceof String) {
          return (Double) left <= right.toString().length();
        }
        if (left instanceof Double && right instanceof Double) {
          return (Double) left <= (Double) right;
        }
        if (left instanceof String && right instanceof String) {
          return left.toString().length() <= right.toString().length();
        }
        throw new RuntimeError(expr.operator, "Unsuported comparison.");
      // Equality
      case EQUAL:
        return isEqual(left, right);
      case BANG_EQUAL:
        return !isEqual(left, right);

    }

    return null;
  }

  @Override
  public Object visitGroupingExpr(Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitUnaryExpr(Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double) right;
      case BANG:
        return !isTruthy(right);
    }
    return null;
  }

  @Override
  public Object visitCommaExpr(Comma expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitCommaExpr'");
  }

  @Override
  public Object visitTernaryExpr(Ternary expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitTernaryExpr'");
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expr);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expr);
    System.out.println(stringify(value));
    return null;
  }

  private boolean isTruthy(Object object) {
    if (object == null)
      return false;
    if (object instanceof Boolean)
      return (boolean) object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null)
      return true;
    if (a == null)
      return false;

    return a.equals(b);
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double)
      return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && left instanceof Double)
      return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

}
