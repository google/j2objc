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

package com.google.devtools.j2objc.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * CaptureInfo encapsulates all the implicitly captured fields and constructor params of inner and
 * local classes.
 */
public class CaptureInfo {

  // The implicit outer reference from a non-static inner class to its outer class.
  private final Map<TypeElement, Capture> outerCaptures = new HashMap<>();

  // The captured result of the receiver expression of a method reference. For example:
  // Supplier<String> s = foo::toString;
  // In this code, the expression "foo" must be captured by the generated lambda type.
  private final Map<TypeElement, Capture> receiverCaptures = new HashMap<>();

  // Captures for local variables that are referenced from within the local class or lambda.
  private final ListMultimap<TypeElement, LocalCapture> localCaptures =
      MultimapBuilder.hashKeys().arrayListValues().build();

  private final List<VariableElement> implicitEnumParams;

  private final TypeUtil typeUtil;

  public CaptureInfo(TypeUtil typeUtil) {
    implicitEnumParams = ImmutableList.of(
        GeneratedVariableElement.newParameter(
            "__name", typeUtil.getJavaString().asType(), null),
        GeneratedVariableElement.newParameter("__ordinal", typeUtil.getInt(), null));
    this.typeUtil = typeUtil;
  }

  /**
   * Contains the construction parameter and field associated with a captured value.
   */
  public static class Capture {

    protected final VariableElement param;
    protected VariableElement field;

    private Capture(VariableElement param) {
      this.param = param;
    }

    public VariableElement getParam() {
      return param;
    }

    public boolean hasField() {
      return field != null;
    }

    public VariableElement getField() {
      return field;
    }
  }

  private static class LocalCapture extends Capture {

    private final VariableElement var;

    private LocalCapture(VariableElement var, VariableElement param) {
      super(param);
      this.var = var;
    }
  }

  public boolean needsOuterReference(TypeElement type) {
    return getOuterField(type) != null;
  }

  public boolean needsOuterParam(TypeElement type) {
    return outerCaptures.containsKey(type) || automaticOuterParam(type);
  }

  private Capture getOuterCapture(TypeElement type) {
    return automaticOuterParam(type) ? getOrCreateOuterCapture(type) : outerCaptures.get(type);
  }

  public VariableElement getOuterParam(TypeElement type) {
    Capture outerCapture = getOuterCapture(type);
    return outerCapture != null ? outerCapture.param : null;
  }

  public VariableElement getOuterField(TypeElement type) {
    Capture outerCapture = outerCaptures.get(type);
    return outerCapture != null ? outerCapture.field : null;
  }

  public VariableElement getReceiverField(TypeElement type) {
    Capture capture = receiverCaptures.get(type);
    return capture != null ? capture.field : null;
  }

  private <T> void maybeAdd(List<T> list, T elem) {
    if (elem != null) {
      list.add(elem);
    }
  }

  public List<Capture> getCaptures(TypeElement type) {
    List<Capture> captures = new ArrayList<>();
    maybeAdd(captures, getOuterCapture(type));
    maybeAdd(captures, receiverCaptures.get(type));
    captures.addAll(localCaptures.get(type));
    return captures;
  }

  public Iterable<VariableElement> getCaptureFields(TypeElement type) {
    return Iterables.transform(
        Iterables.filter(getCaptures(type), Capture::hasField), Capture::getField);
  }

  public Iterable<VariableElement> getCapturedVars(TypeElement type) {
    return Iterables.transform(localCaptures.get(type), capture -> capture.var);
  }

  public Iterable<VariableElement> getLocalCaptureFields(TypeElement type) {
    List<LocalCapture> captures = localCaptures.get(type);
    return captures == null ? Collections.emptyList()
        : Iterables.transform(Iterables.filter(captures, Capture::hasField), Capture::getField);
  }

  public List<VariableElement> getImplicitEnumParams() {
    return implicitEnumParams;
  }

  /**
   * Returns all the implicit params that come before explicit params in a constructor.
   */
  public Iterable<VariableElement> getImplicitPrefixParams(TypeElement type) {
    return Iterables.transform(getCaptures(type), Capture::getParam);
  }

