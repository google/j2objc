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

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XECPublicKeySpecTest {

    @Test
    public void testConstructor() {
        XECPublicKeySpec keySpec = new XECPublicKeySpec(NamedParameterSpec.X25519, BigInteger.TEN);
        assertEquals(NamedParameterSpec.X25519, keySpec.getParams());
        assertEquals(BigInteger.TEN, keySpec.getU());

        keySpec = new XECPublicKeySpec(MGF1ParameterSpec.SHA512, BigInteger.ONE);
        assertEquals(MGF1ParameterSpec.SHA512, keySpec.getParams());
        assertEquals(BigInteger.ONE, keySpec.getU());
    }
}
