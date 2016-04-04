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

package okio;

import com.google.j2objc.LibraryNotLinkedError;

import java.lang.reflect.Method;

/**
 * J2ObjC wrapper for java.security.MessageDigest, which avoids statically linking
 * the JRE security subset library unless actually used.
 */
class MessageDigest {
  Object messageDigest;

  MessageDigest(String algorithm) {
    try {
      Class<?> cls = Class.forName("java.security.MessageDigest");
      Method m = cls.getDeclaredMethod("getInstance", String.class);
      messageDigest = m.invoke(null, algorithm);
    } catch (ClassNotFoundException e) {
      throw new LibraryNotLinkedError("Security support", "jre_security",
          "JavaSecurityMessageDigest");
    } catch (Exception e) {
      // Only happens if the algorithm isn't supported, which isn't likely
      // since this method is only used by okio, so the algorithms are known.
      throw new AssertionError(e);
    }
  }

  public byte[] digest() {
    try {
      Method m = messageDigest.getClass().getMethod("digest");
      return (byte[]) m.invoke(messageDigest);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public byte[] digest(byte[] data) {
    try {
      Method m = messageDigest.getClass().getMethod("digest", new byte[0].getClass());
      return (byte[]) m.invoke(messageDigest, data);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public void update(byte[] input, int offset, int len) {
    try {
      Method m = messageDigest.getClass().getMethod("update",
          new byte[0].getClass(), Integer.TYPE, Integer.TYPE);
      m.invoke(messageDigest, input, offset, len);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  static byte[] digest(String algorithm, byte[] data) {
    return new MessageDigest(algorithm).digest(data);
  }
}
