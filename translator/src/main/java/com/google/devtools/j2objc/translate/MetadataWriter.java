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

package com.google.devtools.j2objc.translate;

import com.google.common.base.Strings;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.gen.SignatureGenerator;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.NativeTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the __metadata method to classes to support reflection.
 */
public class MetadataWriter extends TreeVisitor {

  // Metadata structure version. Increment it when any structure changes are made.
  public static final int METADATA_VERSION = 2;

  private static final NativeTypeBinding CLASS_INFO_TYPE =
      new NativeTypeBinding("const J2ObjcClassInfo *");

  @Override
  public void endVisit(TypeDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    visitType(node);
  }

  private void visitType(AbstractTypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    if (BindingUtil.isSynthetic(type) || !TranslationUtil.needsReflection(type)) {
      return;
    }

    GeneratedMethodBinding metadataBinding = GeneratedMethodBinding.newMethod(
        "__metadata", Modifier.STATIC | Modifier.PRIVATE | BindingUtil.ACC_SYNTHETIC,
        CLASS_INFO_TYPE, type);
    MethodDeclaration metadataDecl = new MethodDeclaration(metadataBinding);
    metadataDecl.setHasDeclaration(false);

    Block body = new Block();
    metadataDecl.setBody(body);

    generateClassMetadata(node, body.getStatements());

    node.getBodyDeclarations().add(metadataDecl);
  }

  private void generateClassMetadata(AbstractTypeDeclaration typeNode, List<Statement> stmts) {
    ITypeBinding type = typeNode.getTypeBinding();
    String fullName = nameTable.getFullName(type);
    StringBuilder sb = new StringBuilder();
    int methodMetadataCount = generateMethodsMetadata(typeNode, stmts);
    int fieldMetadataCount = generateFieldsMetadata(typeNode, stmts);
    int superclassTypeArgsSize = generateSuperclassTypeArguments(type, stmts);
    int innerClassesSize = generateInnerClasses(type, stmts);
    String enclosingMethodStruct = generateEnclosingMethodMetadata(type, stmts);
    String simpleName = type.getName();
    if (type.isAnonymous()) {
      simpleName = "";  // Anonymous classes have an empty simple name.
    }
    String pkgName = Strings.emptyToNull(type.getPackage().getName());
    sb.append("static const J2ObjcClassInfo _").append(fullName).append(" = { ");
    sb.append(METADATA_VERSION).append(", ");
    sb.append(cStr(simpleName)).append(", ");
    sb.append(cStr(pkgName)).append(", ");
    sb.append(getEnclosingName(type)).append(", ");
    sb.append("0x").append(Integer.toHexString(getTypeModifiers(type))).append(", ");
    sb.append(methodMetadataCount).append(", ");
    sb.append(methodMetadataCount > 0 ? "methods, " : "NULL, ");
    sb.append(fieldMetadataCount).append(", ");
    sb.append(fieldMetadataCount > 0 ? "fields, " : "NULL, ");
    sb.append(superclassTypeArgsSize).append(", ");
    sb.append(superclassTypeArgsSize > 0 ? "superclass_type_args, " : "NULL, ");
    sb.append(innerClassesSize).append(", ");
    sb.append(innerClassesSize > 0 ? "inner_classes, " : "NULL, ");
    if (enclosingMethodStruct != null) {
      sb.append('&').append(enclosingMethodStruct).append(", ");
    } else {
      sb.append("NULL, ");
    }
    sb.append(cStr(SignatureGenerator.createClassSignature(type)));
    sb.append(" };");
    stmts.add(new NativeStatement(sb.toString()));
    stmts.add(new ReturnStatement(new NativeExpression("&_" + fullName, CLASS_INFO_TYPE)));
  }

