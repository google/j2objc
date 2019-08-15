/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.spi.CharsetProvider;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import libcore.java.nio.charset.SettableCharsetProvider;

import junit.framework.TestCase;

/**
 * Test class java.nio.Charset.
 */
public class CharsetTest extends TestCase {

  public void test_allAvailableCharsets() throws Exception {
    // Check that we can instantiate every Charset, CharsetDecoder, and CharsetEncoder.
    for (String charsetName : Charset.availableCharsets().keySet()) {
      if (charsetName.equals("UTF-32")) {
        // Our UTF-32 is broken. http://b/2702411
        // TODO: remove this hack when UTF-32 is fixed.
        continue;
      }

      Charset cs = Charset.forName(charsetName);
      assertNotNull(cs.newDecoder());
      if (cs.canEncode()) {
        CharsetEncoder enc = cs.newEncoder();
        assertNotNull(enc);
        assertNotNull(enc.replacement());
      }
    }
  }

  public void test_defaultCharset() {
    assertEquals("UTF-8", Charset.defaultCharset().name());
  }

  public void test_isRegistered() {
    // Regression for HARMONY-45

    // Will contain names of charsets registered with IANA
    Set<String> knownRegisteredCharsets = new HashSet<String>();

    // Will contain names of charsets not known to be registered with IANA
    Set<String> unknownRegisteredCharsets = new HashSet<String>();

    Set<String> names = Charset.availableCharsets().keySet();
    for (Iterator nameItr = names.iterator(); nameItr.hasNext();) {
      String name = (String) nameItr.next();
      if (name.toLowerCase(Locale.ROOT).startsWith("x-")) {
        unknownRegisteredCharsets.add(name);
      } else {
        knownRegisteredCharsets.add(name);
      }
    }

    for (Iterator nameItr = knownRegisteredCharsets.iterator(); nameItr.hasNext();) {
      String name = (String) nameItr.next();
      Charset cs = Charset.forName(name);
      if (!cs.isRegistered()) {
        System.err.println("isRegistered was false for " + name + " " + cs.name() + " " + cs.aliases());
      }
      assertTrue("isRegistered was false for " + name + " " + cs.name() + " " + cs.aliases(), cs.isRegistered());
    }
    for (Iterator nameItr = unknownRegisteredCharsets.iterator(); nameItr.hasNext();) {
      String name = (String) nameItr.next();
      Charset cs = Charset.forName(name);
      assertFalse("isRegistered was true for " + name + " " + cs.name() + " " + cs.aliases(), cs.isRegistered());
    }
  }

  public void test_guaranteedCharsetsAvailable() throws Exception {
    // All Java implementations must support these charsets.
    assertNotNull(Charset.forName("ISO-8859-1"));
    assertNotNull(Charset.forName("US-ASCII"));
    assertNotNull(Charset.forName("UTF-16"));
    assertNotNull(Charset.forName("UTF-16BE"));
    assertNotNull(Charset.forName("UTF-16LE"));
    assertNotNull(Charset.forName("UTF-8"));
  }

  // http://code.google.com/p/android/issues/detail?id=42769
  public void test_42769() throws Exception {
    ArrayList<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < 10; ++i) {
      Thread t = new Thread(new Runnable() {
        public void run() {
          for (int i = 0; i < 50; ++i) {
            Charset.availableCharsets();
          }
        }
      });
      threads.add(t);
    }

