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

import com.google.devtools.j2objc.util.ParserEnvironment;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

class JavacEnvironment implements ParserEnvironment {

  private final JavacTaskImpl task;
  private final JavacFileManager fileManager;
  private final DiagnosticCollector<JavaFileObject> diagnostics;
  private final Context context;
  private final ClassReader classReader;
  private final Symtab symbolTable;
  private final JavacElements javacElements;
  private final JavacTypes javacTypes;

  JavacEnvironment(JavacTaskImpl task, JavacFileManager fileManager,
      DiagnosticCollector<JavaFileObject> diagnostics) {
    this.task = task;
    this.fileManager = fileManager;
    this.diagnostics = diagnostics;
    context = task.getContext();
    classReader = ClassReader.instance(context);
    symbolTable = Symtab.instance(context);
    javacElements = JavacElements.instance(context);
    javacTypes = JavacTypes.instance(context);
  }

  TypeMirror resolvePrimitiveType(String signature) {
    switch (signature) {
      case "B": return symbolTable.byteType;
      case "C": return symbolTable.charType;
      case "D": return symbolTable.doubleType;
      case "F": return symbolTable.floatType;
      case "I": return symbolTable.intType;
      case "J": return symbolTable.longType;
      case "S": return symbolTable.shortType;
      case "V": return symbolTable.voidType;
      case "Z": return symbolTable.booleanType;
      default:
        return null;
    }
  }

  public PackageElement defaultPackage() {
    try {
      return (PackageElement) Symtab.class.getField("unnamedPackage").get(symbolTable);
    } catch (NoSuchFieldException e) {
      // continue
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
    try {
      Object unnamedModule = Symtab.class.getField("unnamedModule");
      return (PackageElement) unnamedModule.getClass().getField("unnamedPackage").get(symbolTable);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  public Context getContext() {
    return context;
  }

  @Override
  public Elements elementUtilities() {
    return javacElements;
  }

  @Override
  public Types typeUtilities() {
    return javacTypes;
  }

  public JavacTaskImpl task() {
    return task;
  }

  public JavacFileManager fileManager() {
    return fileManager;
  }

  public DiagnosticCollector<JavaFileObject> diagnostics() {
    return diagnostics;
  }

  public void saveParameterNames() {
    classReader.saveParameterNames = true;
  }
}
