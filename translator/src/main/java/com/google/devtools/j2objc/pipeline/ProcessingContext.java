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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;

/**
 * Contains all the state required to preprocess and eventually compile a source
 * file into an AST.
 */
public class ProcessingContext {

  // The file may change during preprocessing, but we want to save the path of
  // the original source for documentation and line directives.
  private final String originalSourcePath;
  private InputFile file;  // mutable.
  private final GenerationUnit generationUnit;

  public ProcessingContext(InputFile file, GenerationUnit generationUnit) {
    originalSourcePath = file.getPath();
    this.file = file;
    this.generationUnit = generationUnit;
  }

  public static ProcessingContext fromFile(InputFile file, Options options) {
    return new ProcessingContext(file, GenerationUnit.newSingleFileUnit(file, options));
  }

  public String getOriginalSourcePath() {
    return originalSourcePath;
  }

  public InputFile getFile() {
    return file;
  }

  public void setFile(InputFile newFile) {
    file = newFile;
  }

  public GenerationUnit getGenerationUnit() {
    return generationUnit;
  }
}
