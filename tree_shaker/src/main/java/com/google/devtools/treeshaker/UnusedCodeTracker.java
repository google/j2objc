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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.treeshaker.ElementReferenceMapper.ClassReferenceNode;
import com.google.devtools.treeshaker.ElementReferenceMapper.MethodReferenceNode;
import com.google.devtools.treeshaker.ElementReferenceMapper.ReferenceNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * UnusedCodeTracker traverses all elements of its elementReferenceMap and determines unused code.
 *
 * @author Priyank Malvania
 */
public class UnusedCodeTracker {

  private final TranslationEnvironment env;
  private final HashMap<String, ReferenceNode> elementReferenceMap;
  private final HashMap<String, Set<String>> overrideMap;
  private final Set<String> staticSet;
  private final Set<String> rootSet = new HashSet<String>();
  private final Set<MethodReferenceNode> declaredSet = new HashSet<MethodReferenceNode>();

  public UnusedCodeTracker(TranslationEnvironment env, HashMap<String, ReferenceNode>
      elementReferenceMap, Set<String> staticSet, HashMap<String, Set<String>> overrideMap) {
    Preconditions.checkNotNull(env);
    Preconditions.checkNotNull(elementReferenceMap);
    Preconditions.checkNotNull(staticSet);

    this.env = env;
    this.elementReferenceMap = elementReferenceMap;
    this.staticSet = staticSet;
    this.overrideMap = overrideMap;
  }

  /**
   * Since the MethodInvocation node cannot currently detect invocations of overriding methods,
   * (it only detects the top-level method being invoked), this method allows treeshaker to track
   * which methods are being overridden. For all relevant methods (that are declared but not
   * invoked), checks all other methods with the same overrideID in the overrideMap, and compares
   * each pair with the ElementUtil.overrides method.
   */
  public void mapOverridingMethods() {
    for (String key : elementReferenceMap.keySet()) {
      ReferenceNode node = elementReferenceMap.get(key);
      if (node instanceof MethodReferenceNode) {
        MethodReferenceNode methodNode = (MethodReferenceNode) node;
        if (methodNode.declared && !methodNode.invoked) {
          declaredSet.add(methodNode);
        }
      }
    }

    for (MethodReferenceNode derivedNode : declaredSet) {
      String overrideID = ElementReferenceMapper.stitchOverrideMethodIdentifier(
          derivedNode.methodElement, env.typeUtil());
      assert(overrideMap.get(overrideID) != null);
      for (String otherID : overrideMap.get(overrideID)) {
        MethodReferenceNode baseNode = ((MethodReferenceNode) elementReferenceMap.get(otherID));
        if (env.elementUtil().overrides(derivedNode.methodElement, baseNode.methodElement,
            ElementUtil.getDeclaringClass(derivedNode.methodElement))) {
          baseNode.overridingMethods.add(derivedNode.getUniqueID());
        }
      }
    }
  }

  /**
   * Do tree shaker traversal with staticSet cached in class (because no input root elements).
   */
  public void markUsedElements() {
    markUsedElements(staticSet);
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
  public void markUsedElements(CodeReferenceMap publicRootSet) {
    if (publicRootSet == null) {
      markUsedElements();
      return;
    }
    //Add all public methods in publicRootClasses to root set
    for (String clazz : publicRootSet.getReferencedClasses()) {
      ClassReferenceNode classNode = (ClassReferenceNode) elementReferenceMap
          .get(ElementReferenceMapper.stitchClassIdentifier(clazz));
      assert(classNode != null);
      Iterable<ExecutableElement> methods = ElementUtil.getMethods(classNode.classElement);
      for (ExecutableElement method : methods) {
        if (ElementUtil.isPublic(method)) {
          rootSet.add(ElementReferenceMapper.stitchMethodIdentifier(method, env.typeUtil(),
              env.elementUtil()));
        }
      }
    }

    //Add input root methods to static set
    for (Table.Cell<String, String, ImmutableSet<String>> cell : publicRootSet
        .getReferencedMethods().cellSet()) {
      String clazzName  = cell.getRowKey();
      String methodName  = cell.getColumnKey();
      for (String signature : cell.getValue()) {
        rootSet.add(ElementReferenceMapper
            .stitchMethodIdentifier(clazzName, methodName, signature));
      }
    }

    markUsedElements(staticSet);
    markUsedElements(rootSet);
  }

  /**
   * Do tree shaker traversal starting from input root set of node.
   * @param publicRootSet: Set of String identifiers for the root methods to start traversal from.
   */
  public void markUsedElements(Set<String> publicRootSet) {
    for (String publicRoot : publicRootSet) {
      traverseMethod(publicRoot);
    }
  }

  /**
   * Traverses the method invocation graph created by ElementReferenceMapper, and marks all methods
   * that are reachable from the inputRootSet. Also covers all methods that possibly override these
   * called methods.
   * @param methodID
   */
  public void traverseMethod(String methodID) {
    MethodReferenceNode node = (MethodReferenceNode) elementReferenceMap.get(methodID);
    if (node == null) {
      //TODO(user): This might never be reached, because we create a node for every method,
      //                both invoked and declared.
      ErrorUtil.warning("Encountered .class method while accessing: " + methodID);
      return;
    }
    if (node.reachable) {
      return;
    }
    node.reachable = true;
    markParentClasses(ElementUtil.getDeclaringClass(node.methodElement));

    for (String invokedMethodID : node.invokedMethods) {
      traverseMethod(invokedMethodID);
    }
    for (String overrideMethodID : node.overridingMethods) {
      traverseMethod(overrideMethodID);
    }
  }

  /**
   * Mark all ancestor classes of (sub)class as used
   */
  public void markParentClasses(TypeElement type) {
    while (type != null) {
      String typeID = ElementReferenceMapper.stitchClassIdentifier(type, env.elementUtil());
      ReferenceNode node = elementReferenceMap.get(typeID);
      if (node == null) {
        ErrorUtil.warning("Encountered .class parent class while accessing: " + typeID);
        return;
      }
      if (node.reachable) {
        return;
      }
      node.reachable = true;
      if (ElementUtil.isStatic(type)) {
        return;
      }
      type = ElementUtil.getDeclaringClass(type);
    }
  }

  public CodeReferenceMap buildTreeShakerMap() {
    Builder treeShakerMap = CodeReferenceMap.builder();
    for (ReferenceNode node : elementReferenceMap.values()) {
      if (node.isDead()) {
        node.addToBuilder(treeShakerMap);
      }
    }
    return treeShakerMap.build();
  }
}
