/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.gen;

/**
 * Location in a java source file: filename and line number.
 *
 * @author Viêt Hoà Dinh
 */
public class SourcePosition {
  private String filename = null;
  private int lineNumber = -1;
  private String source = null;

  public SourcePosition(String filename, int lineNumber, String source) {
    this.filename = filename;
    this.lineNumber = lineNumber;
    this.source = source;
  }

  public String getFilename() {
    return filename;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getSource() {
    return source;
  }
}
