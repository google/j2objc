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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.CompilationUnit;

import java.io.IOException;

/**
 * UnitTests for the {@link BindingUtil} class.
 *
 * @author Keith Stanger
 */
public class BindingUtilTest extends GenerationTest {

  public void testIsRuntimeAnnotation() throws IOException {
    // SuppressWarnings is a source-level annotation.
    CompilationUnit unit = translateType("Example", "@SuppressWarnings(\"test\") class Example {}");
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    Annotation annotation = decl.getAnnotations().get(0);
    assertFalse(BindingUtil.isRuntimeAnnotation(annotation.getAnnotationBinding()));

    // Deprecated is a runtime annotation..
    unit = translateType("Example", "@Deprecated class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertTrue(BindingUtil.isRuntimeAnnotation(annotation.getAnnotationBinding()));
  }
}
