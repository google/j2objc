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
/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package tests.security.cert;

import junit.framework.TestCase;

import java.io.IOException;
import java.security.cert.PolicyQualifierInfo;
import java.util.Arrays;

/**
 * PolicyQualifierInfo test
 *
 */
public class PolicyQualifierInfoTest extends TestCase {


    /**
     * Test #1 for <code>PolicyQualifierInfo</code> constructor<br>
     * Assertion: throws <code>IOException</code> if byte array
     * parameter does not represent a valid and parsable policy
     * qualifier info
     */
    public final void test_Ctor() throws IOException {
        try {
            // pass null
            new PolicyQualifierInfo(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            // pass empty array
            new PolicyQualifierInfo(new byte[0]);
            fail("IOE expected");
        } catch (IOException e) {
        }


        try {
            // pass invalid array
            new PolicyQualifierInfo(
                    new byte[] {(byte)0x06, (byte)0x03,
                            (byte)0x81, (byte)0x34, (byte)0x03});
            fail("IOE expected");
        } catch (IOException e) {
        }
    }

    /**
     * Test #2 for <code>PolicyQualifierInfo</code> constructor<br>
     * Assertion: throws <code>IOException</code> if byte array
     * parameter does not represent a valid and parsable policy
     * qualifier info
     */
    public final void testPolicyQualifierInfo02() {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // corrupt root seq length
        encoding[1] = (byte)0x27;

        try {
            // pass invalid array
            new PolicyQualifierInfo(encoding);
            fail("IOE expected");
        } catch (IOException e) {
        }


        // get valid encoding
        encoding = getDerEncoding();
        // corrupt policy qualifier ID:
        //  - change OID to the Relative OID
        encoding[2] = (byte)13;
        try {
            // pass invalid array
            new PolicyQualifierInfo(encoding);
            fail("IOE expected");
        } catch (IOException e) {
        }
    }

    /**
     * Test #3 for <code>PolicyQualifierInfo</code> constructor<br>
     * Assertion: Creates an instance of <code>PolicyQualifierInfo</code>
     * from the encoded bytes
     *
     * @throws IOException
     */
    public final void testPolicyQualifierInfo03() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // pass valid array
        new PolicyQualifierInfo(encoding);
    }

