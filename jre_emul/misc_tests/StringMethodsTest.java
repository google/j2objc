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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import junit.framework.TestCase;

/** Tests java.lang.String methods added since Java 11. */
public class StringMethodsTest extends TestCase {

  public void testLinesMethod() {
    List<String> lines = "hello\nworld".lines().collect(Collectors.toList());
    List<String> expected = Arrays.asList("hello", "world");
    assertEquals(expected, lines);
  }

  public void testIsBlankMethod() {
    String emptyString = "";
    String whitespaceString = "   \t \n "; // Mix of space, tab, newline
    String contentString = "hello";
    String paddedContentString = "  world ";

    // Cases where isBlank() should return true
    assertTrue("Empty string should be blank", emptyString.isBlank());
    assertTrue("String with only whitespace should be blank", whitespaceString.isBlank());

    // Cases where isBlank() should return false
    assertFalse("String with content should not be blank", contentString.isBlank());
    assertFalse("String with padded content should not be blank", paddedContentString.isBlank());
  }
  
  public void testIndentSingleLine() {
    String original = "Hello";
    String expected = "  Hello\n";
    assertEquals("Indenting single line by 2", expected, original.indent(2));
  }

  public void testIndentMultiLine() {
    String original = "Line 1\nLine 2";
    // Note: indent adds spaces to *each* line, including the last one if it's not empty,
    // and ensures the result ends with a newline.
    String expected = "   Line 1\n   Line 2\n";
    assertEquals("Indenting multi-line by 3", expected, original.indent(3));
  }
  
  public void testTranslateEscapes() {
    // Input string containing literal escape sequences.
    String original = "Column 1\\tColumn 2\\nValue: \\101\\s(letter A)"; 

    // Expected output after translation:
    // '\t' -> tab, '\n' -> newline, '\101' -> 'A', '\s' -> ' ' (space)
    String expected = "Column 1\tColumn 2\nValue: A (letter A)";
    String actual = original.translateEscapes();
    assertEquals("translateEscapes should convert \\t, \\n, \\101, \\s", expected, actual);
  }

  public void testTransform() {
    String original = "12345";
    Integer expected = 12345;
    Integer actual = original.transform(s -> Integer.parseInt(s));
    assertEquals(expected, actual);
  }
  
  public void testFormatted() {
    String original = "%s, %s!";
    String expected = "Hello, World!";
    String actual = original.formatted("Hello", "World");
    assertEquals(expected, actual);
  }

  // TODO: uncomment when tests are run with a minimum of Java 15, which supports
  // multi-line strings (stripIndent only works on multi-line strings).
  // public void testSimpleStripIndentUsage() {
  //   // Input string with varying indentation and trailing whitespace
  //   String original =
  //       "  First line with two spaces.\n"
  //       + "    Second line with four spaces.\n"
  //       + " Third line with one space.\n"
  //       + "\n"
  //       + "   Fifth line indented relative to common indent.\n";

  //   String expected =
  //       " First line with two spaces.\n"
  //           + "   Second line with four spaces.\n"
  //           + "Third line with one space.\n"
  //           + "\n" // Blank line remains empty
  //           + "  Fifth line indented relative to common indent.\n";

  //   String actual = original.stripIndent();
  //   assertEquals(expected, actual);
  // }

}
