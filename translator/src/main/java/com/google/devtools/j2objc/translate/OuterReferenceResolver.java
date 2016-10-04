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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionalExpression;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Visits a compilation unit and creates variable elements for outer references
 * and captured local variables where required. Also generates an outer
 * reference path for any nodes where an outer reference is required. The
 * generated paths are lists of variable elements for the outer fields that can
 * be used to reconstruct the given expression.
 *
 * OuterReferenceResolver should be run prior to any AST mutations.
 *
 * @author Keith Stanger
 */
public class OuterReferenceResolver extends TreeVisitor {

  private Map<TypeElement, VariableElement> outerVars = new HashMap<>();
  private Map<TypeElement, VariableElement> outerParams = new HashMap<>();
  private ListMultimap<TypeElement, Capture> captures = ArrayListMultimap.create();
  private Map<TreeNode.Key, List<VariableElement>> outerPaths = new HashMap<>();
  private Map<TreeNode.Key, List<List<VariableElement>>> captureArgs = new HashMap<>();
  private Scope topScope = null;

  @Override
  public void run(TreeNode node) {
    assert topScope == null;
    super.run(node);
  }

  public boolean needsOuterReference(TypeElement type) {
    return outerVars.containsKey(type);
  }

  public boolean needsOuterParam(TypeElement type) {
    return outerParams.containsKey(type) || automaticOuterParam(type);
  }

  public VariableElement getOuterParam(TypeElement type) {
    return outerParams.get(type);
  }

  public TypeMirror getOuterType(TypeElement type) {
    VariableElement outerField = outerVars.get(type);
    if (outerField != null) {
      return outerField.asType();
    }
    return getDeclaringType(type);
  }

