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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Reference-mapping code for TreeShaker functionality that uses the visitor pattern
 * to identify all elements in the source code
 *
 * @author Priyank Malvania
 */
public class ElementReferenceMapper extends UnitTreeVisitor {

  abstract static class ReferenceNode {
    boolean used = false;

    public abstract String getUniqueID();
    public abstract void addToBuilder(Builder builder);
  }

  class ClassReferenceNode extends ReferenceNode {
    final TypeElement classElement;

    public ClassReferenceNode(TypeElement classElement) {
      this.classElement = classElement;
    }

    @Override
    public String getUniqueID() {
      return stitchClassIdentifier(classElement);
    }

    @Override
    public void addToBuilder(Builder builder) {
      builder.addClass(elementUtil.getBinaryName(classElement));
    }
  }

  class FieldReferenceNode extends ReferenceNode {
    final VariableDeclarationFragment fieldFragment;

    public FieldReferenceNode(VariableDeclarationFragment fieldFragment) {
      this.fieldFragment = fieldFragment;
    }

    @Override
    public String getUniqueID() {
      return stitchFieldIdentifier(fieldFragment);
    }

    @Override
    public void addToBuilder(Builder builder) {
      //TODO(user): Enable the following code when I finish coding the FieldAccess use-marking
      //String className = elementUtil.getBinaryName(
      //    ElementUtil.getDeclaringClass(fieldFragment.getVariableElement()));
      //String fragmentIdentifier = fieldFragment.getName().getIdentifier();
      //builder.addDeadField(className, fragmentIdentifier);
    }
  }

  class MethodReferenceNode extends ReferenceNode {
    final ExecutableElement methodElement;
    boolean visited = false;
    Set<String> invokedMethods;

    public MethodReferenceNode(ExecutableElement methodElement) {
      this.methodElement = methodElement;
      this.invokedMethods = new HashSet<String>();
    }

    @Override
    public String getUniqueID() {
      return stitchMethodIdentifier(methodElement);
    }

    @Override
    public void addToBuilder(Builder builder) {
      String className = elementUtil.getBinaryName(ElementUtil.getDeclaringClass(methodElement));
      String methodName = typeUtil.getReferenceName(methodElement);
      String methodSignature = typeUtil.getReferenceSignature(methodElement);
      builder.addMethod(className, methodName, methodSignature);
    }
  }

  private HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();
  private Set<String> staticSet = new HashSet<>();

  public ElementReferenceMapper(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    visitType(node);
  }

  private void visitType(AbstractTypeDeclaration node) {
    TypeElement type = node.getTypeElement();
    elementReferenceMap.putIfAbsent(stitchClassIdentifier(type),
        new ClassReferenceNode(type));
  }

  //TODO(user): Add the field type class to reference classes.
  //Currently, jdt only supports well known types. Soon, we can get type mirror from field
  //and resolve the type by its name using a resolve method in the parser environment.
  @Override
  public void endVisit(VariableDeclarationFragment fragment) {
    elementReferenceMap.putIfAbsent(stitchFieldIdentifier(fragment),
        new FieldReferenceNode(fragment));
  }

