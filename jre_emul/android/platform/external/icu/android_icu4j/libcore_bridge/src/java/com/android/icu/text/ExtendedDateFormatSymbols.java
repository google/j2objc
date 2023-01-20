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

package com.android.icu.text;

import android.icu.text.DateFormatSymbols;
import android.icu.text.DateFormatSymbols.AospExtendedDateFormatSymbols;
import android.icu.util.ULocale;
import libcore.api.IntraCoreApi;

/**
 * Provides extra data not in {@link DateFormatSymbols}.
 *
 * @hide used by {@link java.time.format.DateTimeTextProvider}.
 */
@IntraCoreApi
public class ExtendedDateFormatSymbols {

  private final AospExtendedDateFormatSymbols aospExtendedDfs;

  private ExtendedDateFormatSymbols(AospExtendedDateFormatSymbols extendedDfs) {
    aospExtendedDfs = extendedDfs;
  }

  /**
   * Get an instance.
   *
   * @hide
   */
  @IntraCoreApi
  public static ExtendedDateFormatSymbols getInstance(ULocale locale) {
    return new ExtendedDateFormatSymbols(DateFormatSymbols.getExtendedInstance(locale));
  }

  /**
   * Get the {@link DateFormatSymbols}.
   *
   * @hide
   */
  @IntraCoreApi
  public DateFormatSymbols getDateFormatSymbols() {
    return aospExtendedDfs.getDateFormatSymbols();
  }

  /**
   * Returns {@link DateFormatSymbols#NARROW} quarter strings.
   *
   * @param context {@link DateFormatSymbols#FORMAT} or {@link DateFormatSymbols#STANDALONE}.
   * @throws IllegalArgumentException for bad context or no data.
   * @hide
   */
  @IntraCoreApi
  public String[] getNarrowQuarters(int context) {
    return aospExtendedDfs.getNarrowQuarters(context);
  }
}
