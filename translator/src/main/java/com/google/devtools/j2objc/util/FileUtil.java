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

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Utility methods for reading from various file types.
 *
 * @author Tom Ball, Keith Stanger
 */
public class FileUtil {

  /**
   * Returns the file content as a string, either from a file or jar file entry.
   *
   * @param url the file path (optionally prefixed with "file:") or jar URL.
   */
  public static String readSource(String url) throws IOException {
    if (url.startsWith("jar:")) {
      // Use JarURLConnection to parse the jar URL correctly.
      JarFile jarFile = getJarFile(url);
      try {
        ZipEntry entry = jarFile.getEntry(getJarEntryPath(url));
        Reader in = new InputStreamReader(jarFile.getInputStream(entry));
        String source = CharStreams.toString(in);
        in.close();
        return source;
      } finally {
        jarFile.close();
      }
    } else {
      // Skip file URL prefix, if it exists.
      if (url.startsWith("file:")) {
        url = url.substring(5);
      }
      return Files.toString(new File(url), Options.getCharset());
    }
  }

  private static JarFile getJarFile(String url) throws IOException {
    int pathStart = url.lastIndexOf(':') + 1;
    int pathEnd = url.indexOf('!');
    String jarPath = URLDecoder.decode(url.substring(pathStart, pathEnd), "UTF-8");
    return new JarFile(jarPath);
  }

  private static String getJarEntryPath(String url) {
    int iBang = url.indexOf('!');
    String path = url.substring(iBang + 1);
    // Strip leading path separator, if present.
    return path.startsWith("/") ? path.substring(1) : path;
  }

  /**
   * Returns true if the file or jar entry specified exists.
   */
  public static boolean exists(String url) {
    if (url.startsWith("jar:")) {
      try {
        JarFile jarFile = getJarFile(url);
        ZipEntry entry = jarFile.getEntry(getJarEntryPath(url));
        return entry != null;
      } catch (IOException e) {
        return false;
      }
    } else {
      return new File(url).exists();
    }
  }

  private static InputStream streamForFile(String filename) throws IOException {
    File f = new File(filename);
    if (f.exists()) {
      return new FileInputStream(f);
    } else {
      InputStream stream = J2ObjC.class.getResourceAsStream(filename);
      if (stream == null) {
        throw new FileNotFoundException(filename);
      }
      return stream;
    }
  }

  /**
   * Reads the given properties file.
   */
  public static Properties loadProperties(String resourceName) throws IOException {
    return loadProperties(streamForFile(resourceName));
  }

  public static Properties loadProperties(InputStream in) throws IOException {
    try {
      Properties p = new Properties();
      p.load(in);
      return p;
    } finally {
      in.close();
    }
  }

  private FileUtil() {}
}
