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

/**
 * A handler that writes log messages to the standard output stream
 * {@code System.err}.
 * <p>
 * This handler reads the following properties from the log manager to
 * initialize itself:
 * <ul>
 * <li>java.util.logging.ConsoleHandler.level specifies the logging level,
 * defaults to {@code Level.INFO} if this property is not found or has an
 * invalid value.
 * <li>java.util.logging.ConsoleHandler.filter specifies the name of the filter
 * class to be associated with this handler, defaults to {@code null} if this
 * property is not found or has an invalid value.
 * <li>java.util.logging.ConsoleHandler.formatter specifies the name of the
 * formatter class to be associated with this handler, defaults to
 * {@code java.util.logging.SimpleFormatter} if this property is not found or
 * has an invalid value.
 * <li>java.util.logging.ConsoleHandler.encoding specifies the encoding this
 * handler will use to encode log messages, defaults to {@code null} if this
 * property is not found or has an invalid value.
 * </ul>
 * <p>
 * This class is not thread-safe.
 */
public class ConsoleHandler extends StreamHandler {

    /**
     * Constructs a {@code ConsoleHandler} object.
     */
    public ConsoleHandler() {
        super(System.err);
    }

    /**
     * Closes this handler. The {@code System.err} is flushed but not closed.
     */
    @Override
    public void close() {
        super.close(false);
    }

    /**
     * Logs a record if necessary. A flush operation will be done.
     *
     * @param record
     *            the log record to be logged.
     */
    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        super.flush();
    }
}
