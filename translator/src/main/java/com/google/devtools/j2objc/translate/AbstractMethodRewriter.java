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
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.TranslationUtil;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Checks for missing methods that would cause an ObjC compilation error. Adds stubs for existing
 * abstract methods. Adds the ABSTRACT bit to a MethodDeclaration node if the method is a
 * non-default one from an interface.
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
    IMethodBinding methodBinding = node.getMethodBinding();
    if (!BindingUtil.isAbstract(methodBinding)) {
      return;
    }

    // JDT only adds the abstract bit to a MethodDeclaration node's modifiers if the abstract
    // method is from a class. Since we want our code generator to go over an interface's
    // method nodes for default method support and skip abstract methods, we add the bit if the
    // method is from an interface.
    ITypeBinding declaringClass = methodBinding.getDeclaringClass();
    boolean isInterface = declaringClass.isInterface();
    if (isInterface) {
      node.addModifiers(Modifier.ABSTRACT);
      return;
    }

    // There's no need to stub out an abstract method for an interface's companion class.
    // Similarly, if this is an abstract method in a class and there's no need for reflection,
    // we skip the stubbing out.
    if (!TranslationUtil.needsReflection(declaringClass)) {
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
    checkForIncompleteProtocol(node);
    addReturnTypeNarrowingDeclarations(node);
  }

  private void checkForIncompleteProtocol(AbstractTypeDeclaration node) {
    ITypeBinding typeBinding = node.getTypeBinding();
    if (typeBinding.isInterface() && BindingUtil.hasDefaultMethodsInFamily(typeBinding)) {
      // If there are default methods, then the interface's companion class will
      // be declared to conform to the protocol.
      unit.setHasIncompleteProtocol();
      return;
    }
    if (!Modifier.isAbstract(node.getModifiers()) && !typeBinding.isEnum()) {
      return;
    }
    // Find any interface methods that aren't defined by this abstract type so
    // we can silence incomplete protocol errors.
    // Collect needed methods from this interface and all super-interfaces.
    Queue<ITypeBinding> interfaceQueue = new LinkedList<>();
    Set<IMethodBinding> interfaceMethods = new LinkedHashSet<>();
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

  // Adds declarations for any methods where the known return type is more
  // specific than what is already declared in inherited types.
  private void addReturnTypeNarrowingDeclarations(AbstractTypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    Map<String, IMethodBinding> newDeclarations = new HashMap<>();
    Map<String, ITypeBinding> declaredReturnTypes = new HashMap<>();
    for (ITypeBinding inheritedType : BindingUtil.getOrderedInheritedTypesInclusive(type)) {
      for (IMethodBinding method : inheritedType.getDeclaredMethods()) {
        ITypeBinding returnType = method.getReturnType().getErasure();
        if (returnType.isPrimitive()) {
          continue;  // Short circuit
        }
        String selector = nameTable.getMethodSelector(method);
        ITypeBinding declaredReturnType = declaredReturnTypes.get(selector);
        if (declaredReturnType == null) {
          declaredReturnType = method.getMethodDeclaration().getReturnType().getErasure();
          declaredReturnTypes.put(selector, declaredReturnType);
        } else if (!returnType.isSubTypeCompatible(declaredReturnType)) {
          continue;
        }
        if (declaredReturnType != returnType
            && !nameTable.getObjCType(declaredReturnType).equals(
                nameTable.getObjCType(returnType))) {
          newDeclarations.put(selector, method);
          declaredReturnTypes.put(selector, returnType);
        }
      }
    }

    boolean isInterface = type.isInterface();
    for (IMethodBinding method : newDeclarations.values()) {
      node.getBodyDeclarations().add(newReturnTypeNarrowingDeclaration(method, isInterface));
    }
  }

  private MethodDeclaration newReturnTypeNarrowingDeclaration(
      IMethodBinding method, boolean isInterface) {
    MethodDeclaration decl = new MethodDeclaration(method);
    // Remove all modifiers except the visibility.
    decl.removeModifiers(~(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE));
    decl.addModifiers(Modifier.ABSTRACT | BindingUtil.ACC_SYNTHETIC);
    if (!isInterface) {
      unit.setHasIncompleteImplementation();
    }
    int argCount = 0;
    for (ITypeBinding paramType : method.getParameterTypes()) {
      decl.getParameters().add(new SingleVariableDeclaration(new GeneratedVariableBinding(
          "arg" + argCount++, 0, paramType, false, true, null, method)));
    }
    return decl;
  }
}
