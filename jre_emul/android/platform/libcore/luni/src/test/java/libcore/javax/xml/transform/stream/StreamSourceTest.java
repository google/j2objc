/*
 * Copyright (C) 2022 The Android Open Source Project
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

package libcore.javax.xml.transform.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;

@RunWith(JUnit4.class)
public class StreamSourceTest {

  @Test
  public void constructor() {
    StreamSource ss = new StreamSource();
    assertNull(ss.getInputStream());
    assertNull(ss.getPublicId());
    assertNull(ss.getReader());
    assertNull(ss.getSystemId());
  }

  @Test
  public void constructorWithFile() throws IOException {
    final String PREFIX = "StreamSourceTest";
    File file = File.createTempFile(PREFIX, null);
    StreamSource ss = new StreamSource(file);
    assertNull(ss.getInputStream());
    assertNull(ss.getPublicId());
    assertNull(ss.getReader());
    assertTrue("SystemId is " + ss.getSystemId(), ss.getSystemId().contains(PREFIX));
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void constructorWithInputStreamAndSystemId() {
    final String SYSTEM_ID = "System 65";
    ByteArrayInputStream is = new ByteArrayInputStream(new byte[] { (byte) 0 });
    StreamSource ss = new StreamSource(is, SYSTEM_ID);
    assertEquals(is, ss.getInputStream());
    assertNull(ss.getPublicId());
    assertNull(ss.getReader());
    assertEquals(SYSTEM_ID, ss.getSystemId());
  }

  @Test
  public void constructorWithSystemId() {
    final String SYSTEM_ID = "System 76";
    StreamSource ss = new StreamSource(SYSTEM_ID);
    assertNull(ss.getInputStream());
    assertNull(ss.getPublicId());
    assertNull(ss.getReader());
    assertEquals(SYSTEM_ID, ss.getSystemId());
  }

  @Test
  public void constructorWithReader() {
    StringReader sr = new StringReader("House");
    StreamSource ss = new StreamSource(sr);
    assertNull(ss.getInputStream());
    assertNull(ss.getPublicId());
    assertEquals(sr, ss.getReader());
    assertNull(ss.getSystemId());
  }

  @Test
  public void constructorWithReaderAndSystemId() {
    final String SYSTEM_ID = "System 96";
    StringReader sr = new StringReader("House");
    StreamSource ss = new StreamSource(sr, SYSTEM_ID);
    assertNull(ss.getInputStream());
    assertNull(ss.getPublicId());
    assertEquals(sr, ss.getReader());
    assertEquals(SYSTEM_ID, ss.getSystemId());
  }

  @Test
  public void setInputStream() {
    StreamSource ss = new StreamSource();
    ByteArrayInputStream is = new ByteArrayInputStream(new byte[] {(byte) 0});
    ss.setInputStream(is);
    assertEquals(is, ss.getInputStream());
  }

  @Test
  public void setReader() {
    StreamSource ss = new StreamSource();
    StringReader sr = new StringReader("Thirteen-twenty-one");
    ss.setReader(sr);
    assertEquals(sr, ss.getReader());
  }

  @Test
  public void setPublicId() {
    final String PUBLIC_ID = "Thirteen-twenty-three";
    StreamSource ss = new StreamSource();
    ss.setPublicId(PUBLIC_ID);
    assertEquals(PUBLIC_ID, ss.getPublicId());
  }

  @Test
  public void setSystemId() {
    final String SYSTEM_ID = "Thirteen-twenty-four";
    StreamSource ss = new StreamSource();
    ss.setSystemId(SYSTEM_ID);
    assertEquals(SYSTEM_ID, ss.getSystemId());
  }

  @Test
  public void setSystemIdWithFile() throws IOException {
    final String PREFIX = "StreamSourceTest100";
    StreamSource ss = new StreamSource();
    File file = File.createTempFile(PREFIX, null);
    ss.setSystemId(file);
    assertTrue(ss.getSystemId().contains(PREFIX));
    if (file.exists()) {
      file.delete();
    }
  }
}
