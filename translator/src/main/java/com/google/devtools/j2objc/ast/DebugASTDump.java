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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.lang.model.type.TypeMirror;

/**
 * Dumps a specified AST to show a specified tree's graph. Unlike
 * {@link DebugASTPrinter}, the output does not look like the original
 * Java source. It's instead intended to aid translator debugging and
 * testing.
 *
 * @author Tom Ball
 */
public class DebugASTDump extends TreeVisitor {

  private SourceBuilder sb = new SourceBuilder(false);

  /**
   * Dumps a compilation unit to a file. The output file's path is the
   * same as where translated files get written, but with an "ast" suffix.
   */
  public static void dumpUnit(CompilationUnit unit) {
    String relativeOutputPath = unit.getMainTypeName().replace('.', '/') + ".ast";
    File outputFile = new File(
        unit.getEnv().options().fileUtil().getOutputDirectory(), relativeOutputPath);
    outputFile.getParentFile().mkdirs();

    try (FileOutputStream fout = new FileOutputStream(outputFile);
        OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8")) {
      out.write(dump(unit));
    } catch (IOException e) {
      ErrorUtil.fatalError(e, outputFile.getPath());
    }
  }

  /**
   * Dumps an AST node as a string.
   */
  public static String dump(TreeNode node) {
    DebugASTDump dumper = new DebugASTDump();
    node.accept(dumper);
    dumper.sb.newline();
    return dumper.toString();
  }

  @Override
  public boolean preVisit(TreeNode node) {
    if (sb.length() > 0) {
      sb.newline();
    }
    sb.printIndent();
    sb.print(node.getClass().getSimpleName());
    sb.print(" (line:" + node.getLineNumber() + " pos:" + node.getStartPosition()
        + " len:" + node.getLength() + ")");
    sb.indent();
    return true;
  }

  @Override
  public void postVisit(TreeNode node) {
    sb.unindent();
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(ArrayType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(FieldAccess node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(IntersectionType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(Javadoc node) {
    return true;
  }

  @Override
  public boolean visit(MemberValuePair node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(NameQualifiedType node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(ParameterizedType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(PrimitiveType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(QualifiedName node) {
    printName(node);
    return true;
  }

  @Override
  public boolean visit(QualifiedType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(SimpleName node) {
    printName(node);
    return true;
  }

  @Override
  public boolean visit(SimpleType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(TagElement node) {
    String tagName = node.getTagName();
    sb.print(' ');
    sb.print(tagName != null ? tagName : "null");
    return true;
  }

  @Override
  public boolean visit(TextElement node) {
    sb.print(' ');
    sb.print(node.getText());
    return true;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(TypeMethodReference node) {
    printName(node.getName());
    return true;
  }

  @Override
  public boolean visit(UnionType node) {
    printType(node.getTypeMirror());
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    printName(node.getName());
    return true;
  }

  private void printName(Name name) {
    if (name != null) {
      sb.print(": ");
      sb.print(name.toString());
    }
  }

  private void printType(TypeMirror type) {
    sb.print(": ");
    sb.print(type.toString());
  }

  @Override
  public String toString() {
    return sb.toString();
  }
}
