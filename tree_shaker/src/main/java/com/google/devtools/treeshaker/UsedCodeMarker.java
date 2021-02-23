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

package com.google.devtools.treeshaker;

import static javax.lang.model.element.Modifier.STATIC;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table.Cell;
import com.google.common.flogger.GoogleLogger;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

final class UsedCodeMarker extends UnitTreeVisitor {
  static final String CLASS_INITIALIZER_NAME = "<clinit>##()V";
  static final String ENUM_TYPE_NAME = "java.lang.Enum";
  static final String OBJECT_TYPE_NAME = "java.lang.Object";
  static final String SIGNATURE_PREFIX = "##";

  private final Context context;

  UsedCodeMarker(CompilationUnit unit, Context context) {
    super(unit);
    this.context = context;
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    context.startType(
        getClassName(node.getTypeElement()), ENUM_TYPE_NAME);
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    context.endType();
  }

  @Override
  public void endVisit(FieldAccess node) {
    // Note: accessing a static field of a class implicitly runs the class' static initializer.
    if (node.getVariableElement().getModifiers().contains(STATIC)) {
      context.addMethodInvocation(
          CLASS_INITIALIZER_NAME,
          node.getTypeMirror().toString());
    }
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    context.startType(
        getClassName(node.getTypeElement()), node.getTypeElement().getSuperclass().toString());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    context.endType();
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    context.startMethodDeclaration(
        getMethodName(node.getExecutableElement()),
        node.isConstructor(),
        Modifier.isStatic(node.getModifiers()));
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    context.endMethodDeclaration();
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public void endVisit(MethodInvocation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
    context.addReferencedType(node.getExecutableType().getReturnType());
    for (TypeMirror type : node.getExecutableType().getParameterTypes()) {
      context.addReferencedType(type);
    }
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
    context.addReferencedType(node.getExecutableType().getReturnType());
    for (TypeMirror type : node.getExecutableType().getParameterTypes()) {
      context.addReferencedType(type);
    }
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    context.addReferencedType(node.getVariableElement().asType());
  }

  private static String getDeclaringClassName(ExecutableElement method) {
    return ElementUtil.getDeclaringClass(method).getQualifiedName().toString();
  }

  private String getClassName(TypeElement elem) {
    return elementUtil.getBinaryName(elem).replace('$', '.');
  }

  private static String getQualifiedMethodName(String type, String name, String signature) {
    return getQualifiedMethodName(type, getMethodName(name, signature));
  }

  private static String getQualifiedMethodName(String type, String nameAndSignature) {
    return type + "." + nameAndSignature;
  }

  private static String getMethodName(String name, String signature) {
    return name + SIGNATURE_PREFIX + signature;
  }

  private String getMethodName(ExecutableElement method) {
    return getMethodName(typeUtil.getReferenceName(method), typeUtil.getReferenceSignature(method));
  }

  static final class Context {
    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    // Map of type names to unique integer.
    private int typeCount;
    private final Map<String, Integer> typeMap = new HashMap<>();

    // Qualified method names that are exported (live).
    private final Set<String> exportedMethods;

    // Qualified class names that are exported (live).
    private final ImmutableSet<String> exportedClasses;

    // Library info builder, which contains all of the types processed.
    private final LibraryInfo.Builder lib = LibraryInfo.newBuilder();

    // Scope containing data for the current type being processed.
    private final Deque<String> currentTypeNameScope = new ArrayDeque<>();
    private final Deque<TypeInfo.Builder> currentTypeInfoScope = new ArrayDeque<>();

    // Scope containing data for the current method being processed.
    private final Deque<MemberInfo.Builder> mibScope = new ArrayDeque<>();
    private final Deque<String> methodNameScope = new ArrayDeque<>();
    private final Deque<Set<Integer>> referencedTypesScope = new ArrayDeque<>();

    Context(CodeReferenceMap rootSet) {
      getTypeId(OBJECT_TYPE_NAME);
      exportedMethods = new HashSet<>();
      for (Cell<String, String, ImmutableSet<String>> cell :
               rootSet.getReferencedMethods().cellSet()) {
        String type  = cell.getRowKey();
        String name = cell.getColumnKey();
        for (String signature : cell.getValue()) {
          exportedMethods.add(getQualifiedMethodName(type, name, signature));
        }
      }
      exportedClasses = rootSet.getReferencedClasses();
    }

    LibraryInfo getLibraryInfo() {
      return lib.build();
    }

    private Integer getTypeId(String typeName) {
      Integer index = typeMap.putIfAbsent(typeName, typeCount);
      if (index == null) {
        lib.addTypeMap(typeName);
        return typeCount++;
      }
      return index;
    }

    private void startType(String typeName, String extendsTypeName) {
      logger.atFine().log("Start Type: %s extends %s", typeName, extendsTypeName);
      Integer id = getTypeId(typeName);
      Integer eid = getTypeId(extendsTypeName);
      boolean isExported = exportedClasses.contains(typeName);
      // Push the new type name on top of the stack.
      currentTypeNameScope.push(typeName);
      // Push the new type info builder on top of the stack.
      currentTypeInfoScope.push(TypeInfo.newBuilder()
          .setTypeId(id).setExtendsType(eid).setExported(isExported));
      // Push the static initializer as the current method in scope.
      pushMethodScope(CLASS_INITIALIZER_NAME, MemberInfo.newBuilder()
          .setName(CLASS_INITIALIZER_NAME).setStatic(true).setExported(isExported));
    }

    private void endType() {
      logger.atFine().log("End Type: %s", currentTypeNameScope.peek());
      // Pop the current method (i.e. the static initializer).
      MemberInfo mi = popMethodScope();
      // Pop the current type info, adding the static initializer.
      TypeInfo ti =  currentTypeInfoScope.pop().addMember(mi).build();
      // Pop the current type name.
      currentTypeNameScope.pop();
      // Add the type info to the library info.
      lib.addType(ti);
    }

    private void pushMethodScope(String methodName, MemberInfo.Builder mib) {
      mibScope.push(mib);
      methodNameScope.push(methodName);
      referencedTypesScope.push(new HashSet<>());
    }

    private MemberInfo popMethodScope() {
      for (Integer typeId : referencedTypesScope.pop()) {
        mibScope.peek().addReferencedTypes(typeId);
      }
      methodNameScope.pop();
      return mibScope.pop().build();
    }

    private void startMethodDeclaration(
        String methodName, boolean isConstructor, boolean isStatic) {
      boolean isExported = exportedMethods.contains(
          getQualifiedMethodName(currentTypeNameScope.peek(), methodName));
      logger.atFine().log("Start Method: %s.%s : isConstructor: %s : isStatic: %s, exported: %b",
          currentTypeNameScope.peek(), methodName, isConstructor, isStatic, isExported);
      pushMethodScope(methodName,
          MemberInfo.newBuilder()
          .setName(methodName)
          .setStatic(isStatic)
          .setConstructor(isConstructor)
          .setExported(isExported));
    }

    private void addMethodInvocation(String methodName, String declTypeName) {
      logger.atFine().log("Add Method Inv: %s.%s", declTypeName, methodName);
      int declTypeId = getTypeId(declTypeName);
      mibScope.peek().addInvokedMethods(com.google.devtools.treeshaker.MethodInvocation.newBuilder()
          .setMethod(methodName)
          .setEnclosingType(declTypeId)
          .build());
      addReferencedTypeName(declTypeName);
    }

    private void addReferencedType(TypeMirror type) {
      boolean isPrimitive = type.getKind().isPrimitive();
      logger.atFine().log("Add ReferenecedType: %s : isPrimitve: %b", type, isPrimitive);
      if (isPrimitive)  {
        return;
      }
      addReferencedTypeName(type.toString());
    }

    private void addReferencedTypeName(String typeName) {
      logger.atFine().log("Add ReferenecedTypeName: %s", typeName);
      int typeId = getTypeId(typeName);
      referencedTypesScope.peek().add(typeId);
    }

    private void endMethodDeclaration() {
      logger.atFine().log("End Method: %s", methodNameScope.peek());
      currentTypeInfoScope.peek().addMember(popMethodScope());
    }
  }
}
