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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package tests.security.cert;

import junit.framework.TestCase;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;

import org.apache.harmony.security.tests.support.cert.MyCertPathBuilderSpi;

/**
 * Tests for <code>CertPathBuilderSpi</code> class constructors and methods.
 *
 */
public class CertPathBuilderSpiTest extends TestCase {

    /**
     * Test for <code>CertPathBuilderSpi</code> constructor Assertion:
     * constructs CertPathBuilderSpi
     */
    public void testCertPathBuilderSpi01() throws CertPathBuilderException,
            InvalidAlgorithmParameterException {
        CertPathBuilderSpi certPathBuilder = new MyCertPathBuilderSpi();
        CertPathParameters cpp = null;
        try {
            certPathBuilder.engineBuild(cpp);
            fail("CertPathBuilderException must be thrown");
        } catch (CertPathBuilderException e) {
        }
        CertPathBuilderResult cpbResult = certPathBuilder.engineBuild(cpp);
        assertNull("Not null CertPathBuilderResult", cpbResult);
    }
}
