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

  public void testTypeNeedsReflection() throws IOException {
    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.FULL) package foo; "
        + "import com.google.j2objc.annotations.ReflectionSupport;", "foo/package-info.java");
    CompilationUnit unit = translateType("foo.A", "package foo; public class A {}");
    TranslationUtil translationUtil = unit.getEnv().translationUtil();
    assertTrue(translationUtil.needsReflection(unit.getTypes().get(0)));

    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.NATIVE_ONLY) package bar; "
        + "import com.google.j2objc.annotations.ReflectionSupport;", "bar/package-info.java");
    unit = translateType("bar.A", "package bar; public class A {}");
    translationUtil = unit.getEnv().translationUtil();
    assertFalse(translationUtil.needsReflection(unit.getTypes().get(0)));

    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.NATIVE_ONLY) package baz; "
        + "import com.google.j2objc.annotations.ReflectionSupport;", "baz/package-info.java");
    unit = translateType("baz.A",
        "package baz; import com.google.j2objc.annotations.ReflectionSupport; "
        + "@ReflectionSupport(ReflectionSupport.Level.FULL) public class A {}");
    translationUtil = unit.getEnv().translationUtil();
    assertTrue(translationUtil.needsReflection(unit.getTypes().get(0)));

    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.FULL) package qux; "
        + "import com.google.j2objc.annotations.ReflectionSupport;", "qux/package-info.java");
    unit = translateType("qux.A",
        "package qux; import com.google.j2objc.annotations.ReflectionSupport; "
        + "@ReflectionSupport(ReflectionSupport.Level.NATIVE_ONLY) public class A {}");
    translationUtil = unit.getEnv().translationUtil();
    assertFalse(translationUtil.needsReflection(unit.getTypes().get(0)));

    options.setStripReflection(true);
    unit = translateType("quux.A", "package quux; public class A {}");
    translationUtil = unit.getEnv().translationUtil();
    assertFalse(translationUtil.needsReflection(unit.getTypes().get(0)));

    options.setStripReflection(false);
    unit = translateType("quuz.A", "package quuz; public class A {}");
    translationUtil = unit.getEnv().translationUtil();
    assertTrue(translationUtil.needsReflection(unit.getTypes().get(0)));
  }

  public void testJUnit3TestKeepsReflection() {
    options.setStripReflection(true);
    String source =
        "package foo; "
            + "import junit.framework.TestSuite; "
            + "public class A extends TestSuite {} ";
    CompilationUnit unit = translateType("foo.A", source);
    TranslationUtil translationUtil = unit.getEnv().translationUtil();
    assertTrue(translationUtil.needsReflection(unit.getTypes().get(0)));
  }

  public void testJUnit4TestKeepsReflection() {
    options.setStripReflection(true);
    String source =
        "package foo; "
            + "import org.junit.runner.RunWith; "
            + "import org.junit.runners.JUnit4; "
            + "@RunWith(JUnit4.class) public class A {} ";
    CompilationUnit unit = translateType("foo.A", source);
    TranslationUtil translationUtil = unit.getEnv().translationUtil();
    assertTrue(translationUtil.needsReflection(unit.getTypes().get(0)));
  }
}
