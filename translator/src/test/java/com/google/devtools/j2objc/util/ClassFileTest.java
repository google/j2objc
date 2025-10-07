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
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import java.io.IOException;

/**
 * Tests for {@link ClassFile}.
 *
 * Note: classfile conversion is experimental and not supported.
 */
public class ClassFileTest extends GenerationTest {

  static class Inner {}

  public void testJarFileLoading() throws IOException {
    String jarFilePath = getResourceAsFile("packageInfoLookupTest.jar");
    InputFile input = new JarredInputFile(jarFilePath,
        "com/google/test/packageInfoLookupTest/package-info.class");
    ClassFile cf = ClassFile.create(input);
    assertNotNull(cf);
    assertEquals("com.google.test.packageInfoLookupTest.package-info", cf.getFullName());
  }

  public void testGetFullNameInnerClass() throws IOException {
    ClassFile cf =
        getClassFile("ClassFileTest$Inner.class");
    assertEquals(
        "com.google.devtools.j2objc.util.ClassFileTest.Inner", cf.getFullName());
  }

  public void testGetRelativePathInnerClass() throws IOException {
    String path = "ClassFileTest$Inner.class";
    ClassFile cf = getClassFile(path);
    assertEquals("com/google/devtools/j2objc/util/" + path, cf.getRelativePath());
  }

  private ClassFile getClassFile(String path) throws IOException {
    String clazzPath = getResourceAsFile(path);
    InputFile input = new RegularInputFile(clazzPath, path);
    ClassFile cf = ClassFile.create(input);
    assertNotNull(cf);
    return cf;
  }
}
