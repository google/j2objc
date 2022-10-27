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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SimpleType;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnionType;
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
import java.util.stream.Stream;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

@SuppressWarnings("UngroupedOverloads")
final class UsedCodeMarker extends UnitTreeVisitor {
  static final String CLASS_INITIALIZER_NAME = "<clinit>##()V";
  static final String INITIALIZER_NAME = "<init>##()V";
  static final String EMPTY_METHOD_SIGNATURE = "()V";
  static final String INTERFACE_SUPERTYPE = "none";
  static final String PSEUDO_CONSTRUCTOR_PREFIX = "%%";
  static final String SIGNATURE_PREFIX = "##";
  private static final String USED_BY_NATIVE = "UsedByNative";

  private final Context context;

  UsedCodeMarker(CompilationUnit unit, Context context) {
    super(unit);
    this.context = context;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    startInterfaceType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    endType();
  }

  @Override
  public void endVisit(CastExpression node) {
    addReferencedType(node.getTypeMirror());
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
  public boolean visit(CreationReference node) {
    addMethodInvocation(node.getExecutableElement());
    return true;
  }

  @Override
  public void endVisit(EnhancedForStatement node) {
    // Add a reference to the enhanced for statement's parameter type.
    addReferencedType(node.getParameter().getType().getTypeMirror());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    pushClinit();
    addMethodInvocation(node.getExecutableElement());
    popClinit();
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    startEnumType(node.getTypeElement());
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
  public void endVisit(FieldAccess node) {
    // For fields, add a reference to the declaring class.
    VariableElement var = node.getVariableElement();
    if (var != null) {
      addReferencedTypeName(elementUtil.getBinaryName(ElementUtil.getDeclaringClass(var)));
    }
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    if (Modifier.isStatic(node.getModifiers())) {
      pushClinit();
    }
    return true;
  }

  @Override
  public void endVisit(FieldDeclaration node) {
    if (Modifier.isStatic(node.getModifiers())) {
      popClinit();
    }
  }

  @Override
  public boolean visit(Initializer node) {
    if (Modifier.isStatic(node.getModifiers())) {
      pushClinit();
    }
    return true;
  }

  @Override
  public void endVisit(Initializer node) {
    if (Modifier.isStatic(node.getModifiers())) {
      popClinit();
    }
  }

  @Override
  public void endVisit(InstanceofExpression node) {
    // For 'instanceof' a type, mark that type live.
    addReferencedType(node.getRightOperand().getTypeMirror());
  }

  @Override
  public void endVisit(LambdaExpression node) {
    // A lambda expression implicitly constructs an instance of the interface that it implements.
    addPseudoConstructorInvocation(node.getTypeMirror());
    // A lambda expression implicitly implements the functional interface method, add references to
    // parameters and return type.
    node.getParameters().forEach(vd -> addReferencedType(vd.getVariableElement().asType()));
    addReferencedType(node.getDescriptor().type().getReturnType());
  }

  @Override
  public void endVisit(MarkerAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    ExecutableElement executableElement = node.getExecutableElement();
    ExecutableElement originalMethod = elementUtil.getOriginalMethod(executableElement);
    startMethodDeclaration(
        getMethodName(executableElement),
        getDeclaringClassName(executableElement),
        getMethodName(originalMethod),
        getDeclaringClassName(originalMethod),
        node.isConstructor(),
        Modifier.isStatic(node.getModifiers()),
        node.getExecutableElement());
    addReferencedType(node.getReturnTypeMirror());
    node.getParameters().forEach(svd -> addReferencedType(svd.getType().getTypeMirror()));
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
  public void endVisit(QualifiedName node) {
    // Add the constant's containing type as a referenced type.
    addReferencedType(node.getQualifier().getTypeMirror());
  }

  @Override
  public void endVisit(SingleMemberAnnotation node) {
    visitAnnotation(node);
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    addMethodInvocation(node.getExecutableElement());
    addReferencedType(node.getExecutableType().getReturnType());
    node.getExecutableType().getParameterTypes().forEach(this::addReferencedType);
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    addMethodInvocation(node.getExecutableElement());
    addReferencedType(node.getExecutableType().getReturnType());
    node.getExecutableType().getParameterTypes().forEach(this::addReferencedType);
  }

  @Override
  public boolean visit(TryStatement node) {
    node.getCatchClauses().stream()
        .map(catchClause -> catchClause.getException().getType())
        .flatMap(
            type ->
                type instanceof UnionType
                    ? ((UnionType) type).getTypes().stream()
                    : Stream.of(type))
        .map(Type::getTypeMirror)
        .forEach(this::addReferencedType);
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    if (node.isInterface()) {
      startInterfaceType(node.getTypeElement());
    } else {
      startType(node.getTypeElement());
    }
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
  public boolean visit(SimpleName node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(var);
      addReferencedType(declaringClass.asType());
    }
    return true;
  }

  @Override
  public boolean visit(SimpleType node) {
    addReferencedType(node.getTypeMirror());
    return true;
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

  private static String getImplicitValuesName(String type) {
    return getMethodName("values", "()[" + type);
  }

  private static String getImplicitValueOfName(String type) {
    return getMethodName("valueOf", "(Ljava/lang/String;)" + type);
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
    String pkgName = pkg.getQualifiedName() + ".package-info";
    startTypeScope(pkgName, INTERFACE_SUPERTYPE, ImmutableList.of(), true);
  }

  private void endPackage() {
    endTypeScope();
  }

  private void startEnumType(TypeElement type) {
    startType(type);
    // For enums, add implict static methods.
    String typeName = elementUtil.getBinaryName(type);
    String sigName = typeUtil.getSignatureName(type.asType());
    startMethodDeclaration(getImplicitValuesName(sigName), typeName, false, true, type);
    endMethodDeclaration();
    startMethodDeclaration(getImplicitValueOfName(sigName), typeName, false, true, type);
    endMethodDeclaration();
  }

  private void startInterfaceType(TypeElement type) {
    startType(type);
    // For interfaces, add a pseudo-constructor for use with lambdas.
    String typeName = elementUtil.getBinaryName(type);
    startMethodDeclaration(getPseudoConstructorName(typeName), typeName, true, false, type);
    endMethodDeclaration();
  }

  private void startType(TypeElement type) {
    String typeName = elementUtil.getBinaryName(type);
    TypeElement superType = ElementUtil.getSuperclass(type);
    String superName =
        superType == null ? INTERFACE_SUPERTYPE : elementUtil.getBinaryName(superType);
    List<String> interfaces = ElementUtil.getInterfaces(type).stream()
                                .map(elementUtil::getBinaryName).collect(Collectors.toList());

    boolean exportedClassInnerType =
        ElementUtil.isPublic(type)
            && !context.currentTypeInfoScope.isEmpty()
            && context.currentTypeInfoScope.peek().getExported();

    boolean isExported =
        context.exportedClasses.contains(typeName)
            || ElementUtil.isRuntimeAnnotation(type)
            || ElementUtil.hasNamedAnnotation(type, USED_BY_NATIVE)
            || exportedClassInnerType;

    startTypeScope(typeName, superName, interfaces, isExported);
  }

  private void endType() {
    endTypeScope();
  }

  private static Annotations getAnnotations(AnnotatedConstruct annotatedConstruct) {
    boolean usedByNative = ElementUtil.hasNamedAnnotation(annotatedConstruct, USED_BY_NATIVE);
    return Annotations.newBuilder().setUsedByNative(usedByNative).build();
  }

  private void startTypeScope(String typeName, String superName,
      List<String> interfaces, boolean isExported) {
    Integer id = getTypeId(typeName);
    Integer eid = getTypeId(superName);
    List<Integer> iids = interfaces.stream().map(this::getTypeId).collect(Collectors.toList());
    context.currentTypeNameScope.push(typeName);
    context.currentTypeInfoScope.push(TypeInfo.newBuilder()
        .setTypeId(id).setExtendsType(eid).addAllImplementsType(iids).setExported(isExported));
    // Push the initializer as the current method in scope.
    startMethodScope(MemberInfo.newBuilder()
        .setName(INITIALIZER_NAME).setStatic(false).setExported(isExported));
    // Push the new static initializer in scope.
    context.clinitMemberScope.push(MemberInfo.newBuilder()
        .setName(CLASS_INITIALIZER_NAME).setStatic(true).setExported(isExported));
    context.clinitReferencedTypesScope.push(new HashSet<>());
  }

  private void endTypeScope() {
    // Close the current initializer method.
    endMethodDeclaration();
    // Close the current class initializer method.
    MemberInfo.Builder clinit = context.clinitMemberScope.pop();
    for (Integer typeId : context.clinitReferencedTypesScope.pop()) {
      clinit.addReferencedTypes(typeId);
    }
    context.currentTypeInfoScope.peek().addMember(clinit.build());
    TypeInfo ti = context.currentTypeInfoScope.pop().build();
    context.currentTypeNameScope.pop();
    // Add the type info to the library info.
    context.libraryInfoBuilder.addType(ti);
  }

  private void startMethodScope(MemberInfo.Builder member) {
    context.memberScope.push(member);
    context.referencedTypesScope.push(new HashSet<>());
  }

  private void startMethodDeclaration(
      String methodName,
      String declTypeName,
      boolean isConstructor,
      boolean isStatic,
      AnnotatedConstruct annotatedConstruct) {
    startMethodDeclaration(
        methodName,
        declTypeName,
        methodName,
        declTypeName,
        isConstructor,
        isStatic,
        annotatedConstruct);
  }

  private void startMethodDeclaration(
      String methodName,
      String declTypeName,
      String originalMethodName,
      String originalClassName,
      boolean isConstructor,
      boolean isStatic,
      AnnotatedConstruct annotatedConstruct) {
    boolean isExported =
        context.exportedMethods.contains(getQualifiedMethodName(declTypeName, methodName))
        || context.currentTypeInfoScope.peek().getExported();
    Integer originalTypeId = getTypeId(originalClassName);
    startMethodScope(
        MemberInfo.newBuilder()
            .setName(methodName)
            .setStatic(isStatic)
            .setOriginalType(originalTypeId)
            .setOriginalMethodName(originalMethodName)
            .setConstructor(isConstructor)
            .setExported(isExported)
            .setAnnotations(getAnnotations(annotatedConstruct)));
  }

  private void addPseudoConstructorInvocation(TypeMirror type) {
    String typeName = getTypeMirrorName(type);
    addMethodInvocation(getPseudoConstructorName(typeName), typeName);
  }

  private void addMethodInvocation(ExecutableElement element) {
    addMethodInvocation(getMethodName(element), getDeclaringClassName(element));
  }

  private void addMethodInvocation(String methodName, String declTypeName) {
    int declTypeId = getTypeId(declTypeName);
    context.memberScope.peek()
            .addInvokedMethods(com.google.devtools.treeshaker.MethodInvocation.newBuilder()
                .setMethod(methodName)
                .setEnclosingType(declTypeId)
                .build());
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
    MemberInfo.Builder member = context.memberScope.pop();
    for (Integer typeId : context.referencedTypesScope.pop()) {
      member.addReferencedTypes(typeId);
    }
    context.currentTypeInfoScope.peek().addMember(member.build());
  }

  private void pushClinit() {
    context.memberScope.push(context.clinitMemberScope.peek());
    context.referencedTypesScope.push(context.clinitReferencedTypesScope.peek());
  }

  private void popClinit() {
    context.memberScope.pop();
    context.referencedTypesScope.pop();
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
    private final Deque<MemberInfo.Builder> memberScope = new ArrayDeque<>();
    private final Deque<Set<Integer>> referencedTypesScope = new ArrayDeque<>();

    // Scope containing data for the current class initializer.
    private final Deque<MemberInfo.Builder> clinitMemberScope = new ArrayDeque<>();
    private final Deque<Set<Integer>> clinitReferencedTypesScope = new ArrayDeque<>();

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
