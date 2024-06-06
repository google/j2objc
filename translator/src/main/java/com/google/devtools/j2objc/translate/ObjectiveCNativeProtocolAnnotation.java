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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.ObjectiveCNativeProtocol;
import com.google.j2objc.annotations.ObjectiveCNativeProtocols;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Pair;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.jspecify.nullness.Nullable;

/** Implements the ObjectiveCNativeProtocol annotation. */
public class ObjectiveCNativeProtocolAnnotation extends UnitTreeVisitor {

  public ObjectiveCNativeProtocolAnnotation(CompilationUnit unit) {
    super(unit);
  }

  private @Nullable GeneratedTypeElement nativeProtocolElementFromAnnotation(
      AnnotationMirror annotation) {
    String protocolName = (String) ElementUtil.getAnnotationValue(annotation, "name");
    if (isNullOrEmpty(protocolName)) {
      ErrorUtil.error("ObjectiveCNativeProtocol must specify a native protocol name.");
      return null;
    }

    String protocolHeader = (String) ElementUtil.getAnnotationValue(annotation, "header");
    if (protocolHeader == null) {
      protocolHeader = ""; // Undefined header needs to be treated as no import.
    }
    return GeneratedTypeElement.newIosInterface(protocolName, protocolHeader);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    TypeElement element = node.getTypeElement();
    TypeMirror type = element.asType();

    if (!TypeUtil.isInterface(type) && (!TypeUtil.isClass(type) || TypeUtil.isEnum(type))) {
      return;
    }

    List<TypeMirror> protocols = Lists.newArrayList();
    Attribute.Compound repeatedAnnotation =
        (Attribute.Compound) ElementUtil.getAnnotation(element, ObjectiveCNativeProtocols.class);
    if (repeatedAnnotation != null) {
      for (Pair<Symbol.MethodSymbol, Attribute> value : repeatedAnnotation.values) {
        List<Attribute> attributes = ((Attribute.Array) value.snd).getValue();
        for (Attribute attr : attributes) {
          GeneratedTypeElement protocolAsInterface =
              nativeProtocolElementFromAnnotation((Attribute.Compound) attr);
          if (protocolAsInterface != null) {
            protocols.add(protocolAsInterface.asType());
          }
        }
      }
    } else {
      AnnotationMirror annotation =
          ElementUtil.getAnnotation(element, ObjectiveCNativeProtocol.class);
      if (annotation != null) {
        GeneratedTypeElement protocolAsInterface = nativeProtocolElementFromAnnotation(annotation);
        if (protocolAsInterface != null) {
          protocols.add(protocolAsInterface.asType());
        }
      }
    }

    if (protocols.isEmpty()) {
      return;
    }

    GeneratedTypeElement replacement = GeneratedTypeElement.mutableCopy(element);
    replacement.addInterfaces(protocols);
    node.setTypeElement(replacement);
  }
}
