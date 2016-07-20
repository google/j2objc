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
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Visits a compilation unit and creates variable bindings for outer references
 * and captured local variables where required. Also generates an outer
 * reference path for any nodes where an outer reference is required. The
 * generated paths are lists of variable bindings for the outer fields that can
 * be used to reconstruct the given expression.
 *
 * OuterReferenceResolver should be run prior to any AST mutations.
 *
 * @author Keith Stanger
 */
public class OuterReferenceResolver extends TreeVisitor {

  // A placeholder variable binding that should be replaced with the outer
  // parameter in a constructor.
  public static final VariableElement OUTER_PARAMETER =
      BindingConverter.getVariableElement(GeneratedVariableBinding.newPlaceholder());

  private enum VisitingState { NEEDS_REVISIT, VISITED }

  private Map<ITypeBinding, VariableElement> outerVars = new HashMap<>();
  private Set<ITypeBinding> usesOuterParam = new HashSet<>();
  private ListMultimap<ITypeBinding, Capture> captures = ArrayListMultimap.create();
  private Map<TreeNode.Key, List<VariableElement>> outerPaths = new HashMap<>();
  private Map<TreeNode.Key, List<List<VariableElement>>> captureArgs = new HashMap<>();
  private ArrayList<Scope> scopeStack = new ArrayList<>();
  private Map<ITypeBinding, VisitingState> visitingStates = new HashMap<>();

  @Override
  public void run(TreeNode node) {
    assert scopeStack.isEmpty();
    super.run(node);
  }

  public boolean needsOuterReference(ITypeBinding type) {
    return outerVars.containsKey(type);
  }

  public boolean needsOuterParam(ITypeBinding type) {
    return !type.isLocal() || outerVars.containsKey(type) || usesOuterParam.contains(type);
  }

  public VariableElement getOuterField(ITypeBinding type) {
    return outerVars.get(type);
  }

  public List<IVariableBinding> getInnerFields(ITypeBinding type) {
    List<Capture> capturesForType = captures.get(type);
    List<IVariableBinding> innerFields = new ArrayList<>(capturesForType.size());
    for (Capture capture : capturesForType) {
      innerFields.add((IVariableBinding) BindingConverter.unwrapElement(capture.field));
    }
    return innerFields;
  }

  public List<VariableElement> getPath(TreeNode node) {
    return outerPaths.get(node.getKey());
  }

  public List<List<VariableElement>> getCaptureArgPaths(TreeNode node) {
    assert node instanceof ClassInstanceCreation || node instanceof LambdaExpression;
    List<List<VariableElement>> result = captureArgs.get(node.getKey());
    if (result != null) {
      return result;
    }
    return Collections.emptyList();
  }

  private static class Capture {

    private final VariableElement var;
    private final VariableElement field;

    private Capture(VariableElement var, VariableElement field) {
      this.var = var;
      this.field = field;
    }
  }

  private static class Scope {

    private final ITypeBinding type;
    private final Set<ITypeBinding> inheritedScope;
    private boolean initializingContext;
    private Set<VariableElement> declaredVars = new HashSet<>();

    private Scope(ITypeBinding type, Types typeEnv) {
      this.type = type;
      ImmutableSet.Builder<ITypeBinding> inheritedScopeBuilder = ImmutableSet.builder();
      for (ITypeBinding inheritedType : BindingUtil.getInheritedTypesInclusive(type)) {
        inheritedScopeBuilder.add(inheritedType.getTypeDeclaration());
      }

      // If type is an interface, type.getSuperClass() returns null even though all interfaces
      // "inherit" from Object. Therefore we add this manually to make the set complete. This is
      // needed because Java 8 default methods can call methods in Object.
      if (type.isInterface()) {
        inheritedScopeBuilder.add(typeEnv.getJavaObject());
      }

      this.inheritedScope = inheritedScopeBuilder.build();
      this.initializingContext = !BindingUtil.isLambda(type);
    }
  }

  private Scope peekScope() {
    assert scopeStack.size() > 0;
    return scopeStack.get(scopeStack.size() - 1);
  }

  // Marks the given type to be revisited. Returns true if the type has been maked for a revisit,
  // false if the type has already been visited.
  private boolean revisitScope(ITypeBinding type) {
    VisitingState state = visitingStates.get(type);
    if (state == null) {
      visitingStates.put(type, VisitingState.NEEDS_REVISIT);
      return true;
    }
    switch (state) {
      case NEEDS_REVISIT: return true;
      case VISITED: return false;
    }
    throw new AssertionError("Invalid state");
  }

  private String getOuterFieldName(ITypeBinding type) {
    // Ensure that the new outer field does not conflict with a field in a superclass.
    type = type.getSuperclass();
    int suffix = 0;
    while (type != null) {
      if (BindingUtil.hasOuterContext(type)) {
        suffix++;
      }
      type = type.getSuperclass();
    }
    return "this$" + suffix;
  }

