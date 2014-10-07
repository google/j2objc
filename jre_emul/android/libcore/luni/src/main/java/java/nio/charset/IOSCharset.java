/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio.charset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * iOS native charset support. 
 * 
 * @author Tom Ball
 */
class IOSCharset extends Charset {

  // The NSStringEncoding enum value for this charset.
  private long nsEncoding;
  
  // The maximum number of bytes in any character.
  private float charBytes;

  private static Map<String, IOSCharset> encodings = new HashMap<String, IOSCharset>();

  static {
    // Encodings from NSString.h.
    //
    // All encoding names must be uppercase, so map lookups are case-insensitive.
    addEncoding(/* NSASCIIStringEncoding */ 1L, "US-ASCII", new String[] {
        "cp367", "ascii7", "ISO646-US", "646", "csASCII", "us", "iso_646.irv:1983", 
        "ISO_646.irv:1991", "IBM367", "ASCII", "default", "ANSI_X3.4-1986", 
        "ANSI_X3.4-1968", "iso-ir-6", "ANSI_X3.4-1968", "ANSI_X3.4-1986" }, 1);
    addEncoding(/* NSJapaneseEUCStringEncoding */ 3L, "EUC-JP", new String[] { 
        "eucjis", "Extended_UNIX_Code_Packed_Format_for_Japanese", "x-eucjp", "eucjp", 
        "csEUCPkdFmtjapanese", "x-euc-jp", "euc_jp" }, 2);
    addEncoding(/* NSUTF8StringEncoding */ 4L, "UTF-8",
        new String[] { "unicode-1-1-utf-8", "UTF8" }, 1);
    addEncoding(/* NSISOLatin1StringEncoding */ 5L, "ISO-8859-1", new String[] { 
        "csISOLatin1", "latin1", "IBM-819", "iso-ir-100", "8859_1", "ISO_8859-1:1987", 
        "ISO_8859-1", "819", "l1", "ISO8859-1", "IBM819", "ISO_8859_1", "ISO8859_1", "cp819",
        "ISO8859-1" }, 1);
    addEncoding(/* NSSymbolStringEncoding */ 6L, "X-MACSYMBOL", new String[] { "MacSymbol" }, 1);
    addEncoding(/* NSShiftJISStringEncoding */ 8L, "SHIFT_JIS", 
        new String[] { "x-sjis", "shift_jis", "sjis", "ms_kanji", "shift-jis", "csShiftJIS" }, 1);
    addEncoding(/* NSISOLatin2StringEncoding */ 9L, "ISO-8859-2", 
        new String[] { "csISOLatin2", "iso-ir-101", "ibm-912", "8859_2", "l2", "ISO_8859-2", 
        "ibm912", "912", "ISO8859-2", "latin2", "iso8859_2", "ISO_8859-2:1987", "cp912" }, 1); 
    addEncoding(/* NSUnicodeStringEncoding */ 10L, "UTF-16", 
        new String[] { "utf16", "Unicode", "UnicodeBig", "UTF_16", "unicode" }, 2);
    addEncoding(/* NSWindowsCP1251StringEncoding */ 11L, "WINDOWS-1251", 
        new String[] { "ansi-1251", "cp5347", "cp1251" }, 1);
    addEncoding(/* NSWindowsCP1252StringEncoding */ 12L, "WINDOWS-1252", 
        new String[] { "cp1252", "cp5348" }, 1);
    addEncoding(/* NSWindowsCP1253StringEncoding */ 13L, "WINDOWS-1253", 
        new String[] { "cp5349", "cp1253" }, 1);
    addEncoding(/* NSWindowsCP1254StringEncoding */ 14L, "WINDOWS-1254", 
        new String[] { "cp5350", "cp1254" }, 1);
    addEncoding(/* NSWindowsCP1250StringEncoding */ 15L, "WINDOWS-1250", 
        new String[] { "cp1250", "cp5346" }, 1);
    addEncoding(/* NSISO2022JPStringEncoding */ 21L, "ISO-2022-JP", 
        new String[] { "jis_encoding", "csjisencoding", "jis", "iso2022jp", "csISO2022JP" }, 2);
    addEncoding(/* NSMacOSRomanStringEncoding */ 30L, "X-MACROMAN", new String[] { "MacRoman" }, 1);
    addEncoding(/* NSKOI8RStringEncoding */ 50L, "KOI8-R", 
        new String[] { "cskoi8r", "koi8_r", "koi8" }, 1);
    addEncoding(/* NSISOLatin3StringEncoding */ 51L, "ISO-8859-3", 
        new String[] { "ibm-913", "latin3", "csISOLatin3", "iso-ir-109", "l3", "iso8859_3", 
        "ISO_8859-3:1988", "8859_3", "ibm913", "ISO8859-3", "ISO_8859-3", "913", "cp913" }, 1);
    addEncoding(/* NSISOLatin4StringEncoding */ 52L, "ISO-8859-4", 
        new String[] { "iso-ir-110", "iso8859-4", "ibm914", "ibm-914", "csISOLatin4", "l4", 
        "914", "8859_4", "latin4", "ISO_8859-4", "ISO_8859-4:1988", "iso8859_4", "cp914" }, 1);
    addEncoding(/* NSISOCyrillicStringEncoding */ 22L, "ISO-8859-5", 
        new String[] { "cp915", "ISO8859-5", "ibm915", "ISO_8859-5:1988", "ibm-915", "8859_5", 
        "915", "cyrillic", "iso8859_5", "ISO_8859-5", "iso-ir-144", "csISOLatinCyrillic" }, 1);
    addEncoding(/* NSISOArabicStringEncoding */ 53L, "ISO-8859-6", 
        new String[] { "arabic", "ibm1089", "iso8859_6", "iso-ir-127", "8859_6", "cp1089", 
        "ECMA-114", "ISO_8859-6", "csISOLatinArabic", "1089", "ibm-1089", "ISO8859-6", 
        "ASMO-708", "ISO_8859-6:1987" }, 1);
    addEncoding(/* NSISOGreekStringEncoding */ 54L, "ISO-8859-7", 
        new String[] { "iso8859-7", "sun_eu_greek", "csISOLatinGreek", "813", "ISO_8859-7", 
        "ibm-813", "ISO_8859-7:1987", "greek", "greek8", "iso8859_7", "ECMA-118", "iso-ir-126", 
        "8859_7", "cp813", "ibm813", "ELOT_928" }, 1);
    addEncoding(/* NSISOHebrewStringEncoding */ 55L, "ISO-8859-8", 
        new String[] { "ibm916", "cp916", "csISOLatinHebrew", "ISO_8859-8", "ISO8859-8", 
        "ibm-916", "iso8859_8", "hebrew", "916", "iso-ir-138", "ISO_8859-8:1988", "8859_8" }, 1);
    addEncoding(/* NSISOLatin5StringEncoding */ 57L, "ISO-8859-9", 
        new String[] { "ISO_8859-9", "920", "iso8859_9", "csISOLatin5", "l5", "8859_9", "latin5", 
        "ibm920", "iso-ir-148", "ISO_8859-9:1989", "ISO8859-9", "cp920", "ibm-920" }, 1);
    addEncoding(/* NSISOLatin6StringEncoding */ 58L, "ISO-8859-10", 
        new String[] { "ISO_8859-10", "ISO_8859-10:1992", "ISO-IR-157", "LATIN6", "L6", 
        "csISOLatin6", "ISO8859-10" }, 1);
    addEncoding(/* NSISOThaiStringEncoding */ 59L, "X-ISO-8859-11", 
        new String[] { "iso-8859-11", "iso8859_11" }, 2);
    addEncoding(/* NSISOLatin7StringEncoding */ 61L, "ISO-8859-13", 
        new String[] { "8859_13", "iso8859_13", "iso_8859-13", "ISO8859-13" }, 1);
    addEncoding(/* NSISOLatin8StringEncoding */ 62L, "ISO-8859-14",
        new String[] { "ISO_8859-14", "ISO_8859-14:1998", "ISO-IR-199", "LATIN8", "L8" }, 1);
    addEncoding(/* NSISOLatin9StringEncoding */ 63L, "ISO-8859-15", 
        new String[] { "IBM923", "8859_15", "ISO_8859-15", "ISO-8859-15", "L9", "ISO8859-15", 
        "ISO8859_15_FDIS", "923", "LATIN0", "csISOlatin9", "LATIN9", "csISOlatin0", "IBM-923", 
        "ISO8859_15", "cp923" }, 1);
    addEncoding(/* NSGB2312StringEncoding */ 56L, "GB2312", 
        new String[] { "euc-cn", "x-EUC-CN", "gb2312-1980", "gb2312", "gb2312-80", "EUC_CN", 
        "euccn" }, 2);
    addEncoding(/* NSUTF7StringEncoding */ 64L, "UNICODE-1-1-UTF-7", 
        new String[] { "UTF-7", "TF-7", "u7" }, 1);
    addEncoding(/* NSGSM0338StringEncoding */ 65L, "GSM0338", new String[] { "gsm0338", "gsm" }, 1);
    addEncoding(/* NSBIG5StringEncoding */ 66L, "BIG5", new String[] { "csBig5" }, 2);
    addEncoding(/* NSKoreanEUCStringEncoding */ 67L, "EUC-KR", 
        new String[] { "5601", "ksc5601-1987", "ksc5601_1987", "euckr", "ksc5601", "ksc_5601", 
        "ks_c_5601-1987", "euc_kr", "csEUCKR" }, 2);
    addEncoding(/* NSUTF16BigEndianStringEncoding */ 0x90000100L, "UTF-16BE", 
        new String[] { "X-UTF-16BE", "UTF_16BE", "ISO-10646-UCS-2", "UnicodeBigUnmarked" }, 2);
    addEncoding(/* NSUTF16LittleEndianStringEncoding */ 0x94000100L, "UTF-16LE", 
        new String[] { "UnicodeLittleUnmarked", "UTF_16LE", "X-UTF-16LE" }, 2);
    addEncoding(/* NSUTF32StringEncoding */ 0x8c000100L, "UTF-32", 
        new String[] { "UTF32", "UTF_32" }, 4);
    addEncoding(/* NSUTF32BigEndianStringEncoding */ 0x98000100L, "UTF-32BE", 
        new String[] { "X-UTF-32BE", "UTF_32BE" }, 4);
    addEncoding(/* NSUTF32LittleEndianStringEncoding */ 0x9c000100L, "UTF-32LE", 
        new String[] { "X-UTF-32LE", "UTF_32LE" }, 4);
  }
  
