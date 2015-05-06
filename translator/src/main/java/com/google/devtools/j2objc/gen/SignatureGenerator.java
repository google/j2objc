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

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

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

  private StringBuilder sb = new StringBuilder();

  /**
   * Create a class signature string for a specified type.
   *
   * @return the signature if class is generic, else null.
   */
  public static String createClassSignature(ITypeBinding type) {
    boolean create = needsSignature(type) || needsSignature(type.getSuperclass());
    if (!create) {
      for (ITypeBinding intf : type.getInterfaces()) {
        if (needsSignature(intf)) {
          create = true;
          break;
        }
      }
    }
    if (!create) {
      return null;
    }
    SignatureGenerator builder = new SignatureGenerator();
    builder.genClassSignature(type);
    return builder.toString();
  }

  /**
   * Create a field signature string for a specified variable.
   *
   * @return the signature if field type is a type variable, else null.
   */
  public static String createFieldTypeSignature(IVariableBinding variable) {
    ITypeBinding type = variable.getType();
    if (type.isArray()) {
      if (!type.getElementType().isTypeVariable() && !type.getElementType().isParameterizedType()) {
        return null;
      }
    } else if (!type.isTypeVariable() && !type.isParameterizedType()) {
      return null;
    }
    SignatureGenerator builder = new SignatureGenerator();
    builder.genFieldTypeSignature(type);
    return builder.toString();
  }

  /**
   * Create a method signature string for a specified method or constructor.
   *
   * @return the signature if method is generic or use type variables, else null.
   */
  public static String createMethodTypeSignature(IMethodBinding method) {
    if (!hasGenericSignature(method)) {
      return null;
    }
    SignatureGenerator builder = new SignatureGenerator();
    builder.genMethodTypeSignature(method);
    return builder.toString();
  }

  public static String createJniFunctionSignature(IMethodBinding method) {
    // Mangle function name as described in JNI specification.
    // http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html#wp615
    StringBuilder sb = new StringBuilder();
    sb.append("Java_");

    String methodName = method.getName();
    ITypeBinding declaringClass = method.getDeclaringClass();
    IPackageBinding pkg = declaringClass.getPackage();
    if (pkg != null && !pkg.isUnnamed()) {
      String pkgName = pkg.getName();
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
    for (IMethodBinding m : declaringClass.getDeclaredMethods()) {
      if (methodName.equals(m.getName()) && Modifier.isNative(m.getModifiers())) {
        nameCount++;
      }
    }
    if (nameCount >= 2) {
      // Overloaded native methods, append JNI-mangled parameter types.
      sb.append("__");
      ITypeBinding[] parameters = method.getParameterTypes();
      for (int iParam = 0; iParam < parameters.length; iParam++) {
        String type = createTypeSignature(parameters[iParam]);
        sb.append(jniMangle(type));
      }
    }
    return sb.toString();
  }

  private static void jniMangleClass(ITypeBinding clazz, StringBuilder sb) {
    if (clazz.getDeclaringClass() != null) {
      jniMangleClass(clazz.getDeclaringClass(), sb);
      sb.append("_00024");   // $
    }
    sb.append(jniMangle(clazz.getName()));
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
          if (block != Character.UnicodeBlock.BASIC_LATIN) {
            sb.append(String.format("_%05x", (int) c));
          } else {
            sb.append(c);
          }
          break;
        }
      }
    }
    return sb.toString();
  }

  private static String createTypeSignature(ITypeBinding type) {
    SignatureGenerator builder = new SignatureGenerator();
    builder.genTypeSignature(type.getErasure());
    return builder.toString();
  }

  private static boolean hasGenericSignature(IMethodBinding method) {
    if (method.isGenericMethod() || method.getReturnType().isTypeVariable()) {
      return true;
    }
    for (ITypeBinding param : method.getParameterTypes()) {
      if (param.isTypeVariable()) {
        return true;
      }
    }
    return false;
  }

  public String toString() {
    return sb.toString();
  }

  // Method comments are from libcore.reflect.GenericSignatureParser.

  /**
   * ClassSignature ::=
   *   OptFormalTypeParameters SuperclassSignature {SuperinterfaceSignature}.
   */
  private void genClassSignature(ITypeBinding type) {
    genOptFormalTypeParameters(type.getTypeParameters());
    // JDT returns null for an interface's superclass, but signatures expect Object.
    if (type.isInterface()) {
      sb.append(JAVA_OBJECT_SIGNATURE);
    } else {
      genClassTypeSignature(type.getSuperclass());
    }
    for (ITypeBinding intrface : type.getInterfaces()) {
      genClassTypeSignature(intrface);
    }
  }

  /**
   * FormalTypeParameters:
   *   < FormalTypeParameter+ >
   *
   * FormalTypeParameter:
   *   Identifier ClassBound InterfaceBound*
   */
  private void genOptFormalTypeParameters(ITypeBinding[] typeParameters) {
    if (typeParameters.length > 0) {
      sb.append('<');
      for (ITypeBinding typeParam : typeParameters) {
        genFormalTypeParameter(typeParam);
      }
      sb.append('>');
    }
  }

  /**
   * FormalTypeParameter ::= Ident ClassBound {InterfaceBound}.
   */
  private void genFormalTypeParameter(ITypeBinding typeParam) {
    sb.append(typeParam.getName());
    sb.append(':');
    ITypeBinding bound = typeParam.getBound();
    if (bound != null) {
      genFieldTypeSignature(bound);
    } else {
      ITypeBinding[] bounds = typeParam.getTypeBounds();
      if (bounds.length > 0) {
        for (int i = 0; i < bounds.length; i++) {
          if (i > 0 || bounds[i].isInterface()) {
            sb.append(':');
          }
          genFieldTypeSignature(bounds[i]);
        }
      } else {
        genFieldTypeSignature(typeParam.getErasure());
      }
    }
  }

  /**
   * FieldTypeSignature ::= ClassTypeSignature | ArrayTypeSignature
   *         | TypeVariableSignature.
   */
  private void genFieldTypeSignature(ITypeBinding type) {
    if (type.isArray()) {
      sb.append('[');
      genTypeSignature(type.getComponentType());
    } else if (type.isTypeVariable()) {
      genTypeVariableSignature(type);
    } else {
      genClassTypeSignature(type);
    }
  }

  /**
   * ClassTypeSignature ::= "L" {Ident "/"} Ident
   *   OptTypeArguments {"." Ident OptTypeArguments} ";".
   */
  private void genClassTypeSignature(ITypeBinding type) {
    if (type != null) {
      sb.append('L');
      sb.append(type.getBinaryName().replace('.', '/'));
      genOptTypeArguments(type.getTypeArguments());
      sb.append(';');
    }
  }

  /**
   * OptTypeArguments ::= "<" TypeArgument {TypeArgument} ">".
   */
  private void genOptTypeArguments(ITypeBinding[] typeArguments) {
    if (typeArguments.length > 0) {
      sb.append('<');
      for (ITypeBinding typeParam : typeArguments) {
        genTypeArgument(typeParam);
      }
      sb.append('>');
    }
  }

  /**
   * TypeArgument ::= (["+" | "-"] FieldTypeSignature) | "*".
   */
  private void genTypeArgument(ITypeBinding typeArg) {
    if (typeArg.isWildcardType()) {
      ITypeBinding bound = typeArg.getBound();
      if (bound != null) {
        // JDT bug: bound.isUpperbound() always returns false, but toString() is correct.
        sb.append(typeArg.toString().contains("extends") ? '+' : '-');
        genTypeArgument(bound);
      } else {
        sb.append('*');
        return;
      }
    } else if (typeArg.isTypeVariable()) {
      genTypeVariableSignature(typeArg);
    } else {
      genClassTypeSignature(typeArg);
    }
  }

  /**
   * MethodTypeSignature ::= [FormalTypeParameters]
   *         "(" {TypeSignature} ")" ReturnType {ThrowsSignature}.
   */
  private void genMethodTypeSignature(IMethodBinding method) {
    genOptFormalTypeParameters(method.getTypeParameters());
    sb.append('(');
    for (ITypeBinding param : method.getParameterTypes()) {
      genTypeSignature(param);
    }
    sb.append(')');
    genReturnType(method.getReturnType());
    ITypeBinding[] exceptionTypes = method.getExceptionTypes();
    boolean hasGenericException = false;
    for (ITypeBinding exception : exceptionTypes) {
      if (exception.isGenericType() || exception.isParameterizedType()) {
        hasGenericException = true;
        break;
      }
    }
    if (hasGenericException) {
      for (ITypeBinding exception : exceptionTypes) {
        sb.append('^');
        if (exception.isTypeVariable()) {
          genTypeVariableSignature(exception);
        } else {
          genClassTypeSignature(exception);
        }
      }
    }
  }

  private void genReturnType(ITypeBinding returnType) {
    if (returnType.getBinaryName().equals("V")) {
      sb.append('V');
    } else {
      genTypeSignature(returnType);
    }
  }

  private void genTypeSignature(ITypeBinding type) {
    if (type.isPrimitive()) {
      sb.append(type.getBinaryName());
    } else {
      genFieldTypeSignature(type);
    }
  }

  private void genTypeVariableSignature(ITypeBinding type) {
    sb.append('T');
    sb.append(type.getName());
    sb.append(';');
  }

  private static boolean needsSignature(ITypeBinding type) {
    if (type == null) {
      return false;
    }
    return type.isGenericType() || type.isParameterizedType();
  }
}
