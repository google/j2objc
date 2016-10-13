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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * CaptureInfo encapsulates all the implicitly captured fields and constructor params of inner and
 * local classes.
 */
public class CaptureInfo {

  private final Map<TypeElement, VariableElement> outerParams = new HashMap<>();
  private final Map<TypeElement, VariableElement> outerFields = new HashMap<>();
  private final Map<TypeElement, VariableElement> superOuterParams = new HashMap<>();
  private final ListMultimap<TypeElement, LocalCapture> localCaptures = ArrayListMultimap.create();

  /**
   * Information about a captured local variable.
   */
  public static class LocalCapture {

    private final VariableElement var;
    private final VariableElement param;
    private VariableElement field;

    private LocalCapture(VariableElement var, VariableElement param) {
      this.var = var;
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

  public boolean needsOuterReference(TypeElement type) {
    return outerFields.containsKey(type);
  }

  public boolean needsOuterParam(TypeElement type) {
    return outerParams.containsKey(type) || automaticOuterParam(type);
  }

  public VariableElement getOuterParam(TypeElement type) {
    return automaticOuterParam(type) ? getOrCreateOuterParam(type) : outerParams.get(type);
  }

  public VariableElement getSuperOuterParam(TypeElement type) {
    return superOuterParams.get(type);
  }

  public TypeMirror getOuterType(TypeElement type) {
    VariableElement outerField = outerFields.get(type);
    if (outerField != null) {
      return outerField.asType();
    }
    return getDeclaringType(type);
  }

  public VariableElement getOuterField(TypeElement type) {
    return outerFields.get(type);
  }

  public List<LocalCapture> getLocalCaptures(TypeElement type) {
    return Collections.unmodifiableList(localCaptures.get(type));
  }

  public Iterable<VariableElement> getCapturedVars(TypeElement type) {
    return Iterables.transform(localCaptures.get(type), capture -> capture.var);
  }

  public Iterable<VariableElement> getCaptureParams(TypeElement type) {
    return Iterables.transform(localCaptures.get(type), capture -> capture.param);
  }

  public Iterable<VariableElement> getCaptureFields(TypeElement type) {
    return Iterables.transform(Iterables.filter(
        localCaptures.get(type), LocalCapture::hasField), capture -> capture.field);
  }

  public boolean isCapturing(TypeElement type) {
    return outerFields.containsKey(type)
        || !Iterables.isEmpty(Iterables.filter(localCaptures.get(type), LocalCapture::hasField));
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

  public VariableElement getOrCreateOuterParam(TypeElement type) {
    VariableElement outerParam = outerParams.get(type);
    if (outerParam == null) {
      outerParam = new GeneratedVariableElement(
          "outer$", getDeclaringType(type), ElementKind.PARAMETER, type)
          .setNonnull(true);
      outerParams.put(type, outerParam);
    }
    return outerParam;
  }

  public VariableElement getOrCreateOuterField(TypeElement type) {
    // Create the outer param since it is required to initialize the field.
    getOrCreateOuterParam(type);
    VariableElement outerField = outerFields.get(type);
    if (outerField == null) {
      outerField = new GeneratedVariableElement(
          getOuterFieldName(type), getDeclaringType(type), ElementKind.FIELD, type)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .setNonnull(true);
      outerFields.put(type, outerField);
    }
    return outerField;
  }

  private LocalCapture getOrCreateLocalCapture(VariableElement var, TypeElement declaringType) {
    List<LocalCapture> capturesForType = localCaptures.get(declaringType);
    for (LocalCapture localCapture : capturesForType) {
      if (var.equals(localCapture.var)) {
        return localCapture;
      }
    }
    LocalCapture newCapture = new LocalCapture(var, new GeneratedVariableElement(
        "capture$" + capturesForType.size(), var.asType(), ElementKind.PARAMETER, declaringType));
    capturesForType.add(newCapture);
    return newCapture;
  }

  public VariableElement getOrCreateCaptureParam(VariableElement var, TypeElement declaringType) {
    return getOrCreateLocalCapture(var, declaringType).param;
  }

  public VariableElement getOrCreateCaptureField(VariableElement var, TypeElement declaringType) {
    LocalCapture capture = getOrCreateLocalCapture(var, declaringType);
    if (capture.field == null) {
      capture.field = new GeneratedVariableElement(
          getCaptureFieldName(var, declaringType), var.asType(), ElementKind.FIELD, declaringType)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .addAnnotationMirrors(var.getAnnotationMirrors());
    }
    return capture.field;
  }

  public VariableElement createSuperOuterParam(TypeElement type, TypeMirror superOuterType) {
    assert !superOuterParams.containsKey(type);
    VariableElement param = new GeneratedVariableElement(
        "superOuter$", superOuterType, ElementKind.PARAMETER, type)
        .setNonnull(true);
    superOuterParams.put(type, param);
    return param;
  }

  public void addMethodReferenceReceiver(TypeElement type, TypeMirror receiverType) {
    assert !outerParams.containsKey(type) && !outerFields.containsKey(type);
    // Add the target field as an outer field even though it's not really pointing to outer scope.
    outerParams.put(type, new GeneratedVariableElement(
        "outer$", receiverType, ElementKind.PARAMETER, type)
        .setNonnull(true));
    outerFields.put(type, new GeneratedVariableElement(
        "target$", receiverType, ElementKind.FIELD, type)
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .setNonnull(true));
  }
}
