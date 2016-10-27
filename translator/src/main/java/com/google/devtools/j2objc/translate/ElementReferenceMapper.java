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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
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

  static class ReferenceNode {
    boolean used = false;
  }

  static class ClassReferenceNode extends ReferenceNode{
    final TypeElement classElement;

    public ClassReferenceNode(TypeElement classElement) {
      this.classElement = classElement;
    }
  }

  static class FieldReferenceNode extends ReferenceNode{
    final VariableDeclarationFragment fieldFragment;

    public FieldReferenceNode(VariableDeclarationFragment fieldFragment) {
      this.fieldFragment = fieldFragment;
    }
  }

  static class MethodReferenceNode extends ReferenceNode{
    final ExecutableElement methodElement;
    boolean visited = false;
    Set<String> invokedMethods;

    public MethodReferenceNode(ExecutableElement methodElement) {
      this.methodElement = methodElement;
      this.invokedMethods = new HashSet<String>();
    }
  }

  private HashMap<String, ReferenceNode> elementReferenceMap = new HashMap<>();

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
  public void endVisit(FieldDeclaration field) {
    for (VariableDeclarationFragment fragment : field.getFragments()) {
      elementReferenceMap.putIfAbsent(stitchFieldIdentifier(fragment),
          new FieldReferenceNode(fragment));
    }
  }

  @Override
  public void endVisit(MethodDeclaration method) {
    if (Modifier.isNative(method.getModifiers())) {
      return;
    }
    ExecutableElement methodElement = method.getExecutableElement();
    elementReferenceMap.putIfAbsent(stitchMethodIdentifier(methodElement),
        new MethodReferenceNode(methodElement));
  }

  @Override
  public void endVisit(MethodInvocation method) {
    ExecutableElement methodElement = method.getExecutableElement();
    MethodDeclaration parentMethodDeclaration = TreeUtil.getEnclosingMethod(method);

    if (parentMethodDeclaration == null) {
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
}