  /**
   * When a constructor in invoked (including a default constructor), adds the constructor and
   * invoking method to elementReferenceMap. The class will eventually be marked as used.
   * Counts as both the declaration (in class) and invocation (new _()) of the constructor.
   */
  @Override
  public void endVisit(ClassInstanceCreation instance) {
    ExecutableElement methodElement = instance.getExecutableElement();
    String methodIdentifier = stitchMethodIdentifier(methodElement);
    elementReferenceMap.putIfAbsent(methodIdentifier, new MethodReferenceNode(methodElement));
    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(instance);

    if (parentMethodDeclaration == null) {
      staticSet.add(methodIdentifier);
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    MethodReferenceNode parentMethodNode = (MethodReferenceNode) elementReferenceMap
        .get(stitchMethodIdentifier(parentMethodElement));

    if (parentMethodNode == null) {
      parentMethodNode = new MethodReferenceNode(parentMethodElement);
    }
    parentMethodNode.invokedMethods.add(stitchMethodIdentifier(methodElement));
    elementReferenceMap.put(stitchMethodIdentifier(parentMethodElement), parentMethodNode);
  }

  @Override
  public void endVisit(ConstructorInvocation invocation) {
    ExecutableElement methodElement = invocation.getExecutableElement();
    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(invocation);

    if (parentMethodDeclaration == null) {
      String methodIdentifier = stitchMethodIdentifier(methodElement);
      staticSet.add(methodIdentifier);
      elementReferenceMap.putIfAbsent(methodIdentifier, new MethodReferenceNode(methodElement));
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    MethodReferenceNode parentMethodNode = (MethodReferenceNode) elementReferenceMap
        .get(stitchMethodIdentifier(parentMethodElement));

    if (parentMethodNode == null) {
      parentMethodNode = new MethodReferenceNode(parentMethodElement);
    }
    parentMethodNode.invokedMethods.add(stitchMethodIdentifier(methodElement));
    elementReferenceMap.put(stitchMethodIdentifier(parentMethodElement), parentMethodNode);
  }

  @Override
  public void endVisit(MethodDeclaration method) {
    if (Modifier.isNative(method.getModifiers())) {
      return;
    }
    ExecutableElement methodElement = method.getExecutableElement();
    String methodIdentifier = stitchMethodIdentifier(methodElement);
    elementReferenceMap.putIfAbsent(methodIdentifier, new MethodReferenceNode(methodElement));
  }

  @Override
  public void endVisit(MethodInvocation method) {
    ExecutableElement methodElement = method.getExecutableElement();
    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(method);

    if (parentMethodDeclaration == null) {
      String methodIdentifier = stitchMethodIdentifier(methodElement);
      staticSet.add(methodIdentifier);
      elementReferenceMap.putIfAbsent(methodIdentifier, new MethodReferenceNode(methodElement));
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    MethodReferenceNode parentMethodNode = (MethodReferenceNode) elementReferenceMap
        .get(stitchMethodIdentifier(parentMethodElement));

    if (parentMethodNode == null) {
      parentMethodNode = new MethodReferenceNode(parentMethodElement);
    }
    parentMethodNode.invokedMethods.add(stitchMethodIdentifier(methodElement));
    elementReferenceMap.put(stitchMethodIdentifier(parentMethodElement), parentMethodNode);
  }

  private String stitchClassIdentifier(TypeElement elem) {
    return stitchClassIdentifier(elementUtil.getBinaryName(elem));
  }

  public static String stitchClassIdentifier(String className) {
    StringBuilder sb = new StringBuilder("[");
    sb.append(className);
    sb.append("]");
    return sb.toString();
  }

  private String stitchFieldIdentifier(VariableDeclarationFragment fragment) {
    String className = elementUtil.getBinaryName(
        ElementUtil.getDeclaringClass(fragment.getVariableElement()));
    String fragmentIdentifier = fragment.getName().getIdentifier();
    return stitchFieldIdentifier(className, fragmentIdentifier);
  }

  public static String stitchFieldIdentifier(String className, String fieldName) {
    StringBuilder sb = new StringBuilder("[");
    sb.append(className);
    sb.append(",");
    sb.append(fieldName);
    sb.append("]");
    return sb.toString();
  }

  private String stitchMethodIdentifier(ExecutableElement methodElement) {
    String className = elementUtil.getBinaryName(ElementUtil.getDeclaringClass(methodElement));
    String methodName = typeUtil.getReferenceName(methodElement);
    String methodSignature = typeUtil.getReferenceSignature(methodElement);
    return stitchMethodIdentifier(className, methodName, methodSignature);
  }

  public static String stitchMethodIdentifier(String className, String methodName,
      String signature) {
    StringBuilder sb = new StringBuilder("[");
    sb.append(className);
    sb.append(",");
    sb.append(methodName);
    sb.append(",");
    sb.append(signature);
    sb.append("]");
    return sb.toString();
  }

  public ImmutableMap<String, ReferenceNode> getElementReferenceMap() {
    return ImmutableMap.copyOf(elementReferenceMap);
  }

  public ImmutableSet<String> getStaticSet() {
    return ImmutableSet.copyOf(staticSet);
  }

  /**
   * Do tree shaker traversal with rootSet cached in class (because no input root elements).
   */
  public void shakeTree() {
    shakeTree(staticSet);
  }

  /**
   * Do tree shaker traversal starting from input root set of node.
   * @param publicRootSet: Set of String identifiers for the root methods to start traversal from.
   */
  public void shakeTree(Set<String> publicRootSet) {
    for (String publicRoot : publicRootSet) {
      MethodReferenceNode node = (MethodReferenceNode) elementReferenceMap.get(publicRoot);
      traverseMethod(node);
    }
  }

  /**
   * Add to root set, methods from CodeReferenceMap and also all public methods in input classes.
   * Then, do tree shaker traversal starting from this root set.
   * @param publicRootSet: CodeReferenceMap with public root methods and classes.
   */
  //TODO(user): Current paradigm: All methods in input CodeReferenceMap are assumed to be
  //  public roots to traverse from.
  //Classes in input CodeReferenceMap here allow user to add Dynamically Loaded Classes and keep
  //  their public methods in the public root set.
  //In the future, when we add support for libraries, we will want to include protected methods
  //  of those library classes as well, so we should add "|| ElementUtil.isProtected(method)" after
  //  the isPublic check.
  public void shakeTree(CodeReferenceMap publicRootSet) {
    //Add all public methods in publicRootClasses to static set
    for (String clazz : publicRootSet.getReferencedClasses()) {
      ClassReferenceNode classNode = (ClassReferenceNode) elementReferenceMap
          .get(stitchClassIdentifier(clazz));
      assert(classNode != null);
      Iterable<ExecutableElement> methods = ElementUtil.getMethods(classNode.classElement);
      for (ExecutableElement method : methods) {
        if (ElementUtil.isPublic(method)) {
          staticSet.add(stitchMethodIdentifier(method));
        }
      }
    }

    //Add input root methods to static set
    for (Table.Cell<String, String, ImmutableSet<String>> cell : publicRootSet
        .getReferencedMethods().cellSet()) {
      String clazzName  = cell.getRowKey();
      String methodName  = cell.getColumnKey();
      for (String signature : cell.getValue()) {
        staticSet.add(stitchMethodIdentifier(clazzName, methodName, signature));
      }
    }

    shakeTree(staticSet);
  }

  public void traverseMethod(MethodReferenceNode node) {
    assert(node != null);
    if (node.used == true) {
      return;
    }
    node.used = true;
    markParentClasses(ElementUtil.getDeclaringClass(node.methodElement));

    for (String method : node.invokedMethods) {
      traverseMethod((MethodReferenceNode) elementReferenceMap.get(method));
    }
  }

  /**
   * Mark all ancestor classes of (sub)class as used
   */
  public void markParentClasses(TypeElement type) {
    while (type != null) {
      if (elementReferenceMap.get(stitchClassIdentifier(type)).used == true) {
        return;
      }
      elementReferenceMap.get(stitchClassIdentifier(type)).used = true;
      if (ElementUtil.isStatic(type)) {
        return;
      }
      type = ElementUtil.getDeclaringClass(type);
    }
  }

  public CodeReferenceMap buildTreeShakerMap() {
    Builder treeShakerMap = CodeReferenceMap.builder();
    for (ReferenceNode node : elementReferenceMap.values()) {
      if (!node.used) {
        node.addToBuilder(treeShakerMap);
      }
    }
    return treeShakerMap.build();
  }
}
