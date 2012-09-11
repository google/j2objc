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

package java.lang;

import java.io.UnsupportedEncodingException;

/**
 * Stub implementation of java.lang.String.
 *
 * @see java.lang.Object
 */
public class String implements CharSequence, Comparable<String> {

  public static final java.util.Comparator CASE_INSENSITIVE_ORDER = null;

  public String() {}

  public String(byte[] bytes) {}

  public String(byte[] bytes, int i, int j) {}

  public String(byte[] bytes, int i, int j, String s) throws UnsupportedEncodingException {}

  public String(byte[] bytes, String s) throws UnsupportedEncodingException {}

  public String(char[] chars) {}

  public String(char[] chars, int i, int j) {}

  String(int i, int j, char[] chars) {}

  public String(String s) {}

  public String(StringBuffer sb) {}

  public String(StringBuilder sb) {}

  public static String copyValueOf(char[] chars, int i, int j) {
    return null;
  }

  public static String format(java.util.Locale l, String s, Object... objs) {
    return null;
  }

  public static String format(String s, Object... objs) {
    return null;
  }

  public static String valueOf(boolean b) {
    return null;
  }

  public static String valueOf(char c) {
    return null;
  }

  public static String valueOf(char[] chars) {
    return null;
  }

  public static String valueOf(char[] chars, int i, int j) {
    return null;
  }

  public static String valueOf(double d) {
    return null;
  }

  public static String valueOf(float f) {
    return null;
  }

  public static String valueOf(int i) {
    return null;
  }

  public static String valueOf(long l) {
    return null;
  }

  public static String valueOf(Object o) {
    return null;
  }

  public char charAt(int i) {
    return 0;
  }

  public int codePointAt(int i) {
    return 0;
  }

  public int codePointBefore(int i) {
    return 0;
  }

  public int codePointCount(int i, int j) {
    return 0;
  }

  public int compareTo(String s) {
    return 0;
  }

  public int compareToIgnoreCase(String s) {
    return 0;
  }

  public String concat(String s) {
    return null;
  }

  public boolean contains(CharSequence cs) {
    return false;
  }

  public boolean endsWith(String s) {
    return false;
  }

  public boolean equalsIgnoreCase(String s) {
    return false;
  }

  public byte[] getBytes() {
    return null;
  }

  public byte[] getBytes(String s) throws UnsupportedEncodingException {
    return null;
  }

  public void getChars(int i, int j, char[] chars, int k) {}

  public int indexOf(int i) {
    return 0;
  }

  public int indexOf(int i, int j) {
    return 0;
  }

  public int indexOf(String s) {
    return 0;
  }

  public int indexOf(String s, int i) {
    return 0;
  }

  public String intern() {
    return null;
  }

  public boolean isEmpty() {
    return false;
  }

  public int lastIndexOf(int i) {
    return 0;
  }

  public int lastIndexOf(int i, int j) {
    return 0;
  }

  public int lastIndexOf(String s) {
    return 0;
  }

  public int lastIndexOf(String s, int i) {
    return 0;
  }

  public int length() {
    return 0;
  }

  public boolean matches(String s) {
    return false;
  }

  public boolean regionMatches(boolean b, int i, String s, int j, int k) {
    return false;
  }

  public boolean regionMatches(int i, String s, int j, int k) {
    return false;
  }

  public String replace(char c1, char c2) {
    return null;
  }

  public String replace(CharSequence cs1, CharSequence cs2) {
    return null;
  }

  public String replaceAll(String s1, String s2) {
    return null;
  }

  public String replaceFirst(String s1, String s2) {
    return null;
  }

  public String[] split(String s) {
    return null;
  }

  public String[] split(String s, int i) {
    return null;
  }

  public boolean startsWith(String s) {
    return false;
  }

  public boolean startsWith(String s, int i) {
    return false;
  }

  public CharSequence subSequence(int i, int j) {
    return null;
  }

  public String substring(int i) {
    return null;
  }

  public String substring(int i, int j) {
    return null;
  }

  public char[] toCharArray() {
    return null;
  }

  public String toLowerCase() {
    return null;
  }

  public String toLowerCase(java.util.Locale l) {
    return null;
  }

  public String toUpperCase() {
    return null;
  }

  public String toUpperCase(java.util.Locale l) {
    return null;
  }

  public String trim() {
    return null;
  }

  /* Unimplemented/mapped methods.
  String(byte[] bytes, int i)
  String(byte[] bytes, int i, int j, int k)
  String(byte[] bytes, int i, int j, java.nio.charset.Charset charset)
  String(byte[] bytes, java.nio.charset.Charset charset)
  String(int[] ints, int i, int j)
  String copyValueOf(char[] chars)
  boolean contentEquals(CharSequence cs)
  boolean contentEquals(StringBuffer sb)
  getBytes(int i, int j, byte[] bytes, int k)
  byte[] getBytes(java.nio.charset.Charset charset)
  int offsetByCodePoints(int i, int j)
  */
}
