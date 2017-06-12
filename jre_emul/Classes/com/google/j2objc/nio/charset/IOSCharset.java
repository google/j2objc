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
#include "com/google/j2objc/nio/charset/IconvCharsetDecoder.h"
#include "com/google/j2objc/nio/charset/IconvCharsetEncoder.h"
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
  private final byte[] replacementBytes;

  private static Map<String, IOSCharset> encodings = new HashMap<String, IOSCharset>();

  public static final IOSCharset DEFAULT_CHARSET = getDefaultCharset();

  private IOSCharset(String canonicalName, String[] aliases, long info) {
    super(canonicalName, aliases);
    charsetInfo = info;
    replacementBytes = createReplacementBytes(info);
  }

  private static native byte[] createReplacementBytes(long infoP) /*-[
    CharsetInfo *info = (CharsetInfo *)infoP;
    return [IOSByteArray arrayWithBytes:info->replacementBytes count:info->replacementBytesCount];
  ]-*/;

  public native long cfEncoding() /*-[
    return ((CharsetInfo *)self->charsetInfo_)->cfEncoding;
  ]-*/;

  @Override
  public boolean contains(Charset charset) {
    return false;
  }

  @Override
  public native CharsetEncoder newEncoder() /*-[
    CharsetInfo *info = (CharsetInfo *)self->charsetInfo_;
    return create_ComGoogleJ2objcNioCharsetIconvCharsetEncoder_initWithJavaNioCharsetCharset_withFloat_withFloat_withByteArray_withLong_(
        self, info->averageBytesPerChar, info->maxBytesPerChar, self->replacementBytes_,
        (jlong)info->iconvName);
  ]-*/;

  @Override
  public native CharsetDecoder newDecoder() /*-[
    CharsetInfo *info = (CharsetInfo *)self->charsetInfo_;
    return create_ComGoogleJ2objcNioCharsetIconvCharsetDecoder_initWithJavaNioCharsetCharset_withFloat_withFloat_withLong_(
        self, info->averageCharsPerByte, info->maxCharsPerByte, (jlong)info->iconvName);
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
    CFStringEncoding cfEncoding;
    const char *iconvName;
    NSString *javaName;
    NSString **aliases;
    unsigned aliasCount;
    jfloat averageBytesPerChar;
    jfloat maxBytesPerChar;
    jfloat averageCharsPerByte;
    jfloat maxCharsPerByte;
    const jbyte *replacementBytes;
    unsigned replacementBytesCount;
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
  static const NSString *gb2312_aliases[] = {
      @"gb2312", @"euc-cn", @"x-EUC-CN", @"euccn", @"EUC_CN", @"gb2312-80", @"gb2312-1980" };

  static const jbyte ascii_replacement[] = { 63 };
  static const jbyte utf16be_replacement[] = { -1, -3 };
  static const jbyte utf16le_replacement[] = { -3, -1 };
  static const jbyte iso2022_replacement[] = { 33, 41 };
  static const jbyte utf32be_replacement[] = { 0, 0, -1, -3 };
  static const jbyte utf32le_replacement[] = { -3, -1, 0, 0 };

  // Encodings from NSString.h.
  //
  // All encoding names must be uppercase, so map lookups are case-insensitive.
  static const CharsetInfo iosCharsets[] = {
    { kCFStringEncodingUTF8, "UTF-8", @"UTF-8", utf8_aliases, 2,
      1.1f, 3.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingASCII, "ASCII", @"US-ASCII", ascii_aliases, 16,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingEUC_JP, "EUC-JP", @"EUC-JP", eucjp_aliases, 7,
      3.0f, 3.0f, 0.5f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingISOLatin1, "ISO-8859-1", @"ISO-8859-1", iso8859_aliases, 15,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingDOSJapanese, "SHIFT_JIS", @"SHIFT_JIS", shiftjis_aliases, 6,
      2.0f, 2.0f, 0.5f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingISOLatin2, "ISO-8859-2", @"ISO-8859-2", latin2_aliases, 13,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingUnicode, "UTF-16", @"UTF-16", utf16_aliases, 5,
      2.0f, 4.0f, 0.5f, 1.0f, utf16be_replacement, 2 },
    { kCFStringEncodingWindowsCyrillic, "CP1251", @"WINDOWS-1251", win1251_aliases, 3,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingWindowsLatin1, "CP1252", @"WINDOWS-1252", win1252_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingWindowsGreek, "CP1253", @"WINDOWS-1253", win1253_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingWindowsLatin5, "CP1254", @"WINDOWS-1254", win1254_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingWindowsLatin2, "CP1250", @"WINDOWS-1250", win1250_aliases, 2,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingISO_2022_JP, "ISO-2022-JP", @"ISO-2022-JP", iso2022_aliases, 5,
      4.0f, 8.0f, 0.5f, 1.0f, iso2022_replacement, 2 },
    { kCFStringEncodingMacRoman, "MacRoman", @"X-MACROMAN", macroman_aliases, 1,
      1.0f, 1.0f, 1.0f, 1.0f, ascii_replacement, 1 },
    { kCFStringEncodingUTF16BE, "UTF-16BE", @"UTF-16BE", utf16be_aliases, 4,
      2.0f, 2.0f, 0.5f, 1.0f, utf16be_replacement, 2 },
    { kCFStringEncodingUTF16LE, "UTF-16LE", @"UTF-16LE", utf16le_aliases, 3,
      2.0f, 2.0f, 0.5f, 1.0f, utf16le_replacement, 2 },
    // "UTF-32" is mapped to NSUTF32BigEndianStringEncoding instead of NSUTF32StringEncoding because
    // the former (strangely) encodes in little endian but decodes in big endian. The latter is a
    // closer match to Java's "UTF-32".
    { kCFStringEncodingUTF32BE, "UTF-32BE", @"UTF-32", utf32_aliases, 2,
      4.0f, 4.0f, 0.25f, 1.0f, utf32be_replacement, 4 },
    { kCFStringEncodingUTF32BE, "UTF-32BE", @"UTF-32BE", utf32be_aliases, 2,
      4.0f, 4.0f, 0.25f, 1.0f, utf32be_replacement, 4 },
    { kCFStringEncodingUTF32LE, "UTF-32LE", @"UTF-32LE", utf32le_aliases, 2,
      4.0f, 4.0f, 0.25f, 1.0f, utf32le_replacement, 4 },
    { kCFStringEncodingEUC_CN, "EUC-CN", @"GB2312", gb2312_aliases, 7,
      2.0f, 2.0f, 0.5f, 1.0f, ascii_replacement, 1 },
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
