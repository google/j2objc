/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.logging;

import java.beans.BeansFactory;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListResourceBundle;
import java.util.Properties;
import java.util.StringTokenizer;
import libcore.io.IoUtils;

/**
 * {@code LogManager} is used to maintain configuration properties of the
 * logging framework, and to manage a hierarchical namespace of all named
 * {@code Logger} objects.
 * <p>
 * There is only one global {@code LogManager} instance in the
 * application, which can be get by calling static method
 * {@link #getLogManager()}. This instance is created and
 * initialized during class initialization and cannot be changed.
 * <p>
 * The {@code LogManager} class can be specified by
 * java.util.logging.manager system property, if the property is unavailable or
 * invalid, the default class {@link java.util.logging.LogManager} will
 * be used.
 * <p>
 * On initialization, {@code LogManager} reads its configuration from a
 * properties file, which by default is the "lib/logging.properties" in the JRE
 * directory.
 * <p>
 * However, two optional system properties can be used to customize the initial
 * configuration process of {@code LogManager}.
 * <ul>
 * <li>"java.util.logging.config.class"</li>
 * <li>"java.util.logging.config.file"</li>
 * </ul>
 * <p>
 * These two properties can be set in three ways, by the Preferences API, by the
 * "java" command line property definitions, or by system property definitions
 * passed to JNI_CreateJavaVM.
 * <p>
 * The "java.util.logging.config.class" should specifies a class name. If it is
 * set, this given class will be loaded and instantiated during
 * {@code LogManager} initialization, so that this object's default
 * constructor can read the initial configuration and define properties for
 * {@code LogManager}.
 * <p>
 * If "java.util.logging.config.class" property is not set, or it is invalid, or
 * some exception is thrown during the instantiation, then the
 * "java.util.logging.config.file" system property can be used to specify a
 * properties file. The {@code LogManager} will read initial
 * configuration from this file.
 * <p>
 * If neither of these properties is defined, or some exception is thrown
 * during these two properties using, the {@code LogManager} will read
 * its initial configuration from default properties file, as described above.
 * <p>
 * The global logging properties may include:
 * <ul>
 * <li>"handlers". This property's values should be a list of class names for
 * handler classes separated by whitespace, these classes must be subclasses of
 * {@code Handler} and each must have a default constructor, these
 * classes will be loaded, instantiated and registered as handlers on the root
 * {@code Logger} (the {@code Logger} named ""). These
 * {@code Handler}s maybe initialized lazily.</li>
 * <li>"config". The property defines a list of class names separated by
 * whitespace. Each class must have a default constructor, in which it can
 * update the logging configuration, such as levels, handlers, or filters for
 * some logger, etc. These classes will be loaded and instantiated during
 * {@code LogManager} configuration</li>
 * </ul>
 * <p>
 * This class, together with any handler and configuration classes associated
 * with it, <b>must</b> be loaded from the system classpath when
 * {@code LogManager} configuration occurs.
 * <p>
 * Besides global properties, the properties for loggers and Handlers can be
 * specified in the property files. The names of these properties will start
 * with the complete dot separated names for the handlers or loggers.
 * <p>
 * In the {@code LogManager}'s hierarchical namespace,
 * {@code Loggers} are organized based on their dot separated names. For
 * example, "x.y.z" is child of "x.y".
 * <p>
 * Levels for {@code Loggers} can be defined by properties whose name end
 * with ".level". Thus "alogger.level" defines a level for the logger named as
 * "alogger" and for all its children in the naming hierarchy. Log levels
 * properties are read and applied in the same order as they are specified in
 * the property file. The root logger's level can be defined by the property
 * named as ".level".
 * <p>
 * This class is thread safe. It is an error to synchronize on a
 * {@code LogManager} while synchronized on a {@code Logger}.
 */
public class LogManager {

    /** The shared logging permission. */
    private static final LoggingPermission perm = new LoggingPermission("control", null);

    /** The singleton instance. */
    static LogManager manager;

    /**
     * The {@code String} value of the {@link LoggingMXBean}'s ObjectName.
     */
    public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";

    /**
     * Get the {@code LoggingMXBean} instance. this implementation always throws
     * an UnsupportedOperationException.
     *
     * @return the {@code LoggingMXBean} instance
     */
    public static LoggingMXBean getLoggingMXBean() {
        throw new UnsupportedOperationException();
    }

    // FIXME: use weak reference to avoid heap memory leak
    private Hashtable<String, Logger> loggers;

    /** The configuration properties */
    private Properties props;

    /** the property change listener */
    private PropertyChangeSupport listeners;

    static {     // init LogManager singleton instance
        String className = System.getProperty("java.util.logging.manager");
        if (className != null) {
            manager = (LogManager) getInstanceByClass(className);
        }
        if (manager == null) {
            manager = new LogManager();
        }
        checkConfiguration();

        // if global logger has been initialized, set root as its parent
        Logger root = new Logger("", null);
        root.setLevel(Level.INFO);
        Logger.global.setParent(root);

        manager.addLogger(root);
        manager.addLogger(Logger.global);
    }

