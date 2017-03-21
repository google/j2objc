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
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph representing possible references between Java types.
 *
 * @author Keith Stanger
 */
public class ReferenceGraph {

  private SetMultimap<TypeNode, Edge> edges = HashMultimap.create();

  public Set<TypeNode> getNodes() {
    return Collections.unmodifiableSet(edges.keySet());
  }

  public Set<Edge> getEdges(TypeNode node) {
    return Collections.unmodifiableSet(edges.get(node));
  }

  public void addEdge(Edge e) {
    edges.put(e.getOrigin(), e);
  }

  public List<ReferenceGraph> getStronglyConnectedComponents(Set<TypeNode> seedNodes) {
    List<List<TypeNode>> componentNodesList =
        Tarjans.getStronglyConnectedComponents(edges, seedNodes);
    List<ReferenceGraph> components = new ArrayList<>();
    for (List<TypeNode> componentNodes : componentNodesList) {
      components.add(getSubgraph(componentNodes));
    }
    return components;
  }

  /**
   * Runs a version of Dijkstra's algorithm to find a tight cycle in the given
   * strongly connected component.
   */
  public List<Edge> findShortestCycle(TypeNode root) {
    Map<TypeNode, Edge> backlinks = new HashMap<>();
    Set<TypeNode> visited = new HashSet<>();
    List<TypeNode> toVisit = Lists.newArrayList(root);
    outer: while (true) {
      List<TypeNode> visitNext = new ArrayList<>();
      for (TypeNode source : toVisit) {
        visited.add(source);
        for (Edge e : edges.get(source)) {
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

  private ReferenceGraph getSubgraph(Collection<TypeNode> vertices) {
    ReferenceGraph subgraph = new ReferenceGraph();
    for (TypeNode type : vertices) {
      for (Edge e : edges.get(type)) {
        if (vertices.contains(e.getTarget())) {
          subgraph.addEdge(e);
        }
      }
    }
    return subgraph;
  }

  public void print(PrintStream printStream) {
    ArrayList<TypeNode> typeNodes = new ArrayList<>(edges.keySet());
    Collections.sort(typeNodes, (a, b) -> a.getName().compareTo(b.getName()));
    for (TypeNode typeNode : typeNodes) {
      ArrayList<Edge> outgoingEdges = new ArrayList<>(edges.get(typeNode));
      Collections.sort(
          outgoingEdges, (a, b) -> a.getTarget().getName().compareTo(b.getTarget().getName()));
      printStream.println("class: " + typeNode);
      for (Edge e : outgoingEdges) {
        printStream.println("       " + e);
      }
    }
  }
}
