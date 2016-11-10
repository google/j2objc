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

import com.google.devtools.j2objc.types.Types;

/**
 * The environment used during translation of a compilation unit.
 */
public class TranslationEnvironment {

  private final ElementUtil elementUtil;
  private final TypeUtil typeUtil;
  private final Types typeEnv;
  private final CaptureInfo captureInfo;
  private final NameTable nameTable;
  private final TranslationUtil translationUtil;

  public TranslationEnvironment(ParserEnvironment parserEnv) {
    elementUtil = new ElementUtil(parserEnv.elementUtilities());
    typeUtil = new TypeUtil(parserEnv, elementUtil);
    typeEnv = new Types(parserEnv, typeUtil);
    captureInfo = new CaptureInfo(typeEnv, typeUtil);
    nameTable = new NameTable(typeEnv, typeUtil, captureInfo);
    translationUtil = new TranslationUtil(typeEnv, nameTable);
  }

  public ElementUtil elementUtil() {
    return elementUtil;
  }

  public TypeUtil typeUtil() {
    return typeUtil;
  }

  public Types types() {
    return typeEnv;
  }

  public CaptureInfo captureInfo() {
    return captureInfo;
  }

  public NameTable nameTable() {
    return nameTable;
  }

  public TranslationUtil translationUtil() {
    return translationUtil;
  }
}
