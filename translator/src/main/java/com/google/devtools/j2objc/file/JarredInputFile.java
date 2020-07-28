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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * A file inside a .jar file.
 *
 * @author Mike Thvedt
 */
public class JarredInputFile extends InputFile {
  private final String jarPath;
  private JarFile jarFile;

  private static HashMap<String, JarFile> ozJarCache = new HashMap<>();
  
  /**
   * Create a new JarredSourceFile. The file's unit name will be the same as
   * the given internal path.
   * @param jarPath a filesystem path to the containing .jar
   * @param internalPath the file's path within the jar
   */
  public JarredInputFile(String jarPath, String internalPath) {
    super(internalPath);
    assert !jarPath.endsWith(".java");
    this.jarPath = jarPath;
    this.jarFile = ozJarCache.get(jarPath);
    if (jarFile == null) {
      try {
        this.jarFile = new JarFile(jarPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    	ozJarCache.put(jarPath, jarFile);
    }
  }

  @Override
  public boolean exists() {
    return jarFile.getEntry(super.getUnitName()) != null;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    ZipEntry entry = jarFile.getEntry(super.getUnitName());
    final InputStream entryStream = jarFile.getInputStream(entry);
    return new InputStream() {

      @Override
      public int read() throws IOException {
        return entryStream.read();
      }

      @Override
      public int read(byte[] buffer) throws IOException {
        return entryStream.read(buffer);
      }

      @Override
      public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return entryStream.read(buffer, byteOffset, byteCount);
      }

      @Override
      public void close() throws IOException {
        entryStream.close();
      }
    };
  }

  @Override
  public Reader openReader(Charset charset) throws IOException {
    return new InputStreamReader(getInputStream(), charset);
  }

  @Override
  public String getAbsolutePath() {
    return jarPath;
  }

  @Override
  public String getOriginalLocation() {
    return "jar:file:" + jarPath + "!" + super.getUnitName();
  }

  @Override
  public long lastModified() {
    return new File(jarPath).lastModified();
  }

  @Override
  public String toString() {
    return getOriginalLocation();
  }
}
