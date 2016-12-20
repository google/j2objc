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
import com.google.devtools.j2objc.ast.CompilationUnit;
import java.io.IOException;

/**
 * Unit tests for the {@link TranslationUtil} class.
 */
public class TranslationUtilTest extends GenerationTest {

  public void testPackageNeedsReflection() throws IOException {
    options.setStripReflection(true);
    CompilationUnit unit = compileType("foo.package-info",
        "@ReflectionSupport(value = ReflectionSupport.Level.FULL) package foo; "
        + "import com.google.j2objc.annotations.ReflectionSupport;");
    TranslationUtil translationUtil = unit.getEnv().translationUtil();
    assertTrue(translationUtil.needsReflection(unit.getPackage()));
    unit = compileType("bar.package-info",
        "@ReflectionSupport(value = ReflectionSupport.Level.NATIVE_ONLY) package foo; "
        + "import com.google.j2objc.annotations.ReflectionSupport;");
    assertFalse(translationUtil.needsReflection(unit.getPackage()));
    unit = compileType("mumble.package-info",
        "package mumble; "
        + "import com.google.j2objc.annotations.ReflectionSupport;");
    assertFalse(translationUtil.needsReflection(unit.getPackage()));
  }
}
