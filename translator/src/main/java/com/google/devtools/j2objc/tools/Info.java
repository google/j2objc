package com.google.devtools.j2objc.tools;

import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.devtools.j2objc.util.ErrorUtil;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Set;


/**
 * Helper functions for printing AST tree.
 */
public class Info {
  private static Set<String> astLookup;
  private static TreeMultimap<String, String> tree;

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
      println(indent + "- " + node);
    } else {
      println(indent + "- **" + node + "**");
    }
    for (String childNode : tree.get(node)) {
      printClassHierarchy(childNode, "  " + indent);
    }
  }

  public static void println(Object x) {
    System.out.println(x);
  }

  /**
   * Prints a markdown compliant list of our AST nodes by ancestry, with Objects not in the Eclipse
   * AST bolded. Provides a quick visual of inheritance and J2ObjC AST nodes for output in the wiki.
   */
  public static void printASTClassInfo() {
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
