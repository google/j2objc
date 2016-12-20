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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
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

  public JavacParser(Options options){
    super(options);
  }

  @Override
  public void setEnableDocComments(boolean enable) {
    // Ignore, since JavacTaskImpl always enables them.
  }

  @Override
  public CompilationUnit parse(InputFile file) {
    String source = null;
    try {
      source = FileUtil.readFile(file, options.getCharset());
      return parse(null, file.getUnitName(), source);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      return null;
    }
  }

  @Override
  public CompilationUnit parse(String mainType, String path, String source) {
    try {
      JavacEnvironment parserEnv = createEnvironment(path, source);
      JavacTaskImpl task = parserEnv.task();
      JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) task.parse().iterator().next();
      task.analyze();
      processDiagnostics(parserEnv.diagnostics());
      return TreeConverter.convertCompilationUnit(options, parserEnv, unit);
    } catch (IOException e) {
      ErrorUtil.fatalError(e, path);
    }
    return null;
  }

  private JavacEnvironment createEnvironment(String path, String source) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    JavacFileManager fileManager = getFileManager(compiler, diagnostics);
    List<JavaFileObject> inputFiles = new ArrayList<>();
    inputFiles.add(MemoryFileObject.createJavaFile(path, source));
    JavacTaskImpl task = (JavacTaskImpl) compiler.getTask(
        null, fileManager, diagnostics, getJavacOptions(), null, inputFiles);
    return new JavacEnvironment(task, fileManager, diagnostics);
  }

  private JavacFileManager getFileManager(JavaCompiler compiler,
      DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
    Charset charset = encoding != null ? Charset.forName(encoding) : options.getCharset();
    JavacFileManager fileManager = (JavacFileManager)
        compiler.getStandardFileManager(diagnostics, null, charset);
    addPaths(StandardLocation.CLASS_PATH, classpathEntries, fileManager);
    addPaths(StandardLocation.SOURCE_PATH, sourcepathEntries, fileManager);
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
        Lists.newArrayList(options.getOutputDirectory()));
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
    List<String> javacOptions = new ArrayList<>();

    javacOptions.add("-Xbootclasspath:" + makePathString(Options.getBootClasspath()));
    if (encoding != null) {
      javacOptions.add("-encoding");
      javacOptions.add(encoding);
    }
    SourceVersion javaLevel = options.getSourceVersion();
    if (javaLevel != null) {
      javacOptions.add("-source");
      javacOptions.add(javaLevel.flag());
      javacOptions.add("-target");
      javacOptions.add(javaLevel.flag());
    }
    String lintArgument = options.lintArgument();
    if (lintArgument != null) {
      javacOptions.add(lintArgument);
    }
    javacOptions.add("-proc:none");

    // TODO(tball): this should be in the FileManager, but adding it there
    // causes annotations to be processed twice, causing a "duplicate unit"
    // error. Defining the path as a javac flag works correctly, however.
    if (options.getProcessorPathEntries().size() > 0) {
      javacOptions.add("-processorpath");
      javacOptions.add(makePathString(options.getProcessorPathEntries()));
    }
    return javacOptions;
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
      JavacEnvironment env = new JavacEnvironment(task, fileManager, diagnostics);

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
      if (ErrorUtil.errorCount() == 0) {
        for (CompilationUnitTree ast : units) {
          com.google.devtools.j2objc.ast.CompilationUnit unit = TreeConverter
              .convertCompilationUnit(options, env, (JCTree.JCCompilationUnit) ast);
          handler.handleParsedUnit(unit.getSourceFilePath(), unit);
        }
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
  public Parser.ParseResult parseWithoutBindings(InputFile file, String source) {
    String path = file.getUnitName();
    try {
      JavacEnvironment parserEnv = createEnvironment(path, source);
      JavacTaskImpl task = parserEnv.task();
      JCTree.JCCompilationUnit unit = (JCTree.JCCompilationUnit) task.parse().iterator().next();
      processDiagnostics(parserEnv.diagnostics());
      return new JavacParseResult(file, source, unit);
    } catch (IOException e) {
      ErrorUtil.fatalError(e, path);
    }
    return null;
  }


  private static class JavacParseResult implements Parser.ParseResult {
    private final InputFile file;
    private String source;
    private final JCTree.JCCompilationUnit unit;

    private JavacParseResult(InputFile file, String source, JCTree.JCCompilationUnit unit) {
      this.file = file;
      this.source = source;
      this.unit = unit;
    }

    @Override
    public void stripIncompatibleSource() {
      source = JavacJ2ObjCIncompatibleStripper.strip(source, unit);
    }

    @Override
    public String getSource() {
      return source;
    }

    @Override
    public String mainTypeName() {
      String qualifiedName = FileUtil.getMainTypeName(file);
      JCExpression packageDecl = unit.pid;
      if (packageDecl != null) {
        qualifiedName = packageDecl.toString() + "." + qualifiedName;
      }
      return qualifiedName;
    }

    @Override
    public String toString() {
      return unit.toString();
    }
  }
}
