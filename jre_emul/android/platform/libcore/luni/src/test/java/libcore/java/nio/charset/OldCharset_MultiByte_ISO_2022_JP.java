/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.java.nio.charset;

import java.nio.charset.CharacterCodingException;

public class OldCharset_MultiByte_ISO_2022_JP extends OldCharset_AbstractTest {
  @Override protected void setUp() throws Exception {
    charsetName = "ISO-2022-JP";
    testChars = "東京 とうきょう トウキョウ Tokyo 123".toCharArray();
    testBytes = theseBytes(0x1b, 0x24, 0x42, 0x45, 0x6c, 0x35, 0x7e, 0x1b,
                           0x28, 0x42, 0x20, 0x1b, 0x24, 0x42, 0x24, 0x48,
                           0x24, 0x26, 0x24, 0x2d, 0x24, 0x67, 0x24, 0x26,
                           0x1b, 0x28, 0x42, 0x20, 0x1b, 0x24, 0x42, 0x25,
                           0x48, 0x25, 0x26, 0x25, 0x2d, 0x25, 0x67, 0x25,
                           0x26, 0x1b, 0x28, 0x42, 0x20, 0x54, 0x6f, 0x6b,
                           0x79, 0x6f, 0x20, 0x31, 0x32, 0x33);
    super.setUp();
  }
}
