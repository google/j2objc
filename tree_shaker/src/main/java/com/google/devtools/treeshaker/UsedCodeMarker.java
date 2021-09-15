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

import static java.lang.Math.max;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

final class UsedCodeMarker extends UnitTreeVisitor {
  static final String CLASS_INITIALIZER_NAME = "<clinit>##()V";
  static final String EMPTY_METHOD_SIGNATURE = "()V";
  static final String INTERFACE_SUPERTYPE = "none";
  static final String PSEUDO_CONSTRUCTOR_PREFIX = "%%";
  static final String SIGNATURE_PREFIX = "##";

  private final Context context;

  UsedCodeMarker(CompilationUnit unit, Context context) {
    super(unit);
    this.context = context;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    startType(node.getTypeElement(), true);
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    endType();
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    addMethodInvocation(node.getExecutableElement());
    // Creating an instance of an anonymous class also creates instances of the interfaces.
    if (node.getAnonymousClassDeclaration() != null) {
      for (TypeMirror type : node.getAnonymousClassDeclaration().getSuperInterfaceTypeMirrors()) {
        addPseudoConstructorInvocation(type);
      }
    }
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    addMethodInvocation(node.getExecutableElement());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    addMethodInvocation(node.getExecutableElement());
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    startType(node.getTypeElement(), false);
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    endType();
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    addMethodInvocation(node.getExecutableElement());
  }

  @Override
  public void endVisit(LambdaExpression node) {
    // A lambda expression implicitly constructs an instance of the interface that it implements.
    addPseudoConstructorInvocation(node.getTypeMirror());
  }

  @Override
  public void endVisit(MarkerAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    startMethodDeclaration(
        getMethodName(node.getExecutableElement()),
        getDeclaringClassName(node.getExecutableElement()),
        node.isConstructor(),
        Modifier.isStatic(node.getModifiers()));
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    endMethodDeclaration();
  }

  @Override
  public void endVisit(MethodInvocation node) {
    addMethodInvocation(node.getExecutableElement());
    addReferencedType(node.getExecutableType().getReturnType());
    node.getExecutableType().getParameterTypes().forEach(this::addReferencedType);
  }

  @Override
  public void endVisit(NormalAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    if (!node.getAnnotations().isEmpty()) {
      // Package annotations are only allowed in package-info.java files.
      startPackage(node.getPackageElement());
      node.getAnnotations().forEach(this::visitAnnotation);
      endPackage();
    }
    return false;
  }

  @Override
  public void endVisit(PropertyAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public void endVisit(SimpleName node) {
    // For variable references to static fields in a different class, add a reference to
    // that class.
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null && var.getKind().isField() && var.getModifiers().contains(STATIC)) {
      String declTypeName = elementUtil.getBinaryName(ElementUtil.getDeclaringClass(var));
      if (!declTypeName.equals(context.currentTypeNameScope.peek())) {
        addMethodInvocation(CLASS_INITIALIZER_NAME, declTypeName);
      }
    }
  }

  @Override
  public void endVisit(SingleMemberAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    addMethodInvocation(node.getExecutableElement());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    addMethodInvocation(node.getExecutableElement());
    addReferencedType(node.getExecutableType().getReturnType());
    node.getExecutableType().getParameterTypes().forEach(this::addReferencedType);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    startType(node.getTypeElement(), node.isInterface());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    endType();
  }

  @Override
  public void endVisit(TypeMethodReference node) {
    addMethodInvocation(node.getExecutableElement());
    // A method expression implicitly constructs an instance of the interface that it implements.
    addPseudoConstructorInvocation(node.getTypeMirror());
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    addReferencedType(node.getVariableElement().asType());
  }

