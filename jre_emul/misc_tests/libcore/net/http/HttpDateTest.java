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

package libcore.net.http;

import static org.junit.Assert.assertEquals;

import java.net.HttpCookie;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test additional date format strings added to HttpDate for compatibility with existing apps. */
@RunWith(JUnit4.class)
public final class HttpDateTest {

  private static final String TEST_DATE_STRING = "Sun, 05-Nov-2023 08:07:24 GMT";

  @Test
  public void testParseDate() throws ParseException {
    // Create a HttpCookie instance with a "Sun, 05-Nov-2023 08:07:24 GMT" date string.
    HttpCookie cookie = new HttpCookie("cookie_name", "cookie_value");
    cookie.setMaxAge(1699171644L * 1000);

    // Parse the date string into a Date object.
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
    Date parsedDate = dateFormat.parse(TEST_DATE_STRING);

    // Verify that the parsed date matches the date in the HttpCookie instance.
    assertEquals(parsedDate.getTime(), cookie.getMaxAge());

    // Parse with HttpDate, verify date matches HttpCookie instance's date.
    Date httpParsedDate = HttpDate.parse(TEST_DATE_STRING);
    assertEquals(httpParsedDate.getTime(), cookie.getMaxAge());
  }
}
