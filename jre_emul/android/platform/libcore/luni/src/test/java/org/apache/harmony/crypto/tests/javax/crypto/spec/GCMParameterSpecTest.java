/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.crypto.tests.javax.crypto.spec;

import java.util.Arrays;

import javax.crypto.spec.GCMParameterSpec;

import junit.framework.TestCase;

public class GCMParameterSpecTest extends TestCase {
    private static final byte[] TEST_IV = new byte[8];

    public void testConstructor_IntByteArray_Success() throws Exception {
        new GCMParameterSpec(8, TEST_IV);
    }

    public void testConstructor_IntByteArray_NegativeTLen_Failure() throws Exception {
        try {
            new GCMParameterSpec(-1, TEST_IV);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConstructor_IntByteArray_NullIv_Failure() throws Exception {
        try {
            new GCMParameterSpec(8, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConstructor_IntByteArrayWithOffsets_Success() throws Exception {
        new GCMParameterSpec(8, TEST_IV, 0, TEST_IV.length);
    }

    public void testConstructor_IntByteArrayWithOffsets_NullIv_Failure() throws Exception {
        try {
            new GCMParameterSpec(8, null, 0, TEST_IV.length);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConstructor_IntByteArrayWithOffsets_NegativeOffset_Failure() throws Exception {
        try {
            new GCMParameterSpec(8, TEST_IV, -1, TEST_IV.length);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConstructor_IntByteArrayWithOffsets_TooLongLength_Failure() throws Exception {
        try {
            new GCMParameterSpec(8, TEST_IV, 0, TEST_IV.length + 1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetIV_Success() throws Exception {
        GCMParameterSpec spec = new GCMParameterSpec(8, TEST_IV);

        byte[] actual = spec.getIV();
        assertEquals(Arrays.toString(TEST_IV), Arrays.toString(actual));

        // XOR with 0xFF so we're sure we changed the array
        for (int i = 0; i < actual.length; i++) {
            actual[i] ^= 0xFF;
        }

        assertFalse("Changing the IV returned shouldn't change the parameter spec",
                Arrays.equals(spec.getIV(), actual));
        assertEquals(Arrays.toString(TEST_IV), Arrays.toString(spec.getIV()));
    }

    public void testGetIV_Subarray_Success() throws Exception {
        GCMParameterSpec spec = new GCMParameterSpec(8, TEST_IV, 2, 4);
        assertEquals(Arrays.toString(Arrays.copyOfRange(TEST_IV, 2, 6)),
                Arrays.toString(spec.getIV()));
    }

    public void testGetTLen_Success() throws Exception {
        GCMParameterSpec spec = new GCMParameterSpec(8, TEST_IV);
        assertEquals(8, spec.getTLen());
    }
}
