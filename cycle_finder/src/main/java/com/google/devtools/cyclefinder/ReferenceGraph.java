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
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.translate.OuterReferenceResolver;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Collection;
import java.util.Iterator;
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
  private final NameList whitelist;
  private final NameList blacklist;
  private SetMultimap<String, Edge> edges = HashMultimap.create();
  private List<List<Edge>> cycles = Lists.newArrayList();

  public ReferenceGraph(TypeCollector typeCollector, NameList whitelist, NameList blacklist) {
    this.allTypes = typeCollector.getTypes();
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
    if (!e.getOrigin().getKey().equals(e.getTarget().getKey())) {
      edges.put(e.getOrigin().getKey(), e);
    }
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
            && !BindingUtil.isWeakReference(field)) {
          addEdge(Edge.newFieldEdge(type, field));
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
    SetMultimap<String, String> subtypes = HashMultimap.create();
    for (ITypeBinding type : allTypes.values()) {
      collectSubtypes(type.getKey(), type, subtypes);
    }
    for (String type : allTypes.keySet()) {
      for (Edge e : ImmutableList.copyOf(edges.get(type))) {
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
          addEdge(Edge.newSubtypeEdge(e, allTypes.get(subtype)));
        }
      }
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
      ITypeBinding superclass = type.getSuperclass();
      while (superclass != null) {
        for (Edge e : edges.get(superclass.getKey())) {
          addEdge(Edge.newSuperclassEdge(e, type, superclass));
        }
        superclass = superclass.getSuperclass();
      }
    }
  }

  private void addOuterClassEdges() {
    for (ITypeBinding type : allTypes.values()) {
      if (OuterReferenceResolver.needsOuterReference(type.getTypeDeclaration())
          && !BindingUtil.hasNamedAnnotation(type, "WeakOuter")) {
        ITypeBinding declaringType = type.getDeclaringClass();
        if (declaringType != null && !whitelist.containsType(declaringType)
            && !whitelist.hasOuterForType(type)) {
          addEdge(Edge.newOuterClassEdge(type, declaringType));
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
          if (!targetType.isPrimitive() && !whitelist.containsType(targetType)
              && !BindingUtil.isWeakReference(capturedVar)) {
            addEdge(Edge.newCaptureEdge(type, capturedVar));
          }
        }
      }
    }
  }

  private void runTarjans() {
    Set<String> seedTypes = edges.keySet();
    if (blacklist != null) {
      seedTypes = Sets.newHashSet(seedTypes);
      Iterator<String> it = seedTypes.iterator();
      while (it.hasNext()) {
        if (!blacklist.containsType(allTypes.get(it.next()))) {
          it.remove();
        }
      }
    }
    List<List<String>> stronglyConnectedComponents =
        Tarjans.getStronglyConnectedComponents(edges, seedTypes);
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
      if (shouldAddCycle(cycle)) {
        cycles.add(cycle);
      }
      for (Edge e : cycle) {
        unusedTypes.remove(e.getOrigin().getKey());
      }
    }
  }

  private boolean shouldAddCycle(List<Edge> cycle) {
    if (blacklist == null) {
      return true;
    }
    for (Edge e : cycle) {
      if (blacklist.containsType(e.getOrigin())) {
        return true;
      }
    }
    return false;
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
    while (!curNode.equals(root) || cycle.size() == 0) {
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
