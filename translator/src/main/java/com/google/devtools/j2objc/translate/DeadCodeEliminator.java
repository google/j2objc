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

import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Updates the Java AST to remove methods and classes reported as dead
 * by a ProGuard usage report.
 *
 * @author Daniel Connelly
 */
public class DeadCodeEliminator extends UnitTreeVisitor {

  private final CodeReferenceMap deadCodeMap;

  public DeadCodeEliminator(CompilationUnit unit, CodeReferenceMap deadCodeMap) {
    super(unit);
    this.deadCodeMap = deadCodeMap;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    eliminateDeadCode(type, node.getBodyDeclarations());
    // Also strip supertypes.
    if (deadCodeMap.containsClass(elementUtil.getBinaryName(type))) {
      node.stripSupertypes();
    }
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    TypeElement type = node.getTypeElement();
    eliminateDeadCode(type, node.getBodyDeclarations());
    if (deadCodeMap.containsClass(elementUtil.getBinaryName(type))) {
      // Dead enum means none of the constants are ever used, so they can all be deleted.
      node.getEnumConstants().clear();
      node.stripSuperInterfaces();
    }
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    if (!ElementUtil.isRuntimeAnnotation(type)) {
      eliminateDeadCode(type, node.getBodyDeclarations());
    }
  }

  /**
   * Remove dead members from a type.
   */
  private void eliminateDeadCode(TypeElement type, List<BodyDeclaration> decls) {
    String clazz = elementUtil.getBinaryName(type);
    if (deadCodeMap.containsClass(clazz)) {
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
        TypeElement type = ((TypeDeclaration) decl).getTypeElement();
        if (type.getKind().isInterface() || ElementUtil.isStatic(type)) {
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
    TypeMirror type = ((FieldDeclaration) decl).getTypeMirror();
    if (!(type.getKind().isPrimitive() || typeUtil.isString(type))) {
      return false;
    }

    // Only when every fragment has constant value do we say this is inlinable.
    for (VariableDeclarationFragment fragment : ((FieldDeclaration) decl).getFragments()) {
      if (fragment.getVariableElement().getConstantValue() == null) {
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
        ExecutableElement elem = method.getExecutableElement();
        String name = typeUtil.getReferenceName(elem);
        String signature = typeUtil.getReferenceSignature(elem);
        if (deadCodeMap.containsMethod(clazz, name, signature)) {
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
          VariableElement var = fragment.getVariableElement();
          if (var.getConstantValue() == null
              && deadCodeMap.containsField(clazz, ElementUtil.getName(var))) {
            fragmentsIter.remove();
          }
        }
        if (field.getFragments().isEmpty()) {
          declarationsIter.remove();
        }
      }
    }
  }
}
