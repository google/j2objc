/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.lang;

import java.lang.reflect.Method;

public class CharacterTest extends junit.framework.TestCase {
  public void test_valueOfC() {
    // The JLS requires caching for chars between "\u0000 to \u007f":
    // http://java.sun.com/docs/books/jls/third_edition/html/conversions.html#5.1.7
    // Harmony caches 0-512 and tests for this behavior, so we suppress that test and use this.
    for (char c = '\u0000'; c <= '\u007f'; ++c) {
      Character e = new Character(c);
      Character a = Character.valueOf(c);
      assertEquals(e, a);
      assertSame(Character.valueOf(c), Character.valueOf(c));
    }
    for (int c = '\u0080'; c <= Character.MAX_VALUE; ++c) {
      assertEquals(new Character((char) c), Character.valueOf((char) c));
    }
  }

  public void test_isBmpCodePoint() throws Exception {
    assertTrue(Character.isBmpCodePoint(0x0000));
    assertTrue(Character.isBmpCodePoint(0x0666));
    assertTrue(Character.isBmpCodePoint(0xffff));
    assertFalse(Character.isBmpCodePoint(0x10000));
    assertFalse(Character.isBmpCodePoint(-1));
    assertFalse(Character.isBmpCodePoint(Integer.MAX_VALUE));
    assertFalse(Character.isBmpCodePoint(Integer.MIN_VALUE));
  }

  public void test_isSurrogate() throws Exception {
    assertFalse(Character.isSurrogate('\u0000'));
    assertFalse(Character.isSurrogate('\u0666'));
    assertFalse(Character.isSurrogate((char) (Character.MIN_SURROGATE - 1)));
    for (char ch = Character.MIN_SURROGATE; ch <= Character.MAX_SURROGATE; ++ch) {
      assertTrue(Character.isSurrogate(ch));
    }
    assertFalse(Character.isSurrogate((char) (Character.MAX_SURROGATE + 1)));
  }

  public void test_highSurrogate() throws Exception {
    // The behavior for non-supplementary code points (like these two) is undefined.
    // These are the obvious results if you don't do anything special.
    assertEquals(0xd7c0, Character.highSurrogate(0x0000));
    assertEquals(0xd7c1, Character.highSurrogate(0x0666));
    // These two tests must pass, though.
    assertEquals(0xd800, Character.highSurrogate(0x010000));
    assertEquals(0xdbff, Character.highSurrogate(0x10ffff));
  }

  public void test_lowSurrogate() throws Exception {
    // The behavior for non-supplementary code points (like these two) is undefined.
    // These are the obvious results if you don't do anything special.
    assertEquals(0xdc00, Character.lowSurrogate(0x0000));
    assertEquals(0xde66, Character.lowSurrogate(0x0666));
    // These two tests must pass, though.
    assertEquals(0xdc00, Character.lowSurrogate(0x010000));
    assertEquals(0xdfff, Character.lowSurrogate(0x10ffff));
  }

  /*
  TODO(tball): enable when Unicode character names are supported.
  public void test_getName() throws Exception {
    // Character.getName requires the corresponding ICU data.
    // Changed from "NULL" and "BELL" by Unicode 49.2
    assertEquals("<control-0000>", Character.getName(0x0000));
    assertEquals("<control-0007>", Character.getName(0x0007));
    assertEquals("LATIN SMALL LETTER L", Character.getName('l'));
    // This changed name from Unicode 1.0. Used to be "OPENING...".
    assertEquals("LEFT CURLY BRACKET", Character.getName('{'));
    assertEquals("ARABIC-INDIC DIGIT SIX", Character.getName(0x0666));
    assertEquals("LINEAR B SYLLABLE B008 A", Character.getName(0x010000));

    // Some private use code points.
    assertEquals("PRIVATE USE AREA E000", Character.getName(0xe000));
    assertEquals("SUPPLEMENTARY PRIVATE USE AREA A F0000", Character.getName(0xf0000));

    // An unassigned code point.
    assertNull(Character.getName(0x10ffff));

    try {
      Character.getName(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      Character.getName(Integer.MAX_VALUE);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      Character.getName(Integer.MIN_VALUE);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
  */

