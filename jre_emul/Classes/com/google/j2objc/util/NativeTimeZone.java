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

package com.google.j2objc.util;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*-[
#import "IOSClass.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/util/GregorianCalendar.h"
#import "java/util/GregorianCalendar.h"
]-*/

/**
 * An NSTimeZone-backed concrete TimeZone implementation that provides daylight saving time (DST)
 * and historical time zone offsets from the native iOS/OS X time zone database.
 *
 * @author Lukhnos Liu
 */
public final class NativeTimeZone extends TimeZone {

  // Constants for quick Gregorian calendar computations; these are only used by {@link
  // #getOffset(int, int, int, int, int, int)} and are adapted from libcore's ZoneInfo. The
  // MILLISECONDS_PER_400_YEARS constant reflects the fact that there are only 97 leap years for
  // every 400 years. See the linked method for a brief summary of the leap year rule.
  private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
  private static final long MILLISECONDS_PER_400_YEARS =
      MILLISECONDS_PER_DAY * (400 * 365 + 100 - 3);
  private static final long UNIX_OFFSET = 62167219200000L;
  private static final int[] NORMAL = new int[] {
    0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334,
  };
  private static final int[] LEAP = new int[] {
    0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335,
  };

  private final Object nativeTimeZone;
  private final int rawOffset;
  private final int dstSavings;
  private final boolean useDaylightTime;

  static {
    // Observe the native NSSystemTimeZoneDidChangeNotification so that we can flush TimeZone's
    // cached default time zone upon system time zone change.
    setUpTimeZoneDidChangeNotificationHandler();
  }

  public static native String[] getAvailableNativeTimeZoneNames() /*-[
    NSArray *timeZones = [NSTimeZone knownTimeZoneNames];
    return [IOSObjectArray arrayWithNSArray:timeZones type:NSString_class_()];
  ]-*/;


  public static native NativeTimeZone get(String name) /*-[
    NSTimeZone *timeZone = [NSTimeZone timeZoneWithName:name];
    if (timeZone == nil) {
      return nil;
    }
    return [ComGoogleJ2objcUtilNativeTimeZone getWithNativeTimeZoneWithId:timeZone];
  ]-*/;

  public static native NativeTimeZone getDefaultNativeTimeZone() /*-[
    NSTimeZone *timeZone = [NSTimeZone defaultTimeZone];
    if (timeZone == nil) {  // Unlikely, but just to be defensive.
      return nil;
    }
    return [ComGoogleJ2objcUtilNativeTimeZone getWithNativeTimeZoneWithId:timeZone];
  ]-*/;

  public static native NativeTimeZone getWithNativeTimeZone(Object nativeTimeZone) /*-[
    NSTimeZone *tz = (NSTimeZone *)nativeTimeZone;
    NSDate *now = [NSDate date];
    NSInteger offset = [tz secondsFromGMTForDate:now];
    NSTimeInterval dstOffset = [tz daylightSavingTimeOffsetForDate:now];

    // The DST offset is relative to the current offset, and hence the math here.
    jint rawOffset = (jint)(offset * 1000) - (jint)(dstOffset * 1000.0);

    NSDate *nextTransition = [tz nextDaylightSavingTimeTransitionAfterDate:now];
    jint dstSavings;
    jboolean useDaylightTime;
    if (nextTransition) {
      NSTimeInterval nextDstOffset = [tz daylightSavingTimeOffsetForDate:nextTransition];

      // This is a simplified assumption. Technically, there's nothing in the TZ rules
      // that says you can't have a +1 transition tomorrow, and a +2 the day after. This
      // is why in more modern time libraries, there is no longer the notion of a fixed
      // DST offset.
      NSTimeInterval fixedDstOffset = (dstOffset != 0) ? dstOffset : nextDstOffset;

      // And the offset is always positive regardless the hemisphere the TZ is in.
      dstSavings = fabs(fixedDstOffset) * 1000;
      useDaylightTime = true;
    } else {
      dstSavings = 0;
      useDaylightTime = false;
    }

    return
      create_ComGoogleJ2objcUtilNativeTimeZone_initWithId_withNSString_withInt_withInt_withBoolean_(
        nativeTimeZone, tz.name, rawOffset, dstSavings, useDaylightTime);
  ]-*/;

  private static native void setUpTimeZoneDidChangeNotificationHandler() /*-[
    [[NSNotificationCenter defaultCenter] addObserver:[ComGoogleJ2objcUtilNativeTimeZone class]
                                             selector:@selector(handleTimeZoneChangeWithId:)
                                                 name:NSSystemTimeZoneDidChangeNotification
                                               object:nil];
  ]-*/;

  private static void handleTimeZoneChange(Object notification) {
    TimeZone.setDefault(null);
  }

