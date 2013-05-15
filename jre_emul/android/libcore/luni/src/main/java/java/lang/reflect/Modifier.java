/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang.reflect;

/**
 * This class provides static methods to decode class and member modifiers.
 *
 * @see Class#getModifiers()
 * @see Member#getModifiers()
 */
public class Modifier {

    /**
     * The {@code int} value representing the {@code public}
     * modifier.
     */
    public static final int PUBLIC = 0x1;

    /**
     * The {@code int} value representing the {@code private}
     * modifier.
     */
    public static final int PRIVATE = 0x2;

    /**
     * The {@code int} value representing the {@code protected}
     * modifier.
     */
    public static final int PROTECTED = 0x4;

    /**
     * The {@code int} value representing the {@code static} modifier.
     */
    public static final int STATIC = 0x8;

    /**
     * The {@code int} value representing the {@code final} modifier.
     */
    public static final int FINAL = 0x10;

    /**
     * The {@code int} value representing the {@code synchronized}
     * modifier.
     */
    public static final int SYNCHRONIZED = 0x20;

    /**
     * The {@code int} value representing the {@code volatile}
     * modifier.
     */
    public static final int VOLATILE = 0x40;

    /**
     * The {@code int} value representing the {@code transient}
     * modifier.
     */
    public static final int TRANSIENT = 0x80;

    /**
     * The {@code int} value representing the {@code native} modifier.
     */
    public static final int NATIVE = 0x100;

    /**
     * The {@code int} value representing the {@code interface}
     * modifier.
     */
    public static final int INTERFACE = 0x200;

    /**
     * The {@code int} value representing the {@code abstract}
     * modifier.
     */
    public static final int ABSTRACT = 0x400;

    /**
     * The {@code int} value representing the {@code strict} modifier.
     */
    public static final int STRICT = 0x800;

    // Non-public types required by Java 5 update to class file format
    static final int BRIDGE = 0x40;

    static final int VARARGS = 0x80;

    static final int SYNTHETIC = 0x1000;

    static final int ANNOTATION = 0x2000;

    static final int ENUM = 0x4000;

    /**
     * Constructs a new {@code Modifier} instance.
     */
    public Modifier() {
    }

    /**
     * Returns a mask of all the modifiers that may be applied to classes.
     * @since 1.7
     * @hide 1.7
     */
    public static int classModifiers() {
        return PUBLIC | PROTECTED | PRIVATE | ABSTRACT | STATIC | FINAL | STRICT;
    }

    /**
     * Returns a mask of all the modifiers that may be applied to constructors.
     * @since 1.7
     * @hide 1.7
     */
    public static int constructorModifiers() {
        return PUBLIC | PROTECTED | PRIVATE;
    }

    /**
     * Returns a mask of all the modifiers that may be applied to fields.
     * @since 1.7
     * @hide 1.7
     */
    public static int fieldModifiers() {
        return PUBLIC | PROTECTED | PRIVATE | STATIC | FINAL | TRANSIENT | VOLATILE;
    }

    /**
     * Returns a mask of all the modifiers that may be applied to interfaces.
     * @since 1.7
     * @hide 1.7
     */
    public static int interfaceModifiers() {
        return PUBLIC | PROTECTED | PRIVATE | ABSTRACT | STATIC | STRICT;
    }

    /**
     * Returns a mask of all the modifiers that may be applied to methods.
     * @since 1.7
     * @hide 1.7
     */
    public static int methodModifiers() {
        return PUBLIC | PROTECTED | PRIVATE | ABSTRACT | STATIC | FINAL | SYNCHRONIZED | NATIVE | STRICT;
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * abstract} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         abstract} modifier, {@code false} otherwise
     */
    public static boolean isAbstract(int modifiers) {
        return ((modifiers & ABSTRACT) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * final} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         final} modifier, {@code false} otherwise
     */
    public static boolean isFinal(int modifiers) {
        return ((modifiers & FINAL) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * interface} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         interface} modifier, {@code false} otherwise
     */
    public static boolean isInterface(int modifiers) {
        return ((modifiers & INTERFACE) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * native} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         native} modifier, {@code false} otherwise
     */
    public static boolean isNative(int modifiers) {
        return ((modifiers & NATIVE) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * private} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         private} modifier, {@code false} otherwise
     */
    public static boolean isPrivate(int modifiers) {
        return ((modifiers & PRIVATE) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * protected} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         protected} modifier, {@code false} otherwise
     */
    public static boolean isProtected(int modifiers) {
        return ((modifiers & PROTECTED) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * public} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         public} modifier, {@code false} otherwise
     */
    public static boolean isPublic(int modifiers) {
        return ((modifiers & PUBLIC) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * static} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         static} modifier, {@code false} otherwise
     */
    public static boolean isStatic(int modifiers) {
        return ((modifiers & STATIC) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * strict} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         strict} modifier, {@code false} otherwise
     */
    public static boolean isStrict(int modifiers) {
        return ((modifiers & STRICT) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * synchronized} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         synchronized} modifier, {@code false} otherwise
     */
    public static boolean isSynchronized(int modifiers) {
        return ((modifiers & SYNCHRONIZED) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * transient} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         transient} modifier, {@code false} otherwise
     */
    public static boolean isTransient(int modifiers) {
        return ((modifiers & TRANSIENT) != 0);
    }

    /**
     * Indicates whether or not the specified modifiers contain the {@code
     * volatile} modifier.
     *
     * @param modifiers
     *            the modifiers to test
     * @return {@code true} if the specified modifiers contain the {@code
     *         volatile} modifier, {@code false} otherwise
     */
    public static boolean isVolatile(int modifiers) {
        return ((modifiers & VOLATILE) != 0);
    }

    /**
     * Returns a string containing the string representation of all modifiers
     * present in the specified modifiers. Modifiers appear in the order
     * specified by the Java Language Specification:
     *
     * {@code public private protected abstract static final transient volatile native synchronized interface strict}
     *
     * @param modifiers
     *            the modifiers to print
     * @return a printable representation of the modifiers
     */
    public static java.lang.String toString(int modifiers) {
        StringBuilder buf = new StringBuilder();

        if (isPublic(modifiers)) {
            buf.append("public ");
        }
        if (isProtected(modifiers)) {
            buf.append("protected ");
        }
        if (isPrivate(modifiers)) {
            buf.append("private ");
        }
        if (isAbstract(modifiers)) {
            buf.append("abstract ");
        }
        if (isStatic(modifiers)) {
            buf.append("static ");
        }
        if (isFinal(modifiers)) {
            buf.append("final ");
        }
        if (isTransient(modifiers)) {
            buf.append("transient ");
        }
        if (isVolatile(modifiers)) {
            buf.append("volatile ");
        }
        if (isSynchronized(modifiers)) {
            buf.append("synchronized ");
        }
        if (isNative(modifiers)) {
            buf.append("native ");
        }
        if (isStrict(modifiers)) {
            buf.append("strictfp ");
        }
        if (isInterface(modifiers)) {
            buf.append("interface ");
        }
        if (buf.length() == 0) {
            return "";
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }
}
