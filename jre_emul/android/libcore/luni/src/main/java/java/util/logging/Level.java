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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import libcore.util.Objects;

/**
 * {@code Level} objects are used to indicate the level of logging. There are a
 * set of predefined logging levels, each associated with an integer value.
 * Enabling a certain logging level also enables all logging levels with larger
 * values.
 * <p>
 * The predefined levels in ascending order are FINEST, FINER, FINE, CONFIG,
 * INFO, WARNING, SEVERE. There are two additional predefined levels, which are
 * ALL and OFF. ALL indicates logging all messages, and OFF indicates logging no
 * messages.
 */
public class Level implements Serializable {

    private static final long serialVersionUID = -8176160795706313070L;

    private static final List<Level> levels = new ArrayList<Level>(9);

    /**
     * The OFF level provides no logging messages.
     */
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE);

    /**
     * The SEVERE level provides severe failure messages.
     */
    public static final Level SEVERE = new Level("SEVERE", 1000);

    /**
     * The WARNING level provides warnings.
     */
    public static final Level WARNING = new Level("WARNING", 900);

    /**
     * The INFO level provides informative messages.
     */
    public static final Level INFO = new Level("INFO", 800);

    /**
     * The CONFIG level provides static configuration messages.
     */
    public static final Level CONFIG = new Level("CONFIG", 700);

    /**
     * The FINE level provides tracing messages.
     */
    public static final Level FINE = new Level("FINE", 500);

    /**
     * The FINER level provides more detailed tracing messages.
     */
    public static final Level FINER = new Level("FINER", 400);

    /**
     * The FINEST level provides highly detailed tracing messages.
     */
    public static final Level FINEST = new Level("FINEST", 300);

    /**
     * The ALL level provides all logging messages.
     */
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE);

    /**
     * Parses a level name into a {@code Level} object.
     *
     * @param name
     *            the name of the desired {@code level}, which cannot be
     *            {@code null}.
     * @return the level with the specified name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     * @throws IllegalArgumentException
     *             if {@code name} is not valid.
     */
    public static Level parse(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        synchronized (levels) {
            for (Level level : levels) {
                if (name.equals(level.getName())) {
                    return level;
                }
            }
        }

        boolean isNameAnInt;
        int nameAsInt;
        try {
            nameAsInt = Integer.parseInt(name);
            isNameAnInt = true;
        } catch (NumberFormatException e) {
            nameAsInt = 0;
            isNameAnInt = false;
        }

        if (isNameAnInt) {
            synchronized (levels) {
                /*
                 * Loop through levels a second time, so that the returned
                 * instance will be passed on the order of construction.
                 */
                for (Level level : levels) {
                    if (nameAsInt == level.intValue()) {
                        return level;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot parse name '" + name + "'");
        }

        return new Level(name, nameAsInt);
    }

    /**
     * The name of this Level.
     *
     * @serial
     */
    private final String name;

    /**
     * The integer value indicating the level.
     *
     * @serial
     */
    private final int value;

    /**
     * The name of the resource bundle used to localize the level name.
     *
     * @serial
     */
    private final String resourceBundleName;

    /**
     * The resource bundle associated with this level, used to localize the
     * level name.
     */
    private transient ResourceBundle rb;

    /**
     * Constructs an instance of {@code Level} taking the supplied name and
     * level value.
     *
     * @param name
     *            the name of the level.
     * @param level
     *            an integer value indicating the level.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    protected Level(String name, int level) {
        this(name, level, null);
    }

    /**
     * Constructs an instance of {@code Level} taking the supplied name, level
     * value and resource bundle name.
     *
     * @param name
     *            the name of the level.
     * @param level
     *            an integer value indicating the level.
     * @param resourceBundleName
     *            the name of the resource bundle to use.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    protected Level(String name, int level, String resourceBundleName) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        this.name = name;
        this.value = level;
        this.resourceBundleName = resourceBundleName;
        if (resourceBundleName != null) {
            try {
                rb = ResourceBundle.getBundle(resourceBundleName,
                        Locale.getDefault(), ClassLoader.getSystemClassLoader());
            } catch (MissingResourceException e) {
                rb = null;
            }
        }
        synchronized (levels) {
            levels.add(this);
        }
    }

    /**
     * Gets the name of this level.
     *
     * @return this level's name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the name of the resource bundle associated with this level.
     *
     * @return the name of this level's resource bundle.
     */
    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    /**
     * Gets the integer value indicating this level.
     *
     * @return this level's integer value.
     */
    public final int intValue() {
        return this.value;
    }

    /**
     * Serialization helper method to maintain singletons and add any new
     * levels.
     *
     * @return the resolved instance.
     */
    private Object readResolve() {
        synchronized (levels) {
            for (Level level : levels) {
                if (value != level.value) {
                    continue;
                }
                if (!name.equals(level.name)) {
                    continue;
                }
                if (Objects.equal(resourceBundleName, level.resourceBundleName)) {
                    return level;
                }
            }
            // This is a new value, so add it.
            levels.add(this);
            return this;
        }
    }

    /**
     * Serialization helper to setup transient resource bundle instance.
     *
     * @param in
     *            the input stream to read the instance data from.
     * @throws IOException
     *             if an IO error occurs.
     * @throws ClassNotFoundException
     *             if a class is not found.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        if (resourceBundleName != null) {
            try {
                rb = ResourceBundle.getBundle(resourceBundleName);
            } catch (MissingResourceException e) {
                rb = null;
            }
        }
    }

    /**
     * Gets the localized name of this level. The default locale is used. If no
     * resource bundle is associated with this level then the original level
     * name is returned.
     *
     * @return the localized name of this level.
     */
    public String getLocalizedName() {
        if (rb == null) {
            return name;
        }

        try {
            return rb.getString(name);
        } catch (MissingResourceException e) {
            return name;
        }
    }

    /**
     * Compares two {@code Level} objects for equality. They are considered to
     * be equal if they have the same level value.
     *
     * @param o
     *            the other object to compare this level to.
     * @return {@code true} if this object equals to the supplied object,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Level)) {
            return false;
        }

        return ((Level) o).intValue() == this.value;
    }

    /**
     * Returns the hash code of this {@code Level} object.
     *
     * @return this level's hash code.
     */
    @Override
    public int hashCode() {
        return this.value;
    }

    /**
     * Returns the string representation of this {@code Level} object. In
     * this case, it is the level's name.
     *
     * @return the string representation of this level.
     */
    @Override
    public final String toString() {
        return this.name;
    }
}
