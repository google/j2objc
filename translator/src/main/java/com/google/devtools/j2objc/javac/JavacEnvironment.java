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

import com.google.devtools.j2objc.util.NameTable.Factory;
import com.google.devtools.j2objc.util.ParserEnvironment;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class JavacEnvironment extends ParserEnvironment {
  private final Context context;
  private final ClassReader classReader;
  private final Names javacNames;
  private final Symtab symbolTable;
  private final JavacElements javacElements;
  private final JavacTypes javacTypes;

  protected JavacEnvironment(Factory nameTableFactory, Context context) {
    super(nameTableFactory);
    this.context = context;
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

  private ClassSymbol enterClassJavac(Name className) {
    try {
      Method m = ClassReader.class.getDeclaredMethod("enterName", Name.class);
      return (ClassSymbol) m.invoke(classReader, className);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      return null;
    }
  }

  private ClassSymbol enterClassJavac9(Name className) {
    try {
      Method m = Symtab.class.getDeclaredMethod("enterName", Name.class);
      return (ClassSymbol) m.invoke(symbolTable, className);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
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
}
