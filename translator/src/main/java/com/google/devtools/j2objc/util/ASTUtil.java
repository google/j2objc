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

package com.google.devtools.j2objc.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * Utility methods for manipulating AST nodes.
 *
 * @author Keith Stanger
 */
public final class ASTUtil {

  /**
   * Helper method to isolate the unchecked warning.
   */
  @SuppressWarnings("unchecked")
  public static List<Statement> getStatements(Block block) {
    return block.statements();
  }

  @SuppressWarnings("unchecked")
  public static List<Statement> getStatements(SwitchStatement node) {
    return node.statements();
  }

  @SuppressWarnings("unchecked")
  public static List<SingleVariableDeclaration> getParameters(MethodDeclaration method) {
    return method.parameters();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(MethodInvocation node) {
    return node.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(SuperMethodInvocation node) {
    return node.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(ConstructorInvocation node) {
    return node.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(SuperConstructorInvocation node) {
    return node.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getArguments(ClassInstanceCreation node) {
    return node.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getExtendedOperands(InfixExpression expr) {
    return expr.extendedOperands();
  }

  @SuppressWarnings("unchecked")
  public static List<VariableDeclarationFragment> getFragments(VariableDeclarationStatement node) {
    return node.fragments();
  }

  @SuppressWarnings("unchecked")
  public static List<VariableDeclarationFragment> getFragments(
      VariableDeclarationExpression node) {
    return node.fragments();
  }

  @SuppressWarnings("unchecked")
  public static List<BodyDeclaration> getBodyDeclarations(AbstractTypeDeclaration node) {
    return node.bodyDeclarations();
  }

  @SuppressWarnings("unchecked")
  public static List<BodyDeclaration> getBodyDeclarations(AnonymousClassDeclaration node) {
    return node.bodyDeclarations();
  }

  @SuppressWarnings("unchecked")
  public static List<IExtendedModifier> getModifiers(BodyDeclaration node) {
    return node.modifiers();
  }

  @SuppressWarnings("unchecked")
  public static List<AbstractTypeDeclaration> getTypes(CompilationUnit unit) {
    return unit.types();
  }

  @SuppressWarnings("unchecked")
  public static void setProperty(ASTNode node, Expression expr) {
    ASTNode parent = node.getParent();
    StructuralPropertyDescriptor locator = node.getLocationInParent();
    if (locator instanceof ChildPropertyDescriptor) {
      parent.setStructuralProperty(locator, expr);
    } else {
      // JDT doesn't directly support ChildListProperty replacement.
      List<Expression> args;
      if (parent instanceof MethodInvocation) {
        args = ((MethodInvocation) parent).arguments();
      } else if (parent instanceof ConstructorInvocation) {
        args = ((ConstructorInvocation) parent).arguments();
      } else if (parent instanceof ClassInstanceCreation) {
        args = ((ClassInstanceCreation) parent).arguments();
      } else if (parent instanceof InfixExpression) {
        args = ((InfixExpression) parent).extendedOperands();
      } else if (parent instanceof SynchronizedStatement) {
        SynchronizedStatement stmt = (SynchronizedStatement) parent;
        if (node.equals(stmt.getExpression())) {
          stmt.setExpression((Expression) node);
        }
        return;
      } else if (parent instanceof SuperConstructorInvocation) {
        args = ((SuperConstructorInvocation) parent).arguments();
      } else if (parent instanceof ArrayCreation) {
        args = ((ArrayCreation) parent).dimensions();
      } else if (parent instanceof ArrayInitializer) {
        args = ((ArrayInitializer) parent).expressions();
      } else if (parent instanceof EnumConstantDeclaration) {
        args = ((EnumConstantDeclaration) parent).arguments();
      } else {
        throw new AssertionError("unknown parent node type: " + parent.getClass().getSimpleName());
      }
      for (int i = 0; i < args.size(); i++) {
        if (node.equals(args.get(i))) {
          args.set(i, expr);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void setProperty(ASTNode node, Statement stmt) {
    ASTNode parent = node.getParent();
    StructuralPropertyDescriptor locator = node.getLocationInParent();
    if (locator instanceof ChildPropertyDescriptor) {
      parent.setStructuralProperty(locator, stmt);
    } else {
      // JDT doesn't directly support ChildListProperty replacement.
      List<Statement> args;
      if (parent instanceof Block) {
        args = ((Block) parent).statements();
      } else if (parent instanceof SwitchStatement) {
        args = ((SwitchStatement) parent).statements();
      } else {
        throw new AssertionError("unknown parent node type: " + parent.getClass().getSimpleName());
      }
      for (int i = 0; i < args.size(); i++) {
        if (node.equals(args.get(i))) {
          args.set(i, stmt);
        }
      }
    }
  }
}
