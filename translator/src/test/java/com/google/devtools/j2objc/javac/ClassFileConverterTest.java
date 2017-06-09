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

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.CompilationUnit;
import java.io.IOException;

/**
 * Tests for {@link ClassFileConverter}.
 *
 * Note: classfile conversion is experimental and not supported.
 */
public class ClassFileConverterTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    options.setTranslateClassfiles(true);
  }

  public void testMarkerInterface() throws IOException {
    String source = "package foo.bar; interface Test {}";
    CompilationUnit srcUnit = compileType("foo.bar.Test", source);
    CompilationUnit classfileUnit = compileAsClassFile("foo.bar.Test", source);
    assertEqualASTs(srcUnit, classfileUnit);
  }
}
