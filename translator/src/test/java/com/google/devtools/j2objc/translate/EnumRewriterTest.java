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
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Unit tests for {@link EnumRewriter}.
 *
 * @author Keith Stanger
 */
public class EnumRewriterTest extends GenerationTest {

  public void testGenericEnumConstructor() throws IOException {
    String translation = translateSourceFile(
        "enum Test { A(\"foo\"); private <T> Test(T t) {} }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (instancetype)initWithId:(id)t",
        "withNSString:(NSString *)__name",
        "withInt:(jint)__ordinal {");
    assertTranslation(translation,
        "TestEnum_A = new_TestEnum_initWithId_withNSString_withInt_(@\"foo\", @\"A\", 0);");
  }

  public void testNoDefaultToNsEnumConversion() throws Exception {
    String translation = translateSourceFile("enum Test { A }", "Test", "Test.m");
    assertNotInTranslation(translation, "toNSEnum");
  }

  public void testToNsEnumConversion() throws Exception {
    Options.setSwiftFriendly(true);
    String translation = translateSourceFile("enum Test { A }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (Test_Enum)toNSEnum {",
        "  return (Test_Enum)[self ordinal];",
        "}");
  }
}
