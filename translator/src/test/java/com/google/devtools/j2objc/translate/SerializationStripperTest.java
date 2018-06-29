/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
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

/** Verifies that serialization related members are removed when reflection is stripped. */
public class SerializationStripperTest extends GenerationTest {

  private String sourceFile(String className, boolean implementsSerializable) {
    return "import java.io.IOException;"
        + "import java.io.ObjectInputStream;"
        + "import java.io.ObjectOutputStream;"
        + "import java.io.ObjectStreamException;"
        + "import java.io.Serializable;"
        + "public class "
        + className
        + (implementsSerializable ? " implements Serializable " : "")
        + "{ "
        + "  private static final long serialVersionUID = 1053L;"
        + "  private static final long anotherField = 1122L;"
        + "  private void writeObject(ObjectOutputStream out) throws IOException {}"
        + "  private void readObject(ObjectInputStream in) throws IOException {}"
        + "  private void readObjectNoData() throws ObjectStreamException {}"
        + "  Object writeReplace() throws ObjectStreamException { return null; }"
        + "  Object readResolve() throws ObjectStreamException { return null; }"
        + "  private void anotherMethod() {}"
        + "}";
  }

  public void testNonSerializableTypeIsNotModified() throws IOException {
    options.setStripReflection(true);
    String translation =
        translateSourceFile(
            sourceFile(/* className= */ "Test", /* implementsSerializable= */ false),
            /* typeName= */ "Test",
            /* fileName= */ "Test.m");
    assertTranslation(translation, "serialVersionUID");
    assertTranslation(translation, "anotherField");
    assertTranslation(translation, "writeObject");
    assertTranslation(translation, "readObject");
    assertTranslation(translation, "readObjectNoData");
    assertTranslation(translation, "writeReplace");
    assertTranslation(translation, "readResolve");
    assertTranslation(translation, "anotherMethod");
  }

  public void testSerializableTypeIsNotModifiedWhenReflectionIsNotStripped() throws IOException {
    options.setStripReflection(false);
    String translation =
        translateSourceFile(
            sourceFile(/* className= */ "Test", /* implementsSerializable= */ true),
            /* typeName= */ "Test",
            /* fileName= */ "Test.m");
    assertTranslation(translation, "serialVersionUID");
    assertTranslation(translation, "anotherField");
    assertTranslation(translation, "writeObject");
    assertTranslation(translation, "readObject");
    assertTranslation(translation, "readObjectNoData");
    assertTranslation(translation, "writeReplace");
    assertTranslation(translation, "readResolve");
    assertTranslation(translation, "anotherMethod");
  }

  public void testSerializableTypeIsStripped() throws IOException {
    options.setStripReflection(true);
    String translation =
        translateSourceFile(
            sourceFile(/* className= */ "Test", /* implementsSerializable= */ true),
            /* typeName= */ "Test",
            /* fileName= */ "Test.m");
    assertNotInTranslation(translation, "serialVersionUID");
    assertTranslation(translation, "anotherField");
    assertNotInTranslation(translation, "writeObject");
    assertNotInTranslation(translation, "readObject");
    assertNotInTranslation(translation, "readObjectNoData");
    assertNotInTranslation(translation, "writeReplace");
    assertNotInTranslation(translation, "readResolve");
    assertTranslation(translation, "anotherMethod");
  }
}
