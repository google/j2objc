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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.DeadCodeMap;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Iterator;
import java.util.List;

/**
 * Updates the Java AST to remove methods and classes reported as dead
 * by a ProGuard usage report.
 *
 * @author Daniel Connelly
 */
public class DeadCodeEliminator extends TreeVisitor {

  private static final Joiner innerClassJoiner = Joiner.on('$');

  private final CompilationUnit unit;
  private final DeadCodeMap deadCodeMap;

  public DeadCodeEliminator(CompilationUnit unit, DeadCodeMap deadCodeMap) {
    this.unit = unit;
    this.deadCodeMap = deadCodeMap;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    eliminateDeadCode(node.getTypeBinding(), node.getBodyDeclarations());
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();
    eliminateDeadCode(binding, node.getBodyDeclarations());
    if (deadCodeMap.isDeadClass(binding.getBinaryName())) {
      // Dead enum means none of the constants are ever used, so they can all be deleted.
      node.getEnumConstants().clear();
    }
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    eliminateDeadCode(node.getTypeBinding(), node.getBodyDeclarations());
  }

  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    eliminateDeadCode(node.getTypeBinding(), node.getBodyDeclarations());
  }

  /**
   * Remove dead members from a type.
   */
  private void eliminateDeadCode(ITypeBinding type, List<BodyDeclaration> decls) {
    String clazz = type.getBinaryName();
    if (deadCodeMap.isDeadClass(clazz)) {
      stripClass(decls);
    } else {
      removeDeadMethods(clazz, decls);
      removeDeadFields(clazz, decls);
    }
  }

  private void stripClass(List<BodyDeclaration> decls) {
    for (Iterator<BodyDeclaration> iter = decls.iterator(); iter.hasNext(); ) {
      BodyDeclaration decl = iter.next();

      // Do not strip interfaces or static nested classes. They are independent of the dead class,
      // and even if they are dead, they may still be referenced by other classes.
      if (decl instanceof TypeDeclaration) {
        ITypeBinding type = ((TypeDeclaration) decl).getTypeBinding();
        if (type.isInterface() || Modifier.isStatic(type.getModifiers())) {
          endVisit((TypeDeclaration) decl);
          continue;
        }
      }

      if (!isInlinableConstant(decl)) {
        if (decl instanceof MethodDeclaration) {
          unit.setHasIncompleteProtocol();
        }
        iter.remove();
      }
    }
  }

  private boolean isInlinableConstant(BodyDeclaration decl) {
    if (!(decl instanceof FieldDeclaration)) {
      return false;
    }
    int modifiers = decl.getModifiers();
    if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)
        || Modifier.isPrivate(modifiers)) {
      return false;
    }
    ITypeBinding type = ((FieldDeclaration) decl).getType().getTypeBinding();
    if (!(type.isPrimitive() || typeEnv.isStringType(type))) {
      return false;
    }

    // Only when every fragment has constant value do we say this is inlinable.
    for (VariableDeclarationFragment fragment : ((FieldDeclaration) decl).getFragments()) {
      if (fragment.getVariableBinding().getConstantValue() == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Remove dead methods from a type's body declarations.
   */
  private void removeDeadMethods(String clazz, List<BodyDeclaration> declarations) {
    Iterator<BodyDeclaration> declarationsIter = declarations.iterator();
    while (declarationsIter.hasNext()) {
      BodyDeclaration declaration = declarationsIter.next();
      if (declaration instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) declaration;
        // Need to keep native methods because otherwise the OCNI content will
        // be emitted without a surrounding method.
        // TODO(kstanger): Remove the method and its OCNI comment.
        if (Modifier.isNative(method.getModifiers())) {
          continue;
        }
        IMethodBinding binding = method.getMethodBinding();
        String name = getProGuardName(binding);
        String signature = BindingUtil.getProGuardSignature(binding);
        if (deadCodeMap.isDeadMethod(clazz, name, signature)) {
          if (method.isConstructor()) {
            deadCodeMap.addConstructorRemovedClass(clazz);
          }
          declarationsIter.remove();
        }
      }
    }
  }

  /**
   * Deletes non-constant dead fields from a type's body declarations list.
   */
  private void removeDeadFields(String clazz, List<BodyDeclaration> declarations) {
    Iterator<BodyDeclaration> declarationsIter = declarations.iterator();
    while (declarationsIter.hasNext()) {
      BodyDeclaration declaration = declarationsIter.next();
      if (declaration instanceof FieldDeclaration) {
        FieldDeclaration field = (FieldDeclaration) declaration;
        Iterator<VariableDeclarationFragment> fragmentsIter = field.getFragments().iterator();
        while (fragmentsIter.hasNext()) {
          VariableDeclarationFragment fragment = fragmentsIter.next();
          // Don't delete any constants because we can't detect their use.
          if (fragment.getVariableBinding().getConstantValue() == null
              && deadCodeMap.isDeadField(clazz, fragment.getName().getIdentifier())) {
            fragmentsIter.remove();
          }
        }
        if (field.getFragments().isEmpty()) {
          declarationsIter.remove();
        }
      }
    }
  }

  /**
   * Get the ProGuard name of a method.
   * For non-constructors this is the method's name.
   * For constructors of top-level classes, this is the name of the class.
   * For constructors of inner classes, this is the $-delimited name path
   * from the outermost class declaration to the inner class declaration.
   */
  private String getProGuardName(IMethodBinding method) {
    if (!method.isConstructor() || !method.getDeclaringClass().isMember()) {
      return method.getName();
    }
    ITypeBinding parent = method.getDeclaringClass();
    assert parent != null;
    List<String> components = Lists.newLinkedList(); // LinkedList is faster for prepending.
    do {
      components.add(0, parent.getName());
      parent = parent.getDeclaringClass();
    } while (parent != null);
    return innerClassJoiner.join(components);
  }
}
