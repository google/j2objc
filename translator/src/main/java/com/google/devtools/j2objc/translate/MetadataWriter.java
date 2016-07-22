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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.gen.SignatureGenerator;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.NativeTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Adds the __metadata method to classes to support reflection.
 */
public class MetadataWriter extends TreeVisitor {

  // Metadata structure version. Increment it when any structure changes are made.
  public static final int METADATA_VERSION = 7;

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
    if (!TranslationUtil.needsReflection(type)) {
      return;
    }

    GeneratedMethodBinding metadataBinding = GeneratedMethodBinding.newMethod(
        "__metadata", Modifier.STATIC | Modifier.PRIVATE | BindingUtil.ACC_SYNTHETIC,
        CLASS_INFO_TYPE, type);
    MethodDeclaration metadataDecl = new MethodDeclaration(metadataBinding);
    metadataDecl.setHasDeclaration(false);

    Block body = new Block();
    metadataDecl.setBody(body);

    new MetadataGenerator(node, body.getStatements()).generateClassMetadata();

    node.addBodyDeclaration(metadataDecl);
  }

  /**
   * Generates the metadata contents for a single type.
   */
  private class MetadataGenerator {

    private final AbstractTypeDeclaration typeNode;
    private final ITypeBinding type;
    private final List<Statement> stmts;
    // Use a LinkedHashMap so that we can de-dupe values that are added to the pointer table.
    private final LinkedHashMap<String, Integer> pointers = new LinkedHashMap<>();
    private final RuntimeAnnotationGenerator annotationGenerator;

    private MetadataGenerator(AbstractTypeDeclaration typeNode, List<Statement> stmts) {
      this.typeNode = typeNode;
      type = typeNode.getTypeBinding();
      this.stmts = stmts;
      annotationGenerator = new RuntimeAnnotationGenerator(typeNode);
    }

    private void generateClassMetadata() {
      String fullName = nameTable.getFullName(type);
      int methodMetadataCount = generateMethodsMetadata();
      int fieldMetadataCount = generateFieldsMetadata();
      String annotationsFunc = annotationGenerator.createFunction(typeNode);
      String metadata = UnicodeUtils.format(
          "static const J2ObjcClassInfo _%s = { "
          + "%s, %s, %%s, %s, %s, %d, 0x%x, %d, %d, %s, %s, %s, %s, %s };",
          fullName,
          cStr(type.isAnonymous() ? "" : type.getName()),
          cStr(Strings.emptyToNull(type.getPackage().getName())),
          methodMetadataCount > 0 ? "methods" : "NULL",
          fieldMetadataCount > 0 ? "fields" : "NULL",
          METADATA_VERSION,
          getTypeModifiers(type),
          methodMetadataCount,
          fieldMetadataCount,
          cStrIdx(getTypeName(type.getDeclaringClass())),
          cStrIdx(getTypeList(type.getDeclaredTypes())),
          cStrIdx(getEnclosingMethodSelector()),
          cStrIdx(SignatureGenerator.createClassSignature(type)),
          funcPtrIdx(annotationsFunc));
      // Add the pointer table in a second format pass since it's value is dependent on all other
      // values.
      metadata = UnicodeUtils.format(metadata, getPtrTableEntry());
      stmts.add(new NativeStatement(metadata));
      stmts.add(new ReturnStatement(new NativeExpression("&_" + fullName, CLASS_INFO_TYPE)));
    }

    private String getPtrTableEntry() {
      if (pointers.isEmpty()) {
        return "NULL";
      }
      if (pointers.size() > Short.MAX_VALUE) {
        // Note that values greater that 2^15 and less than 2^16 will not result in a compile
        // error even though the index type is declared as signed.
        // This limit is more restrictive than existing limits on number of methods and fields
        // imposed by the JVM class file format, which allows up to 2^16 each of methods and
        // fields. Our limit is a few times smaller than Java's since we use a signed index, share
        // the table between methods and fields, and have multiple entries for each method or
        // field that can index into the table. See JVMS-4.11.
        ErrorUtil.error(typeNode, "Too many metadata entries causing overflow.");
      }
      stmts.add(new NativeStatement(
          "static const void *ptrTable[] = { " + Joiner.on(", ").join(pointers.keySet()) + " };"));
      return "ptrTable";
    }

    private int generateMethodsMetadata() {
      List<String> methodMetadata = new ArrayList<>();
      for (MethodDeclaration decl : TreeUtil.getMethodDeclarations(typeNode)) {
        IMethodBinding binding = decl.getMethodBinding();
        // Skip synthetic methods
        if (BindingUtil.isSynthetic(decl.getModifiers()) || binding.isSynthetic()
            // Skip enum constructors.
            || (type.isEnum() && binding.isConstructor())) {
          continue;
        }
        String annotationsFunc = annotationGenerator.createFunction(decl);
        String paramAnnotationsFunc = annotationGenerator.createParamsFunction(decl);
        methodMetadata.add(getMethodMetadata(binding, annotationsFunc, paramAnnotationsFunc));
      }
      if (typeNode instanceof AnnotationTypeDeclaration) {
        // Add property accessor and static default methods.
        for (AnnotationTypeMemberDeclaration decl : TreeUtil.getAnnotationMembers(typeNode)) {
          String name = decl.getName().getIdentifier();
          IMethodBinding memberBinding = (IMethodBinding)
              BindingConverter.unwrapElement(decl.getElement());
          String returnType = getTypeName(memberBinding.getReturnType());
          String metadata = UnicodeUtils.format("    { %s, %s, 0x%x, -1, -1, -1, -1, -1, -1 },\n",
              cStr(name), cStr(returnType),
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

    private String getMethodMetadata(
        IMethodBinding method, String annotationsFunc, String paramAnnotationsFunc) {
      String methodName = method instanceof GeneratedMethodBinding
          ? ((GeneratedMethodBinding) method).getJavaName() : method.getName();
      String selector = nameTable.getMethodSelector(method);
      if (selector.equals(methodName) || method.isConstructor()) {
        methodName = null;  // Reduce redundant data.
      }

      int modifiers = getMethodModifiers(method) & BindingUtil.ACC_FLAG_MASK;
      String returnTypeStr = method.isConstructor() ? null : getTypeName(method.getReturnType());
      return UnicodeUtils.format("    { \"%s\", %s, 0x%x, %s, %s, %s, %s, %s, %s },\n",
          selector, cStr(returnTypeStr), modifiers, cStrIdx(methodName),
          cStrIdx(getTypeList(method.getParameterTypes())),
          cStrIdx(getTypeList(method.getExceptionTypes())),
          cStrIdx(SignatureGenerator.createMethodTypeSignature(method)),
          funcPtrIdx(annotationsFunc), funcPtrIdx(paramAnnotationsFunc));
    }

    private int generateFieldsMetadata() {
      List<String> fieldMetadata = new ArrayList<>();
      if (typeNode instanceof EnumDeclaration) {
        for (EnumConstantDeclaration decl : ((EnumDeclaration) typeNode).getEnumConstants()) {
          String annotationsFunc = annotationGenerator.createFunction(decl);
          fieldMetadata.add(generateFieldMetadata(decl.getVariableBinding(), annotationsFunc));
        }
      }
      for (FieldDeclaration decl : TreeUtil.getFieldDeclarations(typeNode)) {
        // Fields that share a declaration can share an annotations function.
        String annotationsFunc = annotationGenerator.createFunction(decl);
        for (VariableDeclarationFragment f : decl.getFragments()) {
          String metadata = generateFieldMetadata(f.getVariableBinding(), annotationsFunc);
          if (metadata != null) {
            fieldMetadata.add(metadata);
          }
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

    private String generateFieldMetadata(IVariableBinding var, String annotationsFunc) {
      if (BindingUtil.isSynthetic(var)) {
        return null;
      }
      int modifiers = getFieldModifiers(var);
      boolean isStatic = BindingUtil.isStatic(var);
      String javaName = var.getName();
      String objcName = nameTable.getVariableShortName(var);
      if ((isStatic && objcName.equals(javaName))
          || (!isStatic && objcName.equals(javaName + '_'))) {
        // Don't print Java name if it matches the default pattern, to conserve space.
        javaName = null;
      }
      String staticRef = null;
      String constantValue;
      if (BindingUtil.isPrimitiveConstant(var)) {
        constantValue = UnicodeUtils.format(".constantValue.%s = %s",
            getRawValueField(var), nameTable.getVariableQualifiedName(var));
      } else {
        // Explicit 0-initializer to avoid Clang warning.
        constantValue = ".constantValue.asLong = 0";
        if (isStatic) {
          staticRef = nameTable.getVariableQualifiedName(var);
        }
      }
      return UnicodeUtils.format(
          "    { %s, %s, %s, 0x%x, %s, %s, %s, %s },\n",
          cStr(objcName), cStr(getTypeName(var.getType())), constantValue, modifiers,
          cStrIdx(javaName), addressOfIdx(staticRef),
          cStrIdx(SignatureGenerator.createFieldTypeSignature(var)), funcPtrIdx(annotationsFunc));
    }

    private String getEnclosingMethodSelector() {
      IMethodBinding enclosingMethod = type.getDeclaringMethod();
      if (enclosingMethod == null) {
        return null;
      }

      // Method isn't enclosing if this type is defined in a type also enclosed
      // by this method.
      if (enclosingMethod.isEqualTo(type.getDeclaringClass().getDeclaringMethod())) {
        return null;
      }

      return nameTable.getMethodSelector(enclosingMethod);
    }

    private String cStrIdx(String str) {
      return getPointerIdx(str != null ? "\"" + str + "\"" : null);
    }

    private String addressOfIdx(String name) {
      return getPointerIdx(name != null ? "&" + name : null);
    }

    // Same as addressOfIdx, but adds a (void *) cast to satisfy c++ compilers.
    private String funcPtrIdx(String name) {
      return getPointerIdx(name != null ? "(void *)&" + name : null);
    }

    private String getPointerIdx(String ptr) {
      if (ptr == null) {
        return "-1";
      }
      Integer idx = pointers.get(ptr);
      if (idx == null) {
        idx = pointers.size();
        pointers.put(ptr, idx);
      }
      return idx.toString();
    }
  }

  private static String getRawValueField(IVariableBinding var) {
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

  private String getTypeName(ITypeBinding type) {
    if (type == null) {
      return null;
    }
    type = type.getErasure();
    if (type.isPrimitive()) {
      return type.getBinaryName();
    } else if (type.isArray()) {
      return "[" + getTypeName(type.getComponentType());
    } else {
      return "L" + nameTable.getFullName(type) + ";";
    }
  }

  private String getTypeList(ITypeBinding[] types) {
    if (types.length == 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (ITypeBinding type : types) {
      sb.append(getTypeName(type));
    }
    return sb.toString();
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
