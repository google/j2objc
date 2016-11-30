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
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.file.JavacFileManager;
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
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
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
  }

  @Override
  public CompilationUnit parse(InputFile file) {
    String source = null;
    try {
      source = FileUtil.readFile(file);
      return parse(file.getUnitName(), source, true);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      return null;
    }
  }

  @Override
  public CompilationUnit parse(String mainType, String path, String source) {
    return parse(path, source, true);
  }

  private CompilationUnit parse(String path, String source, boolean resolve) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (JavacFileManager fileManager = getFileManager(compiler, diagnostics)) {
      List<JavaFileObject> inputFiles = new ArrayList<>();
      inputFiles.add(MemoryFileObject.createJavaFile(path, source));
      JavacTaskImpl task = (JavacTaskImpl) compiler.getTask(
          null, fileManager, diagnostics, getJavacOptions(), null, inputFiles);
      JavacEnvironment parserEnv = new JavacEnvironment(task.getContext());
      JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) task.parse().iterator().next();
      if (resolve) {
        task.analyze();
      }
      processDiagnostics(diagnostics);
      return TreeConverter.convertCompilationUnit(new TranslationEnvironment(parserEnv), unit);
    } catch (IOException e) {
      ErrorUtil.fatalError(e, path);
    }
    return null;
  }

  private JavacFileManager getFileManager(JavaCompiler compiler,
      DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
    Charset charset = encoding != null ? Charset.forName(encoding) : Options.getCharset();
    JavacFileManager fileManager = (JavacFileManager)
        compiler.getStandardFileManager(diagnostics, null, charset);
    addPaths(StandardLocation.CLASS_PATH, Options.getClassPathEntries(), fileManager);
    addPaths(StandardLocation.SOURCE_PATH, Options.getSourcePathEntries(), fileManager);
    addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, Options.getProcessorPathEntries(),
        fileManager);
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
        Lists.newArrayList(Options.getOutputDirectory()));
    fileManager.setLocation(StandardLocation.SOURCE_OUTPUT,
        Lists.newArrayList(FileUtil.createTempDir("annotations")));
    return fileManager;
  }

  private void addPaths(Location location, List<String> paths, JavacFileManager fileManager)
      throws IOException {
    List<File> filePaths = new ArrayList<>();
    for (String path : paths) {
      filePaths.add(new File(path));
    }
    fileManager.setLocation(location, filePaths);
  }

  private List<String> getJavacOptions() {
    List<String> options = new ArrayList<>();

    options.add("-Xbootclasspath:" + makePathString(Options.getBootClasspath()));
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
    String lintArgument = Options.lintArgument();
    if (lintArgument != null) {
      options.add(lintArgument);
    }

    return options;
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
    try (JavacFileManager fileManager = getFileManager(compiler, diagnostics)) {
      List<String> javacOptions = getJavacOptions();

      Iterable<? extends JavaFileObject> fileObjects = fileManager
          .getJavaFileObjectsFromFiles(files);
      JavacTaskImpl task = (JavacTaskImpl) compiler.getTask(null, fileManager, diagnostics,
          javacOptions, null, fileObjects);
      JavacEnvironment parserEnv = new JavacEnvironment(task.getContext());
      TranslationEnvironment env = new TranslationEnvironment(parserEnv);

      List<CompilationUnitTree> units = new ArrayList<>();
      try {
        for (CompilationUnitTree unit : task.parse()) {
          units.add(unit);
        }
        task.analyze();
      } catch (IOException e) {
        // Error listener will report errors.
      }

      processDiagnostics(diagnostics);

      for (CompilationUnitTree ast : units) {
        com.google.devtools.j2objc.ast.CompilationUnit unit = TreeConverter
            .convertCompilationUnit(env, (JCTree.JCCompilationUnit) ast);
        handler.handleParsedUnit(unit.getSourceFilePath(), unit);
      }
    } catch (IOException e) {
      ErrorUtil.fatalError(e, "javac file manager error");
    }
  }

  private void processDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
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
  }

  @Override
  public org.eclipse.jdt.core.dom.CompilationUnit parseWithoutBindings(String unitName,
      String source) {
    // TODO(tball): replace with line below after API updated to return j2objc's CompilationUnit.
    throw new AssertionError("not implemented");
    //return parse(unitName, source, false);
  }

}
