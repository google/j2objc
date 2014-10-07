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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * A {@code StreamHandler} object writes log messages to an output stream, that
 * is, objects of the class {@link java.io.OutputStream}.
 * <p>
 * A {@code StreamHandler} object reads the following properties from the log
 * manager to initialize itself. A default value will be used if a property is
 * not found or has an invalid value.
 * <ul>
 * <li>java.util.logging.StreamHandler.encoding specifies the encoding this
 * handler will use to encode log messages. Default is the encoding used by the
 * current platform.
 * <li>java.util.logging.StreamHandler.filter specifies the name of the filter
 * class to be associated with this handler. No <code>Filter</code> is used by
 * default.
 * <li>java.util.logging.StreamHandler.formatter specifies the name of the
 * formatter class to be associated with this handler. Default is
 * {@code java.util.logging.SimpleFormatter}.
 * <li>java.util.logging.StreamHandler.level specifies the logging level.
 * Defaults is {@code Level.INFO}.
 * </ul>
 * <p>
 * This class is not thread-safe.
 */
public class StreamHandler extends Handler {

    // the output stream this handler writes to
    private OutputStream os;

    // the writer that writes to the output stream
    private Writer writer;

    // the flag indicating whether the writer has been initialized
    private boolean writerNotInitialized;

    /**
     * Constructs a {@code StreamHandler} object. The new stream handler
     * does not have an associated output stream.
     */
    public StreamHandler() {
        initProperties("INFO", null, "java.util.logging.SimpleFormatter", null);
        this.os = null;
        this.writer = null;
        this.writerNotInitialized = true;
    }

    /**
     * Constructs a {@code StreamHandler} object with the supplied output
     * stream. Default properties are read.
     *
     * @param os
     *            the output stream this handler writes to.
     */
    StreamHandler(OutputStream os) {
        this();
        this.os = os;
    }

    /**
     * Constructs a {@code StreamHandler} object. The specified default values
     * will be used if the corresponding properties are not found in the log
     * manager's properties.
     */
    StreamHandler(String defaultLevel, String defaultFilter,
            String defaultFormatter, String defaultEncoding) {
        initProperties(defaultLevel, defaultFilter, defaultFormatter,
                defaultEncoding);
        this.os = null;
        this.writer = null;
        this.writerNotInitialized = true;
    }

    /**
     * Constructs a {@code StreamHandler} object with the supplied output stream
     * and formatter.
     *
     * @param os
     *            the output stream this handler writes to.
     * @param formatter
     *            the formatter this handler uses to format the output.
     * @throws NullPointerException
     *             if {@code os} or {@code formatter} is {@code null}.
     */
    public StreamHandler(OutputStream os, Formatter formatter) {
        this();
        if (os == null) {
            throw new NullPointerException("os == null");
        }
        if (formatter == null) {
            throw new NullPointerException("formatter == null");
        }
        this.os = os;
        internalSetFormatter(formatter);
    }

    // initialize the writer
    private void initializeWriter() {
        this.writerNotInitialized = false;
        if (getEncoding() == null) {
            this.writer = new OutputStreamWriter(this.os);
        } else {
            try {
                this.writer = new OutputStreamWriter(this.os, getEncoding());
            } catch (UnsupportedEncodingException e) {
                /*
                 * Should not happen because it's checked in
                 * super.initProperties().
                 */
            }
        }
        write(getFormatter().getHead(this));
    }

    // Write a string to the output stream.
    private void write(String s) {
        try {
            this.writer.write(s);
        } catch (Exception e) {
            getErrorManager().error("Exception occurred when writing to the output stream", e,
                    ErrorManager.WRITE_FAILURE);
        }
    }

    /**
     * Sets the output stream this handler writes to. Note it does nothing else.
     *
     * @param newOs
     *            the new output stream
     */
    void internalSetOutputStream(OutputStream newOs) {
        this.os = newOs;
    }