  public void test_compare() throws Exception {
    assertEquals(0, Character.compare('a', 'a'));
    assertTrue(Character.compare('a', 'b') < 0);
    assertTrue(Character.compare('b', 'a') > 0);
  }

  public void test_UnicodeBlock_all() throws Exception {
    for (int i = 0; i <= 0x100000; ++i) {
      Character.UnicodeBlock.of(i);
    }
  }

  public void test_UnicodeBlock_of() throws Exception {
    assertEquals(Character.UnicodeBlock.BASIC_LATIN, Character.UnicodeBlock.of(1));
    assertEquals(Character.UnicodeBlock.HANGUL_JAMO, Character.UnicodeBlock.of(0x1100));
    assertEquals(Character.UnicodeBlock.CYPRIOT_SYLLABARY, Character.UnicodeBlock.of(0x10800));
    assertEquals(Character.UnicodeBlock.VARIATION_SELECTORS_SUPPLEMENT, Character.UnicodeBlock.of(0xe0100));
    // Unicode 4.1.
    assertEquals(Character.UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION, Character.UnicodeBlock.of(0x1d200));
    // Unicode 5.0.
    assertEquals(Character.UnicodeBlock.NKO, Character.UnicodeBlock.of(0x07c0));
    // Unicode 5.1.
    assertEquals(Character.UnicodeBlock.SUNDANESE, Character.UnicodeBlock.of(0x1b80));
    // Unicode 5.2.
    assertEquals(Character.UnicodeBlock.SAMARITAN, Character.UnicodeBlock.of(0x0800));
    // Unicode 6.0.
    assertEquals(Character.UnicodeBlock.MANDAIC, Character.UnicodeBlock.of(0x0840));
  }

