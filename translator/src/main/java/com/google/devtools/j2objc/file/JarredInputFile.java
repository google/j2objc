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
package com.google.devtools.j2objc.file;

import com.google.devtools.j2objc.Options;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * A file inside a .jar file.
 *
 * @author Mike Thvedt
 */
public class JarredInputFile implements InputFile {
  private final String jarPath;
  private final String internalPath;

  /**
   * Create a new JarredSourceFile. The file's unit name will be the same as
   * the given internal path.
   * @param jarPath a filesystem path to the containing .jar
   * @param internalPath the file's path within the jar
   */
  public JarredInputFile(String jarPath, String internalPath) {
    assert !jarPath.endsWith(".java");
    this.jarPath = jarPath;
    this.internalPath = internalPath;
  }

  @Override
  public boolean exists() throws IOException {
    JarFile jarFile = new JarFile(jarPath);
    ZipEntry entry = jarFile.getEntry(internalPath);
    return entry != null;
  }

  @Override
  public Reader openReader() throws IOException {
    JarFile jarFile = new JarFile(jarPath);
    ZipEntry entry = jarFile.getEntry(internalPath);
    return new InputStreamReader(jarFile.getInputStream(entry), Options.getCharset());
  }

  @Override
  public String getPath() {
    return "jar:file:" + jarPath + "!" + internalPath;
  }

  public String getContainingPath() {
    return jarPath;
  }

  @Override
  public String getUnitName() {
    return internalPath;
  }

  @Override
  public String getBasename() {
    return internalPath.substring(internalPath.lastIndexOf('/') + 1);
  }

  @Override
  public long lastModified() {
    return new File(jarPath).lastModified();
  }

  @Override
  public String toString() {
    return getPath();
  }
}