    /**
     * Sets the output stream this handler writes to. If there's an existing
     * output stream, the tail string of the associated formatter will be
     * written to it. Then it will be flushed, closed and replaced with
     * {@code os}.
     *
     * @param os
     *            the new output stream.
     * @throws NullPointerException
     *             if {@code os} is {@code null}.
     */
    protected void setOutputStream(OutputStream os) {
        if (os == null) {
            throw new NullPointerException("os == null");
        }
        LogManager.getLogManager().checkAccess();
        close(true);
        this.writer = null;
        this.os = os;
        this.writerNotInitialized = true;
    }

    /**
     * Sets the character encoding used by this handler. A {@code null} value
     * indicates that the default encoding should be used.
     *
     * @throws UnsupportedEncodingException if {@code charsetName} is not supported.
     */
    @Override
    public void setEncoding(String charsetName) throws UnsupportedEncodingException {
        // Flush any existing data first.
        this.flush();
        super.setEncoding(charsetName);
        // renew writer only if the writer exists
        if (this.writer != null) {
            if (getEncoding() == null) {
                this.writer = new OutputStreamWriter(this.os);
            } else {
                try {
                    this.writer = new OutputStreamWriter(this.os, getEncoding());
                } catch (UnsupportedEncodingException e) {
                    /*
                     * Should not happen because it's checked in
                     * super.initProperties().
                     */
                    throw new AssertionError(e);
                }
            }
        }
    }

    /**
     * Closes this handler, but the underlying output stream is only closed if
     * {@code closeStream} is {@code true}. Security is not checked.
     *
     * @param closeStream
     *            whether to close the underlying output stream.
     */
    void close(boolean closeStream) {
        if (this.os != null) {
            if (this.writerNotInitialized) {
                initializeWriter();
            }
            write(getFormatter().getTail(this));
            try {
                this.writer.flush();
                if (closeStream) {
                    this.writer.close();
                    this.writer = null;
                    this.os = null;
                }
            } catch (Exception e) {
                getErrorManager().error("Exception occurred when closing the output stream", e,
                        ErrorManager.CLOSE_FAILURE);
            }
        }
    }

    /**
     * Closes this handler. The tail string of the formatter associated with
     * this handler is written out. A flush operation and a subsequent close
     * operation is then performed upon the output stream. Client applications
     * should not use a handler after closing it.
     */
    @Override
    public void close() {
        LogManager.getLogManager().checkAccess();
        close(true);
    }

    /**
     * Flushes any buffered output.
     */
    @Override
    public void flush() {
        if (this.os != null) {
            try {
                if (this.writer != null) {
                    this.writer.flush();
                } else {
                    this.os.flush();
                }
            } catch (Exception e) {
                getErrorManager().error("Exception occurred when flushing the output stream",
                        e, ErrorManager.FLUSH_FAILURE);
            }
        }
    }

    /**
     * Accepts a logging request. The log record is formatted and written to the
     * output stream if the following three conditions are met:
     * <ul>
     * <li>the supplied log record has at least the required logging level;
     * <li>the supplied log record passes the filter associated with this
     * handler, if any;
     * <li>the output stream associated with this handler is not {@code null}.
     * </ul>
     * If it is the first time a log record is written out, the head string of
     * the formatter associated with this handler is written out first.
     *
     * @param record
     *            the log record to be logged.
     */
    @Override
    public synchronized void publish(LogRecord record) {
        try {
            if (this.isLoggable(record)) {
                if (this.writerNotInitialized) {
                    initializeWriter();
                }
                String msg = null;
                try {
                    msg = getFormatter().format(record);
                } catch (Exception e) {
                    getErrorManager().error("Exception occurred when formatting the log record",
                            e, ErrorManager.FORMAT_FAILURE);
                }
                write(msg);
            }
        } catch (Exception e) {
            getErrorManager().error("Exception occurred when logging the record", e,
                    ErrorManager.GENERIC_FAILURE);
        }
    }

    /**
     * Determines whether the supplied log record needs to be logged. The
     * logging levels are checked as well as the filter. The output stream of
     * this handler is also checked. If it is {@code null}, this method returns
     * {@code false}.
     * <p>
     * Notice : Case of no output stream will return {@code false}.
     *
     * @param record
     *            the log record to be checked.
     * @return {@code true} if {@code record} needs to be logged, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null) {
            return false;
        }
        if (this.os != null && super.isLoggable(record)) {
            return true;
        }
        return false;
    }
}
