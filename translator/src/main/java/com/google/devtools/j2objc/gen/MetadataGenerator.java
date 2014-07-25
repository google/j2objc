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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

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
  private int methodMetadataCount = 0;
  private int fieldMetadataCount = 0;

  public MetadataGenerator(AbstractTypeDeclaration typeNode) {
    this.builder = new StringBuilder();
    this.typeNode = Preconditions.checkNotNull(typeNode);
    this.type = typeNode.getTypeBinding();
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
    println("\n+ (J2ObjcClassInfo *)__metadata {");
    generateMethodsMetadata();
    generateFieldsMetadata();
    int superclassTypeArgsSize = printSuperclassTypeArguments();
    printf("  static J2ObjcClassInfo _%s = { ", fullName);
    printf("\"%s\", ", type.getName());
    String pkgName = type.getPackage().getName();
    if (Strings.isNullOrEmpty(pkgName)) {
      printf("NULL, ");
    } else {
      printf("\"%s\", ", pkgName);
    }
    printf("%s, ", getEnclosingName());
    printf("0x%s, ", Integer.toHexString(getTypeModifiers()));
    printf("%s, ", Integer.toString(methodMetadataCount));
    print(methodMetadataCount > 0 ? "methods, " : "NULL, ");
    printf("%s, ", Integer.toString(fieldMetadataCount));
    print(fieldMetadataCount > 0 ? "fields, " : "NULL, ");
    printf("%s, ", Integer.toString(superclassTypeArgsSize));
    printf(superclassTypeArgsSize > 0 ? "superclass_type_args" : "NULL");
    println("};");
    printf("  return &_%s;\n}\n", fullName);
  }

  private String getEnclosingName() {
    ITypeBinding declaringType = type.getDeclaringClass();
    if (declaringType == null) {
      return "NULL";
    }
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
    for (MethodDeclaration decl : TreeUtil.getMethodDeclarations(typeNode)) {
      String metadata = getMethodMetadata(decl.getMethodBinding());
      if (metadata != null) {
        methodMetadata.add(metadata);
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

  private void generateFieldsMetadata() {
    List<String> fieldMetadata = Lists.newArrayList();
    String typeName = NameTable.getFullName(type);
    if (typeNode instanceof EnumDeclaration) {
      for (EnumConstantDeclaration decl : ((EnumDeclaration) typeNode).getEnumConstants()) {
        fieldMetadata.add(
            generateFieldMetadata(decl.getVariableBinding(), decl.getName(), typeName));
      }
    }
    for (VariableDeclarationFragment f : TreeUtil.getAllFields(typeNode)) {
      fieldMetadata.add(generateFieldMetadata(f.getVariableBinding(), f.getName(), typeName));
    }
    if (fieldMetadata.size() > 0) {
      builder.append("  static J2ObjcFieldInfo fields[] = {\n");
      for (String metadata : fieldMetadata) {
        builder.append(metadata);
      }
      builder.append("  };\n");
    }
    fieldMetadataCount = fieldMetadata.size();
  }

  private String generateFieldMetadata(IVariableBinding var, SimpleName name, String typeName) {
    int modifiers = getFieldModifiers(var);
    String javaName = name.getIdentifier();
    String objcName = var.isEnumConstant() ? NameTable.getName(var)
        : NameTable.javaFieldToObjC(NameTable.getName(var));
    if (objcName.equals(javaName + '_')) {
      // Don't print Java name if it matches the default pattern, to conserve space.
      javaName = null;
    }
    String staticRef = "NULL";
    String constantValue = "";
    if (BindingUtil.isStatic(var)) {
      if (BindingUtil.isPrimitiveConstant(var)) {
        constantValue = String.format(".constantValue.%s = %s",
            getRawValueField(var), NameTable.getPrimitiveConstantName(var));
      } else {
        staticRef = String.format("&%s_%s", typeName, objcName);
      }
    }
    return String.format(
        "    { \"%s\", %s, 0x%x, \"%s\", %s, %s },\n",
        objcName, cStr(javaName), modifiers, getTypeName(var.getType()), staticRef,
        constantValue);
  }

  private String getRawValueField(IVariableBinding var) {
    ITypeBinding type = var.getType();
    assert type.isPrimitive();
    switch (type.getBinaryName().charAt(0)) {
      case 'B': return "asChar";
      case 'C': return "asUnichar";
      case 'D': return "asDouble";
      case 'F': return "asFloat";
      case 'I': return "asInt";
      case 'J': return "asLong";
      case 'S': return "asShort";
      case 'Z': return "asBOOL";
    }
    throw new AssertionError();
  }

  private String getMethodMetadata(IMethodBinding method) {
    if (method.isSynthetic()) {
      return null;
    }
    String methodName = method instanceof GeneratedMethodBinding ?
        ((GeneratedMethodBinding) method).getJavaName() : method.getName();
    String selector = NameTable.getMethodSelector(method);
    if (selector.equals(methodName)) {
      methodName = null;  // Reduce redundant data.
    }

    int modifiers = getMethodModifiers(method);
    String returnTypeStr = method.isConstructor() ? null : getTypeName(method.getReturnType());
    return String.format("    { \"%s\", %s, %s, 0x%x, %s },\n",
        selector, cStr(methodName), cStr(returnTypeStr), modifiers,
        cStr(getThrownExceptions(method)));
  }

  private String getThrownExceptions(IMethodBinding method) {
    ITypeBinding[] exceptionTypes = method.getExceptionTypes();
    if (exceptionTypes.length == 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < exceptionTypes.length; i++) {
      sb.append(getTypeName(exceptionTypes[i]));
    }
    return sb.toString();
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
      return "T" + type.getName() + ";";
    }
    if (type.isPrimitive() || type.isArray()) {
      return type.getBinaryName();
    }
    return "L" + type.getBinaryName() + ";";
  }

  /**
   * Returns the modifiers for a specified type, including internal ones.
   * All class modifiers are defined in the JVM specification, table 4.1.
   */
  private int getTypeModifiers() {
    int modifiers = type.getModifiers();
    if (type.isInterface()) {
      modifiers |= java.lang.reflect.Modifier.INTERFACE;
    }
    if (type.isSynthetic()) {
      modifiers |= BindingUtil.ACC_SYNTHETIC;
    }
    if (type.isAnnotation()) {
      modifiers |= BindingUtil.ACC_ANNOTATION;
    }
    if (type.isEnum()) {
      modifiers |= BindingUtil.ACC_ENUM;
    }
    if (type.isAnonymous()) {
      modifiers |= 0x8000;
    }
    return modifiers;
  }

  /**
   * Returns the modifiers for a specified method, including internal ones.
   * All method modifiers are defined in the JVM specification, table 4.5.
   */
  private static int getMethodModifiers(IMethodBinding type) {
    int modifiers = type.getModifiers();
    if (type.isVarargs()) {
      modifiers |= BindingUtil.ACC_VARARGS;
    }
    if (type.isSynthetic()) {
      modifiers |= BindingUtil.ACC_SYNTHETIC;
    }
    return modifiers;
  }

  /**
   * Returns the modifiers for a specified field, including internal ones.
   * All method modifiers are defined in the JVM specification, table 4.4.
   */
  private static int getFieldModifiers(IVariableBinding type) {
    int modifiers = type.getModifiers();
    if (type.isSynthetic()) {
      modifiers |= BindingUtil.ACC_SYNTHETIC;
    }
    if (type.isEnumConstant()) {
      modifiers |= BindingUtil.ACC_ENUM;
    }
    return modifiers;
  }

  private String cStr(String s) {
    return s == null ? "NULL" : "\"" + s + "\"";
  }

  private void print(String s) {
    builder.append(s);
  }

  private void printf(String format, Object... args) {
    builder.append(String.format(format, args));
  }

  private void println(String s) {
    builder.append(s).append('\n');
  }
}
