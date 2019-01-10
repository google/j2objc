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
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Removes elements annotated as "J2ObjCIncompatible".
 */
class JavacJ2ObjCIncompatibleStripper extends TreeScanner<Void, Void> {
  private final CompilationUnitTree unit;
  private final SourcePositions sourcePositions;

  private final TreeSet<Tree> nodesToStrip =
      Sets.newTreeSet(Comparator.comparingInt(this::startPosition));
  private final Map<String, ImportTree> unusedImports = Maps.newHashMap();
  private final Map<String, ImportTree> unusedStaticImports = Maps.newHashMap();

  public static String strip(
      String source, CompilationUnitTree unit, SourcePositions sourcePositions) {
    JavacJ2ObjCIncompatibleStripper stripper =
        new JavacJ2ObjCIncompatibleStripper(unit, sourcePositions);
    unit.accept(stripper, null);
    return stripper.stripSource(source);
  }

  private JavacJ2ObjCIncompatibleStripper(
      CompilationUnitTree unit, SourcePositions sourcePositions) {
    this.unit = unit;
    this.sourcePositions = sourcePositions;
  }

  // TreeScanner methods.

  @Override
  public Void visitCompilationUnit(CompilationUnitTree unit, Void unused) {
    scan(unit.getImports(), null);
    scan(unit.getPackageAnnotations(), null);
    scan(unit.getTypeDecls(), null);
    return null;
  }

  @Override
  public Void visitImport(ImportTree node, Void aVoid) {
    String name = getLastComponent(node.getQualifiedIdentifier());
    if (node.isStatic()) {
      unusedStaticImports.put(name, node);
    } else {
      unusedImports.put(name, node);
    }
    return null;
  }

  @Override
  public Void visitAnnotatedType(AnnotatedTypeTree node, Void unused) {
    if (checkAnnotations(node.getAnnotations(), node)) {
      super.visitAnnotatedType(node, null);
    }
    return null;
  }

  @Override
  public Void visitClass(ClassTree node, Void unused) {
    if (checkAnnotations(node.getModifiers().getAnnotations(), node)) {
      super.visitClass(node, null);
    }
    return null;
  }

  @Override
  public Void visitIdentifier(IdentifierTree node, Void unused) {
    String name = node.getName().toString();
    unusedImports.remove(name);
    unusedStaticImports.remove(name);
    return null;
  }

  @Override
  public Void visitMethod(MethodTree node, Void unused) {
    if (checkAnnotations(node.getModifiers().getAnnotations(), node)) {
      super.visitMethod(node, null);
    }
    return null;
  }

  @Override
  public Void visitVariable(VariableTree node, Void unused) {
    if (checkAnnotations(node.getModifiers().getAnnotations(), node)) {
      super.visitVariable(node, null);
    }
    return null;
  }

  // Private methods.

  /**
   * Checks for any J2ObjCIncompatible annotations. Returns whether
   * the caller should to continue scanning this node.
   */
  private boolean checkAnnotations(List<? extends AnnotationTree> annotations, Tree node) {
    for (AnnotationTree annotation : annotations) {
      if (isJ2ObjCIncompatible(annotation)) {
        nodesToStrip.add(node);
        return false;
      }
    }
    return true;
  }

  private int startPosition(Tree node) {
    return (int) sourcePositions.getStartPosition(unit, node);
  }

  private int endPosition(Tree node) {
    return (int) sourcePositions.getEndPosition(unit, node);
  }

  private String stripSource(String source) {
    nodesToStrip.addAll(unusedImports.values());
    nodesToStrip.addAll(unusedStaticImports.values());
    StringBuilder sb = new StringBuilder();
    int currentIdx = 0;
    for (Tree node : nodesToStrip) {
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

  private boolean isJ2ObjCIncompatible(AnnotationTree modifier) {
    String name = getLastComponent(modifier.getAnnotationType());
    return name.equals("J2ObjCIncompatible");
  }

  private String getLastComponent(Tree name) {
    switch (name.getKind()) {
      case IDENTIFIER:
        return ((IdentifierTree) name).getName().toString();
      case MEMBER_SELECT:
        return ((MemberSelectTree) name).getIdentifier().toString();
      default:
        return "";
    }
  }
}
