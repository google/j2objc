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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Builds the graph of possible references between types and searches for
 * possible cycles.
 *
 * @author Keith Stanger
 */
public class ReferenceGraph {

  private final Map<String, TypeNode> allTypes;
  private final CaptureFields captureFields;
  private final NameList whitelist;
  private final NameList blacklist;
  private SetMultimap<TypeNode, Edge> edges = HashMultimap.create();
  private List<List<Edge>> cycles = Lists.newArrayList();

  public ReferenceGraph(
      TypeCollector typeCollector, CaptureFields captureFields, NameList whitelist,
      NameList blacklist) {
    this.allTypes = typeCollector.getTypes();
    this.captureFields = captureFields;
    this.whitelist = whitelist;
    this.blacklist = blacklist;
  }

  public List<List<Edge>> findCycles() {
    constructGraph();
    runTarjans();
    return cycles;
  }

  private void constructGraph() {
    addFieldEdges();
    addSubtypeEdges();
    addSuperclassEdges();
    addOuterClassEdges();
    // TODO(kstanger): Capture edges should be added before subtype edges.
    addAnonymousClassCaptureEdges();
  }

  private void addEdge(Edge e) {
    if (!e.getOrigin().equals(e.getTarget())) {
      edges.put(e.getOrigin(), e);
    }
  }

  private void addFieldEdges() {
    for (TypeNode node : allTypes.values()) {
      ITypeBinding type = node.getTypeBinding();
      for (IVariableBinding field : type.getDeclaredFields()) {
        VariableElement fieldE = BindingConverter.getVariableElement(field);
        ITypeBinding fieldType = getElementType(field.getType());
        TypeNode targetNode = allTypes.get(fieldType.getKey());
        if (targetNode != null
            && !whitelist.containsField(field)
            && !whitelist.containsType(fieldType)
            && !fieldType.isPrimitive()
            && !Modifier.isStatic(field.getModifiers())
            // Exclude self-referential fields. (likely linked DS or delegate pattern)
            && !type.isAssignmentCompatible(fieldType)
            && !ElementUtil.isWeakReference(fieldE)
            && !ElementUtil.isRetainedWithField(fieldE)) {
          addEdge(Edge.newFieldEdge(node, targetNode, field));
        }
      }
    }
  }

  private ITypeBinding getElementType(ITypeBinding type) {
    if (type.isArray()) {
      return type.getElementType();
    }
    return type;
  }