    for (Thread t : threads) {
      t.start();
    }
    for (Thread t : threads) {
      t.join();
    }
  }

  public void test_have_canonical_EUC_JP() throws Exception {
    assertEquals("EUC-JP", Charset.forName("EUC-JP").name());
  }

  /* j2objc: b/139491456
  public void test_EUC_JP_replacement_character() throws Exception {
    // We have text either side of the replacement character, because all kinds of errors
    // could lead to a replacement character being returned.
    assertEncodes(Charset.forName("EUC-JP"), " \ufffd ", ' ', 0xf4, 0xfe, ' ');
    assertDecodes(Charset.forName("EUC-JP"), " \ufffd ", ' ', 0xf4, 0xfe, ' ');
  }
  */

  /* j2objc: iOS doesn't support SCSU charset.
  public void test_SCSU_replacement_character() throws Exception {
    // We have text either side of the replacement character, because all kinds of errors
    // could lead to a replacement character being returned.
    assertEncodes(Charset.forName("SCSU"), " \ufffd ", ' ', 14, 0xff, 0xfd, ' ');
    assertDecodes(Charset.forName("SCSU"), " \ufffd ", ' ', 14, 0xff, 0xfd, ' ');
  }
  */

  /* j2objc: b/139491456
  public void test_Shift_JIS_replacement_character() throws Exception {
    // We have text either side of the replacement character, because all kinds of errors
    // could lead to a replacement character being returned.
    assertEncodes(Charset.forName("Shift_JIS"), " \ufffd ", ' ', 0xfc, 0xfc, ' ');
    assertDecodes(Charset.forName("Shift_JIS"), " \ufffd ", ' ', 0xfc, 0xfc, ' ');
  }
  */

  public void test_UTF_16() throws Exception {
    Charset cs = Charset.forName("UTF-16");
    // Writes big-endian, with a big-endian BOM.
    assertEncodes(cs, "a\u0666", 0xfe, 0xff, 0, 'a', 0x06, 0x66);
    // Reads whatever the BOM tells it to read...
    assertDecodes(cs, "a\u0666", 0xfe, 0xff, 0, 'a', 0x06, 0x66);
    assertDecodes(cs, "a\u0666", 0xff, 0xfe, 'a', 0, 0x66, 0x06);
    // ...and defaults to reading big-endian if there's no BOM.
    assertDecodes(cs, "a\u0666", 0, 'a', 0x06, 0x66);
  }

  /* j2objc: b/139491456
  public void test_UTF_16BE() throws Exception {
    Charset cs = Charset.forName("UTF-16BE");
    // Writes big-endian, with no BOM.
    assertEncodes(cs, "a\u0666", 0, 'a', 0x06, 0x66);
    // Treats a little-endian BOM as an error and continues to read big-endian.
    // This test uses REPLACE mode, so we get the U+FFFD replacement character in the result.
    assertDecodes(cs, "\ufffda\u0666", 0xff, 0xfe, 0, 'a', 0x06, 0x66);
    // Accepts a big-endian BOM and includes U+FEFF in the decoded output.
    assertDecodes(cs, "\ufeffa\u0666", 0xfe, 0xff, 0, 'a', 0x06, 0x66);
    // Defaults to reading big-endian.
    assertDecodes(cs, "a\u0666", 0, 'a', 0x06, 0x66);
  }
  */

  /* j2objc: b/139491456
  public void test_UTF_16LE() throws Exception {
    Charset cs = Charset.forName("UTF-16LE");
    // Writes little-endian, with no BOM.
    assertEncodes(cs, "a\u0666", 'a', 0, 0x66, 0x06);
    // Accepts a little-endian BOM and includes U+FEFF in the decoded output.
    assertDecodes(cs, "\ufeffa\u0666", 0xff, 0xfe, 'a', 0, 0x66, 0x06);
    // Treats a big-endian BOM as an error and continues to read little-endian.
    // This test uses REPLACE mode, so we get the U+FFFD replacement character in the result.
    assertDecodes(cs, "\ufffda\u0666", 0xfe, 0xff, 'a', 0, 0x66, 0x06);
    // Defaults to reading little-endian.
    assertDecodes(cs, "a\u0666", 'a', 0, 0x66, 0x06);
  }
  */

  /* j2objc: iOS doesn't support x-UTF-16LE-BOM charset.
  public void test_x_UTF_16LE_BOM() throws Exception {
    Charset cs = Charset.forName("x-UTF-16LE-BOM");
    // Writes little-endian, with a BOM.
    assertEncodes(cs, "a\u0666", 0xff, 0xfe, 'a', 0, 0x66, 0x06);
    // Accepts a little-endian BOM and swallows the BOM.
    assertDecodes(cs, "a\u0666", 0xff, 0xfe, 'a', 0, 0x66, 0x06);
    // Swallows a big-endian BOM, but continues to read little-endian!
    assertDecodes(cs, "\u6100\u6606", 0xfe, 0xff, 'a', 0, 0x66, 0x06);
    // Defaults to reading little-endian.
    assertDecodes(cs, "a\u0666", 'a', 0, 0x66, 0x06);
  }
  */

  /* j2objc: b/139491456
  public void test_UTF_32() throws Exception {
    Charset cs = Charset.forName("UTF-32");
    // Writes big-endian, with no BOM.
    assertEncodes(cs, "a\u0666", 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Reads whatever the BOM tells it to read...
    assertDecodes(cs, "a\u0666", 0, 0, 0xfe, 0xff, 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    assertDecodes(cs, "a\u0666", 0xff, 0xfe, 0, 0, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // ...and defaults to reading big-endian if there's no BOM.
    assertDecodes(cs, "a\u0666", 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
  }
  */

  /* j2objc: b/139491456
  public void test_UTF_32BE() throws Exception {
    Charset cs = Charset.forName("UTF-32BE");
    // Writes big-endian, with no BOM.
    assertEncodes(cs, "a\u0666", 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Treats a little-endian BOM as an error and continues to read big-endian.
    // This test uses REPLACE mode, so we get the U+FFFD replacement character in the result.
    assertDecodes(cs, "\ufffda\u0666", 0xff, 0xfe, 0, 0, 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Accepts a big-endian BOM and swallows the BOM.
    assertDecodes(cs, "a\u0666", 0, 0, 0xfe, 0xff, 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Defaults to reading big-endian.
    assertDecodes(cs, "a\u0666", 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
  }
  */

  /* j2objc: b/139491456
  public void test_UTF_32LE() throws Exception {
    Charset cs = Charset.forName("UTF-32LE");
    // Writes little-endian, with no BOM.
    assertEncodes(cs, "a\u0666", 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // Accepts a little-endian BOM and swallows the BOM.
    assertDecodes(cs, "a\u0666", 0xff, 0xfe, 0, 0, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // Treats a big-endian BOM as an error and continues to read little-endian.
    // This test uses REPLACE mode, so we get the U+FFFD replacement character in the result.
    assertDecodes(cs, "\ufffda\u0666", 0, 0, 0xfe, 0xff, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // Defaults to reading little-endian.
    assertDecodes(cs, "a\u0666", 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
  }
  */

  /* j2objc: iOS doesn't support X-UTF-32BE-BOM charset.
  public void test_X_UTF_32BE_BOM() throws Exception {
    Charset cs = Charset.forName("X-UTF-32BE-BOM");
    // Writes big-endian, with a big-endian BOM.
    assertEncodes(cs, "a\u0666", 0, 0, 0xfe, 0xff, 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Treats a little-endian BOM as an error and continues to read big-endian.
    // This test uses REPLACE mode, so we get the U+FFFD replacement character in the result.
    assertDecodes(cs, "\ufffda\u0666", 0xff, 0xfe, 0, 0, 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Swallows a big-endian BOM, and continues to read big-endian.
    assertDecodes(cs, "a\u0666", 0, 0, 0xfe, 0xff, 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
    // Defaults to reading big-endian.
    assertDecodes(cs, "a\u0666", 0, 0, 0, 'a', 0, 0, 0x06, 0x66);
  }
  */

  /* j2objc: iOS doesn't support X-UTF-32LE-BOM charset.
  public void test_X_UTF_32LE_BOM() throws Exception {
    Charset cs = Charset.forName("X-UTF-32LE-BOM");
    // Writes little-endian, with a little-endian BOM.
    assertEncodes(cs, "a\u0666", 0xff, 0xfe, 0, 0, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // Accepts a little-endian BOM and swallows the BOM.
    assertDecodes(cs, "a\u0666", 0xff, 0xfe, 0, 0, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // Treats a big-endian BOM as an error and continues to read little-endian.
    // This test uses REPLACE mode, so we get the U+FFFD replacement character in the result.
    assertDecodes(cs, "\ufffda\u0666", 0, 0, 0xfe, 0xff, 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
    // Defaults to reading little-endian.
    assertDecodes(cs, "a\u0666", 'a', 0, 0, 0, 0x66, 0x06, 0, 0);
  }
  */

  private byte[] toByteArray(int[] ints) {
    byte[] result = new byte[ints.length];
    for (int i = 0; i < ints.length; ++i) {
      result[i] = (byte) ints[i];
    }
    return result;
  }

  private void assertEncodes(Charset cs, String s, int... expectedByteInts) throws Exception {
    ByteBuffer out = cs.encode(s);
    byte[] bytes = new byte[out.remaining()];
    out.get(bytes);
    assertEquals(Arrays.toString(toByteArray(expectedByteInts)), Arrays.toString(bytes));
  }

  private void assertDecodes(Charset cs, String s, int... byteInts) throws Exception {
    ByteBuffer in = ByteBuffer.wrap(toByteArray(byteInts));
    CharBuffer out = cs.decode(in);
    assertEquals(s, out.toString());
  }

  public void test_forNameLjava_lang_String() {
    // Invoke forName two times with the same canonical name.
    // It should return the same reference.
    Charset cs1 = Charset.forName("UTF-8");
    Charset cs2 = Charset.forName("UTF-8");
    assertSame(cs1, cs2);

    // test forName: invoke forName two times for the same Charset using
    // canonical name and alias, it should return the same reference.
    Charset cs3 = Charset.forName("ASCII");
    Charset cs4 = Charset.forName("US-ASCII");
    assertSame(cs3, cs4);
  }

  static MockCharset charset1 = new MockCharset("mockCharset00",
                                                new String[] { "mockCharset01", "mockCharset02" });

  static MockCharset charset2 = new MockCharset("mockCharset10",
                                                new String[] { "mockCharset11", "mockCharset12" });

  // Test the required 6 charsets are supported.
  public void testRequiredCharsetSupported() {
    assertTrue(Charset.isSupported("US-ASCII"));
    assertTrue(Charset.isSupported("ASCII"));
    assertTrue(Charset.isSupported("ISO-8859-1"));
    assertTrue(Charset.isSupported("ISO8859_1"));
    assertTrue(Charset.isSupported("UTF-8"));
    assertTrue(Charset.isSupported("UTF8"));
    assertTrue(Charset.isSupported("UTF-16"));
    assertTrue(Charset.isSupported("UTF-16BE"));
    assertTrue(Charset.isSupported("UTF-16LE"));

    Charset c1 = Charset.forName("US-ASCII");
    assertEquals("US-ASCII", Charset.forName("US-ASCII").name());
    assertEquals("US-ASCII", Charset.forName("ASCII").name());
    assertEquals("ISO-8859-1", Charset.forName("ISO-8859-1").name());
    assertEquals("ISO-8859-1", Charset.forName("ISO8859_1").name());
    assertEquals("UTF-8", Charset.forName("UTF-8").name());
    assertEquals("UTF-8", Charset.forName("UTF8").name());
    assertEquals("UTF-16", Charset.forName("UTF-16").name());
    assertEquals("UTF-16BE", Charset.forName("UTF-16BE").name());
    assertEquals("UTF-16LE", Charset.forName("UTF-16LE").name());

    assertNotSame(Charset.availableCharsets(), Charset.availableCharsets());
    // assertSame(Charset.forName("US-ASCII"), Charset.availableCharsets().get("US-ASCII"));
    // assertSame(Charset.forName("US-ASCII"), c1);
    assertTrue(Charset.availableCharsets().containsKey("US-ASCII"));
    assertTrue(Charset.availableCharsets().containsKey("ISO-8859-1"));
    assertTrue(Charset.availableCharsets().containsKey("UTF-8"));
    assertTrue(Charset.availableCharsets().containsKey("UTF-16"));
    assertTrue(Charset.availableCharsets().containsKey("UTF-16BE"));
    assertTrue(Charset.availableCharsets().containsKey("UTF-16LE"));
  }

  public void testIsSupported_Null() {
    try {
      Charset.isSupported(null);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testIsSupported_EmptyString() {
    try {
      Charset.isSupported("");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  /* j2objc: b/139491456
  public void testIsSupported_InvalidInitialCharacter() {
    try {
      Charset.isSupported(".char");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
  */

  public void testIsSupported_IllegalName() {
    try {
      Charset.isSupported(" ///#$$");
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }

  public void testIsSupported_NotSupported() {
    assertFalse(Charset.isSupported("well-formed-name-of-a-charset-that-does-not-exist"));
  }

  public void testForName_Null() {
    try {
      Charset.forName(null);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testForName_EmptyString() {
    try {
      Charset.forName("");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testForName_InvalidInitialCharacter() {
    try {
      Charset.forName(".char");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testForName_IllegalName() {
    try {
      Charset.forName(" ///#$$");
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }

  public void testForName_NotSupported() {
    try {
      Charset.forName("impossible");
      fail();
    } catch (UnsupportedCharsetException expected) {
    }
  }

  public void testConstructor_Normal() {
    final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
    MockCharset c = new MockCharset(mockName, new String[] { "mock" });
    assertEquals(mockName, c.name());
    assertEquals(mockName, c.displayName());
    assertEquals(mockName, c.displayName(Locale.getDefault()));
    assertEquals("mock", c.aliases().toArray()[0]);
    assertEquals(1, c.aliases().toArray().length);
  }

  public void testConstructor_EmptyCanonicalName() {
    try {
      new MockCharset("", new String[0]);
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }

  /* j2objc: b/139491456
  public void testConstructor_IllegalCanonicalName_Initial() {
    try {
      new MockCharset("-123", new String[] { "mock" });
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }
  */

  public void testConstructor_IllegalCanonicalName_Middle() {
    try {
      new MockCharset("1%%23", new String[] { "mock" });
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
    try {
      new MockCharset("1//23", new String[] { "mock" });
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }

  public void testConstructor_NullCanonicalName() {
    try {
      MockCharset c = new MockCharset(null, new String[] { "mock" });
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testConstructor_NullAliases() {
    MockCharset c = new MockCharset("mockChar", null);
    assertEquals("mockChar", c.name());
    assertEquals("mockChar", c.displayName());
    assertEquals("mockChar", c.displayName(Locale.getDefault()));
    assertEquals(0, c.aliases().toArray().length);
  }

  public void testConstructor_NullAliase() {
    try {
      new MockCharset("mockChar", new String[] { "mock", null });
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testConstructor_NoAliases() {
    MockCharset c = new MockCharset("mockChar", new String[0]);
    assertEquals("mockChar", c.name());
    assertEquals("mockChar", c.displayName());
    assertEquals("mockChar", c.displayName(Locale.getDefault()));
    assertEquals(0, c.aliases().toArray().length);
  }

  public void testConstructor_EmptyAliases() {
    try {
      new MockCharset("mockChar", new String[] { "" });
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }

  // Test the constructor with illegal aliases: starting with neither a digit nor a letter.
  /* j2objc: b/139491456
  public void testConstructor_IllegalAliases_Initial() {
    try {
      new MockCharset("mockChar", new String[] { "mock", "-123" });
      fail();
    } catch (IllegalCharsetNameException e) {
    }
  }
  */

  public void testConstructor_IllegalAliases_Middle() {
    try {
      new MockCharset("mockChar", new String[] { "mock", "22##ab" });
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
    try {
      new MockCharset("mockChar", new String[] { "mock", "22%%ab" });
      fail();
    } catch (IllegalCharsetNameException expected) {
    }
  }

  public void testAliases_Multiple() {
    final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
    MockCharset c = new MockCharset("mockChar", new String[] { "mock", mockName, "mock2" });
    assertEquals("mockChar", c.name());
    assertEquals(3, c.aliases().size());
    assertTrue(c.aliases().contains("mock"));
    assertTrue(c.aliases().contains(mockName));
    assertTrue(c.aliases().contains("mock2"));

    try {
      c.aliases().clear();
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testAliases_Duplicate() {
    final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
    MockCharset c = new MockCharset("mockChar", new String[] { "mockChar",
                                                                  "mock", mockName, "mock", "mockChar", "mock", "mock2" });
    assertEquals("mockChar", c.name());
    assertEquals(4, c.aliases().size());
    assertTrue(c.aliases().contains("mockChar"));
    assertTrue(c.aliases().contains("mock"));
    assertTrue(c.aliases().contains(mockName));
    assertTrue(c.aliases().contains("mock2"));
  }

  public void testCanEncode() {
    MockCharset c = new MockCharset("mock", null);
    assertTrue(c.canEncode());
  }

  public void testIsRegistered() {
    MockCharset c = new MockCharset("mock", null);
    assertTrue(c.isRegistered());
  }

  public void testDisplayName_Locale_Null() {
    MockCharset c = new MockCharset("mock", null);
    assertEquals("mock", c.displayName(null));
  }

  public void testCompareTo_Normal() {
    MockCharset c1 = new MockCharset("mock", null);
    assertEquals(0, c1.compareTo(c1));

    MockCharset c2 = new MockCharset("Mock", null);
    assertEquals(0, c1.compareTo(c2));

    c2 = new MockCharset("mock2", null);
    assertTrue(c1.compareTo(c2) < 0);
    assertTrue(c2.compareTo(c1) > 0);

    c2 = new MockCharset("mack", null);
    assertTrue(c1.compareTo(c2) > 0);
    assertTrue(c2.compareTo(c1) < 0);

    c2 = new MockCharset("m.", null);
    assertTrue(c1.compareTo(c2) > 0);
    assertTrue(c2.compareTo(c1) < 0);

    c2 = new MockCharset("m:", null);
    assertEquals("mock".compareToIgnoreCase("m:"), c1.compareTo(c2));
    assertEquals("m:".compareToIgnoreCase("mock"), c2.compareTo(c1));

    c2 = new MockCharset("m-", null);
    assertTrue(c1.compareTo(c2) > 0);
    assertTrue(c2.compareTo(c1) < 0);

    c2 = new MockCharset("m_", null);
    assertTrue(c1.compareTo(c2) > 0);
    assertTrue(c2.compareTo(c1) < 0);
  }

  public void testCompareTo_Null() {
    MockCharset c1 = new MockCharset("mock", null);
    try {
      c1.compareTo(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testCompareTo_DiffCharsetClass() {
    MockCharset c1 = new MockCharset("mock", null);
    MockCharset2 c2 = new MockCharset2("Mock", new String[] { "myname" });
    assertEquals(0, c1.compareTo(c2));
    assertEquals(0, c2.compareTo(c1));
  }

  public void testEquals_Normal() {
    MockCharset c1 = new MockCharset("mock", null);
    MockCharset2 c2 = new MockCharset2("mock", null);
    assertTrue(c1.equals(c2));
    assertTrue(c2.equals(c1));

    c2 = new MockCharset2("Mock", null);
    assertFalse(c1.equals(c2));
    assertFalse(c2.equals(c1));
  }

  public void testEquals_Null() {
    MockCharset c1 = new MockCharset("mock", null);
    assertFalse(c1.equals(null));
  }

  public void testEquals_NonCharsetObject() {
    MockCharset c1 = new MockCharset("mock", null);
    assertFalse(c1.equals("test"));
  }

  public void testEquals_DiffCharsetClass() {
    MockCharset c1 = new MockCharset("mock", null);
    MockCharset2 c2 = new MockCharset2("mock", null);
    assertTrue(c1.equals(c2));
    assertTrue(c2.equals(c1));
  }

  public void testHashCode_DiffCharsetClass() {
    MockCharset c1 = new MockCharset("mock", null);
    assertEquals(c1.hashCode(), "mock".hashCode());

    final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
    c1 = new MockCharset(mockName, new String[] { "mockChar", "mock",
                                                     mockName, "mock", "mockChar", "mock", "mock2" });
    assertEquals(mockName.hashCode(), c1.hashCode());
  }

  public void testEncode_CharBuffer_Normal() throws Exception {
    MockCharset c1 = new MockCharset("testEncode_CharBuffer_Normal_mock", null);
    ByteBuffer bb = c1.encode(CharBuffer.wrap("abcdefg"));
    assertEquals("abcdefg", new String(bb.array(), "iso8859-1"));
    bb = c1.encode(CharBuffer.wrap(""));
    assertEquals("", new String(bb.array(), "iso8859-1"));
  }

  public void testEncode_CharBuffer_Unmappable() throws Exception {
    Charset c1 = Charset.forName("iso8859-1");
    ByteBuffer bb = c1.encode(CharBuffer.wrap("abcd\u5D14efg"));
    assertEquals(new String(bb.array(), "iso8859-1"),
                 "abcd" + new String(c1.newEncoder().replacement(), "iso8859-1") + "efg");
  }

  public void testEncode_CharBuffer_NullCharBuffer() {
    MockCharset c = new MockCharset("mock", null);
    try {
      c.encode((CharBuffer) null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testEncode_CharBuffer_NullEncoder() {
    MockCharset2 c = new MockCharset2("mock2", null);
    try {
      c.encode(CharBuffer.wrap("hehe"));
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testEncode_String_Normal() throws Exception {
    MockCharset c1 = new MockCharset("testEncode_String_Normal_mock", null);
    ByteBuffer bb = c1.encode("abcdefg");
    assertEquals("abcdefg", new String(bb.array(), "iso8859-1"));
    bb = c1.encode("");
    assertEquals("", new String(bb.array(), "iso8859-1"));
  }

  public void testEncode_String_Unmappable() throws Exception {
    Charset c1 = Charset.forName("iso8859-1");
    ByteBuffer bb = c1.encode("abcd\u5D14efg");
    assertEquals(new String(bb.array(), "iso8859-1"),
                 "abcd" + new String(c1.newEncoder().replacement(), "iso8859-1") + "efg");
  }

  public void testEncode_String_NullString() {
    MockCharset c = new MockCharset("mock", null);
    try {
      c.encode((String) null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testEncode_String_NullEncoder() {
    MockCharset2 c = new MockCharset2("mock2", null);
    try {
      c.encode("hehe");
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testDecode_Normal() throws Exception {
    MockCharset c1 = new MockCharset("mock", null);
    CharBuffer cb = c1.decode(ByteBuffer.wrap("abcdefg".getBytes("iso8859-1")));
    assertEquals("abcdefg", new String(cb.array()));
    cb = c1.decode(ByteBuffer.wrap("".getBytes("iso8859-1")));
    assertEquals("", new String(cb.array()));
  }

  public void testDecode_Malformed() throws Exception {
    Charset c1 = Charset.forName("iso8859-1");
    CharBuffer cb = c1.decode(ByteBuffer.wrap("abcd\u5D14efg".getBytes("iso8859-1")));
    byte[] replacement = c1.newEncoder().replacement();
    assertEquals(new String(cb.array()).trim(), "abcd" + new String(replacement, "iso8859-1") + "efg");
  }

  public void testDecode_NullByteBuffer() {
    MockCharset c = new MockCharset("mock", null);
    try {
      c.decode(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testDecode_NullDecoder() {
    MockCharset2 c = new MockCharset2("mock2", null);
    try {
      c.decode(ByteBuffer.wrap("hehe".getBytes()));
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testToString() {
    MockCharset c1 = new MockCharset("mock", null);
    assertTrue(-1 != c1.toString().indexOf("mock"));
  }

  static final class MockCharset extends Charset {
    public MockCharset(String canonicalName, String[] aliases) {
      super(canonicalName, aliases);
    }

    public boolean contains(Charset cs) {
      return false;
    }

    public CharsetDecoder newDecoder() {
      return new MockDecoder(this);
    }

    public CharsetEncoder newEncoder() {
      return new MockEncoder(this);
    }
  }

  static class MockCharset2 extends Charset {
    public MockCharset2(String canonicalName, String[] aliases) {
      super(canonicalName, aliases);
    }

    public boolean contains(Charset cs) {
      return false;
    }

    public CharsetDecoder newDecoder() {
      return null;
    }

    public CharsetEncoder newEncoder() {
      return null;
    }
  }

  static class MockEncoder extends java.nio.charset.CharsetEncoder {
    public MockEncoder(Charset cs) {
      super(cs, 1, 3, new byte[] { (byte) '?' });
    }

    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
      while (in.remaining() > 0) {
        out.put((byte) in.get());
        // out.put((byte) '!');
      }
      return CoderResult.UNDERFLOW;
    }
  }

  static class MockDecoder extends java.nio.charset.CharsetDecoder {
    public MockDecoder(Charset cs) {
      super(cs, 1, 10);
    }

    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
      while (in.remaining() > 0) {
        out.put((char) in.get());
      }
      return CoderResult.UNDERFLOW;
    }
  }


  // Test the method isSupported(String) with charset supported by multiple providers.
  /* j2objc: b/139491456
  public void testIsSupported_And_ForName_NormalProvider() throws Exception {
    SettableCharsetProvider.setDelegate(new MockCharsetProvider());
    try {
      assertTrue(Charset.isSupported("mockCharset10"));
      // ignore case problem in mock, intended
      assertTrue(Charset.isSupported("MockCharset11"));
      assertTrue(Charset.isSupported("MockCharset12"));
      assertTrue(Charset.isSupported("MOCKCharset10"));
      // intended case problem in mock
      assertTrue(Charset.isSupported("MOCKCharset11"));
      assertTrue(Charset.isSupported("MOCKCharset12"));

      assertTrue(Charset.forName("mockCharset10") instanceof MockCharset);
      assertTrue(Charset.forName("mockCharset11") instanceof MockCharset);
      assertTrue(Charset.forName("mockCharset12") instanceof MockCharset);

      assertTrue(Charset.forName("mockCharset10") == charset2);
      // intended case problem in mock
      Charset.forName("mockCharset11");
      assertTrue(Charset.forName("mockCharset12") == charset2);
    } finally {
      SettableCharsetProvider.clearDelegate();
    }
  }
  */

  // Test the method availableCharsets() with charset supported by multiple providers.
  /* j2objc: b/139491456
  public void testAvailableCharsets_NormalProvider() throws Exception {
    SettableCharsetProvider.setDelegate(new MockCharsetProvider());
    try {
      assertTrue(Charset.availableCharsets().containsKey("mockCharset00"));
      assertTrue(Charset.availableCharsets().containsKey("MOCKCharset00"));
      assertTrue(Charset.availableCharsets().get("mockCharset00") instanceof MockCharset);
      assertTrue(Charset.availableCharsets().get("MOCKCharset00") instanceof MockCharset);
      assertFalse(Charset.availableCharsets().containsKey("mockCharset01"));
      assertFalse(Charset.availableCharsets().containsKey("mockCharset02"));

      assertTrue(Charset.availableCharsets().get("mockCharset10") == charset2);
      assertTrue(Charset.availableCharsets().get("MOCKCharset10") == charset2);
      assertFalse(Charset.availableCharsets().containsKey("mockCharset11"));
      assertFalse(Charset.availableCharsets().containsKey("mockCharset12"));

      assertTrue(Charset.availableCharsets().containsKey("mockCharset10"));
      assertTrue(Charset.availableCharsets().containsKey("MOCKCharset10"));
      assertTrue(Charset.availableCharsets().get("mockCharset10") == charset2);
      assertFalse(Charset.availableCharsets().containsKey("mockCharset11"));
      assertFalse(Charset.availableCharsets().containsKey("mockCharset12"));
    } finally {
      SettableCharsetProvider.clearDelegate();
    }
  }
  */

  // Test the method forName(String) when the charset provider supports a
  // built-in charset.
  public void testForName_DuplicateWithBuiltInCharset() throws Exception {
    SettableCharsetProvider.setDelegate(new MockCharsetProviderASCII());
    try {
      assertFalse(Charset.forName("us-ascii") instanceof MockCharset);
      assertFalse(Charset.availableCharsets().get("us-ascii") instanceof MockCharset);
    } finally {
      SettableCharsetProvider.clearDelegate();
    }
  }

  // Fails on Android with a StackOverflowException.
  public void testForName_withProviderWithRecursiveCall() throws Exception {
    SettableCharsetProvider.setDelegate(new MockCharsetProviderWithRecursiveCall());
    try {
      Charset.forName("poop");
      fail();
    } catch (UnsupportedCharsetException expected) {
    } finally {
      SettableCharsetProvider.clearDelegate();
    }
  }

  public static class MockCharsetProviderWithRecursiveCall extends CharsetProvider {
      @Override
      public Iterator<Charset> charsets() {
          return null;
      }

      @Override
      public Charset charsetForName(String charsetName) {
          if (Charset.isSupported(charsetName)) {
              return Charset.forName(charsetName);
          }

          return null;
      }
  }

  public static class MockCharsetProvider extends CharsetProvider {
    public Charset charsetForName(String charsetName) {
      if ("MockCharset00".equalsIgnoreCase(charsetName) ||
          "MockCharset01".equalsIgnoreCase(charsetName) ||
          "MockCharset02".equalsIgnoreCase(charsetName)) {
        return charset1;
      } else if ("MockCharset10".equalsIgnoreCase(charsetName) ||
          "MockCharset11".equalsIgnoreCase(charsetName) ||
          "MockCharset12".equalsIgnoreCase(charsetName)) {
        return charset2;
      }
      return null;
    }

    public Iterator charsets() {
      Vector v = new Vector();
      v.add(charset1);
      v.add(charset2);
      return v.iterator();
    }
  }

  // Another mock charset provider attempting to provide the built-in charset "ascii" again.
  public static class MockCharsetProviderASCII extends CharsetProvider {
    public Charset charsetForName(String charsetName) {
      if ("US-ASCII".equalsIgnoreCase(charsetName) || "ASCII".equalsIgnoreCase(charsetName)) {
        return new MockCharset("US-ASCII", new String[] { "ASCII" });
      }
      return null;
    }

    public Iterator charsets() {
      Vector v = new Vector();
      v.add(new MockCharset("US-ASCII", new String[] { "ASCII" }));
      return v.iterator();
    }
  }
}
