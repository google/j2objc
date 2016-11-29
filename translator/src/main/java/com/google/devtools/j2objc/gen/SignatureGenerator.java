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

import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/**
 * Generates signatures for classes, fields and methods, as defined by the JVM spec, 4.3.4,
 * which states:
 * <p>
 * <i>Signatures are used to encode Java programming language type information that is
 * not part of the Java Virtual Machine type system, such as generic type and method
 * declarations and parameterized types.</i>
 * <p>
 * In class files, these strings define Signature attributes. Signature attributes can
 * be dumped from class files using "javap -v fully-qualified-class-name".
 *
 * @author Tom Ball
 */
public class SignatureGenerator {

  private static final String JAVA_OBJECT_SIGNATURE = "Ljava/lang/Object;";

  private final ElementUtil elementUtil;
  private final TypeUtil typeUtil;

  public SignatureGenerator(TypeUtil typeUtil) {
    elementUtil = typeUtil.elementUtil();
    this.typeUtil = typeUtil;
  }

  /**
   * Create a signature for a specified type.
   *
   * @return the signature of the type.
   */
  public String createTypeSignature(TypeMirror type) {
    StringBuilder sb = new StringBuilder();
    genTypeSignature(type, sb);
    return sb.toString();
  }

  /**
   * Create a class signature string for a specified type.
   *
   * @return the signature if class is generic, else null.
   */
  public String createClassSignature(TypeElement type) {
    if (!hasGenericSignature(type)) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    genClassSignature(type, sb);
    return sb.toString();
  }

  /**
   * Create a field signature string for a specified variable.
   *
   * @return the signature if field type is a type variable, else null.
   */
  public String createFieldTypeSignature(VariableElement variable) {
    if (!hasGenericSignature(variable.asType())) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    genTypeSignature(variable.asType(), sb);
    return sb.toString();
  }

  /**
   * Create a method signature string for a specified method or constructor.
   *
   * @return the signature if method is generic or use type variables, else null.
   */
  public String createMethodTypeSignature(ExecutableElement method) {
    if (!hasGenericSignature(method)) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    genMethodTypeSignature(method, sb);
    return sb.toString();
  }

  public String createJniFunctionSignature(ExecutableElement method) {
    // Mangle function name as described in JNI specification.
    // http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html#wp615
    StringBuilder sb = new StringBuilder();
    sb.append("Java_");

    String methodName = ElementUtil.getName(method);
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    PackageElement pkg = ElementUtil.getPackage(declaringClass);
    if (pkg != null && !pkg.isUnnamed()) {
      String pkgName = pkg.getQualifiedName().toString();
      for (String part : pkgName.split("\\.")) {
        sb.append(part);
        sb.append('_');
      }
    }
    jniMangleClass(declaringClass, sb);
    sb.append('_');
    sb.append(jniMangle(methodName));

    // Check whether the method is overloaded.
    int nameCount = 0;
    for (ExecutableElement m : ElementUtil.getExecutables(declaringClass)) {
      if (methodName.equals(ElementUtil.getName(m)) && ElementUtil.isNative(m)) {
        nameCount++;
      }
    }
    if (nameCount >= 2) {
      // Overloaded native methods, append JNI-mangled parameter types.
      sb.append("__");
      for (VariableElement param : method.getParameters()) {
        String type = createTypeSignature(typeUtil.erasure(param.asType()));
        sb.append(jniMangle(type));
      }
    }
    return sb.toString();
  }

