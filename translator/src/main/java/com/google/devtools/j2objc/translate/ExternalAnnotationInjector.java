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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.util.ExternalAnnotations;
import scenelib.annotations.el.AScene;

/** Adds external annotations to the AST. */
public final class ExternalAnnotationInjector extends UnitTreeVisitor {

  // An annotated scene represents the annotations on a set of Java classes and packages.
  private final AScene scene;

  public ExternalAnnotationInjector(CompilationUnit unit, ExternalAnnotations externalAnnotations) {
    super(unit);
    scene = externalAnnotations.getScene();
  }
}
