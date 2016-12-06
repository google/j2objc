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

package com.google.devtools.j2objc.docs;

import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.IOException;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Prints a markdown compliant list of our AST nodes by ancestry, with Objects not in the Eclipse
 * AST bolded. Provides a quick visual of inheritance and J2ObjC AST nodes for project docs.
 *
 * @author Seth Kirby
 */
public class ASTClassInfoPrinter {
  private static Set<String> astLookup;
  private static SortedSetMultimap<String, String> tree;

  private static void walkSuperclassHierarchy(Class<?> node) {
    if (node.getSuperclass() == null || tree.containsKey(node.getSimpleName())) {
      return;
    } else {
      walkSuperclassHierarchy(node.getSuperclass());
      tree.put(node.getSuperclass().getSimpleName(), node.getSimpleName());
    }
  }

  private static void printClassHierarchy(String node, String indent) {
    if (astLookup.contains(node)) {
      System.out.println(indent + "- " + node);
    } else {
      System.out.println(indent + "- **" + node + "**");
    }
    for (String childNode : tree.get(node)) {
      printClassHierarchy(childNode, "  " + indent);
    }
  }

  public static void main(String... args) {
    // Clear saved state for new calls.
    tree = TreeMultimap.create();
    astLookup = Sets.newHashSet();
    ClassPath cp = null;
    try {
      cp = ClassPath.from(ClassLoader.getSystemClassLoader());
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      System.exit(1);
    }
    for (ClassInfo c : cp.getTopLevelClasses("org.eclipse.jdt.core.dom")){
      astLookup.add(c.getSimpleName());
    }
    for (ClassInfo ci : cp.getTopLevelClasses("com.google.devtools.j2objc.ast")) {
      // Ignore package-info and JUnit tests.
      if (ci.getSimpleName().equals("package-info") || TestCase.class.isAssignableFrom(ci.load())) {
        continue;
      }
      walkSuperclassHierarchy(ci.load());
    }
    // Print hierarchy descending from Object.
    printClassHierarchy("Object", "");
  }
}
