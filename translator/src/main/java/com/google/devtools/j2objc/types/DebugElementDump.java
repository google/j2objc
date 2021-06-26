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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.util.ElementUtil;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner8;

/**
 * Dumps a specified AST node to show the graphs of its elements. Unlike
 * {@link com.google.devtools.j2objc.ast.DebugASTPrinter}, the output does
 * not look anything like Java source. It's instead intended to aid translator
 * debugging and testing.
 *
 * @author Tom Ball
 */
public class DebugElementDump extends ElementScanner8<Void, Void> {

  private SourceBuilder sb = new SourceBuilder(false);

  public static String dump(TreeNode node) {
    Element e = TreeUtil.getDeclaredElement(node);
    if (e == null) {
      if (node instanceof Name) {
        e = TreeUtil.getVariableElement((Name) node);
      }
    }
    if (e == null) {
      return "";
    }
    DebugElementDump dumper = new DebugElementDump();
    dumper.visit(e);
    dumper.sb.newline();
    return dumper.sb.toString();
  }

  public static String dump(Element e) {
    DebugElementDump dumper = new DebugElementDump();
    dumper.visit(e);
    dumper.sb.newline();
    return dumper.sb.toString();
  }

  @Override
  public Void visitPackage(PackageElement e, Void unused) {
    printElement("PackageElement", e);
    return DEFAULT_VALUE;
  }

  @Override
  public Void visitType(TypeElement e, Void unused) {
    printElement("TypeElement", e, e.getKind().name());
    return printEnclosedElements(e.getEnclosedElements());
  }

  @Override
  public Void visitVariable(VariableElement e, Void unused) {
    printElement("VariableElement", e, e.getKind().name());
    return DEFAULT_VALUE;
  }

  @Override
  public Void visitExecutable(ExecutableElement e, Void unused) {
    printElement("ExecutableElement", e, e.getKind().name());
    return printEnclosedElements(e.getEnclosedElements());
  }

  @Override
  public Void visitTypeParameter(TypeParameterElement e, Void unused) {
    printElement("TypeParameterElement", e);
    return printEnclosedElements(e.getEnclosedElements());
  }

  private void printElement(String kind, Element element) {
    printElement(kind, element, null);
  }

  private void printElement(String kind, Element element, String details) {
    sb.printIndent();
    sb.print(kind);
    if (details != null) {
      sb.print("(");
      sb.print(details);
      sb.print(")");
    }
    sb.print(": ");
    sb.println(ElementUtil.getName(element));
  }

  private Void printEnclosedElements(Iterable<? extends Element> iterable) {
    sb.indent();
    for (Element e : iterable) {
      scan(e);
    }
    sb.unindent();
    return DEFAULT_VALUE;
  }
}
