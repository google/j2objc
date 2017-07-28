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
import com.google.j2objc.annotations.ReflectionSupport;
import java.io.IOException;

/**
 * Unit tests for the {@link PackageInfoLookup}.
 *
 * @author Tim Gao
 */
public class PackageInfoLookupTest extends GenerationTest {

  public void testFullReflectionSupportSetValue() throws IOException {
    addSourceFile("@ReflectionSupport(value = ReflectionSupport.Level.FULL) package foo;"
        + "import com.google.j2objc.annotations.ReflectionSupport;", "foo/package-info.java");
    CompilationUnit unit = translateType("foo.A", "package foo; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.FULL, packageInfoLookup.getReflectionSupportLevel("foo"));
  }

  public void testFullReflectionSupport() throws IOException {
    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.FULL) package bar;"
        + "import com.google.j2objc.annotations.*;", "bar/package-info.java");
    CompilationUnit unit = translateType("bar.A", "package bar; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.FULL, packageInfoLookup.getReflectionSupportLevel("bar"));
  }

  public void testNativeOnlyReflectionSupport() throws IOException {
    addSourceFile("@com.google.j2objc.annotations.ReflectionSupport"
        + "(com.google.j2objc.annotations.ReflectionSupport.Level.NATIVE_ONLY) package baz;",
        "baz/package-info.java");
    CompilationUnit unit = translateType("baz.A", "package baz; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.NATIVE_ONLY,
        packageInfoLookup.getReflectionSupportLevel("baz"));
  }

  // Verify that ReflectionSupport annotation can be parsed from a compiled jar file.
  public void testReflectionSupportInJarFile() throws IOException {
    String jarFilePath = getResourceAsFile("packageInfoLookupTest.jar");
    options.fileUtil().getClassPathEntries().add(jarFilePath);
    CompilationUnit unit = translateType("com.google.test.packageInfoLookupTest.A",
        "package com.google.test.packageInfoLookupTest; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.FULL,
        packageInfoLookup.getReflectionSupportLevel(unit.getPackage().getName().toString()));
  }

  public void testFullReflectionSupportSetValueCompiled() throws IOException {
    createClassFile("foo.package-info",
        "@ReflectionSupport(value = ReflectionSupport.Level.FULL) package foo;"
        + "import com.google.j2objc.annotations.ReflectionSupport;");
    removeFile("foo/package-info.java");
    CompilationUnit unit = translateType("foo.A", "package foo; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.FULL, packageInfoLookup.getReflectionSupportLevel("foo"));
  }

  public void testFullReflectionSupportCompiled() throws IOException {
    createClassFile("bar.package-info",
        "@ReflectionSupport(ReflectionSupport.Level.FULL) package bar;"
        + "import com.google.j2objc.annotations.ReflectionSupport;");
    removeFile("bar/package-info.java");
    CompilationUnit unit = translateType("bar.A", "package bar; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.FULL, packageInfoLookup.getReflectionSupportLevel("bar"));
  }

  public void testNativeOnlyReflectionSupportCompiled() throws IOException {
    createClassFile("baz.package-info",
        "@com.google.j2objc.annotations.ReflectionSupport"
        + "(com.google.j2objc.annotations.ReflectionSupport.Level.NATIVE_ONLY) package baz;");
    removeFile("baz/package-info.java");
    CompilationUnit unit = translateType("baz.A", "package baz; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertSame(ReflectionSupport.Level.NATIVE_ONLY,
        packageInfoLookup.getReflectionSupportLevel("baz"));
  }

  public void testPackageRenameCompiled() throws IOException {
    createClassFile("foo.package-info",
        "@ObjectiveCName(\"XYZ\") package foo; "
        + "import com.google.j2objc.annotations.ObjectiveCName;");
    removeFile("foo/package-info.java");
    String translation = translateSourceFile("package foo; public class A {}", "foo.A", "foo/A.h");
    assertTranslation(translation, "@interface XYZA");
  }

  // Verify that ParametersAreNonnullByDefault is not set on packages by default.
  public void testParametersAreNonnullByDefaultNotSet() throws IOException {
    CompilationUnit unit = translateType("foo.A", "package foo; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertFalse(packageInfoLookup.hasParametersAreNonnullByDefault("foo"));
  }

  public void testParametersAreNonnullByDefault() throws IOException {
    createClassFile("bar.package-info",
        "@ParametersAreNonnullByDefault package bar;"
        + "import javax.annotation.ParametersAreNonnullByDefault;");
    removeFile("bar/package-info.java");
    CompilationUnit unit = translateType("bar.A", "package bar; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assertTrue(packageInfoLookup.hasParametersAreNonnullByDefault("bar"));
  }
}
