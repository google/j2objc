/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * A collection of factory methods for AST nodes. All nodes constructed have
 * bindings added as appropriate.
 *
 * @author Keith Stanger
 */
public final class ASTFactory {

  public static SimpleName newSimpleName(AST ast, IVariableBinding binding) {
    SimpleName name = ast.newSimpleName(binding.getName());
    Types.addBinding(name, binding);
    return name;
  }

  public static SimpleName newSimpleName(AST ast, IMethodBinding binding) {
    SimpleName name = ast.newSimpleName(binding.getName());
    Types.addBinding(name, binding);
    return name;
  }

  public static QualifiedName newQualifiedName(AST ast, Name qualifier, SimpleName name) {
    QualifiedName qName = ast.newQualifiedName(qualifier, name);
    Types.addBinding(qName, Types.getBinding(name));
    return qName;
  }

  public static Name newName(AST ast, Name qualifier, IVariableBinding var) {
    SimpleName name = newSimpleName(ast, var);
    return qualifier == null ? name : newQualifiedName(ast, qualifier, name);
  }

  public static Name newName(AST ast, List<IVariableBinding> path) {
    Name name = null;
    for (IVariableBinding var : path) {
      name = newName(ast, name, var);
    }
    return name;
  }

  public static Assignment newAssignment(AST ast, Expression lhs, Expression rhs) {
    Assignment assignment = ast.newAssignment();
    assignment.setOperator(Assignment.Operator.ASSIGN);
    assignment.setLeftHandSide(lhs);
    assignment.setRightHandSide(rhs);
    Types.addBinding(assignment, Types.getTypeBinding(lhs));
    return assignment;
  }

  public static VariableDeclarationFragment newVariableDeclarationFragment(
      AST ast, IVariableBinding binding, Expression initializer) {
    VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
    frag.setName(newSimpleName(ast, binding));
    frag.setInitializer(initializer);
    Types.addBinding(frag, binding);
    return frag;
  }

  @SuppressWarnings("unchecked")
  public static VariableDeclarationStatement newVariableDeclarationStatement(
      AST ast, IVariableBinding binding, Expression initializer) {
    VariableDeclarationStatement decl = ast.newVariableDeclarationStatement(
        newVariableDeclarationFragment(ast, binding, initializer));
    decl.setType(Types.makeType(binding.getType()));
    decl.modifiers().addAll(ast.newModifiers(binding.getModifiers()));
    return decl;
  }

  @SuppressWarnings("unchecked")
  public static VariableDeclarationExpression newVariableDeclarationExpression(
      AST ast, IVariableBinding binding, Expression initializer) {
    VariableDeclarationExpression decl = ast.newVariableDeclarationExpression(
        newVariableDeclarationFragment(ast, binding, initializer));
    decl.setType(Types.makeType(binding.getType()));
    decl.modifiers().addAll(ast.newModifiers(binding.getModifiers()));
    Types.addBinding(decl, binding.getType());
    return decl;
  }

  @SuppressWarnings("unchecked")
  public static SingleVariableDeclaration newSingleVariableDeclaration(
      AST ast, IVariableBinding binding) {
    SingleVariableDeclaration decl = ast.newSingleVariableDeclaration();
    decl.setName(newSimpleName(ast, binding));
    decl.setType(Types.makeType(binding.getType()));
    decl.modifiers().addAll(ast.newModifiers(binding.getModifiers()));
    Types.addBinding(decl, binding);
    return decl;
  }

  @SuppressWarnings("unchecked")
  public static FieldDeclaration newFieldDeclaration(
      AST ast, IVariableBinding binding, Expression initializer) {
    FieldDeclaration decl = ast.newFieldDeclaration(
        newVariableDeclarationFragment(ast, binding, initializer));
    decl.setType(Types.makeType(binding.getType()));
    decl.modifiers().addAll(ast.newModifiers(binding.getModifiers()));
    Types.addBinding(decl, binding);
    return decl;
  }

  public static PrefixExpression newPrefixExpression(
      AST ast, PrefixExpression.Operator op, Expression operand, String type) {
    PrefixExpression expr = ast.newPrefixExpression();
    expr.setOperator(op);
    expr.setOperand(operand);
    Types.addBinding(expr, ast.resolveWellKnownType(type));
    return expr;
  }

