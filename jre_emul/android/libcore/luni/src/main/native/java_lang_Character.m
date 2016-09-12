/*
 * Licensed under the Apache License, Version 2.0 (the "License") {
}
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

// JNI functions for java.lang.Character.

#include "J2ObjC_icu.h"
#include "java/lang/Character.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "jni.h"

JNIEXPORT jboolean Java_java_lang_Character_isLowerCaseImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_islower(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isUpperCaseImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isupper(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isTitleCaseImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_istitle(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isDigitImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isdigit(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isDefinedImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isdefined(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isLetterImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isalpha(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isLetterOrDigitImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isalnum(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isAlphabeticImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  // iOS only supports 16-bit characters.
  if (codePoint >= 0x8000) {
    return false;
  }
  return [[NSCharacterSet letterCharacterSet] characterIsMember:(unichar) codePoint];
}

JNIEXPORT jboolean Java_java_lang_Character_isIdeographicImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_getIntPropertyValue(codePoint, UCHAR_IDEOGRAPHIC);
}

JNIEXPORT jboolean Java_java_lang_Character_isUnicodeIdentifierStartImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isIDStart(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isUnicodeIdentifierPartImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isIDPart(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isIdentifierIgnorableImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isIDIgnorable(codePoint);
}

JNIEXPORT jint Java_java_lang_Character_toLowerCaseImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_tolower(codePoint);
}

JNIEXPORT jint Java_java_lang_Character_toUpperCaseImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_toupper(codePoint);
}

JNIEXPORT jint Java_java_lang_Character_toTitleCaseImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_totitle(codePoint);
}

JNIEXPORT jint Java_java_lang_Character_digitImpl(
    JNIEnv *env, jclass cls, jint codePoint, jint radix) {
  return u_digit(codePoint, radix);
}

JNIEXPORT jint Java_java_lang_Character_getNumericValueImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  // This is both an optimization and papers over differences between Java and ICU.
  if (codePoint < 128) {
      if (codePoint >= '0' && codePoint <= '9') {
        return codePoint - '0';
      }
      if (codePoint >= 'a' && codePoint <= 'z') {
        return codePoint - ('a' - 10);
      }
      if (codePoint >= 'A' && codePoint <= 'Z') {
        return codePoint - ('A' - 10);
      }
      return -1;
  }
  // Full-width uppercase A-Z.
  if (codePoint >= 0xff21 && codePoint <= 0xff3a) {
    return codePoint - 0xff17;
  }
  // Full-width lowercase a-z.
  if (codePoint >= 0xff41 && codePoint <= 0xff5a) {
    return codePoint - 0xff37;
  }
  return -1;
}

JNIEXPORT jboolean Java_java_lang_Character_isSpaceCharImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isJavaSpaceChar(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isWhitespaceImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isWhitespace(codePoint);
}

JNIEXPORT jint Java_java_lang_Character_getTypeImpl(JNIEnv *env, jclass cls, jint codePoint) {
  return u_charType(codePoint);
}

JNIEXPORT jbyte Java_java_lang_Character_getDirectionalityImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_charDirection(codePoint);
}

JNIEXPORT jboolean Java_java_lang_Character_isMirroredImpl(
    JNIEnv *env, jclass cls, jint codePoint) {
  return u_isMirrored(codePoint);
}

JNIEXPORT jstring Java_java_lang_Character_getNameImpl(JNIEnv *env, jclass cls, jint codePoint) {
  // iOS doesn't provide Unicode character names. A names list table would be very big,
  // so don't support character names until there is a demonstrated customer need.
  return nil;
}

jint JavaLangCharacter_codePointAtRaw(const jchar *seq, jint index, jint limit) {
  jchar high = seq[index++];
  if (index >= limit) {
    return high;
  }
  jchar low = seq[index];
  if (JavaLangCharacter_isSurrogatePairWithChar_withChar_(high, low)) {
    return JavaLangCharacter_toCodePointWithChar_withChar_(high, low);
  }
  return high;
}

jint JavaLangCharacter_codePointCountRaw(const jchar *seq, jint offset, jint count) {
  jint endIndex = offset + count;
  jint result = 0;
  for (jint i = offset; i < endIndex; i++) {
    jchar c = seq[i];
    if (JavaLangCharacter_isHighSurrogateWithChar_(c)) {
      if (++i < endIndex) {
        c = seq[i];
        if (!JavaLangCharacter_isLowSurrogateWithChar_(c)) {
          result++;
        }
      }
    }
    result++;
  }
  return result;
}

jint JavaLangCharacter_codePointBeforeRaw(const jchar *seq, jint index, jint start) {
  jchar low = seq[--index];
  if (--index < start) {
    return low;
  }
  jchar high = seq[index];
  if (JavaLangCharacter_isSurrogatePairWithChar_withChar_(high, low)) {
    return JavaLangCharacter_toCodePointWithChar_withChar_(high, low);
  }
  return low;
}

jint JavaLangCharacter_offsetByCodePointsRaw(
    const jchar *seq, jint start, jint count, jint index, jint codePointOffset) {
  jint end = start + count;
  if (index < start || index > end) {
    @throw create_JavaLangIndexOutOfBoundsException_init();
  }
  if (codePointOffset == 0) {
    return index;
  }
  if (codePointOffset > 0) {
    jint codePoints = codePointOffset;
    jint i = index;
    while (codePoints > 0) {
      codePoints--;
      if (i >= end) {
        @throw create_JavaLangIndexOutOfBoundsException_init();
      }
      if (JavaLangCharacter_isHighSurrogateWithChar_(seq[i])) {
        jint next = i + 1;
        if (next < end && JavaLangCharacter_isLowSurrogateWithChar_(seq[next])) {
          i++;
        }
      }
      i++;
    }
    return i;
  }
  jint codePoints = -codePointOffset;
  jint i = index;
  while (codePoints > 0) {
    codePoints--;
    i--;
    if (i < start) {
      @throw create_JavaLangIndexOutOfBoundsException_init();
    }
    if (JavaLangCharacter_isLowSurrogateWithChar_(seq[i])) {
      jint prev = i - 1;
      if (prev >= start && JavaLangCharacter_isHighSurrogateWithChar_(seq[prev])) {
        i--;
      }
    }
  }
  return i;
}