  private static void addEncoding(long encoding, String name, String[] aliases, float charBytes) {
    IOSCharset cs = new IOSCharset(encoding, name, aliases, charBytes);
    encodings.put(name, cs);
  }

  protected IOSCharset(long nsEncoding, String canonicalName, String[] aliases, float charBytes) {
    super(canonicalName, aliases);
    this.nsEncoding = nsEncoding;
    this.charBytes = charBytes;
  }
  
  public long nsEncoding() {
    return nsEncoding;
  }

  @Override
  public boolean contains(Charset charset) {
    return false;
  }

  @Override
  public CharsetEncoder newEncoder() {
    return new IOSCharsetEncoder(this, charBytes);
  }

  @Override
  public CharsetDecoder newDecoder() {
    return new IOSCharsetDecoder(this);
  }

  static Set<String> getAvailableCharsetNames() {
    return encodings.keySet();
  }

  static Charset charsetForName(String charsetName) {
    // See if an encoding was requested by name.
    Charset result = encodings.get(charsetName.toUpperCase());
    if (result != null) {
      return result;
    }
    
    // Scan aliases.
    for (IOSCharset cs : encodings.values()) {
      for (String s : cs.aliases()) {
        if (s.equalsIgnoreCase(charsetName)) {
          return cs;
        }
      }
    }
    
    return null;
  }
}
