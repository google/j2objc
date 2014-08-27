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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Does a topological sort on the types declared in this unit so that supertypes
 * and interfaces end up before the types that extend or implement them.
 *
 * Should be called after inner class extraction.
 *
 * @author Keith Stanger
 */
public class TypeSorter {

  private static Multimap<String, String> findSuperTypes(Map<String, ITypeBinding> bindingMap) {
    Multimap<String, String> superTypes = HashMultimap.create();
    for (Map.Entry<String, ITypeBinding> entry : bindingMap.entrySet()) {
      String key = entry.getKey();
      ITypeBinding type = entry.getValue();
      ITypeBinding superclass = type.getSuperclass();
      if (superclass != null) {
        String superclassKey = superclass.getTypeDeclaration().getKey();
        if (bindingMap.containsKey(superclassKey)) {
          superTypes.put(key, superclassKey);
        }
      }
      for (ITypeBinding interfaze : type.getInterfaces()) {
        String interfaceKey = interfaze.getTypeDeclaration().getKey();
        if (bindingMap.containsKey(interfaceKey)) {
          superTypes.put(key, interfaceKey);
        }
      }
    }
    return superTypes;
  }

  public static void sortTypes(CompilationUnit unit) {
    List<AbstractTypeDeclaration> typeNodes = unit.getTypes();
    Map<String, AbstractTypeDeclaration> nodeMap = Maps.newHashMap();
    LinkedHashMap<String, ITypeBinding> bindingMap = Maps.newLinkedHashMap();
    for (AbstractTypeDeclaration node : typeNodes) {
      ITypeBinding typeBinding = node.getTypeBinding();
      String key = typeBinding.getKey();
      nodeMap.put(key, node);
      bindingMap.put(key, typeBinding);
    }
    Multimap<String, String> superTypes = findSuperTypes(bindingMap);

    ArrayList<String> rootTypes = Lists.newArrayListWithCapacity(typeNodes.size());
    for (String type : bindingMap.keySet()) {
      if (!superTypes.containsValue(type)) {
        rootTypes.add(type);
      }
    }

    typeNodes.clear();
    while (!rootTypes.isEmpty()) {
      String nextType = rootTypes.remove(rootTypes.size() - 1);
      typeNodes.add(0, nodeMap.get(nextType));
      for (String superType : superTypes.removeAll(nextType)) {
        if (!superTypes.containsValue(superType)) {
          rootTypes.add(superType);
        }
      }
    }
  }
}
