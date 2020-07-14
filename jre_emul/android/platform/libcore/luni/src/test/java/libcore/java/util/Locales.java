/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */

package libcore.java.util;

import java.util.Locale;
import java.util.Objects;

import static java.util.Locale.Category.DISPLAY;
import static java.util.Locale.Category.FORMAT;
import static org.junit.Assert.assertEquals;

/**
 * Helper class for tests that need to temporarily change the default Locales.
 */
class Locales {
    private final Locale uncategorizedLocale;
    private final Locale displayLocale;
    private final Locale formatLocale;

    private Locales(Locale uncategorizedLocale, Locale displayLocale, Locale formatLocale) {
        this.uncategorizedLocale = uncategorizedLocale;
        this.displayLocale = displayLocale;
        this.formatLocale = formatLocale;
    }

    /**
     * Sets the specified default Locale, default DISPLAY Locale and default FORMAT Locale.
     * Every call to this method should be paired with exactly one corresponding call to
     * reset the previous values:
     * <pre>
     *     Locales locales = Locales.getAndSetDefaultForTest(Locale.US, Locale.CHINA, Locale.UK);
     *     try {
     *         ...
     *     } finally {
     *         locales.setAsDefault();
     *     }
     * </pre>
     */
    public static Locales getAndSetDefaultForTest(Locale uncategorizedLocale, Locale displayLocale,
            Locale formatLocale) {
        Locales oldLocales = getDefault();
        Locales newLocales = new Locales(uncategorizedLocale, displayLocale, formatLocale);
        newLocales.setAsDefault();
        assertEquals(newLocales, getDefault()); // sanity check
        return oldLocales;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Locales)) {
            return false;
        }
        Locales that = (Locales) obj;
        return uncategorizedLocale.equals(that.uncategorizedLocale)
                && displayLocale.equals(that.displayLocale)
                && formatLocale.equals(that.formatLocale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uncategorizedLocale, displayLocale, formatLocale);
    }

    @Override
    public String toString() {
        return "Locales[displayLocale=" + displayLocale + ", locale=" + uncategorizedLocale +
                ", formatLocale=" + formatLocale + ']';
    }

    /**
     * Reset the system's default Locale values to what they were when this
     * Locales was obtained.
     */
    public void setAsDefault() {
        // The lines below must set the Locales in this order because setDefault(Locale)
        // overwrites the other ones.
        Locale.setDefault(uncategorizedLocale);
        Locale.setDefault(DISPLAY, displayLocale);
        Locale.setDefault(FORMAT, formatLocale);
    }

    public static Locales getDefault() {
        return new Locales(
                Locale.getDefault(), Locale.getDefault(DISPLAY), Locale.getDefault(FORMAT));
    }

}
