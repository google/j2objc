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

/**
 * Stub implementation of java.io.ObjectInputStream.
 *
 * @see java.lang.Object
 */
public class ObjectInputStream {
  public final Object readObject() throws IOException, ClassNotFoundException {
    return null;
  }

  public Object readUnshared() throws IOException, ClassNotFoundException {
    return null;
  }
  
  public void defaultReadObject() throws IOException, ClassNotFoundException {}

  public int read() throws IOException {
    return 0;
  }

  public int read(byte[] buffer, int offset, int length) throws IOException {
    return 0;
  }

  public int available() throws IOException {
    return 0;
  }

  public void close() throws IOException {}

  public boolean readBoolean() throws IOException {
    return false;
  }

  public byte readByte() throws IOException {
    return 0;
  }

  public int readUnsignedByte() throws IOException {
    return 0;
  }
  
  public char readChar() throws IOException {
    return 0;
  }

  public short readShort() throws IOException {
    return 0;
  }

  public int readUnsignedShort() throws IOException {
    return 0;
  }

  public int readInt() throws IOException {
    return 0;
  }

  public long readLong() throws IOException {
    return 0L;
  }

  public float readFloat() throws IOException {
    return 0.0f;
  }

  public double readDouble() throws IOException {
    return 0.0;
  }

  public void readFully(byte[] buffer) throws IOException {}

  public void readFully(byte[] buffer, int offset, int length) throws IOException {}

  public int skipBytes(int n) throws IOException {
    return 0;
  }

  public String readLine() throws IOException {
    return null;
  }

  public String readUTF() throws IOException {
    return null;
  }
  
  /* Not implemented
  public void registerValidation(ObjectInputValidation, int)
  */
}
