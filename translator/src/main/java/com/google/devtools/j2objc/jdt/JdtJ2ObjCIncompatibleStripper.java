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

package com.google.devtools.j2objc.jdt;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Removes elements annotated as "J2ObjCIncompatible".
 */
class JdtJ2ObjCIncompatibleStripper extends ASTVisitor {

  private final TreeSet<ASTNode> nodesToStrip = Sets.newTreeSet(START_POS_COMPARATOR);
  private final Map<String, ImportDeclaration> unusedImports = Maps.newHashMap();
  private final Map<String, ImportDeclaration> unusedStaticImports = Maps.newHashMap();

  private static final Comparator<ASTNode> START_POS_COMPARATOR = new Comparator<ASTNode>() {
    @Override
    public int compare(ASTNode a, ASTNode b) {
      return a.getStartPosition() - b.getStartPosition();
    }
  };

  private JdtJ2ObjCIncompatibleStripper(CompilationUnit unit) {
    @SuppressWarnings("unchecked")
    List<ImportDeclaration> imports = unit.imports();
    for (ImportDeclaration importNode : imports) {
      String name = getLastComponent(importNode.getName());
      if (importNode.isStatic()) {
        unusedStaticImports.put(name, importNode);
      } else {
        unusedImports.put(name, importNode);
      }
    }
  }

  public static String strip(String source, CompilationUnit unit) {
    JdtJ2ObjCIncompatibleStripper stripper = new JdtJ2ObjCIncompatibleStripper(unit);
    unit.accept(stripper);
    return stripper.stripSource(source);
  }

  private boolean isJ2ObjCIncompatible(IExtendedModifier modifier) {
    if (modifier instanceof Annotation) {
      String name = getLastComponent(((Annotation) modifier).getTypeName());
      return name.equals("J2ObjCIncompatible");
    }
    return false;
  }

  private String getFirstComponent(Name name) {
    if (name.isSimpleName()) {
      return ((SimpleName) name).getIdentifier();
    } else {
      return getFirstComponent(((QualifiedName) name).getQualifier());
    }
  }

  private String getLastComponent(Name name) {
    if (name.isSimpleName()) {
      return ((SimpleName) name).getIdentifier();
    } else {
      return ((QualifiedName) name).getName().getIdentifier();
    }
  }

  private boolean visitBodyDeclaration(BodyDeclaration node) {
    @SuppressWarnings("unchecked")
    List<IExtendedModifier> modifiers = node.modifiers();
    for (IExtendedModifier modifier : modifiers) {
      if (isJ2ObjCIncompatible(modifier)) {
        nodesToStrip.add(node);
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean visit(ImportDeclaration node) {
    // Prevents marking this import as "used".
    return false;
  }

  @Override
  public boolean visit(SimpleType node) {
    unusedImports.remove(getFirstComponent(node.getName()));
    return false;
  }

  @Override
  public void endVisit(SimpleName node) {
    unusedImports.remove(node.getIdentifier());
    unusedStaticImports.remove(node.getIdentifier());
  }

  @Override
  public boolean visit(QualifiedName node) {
    unusedImports.remove(getFirstComponent(node));
    unusedStaticImports.remove(getFirstComponent(node));
    return false;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitBodyDeclaration(node);
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    return visitBodyDeclaration(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitBodyDeclaration(node);
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    return visitBodyDeclaration(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    return visitBodyDeclaration(node);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitBodyDeclaration(node);
  }

  private String stripSource(String source) {
    nodesToStrip.addAll(unusedImports.values());
    nodesToStrip.addAll(unusedStaticImports.values());
    StringBuilder sb = new StringBuilder();
    int currentIdx = 0;
    for (ASTNode node : nodesToStrip) {
      int startPos = node.getStartPosition();
      if (startPos < currentIdx) {
        continue;
      }
      int endPos = startPos + node.getLength();
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
}