  public void test_UnicodeBlock_forName() throws Exception {
    // No negative tests here because icu4c is more lenient than the RI;
    // we'd allow "basic-latin", and "hangul jamo extended b", for example.
    assertEquals(Character.UnicodeBlock.BASIC_LATIN, Character.UnicodeBlock.forName("basic latin"));
    assertEquals(Character.UnicodeBlock.BASIC_LATIN, Character.UnicodeBlock.forName("BaSiC LaTiN"));
    assertEquals(Character.UnicodeBlock.BASIC_LATIN, Character.UnicodeBlock.forName("BasicLatin"));
    assertEquals(Character.UnicodeBlock.BASIC_LATIN, Character.UnicodeBlock.forName("BASIC_LATIN"));
    assertEquals(Character.UnicodeBlock.BASIC_LATIN, Character.UnicodeBlock.forName("basic_LATIN"));

    assertEquals(Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B, Character.UnicodeBlock.forName("HANGUL_JAMO_EXTENDED_B"));
    assertEquals(Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B, Character.UnicodeBlock.forName("HANGUL JAMO EXTENDED-B"));

    // Failure cases.
    try {
      Character.UnicodeBlock.forName(null);
      fail();
    } catch (NullPointerException expected) {
    }
    try {
      Character.UnicodeBlock.forName("this unicode block does not exist");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    // Renamed blocks.
    assertEquals(Character.UnicodeBlock.GREEK, Character.UnicodeBlock.forName("Greek"));
    assertEquals(Character.UnicodeBlock.GREEK, Character.UnicodeBlock.forName("Greek And Coptic"));
    assertEquals(Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS, Character.UnicodeBlock.forName("Combining Marks For Symbols"));
    assertEquals(Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS, Character.UnicodeBlock.forName("Combining Diacritical Marks For Symbols"));
    assertEquals(Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS, Character.UnicodeBlock.forName("COMBINING_MARKS_FOR_SYMBOLS"));
    assertEquals(Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS, Character.UnicodeBlock.forName("Combining Marks for Symbols"));
    assertEquals(Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS, Character.UnicodeBlock.forName("CombiningMarksforSymbols"));
    assertEquals(Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY, Character.UnicodeBlock.forName("Cyrillic Supplementary"));
    assertEquals(Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY, Character.UnicodeBlock.forName("Cyrillic Supplement"));
  }

  public void test_isAlphabetic() throws Exception {
    assertTrue(Character.isAlphabetic('A'));
    assertTrue(Character.isAlphabetic('a'));
    assertFalse(Character.isAlphabetic('1'));
    assertTrue(Character.isAlphabetic(0x113c)); // Hangul j
  }

  public void test_isIdeographic() throws Exception {
    assertFalse(Character.isIdeographic('A'));
    assertFalse(Character.isIdeographic('a'));
    assertFalse(Character.isIdeographic('1'));
    assertFalse(Character.isIdeographic(0x113c)); // Hangul j

    assertTrue(Character.isIdeographic(0x4db5));
    assertTrue(Character.isIdeographic(0x2f999));
    assertFalse(Character.isIdeographic(0x2f99)); // Kangxi radical shell
  }

  // http://b/9690863
  public void test_isDigit_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isDigit" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      assertEquals(m.invoke(null, i), Character.isDigit(i));
    }
  }

  // http://b/9690863
  public void test_isIdentifierIgnorable_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isIdentifierIgnorable" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      assertEquals(m.invoke(null, i), Character.isIdentifierIgnorable(i));
    }
  }

  // http://b/9690863
  public void test_isLetter_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isLetter" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      assertEquals(m.invoke(null, i), Character.isLetter(i));
    }
  }

  // http://b/9690863
  public void test_isLetterOrDigit_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isLetterOrDigit" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      assertEquals(m.invoke(null, i), Character.isLetterOrDigit(i));
    }
  }

  // http://b/9690863
  public void test_isLowerCase_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isLowerCase" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      assertEquals(m.invoke(null, i), Character.isLowerCase(i));
    }
  }

  // http://b/9690863
  public void test_isSpaceChar_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isSpaceChar" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      // ICU and the RI disagree about character 0x180e. Remove this special case if this changes
      // or Android decides to follow ICU exactly.
      if (i == 0x180e) {
        assertTrue(Character.isSpaceChar(i));
        assertFalse((Boolean) m.invoke(null, i));
      } else {
        assertEquals("Failed for character " + i, m.invoke(null, i), Character.isSpaceChar(i));
      }
    }
  }

  // http://b/9690863
  public void test_isUpperCase_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isUpperCase" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      assertEquals(m.invoke(null, i), Character.isUpperCase(i));
    }
  }

  // http://b/9690863
  public void test_isWhitespace_against_icu4c() throws Exception {
    Method m = Character.class.getDeclaredMethod("isWhitespace" + "Impl", int.class);
    m.setAccessible(true);
    for (int i = 0; i <= 0xffff; ++i) {
      // ICU and the RI disagree about character 0x180e. Remove this special case if this changes
      // or Android decides to follow ICU exactly.
      if (i == 0x180e) {
          assertTrue(Character.isWhitespace(i));
          assertFalse((Boolean) m.invoke(null, i));
      } else {
        assertEquals("Failed for character " + i, m.invoke(null, i), Character.isWhitespace(i));
      }
    }
  }

  // http://b/15492712
  public void test_getDirectionality() throws Exception {
    // We shouldn't throw an exception for any code point.
    for (int c = '\u0000'; c <= Character.MAX_VALUE; ++c) {
      Character.getDirectionality(c);
    }
    assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0x2066));
    assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0x2067));
    assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0x2068));
    assertEquals(Character.DIRECTIONALITY_UNDEFINED, Character.getDirectionality(0x2069));
  }

  public void testStaticHashCode() {
    assertEquals(new Character('A').hashCode(), Character.hashCode('A'));
  }

  public void testBYTES() {
    assertEquals(2, Character.BYTES);
  }
}
