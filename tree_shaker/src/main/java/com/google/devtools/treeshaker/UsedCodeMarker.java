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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

final class UsedCodeMarker extends UnitTreeVisitor {
  static final String CLASS_INITIALIZER_NAME = "<clinit>##()V";
  static final String EMPTY_METHOD_SIGNATURE = "()V";
  static final String PSEUDO_CONSTRUCTOR_PREFIX = "%%";
  static final String SIGNATURE_PREFIX = "##";

  private final Context context;

  UsedCodeMarker(CompilationUnit unit, Context context) {
    super(unit);
    this.context = context;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    context.startType(node.getTypeElement(), true);
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    context.endType();
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
    // Creating an instance of an anonymous class also creates instances of the interfaces.
    if (node.getAnonymousClassDeclaration() != null) {
      for (TypeMirror type : node.getAnonymousClassDeclaration().getSuperInterfaceTypeMirrors()) {
        context.addMethodInvocation(getPseudoConstructorName(type.toString()), type.toString());
      }
    }
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    context.startType(node.getTypeElement(), false);
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    context.endType();
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
  }

  @Override
  public void endVisit(FieldAccess node) {
    // Note: accessing a static field of a class implicitly runs the class' static initializer.
    if (node.getVariableElement().getModifiers().contains(STATIC)) {
      context.addMethodInvocation(
          CLASS_INITIALIZER_NAME,
          node.getExpression().getTypeMirror().toString());
    }
  }

  @Override
  public void endVisit(LambdaExpression node) {
    // A lambda expression implicitly constructs an instance of the interface that it implements.
    context.addMethodInvocation(
        getPseudoConstructorName(node.getTypeMirror().toString()),
        node.getTypeMirror().toString());
  }

  @Override
  public void endVisit(MarkerAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (getDeclaringClassName(node.getExecutableElement()).isEmpty()) {
      context.startAnonymousMethodDeclaration();
    } else {
      context.startMethodDeclaration(
          getMethodName(node.getExecutableElement()),
          node.isConstructor(),
          Modifier.isStatic(node.getModifiers()));
    }
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    context.endMethodDeclaration();
  }

  @Override
  public void endVisit(MethodInvocation node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
    context.addReferencedType(node.getExecutableType().getReturnType());
    node.getExecutableType().getParameterTypes().forEach(context::addReferencedType);
  }

  @Override
  public void endVisit(NormalAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    if (!node.getAnnotations().isEmpty()) {
      // Package annotations are only allowed in package-info.java files.
      context.startPackageInfo(node.getPackageElement().getQualifiedName().toString());
      node.getAnnotations().forEach(this::visitAnnotation);
      context.endPackageInfo();
    }
    return false;
  }

  @Override
  public void endVisit(PropertyAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public void endVisit(SingleMemberAnnotation node) {
    visitAnnotation(node);
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
    node.getExecutableType().getParameterTypes().forEach(context::addReferencedType);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    context.startType(node.getTypeElement(), node.isInterface());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    context.endType();
  }

  @Override
  public void endVisit(TypeMethodReference node) {
    context.addMethodInvocation(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()));
    // A method expression implicitly constructs an instance of the interface that it implements.
    context.addMethodInvocation(
        getPseudoConstructorName(node.getTypeMirror().toString()),
        node.getTypeMirror().toString());
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    context.addReferencedType(node.getVariableElement().asType());
  }

  private static String getDeclaringClassName(ExecutableElement method) {
    return ElementUtil.getDeclaringClass(method).getQualifiedName().toString();
  }