  /**
   * returns all the implicit params that come after explicit params in a constructor.
   */
  public Iterable<VariableElement> getImplicitPostfixParams(TypeElement type) {
    if (ElementUtil.isEnum(type)) {
      return implicitEnumParams;
    }
    return Collections.emptyList();
  }

  public boolean isCapturing(TypeElement type) {
    return !Iterables.isEmpty(Iterables.filter(getCaptures(type), Capture::hasField));
  }

  private static boolean automaticOuterParam(TypeElement type) {
    return ElementUtil.hasOuterContext(type) && !ElementUtil.isLocal(type);
  }

  private static TypeMirror getDeclaringType(TypeElement type) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(type);
    assert declaringClass != null : "Cannot find declaring class for " + type;
    return declaringClass.asType();
  }

  private String getOuterFieldName(TypeElement type) {
    // Ensure that the new outer field does not conflict with a field in a superclass.
    TypeElement typeElement = ElementUtil.getSuperclass(type);
    int suffix = 0;
    while (typeElement != null) {
      if (ElementUtil.hasOuterContext(typeElement)) {
        suffix++;
      }
      typeElement = ElementUtil.getSuperclass(typeElement);
    }
    return "this$" + suffix;
  }

  private String getCaptureFieldName(VariableElement var, TypeElement type) {
    int suffix = 0;
    while ((type = ElementUtil.getSuperclass(type)) != null && ElementUtil.isLocal(type)) {
      suffix++;
    }
    return "val" + (suffix > 0 ? suffix : "") + "$" + var.getSimpleName().toString();
  }

  private Capture getOrCreateOuterCapture(TypeElement type) {
    Capture capture = outerCaptures.get(type);
    if (capture == null) {
      capture = new Capture(
          GeneratedVariableElement.newParameter("outer$", getDeclaringType(type), type)
          .setNonnull(true));
      outerCaptures.put(type, capture);
    }
    return capture;
  }

  public VariableElement getOrCreateOuterParam(TypeElement type) {
    return getOrCreateOuterCapture(type).param;
  }

  public VariableElement getOrCreateOuterField(TypeElement type) {
    // Create the outer param since it is required to initialize the field.
    Capture capture = getOrCreateOuterCapture(type);
    if (capture.field == null) {
      capture.field = GeneratedVariableElement.newField(
          getOuterFieldName(type), getDeclaringType(type), type)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .setNonnull(true)
          .setIsWeak(typeUtil.elementUtil().isWeakOuterType(type));
    }
    return capture.field;
  }

  private LocalCapture getOrCreateLocalCapture(VariableElement var, TypeElement declaringType) {
    List<LocalCapture> capturesForType = localCaptures.get(declaringType);
    for (LocalCapture localCapture : capturesForType) {
      if (var.equals(localCapture.var)) {
        return localCapture;
      }
    }
    LocalCapture newCapture = new LocalCapture(var, GeneratedVariableElement.newParameter(
        "capture$" + capturesForType.size(), var.asType(), declaringType));
    capturesForType.add(newCapture);
    return newCapture;
  }

  public VariableElement getOrCreateCaptureParam(VariableElement var, TypeElement declaringType) {
    return getOrCreateLocalCapture(var, declaringType).param;
  }

  public VariableElement getOrCreateCaptureField(VariableElement var, TypeElement declaringType) {
    LocalCapture capture = getOrCreateLocalCapture(var, declaringType);
    if (capture.field == null) {
      capture.field = GeneratedVariableElement.newField(
          getCaptureFieldName(var, declaringType), var.asType(), declaringType)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .addAnnotationMirrors(var.getAnnotationMirrors());
    }
    return capture.field;
  }

  public void addMethodReferenceReceiver(TypeElement type, TypeMirror receiverType) {
    assert !outerCaptures.containsKey(type);
    Capture capture = new Capture(
        GeneratedVariableElement.newParameter("outer$", receiverType, type).setNonnull(true));
    capture.field = GeneratedVariableElement.newField("target$", receiverType, type)
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .setNonnull(true);
    receiverCaptures.put(type, capture);
  }
}
