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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import libcore.io.IoUtils;

/**
 * A service-provider loader.
 *
 * <p>A service provider is a factory for creating all known implementations of a particular
 * class or interface {@code S}. The known implementations are read from a configuration file in
 * {@code META-INF/services/}. The file's name should match the class' binary name (such as
 * {@code java.util.Outer$Inner}).
 *
 * <p>The file format is as follows.
 * The file's character encoding must be UTF-8.
 * Whitespace is ignored, and {@code #} is used to begin a comment that continues to the
 * next newline.
 * Lines that are empty after comment removal and whitespace trimming are ignored.
 * Otherwise, each line contains the binary name of one implementation class.
 * Duplicate entries are ignored, but entries are otherwise returned in order (that is, the file
 * is treated as an ordered set).
 *
 * <p>Given these classes:
 * <pre>
 * package a.b.c;
 * public interface MyService { ... }
 * public class MyImpl1 implements MyService { ... }
 * public class MyImpl2 implements MyService { ... }
 * </pre>
 * And this configuration file (stored as {@code META-INF/services/a.b.c.MyService}):
 * <pre>
 * # Known MyService providers.
 * a.b.c.MyImpl1  # The original implementation for handling "bar"s.
 * a.b.c.MyImpl2  # A later implementation for "foo"s.
 * </pre>
 * You might use {@code ServiceProvider} something like this:
 * <pre>
 *   for (MyService service : ServiceLoader<MyService>.load(MyService.class)) {
 *     if (service.supports(o)) {
 *       return service.handle(o);
 *     }
 *   }
 * </pre>
 *
 * <p>Note that each iteration creates new instances of the various service implementations, so
 * any heavily-used code will likely want to cache the known implementations itself and reuse them.
 * Note also that the candidate classes are instantiated lazily as you call {@code next} on the
 * iterator: construction of the iterator itself does not instantiate any of the providers.
 *
 * @param <S> the service class or interface
 * @since 1.6
 */
public final class ServiceLoader<S> implements Iterable<S> {
    private final Class<S> service;
    private final ClassLoader classLoader;
    private final Set<URL> services;

    private ServiceLoader(Class<S> service, ClassLoader classLoader) {
        // It makes no sense for service to be null.
        // classLoader is null if you want the system class loader.
        if (service == null) {
            throw new NullPointerException("service == null");
        }
        this.service = service;
        this.classLoader = classLoader;
        this.services = new HashSet<URL>();
        reload();
    }

    /**
     * Invalidates the cache of known service provider class names.
     */
    public void reload() {
        internalLoad();
    }

    /**
     * Returns an iterator over all the service providers offered by this service loader.
     * Note that {@code hasNext} and {@code next} may throw if the configuration is invalid.
     *
     * <p>Each iterator will return new instances of the classes it iterates over, so callers
     * may want to cache the results of a single call to this method rather than call it
     * repeatedly.
     *
     * <p>The returned iterator does not support {@code remove}.
     */
    public Iterator<S> iterator() {
        return new ServiceIterator(this);
    }

    /**
     * Constructs a service loader. If {@code classLoader} is null, the system class loader
     * is used.
     *
     * @param service the service class or interface
     * @param classLoader the class loader
     * @return a new ServiceLoader
     */
    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return new ServiceLoader<S>(service, classLoader);
    }

    private void internalLoad() {
        services.clear();
        try {
            String name = "META-INF/services/" + service.getName();
            services.addAll(Collections.list(classLoader.getResources(name)));
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Constructs a service loader, using the current thread's context class loader.
     *
     * @param service the service class or interface
     * @return a new ServiceLoader
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        return ServiceLoader.load(service, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructs a service loader, using the extension class loader.
     *
     * @param service the service class or interface
     * @return a new ServiceLoader
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (cl != null) {
            while (cl.getParent() != null) {
                cl = cl.getParent();
            }
        }
        return ServiceLoader.load(service, cl);
    }

    /**
     * Internal API to support built-in SPIs that check a system property first.
     * Returns an instance specified by a property with the class' binary name, or null if
     * no such property is set.
     * @hide
     */
    public static <S> S loadFromSystemProperty(final Class<S> service) {
        try {
            final String className = System.getProperty(service.getName());
            if (className != null) {
                Class<?> c = ClassLoader.getSystemClassLoader().loadClass(className);
                return (S) c.newInstance();
            }
            return null;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public String toString() {
        return "ServiceLoader for " + service.getName();
    }

    private class ServiceIterator implements Iterator<S> {
        private final ClassLoader classLoader;
        private final Class<S> service;
        private final Set<URL> services;

        private boolean isRead = false;

        private LinkedList<String> queue = new LinkedList<String>();

        public ServiceIterator(ServiceLoader<S> sl) {
            this.classLoader = sl.classLoader;
            this.service = sl.service;
            this.services = sl.services;
        }

        public boolean hasNext() {
            if (!isRead) {
                readClass();
            }
            return (queue != null && !queue.isEmpty());
        }

        @SuppressWarnings("unchecked")
        public S next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String className = queue.remove();
            try {
                return service.cast(classLoader.loadClass(className).newInstance());
            } catch (Exception e) {
                throw new ServiceConfigurationError("Couldn't instantiate class " + className, e);
            }
        }

        private void readClass() {
            for (URL url : services) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Strip comments and whitespace...
                        int commentStart = line.indexOf('#');
                        if (commentStart != -1) {
                            line = line.substring(0, commentStart);
                        }
                        line = line.trim();
                        // Ignore empty lines.
                        if (line.isEmpty()) {
                            continue;
                        }
                        String className = line;
                        checkValidJavaClassName(className);
                        if (!queue.contains(className)) {
                            queue.add(className);
                        }
                    }
                    isRead = true;
                } catch (Exception e) {
                    throw new ServiceConfigurationError("Couldn't read " + url, e);
                } finally {
                    IoUtils.closeQuietly(reader);
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void checkValidJavaClassName(String className) {
            for (int i = 0; i < className.length(); ++i) {
                char ch = className.charAt(i);
                if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
                    throw new ServiceConfigurationError("Bad character '" + ch + "' in class name");
                }
            }
        }
    }
}