  private String getEnclosingName(ITypeBinding type) {
    ITypeBinding declaringType = type.getDeclaringClass();
    if (declaringType == null) {
      return "NULL";
    }
    StringBuilder sb = new StringBuilder("\"");
    List<String> types = new ArrayList<>();
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

  private int generateMethodsMetadata(AbstractTypeDeclaration typeNode, List<Statement> stmts) {
    List<String> methodMetadata = new ArrayList<>();
    for (MethodDeclaration decl : TreeUtil.getMethodDeclarations(typeNode)) {
      IMethodBinding binding = decl.getMethodBinding();
      if (!BindingUtil.isSynthetic(decl.getModifiers()) && !binding.isSynthetic()) {
        methodMetadata.add(getMethodMetadata(binding));
      }
    }
    if (typeNode instanceof AnnotationTypeDeclaration) {
      // Add property accessor and static default methods.
      for (AnnotationTypeMemberDeclaration decl : TreeUtil.getAnnotationMembers(typeNode)) {
        String name = decl.getName().getIdentifier();
        String returnType = getTypeName(decl.getMethodBinding().getReturnType());
        String metadata = UnicodeUtils.format("    { \"%s\", %s, %s, 0x%x, NULL, NULL },\n",
            name, cStr(name), cStr(returnType),
            java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT);
        methodMetadata.add(metadata);
      }
    }
    if (methodMetadata.size() > 0) {
      StringBuilder sb = new StringBuilder("static const J2ObjcMethodInfo methods[] = {\n");
      for (String metadata : methodMetadata) {
        sb.append(metadata);
      }
      sb.append("  };");
      stmts.add(new NativeStatement(sb.toString()));
    }
    return methodMetadata.size();
  }

  private String getMethodMetadata(IMethodBinding method) {
    String methodName = method instanceof GeneratedMethodBinding
        ? ((GeneratedMethodBinding) method).getJavaName() : method.getName();
    String selector = nameTable.getMethodSelector(method);
    if (selector.equals(methodName)) {
      methodName = null;  // Reduce redundant data.
    }

    int modifiers = getMethodModifiers(method) & BindingUtil.ACC_FLAG_MASK;
    String returnTypeStr = method.isConstructor() ? null : getTypeName(method.getReturnType());
    return UnicodeUtils.format("    { \"%s\", %s, %s, 0x%x, %s, %s },\n",
        selector, cStr(methodName), cStr(returnTypeStr), modifiers,
        cStr(getThrownExceptions(method)),
        cStr(SignatureGenerator.createMethodTypeSignature(method)));
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

  private int generateFieldsMetadata(AbstractTypeDeclaration typeNode, List<Statement> stmts) {
    List<String> fieldMetadata = new ArrayList<>();
    if (typeNode instanceof EnumDeclaration) {
      for (EnumConstantDeclaration decl : ((EnumDeclaration) typeNode).getEnumConstants()) {
        fieldMetadata.add(generateFieldMetadata(decl.getVariableBinding(), decl.getName()));
      }
    }
    for (VariableDeclarationFragment f : TreeUtil.getAllFields(typeNode)) {
      String metadata = generateFieldMetadata(f.getVariableBinding(), f.getName());
      if (metadata != null) {
        fieldMetadata.add(metadata);
      }
    }
    if (fieldMetadata.size() > 0) {
      StringBuilder sb = new StringBuilder("static const J2ObjcFieldInfo fields[] = {\n");
      for (String metadata : fieldMetadata) {
        sb.append(metadata);
      }
      sb.append("  };");
      stmts.add(new NativeStatement(sb.toString()));
    }
    return fieldMetadata.size();
  }

  private String generateFieldMetadata(IVariableBinding var, SimpleName name) {
    if (BindingUtil.isSynthetic(var)) {
      return null;
    }
    int modifiers = getFieldModifiers(var);
    boolean isStatic = BindingUtil.isStatic(var);
    String javaName = name.getIdentifier();
    String objcName = nameTable.getVariableShortName(var);
    if ((isStatic && objcName.equals(javaName)) || (!isStatic && objcName.equals(javaName + '_'))) {
      // Don't print Java name if it matches the default pattern, to conserve space.
      javaName = null;
    }
    String staticRef = "NULL";
    String constantValue;
    if (BindingUtil.isPrimitiveConstant(var)) {
      constantValue = UnicodeUtils.format(".constantValue.%s = %s",
          getRawValueField(var), nameTable.getVariableQualifiedName(var));
    } else {
      // Explicit 0-initializer to avoid Clang warning.
      constantValue = ".constantValue.asLong = 0";
      if (isStatic) {
        staticRef = '&' + nameTable.getVariableQualifiedName(var);
      }
    }
    return UnicodeUtils.format(
        "    { \"%s\", %s, 0x%x, \"%s\", %s, %s, %s },\n",
        objcName, cStr(javaName), modifiers, getTypeName(var.getType()), staticRef,
        cStr(SignatureGenerator.createFieldTypeSignature(var)), constantValue);
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

  private int generateSuperclassTypeArguments(ITypeBinding type, List<Statement> stmts) {
    ITypeBinding superclass = type.getSuperclass();
    if (superclass == null) {
      return 0;
    }
    ITypeBinding[] typeArgs = superclass.getTypeArguments();
    if (typeArgs.length == 0) {
      return 0;
    }
    StringBuilder sb = new StringBuilder("static const char *superclass_type_args[] = {");
    for (int i = 0; i < typeArgs.length; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(cStr(getTypeName(typeArgs[i])));
    }
    sb.append("};");
    stmts.add(new NativeStatement(sb.toString()));
    return typeArgs.length;
  }

  private int generateInnerClasses(ITypeBinding type, List<Statement> stmts) {
    ITypeBinding[] innerTypes = type.getDeclaredTypes();
    if (innerTypes.length == 0) {
      return 0;
    }
    StringBuilder sb = new StringBuilder("static const char *inner_classes[] = {");
    for (int i = 0; i < innerTypes.length; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(cStr(getTypeName(innerTypes[i])));
    }
    sb.append("};");
    stmts.add(new NativeStatement(sb.toString()));
    return innerTypes.length;
  }

  private String generateEnclosingMethodMetadata(ITypeBinding type, List<Statement> stmts) {
    IMethodBinding enclosingMethod = type.getDeclaringMethod();
    if (enclosingMethod == null) {
      return null;
    }

    // Method isn't enclosing if this type is defined in a type also enclosed
    // by this method.
    if (enclosingMethod.isEqualTo(type.getDeclaringClass().getDeclaringMethod())) {
      return null;
    }

    String structName = "enclosing_method";
    StringBuilder sb = new StringBuilder("static const J2ObjCEnclosingMethodInfo ")
        .append(structName).append(" = { ")
        .append(cStr(nameTable.getFullName(enclosingMethod.getDeclaringClass()))).append(", ")
        .append(cStr(nameTable.getMethodSelector(enclosingMethod))).append(" };");
    stmts.add(new NativeStatement(sb.toString()));
    return structName;
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
  private static int getTypeModifiers(ITypeBinding type) {
    int modifiers = type.getModifiers();
    if (type.isInterface()) {
      modifiers |= java.lang.reflect.Modifier.INTERFACE | java.lang.reflect.Modifier.ABSTRACT
          | java.lang.reflect.Modifier.STATIC;
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
      // Anonymous classes are always static, though their closure may include an instance.
      modifiers |= BindingUtil.ACC_ANONYMOUS | java.lang.reflect.Modifier.STATIC;
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
}
