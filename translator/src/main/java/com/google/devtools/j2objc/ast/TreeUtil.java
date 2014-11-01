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

package com.google.devtools.j2objc.ast;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of utility methods for examining tree nodes.
 */
public class TreeUtil {

  public static <T extends TreeNode> T remove(T node) {
    if (node == null) {
      return null;
    }
    node.remove();
    return node;
  }

  public static <T extends TreeNode> List<T> copyList(List<T> originalList) {
    List<T> newList = Lists.newArrayListWithCapacity(originalList.size());
    copyList(originalList, newList);
    return newList;
  }

  @SuppressWarnings("unchecked")
  public static <T extends TreeNode> void copyList(List<T> fromList, List<T> toList) {
    for (T elem : fromList) {
      toList.add((T) elem.copy());
    }
  }

  /**
   * Moves nodes from one list to another, ensuring that they are not
   * double-parented in the process.
   */
  public static <T> void moveList(List<T> fromList, List<T> toList) {
    for (Iterator<T> iter = fromList.iterator(); iter.hasNext(); ) {
      T elem = iter.next();
      iter.remove();
      toList.add(elem);
    }
  }

  private static final Predicate<Annotation> IS_RUNTIME_PREDICATE = new Predicate<Annotation>() {
    public boolean apply(Annotation annotation) {
      return BindingUtil.isRuntimeAnnotation(annotation.getAnnotationBinding());
    }
  };

  public static Iterable<Annotation> getRuntimeAnnotations(Iterable<Annotation> annotations) {
    return Iterables.filter(annotations, IS_RUNTIME_PREDICATE);
  }

  public static List<Annotation> getRuntimeAnnotationsList(Iterable<Annotation> annotations) {
    return Lists.newArrayList(getRuntimeAnnotations(annotations));
  }

