/*
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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Verify String hash values match JRE computed hashes.
 */
public class StringHashTest extends TestCase {

  private static final String EMPTY = "";
  private static final String SHORT = "foo";
  private static final String MEDIUM =
      "The time has come, the Walrus said, to talk of many things.";
  private static final String LONG = "/*"
      + " * Licensed under the Apache License, Version 2.0 (the \"License\");"
      + " * you may not use this file except in compliance with the License."
      + " * You may obtain a copy of the License at"
      + " *"
      + " * http://www.apache.org/licenses/LICENSE-2.0"
      + " *"
      + " * Unless required by applicable law or agreed to in writing, software"
      + " * distributed under the License is distributed on an \"AS IS\" BASIS,"
      + " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
      + " * See the License for the specific language governing permissions and"
      + " * limitations under the License."
      + " */";

  /**
   * Verify String.hashCode() is mapped to NSString+JavaString's javaHash method.
   * NSString uses a different algorithm for calculating string hash values, so
   * only a few need to be checked to verify the category's hash method is used
   * instead of NSString's.
   */
  public void testJavaHashUsed() {
    assertEquals(0, EMPTY.hashCode());
    assertEquals(101574, SHORT.hashCode());
    assertEquals(897094948,
        MEDIUM.hashCode());
    assertEquals(1521439191, LONG.hashCode());
  }
}
