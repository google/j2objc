/*
 * Copyright (C) 2015 The Android Open Source Project
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

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import libcore.util.HexEncoding;

public class AlgorithmParametersPSSTest extends TestCase {

    // ASN.1 DER-encoded forms of DEFAULT_SPEC, WEIRD_SPEC, and WEIRD2_SPEC were generated using
    // Bouncy Castle 1.52 AlgorithmParameters of type "PSS" and checked for correctness using ASN.1
    // DER decoder.
    private static final PSSParameterSpec DEFAULT_SPEC = PSSParameterSpec.DEFAULT;
    private static final byte[] DEFAULT_SPEC_DER_ENCODED = HexEncoding.decode("3000");

    private static final PSSParameterSpec WEIRD_SPEC =
            new PSSParameterSpec("SHA-224", "MGF1", MGF1ParameterSpec.SHA256, 32, 1);
    private static final byte[] WEIRD_SPEC_DER_ENCODED =
            HexEncoding.decode(
                    "3034a00f300d06096086480165030402040500a11c301a06092a864886f70d010108300d060960"
                    + "86480165030402010500a203020120");

    /** Truncated SEQUENCE (one more byte needed at the end) */
    private static final byte[] BROKEN_SPEC1_DER_ENCODED =
            HexEncoding.decode(
                    "303aa00f300d06096086480165030402030500a11c301a06092a864886f70d010108300d060960"
                    + "86480165030402020500a20302011ba303020103");

    /** Payload of SEQUENCE extends beyond the SEQUENCE. */
    private static final byte[] BROKEN_SPEC2_DER_ENCODED =
            HexEncoding.decode(
                    "3037a00f300d06096086480165030402030500a11c301a06092a864886f70d010108300d060960"
                    + "86480165030402020500a20302011ba303020103");

    public void testGetInstance() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        assertNotNull(params);
    }

    public void testGetAlgorithm() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        assertEquals("PSS", params.getAlgorithm());
    }

    public void testGetProvider() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        params.init(DEFAULT_SPEC);
        assertNotNull(params.getProvider());
    }

    public void testInitFailsWhenAlreadyInitialized() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        params.init(DEFAULT_SPEC);
        try {
            params.init(DEFAULT_SPEC);
            fail();
        } catch (InvalidParameterSpecException expected) {}
        try {
            params.init(DEFAULT_SPEC_DER_ENCODED);
            fail();
        } catch (IOException expected) {}
        try {
            params.init(DEFAULT_SPEC_DER_ENCODED, "ASN.1");
            fail();
        } catch (IOException expected) {}
    }

    public void testInitWithPSSParameterSpec() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        params.init(WEIRD_SPEC);
        assertPSSParameterSpecEquals(WEIRD_SPEC, params.getParameterSpec(PSSParameterSpec.class));
    }

    public void testInitWithDerEncoded() throws Exception {
        assertInitWithDerEncoded(WEIRD_SPEC_DER_ENCODED, WEIRD_SPEC);
        assertInitWithDerEncoded(DEFAULT_SPEC_DER_ENCODED, DEFAULT_SPEC);
    }

    private void assertInitWithDerEncoded(
            byte[] encoded, PSSParameterSpec expected) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        params.init(encoded);
        assertPSSParameterSpecEquals(expected, params.getParameterSpec(PSSParameterSpec.class));

        params = AlgorithmParameters.getInstance("PSS");
        params.init(encoded, "ASN.1");
        assertPSSParameterSpecEquals(expected, params.getParameterSpec(PSSParameterSpec.class));
    }

    public void testGetEncodedThrowsWhenNotInitialized() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        try {
            params.getEncoded();
            fail();
        } catch (IOException expected) {}
        try {
            params.getEncoded("ASN.1");
            fail();
        } catch (IOException expected) {}
    }

    public void testGetEncoded() throws Exception {
        assertGetEncoded(WEIRD_SPEC, WEIRD_SPEC_DER_ENCODED);
        assertGetEncoded(DEFAULT_SPEC, DEFAULT_SPEC_DER_ENCODED);
    }

    private void assertGetEncoded(PSSParameterSpec spec, byte[] expectedEncoded) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        params.init(spec);
        byte[] encoded = params.getEncoded("ASN.1");
        assertTrue(Arrays.equals(expectedEncoded, encoded));
        // Assert that getEncoded() returns ASN.1 form.
        assertTrue(Arrays.equals(encoded, params.getEncoded()));
    }

    public void testGetEncodedWithBrokenInput() throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("PSS");
        try {
            params.init(BROKEN_SPEC1_DER_ENCODED);
            fail();
        } catch (IOException expected) {
        } catch (IllegalArgumentException expected) {
            // Bouncy Castle incorrectly throws an IllegalArgumentException instead of IOException.
            if (!"BC".equals(params.getProvider().getName())) {
                throw new RuntimeException(
                        "Unexpected exception. Provider: " + params.getProvider(),
                        expected);
            }
        }

        params = AlgorithmParameters.getInstance("PSS");
        try {
            params.init(BROKEN_SPEC2_DER_ENCODED);
            fail();
        } catch (IOException expected) {
        } catch (IllegalArgumentException expected) {
            // Bouncy Castle incorrectly throws an IllegalArgumentException instead of IOException.
            if (!"BC".equals(params.getProvider().getName())) {
                throw new RuntimeException(
                        "Unexpected exception. Provider: " + params.getProvider(),
                        expected);
            }
        }
    }

    private static void assertPSSParameterSpecEquals(
        PSSParameterSpec spec1, PSSParameterSpec spec2) {
        assertEquals(
                getDigestAlgorithmCanonicalName(spec1.getDigestAlgorithm()),
                getDigestAlgorithmCanonicalName(spec2.getDigestAlgorithm()));
        assertEquals(
                getMGFAlgorithmCanonicalName(spec1.getMGFAlgorithm()),
                getMGFAlgorithmCanonicalName(spec2.getMGFAlgorithm()));

        AlgorithmParameterSpec spec1MgfParams = spec1.getMGFParameters();
        assertNotNull(spec1MgfParams);
        if (!(spec1MgfParams instanceof MGF1ParameterSpec)) {
            fail("Unexpected type of MGF parameters: " + spec1MgfParams.getClass().getName());
        }
        MGF1ParameterSpec spec1Mgf1Params = (MGF1ParameterSpec) spec1MgfParams;
        AlgorithmParameterSpec spec2MgfParams = spec2.getMGFParameters();
        assertNotNull(spec2MgfParams);
        if (!(spec2MgfParams instanceof MGF1ParameterSpec)) {
            fail("Unexpected type of MGF parameters: " + spec2MgfParams.getClass().getName());
        }
        MGF1ParameterSpec spec2Mgf1Params = (MGF1ParameterSpec) spec2MgfParams;

        assertEquals(
                getDigestAlgorithmCanonicalName(spec1Mgf1Params.getDigestAlgorithm()),
                getDigestAlgorithmCanonicalName(spec2Mgf1Params.getDigestAlgorithm()));
        assertEquals(spec1.getSaltLength(), spec2.getSaltLength());
        assertEquals(spec1.getTrailerField(), spec2.getTrailerField());
    }

    // All the craziness with supporting OIDs is needed because Bouncy Castle, when parsing from
    // ASN.1 form, returns PSSParameterSpec instances which use OIDs instead of JCA standard names
    // for digest algorithms and MGF algorithms.
    private static final Map<String, String> DIGEST_OID_TO_NAME = new TreeMap<String, String>(
            String.CASE_INSENSITIVE_ORDER);
    private static final Map<String, String> DIGEST_NAME_TO_OID = new TreeMap<String, String>(
            String.CASE_INSENSITIVE_ORDER);

    private static void addDigestOid(String algorithm, String oid) {
        DIGEST_OID_TO_NAME.put(oid, algorithm);
        DIGEST_NAME_TO_OID.put(algorithm, oid);
    }

    static {
        addDigestOid("SHA-1", "1.3.14.3.2.26");
        addDigestOid("SHA-224", "2.16.840.1.101.3.4.2.4");
        addDigestOid("SHA-256", "2.16.840.1.101.3.4.2.1");
        addDigestOid("SHA-384", "2.16.840.1.101.3.4.2.2");
        addDigestOid("SHA-512", "2.16.840.1.101.3.4.2.3");
    }

    private static String getDigestAlgorithmCanonicalName(String algorithm) {
        if (DIGEST_NAME_TO_OID.containsKey(algorithm)) {
            return algorithm.toUpperCase(Locale.US);
        }

        String nameByOid = DIGEST_OID_TO_NAME.get(algorithm);
        if (nameByOid != null) {
            return nameByOid;
        }

        return algorithm;
    }

    private static String getMGFAlgorithmCanonicalName(String name) {
        if ("MGF1".equalsIgnoreCase(name)) {
            return name.toUpperCase(Locale.US);
        } else if ("1.2.840.113549.1.1.8".equals(name)) {
            return "MGF1";
        } else {
            return name;
        }
    }
}