  private VariableElement getOrCreateOuterField(Scope scope) {
    // Check that this isn't a lambda, since we'll always capture the field itself
    if (scope.initializingContext && scope == peekScope()) {
      usesOuterParam.add(scope.type);
      return OUTER_PARAMETER;
    }

    ITypeBinding type = scope.type;
    VariableElement outerField = outerVars.get(type);
    if (outerField == null) {
      outerField = (VariableElement) BindingConverter.getElement(
          new GeneratedVariableBinding(getOuterFieldName(type),
          Modifier.PRIVATE | Modifier.FINAL, type.getDeclaringClass(), true, false, type, null)
          );
      outerVars.put(type, outerField);
    }
    return outerField;
  }

  private VariableElement getOrCreateInnerField(VariableElement var, ITypeBinding declaringType) {
    List<Capture> capturesForType = captures.get(declaringType);
    VariableElement innerField = null;
    for (Capture capture : capturesForType) {
      if (var.equals(capture.var)) {
        innerField = capture.field;
        break;
      }
    }
    if (innerField == null) {
      GeneratedVariableBinding newField = new GeneratedVariableBinding("val$"
          + var.getSimpleName().toString(), Modifier.PRIVATE | Modifier.FINAL,
          var.asType(), true, false, declaringType, null);
      newField.addAnnotations(var);
      innerField = BindingConverter.getVariableElement(newField);
      captures.put(declaringType, new Capture(var, innerField));
    }
    return innerField;
  }

  private List<VariableElement> getOuterPath(ITypeBinding type) {
    type = type.getTypeDeclaration();
    List<VariableElement> path = new ArrayList<>();
    for (int i = scopeStack.size() - 1; i >= 0; i--) {
      Scope scope = scopeStack.get(i);
      if (type.equals(scope.type)) {
        break;
      }
      if (!BindingUtil.isLambda(scope.type)) {
        path.add(getOrCreateOuterField(scope));
      } else {
        VariableElement outerField = getOrCreateOuterField(scope);
        // A lambda should get added to the outer path only if it is the scope closest to the use.
        if (i == scopeStack.size() - 1) {
          path.add(outerField);
        }
      }
    }
    return path;
  }

  private List<VariableElement> getOuterPathInherited(ITypeBinding type) {
    type = type.getTypeDeclaration();
    List<VariableElement> path = new ArrayList<>();
    for (int i = scopeStack.size() - 1; i >= 0; i--) {
      Scope scope = scopeStack.get(i);
      if (scope.inheritedScope.contains(type)) {
        break;
      }
      if (!BindingUtil.isLambda(scope.type)) {
        path.add(getOrCreateOuterField(scope));
      } else {
        VariableElement outerField = getOrCreateOuterField(scope);
        // A lambda should get added to the outer path only if it is innermost
        if (i == scopeStack.size() - 1) {
          path.add(outerField);
        }
      }
    }
    return path;
  }

  private List<VariableElement> getPathForField(VariableElement var) {
    List<VariableElement> path = getOuterPathInherited(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(var.getEnclosingElement().asType()));
    if (!path.isEmpty()) {
      path.add(var);
    }
    return path;
  }

  private List<VariableElement> getPathForLocalVar(VariableElement var) {
    boolean isConstant = var.getConstantValue() != null;
    ArrayList<VariableElement> path = new ArrayList<>();

    int lastScopeIdx = -1;
    int lastNonLambdaScopeIdx = -1;
    int scopeStackSize = scopeStack.size();

    for (int i = scopeStackSize - 1; i >= 0; i--) {
      Scope scope = scopeStack.get(i);
      if (scope.declaredVars.contains(var)) {
        break;
      }

      lastScopeIdx = i;
      if (!BindingUtil.isLambda(scope.type)) {
        lastNonLambdaScopeIdx = i;
      }
    }

    if (lastScopeIdx == -1) {
      // Var must already be in the declaring scope. Return an empty path.
      return path;
    }

    // Constant reference only needs a one-element path.
    if (isConstant) {
      path.add(var);
      return path;
    }

    // Traverse from the top of the stack, if we hit a lambda scope with a non-lambda scope
    // enclosing it, then we add an outer field to the lambda and add it to the path only if there
    // is not already an outer field in the path (since lambdas will transparently close over outer
    // paths if they already exist). Otherwise, we transparently close over the field. If
    // the scope is the last anonymous or inner class, get/create an inner field and add
    // that to the path. Otherwise, this class is not the last scope so we get/create an outer field
    // reference and add that to the path.
    for (int i = scopeStackSize - 1; i >= lastScopeIdx; i--) {
      Scope scope = scopeStack.get(i);
      if (i == lastNonLambdaScopeIdx) {
        path.add(getOrCreateInnerField(var, scope.type));
      } else {
        if (BindingUtil.isLambda(scope.type)) {
          if (i > lastNonLambdaScopeIdx && lastNonLambdaScopeIdx != -1) {
            VariableElement outer = getOrCreateOuterField(scope);
            if (i == scopeStackSize - 1) {
              path.add(outer);
            }
          } else {
            getOrCreateInnerField(var, scope.type);
          }
        } else {
          path.add(getOrCreateOuterField(scope));
        }
      }
    }
    return path;
  }

