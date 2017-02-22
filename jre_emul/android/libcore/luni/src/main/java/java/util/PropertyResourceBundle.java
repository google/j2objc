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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import static java.nio.charset.Charsets.UTF_8;
import libcore.io.IoUtils;

/**
 * {@code PropertyResourceBundle} loads resources from an {@code InputStream}. All resources are
 * Strings. The resources must be of the form {@code key=value}, one
 * resource per line (see Properties).
 *
 * @see ResourceBundle
 * @see Properties
 * @since 1.1
 */
public class PropertyResourceBundle extends ResourceBundle {

    Properties resources;

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


    /**
     * Constructs a new instance of {@code PropertyResourceBundle} and loads the
     * properties file from the specified {@code InputStream}.
     *
     * @param stream
     *            the {@code InputStream}.
     * @throws IOException
     *             if an error occurs during a read operation on the
     *             {@code InputStream}.
     */
    public PropertyResourceBundle(InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException("stream == null");
        }
        resources = new Properties();
        resources.load(stream);
    }

    /**
     * Constructs a new resource bundle with properties read from {@code reader}.
     *
     * @param reader the {@code Reader}
     * @throws IOException
     * @since 1.6
     */
    public PropertyResourceBundle(Reader reader) throws IOException {
        resources = new Properties();
        resources.load(reader);
    }

    /**
     *  We know the bundle should be a properties file since this is the PropertyResourceBundle class,
     *  not the PropertyResourceBundle class, so here we override handleGetBundle that we won't
     *  bother trying to load as a class first, needlessly throwing exceptions every time we
     *  intend to open a .properties file.
     */
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

        String fileName = bundleName.replace('.', '/') + ".properties";
        InputStream stream = loader != null
                ? loader.getResourceAsStream(fileName)
                : ClassLoader.getSystemResourceAsStream(fileName);
        if (stream != null) {
            try {
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, UTF_8));
                bundle.setLocale(locale);
            } catch (IOException ignored) {
            } finally {
                IoUtils.closeQuietly(stream);
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
    
    protected Set<String> handleKeySet(){
        return resources.stringPropertyNames();
    }

    @SuppressWarnings("unchecked")
    private Enumeration<String> getLocalKeys() {
        return (Enumeration<String>) resources.propertyNames();
    }

    @Override
    public Enumeration<String> getKeys() {
        if (parent == null) {
            return getLocalKeys();
        }
        return new Enumeration<String>() {
            Enumeration<String> local = getLocalKeys();

            Enumeration<String> pEnum = parent.getKeys();

            String nextElement;

            private boolean findNext() {
                if (nextElement != null) {
                    return true;
                }
                while (pEnum.hasMoreElements()) {
                    String next = pEnum.nextElement();
                    if (!resources.containsKey(next)) {
                        nextElement = next;
                        return true;
                    }
                }
                return false;
            }

            public boolean hasMoreElements() {
                if (local.hasMoreElements()) {
                    return true;
                }
                return findNext();
            }

            public String nextElement() {
                if (local.hasMoreElements()) {
                    return local.nextElement();
                }
                if (findNext()) {
                    String result = nextElement;
                    nextElement = null;
                    return result;
                }
                // Cause an exception
                return pEnum.nextElement();
            }
        };
    }

    @Override
    public Object handleGetObject(String key) {
        return resources.get(key);
    }
}