    /**
     * Test #4 for <code>PolicyQualifierInfo</code> constructor<br>
     * Assertion: The encoded byte array is copied on construction
     *
     * @throws IOException
     */
    public final void testPolicyQualifierInfo04() throws IOException  {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        byte[] encodingCopy = encoding.clone();
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encodingCopy);
        // get encoding
        byte[] encodingRet = i.getEncoded();
        // check returned array
        assertTrue(Arrays.equals(encoding, encodingRet));
        // modify input
        encodingCopy[0] = (byte)0;
        // get encoding again
        byte[] encodingRet1 = i.getEncoded();
        // check that above modification did not change
        // internal state of the PolicyQualifierInfo instance
        assertTrue(Arrays.equals(encoding, encodingRet1));
    }

    /**
     * Test #1 for <code>getEncoded()</code> method
     * Assertion: Returns the ASN.1 DER encoded form of
     * this <code>PolicyQualifierInfo</code>
     *
     * @throws IOException
     */
    public final void testGetEncoded01() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        // get encoding
        byte[] encodingRet = i.getEncoded();
        // check returned array
        assertTrue(Arrays.equals(encoding, encodingRet));
    }

    /**
     * Test #2 for <code>getEncoded()</code> method
     * Assertion: a copy is returned each time
     *
     * @throws IOException
     */
    public final void testGetEncoded02() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        byte[] encodingCopy = encoding.clone();
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encodingCopy);
        // get encoding
        byte[] encodingRet = i.getEncoded();
        // modify returned array
        encodingRet[0] = (byte)0;
        // get encoding again
        byte[] encodingRet1 = i.getEncoded();
        // check that above modification did not change
        // internal state of the PolicyQualifierInfo instance
        assertTrue(Arrays.equals(encoding, encodingRet1));
    }

    /**
     * Test #1 for <code>getPolicyQualifier()</code> method
     * Assertion: Returns the ASN.1 DER encoded form of
     * this <code>PolicyQualifierInfo</code>
     *
     * @throws IOException
     */
    public final void testGetPolicyQualifier01() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // get policy qualifier encoding
        byte[] pqEncoding = new byte[28];
        System.arraycopy(encoding, 12, pqEncoding, 0, pqEncoding.length);
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        // get encoding
        byte[] pqEncodingRet = i.getPolicyQualifier();
        // check returned array
        assertTrue(Arrays.equals(pqEncoding, pqEncodingRet));
    }

    /**
     * Test #2 for <code>getPolicyQualifier()</code> method
     * Assertion: a copy is returned each time
     *
     * @throws IOException
     */
    public final void testGetPolicyQualifier02() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // get policy qualifier encoding
        byte[] pqEncoding = new byte[28];
        System.arraycopy(encoding, 12, pqEncoding, 0, pqEncoding.length);
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        // get encoding
        byte[] pqEncodingRet = i.getPolicyQualifier();
        // modify returned array
        pqEncodingRet[0] = (byte)0;
        // get encoding again
        byte[] pqEncodingRet1 = i.getPolicyQualifier();
        //
        assertNotSame(pqEncodingRet, pqEncodingRet1);
        // check that above modification did not change
        // internal state of the PolicyQualifierInfo instance
        assertTrue(Arrays.equals(pqEncoding, pqEncodingRet1));
    }

    /**
     * Test for <code>getPolicyQualifierId()</code> method
     * Assertion: Returns the <code>policyQualifierId</code>
     * field of this <code>PolicyQualifierInfo</code>.
     * The <code>policyQualifierId</code> is an Object Identifier (OID)
     * represented by a set of nonnegative integers separated by periods
     *
     * @throws IOException
     */
    public final void testGetPolicyQualifierId() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);
        // get OID as String and check it
        assertEquals("1.3.6.1.5.5.7.2.1", i.getPolicyQualifierId());

        // get valid encoding
        encoding = getDerEncoding();
        // change OID to 1.3.98437.82818.1
        encoding[5] = (byte)0x86;
        encoding[6] = (byte)0x81;
        encoding[8] = (byte)0x85;
        encoding[9] = (byte)0x87;
        i = new PolicyQualifierInfo(encoding);
        // get OID as String and check it
        assertEquals("1.3.98437.82818.1", i.getPolicyQualifierId());
    }

    /**
     * Test for <code>toString()</code> method
     * Assertion: returns description of the contents of this
     * <code>PolicyQualifierInfo</code> as printable <code>String</code>
     * @throws IOException
     *
     * @throws IOException
     */
    public final void testToString() throws IOException {
        // get valid encoding
        byte[] encoding = getDerEncoding();
        // pass valid array
        PolicyQualifierInfo i = new PolicyQualifierInfo(encoding);

        assertNotNull(i.toString());
    }

    //
    // Private stuff
    //

    /**
     * Returns valid DER encoding for the following ASN.1 definition
     * (as specified in RFC 3280 -
     *  Internet X.509 Public Key Infrastructure.
     *  Certificate and Certificate Revocation List (CRL) Profile.
     *  http://www.ietf.org/rfc/rfc3280.txt):
     *
     *   PolicyQualifierInfo ::= SEQUENCE {
     *      policyQualifierId       PolicyQualifierId,
     *      qualifier               ANY DEFINED BY policyQualifierId
     *   }
     *
     * where policyQualifierId (OID) is
     *      1.3.6.1.5.5.7.2.1
     * and qualifier (IA5String) is
     *      "http://www.qq.com/stmt.txt"
     *
     * (data generated by own encoder during test development)
     */
    private static final byte[] getDerEncoding() {
        // DO NOT MODIFY!
        return  new byte[] {
            (byte)0x30, (byte)0x26, // tag Seq, length
              (byte)0x06, (byte)0x08, // tag OID, length
                (byte)0x2b, (byte)0x06, (byte)0x01, (byte)0x05, // oid value
                (byte)0x05, (byte)0x07, (byte)0x02, (byte)0x01, // oid value
              (byte)0x16, (byte)0x1a, // tag IA5String, length
                (byte)0x68, (byte)0x74, (byte)0x74, (byte)0x70,  // IA5String value
                (byte)0x3a, (byte)0x2f, (byte)0x2f, (byte)0x77,  // IA5String value
                (byte)0x77, (byte)0x77, (byte)0x2e, (byte)0x71,  // IA5String value
                (byte)0x71, (byte)0x2e, (byte)0x63, (byte)0x6f,  // IA5String value
                (byte)0x6d, (byte)0x2f, (byte)0x73, (byte)0x74,  // IA5String value
                (byte)0x6d, (byte)0x74, (byte)0x2e, (byte)0x74,  // IA5String value
                (byte)0x78, (byte)0x74   // IA5String value
        };
    }
}
