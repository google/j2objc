/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.nio;

/**
 * Defines byte order constants.
 */
public final class ByteOrder {
    private static final ByteOrder NATIVE_ORDER;

    /**
     * This constant represents big endian.
     */
    public static final ByteOrder BIG_ENDIAN;

    /**
     * This constant represents little endian.
     */
    public static final ByteOrder LITTLE_ENDIAN;

    private static native boolean isLittleEndian() /*-[
        return CFByteOrderGetCurrent() == CFByteOrderLittleEndian;
    ]-*/;

    static {
        boolean isLittleEndian = isLittleEndian();
        BIG_ENDIAN = new ByteOrder("BIG_ENDIAN", isLittleEndian);
        LITTLE_ENDIAN = new ByteOrder("LITTLE_ENDIAN", !isLittleEndian);
        NATIVE_ORDER = isLittleEndian ? LITTLE_ENDIAN : BIG_ENDIAN;
    }

    private final String name;

    /**
     * This is the only thing that ByteOrder is really used for: to know whether we need to swap
     * bytes to get this order, given bytes in native order. (That is, this is the opposite of
     * the hypothetical "isNativeOrder".)
     * @hide - needed in libcore.io too.
     */
    public final boolean needsSwap;

    private ByteOrder(String name, boolean needsSwap) {
        this.name = name;
        this.needsSwap = needsSwap;
    }

    /**
     * Returns the current platform byte order.
     *
     * @return the byte order object, which is either LITTLE_ENDIAN or
     *         BIG_ENDIAN.
     */
    public static ByteOrder nativeOrder() {
        return NATIVE_ORDER;
    }

    /**
     * Returns a string that describes this object.
     *
     * @return "BIG_ENDIAN" for {@link #BIG_ENDIAN ByteOrder.BIG_ENDIAN}
     *         objects, "LITTLE_ENDIAN" for
     *         {@link #LITTLE_ENDIAN ByteOrder.LITTLE_ENDIAN} objects.
     */
    @Override
    public String toString() {
        return name;
    }
}
