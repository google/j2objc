/*
 * Copyright (C) 2012 The Android Open Source Project
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

package libcore.java.lang.reflect;

import java.lang.reflect.Array;
import junit.framework.TestCase;

public class ArrayTest extends TestCase {
  private static boolean[] booleans;
  private static byte[] bytes;
  private static char[] chars;
  private static double[] doubles;
  private static float[] floats;
  private static int[] ints;
  private static long[] longs;
  private static short[] shorts;

  @Override protected void setUp() throws Exception {
    super.setUp();
    booleans = new boolean[] { true };
    bytes = new byte[] { (byte) 0xff };
    chars = new char[] { '\uffff' };
    doubles = new double[] { (double) 0xffffffffffffffffL };
    floats = new float[] { (float) 0xffffffff };
    ints = new int[] { 0xffffffff };
    longs = new long[] { 0xffffffffffffffffL };
    shorts = new short[] { (short) 0xffff };
  }

  public void testGetBoolean() throws Exception {
    assertEquals(booleans[0], Array.getBoolean(booleans, 0));
    try { Array.getBoolean(bytes, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(chars, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(floats, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(ints, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(longs, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(shorts, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getBoolean(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetByte() throws Exception {
    try { Array.getByte(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals(bytes[0], Array.getByte(bytes, 0));
    try { Array.getByte(chars, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getByte(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getByte(floats, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getByte(ints, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getByte(longs, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getByte(shorts, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getByte(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetChar() throws Exception {
    try { Array.getChar(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getChar(bytes, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals(chars[0], Array.getChar(chars, 0));
    try { Array.getChar(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getChar(floats, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getChar(ints, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getChar(longs, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getChar(shorts, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getChar(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetDouble() throws Exception {
    try { Array.getDouble(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((double) bytes[0], Array.getDouble(bytes, 0));
    assertEquals((double) chars[0], Array.getDouble(chars, 0));
    assertEquals(doubles[0], Array.getDouble(doubles, 0));
    assertEquals((double) floats[0], Array.getDouble(floats, 0));
    assertEquals((double) ints[0], Array.getDouble(ints, 0));
    assertEquals((double) longs[0], Array.getDouble(longs, 0));
    assertEquals((double) shorts[0], Array.getDouble(shorts, 0));
    try { Array.getDouble(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetFloat() throws Exception {
    try { Array.getFloat(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((float) bytes[0], Array.getFloat(bytes, 0));
    assertEquals((float) chars[0], Array.getFloat(chars, 0));
    assertEquals(floats[0], Array.getFloat(floats, 0));
    try { Array.getFloat(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((float) ints[0], Array.getFloat(ints, 0));
    assertEquals((float) longs[0], Array.getFloat(longs, 0));
    assertEquals((float) shorts[0], Array.getFloat(shorts, 0));
    try { Array.getFloat(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetInt() throws Exception {
    try { Array.getInt(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((int) bytes[0], Array.getInt(bytes, 0));
    assertEquals((int) chars[0], Array.getInt(chars, 0));
    try { Array.getInt(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getInt(floats, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals(ints[0], Array.getInt(ints, 0));
    try { Array.getInt(longs, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((int) shorts[0], Array.getInt(shorts, 0));
    try { Array.getInt(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetLong() throws Exception {
    try { Array.getLong(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((long) bytes[0], Array.getLong(bytes, 0));
    assertEquals((long) chars[0], Array.getLong(chars, 0));
    try { Array.getLong(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getLong(floats, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((long) ints[0], Array.getLong(ints, 0));
    assertEquals(longs[0], Array.getLong(longs, 0));
    assertEquals((long) shorts[0], Array.getLong(shorts, 0));
    try { Array.getLong(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testGetShort() throws Exception {
    try { Array.getShort(booleans, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals((int) bytes[0], Array.getShort(bytes, 0));
    try { Array.getShort(chars, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getShort(doubles, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getShort(floats, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getShort(ints, 0); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.getShort(longs, 0); fail(); } catch (IllegalArgumentException expected) {}
    assertEquals(shorts[0], Array.getShort(shorts, 0));
    try { Array.getShort(null, 0); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetBoolean() throws Exception {
    Array.setBoolean(booleans, 0, booleans[0]);
    try { Array.setBoolean(bytes, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(chars, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(doubles, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(floats, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(ints, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(longs, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(shorts, 0, true); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setBoolean(null, 0, true); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetByte() throws Exception {
    try { Array.setByte(booleans, 0, bytes[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setByte(bytes, 0, bytes[0]);
    try { Array.setByte(chars, 0, bytes[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setByte(doubles, 0, bytes[0]);
    Array.setByte(floats, 0, bytes[0]);
    Array.setByte(ints, 0, bytes[0]);
    Array.setByte(longs, 0, bytes[0]);
    Array.setByte(shorts, 0, bytes[0]);
    try { Array.setByte(null, 0, bytes[0]); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetChar() throws Exception {
    try { Array.setChar(booleans, 0, chars[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setChar(bytes, 0, chars[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setChar(chars, 0, chars[0]);
    Array.setChar(doubles, 0, chars[0]);
    Array.setChar(floats, 0, chars[0]);
    Array.setChar(ints, 0, chars[0]);
    Array.setChar(longs, 0, chars[0]);
    try { Array.setChar(shorts, 0, chars[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setChar(null, 0, chars[0]); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetDouble() throws Exception {
    try { Array.setDouble(booleans, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setDouble(bytes, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setDouble(chars, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setDouble(doubles, 0, doubles[0]);
    try { Array.setDouble(floats, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setDouble(ints, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setDouble(longs, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setDouble(shorts, 0, doubles[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setDouble(null, 0, doubles[0]); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetFloat() throws Exception {
    try { Array.setFloat(booleans, 0, floats[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setFloat(bytes, 0, floats[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setFloat(chars, 0, floats[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setFloat(floats, 0, floats[0]);
    Array.setFloat(doubles, 0, floats[0]);
    try { Array.setFloat(ints, 0, floats[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setFloat(longs, 0, floats[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setFloat(shorts, 0, floats[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setFloat(null, 0, floats[0]); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetInt() throws Exception {
    try { Array.setInt(booleans, 0, ints[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setInt(bytes, 0, ints[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setInt(chars, 0, ints[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setInt(doubles, 0, ints[0]);
    Array.setInt(floats, 0, ints[0]);
    Array.setInt(ints, 0, ints[0]);
    Array.setInt(longs, 0, ints[0]);
    try { Array.setInt(shorts, 0, ints[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setInt(null, 0, ints[0]); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetLong() throws Exception {
    try { Array.setLong(booleans, 0, longs[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setLong(bytes, 0, longs[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setLong(chars, 0, longs[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setLong(doubles, 0, longs[0]);
    Array.setLong(floats, 0, longs[0]);
    try { Array.setLong(ints, 0, longs[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setLong(longs, 0, longs[0]);
    try { Array.setLong(shorts, 0, longs[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setLong(null, 0, longs[0]); fail(); } catch (NullPointerException expected) {}
  }

  public void testSetShort() throws Exception {
    try { Array.setShort(booleans, 0, shorts[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setShort(bytes, 0, shorts[0]); fail(); } catch (IllegalArgumentException expected) {}
    try { Array.setShort(chars, 0, shorts[0]); fail(); } catch (IllegalArgumentException expected) {}
    Array.setShort(doubles, 0, shorts[0]);
    Array.setShort(floats, 0, shorts[0]);
    Array.setShort(ints, 0, shorts[0]);
    Array.setShort(longs, 0, shorts[0]);
    Array.setShort(shorts, 0, shorts[0]);
    try { Array.setShort(null, 0, shorts[0]); fail(); } catch (NullPointerException expected) {}
  }
}
