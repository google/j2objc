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
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.TranslationUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Checks for missing methods that would cause an ObjC compilation error.
 * Adds stubs for existing abstract methods.
 *
 * @author Tom Ball, Keith Stanger
 */
public class AbstractMethodRewriter extends TreeVisitor {

  private final CompilationUnit unit;

  public AbstractMethodRewriter(CompilationUnit unit) {
    this.unit = unit;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    if (Modifier.isAbstract(node.getModifiers())) {
      if (!TranslationUtil.needsReflection(node.getMethodBinding().getDeclaringClass())) {
        unit.setHasIncompleteProtocol();
        unit.setHasIncompleteImplementation();
        return;
      }
      Block body = new Block();
      // Generate a body which throws a NSInvalidArgumentException.
      String bodyCode = "// can't call an abstract method\n"
          + "[self doesNotRecognizeSelector:_cmd];";
      if (!BindingUtil.isVoid(node.getReturnType().getTypeBinding())) {
        bodyCode += "\nreturn 0;"; // Never executes, but avoids a gcc warning.
      }
      body.getStatements().add(new NativeStatement(bodyCode));
      node.setBody(body);
      node.removeModifiers(Modifier.ABSTRACT);
    }
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    visitType(node);
  }

  private void visitType(AbstractTypeDeclaration node) {
    ITypeBinding typeBinding = node.getTypeBinding();
    if (!Modifier.isAbstract(node.getModifiers()) && !typeBinding.isEnum()) {
      return;
    }
    // Find any interface methods that aren't defined by this abstract type so
    // we can silence incomplete protocol errors.
    // Collect needed methods from this interface and all super-interfaces.
    Queue<ITypeBinding> interfaceQueue = new LinkedList<ITypeBinding>();
    Set<IMethodBinding> interfaceMethods = new LinkedHashSet<IMethodBinding>();
    interfaceQueue.addAll(Arrays.asList(typeBinding.getInterfaces()));
    ITypeBinding intrface;
    while ((intrface = interfaceQueue.poll()) != null) {
      interfaceMethods.addAll(Arrays.asList(intrface.getDeclaredMethods()));
      interfaceQueue.addAll(Arrays.asList(intrface.getInterfaces()));
    }

    // Check if any interface methods are missing from the implementation
    for (IMethodBinding interfaceMethod : interfaceMethods) {
      if (!isMethodImplemented(typeBinding, interfaceMethod)) {
        unit.setHasIncompleteProtocol();
      }
    }
  }

  private boolean isMethodImplemented(ITypeBinding type, IMethodBinding method) {
    if (type == null) {
      return false;
    }

    for (IMethodBinding m : type.getDeclaredMethods()) {
      if (method.isSubsignature(m)
          || (method.getName().equals(m.getName())
          && method.getReturnType().getErasure().isEqualTo(m.getReturnType().getErasure())
          && Arrays.equals(method.getParameterTypes(), m.getParameterTypes()))) {
        return true;
      }
    }

    return isMethodImplemented(type.getSuperclass(), method);
  }
}
