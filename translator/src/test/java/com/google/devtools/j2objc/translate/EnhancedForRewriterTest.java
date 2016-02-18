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
 * Unit tests for {@link EnhancedForRewriter}.
 *
 * @author Keith Stanger
 */
public class EnhancedForRewriterTest extends GenerationTest {

  // Regression test: Must call "charValue" on boxed type returned from iterator.
  public void testEnhancedForWithBoxedType() throws IOException {
    String source = "import java.util.List;"
        + "public class A { "
        + "Character[] charArray; "
        + "List<Character> charList; "
        + "void test() { for (char c : charArray) {} for (char c : charList) {} } }";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation,
        "jchar c = [((JavaLangCharacter *) nil_chk(*b__++)) charValue];");
    assertTranslation(translation,
        "jchar c = [((JavaLangCharacter *) nil_chk(boxed__)) charValue];");
  }

  public void testEnhancedForLoopAnnotation() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.LoopTranslation;"
        + "import com.google.j2objc.annotations.LoopTranslation.LoopStyle;"
        + "class Test { void test(Iterable<String> strings) { "
        + "for (@LoopTranslation(LoopStyle.JAVA_ITERATOR) String s : strings) {}"
        + "for (@LoopTranslation(LoopStyle.FAST_ENUMERATION) String s : strings) {} } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)testWithJavaLangIterable:(id<JavaLangIterable>)strings {",
          "{",
            "id<JavaUtilIterator> iter__ = [((id<JavaLangIterable>) nil_chk(strings)) iterator];",
            "while ([((id<JavaUtilIterator>) nil_chk(iter__)) hasNext]) {",
              "NSString *s = [iter__ next];",
            "}",
          "}",
          "for (NSString * __strong s in strings) {",
          "}",
        "}");
  }

  public void testLabeledEnhancedForLoop() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.LoopTranslation;"
        + "import com.google.j2objc.annotations.LoopTranslation.LoopStyle;"
        + " class Test { public void foo(boolean b, java.util.List<Object> list) {"
        + " testLabel1: for (Object o: new Object[] { }) {"
        + " if (b) { break testLabel1; } continue testLabel1; }"
        + " testLabel2: for (@LoopTranslation(LoopStyle.JAVA_ITERATOR) Object o: list) {"
        + " if (b) { break testLabel2; } continue testLabel2; } } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "{",
        "  IOSObjectArray *a__ = [IOSObjectArray arrayWithObjects:"
          + "(id[]){  } count:0 type:NSObject_class_()];",
        "  id const *b__ = a__->buffer_;",
        "  id const *e__ = b__ + a__->size_;",
        "  while (b__ < e__) {",
        "    {",
        "      id o = *b__++;",
        "      if (b) {",
        "        goto break_testLabel1;",
        "      }",
        "      goto continue_testLabel1;",
        "    }",
        "    continue_testLabel1: ;",
        "  }",
        "  break_testLabel1: ;",
        "}",
        "{",
        "  id<JavaUtilIterator> iter__ = [((id<JavaUtilList>) nil_chk(list)) iterator];",
        "  while ([((id<JavaUtilIterator>) nil_chk(iter__)) hasNext]) {",
        "    {",
        "      id o = [iter__ next];",
        "      if (b) {",
        "        goto break_testLabel2;",
        "      }",
        "      goto continue_testLabel2;",
        "    }",
        "    continue_testLabel2: ;",
        "  }",
        "  break_testLabel2: ;",
        "}");
  }
}
