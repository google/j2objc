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

package com.google.j2objc.nio.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*-[
#include "com/google/j2objc/nio/charset/IOSCharsetDecoder.h"
#include "com/google/j2objc/nio/charset/IOSCharsetEncoder.h"
#include "java/io/UnsupportedEncodingException.h"
#include "java/lang/System.h"
]-*/

/**
 * iOS native charset support.
 *
 * @author Tom Ball
 */
public class IOSCharset extends Charset {

  // CharsetInfo*
  private final long charsetInfo;

  private static Map<String, IOSCharset> encodings = new HashMap<String, IOSCharset>();

  public static final IOSCharset DEFAULT_CHARSET = getDefaultCharset();

  private IOSCharset(String canonicalName, String[] aliases, long info) {
    super(canonicalName, aliases);
    charsetInfo = info;
  }

  public native long nsEncoding() /*-[
    return ((CharsetInfo *)self->charsetInfo_)->nsEncoding;
  ]-*/;

  @Override
  public boolean contains(Charset charset) {
    return false;
  }

  @Override
  public native CharsetEncoder newEncoder() /*-[
    CharsetInfo *info = (CharsetInfo *)self->charsetInfo_;
    return create_ComGoogleJ2objcNioCharsetIOSCharsetEncoder_initWithJavaNioCharsetCharset_withFloat_withFloat_withLong_(
        self, info->averageBytesPerChar, info->maxBytesPerChar, info->nsEncoding);
  ]-*/;

  @Override
  public native CharsetDecoder newDecoder() /*-[
    CharsetInfo *info = (CharsetInfo *)self->charsetInfo_;
    return create_ComGoogleJ2objcNioCharsetIOSCharsetDecoder_initWithJavaNioCharsetCharset_withFloat_withFloat_withLong_(
        self, info->averageCharsPerByte, info->maxCharsPerByte, info->nsEncoding);
  ]-*/;

  public static Set<String> getAvailableCharsetNames() {
    return getEncodings().keySet();
  }

  public static Charset charsetForName(String charsetName) {
    // See if an encoding was requested by name.
    Map<String, IOSCharset> encodings = getEncodings();
    IOSCharset result = encodings.get(charsetName.toUpperCase());
    if (result != null) {
      return result;
    }

    // Scan aliases.
    for (IOSCharset cs : getEncodings().values()) {
      for (String s : cs.aliases()) {
        if (s.equalsIgnoreCase(charsetName)) {
          return cs;
        }
      }
    }

    return null;
  }

