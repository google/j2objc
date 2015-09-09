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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Version {

  /**
   * Returns the "version" entry from a jar file's manifest, if available.
   * If the class isn't in a jar file, or that jar file doesn't define a
   * "version" entry, then a "not available" string is returned.
   *
   * @param jarClass any class from the target jar
   */
  public static String jarVersion(Class<?> jarClass) {
    String path = jarClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    try (JarFile jar = new JarFile(URLDecoder.decode(path, "UTF-8"))) {
      Manifest manifest = jar.getManifest();
      return manifest.getMainAttributes().getValue("version");
    } catch (IOException e) {
    }
    return "(version info not available)";
  }

  private Version() {}  // Don't instantiate.
}