  private void addSubtypeEdges() {
    SetMultimap<TypeNode, TypeNode> subtypes = HashMultimap.create();
    for (TypeNode type : allTypes.values()) {
      collectSubtypes(type, type, subtypes);
    }
    for (TypeNode type : allTypes.values()) {
      for (Edge e : ImmutableList.copyOf(edges.get(type))) {
        Set<TypeNode> targetSubtypes = subtypes.get(e.getTarget());
        Set<TypeNode> whitelisted = new HashSet<>();
        IVariableBinding field = e.getField();
        for (TypeNode subtype : targetSubtypes) {
          ITypeBinding subtypeBinding = subtype.getTypeBinding();
          if ((field != null && field.isField()
               && whitelist.isWhitelistedTypeForField(field, subtypeBinding))
              || whitelist.containsType(subtypeBinding)) {
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

  private void collectSubtypes(
      TypeNode originalType, TypeNode type, Multimap<TypeNode, TypeNode> subtypes) {
    ITypeBinding typeBinding = type.getTypeBinding();
    for (ITypeBinding interfaze : typeBinding.getInterfaces()) {
      TypeNode interfaceNode = allTypes.get(interfaze.getKey());
      if (interfaceNode != null) {
        subtypes.put(interfaceNode, originalType);
        collectSubtypes(originalType, interfaceNode, subtypes);
      }
    }
    if (typeBinding.getSuperclass() != null) {
      TypeNode superclassNode = allTypes.get(typeBinding.getSuperclass().getKey());
      if (superclassNode != null) {
        subtypes.put(superclassNode, originalType);
        collectSubtypes(originalType, superclassNode, subtypes);
      }
    }
  }

  private void addSuperclassEdges() {
    for (TypeNode type : allTypes.values()) {
      ITypeBinding superclass = type.getTypeBinding().getSuperclass();
      while (superclass != null) {
        TypeNode superclassNode = allTypes.get(superclass.getKey());
        for (Edge e : edges.get(superclassNode)) {
          addEdge(Edge.newSuperclassEdge(e, type, superclassNode));
        }
        superclass = superclass.getSuperclass();
      }
    }
  }

  private void addOuterClassEdges() {
    for (TypeNode type : allTypes.values()) {
      ITypeBinding typeBinding = type.getTypeBinding();
      Element element = BindingConverter.getElement(typeBinding.getTypeDeclaration());
      if (ElementUtil.isTypeElement(element)
          && captureFields.hasOuterReference((TypeElement) element)
          && !ElementUtil.isWeakOuterType((TypeElement) element)) {
        ITypeBinding declaringType = typeBinding.getDeclaringClass();
        if (declaringType != null && !whitelist.containsType(declaringType)
            && !whitelist.hasOuterForType(typeBinding)) {
          addEdge(Edge.newOuterClassEdge(type, allTypes.get(declaringType.getKey())));
        }
      }
    }
  }

  private void addAnonymousClassCaptureEdges() {
    for (TypeNode type : allTypes.values()) {
      ITypeBinding typeBinding = type.getTypeBinding();
      if (typeBinding.isAnonymous()) {
        for (VariableElement capturedVarElement :
             captureFields.getCaptureFields(
                 BindingConverter.getTypeElement(typeBinding.getTypeDeclaration()))) {
          IVariableBinding capturedVarBinding = (IVariableBinding) BindingConverter.unwrapElement(
              capturedVarElement);
          ITypeBinding targetType = getElementType(capturedVarBinding.getType());
          if (!targetType.isPrimitive() && !whitelist.containsType(targetType)
              && !ElementUtil.isWeakReference(capturedVarElement)) {
            TypeNode target = allTypes.get(targetType.getKey());
            if (target != null) {
              addEdge(Edge.newCaptureEdge(
                  type, allTypes.get(targetType.getKey()), capturedVarBinding));
            }
          }
        }
      }
    }
  }

  private void runTarjans() {
    Set<TypeNode> seedTypes = edges.keySet();
    if (blacklist != null) {
      seedTypes = Sets.newHashSet(seedTypes);
      Iterator<TypeNode> it = seedTypes.iterator();
      while (it.hasNext()) {
        if (!blacklist.containsType(it.next().getTypeBinding())) {
          it.remove();
        }
      }
    }
    List<List<TypeNode>> stronglyConnectedComponents =
        Tarjans.getStronglyConnectedComponents(edges, seedTypes);
    for (List<TypeNode> component : stronglyConnectedComponents) {
      handleStronglyConnectedComponent(makeSubgraph(edges, component));
    }
  }

  private void handleStronglyConnectedComponent(SetMultimap<TypeNode, Edge> subgraph) {
    // Make sure to find at least one cycle for each type in the SCC.
    Set<TypeNode> unusedTypes = Sets.newHashSet(subgraph.keySet());
    while (!unusedTypes.isEmpty()) {
      TypeNode root = Iterables.getFirst(unusedTypes, null);
      assert root != null;
      List<Edge> cycle = runDijkstras(subgraph, root);
      if (shouldAddCycle(cycle)) {
        cycles.add(cycle);
      }
      for (Edge e : cycle) {
        unusedTypes.remove(e.getOrigin());
      }
    }
  }

  private boolean shouldAddCycle(List<Edge> cycle) {
    if (blacklist == null) {
      return true;
    }
    for (Edge e : cycle) {
      if (blacklist.containsType(e.getOrigin().getTypeBinding())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Runs a version of Dijkstra's algorithm to find a tight cycle in the given
   * strongly connected component.
   */
  private List<Edge> runDijkstras(SetMultimap<TypeNode, Edge> graph, TypeNode root) {
    Map<TypeNode, Edge> backlinks = new HashMap<>();
    Set<TypeNode> visited = new HashSet<>();
    List<TypeNode> toVisit = Lists.newArrayList(root);
    outer: while (true) {
      List<TypeNode> visitNext = new ArrayList<>();
      for (TypeNode source : toVisit) {
        visited.add(source);
        for (Edge e : graph.get(source)) {
          TypeNode target = e.getTarget();
          if (!visited.contains(target)) {
            visitNext.add(target);
            backlinks.put(target, e);
          } else if (target.equals(root)) {
            backlinks.put(root, e);
            break outer;
          }
        }
      }
      toVisit = visitNext;
    }
    List<Edge> cycle = new ArrayList<>();
    TypeNode curNode = root;
    while (!curNode.equals(root) || cycle.size() == 0) {
      Edge nextEdge = backlinks.get(curNode);
      cycle.add(nextEdge);
      curNode = nextEdge.getOrigin();
    }
    return Lists.newArrayList(Lists.reverse(cycle));
  }

  private static SetMultimap<TypeNode, Edge> makeSubgraph(
      SetMultimap<TypeNode, Edge> graph, Collection<TypeNode> vertices) {
    SetMultimap<TypeNode, Edge> subgraph = HashMultimap.create();
    for (TypeNode type : vertices) {
      for (Edge e : graph.get(type)) {
        if (vertices.contains(e.getTarget())) {
          subgraph.put(type, e);
        }
      }
    }
    return subgraph;
  }
}
