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

/**
 * Stub implementation of AbstractStringBuilder.
 *
 * @see Object
 */
abstract class AbstractStringBuilder implements Appendable, CharSequence {

  char[] value;
  int count;

  AbstractStringBuilder() {}
  AbstractStringBuilder(int i) {}

  public int length() {
    return 0;
  }

  public int capacity() {
    return 0;
  }

  public void ensureCapacity(int i) {}

  void expandCapacity(int i) {}

  public void trimToSize() {}

  public void setLength(int i) {}

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

  public int offsetByCodePoints(int i, int j) {
    return 0;
  }

  public void getChars(int i, int j, char[] buf, int k) {}

  public void setCharAt(int i, char c) {}

  public AbstractStringBuilder append(Object o) {
    return null;
  }

  public AbstractStringBuilder append(String s) {
    return null;
  }

  public AbstractStringBuilder append(StringBuffer sb) {
    return null;
  }

  AbstractStringBuilder append(AbstractStringBuilder sb) {
    return null;
  }

  public AbstractStringBuilder append(CharSequence cs) {
    return null;
  }

  public AbstractStringBuilder append(CharSequence cs, int i, int j) {
    return null;
  }

  public AbstractStringBuilder append(char[] buf) {
    return null;
  }

  public AbstractStringBuilder append(char[] buf, int i, int j) {
    return null;
  }

  public AbstractStringBuilder append(boolean b) {
    return null;
  }

  public AbstractStringBuilder append(char c) {
    return null;
  }

  public AbstractStringBuilder append(int i) {
    return null;
  }

  public AbstractStringBuilder append(long l) {
    return null;
  }

  public AbstractStringBuilder append(float f) {
    return null;
  }

  public AbstractStringBuilder append(double d) {
    return null;
  }

  public AbstractStringBuilder delete(int i, int j) {
    return null;
  }

  public AbstractStringBuilder appendCodePoint(int i) {
    return null;
  }

  public AbstractStringBuilder deleteCharAt(int i) {
    return null;
  }

  public AbstractStringBuilder replace(int i, int j, String s) {
    return null;
  }

  public String substring(int i) {
    return null;
  }

  public CharSequence subSequence(int i, int j) {
    return null;
  }

  public String substring(int i, int j) {
    return null;
  }

  public AbstractStringBuilder insert(int i, char[] buf, int j, int k) {
    return null;
  }

  public AbstractStringBuilder insert(int i, Object o) {
    return null;
  }

  public AbstractStringBuilder insert(int i, String s) {
    return null;
  }

  public AbstractStringBuilder insert(int i, char[] buf) {
    return null;
  }

  public AbstractStringBuilder insert(int i, CharSequence cs) {
    return null;
  }

  public AbstractStringBuilder insert(int i, CharSequence cs, int j, int k) {
    return null;
  }

  public AbstractStringBuilder insert(int i, boolean b) {
    return null;
  }

  public AbstractStringBuilder insert(int i, char c) {
    return null;
  }

  public AbstractStringBuilder insert(int i, int j) {
    return null;
  }

  public AbstractStringBuilder insert(int i, long l) {
    return null;
  }

  public AbstractStringBuilder insert(int i, float f) {
    return null;
  }

  public AbstractStringBuilder insert(int i, double d) {
    return null;
  }

  public int indexOf(String s) {
    return 0;
  }

  public int indexOf(String s, int i) {
    return 0;
  }

  public int lastIndexOf(String s) {
    return 0;
  }

  public int lastIndexOf(String s, int i) {
    return 0;
  }

  public AbstractStringBuilder reverse() {
    return null;
  }

  public abstract String toString();

  final char[] getValue() {
    return null;
  }
}
