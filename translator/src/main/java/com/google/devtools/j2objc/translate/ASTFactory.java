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

import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
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

  @SuppressWarnings("unchecked")
  public static List<Modifier> newModifiers(AST ast, int flags) {
    return ast.newModifiers(flags);
  }

  public static SimpleName newSimpleName(AST ast, IBinding binding) {
    String name = binding.getName();
    if (name.isEmpty()) {
      name = "$Unnamed$";
    }
    SimpleName nameNode = ast.newSimpleName(name);
    Types.addBinding(nameNode, binding);
    return nameNode;
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

  public static FieldAccess newFieldAccess(AST ast, IVariableBinding var, Expression expr) {
    FieldAccess node = ast.newFieldAccess();
    node.setExpression(expr);
    node.setName(newSimpleName(ast, var));
    Types.addBinding(node, var);
    return node;
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

  public static VariableDeclarationStatement newVariableDeclarationStatement(
      AST ast, VariableDeclarationFragment fragment) {
    IVariableBinding varBinding = Types.getVariableBinding(fragment);
    VariableDeclarationStatement decl = ast.newVariableDeclarationStatement(fragment);
    decl.setType(newType(ast, varBinding.getType()));
    ASTUtil.getModifiers(decl).addAll(newModifiers(ast, varBinding.getModifiers()));
    return decl;
  }

  public static VariableDeclarationStatement newVariableDeclarationStatement(
      AST ast, IVariableBinding binding, Expression initializer) {
    return newVariableDeclarationStatement(ast, newVariableDeclarationFragment(
        ast, binding, initializer));
  }

  public static VariableDeclarationExpression newVariableDeclarationExpression(
      AST ast, IVariableBinding binding, Expression initializer) {
    VariableDeclarationExpression decl = ast.newVariableDeclarationExpression(
        newVariableDeclarationFragment(ast, binding, initializer));
    decl.setType(newType(ast, binding.getType()));
    ASTUtil.getModifiers(decl).addAll(newModifiers(ast, binding.getModifiers()));
    Types.addBinding(decl, binding.getType());
    return decl;
  }

  public static SingleVariableDeclaration newSingleVariableDeclaration(
      AST ast, IVariableBinding binding) {
    SingleVariableDeclaration decl = ast.newSingleVariableDeclaration();
    decl.setName(newSimpleName(ast, binding));
    decl.setType(newType(ast, binding.getType()));
    ASTUtil.getModifiers(decl).addAll(newModifiers(ast, binding.getModifiers()));
    Types.addBinding(decl, binding);
    return decl;
  }

  public static FieldDeclaration newFieldDeclaration(
      AST ast, VariableDeclarationFragment fragment) {
    IVariableBinding varBinding = Types.getVariableBinding(fragment);
    FieldDeclaration decl = ast.newFieldDeclaration(fragment);
    decl.setType(newType(ast, varBinding.getType()));
    ASTUtil.getModifiers(decl).addAll(newModifiers(ast, varBinding.getModifiers()));
    return decl;
  }

  public static FieldDeclaration newFieldDeclaration(
      AST ast, IVariableBinding binding, Expression initializer) {
    return newFieldDeclaration(ast, newVariableDeclarationFragment(ast, binding, initializer));
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
      AST ast, Expression lhs, InfixExpression.Operator op, Expression rhs, ITypeBinding type) {
    InfixExpression expr = ast.newInfixExpression();
    expr.setOperator(op);
    expr.setLeftOperand(lhs);
    expr.setRightOperand(rhs);
    Types.addBinding(expr, type);
    return expr;
  }

  public static InfixExpression newInfixExpression(
      AST ast, IVariableBinding lhs, InfixExpression.Operator op, IVariableBinding rhs,
      ITypeBinding type) {
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

  public static ArrayCreation newArrayCreation(AST ast, ArrayInitializer initializer) {
    ITypeBinding type = Types.getTypeBinding(initializer);
    ArrayCreation arrayCreation = ast.newArrayCreation();
    arrayCreation.setType((ArrayType) newType(ast, type));
    arrayCreation.setInitializer(initializer);
    Types.addBinding(arrayCreation, type);
    return arrayCreation;
  }

  public static ForStatement newForStatement(
      AST ast, VariableDeclarationExpression decl, Expression cond, Expression updater,
      Statement body) {
    ForStatement forLoop = ast.newForStatement();
    ASTUtil.getInitializers(forLoop).add(decl);
    forLoop.setExpression(cond);
    ASTUtil.getUpdaters(forLoop).add(updater);
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

  public static SuperMethodInvocation newSuperMethodInvocation(AST ast, IMethodBinding binding) {
    SuperMethodInvocation invocation = ast.newSuperMethodInvocation();
    invocation.setName(newSimpleName(ast, binding));
    Types.addBinding(invocation, binding);
    return invocation;
  }

  public static SuperConstructorInvocation newSuperConstructorInvocation(
      AST ast, IMethodBinding binding) {
    SuperConstructorInvocation invocation = ast.newSuperConstructorInvocation();
    Types.addBinding(invocation, binding);
    return invocation;
  }

  public static MethodInvocation newDereference(AST ast, Expression node) {
    IOSMethodBinding binding = IOSMethodBinding.newDereference(Types.getTypeBinding(node));
    MethodInvocation invocation = newMethodInvocation(ast, binding, null);
    ASTUtil.getArguments(invocation).add(node);
    return invocation;
  }

  public static MethodInvocation newAddressOf(AST ast, Expression node) {
    IOSMethodBinding binding = IOSMethodBinding.newAddressOf(Types.getTypeBinding(node));
    MethodInvocation invocation = newMethodInvocation(ast, binding, null);
    ASTUtil.getArguments(invocation).add(node);
    return invocation;
  }

  public static MethodDeclaration newMethodDeclaration(AST ast, IMethodBinding binding) {
    MethodDeclaration declaration = ast.newMethodDeclaration();
    declaration.setConstructor(binding.isConstructor());
    declaration.setName(newSimpleName(ast, binding));
    declaration.setReturnType2(newType(ast, binding.getReturnType()));
    ASTUtil.getModifiers(declaration).addAll(newModifiers(ast, binding.getModifiers()));
    Types.addBinding(declaration, binding);
    return declaration;
  }

  public static SimpleName newLabel(AST ast, String identifier) {
    SimpleName node = ast.newSimpleName(identifier);
    Types.addBinding(node, IOSTypeBinding.newUnmappedClass(identifier));
    return node;
  }

  public static NumberLiteral newNumberLiteral(AST ast, String token, String type) {
    NumberLiteral literal = ast.newNumberLiteral(token);
    Types.addBinding(literal, ast.resolveWellKnownType(type));
    return literal;
  }

  public static InstanceofExpression newInstanceofExpression(
      AST ast, Expression lhs, ITypeBinding type) {
    InstanceofExpression expr = ast.newInstanceofExpression();
    expr.setLeftOperand(lhs);
    expr.setRightOperand(newType(ast, type));
    Types.addBinding(expr, ast.resolveWellKnownType("boolean"));
    return expr;
  }

  public static CastExpression newCastExpression(AST ast, Expression expr, ITypeBinding type) {
    CastExpression cast = ast.newCastExpression();
    cast.setExpression(expr);
    cast.setType(newType(ast, type));
    Types.addBinding(cast, type);
    return cast;
  }

  public static ClassInstanceCreation newClassInstanceCreation(
      AST ast, IMethodBinding constructor) {
    ClassInstanceCreation node = ast.newClassInstanceCreation();
    node.setType(newType(ast, constructor.getDeclaringClass()));
    Types.addBinding(node, constructor);
    return node;
  }

  public static ThisExpression newThisExpression(AST ast, ITypeBinding type) {
    ThisExpression node = ast.newThisExpression();
    Types.addBinding(node, type);
    return node;
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

  public static Expression makeIntLiteral(AST ast, int i) {
    return makeLiteral(ast, Integer.valueOf(i), ast.resolveWellKnownType("int"));
  }

  public static BooleanLiteral newBooleanLiteral(AST ast, boolean value) {
    BooleanLiteral node = ast.newBooleanLiteral(value);
    Types.addBinding(node, ast.resolveWellKnownType("boolean"));
    return node;
  }

  public static NullLiteral newNullLiteral(AST ast) {
    NullLiteral node = ast.newNullLiteral();
    Types.addBinding(node, ast.resolveWellKnownType("java.lang.Object"));
    return node;
  }

  public static TypeLiteral newTypeLiteral(AST ast, ITypeBinding type) {
    TypeLiteral literal = ast.newTypeLiteral();
    literal.setType(newType(ast, type));
    Types.addBinding(literal, type);
    return literal;
  }

  public static Type newType(AST ast, ITypeBinding binding) {
    Type type;
    if (binding.isPrimitive()) {
      type = ast.newPrimitiveType(PrimitiveType.toCode(binding.getName()));
    } else if (binding.isArray()) {
      type = ast.newArrayType(newType(ast, binding.getComponentType()));
    } else if (binding instanceof PointerTypeBinding) {
      type = newType(ast, ((PointerTypeBinding) binding).getPointeeType());
    } else {
      type = ast.newSimpleType(newSimpleName(ast, binding.getErasure()));
    }
    Types.addBinding(type, binding);
    return type;
  }

  public static ParenthesizedExpression newParenthesizedExpression(AST ast, Expression expr) {
    ParenthesizedExpression result = ast.newParenthesizedExpression();
    result.setExpression(expr);
    Types.addBinding(result, Types.getTypeBinding(expr));
    return result;
  }

  /**
   * Replaces (in place) a QualifiedName node with an equivalent FieldAccess
   * node. This is helpful when a mutation needs to replace the qualifier with
   * a node that has Expression type but not Name type.
   */
  public static FieldAccess convertToFieldAccess(QualifiedName node) {
    AST ast = node.getAST();
    ASTNode parent = node.getParent();
    if (parent instanceof QualifiedName) {
      FieldAccess newParent = convertToFieldAccess((QualifiedName) parent);
      Expression expr = newParent.getExpression();
      assert expr instanceof QualifiedName;
      node = (QualifiedName) expr;
    }
    FieldAccess newNode = newFieldAccess(
        ast, Types.getVariableBinding(node), NodeCopier.copySubtree(ast, node.getQualifier()));
    ASTUtil.setProperty(node, newNode);
    return newNode;
  }
}
