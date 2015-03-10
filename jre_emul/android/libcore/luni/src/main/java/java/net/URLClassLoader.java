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

package java.net;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

/**
 * This class loader is responsible for loading classes and resources from a
 * list of URLs which can refer to either directories or JAR files. Classes
 * loaded by this {@code URLClassLoader} are granted permission to access the
 * URLs contained in the URL search list.
 *
 * This version has been simplified, since iOS is prohibited from dynamically
 * loading classes from bundles other than the main app bundle. No code was 
 * shared, just its public API.
 */
public class URLClassLoader extends SecureClassLoader {

    ArrayList<URL> urls;
    private URLStreamHandlerFactory factory;

    /**
     * Constructs a new {@code URLClassLoader} instance. The newly created
     * instance will have the system ClassLoader as its parent. URLs that end
     * with "/" are assumed to be directories, otherwise they are assumed to be
     * JAR files.
     *
     * @param urls
     *            the list of URLs where a specific class or file could be
     *            found.
     */
    public URLClassLoader(URL[] urls) {
        this(urls, ClassLoader.getSystemClassLoader(), null);
    }

    /**
     * Constructs a new URLClassLoader instance. The newly created instance will
     * have the system ClassLoader as its parent. URLs that end with "/" are
     * assumed to be directories, otherwise they are assumed to be JAR files.
     *
     * @param urls
     *            the list of URLs where a specific class or file could be
     *            found.
     * @param parent
     *            the class loader to assign as this loader's parent.
     */
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        this(urls, parent, null);
    }

    /**
     * Adds the specified URL to the search list.
     *
     * @param url
     *            the URL which is to add.
     */
    protected void addURL(URL url) {
        urls.add(url);
    }

    /**
     * Returns all known URLs which point to the specified resource.
     *
     * @param name
     *            the name of the requested resource.
     * @return the enumeration of URLs which point to the specified resource.
     * @throws IOException
     *             if an I/O error occurs while attempting to connect.
     */
    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        if (name == null) {
            return null;
        }
        // On iOS, every resource is resolved by the SystemClassLoader, so
        // any URL in this class loader will, too.
        return Collections.enumeration(urls);
    }

    /**
     * Returns the search list of this {@code URLClassLoader}.
     *
     * @return the list of all known URLs of this instance.
     */
    public URL[] getURLs() {
        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * Determines if the URL is pointing to a directory.
     */
    private static boolean isDirectory(URL url) {
        String file = url.getFile();
        return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
    }

    /**
     * Returns a new {@code URLClassLoader} instance for the given URLs and the
     * system {@code ClassLoader} as its parent.
     *
     * @param urls
     *            the list of URLs that is passed to the new {@code
     *            URLClassLoader}.
     * @return the created {@code URLClassLoader} instance.
     */
    public static URLClassLoader newInstance(final URL[] urls) {
        return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    }

    /**
     * Returns a new {@code URLClassLoader} instance for the given URLs and the
     * specified {@code ClassLoader} as its parent.
     *
     * @param urls
     *            the list of URLs that is passed to the new URLClassLoader.
     * @param parentCl
     *            the parent class loader that is passed to the new
     *            URLClassLoader.
     * @return the created {@code URLClassLoader} instance.
     */
    public static URLClassLoader newInstance(final URL[] urls, final ClassLoader parentCl) {
        return new URLClassLoader(urls, parentCl);
    }

    /**
     * Constructs a new {@code URLClassLoader} instance. The newly created
     * instance will have the specified {@code ClassLoader} as its parent and
     * use the specified factory to create stream handlers. URLs that end with
     * "/" are assumed to be directories, otherwise they are assumed to be JAR
     * files.
     *
     * @param searchUrls
     *            the list of URLs where a specific class or file could be
     *            found.
     * @param parent
     *            the {@code ClassLoader} to assign as this loader's parent.
     * @param factory
     *            the factory that will be used to create protocol-specific
     *            stream handlers.
     */
    public URLClassLoader(URL[] searchUrls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(parent);
        this.factory = factory;
        urls = new ArrayList<URL>(Arrays.asList(searchUrls));
    }
}