  /**
   * Create an NSTimeZone-backed TimeZone instance.
   *
   * @param nativeTimeZone the NSTimeZone instance.
   * @param name the native time zone's name.
   * @param rawOffset the pre-calculated raw offset (in millis) from UTC. When TimeZone was
   *                  designed, the assumption was that the rawOffset would be a constant at
   *                  all times. We pre-compute this offset using the instant when the
   *                  instance is created.
   * @param useDaylightTime whether this time zone observes DST at the moment this instance
   *                        is created.
   */
  private NativeTimeZone(Object nativeTimeZone, String name, int rawOffset, int dstSavings,
                 boolean useDaylightTime) {
    setID(name);
    this.nativeTimeZone = nativeTimeZone;
    this.rawOffset = rawOffset;
    this.dstSavings = dstSavings;
    this.useDaylightTime = useDaylightTime;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NativeTimeZone)) {
      return false;
    }

    return nativeTimeZone.equals(((NativeTimeZone) obj).nativeTimeZone);
  }

  @Override
  public native int hashCode() /*-[
    return [nativeTimeZone_ hash];
  ]-*/;

  @Override
  public boolean hasSameRules(TimeZone other) {
    if (other instanceof NativeTimeZone) {
      return compareNativeTimeZoneRules(((NativeTimeZone) other).nativeTimeZone);
    }
    return super.hasSameRules(other);
  }

  @Override
  public native int getOffset(long time) /*-[
    double interval = (double)time / 1000.0;
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:interval];
    return (jint)[(NSTimeZone *)nativeTimeZone_ secondsFromGMTForDate:date] * 1000;
  ]-*/;

  /**
   * This implementation is adapted from libcore's ZoneInfo.
   *
   * The method always assumes Gregorian calendar, and uses a simple formula to first derive the
   * instant of the local datetime arguments and then call {@link #getOffset(long)} to get the
   * actual offset. The local datetime used here is always in the non-DST time zone, i.e. the time
   * zone with the "raw" offset, as evidenced by actual JDK implementation and the code below. This
   * means it's possible to call getOffset with a practically non-existent date time, such as 2:30
   * AM, March 13, 2016, which does not exist in US Pacific Time -- it falls in the DST gap of that
   * day.
   *
   * When we compute the milliseconds for the year component, we need to take leap years into
   * consideration. According to http://aa.usno.navy.mil/faq/docs/calendars.php: "The Gregorian leap
   * year rule is: Every year that is exactly divisible by four is a leap year, except for years
   * that are exactly divisible by 100, but these centurial years are leap years if they are exactly
   * divisible by 400. For example, the years 1700, 1800, and 1900 are not leap years, but the year
   * 2000 is." Hence the rules and constants used here.
   *
   * Since this method only supports Gregorian calendar, the return value of any date before October
   * 4, 1582 is not reliable. In addition, the era and dayOfWeek arguments are not used in this
   * method.
   */
  @Override
  public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
    long calc = (year / 400) * MILLISECONDS_PER_400_YEARS;
    year %= 400;

    calc += year * (365 * MILLISECONDS_PER_DAY);
    calc += ((year + 3) / 4) * MILLISECONDS_PER_DAY;

    if (year > 0) {
        calc -= ((year - 1) / 100) * MILLISECONDS_PER_DAY;
    }

    boolean isLeap = (year == 0 || (year % 4 == 0 && year % 100 != 0));
    int[] mlen = isLeap ? LEAP : NORMAL;

    calc += mlen[month] * MILLISECONDS_PER_DAY;
    calc += (day - 1) * MILLISECONDS_PER_DAY;
    calc += millis;

    calc -= rawOffset;
    calc -= UNIX_OFFSET;

    return getOffset(calc);
  }

  @Override
  public int getRawOffset() {
    return rawOffset;
  }

  @Override
  public void setRawOffset(int offsetMillis) {
    throw new UnsupportedOperationException("Cannot set raw offset on a native TimeZone");
  }

  @Override
  public int getDSTSavings() {
    return dstSavings;
  }

  @Override
  public boolean useDaylightTime() {
    return useDaylightTime;
  }

  @Override
  public boolean inDaylightTime(Date date) {
    return getOffset(date.getTime()) != rawOffset;
  }

  @Override
  public native String getDisplayName(boolean daylight, int style, Locale locale) /*-[
    if (style != JavaUtilTimeZone_SHORT && style != JavaUtilTimeZone_LONG) {
      @throw [[[JavaLangIllegalArgumentException alloc] init] autorelease];
    }

    NSTimeZoneNameStyle zoneStyle;

    // "daylight" is defined in <time.h>, hence the renaming.
    if (daylight_ && useDaylightTime_) {
      zoneStyle = (style == JavaUtilTimeZone_SHORT) ?
        NSTimeZoneNameStyleShortDaylightSaving : NSTimeZoneNameStyleDaylightSaving;
    } else {
      zoneStyle = (style == JavaUtilTimeZone_SHORT) ?
        NSTimeZoneNameStyleShortStandard : NSTimeZoneNameStyleStandard;
    }

    // Find native locale.
    NSLocale *nativeLocale;
    if (locale) {
      NSMutableDictionary *components = [NSMutableDictionary dictionary];
      [components setObject:[locale getLanguage] forKey:NSLocaleLanguageCode];
      [components setObject:[locale getCountry] forKey:NSLocaleCountryCode];
      [components setObject:[locale getVariant] forKey:NSLocaleVariantCode];
      NSString *localeId = [NSLocale localeIdentifierFromComponents:components];
      nativeLocale = AUTORELEASE([[NSLocale alloc] initWithLocaleIdentifier:localeId]);
    } else {
      nativeLocale = [NSLocale currentLocale];
    }
    return [(NSTimeZone *) nativeTimeZone_ localizedName:zoneStyle locale:nativeLocale];
  ]-*/;

  private native boolean compareNativeTimeZoneRules(Object otherNativeTimeZone) /*-[
    // [NSTimeZone isEqualToTimeZone:] also compares names, which we don't want. Since we
    // only deal with native time zones that can be obtained with known names, we'll just
    // compare the underlying data.
    NSTimeZone *other = (NSTimeZone *)otherNativeTimeZone;
    return [((NSTimeZone *)self->nativeTimeZone_).data isEqualToData:other.data];
  ]-*/;
}
