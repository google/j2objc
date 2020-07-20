/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.security;

import junit.framework.TestCase;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;

import javax.security.auth.Subject;


public class PrincipalTest extends TestCase {
    /**
     * Default implementation of {@code implies} returns true iff the principal is one
     * of the subject's principals, or if the subject is null.
     */
    public void test_Principal_implies() throws Exception {
        HashSet<Principal> subjectPrincipals = new HashSet<>();
        subjectPrincipals.add(new PrincipalWithEqualityByName("a"));
        subjectPrincipals.add(new PrincipalWithEqualityByName("b"));
        Subject subject = new Subject(
                true /* readOnly */,
                subjectPrincipals,
                Collections.EMPTY_SET /* pubCredentials */,
                Collections.EMPTY_SET /* privCredentials */);
        Principal principalA = new PrincipalWithEqualityByName("a");
        assertTrue(principalA.implies(subject));
        Principal principalC = new PrincipalWithEqualityByName("c");
        assertFalse(principalC.implies(subject));
        assertFalse(principalC.implies(null));
    }

    private static class PrincipalWithEqualityByName implements Principal {

        private final String name;

        PrincipalWithEqualityByName(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof PrincipalWithEqualityByName)) {
                return false;
            }
            return this.name.equals(((PrincipalWithEqualityByName) other).getName());
        }
    }
}
