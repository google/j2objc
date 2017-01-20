/*
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

#include "J2ObjC_common.h"
#include "NSString+JavaString.h"
#include "java/lang/UnsupportedOperationException.h"
#include "java/util/Locale.h"
#include "jni.h"

jarray Java_libcore_icu_ICU_getAvailableBreakIteratorLocalesNative(JNIEnv *env, jclass cls) {
  // Foundation framework doesn't support break iterators.
  // TODO(kstanger): Implement when ICU support is updated to what iOS 10 provides.
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

jarray Java_libcore_icu_ICU_getAvailableCalendarLocalesNative(JNIEnv *env, jclass cls) {
  NSMutableArray *localesWithCalendarFormats = [NSMutableArray array];
  for (NSString *localeId in [NSLocale availableLocaleIdentifiers]) {
    NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
    if ([locale objectForKey:NSLocaleCalendar]) {
      [localesWithCalendarFormats addObject:localeId];
    }
    [locale release];
  }
  return [IOSObjectArray arrayWithNSArray:localesWithCalendarFormats type:NSString_class_()];
}

jarray Java_libcore_icu_ICU_getAvailableCollatorLocalesNative(JNIEnv *env, jclass cls) {
  // Foundation framework doesn't support collators.
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

jarray Java_libcore_icu_ICU_getAvailableDateFormatLocalesNative(JNIEnv *env, jclass cls) {
  NSMutableArray *localesWithDateFormats = [NSMutableArray array];
  NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
  for (NSString *localeId in [NSLocale availableLocaleIdentifiers]) {
    NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
    [formatter setLocale:locale];
    NSString *dateFormat = [formatter dateFormat];
    if (dateFormat) {
      [localesWithDateFormats addObject:localeId];
    }
    [locale release];
  }
  return [IOSObjectArray arrayWithNSArray:localesWithDateFormats type:NSString_class_()];
}

jarray Java_libcore_icu_ICU_getAvailableLocalesNative(JNIEnv *env, jclass cls) {
  NSArray *localeIds = [NSLocale availableLocaleIdentifiers];
  return [IOSObjectArray arrayWithNSArray:localeIds type:NSString_class_()];
}

jarray Java_libcore_icu_ICU_getAvailableNumberFormatLocalesNative(JNIEnv *env, jclass cls) {
  NSMutableArray *localesWithNumberFormats = [NSMutableArray array];
  NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
  for (NSString *localeId in [NSLocale availableLocaleIdentifiers]) {
    NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
    [formatter setLocale:locale];
    NSString *numberFormat = [formatter positiveFormat];
    if (numberFormat) {
      [localesWithNumberFormats addObject:localeId];
    }
    [locale release];
  }
  return [IOSObjectArray arrayWithNSArray:localesWithNumberFormats type:NSString_class_()];
}

jarray Java_libcore_icu_ICU_getAvailableCurrencyCodes(JNIEnv *env, jclass cls) {
  NSArray *currencyCodes = [NSLocale ISOCurrencyCodes];
  return [IOSObjectArray arrayWithNSArray:currencyCodes type:NSString_class_()];
}

jstring Java_libcore_icu_ICU_getCurrencyCode(JNIEnv *env, jclass cls, jobject locale) {
  NSString *languageTag = [(JavaUtilLocale *)locale toLanguageTag];
  NSLocale *nativeLocale = [NSLocale localeWithLocaleIdentifier:languageTag];
  NSNumberFormatter *formatter = AUTORELEASE([[NSNumberFormatter alloc] init]);
  [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
  [formatter setLocale:nativeLocale];
  return [formatter currencyCode];
}

jstring Java_libcore_icu_ICU_getCurrencyDisplayName(
    JNIEnv *env, jclass cls, jstring languageTag, jstring currencyCode) {
  NSLocale *nativeLocale = AUTORELEASE([[NSLocale alloc] initWithLocaleIdentifier:languageTag]);
  return [nativeLocale displayNameForKey:NSLocaleCurrencyCode value:currencyCode];
}

jint Java_libcore_icu_ICU_getCurrencyFractionDigits(JNIEnv *env, jclass cls, jstring currencyCode) {
  NSNumberFormatter *formatter = AUTORELEASE([[NSNumberFormatter alloc] init]);
  [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
  [formatter setCurrencyCode:currencyCode];
  return (jint) [formatter maximumFractionDigits];
}

jstring Java_libcore_icu_ICU_getCurrencySymbol(
    JNIEnv *env, jclass cls, jstring languageTag, jstring currencyCode) {
  NSLocale *nativeLocale = AUTORELEASE([[NSLocale alloc] initWithLocaleIdentifier:languageTag]);
  return [nativeLocale displayNameForKey:NSLocaleCurrencySymbol value:currencyCode];
}

jstring Java_libcore_icu_ICU_getDisplayCountryNative(
    JNIEnv *env, jclass cls, jstring targetLanguageTag, jstring languageTag) {
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
  NSString *country = [locale objectForKey:NSLocaleCountryCode];
  [locale release];
  return (country) ? country : targetLanguageTag;
}

jstring Java_libcore_icu_ICU_getDisplayLanguageNative(
    JNIEnv *env, jclass cls, jstring targetLanguageTag, jstring languageTag) {
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
  NSString *language = [locale objectForKey:NSLocaleLanguageCode];
  [locale release];
  return (language) ? language : targetLanguageTag;
}

jstring Java_libcore_icu_ICU_getDisplayVariantNative(
    JNIEnv *env, jclass cls, jstring targetLanguageTag, jstring languageTag) {
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
  NSString *variant = [locale objectForKey:NSLocaleVariantCode];
  [locale release];
  return (variant) ? variant : targetLanguageTag;
}

jstring Java_libcore_icu_ICU_getDisplayScriptNative(
    JNIEnv *env, jclass cls, jstring targetLanguageTag, jstring languageTag) {
  @throw create_JavaLangUnsupportedOperationException_initWithNSString_(
      @"Display script not available on iOS");
  return nil;
}

jstring Java_libcore_icu_ICU_getISO3Country(JNIEnv *env, jclass cls, jstring languageTag) {
  @throw create_JavaLangUnsupportedOperationException_initWithNSString_(
      @"ISO3 codes not available on iOS");
  return nil;
}

jstring Java_libcore_icu_ICU_getISO3Language(JNIEnv *env, jclass cls, jstring languageTag) {
  @throw create_JavaLangUnsupportedOperationException_initWithNSString_(
      @"ISO3 codes not available on iOS");
  return nil;
}

jarray Java_libcore_icu_ICU_getISOLanguagesNative(JNIEnv *env, jclass cls) {
  NSArray *languages = [NSLocale ISOLanguageCodes];
  NSUInteger count = [languages count];
  NSMutableData* data = [NSMutableData dataWithLength:count * sizeof(id)];
  NSRange range = NSMakeRange(0, count);
  [languages getObjects:(__unsafe_unretained id *) data.mutableBytes range:range];
  IOSObjectArray * result =
      [IOSObjectArray arrayWithObjects:(__unsafe_unretained id *) data.mutableBytes
                                 count:(jint)count
                                  type:NSString_class_()];
  return result;
}

jarray Java_libcore_icu_ICU_getISOCountriesNative(JNIEnv *env, jclass cls) {
  NSArray *countries = [NSLocale ISOCountryCodes];
  NSUInteger count = [countries count];
  NSMutableData* data = [NSMutableData dataWithLength: count * sizeof(id)];
  NSRange range = NSMakeRange(0, count);
  [countries getObjects:(__unsafe_unretained id *) data.mutableBytes range:range];
  IOSObjectArray * result =
      [IOSObjectArray arrayWithObjects:(__unsafe_unretained id *) data.mutableBytes
                                 count:(jint)count
                                  type:NSString_class_()];
  return result;
}

void Java_libcore_icu_ICU_setDefaultLocale(JNIEnv *_env_, jclass _cls_, jstring languageTag) {
  [[NSUserDefaults standardUserDefaults] setObject:languageTag forKey:@"LanguageCode"];
  [[NSUserDefaults standardUserDefaults] synchronize];
}
