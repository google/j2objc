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
#include "java/lang/Integer.h"
#include "java/lang/UnsupportedOperationException.h"
#include "java/util/Locale.h"
#include "jni.h"
#include "libcore/icu/LocaleData.h"

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
  NSString *country = [locale displayNameForKey:NSLocaleCountryCode value:targetLanguageTag];
  [locale release];
  return (country) ? country : targetLanguageTag;
}

jstring Java_libcore_icu_ICU_getDisplayLanguageNative(
    JNIEnv *env, jclass cls, jstring targetLanguageTag, jstring languageTag) {
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
  NSString *language = [locale displayNameForKey:NSLocaleLanguageCode value:targetLanguageTag];
  [locale release];
  return (language) ? language : targetLanguageTag;
}

jstring Java_libcore_icu_ICU_getDisplayVariantNative(
    JNIEnv *env, jclass cls, jstring targetLanguageTag, jstring languageTag) {
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
  NSString *variant = [locale displayNameForKey:NSLocaleVariantCode value:targetLanguageTag];
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

jchar GetNumberPropChar(CFNumberFormatterRef nf, CFStringRef propKey, jchar defaultVal) {
  jchar result = defaultVal;
  CFStringRef propertyStr = CFNumberFormatterCopyProperty(nf, propKey);
  if (propertyStr != NULL) {
    if (CFStringGetLength(propertyStr) > 0) {
      result = CFStringGetCharacterAtIndex(propertyStr, 0);
    }
    CFRelease(propertyStr);
  }
  return result;
}

jboolean Java_libcore_icu_ICU_initLocaleDataNative(
    JNIEnv *env, jclass cls, jstring languageTag, jobject resultP) {
  LibcoreIcuLocaleData *result = (LibcoreIcuLocaleData *)resultP;
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
  CFLocaleRef cfLocale = (CFLocaleRef)locale;
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  [dateFormatter setLocale:locale];
  IOSClass *stringClass = NSString_class_();

  IOSObjectArray *amPm = [IOSObjectArray arrayWithLength:2 type:stringClass];
  [amPm replaceObjectAtIndex:0 withObject:[dateFormatter AMSymbol]];
  [amPm replaceObjectAtIndex:1 withObject:[dateFormatter PMSymbol]];
  LibcoreIcuLocaleData_set_amPm_(result, amPm);

  NSArray *symbols = [dateFormatter eraSymbols];
  IOSObjectArray *eras;
  if (!symbols || symbols.count == 0) {
    // Calendar doesn't have eras, so use blanks.
    eras = [IOSObjectArray arrayWithObjects:(id[]){ @"", @"" } count:2 type:NSString_class_()];
  } else if (symbols.count == 1) {
    // Calendar doesn't have "BC" style era, so substitute blank.
    eras = [IOSObjectArray arrayWithObjects:(id[]){ @"", symbols.firstObject }
                                      count:2
                                       type:NSString_class_()];
  } else {
      eras = [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  }
  LibcoreIcuLocaleData_set_eras_(result, eras);

  // Month symbols
  symbols = [dateFormatter monthSymbols];
  IOSObjectArray *longMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_longMonthNames_(result, longMonthNames);

  symbols = [dateFormatter shortMonthSymbols];
  IOSObjectArray *shortMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_shortMonthNames_(result, shortMonthNames);

  symbols = [dateFormatter veryShortMonthSymbols];
  IOSObjectArray *tinyMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_tinyMonthNames_(result, tinyMonthNames);

  symbols = [dateFormatter standaloneMonthSymbols];
  IOSObjectArray *longStandAloneMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_longStandAloneMonthNames_(result, longStandAloneMonthNames);

  symbols = [dateFormatter shortStandaloneMonthSymbols];
  IOSObjectArray *shortStandAloneMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_shortStandAloneMonthNames_(result, shortStandAloneMonthNames);

  symbols = [dateFormatter veryShortStandaloneMonthSymbols];
  IOSObjectArray *tinyStandAloneMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_tinyStandAloneMonthNames_(result, tinyStandAloneMonthNames);

  // Weekday symbols. Java weekday indices start with 1, so a pad is inserted at the beginning.
  NSMutableArray *weekdays =
      [NSMutableArray arrayWithArray:[dateFormatter weekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *longWeekdayNames = [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_longWeekdayNames_(result, longWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter shortWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *shortWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_shortWeekdayNames_(result, shortWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter veryShortWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *tinyWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_tinyWeekdayNames_(result, tinyWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter standaloneWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *longStandAloneWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_longStandAloneWeekdayNames_(result, longStandAloneWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter shortStandaloneWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *shortStandAloneWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_shortStandAloneWeekdayNames_(result, shortStandAloneWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter veryShortStandaloneWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *tinyStandAloneWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_tinyStandAloneWeekdayNames_(result, tinyStandAloneWeekdayNames);

  // Relative date names.
  [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
  [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
  [dateFormatter setDoesRelativeDateFormatting:true];
  NSDate *today = [NSDate date];
  LibcoreIcuLocaleData_set_today_(result, [dateFormatter stringFromDate:today]);
  NSTimeInterval daysSeconds = 24 * 60 * 60;
  NSDate *yesterday = [NSDate dateWithTimeInterval:-daysSeconds sinceDate:today];
  LibcoreIcuLocaleData_set_yesterday_(result, [dateFormatter stringFromDate:yesterday]);
  NSDate *tomorrow = [NSDate dateWithTimeInterval:daysSeconds sinceDate:today];
  LibcoreIcuLocaleData_set_tomorrow_(result, [dateFormatter stringFromDate:tomorrow]);
  [dateFormatter setDoesRelativeDateFormatting:false];

  // Time formats.
  [dateFormatter setDateStyle:NSDateFormatterNoStyle];
  [dateFormatter setTimeStyle:NSDateFormatterFullStyle];
  LibcoreIcuLocaleData_set_fullTimeFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setTimeStyle:NSDateFormatterLongStyle];
  LibcoreIcuLocaleData_set_longTimeFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
  LibcoreIcuLocaleData_set_mediumTimeFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
  LibcoreIcuLocaleData_set_shortTimeFormat_(result, [dateFormatter dateFormat]);

  // Date formats.
  [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
  [dateFormatter setDateStyle:NSDateFormatterFullStyle];
  LibcoreIcuLocaleData_set_fullDateFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setDateStyle:NSDateFormatterLongStyle];
  LibcoreIcuLocaleData_set_longDateFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
  LibcoreIcuLocaleData_set_mediumDateFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setDateStyle:NSDateFormatterShortStyle];
  LibcoreIcuLocaleData_set_shortDateFormat_(result, [dateFormatter dateFormat]);

  // Decimal format symbols.
  CFNumberFormatterRef nf =
      CFNumberFormatterCreate(kCFAllocatorDefault, cfLocale, kCFNumberFormatterNoStyle);
  if (!nf) {
    // Full locale description isn't recognized, try one with just language and country codes.
    languageTag = [NSString stringWithFormat:@"%@-%@", [locale languageCode], [locale countryCode]];
    locale = [[NSLocale alloc] initWithLocaleIdentifier:languageTag];
    cfLocale = (CFLocaleRef)locale;
    nf = CFNumberFormatterCreate(kCFAllocatorDefault, cfLocale, kCFNumberFormatterNoStyle);
  }
  if (nf) {
    result->zeroDigit_ = GetNumberPropChar(nf, kCFNumberFormatterZeroSymbol, '0');
    result->decimalSeparator_ = GetNumberPropChar(nf, kCFNumberFormatterDecimalSeparator, '.');
    result->groupingSeparator_ = GetNumberPropChar(nf, kCFNumberFormatterGroupingSeparator, ',');
    result->patternSeparator_ = ';';  // There is no iOS API to fetch a locale-specific version.
    LibcoreIcuLocaleData_setAndConsume_percent_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterPercentSymbol));
    result->perMill_ = GetNumberPropChar(nf, kCFNumberFormatterPerMillSymbol, '0');
    result->monetarySeparator_ =
        GetNumberPropChar(nf, kCFNumberFormatterCurrencyGroupingSeparator, ',');
    LibcoreIcuLocaleData_setAndConsume_minusSign_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterMinusSign));
    LibcoreIcuLocaleData_setAndConsume_exponentSeparator_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterExponentSymbol));
    LibcoreIcuLocaleData_setAndConsume_infinity_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterInfinitySymbol));
    LibcoreIcuLocaleData_setAndConsume_NaN_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterNaNSymbol));
    LibcoreIcuLocaleData_setAndConsume_currencySymbol_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterCurrencySymbol));
    LibcoreIcuLocaleData_setAndConsume_internationalCurrencySymbol_(
        result, CFNumberFormatterCopyProperty(nf, kCFNumberFormatterInternationalCurrencySymbol));
    CFRelease(nf);

    // Number formats.
    nf = CFNumberFormatterCreate(kCFAllocatorDefault, cfLocale, kCFNumberFormatterDecimalStyle);
    CFStringRef formatStr = CFStringCreateCopy(kCFAllocatorDefault, CFNumberFormatterGetFormat(nf));
    LibcoreIcuLocaleData_setAndConsume_integerPattern_(result, (NSString *)formatStr);
    LibcoreIcuLocaleData_set_numberPattern_(result, (NSString *)formatStr);
    CFRelease(nf);
    nf = CFNumberFormatterCreate(kCFAllocatorDefault, cfLocale, kCFNumberFormatterCurrencyStyle);
    formatStr = CFStringCreateCopy(kCFAllocatorDefault, CFNumberFormatterGetFormat(nf));
    LibcoreIcuLocaleData_setAndConsume_currencyPattern_(result, (NSString *)formatStr);
    CFRelease(nf);
    nf = CFNumberFormatterCreate(kCFAllocatorDefault, cfLocale, kCFNumberFormatterPercentStyle);
    formatStr = CFStringCreateCopy(kCFAllocatorDefault, CFNumberFormatterGetFormat(nf));
    LibcoreIcuLocaleData_setAndConsume_percentPattern_(result, (NSString *)formatStr);
    CFRelease(nf);
  }

  // Calendar data.
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSLocale *currentLocale = [calendar locale];
  [calendar setLocale:locale];
  JavaLangInteger *firstWeekday = JavaLangInteger_valueOfWithInt_((int) [calendar firstWeekday]);
  LibcoreIcuLocaleData_set_firstDayOfWeek_(result, firstWeekday);
  JavaLangInteger *minimalDays =
      JavaLangInteger_valueOfWithInt_((int) [calendar minimumDaysInFirstWeek]);
  LibcoreIcuLocaleData_set_minimalDaysInFirstWeek_(result, minimalDays);
  [calendar setLocale:currentLocale];

  [locale release];
  [dateFormatter release];
  return true;
}
