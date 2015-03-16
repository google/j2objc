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

package com.google.devtools.j2objc.annotations;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * A test processor for annotation processing. This is the processor in Processor.jar.
 */
@SupportedAnnotationTypes("com.google.j2objc.annotations.ObjectiveCName")
public class J2ObjCTestProcessor extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement type: annotations) {
      Writer w = null;
      try {
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile("ProcessingResult");
        w = sourceFile.openWriter();
        w.write("public class ProcessingResult {"
            + "  public String getResult() {"
            + "    return \"" + type.getSimpleName() + "\";"
            + "  }"
            + "}");
      } catch (IOException e) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR, "Could not process annotations");
        e.printStackTrace();
        return false;
      } finally {
        if (w != null) {
          try {
            w.close();
          } catch (IOException e) {}
        }
      }
    }

    return false;
  }
}
