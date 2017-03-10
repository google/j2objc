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
import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

/**
 * Adds the __metadata method to classes to support reflection.
 */
public class MetadataWriter extends UnitTreeVisitor {

  // Metadata structure version. Increment it when any structure changes are made.
  public static final int METADATA_VERSION = 7;

  private static final NativeType CLASS_INFO_TYPE = new NativeType("const J2ObjcClassInfo *");
  private final ArrayType annotationArray;
  private final ArrayType annotationArray2D;

  public MetadataWriter(CompilationUnit unit) {
    super(unit);
    TypeMirror annotationType =
        GeneratedTypeElement.newEmulatedInterface("java.lang.annotation.Annotation").asType();
    annotationArray = typeUtil.getArrayType(annotationType);
    annotationArray2D = typeUtil.getArrayType(annotationArray);
  }

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
    TypeElement type = node.getTypeElement();
    if (!translationUtil.needsReflection(type)) {
      return;
    }

    ExecutableElement metadataElement =
        GeneratedExecutableElement.newMethodWithSelector("__metadata", CLASS_INFO_TYPE, type)
        .addModifiers(Modifier.STATIC, Modifier.PRIVATE);
    MethodDeclaration metadataDecl = new MethodDeclaration(metadataElement);
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
    private final TypeElement type;
    private final String className;
    private final List<Statement> stmts;
    // Use a LinkedHashMap so that we can de-dupe values that are added to the pointer table.
    private final LinkedHashMap<String, Integer> pointers = new LinkedHashMap<>();
    private int annotationFuncCount = 0;

    private MetadataGenerator(AbstractTypeDeclaration typeNode, List<Statement> stmts) {
      this.typeNode = typeNode;
      type = typeNode.getTypeElement();
      className = nameTable.getFullName(type);
      this.stmts = stmts;
    }