  /*-[
  typedef struct {
    NSStringEncoding nsEncoding;
    NSString *javaName;
    NSString **aliases;
    unsigned aliasCount;
    jfloat averageBytesPerChar;
    jfloat maxBytesPerChar;
    jfloat averageCharsPerByte;
    jfloat maxCharsPerByte;
  } CharsetInfo;

  static const NSString *utf8_aliases[] = { @"unicode-1-1-utf-8", @"UTF8" };
  static const NSString *ascii_aliases[] = {
      @"cp367", @"ascii7", @"ISO646-US", @"646", @"csASCII", @"us", @"iso_646.irv:1983",
      @"ISO_646.irv:1991", @"IBM367", @"ASCII", @"default", @"ANSI_X3.4-1986",
      @"ANSI_X3.4-1968", @"iso-ir-6", @"ANSI_X3.4-1968", @"ANSI_X3.4-1986" };
  static const NSString *eucjp_aliases[] = {
      @"eucjis", @"Extended_UNIX_Code_Packed_Format_for_Japanese", @"x-eucjp", @"eucjp",
      @"csEUCPkdFmtjapanese", @"x-euc-jp", @"euc_jp" };
  static const NSString *iso8859_aliases[] = {
      @"csISOLatin1", @"latin1", @"IBM-819", @"iso-ir-100", @"8859_1",
      @ "ISO_8859-1:1987", @"ISO_8859-1", @"819", @"l1", @"ISO8859-1",
      @"IBM819", @"ISO_8859_1", @"ISO8859_1", @"cp819", @"ISO8859-1" };
  static const NSString *symbol_aliases[] = { @"MacSymbol" };
  static const NSString *shiftjis_aliases[] = {
      @"x-sjis", @"shift_jis", @"sjis", @"ms_kanji", @"shift-jis", @"csShiftJIS" };
  static const NSString *latin2_aliases[] = {
      @"csISOLatin2", @"iso-ir-101", @"ibm-912", @"8859_2", @"l2", @"ISO_8859-2",
      @"ibm912", @"912", @"ISO8859-2", @"latin2", @"iso8859_2", @"ISO_8859-2:1987", @"cp912" };
  static const NSString *utf16_aliases[] = {
      @"utf16", @"Unicode", @"UnicodeBig", @"UTF_16", @"unicode" };
  static const NSString *win1251_aliases[] = { @"ansi-1251", @"cp5347", @"cp1251" };
  static const NSString *win1252_aliases[] = { @"cp1252", @"cp5348" };
  static const NSString *win1253_aliases[] = { @"cp5349", @"cp1253" };
  static const NSString *win1254_aliases[] = { @"cp5350", @"cp1254" };
  static const NSString *win1250_aliases[] = { @"cp1250", @"cp5346" };
  static const NSString *iso2022_aliases[] = {
      @"jis_encoding", @"csjisencoding", @"jis", @"iso2022jp", @"csISO2022JP" };
  static const NSString *macroman_aliases[] = { @"MacRoman" };
  static const NSString *utf16be_aliases[] = {
      @"X-UTF-16BE", @"UTF_16BE", @"ISO-10646-UCS-2", @"UnicodeBigUnmarked" };
  static const NSString *utf16le_aliases[] = {
      @"UnicodeLittleUnmarked", @"UTF_16LE", @"X-UTF-16LE" };
  static const NSString *utf32_aliases[] = { @"UTF32", @"UTF_32" };
  static const NSString *utf32be_aliases[] = { @"X-UTF-32BE", @"UTF_32BE" };
  static const NSString *utf32le_aliases[] = { @"X-UTF-32LE", @"UTF_32LE" };

  // Encodings from NSString.h.
  //
  // All encoding names must be uppercase, so map lookups are case-insensitive.
  static const CharsetInfo iosCharsets[] = {
    { NSUTF8StringEncoding, @"UTF-8", (NSString **) utf8_aliases, 2,
      1.1f, 3.0f, 1.0f, 1.0f },
    { NSASCIIStringEncoding, @"US-ASCII", (NSString **) ascii_aliases, 16,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSJapaneseEUCStringEncoding, @"EUC-JP", (NSString **) eucjp_aliases, 7,
      3.0f, 3.0f, 0.5f, 1.0f },
    { NSISOLatin1StringEncoding, @"ISO-8859-1", (NSString **) iso8859_aliases, 15,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSSymbolStringEncoding, @"X-MACSYMBOL", (NSString **) symbol_aliases, 1,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSShiftJISStringEncoding, @"SHIFT_JIS", (NSString **) shiftjis_aliases, 6,
      2.0f, 2.0f, 0.5f, 1.0f },
    { NSISOLatin2StringEncoding, @"ISO-8859-2", (NSString **) latin2_aliases, 13,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSUnicodeStringEncoding, @"UTF-16", (NSString **) utf16_aliases, 5,
      2.0f, 4.0f, 0.5f, 1.0f },
    { NSWindowsCP1251StringEncoding, @"WINDOWS-1251", (NSString **) win1251_aliases, 3,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSWindowsCP1252StringEncoding, @"WINDOWS-1252", (NSString **) win1252_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSWindowsCP1253StringEncoding, @"WINDOWS-1253", (NSString **) win1253_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSWindowsCP1254StringEncoding, @"WINDOWS-1254", (NSString **) win1254_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSWindowsCP1250StringEncoding, @"WINDOWS-1250", (NSString **) win1250_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSISO2022JPStringEncoding, @"ISO-2022-JP", (NSString **) iso2022_aliases, 5,
      4.0f, 8.0f, 0.5f, 1.0f },
    { NSMacOSRomanStringEncoding, @"X-MACROMAN", (NSString **) macroman_aliases, 1,
      1.0f, 1.0f, 1.0f, 1.0f },
    { NSUTF16BigEndianStringEncoding, @"UTF-16BE", (NSString **) utf16be_aliases, 4,
      2.0f, 2.0f, 0.5f, 1.0f },
    { NSUTF16LittleEndianStringEncoding, @"UTF-16LE", (NSString **) utf16le_aliases, 3,
      2.0f, 2.0f, 0.5f, 1.0f },
    { NSUTF32StringEncoding, @"UTF-32", (NSString **) utf32_aliases, 2,
      4.0f, 4.0f, 0.25f, 1.0f },
    { NSUTF32BigEndianStringEncoding, @"UTF-32BE", (NSString **) utf32be_aliases, 2,
      4.0f, 4.0f, 0.25f, 1.0f },
    { NSUTF32LittleEndianStringEncoding, @"UTF-32LE", (NSString **) utf32le_aliases, 2,
      4.0f, 4.0f, 0.25f, 1.0f },
  };
  static const int numIosCharsets = sizeof(iosCharsets) / sizeof(CharsetInfo);

  static ComGoogleJ2objcNioCharsetIOSCharset *addEncoding(const CharsetInfo *info) {
    IOSObjectArray *aliases = [IOSObjectArray arrayWithObjects:info->aliases
                                                         count:info->aliasCount
                                                          type:NSString_class_()];
    ComGoogleJ2objcNioCharsetIOSCharset *cs =
        create_ComGoogleJ2objcNioCharsetIOSCharset_initWithNSString_withNSStringArray_withLong_(
            info->javaName, aliases, (jlong)info);
    [ComGoogleJ2objcNioCharsetIOSCharset_encodings putWithId:info->javaName withId:cs];
    return cs;
  }
  ]-*/

  private static native IOSCharset getDefaultCharset() /*-[
    NSString *fileEncoding = JavaLangSystem_getPropertyWithNSString_(@"file.encoding");
    if (fileEncoding) {
      @try {
        return (ComGoogleJ2objcNioCharsetIOSCharset *)
            JavaNioCharsetCharset_forNameUEEWithNSString_(fileEncoding);
      }
      @catch (JavaIoUnsupportedEncodingException *e) {
        // Fall-through to use system default.
      }
    }
    // Return UTF-8 default, like JRE does.
    return addEncoding(&iosCharsets[0]);
  ]-*/;

  private static native Map<String, IOSCharset> getEncodings() /*-[
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      for (jint i = 0; i < numIosCharsets; i++) {
        addEncoding(&iosCharsets[i]);
      }
    });
    return ComGoogleJ2objcNioCharsetIOSCharset_encodings;
  ]-*/;
}
