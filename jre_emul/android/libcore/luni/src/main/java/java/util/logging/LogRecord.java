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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A {@code LogRecord} object represents a logging request. It is passed between
 * the logging framework and individual logging handlers. Client applications
 * should not modify a {@code LogRecord} object that has been passed into the
 * logging framework.
 * <p>
 * The {@code LogRecord} class will infer the source method name and source
 * class name the first time they are accessed if the client application didn't
 * specify them explicitly. This automatic inference is based on the analysis of
 * the call stack and is not guaranteed to be precise. Client applications
 * should force the initialization of these two fields by calling
 * {@code getSourceClassName} or {@code getSourceMethodName} if they expect to
 * use them after passing the {@code LogRecord} object to another thread or
 * transmitting it over RMI.
 */
public class LogRecord implements Serializable {

    private static final long serialVersionUID = 5372048053134512534L;

    // The major byte used in serialization.
    private static final int MAJOR = 1;

    // The minor byte used in serialization.
    private static final int MINOR = 4;

    // Store the current value for the sequence number.
    private static long currentSequenceNumber = 0;

    // Store the id for each thread.
    private static ThreadLocal<Integer> currentThreadId = new ThreadLocal<Integer>();

    // The base id as the starting point for thread ID allocation.
    private static int initThreadId = 0;

    /**
     * The logging level.
     *
     * @serial
     */
    private Level level;

    /**
     * The sequence number.
     *
     * @serial
     */
    private long sequenceNumber;

    /**
     * The name of the class that issued the logging call.
     *
     * @serial
     */
    private String sourceClassName;

    /**
     * The name of the method that issued the logging call.
     *
     * @serial
     */
    private String sourceMethodName;

    /**
     * The original message text.
     *
     * @serial
     */
    private String message;

    /**
     * The ID of the thread that issued the logging call.
     *
     * @serial
     */
    private int threadID;

    /**
     * The time that the event occurred, in milliseconds since 1970.
     *
     * @serial
     */
    private long millis;

    /**
     * The associated {@code Throwable} object if any.
     *
     * @serial
     */
    private Throwable thrown;

    /**
     * The name of the source logger.
     *
     * @serial
     */
    private String loggerName;

    /**
     * The name of the resource bundle used to localize the log message.
     *
     * @serial
     */
    private String resourceBundleName;

    // The associated resource bundle if any.
    private transient ResourceBundle resourceBundle;

    // The parameters.
    private transient Object[] parameters;

    // If the source method and source class has been initialized
    private transient boolean sourceInitialized;

