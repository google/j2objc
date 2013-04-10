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

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
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
}
