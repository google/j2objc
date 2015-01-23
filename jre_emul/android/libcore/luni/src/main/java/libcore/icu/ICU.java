/*
 * Copyright (C) 2008 The Android Open Source Project
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

package libcore.icu;

import java.util.LinkedHashSet;
import java.util.Locale;

/*-[
#import "java/lang/UnsupportedOperationException.h"
]-*/

/**
 * Makes ICU data accessible by pulling it from the Foundation API.
 */
public final class ICU {
    /**
     * Cache for ISO language names.
     */
    private static String[] isoLanguages;

    /**
     * Cache for ISO country names.
     */
    private static String[] isoCountries;

    /**
     * Returns an array of ISO language names (two-letter codes), fetched either
     * from ICU's database or from our memory cache.
     *
     * @return The array.
     */
    public static String[] getISOLanguages() {
        if (isoLanguages == null) {
            isoLanguages = getISOLanguagesNative();
        }
        return isoLanguages.clone();
    }

    /**
     * Returns an array of ISO country names (two-letter codes), fetched either
     * from ICU's database or from our memory cache.
     *
     * @return The array.
     */
    public static String[] getISOCountries() {
        if (isoCountries == null) {
            isoCountries = getISOCountriesNative();
        }
        return isoCountries.clone();
    }

    /**
     * Returns the appropriate {@code Locale} given a {@code String} of the form returned
     * by {@code toString}. This is very lenient, and doesn't care what's between the underscores:
     * this method can parse strings that {@code Locale.toString} won't produce.
     * Used to remove duplication.
     */
    public static Locale localeFromString(String localeName) {
        int first = localeName.indexOf('_');
        int second = localeName.indexOf('_', first + 1);
        if (first == -1) {
            // Language only ("ja").
            return new Locale(localeName);
        } else if (second == -1) {
            // Language and country ("ja_JP").
            return new Locale(localeName.substring(0, first), localeName.substring(first + 1));
        } else {
            // Language and country and variant ("ja_JP_TRADITIONAL").
            return new Locale(localeName.substring(0, first), localeName.substring(first + 1, second), localeName.substring(second + 1));
        }
    }

    public static Locale[] localesFromStrings(String[] localeNames) {
        // We need to remove duplicates caused by the conversion of "he" to "iw", et cetera.
        // Java needs the obsolete code, ICU needs the modern code, but we let ICU know about
        // both so that we never need to convert back when talking to it.
        LinkedHashSet<Locale> set = new LinkedHashSet<Locale>();
        for (String localeName : localeNames) {
            set.add(localeFromString(localeName));
        }
        return set.toArray(new Locale[set.size()]);
    }

    private static Locale[] availableLocalesCache;
    public static Locale[] getAvailableLocales() {
        if (availableLocalesCache == null) {
            availableLocalesCache = localesFromStrings(getAvailableLocalesNative());
        }
        return availableLocalesCache.clone();
    }

    public static Locale[] getAvailableBreakIteratorLocales() {
        return localesFromStrings(getAvailableBreakIteratorLocalesNative());
    }

    public static Locale[] getAvailableCalendarLocales() {
        return localesFromStrings(getAvailableCalendarLocalesNative());
    }

    public static Locale[] getAvailableCollatorLocales() {
        return localesFromStrings(getAvailableCollatorLocalesNative());
    }

    public static Locale[] getAvailableDateFormatLocales() {
        return localesFromStrings(getAvailableDateFormatLocalesNative());
    }

    public static Locale[] getAvailableDateFormatSymbolsLocales() {
        return getAvailableDateFormatLocales();
    }

    public static Locale[] getAvailableDecimalFormatSymbolsLocales() {
        return getAvailableNumberFormatLocales();
    }

    public static Locale[] getAvailableNumberFormatLocales() {
        return localesFromStrings(getAvailableNumberFormatLocalesNative());
    }

    public static native String[] getAvailableCurrencyCodes() /*-[
      NSArray *currencyCodes = [NSLocale ISOCurrencyCodes];
      return [IOSObjectArray arrayWithNSArray:currencyCodes type:NSString_class_()];
    ]-*/;

    // --- Native methods accessing iOS data.

    private static native String[] getISOLanguagesNative() /*-[
      NSArray *languages = [NSLocale ISOLanguageCodes];
      NSUInteger count = [languages count];
      NSMutableData* data = [NSMutableData dataWithLength: count * sizeof(id)];
      NSRange range = NSMakeRange(0, count);
      [languages getObjects:(__unsafe_unretained id *) data.mutableBytes range:range];
      IOSObjectArray * result =
          [IOSObjectArray arrayWithObjects:(__unsafe_unretained id *) data.mutableBytes
                                     count:(jint)count
                                      type:NSString_class_()];
      return result;
    ]-*/;

    private static native String[] getISOCountriesNative() /*-[
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
    ]-*/;

