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
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Adapter class for a file in J2ObjC.
 * These files are immutable, and don't consume any resources.
 *
 * @author Mike Thvedt
 */
public abstract class InputFile {

	private String unitPath;
  
  public InputFile(String unitPath) {
    this.unitPath = unitPath;
  }

  public abstract boolean exists() throws IOException;

  public abstract InputStream getInputStream() throws IOException;

  /**
   * Opens a new reader for this SourceFile.
   * The caller is responsible for closing the reader.
   */
  public abstract Reader openReader(Charset charset) throws IOException;

  /**
   * Gets a full path of a Java or JAR file. The path must be an actual file system path that can be
   * used to read the soure. For Java source files extracted from a JAR file, return the absolute
   * path of the extracted Java source file. For non-extracted JAR files, return the absolute path
   * of the JAR file.
   */
  public abstract String getAbsolutePath();

  /**
   * Gets the original location of a Java source file.
   * For Java source files directly from or extracted from a JAR file, return
   * "jar:file:path/to/originalJarFile.jar!internal/path/to/SourceFile.java".
   * Used in J2ObjC for progress messages, error messages, and output comments.
   */
  public abstract String getOriginalLocation();

  /**
   * The compilation unit name of this SourceFile.
   * The usual Java practice is that this looks something like
   * {@code "path/to/package/SourceFile.java"}, taken from
   * a path relative to some base directory.
   */
  public final String getUnitName() { return unitPath; }

  /**
   * Gets name of the file stripped of any path components.
   * For example, the basename of "package/path/SourceFile.java" is
   * "SourceFile.java".
   */
  public final String getBasename() {
    return unitPath.substring(unitPath.lastIndexOf('/') + 1);
  }

  public abstract long lastModified();
}