  public static boolean hasAnnotation(Class<?> annotationClass, List<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      ITypeBinding annotationType = annotation.getAnnotationBinding().getAnnotationType();
      if (annotationType.getQualifiedName().equals(annotationClass.getName())) {
        return true;
      }
    }
    return false;
  }

  public static <T extends TreeNode> T getNearestAncestorWithType(Class<T> type, TreeNode node) {
    while (node != null) {
      if (type.isInstance(node)) {
        return type.cast(node);
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the method which is the parent of the specified node.
   */
  public static MethodDeclaration getOwningMethod(TreeNode node) {
    return getNearestAncestorWithType(MethodDeclaration.class, node);
  }

  /**
   * Returns the type declaration which the specified node is part of.
   */
  public static AbstractTypeDeclaration getOwningType(TreeNode node) {
    return getNearestAncestorWithType(AbstractTypeDeclaration.class, node);
  }

  /**
   * Returns the statement which is the parent of the specified node.
   */
  public static Statement getOwningStatement(TreeNode node) {
    return getNearestAncestorWithType(Statement.class, node);
  }

  /**
   * Gets the CompilationUnit ancestor of this node.
   */
  public static CompilationUnit getCompilationUnit(TreeNode node) {
    return getNearestAncestorWithType(CompilationUnit.class, node);
  }

  public static Iterable<FieldDeclaration> getFieldDeclarations(AbstractTypeDeclaration node) {
    return Iterables.filter(node.getBodyDeclarations(), FieldDeclaration.class);
  }

  public static List<FieldDeclaration> getFieldDeclarationsList(AbstractTypeDeclaration node) {
    return Lists.newArrayList(getFieldDeclarations(node));
  }

  public static Iterable<VariableDeclarationFragment> getAllFields(AbstractTypeDeclaration node) {
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
                fragIter = fieldIter.next().getFragments().iterator();
              }
            } while (fieldIter.hasNext() || (fragIter != null && fragIter.hasNext()));
            return endOfData();
          }
        };
      }
    };
  }

  public static Iterable<MethodDeclaration> getMethodDeclarations(AbstractTypeDeclaration node) {
    return Iterables.filter(node.getBodyDeclarations(), MethodDeclaration.class);
  }

  public static List<MethodDeclaration> getMethodDeclarationsList(AbstractTypeDeclaration node) {
    return Lists.newArrayList(getMethodDeclarations(node));
  }

  public static Iterable<FunctionDeclaration> getFunctionDeclarations(
      AbstractTypeDeclaration node) {
    return Iterables.filter(node.getBodyDeclarations(), FunctionDeclaration.class);
  }

  public static List<BodyDeclaration> getBodyDeclarations(TreeNode node) {
    if (node instanceof AbstractTypeDeclaration) {
      return ((AbstractTypeDeclaration) node).getBodyDeclarations();
    } else if (node instanceof AnonymousClassDeclaration) {
      return ((AnonymousClassDeclaration) node).getBodyDeclarations();
    } else {
      throw new AssertionError(
          "node type does not contains body declarations: " + node.getClass().getSimpleName());
    }
  }

  public static List<BodyDeclaration> asDeclarationSublist(BodyDeclaration node) {
    List<BodyDeclaration> declarations = getBodyDeclarations(node.getParent());
    int index = declarations.indexOf(node);
    assert index != -1;
    return declarations.subList(index, index + 1);
  }

  /**
   * Gets a variable binding for the given expression if the expression
   * represents a variable. Returns null otherwise.
   */
  public static IVariableBinding getVariableBinding(Expression node) {
    switch (node.getKind()) {
      case FIELD_ACCESS:
        return ((FieldAccess) node).getVariableBinding();
      case SUPER_FIELD_ACCESS:
        return ((SuperFieldAccess) node).getVariableBinding();
      case QUALIFIED_NAME:
      case SIMPLE_NAME:
        return getVariableBinding((Name) node);
      default:
        return null;
    }
  }

  public static IVariableBinding getVariableBinding(Name node) {
    IBinding binding = node.getBinding();
    return (binding instanceof IVariableBinding) ? (IVariableBinding) binding : null;
  }

  public static IMethodBinding getMethodBinding(Expression node) {
    switch (node.getKind()) {
      case CLASS_INSTANCE_CREATION:
        return ((ClassInstanceCreation) node).getMethodBinding();
      case METHOD_INVOCATION:
        return ((MethodInvocation) node).getMethodBinding();
      case SUPER_METHOD_INVOCATION:
        return ((SuperMethodInvocation) node).getMethodBinding();
      default:
        return null;
    }
  }

  /**
   * Gets the relative file path of the source java file for this compilation
   * unit.
   */
  public static String getSourceFileName(CompilationUnit unit) {
    PackageDeclaration pkg = unit.getPackage();
    if (pkg.isDefaultPackage()) {
      return unit.getMainTypeName() + ".java";
    } else {
      return pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + unit.getMainTypeName() + ".java";
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
      return ((Block) node).getStatements();
    }
    TreeNode parent = node.getParent();
    if (parent instanceof Block) {
      List<Statement> stmts = ((Block) parent).getStatements();
      for (int i = 0; i < stmts.size(); i++) {
        if (stmts.get(i) == node) {
          return stmts.subList(i, i + 1);
        }
      }
    }
    Block block = new Block();
    node.replaceWith(block);
    block.getStatements().add(node);
    return block.getStatements();
  }

  public static void insertAfter(Statement node, Statement toInsert) {
    asStatementList(node).add(toInsert);
  }

  public static void insertBefore(Statement node, Statement toInsert) {
    asStatementList(node).add(0, toInsert);
  }

  /**
   * Replaces (in place) a QualifiedName node with an equivalent FieldAccess
   * node. This is helpful when a mutation needs to replace the qualifier with
   * a node that has Expression type but not Name type.
   */
  public static FieldAccess convertToFieldAccess(QualifiedName node) {
    TreeNode parent = node.getParent();
    if (parent instanceof QualifiedName) {
      FieldAccess newParent = convertToFieldAccess((QualifiedName) parent);
      Expression expr = newParent.getExpression();
      assert expr instanceof QualifiedName;
      node = (QualifiedName) expr;
    }
    IVariableBinding variableBinding = getVariableBinding(node);
    assert variableBinding != null : "node must be a variable";
    FieldAccess newNode = new FieldAccess(variableBinding, remove(node.getQualifier()));
    node.replaceWith(newNode);
    return newNode;
  }

  public static Expression newLiteral(Object value) {
    if (value instanceof Boolean) {
      return new BooleanLiteral((Boolean) value);
    } else if (value instanceof Character) {
      return new CharacterLiteral((Character) value);
    } else if (value instanceof Number) {
      return new NumberLiteral((Number) value);
    } else if (value instanceof String) {
      return new StringLiteral((String) value);
    }
    throw new AssertionError("unknown constant type");
  }

  /**
   * If possible give this expression an unbalanced extra retain. The caller
   * must ensure the result is eventually consumed. Used to avoid an autorelease
   * when creating a new object.
   */
  public static boolean retainResult(Expression node) {
    switch (node.getKind()) {
      case ARRAY_CREATION:
        ((ArrayCreation) node).setHasRetainedResult(true);
        return true;
      case CLASS_INSTANCE_CREATION:
        ((ClassInstanceCreation) node).setHasRetainedResult(true);
        return true;
      default:
        return false;
    }
  }
}