    private void generateClassMetadata() {
      String fullName = nameTable.getFullName(type);
      int methodMetadataCount = generateMethodsMetadata();
      int fieldMetadataCount = generateFieldsMetadata();
      String annotationsFunc = createAnnotationsFunction(typeNode);
      String metadata = UnicodeUtils.format(
          "static const J2ObjcClassInfo _%s = { "
          + "%s, %s, %%s, %s, %s, %d, 0x%x, %d, %d, %s, %s, %s, %s, %s };",
          fullName,
          cStr(ElementUtil.isAnonymous(type) ? "" : ElementUtil.getName(type)),
          cStr(Strings.emptyToNull(ElementUtil.getName(ElementUtil.getPackage(type)))),
          methodMetadataCount > 0 ? "methods" : "NULL",
          fieldMetadataCount > 0 ? "fields" : "NULL",
          METADATA_VERSION,
          getTypeModifiers(type),
          methodMetadataCount,
          fieldMetadataCount,
          cStrIdx(getTypeName(ElementUtil.getDeclaringClass(type))),
          cStrIdx(getTypeList(ElementUtil.asTypes(ElementUtil.getDeclaredTypes(type)))),
          cStrIdx(getEnclosingMethodSelector()),
          cStrIdx(signatureGenerator.createClassSignature(type)),
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
      List<String> selectorMetadata = new ArrayList<>();
      int methodCount = 0;
      for (MethodDeclaration decl : TreeUtil.getMethodDeclarations(typeNode)) {
        ExecutableElement element = decl.getExecutableElement();
        // Skip synthetic methods and enum constructors.
        if (ElementUtil.isSynthetic(element)
            || (ElementUtil.isEnum(type) && ElementUtil.isConstructor(element))) {
          continue;
        }
        String annotationsFunc = createAnnotationsFunction(decl);
        String paramAnnotationsFunc = createParamAnnotationsFunction(decl);
        methodMetadata.add(getMethodMetadata(element, annotationsFunc, paramAnnotationsFunc));
        String selector = nameTable.getMethodSelector(element);
        String metadata = UnicodeUtils.format("methods[%d].selector = @selector(%s);",
            methodCount, selector);
        ++methodCount;
        selectorMetadata.add(metadata);
      }
      if (typeNode instanceof AnnotationTypeDeclaration) {
        // Add property accessor and static default methods.
        for (AnnotationTypeMemberDeclaration decl : TreeUtil.getAnnotationMembers(typeNode)) {
          String name = ElementUtil.getName(decl.getExecutableElement());
          String returnType = getTypeName(decl.getExecutableElement().getReturnType());
          String metadata = UnicodeUtils.format("    { NULL, %s, 0x%x, -1, -1, -1, -1, -1, -1 },\n",
              cStr(returnType),
              java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT);
          methodMetadata.add(metadata);
          metadata = UnicodeUtils.format("methods[%d].selector = @selector(%s);",
              methodCount, name);
          ++methodCount;
          selectorMetadata.add(metadata);
        }
      }
      if (methodMetadata.size() > 0) {
        StringBuilder sb = new StringBuilder("static J2ObjcMethodInfo methods[] = {\n");
        for (String metadata : methodMetadata) {
          sb.append(metadata);
        }
        sb.append("  };");
        stmts.add(new NativeStatement(sb.toString()));
        stmts.add(new NativeStatement("#pragma clang diagnostic push"));
        stmts.add(new NativeStatement(
            "#pragma clang diagnostic ignored \"-Wobjc-multiple-method-names\""));
        for (String selector : selectorMetadata) {
          stmts.add(new NativeStatement(selector));
        }
        stmts.add(new NativeStatement("#pragma clang diagnostic pop"));
      }
      return methodMetadata.size();
    }

    private String getMethodMetadata(
        ExecutableElement method, String annotationsFunc, String paramAnnotationsFunc) {
      String methodName = ElementUtil.getName(method);
      String selector = nameTable.getMethodSelector(method);
      boolean isConstructor = ElementUtil.isConstructor(method);
      if (selector.equals(methodName) || isConstructor) {
        methodName = null;  // Reduce redundant data.
      }

      int modifiers = getMethodModifiers(method) & ElementUtil.ACC_FLAG_MASK;
      String returnTypeStr = isConstructor ? null : getTypeName(method.getReturnType());
      return UnicodeUtils.format("    { NULL, %s, 0x%x, %s, %s, %s, %s, %s, %s },\n",
          cStr(returnTypeStr), modifiers, cStrIdx(methodName),
          cStrIdx(getTypeList(ElementUtil.asTypes(method.getParameters()))),
          cStrIdx(getTypeList(method.getThrownTypes())),
          cStrIdx(signatureGenerator.createMethodTypeSignature(method)),
          funcPtrIdx(annotationsFunc), funcPtrIdx(paramAnnotationsFunc));
    }

    private int generateFieldsMetadata() {
      List<String> fieldMetadata = new ArrayList<>();
      if (typeNode instanceof EnumDeclaration) {
        for (EnumConstantDeclaration decl : ((EnumDeclaration) typeNode).getEnumConstants()) {
          String annotationsFunc = createAnnotationsFunction(decl);
          fieldMetadata.add(generateFieldMetadata(decl.getVariableElement(), annotationsFunc));
        }
      }
      for (FieldDeclaration decl : TreeUtil.getFieldDeclarations(typeNode)) {
        // Fields that share a declaration can share an annotations function.
        String annotationsFunc = createAnnotationsFunction(decl);
        for (VariableDeclarationFragment f : decl.getFragments()) {
          String metadata = generateFieldMetadata(f.getVariableElement(), annotationsFunc);
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

    private String generateFieldMetadata(VariableElement var, String annotationsFunc) {
      int modifiers = getFieldModifiers(var);
      boolean isStatic = ElementUtil.isStatic(var);
      String javaName = ElementUtil.getName(var);
      String objcName = nameTable.getVariableShortName(var);
      if ((isStatic && objcName.equals(javaName))
          || (!isStatic && objcName.equals(javaName + '_'))) {
        // Don't print Java name if it matches the default pattern, to conserve space.
        javaName = null;
      }
      String staticRef = null;
      String constantValue;
      if (ElementUtil.isPrimitiveConstant(var)) {
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
          cStr(objcName), cStr(getTypeName(var.asType())), constantValue, modifiers,
          cStrIdx(javaName), addressOfIdx(staticRef),
          cStrIdx(signatureGenerator.createFieldTypeSignature(var)), funcPtrIdx(annotationsFunc));
    }

    private String getEnclosingMethodSelector() {
      Element enclosing = type.getEnclosingElement();
      return ElementUtil.isExecutableElement(enclosing)
          ? nameTable.getMethodSelector((ExecutableElement) enclosing) : null;
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

    /**
     * Generate a function that returns the annotations for a BodyDeclarations node.
     */
    private String createAnnotationsFunction(BodyDeclaration decl) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(decl.getAnnotations());
      if (runtimeAnnotations.isEmpty()) {
        return null;
      }
      return addAnnotationsFunction(createAnnotations(runtimeAnnotations));
    }

    /**
     * Generate a function that returns the 2-dimentional array of annotations for method
     * parameters.
     */
    private String createParamAnnotationsFunction(MethodDeclaration method) {
      List<SingleVariableDeclaration> params = method.getParameters();

      // Quick test to see if there are any parameter annotations.
      boolean hasAnnotations = false;
      for (SingleVariableDeclaration param : params) {
        if (!Iterables.isEmpty(TreeUtil.getRuntimeAnnotations(param.getAnnotations()))) {
          hasAnnotations = true;
          break;
        }
      }
      if (!hasAnnotations) {
        return null;
      }

      List<Expression> subArrays = new ArrayList<>();
      for (SingleVariableDeclaration param : params) {
        subArrays.add(createAnnotations(
            TreeUtil.getRuntimeAnnotationsList(param.getAnnotations())));
      }

      return addAnnotationsFunction(
          translationUtil.createObjectArray(subArrays, annotationArray2D));
    }

    private String addAnnotationsFunction(Expression result) {
      String name = className + "__Annotations$" + annotationFuncCount++;
      FunctionDeclaration decl = new FunctionDeclaration(name, result.getTypeMirror());
      decl.addModifiers(java.lang.reflect.Modifier.PRIVATE);
      Block body = new Block();
      decl.setBody(body);
      body.addStatement(new ReturnStatement(result));
      typeNode.addBodyDeclaration(decl);
      return name;
    }
  }

  private Expression createAnnotations(List<Annotation> annotations) {
    List<Expression> expressions = new ArrayList<>();
    for (Annotation annotation : annotations) {
      expressions.add(translationUtil.createAnnotation(annotation.getAnnotationMirror()));
    }
    return translationUtil.createObjectArray(expressions, annotationArray);
  }

  private static String getRawValueField(VariableElement var) {
    switch (var.asType().getKind()) {
      case BOOLEAN: return "asBOOL";
      case BYTE: return "asChar";
      case CHAR: return "asUnichar";
      case DOUBLE: return "asDouble";
      case FLOAT: return "asFloat";
      case INT: return "asInt";
      case LONG: return "asLong";
      case SHORT: return "asShort";
      default: throw new AssertionError("Expected a primitive type.");
    }
  }

  private String getTypeName(TypeMirror type) {
    if (type == null) {
      return null;
    }
    type = typeUtil.erasure(type);
    if (TypeUtil.isDeclaredType(type)) {
      return getTypeName(TypeUtil.asTypeElement(type));
    } else if (TypeUtil.isArray(type)) {
      return "[" + getTypeName(((ArrayType) type).getComponentType());
    } else {
      return TypeUtil.getBinaryName(type);
    }
  }

  private String getTypeName(TypeElement type) {
    return type == null ? null : "L" + nameTable.getFullName(type) + ";";
  }

  private String getTypeList(Iterable<? extends TypeMirror> types) {
    if (Iterables.isEmpty(types)) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (TypeMirror type : types) {
      sb.append(getTypeName(type));
    }
    return sb.toString();
  }

  /**
   * Returns the modifiers for a specified type, including internal ones.
   * All class modifiers are defined in the JVM specification, table 4.1.
   */
  private static int getTypeModifiers(TypeElement type) {
    int modifiers = ElementUtil.fromModifierSet(type.getModifiers());
    if (type.getKind().isInterface()) {
      modifiers |= java.lang.reflect.Modifier.INTERFACE | java.lang.reflect.Modifier.ABSTRACT
          | java.lang.reflect.Modifier.STATIC;
    }
    if (ElementUtil.isSynthetic(type)) {
      modifiers |= ElementUtil.ACC_SYNTHETIC;
    }
    if (ElementUtil.isAnnotationType(type)) {
      modifiers |= ElementUtil.ACC_ANNOTATION;
    }
    if (ElementUtil.isEnum(type)) {
      modifiers |= ElementUtil.ACC_ENUM;
    }
    if (ElementUtil.isAnonymous(type)) {
      // Anonymous classes are always static, though their closure may include an instance.
      modifiers |= ElementUtil.ACC_ANONYMOUS | java.lang.reflect.Modifier.STATIC;
    }
    return modifiers;
  }

  /**
   * Returns the modifiers for a specified method, including internal ones.
   * All method modifiers are defined in the JVM specification, table 4.5.
   */
  private static int getMethodModifiers(ExecutableElement method) {
    int modifiers = ElementUtil.fromModifierSet(method.getModifiers());
    if (method.isVarArgs()) {
      modifiers |= ElementUtil.ACC_VARARGS;
    }
    if (ElementUtil.isSynthetic(method)) {
      modifiers |= ElementUtil.ACC_SYNTHETIC;
    }
    return modifiers;
  }

  /**
   * Returns the modifiers for a specified field, including internal ones.
   * All method modifiers are defined in the JVM specification, table 4.4.
   */
  private static int getFieldModifiers(VariableElement var) {
    int modifiers = ElementUtil.fromModifierSet(var.getModifiers());
    if (ElementUtil.isSynthetic(var)) {
      modifiers |= ElementUtil.ACC_SYNTHETIC;
    }
    if (ElementUtil.isEnumConstant(var)) {
      modifiers |= ElementUtil.ACC_ENUM;
    }
    return modifiers;
  }

  private String cStr(String s) {
    return s == null ? "NULL" : "\"" + s + "\"";
  }
}
