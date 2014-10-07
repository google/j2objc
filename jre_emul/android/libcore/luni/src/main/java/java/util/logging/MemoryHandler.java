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
 * A {@code Handler} put the description of log events into a cycled memory
 * buffer.
 * <p>
 * Mostly this {@code MemoryHandler} just puts the given {@code LogRecord} into
 * the internal buffer and doesn't perform any formatting or any other process.
 * When the buffer is full, the earliest buffered records will be discarded.
 * <p>
 * Every {@code MemoryHandler} has a target handler, and push action can be
 * triggered so that all buffered records will be output to the target handler
 * and normally the latter will publish the records. After the push action, the
 * buffer will be cleared.
 * <p>
 * The push method can be called directly, but will also be called automatically
 * if a new <code>LogRecord</code> is added that has a level greater than or
 * equal to than the value defined for the property
 * java.util.logging.MemoryHandler.push.
 * <p>
 * {@code MemoryHandler} will read following {@code LogManager} properties for
 * initialization, if given properties are not defined or has invalid values,
 * default value will be used.
 * <ul>
 * <li>java.util.logging.MemoryHandler.filter specifies the {@code Filter}
 * class name, defaults to no {@code Filter}.</li>
 * <li>java.util.logging.MemoryHandler.level specifies the level for this
 * {@code Handler}, defaults to {@code Level.ALL}.</li>
 * <li>java.util.logging.MemoryHandler.push specifies the push level, defaults
 * to level.SEVERE.</li>
 * <li>java.util.logging.MemoryHandler.size specifies the buffer size in number
 * of {@code LogRecord}, defaults to 1000.</li>
 * <li>java.util.logging.MemoryHandler.target specifies the class of the target
 * {@code Handler}, no default value, which means this property must be
 * specified either by property setting or by constructor.</li>
 * </ul>
 */
public class MemoryHandler extends Handler {

    // default maximum buffered number of LogRecord
    private static final int DEFAULT_SIZE = 1000;

    // target handler
    private Handler target;

    // buffer size
    private int size = DEFAULT_SIZE;

    // push level
    private Level push = Level.SEVERE;

    // LogManager instance for convenience
    private final LogManager manager = LogManager.getLogManager();

    // buffer
    private LogRecord[] buffer;

    // current position in buffer
    private int cursor;

    /**
     * Default constructor, construct and init a {@code MemoryHandler} using
     * {@code LogManager} properties or default values.
     *
     * @throws RuntimeException
     *             if property value are invalid and no default value could be
     *             used.
     */
    public MemoryHandler() {
        String className = this.getClass().getName();
        // init target
        final String targetName = manager.getProperty(className + ".target");
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
            Class<?> targetClass = loader.loadClass(targetName);
            target = (Handler) targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot load target handler '" + targetName + "'");
        }
        // init size
        String sizeString = manager.getProperty(className + ".size");
        if (sizeString != null) {
            try {
                size = Integer.parseInt(sizeString);
                if (size <= 0) {
                    size = DEFAULT_SIZE;
                }
            } catch (Exception e) {
                printInvalidPropMessage(className + ".size", sizeString, e);
            }
        }
        // init push level
        String pushName = manager.getProperty(className + ".push");
        if (pushName != null) {
            try {
                push = Level.parse(pushName);
            } catch (Exception e) {
                printInvalidPropMessage(className + ".push", pushName, e);
            }
        }
        // init other properties which are common for all Handler
        initProperties("ALL", null, "java.util.logging.SimpleFormatter", null);
        buffer = new LogRecord[size];
    }

    /**
     * Construct and init a {@code MemoryHandler} using given target, size and
     * push level, other properties using {@code LogManager} properties or
     * default values.
     *
     * @param target
     *            the given {@code Handler} to output
     * @param size
     *            the maximum number of buffered {@code LogRecord}, greater than
     *            zero
     * @param pushLevel
     *            the push level
     * @throws IllegalArgumentException
     *             if {@code size <= 0}
     * @throws RuntimeException
     *             if property value are invalid and no default value could be
     *             used.
     */
    public MemoryHandler(Handler target, int size, Level pushLevel) {
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        target.getLevel();
        pushLevel.intValue();
        this.target = target;
        this.size = size;
        this.push = pushLevel;
        initProperties("ALL", null, "java.util.logging.SimpleFormatter", null);
        buffer = new LogRecord[size];
    }

    /**
     * Close this handler and target handler, free all associated resources.
     */
    @Override
    public void close() {
        manager.checkAccess();
        target.close();
        setLevel(Level.OFF);
    }

    /**
     * Call target handler to flush any buffered output. Note that this doesn't
     * cause this {@code MemoryHandler} to push.
     */
    @Override
    public void flush() {
        target.flush();
    }

    /**
     * Put a given {@code LogRecord} into internal buffer. If given record is
     * not loggable, just return. Otherwise it is stored in the buffer.
     * Furthermore if the record's level is not less than the push level, the
     * push action is triggered to output all the buffered records to the target
     * handler, and the target handler will publish them.
     *
     * @param record
     *            the log record
     */
    @Override public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        if (cursor >= size) {
            cursor = 0;
        }
        buffer[cursor++] = record;
        if (record.getLevel().intValue() >= push.intValue()) {
            push();
        }
    }

    /**
     * Return the push level.
     *
     * @return the push level
     */
    public Level getPushLevel() {
        return push;
    }

    /**
     * Check if given {@code LogRecord} would be put into this
     * {@code MemoryHandler}'s internal buffer.
     * <p>
     * The given {@code LogRecord} is loggable if and only if it has appropriate
     * level and it pass any associated filter's check.
     * <p>
     * Note that the push level is not used for this check.
     *
     * @param record
     *            the given {@code LogRecord}
     * @return the given {@code LogRecord} if it should be logged, {@code false}
     *         if {@code LogRecord} is {@code null}.
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        return super.isLoggable(record);
    }

    /**
     * Triggers a push action to output all buffered records to the target handler,
     * and the target handler will publish them. Then the buffer is cleared.
     */
    public void push() {
        for (int i = cursor; i < size; i++) {
            if (buffer[i] != null) {
                target.publish(buffer[i]);
            }
            buffer[i] = null;
        }
        for (int i = 0; i < cursor; i++) {
            if (buffer[i] != null) {
                target.publish(buffer[i]);
            }
            buffer[i] = null;
        }
        cursor = 0;
    }

    /**
     * Set the push level. The push level is used to check the push action
     * triggering. When a new {@code LogRecord} is put into the internal
     * buffer and its level is not less than the push level, the push action
     * will be triggered. Note that set new push level won't trigger push action.
     *
     * @param newLevel
     *                 the new level to set.
     */
    public void setPushLevel(Level newLevel) {
        manager.checkAccess();
        newLevel.intValue();
        this.push = newLevel;
    }
}
