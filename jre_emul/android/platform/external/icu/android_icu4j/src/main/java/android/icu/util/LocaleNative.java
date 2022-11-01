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

package android.icu.util;

import java.util.Locale;
import libcore.api.IntraCoreApi;

/**
 * Provide functionalities implemented by ICU4C for {@link java.util.Locale}, which avoids using
 * {@link android.icu.util.ULocale} to prevent circular runtime dependency.
 *
 * @hide
 */
@IntraCoreApi
public final class LocaleNative {

  private LocaleNative() {}

  /**
   * Set the default Locale in ICU4C.
   *
   * <p>Libcore's default locale is synchronized with the ICU4C's default locale. But libicu.so does
   * not expose uloc_setDefault via NDK because app can otherwise break this synchronization.
   * Instead, expose this uloc_setDefault as @IntraCoreApi called by libcore.
   *
   * @param languageTag BCP-47 language tag to be set the default locale.
   * @hide
   */
  @IntraCoreApi
  public static void setDefault(String languageTag) {
    setDefaultNative(languageTag);
  }

  private static native void setDefaultNative(String languageTag);

  /**
   * Returns localized country name.
   *
   * <p>Behaves the same as {@link android.icu.util.ULocale#getDisplayCountry(ULocale)}, but
   * implemented by ICU4C.
   *
   * @param targetLocale the locale in which to display the name.
   * @param locale the locale whose country will be displayed
   * @hide
   */
  @IntraCoreApi
  public static String getDisplayCountry(Locale targetLocale, Locale locale) {
    return getDisplayCountryNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
  }

  private static native String getDisplayCountryNative(
      String targetLanguageTag, String languageTag);

  /**
   * Returns localized language name.
   *
   * <p>Behaves the same as {@link android.icu.util.ULocale#getDisplayCountry(ULocale)}, but
   * implemented by ICU4C.
   *
   * @param targetLocale the locale in which to display the name.
   * @param locale the locale whose language will be displayed
   * @hide
   */
  @IntraCoreApi
  public static String getDisplayLanguage(Locale targetLocale, Locale locale) {
    return getDisplayLanguageNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
  }

  private static native String getDisplayLanguageNative(
      String targetLanguageTag, String languageTag);

  /**
   * Returns localized variant name.
   *
   * <p>Behaves the same as {@link android.icu.util.ULocale#getDisplayCountry(ULocale)}, but
   * implemented by ICU4C.
   *
   * @param targetLocale the locale in which to display the name.
   * @param locale the locale whose variant will be displayed
   * @hide
   */
  @IntraCoreApi
  public static String getDisplayVariant(Locale targetLocale, Locale locale) {
    return getDisplayVariantNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
  }

  private static native String getDisplayVariantNative(
      String targetLanguageTag, String languageTag);

  /**
   * Returns localized script name.
   *
   * <p>Behaves the same as {@link android.icu.util.ULocale#getDisplayScript(ULocale)} (ULocale)},
   * but implemented by ICU4C.
   *
   * @param targetLocale the locale in which to display the name.
   * @param locale the locale whose script will be displayed
   * @hide
   */
  @IntraCoreApi
  public static String getDisplayScript(Locale targetLocale, Locale locale) {
    return getDisplayScriptNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
  }

  private static native String getDisplayScriptNative(String targetLanguageTag, String languageTag);
}
