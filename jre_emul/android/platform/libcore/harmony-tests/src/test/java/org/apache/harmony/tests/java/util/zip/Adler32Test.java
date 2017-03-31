/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.tests.java.util.zip;

import java.util.zip.Adler32;

public class Adler32Test extends junit.framework.TestCase {

    /**
     * java.util.zip.Adler32#Adler32()
     */
    public void test_Constructor() {
        // test method of java.util.zip.Adler32()
        Adler32 adl = new Adler32();
        assertEquals("Constructor of adl32 failed", 1, adl.getValue());
    }

    /**
     * java.util.zip.Adler32#getValue()
     */
    public void test_getValue() {
        // test methods of java.util.zip.getValue()
        Adler32 adl = new Adler32();
        assertEquals("GetValue should return a zero as a result of construction an object of Adler32",
                1, adl.getValue());

        adl.reset();
        adl.update(1);
        // System.out.print("value of adl"+adl.getValue());
        // The value of the adl should be 131074
        assertEquals("update(int) failed to update the checksum to the correct value ",
                131074, adl.getValue());
        adl.reset();
        assertEquals("reset failed to reset the checksum value to zero", 1, adl
                .getValue());

        adl.reset();
        adl.update(Integer.MIN_VALUE);
        // System.out.print("value of adl " + adl.getValue());
        // The value of the adl should be 65537
        assertEquals("update(min) failed to update the checksum to the correct value ",
                65537L, adl.getValue());
    }

    /**
     * java.util.zip.Adler32#reset()
     */
    public void test_reset() {
        // test methods of java.util.zip.reset()
        Adler32 adl = new Adler32();
        adl.update(1);
        // System.out.print("value of adl"+adl.getValue());
        // The value of the adl should be 131074
        assertEquals("update(int) failed to update the checksum to the correct value ",
                131074, adl.getValue());
        adl.reset();
        assertEquals("reset failed to reset the checksum value to zero", 1, adl
                .getValue());
    }

    /**
     * java.util.zip.Adler32#update(int)
     */
    public void test_updateI() {
        // test methods of java.util.zip.update(int)
        Adler32 adl = new Adler32();
        adl.update(1);
        // The value of the adl should be 131074
        assertEquals("update(int) failed to update the checksum to the correct value ",
                131074, adl.getValue());

        adl.reset();
        adl.update(Integer.MAX_VALUE);
        // System.out.print("value of adl " + adl.getValue());
        // The value of the adl should be 16777472
        assertEquals("update(max) failed to update the checksum to the correct value ",
                16777472L, adl.getValue());

        adl.reset();
        adl.update(Integer.MIN_VALUE);
        // System.out.print("value of adl " + adl.getValue());
        // The value of the adl should be 65537
        assertEquals("update(min) failed to update the checksum to the correct value ",
                65537L, adl.getValue());

    }

    /**
     * java.util.zip.Adler32#update(byte[])
     */
    public void test_update$B() {
        // test method of java.util.zip.update(byte[])
        byte byteArray[] = { 1, 2 };
        Adler32 adl = new Adler32();
        adl.update(byteArray);
        // System.out.print("value of adl"+adl.getValue());
        // The value of the adl should be 393220
        assertEquals("update(byte[]) failed to update the checksum to the correct value ",
                393220, adl.getValue());

        adl.reset();
        byte byteEmpty[] = new byte[10000];
        adl.update(byteEmpty);
        // System.out.print("value of adl"+adl.getValue());
        // The value of the adl should be 655360001
        assertEquals("update(byte[]) failed to update the checksum to the correct value ",
                655360001L, adl.getValue());

    }

    /**
     * java.util.zip.Adler32#update(byte[], int, int)
     */
    public void test_update$BII() {
        // test methods of java.util.zip.update(byte[],int,int)
        byte[] byteArray = { 1, 2, 3 };
        Adler32 adl = new Adler32();
        int off = 2;// accessing the 2nd element of byteArray
        int len = 1;
        int lenError = 3;
        int offError = 4;
        adl.update(byteArray, off, len);
        // System.out.print("value of adl"+adl.getValue());
        // The value of the adl should be 262148
        assertEquals("update(byte[],int,int) failed to update the checksum to the correct value ",
                262148, adl.getValue());
        int r = 0;

        try {
            adl.update(byteArray, off, lenError);
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 1;
        }
        assertEquals("update(byte[],int,int) failed b/c lenError>byte[].length-off",
                1, r);

        try {
            adl.update(byteArray, offError, len);
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 2;
        }
        assertEquals("update(byte[],int,int) failed b/c offError>byte[].length",
                2, r);

    }

    @Override
    protected void setUp() {
    }

    @Override
    protected void tearDown() {
    }

}
