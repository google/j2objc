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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import libcore.io.IoUtils;

/**
 * {@code ResourceBundle} is an abstract class which is the superclass of classes which
 * provide {@code Locale}-specific resources. A bundle contains a number of named
 * resources, where the names are {@code Strings}. A bundle may have a parent bundle,
 * and when a resource is not found in a bundle, the parent bundle is searched for
 * the resource. If the fallback mechanism reaches the base bundle and still
 * can't find the resource it throws a {@code MissingResourceException}.
 *
 * <ul>
 * <li>All bundles for the same group of resources share a common base bundle.
 * This base bundle acts as the root and is the last fallback in case none of
 * its children was able to respond to a request.</li>
 * <li>The first level contains changes between different languages. Only the
 * differences between a language and the language of the base bundle need to be
 * handled by a language-specific {@code ResourceBundle}.</li>
 * <li>The second level contains changes between different countries that use
 * the same language. Only the differences between a country and the country of
 * the language bundle need to be handled by a country-specific {@code ResourceBundle}.
 * </li>
 * <li>The third level contains changes that don't have a geographic reason
 * (e.g. changes that where made at some point in time like {@code PREEURO} where the
 * currency of come countries changed. The country bundle would return the
 * current currency (Euro) and the {@code PREEURO} variant bundle would return the old
 * currency (e.g. DM for Germany).</li>
 * </ul>
 *
 * <strong>Examples</strong>
 * <ul>
 * <li>BaseName (base bundle)
 * <li>BaseName_de (german language bundle)
 * <li>BaseName_fr (french language bundle)
 * <li>BaseName_de_DE (bundle with Germany specific resources in german)
 * <li>BaseName_de_CH (bundle with Switzerland specific resources in german)
 * <li>BaseName_fr_CH (bundle with Switzerland specific resources in french)
 * <li>BaseName_de_DE_PREEURO (bundle with Germany specific resources in german of
 * the time before the Euro)
 * <li>BaseName_fr_FR_PREEURO (bundle with France specific resources in french of
 * the time before the Euro)
 * </ul>
 *
 * It's also possible to create variants for languages or countries. This can be
 * done by just skipping the country or language abbreviation:
 * BaseName_us__POSIX or BaseName__DE_PREEURO. But it's not allowed to
 * circumvent both language and country: BaseName___VARIANT is illegal.
 *
 * @see Properties
 * @see PropertyResourceBundle
 * @see ListResourceBundle
 * @since 1.1
 */
public abstract class ResourceBundle {

    private static final String UNDER_SCORE = "_";

    private static final String EMPTY_STRING = "";

    /**
     * The parent of this {@code ResourceBundle} that is used if this bundle doesn't
     * include the requested resource.
     */
    protected ResourceBundle parent;

    private Locale locale;

    private long lastLoadTime = 0;

    static class MissingBundle extends ResourceBundle {
        @Override
        public Enumeration<String> getKeys() {
            return null;
        }

        @Override
        public Object handleGetObject(String name) {
            return null;
        }
    }

    private static final ResourceBundle MISSING = new MissingBundle();

    private static final ResourceBundle MISSINGBASE = new MissingBundle();

    private static final WeakHashMap<Object, Hashtable<String, ResourceBundle>> cache
            = new WeakHashMap<Object, Hashtable<String, ResourceBundle>>();

    private static Locale cacheLocale = Locale.getDefault();

    /**
     * Constructs a new instance of this class.
     */
    public ResourceBundle() {
        /* empty */
    }

