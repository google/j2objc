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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.PathClassLoader;
import com.google.devtools.j2objc.util.SourceVersion;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.tree.JCTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
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
  
  private JavacFileManager fileManager; 

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
      source = options.fileUtil().readFile(file);
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

  private JavacFileManager getFileManager(JavaCompiler compiler,
      DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
    fileManager = (JavacFileManager)
        compiler.getStandardFileManager(diagnostics, null, options.fileUtil().getCharset());
    addPaths(StandardLocation.CLASS_PATH, classpathEntries, fileManager);
    addPaths(StandardLocation.SOURCE_PATH, sourcepathEntries, fileManager);
    addPaths(StandardLocation.PLATFORM_CLASS_PATH, options.getBootClasspath(), fileManager);
    List<String> processorPathEntries = options.getProcessorPathEntries();
    if (!processorPathEntries.isEmpty()) {
      addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, processorPathEntries, fileManager);
    }
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
        Lists.newArrayList(options.fileUtil().getOutputDirectory()));
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

  private List<String> getJavacOptions(boolean processAnnotations) {
    List<String> javacOptions = new ArrayList<>();
    String encoding = options.fileUtil().getFileEncoding();
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
    if (processAnnotations) {
      javacOptions.add("-proc:only");
    } else {
      javacOptions.add("-proc:none");
    }
    return javacOptions;
  }

  @Override
  public void parseFiles(Collection<String> paths, Handler handler, SourceVersion sourceVersion) {
    List<File> files = new ArrayList<>();
    for (String path : paths) {
      files.add(new File(path));
    }
    try {
      JavacEnvironment env = createEnvironment(files, null, false);
      List<CompilationUnitTree> units = new ArrayList<>();
      for (CompilationUnitTree unit : env.task().parse()) {
        units.add(unit);
      }
      env.task().analyze();
      processDiagnostics(env.diagnostics());

      if (ErrorUtil.errorCount() == 0) {
        for (CompilationUnitTree ast : units) {
          com.google.devtools.j2objc.ast.CompilationUnit unit = TreeConverter
              .convertCompilationUnit(options, env, (JCTree.JCCompilationUnit) ast);
          processDiagnostics(env.diagnostics());
          handler.handleParsedUnit(unit.getSourceFilePath(), unit);
        }
      }
    } catch (IOException e) {
      ErrorUtil.fatalError(e, "javac file manager error");
    }
  }

  // Creates a javac environment from a memory source.
  private JavacEnvironment createEnvironment(String path, String source) throws IOException {
    List<JavaFileObject> inputFiles = new ArrayList<>();
    inputFiles.add(MemoryFileObject.createJavaFile(path, source));
    return createEnvironment(Collections.emptyList(), inputFiles, false);
  }

  // Creates a javac environment from a collection of files and/or file objects.
  private JavacEnvironment createEnvironment(List<File> files, List<JavaFileObject> fileObjects,
      boolean processAnnotations) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    JavacFileManager fileManager = getFileManager(compiler, diagnostics);
    List<String> javacOptions = getJavacOptions(processAnnotations);
    if (fileObjects == null) {
      fileObjects = new ArrayList<>();
    }
    for (JavaFileObject jfo : fileManager.getJavaFileObjectsFromFiles(files)) {
      fileObjects.add(jfo);
    }
    JavacTaskImpl task = (JavacTaskImpl) compiler.getTask(null, fileManager, diagnostics,
        javacOptions, null, fileObjects);
    return new JavacEnvironment(task, fileManager, diagnostics);
  }

  private void processDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      ErrorUtil.parserDiagnostic(diagnostic);
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


  @Override
  public ProcessingResult processAnnotations(Iterable<String> fileArgs,
      List<ProcessingContext> inputs) {
    final List<ProcessingContext> generatedInputs = Lists.newArrayList();
    PathClassLoader loader = new PathClassLoader(options.fileUtil().getClassPathEntries());
    loader.addPaths(options.getProcessorPathEntries());
    Iterator<Processor> serviceIterator = ServiceLoader.load(Processor.class, loader).iterator();
    if (serviceIterator.hasNext()) {
      List<File> inputFiles = new ArrayList<>();
      for (ProcessingContext input : inputs) {
        inputFiles.add(new File(input.getFile().getAbsolutePath()));
      }
      try {
        JavacEnvironment env = createEnvironment(inputFiles, null, true);
        List<CompilationUnitTree> units = new ArrayList<>();
        for (CompilationUnitTree unit : env.task().parse()) {
          units.add(unit);
        }
        // JavacTaskImpl.enter() parses and runs annotation processing, but
        // not type checking and attribution (that's done by analyze()).
        env.task().enter();
        processDiagnostics(env.diagnostics());
        // The source output directory is created and set in createEnvironment().
        File sourceOutputDirectory =
            env.fileManager().getLocation(StandardLocation.SOURCE_OUTPUT).iterator().next();
        collectGeneratedInputs(sourceOutputDirectory, "", generatedInputs);
        return new JavacProcessingResult(generatedInputs, sourceOutputDirectory);
      } catch (IOException e) {
        ErrorUtil.fatalError(e, "javac file manager error");
      }
    }
    // No annotation processors on classpath, or processing errors reported.
    return new JavacProcessingResult(generatedInputs, null);
  }
  
  @Override
  public void close() throws IOException {
    if (fileManager != null) {
      try {
        fileManager.close();
      } finally {
        fileManager = null;
      }
    }
  }

  private void collectGeneratedInputs(
      File dir, String currentRelativePath, List<ProcessingContext> inputs) {
    assert dir.exists() && dir.isDirectory();
    for (File f : dir.listFiles()) {
      String relativeName = currentRelativePath + File.separatorChar + f.getName();
      if (f.isDirectory()) {
        collectGeneratedInputs(f, relativeName, inputs);
      } else {
        if (f.getName().endsWith(".java")) {
          inputs.add(ProcessingContext.fromFile(
              new RegularInputFile(f.getPath(), relativeName), options));
        }
      }
    }
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
      ExpressionTree packageDecl = unit.getPackageName();
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

  private static class JavacProcessingResult implements Parser.ProcessingResult {
    private final List<ProcessingContext> generatedSources;
    private final File sourceOutputDirectory;

    public JavacProcessingResult(List<ProcessingContext> generatedSources,
        File sourceOutputDirectory) {
      this.generatedSources = generatedSources;
      this.sourceOutputDirectory = sourceOutputDirectory;
    }

    @Override
    public List<ProcessingContext> getGeneratedSources() {
      return generatedSources;
    }

    @Override
    public File getSourceOutputDirectory() {
      return sourceOutputDirectory;
    }

  }
}