  private String getDeclaringClassName(ExecutableElement method) {
    return elementUtil.getBinaryName(ElementUtil.getDeclaringClass(method));
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

  private String getTypeMirrorName(TypeMirror type) {
    TypeElement typeElement = TypeUtil.asTypeElement(type);
    return typeElement == null ? type.toString() : elementUtil.getBinaryName(typeElement);
  }

  private static String getPseudoConstructorName(String type) {
    int index = max(type.lastIndexOf('.'), type.lastIndexOf('$')) + 1;
    return getMethodName(PSEUDO_CONSTRUCTOR_PREFIX + type.substring(index), EMPTY_METHOD_SIGNATURE);
  }

  private static boolean isUntrackedClass(String typeName) {
    return typeName.indexOf('.') == -1;
  }

  private void visitAnnotation(Annotation node) {
    // A reference to an annotation implicitly constructs an instance of that annotation.
    addPseudoConstructorInvocation(node.getTypeMirror());
  }

  private Integer getTypeId(String typeName) {
    Integer index = context.typeMap.putIfAbsent(typeName, context.typeCount);
    if (index == null) {
      context.libraryInfoBuilder.addTypeMap(typeName);
      return context.typeCount++;
    }
    return index;
  }

  private void startPackage(PackageElement pkg) {
    String pkgName =  pkg.getQualifiedName() + ".package-info";
    startTypeScope(pkgName, INTERFACE_SUPERTYPE, false, ImmutableList.of(), true);
  }

  private void endPackage() {
    endTypeScope();
  }

  private void startType(TypeElement type, boolean isInterface) {
    String typeName = elementUtil.getBinaryName(type);
    TypeElement superType = ElementUtil.getSuperclass(type);
    String superName =
        superType == null ? INTERFACE_SUPERTYPE : elementUtil.getBinaryName(superType);
    List<String> interfaces = ElementUtil.getInterfaces(type).stream()
                                .map(elementUtil::getBinaryName).collect(Collectors.toList());
    boolean isExported = context.exportedClasses.contains(typeName)
                         || ElementUtil.isGeneratedAnnotation(type);
    startTypeScope(typeName, superName, isInterface, interfaces, isExported);
  }

  private void endType() {
    endTypeScope();
  }

  private void startTypeScope(String typeName, String superName, boolean isInterface,
      List<String> interfaces, boolean isExported) {
    Integer id = getTypeId(typeName);
    Integer eid = getTypeId(superName);
    List<Integer> iids = interfaces.stream().map(this::getTypeId).collect(Collectors.toList());
    context.currentTypeNameScope.push(typeName);
    context.currentTypeInfoScope.push(TypeInfo.newBuilder()
        .setTypeId(id).setExtendsType(eid).addAllImplementsType(iids).setExported(isExported));
    // Push the static initializer as the current method in scope.
    startMethodScope(CLASS_INITIALIZER_NAME, MemberInfo.newBuilder()
        .setName(CLASS_INITIALIZER_NAME).setStatic(true).setExported(isExported));
    // For interfaces, add a pseudo-constructor for use with lambdas.
    if (isInterface) {
      startMethodDeclaration(getPseudoConstructorName(typeName), typeName, true, false);
      endMethodDeclaration();
    }
  }

  private void endTypeScope() {
    // Close the current method (i.e. the static initializer).
    endMethodDeclaration();
    TypeInfo ti = context.currentTypeInfoScope.pop().build();
    context.currentTypeNameScope.pop();
    // Add the type info to the library info.
    context.libraryInfoBuilder.addType(ti);
  }

  private void startMethodScope(String methodName, MemberInfo.Builder mib) {
    context.mibScope.push(mib);
    context.methodNameScope.push(methodName);
    context.referencedTypesScope.push(new HashSet<>());
  }

  private void startMethodDeclaration(
      String methodName, String declTypeName, boolean isConstructor, boolean isStatic) {
    boolean isExported =
        context.exportedMethods.contains(getQualifiedMethodName(declTypeName, methodName))
        || context.currentTypeInfoScope.peek().getExported();
    startMethodScope(methodName,
        MemberInfo.newBuilder()
        .setName(methodName)
        .setStatic(isStatic)
        .setConstructor(isConstructor)
        .setExported(isExported));
  }

  private void addPseudoConstructorInvocation(TypeMirror type) {
    String typeName = getTypeMirrorName(type);
    addMethodInvocation(getPseudoConstructorName(typeName), typeName);
  }

  private void addMethodInvocation(ExecutableElement element) {
    addMethodInvocation(getMethodName(element), getDeclaringClassName(element));
  }

  private void addMethodInvocation(String methodName, String declTypeName) {
    if (isUntrackedClass(declTypeName)) {
      // Methods of anonymous and local classes are not tracked.
      return;
    }
    int declTypeId = getTypeId(declTypeName);
    context.mibScope.peek()
            .addInvokedMethods(com.google.devtools.treeshaker.MethodInvocation.newBuilder()
                .setMethod(methodName)
                .setEnclosingType(declTypeId)
                .build());
    addReferencedTypeName(declTypeName);
  }

  private void addReferencedType(TypeMirror type) {
    if (type.getKind().isPrimitive())  {
      return;
    }
    addReferencedTypeName(getTypeMirrorName(type));
  }

  private void addReferencedTypeName(String typeName) {
    int typeId = getTypeId(typeName);
    context.referencedTypesScope.peek().add(typeId);
  }

  private void endMethodDeclaration() {
    for (Integer typeId : context.referencedTypesScope.pop()) {
      context.mibScope.peek().addReferencedTypes(typeId);
    }
    context.methodNameScope.pop();
    MemberInfo mi = context.mibScope.pop().build();
    context.currentTypeInfoScope.peek().addMember(mi);
  }

  static final class Context {
    // Map of type names to unique integer.
    private int typeCount;
    private final Map<String, Integer> typeMap = new HashMap<>();

    // Qualified method names that are exported (live).
    private final Set<String> exportedMethods;

    // Qualified class names that are exported (live).
    private final ImmutableSet<String> exportedClasses;

    // Library info builder, which contains all of the types processed.
    private final LibraryInfo.Builder libraryInfoBuilder = LibraryInfo.newBuilder();

    // Scope containing data for the current type being processed.
    private final Deque<String> currentTypeNameScope = new ArrayDeque<>();
    private final Deque<TypeInfo.Builder> currentTypeInfoScope = new ArrayDeque<>();

    // Scope containing data for the current method being processed.
    private final Deque<MemberInfo.Builder> mibScope = new ArrayDeque<>();
    private final Deque<String> methodNameScope = new ArrayDeque<>();
    private final Deque<Set<Integer>> referencedTypesScope = new ArrayDeque<>();

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
      return libraryInfoBuilder.build();
    }
  }
}
