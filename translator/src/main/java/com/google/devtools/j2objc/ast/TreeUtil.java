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
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of utility methods for examining tree nodes.
 */
public class TreeUtil {

  public static <T extends TreeNode> T remove(T node) {
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

  /**
   * Returns the method which is the parent of the specified node.
   */
  public static MethodDeclaration getOwningMethod(TreeNode node) {
    TreeNode n = node;
    while (n != null) {
      if (n instanceof MethodDeclaration) {
        return (MethodDeclaration) n;
      }
      n = n.getParent();
    }
    return null;
  }

  /**
   * Returns the type declaration which the specified node is part of.
   */
  public static AbstractTypeDeclaration getOwningType(TreeNode node) {
    TreeNode n = node;
    while (n != null) {
      if (n instanceof AbstractTypeDeclaration) {
        return (AbstractTypeDeclaration) n;
      }
      n = n.getParent();
    }
    return null;
  }

  /**
   * Returns the statement which is the parent of the specified node.
   */
  public static Statement getOwningStatement(TreeNode node) {
    TreeNode n = node;
    while (n != null) {
      if (n instanceof Statement) {
        return (Statement) n;
      }
      n = n.getParent();
    }
    return null;
  }

  /**
   * Gets the CompilationUnit ancestor of this node.
   */
  public static CompilationUnit getCompilationUnit(TreeNode node) {
    TreeNode n = node;
    while (n != null) {
      if (n instanceof CompilationUnit) {
        return (CompilationUnit) n;
      }
      n = n.getParent();
    }
    return null;
  }

  public static Iterable<FieldDeclaration> getFieldDeclarations(AbstractTypeDeclaration node) {
    return Iterables.filter(node.getBodyDeclarations(), FieldDeclaration.class);
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

  /**
   * Gets a variable binding for the given expression if the expression
   * represents a variable. Returns null otherwise.
   */
  public static IVariableBinding getVariableBinding(Expression node) {
    if (node instanceof FieldAccess) {
      return ((FieldAccess) node).getVariableBinding();
    } else if (node instanceof Name) {
      IBinding binding = ((Name) node).getBinding();
      return (binding instanceof IVariableBinding) ? (IVariableBinding) binding : null;
    } else if (node instanceof SuperFieldAccess) {
      return ((SuperFieldAccess) node).getVariableBinding();
    }
    return null;
  }

  /**
   * Gets the relative file path of the source java file for this compilation
   * unit.
   */
  public static String getSourceFileName(CompilationUnit unit) {
    PackageDeclaration pkg = unit.getPackage();
    if (pkg != null) {
      return pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + unit.getMainTypeName() + ".java";
    } else {
      return unit.getMainTypeName() + ".java";
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
}