  @VisibleForTesting
  static String getQualifiedMethodName(String type, String name, String signature) {
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

  private static String getPseudoConstructorName(String typeName) {
    return getMethodName(
        PSEUDO_CONSTRUCTOR_PREFIX + typeName.substring(typeName.lastIndexOf('.') + 1),
        EMPTY_METHOD_SIGNATURE);
  }

  private void visitAnnotation(Annotation node) {
    // A reference to an annotation implicitly constructs an instance of that annotation.
    context.addMethodInvocation(
        getPseudoConstructorName(node.getTypeMirror().toString()),
        node.getTypeMirror().toString());
  }

  @VisibleForTesting
  static String eraseParametricTypes(String typeName) {
    // cases:
    // - no paramatric types: C -> C
    // - simple parametric type: C<A> -> C
    // - nested parametric type: C<D<A>> -> C
    // - nested multi-parametric type: C<D<A>,D<B>> -> C
    // - chained parametric type: C<A>.D<A> -> C.D
    int begin = typeName.indexOf('<');
    if (begin == -1) {
      return typeName;
    }
    int unmatched = 1;
    int index = begin + 1;
    while (unmatched > 0 && index < typeName.length()) {
      char current = typeName.charAt(index);
      if (current == '<') {
        unmatched++;
      } else if (current == '>') {
        unmatched--;
      }
      index++;
    }
    String first = typeName.substring(0, begin);
    if (index == typeName.length()) {
      return first;
    }
    return first + eraseParametricTypes(typeName.substring(index));
  }

  static final class Context {
    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    private interface Closer { void close(); }

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
    private final Deque<Closer> currentTypeCloserScope = new ArrayDeque<>();

    // Scope containing data for the current method being processed.
    private final Deque<MemberInfo.Builder> mibScope = new ArrayDeque<>();
    private final Deque<String> methodNameScope = new ArrayDeque<>();
    private final Deque<Set<Integer>> referencedTypesScope = new ArrayDeque<>();
    private final Deque<Closer> currentMethodCloserScope = new ArrayDeque<>();

    private final Closer nopCloser = () -> {};
    private final Closer closeTypeScopeCloser = this::closeTypeScope;
    private final Closer closeMethodScopeCloser = this::closeMethodScope;

    Context(CodeReferenceMap rootSet) {
      exportedMethods = new HashSet<>();
      rootSet.getReferencedMethods().cellSet().forEach(cell -> {
        String type  = cell.getRowKey();
        String name = cell.getColumnKey();
        cell.getValue().forEach(signature ->
            exportedMethods.add(getQualifiedMethodName(type, name, signature)));
      });
      exportedClasses = rootSet.getReferencedClasses();
    }

    LibraryInfo getLibraryInfo() {
      return lib.build();
    }

    private Integer getTypeId(String typeName) {
      String rawTypeName = eraseParametricTypes(typeName);
      Integer index = typeMap.putIfAbsent(rawTypeName, typeCount);
      if (index == null) {
        lib.addTypeMap(rawTypeName);
        return typeCount++;
      }
      return index;
    }

    private void startPackageInfo(String packageName) {
      String typeName =  packageName + ".package-info";
      startTypeScope(typeName, "java.lang.Object", false, ImmutableList.of(), true);
    }

    private void endPackageInfo() {
      endTypeScope();
    }

    private void startType(TypeElement type, boolean isInterface) {
      String typeName = type.getQualifiedName().toString();
      String superName = type.getSuperclass().toString();
      boolean isExported = exportedClasses.contains(typeName);
      startTypeScope(typeName, superName, isInterface, type.getInterfaces(), isExported);
    }

    private void endType() {
      endTypeScope();
    }

    private void startTypeScope(String typeName, String superName, boolean isInterface,
        List<? extends TypeMirror> interfaces, boolean isExported) {
      logger.atFine().log("Start Type Scope: %s extends %s", typeName, superName);

      if (typeName.isEmpty()) {
        // Anonymous types are not currently tracked.
        currentTypeCloserScope.push(nopCloser);
        return;
      }
      Integer id = getTypeId(typeName);
      Integer eid = getTypeId(superName);
      List<Integer> iids =
          interfaces.stream().map(tm -> getTypeId(tm.toString())).collect(Collectors.toList());
      currentTypeNameScope.push(typeName);
      currentTypeInfoScope.push(TypeInfo.newBuilder()
          .setTypeId(id).setExtendsType(eid).addAllImplementsType(iids).setExported(isExported));
      currentTypeCloserScope.push(closeTypeScopeCloser);
      // Push the static initializer as the current method in scope.
      startMethodScope(CLASS_INITIALIZER_NAME, MemberInfo.newBuilder()
          .setName(CLASS_INITIALIZER_NAME).setStatic(true).setExported(isExported));
      // For interfaces, add a pseudo-constructor for use with lambdas.
      if (isInterface) {
        startMethodDeclaration(getPseudoConstructorName(typeName), true, false);
        endMethodDeclaration();
      }
    }

    private void endTypeScope() {
      currentTypeCloserScope.pop().close();
    }

    private void closeTypeScope() {
      logger.atFine().log("Close Type Scope: %s", currentTypeNameScope.peek());
      // Close the current method (i.e. the static initializer).
      closeMethodScope();
      TypeInfo ti = currentTypeInfoScope.pop().build();
      currentTypeNameScope.pop();
      // Add the type info to the library info.
      lib.addType(ti);
    }

    private void startMethodScope(String methodName, MemberInfo.Builder mib) {
      mibScope.push(mib);
      methodNameScope.push(methodName);
      referencedTypesScope.push(new HashSet<>());
    }

    private void closeMethodScope() {
      logger.atFine().log("Close Method: %s", methodNameScope.peek());
      for (Integer typeId : referencedTypesScope.pop()) {
        mibScope.peek().addReferencedTypes(typeId);
      }
      methodNameScope.pop();
      MemberInfo mi = mibScope.pop().build();
      currentTypeInfoScope.peek().addMember(mi);
    }

    private void startAnonymousMethodDeclaration() {
      // Methods of anonymous clases are not currently tracked.
      currentMethodCloserScope.push(nopCloser);
    }

    private void startMethodDeclaration(
        String methodName, boolean isConstructor, boolean isStatic) {
      boolean isExported =
          exportedMethods.contains(getQualifiedMethodName(currentTypeNameScope.peek(), methodName))
          || currentTypeInfoScope.peek().getExported();
      logger.atFine().log("Start Method: %s.%s : isConstructor: %s : isStatic: %s, exported: %b",
          currentTypeNameScope.peek(), methodName, isConstructor, isStatic, isExported);
      currentMethodCloserScope.push(closeMethodScopeCloser);
      startMethodScope(methodName,
          MemberInfo.newBuilder()
          .setName(methodName)
          .setStatic(isStatic)
          .setConstructor(isConstructor)
          .setExported(isExported));
    }

    private void addMethodInvocation(String methodName, String declTypeName) {
      logger.atFine().log("Add Method Inv: type: %s method: %s", declTypeName, methodName);
      if (declTypeName.isEmpty()) {
        // Methods of anonymous classes are not currently tracked.
        return;
      }
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
      currentMethodCloserScope.pop().close();
    }
  }
}
