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

import java.lang.reflect.Method;

/**
 * Supported Java versions, used by the -source and -target flags.
 */
public enum SourceVersion {
  JAVA_22(22, "22"),
  JAVA_21(21, "21"),
  JAVA_20(20, "20"),
  JAVA_19(19, "19"),
  JAVA_18(18, "18"),
  JAVA_17(17, "17"),
  JAVA_16(16, "16"),
  JAVA_15(15, "15"),
  JAVA_14(14, "14"),
  JAVA_13(13, "13"),
  JAVA_12(12, "12"),
  JAVA_11(11, "11"),
  JAVA_10(10, "10"),
  JAVA_9(9, "9"),
  JAVA_8(8, "1.8"),
  JAVA_7(7, "1.7"),
  JAVA_6(6, "1.6"),
  JAVA_5(5, "1.5");

  // Max version supported by translator and runtime.
  private static SourceVersion maxSupportedVersion = JAVA_15;

  // Max version supported by this class. This allows work on
  // future versions to be done without exposing customers to
  // a partial implementation.
  private static final SourceVersion maxVersion = JAVA_17;

  private final int version;
  private final String flag;

  SourceVersion(int version, String flag) {
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
    for (SourceVersion sv : values()) {
      if (sv.flag.equals(fullFlag)) {
        return sv;
      }
    }
    throw new IllegalArgumentException(flag);
  }

  public static SourceVersion valueOf(int majorVersion) {
    for (SourceVersion sv : values()) {
      if (sv.version == majorVersion) {
        return sv;
      }
    }
    throw new IllegalArgumentException("Unsupported version: " + majorVersion);
  }

  public static SourceVersion getMaxSupportedVersion() {
    return maxSupportedVersion;
  }

  public static void setMaxSupportedVersion(SourceVersion sourceVersion) {
    maxSupportedVersion = sourceVersion;
  }

  public static SourceVersion getMaxVersion() {
    return maxVersion;
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
}
