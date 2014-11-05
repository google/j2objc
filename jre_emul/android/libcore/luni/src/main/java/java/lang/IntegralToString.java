/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

/*-[
#include "java_lang_IntegralToString.h"
]-*/

/**
 * Converts integral types to strings. This class is public but hidden so that it can also be
 * used by java.util.Formatter to speed up %d. This class is in java.lang so that it can take
 * advantage of the package-private String constructor.
 *
 * The most important methods are appendInt/appendLong and intToString(int)/longToString(int).
 * The former are used in the implementation of StringBuilder, StringBuffer, and Formatter, while
 * the latter are used by Integer.toString and Long.toString.
 *
 * The append methods take AbstractStringBuilder rather than Appendable because the latter requires
 * CharSequences, while we only have raw char[]s. Since much of the savings come from not creating
 * any garbage, we can't afford temporary CharSequence instances.
 *
 * One day the performance advantage of the binary/hex/octal specializations will be small enough
 * that we can lose the duplication, but until then this class offers the full set.
 *
 * @hide
 */
public final class IntegralToString {

    private IntegralToString() {
    }

    /**
     * Equivalent to Integer.toString(i, radix).
     */
    public static native String intToString(int i, int radix);

    /**
     * Equivalent to Integer.toString(i).
     */
    public static native String intToString(int i) /*-[
      return IntegralToString_convertInt(NULL, i);
    ]-*/;

    /**
     * Equivalent to sb.append(Integer.toString(i)).
     */
    public static native void appendInt(AbstractStringBuilder sb, int i) /*-[
      IntegralToString_convertInt(&sb->delegate_, i);
    ]-*/;

    /**
     * Equivalent to Long.toString(v, radix).
     */
    public static native String longToString(long v, int radix);

    /**
     * Equivalent to Long.toString(l).
     */
    public static native String longToString(long l) /*-[
      return IntegralToString_convertLong(NULL, l);
    ]-*/;

    /**
     * Equivalent to sb.append(Long.toString(l)).
     */
    public static native void appendLong(AbstractStringBuilder sb, long l) /*-[
      IntegralToString_convertLong(&sb->delegate_, l);
    ]-*/;

    public static native String intToBinaryString(int i);

    public static native String longToBinaryString(long v);

    public static native StringBuilder appendByteAsHex(StringBuilder sb, byte b, boolean upperCase);

    public static native String byteToHexString(byte b, boolean upperCase);

    public static native String bytesToHexString(byte[] bytes, boolean upperCase);

    public static native String intToHexString(int i, boolean upperCase, int minWidth);

    public static native String longToHexString(long v);

    public static native String intToOctalString(int i);

    public static native String longToOctalString(long v);
}
