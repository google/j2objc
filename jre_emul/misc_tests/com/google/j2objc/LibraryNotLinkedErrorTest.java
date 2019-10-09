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

package com.google.j2objc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for com.google.LibraryNotLinkedError. */
@RunWith(JUnit4.class)
public final class LibraryNotLinkedErrorTest {

  @Test
  public void verifyStandardErrorMsg() throws Exception {
    try {
      throw new LibraryNotLinkedError("someFunction", "someLibrary", "SomeLibraryClass");
    } catch (LibraryNotLinkedError e) {
      assertEquals(
          "someFunction is unavailable. Fix this by:\n"
              + "1) If linking with -ObjC, add -lsomeLibrary to the link flags.\n"
              + "2) If linking without -ObjC, call \"SomeLibraryClass()\" (Swift) or\n"
              + "   \"SomeLibraryClass_class_()\" (Objective C) to add a build dependency.",
          e.getMessage());
    }
  }

  @Test
  public void verifyCustomErrorMsg() throws Exception {
    try {
      throw new LibraryNotLinkedError(
          "SerialVersionUID hashing",
          "jre_security",
          "JavaIoSerialVersionUIDDigest",
          "3) Add serialVersionUID fields to all Serializable classes.");
    } catch (LibraryNotLinkedError e) {
      assertEquals(
          "SerialVersionUID hashing is unavailable. Fix this by:\n"
              + "1) If linking with -ObjC, add -ljre_security to the link flags.\n"
              + "2) If linking without -ObjC, call \"JavaIoSerialVersionUIDDigest()\" (Swift) or\n"
              + "   \"JavaIoSerialVersionUIDDigest_class_()\" (Objective C) to add a build"
              + " dependency.\n"
              + "3) Add serialVersionUID fields to all Serializable classes.",
          e.getMessage());
    }
  }
}
