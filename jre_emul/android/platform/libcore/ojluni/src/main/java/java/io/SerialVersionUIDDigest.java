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

package java.io;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Dynamically loaded implementation for computing serialVersionUID hashes. Public so that users can
 * add an explicit dependency to force load this class.
 */
public class SerialVersionUIDDigest implements ObjectStreamClass.Digest {

  private final MessageDigest digest;

  SerialVersionUIDDigest() {
    try {
      digest = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    }
  }

  public byte[] digest(byte[] input) {
    return digest.digest(input);
  }
}
