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

package dalvik.system;

import junit.framework.TestCase;

/**
 * Test JNI behavior
 */
public final class JniTest extends TestCase {

    static {
        System.loadLibrary("javacoretests");
    }

    /** @return this argument of method */
    private native JniTest returnThis();
    /** @return class argument of method */
    private static native Class<JniTest> returnClass();

    private native Object returnObjectArgFrom16(int arg_no,
                                                Object o1,  Object o2,  Object o3,  Object o4,  Object o5,
                                                Object o6,  Object o7,  Object o8,  Object o9,  Object o10,
                                                Object o11, Object o12, Object o13, Object o14, Object o15,
                                                Object o16);
    private native boolean returnBooleanArgFrom16(int arg_no,
                                                  boolean o1,  boolean o2,  boolean o3,  boolean o4,  boolean o5,
                                                  boolean o6,  boolean o7,  boolean o8,  boolean o9,  boolean o10,
                                                  boolean o11, boolean o12, boolean o13, boolean o14, boolean o15,
                                                  boolean o16);
    private native char returnCharArgFrom16(int arg_no,
                                            char o1,  char o2,  char o3,  char o4,  char o5,
                                            char o6,  char o7,  char o8,  char o9,  char o10,
                                            char o11, char o12, char o13, char o14, char o15,
                                            char o16);
    private native byte returnByteArgFrom16(int arg_no,
                                            byte o1,  byte o2,  byte o3,  byte o4,  byte o5,
                                            byte o6,  byte o7,  byte o8,  byte o9,  byte o10,
                                            byte o11, byte o12, byte o13, byte o14, byte o15,
                                            byte o16);
    private native short returnShortArgFrom16(int arg_no,
                                              short o1,  short o2,  short o3,  short o4,  short o5,
                                              short o6,  short o7,  short o8,  short o9,  short o10,
                                              short o11, short o12, short o13, short o14, short o15,
                                              short o16);
    private native int returnIntArgFrom16(int arg_no,
                                          int o1,  int o2,  int o3,  int o4,  int o5,
                                          int o6,  int o7,  int o8,  int o9,  int o10,
                                          int o11, int o12, int o13, int o14, int o15,
                                          int o16);
    private native long returnLongArgFrom16(int arg_no,
                                            long o1,  long o2,  long o3,  long o4,  long o5,
                                            long o6,  long o7,  long o8,  long o9,  long o10,
                                            long o11, long o12, long o13, long o14, long o15,
                                            long o16);
    private native float returnFloatArgFrom16(int arg_no,
                                              float o1,  float o2,  float o3,  float o4,  float o5,
                                              float o6,  float o7,  float o8,  float o9,  float o10,
                                              float o11, float o12, float o13, float o14, float o15,
                                              float o16);
    private native double returnDoubleArgFrom16(int arg_no,
                                                double o1,  double o2,  double o3,  double o4,  double o5,
                                                double o6,  double o7,  double o8,  double o9,  double o10,
                                                double o11, double o12, double o13, double o14, double o15,
                                                double o16);

    /** Test cases for implicit this argument */
    public void testPassingThis() {
        assertEquals(this, returnThis());
    }

    /** Test cases for implicit class argument */
    public void testPassingClass() {
        assertEquals(JniTest.class, returnClass());
    }

