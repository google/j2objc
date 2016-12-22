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
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

class JavacEnvironment implements ParserEnvironment, Closeable {

  private final JavacTaskImpl task;
  private final JavacFileManager fileManager;
  private final DiagnosticCollector<JavaFileObject> diagnostics;
  private final Context context;
  private final ClassReader classReader;
  private final Names javacNames;
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
    javacNames = Names.instance(context);
    symbolTable = Symtab.instance(context);
    javacElements = JavacElements.instance(context);
    javacTypes = JavacTypes.instance(context);
  }

  @Override
  public Element resolve(String name) {
    Name className = javacNames.fromString(name);

    // Check first if compiler already created or loaded the class.
    ClassSymbol symbol = symbolTable.classes.get(className);
    if (symbol == null) {
      // Not available, read it from a class file.
      // Note: the enterName(Name) method moved from ClassReader to
      // Symtab in Java 9. Reflection is used to support both locations.
      symbol = enterClassJavac(className);
      if (symbol != null) {
        symbolTable.classes.put(className, symbol);
      } else {
        symbol = enterClassJavac9(className);
        // The symbolTable is already updated in Java 9.
      }
    }
    return symbol;
  }

  public PackageElement defaultPackage() {
    return symbolTable.unnamedPackage;
  }

  private ClassSymbol enterClassJavac(Name className) {
    try {
      Method m = ClassReader.class.getDeclaredMethod("enterClass", Name.class);
      return (ClassSymbol) m.invoke(classReader, className);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      return null;
    }
  }

  private ClassSymbol enterClassJavac9(Name className) {
    // TODO(tball): remove reflection use when Java 9 is minimum version.
    try {
      Field javaBaseField = Names.class.getDeclaredField("java_base");
      Name javaBaseName = (Name) javaBaseField.get(javacNames);

      Class<?> moduleSymbolCls = Class.forName("com.sun.tools.javac.code.Symbol.MethodSymbol");
      Method enterModule = Symtab.class.getDeclaredMethod("enterModule", Name.class);
      Object javaBaseModule = enterModule.invoke(symbolTable, javaBaseName);

      Method enterClass = Symtab.class.getDeclaredMethod("enterClass", moduleSymbolCls, Name.class);
      return (ClassSymbol) enterClass.invoke(symbolTable, javaBaseModule, className);
    } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException
        | InvocationTargetException | IllegalAccessException e) {
      return null;
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

  @Override
  public void close() throws IOException {
    fileManager.close();
  }
}
