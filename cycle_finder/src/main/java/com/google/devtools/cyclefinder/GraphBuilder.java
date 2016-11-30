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

package com.google.devtools.cyclefinder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Builds the graph of possible references between types.
 *
 * @author Keith Stanger
 */
public class GraphBuilder {

  private final Map<String, TypeNode> allTypes = new HashMap<>();
  private final NameList whitelist;
  private final ReferenceGraph graph = new ReferenceGraph();
  private final Map<TypeNode, TypeNode> superclasses = new HashMap<>();
  private final SetMultimap<TypeNode, TypeNode> subtypes = HashMultimap.create();
  private final SetMultimap<TypeNode, Edge> possibleOuterEdges = HashMultimap.create();
  private final Set<TypeNode> hasOuterRef = new HashSet<>();

  public GraphBuilder(NameList whitelist) {
    this.whitelist = whitelist;
  }

  public GraphBuilder constructGraph() {
    addOuterEdges();
    addSubtypeEdges();
    addSuperclassEdges();
    return this;
  }

  public ReferenceGraph getGraph() {
    return graph;
  }

  private void addEdge(Edge e) {
    if (!e.getOrigin().equals(e.getTarget())) {
      graph.addEdge(e);
    }
  }

  private static ITypeBinding getElementType(ITypeBinding type) {
    if (type.isArray()) {
      return type.getElementType();
    }
    return type;
  }

  private void addOuterEdges() {
    for (TypeNode type : hasOuterRef) {
      for (Edge e : possibleOuterEdges.get(type)) {
        addEdge(e);
      }
    }
  }

  private void addSubtypeEdges() {
    for (TypeNode type : allTypes.values()) {
      for (Edge e : ImmutableList.copyOf(graph.getEdges(type))) {
        Set<TypeNode> targetSubtypes = subtypes.get(e.getTarget());
        Set<TypeNode> whitelisted = new HashSet<>();
        String fieldName = e.getFieldQualifiedName();
        if (fieldName == null) {
          continue;  // Outer or capture field.
        }
        for (TypeNode subtype : targetSubtypes) {
          if (whitelist.isWhitelistedTypeForField(fieldName, subtype)
              || whitelist.containsType(subtype)) {
            whitelisted.add(subtype);
            whitelisted.addAll(subtypes.get(subtype));
          }
        }
        for (TypeNode subtype : Sets.difference(targetSubtypes, whitelisted)) {
          addEdge(Edge.newSubtypeEdge(e, subtype));
        }
      }
    }
  }

  private void addSuperclassEdges() {
    for (TypeNode type : allTypes.values()) {
      TypeNode superclassNode = superclasses.get(type);
      while (superclassNode != null) {
        for (Edge e : graph.getEdges(superclassNode)) {
          addEdge(Edge.newSuperclassEdge(e, type, superclassNode));
        }
        superclassNode = superclasses.get(superclassNode);
      }
    }
  }

