/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util.zip;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SmallTests extends TestSuite {

  private static final Class<?>[] smallTestClasses = new Class[] {
    DeflaterInputStreamTest.class,
    DeflaterOutputStreamTest.class,
    DeflaterTest.class,
    GZIPInputStreamTest.class,
    GZIPOutputStreamTest.class,
    InflaterTest.class,
    OldAndroidChecksumTest.class,
    OldAndroidDeflateTest.class,
    OldAndroidGZIPStreamTest.class,
    OldAndroidZipStreamTest.class,
    OldDataFormatExceptionTest.class,
    ZipEntryTest.class,
    ZipFileTest.class,
    ZipInputStreamTest.class,
    ZipOutputStreamTest.class
  };

  public static Test suite() {
    return new TestSuite(smallTestClasses);
  }
}
