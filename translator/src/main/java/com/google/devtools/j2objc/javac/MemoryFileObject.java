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

package com.google.devtools.j2objc.javac;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

/**
 * A JavaFileObject implementation that stores its content in memory.
 * This implementation is just used for compiling source strings,
 * normally for tests, so any method not used by javac when compiling
 * them is unimplemented.
 */
class MemoryFileObject implements JavaFileObject {
  private final String path;
  private final Kind kind;
  private final String source;
  private long dateLastModified;

  public static JavaFileObject createJavaFile(String path, String source) {
    return new MemoryFileObject(path, JavaFileObject.Kind.SOURCE, source);
  }

  MemoryFileObject(String path, Kind kind, String source) {
    this.path = path;
    this.kind = kind;
    this.source = source;
    timeStamp();
  }

  @Override
  public URI toUri() {
    try {
      return new URI("mem", null, "/" + path, null);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public String getName() {
    return path;
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new StringReader(source);
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return source;
  }

  @Override
  public long getLastModified() {
    return dateLastModified;
  }

  @Override
  public boolean delete() {
    return false;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    String baseName = simpleName + kind.extension;
    return kind.equals(getKind())
        && (baseName.equals(path) || path.endsWith("/" + baseName));
  }

  @Override
  public NestingKind getNestingKind() {
    return null;
  }

  @Override
  public Modifier getAccessLevel() {
    return null;
  }

  private void timeStamp() {
    dateLastModified = System.currentTimeMillis();
  }

  @Override
  public InputStream openInputStream() throws IOException {
    throw new java.lang.UnsupportedOperationException();
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    throw new java.lang.UnsupportedOperationException();
  }

  @Override
  public Writer openWriter() throws IOException {
    throw new java.lang.UnsupportedOperationException();
  }
}
