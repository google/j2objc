/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.AssertionFailedError;

public class SerializationTester<T> {
    private final String golden;
    private final T value;

    public SerializationTester(T value, String golden) {
        this.golden = golden;
        this.value = value;
    }

    /**
     * Returns true if {@code a} and {@code b} are equal. Override this if
     * {@link Object#equals} isn't appropriate or sufficient for this tester's
     * value type.
     */
    protected boolean equals(T a, T b) {
        return a.equals(b);
    }

    /**
     * Verifies that {@code deserialized} is valid. Implementations of this
     * method may mutate {@code deserialized}.
     */
    protected void verify(T deserialized) throws Exception {}

    public void test() {
        try {
            if (golden == null || golden.length() == 0) {
                fail("No golden value supplied! Consider using this: "
                        + hexEncode(serialize(value)));
            }

            @SuppressWarnings("unchecked") // deserialize should return the proper type
            T deserialized = (T) deserialize(hexDecode(golden));
            assertTrue("User-constructed value doesn't equal deserialized golden value",
                    equals(value, deserialized));

            @SuppressWarnings("unchecked") // deserialize should return the proper type
            T reserialized = (T) deserialize(serialize(value));
            assertTrue("User-constructed value doesn't equal itself, reserialized",
                    equals(value, reserialized));

            // just a sanity check! if this fails, verify() is probably broken
            verify(value);
            verify(deserialized);
            verify(reserialized);

        } catch (Exception e) {
            Error failure = new AssertionFailedError();
            failure.initCause(e);
            throw failure;
        }
    }

    private static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(object);
        return out.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object result = in.readObject();
        assertEquals(-1, in.read());
        return result;
    }

    private static String hexEncode(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private static byte[] hexDecode(String s) {
        byte[] result = new byte[s.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(s.substring(i*2, i*2 + 2), 16);
        }
        return result;
    }

    /**
     * Returns a serialized-and-deserialized copy of {@code object}.
     */
    public static Object reserialize(Object object) throws IOException, ClassNotFoundException {
        return deserialize(serialize(object));
    }

    public static String serializeHex(Object object) throws IOException {
        return hexEncode(serialize(object));
    }

    public static Object deserializeHex(String hex) throws IOException, ClassNotFoundException {
        return deserialize(hexDecode(hex));
    }
}
