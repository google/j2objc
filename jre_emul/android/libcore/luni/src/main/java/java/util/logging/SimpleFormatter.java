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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import libcore.io.IoUtils;

/**
 * {@code SimpleFormatter} can be used to print a summary of the information
 * contained in a {@code LogRecord} object in a human readable format.
 */
public class SimpleFormatter extends Formatter {
    /**
     * Constructs a new {@code SimpleFormatter}.
     */
    public SimpleFormatter() {
    }

    /**
     * Converts a {@link LogRecord} object into a human readable string
     * representation.
     *
     * @param r
     *            the log record to be formatted into a string.
     * @return the formatted string.
     */
    @Override
    public String format(LogRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format("{0, date} {0, time} ",
                new Object[] { new Date(r.getMillis()) }));
        sb.append(r.getSourceClassName()).append(" ");
        sb.append(r.getSourceMethodName()).append(System.lineSeparator());
        sb.append(r.getLevel().getName()).append(": ");
        sb.append(formatMessage(r)).append(System.lineSeparator());
        if (r.getThrown() != null) {
            sb.append("Throwable occurred: ");
            Throwable t = r.getThrown();
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                sb.append(sw.toString());
            } finally {
                IoUtils.closeQuietly(pw);
            }
        }
        return sb.toString();
    }
}
