/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.types;

import com.google.common.collect.Maps;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.ParserEnvironment;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Types is a singleton service class for type-related operations.
 *
 * @author Tom Ball
 */
public class Types {

  private final ParserEnvironment env;
  private final TypeUtil typeUtil;
  private final Map<ITypeBinding, ITypeBinding> typeMap = Maps.newHashMap();

  // Commonly used java types.
  private final ITypeBinding javaObjectType;
  private final ITypeBinding javaClassType;
  private final ITypeBinding javaCloneableType;
  private final ITypeBinding javaNumberType;
  private final ITypeBinding javaStringType;
  private final ITypeBinding javaThrowableType;

  // Non-standard naming pattern is used, since in this case it's more readable.
  private final ITypeBinding NSCopying;
  private final ITypeBinding NSObject;
  private final ITypeBinding NSNumber;
  private final ITypeBinding NSString;
  private final ITypeBinding NSException;
  private final ITypeBinding IOSClass;

  // Special IOS types.
  private final ITypeBinding idType;

  private final Map<String, ITypeBinding> javaBindingMap = Maps.newHashMap();

  // Commonly used methods.
  private final IOSMethodBinding retainMethod;
  private final IOSMethodBinding releaseMethod;
  private final IOSMethodBinding autoreleaseMethod;
  private final IOSMethodBinding allocMethod;
  private final IOSMethodBinding deallocMethod;

  public Types(ParserEnvironment env, TypeUtil typeUtil) {
    this.env = env;
    this.typeUtil = typeUtil;

    // Find core java types.
    javaObjectType = resolveWellKnownType("java.lang.Object");
    javaClassType = resolveWellKnownType("java.lang.Class");
    javaCloneableType = resolveWellKnownType("java.lang.Cloneable");
    javaStringType = resolveWellKnownType("java.lang.String");
    javaThrowableType = resolveWellKnownType("java.lang.Throwable");
    ITypeBinding binding = resolveWellKnownType("java.lang.Integer");
    javaNumberType = binding.getSuperclass();

    // Create core IOS types.
    NSCopying = BindingConverter.unwrapTypeElement(TypeUtil.NS_COPYING);
    NSObject = BindingConverter.unwrapTypeElement(TypeUtil.NS_OBJECT);
    NSNumber = BindingConverter.unwrapTypeElement(TypeUtil.NS_NUMBER);
    NSString = BindingConverter.unwrapTypeElement(TypeUtil.NS_STRING);
    NSException = BindingConverter.unwrapTypeElement(TypeUtil.NS_EXCEPTION);
    IOSClass = BindingConverter.unwrapTypeElement(TypeUtil.IOS_CLASS);
    idType = BindingConverter.unwrapTypeMirrorIntoTypeBinding(TypeUtil.ID_TYPE);

    initializeTypeMap();

    ITypeBinding voidType = resolveWellKnownType("void");

    // Commonly used methods.
    retainMethod = IOSMethodBinding.newMethod(
        NameTable.RETAIN_METHOD, Modifier.PUBLIC, idType, NSObject);
    releaseMethod = IOSMethodBinding.newMethod(
        NameTable.RELEASE_METHOD, Modifier.PUBLIC, voidType, NSObject);
    autoreleaseMethod = IOSMethodBinding.newMethod(
        NameTable.AUTORELEASE_METHOD, Modifier.PUBLIC, idType, NSObject);
    allocMethod = IOSMethodBinding.newMethod(
        NameTable.ALLOC_METHOD, Modifier.PUBLIC, idType, NSObject);
    deallocMethod = IOSMethodBinding.newMethod(
        NameTable.DEALLOC_METHOD, Modifier.PUBLIC, idType, NSObject);
  }

  /**
   * Initialize type map with classes that are explicitly mapped to an iOS
   * type.
   *
   * NOTE: if this method's list is changed, IOSClass.forName() needs to be
   * similarly updated.
   */
  private void initializeTypeMap() {
    typeMap.put(javaObjectType, NSObject);
    typeMap.put(javaClassType, IOSClass);
    typeMap.put(javaCloneableType, NSCopying);
    typeMap.put(javaNumberType, NSNumber);
    typeMap.put(javaStringType, NSString);
    typeMap.put(javaThrowableType, NSException);
  }

  private ITypeBinding resolveWellKnownType(String name) {
    return BindingConverter.unwrapTypeElement((TypeElement) env.resolve(name));
  }

  /**
   * Given a JDT type binding created by the parser, either replace it with an iOS
   * equivalent, or return the given type.
   */
  public ITypeBinding mapType(ITypeBinding binding) {
    if (binding == null) {  // happens when mapping a primitive type
      return null;
    }
    // getTypeDeclaration will return the canonical binding for the type with
    // type parameters and type annotations removed. Note that getErasure() does
    // not strip type annotations.
    binding = binding.getTypeDeclaration();
    if (binding.isArray()) {
      return BindingConverter.unwrapTypeElement(
          typeUtil.getIosArray(BindingConverter.getType(binding.getComponentType())));
    }
    ITypeBinding newBinding = typeMap.get(binding);
    if (newBinding == null && binding.isAssignmentCompatible(javaClassType)) {
      newBinding = typeMap.get(javaClassType);
    }
    return newBinding != null ? newBinding : binding;
  }

  public ITypeBinding resolveJavaType(String name) {
    ITypeBinding result = javaBindingMap.get(name);
    if (result == null) {
      result = resolveWellKnownType(name);
    }
    return result;
  }

  public boolean isJavaStringType(ITypeBinding type) {
    return javaStringType.equals(type);
  }

  public boolean isJavaStringType(TypeMirror type) {
    return javaStringType.equals(BindingConverter.unwrapTypeMirrorIntoTypeBinding(type));
  }

  public boolean isStringType(TypeMirror type) {
    return isStringType(BindingConverter.unwrapTypeMirrorIntoTypeBinding(type));
  }

  public boolean isStringType(ITypeBinding type) {
    return javaStringType.isEqualTo(type) || NSString.isEqualTo(type);
  }

  public boolean isIdType(ITypeBinding type) {
    return type == idType || type == NSObject || type == javaObjectType
        || (type instanceof NativeTypeBinding && type.getName().equals("id"));
  }

  // Used by SignatureGenerator. Other classes should use getNSObject().
  public ITypeBinding getJavaObject() {
    return javaObjectType;
  }

  public ITypeBinding getIdType() {
    return idType;
  }

  public IOSMethodBinding getRetainMethod() {
    return retainMethod;
  }

  public IOSMethodBinding getReleaseMethod() {
    return releaseMethod;
  }

  public IOSMethodBinding getAutoreleaseMethod() {
    return autoreleaseMethod;
  }

  public IOSMethodBinding getAllocMethod() {
    return allocMethod;
  }

  public IOSMethodBinding getDeallocMethod() {
    return deallocMethod;
  }
}
