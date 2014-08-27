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

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
  public static List<Expression> getArguments(EnumConstantDeclaration node) {
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
  public static List<VariableDeclarationFragment> getFragments(FieldDeclaration node) {
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

  public static List<BodyDeclaration> getBodyDeclarations(ASTNode node) {
    if (node instanceof AbstractTypeDeclaration) {
      return getBodyDeclarations((AbstractTypeDeclaration) node);
    } else if (node instanceof AnonymousClassDeclaration) {
      return getBodyDeclarations((AnonymousClassDeclaration) node);
    } else {
      throw new AssertionError(
          "node type does not contains body declarations: " + node.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("unchecked")
  public static List<IExtendedModifier> getModifiers(BodyDeclaration node) {
    return node.modifiers();
  }

  @SuppressWarnings("unchecked")
  public static List<IExtendedModifier> getModifiers(VariableDeclarationStatement node) {
    return node.modifiers();
  }

  @SuppressWarnings("unchecked")
  public static List<IExtendedModifier> getModifiers(VariableDeclarationExpression node) {
    return node.modifiers();
  }

  @SuppressWarnings("unchecked")
  public static List<IExtendedModifier> getModifiers(SingleVariableDeclaration node) {
    return node.modifiers();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getExpressions(ArrayInitializer node) {
    return node.expressions();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getDimensions(ArrayCreation node) {
    return node.dimensions();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getInitializers(ForStatement node) {
    return node.initializers();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> getUpdaters(ForStatement node) {
    return node.updaters();
  }

  @SuppressWarnings("unchecked")
  public static List<Type> getSuperInterfaceTypes(TypeDeclaration node) {
    return node.superInterfaceTypes();
  }

  @SuppressWarnings("unchecked")
  public static List<Type> getTypeBounds(TypeParameter node) {
    return node.typeBounds();
  }

  @SuppressWarnings("unchecked")
  public static List<TypeParameter> getTypeParameters(MethodDeclaration node) {
    return node.typeParameters();
  }

  @SuppressWarnings("unchecked")
  public static List<Type> getTypeArguments(ParameterizedType node) {
    return node.typeArguments();
  }

  @SuppressWarnings("unchecked")
  public static List<EnumConstantDeclaration> getEnumConstants(EnumDeclaration node) {
    return node.enumConstants();
  }

  @SuppressWarnings("unchecked")
  public static List<AbstractTypeDeclaration> getTypes(CompilationUnit unit) {
    return unit.types();
  }

  @SuppressWarnings("unchecked")
  public static List<Comment> getCommentList(CompilationUnit unit) {
    return unit.getCommentList();
  }

  @SuppressWarnings("unchecked")
  public static List<Type> getTypes(UnionType unionType) {
    return unionType.types();
  }

  @SuppressWarnings("unchecked")
  public static List<VariableDeclarationExpression> getResources(TryStatement tryStatement) {
    return tryStatement.resources();
  }

  @SuppressWarnings("unchecked")
  public static List<CatchClause> getCatchClauses(TryStatement node) {
    return node.catchClauses();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void setProperty(ASTNode node, ASTNode newNode) {
    ASTNode parent = node.getParent();
    StructuralPropertyDescriptor locator = node.getLocationInParent();
    if (locator instanceof ChildPropertyDescriptor) {
      parent.setStructuralProperty(locator, newNode);
    } else {
      // JDT doesn't directly support ChildListProperty replacement.
      List args;
      if (parent instanceof ArrayCreation) {
        args = ((ArrayCreation) parent).dimensions();
      } else if (parent instanceof ArrayInitializer) {
        args = ((ArrayInitializer) parent).expressions();
      } else if (parent instanceof Block) {
        args = ((Block) parent).statements();
      } else if (parent instanceof ClassInstanceCreation) {
        args = ((ClassInstanceCreation) parent).arguments();
      } else if (parent instanceof ConstructorInvocation) {
        args = ((ConstructorInvocation) parent).arguments();
      } else if (parent instanceof EnumConstantDeclaration) {
        args = ((EnumConstantDeclaration) parent).arguments();
      } else if (parent instanceof InfixExpression) {
        args = ((InfixExpression) parent).extendedOperands();
      } else if (parent instanceof MethodInvocation) {
        args = ((MethodInvocation) parent).arguments();
      } else if (parent instanceof SuperConstructorInvocation) {
        args = ((SuperConstructorInvocation) parent).arguments();
      } else if (parent instanceof SuperMethodInvocation) {
        args = ((SuperMethodInvocation) parent).arguments();
      } else if (parent instanceof SwitchStatement) {
        args = ((SwitchStatement) parent).statements();
      } else if (parent instanceof TypeDeclaration) {
        args = ((TypeDeclaration) parent).superInterfaceTypes();
      } else {
        throw new AssertionError("unknown parent node type: " + parent.getClass().getSimpleName());
      }
      for (int i = 0; i < args.size(); i++) {
        if (node.equals(args.get(i))) {
          args.set(i, newNode);
        }
      }
    }
  }

  /**
   * Returns the given statement as a list of statements that can be added to.
   * If node is a Block, then returns it's statement list. If node is the direct
   * child of a Block, returns the sublist containing node as the only element.
   * Otherwise, creates a new Block node in the place of node and returns its
   * list of statements.
   */
  public static List<Statement> asStatementList(Statement node) {
    if (node instanceof Block) {
      return getStatements((Block) node);
    }
    ASTNode parent = node.getParent();
    if (parent instanceof Block) {
      List<Statement> stmts = getStatements((Block) parent);
      for (int i = 0; i < stmts.size(); i++) {
        if (stmts.get(i) == node) {
          return stmts.subList(i, i + 1);
        }
      }
    }
    Block block = node.getAST().newBlock();
    setProperty(node, block);
    getStatements(block).add(node);
    return getStatements(block);
  }

  public static void insertAfter(Statement node, Statement toInsert) {
    asStatementList(node).add(toInsert);
  }

  public static void insertBefore(Statement node, Statement toInsert) {
    asStatementList(node).add(0, toInsert);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getProperties(ASTNode node) {
    return node.properties();
  }

  /**
   * Returns the type declaration which the specified node is part of.
   */
  public static AbstractTypeDeclaration getOwningType(ASTNode node) {
    ASTNode n = node;
    while (n != null) {
      if (n instanceof AbstractTypeDeclaration) {
        return (AbstractTypeDeclaration) n;
      }
      n = n.getParent();
    }
    return null;
  }

  /**
   * Returns the method which is the parent of the specified node.
   */
  public static MethodDeclaration getOwningMethod(ASTNode node) {
    ASTNode n = node;
    while (n != null) {
      if (n instanceof MethodDeclaration) {
        return (MethodDeclaration) n;
      }
      n = n.getParent();
    }
    return null;
  }

  /**
   * Returns the statement which is the parent of the specified node.
   */
  public static Statement getOwningStatement(ASTNode node) {
    ASTNode n = node;
    while (n != null) {
      if (n instanceof Statement) {
        return (Statement) n;
      }
      n = n.getParent();
    }
    return null;
  }

  public static List<MethodDeclaration> getMethodDeclarations(AbstractTypeDeclaration node) {
    List<MethodDeclaration> methods = Lists.newArrayList();
    for (BodyDeclaration bodyDecl : getBodyDeclarations(node)) {
      if (bodyDecl instanceof MethodDeclaration) {
        methods.add((MethodDeclaration) bodyDecl);
      }
    }
    return methods;
  }

  public static Iterable<FieldDeclaration> getFieldDeclarations(AbstractTypeDeclaration node) {
    return Iterables.filter(getBodyDeclarations(node), FieldDeclaration.class);
  }

  public static Iterable<VariableDeclarationFragment> getAllFields(
      AbstractTypeDeclaration node) {
    final Iterable<FieldDeclaration> fieldDecls = getFieldDeclarations(node);
    return new Iterable<VariableDeclarationFragment>() {
      public Iterator<VariableDeclarationFragment> iterator() {
        final Iterator<FieldDeclaration> fieldIter = fieldDecls.iterator();
        return new AbstractIterator<VariableDeclarationFragment>() {
          private Iterator<VariableDeclarationFragment> fragIter;
          @Override protected VariableDeclarationFragment computeNext() {
            do {
              if (fragIter != null && fragIter.hasNext()) {
                return fragIter.next();
              }
              if (fieldIter.hasNext()) {
                fragIter = ASTUtil.getFragments(fieldIter.next()).iterator();
              }
            } while (fieldIter.hasNext() || (fragIter != null && fragIter.hasNext()));
            return endOfData();
          }
        };
      }
    };
  }
}
