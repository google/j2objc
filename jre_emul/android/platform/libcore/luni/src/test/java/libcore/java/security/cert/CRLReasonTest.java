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

package libcore.java.security.cert;

import java.security.cert.CRLReason;

import junit.framework.TestCase;

public class CRLReasonTest extends TestCase {
    public void testCryptoPrimitive_ordinal_ExpectedValues() throws Exception {
        assertEquals("UNSPECIFIED", 0, CRLReason.UNSPECIFIED.ordinal());
        assertEquals("KEY_COMPROMISE", 1, CRLReason.KEY_COMPROMISE.ordinal());
        assertEquals("CA_COMPROMISE", 2, CRLReason.CA_COMPROMISE.ordinal());
        assertEquals("AFFILIATION_CHANGED", 3, CRLReason.AFFILIATION_CHANGED.ordinal());
        assertEquals("SUPERSEDED", 4, CRLReason.SUPERSEDED.ordinal());
        assertEquals("CESSATION_OF_OPERATION", 5, CRLReason.CESSATION_OF_OPERATION.ordinal());
        assertEquals("CERTIFICATE_HOLD", 6, CRLReason.CERTIFICATE_HOLD.ordinal());
        assertEquals("UNUSED", 7, CRLReason.UNUSED.ordinal());
        assertEquals("REMOVE_FROM_CRL", 8, CRLReason.REMOVE_FROM_CRL.ordinal());
        assertEquals("PRIVILEGE_WITHDRAWN", 9, CRLReason.PRIVILEGE_WITHDRAWN.ordinal());
        assertEquals("AA_COMPROMISE", 10, CRLReason.AA_COMPROMISE.ordinal());
    }

    public void testCRLReason_values_ExpectedValues() throws Exception {
        CRLReason[] reasons = CRLReason.values();
        assertEquals(11, reasons.length);
        assertEquals(CRLReason.UNSPECIFIED, reasons[0]);
        assertEquals(CRLReason.KEY_COMPROMISE, reasons[1]);
        assertEquals(CRLReason.CA_COMPROMISE, reasons[2]);
        assertEquals(CRLReason.AFFILIATION_CHANGED, reasons[3]);
        assertEquals(CRLReason.SUPERSEDED, reasons[4]);
        assertEquals(CRLReason.CESSATION_OF_OPERATION, reasons[5]);
        assertEquals(CRLReason.CERTIFICATE_HOLD, reasons[6]);
        assertEquals(CRLReason.UNUSED, reasons[7]);
        assertEquals(CRLReason.REMOVE_FROM_CRL, reasons[8]);
        assertEquals(CRLReason.PRIVILEGE_WITHDRAWN, reasons[9]);
        assertEquals(CRLReason.AA_COMPROMISE, reasons[10]);
    }

    public void testCRLReason_valueOf_ExpectedValues() throws Exception {
        assertEquals(CRLReason.UNSPECIFIED, CRLReason.valueOf("UNSPECIFIED"));
        assertEquals(CRLReason.KEY_COMPROMISE, CRLReason.valueOf("KEY_COMPROMISE"));
        assertEquals(CRLReason.CA_COMPROMISE, CRLReason.valueOf("CA_COMPROMISE"));
        assertEquals(CRLReason.AFFILIATION_CHANGED, CRLReason.valueOf("AFFILIATION_CHANGED"));
        assertEquals(CRLReason.SUPERSEDED, CRLReason.valueOf("SUPERSEDED"));
        assertEquals(CRLReason.CESSATION_OF_OPERATION, CRLReason.valueOf("CESSATION_OF_OPERATION"));
        assertEquals(CRLReason.CERTIFICATE_HOLD, CRLReason.valueOf("CERTIFICATE_HOLD"));
        assertEquals(CRLReason.UNUSED, CRLReason.valueOf("UNUSED"));
        assertEquals(CRLReason.REMOVE_FROM_CRL, CRLReason.valueOf("REMOVE_FROM_CRL"));
        assertEquals(CRLReason.PRIVILEGE_WITHDRAWN, CRLReason.valueOf("PRIVILEGE_WITHDRAWN"));
        assertEquals(CRLReason.AA_COMPROMISE, CRLReason.valueOf("AA_COMPROMISE"));
    }
}