  private static TypeMirror getDeclaringType(TypeElement type) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(type);
    assert declaringClass != null : "Cannot find declaring class for " + type;
    return declaringClass.asType();
  }

  public VariableElement getOuterField(TypeElement type) {
    return outerVars.get(type);
  }

  public List<VariableElement> getInnerFields(TypeElement type) {
    List<Capture> capturesForType = captures.get(type);
    List<VariableElement> innerFields = new ArrayList<>(capturesForType.size());
    for (Capture capture : capturesForType) {
      innerFields.add(capture.field);
    }
    return innerFields;
  }

  public List<VariableElement> getPath(TreeNode node) {
    return outerPaths.get(node.getKey());
  }

  public List<List<VariableElement>> getCaptureArgPaths(TreeNode node) {
    List<List<VariableElement>> result = captureArgs.get(node.getKey());
    if (result != null) {
      return result;
    }
    return Collections.emptyList();
  }

  private static boolean automaticOuterParam(TypeElement type) {
    return ElementUtil.hasOuterContext(type) && !ElementUtil.isLocal(type);
  }

  private static class Capture {

    private final VariableElement var;
    private final VariableElement field;

    private Capture(VariableElement var, VariableElement field) {
      this.var = var;
      this.field = field;
    }
  }

  private enum ScopeKind { CLASS, LAMBDA, METHOD }

  /**
   * Encapsulates relevant information about the types being visited. Scope instances are linked to
   * form a stack of enclosing types and methods.
   */
  private class Scope {

    private final ScopeKind kind;
    private final Scope outer;
    private final Scope outerClass;  // Direct pointer to the next CLASS scope.
    private final TypeElement type;
    private final Set<Element> inheritedScope;
    private final boolean initializingContext;
    private final Set<VariableElement> declaredVars = new HashSet<>();
    // These callbacks are used for correct resolution of local classes where the captures are not
    // always known at the point of creation.
    private List<Runnable> onExit = new ArrayList<>();
    private List<Runnable> onOuterParam = new ArrayList<>();
    // The following fields are used only by CLASS scope kinds.
    private int constructorCount = 0;
    private int constructorsNotNeedingSuperOuterScope = 0;

    private Scope(Scope outer, TypeElement type) {
      kind = ElementUtil.isLambda(type) ? ScopeKind.LAMBDA : ScopeKind.CLASS;
      this.outer = outer;
      outerClass = firstClassScope(outer);
      this.type = type;
      ImmutableSet.Builder<Element> inheritedScopeBuilder = ImmutableSet.builder();

      // Lambdas are ignored when resolving implicit outer scope.
      if (kind == ScopeKind.CLASS) {
        for (DeclaredType inheritedType :
          ElementUtil.getInheritedDeclaredTypesInclusive(type.asType(), env)) {
          inheritedScopeBuilder.add(inheritedType.asElement());
        }
      }

      // If type is an interface, type.getSuperClass() returns null even though all interfaces
      // "inherit" from Object. Therefore we add this manually to make the set complete. This is
      // needed because Java 8 default methods can call methods in Object.
      if (ElementUtil.isInterface(type)) {
        inheritedScopeBuilder.add(env.types().getJavaObjectElement());
      }

      this.inheritedScope = inheritedScopeBuilder.build();
      this.initializingContext = kind == ScopeKind.CLASS;
    }

    /**
     * Creates a Scope for a method declaration. This scope will contain mostly the same state as
     * its enclosing CLASS scope, but may have a different value for "initializingContext".
     */
    private Scope(Scope outer, ExecutableElement method) {
      kind = ScopeKind.METHOD;
      this.outer = outer;
      // Skip over the immediately enclosing class, since this scope has the same type information.
      outerClass = outer.outerClass;
      type = outer.type;
      inheritedScope = outer.inheritedScope;
      initializingContext = ElementUtil.isConstructor(method);
    }
  }

  private Scope peekScope() {
    assert topScope != null;
    return topScope;
  }

  private static Scope firstClassScope(Scope scope) {
    while (scope != null && scope.kind != ScopeKind.CLASS) {
      scope = scope.outer;
    }
    return scope;
  }

  // Finds the non-method scope for the given type.
  private Scope findScopeForType(TypeElement type) {
    Scope scope = peekScope();
    while (scope != null) {
      if (scope.kind != ScopeKind.METHOD && type.equals(scope.type)) {
        return scope;
      }
      scope = scope.outer;
    }
    return null;
  }

  private Runnable captureCurrentScope(Runnable runnable) {
    Scope capturedScope = peekScope();
    return new Runnable() {
      @Override
      public void run() {
        Scope saved = topScope;
        topScope = capturedScope;
        runnable.run();
        topScope = saved;
      }
    };
  }

  private void onExitScope(TypeElement type, Runnable runnable) {
    Scope scope = findScopeForType(type);
    if (scope != null) {
      scope.onExit.add(captureCurrentScope(runnable));
    } else {
      // The given type is not currently in scope, so execute the runnable now.
      runnable.run();
    }
  }

  // Executes the runnable if or when the given type needs an outer param.
  private void whenNeedsOuterParam(TypeElement type, Runnable runnable) {
    if (needsOuterParam(type)) {
      runnable.run();
    } else if (ElementUtil.isLocal(type)) {
      Scope scope = findScopeForType(type);
      if (scope != null) {
        scope.onOuterParam.add(captureCurrentScope(runnable));
      }
    }
  }

  private String getOuterFieldName(TypeElement type) {
    // Ensure that the new outer field does not conflict with a field in a superclass.
    TypeElement typeElement = ElementUtil.getSuperclass(type);
    int suffix = 0;
    while (typeElement != null) {
      if (ElementUtil.hasOuterContext(typeElement)) {
        suffix++;
      }
      typeElement = ElementUtil.getSuperclass(typeElement);
    }
    return "this$" + suffix;
  }

  private String getCaptureFieldName(VariableElement var, TypeElement type) {
    int suffix = 0;
    while ((type = ElementUtil.getSuperclass(type)) != null && ElementUtil.isLocal(type)) {
      suffix++;
    }
    return "val" + (suffix > 0 ? suffix : "") + "$" + var.getSimpleName().toString();
  }

  private VariableElement getOrCreateOuterParam(TypeElement type) {
    VariableElement outerParam = outerParams.get(type);
    if (outerParam == null) {
      outerParam = new GeneratedVariableElement(
          "outer$", getDeclaringType(type), ElementKind.PARAMETER, type)
          .setNonnull(true);
      outerParams.put(type, outerParam);
      Scope scope = findScopeForType(type);
      for (Runnable runnable : scope.onOuterParam) {
        runnable.run();
      }
    }
    return outerParam;
  }

  private VariableElement getOrCreateOuterField(TypeElement type) {
    VariableElement outerField = outerVars.get(type);
    if (outerField == null) {
      outerField = new GeneratedVariableElement(
          getOuterFieldName(type), getDeclaringType(type), ElementKind.FIELD, type)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .setNonnull(true);
      outerVars.put(type, outerField);
    }
    return outerField;
  }

  private VariableElement getOrCreateOuterVar(Scope scope) {
    // Always create the outer param since it is required to initialize the field.
    VariableElement outerParam = getOrCreateOuterParam(scope.type);
    if (scope.initializingContext && scope == peekScope()) {
      return outerParam;
    }
    return getOrCreateOuterField(scope.type);
  }

  private VariableElement getOrCreateInnerField(VariableElement var, TypeElement declaringType) {
    List<Capture> capturesForType = captures.get(declaringType);
    VariableElement innerField = null;
    for (Capture capture : capturesForType) {
      if (var.equals(capture.var)) {
        innerField = capture.field;
        break;
      }
    }
    if (innerField == null) {
      innerField = new GeneratedVariableElement(
          getCaptureFieldName(var, declaringType), var.asType(), ElementKind.FIELD, declaringType)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .addAnnotationMirrors(var.getAnnotationMirrors());
      captures.put(declaringType, new Capture(var, innerField));
    }
    return innerField;
  }

  private List<VariableElement> getOuterPath(TypeElement type) {
    List<VariableElement> path = new ArrayList<>();
    for (Scope scope = peekScope(); !type.equals(scope.type); scope = scope.outerClass) {
      path.add(getOrCreateOuterVar(scope));
    }
    return path;
  }

  private List<VariableElement> getOuterPathInherited(TypeElement type) {
    List<VariableElement> path = new ArrayList<>();
    for (Scope scope = peekScope(); !scope.inheritedScope.contains(type);
         scope = scope.outerClass) {
      path.add(getOrCreateOuterVar(scope));
    }
    return path;
  }

  private List<VariableElement> getPathForField(VariableElement var) {
    List<VariableElement> path = getOuterPathInherited((TypeElement) var.getEnclosingElement());
    if (!path.isEmpty()) {
      path.add(var);
    }
    return path;
  }

  private List<VariableElement> getPathForLocalVar(VariableElement var) {
    ArrayList<VariableElement> path = new ArrayList<>();
    Scope scope = peekScope();
    if (scope.declaredVars.contains(var)) {
      // Var is declared in current scope, return empty path.
      return path;
    }
    if (var.getConstantValue() != null) {
      // Var has constant value, return path directly to var.
      path.add(var);
      return path;
    }
    Scope lastScope = scope;
    while (!(scope = scope.outer).declaredVars.contains(var)) {
      // Except for the top scope, only include CLASS scopes when generating the path.
      if (scope == lastScope.outerClass) {
        path.add(getOrCreateOuterVar(lastScope));
        lastScope = scope;
      }
    }
    path.add(getOrCreateInnerField(var, lastScope.type));
    return path;
  }

  private void addPath(TreeNode node, List<VariableElement> path) {
    if (!path.isEmpty()) {
      outerPaths.put(node.getKey(), path);
    }
  }

  private void pushType(TypeElement type) {
    topScope = new Scope(topScope, type);
    if (automaticOuterParam(type)) {
      getOrCreateOuterParam(type);
    }
  }

  private void popType() {
    Scope currentScope = peekScope();
    topScope = currentScope.outer;
    for (Runnable runnable : currentScope.onExit) {
      runnable.run();
    }
  }

  // Resolve the path for the outer scope to a SuperConstructorInvocation. This path goes on the
  // type node because there may be implicit super invocations.
  private void addSuperOuterPath(TreeNode node, TypeElement type) {
    TypeElement superclass = ElementUtil.getSuperclass(type);
    if (superclass != null && needsOuterParam(superclass)) {
      addPath(node, getOuterPathInherited(ElementUtil.getDeclaringClass(superclass)));
    }
  }

  private void resolveCaptureArgs(TreeNode node, TypeElement type) {
    List<Capture> capturesForType = captures.get(type);
    List<List<VariableElement>> capturePaths = new ArrayList<>(capturesForType.size());
    for (Capture capture : capturesForType) {
      List<VariableElement> path = getPathForLocalVar(capture.var);
      if (path.isEmpty()) {
        path = Collections.singletonList(capture.var);
      }
      capturePaths.add(path);
    }
    captureArgs.put(node.getKey(), capturePaths);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    Scope currentScope = peekScope();
    if (currentScope.constructorCount == 0) {
      // Implicit default constructor.
      currentScope.constructorCount++;
    }
    if (currentScope.constructorCount > currentScope.constructorsNotNeedingSuperOuterScope) {
      addSuperOuterPath(node, node.getTypeElement());
    }
    resolveCaptureArgs(node, ElementUtil.getSuperclass(node.getTypeElement()));
    popType();
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    TreeNode parent = node.getParent();
    if (!(parent instanceof ClassInstanceCreation)
        || ((ClassInstanceCreation) parent).getExpression() == null) {
      addSuperOuterPath(node, node.getTypeElement());
    }
    popType();
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    popType();
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    popType();
  }

  private void endVisitFunctionalExpression(FunctionalExpression node) {
    // Resolve outer and capture arguments.
    TypeElement typeElement = node.getTypeElement();
    if (needsOuterParam(typeElement)) {
      addPath(node, getOuterPathInherited(TypeUtil.asTypeElement(getOuterType(typeElement))));
    }
    resolveCaptureArgs(node, typeElement);
  }

  @Override
  public boolean visit(LambdaExpression node) {
    pushType(node.getTypeElement());
    return true;
  }

  @Override
  public void endVisit(LambdaExpression node) {
    popType();
    endVisitFunctionalExpression(node);
  }

  @Override
  public void endVisit(ExpressionMethodReference node) {
    Expression target = node.getExpression();
    if (!ElementUtil.isStatic(node.getExecutableElement()) && isValue(target)) {
      TypeElement type = node.getTypeElement();
      TypeMirror targetType = target.getTypeMirror();
      // Add the target field as an outer field even though it's not really pointing to outer scope.
      outerVars.put(type, new GeneratedVariableElement(
          "target$", targetType, ElementKind.FIELD, type)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .setNonnull(true));
      outerParams.put(type, new GeneratedVariableElement(
          "outer$", targetType, ElementKind.PARAMETER, type)
          .setNonnull(true));
    }
  }

  private static boolean isValue(Expression expr) {
    return !(expr instanceof Name) || !ElementUtil.isType(((Name) expr).getElement());
  }

  @Override
  public boolean visit(FieldAccess node) {
    node.getExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    node.getQualifier().accept(this);
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null) {
      if (ElementUtil.isField(var) && !ElementUtil.isStatic(var)) {
        addPath(node, getPathForField(var));
      } else if (!ElementUtil.isField(var)) {
        addPath(node, getPathForLocalVar(var));
      }
    }
    return true;
  }

  @Override
  public boolean visit(ThisExpression node) {
    Name qualifier = node.getQualifier();
    if (qualifier != null) {
      addPath(node, getOuterPath((TypeElement) qualifier.getElement()));
    } else {
      Scope currentScope = peekScope();
      if (ElementUtil.isLambda(currentScope.type)) {
          addPath(node, getOuterPath(ElementUtil.getDeclaringClass(currentScope.type)));
      }
    }
    return true;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    ExecutableElement method = node.getExecutableElement();
    if (node.getExpression() == null && !ElementUtil.isStatic(method)) {
      addPath(node, getOuterPathInherited((TypeElement) method.getEnclosingElement()));
    }
  }

  private void addSuperInvocationPath(TreeNode node, Name qualifier) {
    if (qualifier != null) {
      addPath(node, getOuterPath((TypeElement) qualifier.getElement()));
    } else {
      Scope currentScope = peekScope();
      if (ElementUtil.isLambda(currentScope.type)) {
        addPath(node, getOuterPath(ElementUtil.getDeclaringClass(currentScope.type)));
      }
    }
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    if (ElementUtil.isDefault(node.getExecutableElement())) {
      // Default methods can be invoked with a SuperMethodInvocation. In this
      // case the qualifier is not an enclosing class, but the interface that
      // implements the default method. Since the default method is an instance
      // method it captures self.
      Scope currentScope = peekScope();
      if (ElementUtil.isLambda(currentScope.type)) {
        addPath(node, getOuterPath(ElementUtil.getDeclaringClass(currentScope.type)));
      }
      return;
    }
    addSuperInvocationPath(node, node.getQualifier());
  }

  @Override
  public void endVisit(SuperMethodReference node) {
    TypeElement lambdaType = node.getTypeElement();
    pushType(lambdaType);
    addSuperInvocationPath(node.getName(), node.getQualifier());
    popType();
    endVisitFunctionalExpression(node);
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    TypeElement typeElement = (TypeElement) node.getExecutableElement().getEnclosingElement();
    if (node.getExpression() == null) {
      whenNeedsOuterParam(typeElement, () -> {
        addPath(node, getOuterPathInherited(TypeUtil.asTypeElement(getOuterType(typeElement))));
      });
    }
    if (ElementUtil.isLocal(typeElement)) {
      onExitScope(typeElement, () -> {
        resolveCaptureArgs(node, typeElement);
      });
    }
  }

  @Override
  public void endVisit(CreationReference node) {
    Type typeNode = node.getType();
    TypeMirror creationType = typeNode.getTypeMirror();
    if (TypeUtil.isArray(creationType)) {
      // Nothing to capture for array creations.
      return;
    }

    TypeElement lambdaType = node.getTypeElement();
    pushType(lambdaType);
    // This is kind of messy, but we use the Type child node as the key for capture scope to be
    // transferred to the inner ClassInstanceCreation. The capture scope of the CreationReference
    // node will be transferred to the ClassInstanceCreation that creates the lambda instance.
    TypeElement creationElement = TypeUtil.asTypeElement(creationType);
    whenNeedsOuterParam(creationElement, () -> {
      TypeElement enclosingTypeElement = ElementUtil.getDeclaringClass(creationElement);
      addPath(typeNode, getOuterPathInherited(enclosingTypeElement));
    });
    if (ElementUtil.isLocal(creationElement)) {
      onExitScope(creationElement, () -> {
        resolveCaptureArgs(typeNode, creationElement);
      });
    }
    popType();

    endVisitFunctionalExpression(node);
  }

  private boolean visitVariableDeclaration(VariableDeclaration node) {
    peekScope().declaredVars.add(node.getVariableElement());
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    return visitVariableDeclaration(node);
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    return visitVariableDeclaration(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    Scope currentScope = peekScope();
    ExecutableElement elem = node.getExecutableElement();
    if (ElementUtil.isConstructor(elem)) {
      currentScope.constructorCount++;
    }
    topScope = new Scope(currentScope, elem);
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    topScope = topScope.outer;
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    firstClassScope(peekScope()).constructorsNotNeedingSuperOuterScope++;
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    if (node.getExpression() != null) {
      firstClassScope(peekScope()).constructorsNotNeedingSuperOuterScope++;
    }
  }
}
