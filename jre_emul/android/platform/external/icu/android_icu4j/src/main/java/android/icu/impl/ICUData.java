/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2004-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 *
 * Created on Feb 4, 2004
 *
 */
package android.icu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.MissingResourceException;
import java.util.logging.Logger;

import android.icu.util.VersionInfo;

/**
 * Provides access to ICU data files as InputStreams.  Implements security checking.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class ICUData {
    /**
     * The data path to be used with getBundleInstance API
     */
    static final String ICU_DATA_PATH = "android/icu/impl/";
    /**
     * The ICU data package name.
     * This is normally the name of the .dat package, and the prefix (plus '/')
     * of the package entry names.
     */
    static final String PACKAGE_NAME = "icudt" + VersionInfo.ICU_DATA_VERSION_PATH;
    /**
     * The data path to be used with Class.getResourceAsStream().
     */
    public static final String ICU_BUNDLE = "data/" + PACKAGE_NAME;

    /**
     * The base name of ICU data to be used with ClassLoader.getResourceAsStream(),
     * ICUResourceBundle.getBundleInstance() etc.
     */
    public static final String ICU_BASE_NAME = ICU_DATA_PATH + ICU_BUNDLE;

    /**
     * The base name of collation data to be used with getBundleInstance API
     */
    public static final String ICU_COLLATION_BASE_NAME = ICU_BASE_NAME + "/coll";

    /**
     * The base name of rbbi data to be used with getData API
     */
    public static final String ICU_BRKITR_NAME = "brkitr";

    /**
     * The base name of rbbi data to be used with getBundleInstance API
     */
    public static final String ICU_BRKITR_BASE_NAME = ICU_BASE_NAME + '/' + ICU_BRKITR_NAME;

    /**
     * The base name of rbnf data to be used with getBundleInstance API
     */
    public static final String ICU_RBNF_BASE_NAME = ICU_BASE_NAME + "/rbnf";

    /**
     * The base name of transliterator data to be used with getBundleInstance API
     */
    public static final String ICU_TRANSLIT_BASE_NAME = ICU_BASE_NAME + "/translit";

    public static final String ICU_LANG_BASE_NAME = ICU_BASE_NAME + "/lang";
    public static final String ICU_CURR_BASE_NAME = ICU_BASE_NAME + "/curr";
    public static final String ICU_REGION_BASE_NAME = ICU_BASE_NAME + "/region";
    public static final String ICU_ZONE_BASE_NAME = ICU_BASE_NAME + "/zone";
    public static final String ICU_UNIT_BASE_NAME = ICU_BASE_NAME + "/unit";

    /**
     * For testing (otherwise false): When reading an InputStream from a Class or ClassLoader
     * (that is, not from a file), log when the stream contains ICU binary data.
     *
     * This cannot be ICUConfig'ured because ICUConfig calls ICUData.getStream()
     * to read the properties file, so we would get a circular dependency
     * in the class initialization.
     */
    private static final boolean logBinaryDataFromInputStream = false;
    private static final Logger logger = logBinaryDataFromInputStream ?
            Logger.getLogger(ICUData.class.getName()) : null;

    public static boolean exists(final String resourceName) {
        URL i = null;
        if (System.getSecurityManager() != null) {
            i = AccessController.doPrivileged(new PrivilegedAction<URL>() {
                    @Override
                    public URL run() {
                        return ICUData.class.getResource(resourceName);
                    }
                });
        } else {
            i = ICUData.class.getResource(resourceName);
        }
        return i != null;
    }

    private static InputStream getStream(final Class<?> root, final String resourceName, boolean required) {
        InputStream i = null;
        if (System.getSecurityManager() != null) {
            i = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                    @Override
                    public InputStream run() {
                        return root.getResourceAsStream(resourceName);
                    }
                });
        } else {
            i = root.getResourceAsStream(resourceName);
        }

        if (i == null && required) {
            throw new MissingResourceException("could not locate data " +resourceName, root.getPackage().getName(), resourceName);
        }
        checkStreamForBinaryData(i, resourceName);
        return i;
    }

    /**
     * Should be called only from ICUBinary.getData() or from convenience overloads here.
     */
    static InputStream getStream(final ClassLoader loader, final String resourceName, boolean required) {
        InputStream i = null;
        if (System.getSecurityManager() != null) {
            i = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                    @Override
                    public InputStream run() {
                        return loader.getResourceAsStream(resourceName);
                    }
                });
        } else {
            i = loader.getResourceAsStream(resourceName);
        }
        if (i == null && required) {
            throw new MissingResourceException("could not locate data", loader.toString(), resourceName);
        }
        checkStreamForBinaryData(i, resourceName);
        return i;
    }

    @SuppressWarnings("unused")  // used if logBinaryDataFromInputStream == true
    private static void checkStreamForBinaryData(InputStream is, String resourceName) {
        if (logBinaryDataFromInputStream && is != null && resourceName.indexOf(PACKAGE_NAME) >= 0) {
            try {
                is.mark(32);
                byte[] b = new byte[32];
                int len = is.read(b);
                if (len == 32 && b[2] == (byte)0xda && b[3] == 0x27) {
                    String msg = String.format(
                            "ICU binary data file loaded from Class/ClassLoader as InputStream " +
                            "from %s: MappedData %02x%02x%02x%02x  dataFormat %02x%02x%02x%02x",
                            resourceName,
                            b[0], b[1], b[2], b[3],
                            b[12], b[13], b[14], b[15]);
                    logger.info(msg);
                }
                is.reset();
            } catch (IOException ignored) {
            }
        }
    }

    public static InputStream getStream(ClassLoader loader, String resourceName){
        return getStream(loader,resourceName, false);
    }

    public static InputStream getRequiredStream(ClassLoader loader, String resourceName){
        return getStream(loader, resourceName, true);
    }

    /**
     * Convenience override that calls getStream(ICUData.class, resourceName, false);
     * Returns null if the resource could not be found.
     */
    public static InputStream getStream(String resourceName) {
        return getStream(ICUData.class, resourceName, false);
    }

    /**
     * Convenience method that calls getStream(ICUData.class, resourceName, true).
     * @throws MissingResourceException if the resource could not be found
     */
    public static InputStream getRequiredStream(String resourceName) {
        return getStream(ICUData.class, resourceName, true);
    }

    /**
     * Convenience override that calls getStream(root, resourceName, false);
     * Returns null if the resource could not be found.
     */
    public static InputStream getStream(Class<?> root, String resourceName) {
        return getStream(root, resourceName, false);
    }

    /**
     * Convenience method that calls getStream(root, resourceName, true).
     * @throws MissingResourceException if the resource could not be found
     */
    public static InputStream getRequiredStream(Class<?> root, String resourceName) {
        return getStream(root, resourceName, true);
    }
}
