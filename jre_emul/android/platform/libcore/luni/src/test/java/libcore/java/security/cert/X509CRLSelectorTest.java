/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.security.cert.X509CRLSelector;
import java.util.Collection;
import javax.security.auth.x500.X500Principal;
import junit.framework.TestCase;

public final class X509CRLSelectorTest extends TestCase {

    private static final String PRINCIPAL_STRING =
            "C=US, ST=California, L=Mountain View, O=Google Inc, CN=www.google.com";
    private static final X500Principal PRINCIPAL = new X500Principal(PRINCIPAL_STRING);

    public void testGetIssuersImmutable() {
        X509CRLSelector crlSelector = new X509CRLSelector();
        crlSelector.addIssuer(PRINCIPAL);
        Collection<X500Principal> issuers = crlSelector.getIssuers();
        try {
            issuers.clear();
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testGetIssuersNamesCopy() {
        X509CRLSelector crlSelector = new X509CRLSelector();
        crlSelector.addIssuer(PRINCIPAL);
        Collection<Object> issuers = crlSelector.getIssuerNames();
        assertEquals(1, issuers.size());
        issuers.clear();
        assertEquals(0, issuers.size());
    }
}
