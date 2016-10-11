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
import com.google.devtools.j2objc.ast.CommonTypeDeclaration;
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
  private Map<TypeElement, VariableElement> superOuterParams = new HashMap<>();
  private ListMultimap<TypeElement, Capture> captures = ArrayListMultimap.create();
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

  public VariableElement getSuperOuterParam(TypeElement type) {
    return superOuterParams.get(type);
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

  private Name getOuterPath(TypeElement type) {
    Name path = null;
    for (Scope scope = peekScope(); !type.equals(scope.type); scope = scope.outerClass) {
      path = Name.newName(path, getOrCreateOuterVar(scope));
    }
    return path;
  }

  private Name getOuterPathInherited(TypeElement type) {
    Name path = null;
    for (Scope scope = peekScope(); !scope.inheritedScope.contains(type);
         scope = scope.outerClass) {
      path = Name.newName(path, getOrCreateOuterVar(scope));
    }
    return path;
  }

  private Name getPathForField(VariableElement var) {
    Name path = getOuterPathInherited((TypeElement) var.getEnclosingElement());
    if (path != null) {
      path = Name.newName(path, var);
    }
    return path;
  }

  private Expression getPathForLocalVar(VariableElement var) {
    Name path = null;
    Scope scope = peekScope();
    if (scope.declaredVars.contains(var)) {
      // Var is declared in current scope, return empty path.
      return path;
    }
    if (var.getConstantValue() != null) {
      // Var has constant value, return a literal.
      return TreeUtil.newLiteral(var.getConstantValue(), typeEnv);
    }
    Scope lastScope = scope;
    while (!(scope = scope.outer).declaredVars.contains(var)) {
      // Except for the top scope, only include CLASS scopes when generating the path.
      if (scope == lastScope.outerClass) {
        path = Name.newName(path, getOrCreateOuterVar(lastScope));
        lastScope = scope;
      }
    }
    return Name.newName(path, getOrCreateInnerField(var, lastScope.type));
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
  private void addSuperOuterPath(CommonTypeDeclaration node) {
    TypeElement superclass = ElementUtil.getSuperclass(node.getTypeElement());
    if (superclass != null && needsOuterParam(superclass)) {
      node.setSuperOuter(getOuterPathInherited(ElementUtil.getDeclaringClass(superclass)));
    }
  }

  private void addCaptureArgs(TypeElement type, List<Expression> args) {
    for (Capture capture : captures.get(type)) {
      Expression path = getPathForLocalVar(capture.var);
      if (path == null) {
        path = new SimpleName(capture.var);
      }
      args.add(path);
    }
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
      addSuperOuterPath(node);
    }
    addCaptureArgs(ElementUtil.getSuperclass(node.getTypeElement()), node.getSuperCaptureArgs());
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
    Expression superOuter = parent instanceof ClassInstanceCreation
        ? TreeUtil.remove(((ClassInstanceCreation) parent).getExpression()) : null;
    if (superOuter != null) {
      // The parent creation node has an explicit outer reference that needs to be passed through to
      // the superclass constructor.
      TypeElement type = node.getTypeElement();
      ((ClassInstanceCreation) parent).setSuperOuterArg(superOuter);
      VariableElement param = new GeneratedVariableElement(
          "superOuter$", superOuter.getTypeMirror(), ElementKind.PARAMETER, type)
          .setNonnull(true);
      superOuterParams.put(type, param);
      node.setSuperOuter(new SimpleName(param));
    } else {
      addSuperOuterPath(node);
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
      node.setLambdaOuterArg(
          getOuterPathInherited(TypeUtil.asTypeElement(getOuterType(typeElement))));
    }
    addCaptureArgs(typeElement, node.getLambdaCaptureArgs());
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
      /*if (ElementUtil.isField(var) && !ElementUtil.isStatic(var)) {
        Name path = getPathForField(var);
        if (path != null) {
          node.replaceWith(path);
        }
      } else if (!ElementUtil.isField(var)) {
        Expression path = getPathForLocalVar(var);
        if (path != null) {
          node.replaceWith(path);
        }
      }*/
      Expression path = null;
      if (ElementUtil.isInstanceVar(var)) {
        path = getPathForField(var);
      } else if (!ElementUtil.isField(var)) {
        path = getPathForLocalVar(var);
      }
      if (path != null) {
        node.replaceWith(path);
      }
    }
    return true;
  }

  @Override
  public boolean visit(ThisExpression node) {
    Name qualifier = TreeUtil.remove(node.getQualifier());
    if (qualifier != null) {
      Name path = getOuterPath((TypeElement) qualifier.getElement());
      if (path != null) {
        node.replaceWith(path);
      }
    } else {
      Scope currentScope = peekScope();
      if (ElementUtil.isLambda(currentScope.type)) {
        Name path = getOuterPath(ElementUtil.getDeclaringClass(currentScope.type));
        assert path != null : "this keyword within a lambda should have a non-empty path";
        node.replaceWith(path);
      }
    }
    return true;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    ExecutableElement method = node.getExecutableElement();
    if (node.getExpression() == null && !ElementUtil.isStatic(method)) {
      node.setExpression(getOuterPathInherited(ElementUtil.getDeclaringClass(method)));
    }
  }

  private Name getSuperInvocationPath(Name qualifier) {
    if (qualifier != null) {
      return getOuterPath((TypeElement) qualifier.getElement());
    } else {
      Scope currentScope = peekScope();
      if (ElementUtil.isLambda(currentScope.type)) {
        return getOuterPath(ElementUtil.getDeclaringClass(currentScope.type));
      }
    }
    return null;
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
        node.setReceiver(getOuterPath(ElementUtil.getDeclaringClass(currentScope.type)));
      }
    } else {
      node.setReceiver(getSuperInvocationPath(node.getQualifier()));
    }
    node.setQualifier(null);
  }

  @Override
  public void endVisit(SuperMethodReference node) {
    TypeElement lambdaType = node.getTypeElement();
    pushType(lambdaType);
    node.setReceiver(getSuperInvocationPath(TreeUtil.remove(node.getQualifier())));
    popType();
    endVisitFunctionalExpression(node);
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    TypeElement typeElement = (TypeElement) node.getExecutableElement().getEnclosingElement();
    if (node.getExpression() == null) {
      whenNeedsOuterParam(typeElement, () -> {
        node.setExpression(
            getOuterPathInherited(TypeUtil.asTypeElement(getOuterType(typeElement))));
      });
    }
    if (ElementUtil.isLocal(typeElement)) {
      onExitScope(typeElement, () -> {
        addCaptureArgs(typeElement, node.getCaptureArgs());
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
      node.setCreationOuterArg(getOuterPathInherited(enclosingTypeElement));
    });
    if (ElementUtil.isLocal(creationElement)) {
      onExitScope(creationElement, () -> {
        addCaptureArgs(creationElement, node.getCreationCaptureArgs());
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
