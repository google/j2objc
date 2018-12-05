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

package tests.security.spec;

import junit.framework.TestCase;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

/**
 * Tests for <code>PSSParameterSpec</code> class (1.5)
 *
 */
public class PSSParameterSpecTest extends TestCase {

    /**
     * Test #1 for <code>PSSParameterSpec(int)</code> ctor<br>
     * Assertion: constructs using valid parameter
     * <code>PSSParameterSpec<code> object
     */
    public final void testPSSParameterSpec0101() {
        AlgorithmParameterSpec aps = new PSSParameterSpec(20);
        assertTrue(aps instanceof PSSParameterSpec);
    }

    /**
     * Test #2 for <code>PSSParameterSpec(int)</code> ctor<br>
     * Assertion:
     * throws <code>IllegalArgumentException</code>
     * if <code>saltLen</code> less than 0
     */
    public final void testPSSParameterSpec0102() {
        try {
            new PSSParameterSpec(-1);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test #1 for
     * <code>
     * PSSParameterSpec(String,String,AlgorithmParameterSpec,int,int)
     * </code> ctor<br>
     * Assertion: constructs using valid parameters
     * <code>PSSParameterSpec<code> object
     */
    public final void testPSSParameterSpec0201() {
        AlgorithmParameterSpec aps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertTrue(aps instanceof PSSParameterSpec);
    }

    /**
     * Test #2 for
     * <code>
     * PSSParameterSpec(String,String,AlgorithmParameterSpec,int,int)
     * </code> ctor<br>
     * Assertion:
     * throws <code>NullPointerException</code>
     * if <code>mdName</code> is null
     */
    public final void testPSSParameterSpec0202() {
        try {
            new PSSParameterSpec(null, "MGF1", MGF1ParameterSpec.SHA1, 20, 1);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #3 for
     * <code>
     * PSSParameterSpec(String,String,AlgorithmParameterSpec,int,int)
     * </code> ctor<br>
     * Assertion:
     * throws <code>NullPointerException</code>
     * if <code>mgfName</code> is null
     */
    public final void testPSSParameterSpec0203() {
        try {
            new PSSParameterSpec("SHA-1", null, MGF1ParameterSpec.SHA1, 20, 1);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #4 for
     * <code>
     * PSSParameterSpec(String,String,AlgorithmParameterSpec,int,int)
     * </code> ctor<br>
     * Assertion:
     * throws <code>IllegalArgumentException<code>
     * if <code>saltLen<code> less than 0
     */
    public final void testPSSParameterSpec0204() {
        try {
            new PSSParameterSpec("SHA-1", "MGF1",
                    MGF1ParameterSpec.SHA1, -20, 1);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test #5 for
     * <code>
     * PSSParameterSpec(String,String,AlgorithmParameterSpec,int,int)
     * </code> ctor<br>
     * Assertion:
     * throws <code>IllegalArgumentException</code>
     * if <code>trailerField</code> less than 0
     */
    public final void testPSSParameterSpec0205() {
        try {
            new PSSParameterSpec("SHA-1", "MGF1",
                    MGF1ParameterSpec.SHA1, 20, -1);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test #6 for
     * <code>
     * PSSParameterSpec(String,String,AlgorithmParameterSpec,int,int)
     * </code> ctor<br>
     * Assertion: <code>AlgorithmParameterSpec</code> can be null
     *
     */
    public final void testPSSParameterSpec0206() {
        new PSSParameterSpec("SHA-1", "MGF1", null, 20, 1);
    }

    /**
     * Test for <code>getDigestAlgorithm()</code> method
     * Assertion: returns message digest algorithm name
     */
    public final void testGetDigestAlgorithm() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertEquals("SHA-1", pssps.getDigestAlgorithm());
    }

    /**
     * Test for <code>getMGFAlgorithm()</code> method
     * Assertion: returns mask generation function algorithm name
     */
    public final void testGetMGFAlgorithm() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertEquals("MGF1", pssps.getMGFAlgorithm());
    }

    /**
     * Test #1 for <code>getMGFParameters()</code> method
     * Assertion: returns mask generation function parameters
     */
    public final void testGetMGFParameters01() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertTrue(MGF1ParameterSpec.SHA1.equals(pssps.getMGFParameters()));
    }

    /**
     * Test #2 for <code>getMGFParameters()</code> method
     * Assertion: returns <code>null</code>
     * if <code>null</code> had been passed as
     * AlgorithmParameterSpec parameter to the ctor
     */
    public final void testGetMGFParameters02() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                null, 20, 1);
        assertNull(pssps.getMGFParameters());
    }


    /**
     * Test for <code>getSaltLength()</code> method<br>
     * Assertion: returns salt length value
     */
    public final void testGetSaltLength() {
        PSSParameterSpec pssps = new PSSParameterSpec(20);
        assertEquals(20, pssps.getSaltLength());
    }

    /**
     * Test for <code>getTrailerField()</code> method<br>
     * Assertion: returns trailer field value
     */
    public final void testGetTrailerField() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertEquals(1, pssps.getTrailerField());
    }

    /**
     * Test for <code>DEFAULT</code> field<br>
     * Assertion: default message digest algorithm name is "SHA-1"
     */
    public final void testDEFAULTmdName() {
        assertEquals("SHA-1", PSSParameterSpec.DEFAULT.getDigestAlgorithm());
    }

    /**
     * Test for <code>DEFAULT</code> field<br>
     * Assertion: default mask generation function algorithm name is "MGF1"
     */
    public final void testDEFAULTmgfName() {
        assertEquals("MGF1", PSSParameterSpec.DEFAULT.getMGFAlgorithm());
    }

    /**
     * Test for <code>DEFAULT</code> field<br>
     * Assertion: default algorithm parameters for mask
     * generation function are <code>MGF1ParameterSpec.SHA1</code>
     */
    public final void testDEFAULTmgfSpec() {
        assertTrue(MGF1ParameterSpec.SHA1.equals(PSSParameterSpec.DEFAULT.getMGFParameters()));
    }

    /**
     * Test for <code>DEFAULT</code> field<br>
     * Assertion: default salt length value is 20
     */
    public final void testDEFAULTsaltLen() {
        assertEquals(20, PSSParameterSpec.DEFAULT.getSaltLength());
    }

    /**
     * Test for <code>DEFAULT</code> field<br>
     * Assertion: default trailer field value is 1
     */
    public final void testDEFAULTtrailerField() {
        assertEquals(1, PSSParameterSpec.DEFAULT.getTrailerField());
    }
}
