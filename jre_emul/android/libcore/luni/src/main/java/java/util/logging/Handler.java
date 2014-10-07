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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * A {@code Handler} object accepts a logging request and exports the desired
 * messages to a target, for example, a file, the console, etc. It can be
 * disabled by setting its logging level to {@code Level.OFF}.
 */
public abstract class Handler {

    private static final Level DEFAULT_LEVEL = Level.ALL;

    // the error manager to report errors during logging
    private ErrorManager errorMan;

    // the character encoding used by this handler
    private String encoding;

    // the logging level
    private Level level;

    // the formatter used to export messages
    private Formatter formatter;

    // the filter used to filter undesired messages
    private Filter filter;

    // class name, used for property reading
    private String prefix;

    /**
     * Constructs a {@code Handler} object with a default error manager instance
     * {@code ErrorManager}, the default encoding, and the default logging
     * level {@code Level.ALL}. It has no filter and no formatter.
     */
    protected Handler() {
        this.errorMan = new ErrorManager();
        this.level = DEFAULT_LEVEL;
        this.encoding = null;
        this.filter = null;
        this.formatter = null;
        this.prefix = this.getClass().getName();
    }

    // get a instance from given class name, using Class.forName()
    private Object getDefaultInstance(String className) {
        Object result = null;
        if (className == null) {
            return result;
        }
        try {
            result = Class.forName(className).newInstance();
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    // get a instance from given class name, using context classloader
    private Object getCustomizeInstance(final String className) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        Class<?> c = loader.loadClass(className);
        return c.newInstance();
    }

    // print error message in some format
    void printInvalidPropMessage(String key, String value, Exception e) {
        String msg = "Invalid property value for " + prefix + ":" + key + "/" + value;
        errorMan.error(msg, e, ErrorManager.GENERIC_FAILURE);
    }

    /**
     * init the common properties, including filter, level, formatter, and
     * encoding
     */
    void initProperties(String defaultLevel, String defaultFilter,
            String defaultFormatter, String defaultEncoding) {
        LogManager manager = LogManager.getLogManager();

        // set filter
        final String filterName = manager.getProperty(prefix + ".filter");
        if (filterName != null) {
            try {
                filter = (Filter) getCustomizeInstance(filterName);
            } catch (Exception e1) {
                printInvalidPropMessage("filter", filterName, e1);
                filter = (Filter) getDefaultInstance(defaultFilter);
            }
        } else {
            filter = (Filter) getDefaultInstance(defaultFilter);
        }

        // set level
        String levelName = manager.getProperty(prefix + ".level");
        if (levelName != null) {
            try {
                level = Level.parse(levelName);
            } catch (Exception e) {
                printInvalidPropMessage("level", levelName, e);
                level = Level.parse(defaultLevel);
            }
        } else {
            level = Level.parse(defaultLevel);
        }

        // set formatter
        final String formatterName = manager.getProperty(prefix + ".formatter");
        if (formatterName != null) {
            try {
                formatter = (Formatter) getCustomizeInstance(formatterName);
            } catch (Exception e) {
                printInvalidPropMessage("formatter", formatterName, e);
                formatter = (Formatter) getDefaultInstance(defaultFormatter);
            }
        } else {
            formatter = (Formatter) getDefaultInstance(defaultFormatter);
        }

        // set encoding
        final String encodingName = manager.getProperty(prefix + ".encoding");
        try {
            internalSetEncoding(encodingName);
        } catch (UnsupportedEncodingException e) {
            printInvalidPropMessage("encoding", encodingName, e);
        }
    }

    /**
     * Closes this handler. A flush operation will be performed and all the
     * associated resources will be freed. Client applications should not use
     * this handler after closing it.
     */
    public abstract void close();

    /**
     * Flushes any buffered output.
     */
    public abstract void flush();

    /**
     * Accepts a logging request and sends it to the the target.
     *
     * @param record
     *            the log record to be logged; {@code null} records are ignored.
     */
    public abstract void publish(LogRecord record);

    /**
     * Gets the character encoding used by this handler, {@code null} for
     * default encoding.
     *
     * @return the character encoding used by this handler.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Gets the error manager used by this handler to report errors during
     * logging.
     *
     * @return the error manager used by this handler.
     */
    public ErrorManager getErrorManager() {
        LogManager.getLogManager().checkAccess();
        return this.errorMan;
    }

    /**
     * Gets the filter used by this handler.
     *
     * @return the filter used by this handler (possibly {@code null}).
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Gets the formatter used by this handler to format the logging messages.
     *
     * @return the formatter used by this handler (possibly {@code null}).
     */
    public Formatter getFormatter() {
        return this.formatter;
    }

    /**
     * Gets the logging level of this handler, records with levels lower than
     * this value will be dropped.
     *
     * @return the logging level of this handler.
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * Determines whether the supplied log record needs to be logged. The
     * logging levels will be checked as well as the filter.
     *
     * @param record
     *            the log record to be checked.
     * @return {@code true} if the supplied log record needs to be logged,
     *         otherwise {@code false}.
     */
    public boolean isLoggable(LogRecord record) {
        if (record == null) {
            throw new NullPointerException("record == null");
        }
        if (this.level.intValue() == Level.OFF.intValue()) {
            return false;
        } else if (record.getLevel().intValue() >= this.level.intValue()) {
            return this.filter == null || this.filter.isLoggable(record);
        }
        return false;
    }

    /**
     * Reports an error to the error manager associated with this handler,
     * {@code ErrorManager} is used for that purpose. No security checks are
     * done, therefore this is compatible with environments where the caller
     * is non-privileged.
     *
     * @param msg
     *            the error message, may be {@code null}.
     * @param ex
     *            the associated exception, may be {@code null}.
     * @param code
     *            an {@code ErrorManager} error code.
     */
    protected void reportError(String msg, Exception ex, int code) {
        this.errorMan.error(msg, ex, code);
    }

    /**
     * Sets the character encoding used by this handler. A {@code null} value
     * indicates the use of the default encoding. This internal method does
     * not check security.
     *
     * @param newEncoding
     *            the character encoding to set.
     * @throws UnsupportedEncodingException
     *             if the specified encoding is not supported by the runtime.
     */
    void internalSetEncoding(String newEncoding) throws UnsupportedEncodingException {
        // accepts "null" because it indicates using default encoding
        if (newEncoding == null) {
            this.encoding = null;
        } else {
            if (Charset.isSupported(newEncoding)) {
                this.encoding = newEncoding;
            } else {
                throw new UnsupportedEncodingException(newEncoding);
            }
        }
    }

    /**
     * Sets the character encoding used by this handler, {@code null} indicates
     * a default encoding.
     *
     * @throws UnsupportedEncodingException if {@code charsetName} is not supported.
     */
    public void setEncoding(String charsetName) throws UnsupportedEncodingException {
        LogManager.getLogManager().checkAccess();
        internalSetEncoding(charsetName);
    }

    /**
     * Sets the error manager for this handler.
     *
     * @param newErrorManager
     *            the error manager to set.
     * @throws NullPointerException
     *             if {@code em} is {@code null}.
     */
    public void setErrorManager(ErrorManager newErrorManager) {
        LogManager.getLogManager().checkAccess();
        if (newErrorManager == null) {
            throw new NullPointerException("newErrorManager == null");
        }
        this.errorMan = newErrorManager;
    }

    /**
     * Sets the filter to be used by this handler.
     *
     * @param newFilter
     *            the filter to set, may be {@code null}.
     */
    public void setFilter(Filter newFilter) {
        LogManager.getLogManager().checkAccess();
        this.filter = newFilter;
    }

    /**
     * Sets the formatter to be used by this handler. This internal method does
     * not check security.
     *
     * @param newFormatter
     *            the formatter to set.
     */
    void internalSetFormatter(Formatter newFormatter) {
        if (newFormatter == null) {
            throw new NullPointerException("newFormatter == null");
        }
        this.formatter = newFormatter;
    }

    /**
     * Sets the formatter to be used by this handler.
     *
     * @param newFormatter
     *            the formatter to set.
     * @throws NullPointerException
     *             if {@code newFormatter} is {@code null}.
     */
    public void setFormatter(Formatter newFormatter) {
        LogManager.getLogManager().checkAccess();
        internalSetFormatter(newFormatter);
    }

    /**
     * Sets the logging level of the messages logged by this handler, levels
     * lower than this value will be dropped.
     *
     * @param newLevel
     *            the logging level to set.
     * @throws NullPointerException
     *             if {@code newLevel} is {@code null}.
     */
    public void setLevel(Level newLevel) {
        if (newLevel == null) {
            throw new NullPointerException("newLevel == null");
        }
        LogManager.getLogManager().checkAccess();
        this.level = newLevel;
    }
}
