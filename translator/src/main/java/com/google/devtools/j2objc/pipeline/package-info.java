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

/**
 * Contains J2ObjC's high-level logic for managing the various steps that occur
 * during translation. The following processing steps occur in this order:
 * - Annotation processing. The AnnotationPreProcessor searches the classpath
 *   for annotation processors and does a java compile step to run annotation
 *   processing and collect the generated .java files.
 * - J2ObjC preprocessing. The InputFilePreprocessor parses each input Java
 *   source file without resolving bindings. This step is used to generate
 *   header mappings and package prefix mappings (from package-info.java files).
 * - J2ObjC processing and generation. The final step parses each input file
 *   with bindings and converts the tree to our own AST structure. The resulting
 *   tree is passed through each of the mutation passes (in the translate
 *   package) then generated into a header and an implementation file. If
 *   --build-closure is specified, then each tree is scanned for dependencies
 *   which are queued to go through the same processing.
 */
package com.google.devtools.j2objc.pipeline;
