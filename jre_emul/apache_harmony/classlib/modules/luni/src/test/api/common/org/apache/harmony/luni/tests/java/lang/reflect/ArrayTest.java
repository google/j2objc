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
 * @author Serguei S.Zapreyev
 * 
 * This ArrayTest class ("Software") is furnished under license and may only be
 * used or copied in accordance with the terms of that license.
 *  
 */

package org.apache.harmony.luni.tests.java.lang.reflect;

import java.lang.reflect.Array;
import junit.framework.TestCase;

/*
 * Created on 01.28.2006
 */

@SuppressWarnings(value={"all"}) public class ArrayTest extends TestCase {

    /**
     *  
     */
    public void test_get_Obj_I() {
        class X {
            public int fld;

            public X() {
                return;
            }

            public X(X a9) {
                return;
            }
        }
        try {
            Object o = Array.newInstance(X.class, 777);
            X inst[] = (X[]) o;
            inst[776] = new X();
            inst[776].fld = 777;
            assertTrue("Error1", ((X) Array.get(o, 776)).fld == 777);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getBoolean_I() {
        try {
            Object o = Array.newInstance(boolean.class, 777);
            boolean inst[] = (boolean[]) o;
            inst[776] = false;
            assertTrue("Error1",
                    (((Boolean) Array.get(o, 776)).booleanValue()) == false);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getByte_I() {
        try {
            Object o = Array.newInstance(byte.class, 777);
            byte inst[] = (byte[]) o;
            inst[776] = (byte) 7;
            assertTrue("Error1",
                    (((Byte) Array.get(o, 776)).byteValue()) == (byte) 7);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getChar_I() {
        try {
            Object o = Array.newInstance(char.class, 777);
            char inst[] = (char[]) o;
            inst[776] = 'Z';
            assertTrue("Error1",
                    (((Character) Array.get(o, 776)).charValue()) == 'Z');
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getDouble_I() {
        try {
            Object o = Array.newInstance(double.class, 777);
            double inst[] = (double[]) o;
            inst[776] = 345.543d;
            assertTrue("Error1",
                    (((Double) Array.get(o, 776)).doubleValue()) == 345.543d);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getFloat_I() {
        try {
            Object o = Array.newInstance(float.class, 777);
            float inst[] = (float[]) o;
            inst[776] = 543.345f;
            assertTrue("Error1",
                    (((Float) Array.get(o, 776)).floatValue()) == 543.345f);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getInt_I() {
        try {
            Object o = Array.newInstance(int.class, 777);
            int inst[] = (int[]) o;
            inst[776] = Integer.MAX_VALUE;
            assertTrue(
                    "Error1",
                    (((Integer) Array.get(o, 776)).intValue()) == Integer.MAX_VALUE);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getLength_Obj() {
        try {
            Object o = Array.newInstance(ArrayTest.class, 777);
            assertTrue("Error1", Array.getLength(o) == 777);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getLong_I() {
        try {
            Object o = Array.newInstance(long.class, 777);
            long inst[] = (long[]) o;
            inst[776] = 999999999999l;
            assertTrue("Error1",
                    (((Long) Array.get(o, 776)).longValue()) == 999999999999l);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_getShort_I() {
        try {
            Object o = Array.newInstance(short.class, 777);
            short inst[] = (short[]) o;
            inst[776] = Short.MAX_VALUE;
            assertTrue(
                    "Error1",
                    (((Short) Array.get(o, 776)).shortValue()) == Short.MAX_VALUE);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     *
    public void test_newInstance_Obj() {
        class X {
            public X() {
                return;
            }

            public X(X a9) {
                return;
            }
        }
        new X(new X());
        try {
            Object o = Array.newInstance(X.class, 777);
            assertTrue("Error1", o.getClass().getName().equals(
                    "[Ljava.lang.reflect.ArrayTest$2X;"));
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     *
    public void test_newInstance_Obj_IArr() {
        class X {
            public X() {
                return;
            }

            public X(X a9) {
                return;
            }
        }
        new X(new X());
        try {
            Object o = Array.newInstance(X.class, 777);
            Object o2 = Array.newInstance(o.getClass(), 255);
            assertTrue("Error1" + o2.getClass().getName(), o2.getClass()
                    .getName().equals("[[Ljava.lang.reflect.ArrayTest$3X;"));
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_set_Obj_I() {
        class X {
            public int fld;

            public X() {
                return;
            }

            public X(X a9) {
                return;
            }
        }
        try {
            Object o = Array.newInstance(X.class, 777);
            X x = new X();
            x.fld = 777;
            Array.set(o, 776, (Object) x);
            assertTrue("Error1", ((X) Array.get(o, 776)).fld == 777);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setBoolean_I() {
        try {
            Object o = Array.newInstance(boolean.class, 777);
            Array.set(o, 776, (Object) new Boolean(false));
            assertTrue("Error1",
                    (((Boolean) Array.get(o, 776)).booleanValue()) == false);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setByte_I() {
        try {
            Object o = Array.newInstance(byte.class, 777);
            Array.set(o, 776, (Object) new Byte((byte) 7));
            assertTrue("Error1",
                    (((Byte) Array.get(o, 776)).byteValue()) == (byte) 7);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setChar_I() {
        try {
            Object o = Array.newInstance(char.class, 777);
            Array.set(o, 776, (Object) new Character('Z'));
            assertTrue("Error1",
                    (((Character) Array.get(o, 776)).charValue()) == 'Z');
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setDouble_I() {
        try {
            Object o = Array.newInstance(double.class, 777);
            Array.set(o, 776, (Object) new Double(345.543d));
            assertTrue("Error1",
                    (((Double) Array.get(o, 776)).doubleValue()) == 345.543d);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setFloat_I() {
        try {
            Object o = Array.newInstance(float.class, 777);
            Array.set(o, 776, (Object) new Float(543.345f));
            assertTrue("Error1",
                    (((Float) Array.get(o, 776)).floatValue()) == 543.345f);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setInt_I() {
        try {
            Object o = Array.newInstance(int.class, 777);
            Array.set(o, 776, (Object) new Integer(Integer.MAX_VALUE));
            assertTrue(
                    "Error1",
                    (((Integer) Array.get(o, 776)).intValue()) == Integer.MAX_VALUE);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setLong_I() {
        try {
            Object o = Array.newInstance(long.class, 777);
            Array.set(o, 776, (Object) new Long(999999999999l));
            assertTrue("Error1",
                    (((Long) Array.get(o, 776)).longValue()) == 999999999999l);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }

    /**
     *  
     */
    public void test_setShort_I() {
        try {
            Object o = Array.newInstance(short.class, 777);
            Array.set(o, 776, (Object) new Short(Short.MAX_VALUE));
            assertTrue(
                    "Error1",
                    (((Short) Array.get(o, 776)).shortValue()) == Short.MAX_VALUE);
        } catch (Exception e) {
            fail("Error2: " + e.toString());
        }
    }
}