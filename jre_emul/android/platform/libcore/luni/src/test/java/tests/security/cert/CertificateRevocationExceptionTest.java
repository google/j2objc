/*
 * Copyright 2014 The Android Open Source Project
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

package tests.security.cert;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.cert.CRLReason;
import java.security.cert.CertificateRevokedException;
import java.security.cert.Extension;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

/**
 *
 */
public class CertificateRevocationExceptionTest extends TestCase implements SerializableAssert {
    private CertificateRevokedException getTestException() {
        HashMap<String, Extension> extensions = new HashMap<String, Extension>();
        // REASON_CODE
        extensions.put("2.5.29.21", getReasonExtension());
        extensions.put("2.5.29.24", getInvalidityExtension());
        return new CertificateRevokedException(
                        new Date(1199226851000L),
                        CRLReason.CESSATION_OF_OPERATION,
                        new X500Principal("CN=test1"),
                        extensions);
    }

    private Extension getReasonExtension() {
        return new Extension() {
            @Override
            public String getId() {
                return "2.5.29.21";
            }

            @Override
            public boolean isCritical() {
                return false;
            }

            @Override
            public byte[] getValue() {
                return new byte[] {4, 3, 10, 1, 5};
            }

            @Override
            public void encode(OutputStream out) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Extension getInvalidityExtension() {
        return new Extension() {
            @Override
            public String getId() {
                return "2.5.29.24";
            }

            @Override
            public boolean isCritical() {
                return false;
            }

            @Override
            public byte[] getValue() {
                return new byte[] {
                        0x18, 0x0F, 0x32, 0x30, 0x31, 0x34, 0x30, 0x31, 0x31, 0x37, 0x30, 0x38,
                        0x33, 0x30, 0x30, 0x39, 0x5a
                };
            }

            @Override
            public void encode(OutputStream out) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void testGetExtensions() throws Exception {
        CertificateRevokedException original = getTestException();
        Map<String, Extension> extensions = original.getExtensions();
        assertNotSame(extensions, original.getExtensions());

        try {
            extensions.put("2.2.2.2", getReasonExtension());
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testGetRevocationDate() throws Exception {
        CertificateRevokedException exception = getTestException();

        Date firstDate = exception.getRevocationDate();
        assertNotSame(firstDate, exception.getRevocationDate());

        firstDate.setYear(firstDate.getYear() + 1);
        assertTrue(firstDate.compareTo(exception.getRevocationDate()) > 0);
    }

    public void testGetInvalidityDate() throws Exception {
        CertificateRevokedException exception = getTestException();

        Date firstDate = exception.getInvalidityDate();
        assertNotSame(firstDate, exception.getInvalidityDate());

        firstDate.setYear(firstDate.getYear() + 1);
        assertTrue(firstDate.compareTo(exception.getInvalidityDate()) > 0);
    }

    public void testGetAuthorityName() throws Exception {
        CertificateRevokedException exception = getTestException();
        assertEquals(new X500Principal("CN=test1"), exception.getAuthorityName());
    }

    /**
     * serialization/deserialization compatibility.
     */
    public void testSerializationCertificateRevokedExceptionSelf() throws Exception {
        SerializationTest.verifySelf(getTestException(), this);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCertificateRevokedExceptionCompatability() throws Exception {
        // create test file (once)
        // SerializationTest.createGoldenFile("/tmp", this, getTestException());
        SerializationTest.verifyGolden(this, getTestException());
    }

    @Override
    public void assertDeserialized(Serializable initial, Serializable deserialized) {
        assertTrue(initial instanceof CertificateRevokedException);
        assertTrue(deserialized instanceof CertificateRevokedException);

        CertificateRevokedException expected = (CertificateRevokedException) initial;
        CertificateRevokedException actual = (CertificateRevokedException) deserialized;

        assertEquals(expected.getInvalidityDate(), actual.getInvalidityDate());
        assertNotSame(expected.getInvalidityDate(), actual.getInvalidityDate());
        assertEquals(expected.getRevocationDate(), actual.getRevocationDate());
        assertNotSame(expected.getRevocationDate(), actual.getRevocationDate());
        assertEquals(expected.getRevocationReason(), expected.getRevocationReason());
        assertEquals(expected.getAuthorityName(), actual.getAuthorityName());
        assertNotSame(expected.getAuthorityName(), actual.getAuthorityName());

        assertEquals(expected.getExtensions().size(), actual.getExtensions().size());
        assertEquals(expected.getExtensions().keySet(), actual.getExtensions().keySet());
    }
}