  private static boolean hasWildcard(ITypeBinding type) {
    if (type.isWildcardType()) {
      return true;
    }
    for (ITypeBinding typeParam : type.getTypeArguments()) {
      if (hasWildcard(typeParam)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasNestedWildcard(ITypeBinding type) {
    ITypeBinding bound = type.getBound();
    if (bound != null && hasWildcard(bound)) {
      return true;
    }
    for (ITypeBinding typeParam : type.getTypeArguments()) {
      if (hasNestedWildcard(typeParam)) {
        return true;
      }
    }
    return false;
  }

  public void visitAST(CompilationUnit unit) {
    new Visitor(unit).run();
  }

  private class Visitor extends UnitTreeVisitor {

    private final CaptureInfo captureInfo;
    private final NameUtil nameUtil;

    private Visitor(CompilationUnit unit) {
      super(unit);
      captureInfo = unit.getEnv().captureInfo();
      nameUtil = new NameUtil(typeUtil);
    }

    private TypeNode createNode(ITypeBinding type, String signature, String name) {
      TypeNode node = new TypeNode(signature, name, NameUtil.getQualifiedName(type));
      allTypes.put(signature, node);
      followType(type, node);
      return node;
    }

    private TypeNode getOrCreateNode(ITypeBinding type) {
      String signature = nameUtil.getSignature(type);
      TypeNode node = allTypes.get(signature);
      if (node != null) {
        return node;
      }
      if (type.isPrimitive() || type.isRawType()) {
        return null;
      }
      if (hasNestedWildcard(type)) {
        // Avoid infinite recursion caused by nested wildcard types.
        return null;
      }
      return createNode(type, signature, NameUtil.getName(type));
    }

    private void visitType(ITypeBinding type) {
      TypeMirror typeM = BindingConverter.getType(type);
      if (type == null) {
        return;
      } else if (TypeUtil.isIntersection(typeM)) {
        for (TypeMirror bound : ((IntersectionType) typeM).getBounds()) {
          getOrCreateNode(getElementType(BindingConverter.unwrapTypeMirrorIntoTypeBinding(bound)));
        }
      } else {
        getOrCreateNode(getElementType(type));
      }
    }

    private void followType(ITypeBinding type, TypeNode node) {
      ITypeBinding superclass = type.getSuperclass();
      if (superclass != null) {
        TypeNode superclassNode = getOrCreateNode(superclass);
        if (superclassNode != null) {
          superclasses.put(node, superclassNode);
          subtypes.put(superclassNode, node);
        }
      }
      for (ITypeBinding interfaze : type.getInterfaces()) {
        TypeNode interfaceNode = getOrCreateNode(interfaze);
        if (interfaceNode != null) {
          subtypes.put(interfaceNode, node);
        }
      }
      followDeclaringClass(type, node);
      followFields(type, node);
    }

    private void followFields(ITypeBinding type, TypeNode node) {
      for (IVariableBinding field : type.getDeclaredFields()) {
        ITypeBinding fieldType = getElementType(field.getType());
        for (ITypeBinding typeParam : fieldType.getTypeArguments()) {
          visitType(typeParam);
        }
        TypeNode target = getOrCreateNode(fieldType);
        VariableElement fieldE = BindingConverter.getVariableElement(field);
        if (target != null
            && !whitelist.containsField(node, field.getName())
            && !whitelist.containsType(target)
            && !fieldType.isPrimitive()
            && !Modifier.isStatic(field.getModifiers())
            // Exclude self-referential fields. (likely linked DS or delegate pattern)
            && !type.isAssignmentCompatible(fieldType)
            && !ElementUtil.isWeakReference(fieldE)
            && !ElementUtil.isRetainedWithField(fieldE)) {
          addEdge(Edge.newFieldEdge(node, target, field.getName()));
        }
      }
    }

    private void followDeclaringClass(ITypeBinding typeBinding, TypeNode typeNode) {
      ITypeBinding declaringClass = typeBinding.getDeclaringClass();
      if (declaringClass == null) {
        return;
      }
      TypeNode declaringClassNode = getOrCreateNode(declaringClass);
      if (declaringClassNode == null) {
        return;
      }
      Element element = BindingConverter.getElement(typeBinding.getTypeDeclaration());
      TypeNode declarationType = getOrCreateNode(typeBinding.getTypeDeclaration());
      if (declarationType != null
          && ElementUtil.isTypeElement(element)
          && ElementUtil.hasOuterContext((TypeElement) element)
          && !ElementUtil.isWeakOuterType((TypeElement) element)
          && !whitelist.containsType(declaringClassNode)
          && !whitelist.hasOuterForType(typeNode)) {
        possibleOuterEdges.put(
            declarationType, Edge.newOuterClassEdge(typeNode, declaringClassNode));
      }
    }

    private void followCaptureFields(ITypeBinding typeBinding, TypeNode typeNode) {
      assert typeBinding.isAnonymous();
      for (VariableElement capturedVarElement : captureInfo.getCaptureFields(
          BindingConverter.getTypeElement(typeBinding.getTypeDeclaration()))) {
        IVariableBinding capturedVarBinding =
            BindingConverter.unwrapVariableElement(capturedVarElement);
        ITypeBinding targetType = getElementType(capturedVarBinding.getType());
        TypeNode targetNode = getOrCreateNode(targetType);
        if (targetNode != null && !whitelist.containsType(targetNode)
            && !ElementUtil.isWeakReference(capturedVarElement)) {
          addEdge(Edge.newCaptureEdge(typeNode, targetNode, capturedVarBinding.getName()));
        }
      }
    }

    private void maybeAddOuterReference(ITypeBinding typeBinding, TypeNode typeNode) {
      TypeElement element = BindingConverter.getTypeElement(typeBinding);
      if (captureInfo.needsOuterReference(element)) {
        hasOuterRef.add(typeNode);
      }
    }

    @Override
    public boolean visit(TypeDeclaration node) {
      ITypeBinding binding = BindingConverter.unwrapTypeElement(node.getTypeElement());
      TypeNode typeNode = createNode(
          binding, nameUtil.getSignature(binding), NameUtil.getName(binding));
      maybeAddOuterReference(binding, typeNode);
      return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      ITypeBinding binding = BindingConverter.unwrapTypeElement(node.getTypeElement());
      TypeNode typeNode = createNode(
          binding, nameUtil.getSignature(binding), "anonymous:" + node.getLineNumber());
      maybeAddOuterReference(binding, typeNode);
      followCaptureFields(binding, typeNode);
      return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
      visitType(BindingConverter.unwrapTypeMirrorIntoTypeBinding(node.getTypeMirror()));
      return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      visitType(BindingConverter.unwrapTypeMirrorIntoTypeBinding(node.getTypeMirror()));
      return true;
    }
  }
}