  private void addPath(TreeNode node, List<VariableElement> path) {
    if (!path.isEmpty()) {
      outerPaths.put(node.getKey(), path);
    }
  }

  private void pushType(TreeNode node, ITypeBinding type) {
    scopeStack.add(new Scope(type, typeEnv));

    ITypeBinding superclass = type.getSuperclass();
    if (superclass != null && BindingUtil.hasOuterContext(superclass)) {
      addPath(node, getOuterPathInherited(superclass.getDeclaringClass()));
    }
  }

  private void popType(TreeNode node) {
    Scope currentScope = scopeStack.remove(scopeStack.size() - 1);
    VisitingState state = visitingStates.get(currentScope.type);
    boolean revisit = state == VisitingState.NEEDS_REVISIT;
    visitingStates.put(currentScope.type, VisitingState.VISITED);
    if (revisit) {
      node.accept(this);
    }
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    pushType(node, node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    popType(node);
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    pushType(node, node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    popType(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    pushType(node, node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    popType(node);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    pushType(node, node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    popType(node);
  }

  @Override
  public boolean visit(LambdaExpression node) {
    pushType(node, node.getLambdaTypeBinding());
    return true;
  }

  @Override
  public void endVisit(LambdaExpression node) {
    popType(node);

    // if we have an outer field, add the path to it so we can reference
    // it in the lambda_get function.
    VariableElement outerField = getOuterField(node.getLambdaTypeBinding());
    if (outerField != null) {
      addPath(node, getOuterPathInherited(
          BindingConverter.unwrapTypeMirrorIntoTypeBinding(outerField.asType())));
    }
    ITypeBinding type = node.getLambdaTypeBinding();
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
      addPath(node, getOuterPath(qualifier.getTypeBinding()));
    } else {
      assert scopeStack.size() > 0;
      Scope currentScope = scopeStack.get(scopeStack.size() - 1);
      if (BindingUtil.isLambda(currentScope.type)) {
          addPath(node, getOuterPath(currentScope.type.getDeclaringClass()));
      }
    }
    return true;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding method = node.getMethodBinding();
    if (node.getExpression() == null && !Modifier.isStatic(method.getModifiers())) {
      addPath(node, getOuterPathInherited(method.getDeclaringClass()));
    }
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    if (BindingUtil.isDefault(node.getMethodBinding())) {
      // Default methods can be invoked with a SuperMethodInvocation. In this
      // case the qualifier is not an enclosing class, but the interface that
      // implements the default method. Since the default method is an instance
      // method it captures self.
      Scope currentScope = scopeStack.get(scopeStack.size() - 1);
      if (BindingUtil.isLambda(currentScope.type)) {
        addPath(node, getOuterPath(currentScope.type.getDeclaringClass()));
      }
      return;
    }
    Name qualifier = node.getQualifier();
    if (qualifier != null) {
      addPath(node, getOuterPath(qualifier.getTypeBinding()));
    } else {
      Scope currentScope = scopeStack.get(scopeStack.size() - 1);
      if (BindingUtil.isLambda(currentScope.type)) {
        addPath(node, getOuterPath(currentScope.type.getDeclaringClass()));
      }
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    ITypeBinding type = node.getTypeBinding().getTypeDeclaration();
    if (node.getExpression() == null && BindingUtil.hasOuterContext(type)) {
      addPath(node, getOuterPathInherited(type.getDeclaringClass()));
    }
    if (type.isLocal() && !revisitScope(type)) {
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
  }

  private boolean visitVariableDeclaration(VariableDeclaration node) {
    assert scopeStack.size() > 0;
    Scope currentScope = scopeStack.get(scopeStack.size() - 1);
    currentScope.declaredVars.add(node.getVariableElement());
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
    IMethodBinding binding = node.getMethodBinding();
    // Assume all code except for non-constructor methods is initializer code.
    if (!binding.isConstructor()) {
      peekScope().initializingContext = false;
    }
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    IMethodBinding binding = node.getMethodBinding();
    if (!binding.isConstructor()) {
      peekScope().initializingContext = true;
    }
  }
}
