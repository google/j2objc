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

package com.google.j2objc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 * Utility test support methods to check for minimum OS and SDK versions.
 */
public class EnvironmentUtil extends TestCase {

  private static final Pattern VERSION_REGEX = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+))?");

  /**
   * Returns true if test is executing on macOS.
   */
  public static boolean onMacOSX() {
    return System.getProperty("os.name").equals("Mac OS X");
  }

  /**
   * Returns true if test is executing on iPhone device.
   */
  public static boolean onIPhone() {
    return System.getProperty("os.name").equals("iPhone");
  }

  /**
   * Returns true if test is executing on macOS.
   */
  public static boolean onIPhoneSimulator() {
    return System.getProperty("os.name").equals("iPhone Simulator");
  }

  /**
   * Returns the version of the operating system being run.
   */
  public static String osVersion() {
    return System.getProperty("os.version");
  }

  /**
   * Returns true if test is running on a minimum OS version.
   * Specified version strings must be of the form "n.n[.n]", such as
   * "10.12" or "9.3.1".
   */
  public static boolean onMinimumOSVersion(String minimum) {
    return compareVersions(minimum, osVersion()) >= 0;
  }

  private static int compareVersions(String v1, String v2) {
    int[] version1 = parseVersion(v1, 0);
    int[] version2 = parseVersion(v2, Integer.MAX_VALUE);
    for (int i = 0; i < 3; i++) {
      if (version1[i] != version2[i]) {
        return version1[i] < version2[i] ? 1 : -1;
      }
    }
    return 0;
  }

  private static int[] parseVersion(String version, int defaultRevision) {
    Matcher m = VERSION_REGEX.matcher(version);
    if (!m.matches()) {
      throw new IllegalArgumentException("invalid version string: \"" + version + "\"");
    }
    String revision = m.group(4);
    return new int[] {
        Integer.parseInt(m.group(1)),  // major version number
        Integer.parseInt(m.group(2)),  // minor version number
        revision == null ? defaultRevision : Integer.parseInt(revision),
    };
  }

  // Unit test for above.
  public void testCompareVersions() {
    assertEquals(0, compareVersions("1.2.3", "1.2.3"));
    assertEquals(1, compareVersions("4.5", "4.5.6"));
    assertEquals(1, compareVersions("7.8.9", "7.8.10"));
    assertEquals(1, compareVersions("7.8.9", "7.9.3"));
    assertEquals(-1, compareVersions("6.5.4", "6.5.3"));
    assertEquals(-1, compareVersions("6.5.4", "6.4.2"));
 }

}
