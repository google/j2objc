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
 * Tests for {@link StaticVarRewriter}.
 *
 * @author Keith Stanger
 */
public class StaticVarRewriterTest extends GenerationTest {

  public void testRewriteChildOfQualifiedName() throws IOException {
    String translation = translateSourceFile(
        "class Test { static Test test = new Test(); Object obj = new Object();"
        + "static class Other { void test() { test.obj.toString(); test.obj.toString(); } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "[nil_chk(((Test *) nil_chk([Test test]))->obj_) description];",
        "[[Test test]->obj_ description];");
  }
}