    private static native String[] getAvailableBreakIteratorLocalesNative() /*-[
      // Foundation framework doesn't support break iterators.
      return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
    ]-*/;

    private static native String[] getAvailableCollatorLocalesNative() /*-[
      // Foundation framework doesn't support collators.
      return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
    ]-*/;

    private static native String[] getAvailableLocalesNative() /*-[
      NSArray *localeIds = [NSLocale availableLocaleIdentifiers];
      return [IOSObjectArray arrayWithNSArray:localeIds type:NSString_class_()];
    ]-*/;

    private static native String[] getAvailableDateFormatLocalesNative() /*-[
      NSMutableArray *localesWithDateFormats = [NSMutableArray array];
      NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
      for (NSString *localeId in [NSLocale availableLocaleIdentifiers]) {
        NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
        [formatter setLocale:locale];
        NSString *dateFormat = [formatter dateFormat];
        if (dateFormat) {
          [localesWithDateFormats addObject:localeId];
        }
#if !__has_feature(objc_arc)
        [locale release];
#endif
      }
      return [IOSObjectArray arrayWithNSArray:localesWithDateFormats type:NSString_class_()];
    ]-*/;

    private static native String[] getAvailableCalendarLocalesNative() /*-[
      NSMutableArray *localesWithCalendarFormats = [NSMutableArray array];
      for (NSString *localeId in [NSLocale availableLocaleIdentifiers]) {
        NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
        if ([locale objectForKey:NSLocaleCalendar]) {
          [localesWithCalendarFormats addObject:localeId];
        }
#if !__has_feature(objc_arc)
        [locale release];
#endif
      }
      return [IOSObjectArray arrayWithNSArray:localesWithCalendarFormats type:NSString_class_()];
    ]-*/;

    private static native String[] getAvailableNumberFormatLocalesNative() /*-[
      NSMutableArray *localesWithNumberFormats = [NSMutableArray array];
      NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
      for (NSString *localeId in [NSLocale availableLocaleIdentifiers]) {
        NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
        [formatter setLocale:locale];
        NSString *numberFormat = [formatter positiveFormat];
        if (numberFormat) {
          [localesWithNumberFormats addObject:localeId];
        }
#if !__has_feature(objc_arc)
        [locale release];
#endif
      }
      return [IOSObjectArray arrayWithNSArray:localesWithNumberFormats type:NSString_class_()];
    ]-*/;

    public static native String getDisplayCountryNative(String countryCode, String localeId) /*-[
      NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
      NSString *country = [locale objectForKey:NSLocaleCountryCode];
#if !__has_feature(objc_arc)
      [locale release];
#endif
      return (country) ? country : countryCode;
    ]-*/;

    public static native String getDisplayLanguageNative(String languageCode, String localeId) /*-[
      NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
      NSString *language = [locale objectForKey:NSLocaleLanguageCode];
  #if !__has_feature(objc_arc)
      [locale release];
  #endif
      return (language) ? language : languageCode;
    ]-*/;

    public static native String getDisplayVariantNative(String variantCode, String localeId) /*-[
      NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
      NSString *variant = [locale objectForKey:NSLocaleVariantCode];
  #if !__has_feature(objc_arc)
      [locale release];
  #endif
      return (variant) ? variant : variantCode;
    ]-*/;

    public static native String getISO3CountryNative(String localeId) /*-[
      @throw AUTORELEASE([[JavaLangUnsupportedOperationException alloc]
                         initWithNSString:@"ISO3 codes not available on iOS"]);
      return nil;
    ]-*/;

    public static native String getISO3LanguageNative(String localeId) /*-[
      @throw AUTORELEASE([[JavaLangUnsupportedOperationException alloc]
                         initWithNSString:@"ISO3 codes not available on iOS"]);
      return nil;
    ]-*/;

    public static native String getCurrencyCode(String localeId) /*-[
      NSLocale *nativeLocale =
          AUTORELEASE([[NSLocale alloc] initWithLocaleIdentifier:localeId]);
      NSNumberFormatter *formatter = AUTORELEASE([[NSNumberFormatter alloc] init]);
      [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
      [formatter setLocale:nativeLocale];
      return [formatter currencyCode];
    ]-*/;

    public static native String getCurrencySymbol(String localeId) /*-[
      NSLocale *nativeLocale =
          AUTORELEASE([[NSLocale alloc] initWithLocaleIdentifier:localeId]);
      NSNumberFormatter *formatter = AUTORELEASE([[NSNumberFormatter alloc] init]);
      [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
      [formatter setLocale:nativeLocale];
      return [formatter currencySymbol];
    ]-*/;

    public static native int getCurrencyFractionDigits(String currencyCode) /*-[
      NSNumberFormatter *formatter = AUTORELEASE([[NSNumberFormatter alloc] init]);
      [formatter setCurrencyCode:currencyCode];
      return (int) [formatter maximumFractionDigits];
    ]-*/;
}
