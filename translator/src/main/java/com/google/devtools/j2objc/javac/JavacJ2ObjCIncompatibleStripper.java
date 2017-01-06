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

package com.google.devtools.j2objc.javac;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Removes elements annotated as "J2ObjCIncompatible".
 */
class JavacJ2ObjCIncompatibleStripper extends TreeScanner {
  private final EndPosTable endPositions;

  private final TreeSet<JCTree> nodesToStrip = Sets.newTreeSet(START_POS_COMPARATOR);
  private final Map<String, JCImport> unusedImports = Maps.newHashMap();
  private final Map<String, JCImport> unusedStaticImports = Maps.newHashMap();

  private static final Comparator<JCTree> START_POS_COMPARATOR = new Comparator<JCTree>() {
    @Override
    public int compare(JCTree a, JCTree b) {
      return a.getStartPosition() - b.getStartPosition();
    }
  };

  public static String strip(String source, JCCompilationUnit unit) {
    JavacJ2ObjCIncompatibleStripper stripper = new JavacJ2ObjCIncompatibleStripper(unit);
    unit.accept(stripper);
    return stripper.stripSource(source);
  }

  private JavacJ2ObjCIncompatibleStripper(JCCompilationUnit unit) {
    endPositions = unit.endPositions;
  }

  // TreeScanner methods.

  @Override
  public void visitTopLevel(JCCompilationUnit unit) {
    for (JCImport importNode : unit.getImports()) {
      String name = getLastComponent(importNode.getQualifiedIdentifier());
      if (importNode.isStatic()) {
        unusedStaticImports.put(name, importNode);
      } else {
        unusedImports.put(name, importNode);
      }
    }
    scan(unit.getPackageAnnotations());
    for (JCTree tree : unit.getTypeDecls()) {
      scan(tree);
    }
  }

  @Override
  public void visitAnnotatedType(JCAnnotatedType node) {
    if (checkAnnotations(node.getAnnotations(), node)) {
      super.visitAnnotatedType(node);
    }
  }

  @Override
  public void visitClassDef(JCClassDecl node) {
    if (checkAnnotations(node.getModifiers().getAnnotations(), node)) {
      super.visitClassDef(node);
    }
  }

  @Override
  public void visitIdent(JCIdent node) {
    String name = node.getName().toString();
    unusedImports.remove(name);
    unusedStaticImports.remove(name);
  }

  @Override
  public void visitMethodDef(JCMethodDecl node) {
    if (checkAnnotations(node.getModifiers().getAnnotations(), node)) {
      super.visitMethodDef(node);
    }
  }

  @Override
  public void visitVarDef(JCVariableDecl node) {
    if (checkAnnotations(node.getModifiers().getAnnotations(), node)) {
      super.visitVarDef(node);
    }
  }

  // Private methods.

  /**
   * Checks for any J2ObjCIncompatible annotations. Returns whether
   * the caller should to continue scanning this node.
   */
  private boolean checkAnnotations(List<JCAnnotation> annotations, JCTree node) {
    for (JCAnnotation annotation : annotations) {
      if (isJ2ObjCIncompatible(annotation)) {
        nodesToStrip.add(node);
        return false;
      }
    }
    return true;
  }

  private int startPosition(JCTree node) {
    return TreeInfo.getStartPos(node);
  }

  private int endPosition(JCTree node) {
    return TreeInfo.getEndPos(node, endPositions);
  }

  private String stripSource(String source) {
    nodesToStrip.addAll(unusedImports.values());
    nodesToStrip.addAll(unusedStaticImports.values());
    StringBuilder sb = new StringBuilder();
    int currentIdx = 0;
    for (JCTree node : nodesToStrip) {
      int startPos = startPosition(node);
      if (startPos < currentIdx) {
        continue;
      }
      int endPos = endPosition(node);
      sb.append(source.substring(currentIdx, startPos));
      // Preserve newlines from the stripped node so that we can add line
      // directives consistent with the original source file.
      for (int i = startPos; i < endPos; i++) {
        if (source.charAt(i) == '\n') {
          sb.append('\n');
        }
      }
      currentIdx = endPos;
    }
    sb.append(source.substring(currentIdx));
    return sb.toString();
  }

  private boolean isJ2ObjCIncompatible(JCAnnotation modifier) {
    String name = getLastComponent(modifier.annotationType);
    return name.equals("J2ObjCIncompatible");
  }

  private String getLastComponent(JCTree name) {
    switch (name.getKind()) {
      case IDENTIFIER:
        return ((JCIdent) name).getName().toString();
      case MEMBER_SELECT:
        return ((JCFieldAccess) name).getIdentifier().toString();
      default:
        return "";
    }
  }

  static class Node implements Comparable<Node> {
    private final JCTree tree;
    private final int startPos;

    Node(JCTree tree) {
      this.tree = tree;
      this.startPos = TreeInfo.getStartPos(tree);
    }

    @Override
    public int compareTo(Node other) {
      return Integer.compare(this.startPos, other.startPos);
    }

    JCTree getTree() {
      return tree;
    }

    int getStartpos() {
      return startPos;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Node && tree.equals(((Node) obj).tree);
    }

    @Override
    public int hashCode() {
      return tree.hashCode();
    }
  }
}
