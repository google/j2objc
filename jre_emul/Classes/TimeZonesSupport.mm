// Copyright 2013 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  TimeZones.mm
//  Objective-C++ native methods for libcore.icu.TimeZones, created from
//  Android's libcore_icu_TimeZones.cpp.
//
//  Created by Tom Ball on 2/6/2013.
//

#include <vector>

#include "ICUSupport.h"
#include "TimeZonesSupport.h"

#include "UniquePtr.h"
#include "unicode/smpdtfmt.h"
#include "unicode/timezone.h"

static void setStringArrayElement(IOSObjectArray *array, NSUInteger i,
                                  const UnicodeString& s) {
  NSString *nsstring =
      [NSString stringWithCharacters:(unichar *) s.getBuffer()
                              length:s.length()];
  [array replaceObjectAtIndex:i withObject:nsstring];
}

@implementation TimeZonesSupport

+ (IOSObjectArray *)forCountryCode:(NSString *)countryCode {
  if (countryCode == NULL) {
    return NULL;
  }
  const char* c_str = [countryCode UTF8String];
  UniquePtr<StringEnumeration> ids(icu::TimeZone::createEnumeration(c_str));
  if (ids.get() == NULL) {
    return NULL;
  }
  UErrorCode status = U_ZERO_ERROR;
  int32_t idCount = ids->count(status);
  IOSClass *stringClass = [IOSClass classWithClass:[NSString class]];
  IOSObjectArray *result =
      [IOSObjectArray arrayWithLength:idCount
                                 type:stringClass];
  for (NSUInteger i = 0; i < idCount; ++i) {
    const UnicodeString* id_ = ids->snext(status);
    setStringArrayElement(result, i, *id_);
  }
  return result;
}

struct TimeZoneNames {
  TimeZone* tz;

  UnicodeString longStd;
  UnicodeString shortStd;
  UnicodeString longDst;
  UnicodeString shortDst;

  UDate standardDate;
  UDate daylightDate;
};

// Create an NSString instance from a ICU UnicodeString instance.
// TODO(tball): remove once the ICU C++ API references are rewritten.
NSString *stringFromUnicodeString(const UnicodeString& ustr) {
  const unichar *buffer = ustr.getBuffer();
  if (!buffer) {
    return nil;
  }
  NSString *s = [NSString stringWithCharacters:buffer
                                        length:ustr.length()];
  return s;
}

static bool isUtc(const UnicodeString& ustr) {
  NSString *s = stringFromUnicodeString(ustr);
  if (!s) {
    return NO;
  }
  return [s compare:@"Etc/UCT"] == NSOrderedSame ||
         [s compare:@"Etc/Universal"] == NSOrderedSame ||
         [s compare:@"Etc/Zulu"] == NSOrderedSame ||
         [s compare:@"UCT"] == NSOrderedSame ||
         [s compare:@"UTC"] == NSOrderedSame ||
         [s compare:@"Etc/Universal"] == NSOrderedSame ||
         [s compare:@"Etc/Zulu"] == NSOrderedSame;
}

