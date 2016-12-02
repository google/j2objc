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

package com.google.devtools.cyclefinder;

import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/**
 * Generates various kinds of names for types:
 * - Unique type signature for identifying nodes.
 * - Qualified names used by whitelists.
 * - Readable names for printing edges.
 *
 * @author Keith Stanger
 */
public class NameUtil {

  private final ElementUtil elementUtil;
  private final TypeUtil typeUtil;
  private final Map<Element, String> captureNames = new HashMap<>();
  private static int captureCount = 1;

  public NameUtil(TypeUtil typeUtil) {
    this.elementUtil = typeUtil.elementUtil();
    this.typeUtil = typeUtil;
  }

  /**
   * Generates a unique signature for this type that can be used as a key.
   */
  public String getSignature(TypeMirror type) {
    StringBuilder sb = new StringBuilder();
    buildTypeSignature(type, sb);
    return sb.toString();
  }

  private void buildTypeSignature(TypeMirror type, StringBuilder sb) {
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
        sb.append('[');
        buildTypeSignature(((ArrayType) type).getComponentType(), sb);
        break;
      case DECLARED:
        buildDeclaredType((DeclaredType) type, sb);
        sb.append(';');
        break;
      case TYPEVAR:
        buildTypeVariable((TypeVariable) type, sb);
        break;
      case INTERSECTION:
        sb.append("&{");
        for (TypeMirror bound : ((IntersectionType) type).getBounds()) {
          buildTypeSignature(bound, sb);
        }
        sb.append('}');
        break;
      case WILDCARD:
        TypeMirror upperBound = ((WildcardType) type).getExtendsBound();
        TypeMirror lowerBound = ((WildcardType) type).getSuperBound();
        if (upperBound != null) {
          sb.append('+');
          buildTypeSignature(upperBound, sb);
        } else if (lowerBound != null) {
          sb.append('-');
          buildTypeSignature(lowerBound, sb);
        } else {
          sb.append('*');
        }
        break;
      default:
        throw new AssertionError("Unexpected type kind: " + type.getKind());
    }
  }

  private void buildDeclaredType(DeclaredType type, StringBuilder sb) {
    TypeMirror enclosing = type.getEnclosingType();
    if (TypeUtil.isNone(enclosing)) {
      sb.append('L');
      buildElementSignature(type.asElement(), sb);
    } else {
      buildDeclaredType((DeclaredType) enclosing, sb);
      String binaryName = elementUtil.getBinaryName(TypeUtil.asTypeElement(type));
      String enclosingBinaryName = elementUtil.getBinaryName(TypeUtil.asTypeElement(enclosing));
      assert binaryName.startsWith(enclosingBinaryName + "$");
      sb.append('.').append(binaryName.substring(enclosingBinaryName.length() + 1));
    }
    buildTypeArguments(type, sb);
  }

  private void buildTypeArguments(DeclaredType type, StringBuilder sb) {
    Element elem = type.asElement();
    List<? extends TypeMirror> typeArguments = type.getTypeArguments();
    if (!typeArguments.isEmpty()) {
      sb.append('<');
      for (TypeMirror typeArg : typeArguments) {
        TypeParameterElement typeParam = TypeUtil.asTypeParameterElement(typeArg);
        if (typeParam != null && elem.equals(typeParam.getEnclosingElement())) {
          // The type param is directly declared by the type being emitted so we don't need to fully
          // qualify it as buildTypeSignature() would.
          sb.append('T');
          sb.append(ElementUtil.getName(typeParam));
          sb.append(';');
        } else {
          buildTypeSignature(typeArg, sb);
        }
      }
      sb.append('>');
    }
  }

  private void buildTypeVariable(TypeVariable type, StringBuilder sb) {
    TypeParameterElement typeParam = (TypeParameterElement) type.asElement();
    if (isCapture(typeParam)) {
      String name = captureNames.get(typeParam);
      if (name == null) {
        name = "!CAP" + captureCount++ + '!';
        captureNames.put(typeParam, name);
      }
      sb.append(name);
    } else {
      sb.append("T:");
      buildElementSignature(typeParam.getEnclosingElement(), sb);
      sb.append(':').append(ElementUtil.getName(typeParam)).append(';');
    }
  }

  private void buildElementSignature(Element elem, StringBuilder sb) {
    if (ElementUtil.isTypeElement(elem)) {
      sb.append(elementUtil.getBinaryName((TypeElement) elem).replace('.', '/'));
    } else if (ElementUtil.isExecutableElement(elem)) {
      buildExecutableElementSignature((ExecutableElement) elem, sb);
    } else {
      throw new AssertionError("Unexpected element: " + elem + " with kind: " + elem.getKind());
    }
  }

  private void buildExecutableElementSignature(ExecutableElement elem, StringBuilder sb) {
    buildElementSignature(elem.getEnclosingElement(), sb);
    sb.append('.').append(ElementUtil.getName(elem)).append('(');
    for (VariableElement var : elem.getParameters()) {
      buildTypeSignature(typeUtil.erasure(var.asType()), sb);
    }
    sb.append(')');
  }

  /**
   * Generates a readable name for this type.
   */
  public static String getName(TypeMirror type) {
    StringBuilder sb = new StringBuilder();
    buildTypeName(type, sb);
    return sb.toString();
  }

  private static void buildTypeName(TypeMirror type, StringBuilder sb) {
    switch (type.getKind()) {
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
        sb.append(TypeUtil.getName(type));
        break;
      case ARRAY:
        buildTypeName(((ArrayType) type).getComponentType(), sb);
        sb.append("[]");
        break;
      case DECLARED: {
        sb.append(ElementUtil.getName(TypeUtil.asTypeElement(type)));
        List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
        if (!typeArguments.isEmpty()) {
          sb.append('<');
          boolean first = true;
          for (TypeMirror typeArg : typeArguments) {
            if (!first) {
              sb.append(", ");
            }
            first = false;
            buildTypeName(typeArg, sb);
          }
          sb.append('>');
        }
        break;
      }
      case TYPEVAR:
        sb.append(ElementUtil.getName(((TypeVariable) type).asElement()));
        break;
      case INTERSECTION: {
        boolean first = true;
        for (TypeMirror bound : ((IntersectionType) type).getBounds()) {
          if (!first) {
            sb.append(" & ");
          }
          first = false;
          buildTypeName(bound, sb);
        }
        break;
      }
      case WILDCARD: {
        sb.append('?');
        TypeMirror extendsBound = ((WildcardType) type).getExtendsBound();
        TypeMirror superBound = ((WildcardType) type).getSuperBound();
        if (extendsBound != null) {
          sb.append(" extends ");
          buildTypeName(extendsBound, sb);
        }
        if (superBound != null) {
          sb.append(" super ");
          buildTypeName(superBound, sb);
        }
        break;
      }
      default:
        throw new AssertionError("Unexpected type: " + type + " with kind: " + type.getKind());
    }
  }

  /**
   * Gets the qualified name for this type, as expected by a NameList instance.
   */
  public static String getQualifiedName(TypeMirror type) {
    switch (type.getKind()) {
      case DECLARED:
        return getQualifiedNameForTypeElement((TypeElement) ((DeclaredType) type).asElement());
      case TYPEVAR:
        return getQualifiedNameForElement(((TypeVariable) type).asElement());
      case INTERSECTION:
        return "&";
      case WILDCARD:
        return "?";
      default:
        throw new AssertionError("Unexpected type: " + type + " with kind: " + type.getKind());
    }
  }

  private static String getQualifiedNameForTypeElement(TypeElement type) {
    switch (type.getNestingKind()) {
      case ANONYMOUS:
        return getQualifiedNameForElement(type.getEnclosingElement()) + ".$";
      case LOCAL:
        return getQualifiedNameForElement(type.getEnclosingElement()) + '.'
            + ElementUtil.getName(type);
      default:
        return ElementUtil.getQualifiedName(type);
    }
  }

  private static String getQualifiedNameForElement(Element e) {
    if (isCapture(e)) {
      return "!";
    } else if (ElementUtil.isTypeElement(e)) {
      return getQualifiedNameForTypeElement((TypeElement) e);
    } else if (ElementUtil.isExecutableElement(e) || ElementUtil.isTypeParameterElement(e)) {
      return getQualifiedNameForElement(e.getEnclosingElement()) + '.' + ElementUtil.getName(e);
    }
    return getQualifiedNameForElement(e.getEnclosingElement());
  }

  private static boolean isCapture(Element e) {
    return ElementUtil.isTypeParameterElement(e)
        && e.getEnclosingElement().getKind() == ElementKind.OTHER;
  }
}
