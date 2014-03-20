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

package com.google.devtools.j2objc;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Abstract base class for processing a list of files. Supports .jar and
 * @manifest files.
 *
 * @author Tom Ball, Keith Stanger
 */
abstract class FileProcessor {

  private static final Logger logger = Logger.getLogger(FileProcessor.class.getName());

  public void processFiles(Iterable<String> files) {
    for (String file : files) {
      processFile(file);
    }
  }

  public void processFile(String file) {
    if (file.startsWith("@")) {
      processManifestFile(file.substring(1));
    } else {
      processSourceFile(file);
    }
  }

  private void processManifestFile(String filename) {
    if (filename.isEmpty()) {
      ErrorUtil.error("no @ file specified");
      return;
    }
    File f = new File(filename);
    if (!f.exists()) {
      ErrorUtil.error("no such file: " + filename);
      return;
    }
    try {
      String fileList = Files.toString(f, Options.getCharset());
      String[] files = fileList.split("\\s+");  // Split on any whitespace.
      for (String file : files) {
        processSourceFile(file);
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  private void processSourceFile(String filename) {
    logger.finest("processing " + filename);
    if (filename.endsWith(".java")) {
      processJavaFile(filename);
    } else if (filename.endsWith(".jar")) {
      processJarFile(filename);
    } else {
      ErrorUtil.error("Unknown file type: " + filename);
    }
  }

  protected void processJavaFile(String filename) {
    File f = getFileOrNull(filename);
    if (f != null) {
      processFoundJavaFile(filename, f);
      return;
    }
    if (f == null) {
      for (String pathEntry : Options.getSourcePathEntries()) {
        if (pathEntry.endsWith(".jar")) {
          String source = getJarEntryOrNull(pathEntry, filename);
          if (source != null) {
            processSource(filename, source);
            return;
          }
        } else {
          f = getFileOrNull(pathEntry + File.separatorChar + filename);
          if (f != null) {
            processFoundJavaFile(filename, f);
            return;
          }
        }
      }
    }
    ErrorUtil.error("No such file: " + filename);
  }

  protected void processFoundJavaFile(String filename, File file) {
    try {
      processSource(filename, Files.toString(file, Options.getCharset()));
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  private String getJarEntryOrNull(String jarFile, String path) {
    File f = new File(jarFile);
    if (!f.exists() || !f.isFile()) {
      return null;
    }
    try {
      ZipFile zfile = new ZipFile(f);
      try {
        ZipEntry entry = zfile.getEntry(path);
        if (entry != null) {
          Reader in = new InputStreamReader(zfile.getInputStream(entry));
          return CharStreams.toString(in);
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
    } catch (IOException e) {
      ErrorUtil.warning(e.getMessage());
    }
    return null;
  }

  protected void processJarFile(String filename) {
    File f = new File(filename);
    if (!f.exists() || !f.isFile()) {
      ErrorUtil.error("No such file: " + filename);
      return;
    }
    try {
      ZipFile zfile = new ZipFile(f);
      try {
        Enumeration<? extends ZipEntry> enumerator = zfile.entries();
        while (enumerator.hasMoreElements()) {
          ZipEntry entry = enumerator.nextElement();
          String path = entry.getName();
          if (path.endsWith(".java")) {
            Reader in = new InputStreamReader(zfile.getInputStream(entry));
            processSource(path, CharStreams.toString(in));
          }
        }
      } finally {
        zfile.close();  // Also closes input stream.
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  protected void processSource(String path, String source) {
    logger.finest("parsing " + path);
    TimeTracker ticker = getTicker(path);
    ticker.push();
    ASTParser parser = ASTParser.newParser(AST.JLS4);
    parser.setCompilerOptions(Options.getCompilerOptions());
    parser.setSource(source.toCharArray());
    parser.setResolveBindings(true);
    setPaths(parser);
    parser.setUnitName(path);
    CompilationUnit unit = (CompilationUnit) parser.createAST(null);

    checkCompilationErrors(path, unit);

    ticker.tick("Parsing file");

    ErrorUtil.setCurrentFileName(path);
    NameTable.initialize(unit);
    Types.initialize(unit);
    processUnit(path, source, unit, ticker);
    NameTable.cleanup();
    Types.cleanup();

    ticker.pop();
    ticker.tick("Total processing time");
    ticker.printResults(System.out);
  }

  protected TimeTracker getTicker(String name) {
    if (logger.isLoggable(Level.FINEST)) {
      return TimeTracker.start(name);
    } else {
      return TimeTracker.noop();
    }
  }

  protected abstract void processUnit(
      String path, String source, CompilationUnit unit, TimeTracker ticker);

  protected static String getRelativePath(String path, CompilationUnit unit) {
    int index = path.lastIndexOf(File.separatorChar);
    String name = index >= 0 ? path.substring(index + 1) : path;
    PackageDeclaration pkg = unit.getPackage();
    if (pkg == null) {
      return name;
    } else {
      return pkg.getName().getFullyQualifiedName().replace('.', File.separatorChar)
          + File.separatorChar + name;
    }
  }

  private static void setPaths(ASTParser parser) {
    // Add existing boot classpath after declared path, so that core files
    // referenced, but not being translated, are included.  This only matters
    // when compiling the JRE emulation library sources.
    List<String> fullClasspath = Lists.newArrayList();
    String[] classpathEntries = Options.getClassPathEntries();
    for (int i = 0; i < classpathEntries.length; i++) {
      fullClasspath.add(classpathEntries[i]);
    }
    String bootclasspath = Options.getBootClasspath();
    for (String path : bootclasspath.split(":")) {
      // JDT requires that all path elements exist and can hold class files.
      File f = new File(path);
      if (f.exists() && (f.isDirectory() || path.endsWith(".jar"))) {
        fullClasspath.add(path);
      }
    }
    parser.setEnvironment(fullClasspath.toArray(new String[0]), Options.getSourcePathEntries(),
        Options.getFileEncodings(), true);

    // Workaround for ASTParser.setEnvironment() bug, which ignores its
    // last parameter.  This has been fixed in the Eclipse post-3.7 Java7
    // branch.
    try {
      Field field = parser.getClass().getDeclaredField("bits");
      field.setAccessible(true);
      int bits = field.getInt(parser);
      // Turn off CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH
      bits &= ~0x20;
      field.setInt(parser, bits);
    } catch (Exception e) {
      // should never happen, since only the one known class is manipulated
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void checkCompilationErrors(String filename, CompilationUnit unit) {
    List<IProblem> errors = Lists.newArrayList();
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError()) {
        if (((problem.getID() & IProblem.ImportRelated) != 0) && Options.ignoreMissingImports()) {
          continue;
        } else {
          ErrorUtil.error(String.format(
              "%s:%s: %s", filename, problem.getSourceLineNumber(), problem.getMessage()));
        }
      }
    }
  }

  private static File getFileOrNull(String fileName) {
    File f = new File(fileName);
    return f.exists() ? f : null;
  }
}