+ (IOSObjectArray *)getZoneStringsImpl:(NSString *)localeName
                                   ids:(IOSObjectArray *)timeZoneIds {
  Locale locale = [ICUSupport getLocale:[localeName UTF8String]];
  UErrorCode status = U_ZERO_ERROR;
  UnicodeString longPattern("zzzz", 4, US_INV);
  SimpleDateFormat longFormat(longPattern, locale, status);
  // 'z' only uses "common" abbreviations. 'V' allows all known abbreviations.
  // For example, "PST" is in common use in en_US, but "CET" isn't.
  UnicodeString commonShortPattern("z", 1, US_INV);
  SimpleDateFormat shortFormat(commonShortPattern, locale, status);
  UnicodeString allShortPattern("V", 1, US_INV);
  SimpleDateFormat allShortFormat(allShortPattern, locale, status);

  UnicodeString utc("UTC", 3, US_INV);

  // Find out what year this is.
  UniquePtr<Calendar> calendar(
      Calendar::createInstance(*TimeZone::getGMT(), status));
  calendar->setTime(Calendar::getNow(), status);
  int year = calendar->get(UCAL_YEAR, status);

  // Get a UDate corresponding to February 1st this year.
  calendar->clear();
  calendar->set(UCAL_YEAR, year);
  calendar->set(UCAL_MONTH, UCAL_FEBRUARY);
  calendar->set(UCAL_DAY_OF_MONTH, 1);
  UDate date1 = calendar->getTime(status);

  // Get a UDate corresponding to July 15th this year.
  calendar->clear();
  calendar->set(UCAL_YEAR, year);
  calendar->set(UCAL_MONTH, UCAL_JULY);
  calendar->set(UCAL_DAY_OF_MONTH, 15);
  UDate date2 = calendar->getTime(status);

  UnicodeString pacific_apia("Pacific/Apia", 12, US_INV);
  UnicodeString gmt("GMT", 3, US_INV);

  // In the first pass, we get the long names for the time zone.
  // We also get any commonly-used abbreviations.
  std::vector<TimeZoneNames> table;
  NSMutableDictionary *usedAbbreviations = [NSMutableDictionary dictionary];
  NSUInteger idCount = [timeZoneIds count];
  for (NSUInteger i = 0; i < idCount; ++i) {
    NSString *zoneId = [timeZoneIds objectAtIndex:i];
    UnicodeString zone([zoneId UTF8String]);

    TimeZoneNames row;
    if (isUtc(zone)) {
      // ICU doesn't have names for the UTC zones; it just says "GMT+00:00"
      // for both long and short names. We don't want this. The best we can
      // do is use "UTC" for everything (since we don't know how to say
      // "Universal Coordinated Time").
      row.tz = NULL;
      row.longStd = row.shortStd = row.longDst = row.shortDst = utc;
      table.push_back(row);
      [usedAbbreviations setObject:stringFromUnicodeString(utc)
                            forKey:stringFromUnicodeString(utc)];
      continue;
    }

    row.tz = TimeZone::createTimeZone(zone);
    longFormat.setTimeZone(*row.tz);
    shortFormat.setTimeZone(*row.tz);

    int32_t daylightOffset;
    int32_t rawOffset;
    row.tz->getOffset(date1, false, rawOffset, daylightOffset, status);
    if (daylightOffset != 0) {
      // The TimeZone is reporting that we are in daylight time for the winter
      // date. The dates are for the wrong hemisphere, so swap them.
      row.standardDate = date2;
      row.daylightDate = date1;
    } else {
      row.standardDate = date1;
      row.daylightDate = date2;
    }

    longFormat.format(row.standardDate, row.longStd);
    shortFormat.format(row.standardDate, row.shortStd);
    longFormat.format(row.daylightDate, row.longDst);
    shortFormat.format(row.daylightDate, row.shortDst);

    if ((row.longStd == row.longDst) || (row.shortStd == row.shortDst)) {
      row.tz->getDisplayName(true, TimeZone::LONG, locale, row.longDst);
      row.tz->getDisplayName(false, TimeZone::LONG, locale, row.longStd);
      row.tz->getDisplayName(true, TimeZone::SHORT, locale, row.shortDst);
      row.tz->getDisplayName(false, TimeZone::SHORT, locale, row.shortStd);
    }

    if (zone == pacific_apia) {
      if (row.longDst.startsWith(gmt)) {
        row.longDst = "Samoa Summer Time";
      } else {
        abort();
      }
    }

    table.push_back(row);
    [usedAbbreviations setObject:stringFromUnicodeString(row.longStd)
                          forKey:stringFromUnicodeString(row.shortStd)];
    [usedAbbreviations setObject:stringFromUnicodeString(row.longDst)
                          forKey:stringFromUnicodeString(row.shortDst)];
  }

  // In the second pass, we create the Java String[][].
  // We also look for any uncommon abbreviations that don't conflict with
  // ones we've already seen.
  IOSClass *stringClass = [IOSClass classWithClass:[NSString class]];
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:idCount
                                                      type:stringClass];
  for (NSUInteger i = 0; i < table.size(); ++i) {
    TimeZoneNames& row(table[i]);
    // Did we get a GMT offset instead of an abbreviation?
    if (row.shortStd.length() > 3 && row.shortStd.startsWith(gmt)) {
      // See if we can do better...
      UnicodeString uncommonStd, uncommonDst;
      allShortFormat.setTimeZone(*row.tz);
      allShortFormat.format(row.standardDate, uncommonStd);
      if (row.tz->useDaylightTime()) {
        allShortFormat.format(row.daylightDate, uncommonDst);
      } else {
        uncommonDst = uncommonStd;
      }

      // If this abbreviation isn't already in use, we can use it.
      id abbrev =
          [usedAbbreviations objectForKey:stringFromUnicodeString(uncommonStd)];
      if (!abbrev) {
        row.shortStd = uncommonStd;
        [usedAbbreviations setObject:stringFromUnicodeString(row.longStd)
                              forKey:stringFromUnicodeString(row.shortStd)];
      }
      abbrev =
          [usedAbbreviations objectForKey:stringFromUnicodeString(uncommonDst)];
      if (!abbrev) {
        row.shortDst = uncommonDst;
        [usedAbbreviations setObject:stringFromUnicodeString(row.longDst)
                              forKey:stringFromUnicodeString(row.shortDst)];
      }
    }

    // Fill in whatever we got. We don't use the display names if they're
    // "GMT[+-]xx:xx" because icu4c doesn't use the up-to-date time zone
    // transition data, so it gets these wrong. TimeZone.getDisplayName
    // creates accurate names on demand.
    IOSObjectArray *nsRow = [IOSObjectArray arrayWithLength:5 type:stringClass];
    NSString *zone = [timeZoneIds objectAtIndex:i];
    [nsRow replaceObjectAtIndex:0 withObject:zone];
    if (!row.longStd.startsWith(gmt)) {
      setStringArrayElement(nsRow, 1, row.longStd);
    }
    if (!row.shortStd.startsWith(gmt)) {
      setStringArrayElement(nsRow, 2, row.shortStd);
    }
    if (!row.longDst.startsWith(gmt)) {
      setStringArrayElement(nsRow, 3, row.longDst);
    }
    if (!row.shortDst.startsWith(gmt)) {
      setStringArrayElement(nsRow, 4, row.shortDst);
    }
    [result replaceObjectAtIndex:i withObject:nsRow];
    delete row.tz;
  }

  return result;
}

@end
