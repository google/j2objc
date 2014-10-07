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

import com.google.j2objc.annotations.Weak;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Loggers are used to log records to a variety of destinations such as log files or
 * the console. They use instances of {@link Handler} to actually do the destination-specific
 * operations.
 *
 * <p>Client applications can get named loggers by calling the {@code getLogger}
 * methods. They can also get anonymous loggers by calling the
 * {@code getAnonymousLogger} methods. Named loggers are organized in a
 * namespace hierarchy managed by a log manager. The naming convention is
 * usually the Java package naming convention. Anonymous loggers do not belong to any namespace.
 *
 * <p>Developers should use named loggers to enable logging to be controlled on a
 * per-{@code Logger} granularity. The recommended idiom is to create and assign the logger to
 * a {@code static final} field. This ensures that there's always a strong reference to the logger,
 * preventing it from being garbage collected. In particular, {@link LogManager#addLogger(Logger)}
 * will <i>not</i> keep your logger live.
 *
 * <p>Loggers "inherit" log level setting from their parent if their own level is
 * set to {@code null}. This is also true for the resource bundle. The logger's
 * resource bundle is used to localize the log messages if no resource bundle
 * name is given when a log method is called. If {@code getUseParentHandlers()}
 * returns {@code true}, loggers also inherit their parent's handlers. In this
 * context, "inherit" only means that "behavior" is inherited. The internal
 * field values will not change, for example, {@code getLevel()} still returns
 * {@code null}.
 * <p>
 * When loading a given resource bundle, the logger first tries to use the
 * context {@code ClassLoader}. If that fails, it tries the system {@code ClassLoader}. And if
 * that still fails, it searches up the class stack and uses each class's
 * {@code ClassLoader} to try to locate the resource bundle.
 * <p>
 * Some log methods accept log requests that do not specify the source class and
 * source method. In these cases, the logging framework will automatically infer
 * the calling class and method, but this is not guaranteed to be accurate.
 * <p>
 * Once a {@code LogRecord} object has been passed into the logging framework,
 * it is owned by the logging framework and the client applications should not
 * use it any longer.
 * <p>
 * All methods of this class are thread-safe.
 *
 * @see LogManager
 */
public class Logger {

    /**
     * The name of the global logger. Before using this, see the discussion of how to use
     * {@code Logger} in the class documentation.
     * @since 1.6
     */
    public static final String GLOBAL_LOGGER_NAME = "global";

    /**
     * The global logger is provided as convenience for casual use.
     * @deprecated This is deadlock-prone. Use {@code Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)}
     * as a direct replacement, but read the discussion of how to use {@link Logger} in the class
     * documentation.
     */
    @Deprecated
    public static final Logger global = new Logger(GLOBAL_LOGGER_NAME, null);

    /**
     * When converting the concurrent collection of handlers to an array, we
     * always pass a zero-length array to avoid size miscalculations. Passing
     * properly-sized arrays is non-atomic, and risks a null element in the
     * result.
     */
    private static final Handler[] EMPTY_HANDLERS_ARRAY = new Handler[0];

    /** The name of this logger. */
    private volatile String name;

    /** The parent logger of this logger. */
    @Weak
    Logger parent;

    /** The logging level of this logger, or null if none is set. */
    volatile Level levelObjVal;

    /**
     * The effective logging level of this logger. In order of preference this
     * is the first applicable of:
     * <ol>
     * <li>the int value of this logger's {@link #levelObjVal}
     * <li>the logging level of the parent
     * <li>the default level ({@link Level#INFO})
     * </ol>
     */
    volatile int levelIntVal = Level.INFO.intValue();

    /** The filter. */
    private Filter filter;

    /**
     * The resource bundle used to localize logging messages. If null, no
     * localization will be performed.
     */
    private volatile String resourceBundleName;

    /** The loaded resource bundle according to the specified name. */
    private volatile ResourceBundle resourceBundle;

    /**
     * The handlers attached to this logger. Eagerly initialized and
     * concurrently modified.
     */
    private final List<Handler> handlers = new CopyOnWriteArrayList<Handler>();

    /** True to notify the parent's handlers of each log message. */
    private boolean notifyParentHandlers = true;

    /**
     * Indicates whether this logger is named. Only {@link #getAnonymousLogger
     * anonymous loggers} are unnamed.
     */
    private boolean isNamed = true;

    /**
     * Child loggers. Should be accessed only while synchronized on {@code
     * LogManager.getLogManager()}.
     */
    final List<Logger> children = new ArrayList<Logger>();

    /**
     * Constructs a {@code Logger} object with the supplied name and resource
     * bundle name; {@code notifiyParentHandlers} is set to {@code true}.
     * <p>
     * Notice : Loggers use a naming hierarchy. Thus "z.x.y" is a child of "z.x".
     *
     * @param name
     *            the name of this logger, may be {@code null} for anonymous
     *            loggers.
     * @param resourceBundleName
     *            the name of the resource bundle used to localize logging
     *            messages, may be {@code null}.
     * @throws MissingResourceException
     *             if the specified resource bundle can not be loaded.
     */
    protected Logger(String name, String resourceBundleName) {
        this.name = name;
        initResourceBundle(resourceBundleName);
    }

    /**
     * Load the specified resource bundle, use privileged code.
     *
     * @param resourceBundleName
     *            the name of the resource bundle to load, cannot be {@code null}.
     * @return the loaded resource bundle.
     * @throws MissingResourceException
     *             if the specified resource bundle can not be loaded.
     */
    static ResourceBundle loadResourceBundle(String resourceBundleName) {
        // try context class loader to load the resource
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
            } catch (MissingResourceException ignored) {
                // Failed to load using context class loader, ignore
            }
        }
        // try system class loader to load the resource
        cl = ClassLoader.getSystemClassLoader();
        if (cl != null) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
            } catch (MissingResourceException ignored) {
                // Failed to load using system class loader, ignore
            }
        }
        throw new MissingResourceException("Failed to load the specified resource bundle \"" +
                resourceBundleName + "\"", resourceBundleName, null);
    }

    /**
     * Gets an anonymous logger to use internally in a thread. Anonymous loggers
     * are not registered in the log manager's namespace. No security checks
     * will be performed when updating an anonymous logger's control settings.
     * <p>
     * The anonymous loggers' parent is set to be the root logger. This way it
     * inherits the default logging level and handlers from the root logger.
     *
     * @return a new instance of anonymous logger.
     */
    public static Logger getAnonymousLogger() {
        return getAnonymousLogger(null);
    }

    /**
     * Gets an anonymous logger to use internally in a thread. Anonymous loggers
     * are not registered in the log manager's namespace. No security checks
     * will be performed when updating an anonymous logger's control settings.
     * <p>
     * The anonymous loggers' parent is set to be the root logger. This way it
     * inherits default logging level and handlers from the root logger.
     *
     * @param resourceBundleName
     *            the name of the resource bundle used to localize log messages.
     * @return a new instance of anonymous logger.
     * @throws MissingResourceException
     *             if the specified resource bundle can not be loaded.
     */
    public static Logger getAnonymousLogger(String resourceBundleName) {
        Logger result = new Logger(null, resourceBundleName);
        result.isNamed = false;
        LogManager logManager = LogManager.getLogManager();
        logManager.setParent(result, logManager.getLogger(""));
        return result;
    }

    /**
     * Initializes this logger's resource bundle.
     *
     * @throws IllegalArgumentException if this logger's resource bundle already
     *      exists and is different from the resource bundle specified.
     */
    private synchronized void initResourceBundle(String resourceBundleName) {
        String current = this.resourceBundleName;

        if (current != null) {
            if (current.equals(resourceBundleName)) {
                return;
            } else {
                throw new IllegalArgumentException("Resource bundle name '" + resourceBundleName + "' is inconsistent with the existing '" + current + "'");
            }
        }

        if (resourceBundleName != null) {
            this.resourceBundle = loadResourceBundle(resourceBundleName);
            this.resourceBundleName = resourceBundleName;
        }
    }

    /**
     * Gets a named logger. The returned logger may already exist or may be
     * newly created. In the latter case, its level will be set to the
     * configured level according to the {@code LogManager}'s properties.
     *
     * @param name
     *            the name of the logger to get, cannot be {@code null}.
     * @return a named logger.
     * @throws MissingResourceException
     *             If the specified resource bundle can not be loaded.
     */
    public static Logger getLogger(String name) {
        return LogManager.getLogManager().getOrCreate(name, null);
    }

    /**
     * Gets a named logger associated with the supplied resource bundle. The
     * resource bundle will be used to localize logging messages.
     *
     * @param name
     *            the name of the logger to get, cannot be {@code null}.
     * @param resourceBundleName
     *            the name of the resource bundle, may be {@code null}.
     * @throws IllegalArgumentException
     *             if the logger identified by {@code name} is associated with a
     *             resource bundle and its name is not equal to
     *             {@code resourceBundleName}.
     * @throws MissingResourceException
     *             if the name of the resource bundle cannot be found.
     * @return a named logger.
     */
    public static Logger getLogger(String name, String resourceBundleName) {
        Logger result = LogManager.getLogManager()
                .getOrCreate(name, resourceBundleName);
        result.initResourceBundle(resourceBundleName);
        return result;
    }

    /**
     * Returns the global {@code Logger}.
     * @since 1.7
     */
    public static Logger getGlobal() {
        return global;
    }

    /**
     * Adds a handler to this logger. The {@code name} will be fed with log
     * records received by this logger.
     *
     * @param handler
     *            the handler object to add, cannot be {@code null}.
     */
    public void addHandler(Handler handler) {
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        // Anonymous loggers can always add handlers
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        this.handlers.add(handler);
    }

    /**
     * Set the logger's manager and initializes its configuration from the
     * manager's properties.
     */
    void setManager(LogManager manager) {
        String levelProperty = manager.getProperty(name + ".level");
        if (levelProperty != null) {
            try {
                manager.setLevelRecursively(Logger.this, Level.parse(levelProperty));
            } catch (IllegalArgumentException invalidLevel) {
                invalidLevel.printStackTrace();
            }
        }

        String handlersPropertyName = name.isEmpty() ? "handlers" : name + ".handlers";
        String handlersProperty = manager.getProperty(handlersPropertyName);
        if (handlersProperty != null) {
            for (String handlerName : handlersProperty.split(",|\\s")) {
                if (handlerName.isEmpty()) {
                    continue;
                }

                final Handler handler;
                try {
                    handler = (Handler) LogManager.getInstanceByClass(handlerName);
                } catch (Exception invalidHandlerName) {
                    invalidHandlerName.printStackTrace();
                    continue;
                }

                try {
                    String level = manager.getProperty(handlerName + ".level");
                    if (level != null) {
                        handler.setLevel(Level.parse(level));
                    }
                } catch (Exception invalidLevel) {
                    invalidLevel.printStackTrace();
                }

                handlers.add(handler);
            }
        }
    }

    /**
     * Gets all the handlers associated with this logger.
     *
     * @return an array of all the handlers associated with this logger.
     */
    public Handler[] getHandlers() {
        return handlers.toArray(EMPTY_HANDLERS_ARRAY);
    }

    /**
     * Removes a handler from this logger. If the specified handler does not
     * exist then this method has no effect.
     *
     * @param handler
     *            the handler to be removed.
     */
    public void removeHandler(Handler handler) {
        // Anonymous loggers can always remove handlers
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        if (handler == null) {
            return;
        }
        this.handlers.remove(handler);
    }

    /**
     * Gets the filter used by this logger.
     *
     * @return the filter used by this logger, may be {@code null}.
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter used by this logger.
     *
     * @param newFilter
     *            the filter to set, may be {@code null}.
     */
    public void setFilter(Filter newFilter) {
        // Anonymous loggers can always set the filter
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        filter = newFilter;
    }

    /**
     * Gets the logging level of this logger. A {@code null} level indicates
     * that this logger inherits its parent's level.
     *
     * @return the logging level of this logger.
     */
    public Level getLevel() {
        return levelObjVal;
    }

    /**
     * Sets the logging level for this logger. A {@code null} level indicates
     * that this logger will inherit its parent's level.
     *
     * @param newLevel
     *            the logging level to set.
     */
    public void setLevel(Level newLevel) {
        // Anonymous loggers can always set the level
        LogManager logManager = LogManager.getLogManager();
        if (this.isNamed) {
            logManager.checkAccess();
        }
        logManager.setLevelRecursively(this, newLevel);
    }

    /**
     * Gets the flag which indicates whether to use the handlers of this
     * logger's parent to publish incoming log records, potentially recursively
     * up the namespace.
     *
     * @return {@code true} if set to use parent's handlers, {@code false}
     *         otherwise.
     */
    public boolean getUseParentHandlers() {
        return this.notifyParentHandlers;
    }

    /**
     * Sets the flag which indicates whether to use the handlers of this
     * logger's parent, potentially recursively up the namespace.
     *
     * @param notifyParentHandlers
     *            the new flag indicating whether to use the parent's handlers.
     */
    public void setUseParentHandlers(boolean notifyParentHandlers) {
        // Anonymous loggers can always set the useParentHandlers flag
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        this.notifyParentHandlers = notifyParentHandlers;
    }

    /**
     * Gets the nearest parent of this logger in the namespace, a {@code null}
     * value will be returned if called on the root logger.
     *
     * @return the parent of this logger in the namespace.
     */
    public Logger getParent() {
        return parent;
    }

    /**
     * Sets the parent of this logger in the namespace. This method should be
     * used by the {@code LogManager} object only.
     *
     * @param parent
     *            the parent logger to set.
     */
    public void setParent(Logger parent) {
        if (parent == null) {
            throw new NullPointerException("parent == null");
        }

        // even anonymous loggers are checked
        LogManager logManager = LogManager.getLogManager();
        logManager.checkAccess();
        logManager.setParent(this, parent);
    }

    /**
     * Gets the name of this logger, {@code null} for anonymous loggers.
     *
     * @return the name of this logger.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the loaded resource bundle used by this logger to localize logging
     * messages. If the value is {@code null}, the parent's resource bundle will be
     * inherited.
     *
     * @return the loaded resource bundle used by this logger.
     */
    public ResourceBundle getResourceBundle() {
        return this.resourceBundle;
    }

    /**
     * Gets the name of the loaded resource bundle used by this logger to
     * localize logging messages. If the value is {@code null}, the parent's resource
     * bundle name will be inherited.
     *
     * @return the name of the loaded resource bundle used by this logger.
     */
    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    /**
     * This method is for compatibility. Tests written to the reference
     * implementation API imply that the isLoggable() method is not called
     * directly. This behavior is important because subclass may override
     * isLoggable() method, so that affect the result of log methods.
     */
    private boolean internalIsLoggable(Level l) {
        int effectiveLevel = levelIntVal;
        if (effectiveLevel == Level.OFF.intValue()) {
            // always return false if the effective level is off
            return false;
        }
        return l.intValue() >= effectiveLevel;
    }

    /**
     * Determines whether this logger will actually log messages of the
     * specified level. The effective level used to do the determination may be
     * inherited from its parent. The default level is {@code Level.INFO}.
     *
     * @param l
     *            the level to check.
     * @return {@code true} if this logger will actually log this level,
     *         otherwise {@code false}.
     */
    public boolean isLoggable(Level l) {
        return internalIsLoggable(l);
    }

    /**
     * Sets the resource bundle and its name for a supplied LogRecord object.
     * This method first tries to use this logger's resource bundle if any,
     * otherwise try to inherit from this logger's parent, recursively up the
     * namespace.
     */
    private void setResourceBundle(LogRecord record) {
        for (Logger p = this; p != null; p = p.parent) {
            String resourceBundleName = p.resourceBundleName;
            if (resourceBundleName != null) {
                record.setResourceBundle(p.resourceBundle);
                record.setResourceBundleName(resourceBundleName);
                return;
            }
        }
    }

    /**
     * Logs a message indicating that a method has been entered. A log record
     * with log level {@code Level.FINER}, log message "ENTRY", the specified
     * source class name and source method name is submitted for logging.
     *
     * @param sourceClass
     *            the calling class name.
     * @param sourceMethod
     *            the method name.
     */
    public void entering(String sourceClass, String sourceMethod) {
        if (!internalIsLoggable(Level.FINER)) {
            return;
        }

        LogRecord record = new LogRecord(Level.FINER, "ENTRY");
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message indicating that a method has been entered. A log record
     * with log level {@code Level.FINER}, log message "ENTRY", the specified
     * source class name, source method name and one parameter is submitted for
     * logging.
     *
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param param
     *            the parameter for the method call.
     */
    public void entering(String sourceClass, String sourceMethod, Object param) {
        if (!internalIsLoggable(Level.FINER)) {
            return;
        }

        LogRecord record = new LogRecord(Level.FINER, "ENTRY" + " {0}");
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(new Object[] { param });
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message indicating that a method has been entered. A log record
     * with log level {@code Level.FINER}, log message "ENTRY", the specified
     * source class name, source method name and array of parameters is
     * submitted for logging.
     *
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param params
     *            an array of parameters for the method call.
     */
    public void entering(String sourceClass, String sourceMethod,
            Object[] params) {
        if (!internalIsLoggable(Level.FINER)) {
            return;
        }

        String msg = "ENTRY";
        if (params != null) {
            StringBuilder msgBuffer = new StringBuilder("ENTRY");
            for (int i = 0; i < params.length; i++) {
                msgBuffer.append(" {").append(i).append("}");
            }
            msg = msgBuffer.toString();
        }
        LogRecord record = new LogRecord(Level.FINER, msg);
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(params);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message indicating that a method is exited. A log record with log
     * level {@code Level.FINER}, log message "RETURN", the specified source
     * class name and source method name is submitted for logging.
     *
     * @param sourceClass
     *            the calling class name.
     * @param sourceMethod
     *            the method name.
     */
    public void exiting(String sourceClass, String sourceMethod) {
        if (!internalIsLoggable(Level.FINER)) {
            return;
        }

        LogRecord record = new LogRecord(Level.FINER, "RETURN");
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message indicating that a method is exited. A log record with log
     * level {@code Level.FINER}, log message "RETURN", the specified source
     * class name, source method name and return value is submitted for logging.
     *
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param result
     *            the return value of the method call.
     */
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        if (!internalIsLoggable(Level.FINER)) {
            return;
        }

        LogRecord record = new LogRecord(Level.FINER, "RETURN" + " {0}");
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(new Object[] { result });
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message indicating that an exception is thrown. A log record with
     * log level {@code Level.FINER}, log message "THROW", the specified source
     * class name, source method name and the {@code Throwable} object is
     * submitted for logging.
     *
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param thrown
     *            the {@code Throwable} object.
     */
    public void throwing(String sourceClass, String sourceMethod,
            Throwable thrown) {
        if (!internalIsLoggable(Level.FINER)) {
            return;
        }

        LogRecord record = new LogRecord(Level.FINER, "THROW");
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setThrown(thrown);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of level {@code Level.SEVERE}; the message is transmitted
     * to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    /**
     * Logs a message of level {@code Level.WARNING}; the message is
     * transmitted to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void warning(String msg) {
        log(Level.WARNING, msg);
    }

    /**
     * Logs a message of level {@code Level.INFO}; the message is transmitted
     * to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void info(String msg) {
        log(Level.INFO, msg);
    }

    /**
     * Logs a message of level {@code Level.CONFIG}; the message is transmitted
     * to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void config(String msg) {
        log(Level.CONFIG, msg);
    }

    /**
     * Logs a message of level {@code Level.FINE}; the message is transmitted
     * to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    /**
     * Logs a message of level {@code Level.FINER}; the message is transmitted
     * to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    /**
     * Logs a message of level {@code Level.FINEST}; the message is transmitted
     * to all subscribed handlers.
     *
     * @param msg
     *            the message to log.
     */
    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    /**
     * Logs a message of the specified level. The message is transmitted to all
     * subscribed handlers.
     *
     * @param logLevel
     *            the level of the specified message.
     * @param msg
     *            the message to log.
     */
    public void log(Level logLevel, String msg) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the specified level with the supplied parameter. The
     * message is then transmitted to all subscribed handlers.
     *
     * @param logLevel
     *            the level of the given message.
     * @param msg
     *            the message to log.
     * @param param
     *            the parameter associated with the event that is logged.
     */
    public void log(Level logLevel, String msg, Object param) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setParameters(new Object[] { param });
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the specified level with the supplied parameter array.
     * The message is then transmitted to all subscribed handlers.
     *
     * @param logLevel
     *            the level of the given message
     * @param msg
     *            the message to log.
     * @param params
     *            the parameter array associated with the event that is logged.
     */
    public void log(Level logLevel, String msg, Object[] params) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setParameters(params);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the specified level with the supplied {@code Throwable}
     * object. The message is then transmitted to all subscribed handlers.
     *
     * @param logLevel
     *            the level of the given message.
     * @param msg
     *            the message to log.
     * @param thrown
     *            the {@code Throwable} object associated with the event that is
     *            logged.
     */
    public void log(Level logLevel, String msg, Throwable thrown) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setThrown(thrown);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a given log record. Only records with a logging level that is equal
     * or greater than this logger's level will be submitted to this logger's
     * handlers for logging. If {@code getUseParentHandlers()} returns {@code
     * true}, the log record will also be submitted to the handlers of this
     * logger's parent, potentially recursively up the namespace.
     * <p>
     * Since all other log methods call this method to actually perform the
     * logging action, subclasses of this class can override this method to
     * catch all logging activities.
     * </p>
     *
     * @param record
     *            the log record to be logged.
     */
    public void log(LogRecord record) {
        if (!internalIsLoggable(record.getLevel())) {
            return;
        }

        // apply the filter if any
        Filter f = filter;
        if (f != null && !f.isLoggable(record)) {
            return;
        }

        /*
         * call the handlers of this logger, throw any exception that occurs
         */
        Handler[] allHandlers = getHandlers();
        for (Handler element : allHandlers) {
            element.publish(record);
        }
        // call the parent's handlers if set useParentHandlers
        Logger temp = this;
        Logger theParent = temp.parent;
        while (theParent != null && temp.getUseParentHandlers()) {
            Handler[] ha = theParent.getHandlers();
            for (Handler element : ha) {
                element.publish(record);
            }
            temp = theParent;
            theParent = temp.parent;
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name.
     *
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param msg
     *            the message to be logged.
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter.
     *
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param msg
     *            the message to be logged
     * @param param
     *            the parameter associated with the event that is logged.
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Object param) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(new Object[] { param });
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter array.
     *
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param msg
     *            the message to be logged.
     * @param params
     *            the parameter array associated with the event that is logged.
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Object[] params) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(params);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and {@code Throwable} object.
     *
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param msg
     *            the message to be logged.
     * @param thrown
     *            the {@code Throwable} object.
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Throwable thrown) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setThrown(thrown);
        setResourceBundle(record);
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name, using the given resource bundle to localize the
     * message. If {@code bundleName} is null, the empty string or not valid then
     * the message is not localized.
     *
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        if (bundleName != null) {
            try {
                record.setResourceBundle(loadResourceBundle(bundleName));
            } catch (MissingResourceException e) {
                // ignore
            }
            record.setResourceBundleName(bundleName);
        }
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter, using the given resource bundle to
     * localize the message. If {@code bundleName} is null, the empty string
     * or not valid then the message is not localized.
     *
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @param param
     *            the parameter associated with the event that is logged.
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Object param) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        if (bundleName != null) {
            try {
                record.setResourceBundle(loadResourceBundle(bundleName));
            } catch (MissingResourceException e) {
                // ignore
            }
            record.setResourceBundleName(bundleName);
        }
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(new Object[] { param });
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter array, using the given resource bundle
     * to localize the message. If {@code bundleName} is null, the empty string
     * or not valid then the message is not localized.
     *
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @param params
     *            the parameter array associated with the event that is logged.
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Object[] params) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        if (bundleName != null) {
            try {
                record.setResourceBundle(loadResourceBundle(bundleName));
            } catch (MissingResourceException e) {
                // ignore
            }
            record.setResourceBundleName(bundleName);
        }
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setParameters(params);
        log(record);
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and {@code Throwable} object, using the given resource
     * bundle to localize the message. If {@code bundleName} is null, the empty
     * string or not valid then the message is not localized.
     *
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @param thrown
     *            the {@code Throwable} object.
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Throwable thrown) {
        if (!internalIsLoggable(logLevel)) {
            return;
        }

        LogRecord record = new LogRecord(logLevel, msg);
        if (bundleName != null) {
            try {
                record.setResourceBundle(loadResourceBundle(bundleName));
            } catch (MissingResourceException e) {
                // ignore
            }
            record.setResourceBundleName(bundleName);
        }
        record.setLoggerName(this.name);
        record.setSourceClassName(sourceClass);
        record.setSourceMethodName(sourceMethod);
        record.setThrown(thrown);
        log(record);
    }

    void reset() {
        levelObjVal = null;
        levelIntVal = Level.INFO.intValue();

        for (Handler handler : handlers) {
            try {
                if (handlers.remove(handler)) {
                    handler.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
