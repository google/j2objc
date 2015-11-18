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

package libcore.icu;

import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Unit tests for {@link libcore.icu.NativeDecimalFormat}.
 */
public class NativeDecimalFormatTest extends TestCase {

  // Verify formatting a date with a format string with zeros.
  public void testZeroHour() throws Exception {
    DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    String timeString = "00:00:23";
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    // Issue found a ParseException was thrown here.
    Date date = sdf.parse(timeString);
    // Only check seconds, as hour and date changes depending on timezone.
    assertTrue(date.toString().contains(":23"));
  }
}
