/*
 * Copyright (C) 2018 The Android Open Source Project
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
package libcore.util;

/**
 * @hide
 */
@libcore.api.CorePlatformApi
public final class ArrayUtils {
    private ArrayUtils() {}

    /**
     * Throws {@link ArrayIndexOutOfBoundsException} if the range is out of bounds.
     * @param len length of the array. Must be non-negative
     * @param offset start index of the range. Must be non-negative
     * @param count length of the range. Must be non-negative
     * @throws ArrayIndexOutOfBoundsException if the range from {@code offset} with length
     * {@code count} is out of bounds of the array
     */
    @libcore.api.CorePlatformApi
    public static void throwsIfOutOfBounds(int len, int offset, int count) {
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative length: " + len);
        }

        if ((offset | count) < 0 || offset > len - count) {
            throw new ArrayIndexOutOfBoundsException(
                "length=" + len + "; regionStart=" + offset + "; regionLength=" + count);
        }
    }
}
