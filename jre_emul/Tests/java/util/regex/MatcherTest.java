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

package java.util.regex;

import junit.framework.TestCase;

/**
 * Supplemental tests for java.util.regex.Matcher support.
 *
 * @author Keith Stanger
 */
public class MatcherTest extends TestCase {

  public void testFindWithStart() {
    Pattern p = Pattern.compile("foo");
    Matcher m = p.matcher("a foo bar");
    assertTrue(m.find(0));
  }

  public void testFindWithAnchoringBounds() {
    Pattern p = Pattern.compile("^foo$");
    Matcher m = p.matcher("a foo bar");
    m.region(2, 5);
    assertTrue(m.find());
    m.reset();
    m.region(2, 5);
    m.useAnchoringBounds(false);
    assertFalse(m.find());
  }

  public void testMatchesRegion() {
    Pattern p = Pattern.compile("foo");
    Matcher m = p.matcher("a foo bar");
    m.region(2, 5);
    assertTrue(m.matches());
  }

  public void testMatchesWithAnchoringBounds() {
    Pattern p = Pattern.compile("^foo$");
    Matcher m = p.matcher("a foo bar");
    m.region(2, 5);
    assertTrue(m.matches());
    m.reset();
    m.region(2, 5);
    m.useAnchoringBounds(false);
    assertFalse(m.matches());
  }

  /**
   * Verify that .matches() will attempt to match the entire input. In some
   * cases this means that .matches() will match a different portion of the
   * input than .find().
   */
  public void testMatchesEntireInput() {
    Pattern p = Pattern.compile("(\\w{2,9})(foo\\$)?");
    Matcher m = p.matcher("abcdfoo$");

    // Since .match() must match the entire input, it must use both groups to do
    // so. The resulting groups are different from what they will be from
    // calling .find().
    assertTrue(m.matches());
    assertEquals("abcdfoo$", m.group(0));
    assertEquals("abcd", m.group(1));
    assertEquals("foo$", m.group(2));

    m.reset();
    // Since the second group is optional (?), .find() will match all the
    // alpha-numeric characters to the first group and leave the second group
    // unmatched.
    assertTrue(m.find());
    assertEquals("abcdfoo", m.group(0));
    assertEquals("abcdfoo", m.group(1));
    assertNull(m.group(2));
  }
}
