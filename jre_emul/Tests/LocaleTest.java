/*
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

import java.util.Locale;
import junit.framework.TestCase;

/** java.util.Locale regression tests. */
public class LocaleTest extends TestCase {

  private static final String USER_LOCALE_PROPERTY = "user.locale";

  // Issue 895: Locale does not carry the script code of the runtime environment.
  public void testLocale() {
    String defaultLocale = System.getProperty(USER_LOCALE_PROPERTY);

    assertLocale("zh-Hant-HK", "zh", "", "Hant", "HK");
    assertLocale("zh-Hans-SG", "zh", "", "Hans", "SG");
    assertLocale("fr-CA", "fr", "", "", "CA");
    assertLocale("en-US-POSIX", "en", "POSIX", "", "US");

    if (defaultLocale == null) {
      System.clearProperty(USER_LOCALE_PROPERTY);
    } else {
      System.setProperty(USER_LOCALE_PROPERTY, defaultLocale);
    }
  }

  private void assertLocale(
      String languageTag, String language, String variant, String script, String country) {
    System.setProperty(USER_LOCALE_PROPERTY, languageTag);

    // This method is private in JRE but public in libcore (made visible for testing).
    Locale locale = Locale.initDefault();

    assertEquals(language, locale.getLanguage());
    assertEquals(variant, locale.getVariant());
    assertEquals(script, locale.getScript());
    assertEquals(country, locale.getCountry());
  }
}
