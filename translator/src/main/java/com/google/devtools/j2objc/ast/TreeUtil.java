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
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.io.File;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

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
    @Override
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
    return getAnnotation(annotationClass, annotations) != null;
  }

  public static Annotation getAnnotation(Class<?> annotationClass, List<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      ITypeBinding annotationType = annotation.getAnnotationBinding().getAnnotationType();
      if (annotationType.getQualifiedName().equals(annotationClass.getName())) {
        return annotation;
      }
    }
    return null;
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

  public static TreeNode getNearestAncestorWithTypeOneOf(List<Class<? extends TreeNode>> types,
      TreeNode node) {
    while (node != null) {
      for (Class<? extends TreeNode> c : types) {
        if (c.isInstance(node)) {
          return node;
        }
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the first descendant of the given node that is not a ParenthesizedExpression.
   */
  public static Expression trimParentheses(Expression node) {
    while (node instanceof ParenthesizedExpression) {
      node = ((ParenthesizedExpression) node).getExpression();
    }
    return node;
  }

  /**
   * With lambdas and methodbindings, the return type of the method binding does not necessarily
   * match the return type of the functional interface, which enforces the type contracts. To get
   * the return type of a lambda or method binding, we need the return type of the functional
   * interface.
   */
  public static TypeMirror getOwningReturnType(TreeNode node) {
    while (node != null) {
      if (node instanceof MethodDeclaration) {
        return ((MethodDeclaration) node).getMethodElement().getReturnType();
      } else if (node instanceof LambdaExpression) {
        return ElementUtil.getFunctionalInterface(((LambdaExpression) node).getTypeMirror())
            .getReturnType();
      } else if (node instanceof MethodReference) {
        return ElementUtil.getFunctionalInterface(((MethodReference) node).getTypeMirror())
            .getReturnType();
      }
      node = node.getParent();
    }
    return null;
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
    return asFragments(getFieldDeclarations(node));
  }

  public static Iterable<VariableDeclarationFragment> asFragments(
      final Iterable<FieldDeclaration> fieldDecls) {
    return new Iterable<VariableDeclarationFragment>() {
      @Override
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
    return getMethodDeclarations(node.getBodyDeclarations());
  }

  public static Iterable<MethodDeclaration> getMethodDeclarations(AnonymousClassDeclaration node) {
    return getMethodDeclarations(node.getBodyDeclarations());
  }

  private static Iterable<MethodDeclaration> getMethodDeclarations(List<BodyDeclaration> nodes) {
    return Iterables.filter(nodes, MethodDeclaration.class);
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
    node = trimParentheses(node);
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

  public static VariableElement getVariableElement(Expression node) {
    return (VariableElement) BindingConverter.getElement(getVariableBinding(node));
  }

  public static IVariableBinding getVariableBinding(Name node) {
    Element element = node.getElement();
    return element != null && ElementUtil.isVariable(element)
        ? (IVariableBinding) BindingConverter.unwrapElement(element) : null;
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

  public static ITypeBinding getEnclosingTypeBinding(TreeNode node) {
    TypeDeclaration enclosingType = TreeUtil.getNearestAncestorWithType(TypeDeclaration.class,
        node);
    return enclosingType == null ? null : enclosingType.getTypeBinding();
  }

  public static IMethodBinding getEnclosingMethodBinding(TreeNode node) {
    MethodDeclaration enclosingMethod = TreeUtil.getNearestAncestorWithType(
        MethodDeclaration.class, node);
    return enclosingMethod == null ? null : enclosingMethod.getMethodBinding();
  }

  /**
   * Gets the fully qualified name of the main type in this compilation unit.
   */
  public static String getQualifiedMainTypeName(CompilationUnit unit) {
    PackageDeclaration pkg = unit.getPackage();
    if (pkg.isDefaultPackage()) {
      return unit.getMainTypeName();
    } else {
      return pkg.getName().getFullyQualifiedName() + '.' + unit.getMainTypeName();
    }
  }

  /**
   * Gets the relative file path of the source java file for this compilation
   * unit.
   */
  public static String getSourceFileName(CompilationUnit unit) {
    return getQualifiedMainTypeName(unit).replace('.', File.separatorChar) + ".java";
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
    return new LonelyStatementList(node);
  }

  /**
   * This list wraps a single statement, and inserts a block node in its place
   * upon adding additional nodes.
   */
  private static class LonelyStatementList extends AbstractList<Statement> {

    private final Statement lonelyStatement;
    private List<Statement> delegate = null;

    public LonelyStatementList(Statement stmt) {
      lonelyStatement = stmt;
    }

    private List<Statement> getDelegate() {
      if (delegate == null) {
        Block block = new Block();
        lonelyStatement.replaceWith(block);
        delegate = block.getStatements();
        delegate.add(lonelyStatement);
      }
      return delegate;
    }

    @Override
    public Statement get(int idx) {
      if (delegate != null) {
        return delegate.get(idx);
      }
      if (idx != 0) {
        throw new IndexOutOfBoundsException();
      }
      return lonelyStatement;
    }

    @Override
    public int size() {
      if (delegate != null) {
        return delegate.size();
      }
      return 1;
    }

    @Override
    public void add(int idx, Statement stmt) {
      getDelegate().add(idx, stmt);
    }
  }

  public static void insertAfter(Statement node, Statement toInsert) {
    asStatementList(node).add(toInsert);
  }

  public static void insertBefore(Statement node, Statement toInsert) {
    asStatementList(node).add(0, toInsert);
  }

  public static Expression newLiteral(Object value, Types typeEnv) {
    if (value instanceof Boolean) {
      return new BooleanLiteral((Boolean) value, typeEnv);
    } else if (value instanceof Character) {
      return new CharacterLiteral((Character) value, typeEnv);
    } else if (value instanceof Number) {
      return new NumberLiteral((Number) value, typeEnv);
    } else if (value instanceof String) {
      return new StringLiteral((String) value, typeEnv);
    }
    throw new AssertionError("unknown constant type");
  }

  /**
   * Method sorter, suitable for documentation and
   * code-completion lists.
   *
   * Sort ordering: constructors first, then alphabetical by name. If they have the
   * same name, then compare the first parameter's simple type name, then the second, etc.
   */
  public static void sortMethods(List<MethodDeclaration> methods) {
    Collections.sort(methods, new Comparator<MethodDeclaration>() {
      @Override
      public int compare(MethodDeclaration m1, MethodDeclaration m2) {
        if (m1.isConstructor() && !m2.isConstructor()) {
          return -1;
        }
        if (!m1.isConstructor() && m2.isConstructor()) {
          return 1;
        }
        String m1Name = m1.getName().getIdentifier();
        String m2Name = m2.getName().getIdentifier();
        if (!m1Name.equals(m2Name)) {
          return m1Name.compareToIgnoreCase(m2Name);
        }
        int nParams = m1.getParameters().size();
        int nOtherParams = m2.getParameters().size();
        int max = Math.min(nParams, nOtherParams);
        for (int i = 0; i < max; i++) {
          String paramType = m1.getParameter(i).getType().getTypeBinding().getName();
          String otherParamType = m2.getParameter(i).getType().getTypeBinding().getName();
          if (!paramType.equals(otherParamType)) {
            return paramType.compareToIgnoreCase(otherParamType);
          }
        }
        return nParams - nOtherParams;
      }
    });
  }

  public static List<AnnotationTypeMemberDeclaration> getAnnotationMembers(
      AbstractTypeDeclaration node) {
    return Lists.newArrayList(
        Iterables.filter(node.getBodyDeclarations(), AnnotationTypeMemberDeclaration.class));
  }
}
