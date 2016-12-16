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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
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
    protected boolean reachable = false;

    public abstract String getUniqueID();
    public abstract void addToBuilder(Builder builder);
    public abstract boolean isDead();
  }

  class ClassReferenceNode extends ReferenceNode {
    final TypeElement classElement;
    boolean containsPublicField = false;
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

    /**
     * Returns whether the class is unused or not.
     *   Classes that contain public fields can possibly be referenced by the field indirectly,
     *   so we want to keep those class source files.
     */
    //TODO(user): When FieldAccess detection is supported, mark that class as reachable there,
    // and remove the containsPublicField flag here.
    @Override
    public boolean isDead() {
      return !(reachable || containsPublicField);
    }
  }

  class FieldReferenceNode extends ReferenceNode {
    final VariableDeclarationFragment fieldFragment;
    final boolean isPublic;

    public FieldReferenceNode(VariableDeclarationFragment fieldFragment) {
      this.fieldFragment = fieldFragment;
      this.isPublic = !ElementUtil.isPrivate(fieldFragment.getVariableElement());
    }

    @Override
    public String getUniqueID() {
      return stitchFieldIdentifier(fieldFragment);
    }

    @Override
    public void addToBuilder(Builder builder) {
      //TODO(user): Enable the following code when the FieldAccess use-marking is done.
      //String className = elementUtil.getBinaryName(
      //    ElementUtil.getDeclaringClass(fieldFragment.getVariableElement()));
      //String fragmentIdentifier = fieldFragment.getName().getIdentifier();
      //builder.addDeadField(className, fragmentIdentifier);
    }

    @Override
    public boolean isDead() {
      return !reachable;
    }
  }

  class MethodReferenceNode extends ReferenceNode {
    final ExecutableElement methodElement;
    boolean invoked = false;
    boolean declared = false;
    Set<String> invokedMethods;
    Set<String> overridingMethods;

    public MethodReferenceNode(ExecutableElement methodElement) {
      this.methodElement = methodElement;
      this.invokedMethods = new HashSet<String>();
      this.overridingMethods = new HashSet<String>();
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

    /**
     * Returns whether the method is unused or not.
     *   Methods that are invoked but not declared are .class methods not included in source files.
     *   Hence, they should not be added to the CodeReferenceMap at all.
     */
    @Override
    public boolean isDead() {
      return !(reachable || (invoked && !declared));
    }
  }

  private final HashMap<String, ReferenceNode> elementReferenceMap;
  private final Set<String> staticSet;
  private final HashMap<String, Set<String>> overrideMap;

  public ElementReferenceMapper(CompilationUnit unit, HashMap<String, ReferenceNode>
      elementReferenceMap, Set<String> staticSet, HashMap<String, Set<String>> overrideMap) {
    super(unit);
    this.elementReferenceMap = elementReferenceMap;
    this.staticSet = staticSet;
    this.overrideMap = overrideMap;
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
    elementReferenceMap.putIfAbsent(stitchClassIdentifier(type), new ClassReferenceNode(type));
  }

  //TODO(user): Add the field type class to reference classes.
  //Currently, jdt only supports well known types. Soon, we can get type mirror from field
  //and resolve the type by its name using a resolve method in the parser environment.
  @Override
  public void endVisit(VariableDeclarationFragment fragment) {
    //TODO(user): Add field to elementReferenceMap when field detection is enabled and the
    //  ElementUtil.getBinaryName() method doesn't break when called on a static block's
    //  ExecutableElement.
    //String fieldID = stitchFieldIdentifier(fragment);
    //elementReferenceMap.putIfAbsent(fieldID, new FieldReferenceNode(fragment));

    Element element = fragment.getVariableElement().getEnclosingElement();
    if (element instanceof TypeElement) {
      TypeElement type = (TypeElement) element;
      if (ElementUtil.isPublic(fragment.getVariableElement())) {
        ClassReferenceNode node = (ClassReferenceNode) elementReferenceMap
            .get(stitchClassIdentifier(type));
        if (node == null) {
          node = new ClassReferenceNode(type);
        }
        node.containsPublicField = true;
        elementReferenceMap.putIfAbsent(stitchClassIdentifier(type), node);
      }
    }
  }

  /**
   * Adds a node for the child in the elementReferenceMap if it doesn't exist, marks it as declared,
   * and adds this method to the override map.
   * @param methodElement
   */
  private void handleChildMethod(ExecutableElement methodElement) {
    String methodIdentifier = stitchMethodIdentifier(methodElement);
    MethodReferenceNode node = (MethodReferenceNode) elementReferenceMap.get(methodIdentifier);
    if (node == null) {
      node = new MethodReferenceNode(methodElement);
    }
    node.invoked = true;
    elementReferenceMap.put(methodIdentifier, node);
    addToOverrideMap(methodElement);
  }

  /**
   * Adds a node for the parent in the elementReferenceMap if it doesn't exist, adds the method to
   * the override map, and links the child method in the invokedMethods set.
   * @param parentMethodElement
   * @param childMethodElement
   */
  private void handleParentMethod(ExecutableElement parentMethodElement, ExecutableElement
      childMethodElement) {
    MethodReferenceNode parentMethodNode = (MethodReferenceNode) elementReferenceMap
        .get(stitchMethodIdentifier(parentMethodElement));
    if (parentMethodNode == null) {
      parentMethodNode = new MethodReferenceNode(parentMethodElement);
    }
    parentMethodNode.invokedMethods.add(stitchMethodIdentifier(childMethodElement));
    elementReferenceMap.put(stitchMethodIdentifier(parentMethodElement), parentMethodNode);
    addToOverrideMap(parentMethodElement);
  }

  /**
   * When a constructor in invoked (including a default constructor), adds the constructor and
   * invoking method to elementReferenceMap. The class will eventually be marked as used.
   * Counts as both the declaration (in class) and invocation (new _()) of the constructor.
   */
  @Override
  public void endVisit(ClassInstanceCreation instance) {
    ExecutableElement childMethodElement = instance.getExecutableElement();
    handleChildMethod(childMethodElement);

    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(instance);
    if (parentMethodDeclaration == null) {
      staticSet.add(stitchMethodIdentifier(childMethodElement));
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    handleParentMethod(parentMethodElement, childMethodElement);
  }

  @Override
  public void endVisit(ConstructorInvocation invocation) {
    ExecutableElement childMethodElement = invocation.getExecutableElement();
    handleChildMethod(childMethodElement);

    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(invocation);
    if (parentMethodDeclaration == null) {
      staticSet.add(stitchMethodIdentifier(childMethodElement));
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    handleParentMethod(parentMethodElement, childMethodElement);
  }

  @Override
  public void endVisit(SuperConstructorInvocation invocation) {
    ExecutableElement childMethodElement = invocation.getExecutableElement();
    handleChildMethod(childMethodElement);

    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(invocation);
    if (parentMethodDeclaration == null) {
      staticSet.add(stitchMethodIdentifier(childMethodElement));
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    handleParentMethod(parentMethodElement, childMethodElement);
  }

  @Override
  public void endVisit(MethodDeclaration method) {
    if (Modifier.isNative(method.getModifiers())) {
      return;
    }
    ExecutableElement methodElement = method.getExecutableElement();
    String methodIdentifier = stitchMethodIdentifier(methodElement);

    MethodReferenceNode node = (MethodReferenceNode) elementReferenceMap.get(methodIdentifier);
    if (node == null) {
      node = new MethodReferenceNode(methodElement);
    }
    node.declared = true;
    elementReferenceMap.put(stitchMethodIdentifier(methodElement), node);
    addToOverrideMap(methodElement);
  }

  @Override
  public void endVisit(MethodInvocation method) {
    ExecutableElement childMethodElement = method.getExecutableElement();
    handleChildMethod(childMethodElement);

    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(method);
    if (parentMethodDeclaration == null) {
      staticSet.add(stitchMethodIdentifier(childMethodElement));
      return;
    }
    ExecutableElement parentMethodElement = parentMethodDeclaration.getExecutableElement();
    handleParentMethod(parentMethodElement, childMethodElement);
  }

  /**
   * Adds the common IDs of overriding methods (methodName and signature) to the override map.
   * @param methodElement
   */
  private void addToOverrideMap(ExecutableElement methodElement) {
    String overrideID = stitchOverrideMethodIdentifier(methodElement);
    if (overrideMap.containsKey(overrideID)) {
      overrideMap.get(overrideID).add(stitchMethodIdentifier(methodElement));
    } else {
      HashSet<String> overrideSet = new HashSet<String>();
      overrideSet.add(stitchMethodIdentifier(methodElement));
      overrideMap.put(overrideID, overrideSet);
    }
  }

  public String stitchClassIdentifier(TypeElement elem) {
    return stitchClassIdentifier(elem, elementUtil);
  }

  public static String stitchClassIdentifier(TypeElement elem, ElementUtil inputElementUtil) {
    return stitchClassIdentifier(inputElementUtil.getBinaryName(elem));
  }

  public static String stitchClassIdentifier(String className) {
    StringBuilder sb = new StringBuilder("[");
    sb.append(className);
    sb.append("]");
    return sb.toString();
  }

  public String stitchFieldIdentifier(VariableDeclarationFragment fragment) {
    return stitchFieldIdentifier(fragment, elementUtil);
  }

  public static String stitchFieldIdentifier(VariableDeclarationFragment fragment, ElementUtil
      inputElementUtil) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(fragment.getVariableElement());
    String className = inputElementUtil.getBinaryName(declaringClass);
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

  public String stitchOverrideMethodIdentifier(ExecutableElement methodElement) {
    return stitchOverrideMethodIdentifier(methodElement, typeUtil);
  }

  public static String stitchOverrideMethodIdentifier(ExecutableElement methodElement,
      TypeUtil inputTypeUtil) {
    String methodName = inputTypeUtil.getReferenceName(methodElement);
    String methodSignature = inputTypeUtil.getReferenceSignature(methodElement);
    return stitchOverrideMethodIdentifier(methodName, methodSignature);
  }

  public static String stitchOverrideMethodIdentifier(String methodName, String signature) {
    StringBuilder sb = new StringBuilder("[");
    sb.append(methodName);
    sb.append(",");
    sb.append(signature);
    sb.append("]");
    return sb.toString();
  }

  public String stitchMethodIdentifier(ExecutableElement methodElement) {
    return stitchMethodIdentifier(methodElement, typeUtil, elementUtil);
  }

  public static String stitchMethodIdentifier(ExecutableElement methodElement,
      TypeUtil inputTypeUtil, ElementUtil inputElementUtil) {
    String className = inputElementUtil.getBinaryName(ElementUtil.getDeclaringClass(methodElement));
    String methodName = inputTypeUtil.getReferenceName(methodElement);
    String methodSignature = inputTypeUtil.getReferenceSignature(methodElement);
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
}
