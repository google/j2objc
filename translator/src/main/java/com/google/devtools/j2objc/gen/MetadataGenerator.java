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

package com.google.devtools.j2objc.gen;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Generates the "__metadata" method for a class.
 *
 * @author Tom Ball, Keith Stanger
 */
public class MetadataGenerator {

  private final StringBuilder builder;
  private final AbstractTypeDeclaration typeNode;
  private final ITypeBinding type;
  private boolean generated = false;
  private boolean hasMetadata = false;
  private int methodMetadataCount = 0;

  public MetadataGenerator(AbstractTypeDeclaration typeNode) {
    this.builder = new StringBuilder();
    this.typeNode = Preconditions.checkNotNull(typeNode);
    this.type = Types.getTypeBinding(typeNode);
  }

  public boolean hasMetadata() {
    ensureGenerated();
    return hasMetadata;
  }

  public String getMetadataSource() {
    ensureGenerated();
    return builder.toString();
  }

  private void ensureGenerated() {
    if (!generated) {
      generateMetadata();
      generated = true;
    }
  }

  private void generateMetadata() {
    String fullName = NameTable.getFullName(type);
    println("+ (J2ObjcClassInfo *)__metadata {");
    generateMethodsMetadata();
    int superclassTypeArgsSize = printSuperclassTypeArguments();
    printf("  static J2ObjcClassInfo _%s = { ", fullName);
    printf("\"%s\", ", type.getName());
    String pkgName = type.getPackage().getName();
    if (Strings.isNullOrEmpty(pkgName)) {
      printf("NULL, ");
    } else {
      printf("\"%s\", ", pkgName);
      hasMetadata = true;
    }
    printf("%s, ", getEnclosingName());
    printf("0x%s, ", Integer.toHexString(getTypeModifiers()));
    printf("%s, ", Integer.toString(methodMetadataCount));
    print(methodMetadataCount > 0 ? "methods, " : "NULL, ");
    printf("%s, ", Integer.toString(superclassTypeArgsSize));
    printf(superclassTypeArgsSize > 0 ? "superclass_type_args" : "NULL");
    println("};");
    printf("  return &_%s;\n}\n\n", fullName);
  }

  private String getEnclosingName() {
    ITypeBinding declaringType = type.getDeclaringClass();
    if (declaringType == null) {
      return "NULL";
    }
    hasMetadata = true;
    StringBuilder sb = new StringBuilder("\"");
    List<String> types = Lists.newArrayList();
    while (declaringType != null) {
      types.add(declaringType.getName());
      declaringType = declaringType.getDeclaringClass();
    }
    for (int i = types.size() - 1; i >= 0; i--) {
      sb.append(types.get(i));
      if (i > 0) {
        sb.append("$");
      }
    }
    sb.append("\"");
    return sb.toString();
  }

  private void generateMethodsMetadata() {
    List<String> methodMetadata = Lists.newArrayList();
    for (MethodDeclaration decl : ASTUtil.getMethodDeclarations(typeNode)) {
      String metadata = getMethodMetadata(Types.getMethodBinding(decl));
      if (metadata != null) {
        methodMetadata.add(metadata);
        hasMetadata = true;
      }
    }
    if (methodMetadata.size() > 0) {
      builder.append("  static J2ObjcMethodInfo methods[] = {\n");
      for (String metadata : methodMetadata) {
        builder.append(metadata);
      }
      builder.append("  };\n");
    }
    methodMetadataCount = methodMetadata.size();
  }

  private String getMethodMetadata(IMethodBinding method) {
    ITypeBinding[] paramTypes = method.getParameterTypes();
    if ((method.isConstructor() && paramTypes.length == 0) || method.isSynthetic()) {
      return null;
    }

    // Needs metadata if not a public static or instance method.
    int modifiers = getMethodModifiers(method);
    boolean needsMetadata = (modifiers & ~Modifier.STATIC) != Modifier.PUBLIC;

    String returnTypeStr = "NULL";
    if (!method.isConstructor()) {
      ITypeBinding returnType = method.getReturnType();
      returnTypeStr = String.format("\"%s\"", getTypeName(returnType));
      if (!returnType.isPrimitive() || returnType.getName().equals("boolean")) {
        needsMetadata = true;
      }
    }
    String methodName = "NULL";
    // Most of the time the method name can be parsed from the ObjC selector
    // but not if the first parameter contains the substring "With".
    if (paramTypes.length > 0 && NameTable.parameterKeyword(paramTypes[0]).contains("With")) {
      methodName = "\"" + method.getName() + "\"";
      needsMetadata = true;
    }
    if (!needsMetadata) {
      return null;
    }
    return String.format("    { \"%s\", %s, %s, 0x%x },\n",
        NameTable.getMethodSelector(method), methodName, returnTypeStr, modifiers);
  }

  private int printSuperclassTypeArguments() {
    ITypeBinding superclass = type.getSuperclass();
    if (superclass == null) {
      return 0;
    }
    ITypeBinding[] typeArgs = superclass.getTypeArguments();
    if (typeArgs.length == 0) {
      return 0;
    }
    hasMetadata = true;
    print("  static const char *superclass_type_args[] = {");
    for (int i = 0; i < typeArgs.length; i++) {
      if (i != 0) {
        print(", ");
      }
      printf("\"%s\"", getTypeName(typeArgs[i]));
    }
    println("};");
    return typeArgs.length;
  }

  private static String getTypeName(ITypeBinding type) {
    if (type.isTypeVariable()) {
      return "T" + type.getName();
    }
    if (type.isPrimitive()) {
      return type.getBinaryName();
    }
    return "L" + NameTable.getFullName(type);
  }

  /**
   * Returns the modifiers for a specified type, including internal ones.
   * All class modifiers are defined in the JVM specification, table 4.1.
   */
  private int getTypeModifiers() {
    int modifiers = type.getModifiers();
    if (type.isInterface()) {
      modifiers |= 0x200;
    }
    if (type.isSynthetic()) {
      modifiers |= 0x1000;
    }
    if (type.isAnnotation()) {
      modifiers |= 0x2000;
    }
    if (type.isEnum()) {
      modifiers |= 0x4000;
    }
    if (type.isAnonymous()) {
      modifiers |= 0x8000;
    }
    hasMetadata |= (modifiers & ~0x200) != Modifier.PUBLIC;
    return modifiers;
  }

  /**
   * Returns the modifiers for a specified method, including internal ones.
   * All method modifiers are defined in the JVM specification, table 4.5.
   */
  private static int getMethodModifiers(IMethodBinding type) {
    int modifiers = type.getModifiers();
    if (type.isVarargs()) {
      modifiers |= 0x80;
    }
    if (type.isSynthetic()) {
      modifiers |= 0x1000;
    }
    return modifiers;
  }

  public void print(String s) {
    builder.append(s);
  }

  public void printf(String format, Object... args) {
    builder.append(String.format(format, args));
  }

  public void println(String s) {
    builder.append(s).append('\n');
  }
}
