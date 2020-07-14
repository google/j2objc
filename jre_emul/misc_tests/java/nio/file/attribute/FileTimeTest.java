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

package java.nio.file.attribute;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Test unit conversions of J2ObjC's FileTime implementations.
 *
 * @author Mary Qin
 */
public class FileTimeTest extends TestCase {

  public void testFrom() {
    assertEquals(new FileTime(1), FileTime.from(1000000, TimeUnit.NANOSECONDS));
    assertEquals(new FileTime(1), FileTime.from(1000, TimeUnit.MICROSECONDS));
    assertEquals(new FileTime(1), FileTime.from(1, TimeUnit.MILLISECONDS));
    assertEquals(new FileTime(86400000), FileTime.from(86400, TimeUnit.SECONDS));
    assertEquals(new FileTime(86400000), FileTime.from(1440, TimeUnit.MINUTES));
    assertEquals(new FileTime(86400000), FileTime.from(24, TimeUnit.HOURS));
    assertEquals(new FileTime(86400000), FileTime.from(1, TimeUnit.DAYS));
  }

  public void testTo() {
    FileTime ft = new FileTime(5);
    assertEquals(5000000, ft.to(TimeUnit.NANOSECONDS));
    assertEquals(5000, ft.to(TimeUnit.MICROSECONDS));
    assertEquals(5, ft.to(TimeUnit.MILLISECONDS));
    FileTime ft2 = new FileTime(172800000);
    assertEquals(172800, ft2.to(TimeUnit.SECONDS));
    assertEquals(2880, ft2.to(TimeUnit.MINUTES));
    assertEquals(48, ft2.to(TimeUnit.HOURS));
    assertEquals(2, ft2.to(TimeUnit.DAYS));
  }

  public void testToMillis() {
    FileTime ft = new FileTime(0);
    FileTime ft2 = new FileTime(100000);
    assertEquals(0, ft.toMillis());
    assertEquals(100000, ft2.toMillis());
  }

  public void testFromMillis() {
    assertEquals(new FileTime(0), FileTime.fromMillis(0));
    assertEquals(new FileTime(567), FileTime.fromMillis(567));
  }

  public void testCompare() {
    FileTime ft = new FileTime(1);
    assertTrue(ft.equals(new FileTime(1)));
    assertFalse(ft.equals(new FileTime(0)));
    assertEquals(0, ft.compareTo(new FileTime(1)));
    assertEquals(-1, ft.compareTo(new FileTime(0)));
    assertEquals(1, ft.compareTo(new FileTime(5)));
  }

}
