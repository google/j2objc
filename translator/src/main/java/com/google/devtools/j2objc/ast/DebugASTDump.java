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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
    File outputFile = new File(Options.getOutputDirectory(), relativeOutputPath);

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
    return dumper.sb.toString();
  }

  @Override
  public boolean preVisit(TreeNode node) {
    sb.printIndent();
    sb.print(node.getClass().getSimpleName());
    sb.indent();
    return true;
  }

  @Override
  public void postVisit(TreeNode node) {
    sb.newline();
    sb.unindent();
  }
}