  private static void jniMangleClass(TypeElement clazz, StringBuilder sb) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(clazz);
    if (declaringClass != null) {
      jniMangleClass(declaringClass, sb);
      sb.append("_00024");  // $
    }
    sb.append(jniMangle(ElementUtil.getName(clazz)));
  }

  private static String jniMangle(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '.': sb.append('_');  break;
        case '/': sb.append('_');  break;
        case '_': sb.append("_1"); break;
        case ';': sb.append("_2"); break;
        case '[': sb.append("_3"); break;
        case '$': sb.append("_00024"); break;
        default: {
          Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
          if (!Character.UnicodeBlock.BASIC_LATIN.equals(block)) {
            sb.append(UnicodeUtils.format("_%05x", (int) c));
          } else {
            sb.append(c);
          }
          break;
        }
      }
    }
    return sb.toString();
  }

  private static boolean hasGenericSignature(TypeElement type) {
    return !type.getTypeParameters().isEmpty() || hasGenericSignature(type.getSuperclass())
        || hasGenericSignature(type.getInterfaces());
  }

  private static boolean hasGenericSignature(TypeMirror type) {
    if (type == null) {
      return false;
    }
    while (TypeUtil.isArray(type)) {
      type = ((ArrayType) type).getComponentType();
    }
    switch (type.getKind()) {
      case TYPEVAR: return true;
      case DECLARED: return !((DeclaredType) type).getTypeArguments().isEmpty();
      default: return false;
    }
  }

  private static boolean hasGenericSignature(Iterable<? extends TypeMirror> typeList) {
    for (TypeMirror type : typeList) {
      if (hasGenericSignature(type)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasGenericSignature(ExecutableElement method) {
    // Is this method generic?
    return !method.getTypeParameters().isEmpty() || hasGenericSignature(method.getReturnType())
        // Are any of its parameters?
        || hasGenericSignature(ElementUtil.asTypes(method.getParameters()))
        // Are any of its thrown exceptions ?
        || hasGenericSignature(method.getThrownTypes());
  }

  // Method comments are from libcore.reflect.GenericSignatureParser.

  /**
   * ClassSignature ::=
   *   OptFormalTypeParameters SuperclassSignature {SuperinterfaceSignature}.
   */
  private void genClassSignature(TypeElement type, StringBuilder sb) {
    genOptFormalTypeParameters(type.getTypeParameters(), sb);
    // JDT returns null for an interface's superclass, but signatures expect Object.
    if (type.getKind().isInterface()) {
      sb.append(JAVA_OBJECT_SIGNATURE);
    } else {
      genTypeSignature(type.getSuperclass(), sb);
    }
    for (TypeMirror intrface : type.getInterfaces()) {
      genTypeSignature(intrface, sb);
    }
  }

  /**
   * FormalTypeParameters:
   *   < FormalTypeParameter+ >
   *
   * FormalTypeParameter:
   *   Identifier ClassBound InterfaceBound*
   */
  private void genOptFormalTypeParameters(
      List<? extends TypeParameterElement> typeParameters, StringBuilder sb) {
    if (!typeParameters.isEmpty()) {
      sb.append('<');
      for (TypeParameterElement typeParam : typeParameters) {
        genFormalTypeParameter(typeParam, sb);
      }
      sb.append('>');
    }
  }

  /**
   * FormalTypeParameter ::= Ident ClassBound {InterfaceBound}.
   */
  private void genFormalTypeParameter(TypeParameterElement typeParam, StringBuilder sb) {
    sb.append(ElementUtil.getName(typeParam));
    List<? extends TypeMirror> bounds = typeParam.getBounds();
    if (bounds.isEmpty()) {
      sb.append(':').append(JAVA_OBJECT_SIGNATURE);
    } else {
      if (TypeUtil.isInterface(bounds.get(0))) {
        sb.append(':');
      }
      for (TypeMirror bound : bounds) {
        sb.append(':');
        genTypeSignature(bound, sb);
      }
    }
  }

  // TODO(kstanger): Figure out if this can replace TypeUtil.getSignatureName().
  private void genTypeSignature(TypeMirror type, StringBuilder sb) {
    switch (type.getKind()) {
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
      case VOID:
        sb.append(TypeUtil.getBinaryName(type));
        break;
      case ARRAY:
        // ArrayTypeSignature ::= "[" TypSignature.
        sb.append('[');
        genTypeSignature(((ArrayType) type).getComponentType(), sb);
        break;
      case DECLARED:
        // ClassTypeSignature ::= "L" {Ident "/"} Ident
        //   OptTypeArguments {"." Ident OptTypeArguments} ";".
        sb.append('L');
        sb.append(elementUtil.getBinaryName(TypeUtil.asTypeElement(type)).replace('.', '/'));
        genOptTypeArguments(((DeclaredType) type).getTypeArguments(), sb);
        sb.append(';');
        break;
      case TYPEVAR:
        // TypeVariableSignature ::= "T" Ident ";".
        sb.append('T');
        sb.append(ElementUtil.getName(((TypeVariable) type).asElement()));
        sb.append(';');
        break;
      case WILDCARD:
        // TypeArgument ::= (["+" | "-"] FieldTypeSignature) | "*".
        TypeMirror upperBound = ((WildcardType) type).getExtendsBound();
        TypeMirror lowerBound = ((WildcardType) type).getSuperBound();
        if (upperBound != null) {
          sb.append('+');
          genTypeSignature(upperBound, sb);
        } else if (lowerBound != null) {
          sb.append('-');
          genTypeSignature(lowerBound, sb);
        } else {
          sb.append('*');
        }
        break;
      default:
        throw new AssertionError("Unexpected type kind: " + type.getKind());
    }
  }

  /**
   * OptTypeArguments ::= "<" TypeArgument {TypeArgument} ">".
   */
  private void genOptTypeArguments(List<? extends TypeMirror> typeArguments, StringBuilder sb) {
    if (!typeArguments.isEmpty()) {
      sb.append('<');
      for (TypeMirror typeParam : typeArguments) {
        genTypeSignature(typeParam, sb);
      }
      sb.append('>');
    }
  }

  /**
   * MethodTypeSignature ::= [FormalTypeParameters]
   *         "(" {TypeSignature} ")" ReturnType {ThrowsSignature}.
   */
  private void genMethodTypeSignature(ExecutableElement method, StringBuilder sb) {
    genOptFormalTypeParameters(method.getTypeParameters(), sb);
    sb.append('(');
    for (VariableElement param : method.getParameters()) {
      genTypeSignature(param.asType(), sb);
    }
    sb.append(')');
    genTypeSignature(method.getReturnType(), sb);
    List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
    if (hasGenericSignature(thrownTypes)) {
      for (TypeMirror thrownType : thrownTypes) {
        sb.append('^');
        genTypeSignature(thrownType, sb);
      }
    }
  }
}
