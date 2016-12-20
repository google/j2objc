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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A single file in the filesystem.
 *
 * @author Mike Thvedt
 */
public class RegularInputFile implements InputFile {
  private final String path;
  private final String unitPath;

  public RegularInputFile(String unitPath) {
    this(unitPath, unitPath);
  }

  public RegularInputFile(String fsPath, String unitPath) {
    this.path = fsPath;
    this.unitPath = unitPath;
  }

  @Override
  public boolean exists() {
    return new File(path).exists();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(new File(path));
  }

  @Override
  public Reader openReader(Charset charset) throws IOException {
    return new InputStreamReader(getInputStream(),  charset);
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public String getContainingPath() {
    return unitPath;
  }

  @Override
  public String getUnitName() {
    return unitPath;
  }

  @Override
  public String getBasename() {
    return unitPath.substring(unitPath.lastIndexOf(File.separatorChar) + 1);
  }

  @Override
  public long lastModified() {
    return new File(path).lastModified();
  }

  @Override
  public String toString() {
    return getPath();
  }
}
