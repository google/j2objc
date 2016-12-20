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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.gen.SignatureGenerator;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.TypeUtil;

/**
 * A TreeVisitor that contains CompilationUnit specific state accessible by subclasses.
 */
public class UnitTreeVisitor extends TreeVisitor {

  protected final CompilationUnit unit;
  protected final ElementUtil elementUtil;
  protected final TypeUtil typeUtil;
  protected final NameTable nameTable;
  protected final SignatureGenerator signatureGenerator;
  protected final TranslationUtil translationUtil;
  protected final Options options;

  public UnitTreeVisitor(CompilationUnit unit) {
    this.unit = unit;
    TranslationEnvironment env = unit.getEnv();
    elementUtil = env.elementUtil();
    typeUtil = env.typeUtil();
    nameTable = env.nameTable();
    signatureGenerator = env.signatureGenerator();
    translationUtil = env.translationUtil();
    this.options = env.options();
  }

  public void run() {
    unit.accept(this);
  }
}
