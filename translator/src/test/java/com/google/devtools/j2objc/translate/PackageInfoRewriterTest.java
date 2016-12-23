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

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Unit tests for {@link PackageInfoRewriter}.
 *
 * @author Tim Gao
 */
public class PackageInfoRewriterTest extends GenerationTest {

  public void testReflectionSupportAnnotation() throws IOException {
    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.FULL)\n"
        + "package foo;\n"
        + "import com.google.j2objc.annotations.ReflectionSupport;", "foo/package-info.java");
    String translation = translateSourceFile("foo.package-info", "foo/package-info.m");
    assertNotInTranslation(translation, "@interface");
    assertNotInTranslation(translation, "@implementation");

    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.NATIVE_ONLY)\n"
        + "package foo;\n"
        + "import com.google.j2objc.annotations.ReflectionSupport;", "foo/package-info.java");
    translation = translateSourceFile("foo.package-info", "foo/package-info.m");
    assertNotInTranslation(translation, "@interface");
    assertNotInTranslation(translation, "@implementation");
  }
}
