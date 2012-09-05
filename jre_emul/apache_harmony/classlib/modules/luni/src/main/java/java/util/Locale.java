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

package java.util;

import java.io.Serializable;

/**
 * {@code Locale} represents a language/country/variant combination. It is an identifier
 * which dictates particular conventions for the presentation of information.
 * The language codes are two letter lowercase codes as defined by ISO-639. The
 * country codes are three letter uppercase codes as defined by ISO-3166. The
 * variant codes are unspecified.
 * 
 * @see ResourceBundle
 */
public final class Locale implements Cloneable, Serializable {

    private static final long serialVersionUID = 9149081749638150636L;

    // Initialize a default which is used during static
    // initialization of the default for the platform.
    private static Locale defaultLocale = new Locale();

    /**
     * Locale constant for en_CA.
     */
    public static final Locale CANADA = new Locale("en", "CA"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for fr_CA.
     */
    public static final Locale CANADA_FRENCH = new Locale("fr", "CA"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale CHINA = new Locale("zh", "CN"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for zh.
     */
    public static final Locale CHINESE = new Locale("zh", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for en.
     */
    public static final Locale ENGLISH = new Locale("en", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for fr_FR.
     */
    public static final Locale FRANCE = new Locale("fr", "FR"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for fr.
     */
    public static final Locale FRENCH = new Locale("fr", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for de.
     */
    public static final Locale GERMAN = new Locale("de", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for de_DE.
     */
    public static final Locale GERMANY = new Locale("de", "DE"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for it.
     */
    public static final Locale ITALIAN = new Locale("it", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for it_IT.
     */
    public static final Locale ITALY = new Locale("it", "IT"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for ja_JP.
     */
    public static final Locale JAPAN = new Locale("ja", "JP"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ja.
     */
    public static final Locale JAPANESE = new Locale("ja", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ko_KR.
     */
    public static final Locale KOREA = new Locale("ko", "KR"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ko.
     */
    public static final Locale KOREAN = new Locale("ko", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale PRC = new Locale("zh", "CN"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale SIMPLIFIED_CHINESE = new Locale("zh", "CN"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TAIWAN = new Locale("zh", "TW"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TRADITIONAL_CHINESE = new Locale("zh", "TW"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for en_GB.
     */
    public static final Locale UK = new Locale("en", "GB"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for en_US.
     */
    public static final Locale US = new Locale("en", "US"); //$NON-NLS-1$//$NON-NLS-2$

    static {
        String language = getUserLanguage("en");
        String region = getUserRegion("US");
        String variant = getUserVariant("");
        defaultLocale = new Locale(language, region, variant);
    }
    
    private static native String getUserLanguage(String defaultLanguage) /*-{
      NSString *language = [[NSLocale currentLocale] objectForKey:NSLocaleLanguageCode];
      if (language == nil) {
        language = @"";
      }
#if ! __has_feature(objc_arc)
      [language autorelease];
#endif
      return language;
    }-*/;
    
    private static native String getUserRegion(String defaultRegion) /*-{
      NSString *region = [[NSLocale currentLocale] objectForKey:NSLocaleCountryCode];
      if (region == nil) {
        region = @"";
      }
#if ! __has_feature(objc_arc)
      [region autorelease];
#endif
      return region;
    }-*/;

    private static native String getUserVariant(String defaultVariant) /*-{
      NSString *variant = [[NSLocale currentLocale] objectForKey:NSLocaleVariantCode];
      if (variant == nil) {
      	variant = @"";
      }
#if ! __has_feature(objc_arc)
      [variant autorelease];
#endif
      return variant;
    }-*/;
  
  
    private transient String countryCode;
    private transient String languageCode;
    private transient String variantCode;

	/**
	 * Constructs a default which is used during static initialization of the
	 * default for the platform.
	 */
	private Locale() {
		languageCode = "en"; //$NON-NLS-1$
		countryCode = "US"; //$NON-NLS-1$
		variantCode = ""; //$NON-NLS-1$
	}
	    

    /**
     * Constructs a new {@code Locale} using the specified language.
     * 
     * @param language
     *            the language this {@code Locale} represents.
     */
    public Locale(String language) {
        this(language, "", ""); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Constructs a new {@code Locale} using the specified language and country codes.
     * 
     * @param language
     *            the language this {@code Locale} represents.
     * @param country
     *            the country this {@code Locale} represents.
     */
    public Locale(String language, String country) {
        this(language, country, ""); //$NON-NLS-1$
    }

    /**
     * Constructs a new {@code Locale} using the specified language, country, and
     * variant codes.
     * 
     * @param language
     *            the language this {@code Locale} represents.
     * @param country
     *            the country this {@code Locale} represents.
     * @param variant
     *            the variant this {@code Locale} represents.
     * @throws NullPointerException
     *             if {@code language}, {@code country}, or
     *             {@code variant} is {@code null}.
     */
    public Locale(String language, String country, String variant) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        if(language.length() == 0 && country.length() == 0){
            languageCode = "";
            countryCode = "";
            variantCode = variant;
        } else {
            languageCode = language;
            // Map new language codes to the obsolete language
            // codes so the correct resource bundles will be used.
            if (languageCode.equals("he")) {//$NON-NLS-1$
                languageCode = "iw"; //$NON-NLS-1$
            } else if (languageCode.equals("id")) {//$NON-NLS-1$
                languageCode = "in"; //$NON-NLS-1$
            } else if (languageCode.equals("yi")) {//$NON-NLS-1$
                languageCode = "ji"; //$NON-NLS-1$
            }
    
            // countryCode is defined in ASCII character set
            countryCode = country;
    
            // Work around for be compatible with RI
            variantCode = variant;
        }
    }

    /**
     * Returns a new {@code Locale} with the same language, country and variant codes as
     * this {@code Locale}.
     * 
     * @return a shallow copy of this {@code Locale}.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        return new Locale(languageCode, countryCode, variantCode);
    }

    /**
     * Compares the specified object to this {@code Locale} and returns whether they are
     * equal. The object must be an instance of {@code Locale} and have the same
     * language, country and variant.
     * 
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this {@code Locale},
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Locale) {
            Locale o = (Locale) object;
            return languageCode.equals(o.languageCode)
                    && countryCode.equals(o.countryCode)
                    && variantCode.equals(o.variantCode);
        }
        return false;
    }

    /**
     * Gets the list of installed {@code Locale}. At least a {@code Locale} that is equal to
     * {@code Locale.US} must be contained in this array.
     *
     * @return an array of {@code Locale}s.
     */
    public static native Locale[] getAvailableLocales() /*-{
        NSArray *locales = [NSLocale availableLocaleIdentifiers];
        NSUInteger n = [locales count];
        IOSClass *localeType = [IOSClass classWithClass:[JavaUtilLocale class]];
        IOSObjectArray *result = [[IOSObjectArray alloc] initWithLength:n type:localeType];
        for (NSUInteger i = 0; i < n; i++) {
            NSString *localeIdentifier = [locales objectAtIndex:i];
            NSDictionary *parts = [NSLocale componentsFromLocaleIdentifier:localeIdentifier];
            NSString *language = [parts objectForKey:NSLocaleLanguageCode];
            if (!language) {
              language = @"";
            }
            NSString *country = [parts objectForKey:NSLocaleCountryCode];
            if (!country) {
              country = @"";
            }
            NSString *variant = [parts objectForKey:NSLocaleVariantCode];
            if (!variant) {
              variant = @"";
            }
            JavaUtilLocale *javaLocale = [[JavaUtilLocale alloc] initWithNSString:language
                                                                     withNSString:country
                                                                     withNSString:variant];
            [result replaceObjectAtIndex:i withObject:javaLocale];
        }
#if ! __has_feature(objc_arc)
        [result autorelease];
#endif
        return result;
    }-*/;

    /**
     * Gets the country code for this {@code Locale} or an empty string of no country
     * was set.
     * 
     * @return a country code.
     */
    public String getCountry() {
        return countryCode;
    }

    /**
     * Gets the default {@code Locale}.
     * 
     * @return the default {@code Locale}.
     */
    public static Locale getDefault() {
        return defaultLocale;
    }

    /**
     * Gets the full country name in the default {@code Locale} for the country code of
     * this {@code Locale}. If there is no matching country name, the country code is
     * returned.
     * 
     * @return a country name.
     */
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault());
    }

    /**
     * Gets the full country name in the specified {@code Locale} for the country code
     * of this {@code Locale}. If there is no matching country name, the country code is
     * returned.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a country name.
     */
	public String getDisplayCountry(Locale locale) {
      throw new AssertionError("not implemented");
	}

    /**
     * Gets the full language name in the default {@code Locale} for the language code
     * of this {@code Locale}. If there is no matching language name, the language code
     * is returned.
     * 
     * @return a language name.
     */
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault());
    }

    /**
     * Gets the full language name in the specified {@code Locale} for the language code
     * of this {@code Locale}. If there is no matching language name, the language code
     * is returned.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a language name.
     */
	public String getDisplayLanguage(Locale locale) {
      throw new AssertionError("not implemented");
	}

    /**
     * Gets the full language, country, and variant names in the default {@code Locale}
     * for the codes of this {@code Locale}.
     * 
     * @return a {@code Locale} name.
     */
    public final String getDisplayName() {
        return getDisplayName(getDefault());
    }

    /**
     * Gets the full language, country, and variant names in the specified
     * Locale for the codes of this {@code Locale}.
     * 
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a {@code Locale} name.
     */
    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        if (languageCode.length() > 0) {
            buffer.append(getDisplayLanguage(locale));
            count++;
        }
        if (countryCode.length() > 0) {
            if (count == 1) {
                buffer.append(" ("); //$NON-NLS-1$
            }
            buffer.append(getDisplayCountry(locale));
            count++;
        }
        if (variantCode.length() > 0) {
            if (count == 1) {
                buffer.append(" ("); //$NON-NLS-1$
            } else if (count == 2) {
                buffer.append(","); //$NON-NLS-1$
            }
            buffer.append(getDisplayVariant(locale));
            count++;
        }
        if (count > 1) {
            buffer.append(")"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    /**
     * Gets the full variant name in the default {@code Locale} for the variant code of
     * this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     * 
     * @return a variant name.
     */
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault());
    }

    /**
     * Gets the full variant name in the specified {@code Locale} for the variant code
     * of this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a variant name.
     */
	public String getDisplayVariant(Locale locale) {
        return getVariant();
	}

    /**
     * Gets the three letter ISO country code which corresponds to the country
     * code for this {@code Locale}.
     *
     * @return a three letter ISO language code.
     * @throws MissingResourceException
     *                if there is no matching three letter ISO country code.
     */
	public String getISO3Country() throws MissingResourceException {
      throw new AssertionError("not implemented");
	}

    /**
     * Gets the three letter ISO language code which corresponds to the language
     * code for this {@code Locale}.
     *
     * @return a three letter ISO language code.
     * @throws MissingResourceException
     *                if there is no matching three letter ISO language code.
     */
	public String getISO3Language() throws MissingResourceException {
        throw new AssertionError("not implemented");
	}

    /**
     * Gets the list of two letter ISO country codes which can be used as the
     * country code for a {@code Locale}.
     * 
     * @return an array of strings.
     */
    public static native String[] getISOCountries() /*-{
        NSArray *countries = [NSLocale ISOCountryCodes];
        NSUInteger count = [countries count];
        NSMutableData* data = [NSMutableData dataWithLength: count * sizeof(id)];
        NSRange range = NSMakeRange(0, count);
        [countries getObjects:(__unsafe_unretained id *) data.mutableBytes range:range];
        IOSObjectArray * result =
            [IOSObjectArray arrayWithObjects:(__unsafe_unretained id *) data.mutableBytes
                                       count:count
                                        type:[IOSClass classWithClass:[NSString class]]];
        return result;
    }-*/;

    /**
     * Gets the list of two letter ISO language codes which can be used as the
     * language code for a {@code Locale}.
     *
     * @return an array of strings.
     */
	public static native String[] getISOLanguages() /*-{
        NSArray *languages = [NSLocale ISOLanguageCodes];
        NSUInteger count = [languages count];
        NSMutableData* data = [NSMutableData dataWithLength: count * sizeof(id)];    
        NSRange range = NSMakeRange(0, count);
        [languages getObjects:(__unsafe_unretained id *) data.mutableBytes range:range];
        IOSObjectArray * result = 
            [IOSObjectArray arrayWithObjects:(__unsafe_unretained id *) data.mutableBytes
                                       count:count
                                        type:[IOSClass classWithClass:[NSString class]]];
        return result;
    }-*/;

    /**
     * Gets the language code for this {@code Locale} or the empty string of no language
     * was set.
     * 
     * @return a language code.
     */
    public String getLanguage() {
        return languageCode;
    }

    /**
     * Gets the variant code for this {@code Locale} or an empty {@code String} of no variant
     * was set.
     * 
     * @return a variant code.
     */
    public String getVariant() {
        return variantCode;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     * 
     * @return the receiver's hash.
     * @see #equals
     */
    @Override
    public synchronized int hashCode() {
        return countryCode.hashCode() + languageCode.hashCode()
                + variantCode.hashCode();
    }

    /**
     * Sets the default {@code Locale} to the specified {@code Locale}.
     * 
     * @param locale
     *            the new default {@code Locale}.
     * @throws SecurityException
     *                if there is a {@code SecurityManager} in place which does not allow this
     *                operation.
     */
    public synchronized static void setDefault(Locale locale) {
        if (locale != null) {
            defaultLocale = locale;
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Returns the string representation of this {@code Locale}. It consists of the
     * language followed by the country and at the end the variant. They are
     * separated by underscores. If the language is missing the string begins
     * with an underscore. If the country is missing there are 2 underscores
     * between the language and the variant. the variant alone canot be defined
     * without a language and/or a country (in this case this method would
     * return the empty string).
     *
     * Examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX"
     *
     * @return the string representation of this {@code Locale}.
     */
    @Override
    public final String toString() {
        StringBuilder result = new StringBuilder();
        result.append(languageCode);
        if (countryCode.length() > 0) {
            result.append('_');
            result.append(countryCode);
        }
        if (variantCode.length() > 0 && result.length() > 0) {
            if (0 == countryCode.length()) {
                result.append("__"); //$NON-NLS-1$
            } else {
                result.append('_');
            }
            result.append(variantCode);
        }
        return result.toString();
    }
}
