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

public class ObjectiveCNativeProtocolAnnotationTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
  }

  public void testNameRequired() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeProtocol; "
            + "@ObjectiveCNativeProtocol() "
            + "public class Foo {"
            + "}",
        "Foo.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("Foo", "Foo.h");
    assertError("ObjectiveCNativeProtocol must specify a native protocol name.");
  }

  public void testSingleProtocolNoHeader() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeProtocol; "
            + "@ObjectiveCNativeProtocol(name=\"UICollectionViewDataSource\") "
            + "public class Foo {"
            + "}",
        "Foo.java");
    String testHeader = translateSourceFile("Foo", "Foo.h");

    assertNoWarnings();
    assertNoErrors();

    assertInTranslation(testHeader, "@interface Foo : NSObject < UICollectionViewDataSource >");
    assertNotInTranslation(testHeader, "UICollectionViewDataSource.h");
  }

  public void testSingleProtocol() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeProtocol; "
            + "@ObjectiveCNativeProtocol(name=\"Bar\", header=\"some/local/path/BarProtocol.h\") "
            + "public class Foo {"
            + "}",
        "Foo.java");
    String testHeader = translateSourceFile("Foo", "Foo.h");

    assertNoWarnings();
    assertNoErrors();

    assertInTranslation(testHeader, "@interface Foo : NSObject < Bar >");
    assertNotInTranslation(testHeader, "Bar.h");
    assertInTranslation(testHeader, "#include \"some/local/path/BarProtocol.h\"");
  }

  public void testMultipleProtocols() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeProtocol; "
            + "@ObjectiveCNativeProtocol(name=\"UICollectionViewDataSource\") "
            + "@ObjectiveCNativeProtocol(name=\"Bar\", header=\"some/local/path/BarProtocol.h\") "
            + "public class Foo {"
            + "}",
        "Foo.java");
    String testHeader = translateSourceFile("Foo", "Foo.h");

    assertNoWarnings();
    assertNoErrors();

    assertInTranslation(
        testHeader, "@interface Foo : NSObject < UICollectionViewDataSource, Bar >");
    assertNotInTranslation(testHeader, "UICollectionViewDataSource.h");
    assertNotInTranslation(testHeader, "Bar.h");
    assertInTranslation(testHeader, "#include \"some/local/path/BarProtocol.h\"");
  }
}