  public static InfixExpression newInfixExpression(
      AST ast, Expression lhs, InfixExpression.Operator op, Expression rhs, String type) {
    InfixExpression expr = ast.newInfixExpression();
    expr.setOperator(op);
    expr.setLeftOperand(lhs);
    expr.setRightOperand(rhs);
    Types.addBinding(expr, ast.resolveWellKnownType(type));
    return expr;
  }

  public static InfixExpression newInfixExpression(
      AST ast, IVariableBinding lhs, InfixExpression.Operator op, IVariableBinding rhs,
      String type) {
    return newInfixExpression(ast, newSimpleName(ast, lhs), op, newSimpleName(ast, rhs), type);
  }

  public static PostfixExpression newPostfixExpression(
      AST ast, IVariableBinding var, PostfixExpression.Operator op) {
    PostfixExpression expr = ast.newPostfixExpression();
    expr.setOperator(op);
    expr.setOperand(newSimpleName(ast, var));
    Types.addBinding(expr, var.getType());
    return expr;
  }

  public static ArrayAccess newArrayAccess(
      AST ast, IVariableBinding array, IVariableBinding index) {
    ITypeBinding arrayType = array.getType();
    assert arrayType.isArray();
    ArrayAccess access = ast.newArrayAccess();
    access.setArray(newSimpleName(ast, array));
    access.setIndex(newSimpleName(ast, index));
    Types.addBinding(access, arrayType.getComponentType());
    return access;
  }

  public static ForStatement newForStatement(
      AST ast, VariableDeclarationExpression decl, Expression cond, Expression updater,
      Statement body) {
    ForStatement forLoop = ast.newForStatement();
    @SuppressWarnings("unchecked")
    List<Expression> initializers = forLoop.initializers(); // safe by definition
    initializers.add(decl);
    forLoop.setExpression(cond);
    @SuppressWarnings("unchecked")
    List<Expression> updaters = forLoop.updaters(); // safe by definition
    updaters.add(updater);
    forLoop.setBody(body);
    return forLoop;
  }

  public static MethodInvocation newMethodInvocation(
      AST ast, IMethodBinding binding, Expression expr) {
    MethodInvocation invocation = ast.newMethodInvocation();
    invocation.setExpression(expr);
    invocation.setName(newSimpleName(ast, binding));
    Types.addBinding(invocation, binding);
    return invocation;
  }

  public static NumberLiteral newNumberLiteral(AST ast, String token, String type) {
    NumberLiteral literal = ast.newNumberLiteral(token);
    Types.addBinding(literal, ast.resolveWellKnownType(type));
    return literal;
  }

  public static InfixExpression createNullCheck(AST ast, IVariableBinding var, boolean equal) {
    NullLiteral nullNode = ast.newNullLiteral();
    Types.addBinding(nullNode, ast.resolveWellKnownType("java.lang.Object"));
    return newInfixExpression(
        ast, newSimpleName(ast, var),
        equal ? InfixExpression.Operator.EQUALS : InfixExpression.Operator.NOT_EQUALS, nullNode,
        "boolean");
  }

  public static InstanceofExpression newInstanceofExpression(AST ast, Expression lhs, Type rhs) {
    InstanceofExpression expr = ast.newInstanceofExpression();
    expr.setLeftOperand(lhs);
    expr.setRightOperand(rhs);
    Types.addBinding(expr, ast.resolveWellKnownType("boolean"));
    return expr;
  }

  private static Expression makeLiteralInternal(AST ast, Object value) {
    if (value instanceof Boolean) {
      return ast.newBooleanLiteral((Boolean) value);
    } else if (value instanceof Character) {
      CharacterLiteral c = ast.newCharacterLiteral();
      c.setCharValue((Character) value);
      return c;
    } else if (value instanceof Number) {
      return ast.newNumberLiteral(value.toString());
    } else if (value instanceof String) {
      StringLiteral s = ast.newStringLiteral();
      s.setLiteralValue((String) value);
      return s;
    }
    throw new AssertionError("unknown constant type");
  }

  /**
   * Returns a literal node for a specified constant value.
   */
  public static Expression makeLiteral(AST ast, Object value, ITypeBinding type) {
    Expression literal = makeLiteralInternal(ast, value);
    Types.addBinding(literal, type);
    return literal;
  }
}
