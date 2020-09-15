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

package libcore.java.io;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SmallTests extends TestSuite {

  private static final Class<?>[] smallTestClasses = new Class[] {
    CharArrayWriterTest.class,
    DataOutputStreamTest.class,
    FileDescriptorTest.class,
    FileInputStreamTest.class,
    FileOutputStreamTest.class,
    FileTest.class,
    FilterInputStreamNullSourceTest.class,
    InputStreamReaderTest.class,
    ObjectOutputStreamTest.class,
    OldAndroidBufferedInputStreamTest.class,
    OldAndroidBufferedOutputStreamTest.class,
    OldAndroidBufferedReaderTest.class,
    OldAndroidBufferedWriterTest.class,
    OldAndroidByteArrayInputStreamTest.class,
    OldAndroidByteArrayOutputStreamTest.class,
    OldAndroidCharArrayReaderTest.class,
    OldAndroidDataInputStreamTest.class,
    OldAndroidDataOutputStreamTest.class,
    OldAndroidFileTest.class,
    OldAndroidInputStreamReaderTest.class,
    OldAndroidLineNumberReaderTest.class,
    OldAndroidOutputStreamWriterTest.class,
    OldAndroidPipedStreamTest.class,
    OldAndroidPrintWriterTest.class,
    OldAndroidPushbackInputStreamTest.class,
    OldAndroidPushbackReaderTest.class,
    OldAndroidSerializationTest.class,
    OldAndroidStreamTokenizerTest.class,
    OldAndroidStringReaderTest.class,
    OldAndroidStringWriterTest.class,
    OldBufferedInputStreamTest.class,
    OldBufferedOutputStreamTest.class,
    OldBufferedReaderTest.class,
    OldBufferedWriterTest.class,
    OldByteArrayInputStreamTest.class,
    OldByteArrayOutputStreamTest.class,
    OldCharArrayReaderTest.class,
    OldCharArrayWriterTest.class,
    OldDataInputOutputStreamTest.class,
    OldDataInputStreamTest.class,
    OldDataOutputStreamTest.class,
    OldFileReaderTest.class,
    OldFileTest.class,
    OldFileWriterTest.class,
    OldFilterInputStreamTest.class,
    OldFilterOutputStreamTest.class,
    OldFilterReaderTest.class,
    OldFilterWriterTest.class,
    OldInputStreamReaderTest.class,
    OldInputStreamTest.class,
    OldLineNumberInputStreamTest.class,
    OldLineNumberReaderTest.class,
    OldObjectInputOutputStreamTest.class,
    OldObjectInputStreamGetFieldTest.class,
    OldObjectOutputStreamTest.class,
    OldObjectStreamFieldTest.class,
    OldOutputStreamTest.class,
    OldOutputStreamWriterTest.class,
    OldPipedOutputStreamTest.class,
    OldPipedWriterTest.class,
    OldPushbackInputStreamTest.class,
    OldPushbackReaderTest.class,
    OldRandomAccessFileTest.class,
    OldReaderTest.class,
    OldSequenceInputStreamTest.class,
    OldStreamTokenizerTest.class,
    OldStringBufferInputStreamTest.class,
    OldStringReaderTest.class,
    OldStringWriterTest.class,
    OldWriterTest.class,
    OutputStreamWriterTest.class,
    StreamTokenizerTest.class
  };

  public static Test suite() {
    return new TestSuite(smallTestClasses);
  }
}
