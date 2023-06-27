/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1996, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1999 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

/* J2ObjC removed
import java.io.UncheckedIOException;
import java.lang.ref.Reference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.util.jar.JarEntry;
import sun.security.action.GetPropertyAction;
*/

import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.util.locale.BaseLocale;
import sun.util.locale.LocaleObjectCache;

// Android-removed: Support for ResourceBundleControlProvider.
// Removed references to ResourceBundleControlProvider from the documentation.
// The service provider interface ResourceBundleControlProvider is not
// available on Android.
// Android-removed: Mentions of modules as they are not supported.
/**
 *
 * Resource bundles contain locale-specific objects.  When your program needs a
 * locale-specific resource, a <code>String</code> for example, your program can
 * load it from the resource bundle that is appropriate for the current user's
 * locale. In this way, you can write program code that is largely independent
 * of the user's locale isolating most, if not all, of the locale-specific
 * information in resource bundles.
 *
 * <p>
 * This allows you to write programs that can:
 * <UL>
 * <LI> be easily localized, or translated, into different languages
 * <LI> handle multiple locales at once
 * <LI> be easily modified later to support even more locales
 * </UL>
 *
 * <P>
 * Resource bundles belong to families whose members share a common base
 * name, but whose names also have additional components that identify
 * their locales. For example, the base name of a family of resource
 * bundles might be "MyResources". The family should have a default
 * resource bundle which simply has the same name as its family -
 * "MyResources" - and will be used as the bundle of last resort if a
 * specific locale is not supported. The family can then provide as
 * many locale-specific members as needed, for example a German one
 * named "MyResources_de".
 *
 * <P>
 * Each resource bundle in a family contains the same items, but the items have
 * been translated for the locale represented by that resource bundle.
 * For example, both "MyResources" and "MyResources_de" may have a
 * <code>String</code> that's used on a button for canceling operations.
 * In "MyResources" the <code>String</code> may contain "Cancel" and in
 * "MyResources_de" it may contain "Abbrechen".
 *
 * <P>
 * If there are different resources for different countries, you
 * can make specializations: for example, "MyResources_de_CH" contains objects for
 * the German language (de) in Switzerland (CH). If you want to only
 * modify some of the resources
 * in the specialization, you can do so.
 *
 * <P>
 * When your program needs a locale-specific object, it loads
 * the <code>ResourceBundle</code> class using the
 * {@link #getBundle(java.lang.String, java.util.Locale) getBundle}
 * method:
 * <blockquote>
 * <pre>
 * ResourceBundle myResources =
 *      ResourceBundle.getBundle("MyResources", currentLocale);
 * </pre>
 * </blockquote>
 *
 * <P>
 * Resource bundles contain key/value pairs. The keys uniquely
 * identify a locale-specific object in the bundle. Here's an
 * example of a <code>ListResourceBundle</code> that contains
 * two key/value pairs:
 * <blockquote>
 * <pre>
 * public class MyResources extends ListResourceBundle {
 *     protected Object[][] getContents() {
 *         return new Object[][] {
 *             // LOCALIZE THE SECOND STRING OF EACH ARRAY (e.g., "OK")
 *             {"OkKey", "OK"},
 *             {"CancelKey", "Cancel"},
 *             // END OF MATERIAL TO LOCALIZE
 *        };
 *     }
 * }
 * </pre>
 * </blockquote>
 * Keys are always <code>String</code>s.
 * In this example, the keys are "OkKey" and "CancelKey".
 * In the above example, the values
 * are also <code>String</code>s--"OK" and "Cancel"--but
 * they don't have to be. The values can be any type of object.
 *
 * <P>
 * You retrieve an object from resource bundle using the appropriate
 * getter method. Because "OkKey" and "CancelKey"
 * are both strings, you would use <code>getString</code> to retrieve them:
 * <blockquote>
 * <pre>
 * button1 = new Button(myResources.getString("OkKey"));
 * button2 = new Button(myResources.getString("CancelKey"));
 * </pre>
 * </blockquote>
 * The getter methods all require the key as an argument and return
 * the object if found. If the object is not found, the getter method
 * throws a <code>MissingResourceException</code>.
 *
 * <P>
 * Besides <code>getString</code>, <code>ResourceBundle</code> also provides
 * a method for getting string arrays, <code>getStringArray</code>,
 * as well as a generic <code>getObject</code> method for any other
 * type of object. When using <code>getObject</code>, you'll
 * have to cast the result to the appropriate type. For example:
 * <blockquote>
 * <pre>
 * int[] myIntegers = (int[]) myResources.getObject("intList");
 * </pre>
 * </blockquote>
 *
 * <P>
 * The Java Platform provides two subclasses of <code>ResourceBundle</code>,
 * <code>ListResourceBundle</code> and <code>PropertyResourceBundle</code>,
 * that provide a fairly simple way to create resources.
 * As you saw briefly in a previous example, <code>ListResourceBundle</code>
 * manages its resource as a list of key/value pairs.
 * <code>PropertyResourceBundle</code> uses a properties file to manage
 * its resources.
 *
 * <p>
 * If <code>ListResourceBundle</code> or <code>PropertyResourceBundle</code>
 * do not suit your needs, you can write your own <code>ResourceBundle</code>
 * subclass.  Your subclasses must override two methods: <code>handleGetObject</code>
 * and <code>getKeys()</code>.
 *
 * <p>
 * The implementation of a {@code ResourceBundle} subclass must be thread-safe
 * if it's simultaneously used by multiple threads. The default implementations
 * of the non-abstract methods in this class, and the methods in the direct
 * known concrete subclasses {@code ListResourceBundle} and
 * {@code PropertyResourceBundle} are thread-safe.
 *
 * <h3>ResourceBundle.Control</h3>
 *
 * The {@link ResourceBundle.Control} class provides information necessary
 * to perform the bundle loading process by the <code>getBundle</code>
 * factory methods that take a <code>ResourceBundle.Control</code>
 * instance. You can implement your own subclass in order to enable
 * non-standard resource bundle formats, change the search strategy, or
 * define caching parameters. Refer to the descriptions of the class and the
 * {@link #getBundle(String, Locale, ClassLoader, Control) getBundle}
 * factory method for details.
 *
 * <h3>Cache Management</h3>
 *
 * Resource bundle instances created by the <code>getBundle</code> factory
 * methods are cached by default, and the factory methods return the same
 * resource bundle instance multiple times if it has been
 * cached. <code>getBundle</code> clients may clear the cache, manage the
 * lifetime of cached resource bundle instances using time-to-live values,
 * or specify not to cache resource bundle instances. Refer to the
 * descriptions of the {@linkplain #getBundle(String, Locale, ClassLoader,
 * Control) <code>getBundle</code> factory method}, {@link
 * #clearCache(ClassLoader) clearCache}, {@link
 * Control#getTimeToLive(String, Locale)
 * ResourceBundle.Control.getTimeToLive}, and {@link
 * Control#needsReload(String, Locale, String, ClassLoader, ResourceBundle,
 * long) ResourceBundle.Control.needsReload} for details.
 *
 * <h3>Example</h3>
 *
 * The following is a very simple example of a <code>ResourceBundle</code>
 * subclass, <code>MyResources</code>, that manages two resources (for a larger number of
 * resources you would probably use a <code>Map</code>).
 * Notice that you don't need to supply a value if
 * a "parent-level" <code>ResourceBundle</code> handles the same
 * key with the same value (as for the okKey below).
 * <blockquote>
 * <pre>
 * // default (English language, United States)
 * public class MyResources extends ResourceBundle {
 *     public Object handleGetObject(String key) {
 *         if (key.equals("okKey")) return "Ok";
 *         if (key.equals("cancelKey")) return "Cancel";
 *         return null;
 *     }
 *
 *     public Enumeration&lt;String&gt; getKeys() {
 *         return Collections.enumeration(keySet());
 *     }
 *
 *     // Overrides handleKeySet() so that the getKeys() implementation
 *     // can rely on the keySet() value.
 *     protected Set&lt;String&gt; handleKeySet() {
 *         return new HashSet&lt;String&gt;(Arrays.asList("okKey", "cancelKey"));
 *     }
 * }
 *
 * // German language
 * public class MyResources_de extends MyResources {
 *     public Object handleGetObject(String key) {
 *         // don't need okKey, since parent level handles it.
 *         if (key.equals("cancelKey")) return "Abbrechen";
 *         return null;
 *     }
 *
 *     protected Set&lt;String&gt; handleKeySet() {
 *         return new HashSet&lt;String&gt;(Arrays.asList("cancelKey"));
 *     }
 * }
 * </pre>
 * </blockquote>
 * You do not have to restrict yourself to using a single family of
 * <code>ResourceBundle</code>s. For example, you could have a set of bundles for
 * exception messages, <code>ExceptionResources</code>
 * (<code>ExceptionResources_fr</code>, <code>ExceptionResources_de</code>, ...),
 * and one for widgets, <code>WidgetResource</code> (<code>WidgetResources_fr</code>,
 * <code>WidgetResources_de</code>, ...); breaking up the resources however you like.
 *
 * @see ListResourceBundle
 * @see PropertyResourceBundle
 * @see MissingResourceException
 * @since 1.1
 * @revised 9
 */
public abstract class ResourceBundle {

    /** initial size of the bundle cache */
    private static final int INITIAL_CACHE_SIZE = 32;

    // Android-removed: JavaUtilResourceBundleAccess is absent.
    /*
    static {
        SharedSecrets.setJavaUtilResourceBundleAccess(
            new JavaUtilResourceBundleAccess() {
                @Override
                public void setParent(ResourceBundle bundle,
                                      ResourceBundle parent) {
                    bundle.setParent(parent);
                }

                @Override
                public ResourceBundle getParent(ResourceBundle bundle) {
                    return bundle.parent;
                }

                @Override
                public void setLocale(ResourceBundle bundle, Locale locale) {
                    bundle.locale = locale;
                }

                @Override
                public void setName(ResourceBundle bundle, String name) {
                    bundle.name = name;
                }

                @Override
                public ResourceBundle getBundle(String baseName, Locale locale, Module module) {
                    // use the given module as the caller to bypass the access check
                    return getBundleImpl(module, module,
                                         baseName, locale,
                                         getDefaultControl(module, baseName));
                }

                @Override
                public ResourceBundle newResourceBundle(Class<? extends ResourceBundle> bundleClass) {
                    return ResourceBundleProviderHelper.newResourceBundle(bundleClass);
                }
            });
    }
    */

    /** constant indicating that no resource bundle exists */
    private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle() {
            public Enumeration<String> getKeys() { return null; }
            protected Object handleGetObject(String key) { return null; }
            public String toString() { return "NONEXISTENT_BUNDLE"; }
        };


    /**
     * The cache is a map from cache keys (with bundle base name, locale, and
     * class loader) to either a resource bundle or NONEXISTENT_BUNDLE wrapped by a
     * BundleReference.
     *
     * The cache is a ConcurrentMap, allowing the cache to be searched
     * concurrently by multiple threads.  This will also allow the cache keys
     * to be reclaimed along with the ClassLoaders they reference.
     *
     * This variable would be better named "cache", but we keep the old
     * name for compatibility with some workarounds for bug 4212439.
     */
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList
        = new ConcurrentHashMap<>(INITIAL_CACHE_SIZE);

    /**
     * Queue for reference objects referring to class loaders or bundles.
     */
    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

    /**
     * Returns the base name of this bundle, if known, or {@code null} if unknown.
     *
     * If not null, then this is the value of the {@code baseName} parameter
     * that was passed to the {@code ResourceBundle.getBundle(...)} method
     * when the resource bundle was loaded.
     *
     * @return The base name of the resource bundle, as provided to and expected
     * by the {@code ResourceBundle.getBundle(...)} methods.
     *
     * @see #getBundle(java.lang.String, java.util.Locale, java.lang.ClassLoader)
     *
     * @since 1.8
     */
    public String getBaseBundleName() {
        return name;
    }

    /**
     * The parent bundle of this bundle.
     * The parent bundle is searched by {@link #getObject getObject}
     * when this bundle does not contain a particular resource.
     */
    protected ResourceBundle parent = null;

    /**
     * The locale for this bundle.
     */
    private Locale locale = null;

    /**
     * The base bundle name for this bundle.
     */
    private String name;

    /**
     * The flag indicating this bundle has expired in the cache.
     */
    private volatile boolean expired;

    /**
     * The back link to the cache key. null if this bundle isn't in
     * the cache (yet) or has expired.
     */
    private volatile CacheKey cacheKey;

