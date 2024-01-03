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

package com.google.devtools.j2objc.util;

import static com.google.common.base.StandardSystemProperty.JAVA_SPECIFICATION_VERSION;

import com.google.common.primitives.Ints;
import java.lang.reflect.Method;
import java.util.Objects;

/** Java source versions, used by the -source and -target flags. */
public final class SourceVersion {
  public static final SourceVersion JAVA_15 = new SourceVersion(15, "15");
  public static final SourceVersion JAVA_14 = new SourceVersion(14, "14");
  public static final SourceVersion JAVA_13 = new SourceVersion(13, "13");
  public static final SourceVersion JAVA_12 = new SourceVersion(12, "12");
  public static final SourceVersion JAVA_11 = new SourceVersion(11, "11");
  public static final SourceVersion JAVA_10 = new SourceVersion(10, "10");
  public static final SourceVersion JAVA_9 = new SourceVersion(9, "9");
  public static final SourceVersion JAVA_8 = new SourceVersion(8, "1.8");
  public static final SourceVersion JAVA_7 = new SourceVersion(7, "1.7");
  public static final SourceVersion JAVA_6 = new SourceVersion(6, "1.6");
  public static final SourceVersion JAVA_5 = new SourceVersion(5, "1.5");

  // Max version supported by translator and runtime.
  private static SourceVersion maxSupportedVersion = JAVA_15;


  private final int version;
  private final String flag;

  private SourceVersion(int version, String flag) {
    this.flag = flag;
    this.version = version;
  }

  public int version() {
    return version;
  }

  public String flag() {
    return flag;
  }

  public static SourceVersion parse(String flag) {
    String fullFlag = flag.length() == 1 ? "1." + flag : flag;
    String shortFlag = flag.startsWith("1.") ? flag.substring("1.".length()) : flag;
    Integer version = Ints.tryParse(shortFlag);
    if (version == null) {
      throw new IllegalArgumentException(flag);
    }
    return new SourceVersion(version, fullFlag);
  }

  public static SourceVersion valueOf(int majorVersion) {
    return new SourceVersion(majorVersion, (majorVersion < 9 ? "1." : "") + majorVersion);
  }

  public static SourceVersion getMaxSupportedVersion() {
    return maxSupportedVersion;
  }

  public static void setMaxSupportedVersion(SourceVersion sourceVersion) {
    maxSupportedVersion = sourceVersion;
  }

  /**
   * Returns the source version value associated with the runtime currently running.
   */
  public static SourceVersion defaultVersion() {
    SourceVersion sourceVersion;
    try {
      Method versionMethod = Runtime.class.getMethod("version");
      Object version = versionMethod.invoke(null);
      int majorVersion = (int) version.getClass().getMethod("major").invoke(version);
      sourceVersion = SourceVersion.valueOf(majorVersion);
    } catch (Exception e) {
      sourceVersion = SourceVersion.parse(JAVA_SPECIFICATION_VERSION.value());
    }
    return sourceVersion.version > maxSupportedVersion.version
        ? maxSupportedVersion
        : sourceVersion;
  }

  public static boolean executingOnSupportedVersion() {
    SourceVersion runningVersion = SourceVersion.parse(JAVA_SPECIFICATION_VERSION.value());
    return runningVersion.version <= maxSupportedVersion.version;
  }

  @Override
  public String toString() {
    return flag;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SourceVersion)) {
      return false;
    }
    SourceVersion that = (SourceVersion) o;
    return this.version == that.version && this.flag.equals(that.flag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, flag);
  }
}
