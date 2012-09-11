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
 * Stub implementation of java.lang.Object.  This class is not translated,
 * but is only included in jre_emul.jar so that jar can be used for
 * verifying that the JRE emulation library supports all JRE references
 * in specified sources.  The easiest way to do this is to run:
 *
 * javac -bootclasspath <path>/jre_emul.jar -extdirs '' <sources>
 */
public class Object {

  public final Class<?> getClass() {
    return null;
  }

  public int hashCode() {
    return 0;
  }

  public boolean equals(Object obj) {
    return false;
  }

  protected Object clone() throws CloneNotSupportedException {
    return null;
  }

  public String toString() {
    return "";
  }

  protected void finalize() throws Throwable {}

  /* Untranslated methods.
  public final native void notify();
  public final native void notifyAll();
  public final native void wait(long timeout) throws InterruptedException;
  public final native void wait(long timeout, int nanos) throws InterruptedException;
  public final native void wait() throws InterruptedException;
  */
}
