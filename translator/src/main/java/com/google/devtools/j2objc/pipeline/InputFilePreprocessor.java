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

package com.google.devtools.j2objc.pipeline;

import com.google.common.io.Files;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Preprocesses each input file in the batch.
 */
public class InputFilePreprocessor {

  private static final Logger logger = Logger.getLogger(InputFilePreprocessor.class.getName());

  private final JdtParser parser;
  private File strippedSourcesDir;

  public InputFilePreprocessor(JdtParser parser) {
    this.parser = parser;
  }

  public void processInputs(Iterable<ProcessingContext> inputs) {
    for (ProcessingContext input : inputs) {
      processInput(input);
    }
  }

  private void processInput(ProcessingContext input) {
    try {
      if (input.getFile().getUnitName().endsWith("package-info.java")) {
        processPackageInfoSource(input);
      } else {
        processRegularSource(input);
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }

  public File getStrippedSourcesDir() {
    return strippedSourcesDir;
  }

  private File getCreatedStrippedSourcesDir() {
    if (strippedSourcesDir == null) {
      strippedSourcesDir = Files.createTempDir();
      logger.finest("Created temp dir for stripped sources: " + strippedSourcesDir.getPath());
    }
    return strippedSourcesDir;
  }

  private void processRegularSource(ProcessingContext input) throws IOException {
    InputFile file = input.getFile();
    String source = FileUtil.readFile(file);
    boolean doIncompatibleStripping = source.contains("J2ObjCIncompatible");
    if (!(Options.shouldMapHeaders() || doIncompatibleStripping)) {
      // No need to parse.
      return;
    }
    CompilationUnit compilationUnit = parser.parseWithoutBindings(file.getUnitName(), source);
    if (compilationUnit == null) {
      // An error occured, reported by the JdtParser.
      return;
    }
    String qualifiedName = FileUtil.getQualifiedMainTypeName(file, compilationUnit);
    if (Options.shouldMapHeaders()) {
      Options.getHeaderMap().put(qualifiedName, input.getGenerationUnit().getOutputPath() + ".h");
    }
    if (doIncompatibleStripping) {
      String newSource = J2ObjCIncompatibleStripper.strip(source, compilationUnit);
      File strippedDir = getCreatedStrippedSourcesDir();
      String relativePath = qualifiedName.replace('.', File.separatorChar) + ".java";
      File strippedFile = new File(strippedDir, relativePath);
      Files.createParentDirs(strippedFile);
      Files.write(newSource, strippedFile, Options.getCharset());
      input.setFile(new RegularInputFile(strippedFile.getPath(), relativePath));
    }
  }

  private void processPackageInfoSource(ProcessingContext input) throws IOException {
    InputFile file = input.getFile();
    String source = FileUtil.readFile(file);
    CompilationUnit compilationUnit = parser.parseWithBindings(file.getUnitName(), source);
    if (compilationUnit != null) {
      extractPackagePrefix(file, compilationUnit);
    }
  }

  private void extractPackagePrefix(InputFile file, CompilationUnit unit) {
    // We should only reach here if it's a package-info.java file.
    assert file.getUnitName().endsWith("package-info.java");
    @SuppressWarnings("unchecked")
    List<Annotation> annotations = (List<Annotation>) unit.getPackage().annotations();
    for (Annotation annotation : annotations) {
      // getFullyQualifiedName() might not actually return a fully qualified name.
      String name = annotation.getTypeName().getFullyQualifiedName();
      if (name.endsWith("ObjectiveCName")) {
        // Per Eclipse docs, binding resolution can be a resource hog.
        if (annotation.resolveAnnotationBinding().getAnnotationType().getQualifiedName().equals(
            ObjectiveCName.class.getCanonicalName())) {
          String key = unit.getPackage().getName().getFullyQualifiedName();
          String val = (String) ((SingleMemberAnnotation) annotation).getValue()
              .resolveConstantExpressionValue();
          Options.addPackagePrefix(key, val);
        }
      }
    }
  }
}