    /** Test passing object references as arguments to a native method */
    public void testPassingObjectReferences() {
        final Object[] literals = {"Bradshaw", "Isherwood", "Oldknow", "Mallet",
                                   JniTest.class, null, Integer.valueOf(0)};
        final Object[] a  = new Object[16];
        // test selection from a list of object literals where the literals are all the same
        for(Object literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnObjectArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                         a[5], a[6], a[7], a[8], a[9], a[10],
                                                         a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnObjectArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                         a[5], a[6], a[7], a[8], a[9], a[10],
                                                         a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing booleans as arguments to a native method */
    public void testPassingBooleans() {
        final boolean[] literals = {true, false, false, true};
        final boolean[] a  = new boolean[16];
        // test selection from a list of object literals where the literals are all the same
        for(boolean literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnBooleanArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                          a[5], a[6], a[7], a[8], a[9], a[10],
                                                          a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnBooleanArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                          a[5], a[6], a[7], a[8], a[9], a[10],
                                                          a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing characters as arguments to a native method */
    public void testPassingChars() {
        final char[] literals = {Character.MAX_VALUE, Character.MIN_VALUE,
                                 Character.MAX_HIGH_SURROGATE, Character.MAX_LOW_SURROGATE,
                                 Character.MIN_HIGH_SURROGATE, Character.MIN_LOW_SURROGATE,
                                 'a', 'z', 'A', 'Z', '0', '9'};
        final char[] a  = new char[16];
        // test selection from a list of object literals where the literals are all the same
        for(char literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnCharArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                       a[5], a[6], a[7], a[8], a[9], a[10],
                                                       a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnCharArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                       a[5], a[6], a[7], a[8], a[9], a[10],
                                                       a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing bytes as arguments to a native method */
    public void testPassingBytes() {
        final byte[] literals = {Byte.MAX_VALUE, Byte.MIN_VALUE, 0, -1};
        final byte[] a  = new byte[16];
        // test selection from a list of object literals where the literals are all the same
        for(byte literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnByteArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                       a[5], a[6], a[7], a[8], a[9], a[10],
                                                       a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnByteArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                       a[5], a[6], a[7], a[8], a[9], a[10],
                                                       a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing shorts as arguments to a native method */
    public void testPassingShorts() {
        final short[] literals = {Byte.MAX_VALUE, Byte.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE, 0, -1};
        final short[] a  = new short[16];
        // test selection from a list of object literals where the literals are all the same
        for(short literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnShortArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                        a[5], a[6], a[7], a[8], a[9], a[10],
                                                        a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnShortArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                        a[5], a[6], a[7], a[8], a[9], a[10],
                                                        a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing ints as arguments to a native method */
    public void testPassingInts() {
        final int[] literals = {Byte.MAX_VALUE, Byte.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE,
                                Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1};
        final int[] a  = new int[16];
        // test selection from a list of object literals where the literals are all the same
        for(int literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnIntArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                      a[5], a[6], a[7], a[8], a[9], a[10],
                                                      a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnIntArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                      a[5], a[6], a[7], a[8], a[9], a[10],
                                                      a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing longs as arguments to a native method */
    public void testPassingLongs() {
        final long[] literals = {Byte.MAX_VALUE, Byte.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE,
                                 Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, 0, -1};
        final long[] a  = new long[16];
        // test selection from a list of object literals where the literals are all the same
        for(long literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnLongArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                       a[5], a[6], a[7], a[8], a[9], a[10],
                                                       a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnLongArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                       a[5], a[6], a[7], a[8], a[9], a[10],
                                                       a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing floats as arguments to a native method */
    public void testPassingFloats() {
        final float[] literals = {Byte.MAX_VALUE, Byte.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE,
                                  Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE,
                                  Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, Float.NaN,
                                  Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, (float)Math.E, (float)Math.PI, 0, -1};
        final float[] a  = new float[16];
        // test selection from a list of object literals where the literals are all the same
        for(float literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnFloatArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                        a[5], a[6], a[7], a[8], a[9], a[10],
                                                        a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnFloatArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                        a[5], a[6], a[7], a[8], a[9], a[10],
                                                        a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    /** Test passing doubles as arguments to a native method */
    public void testPassingDoubles() {
        final double[] literals = {Byte.MAX_VALUE, Byte.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE,
                                   Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE,
                                   Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, Float.NaN,
                                   Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                                   Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, Double.NaN,
                                   Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                                   Math.E, Math.PI, 0, -1};
        final double[] a  = new double[16];
        // test selection from a list of object literals where the literals are all the same
        for(double literal : literals) {
            for(int i = 0; i < 16; i++) {
                a[i] = literal;
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnDoubleArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                         a[5], a[6], a[7], a[8], a[9], a[10],
                                                         a[11], a[12], a[13], a[14], a[15]));
            }
        }
        // test selection from a list of object literals where the literals are shuffled
        for(int j = 0; j < literals.length; j++) {
            for(int i = 0; i < 16; i++) {
                a[i] = literals[(i + j) % literals.length];
            }
            for(int i = 0; i < 16; i++) {
                assertEquals(a[i], returnDoubleArgFrom16(i, a[0], a[1], a[2], a[3], a[4],
                                                         a[5], a[6], a[7], a[8], a[9], a[10],
                                                         a[11], a[12], a[13], a[14], a[15]));
            }
        }
    }

    private static native Class<?> envGetSuperclass(Class<?> clazz);

    public void testGetSuperclass() {
        assertEquals(Object.class, envGetSuperclass(String.class));
        assertEquals(null, envGetSuperclass(Object.class));
        assertEquals(null, envGetSuperclass(int.class));
        assertEquals(null, envGetSuperclass(Runnable.class));
    }
}
