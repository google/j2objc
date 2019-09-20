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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.Comment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TreeNode.Kind;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

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
    eliminateDeadCode(type, node);
    // Also strip supertypes.
    if (deadCodeMap.containsClass(elementUtil.getBinaryName(type))) {
      node.stripSupertypes();
    }
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    TypeElement type = node.getTypeElement();
    eliminateDeadCode(type, node);
    if (deadCodeMap.containsClass(elementUtil.getBinaryName(type))) {
      // Dead enum means none of the constants are ever used, so they can all be deleted.
      node.getEnumConstants().clear();
      node.stripSuperInterfaces();
    }
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    if (!ElementUtil.isGeneratedAnnotation(type)) {
      eliminateDeadCode(type, node);
    }
  }

  /**
   * Remove dead members from a type.
   */
  private void eliminateDeadCode(TypeElement type, AbstractTypeDeclaration node) {
    List<BodyDeclaration> decls = node.getBodyDeclarations();
    String clazz = elementUtil.getBinaryName(type);
    if (deadCodeMap.containsClass(clazz)) {
      stripClass(node);
    } else {
      removeDeadMethods(clazz, decls);
      removeDeadFields(clazz, decls);
    }
  }

  private void stripClass(AbstractTypeDeclaration node) {
    boolean removeClass = true;
    for (Iterator<BodyDeclaration> iter = node.getBodyDeclarations().iterator(); iter.hasNext(); ) {
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

      if (decl.getKind() == Kind.FIELD_DECLARATION) {
        FieldDeclaration field = (FieldDeclaration) decl;
        Iterator<VariableDeclarationFragment> fragmentsIter = field.getFragments().iterator();
        while (fragmentsIter.hasNext()) {
          VariableDeclarationFragment fragment = fragmentsIter.next();
          // Don't delete any constants because we can't detect their use.
          if (fragment.getVariableElement().getConstantValue() == null) {
            fragmentsIter.remove();
          } else {
            removeClass = false;
          }
        }
        if (field.getFragments().isEmpty()) {
          iter.remove();
        }
      } else {
        if (decl instanceof MethodDeclaration) {
          unit.setHasIncompleteProtocol();
        }
        iter.remove();
      }
    }

    if (removeClass) {
      node.setDeadClass(true);

      // Remove any class-level OCNI comment blocks.
      int srcStart = node.getStartPosition();
      String src = unit.getSource().substring(srcStart, srcStart + node.getLength());
      if (src.contains("/*-[")) {
        int ocniStart = srcStart + src.indexOf("/*-[");
        int ocniEnd = ocniStart + node.getLength();
        Iterator<Comment> commentsIter = unit.getCommentList().iterator();
        while (commentsIter.hasNext()) {
          Comment comment = commentsIter.next();
          if (comment.isBlockComment()
              && comment.getStartPosition() >= ocniStart
              && (comment.getStartPosition() + comment.getLength()) <= ocniEnd) {
            commentsIter.remove();
          }
        }
      }
    }
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
        ExecutableElement elem = method.getExecutableElement();
        String name = typeUtil.getReferenceName(elem);
        String signature = typeUtil.getReferenceSignature(elem);
        if (deadCodeMap.containsMethod(clazz, name, signature)) {
          if (method.isConstructor()) {
            deadCodeMap.addConstructorRemovedClass(clazz);
          }
          if (Modifier.isNative(method.getModifiers())) {
            removeMethodOCNI(method);
          }
          declarationsIter.remove();
        }
      }
    }
  }

  /**
   * Remove the OCNI comment associated with a native method, if it exists.
   */
  private void removeMethodOCNI(MethodDeclaration method) {
    int methodStart = method.getStartPosition();
    String src = unit.getSource().substring(methodStart, methodStart + method.getLength());
    if (src.contains("/*-[")) {
      int ocniStart = methodStart + src.indexOf("/*-[");
      Iterator<Comment> commentsIter = unit.getCommentList().iterator();
      while (commentsIter.hasNext()) {
        Comment comment = commentsIter.next();
        if (comment.isBlockComment() && comment.getStartPosition() == ocniStart) {
          commentsIter.remove();
          break;
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
          // Don't delete any constants because we can't detect their use. Instead,
          // these are translated by the TypeDeclarationGenerator as #define directives,
          // so the enclosing type can still be deleted if otherwise empty.
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

  /**
   * Remove empty classes marked as dead. This needs to be done after translation
   * to avoid inner class references in the AST returned by DeadCodeEliminator.
   */
  public static void removeDeadClasses(CompilationUnit unit, CodeReferenceMap deadCodeMap) {
    ElementUtil elementUtil = unit.getEnv().elementUtil();
    Iterator<AbstractTypeDeclaration> iter = unit.getTypes().iterator();
    while (iter.hasNext()) {
      AbstractTypeDeclaration type = iter.next();
      TypeElement typeElement = type.getTypeElement();
      if (!ElementUtil.isGeneratedAnnotation(typeElement)) {
        if (deadCodeMap.containsClass(typeElement, elementUtil)) {
          type.setDeadClass(true);
        } else {
          // Keep class, remove dead interfaces.
          if (typeElement.getInterfaces().size() > 0) {
            GeneratedTypeElement replacement = GeneratedTypeElement.mutableCopy(typeElement);
            for (TypeElement intrface : ElementUtil.getInterfaces(typeElement)) {
              if (!deadCodeMap.containsClass(intrface, elementUtil)) {
                replacement.addInterface(intrface.asType());
              }
            }
            if (typeElement.getInterfaces().size() > replacement.getInterfaces().size()) {
              type.setTypeElement(replacement);
            }
          }
        }
      }
    }
  }
}
