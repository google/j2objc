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

import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import java.io.IOException;
import java.util.List;

/**
 * Preprocesses each input file in the batch.
 */
public class InputFilePreprocessor {

  private final JdtParser parser;

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

  private void processRegularSource(ProcessingContext input) throws IOException {
    if (Options.shouldMapHeaders()) {
      InputFile file = input.getFile();
      String source = FileUtil.readFile(file);
      CompilationUnit compilationUnit = parser.parseWithoutBindings(file.getUnitName(), source);
      if (compilationUnit != null) {
        addHeaderMapping(input, compilationUnit);
      }
    }
  }

  private void addHeaderMapping(ProcessingContext input, CompilationUnit compilationUnit) {
    Options.getHeaderMappings().put(
        FileUtil.getQualifiedMainTypeName(input.getFile(), compilationUnit),
        input.getGenerationUnit().getOutputPath() + ".h");
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
          String previousVal = Options.addPackagePrefix(key, val);
          if (previousVal != null && !previousVal.equals(val)) {
            ErrorUtil.error(String.format("Package %s has name %s defined in file %s, but"
                + "is already named %s", key, val, file.getPath(), previousVal));
          }
        }
      }
    }
  }
}
