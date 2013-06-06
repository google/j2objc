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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.translate.OuterReferenceResolver;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the graph of possible references between types and searches for
 * possible cycles.
 *
 * @author Keith Stanger
 */
public class ReferenceGraph {

  private final Map<String, ITypeBinding> allTypes;
  private final Whitelist whitelist;
  private SetMultimap<String, Edge> edges = HashMultimap.create();
  private List<List<Edge>> cycles = Lists.newArrayList();

  public ReferenceGraph(TypeCollector typeCollector, Whitelist whitelist) {
    this.allTypes = typeCollector.getTypes();
    this.whitelist = whitelist;
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
    // TODO(user): Capture edges should be added before subtype edges.
    addAnonymousClassCaptureEdges();
  }

  private void addFieldEdges() {
    for (ITypeBinding type : allTypes.values()) {
      for (IVariableBinding field : type.getDeclaredFields()) {
        ITypeBinding fieldType = getElementType(field.getType());
        if (!whitelist.containsField(field)
            && !whitelist.containsType(fieldType)
            && !fieldType.isPrimitive()
            && !Modifier.isStatic(field.getModifiers())
            // Exclude self-referential fields. (likely linked DS or delegate pattern)
            && !type.isAssignmentCompatible(fieldType)
            && !isWeak(field)) {
          edges.put(type.getKey(), Edge.newFieldEdge(type, field));
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

  private boolean hasWildcard(ITypeBinding type) {
    if (type.isWildcardType()) {
      return true;
    }
    if (type.isParameterizedType()) {
      for (ITypeBinding arg : type.getTypeArguments()) {
        if (hasWildcard(arg)) {
          return true;
        }
      }
    }
    return false;
  }

  private Set<String> findAssignableTypes(ITypeBinding type) {
    Set<String> assignableTypes = Sets.newHashSet();
    for (ITypeBinding assignable : allTypes.values()) {
      if (assignable.isAssignmentCompatible(type)) {
        assignableTypes.add(assignable.getKey());
      }
    }
    return assignableTypes;
  }

  private void addSubtypeEdges() {
    SetMultimap<String, String> subtypes = HashMultimap.create();
    for (ITypeBinding type : allTypes.values()) {
      collectSubtypes(type.getKey(), type, subtypes);
      // The internal implementation of isAssignmentCompatible caches all
      // results and will quickly fill up heap space, so we must use it
      // sparingly.
      if (hasWildcard(type)) {
        subtypes.putAll(type.getKey(), findAssignableTypes(type));
      }
    }
    for (String type : allTypes.keySet()) {
      Set<Edge> newEdges = Sets.newHashSet();
      for (Edge e : edges.get(type)) {
        Set<String> targetSubtypes = subtypes.get(e.getTarget().getKey());
        Set<String> whitelistKeys = Sets.newHashSet();
        IVariableBinding field = e.getField();
        for (String subtype : targetSubtypes) {
          ITypeBinding subtypeBinding = allTypes.get(subtype);
          if ((field != null && field.isField()
               && whitelist.isWhitelistedTypeForField(field, subtypeBinding))
              || whitelist.containsType(subtypeBinding)) {
            whitelistKeys.add(subtype);
            whitelistKeys.addAll(subtypes.get(subtype));
          }
        }
        for (String subtype : Sets.difference(targetSubtypes, whitelistKeys)) {
          newEdges.add(Edge.newSubtypeEdge(e, allTypes.get(subtype)));
        }
      }
      edges.putAll(type, newEdges);
    }
  }

  private void collectSubtypes(
      String originalType, ITypeBinding type, Multimap<String, String> subtypes) {
    for (ITypeBinding interfaze : type.getInterfaces()) {
      subtypes.put(interfaze.getKey(), originalType);
      collectSubtypes(originalType, interfaze, subtypes);
    }
    if (type.getSuperclass() != null) {
      subtypes.put(type.getSuperclass().getKey(), originalType);
      collectSubtypes(originalType, type.getSuperclass(), subtypes);
    }
  }

  private void addSuperclassEdges() {
    for (ITypeBinding type : allTypes.values()) {
      Set<Edge> newEdges = Sets.newHashSet();
      ITypeBinding superclass = type.getSuperclass();
      while (superclass != null) {
        for (Edge e : edges.get(superclass.getKey())) {
          newEdges.add(Edge.newSuperclassEdge(e, type, superclass));
        }
        superclass = superclass.getSuperclass();
      }
      edges.putAll(type.getKey(), newEdges);
    }
  }

  private void addOuterClassEdges() {
    for (ITypeBinding type : allTypes.values()) {
      if (OuterReferenceResolver.needsOuterReference(type.getTypeDeclaration())
          && !isWeakOuter(type)) {
        ITypeBinding declaringType = type.getDeclaringClass();
        if (declaringType != null && !whitelist.containsType(declaringType)) {
          edges.put(type.getKey(), Edge.newOuterClassEdge(type, declaringType));
        }
      }
    }
  }

  private void addAnonymousClassCaptureEdges() {
    for (ITypeBinding type : allTypes.values()) {
      if (type.isAnonymous()) {
        for (IVariableBinding capturedVar :
             OuterReferenceResolver.getCapturedVars(type.getTypeDeclaration())) {
          ITypeBinding targetType = getElementType(capturedVar.getType());
          if (!targetType.isPrimitive() && !whitelist.containsType(targetType)) {
            edges.put(type.getKey(), Edge.newCaptureEdge(type, capturedVar));
          }
        }
      }
    }
  }

  private static boolean isWeak(IVariableBinding field) {
    for (IAnnotationBinding annotation : field.getAnnotations()) {
      String name = annotation.getAnnotationType().getQualifiedName();
      if (name.equals("com.google.j2objc.annotations.Weak")) {
        return true;
      }
    }
    return false;
  }

  private static boolean isWeakOuter(ITypeBinding type) {
    for (IAnnotationBinding annotation : type.getAnnotations()) {
      String name = annotation.getAnnotationType().getQualifiedName();
      if (name.equals("com.google.j2objc.annotations.WeakOuter")) {
        return true;
      }
    }
    return false;
  }

  private void runTarjans() {
    List<List<String>> stronglyConnectedComponents = Tarjans.getStronglyConnectedComponents(edges);
    for (List<String> component : stronglyConnectedComponents) {
      handleStronglyConnectedComponent(makeSubgraph(edges, component));
    }
  }

  private void handleStronglyConnectedComponent(SetMultimap<String, Edge> subgraph) {
    // Make sure to find at least one cycle for each type in the SCC.
    Set<String> unusedTypes = Sets.newHashSet(subgraph.keySet());
    while (!unusedTypes.isEmpty()) {
      String root = Iterables.getFirst(unusedTypes, null);
      assert root != null;
      List<Edge> cycle = runDijkstras(subgraph, root);
      cycles.add(cycle);
      for (Edge e : cycle) {
        unusedTypes.remove(e.getOrigin().getKey());
      }
    }
  }

  /**
   * Runs a version of Dijkstra's algorithm to find a tight cycle in the given
   * strongly connected component.
   */
  private List<Edge> runDijkstras(SetMultimap<String, Edge> graph, String root) {
    Map<String, Edge> backlinks = Maps.newHashMap();
    Set<String> visited = Sets.newHashSet();
    List<String> toVisit = Lists.newArrayList(root);
    outer: while (true) {
      List<String> visitNext = Lists.newArrayList();
      for (String source : toVisit) {
        visited.add(source);
        for (Edge e : graph.get(source)) {
          String target = e.getTarget().getKey();
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
    List<Edge> cycle = Lists.newArrayList();
    String curNode = root;
    while (curNode != root || cycle.size() == 0) {
      Edge nextEdge = backlinks.get(curNode);
      cycle.add(nextEdge);
      curNode = nextEdge.getOrigin().getKey();
    }
    return Lists.newArrayList(Lists.reverse(cycle));
  }

  private static SetMultimap<String, Edge> makeSubgraph(
      SetMultimap<String, Edge> graph, Collection<String> vertices) {
    SetMultimap<String, Edge> subgraph = HashMultimap.create();
    for (String type : vertices) {
      for (Edge e : graph.get(type)) {
        if (vertices.contains(e.getTarget().getKey())) {
          subgraph.put(type, e);
        }
      }
    }
    return subgraph;
  }
}