    /**
     * Constructs a {@code LogRecord} object using the supplied the logging
     * level and message. The millis property is set to the current time. The
     * sequence property is set to a new unique value, allocated in increasing
     * order within the VM. The thread ID is set to a unique value
     * for the current thread. All other properties are set to {@code null}.
     *
     * @param level
     *            the logging level, may not be {@code null}.
     * @param msg
     *            the raw message.
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public LogRecord(Level level, String msg) {
        if (level == null) {
            throw new NullPointerException("level == null");
        }
        this.level = level;
        this.message = msg;
        this.millis = System.currentTimeMillis();

        synchronized (LogRecord.class) {
            this.sequenceNumber = currentSequenceNumber++;
            Integer id = currentThreadId.get();
            if (id == null) {
                this.threadID = initThreadId;
                currentThreadId.set(Integer.valueOf(initThreadId++));
            } else {
                this.threadID = id.intValue();
            }
        }

        this.sourceClassName = null;
        this.sourceMethodName = null;
        this.loggerName = null;
        this.parameters = null;
        this.resourceBundle = null;
        this.resourceBundleName = null;
        this.thrown = null;
    }

    /**
     * Gets the logging level.
     *
     * @return the logging level.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Sets the logging level.
     *
     * @param level
     *            the level to set.
     * @throws NullPointerException
     *             if {@code level} is {@code null}.
     */
    public void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException("level == null");
        }
        this.level = level;
    }

    /**
     * Gets the name of the logger.
     *
     * @return the logger name.
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * Sets the name of the logger.
     *
     * @param loggerName
     *            the logger name to set.
     */
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * Gets the raw message.
     *
     * @return the raw message, may be {@code null}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the raw message. When this record is formatted by a logger that has
     * a localization resource bundle that contains an entry for {@code message},
     * then the raw message is replaced with its localized version.
     *
     * @param message
     *            the raw message to set, may be {@code null}.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the time when this event occurred, in milliseconds since 1970.
     *
     * @return the time when this event occurred, in milliseconds since 1970.
     */
    public long getMillis() {
        return millis;
    }

    /**
     * Sets the time when this event occurred, in milliseconds since 1970.
     *
     * @param millis
     *            the time when this event occurred, in milliseconds since 1970.
     */
    public void setMillis(long millis) {
        this.millis = millis;
    }

    /**
     * Gets the parameters.
     *
     * @return the array of parameters or {@code null} if there are no
     *         parameters.
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters.
     *
     * @param parameters
     *            the array of parameters to set, may be {@code null}.
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the resource bundle used to localize the raw message during
     * formatting.
     *
     * @return the associated resource bundle, {@code null} if none is
     *         available or the message is not localizable.
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Sets the resource bundle used to localize the raw message during
     * formatting.
     *
     * @param resourceBundle
     *            the resource bundle to set, may be {@code null}.
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Gets the name of the resource bundle.
     *
     * @return the name of the resource bundle, {@code null} if none is
     *         available or the message is not localizable.
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * Sets the name of the resource bundle.
     *
     * @param resourceBundleName
     *            the name of the resource bundle to set.
     */
    public void setResourceBundleName(String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
    }

    /**
     * Gets the sequence number.
     *
     * @return the sequence number.
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number. It is usually not necessary to call this method
     * to change the sequence number because the number is allocated when this
     * instance is constructed.
     *
     * @param sequenceNumber
     *            the sequence number to set.
     */
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the name of the class that is the source of this log record. This
     * information can be changed, may be {@code null} and is untrusted.
     *
     * @return the name of the source class of this log record (possiblity {@code null})
     */
    public String getSourceClassName() {
        initSource();
        return sourceClassName;
    }

    /*
     *  Init the sourceClass and sourceMethod fields.
     */
    private void initSource() {
        if (sourceInitialized) {
            return;
        }

        boolean sawLogger = false;
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            String current = element.getClassName();
            if (current.startsWith(Logger.class.getName())) {
                sawLogger = true;
            } else if (sawLogger) {
                this.sourceClassName = element.getClassName();
                this.sourceMethodName = element.getMethodName();
                break;
            }
        }

        sourceInitialized = true;
    }

    /**
     * Sets the name of the class that is the source of this log record.
     *
     * @param sourceClassName
     *            the name of the source class of this log record, may be
     *            {@code null}.
     */
    public void setSourceClassName(String sourceClassName) {
        sourceInitialized = true;
        this.sourceClassName = sourceClassName;
    }

    /**
     * Gets the name of the method that is the source of this log record.
     *
     * @return the name of the source method of this log record.
     */
    public String getSourceMethodName() {
        initSource();
        return sourceMethodName;
    }

    /**
     * Sets the name of the method that is the source of this log record.
     *
     * @param sourceMethodName
     *            the name of the source method of this log record, may be
     *            {@code null}.
     */
    public void setSourceMethodName(String sourceMethodName) {
        sourceInitialized = true;
        this.sourceMethodName = sourceMethodName;
    }

    /**
     * Gets a unique ID of the thread originating the log record. Every thread
     * becomes a different ID.
     * <p>
     * Notice : the ID doesn't necessary map the OS thread ID
     * </p>
     *
     * @return the ID of the thread originating this log record.
     */
    public int getThreadID() {
        return threadID;
    }

    /**
     * Sets the ID of the thread originating this log record.
     *
     * @param threadID
     *            the new ID of the thread originating this log record.
     */
    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    /**
     * Gets the {@code Throwable} object associated with this log record.
     *
     * @return the {@code Throwable} object associated with this log record.
     */
    public Throwable getThrown() {
        return thrown;
    }

    /**
     * Sets the {@code Throwable} object associated with this log record.
     *
     * @param thrown
     *            the new {@code Throwable} object to associate with this log
     *            record.
     */
    public void setThrown(Throwable thrown) {
        this.thrown = thrown;
    }

    /*
     * Customized serialization.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeByte(MAJOR);
        out.writeByte(MINOR);
        if (parameters == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(parameters.length);
            for (Object element : parameters) {
                out.writeObject((element == null) ? null : element.toString());
            }
        }
    }

    /*
     * Customized deserialization.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        byte major = in.readByte();
        byte minor = in.readByte();
        // only check MAJOR version
        if (major != MAJOR) {
            throw new IOException("Different version " + Byte.valueOf(major) + "." + Byte.valueOf(minor));
        }

        int length = in.readInt();
        if (length >= 0) {
            parameters = new Object[length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = in.readObject();
            }
        }
        if (resourceBundleName != null) {
            try {
                resourceBundle = Logger.loadResourceBundle(resourceBundleName);
            } catch (MissingResourceException e) {
                // Cannot find the specified resource bundle
                resourceBundle = null;
            }
        }
    }
}
