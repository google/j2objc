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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.gen.SignatureGenerator;

/**
 * The environment used during translation of a compilation unit.
 */
public class TranslationEnvironment {

  private final ElementUtil elementUtil;
  private final TypeUtil typeUtil;
  private final CaptureInfo captureInfo;
  private final NameTable nameTable;
  private final SignatureGenerator signatureGenerator;
  private final TranslationUtil translationUtil;
  private final Runnable resetMethod;
  private final Options options;

  public TranslationEnvironment(Options options, ParserEnvironment parserEnv) {
    elementUtil = new ElementUtil(parserEnv.elementUtilities());
    typeUtil = new TypeUtil(parserEnv, elementUtil);
    captureInfo = new CaptureInfo(typeUtil);
    nameTable = new NameTable(typeUtil, captureInfo, options);
    signatureGenerator = new SignatureGenerator(typeUtil);
    translationUtil = new TranslationUtil(typeUtil, nameTable, options, elementUtil);
    this.options = options;
    resetMethod = () -> parserEnv.reset();
  }

  public ElementUtil elementUtil() {
    return elementUtil;
  }

  public Options options() {
    return options;
  }

  public TypeUtil typeUtil() {
    return typeUtil;
  }

  public CaptureInfo captureInfo() {
    return captureInfo;
  }

  public NameTable nameTable() {
    return nameTable;
  }

  public SignatureGenerator signatureGenerator() {
    return signatureGenerator;
  }

  public TranslationUtil translationUtil() {
    return translationUtil;
  }

  // TODO(tball): remove when javac front-end update is complete.
  public void reset() {
    resetMethod.run();
  }
}
