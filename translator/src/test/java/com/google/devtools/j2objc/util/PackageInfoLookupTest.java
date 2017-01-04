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

  public void testReflectionSupportAnnotation() throws IOException {
    addSourceFile("@ReflectionSupport(value = ReflectionSupport.Level.FULL) package foo;"
        + "import com.google.j2objc.annotations.ReflectionSupport;", "foo/package-info.java");
    CompilationUnit unit = translateType("foo.A", "package foo; public class A {}");
    PackageInfoLookup packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assert packageInfoLookup.getReflectionSupportLevel("foo") == ReflectionSupport.Level.FULL;

    addSourceFile("@ReflectionSupport(ReflectionSupport.Level.FULL) package bar;"
        + "import com.google.j2objc.annotations.*;", "bar/package-info.java");
    unit = translateType("bar.A", "package bar; public class A {}");
    packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assert packageInfoLookup.getReflectionSupportLevel("bar") == ReflectionSupport.Level.FULL;

    addSourceFile("@com.google.j2objc.annotations.ReflectionSupport"
        + "(com.google.j2objc.annotations.ReflectionSupport.Level.NATIVE_ONLY) package baz;",
        "baz/package-info.java");
    unit = translateType("baz.A", "package baz; public class A {}");
    packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assert
        packageInfoLookup.getReflectionSupportLevel("baz") == ReflectionSupport.Level.NATIVE_ONLY;

    // Verify that ReflectionSupport annotation can be parsed from class files
    String jarFilePath = getResourceAsFile("packageInfoLookupTest.jar");
    options.fileUtil().getClassPathEntries().add(jarFilePath);
    unit = translateType("com.google.test.packageInfoLookupTest.A",
        "package com.google.test.packageInfoLookupTest; public class A {}");
    packageInfoLookup = unit.getEnv().options().getPackageInfoLookup();
    assert packageInfoLookup.getReflectionSupportLevel(unit.getPackage().getName().toString())
        == ReflectionSupport.Level.FULL;
  }
}
