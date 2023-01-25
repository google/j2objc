/*
 * Copyright (C) 2022 The Android Open Source Project
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

package libcore.java.security.spec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.security.spec.MGF1ParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPrivateKeySpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XECPrivateKeySpecTest {

    @Test
    public void testConstructor() {
        byte[] scalar = new byte[] {9, 8, 7};
        XECPrivateKeySpec keySpec = new XECPrivateKeySpec(NamedParameterSpec.X25519, scalar);
        assertEquals(NamedParameterSpec.X25519, keySpec.getParams());
        assertArrayEquals(scalar, keySpec.getScalar());

        scalar = new byte[] {1, 3, 5, 7, 9};
        keySpec = new XECPrivateKeySpec(MGF1ParameterSpec.SHA512, scalar);
        assertEquals(MGF1ParameterSpec.SHA512, keySpec.getParams());
        assertArrayEquals(scalar, keySpec.getScalar());
    }
}
