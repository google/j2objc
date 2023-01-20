/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.icu.util;

import android.icu.util.Calendar;
import android.icu.util.ULocale;
import java.util.Objects;
import libcore.api.IntraCoreApi;

/**
 * Provide extra functionalities on top of {@link Calendar} public APIs.
 *
 * @hide
 */
@IntraCoreApi
public class ExtendedCalendar {

  private final Calendar calendar;
  private final ULocale uLocale;

  private ExtendedCalendar(ULocale uLocale) {
    this.uLocale = uLocale;
    this.calendar = Calendar.getInstance(uLocale);
  }

  /**
   * Get an instance
   *
   * @param uLocale non-null ULocale
   * @hide
   */
  @IntraCoreApi
  public static ExtendedCalendar getInstance(ULocale uLocale) {
    Objects.requireNonNull(uLocale);
    return new ExtendedCalendar(uLocale);
  }

  /**
   * Returns the {@link android.icu.text.DateFormat} pattern for the given date and time styles.
   * Similiar to {@link Calendar#getDateTimeFormat(int, int, ULocale)} but returns the pattern
   * string instead of an instance of {@link android.icu.text.DateFormat}.
   *
   * @see {@link Calendar#getDateTimeFormat(int, int, ULocale)} for the style parameters.
   * @hide
   */
  @IntraCoreApi
  public String getDateTimePattern(int dateStyle, int timeStyle) {
    return Calendar.getDateTimeFormatString(uLocale, calendar.getType(), dateStyle, timeStyle);
  }
}
