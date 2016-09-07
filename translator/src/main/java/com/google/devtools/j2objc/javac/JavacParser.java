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

import com.google.common.base.Joiner;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.SourceVersion;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.tree.JCTree;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Parser front-end that uses javac.
 *
 * @author Tom Ball
 */
public class JavacParser extends Parser {

  @Override
  public void setEnableDocComments(boolean enable) {
    // Ignore, since JavacTaskImpl always enables them.

    // TODO(tball): Disable doc comments with a ParserFactory subclass whose newParser()
    // method uses this flag instead of its keepDocComments argument (this argument actually
    // causes all comments to be saved, not just doc comments).
  }

  @Override
  public CompilationUnit parse(InputFile file) {
    return null;
  }

  @Override
  public CompilationUnit parse(String mainType, String path, String source) {
    return null;
  }

  private List<String> getJavacOptions() {
    List<String> options = new ArrayList<>();

    options.addAll(maybeBuildPathOption("-classpath", Options.getClassPathEntries()));
    options.addAll(maybeBuildPathOption("-sourcepath", Options.getSourcePathEntries()));
    options.addAll(maybeBuildPathOption("-processorpath", Options.getProcessorPathEntries()));
    options.add("-Xbootclasspath:" + makePathString(Options.getBootClasspath()));

    options.add("-d");
    options.add(Options.getOutputDirectory().getPath());
    if (encoding != null) {
      options.add("-encoding");
      options.add(encoding);
    }
    SourceVersion javaLevel = Options.getSourceVersion();
    if (javaLevel != null) {
      options.add("-source");
      options.add(javaLevel.flag());
      options.add("-target");
      options.add(javaLevel.flag());
    }
    // TODO(tball): turn on any specified lint warnings.

    return options;
  }

  private List<String> maybeBuildPathOption(String flag, List<String> pathEntries) {
    ArrayList<String> flags = new ArrayList<>();
    if (pathEntries != null && !pathEntries.isEmpty()) {
      flags.add(flag);
      flags.add(makePathString(pathEntries));
    }
    return flags;
  }

  private String makePathString(List<String> paths) {
    return Joiner.on(":").join(paths);
  }

  @Override
  public void parseFiles(Collection<String> paths, Handler handler, SourceVersion sourceVersion) {
    List<File> files = new ArrayList<>();
    for (String path : paths) {
      files.add(new File(path));
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    Charset charset = encoding != null ? Charset.forName(encoding) : Options.getCharset();
    StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, charset);
    List<String> javacOptions = getJavacOptions();

    Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromFiles(files);
    JavacTaskImpl task = (JavacTaskImpl) compiler.getTask(
        null, fileManager, diagnostics, javacOptions, null, fileObjects);
    JavacEnvironment env = new JavacEnvironment(nameTableFactory, task.getContext());

    List<CompilationUnitTree> units = new ArrayList<>();
    try {
      for (CompilationUnitTree unit : task.parse()) {
        units.add(unit);
      }
      task.analyze();
    } catch (IOException e) {
      // Error listener will report errors.
    }

    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      Kind kind = diagnostic.getKind();
      if (kind == Kind.ERROR) {
        ErrorUtil.error(diagnostic.getMessage(null));
      } else if (kind == Kind.MANDATORY_WARNING || kind == Kind.WARNING) {
        ErrorUtil.warning(diagnostic.getMessage(null));
      } else {
        // Ignore other diagnostic types.
      }
    }

    for (CompilationUnitTree ast : units) {
      com.google.devtools.j2objc.ast.CompilationUnit unit =
          TreeConverter.convertCompilationUnit(env, (JCTree.JCCompilationUnit) ast);
      handler.handleParsedUnit(unit.getSourceFilePath(), unit);
    }

    try {
      fileManager.close();
    } catch (IOException e) {
      ErrorUtil.fatalError(e, "failed closing javac file manager");
    }
  }

  @Override
  public org.eclipse.jdt.core.dom.CompilationUnit parseWithoutBindings(String unitName,
      String source) {
    // TODO(tball): implement after API updated to return j2objc's CompilationUnit.
    throw new AssertionError("not implemented");
  }

}