    /**
     * Finds the named resource bundle for the default {@code Locale} and the caller's
     * {@code ClassLoader}.
     *
     * @param bundleName
     *            the name of the {@code ResourceBundle}.
     * @return the requested {@code ResourceBundle}.
     * @throws MissingResourceException
     *                if the {@code ResourceBundle} cannot be found.
     */
    public static ResourceBundle getBundle(String bundleName) throws MissingResourceException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            classLoader = getLoader();
        }
        return getBundle(bundleName, Locale.getDefault(), classLoader);
    }

    /**
     * Finds the named {@code ResourceBundle} for the specified {@code Locale} and the caller
     * {@code ClassLoader}.
     *
     * @param bundleName
     *            the name of the {@code ResourceBundle}.
     * @param locale
     *            the {@code Locale}.
     * @return the requested resource bundle.
     * @throws MissingResourceException
     *                if the resource bundle cannot be found.
     */
    public static ResourceBundle getBundle(String bundleName, Locale locale) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            classLoader = getLoader();
        }
        return getBundle(bundleName, locale, classLoader);
    }

    /**
     * Finds the named resource bundle for the specified {@code Locale} and {@code ClassLoader}.
     *
     * The passed base name and {@code Locale} are used to create resource bundle names.
     * The first name is created by concatenating the base name with the result
     * of {@link Locale#toString()}. From this name all parent bundle names are
     * derived. Then the same thing is done for the default {@code Locale}. This results
     * in a list of possible bundle names.
     *
     * <strong>Example</strong> For the basename "BaseName", the {@code Locale} of the
     * German part of Switzerland (de_CH) and the default {@code Locale} en_US the list
     * would look something like this:
     *
     * <ol>
     * <li>BaseName_de_CH</li>
     * <li>BaseName_de</li>
     * <li>Basename_en_US</li>
     * <li>Basename_en</li>
     * <li>BaseName</li>
     * </ol>
     *
     * This list also shows the order in which the bundles will be searched for a requested
     * resource in the German part of Switzerland (de_CH).
     *
     * As a first step, this method tries to instantiate
     * a {@code ResourceBundle} with the names provided.
     * If such a class can be instantiated and initialized, it is returned and
     * all the parent bundles are instantiated too. If no such class can be
     * found this method tries to load a {@code .properties} file with the names by
     * replacing dots in the base name with a slash and by appending
     * "{@code .properties}" at the end of the string. If such a resource can be found
     * by calling {@link ClassLoader#getResource(String)} it is used to
     * initialize a {@link PropertyResourceBundle}. If this succeeds, it will
     * also load the parents of this {@code ResourceBundle}.
     *
     * For compatibility with older code, the bundle name isn't required to be
     * a fully qualified class name. It's also possible to directly pass
     * the path to a properties file (without a file extension).
     *
     * @param bundleName
     *            the name of the {@code ResourceBundle}.
     * @param locale
     *            the {@code Locale}.
     * @param loader
     *            the {@code ClassLoader} to use.
     * @return the requested {@code ResourceBundle}.
     * @throws MissingResourceException
     *                if the {@code ResourceBundle} cannot be found.
     */
    public static ResourceBundle getBundle(String bundleName, Locale locale,
            ClassLoader loader) throws MissingResourceException {
        if (loader == null) {
            throw new NullPointerException("loader == null");
        } else if (bundleName == null) {
            throw new NullPointerException("bundleName == null");
        }
        Locale defaultLocale = Locale.getDefault();
        if (!cacheLocale.equals(defaultLocale)) {
            cache.clear();
            cacheLocale = defaultLocale;
        }
        ResourceBundle bundle = null;
        if (!locale.equals(defaultLocale)) {
            bundle = handleGetBundle(false, bundleName, locale, loader);
        }
        if (bundle == null) {
            bundle = handleGetBundle(true, bundleName, defaultLocale, loader);
            if (bundle == null) {
                throw missingResourceException(bundleName + '_' + locale, "");
            }
        }
        return bundle;
    }

    private static MissingResourceException missingResourceException(String className, String key) {
        String detail = "Can't find resource for bundle '" + className + "', key '" + key + "'";
        throw new MissingResourceException(detail, className, key);
    }

    /**
     * Finds the named resource bundle for the specified base name and control.
     *
     * @param baseName
     *            the base name of a resource bundle
     * @param control
     *            the control that control the access sequence
     * @return the named resource bundle
     *
     * @since 1.6
     */
    public static ResourceBundle getBundle(String baseName, ResourceBundle.Control control) {
        return getBundle(baseName, Locale.getDefault(), getLoader(), control);
    }

    /**
     * Finds the named resource bundle for the specified base name and control.
     *
     * @param baseName
     *            the base name of a resource bundle
     * @param targetLocale
     *            the target locale of the resource bundle
     * @param control
     *            the control that control the access sequence
     * @return the named resource bundle
     *
     * @since 1.6
     */
    public static ResourceBundle getBundle(String baseName,
            Locale targetLocale, ResourceBundle.Control control) {
        return getBundle(baseName, targetLocale, getLoader(), control);
    }

    private static ClassLoader getLoader() {
        ClassLoader cl = ResourceBundle.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    /**
     * Finds the named resource bundle for the specified base name and control.
     *
     * @param baseName
     *            the base name of a resource bundle
     * @param targetLocale
     *            the target locale of the resource bundle
     * @param loader
     *            the class loader to load resource
     * @param control
     *            the control that control the access sequence
     * @return the named resource bundle
     *
     * @since 1.6
     */
    public static ResourceBundle getBundle(String baseName,
            Locale targetLocale, ClassLoader loader,
            ResourceBundle.Control control) {
        boolean expired = false;
        String bundleName = control.toBundleName(baseName, targetLocale);
        Object cacheKey = loader != null ? loader : "null";
        Hashtable<String, ResourceBundle> loaderCache = getLoaderCache(cacheKey);
        ResourceBundle result = loaderCache.get(bundleName);
        if (result != null) {
            long time = control.getTimeToLive(baseName, targetLocale);
            if (time == 0 || time == Control.TTL_NO_EXPIRATION_CONTROL
                    || time + result.lastLoadTime < System.currentTimeMillis()) {
                if (MISSING == result) {
                    throw new MissingResourceException(null, bundleName + '_'
                            + targetLocale, EMPTY_STRING);
                }
                return result;
            }
            expired = true;
        }
        // try to load
        ResourceBundle ret = processGetBundle(baseName, targetLocale, loader,
                control, expired, result);

        if (ret != null) {
            loaderCache.put(bundleName, ret);
            ret.lastLoadTime = System.currentTimeMillis();
            return ret;
        }
        loaderCache.put(bundleName, MISSING);
        throw new MissingResourceException(null, bundleName + '_' + targetLocale, EMPTY_STRING);
    }

    private static ResourceBundle processGetBundle(String baseName,
            Locale targetLocale, ClassLoader loader,
            ResourceBundle.Control control, boolean expired,
            ResourceBundle result) {
        List<Locale> locales = control.getCandidateLocales(baseName, targetLocale);
        if (locales == null) {
            throw new IllegalArgumentException();
        }
        List<String> formats = control.getFormats(baseName);
        if (Control.FORMAT_CLASS == formats
                || Control.FORMAT_PROPERTIES == formats
                || Control.FORMAT_DEFAULT == formats) {
            throw new IllegalArgumentException();
        }
        ResourceBundle ret = null;
        ResourceBundle currentBundle = null;
        ResourceBundle bundle = null;
        for (Locale locale : locales) {
            for (String format : formats) {
                try {
                    if (expired) {
                        bundle = control.newBundle(baseName, locale, format,
                                loader, control.needsReload(baseName, locale,
                                        format, loader, result, System
                                                .currentTimeMillis()));

                    } else {
                        try {
                            bundle = control.newBundle(baseName, locale,
                                    format, loader, false);
                        } catch (IllegalArgumentException e) {
                            // do nothing
                        }
                    }
                } catch (IllegalAccessException e) {
                    // do nothing
                } catch (InstantiationException e) {
                    // do nothing
                } catch (IOException e) {
                    // do nothing
                }
                if (bundle != null) {
                    if (currentBundle != null) {
                        currentBundle.setParent(bundle);
                        currentBundle = bundle;
                    } else {
                        if (ret == null) {
                            ret = bundle;
                            currentBundle = ret;
                        }
                    }
                }
                if (bundle != null) {
                    break;
                }
            }
        }

        if ((ret == null)
                || (Locale.ROOT.equals(ret.getLocale()) && (!(locales.size() == 1 && locales
                        .contains(Locale.ROOT))))) {
            Locale nextLocale = control.getFallbackLocale(baseName, targetLocale);
            if (nextLocale != null) {
                ret = processGetBundle(baseName, nextLocale, loader, control,
                        expired, result);
            }
        }

        return ret;
    }

    /**
     * Returns the names of the resources contained in this {@code ResourceBundle}.
     *
     * @return an {@code Enumeration} of the resource names.
     */
    public abstract Enumeration<String> getKeys();

    /**
     * Gets the {@code Locale} of this {@code ResourceBundle}. In case a bundle was not
     * found for the requested {@code Locale}, this will return the actual {@code Locale} of
     * this resource bundle that was found after doing a fallback.
     *
     * @return the {@code Locale} of this {@code ResourceBundle}.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the named resource from this {@code ResourceBundle}. If the resource
     * cannot be found in this bundle, it falls back to the parent bundle (if
     * it's not null) by calling the {@link #handleGetObject} method. If the resource still
     * can't be found it throws a {@code MissingResourceException}.
     *
     * @param key
     *            the name of the resource.
     * @return the resource object.
     * @throws MissingResourceException
     *                if the resource is not found.
     */
    public final Object getObject(String key) {
        ResourceBundle last, theParent = this;
        do {
            Object result = theParent.handleGetObject(key);
            if (result != null) {
                return result;
            }
            last = theParent;
            theParent = theParent.parent;
        } while (theParent != null);
        throw missingResourceException(last.getClass().getName(), key);
    }

    /**
     * Returns the named string resource from this {@code ResourceBundle}.
     *
     * @param key
     *            the name of the resource.
     * @return the resource string.
     * @throws MissingResourceException
     *                if the resource is not found.
     * @throws ClassCastException
     *                if the resource found is not a string.
     * @see #getObject(String)
     */
    public final String getString(String key) {
        return (String) getObject(key);
    }

    /**
     * Returns the named resource from this {@code ResourceBundle}.
     *
     * @param key
     *            the name of the resource.
     * @return the resource string array.
     * @throws MissingResourceException
     *                if the resource is not found.
     * @throws ClassCastException
     *                if the resource found is not an array of strings.
     * @see #getObject(String)
     */
    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    private static ResourceBundle handleGetBundle(boolean loadBase, String base, Locale locale,
            ClassLoader loader) {
        String localeName = locale.toString();
        String bundleName = localeName.isEmpty()
                ? base
                : (base + "_" + localeName);
        Object cacheKey = loader != null ? loader : "null";
        Hashtable<String, ResourceBundle> loaderCache = getLoaderCache(cacheKey);
        ResourceBundle cached = loaderCache.get(bundleName);
        if (cached != null) {
            if (cached == MISSINGBASE) {
                return null;
            } else if (cached == MISSING) {
                if (!loadBase) {
                    return null;
                }
                Locale newLocale = strip(locale);
                if (newLocale == null) {
                    return null;
                }
                return handleGetBundle(loadBase, base, newLocale, loader);
            }
            return cached;
        }

        ResourceBundle bundle = null;
        try {
            Class<?> bundleClass = Class.forName(bundleName, true, loader);
            if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                bundle = (ResourceBundle) bundleClass.newInstance();
            }
        } catch (LinkageError ignored) {
        } catch (Exception ignored) {
        }

        if (bundle != null) {
            bundle.setLocale(locale);
        } else {
            String fileName = bundleName.replace('.', '/') + ".properties";
            InputStream stream = loader != null
                    ? loader.getResourceAsStream(fileName)
                    : ClassLoader.getSystemResourceAsStream(fileName);
            if (stream != null) {
                try {
                    bundle = new PropertyResourceBundle(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                    bundle.setLocale(locale);
                } catch (IOException ignored) {
                } finally {
                    IoUtils.closeQuietly(stream);
                }
            }
        }

        Locale strippedLocale = strip(locale);
        if (bundle != null) {
            if (strippedLocale != null) {
                ResourceBundle parent = handleGetBundle(loadBase, base, strippedLocale, loader);
                if (parent != null) {
                    bundle.setParent(parent);
                }
            }
            loaderCache.put(bundleName, bundle);
            return bundle;
        }

        if (strippedLocale != null && (loadBase || !strippedLocale.toString().isEmpty())) {
            bundle = handleGetBundle(loadBase, base, strippedLocale, loader);
            if (bundle != null) {
                loaderCache.put(bundleName, bundle);
                return bundle;
            }
        }
        loaderCache.put(bundleName, loadBase ? MISSINGBASE : MISSING);
        return null;
    }

    private static Hashtable<String, ResourceBundle> getLoaderCache(Object cacheKey) {
        synchronized (cache) {
            Hashtable<String, ResourceBundle> loaderCache = cache.get(cacheKey);
            if (loaderCache == null) {
                loaderCache = new Hashtable<String, ResourceBundle>();
                cache.put(cacheKey, loaderCache);
            }
            return loaderCache;
        }
    }

    /**
     * Returns the named resource from this {@code ResourceBundle}, or null if the
     * resource is not found.
     *
     * @param key
     *            the name of the resource.
     * @return the resource object.
     */
    protected abstract Object handleGetObject(String key);

    /**
     * Sets the parent resource bundle of this {@code ResourceBundle}. The parent is
     * searched for resources which are not found in this {@code ResourceBundle}.
     *
     * @param bundle
     *            the parent {@code ResourceBundle}.
     */
    protected void setParent(ResourceBundle bundle) {
        parent = bundle;
    }

    /**
     * Returns a locale with the most-specific field removed, or null if this
     * locale had an empty language, country and variant.
     */
    private static Locale strip(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if (!variant.isEmpty()) {
            variant = "";
        } else if (!country.isEmpty()) {
            country = "";
        } else if (!language.isEmpty()) {
            language = "";
        } else {
            return null;
        }
        return new Locale(language, country, variant);
    }

    private void setLocale(Locale locale) {
        this.locale = locale;
    }

    public static void clearCache() {
        cache.remove(ClassLoader.getSystemClassLoader());
    }

    public static void clearCache(ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException("loader == null");
        }
        cache.remove(loader);
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        return keySet().contains(key);
    }

    public Set<String> keySet() {
        Set<String> ret = new HashSet<String>();
        Enumeration<String> keys = getKeys();
        while (keys.hasMoreElements()) {
            ret.add(keys.nextElement());
        }
        return ret;
    }

    protected Set<String> handleKeySet() {
        Set<String> set = keySet();
        Set<String> ret = new HashSet<String>();
        for (String key : set) {
            if (handleGetObject(key) != null) {
                ret.add(key);
            }
        }
        return ret;
    }

    private static class NoFallbackControl extends Control {

        static final Control NOFALLBACK_FORMAT_PROPERTIES_CONTROL = new NoFallbackControl(
                JAVAPROPERTIES);

        static final Control NOFALLBACK_FORMAT_CLASS_CONTROL = new NoFallbackControl(
                JAVACLASS);

        static final Control NOFALLBACK_FORMAT_DEFAULT_CONTROL = new NoFallbackControl(
                listDefault);

        public NoFallbackControl(String format) {
            listClass = new ArrayList<String>();
            listClass.add(format);
            super.format = Collections.unmodifiableList(listClass);
        }

        public NoFallbackControl(List<String> list) {
            super.format = list;
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException("baseName == null");
            } else if (locale == null) {
                throw new NullPointerException("locale == null");
            }
            return null;
        }
    }

    private static class SimpleControl extends Control {
        public SimpleControl(String format) {
            listClass = new ArrayList<String>();
            listClass.add(format);
            super.format = Collections.unmodifiableList(listClass);
        }
    }

    /**
     * ResourceBundle.Control is a static utility class defines ResourceBundle
     * load access methods, its default access order is as the same as before.
     * However users can implement their own control.
     *
     * @since 1.6
     */
    public static class Control {
        static List<String> listDefault = new ArrayList<String>();

        static List<String> listClass = new ArrayList<String>();

        static List<String> listProperties = new ArrayList<String>();

        static String JAVACLASS = "java.class";

        static String JAVAPROPERTIES = "java.properties";

        static {
            listDefault.add(JAVACLASS);
            listDefault.add(JAVAPROPERTIES);
            listClass.add(JAVACLASS);
            listProperties.add(JAVAPROPERTIES);
        }

        /**
         * a list defines default format
         */
        public static final List<String> FORMAT_DEFAULT = Collections
                .unmodifiableList(listDefault);

        /**
         * a list defines java class format
         */
        public static final List<String> FORMAT_CLASS = Collections
                .unmodifiableList(listClass);

        /**
         * a list defines property format
         */
        public static final List<String> FORMAT_PROPERTIES = Collections
                .unmodifiableList(listProperties);

        /**
         * a constant that indicates cache will not be used.
         */
        public static final long TTL_DONT_CACHE = -1L;

        /**
         * a constant that indicates cache will not be expired.
         */
        public static final long TTL_NO_EXPIRATION_CONTROL = -2L;

        private static final Control FORMAT_PROPERTIES_CONTROL = new SimpleControl(
                JAVAPROPERTIES);

        private static final Control FORMAT_CLASS_CONTROL = new SimpleControl(
                JAVACLASS);

        private static final Control FORMAT_DEFAULT_CONTROL = new Control();

        List<String> format;

        /**
         * default constructor
         *
         */
        protected Control() {
            listClass = new ArrayList<String>();
            listClass.add(JAVACLASS);
            listClass.add(JAVAPROPERTIES);
            format = Collections.unmodifiableList(listClass);
        }

        /**
         * Returns a control according to {@code formats}.
         */
        public static Control getControl(List<String> formats) {
            switch (formats.size()) {
            case 1:
                if (formats.contains(JAVACLASS)) {
                    return FORMAT_CLASS_CONTROL;
                }
                if (formats.contains(JAVAPROPERTIES)) {
                    return FORMAT_PROPERTIES_CONTROL;
                }
                break;
            case 2:
                if (formats.equals(FORMAT_DEFAULT)) {
                    return FORMAT_DEFAULT_CONTROL;
                }
                break;
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns a control according to {@code formats} whose fallback
         * locale is null.
         */
        public static Control getNoFallbackControl(List<String> formats) {
            switch (formats.size()) {
            case 1:
                if (formats.contains(JAVACLASS)) {
                    return NoFallbackControl.NOFALLBACK_FORMAT_CLASS_CONTROL;
                }
                if (formats.contains(JAVAPROPERTIES)) {
                    return NoFallbackControl.NOFALLBACK_FORMAT_PROPERTIES_CONTROL;
                }
                break;
            case 2:
                if (formats.equals(FORMAT_DEFAULT)) {
                    return NoFallbackControl.NOFALLBACK_FORMAT_DEFAULT_CONTROL;
                }
                break;
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns a list of candidate locales according to {@code baseName} in
         * {@code locale}.
         */
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException("baseName == null");
            } else if (locale == null) {
                throw new NullPointerException("locale == null");
            }
            List<Locale> retList = new ArrayList<Locale>();
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            if (!EMPTY_STRING.equals(variant)) {
                retList.add(new Locale(language, country, variant));
            }
            if (!EMPTY_STRING.equals(country)) {
                retList.add(new Locale(language, country));
            }
            if (!EMPTY_STRING.equals(language)) {
                retList.add(new Locale(language));
            }
            retList.add(Locale.ROOT);
            return retList;
        }

        /**
         * Returns a list of strings of formats according to {@code baseName}.
         */
        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException("baseName == null");
            }
            return format;
        }

        /**
         * Returns the fallback locale for {@code baseName} in {@code locale}.
         */
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException("baseName == null");
            } else if (locale == null) {
                throw new NullPointerException("locale == null");
            }
            if (Locale.getDefault() != locale) {
                return Locale.getDefault();
            }
            return null;
        }

        /**
         * Returns a new ResourceBundle.
         *
         * @param baseName
         *            the base name to use
         * @param locale
         *            the given locale
         * @param format
         *            the format, default is "java.class" or "java.properties"
         * @param loader
         *            the classloader to use
         * @param reload
         *            whether to reload the resource
         * @return a new ResourceBundle according to the give parameters
         * @throws IllegalAccessException
         *             if we can not access resources
         * @throws InstantiationException
         *             if we can not instantiate a resource class
         * @throws IOException
         *             if other I/O exception happens
         */
        public ResourceBundle newBundle(String baseName, Locale locale,
                String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException,
                IOException {
            if (format == null) {
                throw new NullPointerException("format == null");
            } else if (loader == null) {
                throw new NullPointerException("loader == null");
            }
            final String bundleName = toBundleName(baseName, locale);
            final ClassLoader clsloader = loader;
            ResourceBundle ret;
            if (format.equals(JAVACLASS)) {
                Class<?> cls = null;
                try {
                    cls = clsloader.loadClass(bundleName);
                } catch (Exception e) {
                }
                if (cls == null) {
                    return null;
                }
                try {
                    ResourceBundle bundle = (ResourceBundle) cls.newInstance();
                    bundle.setLocale(locale);
                    return bundle;
                } catch (NullPointerException e) {
                    return null;
                }
            }
            if (format.equals(JAVAPROPERTIES)) {
                InputStream streams = null;
                final String resourceName = toResourceName(bundleName, "properties");
                if (reload) {
                    URL url = null;
                    try {
                        url = loader.getResource(resourceName);
                    } catch (NullPointerException e) {
                        // do nothing
                    }
                    if (url != null) {
                        streams = new FileInputStream(url.getFile());
                    }
                } else {
                    try {
                        streams = clsloader.getResourceAsStream(resourceName);
                    } catch (NullPointerException e) {
                        // do nothing
                    }
                }
                if (streams != null) {
                    try {
                        ret = new PropertyResourceBundle(new InputStreamReader(streams));
                        ret.setLocale(locale);
                        streams.close();
                    } catch (IOException e) {
                        return null;
                    }
                    return ret;
                }
                return null;
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns the time to live of the ResourceBundle {@code baseName} in {@code locale},
         * default is TTL_NO_EXPIRATION_CONTROL.
         */
        public long getTimeToLive(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException("baseName == null");
            } else if (locale == null) {
                throw new NullPointerException("locale == null");
            }
            return TTL_NO_EXPIRATION_CONTROL;
        }

        /**
         * Returns true if the ResourceBundle needs to reload.
         *
         * @param baseName
         *            the base name of the ResourceBundle
         * @param locale
         *            the locale of the ResourceBundle
         * @param format
         *            the format to load
         * @param loader
         *            the ClassLoader to load resource
         * @param bundle
         *            the ResourceBundle
         * @param loadTime
         *            the expired time
         * @return if the ResourceBundle needs to reload
         */
        public boolean needsReload(String baseName, Locale locale,
                String format, ClassLoader loader, ResourceBundle bundle,
                long loadTime) {
            if (bundle == null) {
                // FIXME what's the use of bundle?
                throw new NullPointerException("bundle == null");
            }
            String bundleName = toBundleName(baseName, locale);
            String suffix = format;
            if (format.equals(JAVACLASS)) {
                suffix = "class";
            }
            if (format.equals(JAVAPROPERTIES)) {
                suffix = "properties";
            }
            String urlname = toResourceName(bundleName, suffix);
            URL url = loader.getResource(urlname);
            if (url != null) {
                String fileName = url.getFile();
                long lastModified = new File(fileName).lastModified();
                if (lastModified > loadTime) {
                    return true;
                }
            }
            return false;
        }

        /**
         * a utility method to answer the name of a resource bundle according to
         * the given base name and locale
         *
         * @param baseName
         *            the given base name
         * @param locale
         *            the locale to use
         * @return the name of a resource bundle according to the given base
         *         name and locale
         */
        public String toBundleName(String baseName, Locale locale) {
            final String emptyString = EMPTY_STRING;
            final String preString = UNDER_SCORE;
            final String underline = UNDER_SCORE;
            if (baseName == null) {
                throw new NullPointerException("baseName == null");
            }
            StringBuilder ret = new StringBuilder();
            StringBuilder prefix = new StringBuilder();
            ret.append(baseName);
            if (!locale.getLanguage().equals(emptyString)) {
                ret.append(underline);
                ret.append(locale.getLanguage());
            } else {
                prefix.append(preString);
            }
            if (!locale.getCountry().equals(emptyString)) {
                ret.append((CharSequence) prefix);
                ret.append(underline);
                ret.append(locale.getCountry());
                prefix = new StringBuilder();
            } else {
                prefix.append(preString);
            }
            if (!locale.getVariant().equals(emptyString)) {
                ret.append((CharSequence) prefix);
                ret.append(underline);
                ret.append(locale.getVariant());
            }
            return ret.toString();
        }

        /**
         * a utility method to answer the name of a resource according to the
         * given bundleName and suffix
         *
         * @param bundleName
         *            the given bundle name
         * @param suffix
         *            the suffix
         * @return the name of a resource according to the given bundleName and
         *         suffix
         */
        public final String toResourceName(String bundleName, String suffix) {
            if (suffix == null) {
                throw new NullPointerException("suffix == null");
            }
            StringBuilder ret = new StringBuilder(bundleName.replace('.', '/'));
            ret.append('.');
            ret.append(suffix);
            return ret.toString();
        }
    }
}
