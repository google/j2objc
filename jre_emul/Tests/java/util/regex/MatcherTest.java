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
}