    private static native void checkConfiguration() /*-[
      // To disable on iOS to improve startup performance, define
      // DISABLE_JAVA_LOG_CONFIGURATION to non-zero in project.
      #if !defined(DISABLE_JAVA_LOG_CONFIGURATION) || DISABLE_JAVA_LOG_CONFIGURATION == 0
        @try {
          [JavaUtilLoggingLogManager_manager readConfiguration];
        }
        @catch (JavaIoIOException *e) {
          [e printStackTrace];
        }
      #endif
    ]-*/;

    /**
     * Default constructor. This is not public because there should be only one
     * {@code LogManager} instance, which can be get by
     * {@code LogManager.getLogManager()}. This is protected so that
     * application can subclass the object.
     */
    protected LogManager() {
        loggers = new Hashtable<String, Logger>();
        props = new Properties();
        listeners = BeansFactory.newPropertyChangeSupportSafe(this);
        // add shutdown hook to ensure that the associated resource will be
        // freed when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                reset();
            }
        });
    }

    /**
     * Does nothing.
     */
    public void checkAccess() {
    }

    /**
     * Add a given logger into the hierarchical namespace. The
     * {@code Logger.addLogger()} factory methods call this method to add newly
     * created Logger. This returns false if a logger with the given name has
     * existed in the namespace
     * <p>
     * Note that the {@code LogManager} may only retain weak references to
     * registered loggers. In order to prevent {@code Logger} objects from being
     * unexpectedly garbage collected it is necessary for <i>applications</i>
     * to maintain references to them.
     * </p>
     *
     * @param logger
     *            the logger to be added.
     * @return true if the given logger is added into the namespace
     *         successfully, false if the given logger exists in the namespace.
     */
    public synchronized boolean addLogger(Logger logger) {
        String name = logger.getName();
        if (loggers.get(name) != null) {
            return false;
        }
        addToFamilyTree(logger, name);
        loggers.put(name, logger);
        logger.setManager(this);
        return true;
    }

    private void addToFamilyTree(Logger logger, String name) {
        Logger parent = null;
        // find parent
        int lastSeparator;
        String parentName = name;
        while ((lastSeparator = parentName.lastIndexOf('.')) != -1) {
            parentName = parentName.substring(0, lastSeparator);
            parent = loggers.get(parentName);
            if (parent != null) {
                setParent(logger, parent);
                break;
            } else if (getProperty(parentName + ".level") != null ||
                    getProperty(parentName + ".handlers") != null) {
                parent = Logger.getLogger(parentName);
                setParent(logger, parent);
                break;
            }
        }
        if (parent == null && (parent = loggers.get("")) != null) {
            setParent(logger, parent);
        }

        // find children
        // TODO: performance can be improved here?
        String nameDot = name + '.';
        Collection<Logger> allLoggers = loggers.values();
        for (final Logger child : allLoggers) {
            Logger oldParent = child.getParent();
            if (parent == oldParent && (name.length() == 0 || child.getName().startsWith(nameDot))) {
                final Logger thisLogger = logger;
                child.setParent(thisLogger);
                if (oldParent != null) {
                    // -- remove from old parent as the parent has been changed
                    oldParent.children.remove(child);
                }
            }
        }
    }

    /**
     * Get the logger with the given name.
     *
     * @param name
     *            name of logger
     * @return logger with given name, or {@code null} if nothing is found.
     */
    public synchronized Logger getLogger(String name) {
        return loggers.get(name);
    }

    /**
     * Get a {@code Enumeration} of all registered logger names.
     *
     * @return enumeration of registered logger names
     */
    public synchronized Enumeration<String> getLoggerNames() {
        return loggers.keys();
    }

    /**
     * Get the global {@code LogManager} instance.
     *
     * @return the global {@code LogManager} instance
     */
    public static LogManager getLogManager() {
        return manager;
    }

    /**
     * Get the value of property with given name.
     *
     * @param name
     *            the name of property
     * @return the value of property
     */
    public String getProperty(String name) {
        return props.getProperty(name);
    }

    /**
     * Re-initialize the properties and configuration. The initialization
     * process is same as the {@code LogManager} instantiation.
     * <p>
     * Notice : No {@code PropertyChangeEvent} are fired.
     * </p>
     *
     * @throws IOException
     *             if any IO related problems happened.
     */
    public void readConfiguration() throws IOException {
        // check config class
        String configClassName = System.getProperty("java.util.logging.config.class");
        if (configClassName == null || getInstanceByClass(configClassName) == null) {
            // if config class failed, check config file
            String configFile = System.getProperty("java.util.logging.config.file");

            if (configFile == null) {
                // if cannot find configFile, use default logging.properties
                configFile = System.getProperty("java.home") + File.separator + "lib" +
                        File.separator + "logging.properties";
            }

            InputStream input = null;
            try {
                if (new File(configFile).exists()) {
                    input = new FileInputStream(configFile);
                } else {
                    // fall back to using the built-in logging.properties file
                    input = LogManager.class.getResourceAsStream("logging.properties");
                    if (input == null) {
                        input = new ByteArrayInputStream(
                            IOSLogHandler.IOS_LOG_MANAGER_DEFAULTS.getBytes());
                    }
                }
                readConfiguration(new BufferedInputStream(input));
            } finally {
                IoUtils.closeQuietly(input);
            }
        }
    }

    // use SystemClassLoader to load class from system classpath
    static Object getInstanceByClass(final String className) {
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
            return clazz.newInstance();
        } catch (Exception e) {
            try {
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                return clazz.newInstance();
            } catch (Exception innerE) {
                System.err.println("Loading class '" + className + "' failed");
                System.err.println(innerE);
                return null;
            }
        }
    }

    // actual initialization process from a given input stream
    private synchronized void readConfigurationImpl(InputStream ins)
            throws IOException {
        reset();
        props.load(ins);

        // The RI treats the root logger as special. For compatibility, always
        // update the root logger's handlers.
        Logger root = loggers.get("");
        if (root != null) {
            root.setManager(this);
        }

        // parse property "config" and apply setting
        String configs = props.getProperty("config");
        if (configs != null) {
            StringTokenizer st = new StringTokenizer(configs, " ");
            while (st.hasMoreTokens()) {
                String configerName = st.nextToken();
                getInstanceByClass(configerName);
            }
        }

        // set levels for logger
        Collection<Logger> allLoggers = loggers.values();
        for (Logger logger : allLoggers) {
            String property = props.getProperty(logger.getName() + ".level");
            if (property != null) {
                logger.setLevel(Level.parse(property));
            }
        }
        if (listeners != null) {
          listeners.firePropertyChange(null, null, null);
        }
    }

    /**
     * Re-initialize the properties and configuration from the given
     * {@code InputStream}
     * <p>
     * Notice : No {@code PropertyChangeEvent} are fired.
     * </p>
     *
     * @param ins
     *            the input stream
     * @throws IOException
     *             if any IO related problems happened.
     */
    public void readConfiguration(InputStream ins) throws IOException {
        checkAccess();
        readConfigurationImpl(ins);
    }

    /**
     * Reset configuration.
     *
     * <p>All handlers are closed and removed from any named loggers. All loggers'
     * level is set to null, except the root logger's level is set to
     * {@code Level.INFO}.
     */
    public synchronized void reset() {
        checkAccess();
        props = new Properties();
        Enumeration<String> names = getLoggerNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Logger logger = getLogger(name);
            if (logger != null) {
                logger.reset();
            }
        }
        Logger root = loggers.get("");
        if (root != null) {
            root.setLevel(Level.INFO);
        }
    }

    /**
     * Add a {@code PropertyChangeListener}, which will be invoked when
     * the properties are reread.
     *
     * @param l
     *            the {@code PropertyChangeListener} to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (l == null) {
            throw new NullPointerException("l == null");
        }
        if (listeners == null) {
            BeansFactory.throwNotLoadedError();
        }
        checkAccess();
        listeners.addPropertyChangeListener(l);
    }

    /**
     * Remove a {@code PropertyChangeListener}, do nothing if the given
     * listener is not found.
     *
     * @param l
     *            the {@code PropertyChangeListener} to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null) {
            BeansFactory.throwNotLoadedError();
        }
        checkAccess();
        listeners.removePropertyChangeListener(l);
    }

    /**
     * Returns a named logger associated with the supplied resource bundle.
     *
     * @param resourceBundleName the resource bundle to associate, or null for
     *      no associated resource bundle.
     */
    synchronized Logger getOrCreate(String name, String resourceBundleName) {
        Logger result = getLogger(name);
        if (result == null) {

        result = new Logger(name, resourceBundleName);
            addLogger(result);
        }
        return result;
    }


    /**
     * Sets the parent of this logger in the namespace. Callers must first
     * {@link #checkAccess() check security}.
     *
     * @param newParent
     *            the parent logger to set.
     */
    synchronized void setParent(Logger logger, Logger newParent) {
        logger.parent = newParent;

        if (logger.levelObjVal == null) {
            setLevelRecursively(logger, null);
        }
        newParent.children.add(logger);
    }

    /**
     * Sets the level on {@code logger} to {@code newLevel}. Any child loggers
     * currently inheriting their level from {@code logger} will be updated
     * recursively.
     *
     * @param newLevel the new minimum logging threshold. If null, the logger's
     *      parent level will be used; or {@code Level.INFO} for loggers with no
     *      parent.
     */
    synchronized void setLevelRecursively(Logger logger, Level newLevel) {
        int previous = logger.levelIntVal;
        logger.levelObjVal = newLevel;

        if (newLevel == null) {
            logger.levelIntVal = logger.parent != null
                    ? logger.parent.levelIntVal
                    : Level.INFO.intValue();
        } else {
            logger.levelIntVal = newLevel.intValue();
        }

        if (previous != logger.levelIntVal) {
            for (Logger child : logger.children) {
                if (child.levelObjVal == null) {
                    setLevelRecursively(child, null);
                }
            }
        }
    }

}
