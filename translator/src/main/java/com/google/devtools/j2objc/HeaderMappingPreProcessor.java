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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.util.JdtParser;

/**
 * Reads source files and extracts header mappings between translated header files and associated
 * Java type declarations.
 * This processor should be invoked on any GenerationUnits with a set output path,
 * and ignores GenerationUnits that don't have any. In particular, it should be invoked whenever
 * any command line flags are set that set output paths dependent on input paths,
 * such as --use-source-files.
 *
 * Note that the reason we need to process the type information of all the Java source files
 * first, instead of doing it iteratively during the translation phase for each file, is that we may
 * have cross-referencing among the given Java source files in a single J2ObjC invocation.
 */
public class HeaderMappingPreProcessor extends FileProcessor {
  public HeaderMappingPreProcessor(JdtParser parser) {
    super(parser);
  }

  @Override
  protected void processCompiledGenerationUnit(GenerationUnit generationUnit) {
    for (CompilationUnit compilationUnit : generationUnit.getCompilationUnits()) {
      for (AbstractTypeDeclaration decl : compilationUnit.getTypes()) {
        String name = decl.getTypeBinding().getQualifiedName();
        Options.getHeaderMappings().put(name, generationUnit.getOutputPath() + ".h");
      }
    }
  }
}
