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

/**
 * Supported Java versions, used by the -source and -target flags.
 */
public enum SourceVersion {

  JAVA_8(8, "1.8"), JAVA_7(7, "1.7"), JAVA_6(6, "1.6"), JAVA_5(5, "1.5");

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

  public static boolean java7Minimum(SourceVersion sourceVersion) {
    return sourceVersion.version >= 7;
  }

  public static boolean java8Minimum(SourceVersion sourceVersion) {
    return sourceVersion.version >= 8;
  }

  @Override
  public String toString() {
    return flag;
  }
}
