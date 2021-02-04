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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table.Cell;
import com.google.common.flogger.GoogleLogger;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
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
  static final String OBJECT_TYPE_NAME = "java.lang.Object";
  static final String SIGNATURE_PREFIX = "##";

  private final Context context;

  UsedCodeMarker(CompilationUnit unit, Context context) {
    super(unit);
    this.context = context;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    context.startType(getClassName(node.getTypeElement()), OBJECT_TYPE_NAME);
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    context.endType();
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
  public void endVisit(ClassInstanceCreation instance) {
    context.addMethodInvocation(
        getMethodName(instance.getExecutableElement()),
        getDeclaringClassName(instance.getExecutableElement()));
  }

  @Override
  public void endVisit(ConstructorInvocation invocation) {
    context.addMethodInvocation(
        getMethodName(invocation.getExecutableElement()),
        getDeclaringClassName(invocation.getExecutableElement()));
  }

  @Override
  public boolean visit(MethodDeclaration method) {
    context.startMethodDeclaration(
        getMethodName(method.getExecutableElement()),
        method.isConstructor(),
        Modifier.isStatic(method.getModifiers()));
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration method) {
    context.endMethodDeclaration();
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public void endVisit(MethodInvocation method) {
    context.addMethodInvocation(
        getMethodName(method.getExecutableElement()),
        getDeclaringClassName(method.getExecutableElement()));
    context.addReferencedType(method.getExecutableType().getReturnType());
    for (TypeMirror type : method.getExecutableType().getParameterTypes()) {
      context.addReferencedType(type);
    }
  }

  @Override
  public void endVisit(VariableDeclarationFragment decl) {
    context.addReferencedType(decl.getVariableElement().asType());
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
    // TODO(dpo): resolve how j2cl represents class and static initializers.
    if (method.getSimpleName().contentEquals("<clinit>")) {
      return "<clinit>";
    }
    if (method.getSimpleName().contentEquals("<init>")) {
      return "<init>";
    }
    return getMethodName(typeUtil.getReferenceName(method), typeUtil.getReferenceSignature(method));
  }

  static final class Context {
    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    // Map of type names to unique integer.
    private int typeCount;
    private final Map<String, Integer> typeMap = new HashMap<>();

    // Fully qualified method names that are exported (live).
    private final Set<String> exportedMethods;

    // library info builder, which contains all of the types processed.
    private final LibraryInfo.Builder lib = LibraryInfo.newBuilder();

    // Scope containing data for the current types being processed.
    private final Deque<String> currentTypeNameScope = new ArrayDeque<>();
    private final Deque<TypeInfo.Builder> currentTypeInfoScope = new ArrayDeque<>();

    // Data for the current method being processed (null if not processing a method).
    private MemberInfo.Builder mib;
    private String currentMethodName;
    private Set<Integer> currentReferencedTypes;

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
      // TODO(dpo): add support for exported classes.
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
      MemberInfo mib = MemberInfo.newBuilder().setName("$clinit").setStatic(true).build();
      // Push the new type name on top of the stack.
      currentTypeNameScope.push(typeName);
      // Push the new type infor builder on top of the stack.
      currentTypeInfoScope.push(
          TypeInfo.newBuilder().setTypeId(id).setExtendsType(eid).addMember(mib));
    }

    private void endType() {
      logger.atFine().log("End Type: %s", currentTypeNameScope.peek());
      // Add the type to the library info builder and remove from the stack.
      lib.addType(currentTypeInfoScope.pop().build());
      currentTypeNameScope.pop();
    }

    private void startMethodDeclaration(
        String methodName, boolean isConstructor, boolean isStatic) {
      logger.atFine().log("Start Method: %s.%s : isConstructor: %s : isStatic: %s",
          currentTypeNameScope.peek(), methodName, isConstructor, isStatic);
      currentMethodName = methodName;
      currentReferencedTypes = new HashSet<>();
      String qualifiedMethodName =
          getQualifiedMethodName(currentTypeNameScope.peek(), currentMethodName);
      mib = MemberInfo.newBuilder()
            .setName(currentMethodName)
            .setStatic(isStatic)
            .setConstructor(isConstructor)
            .setJsAccessible(exportedMethods.contains(qualifiedMethodName));
    }

    private void addMethodInvocation(String methodName, String declTypeName) {
      logger.atFine().log("Add Method Inv: %s.%s", declTypeName, methodName);
      int declTypeId = getTypeId(declTypeName);
      mib.addInvokedMethods(com.google.devtools.treeshaker.MethodInvocation.newBuilder()
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
      currentReferencedTypes.add(typeId);
    }

    private void endMethodDeclaration() {
      logger.atFine().log("End Method: %s", currentMethodName);
      for (Integer typeId : currentReferencedTypes) {
        mib.addReferencedTypes(typeId);
      }
      currentTypeInfoScope.peek().addMember(mib.build());
      currentMethodName = null;
      currentReferencedTypes = null;
      mib = null;
    }
  }
}
