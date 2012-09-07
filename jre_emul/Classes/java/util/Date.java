/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

/*-{
#import "java/lang/IllegalArgumentException.h"
}-*/

import java.io.Serializable;

/**
 * iOS version of java.util.Date.  No code was shared, just its public API.
 *
 * NOTE: most of java.util.Date is deprecated, so only the non-deprecated
 * public API is implemented.
 *
 * @author Tom Ball
 */
public class Date implements Cloneable, Comparable<Date>, Serializable {
  private long milliseconds;

  // TODO(user): Apple recommends caching date formatters, so do so if it
  // proves to be a performance issue in practice.

  public static native long parse(String s) /*-{
    if (!s) {
      id exception = [[JavaLangIllegalArgumentException alloc] init];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
    }
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
#if ! __has_feature(objc_arc)
    [formatter autorelease];
#endif
    [formatter setLenient:YES];
    NSDate *d = [formatter dateFromString:s];
    return (long long) ([d timeIntervalSince1970] * 1000);
  }-*/;

  public static native long UTC(int year, int month, int date, int hrs, int min,
      int sec) /*-{
    // Convert arguments into RFC-3339 string format.
    NSString *dateTimeString = [NSString stringWithFormat:@"%4d-%02d-%02dT%02d:%02d:%02dZ",
                                year + 1900, month + 1, date, hrs, min, sec];

    // Parse string, as described in Apple's Data Formatting Guide.
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
#if ! __has_feature(objc_arc)
    [formatter autorelease];
    [locale autorelease];
#endif
    [formatter setLocale:locale];
    [formatter setDateFormat:@"yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'"];
    [formatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
    NSDate *d = [formatter dateFromString:dateTimeString];
    return (long long) ([d timeIntervalSince1970] * 1000);
  }-*/;

  public Date() {
    milliseconds = now();
  }

  private static native long now() /*-{
    return (long long) ([[NSDate date] timeIntervalSince1970] * 1000);
  }-*/;

  public Date(long date) {
    milliseconds = date;
  }

  public Date(String date) {
    this(Date.parse(date));
  }

  private Date(Date other) {
    milliseconds = other.milliseconds;
  }

  public boolean after(Date when) {
    return milliseconds > when.milliseconds;
  }

  public boolean before(Date when) {
    return milliseconds < when.milliseconds;
  }

  public Object clone() {
    return new Date(this);
  }

  public int compareTo(Date other) {
    return (int) (milliseconds - other.milliseconds);
  }

  public long getTime()  {
    return milliseconds;
  }

  @Override
  public boolean equals(Object object) {
    return (object == this) ||
        (object instanceof Date) && milliseconds == ((Date) object).milliseconds;
  }

  @Override
  public int hashCode() {
    return (int) (milliseconds >>> 32) ^ (int) milliseconds;
  }


  // Deprecated methods not implemented:
  // public Date(int year, int month, int date)
  // public Date(int year, int month, int date, int hrs, int min)
  // public Date(int year, int month, int date, int hrs, int min, int sec)
  // public int getDate()
  // public int getDay()
  // public int getHours()
  // public int getMinutes()
  // public int getMonth()
  // public int getSeconds()
  // public int getTimezoneOffset()
  // public int getYear()
  // public void setDate(int date)
  // public void setHours(int hours)
  // public void setMinutes(int minutes)
  // public void setMonth(int month)
  // public void setSeconds(int seconds)
  // public void setTime(long time)
  // public void setYear(int year)
  // public String toGMTString()
  // public String toLocaleString()
}
