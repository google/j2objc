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
 * Stub implementation of java.io.ObjectOutputStream.
 *
 * @see java.lang.Object
 */
public class ObjectOutputStream extends OutputStream {
  public void useProtocolVersion(int version) throws IOException {}
  public void writeObject(Object o) throws IOException {}
  public void writeUnshared(Object o) throws IOException {}
  public void defaultWriteObject() throws IOException {}
  public void writeFields() throws IOException {}
  public void reset() throws IOException {}
  public void write(int i) throws IOException {}
  public void write(byte[] buf) throws IOException {}
  public void write(byte[] buf, int offset, int length) throws IOException {}
  public void flush() throws IOException {}
  public void close() throws IOException {}
  public void writeBoolean(boolean b) throws IOException {}
  public void writeByte(int b) throws IOException {}
  public void writeShort(int s) throws IOException {}
  public void writeChar(int c) throws IOException {}
  public void writeInt(int i) throws IOException {}
  public void writeLong(long l) throws IOException {}
  public void writeFloat(float f) throws IOException {}
  public void writeDouble(double d) throws IOException {}
  public void writeBytes(String s) throws IOException {}
  public void writeChars(String s) throws IOException {}
  public void writeUTF(String s) throws IOException {}
}
