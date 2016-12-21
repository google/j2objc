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
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;

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

  private static TypeMirror getElementType(TypeMirror t) {
    while (TypeUtil.isArray(t)) {
      t = ((ArrayType) t).getComponentType();
    }
    return t;
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

  private static final TypeVisitor<Integer, Void> TYPE_DEPTH_COUNTER =
      new SimpleTypeVisitor8<Integer, Void>(0) {

    private int tryVisit(TypeMirror t) {
      return t == null ? 0 : visit(t);
    }

    private int visitList(List<? extends TypeMirror> types) {
      int max = 0;
      for (TypeMirror t : types) {
        max = Math.max(max, visit(t));
      }
      return max;
    }

    @Override
    public Integer visitArray(ArrayType t, Void p) {
      return visit(t.getComponentType()) + 1;
    }

    @Override
    public Integer visitDeclared(DeclaredType t, Void p) {
      // Visit enclosing types but don't penalize them by adding +1.
      return Math.max(visit(t.getEnclosingType()), visitList(t.getTypeArguments()) + 1);
    }

    @Override
    public Integer visitTypeVariable(TypeVariable t, Void p) {
      return 1;
    }

    @Override
    public Integer visitWildcard(WildcardType t, Void p) {
      return Math.max(tryVisit(t.getExtendsBound()), tryVisit(t.getSuperBound())) + 1;
    }
  };

  private static boolean isRawType(TypeMirror type) {
    return TypeUtil.isDeclaredType(type)
        && !TypeUtil.asTypeElement(type).getTypeParameters().isEmpty()
        && ((DeclaredType) type).getTypeArguments().isEmpty();
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

    private TypeNode createNode(TypeMirror type, String signature, String name) {
      TypeNode node = new TypeNode(signature, name, NameUtil.getQualifiedName(type));
      allTypes.put(signature, node);
      followType(type, node);
      return node;
    }

    private TypeNode getOrCreateNode(TypeMirror type) {
      type = getElementType(type);
      String signature = nameUtil.getSignature(type);
      TypeNode node = allTypes.get(signature);
      if (node != null) {
        return node;
      }
      if (!TypeUtil.isReferenceType(type) || isRawType(type)) {
        return null;
      }
      if (TYPE_DEPTH_COUNTER.visit(type) > 5) {
        // Avoid infinite recursion caused by type argument cycles.
        return null;
      }
      return createNode(type, signature, NameUtil.getName(type));
    }

    private void visitType(TypeMirror type) {
      if (type == null) {
        return;
      } else if (TypeUtil.isIntersection(type)) {
        for (TypeMirror bound : ((IntersectionType) type).getBounds()) {
          getOrCreateNode(bound);
        }
      } else {
        getOrCreateNode(type);
      }
    }

    private void followType(TypeMirror type, TypeNode node) {
      for (TypeMirror supertype : typeUtil.directSupertypes(type)) {
        TypeNode supertypeNode = getOrCreateNode(supertype);
        if (supertypeNode != null) {
          subtypes.put(supertypeNode, node);
          if (TypeUtil.isDeclaredType(supertype)
              && TypeUtil.getDeclaredTypeKind(supertype).isClass()) {
            superclasses.put(node, supertypeNode);
          }
        }
      }
      if (TypeUtil.isDeclaredType(type)) {
        followDeclaredType((DeclaredType) type, node);
      }
    }

    private void followDeclaredType(DeclaredType type, TypeNode node) {
      followEnclosingType((DeclaredType) type, node);
      followFields((DeclaredType) type, node);
      for (TypeMirror typeArg : type.getTypeArguments()) {
        visitType(typeArg);
      }
    }

    private void followFields(DeclaredType type, TypeNode node) {
      TypeElement element = (TypeElement) type.asElement();
      for (VariableElement field : ElementUtil.getDeclaredFields(element)) {
        TypeMirror fieldType = getElementType(typeUtil.asMemberOf(type, field));
        TypeNode target = getOrCreateNode(fieldType);
        String fieldName = ElementUtil.getName(field);
        if (target != null
            && !whitelist.containsField(node, fieldName)
            && !whitelist.containsType(target)
            && !ElementUtil.isStatic(field)
            // Exclude self-referential fields. (likely linked DS or delegate pattern)
            && !typeUtil.isAssignable(type, fieldType)
            && !ElementUtil.isWeakReference(field)
            && !ElementUtil.isRetainedWithField(field)) {
          addEdge(Edge.newFieldEdge(node, target, fieldName));
        }
      }
    }

    private void followEnclosingType(DeclaredType type, TypeNode typeNode) {
      TypeMirror enclosingType = type.getEnclosingType();
      if (TypeUtil.isNone(enclosingType)) {
        return;
      }
      TypeNode enclosingTypeNode = getOrCreateNode(enclosingType);
      TypeElement element = (TypeElement) type.asElement();
      TypeNode declarationType = getOrCreateNode(element.asType());
      if (declarationType != null && enclosingTypeNode != null
          && ElementUtil.hasOuterContext(element)
          && !elementUtil.isWeakOuterType(element)
          && !whitelist.containsType(enclosingTypeNode)
          && !whitelist.hasOuterForType(typeNode)) {
        possibleOuterEdges.put(
            declarationType, Edge.newOuterClassEdge(typeNode, enclosingTypeNode));
      }
    }

    private void followCaptureFields(TypeElement type, TypeNode typeNode) {
      assert ElementUtil.isAnonymous(type);
      for (VariableElement capturedVarElement : captureInfo.getLocalCaptureFields(type)) {
        TypeNode targetNode = getOrCreateNode(capturedVarElement.asType());
        if (targetNode != null && !whitelist.containsType(targetNode)
            && !ElementUtil.isWeakReference(capturedVarElement)) {
          addEdge(Edge.newCaptureEdge(
              typeNode, targetNode, ElementUtil.getName(capturedVarElement)));
        }
      }
    }

    private void handleTypeDeclaration(TypeDeclaration node) {
      TypeElement typeElem = node.getTypeElement();
      TypeMirror type = typeElem.asType();
      String name = ElementUtil.isAnonymous(typeElem)
          ? "anonymous:" + node.getLineNumber() : NameUtil.getName(type);
      TypeNode typeNode = createNode(type, nameUtil.getSignature(type), name);
      if (captureInfo.needsOuterReference(typeElem)) {
        hasOuterRef.add(typeNode);
      }
      if (ElementUtil.isAnonymous(typeElem)) {
        followCaptureFields(typeElem, typeNode);
      }
    }

    @Override
    public boolean visit(TypeDeclaration node) {
      handleTypeDeclaration(node);
      return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
      visitType(node.getTypeMirror());
      return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      visitType(node.getTypeMirror());
      return true;
    }
  }
}
