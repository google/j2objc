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

import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of Tarjan's strongly connected components algorithm.
 * http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
 */
class Tarjans {

  private final SetMultimap<TypeNode, Edge> edges;
  private final Set<TypeNode> seedTypes;
  private int vIndex = 0;
  // In case of performance issues, consider a data structure with faster .contains().
  private ArrayList<Vertex> stack = new ArrayList<>();
  private Map<TypeNode, Vertex> vertices = new HashMap<>();
  private List<List<TypeNode>> stronglyConnectedComponents = new ArrayList<>();

  private Tarjans(SetMultimap<TypeNode, Edge> edges, Set<TypeNode> seedTypes) {
    this.edges = edges;
    this.seedTypes = seedTypes;
  }

  public static List<List<TypeNode>> getStronglyConnectedComponents(
      SetMultimap<TypeNode, Edge> edges, Set<TypeNode> seedTypes) {
    Tarjans tarjans = new Tarjans(edges, seedTypes);
    tarjans.run();
    return tarjans.stronglyConnectedComponents;
  }

  private void run() {
    for (TypeNode type : seedTypes) {
      Vertex v = getVertex(type);
      if (v.index == -1) {
        visit(v);
      }
    }
  }

  private void visit(Vertex v) {
    v.index = v.lowlink = vIndex++;
    stack.add(v);

    for (Edge edge : edges.get(v.type)) {
      Vertex w = getVertex(edge.getTarget());
      if (w.index == -1) {
        visit(w);
        v.lowlink = Math.min(v.lowlink, w.lowlink);
      } else if (stack.contains(w)) {
        v.lowlink = Math.min(v.lowlink, w.index);
      }
    }

    if (v.lowlink == v.index) {
      int idx = stack.indexOf(v);
      assert idx >= 0;
      List<Vertex> stronglyConnected = stack.subList(idx, stack.size());
      if (stronglyConnected.size() > 1) {
        List<TypeNode> stronglyConnectedTypes = new ArrayList<>(stronglyConnected.size());
        for (Vertex ver : stronglyConnected) {
          stronglyConnectedTypes.add(ver.type);
        }
        stronglyConnectedComponents.add(stronglyConnectedTypes);
      }
      stronglyConnected.clear();  // Removes the sublist from stack.
    }
  }

  private Vertex getVertex(TypeNode type) {
    Vertex v = vertices.get(type);
    if (v == null) {
      v = new Vertex(type);
      vertices.put(type, v);
    }
    return v;
  }

  private static class Vertex {
    private int index = -1;
    private int lowlink = -1;
    private TypeNode type;

    private Vertex(TypeNode type) {
      this.type = type;
    }

    public String toString() {
      return type.toString();
    }
  }
}
