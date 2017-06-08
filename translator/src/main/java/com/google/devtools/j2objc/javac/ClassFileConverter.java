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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Converts a JVM classfile into a CompilationUnit.
 */
public class ClassFileConverter extends ClassVisitor {
  private final JavacEnvironment parserEnv;
  private final InputFile file;
  private TypeElement element;

  public static CompilationUnit convert(
      Options options, JavacEnvironment env, InputFile file) throws IOException {
    ClassReader classReader = new ClassReader(file.getInputStream());
    ClassFileConverter converter = new ClassFileConverter(env, file);
    classReader.accept(converter, 0);
    if (converter.element == null) {
      ErrorUtil.error("Invalid class file: " + file.getOriginalLocation());
    }
    return converter.createUnit(new TranslationEnvironment(options, env));
  }

  ClassFileConverter(JavacEnvironment parserEnv, InputFile file) {
    super(Opcodes.ASM5);
    this.parserEnv = parserEnv;
    this.file = file;
  }

  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    setClassPath(name);
    
    String typeName = Type.getObjectType(name).getClassName();
    element = parserEnv.elementUtilities().getTypeElement(typeName);
  }

  // Set classpath to the root path of the input file, to support element lookup.
  private void setClassPath(String name) {
    try {
      String fullPath = file.getAbsolutePath();
      String rootPath = fullPath.substring(0, fullPath.lastIndexOf(name + ".class"));
      List<File> classPath = new ArrayList<>();
      classPath.add(new File(rootPath));
      parserEnv.fileManager().setLocation(StandardLocation.CLASS_PATH, classPath);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }
  }
  
  private CompilationUnit createUnit(TranslationEnvironment translationEnv) {
    /* TODO:
     *  . create new CompilationUnit using translation environment
     *  . create new TypeDeclaration using element.
     *  . set CompilationUnit package using element's package.
     */
    return null;
  }
}