    /**
     * A Set of the keys contained only in this ResourceBundle.
     */
    private volatile Set<String> keySet;

    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    public ResourceBundle() {
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     * Calling this method is equivalent to calling
     * <blockquote>
     * <code>(String) {@link #getObject(java.lang.String) getObject}(key)</code>.
     * </blockquote>
     *
     * @param key the key for the desired string
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no object for the given key can be found
     * @exception ClassCastException if the object found for the given key is not a string
     * @return the string for the given key
     */
    public final String getString(String key) {
        return (String) getObject(key);
    }

    /**
     * Gets a string array for the given key from this resource bundle or one of its parents.
     * Calling this method is equivalent to calling
     * <blockquote>
     * <code>(String[]) {@link #getObject(java.lang.String) getObject}(key)</code>.
     * </blockquote>
     *
     * @param key the key for the desired string array
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no object for the given key can be found
     * @exception ClassCastException if the object found for the given key is not a string array
     * @return the string array for the given key
     */
    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    /**
     * Gets an object for the given key from this resource bundle or one of its parents.
     * This method first tries to obtain the object from this resource bundle using
     * {@link #handleGetObject(java.lang.String) handleGetObject}.
     * If not successful, and the parent resource bundle is not null,
     * it calls the parent's <code>getObject</code> method.
     * If still not successful, it throws a MissingResourceException.
     *
     * @param key the key for the desired object
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no object for the given key can be found
     * @return the object for the given key
     */
    public final Object getObject(String key) {
        Object obj = handleGetObject(key);
        if (obj == null) {
            if (parent != null) {
                obj = parent.getObject(key);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle "
                                                   +this.getClass().getName()
                                                   +", key "+key,
                                                   this.getClass().getName(),
                                                   key);
            }
        }
        return obj;
    }

    /**
     * Returns the locale of this resource bundle. This method can be used after a
     * call to getBundle() to determine whether the resource bundle returned really
     * corresponds to the requested locale or is a fallback.
     *
     * @return the locale of this resource bundle
     */
    public Locale getLocale() {
        return locale;
    }

    /*
     * Automatic determination of the ClassLoader to be used to load
     * resources on behalf of the client.
     */
    private static ClassLoader getLoader(Class<?> caller) {
        ClassLoader cl = caller == null ? null : caller.getClassLoader();
        if (cl == null) {
            // When the caller's loader is the boot class loader, cl is null
            // here. In that case, ClassLoader.getSystemClassLoader() may
            // return the same class loader that the application is
            // using. We therefore use a wrapper ClassLoader to create a
            // separate scope for bundles loaded on behalf of the Java
            // runtime so that these bundles cannot be returned from the
            // cache to the application (5048280).
            cl = RBClassLoader.INSTANCE;
        }
        return cl;
    }

    /**
     * A wrapper of ClassLoader.getSystemClassLoader().
     */
    private static class RBClassLoader extends ClassLoader {
        private static final RBClassLoader INSTANCE = AccessController.doPrivileged(
                new PrivilegedAction<RBClassLoader>() {
                    public RBClassLoader run() {
                        return new RBClassLoader();
                    }
                });
        private static final ClassLoader loader = ClassLoader.getSystemClassLoader();

        private RBClassLoader() {
        }
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (loader != null) {
                return loader.loadClass(name);
            }
            return Class.forName(name);
        }
        public URL getResource(String name) {
            if (loader != null) {
                return loader.getResource(name);
            }
            return ClassLoader.getSystemResource(name);
        }
        public InputStream getResourceAsStream(String name) {
            if (loader != null) {
                return loader.getResourceAsStream(name);
            }
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }

    // Android-removed: modules are not supported.
    /*
    private static ClassLoader getLoader(Module module) {
        PrivilegedAction<ClassLoader> pa = module::getClassLoader;
        return AccessController.doPrivileged(pa);
    }

    /**
     * @param module a non-null-screened module form the {@link CacheKey#getModule()}.
     * @return the ClassLoader to use in {@link Control#needsReload}
     *         and {@link Control#newBundle}
     *
    private static ClassLoader getLoaderForControl(Module module) {
        ClassLoader loader = getLoader(module);
        return loader == null ? ClassLoader.getPlatformClassLoader() : loader;
    }
    */

    /**
     * Sets the parent bundle of this bundle.
     * The parent bundle is searched by {@link #getObject getObject}
     * when this bundle does not contain a particular resource.
     *
     * @param parent this bundle's parent bundle.
     */
    protected void setParent(ResourceBundle parent) {
        assert parent != NONEXISTENT_BUNDLE;
        this.parent = parent;
    }

    // Android-changed: as modules are not supported keep using ClassLoader as part
    // of CacheKey.
    /**
     * Key used for cached resource bundles.  The key checks the base
     * name, the locale, and the class loader to determine if the
     * resource is a match to the requested one. The loader may be
     * null, but the base name and the locale must have a non-null
     * value.
     */
    private static final class CacheKey implements Cloneable {
        // These three are the actual keys for lookup in Map.
        private final String name;
        private volatile Locale locale;
        private KeyElementReference<ClassLoader> loaderRef;
        // Android-removed: modules are not supported.
        /*
        private final KeyElementReference<Module> moduleRef;
        private final KeyElementReference<Module> callerRef;
        // this is the part of hashCode that pertains to module and callerModule
        // which can be GCed..
        private final int modulesHash;
        */

        // bundle format which is necessary for calling
        // Control.needsReload().
        private volatile String format;

        // These time values are in CacheKey so that NONEXISTENT_BUNDLE
        // doesn't need to be cloned for caching.

        // The time when the bundle has been loaded
        private volatile long loadTime;

        // The time when the bundle expires in the cache, or either
        // Control.TTL_DONT_CACHE or Control.TTL_NO_EXPIRATION_CONTROL.
        private volatile long expirationTime;

        // Placeholder for an error report by a Throwable
        private volatile Throwable cause;

        // Android-removed: Support for ResourceBundleControlProvider.
        /*
        // ResourceBundleProviders for loading ResourceBundles
        private volatile ServiceLoader<ResourceBundleProvider> providers;
        private volatile boolean providersChecked;

        // Boolean.TRUE if the factory method caller provides a ResourceBundleProvier.
        private volatile Boolean callerHasProvider;
        */

        // Android-changed: keep Java 8 constructor.
        // CacheKey(String baseName, Locale locale, Module module, Module caller) {
        CacheKey(String baseName, Locale locale, ClassLoader loader) {
            // Objects.requireNonNull(module);
            // Objects.requireNonNull(caller);

            this.name = baseName;
            this.locale = locale;
            // this.moduleRef = new KeyElementReference<>(module, referenceQueue, this);
            // this.callerRef = new KeyElementReference<>(caller, referenceQueue, this);
            // this.modulesHash = module.hashCode() ^ caller.hashCode();
            if (loader == null) {
                this.loaderRef = null;
            } else {
                this.loaderRef = new KeyElementReference<>(loader, referenceQueue, this);
            }
        }

        CacheKey(CacheKey src) {
            // Android-removed: modules are not supported.
            /*
            // Create References to src's modules
            this.moduleRef = new KeyElementReference<>(
                Objects.requireNonNull(src.getModule()), referenceQueue, this);
            this.callerRef = new KeyElementReference<>(
                Objects.requireNonNull(src.getCallerModule()), referenceQueue, this);
            */
            // Copy fields from src. ResourceBundleProviders related fields
            // and "cause" should not be copied.
            this.name = src.name;
            this.locale = src.locale;
            if (src.loaderRef == null) {
                this.loaderRef = null;
            } else {
                ClassLoader loader = src.loaderRef.get();
                this.loaderRef = new KeyElementReference<>(loader, referenceQueue, this);
            }
            // Android-removed: modules are not supported.
            // this.modulesHash = src.modulesHash;
            this.format = src.format;
            this.loadTime = src.loadTime;
            this.expirationTime = src.expirationTime;
        }

        String getName() {
            return name;
        }

        Locale getLocale() {
            return locale;
        }

        CacheKey setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        // Android-removed: modules are not supported.
        /*
        Module getModule() {
            return moduleRef.get();
        }

        Module getCallerModule() {
            return callerRef.get();
        }

        ServiceLoader<ResourceBundleProvider> getProviders() {
            if (!providersChecked) {
                providers = getServiceLoader(getModule(), name);
                providersChecked = true;
            }
            return providers;
        }

        boolean hasProviders() {
            return getProviders() != null;
        }

        boolean callerHasProvider() {
            return callerHasProvider == Boolean.TRUE;
        }
        */

        ClassLoader getLoader() {
            return loaderRef != null ? loaderRef.get() : null;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            try {
                final CacheKey otherEntry = (CacheKey)other;
                // Android-removed: modules are not supported.
                //quick check to see if they are not equal
                // if (modulesHash != otherEntry.modulesHash) {
                //     return false;
                // }
                //are the names the same?
                if (!name.equals(otherEntry.name)) {
                    return false;
                }
                // are the locales the same?
                if (!locale.equals(otherEntry.locale)) {
                    return false;
                }

                //are refs (both non-null) or (both null)?
                if (loaderRef == null) {
                    return otherEntry.loaderRef == null;
                }
                ClassLoader loader = loaderRef.get();
                return (otherEntry.loaderRef != null)
                        // with a null reference we can no longer find
                        // out which class loader was referenced; so
                        // treat it as unequal
                        && (loader != null)
                        && (loader == otherEntry.loaderRef.get());
            } catch (    NullPointerException | ClassCastException e) {
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hashCode = (name.hashCode() << 3) ^ locale.hashCode();
            ClassLoader loader = getLoader();
            if (loader != null) {
                hashCode ^= loader.hashCode();
            }
            return hashCode;
        }

        public Object clone() {
            try {
                CacheKey clone = (CacheKey) super.clone();
                if (loaderRef != null) {
                    clone.loaderRef = new KeyElementReference(loaderRef.get(),
                                                          referenceQueue, clone);
                }
                // Clear the reference to a Throwable
                clone.cause = null;
                return clone;
            } catch (CloneNotSupportedException e) {
                //this should never happen
                throw new InternalError(e);
            }
        }

        String getFormat() {
            return format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        private void setCause(Throwable cause) {
            if (this.cause == null) {
                this.cause = cause;
            } else {
                // Override the cause if the previous one is
                // ClassNotFoundException.
                if (this.cause instanceof ClassNotFoundException) {
                    this.cause = cause;
                }
            }
        }

        private Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            String l = locale.toString();
            if (l.isEmpty()) {
                if (!locale.getVariant().isEmpty()) {
                    l = "__" + locale.getVariant();
                } else {
                    l = "\"\"";
                }
            }
            return "CacheKey[" + name +
                   ", locale=" + l +
                   ", classLoader" + getLoader() +
                   // Android-removed: modules are not supported.
                   // ", module=" + getModule() +
                   // ", callerModule=" + getCallerModule() +
                   ", format=" + format +
                   "]";
        }
    }

    /**
     * The common interface to get a CacheKey in LoaderReference and
     * BundleReference.
     */
    private static interface CacheKeyReference {
        public CacheKey getCacheKey();
    }

    /**
     * References to a CacheKey element as a WeakReference so that it can be
     * garbage collected when nobody else is using it.
     */
    private static class KeyElementReference<T> extends WeakReference<T>
                                                implements CacheKeyReference {
        @Weak
        private final CacheKey cacheKey;

        KeyElementReference(T referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        @Override
        public CacheKey getCacheKey() {
            return cacheKey;
        }
    }

    /**
     * References to bundles are soft references so that they can be garbage
     * collected when they have no hard references.
     */
    private static class BundleReference extends SoftReference<ResourceBundle>
                                         implements CacheKeyReference {
        private final CacheKey cacheKey;

        BundleReference(ResourceBundle referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        @Override
        public CacheKey getCacheKey() {
            return cacheKey;
        }
    }

    /**
     * Gets a resource bundle using the specified base name, the default locale,
     * and the caller's class loader. Calling this method is equivalent to calling
     * <blockquote>
     * <code>getBundle(baseName, Locale.getDefault(), this.getClass().getClassLoader())</code>,
     * </blockquote>
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @exception java.lang.NullPointerException
     *     if <code>baseName</code> is <code>null</code>
     * @exception MissingResourceException
     *     if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and the default locale
     *
     * @see <a href="#default_behavior">Resource Bundle Search and Loading Strategy</a>
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName)
    {
        Class<?> caller = Reflection.getCallerClass();
        return getBundleImpl(baseName, Locale.getDefault(),
                             caller, getDefaultControl(caller, baseName));
    }

    /**
     * Returns a resource bundle using the specified base name, the
     * default locale and the specified control. Calling this method
     * is equivalent to calling
     * <pre>
     * getBundle(baseName, Locale.getDefault(),
     *           this.getClass().getClassLoader(), control),
     * </pre>
     * except that <code>getClassLoader()</code> is run with the security
     * privileges of <code>ResourceBundle</code>.  See {@link
     * #getBundle(String, Locale, ClassLoader, Control) getBundle} for the
     * complete description of the resource bundle loading process with a
     * <code>ResourceBundle.Control</code>.
     *
     * @param baseName
     *        the base name of the resource bundle, a fully qualified class
     *        name
     * @param control
     *        the control which gives information for the resource bundle
     *        loading process
     * @return a resource bundle for the given base name and the default locale
     * @throws NullPointerException
     *         if <code>baseName</code> or <code>control</code> is
     *         <code>null</code>
     * @throws MissingResourceException
     *         if no resource bundle for the specified base name can be found
     * @throws IllegalArgumentException
     *         if the given <code>control</code> doesn't perform properly
     *         (e.g., <code>control.getCandidateLocales</code> returns null.)
     *         Note that validation of <code>control</code> is performed as
     *         needed.
     * @since 1.6
     * @revised 9
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName,
                                                 Control control) {
        Class<?> caller = Reflection.getCallerClass();
        Locale targetLocale = Locale.getDefault();
        // Android-removed: modules are not supported.
        // checkNamedModule(caller);
        return getBundleImpl(baseName, targetLocale, caller, control);
    }

    /**
     * Gets a resource bundle using the specified base name and locale,
     * and the caller's class loader. Calling this method is equivalent to calling
     * <blockquote>
     * <code>getBundle(baseName, locale, this.getClass().getClassLoader())</code>,
     * </blockquote>
     *
     * @param baseName
     *        the base name of the resource bundle, a fully qualified class name
     * @param locale
     *        the locale for which a resource bundle is desired
     * @exception NullPointerException
     *        if <code>baseName</code> or <code>locale</code> is <code>null</code>
     * @exception MissingResourceException
     *        if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and locale
     *
     * @see <a href="#default_behavior">Resource Bundle Search and Loading Strategy</a>
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName,
                                                 Locale locale)
    {
        Class<?> caller = Reflection.getCallerClass();
        return getBundleImpl(baseName, locale,
                             caller, getDefaultControl(caller, baseName));
    }

    // Android-removed: modules are not supported.
    /*
     * Gets a resource bundle using the specified base name and the default locale
     * on behalf of the specified module. This method is equivalent to calling
     * <blockquote>
     * <code>getBundle(baseName, Locale.getDefault(), module)</code>
     * </blockquote>
     *
     * @param baseName the base name of the resource bundle,
     *                 a fully qualified class name
     * @param module   the module for which the resource bundle is searched
     * @throws NullPointerException
     *         if {@code baseName} or {@code module} is {@code null}
     * @throws SecurityException
     *         if a security manager exists and the caller is not the specified
     *         module and doesn't have {@code RuntimePermission("getClassLoader")}
     * @throws MissingResourceException
     *         if no resource bundle for the specified base name can be found in the
     *         specified module
     * @return a resource bundle for the given base name and the default locale
     * @since 9
     * @spec JPMS
     * @see ResourceBundleProvider
     * @see <a href="#default_behavior">Resource Bundle Search and Loading Strategy</a>
     * @see <a href="#resource-bundle-modules">Resource Bundles and Named Modules</a>
     *
    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Module module) {
        return getBundleFromModule(Reflection.getCallerClass(), module, baseName,
                                   Locale.getDefault(),
                                   getDefaultControl(module, baseName));
    }

    /*
     * Gets a resource bundle using the specified base name and locale
     * on behalf of the specified module.
     *
     * <p> Resource bundles in named modules may be encapsulated.  When
     * the resource bundle is loaded from a
     * {@linkplain ResourceBundleProvider service provider}, the caller module
     * must have an appropriate <i>uses</i> clause in its <i>module descriptor</i>
     * to declare that the module uses of {@link ResourceBundleProvider}
     * for the named resource bundle.
     * Otherwise, it will load the resource bundles that are local in the
     * given module as if calling {@link Module#getResourceAsStream(String)}
     * or that are visible to the class loader of the given module
     * as if calling {@link ClassLoader#getResourceAsStream(String)}.
     * When the resource bundle is loaded from the specified module, it is
     * subject to the encapsulation rules specified by
     * {@link Module#getResourceAsStream Module.getResourceAsStream}.
     *
     * <p>
     * If the given {@code module} is an unnamed module, then this method is
     * equivalent to calling {@link #getBundle(String, Locale, ClassLoader)
     * getBundle(baseName, targetLocale, module.getClassLoader()} to load
     * resource bundles that are visible to the class loader of the given
     * unnamed module. Custom {@link java.util.spi.ResourceBundleControlProvider}
     * implementations, if present, will only be invoked if the specified
     * module is an unnamed module.
     *
     * @param baseName the base name of the resource bundle,
     *                 a fully qualified class name
     * @param targetLocale the locale for which a resource bundle is desired
     * @param module   the module for which the resource bundle is searched
     * @throws NullPointerException
     *         if {@code baseName}, {@code targetLocale}, or {@code module} is
     *         {@code null}
     * @throws SecurityException
     *         if a security manager exists and the caller is not the specified
     *         module and doesn't have {@code RuntimePermission("getClassLoader")}
     * @throws MissingResourceException
     *         if no resource bundle for the specified base name and locale can
     *         be found in the specified {@code module}
     * @return a resource bundle for the given base name and locale in the module
     * @since 9
     * @spec JPMS
     * @see <a href="#default_behavior">Resource Bundle Search and Loading Strategy</a>
     * @see <a href="#resource-bundle-modules">Resource Bundles and Named Modules</a>
     *
    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Locale targetLocale, Module module) {
        return getBundleFromModule(Reflection.getCallerClass(), module, baseName, targetLocale,
                                   getDefaultControl(module, baseName));
    }
    */

    /**
     * Returns a resource bundle using the specified base name, target
     * locale and control, and the caller's class loader. Calling this
     * method is equivalent to calling
     * <pre>
     * getBundle(baseName, targetLocale, this.getClass().getClassLoader(),
     *           control),
     * </pre>
     * except that <code>getClassLoader()</code> is run with the security
     * privileges of <code>ResourceBundle</code>.  See {@link
     * #getBundle(String, Locale, ClassLoader, Control) getBundle} for the
     * complete description of the resource bundle loading process with a
     * <code>ResourceBundle.Control</code>.
     *
     * @param baseName
     *        the base name of the resource bundle, a fully qualified
     *        class name
     * @param targetLocale
     *        the locale for which a resource bundle is desired
     * @param control
     *        the control which gives information for the resource
     *        bundle loading process
     * @return a resource bundle for the given base name and a
     *         <code>Locale</code> in <code>locales</code>
     * @throws NullPointerException
     *         if <code>baseName</code>, <code>locales</code> or
     *         <code>control</code> is <code>null</code>
     * @throws MissingResourceException
     *         if no resource bundle for the specified base name in any
     *         of the <code>locales</code> can be found.
     * @throws IllegalArgumentException
     *         if the given <code>control</code> doesn't perform properly
     *         (e.g., <code>control.getCandidateLocales</code> returns null.)
     *         Note that validation of <code>control</code> is performed as
     *         needed.
     * @since 1.6
     * @revised 9
     */
    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale targetLocale,
                                                 Control control) {
        Class<?> caller = Reflection.getCallerClass();
        // Android-removed: modules are not supported.
        // checkNamedModule(caller);
        return getBundleImpl(baseName, targetLocale, caller, control);
    }

    // Android-removed: Support for ResourceBundleControlProvider.
    // Removed documentation referring to that SPI.
    /**
     * Gets a resource bundle using the specified base name, locale, and class
     * loader.
     *
     * This is equivalent to calling:
     * <blockquote><pre>
     * getBundle(baseName, targetLocale, loader, control)
     * </pre></blockquote>
     * passing a default instance of {@link Control}.
     * Refer to the
     * description of <a href="#modify_default_behavior">modifying the default
     * behavior</a>. The following describes the default behavior.
     *
     * <p>
     * <b><a id="default_behavior">Resource Bundle Search and Loading Strategy</a></b>
     *
     * <p><code>getBundle</code> uses the base name, the specified locale, and
     * the default locale (obtained from {@link java.util.Locale#getDefault()
     * Locale.getDefault}) to generate a sequence of <a
     * id="candidates"><em>candidate bundle names</em></a>.  If the specified
     * locale's language, script, country, and variant are all empty strings,
     * then the base name is the only candidate bundle name.  Otherwise, a list
     * of candidate locales is generated from the attribute values of the
     * specified locale (language, script, country and variant) and appended to
     * the base name.  Typically, this will look like the following:
     *
     * <pre>
     *     baseName + "_" + language + "_" + script + "_" + country + "_" + variant
     *     baseName + "_" + language + "_" + script + "_" + country
     *     baseName + "_" + language + "_" + script
     *     baseName + "_" + language + "_" + country + "_" + variant
     *     baseName + "_" + language + "_" + country
     *     baseName + "_" + language
     * </pre>
     *
     * <p>Candidate bundle names where the final component is an empty string
     * are omitted, along with the underscore.  For example, if country is an
     * empty string, the second and the fifth candidate bundle names above
     * would be omitted.  Also, if script is an empty string, the candidate names
     * including script are omitted.  For example, a locale with language "de"
     * and variant "JAVA" will produce candidate names with base name
     * "MyResource" below.
     *
     * <pre>
     *     MyResource_de__JAVA
     *     MyResource_de
     * </pre>
     *
     * In the case that the variant contains one or more underscores ('_'), a
     * sequence of bundle names generated by truncating the last underscore and
     * the part following it is inserted after a candidate bundle name with the
     * original variant.  For example, for a locale with language "en", script
     * "Latn, country "US" and variant "WINDOWS_VISTA", and bundle base name
     * "MyResource", the list of candidate bundle names below is generated:
     *
     * <pre>
     * MyResource_en_Latn_US_WINDOWS_VISTA
     * MyResource_en_Latn_US_WINDOWS
     * MyResource_en_Latn_US
     * MyResource_en_Latn
     * MyResource_en_US_WINDOWS_VISTA
     * MyResource_en_US_WINDOWS
     * MyResource_en_US
     * MyResource_en
     * </pre>
     *
     * <blockquote><b>Note:</b> For some <code>Locale</code>s, the list of
     * candidate bundle names contains extra names, or the order of bundle names
     * is slightly modified.  See the description of the default implementation
     * of {@link Control#getCandidateLocales(String, Locale)
     * getCandidateLocales} for details.</blockquote>
     *
     * <p><code>getBundle</code> then iterates over the candidate bundle names
     * to find the first one for which it can <em>instantiate</em> an actual
     * resource bundle. It uses the default controls' {@link Control#getFormats
     * getFormats} method, which generates two bundle names for each generated
     * name, the first a class name and the second a properties file name. For
     * each candidate bundle name, it attempts to create a resource bundle:
     *
     * <ul><li>First, it attempts to load a class using the generated class name.
     * If such a class can be found and loaded using the specified class
     * loader, is assignment compatible with ResourceBundle, is accessible from
     * ResourceBundle, and can be instantiated, <code>getBundle</code> creates a
     * new instance of this class and uses it as the <em>result resource
     * bundle</em>.
     *
     * <li>Otherwise, <code>getBundle</code> attempts to locate a property
     * resource file using the generated properties file name.  It generates a
     * path name from the candidate bundle name by replacing all "." characters
     * with "/" and appending the string ".properties".  It attempts to find a
     * "resource" with this name using {@link
     * java.lang.ClassLoader#getResource(java.lang.String)
     * ClassLoader.getResource}.  (Note that a "resource" in the sense of
     * <code>getResource</code> has nothing to do with the contents of a
     * resource bundle, it is just a container of data, such as a file.)  If it
     * finds a "resource", it attempts to create a new {@link
     * PropertyResourceBundle} instance from its contents.  If successful, this
     * instance becomes the <em>result resource bundle</em>.  </ul>
     *
     * <p>This continues until a result resource bundle is instantiated or the
     * list of candidate bundle names is exhausted.  If no matching resource
     * bundle is found, the default control's {@link Control#getFallbackLocale
     * getFallbackLocale} method is called, which returns the current default
     * locale.  A new sequence of candidate locale names is generated using this
     * locale and searched again, as above.
     *
     * <p>If still no result bundle is found, the base name alone is looked up. If
     * this still fails, a <code>MissingResourceException</code> is thrown.
     *
     * <p><a id="parent_chain"> Once a result resource bundle has been found,
     * its <em>parent chain</em> is instantiated</a>.  If the result bundle already
     * has a parent (perhaps because it was returned from a cache) the chain is
     * complete.
     *
     * <p>Otherwise, <code>getBundle</code> examines the remainder of the
     * candidate locale list that was used during the pass that generated the
     * result resource bundle.  (As before, candidate bundle names where the
     * final component is an empty string are omitted.)  When it comes to the
     * end of the candidate list, it tries the plain bundle name.  With each of the
     * candidate bundle names it attempts to instantiate a resource bundle (first
     * looking for a class and then a properties file, as described above).
     *
     * <p>Whenever it succeeds, it calls the previously instantiated resource
     * bundle's {@link #setParent(java.util.ResourceBundle) setParent} method
     * with the new resource bundle.  This continues until the list of names
     * is exhausted or the current bundle already has a non-null parent.
     *
     * <p>Once the parent chain is complete, the bundle is returned.
     *
     * <p><b>Note:</b> <code>getBundle</code> caches instantiated resource
     * bundles and might return the same resource bundle instance multiple times.
     *
     * <p><b>Note:</b>The <code>baseName</code> argument should be a fully
     * qualified class name. However, for compatibility with earlier versions,
     * Java SE Runtime Environments do not verify this, and so it is
     * possible to access <code>PropertyResourceBundle</code>s by specifying a
     * path name (using "/") instead of a fully qualified class name (using
     * ".").
     *
     * <p><a id="default_behavior_example">
     * <strong>Example:</strong></a>
     * <p>
     * The following class and property files are provided:
     * <ul>
     *     <li>MyResources.class
     *     <li>MyResources.properties
     *     <li>MyResources_fr.properties
     *     <li>MyResources_fr_CH.class
     *     <li>MyResources_fr_CH.properties
     *     <li>MyResources_en.properties
     *     <li>MyResources_es_ES.class
     * </ul>
     *
     * The contents of all files are valid (that is, public non-abstract
     * subclasses of <code>ResourceBundle</code> for the ".class" files,
     * syntactically correct ".properties" files).  The default locale is
     * <code>Locale("en", "GB")</code>.
     *
     * <p>Calling <code>getBundle</code> with the locale arguments below will
     * instantiate resource bundles as follows:
     *
     * <table class="striped">
     * <caption style="display:none">getBundle() locale to resource bundle mapping</caption>
     * <thead>
     * <tr><th scope="col">Locale</th><th scope="col">Resource bundle</th></tr>
     * </thead>
     * <tbody>
     * <tr><th scope="row">Locale("fr", "CH")</th><td>MyResources_fr_CH.class, parent MyResources_fr.properties, parent MyResources.class</td></tr>
     * <tr><th scope="row">Locale("fr", "FR")</th><td>MyResources_fr.properties, parent MyResources.class</td></tr>
     * <tr><th scope="row">Locale("de", "DE")</th><td>MyResources_en.properties, parent MyResources.class</td></tr>
     * <tr><th scope="row">Locale("en", "US")</th><td>MyResources_en.properties, parent MyResources.class</td></tr>
     * <tr><th scope="row">Locale("es", "ES")</th><td>MyResources_es_ES.class, parent MyResources.class</td></tr>
     * </tbody>
     * </table>
     *
     * <p>The file MyResources_fr_CH.properties is never used because it is
     * hidden by the MyResources_fr_CH.class. Likewise, MyResources.properties
     * is also hidden by MyResources.class.
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale the locale for which a resource bundle is desired
     * @param loader the class loader from which to load the resource bundle
     * @return a resource bundle for the given base name and locale
     * @exception java.lang.NullPointerException
     *        if <code>baseName</code>, <code>locale</code>, or <code>loader</code> is <code>null</code>
     * @exception MissingResourceException
     *        if no resource bundle for the specified base name can be found
     * @since 1.2
     * @revised 9
     */
    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Locale locale,
                                           ClassLoader loader)
    {
        if (loader == null) {
            throw new NullPointerException();
        }
        Class<?> caller = Reflection.getCallerClass();
        return getBundleImpl(baseName, locale, caller, loader, getDefaultControl(caller, baseName));
    }

    /**
     * Returns a resource bundle using the specified base name, target
     * locale, class loader and control. Unlike the {@link
     * #getBundle(String, Locale, ClassLoader) getBundle}
     * factory methods with no {@code control} argument, the given
     * <code>control</code> specifies how to locate and instantiate resource
     * bundles. Conceptually, the bundle loading process with the given
     * <code>control</code> is performed in the following steps.
     *
     * <ol>
     * <li>This factory method looks up the resource bundle in the cache for
     * the specified <code>baseName</code>, <code>targetLocale</code> and
     * <code>loader</code>.  If the requested resource bundle instance is
     * found in the cache and the time-to-live periods of the instance and
     * all of its parent instances have not expired, the instance is returned
     * to the caller. Otherwise, this factory method proceeds with the
     * loading process below.</li>
     *
     * <li>The {@link ResourceBundle.Control#getFormats(String)
     * control.getFormats} method is called to get resource bundle formats
     * to produce bundle or resource names. The strings
     * <code>"java.class"</code> and <code>"java.properties"</code>
     * designate class-based and {@linkplain PropertyResourceBundle
     * property}-based resource bundles, respectively. Other strings
     * starting with <code>"java."</code> are reserved for future extensions
     * and must not be used for application-defined formats. Other strings
     * designate application-defined formats.</li>
     *
     * <li>The {@link ResourceBundle.Control#getCandidateLocales(String,
     * Locale) control.getCandidateLocales} method is called with the target
     * locale to get a list of <em>candidate <code>Locale</code>s</em> for
     * which resource bundles are searched.</li>
     *
     * <li>The {@link ResourceBundle.Control#newBundle(String, Locale,
     * String, ClassLoader, boolean) control.newBundle} method is called to
     * instantiate a <code>ResourceBundle</code> for the base bundle name, a
     * candidate locale, and a format. (Refer to the note on the cache
     * lookup below.) This step is iterated over all combinations of the
     * candidate locales and formats until the <code>newBundle</code> method
     * returns a <code>ResourceBundle</code> instance or the iteration has
     * used up all the combinations. For example, if the candidate locales
     * are <code>Locale("de", "DE")</code>, <code>Locale("de")</code> and
     * <code>Locale("")</code> and the formats are <code>"java.class"</code>
     * and <code>"java.properties"</code>, then the following is the
     * sequence of locale-format combinations to be used to call
     * <code>control.newBundle</code>.
     *
     * <table class=striped style="width: 50%; text-align: left; margin-left: 40px;">
     * <caption style="display:none">locale-format combinations for newBundle</caption>
     * <thead>
     * <tr>
     * <th scope="col">Index</th>
     * <th scope="col"><code>Locale</code></th>
     * <th scope="col"><code>format</code></th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <th scope="row">1</th>
     * <td><code>Locale("de", "DE")</code></td>
     * <td><code>java.class</code></td>
     * </tr>
     * <tr>
     * <th scope="row">2</th>
     * <td><code>Locale("de", "DE")</code></td>
     * <td><code>java.properties</code></td>
     * </tr>
     * <tr>
     * <th scope="row">3</th>
     * <td><code>Locale("de")</code></td>
     * <td><code>java.class</code></td>
     * </tr>
     * <tr>
     * <th scope="row">4</th>
     * <td><code>Locale("de")</code></td>
     * <td><code>java.properties</code></td>
     * </tr>
     * <tr>
     * <th scope="row">5</th>
     * <td><code>Locale("")</code></td>
     * <td><code>java.class</code></td>
     * </tr>
     * <tr>
     * <th scope="row">6</th>
     * <td><code>Locale("")</code></td>
     * <td><code>java.properties</code></td>
     * </tr>
     * </tbody>
     * </table>
     * </li>
     *
     * <li>If the previous step has found no resource bundle, proceed to
     * Step 6. If a bundle has been found that is a base bundle (a bundle
     * for <code>Locale("")</code>), and the candidate locale list only contained
     * <code>Locale("")</code>, return the bundle to the caller. If a bundle
     * has been found that is a base bundle, but the candidate locale list
     * contained locales other than Locale(""), put the bundle on hold and
     * proceed to Step 6. If a bundle has been found that is not a base
     * bundle, proceed to Step 7.</li>
     *
     * <li>The {@link ResourceBundle.Control#getFallbackLocale(String,
     * Locale) control.getFallbackLocale} method is called to get a fallback
     * locale (alternative to the current target locale) to try further
     * finding a resource bundle. If the method returns a non-null locale,
     * it becomes the next target locale and the loading process starts over
     * from Step 3. Otherwise, if a base bundle was found and put on hold in
     * a previous Step 5, it is returned to the caller now. Otherwise, a
     * MissingResourceException is thrown.</li>
     *
     * <li>At this point, we have found a resource bundle that's not the
     * base bundle. If this bundle set its parent during its instantiation,
     * it is returned to the caller. Otherwise, its <a
     * href="./ResourceBundle.html#parent_chain">parent chain</a> is
     * instantiated based on the list of candidate locales from which it was
     * found. Finally, the bundle is returned to the caller.</li>
     * </ol>
     *
     * <p>During the resource bundle loading process above, this factory
     * method looks up the cache before calling the {@link
     * Control#newBundle(String, Locale, String, ClassLoader, boolean)
     * control.newBundle} method.  If the time-to-live period of the
     * resource bundle found in the cache has expired, the factory method
     * calls the {@link ResourceBundle.Control#needsReload(String, Locale,
     * String, ClassLoader, ResourceBundle, long) control.needsReload}
     * method to determine whether the resource bundle needs to be reloaded.
     * If reloading is required, the factory method calls
     * <code>control.newBundle</code> to reload the resource bundle.  If
     * <code>control.newBundle</code> returns <code>null</code>, the factory
     * method puts a dummy resource bundle in the cache as a mark of
     * nonexistent resource bundles in order to avoid lookup overhead for
     * subsequent requests. Such dummy resource bundles are under the same
     * expiration control as specified by <code>control</code>.
     *
     * <p>All resource bundles loaded are cached by default. Refer to
     * {@link Control#getTimeToLive(String,Locale)
     * control.getTimeToLive} for details.
     *
     * <p>The following is an example of the bundle loading process with the
     * default <code>ResourceBundle.Control</code> implementation.
     *
     * <p>Conditions:
     * <ul>
     * <li>Base bundle name: <code>foo.bar.Messages</code>
     * <li>Requested <code>Locale</code>: {@link Locale#ITALY}</li>
     * <li>Default <code>Locale</code>: {@link Locale#FRENCH}</li>
     * <li>Available resource bundles:
     * <code>foo/bar/Messages_fr.properties</code> and
     * <code>foo/bar/Messages.properties</code></li>
     * </ul>
     *
     * <p>First, <code>getBundle</code> tries loading a resource bundle in
     * the following sequence.
     *
     * <ul>
     * <li>class <code>foo.bar.Messages_it_IT</code>
     * <li>file <code>foo/bar/Messages_it_IT.properties</code>
     * <li>class <code>foo.bar.Messages_it</code></li>
     * <li>file <code>foo/bar/Messages_it.properties</code></li>
     * <li>class <code>foo.bar.Messages</code></li>
     * <li>file <code>foo/bar/Messages.properties</code></li>
     * </ul>
     *
     * <p>At this point, <code>getBundle</code> finds
     * <code>foo/bar/Messages.properties</code>, which is put on hold
     * because it's the base bundle.  <code>getBundle</code> calls {@link
     * Control#getFallbackLocale(String, Locale)
     * control.getFallbackLocale("foo.bar.Messages", Locale.ITALY)} which
     * returns <code>Locale.FRENCH</code>. Next, <code>getBundle</code>
     * tries loading a bundle in the following sequence.
     *
     * <ul>
     * <li>class <code>foo.bar.Messages_fr</code></li>
     * <li>file <code>foo/bar/Messages_fr.properties</code></li>
     * <li>class <code>foo.bar.Messages</code></li>
     * <li>file <code>foo/bar/Messages.properties</code></li>
     * </ul>
     *
     * <p><code>getBundle</code> finds
     * <code>foo/bar/Messages_fr.properties</code> and creates a
     * <code>ResourceBundle</code> instance. Then, <code>getBundle</code>
     * sets up its parent chain from the list of the candidate locales.  Only
     * <code>foo/bar/Messages.properties</code> is found in the list and
     * <code>getBundle</code> creates a <code>ResourceBundle</code> instance
     * that becomes the parent of the instance for
     * <code>foo/bar/Messages_fr.properties</code>.
     *
     * @param baseName
     *        the base name of the resource bundle, a fully qualified
     *        class name
     * @param targetLocale
     *        the locale for which a resource bundle is desired
     * @param loader
     *        the class loader from which to load the resource bundle
     * @param control
     *        the control which gives information for the resource
     *        bundle loading process
     * @return a resource bundle for the given base name and locale
     * @throws NullPointerException
     *         if <code>baseName</code>, <code>targetLocale</code>,
     *         <code>loader</code>, or <code>control</code> is
     *         <code>null</code>
     * @throws MissingResourceException
     *         if no resource bundle for the specified base name can be found
     * @throws IllegalArgumentException
     *         if the given <code>control</code> doesn't perform properly
     *         (e.g., <code>control.getCandidateLocales</code> returns null.)
     *         Note that validation of <code>control</code> is performed as
     *         needed.
     * @since 1.6
     * @revised 9
     */
    @CallerSensitive
    public static ResourceBundle getBundle(String baseName, Locale targetLocale,
                                           ClassLoader loader, Control control) {
        if (loader == null || control == null) {
            throw new NullPointerException();
        }
        Class<?> caller = Reflection.getCallerClass();
        // Android-removed: modules are not supported.
        // checkNamedModule(caller);
        return getBundleImpl(baseName, targetLocale, caller, loader, control);
    }

    private static Control getDefaultControl(String baseName) {
        // Android-removed: Support for ResourceBundleControlProvider.
        /*
        if (providers != null) {
            for (ResourceBundleControlProvider provider : providers) {
                Control control = provider.getControl(baseName);
                if (control != null) {
                    return control;
                }
            }
        }
        */
        return Control.INSTANCE;
    }

    private static Control getDefaultControl(Class<?> caller, String baseName) {
        // Android-removed: modules are not supported.
        // return getDefaultControl(caller.getModule(), baseName);
        return getDefaultControl(baseName);
    }

    // Android-removed: modules are not supported
    /*
    private static Control getDefaultControl(Module targetModule, String baseName) {
        return targetModule.isNamed() ?
            Control.INSTANCE :
            ResourceBundleControlProviderHolder.getControl(baseName);
    }
    */

    // Android-removed: support for ResourceBundleControlProvider
    /*
    private static class ResourceBundleControlProviderHolder {
        private static final PrivilegedAction<List<ResourceBundleControlProvider>> pa =
            () -> {
                return Collections.unmodifiableList(
                    ServiceLoader.load(ResourceBundleControlProvider.class,
                                       ClassLoader.getSystemClassLoader()).stream()
                        .map(ServiceLoader.Provider::get)
                        .collect(Collectors.toList()));
            };

        private static final List<ResourceBundleControlProvider> CONTROL_PROVIDERS =
            AccessController.doPrivileged(pa);

        private static Control getControl(String baseName) {
            return CONTROL_PROVIDERS.isEmpty() ?
                Control.INSTANCE :
                CONTROL_PROVIDERS.stream()
                    .flatMap(provider -> Stream.ofNullable(provider.getControl(baseName)))
                    .findFirst()
                    .orElse(Control.INSTANCE);
        }
    }
    */

    // Android-removed: modules are not supported
    /*
    private static void checkNamedModule(Class<?> caller) {
        if (caller.getModule().isNamed()) {
            throw new UnsupportedOperationException(
                    "ResourceBundle.Control not supported in named modules");
        }
    }
    */

    private static ResourceBundle getBundleImpl(String baseName,
                                                Locale locale,
                                                Class<?> caller,
                                                Control control) {
        return getBundleImpl(baseName, locale, caller, caller.getClassLoader(), control);
    }

    /**
     * This method will find resource bundles using the legacy mechanism
     * if the caller is unnamed module or the given class loader is
     * not the class loader of the caller module getting the resource
     * bundle, i.e. find the class that is visible to the class loader
     * and properties from unnamed module.
     *
     * The module-aware resource bundle lookup mechanism will load
     * the service providers using the service loader mechanism
     * as well as properties local in the caller module.
     */
    private static ResourceBundle getBundleImpl(String baseName,
                                                Locale locale,
                                                Class<?> caller,
                                                ClassLoader loader,
                                                Control control) {
        if (caller == null) {
            throw new InternalError("null caller");
        }

        // Android-removed: modules are not supported.
        /*
        Module callerModule = caller.getModule();

        // get resource bundles for a named module only if loader is the module's class loader
        if (callerModule.isNamed() && loader == getLoader(callerModule)) {
            return getBundleImpl(callerModule, callerModule, baseName, locale, control);
        }

        // find resource bundles from unnamed module of given class loader
        // Java agent can add to the bootclasspath e.g. via
        // java.lang.instrument.Instrumentation and load classes in unnamed module.
        // It may call RB::getBundle that will end up here with loader == null.
        Module unnamedModule = loader != null
            ? loader.getUnnamedModule()
            : BootLoader.getUnnamedModule();
        */
        // return getBundleImpl(callerModule, unnamedModule, baseName, locale, control);
        return getBundleImpl(baseName, locale, loader, control);
    }

    // Android-removed: modules are not supported.
    /*
    private static ResourceBundle getBundleFromModule(Class<?> caller,
                                                      Module module,
                                                      String baseName,
                                                      Locale locale,
                                                      Control control) {
        Objects.requireNonNull(module);
        Module callerModule = caller.getModule();
        if (callerModule != module) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(GET_CLASSLOADER_PERMISSION);
            }
        }
        return getBundleImpl(callerModule, module, baseName, locale, control);
    }
    */

    // Android-changed: modules are not supported.
    // private static ResourceBundle getBundleImpl(Module callerModule,
    //                                          Module module,
    private static ResourceBundle getBundleImpl(String baseName, Locale locale,
                                                ClassLoader loader, Control control) {
        if (locale == null || control == null) {
            throw new NullPointerException();
        }

        // We create a CacheKey here for use by this call. The base name
        // and modules will never change during the bundle loading
        // process. We have to make sure that the locale is set before
        // using it as a cache key.
        // CacheKey cacheKey = new CacheKey(baseName, locale, module, callerModule);
        CacheKey cacheKey = new CacheKey(baseName, locale, loader);
        ResourceBundle bundle = null;

        // Quick lookup of the cache.
        BundleReference bundleRef = cacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = bundleRef.get();
            bundleRef = null;
        }

        // If this bundle and all of its parents are valid (not expired),
        // then return this bundle. If any of the bundles is expired, we
        // don't call control.needsReload here but instead drop into the
        // complete loading process below.
        if (isValidBundle(bundle) && hasValidParentChain(bundle)) {
            return bundle;
        }

        // No valid bundle was found in the cache, so we need to load the
        // resource bundle and its parents.

        boolean isKnownControl = (control == Control.INSTANCE) ||
                                   (control instanceof SingleFormatControl);
        List<String> formats = control.getFormats(baseName);
        if (!isKnownControl && !checkList(formats)) {
            throw new IllegalArgumentException("Invalid Control: getFormats");
        }

        ResourceBundle baseBundle = null;
        for (Locale targetLocale = locale;
             targetLocale != null;
             targetLocale = control.getFallbackLocale(baseName, targetLocale)) {
            List<Locale> candidateLocales = control.getCandidateLocales(baseName, targetLocale);
            if (!isKnownControl && !checkList(candidateLocales)) {
                throw new IllegalArgumentException("Invalid Control: getCandidateLocales");
            }
            /*
            bundle = findBundle(callerModule, module, cacheKey,
                                candidateLocales, formats, 0, control, baseBundle);
            */
            bundle = findBundle(cacheKey, candidateLocales, formats, 0, control, baseBundle);

            // If the loaded bundle is the base bundle and exactly for the
            // requested locale or the only candidate locale, then take the
            // bundle as the resulting one. If the loaded bundle is the base
            // bundle, it's put on hold until we finish processing all
            // fallback locales.
            if (isValidBundle(bundle)) {
                boolean isBaseBundle = Locale.ROOT.equals(bundle.locale);
                if (!isBaseBundle || bundle.locale.equals(locale)
                    || (candidateLocales.size() == 1
                        && bundle.locale.equals(candidateLocales.get(0)))) {
                    break;
                }

                // If the base bundle has been loaded, keep the reference in
                // baseBundle so that we can avoid any redundant loading in case
                // the control specify not to cache bundles.
                if (isBaseBundle && baseBundle == null) {
                    baseBundle = bundle;
                }
            }
        }

        if (bundle == null) {
            if (baseBundle == null) {
                throwMissingResourceException(baseName, locale, cacheKey.getCause());
            }
            bundle = baseBundle;
        }

        // keep callerModule and module reachable for as long as we are operating
        // with WeakReference(s) to them (in CacheKey)...
        // Reference.reachabilityFence(callerModule);
        // Reference.reachabilityFence(module);

        return bundle;
    }

    /**
     * Checks if the given <code>List</code> is not null, not empty,
     * not having null in its elements.
     */
    private static boolean checkList(List<?> a) {
        boolean valid = (a != null && !a.isEmpty());
        if (valid) {
            int size = a.size();
            for (int i = 0; valid && i < size; i++) {
                valid = (a.get(i) != null);
            }
        }
        return valid;
    }

    // private static ResourceBundle findBundle(Module callerModule,
    //                                         Module module,
    private static ResourceBundle findBundle(CacheKey cacheKey,
                                             List<Locale> candidateLocales,
                                             List<String> formats,
                                             int index,
                                             Control control,
                                             ResourceBundle baseBundle) {
        Locale targetLocale = candidateLocales.get(index);
        ResourceBundle parent = null;
        if (index != candidateLocales.size() - 1) {
            /*
            parent = findBundle(callerModule, module, cacheKey,
                                candidateLocales, formats, index + 1,
                                control, baseBundle);
            */
            parent = findBundle(cacheKey, candidateLocales, formats, index + 1,
                                control, baseBundle);
        } else if (baseBundle != null && Locale.ROOT.equals(targetLocale)) {
            return baseBundle;
        }

        // Before we do the real loading work, see whether we need to
        // do some housekeeping: If references to modules or
        // resource bundles have been nulled out, remove all related
        // information from the cache.
        Object ref;
        while ((ref = referenceQueue.poll()) != null) {
            cacheList.remove(((CacheKeyReference)ref).getCacheKey());
        }

        // flag indicating the resource bundle has expired in the cache
        boolean expiredBundle = false;

        // First, look up the cache to see if it's in the cache, without
        // attempting to load bundle.
        cacheKey.setLocale(targetLocale);
        ResourceBundle bundle = findBundleInCache(cacheKey, control);
        if (isValidBundle(bundle)) {
            expiredBundle = bundle.expired;
            if (!expiredBundle) {
                // If its parent is the one asked for by the candidate
                // locales (the runtime lookup path), we can take the cached
                // one. (If it's not identical, then we'd have to check the
                // parent's parents to be consistent with what's been
                // requested.)
                if (bundle.parent == parent) {
                    return bundle;
                }
                // Otherwise, remove the cached one since we can't keep
                // the same bundles having different parents.
                BundleReference bundleRef = cacheList.get(cacheKey);
                // Android-changed: Use refersTo().
                if (bundleRef != null && bundleRef.get() == bundle) {
                    cacheList.remove(cacheKey, bundleRef);
                }
            }
        }

        if (bundle != NONEXISTENT_BUNDLE) {
            CacheKey constKey = (CacheKey) cacheKey.clone();

            try {
                bundle = loadBundle(cacheKey, formats, control, expiredBundle);
                if (bundle != null) {
                    if (bundle.parent == null) {
                        bundle.setParent(parent);
                    }
                    bundle.locale = targetLocale;
                    bundle = putBundleInCache(cacheKey, bundle, control);
                    return bundle;
                }

                // Put NONEXISTENT_BUNDLE in the cache as a mark that there's no bundle
                // instance for the locale.
                putBundleInCache(cacheKey, NONEXISTENT_BUNDLE, control);
            } finally {
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return parent;
    }

    private static final String UNKNOWN_FORMAT = "";


    // Android-removed: modules are not supported.
    /*
     * Loads a ResourceBundle in named modules
     *
    private static ResourceBundle loadBundle(CacheKey cacheKey,
                                             List<String> formats,
                                             Control control,
                                             Module module,
                                             Module callerModule) {
        String baseName = cacheKey.getName();
        Locale targetLocale = cacheKey.getLocale();

        ResourceBundle bundle = null;
        if (cacheKey.hasProviders()) {
            if (callerModule == module) {
                bundle = loadBundleFromProviders(baseName,
                                                 targetLocale,
                                                 cacheKey.getProviders(),
                                                 cacheKey);
            } else {
                // load from provider if the caller module has access to the
                // service type and also declares `uses`
                ClassLoader loader = getLoader(module);
                Class<ResourceBundleProvider> svc =
                    getResourceBundleProviderType(baseName, loader);
                if (svc != null
                        && Reflection.verifyModuleAccess(callerModule, svc)
                        && callerModule.canUse(svc)) {
                    bundle = loadBundleFromProviders(baseName,
                                                     targetLocale,
                                                     cacheKey.getProviders(),
                                                     cacheKey);
                }
            }

            if (bundle != null) {
                cacheKey.setFormat(UNKNOWN_FORMAT);
            }
        }

        // If none of providers returned a bundle and the caller has no provider,
        // look up module-local bundles or from the class path
        if (bundle == null && !cacheKey.callerHasProvider()) {
            for (String format : formats) {
                try {
                    switch (format) {
                    case "java.class":
                        bundle = ResourceBundleProviderHelper
                            .loadResourceBundle(callerModule, module, baseName, targetLocale);

                        break;
                    case "java.properties":
                        bundle = ResourceBundleProviderHelper
                            .loadPropertyResourceBundle(callerModule, module, baseName, targetLocale);
                        break;
                    default:
                        throw new InternalError("unexpected format: " + format);
                    }

                    if (bundle != null) {
                        cacheKey.setFormat(format);
                        break;
                    }
                } catch (LinkageError|Exception e) {
                    cacheKey.setCause(e);
                }
            }
        }
        return bundle;
    }

    /*
     * Returns a ServiceLoader that will find providers that are bound to
     * a given named module.
     *
    private static ServiceLoader<ResourceBundleProvider> getServiceLoader(Module module,
                                                                          String baseName)
    {
        if (!module.isNamed()) {
            return null;
        }

        ClassLoader loader = getLoader(module);
        Class<ResourceBundleProvider> service =
                getResourceBundleProviderType(baseName, loader);
        if (service != null && Reflection.verifyModuleAccess(module, service)) {
            try {
                // locate providers that are visible to the class loader
                // ServiceConfigurationError will be thrown if the module
                // does not declare `uses` the service type
                return ServiceLoader.load(service, loader, module);
            } catch (ServiceConfigurationError e) {
                // "uses" not declared
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the service type of the given baseName that is visible
     * to the given class loader
     *
    private static Class<ResourceBundleProvider>
            getResourceBundleProviderType(String baseName, ClassLoader loader)
    {
        // Look up <packagename> + ".spi." + <name>"Provider"
        int i = baseName.lastIndexOf('.');
        if (i <= 0) {
            return null;
        }

        String name = baseName.substring(i+1, baseName.length()) + "Provider";
        String providerName = baseName.substring(0, i) + ".spi." + name;

        // Use the class loader of the getBundle caller so that the caller's
        // visibility of the provider type is checked.
        return AccessController.doPrivileged(
            new PrivilegedAction<>() {
                @Override
                public Class<ResourceBundleProvider> run() {
                    try {
                        Class<?> c = Class.forName(providerName, false, loader);
                        if (ResourceBundleProvider.class.isAssignableFrom(c)) {
                            @SuppressWarnings("unchecked")
                            Class<ResourceBundleProvider> s = (Class<ResourceBundleProvider>) c;
                            return s;
                        }
                    } catch (ClassNotFoundException e) {}
                    return null;
                }
            });
    }

    /**
     * Loads ResourceBundle from service providers.
     *
    private static ResourceBundle loadBundleFromProviders(String baseName,
                                                          Locale locale,
                                                          ServiceLoader<ResourceBundleProvider> providers,
                                                          CacheKey cacheKey)
    {
        if (providers == null) return null;

        return AccessController.doPrivileged(
                new PrivilegedAction<>() {
                    public ResourceBundle run() {
                        for (Iterator<ResourceBundleProvider> itr = providers.iterator(); itr.hasNext(); ) {
                            try {
                                ResourceBundleProvider provider = itr.next();
                                if (cacheKey != null && cacheKey.callerHasProvider == null
                                        && cacheKey.getModule() == provider.getClass().getModule()) {
                                    cacheKey.callerHasProvider = Boolean.TRUE;
                                }
                                ResourceBundle bundle = provider.getBundle(baseName, locale);
                                trace("provider %s %s locale: %s bundle: %s%n", provider, baseName, locale, bundle);
                                if (bundle != null) {
                                    return bundle;
                                }
                            } catch (ServiceConfigurationError | SecurityException e) {
                                if (cacheKey != null) {
                                    cacheKey.setCause(e);
                                }
                            }
                        }
                        if (cacheKey != null && cacheKey.callerHasProvider == null) {
                            cacheKey.callerHasProvider = Boolean.FALSE;
                        }
                        return null;
                    }
                });

    }
    */

    /*
     * Legacy mechanism to load resource bundles
     */
    private static ResourceBundle loadBundle(CacheKey cacheKey,
                                             List<String> formats,
                                             Control control,
                                             boolean reload) {

        // Here we actually load the bundle in the order of formats
        // specified by the getFormats() value.
        Locale targetLocale = cacheKey.getLocale();

        // Android-removed: modules are not supported
        /*
        Module module = cacheKey.getModule();
        if (module == null) {
            // should not happen
            throw new InternalError(
                "Module for cache key: " + cacheKey + " has been GCed.");
        }
        ClassLoader loader = getLoaderForControl(module);
        */

        ResourceBundle bundle = null;
        for (String format : formats) {
            try {
                // ResourceBundle.Control.newBundle may be overridden
                bundle = control.newBundle(cacheKey.getName(), targetLocale, format,
                                           cacheKey.getLoader(), reload);
            } catch (LinkageError | Exception error) {
                // We need to handle the LinkageError case due to
                // inconsistent case-sensitivity in ClassLoader.
                // See 6572242 for details.
                cacheKey.setCause(error);
            }
            if (bundle != null) {
                // Set the format in the cache key so that it can be
                // used when calling needsReload later.
                cacheKey.setFormat(format);
                bundle.name = cacheKey.getName();
                bundle.locale = targetLocale;
                // Bundle provider might reuse instances. So we should make
                // sure to clear the expired flag here.
                bundle.expired = false;
                break;
            }
        }

        return bundle;
    }

    private static boolean isValidBundle(ResourceBundle bundle) {
        return bundle != null && bundle != NONEXISTENT_BUNDLE;
    }

    /**
     * Determines whether any of resource bundles in the parent chain,
     * including the leaf, have expired.
     */
    private static boolean hasValidParentChain(ResourceBundle bundle) {
        long now = System.currentTimeMillis();
        while (bundle != null) {
            if (bundle.expired) {
                return false;
            }
            CacheKey key = bundle.cacheKey;
            if (key != null) {
                long expirationTime = key.expirationTime;
                if (expirationTime >= 0 && expirationTime <= now) {
                    return false;
                }
            }
            bundle = bundle.parent;
        }
        return true;
    }

    /**
     * Throw a MissingResourceException with proper message
     */
    private static void throwMissingResourceException(String baseName,
                                                      Locale locale,
                                                      Throwable cause) {
        // If the cause is a MissingResourceException, avoid creating
        // a long chain. (6355009)
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        throw new MissingResourceException("Can't find bundle for base name "
                                           + baseName + ", locale " + locale,
                                           baseName + "_" + locale, // className
                                           "",                      // key
                                           cause);
    }

    /**
     * Finds a bundle in the cache. Any expired bundles are marked as
     * `expired' and removed from the cache upon return.
     *
     * @param cacheKey the key to look up the cache
     * @param control the Control to be used for the expiration control
     * @return the cached bundle, or null if the bundle is not found in the
     * cache or its parent has expired. <code>bundle.expire</code> is true
     * upon return if the bundle in the cache has expired.
     */
    private static ResourceBundle findBundleInCache(CacheKey cacheKey,
                                                    Control control) {
        BundleReference bundleRef = cacheList.get(cacheKey);
        if (bundleRef == null) {
            return null;
        }
        ResourceBundle bundle = bundleRef.get();
        if (bundle == null) {
            return null;
        }
        ResourceBundle p = bundle.parent;
        assert p != NONEXISTENT_BUNDLE;
        // If the parent has expired, then this one must also expire. We
        // check only the immediate parent because the actual loading is
        // done from the root (base) to leaf (child) and the purpose of
        // checking is to propagate expiration towards the leaf. For
        // example, if the requested locale is ja_JP_JP and there are
        // bundles for all of the candidates in the cache, we have a list,
        //
        // base <- ja <- ja_JP <- ja_JP_JP
        //
        // If ja has expired, then it will reload ja and the list becomes a
        // tree.
        //
        // base <- ja (new)
        //  "   <- ja (expired) <- ja_JP <- ja_JP_JP
        //
        // When looking up ja_JP in the cache, it finds ja_JP in the cache
        // which references to the expired ja. Then, ja_JP is marked as
        // expired and removed from the cache. This will be propagated to
        // ja_JP_JP.
        //
        // Now, it's possible, for example, that while loading new ja_JP,
        // someone else has started loading the same bundle and finds the
        // base bundle has expired. Then, what we get from the first
        // getBundle call includes the expired base bundle. However, if
        // someone else didn't start its loading, we wouldn't know if the
        // base bundle has expired at the end of the loading process. The
        // expiration control doesn't guarantee that the returned bundle and
        // its parents haven't expired.
        //
        // We could check the entire parent chain to see if there's any in
        // the chain that has expired. But this process may never end. An
        // extreme case would be that getTimeToLive returns 0 and
        // needsReload always returns true.
        if (p != null && p.expired) {
            assert bundle != NONEXISTENT_BUNDLE;
            bundle.expired = true;
            bundle.cacheKey = null;
            cacheList.remove(cacheKey, bundleRef);
            bundle = null;
        } else {
            CacheKey key = bundleRef.getCacheKey();
            long expirationTime = key.expirationTime;
            if (!bundle.expired && expirationTime >= 0 &&
                expirationTime <= System.currentTimeMillis()) {
                // its TTL period has expired.
                if (bundle != NONEXISTENT_BUNDLE) {
                    // Synchronize here to call needsReload to avoid
                    // redundant concurrent calls for the same bundle.
                    synchronized (bundle) {
                        expirationTime = key.expirationTime;
                        if (!bundle.expired && expirationTime >= 0 &&
                            expirationTime <= System.currentTimeMillis()) {
                            try {
                                // Android-changed: modules are not supported.
                                /*
                                Module module = cacheKey.getModule();
                                bundle.expired =
                                   module == null || // already GCed
                                */
                                bundle.expired = control.needsReload(key.getName(),
                                                        key.getLocale(),
                                                        key.getFormat(),
                                                        // getLoaderForControl(module),
                                                        key.getLoader(),
                                                        bundle,
                                                        key.loadTime);
                            } catch (Exception e) {
                                cacheKey.setCause(e);
                            }
                            if (bundle.expired) {
                                // If the bundle needs to be reloaded, then
                                // remove the bundle from the cache, but
                                // return the bundle with the expired flag
                                // on.
                                bundle.cacheKey = null;
                                cacheList.remove(cacheKey, bundleRef);
                            } else {
                                // Update the expiration control info. and reuse
                                // the same bundle instance
                                setExpirationTime(key, control);
                            }
                        }
                    }
                } else {
                    // We just remove NONEXISTENT_BUNDLE from the cache.
                    cacheList.remove(cacheKey, bundleRef);
                    bundle = null;
                }
            }
        }
        return bundle;
    }

    /**
     * Put a new bundle in the cache.
     *
     * @param cacheKey the key for the resource bundle
     * @param bundle the resource bundle to be put in the cache
     * @return the ResourceBundle for the cacheKey; if someone has put
     * the bundle before this call, the one found in the cache is
     * returned.
     */
    private static ResourceBundle putBundleInCache(CacheKey cacheKey,
                                                   ResourceBundle bundle,
                                                   Control control) {
        setExpirationTime(cacheKey, control);
        if (cacheKey.expirationTime != Control.TTL_DONT_CACHE) {
            CacheKey key = (CacheKey) cacheKey.clone();
            BundleReference bundleRef = new BundleReference(bundle, referenceQueue, key);
            bundle.cacheKey = key;

            // Put the bundle in the cache if it's not been in the cache.
            BundleReference result = cacheList.putIfAbsent(key, bundleRef);

            // If someone else has put the same bundle in the cache before
            // us and it has not expired, we should use the one in the cache.
            if (result != null) {
                ResourceBundle rb = result.get();
                if (rb != null && !rb.expired) {
                    // Clear the back link to the cache key
                    bundle.cacheKey = null;
                    bundle = rb;
                    // Clear the reference in the BundleReference so that
                    // it won't be enqueued.
                    bundleRef.clear();
                } else {
                    // Replace the invalid (garbage collected or expired)
                    // instance with the valid one.
                    cacheList.put(key, bundleRef);
                }
            }
        }
        return bundle;
    }

    private static void setExpirationTime(CacheKey cacheKey, Control control) {
        long ttl = control.getTimeToLive(cacheKey.getName(),
                                         cacheKey.getLocale());
        if (ttl >= 0) {
            // If any expiration time is specified, set the time to be
            // expired in the cache.
            long now = System.currentTimeMillis();
            cacheKey.loadTime = now;
            cacheKey.expirationTime = now + ttl;
        } else if (ttl >= Control.TTL_NO_EXPIRATION_CONTROL) {
            cacheKey.expirationTime = ttl;
        } else {
            throw new IllegalArgumentException("Invalid Control: TTL=" + ttl);
        }
    }

    /**
     * Removes all resource bundles from the cache that have been loaded
     * using the caller's class loader.
     *
     * @since 1.6
     * @revised 9
     * @see ResourceBundle.Control#getTimeToLive(String,Locale)
     */
    @CallerSensitive
    public static final void clearCache() {
        // Android-changed: modules are not supported.
        /*
        Class<?> caller = Reflection.getCallerClass();
        cacheList.keySet().removeIf(
            key -> key.getCallerModule() == caller.getModule()
        );
        */
        clearCache(getLoader(Reflection.getCallerClass()));
    }

    /**
     * Removes all resource bundles from the cache that have been loaded
     * by the given class loader.
     *
     * @param loader the class loader
     * @exception NullPointerException if <code>loader</code> is null
     * @since 1.6
     * @see ResourceBundle.Control#getTimeToLive(String,Locale)
     */
    public static final void clearCache(ClassLoader loader) {
        Objects.requireNonNull(loader);
        // Android-changed: modules are no supported.
        /*
        cacheList.keySet().removeIf(
            key -> {
                Module m;
                return (m = key.getModule()) != null &&
                       getLoader(m) == loader;
            }
        );
        */
        cacheList.keySet().removeIf(key -> key.getLoader() == loader);
    }

    /**
     * Gets an object for the given key from this resource bundle.
     * Returns null if this resource bundle does not contain an
     * object for the given key.
     *
     * @param key the key for the desired object
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @return the object for the given key, or null
     */
    protected abstract Object handleGetObject(String key);

    /**
     * Returns an enumeration of the keys.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     *         this <code>ResourceBundle</code> and its parent bundles.
     */
    public abstract Enumeration<String> getKeys();

    /**
     * Determines whether the given <code>key</code> is contained in
     * this <code>ResourceBundle</code> or its parent bundles.
     *
     * @param key
     *        the resource <code>key</code>
     * @return <code>true</code> if the given <code>key</code> is
     *        contained in this <code>ResourceBundle</code> or its
     *        parent bundles; <code>false</code> otherwise.
     * @exception NullPointerException
     *         if <code>key</code> is <code>null</code>
     * @since 1.6
     */
    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            if (rb.handleKeySet().contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a <code>Set</code> of all keys contained in this
     * <code>ResourceBundle</code> and its parent bundles.
     *
     * @return a <code>Set</code> of all keys contained in this
     *         <code>ResourceBundle</code> and its parent bundles.
     * @since 1.6
     */
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            keys.addAll(rb.handleKeySet());
        }
        return keys;
    }

    /**
     * Returns a <code>Set</code> of the keys contained <em>only</em>
     * in this <code>ResourceBundle</code>.
     *
     * <p>The default implementation returns a <code>Set</code> of the
     * keys returned by the {@link #getKeys() getKeys} method except
     * for the ones for which the {@link #handleGetObject(String)
     * handleGetObject} method returns <code>null</code>. Once the
     * <code>Set</code> has been created, the value is kept in this
     * <code>ResourceBundle</code> in order to avoid producing the
     * same <code>Set</code> in subsequent calls. Subclasses can
     * override this method for faster handling.
     *
     * @return a <code>Set</code> of the keys contained only in this
     *        <code>ResourceBundle</code>
     * @since 1.6
     */
    protected Set<String> handleKeySet() {
        if (keySet == null) {
            synchronized (this) {
                if (keySet == null) {
                    Set<String> keys = new HashSet<>();
                    Enumeration<String> enumKeys = getKeys();
                    while (enumKeys.hasMoreElements()) {
                        String key = enumKeys.nextElement();
                        if (handleGetObject(key) != null) {
                            keys.add(key);
                        }
                    }
                    keySet = keys;
                }
            }
        }
        return keySet;
    }



    /**
     * <code>ResourceBundle.Control</code> defines a set of callback methods
     * that are invoked by the {@link ResourceBundle#getBundle(String,
     * Locale, ClassLoader, Control) ResourceBundle.getBundle} factory
     * methods during the bundle loading process. In other words, a
     * <code>ResourceBundle.Control</code> collaborates with the factory
     * methods for loading resource bundles. The default implementation of
     * the callback methods provides the information necessary for the
     * factory methods to perform the <a
     * href="./ResourceBundle.html#default_behavior">default behavior</a>.
     *
     * <p>In addition to the callback methods, the {@link
     * #toBundleName(String, Locale) toBundleName} and {@link
     * #toResourceName(String, String) toResourceName} methods are defined
     * primarily for convenience in implementing the callback
     * methods. However, the <code>toBundleName</code> method could be
     * overridden to provide different conventions in the organization and
     * packaging of localized resources.  The <code>toResourceName</code>
     * method is <code>final</code> to avoid use of wrong resource and class
     * name separators.
     *
     * <p>Two factory methods, {@link #getControl(List)} and {@link
     * #getNoFallbackControl(List)}, provide
     * <code>ResourceBundle.Control</code> instances that implement common
     * variations of the default bundle loading process.
     *
     * <p>The formats returned by the {@link Control#getFormats(String)
     * getFormats} method and candidate locales returned by the {@link
     * ResourceBundle.Control#getCandidateLocales(String, Locale)
     * getCandidateLocales} method must be consistent in all
     * <code>ResourceBundle.getBundle</code> invocations for the same base
     * bundle. Otherwise, the <code>ResourceBundle.getBundle</code> methods
     * may return unintended bundles. For example, if only
     * <code>"java.class"</code> is returned by the <code>getFormats</code>
     * method for the first call to <code>ResourceBundle.getBundle</code>
     * and only <code>"java.properties"</code> for the second call, then the
     * second call will return the class-based one that has been cached
     * during the first call.
     *
     * <p>A <code>ResourceBundle.Control</code> instance must be thread-safe
     * if it's simultaneously used by multiple threads.
     * <code>ResourceBundle.getBundle</code> does not synchronize to call
     * the <code>ResourceBundle.Control</code> methods. The default
     * implementations of the methods are thread-safe.
     *
     * <p>Applications can specify <code>ResourceBundle.Control</code>
     * instances returned by the <code>getControl</code> factory methods or
     * created from a subclass of <code>ResourceBundle.Control</code> to
     * customize the bundle loading process. The following are examples of
     * changing the default bundle loading process.
     *
     * <p><b>Example 1</b>
     *
     * <p>The following code lets <code>ResourceBundle.getBundle</code> look
     * up only properties-based resources.
     *
     * <pre>
     * import java.util.*;
     * import static java.util.ResourceBundle.Control.*;
     * ...
     * ResourceBundle bundle =
     *   ResourceBundle.getBundle("MyResources", new Locale("fr", "CH"),
     *                            ResourceBundle.Control.getControl(FORMAT_PROPERTIES));
     * </pre>
     *
     * Given the resource bundles in the <a
     * href="./ResourceBundle.html#default_behavior_example">example</a> in
     * the <code>ResourceBundle.getBundle</code> description, this
     * <code>ResourceBundle.getBundle</code> call loads
     * <code>MyResources_fr_CH.properties</code> whose parent is
     * <code>MyResources_fr.properties</code> whose parent is
     * <code>MyResources.properties</code>. (<code>MyResources_fr_CH.properties</code>
     * is not hidden, but <code>MyResources_fr_CH.class</code> is.)
     *
     * <p><b>Example 2</b>
     *
     * <p>The following is an example of loading XML-based bundles
     * using {@link Properties#loadFromXML(java.io.InputStream)
     * Properties.loadFromXML}.
     *
     * <pre>
     * ResourceBundle rb = ResourceBundle.getBundle("Messages",
     *     new ResourceBundle.Control() {
     *         public List&lt;String&gt; getFormats(String baseName) {
     *             if (baseName == null)
     *                 throw new NullPointerException();
     *             return Arrays.asList("xml");
     *         }
     *         public ResourceBundle newBundle(String baseName,
     *                                         Locale locale,
     *                                         String format,
     *                                         ClassLoader loader,
     *                                         boolean reload)
     *                          throws IllegalAccessException,
     *                                 InstantiationException,
     *                                 IOException {
     *             if (baseName == null || locale == null
     *                   || format == null || loader == null)
     *                 throw new NullPointerException();
     *             ResourceBundle bundle = null;
     *             if (format.equals("xml")) {
     *                 String bundleName = toBundleName(baseName, locale);
     *                 String resourceName = toResourceName(bundleName, format);
     *                 InputStream stream = null;
     *                 if (reload) {
     *                     URL url = loader.getResource(resourceName);
     *                     if (url != null) {
     *                         URLConnection connection = url.openConnection();
     *                         if (connection != null) {
     *                             // Disable caches to get fresh data for
     *                             // reloading.
     *                             connection.setUseCaches(false);
     *                             stream = connection.getInputStream();
     *                         }
     *                     }
     *                 } else {
     *                     stream = loader.getResourceAsStream(resourceName);
     *                 }
     *                 if (stream != null) {
     *                     BufferedInputStream bis = new BufferedInputStream(stream);
     *                     bundle = new XMLResourceBundle(bis);
     *                     bis.close();
     *                 }
     *             }
     *             return bundle;
     *         }
     *     });
     *
     * ...
     *
     * private static class XMLResourceBundle extends ResourceBundle {
     *     private Properties props;
     *     XMLResourceBundle(InputStream stream) throws IOException {
     *         props = new Properties();
     *         props.loadFromXML(stream);
     *     }
     *     protected Object handleGetObject(String key) {
     *         return props.getProperty(key);
     *     }
     *     public Enumeration&lt;String&gt; getKeys() {
     *         ...
     *     }
     * }
     * </pre>
     *
     * @since 1.6
     * @revised 9
     */
    public static class Control {
        /**
         * The default format <code>List</code>, which contains the strings
         * <code>"java.class"</code> and <code>"java.properties"</code>, in
         * this order. This <code>List</code> is unmodifiable.
         *
         * @see #getFormats(String)
         */
        public static final List<String> FORMAT_DEFAULT
            = List.of("java.class", "java.properties");

        /**
         * The class-only format <code>List</code> containing
         * <code>"java.class"</code>. This <code>List</code> is unmodifiable.
         *
         * @see #getFormats(String)
         */
        public static final List<String> FORMAT_CLASS = List.of("java.class");

        /**
         * The properties-only format <code>List</code> containing
         * <code>"java.properties"</code>. This <code>List</code> is unmodifiable.
         *
         * @see #getFormats(String)
         */
        public static final List<String> FORMAT_PROPERTIES
            = List.of("java.properties");

        /**
         * The time-to-live constant for not caching loaded resource bundle
         * instances.
         *
         * @see #getTimeToLive(String, Locale)
         */
        public static final long TTL_DONT_CACHE = -1;

        /**
         * The time-to-live constant for disabling the expiration control
         * for loaded resource bundle instances in the cache.
         *
         * @see #getTimeToLive(String, Locale)
         */
        public static final long TTL_NO_EXPIRATION_CONTROL = -2;

        private static final Control INSTANCE = new Control();

        /**
         * Sole constructor. (For invocation by subclass constructors,
         * typically implicit.)
         */
        protected Control() {
        }

        /**
         * Returns a <code>ResourceBundle.Control</code> in which the {@link
         * #getFormats(String) getFormats} method returns the specified
         * <code>formats</code>. The <code>formats</code> must be equal to
         * one of {@link Control#FORMAT_PROPERTIES}, {@link
         * Control#FORMAT_CLASS} or {@link
         * Control#FORMAT_DEFAULT}. <code>ResourceBundle.Control</code>
         * instances returned by this method are singletons and thread-safe.
         *
         * <p>Specifying {@link Control#FORMAT_DEFAULT} is equivalent to
         * instantiating the <code>ResourceBundle.Control</code> class,
         * except that this method returns a singleton.
         *
         * @param formats
         *        the formats to be returned by the
         *        <code>ResourceBundle.Control.getFormats</code> method
         * @return a <code>ResourceBundle.Control</code> supporting the
         *        specified <code>formats</code>
         * @exception NullPointerException
         *        if <code>formats</code> is <code>null</code>
         * @exception IllegalArgumentException
         *        if <code>formats</code> is unknown
         */
        public static final Control getControl(List<String> formats) {
            if (formats.equals(Control.FORMAT_PROPERTIES)) {
                return SingleFormatControl.PROPERTIES_ONLY;
            }
            if (formats.equals(Control.FORMAT_CLASS)) {
                return SingleFormatControl.CLASS_ONLY;
            }
            if (formats.equals(Control.FORMAT_DEFAULT)) {
                return Control.INSTANCE;
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns a <code>ResourceBundle.Control</code> in which the {@link
         * #getFormats(String) getFormats} method returns the specified
         * <code>formats</code> and the {@link
         * Control#getFallbackLocale(String, Locale) getFallbackLocale}
         * method returns <code>null</code>. The <code>formats</code> must
         * be equal to one of {@link Control#FORMAT_PROPERTIES}, {@link
         * Control#FORMAT_CLASS} or {@link Control#FORMAT_DEFAULT}.
         * <code>ResourceBundle.Control</code> instances returned by this
         * method are singletons and thread-safe.
         *
         * @param formats
         *        the formats to be returned by the
         *        <code>ResourceBundle.Control.getFormats</code> method
         * @return a <code>ResourceBundle.Control</code> supporting the
         *        specified <code>formats</code> with no fallback
         *        <code>Locale</code> support
         * @exception NullPointerException
         *        if <code>formats</code> is <code>null</code>
         * @exception IllegalArgumentException
         *        if <code>formats</code> is unknown
         */
        public static final Control getNoFallbackControl(List<String> formats) {
            if (formats.equals(Control.FORMAT_DEFAULT)) {
                return NoFallbackControl.NO_FALLBACK;
            }
            if (formats.equals(Control.FORMAT_PROPERTIES)) {
                return NoFallbackControl.PROPERTIES_ONLY_NO_FALLBACK;
            }
            if (formats.equals(Control.FORMAT_CLASS)) {
                return NoFallbackControl.CLASS_ONLY_NO_FALLBACK;
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns a <code>List</code> of <code>String</code>s containing
         * formats to be used to load resource bundles for the given
         * <code>baseName</code>. The <code>ResourceBundle.getBundle</code>
         * factory method tries to load resource bundles with formats in the
         * order specified by the list. The list returned by this method
         * must have at least one <code>String</code>. The predefined
         * formats are <code>"java.class"</code> for class-based resource
         * bundles and <code>"java.properties"</code> for {@linkplain
         * PropertyResourceBundle properties-based} ones. Strings starting
         * with <code>"java."</code> are reserved for future extensions and
         * must not be used by application-defined formats.
         *
         * <p>It is not a requirement to return an immutable (unmodifiable)
         * <code>List</code>.  However, the returned <code>List</code> must
         * not be mutated after it has been returned by
         * <code>getFormats</code>.
         *
         * <p>The default implementation returns {@link #FORMAT_DEFAULT} so
         * that the <code>ResourceBundle.getBundle</code> factory method
         * looks up first class-based resource bundles, then
         * properties-based ones.
         *
         * @param baseName
         *        the base name of the resource bundle, a fully qualified class
         *        name
         * @return a <code>List</code> of <code>String</code>s containing
         *        formats for loading resource bundles.
         * @exception NullPointerException
         *        if <code>baseName</code> is null
         * @see #FORMAT_DEFAULT
         * @see #FORMAT_CLASS
         * @see #FORMAT_PROPERTIES
         */
        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return FORMAT_DEFAULT;
        }

        /**
         * Returns a <code>List</code> of <code>Locale</code>s as candidate
         * locales for <code>baseName</code> and <code>locale</code>. This
         * method is called by the <code>ResourceBundle.getBundle</code>
         * factory method each time the factory method tries finding a
         * resource bundle for a target <code>Locale</code>.
         *
         * <p>The sequence of the candidate locales also corresponds to the
         * runtime resource lookup path (also known as the <I>parent
         * chain</I>), if the corresponding resource bundles for the
         * candidate locales exist and their parents are not defined by
         * loaded resource bundles themselves.  The last element of the list
         * must be a {@linkplain Locale#ROOT root locale} if it is desired to
         * have the base bundle as the terminal of the parent chain.
         *
         * <p>If the given locale is equal to <code>Locale.ROOT</code> (the
         * root locale), a <code>List</code> containing only the root
         * <code>Locale</code> must be returned. In this case, the
         * <code>ResourceBundle.getBundle</code> factory method loads only
         * the base bundle as the resulting resource bundle.
         *
         * <p>It is not a requirement to return an immutable (unmodifiable)
         * <code>List</code>. However, the returned <code>List</code> must not
         * be mutated after it has been returned by
         * <code>getCandidateLocales</code>.
         *
         * <p>The default implementation returns a <code>List</code> containing
         * <code>Locale</code>s using the rules described below.  In the
         * description below, <em>L</em>, <em>S</em>, <em>C</em> and <em>V</em>
         * respectively represent non-empty language, script, country, and
         * variant.  For example, [<em>L</em>, <em>C</em>] represents a
         * <code>Locale</code> that has non-empty values only for language and
         * country.  The form <em>L</em>("xx") represents the (non-empty)
         * language value is "xx".  For all cases, <code>Locale</code>s whose
         * final component values are empty strings are omitted.
         *
         * <ol><li>For an input <code>Locale</code> with an empty script value,
         * append candidate <code>Locale</code>s by omitting the final component
         * one by one as below:
         *
         * <ul>
         * <li> [<em>L</em>, <em>C</em>, <em>V</em>] </li>
         * <li> [<em>L</em>, <em>C</em>] </li>
         * <li> [<em>L</em>] </li>
         * <li> <code>Locale.ROOT</code> </li>
         * </ul></li>
         *
         * <li>For an input <code>Locale</code> with a non-empty script value,
         * append candidate <code>Locale</code>s by omitting the final component
         * up to language, then append candidates generated from the
         * <code>Locale</code> with country and variant restored:
         *
         * <ul>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>, <em>V</em>]</li>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>]</li>
         * <li> [<em>L</em>, <em>S</em>]</li>
         * <li> [<em>L</em>, <em>C</em>, <em>V</em>]</li>
         * <li> [<em>L</em>, <em>C</em>]</li>
         * <li> [<em>L</em>]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul></li>
         *
         * <li>For an input <code>Locale</code> with a variant value consisting
         * of multiple subtags separated by underscore, generate candidate
         * <code>Locale</code>s by omitting the variant subtags one by one, then
         * insert them after every occurrence of <code> Locale</code>s with the
         * full variant value in the original list.  For example, if
         * the variant consists of two subtags <em>V1</em> and <em>V2</em>:
         *
         * <ul>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>, <em>V1</em>, <em>V2</em>]</li>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>, <em>V1</em>]</li>
         * <li> [<em>L</em>, <em>S</em>, <em>C</em>]</li>
         * <li> [<em>L</em>, <em>S</em>]</li>
         * <li> [<em>L</em>, <em>C</em>, <em>V1</em>, <em>V2</em>]</li>
         * <li> [<em>L</em>, <em>C</em>, <em>V1</em>]</li>
         * <li> [<em>L</em>, <em>C</em>]</li>
         * <li> [<em>L</em>]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul></li>
         *
         * <li>Special cases for Chinese.  When an input <code>Locale</code> has the
         * language "zh" (Chinese) and an empty script value, either "Hans" (Simplified) or
         * "Hant" (Traditional) might be supplied, depending on the country.
         * When the country is "CN" (China) or "SG" (Singapore), "Hans" is supplied.
         * When the country is "HK" (Hong Kong SAR China), "MO" (Macau SAR China),
         * or "TW" (Taiwan), "Hant" is supplied.  For all other countries or when the country
         * is empty, no script is supplied.  For example, for <code>Locale("zh", "CN")
         * </code>, the candidate list will be:
         * <ul>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hans"), <em>C</em>("CN")]</li>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hans")]</li>
         * <li> [<em>L</em>("zh"), <em>C</em>("CN")]</li>
         * <li> [<em>L</em>("zh")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul>
         *
         * For <code>Locale("zh", "TW")</code>, the candidate list will be:
         * <ul>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hant"), <em>C</em>("TW")]</li>
         * <li> [<em>L</em>("zh"), <em>S</em>("Hant")]</li>
         * <li> [<em>L</em>("zh"), <em>C</em>("TW")]</li>
         * <li> [<em>L</em>("zh")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul></li>
         *
         * <li>Special cases for Norwegian.  Both <code>Locale("no", "NO",
         * "NY")</code> and <code>Locale("nn", "NO")</code> represent Norwegian
         * Nynorsk.  When a locale's language is "nn", the standard candidate
         * list is generated up to [<em>L</em>("nn")], and then the following
         * candidates are added:
         *
         * <ul><li> [<em>L</em>("no"), <em>C</em>("NO"), <em>V</em>("NY")]</li>
         * <li> [<em>L</em>("no"), <em>C</em>("NO")]</li>
         * <li> [<em>L</em>("no")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul>
         *
         * If the locale is exactly <code>Locale("no", "NO", "NY")</code>, it is first
         * converted to <code>Locale("nn", "NO")</code> and then the above procedure is
         * followed.
         *
         * <p>Also, Java treats the language "no" as a synonym of Norwegian
         * Bokm&#xE5;l "nb".  Except for the single case <code>Locale("no",
         * "NO", "NY")</code> (handled above), when an input <code>Locale</code>
         * has language "no" or "nb", candidate <code>Locale</code>s with
         * language code "no" and "nb" are interleaved, first using the
         * requested language, then using its synonym. For example,
         * <code>Locale("nb", "NO", "POSIX")</code> generates the following
         * candidate list:
         *
         * <ul>
         * <li> [<em>L</em>("nb"), <em>C</em>("NO"), <em>V</em>("POSIX")]</li>
         * <li> [<em>L</em>("no"), <em>C</em>("NO"), <em>V</em>("POSIX")]</li>
         * <li> [<em>L</em>("nb"), <em>C</em>("NO")]</li>
         * <li> [<em>L</em>("no"), <em>C</em>("NO")]</li>
         * <li> [<em>L</em>("nb")]</li>
         * <li> [<em>L</em>("no")]</li>
         * <li> <code>Locale.ROOT</code></li>
         * </ul>
         *
         * <code>Locale("no", "NO", "POSIX")</code> would generate the same list
         * except that locales with "no" would appear before the corresponding
         * locales with "nb".</li>
         * </ol>
         *
         * <p>The default implementation uses an {@link ArrayList} that
         * overriding implementations may modify before returning it to the
         * caller. However, a subclass must not modify it after it has
         * been returned by <code>getCandidateLocales</code>.
         *
         * <p>For example, if the given <code>baseName</code> is "Messages"
         * and the given <code>locale</code> is
         * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>, then a
         * <code>List</code> of <code>Locale</code>s:
         * <pre>
         *     Locale("ja", "", "XX")
         *     Locale("ja")
         *     Locale.ROOT
         * </pre>
         * is returned. And if the resource bundles for the "ja" and
         * "" <code>Locale</code>s are found, then the runtime resource
         * lookup path (parent chain) is:
         * <pre>{@code
         *     Messages_ja -> Messages
         * }</pre>
         *
         * @param baseName
         *        the base name of the resource bundle, a fully
         *        qualified class name
         * @param locale
         *        the locale for which a resource bundle is desired
         * @return a <code>List</code> of candidate
         *        <code>Locale</code>s for the given <code>locale</code>
         * @exception NullPointerException
         *        if <code>baseName</code> or <code>locale</code> is
         *        <code>null</code>
         */
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return new ArrayList<>(CANDIDATES_CACHE.get(locale.getBaseLocale()));
        }

        private static final CandidateListCache CANDIDATES_CACHE = new CandidateListCache();

        private static class CandidateListCache extends LocaleObjectCache<BaseLocale, List<Locale>> {
            protected List<Locale> createObject(BaseLocale base) {
                String language = base.getLanguage();
                String script = base.getScript();
                String region = base.getRegion();
                String variant = base.getVariant();

                // Special handling for Norwegian
                boolean isNorwegianBokmal = false;
                boolean isNorwegianNynorsk = false;
                if (language.equals("no")) {
                    if (region.equals("NO") && variant.equals("NY")) {
                        variant = "";
                        isNorwegianNynorsk = true;
                    } else {
                        isNorwegianBokmal = true;
                    }
                }
                if (language.equals("nb") || isNorwegianBokmal) {
                    List<Locale> tmpList = getDefaultList("nb", script, region, variant);
                    // Insert a locale replacing "nb" with "no" for every list entry
                    List<Locale> bokmalList = new LinkedList<>();
                    for (Locale l : tmpList) {
                        bokmalList.add(l);
                        if (l.getLanguage().isEmpty()) {
                            break;
                        }
                        bokmalList.add(Locale.getInstance("no", l.getScript(), l.getCountry(),
                                l.getVariant(), null));
                    }
                    return bokmalList;
                } else if (language.equals("nn") || isNorwegianNynorsk) {
                    // Insert no_NO_NY, no_NO, no after nn
                    List<Locale> nynorskList = getDefaultList("nn", script, region, variant);
                    int idx = nynorskList.size() - 1;
                    nynorskList.add(idx++, Locale.getInstance("no", "NO", "NY"));
                    nynorskList.add(idx++, Locale.getInstance("no", "NO", ""));
                    nynorskList.add(idx++, Locale.getInstance("no", "", ""));
                    return nynorskList;
                }
                // Special handling for Chinese
                else if (language.equals("zh")) {
                    if (script.isEmpty() && !region.isEmpty()) {
                        // Supply script for users who want to use zh_Hans/zh_Hant
                        // as bundle names (recommended for Java7+)
                        switch (region) {
                        case "TW":
                        case "HK":
                        case "MO":
                            script = "Hant";
                            break;
                        case "CN":
                        case "SG":
                            script = "Hans";
                            break;
                        }
                    }
                }

                return getDefaultList(language, script, region, variant);
            }

            private static List<Locale> getDefaultList(String language, String script, String region, String variant) {
                List<String> variants = null;

                if (!variant.isEmpty()) {
                    variants = new LinkedList<>();
                    int idx = variant.length();
                    while (idx != -1) {
                        variants.add(variant.substring(0, idx));
                        idx = variant.lastIndexOf('_', --idx);
                    }
                }

                List<Locale> list = new LinkedList<>();

                if (variants != null) {
                    for (String v : variants) {
                        list.add(Locale.getInstance(language, script, region, v, null));
                    }
                }
                if (!region.isEmpty()) {
                    list.add(Locale.getInstance(language, script, region, "", null));
                }
                if (!script.isEmpty()) {
                    list.add(Locale.getInstance(language, script, "", "", null));
                    // Special handling for Chinese
                    if (language.equals("zh")) {
                        if (region.isEmpty()) {
                            // Supply region(country) for users who still package Chinese
                            // bundles using old convension.
                            switch (script) {
                                case "Hans":
                                    region = "CN";
                                    break;
                                case "Hant":
                                    region = "TW";
                                    break;
                            }
                        }
                    }

                    // With script, after truncating variant, region and script,
                    // start over without script.
                    if (variants != null) {
                        for (String v : variants) {
                            list.add(Locale.getInstance(language, "", region, v, null));
                        }
                    }
                    if (!region.isEmpty()) {
                        list.add(Locale.getInstance(language, "", region, "", null));
                    }
                }
                if (!language.isEmpty()) {
                    list.add(Locale.getInstance(language, "", "", "", null));
                }
                // Add root locale at the end
                list.add(Locale.ROOT);

                return list;
            }
        }

        /**
         * Returns a <code>Locale</code> to be used as a fallback locale for
         * further resource bundle searches by the
         * <code>ResourceBundle.getBundle</code> factory method. This method
         * is called from the factory method every time when no resulting
         * resource bundle has been found for <code>baseName</code> and
         * <code>locale</code>, where locale is either the parameter for
         * <code>ResourceBundle.getBundle</code> or the previous fallback
         * locale returned by this method.
         *
         * <p>The method returns <code>null</code> if no further fallback
         * search is desired.
         *
         * <p>The default implementation returns the {@linkplain
         * Locale#getDefault() default <code>Locale</code>} if the given
         * <code>locale</code> isn't the default one.  Otherwise,
         * <code>null</code> is returned.
         *
         * @param baseName
         *        the base name of the resource bundle, a fully
         *        qualified class name for which
         *        <code>ResourceBundle.getBundle</code> has been
         *        unable to find any resource bundles (except for the
         *        base bundle)
         * @param locale
         *        the <code>Locale</code> for which
         *        <code>ResourceBundle.getBundle</code> has been
         *        unable to find any resource bundles (except for the
         *        base bundle)
         * @return a <code>Locale</code> for the fallback search,
         *        or <code>null</code> if no further fallback search
         *        is desired.
         * @exception NullPointerException
         *        if <code>baseName</code> or <code>locale</code>
         *        is <code>null</code>
         */
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            Locale defaultLocale = Locale.getDefault();
            return locale.equals(defaultLocale) ? null : defaultLocale;
        }

        /**
         * Instantiates a resource bundle for the given bundle name of the
         * given format and locale, using the given class loader if
         * necessary. This method returns <code>null</code> if there is no
         * resource bundle available for the given parameters. If a resource
         * bundle can't be instantiated due to an unexpected error, the
         * error must be reported by throwing an <code>Error</code> or
         * <code>Exception</code> rather than simply returning
         * <code>null</code>.
         *
         * <p>If the <code>reload</code> flag is <code>true</code>, it
         * indicates that this method is being called because the previously
         * loaded resource bundle has expired.
         *
         * <p>The default implementation instantiates a
         * <code>ResourceBundle</code> as follows.
         *
         * <ul>
         *
         * <li>The bundle name is obtained by calling {@link
         * #toBundleName(String, Locale) toBundleName(baseName,
         * locale)}.</li>
         *
         * <li>If <code>format</code> is <code>"java.class"</code>, the
         * {@link Class} specified by the bundle name is loaded with the
         * given class loader. If the {@code Class} is found and accessible
         * then the <code>ResourceBundle</code> is instantiated.  The
         * resource bundle is accessible if the package of the bundle class file
         * is open unconditionally; otherwise, {@code IllegalAccessException}
         * will be thrown.
         * Note that the <code>reload</code> flag is ignored for loading
         * class-based resource bundles in this default implementation.
         * </li>
         *
         * <li>If <code>format</code> is <code>"java.properties"</code>,
         * {@link #toResourceName(String, String) toResourceName(bundlename,
         * "properties")} is called to get the resource name.
         * If <code>reload</code> is <code>true</code>, {@link
         * ClassLoader#getResource(String) load.getResource} is called
         * to get a {@link URL} for creating a {@link
         * URLConnection}. This <code>URLConnection</code> is used to
         * {@linkplain URLConnection#setUseCaches(boolean) disable the
         * caches} of the underlying resource loading layers,
         * and to {@linkplain URLConnection#getInputStream() get an
         * <code>InputStream</code>}.
         * Otherwise, {@link ClassLoader#getResourceAsStream(String)
         * loader.getResourceAsStream} is called to get an {@link
         * InputStream}. Then, a {@link
         * PropertyResourceBundle} is constructed with the
         * <code>InputStream</code>.</li>
         *
         * <li>If <code>format</code> is neither <code>"java.class"</code>
         * nor <code>"java.properties"</code>, an
         * <code>IllegalArgumentException</code> is thrown.</li>
         *
         * </ul>
         *
         * @param baseName
         *        the base bundle name of the resource bundle, a fully
         *        qualified class name
         * @param locale
         *        the locale for which the resource bundle should be
         *        instantiated
         * @param format
         *        the resource bundle format to be loaded
         * @param loader
         *        the <code>ClassLoader</code> to use to load the bundle
         * @param reload
         *        the flag to indicate bundle reloading; <code>true</code>
         *        if reloading an expired resource bundle,
         *        <code>false</code> otherwise
         * @return the resource bundle instance,
         *        or <code>null</code> if none could be found.
         * @exception NullPointerException
         *        if <code>bundleName</code>, <code>locale</code>,
         *        <code>format</code>, or <code>loader</code> is
         *        <code>null</code>, or if <code>null</code> is returned by
         *        {@link #toBundleName(String, Locale) toBundleName}
         * @exception IllegalArgumentException
         *        if <code>format</code> is unknown, or if the resource
         *        found for the given parameters contains malformed data.
         * @exception ClassCastException
         *        if the loaded class cannot be cast to <code>ResourceBundle</code>
         * @exception IllegalAccessException
         *        if the class or its nullary constructor is not
         *        accessible.
         * @exception InstantiationException
         *        if the instantiation of a class fails for some other
         *        reason.
         * @exception ExceptionInInitializerError
         *        if the initialization provoked by this method fails.
         * @exception SecurityException
         *        If a security manager is present and creation of new
         *        instances is denied. See {@link Class#newInstance()}
         *        for details.
         * @exception IOException
         *        if an error occurred when reading resources using
         *        any I/O operations
         * @revised 9
         */
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends ResourceBundle> bundleClass
                        = (Class<? extends ResourceBundle>)loader.loadClass(bundleName);

                    // If the class isn't a ResourceBundle subclass, throw a
                    // ClassCastException.
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = bundleClass.newInstance();
                    } else {
                        throw new ClassCastException(bundleClass.getName()
                                     + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                }
            } else if (format.equals("java.properties")) {
                final String resourceName = toResourceName0(bundleName, "properties");
                if (resourceName == null) {
                    return bundle;
                }
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream = null;
                try {
                    stream = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<InputStream>() {
                            public InputStream run() throws IOException {
                                InputStream is = null;
                                if (reloadFlag) {
                                    URL url = classLoader.getResource(resourceName);
                                    if (url != null) {
                                        URLConnection connection = url.openConnection();
                                        if (connection != null) {
                                            // Disable caches to get fresh data for
                                            // reloading.
                                            connection.setUseCaches(false);
                                            is = connection.getInputStream();
                                        }
                                    }
                                } else {
                                    is = classLoader.getResourceAsStream(resourceName);
                                }
                                return is;
                            }
                        });
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                if (stream != null) {
                    try {
                        // Android-changed: Use UTF-8 for property based resources. b/26879578
                        // bundle = new PropertyResourceBundle(stream);
                        bundle = new PropertyResourceBundle(
                                new InputStreamReader(stream, StandardCharsets.UTF_8));
                    } finally {
                        stream.close();
                    }
                }
            } else {
                throw new IllegalArgumentException("unknown format: " + format);
            }
            return bundle;
        }

        /**
         * Returns the time-to-live (TTL) value for resource bundles that
         * are loaded under this
         * <code>ResourceBundle.Control</code>. Positive time-to-live values
         * specify the number of milliseconds a bundle can remain in the
         * cache without being validated against the source data from which
         * it was constructed. The value 0 indicates that a bundle must be
         * validated each time it is retrieved from the cache. {@link
         * #TTL_DONT_CACHE} specifies that loaded resource bundles are not
         * put in the cache. {@link #TTL_NO_EXPIRATION_CONTROL} specifies
         * that loaded resource bundles are put in the cache with no
         * expiration control.
         *
         * <p>The expiration affects only the bundle loading process by the
         * <code>ResourceBundle.getBundle</code> factory method.  That is,
         * if the factory method finds a resource bundle in the cache that
         * has expired, the factory method calls the {@link
         * #needsReload(String, Locale, String, ClassLoader, ResourceBundle,
         * long) needsReload} method to determine whether the resource
         * bundle needs to be reloaded. If <code>needsReload</code> returns
         * <code>true</code>, the cached resource bundle instance is removed
         * from the cache. Otherwise, the instance stays in the cache,
         * updated with the new TTL value returned by this method.
         *
         * <p>All cached resource bundles are subject to removal from the
         * cache due to memory constraints of the runtime environment.
         * Returning a large positive value doesn't mean to lock loaded
         * resource bundles in the cache.
         *
         * <p>The default implementation returns {@link #TTL_NO_EXPIRATION_CONTROL}.
         *
         * @param baseName
         *        the base name of the resource bundle for which the
         *        expiration value is specified.
         * @param locale
         *        the locale of the resource bundle for which the
         *        expiration value is specified.
         * @return the time (0 or a positive millisecond offset from the
         *        cached time) to get loaded bundles expired in the cache,
         *        {@link #TTL_NO_EXPIRATION_CONTROL} to disable the
         *        expiration control, or {@link #TTL_DONT_CACHE} to disable
         *        caching.
         * @exception NullPointerException
         *        if <code>baseName</code> or <code>locale</code> is
         *        <code>null</code>
         */
        public long getTimeToLive(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return TTL_NO_EXPIRATION_CONTROL;
        }

        /**
         * Determines if the expired <code>bundle</code> in the cache needs
         * to be reloaded based on the loading time given by
         * <code>loadTime</code> or some other criteria. The method returns
         * <code>true</code> if reloading is required; <code>false</code>
         * otherwise. <code>loadTime</code> is a millisecond offset since
         * the <a href="Calendar.html#Epoch"> <code>Calendar</code>
         * Epoch</a>.
         *
         * <p>
         * The calling <code>ResourceBundle.getBundle</code> factory method
         * calls this method on the <code>ResourceBundle.Control</code>
         * instance used for its current invocation, not on the instance
         * used in the invocation that originally loaded the resource
         * bundle.
         *
         * <p>The default implementation compares <code>loadTime</code> and
         * the last modified time of the source data of the resource
         * bundle. If it's determined that the source data has been modified
         * since <code>loadTime</code>, <code>true</code> is
         * returned. Otherwise, <code>false</code> is returned. This
         * implementation assumes that the given <code>format</code> is the
         * same string as its file suffix if it's not one of the default
         * formats, <code>"java.class"</code> or
         * <code>"java.properties"</code>.
         *
         * @param baseName
         *        the base bundle name of the resource bundle, a
         *        fully qualified class name
         * @param locale
         *        the locale for which the resource bundle
         *        should be instantiated
         * @param format
         *        the resource bundle format to be loaded
         * @param loader
         *        the <code>ClassLoader</code> to use to load the bundle
         * @param bundle
         *        the resource bundle instance that has been expired
         *        in the cache
         * @param loadTime
         *        the time when <code>bundle</code> was loaded and put
         *        in the cache
         * @return <code>true</code> if the expired bundle needs to be
         *        reloaded; <code>false</code> otherwise.
         * @exception NullPointerException
         *        if <code>baseName</code>, <code>locale</code>,
         *        <code>format</code>, <code>loader</code>, or
         *        <code>bundle</code> is <code>null</code>
         */
        public boolean needsReload(String baseName, Locale locale,
                                   String format, ClassLoader loader,
                                   ResourceBundle bundle, long loadTime) {
            if (bundle == null) {
                throw new NullPointerException();
            }
            if (format.equals("java.class") || format.equals("java.properties")) {
                format = format.substring(5);
            }
            boolean result = false;
            try {
                String resourceName = toResourceName0(toBundleName(baseName, locale), format);
                if (resourceName == null) {
                    return result;
                }
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    long lastModified = 0;
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        // disable caches to get the correct data
                        connection.setUseCaches(false);
                        // j2objc: disable jar loading (unsupported).
//                        if (connection instanceof JarURLConnection) {
//                            JarEntry ent = ((JarURLConnection)connection).getJarEntry();
//                            if (ent != null) {
//                                lastModified = ent.getTime();
//                                if (lastModified == -1) {
//                                    lastModified = 0;
//                                }
//                            }
//                        } else {
                            lastModified = connection.getLastModified();
//                        }
                    }
                    result = lastModified >= loadTime;
                }
            } catch (NullPointerException npe) {
                throw npe;
            } catch (Exception e) {
                // ignore other exceptions
            }
            return result;
        }

        /**
         * Converts the given <code>baseName</code> and <code>locale</code>
         * to the bundle name. This method is called from the default
         * implementation of the {@link #newBundle(String, Locale, String,
         * ClassLoader, boolean) newBundle} and {@link #needsReload(String,
         * Locale, String, ClassLoader, ResourceBundle, long) needsReload}
         * methods.
         *
         * <p>This implementation returns the following value:
         * <pre>
         *     baseName + "_" + language + "_" + script + "_" + country + "_" + variant
         * </pre>
         * where <code>language</code>, <code>script</code>, <code>country</code>,
         * and <code>variant</code> are the language, script, country, and variant
         * values of <code>locale</code>, respectively. Final component values that
         * are empty Strings are omitted along with the preceding '_'.  When the
         * script is empty, the script value is omitted along with the preceding '_'.
         * If all of the values are empty strings, then <code>baseName</code>
         * is returned.
         *
         * <p>For example, if <code>baseName</code> is
         * <code>"baseName"</code> and <code>locale</code> is
         * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>, then
         * <code>"baseName_ja_&thinsp;_XX"</code> is returned. If the given
         * locale is <code>Locale("en")</code>, then
         * <code>"baseName_en"</code> is returned.
         *
         * <p>Overriding this method allows applications to use different
         * conventions in the organization and packaging of localized
         * resources.
         *
         * @param baseName
         *        the base name of the resource bundle, a fully
         *        qualified class name
         * @param locale
         *        the locale for which a resource bundle should be
         *        loaded
         * @return the bundle name for the resource bundle
         * @exception NullPointerException
         *        if <code>baseName</code> or <code>locale</code>
         *        is <code>null</code>
         */
        public String toBundleName(String baseName, Locale locale) {
            if (locale == Locale.ROOT) {
                return baseName;
            }

            String language = locale.getLanguage();
            String script = locale.getScript();
            String country = locale.getCountry();
            String variant = locale.getVariant();

            if (language == "" && country == "" && variant == "") {
                return baseName;
            }

            StringBuilder sb = new StringBuilder(baseName);
            sb.append('_');
            if (script != "") {
                if (variant != "") {
                    sb.append(language).append('_').append(script).append('_').append(country).append('_').append(variant);
                } else if (country != "") {
                    sb.append(language).append('_').append(script).append('_').append(country);
                } else {
                    sb.append(language).append('_').append(script);
                }
            } else {
                if (variant != "") {
                    sb.append(language).append('_').append(country).append('_').append(variant);
                } else if (country != "") {
                    sb.append(language).append('_').append(country);
                } else {
                    sb.append(language);
                }
            }
            return sb.toString();

        }

        /**
         * Converts the given {@code bundleName} to the form required
         * by the {@link ClassLoader#getResource ClassLoader.getResource}
         * method by replacing all occurrences of {@code '.'} in
         * {@code bundleName} with {@code '/'} and appending a
         * {@code '.'} and the given file {@code suffix}. For
         * example, if {@code bundleName} is
         * {@code "foo.bar.MyResources_ja_JP"} and {@code suffix}
         * is {@code "properties"}, then
         * {@code "foo/bar/MyResources_ja_JP.properties"} is returned.
         *
         * @param bundleName
         *        the bundle name
         * @param suffix
         *        the file type suffix
         * @return the converted resource name
         * @exception NullPointerException
         *         if {@code bundleName} or {@code suffix}
         *         is {@code null}
         */
        public final String toResourceName(String bundleName, String suffix) {
            StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
            sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
            return sb.toString();
        }

        private String toResourceName0(String bundleName, String suffix) {
            // application protocol check
            if (bundleName.contains("://")) {
                return null;
            } else {
                return toResourceName(bundleName, suffix);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void uncheckedThrow(Throwable t) throws T {
        if (t != null)
            throw (T)t;
        else
            throw new Error("Unknown Exception");
    }

    private static class SingleFormatControl extends Control {
        private static final Control PROPERTIES_ONLY
            = new SingleFormatControl(FORMAT_PROPERTIES);

        private static final Control CLASS_ONLY
            = new SingleFormatControl(FORMAT_CLASS);

        private final List<String> formats;

        protected SingleFormatControl(List<String> formats) {
            this.formats = formats;
        }

        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return formats;
        }
    }

    private static final class NoFallbackControl extends SingleFormatControl {
        private static final Control NO_FALLBACK
            = new NoFallbackControl(FORMAT_DEFAULT);

        private static final Control PROPERTIES_ONLY_NO_FALLBACK
            = new NoFallbackControl(FORMAT_PROPERTIES);

        private static final Control CLASS_ONLY_NO_FALLBACK
            = new NoFallbackControl(FORMAT_CLASS);

        protected NoFallbackControl(List<String> formats) {
            super(formats);
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return null;
        }
    }

    // Android-removed: modules are not supported.
    /*
    private static class ResourceBundleProviderHelper {
        /**
         * Returns a new ResourceBundle instance of the given bundleClass
         *
        static ResourceBundle newResourceBundle(Class<? extends ResourceBundle> bundleClass) {
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends ResourceBundle> ctor =
                    bundleClass.getConstructor();
                if (!Modifier.isPublic(ctor.getModifiers())) {
                    return null;
                }
                // java.base may not be able to read the bundleClass's module.
                PrivilegedAction<Void> pa = () -> { ctor.setAccessible(true); return null;};
                AccessController.doPrivileged(pa);
                try {
                    return ctor.newInstance((Object[]) null);
                } catch (InvocationTargetException e) {
                    uncheckedThrow(e);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new InternalError(e);
                }
            } catch (NoSuchMethodException e) {
                throw new InternalError(e);
            }
            return null;
        }

        /**
         * Loads a {@code ResourceBundle} of the given {@code bundleName} local to
         * the given {@code module}. If not found, search the bundle class
         * that is visible from the module's class loader.
         *
         * The caller module is used for access check only.
         *
        static ResourceBundle loadResourceBundle(Module callerModule,
                                                 Module module,
                                                 String baseName,
                                                 Locale locale)
        {
            String bundleName = Control.INSTANCE.toBundleName(baseName, locale);
            try {
                PrivilegedAction<Class<?>> pa = () -> Class.forName(module, bundleName);
                Class<?> c = AccessController.doPrivileged(pa, null, GET_CLASSLOADER_PERMISSION);
                trace("local in %s %s caller %s: %s%n", module, bundleName, callerModule, c);

                if (c == null) {
                    // if not found from the given module, locate resource bundle
                    // that is visible to the module's class loader
                    ClassLoader loader = getLoader(module);
                    if (loader != null) {
                        c = Class.forName(bundleName, false, loader);
                    } else {
                        c = BootLoader.loadClassOrNull(bundleName);
                    }
                    trace("loader for %s %s caller %s: %s%n", module, bundleName, callerModule, c);
                }

                if (c != null && ResourceBundle.class.isAssignableFrom(c)) {
                    @SuppressWarnings("unchecked")
                    Class<ResourceBundle> bundleClass = (Class<ResourceBundle>) c;
                    Module m = bundleClass.getModule();
                    if (!isAccessible(callerModule, m, bundleClass.getPackageName())) {
                        trace("   %s does not have access to %s/%s%n", callerModule,
                              m.getName(), bundleClass.getPackageName());
                        return null;
                    }

                    return newResourceBundle(bundleClass);
                }
            } catch (ClassNotFoundException e) {}
            return null;
        }

        /**
         * Tests if resources of the given package name from the given module are
         * open to the caller module.
         *
        static boolean isAccessible(Module callerModule, Module module, String pn) {
            if (!module.isNamed() || callerModule == module)
                return true;

            return module.isOpen(pn, callerModule);
        }

        /**
         * Loads properties of the given {@code bundleName} local in the given
         * {@code module}.  If the .properties is not found or not open
         * to the caller module to access, it will find the resource that
         * is visible to the module's class loader.
         *
         * The caller module is used for access check only.
         *
        static ResourceBundle loadPropertyResourceBundle(Module callerModule,
                                                         Module module,
                                                         String baseName,
                                                         Locale locale)
            throws IOException
        {
            String bundleName = Control.INSTANCE.toBundleName(baseName, locale);

            PrivilegedAction<InputStream> pa = () -> {
                try {
                    String resourceName = Control.INSTANCE
                        .toResourceName0(bundleName, "properties");
                    if (resourceName == null) {
                        return null;
                    }
                    trace("local in %s %s caller %s%n", module, resourceName, callerModule);

                    // if the package is in the given module but not opened
                    // locate it from the given module first.
                    String pn = toPackageName(bundleName);
                    trace("   %s/%s is accessible to %s : %s%n",
                            module.getName(), pn, callerModule,
                            isAccessible(callerModule, module, pn));
                    if (isAccessible(callerModule, module, pn)) {
                        InputStream in = module.getResourceAsStream(resourceName);
                        if (in != null) {
                            return in;
                        }
                    }

                    ClassLoader loader = module.getClassLoader();
                    trace("loader for %s %s caller %s%n", module, resourceName, callerModule);

                    try {
                        if (loader != null) {
                            return loader.getResourceAsStream(resourceName);
                        } else {
                            URL url = BootLoader.findResource(resourceName);
                            if (url != null) {
                                return url.openStream();
                            }
                        }
                    } catch (Exception e) {}
                    return null;

                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };

            try (InputStream stream = AccessController.doPrivileged(pa)) {
                if (stream != null) {
                    return new PropertyResourceBundle(stream);
                } else {
                    return null;
                }
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        private static String toPackageName(String bundleName) {
            int i = bundleName.lastIndexOf('.');
            return i != -1 ? bundleName.substring(0, i) : "";
        }
    }
    */

    /* J2ObjC removed
    private static final boolean TRACE_ON = Boolean.valueOf(
        GetPropertyAction.privilegedGetProperty("resource.bundle.debug", "false"));

    private static void trace(String format, Object... params) {
        if (TRACE_ON)
            System.out.format(format, params);
    }
    */
}
