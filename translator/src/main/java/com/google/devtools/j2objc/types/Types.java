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
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Types is a singleton service class for type-related operations.
 *
 * @author Tom Ball
 */
public class Types {

  private final AST ast;
  private final Map<ITypeBinding, ITypeBinding> typeMap = Maps.newHashMap();
  private final Map<ITypeBinding, ITypeBinding> primitiveToWrapperTypes =
      new HashMap<ITypeBinding, ITypeBinding>();
  private final Map<ITypeBinding, ITypeBinding> wrapperToPrimitiveTypes =
      new HashMap<ITypeBinding, ITypeBinding>();

  // Commonly used java types.
  private final ITypeBinding javaObjectType;
  private final ITypeBinding javaClassType;
  private final ITypeBinding javaCloneableType;
  private final ITypeBinding javaNumberType;
  private final ITypeBinding javaStringType;
  private final ITypeBinding javaVoidType;

  // Lazily load localRefType, since its initialization requires Types to be fully initialized.
  private ITypeBinding localRefType;

  // Non-standard naming pattern is used, since in this case it's more readable.
  private final IOSTypeBinding NSCopying;
  private final IOSTypeBinding NSObject;
  private final IOSTypeBinding NSNumber;
  private final IOSTypeBinding NSString;
  private final IOSTypeBinding IOSClass;

  private IOSTypeBinding IOSObjectArray;

  private final Map<String, ITypeBinding> javaBindingMap = Maps.newHashMap();
  private final Map<String, ITypeBinding> iosBindingMap = Maps.newHashMap();

  // Map a primitive type to its emulation array type.
  private final Map<ITypeBinding, IOSTypeBinding> arrayBindingMap = Maps.newHashMap();

  // Cache of pointer types.
  private final Map<ITypeBinding, PointerTypeBinding> pointerTypeMap = Maps.newHashMap();

  // Commonly used methods.
  private final IOSMethodBinding retainMethod;
  private final IOSMethodBinding releaseMethod;
  private final IOSMethodBinding autoreleaseMethod;

  public Types(AST ast) {
    this.ast = ast;

    // Find core java types.
    javaObjectType = ast.resolveWellKnownType("java.lang.Object");
    javaClassType = ast.resolveWellKnownType("java.lang.Class");
    javaCloneableType = ast.resolveWellKnownType("java.lang.Cloneable");
    javaStringType = ast.resolveWellKnownType("java.lang.String");
    javaVoidType = ast.resolveWellKnownType("java.lang.Void");
    ITypeBinding binding = ast.resolveWellKnownType("java.lang.Integer");
    javaNumberType = binding.getSuperclass();

    // Create core IOS types.
    NSCopying = mapIOSType(IOSTypeBinding.newInterface("NSCopying", javaCloneableType));
    NSObject = mapIOSType(IOSTypeBinding.newClass("NSObject", javaObjectType));
    NSNumber = mapIOSType(IOSTypeBinding.newClass("NSNumber", javaNumberType, NSObject));
    NSString = mapIOSType(IOSTypeBinding.newClass("NSString", javaStringType, NSObject));
    IOSClass = mapIOSType(IOSTypeBinding.newUnmappedClass("IOSClass"));
    IOSTypeBinding idType = mapIOSType(IOSTypeBinding.newUnmappedClass("id"));
    mapIOSType(IOSTypeBinding.newUnmappedClass("NSZone"));

    initializeArrayTypes();
    initializeTypeMap();
    initializeCommonJavaTypes();
    populatePrimitiveAndWrapperTypeMaps();

    ITypeBinding voidType = ast.resolveWellKnownType("void");

    // Commonly used methods.
    retainMethod = IOSMethodBinding.newMethod(
        NameTable.RETAIN_METHOD, Modifier.PUBLIC, idType, NSObject);
    releaseMethod = IOSMethodBinding.newMethod(
        NameTable.RELEASE_METHOD, Modifier.PUBLIC, voidType, NSObject);
    autoreleaseMethod = IOSMethodBinding.newMethod(
        NameTable.AUTORELEASE_METHOD, Modifier.PUBLIC, idType, NSObject);
  }

  private IOSTypeBinding mapIOSType(IOSTypeBinding type) {
    iosBindingMap.put(type.getName(), type);
    return type;
  }

  private void initializeArrayTypes() {
    initializePrimitiveArray("boolean", "IOSBooleanArray");
    initializePrimitiveArray("byte", "IOSByteArray");
    initializePrimitiveArray("char", "IOSCharArray");
    initializePrimitiveArray("double", "IOSDoubleArray");
    initializePrimitiveArray("float", "IOSFloatArray");
    initializePrimitiveArray("int", "IOSIntArray");
    initializePrimitiveArray("long", "IOSLongArray");
    initializePrimitiveArray("short", "IOSShortArray");
    IOSObjectArray = mapIOSType(IOSTypeBinding.newUnmappedClass("IOSObjectArray"));
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
    typeMap.put(javaStringType, NSString);
    typeMap.put(javaNumberType, NSNumber);
  }

  private void initializeCommonJavaTypes() {
    ITypeBinding charSequence = BindingUtil.findInterface(javaStringType, "java.lang.CharSequence");
    javaBindingMap.put("java.lang.CharSequence", charSequence);
    iosBindingMap.put("JavaLangCharSequence", charSequence);
    javaBindingMap.put("java.lang.Number", javaNumberType);
  }

  private void initializePrimitiveArray(String javaTypeName, String iosTypeName) {
    ITypeBinding javaType = ast.resolveWellKnownType(javaTypeName);
    IOSTypeBinding iosType = mapIOSType(IOSTypeBinding.newUnmappedClass(iosTypeName));
    iosType.setHeader("IOSPrimitiveArray");
    arrayBindingMap.put(javaType, iosType);
  }

  private void populatePrimitiveAndWrapperTypeMaps() {
    loadPrimitiveAndWrapperTypes("boolean", "java.lang.Boolean");
    loadPrimitiveAndWrapperTypes("byte", "java.lang.Byte");
    loadPrimitiveAndWrapperTypes("char", "java.lang.Character");
    loadPrimitiveAndWrapperTypes("short", "java.lang.Short");
    loadPrimitiveAndWrapperTypes("int", "java.lang.Integer");
    loadPrimitiveAndWrapperTypes("long", "java.lang.Long");
    loadPrimitiveAndWrapperTypes("float", "java.lang.Float");
    loadPrimitiveAndWrapperTypes("double", "java.lang.Double");
    loadPrimitiveAndWrapperTypes("void", "java.lang.Void");
  }

  private void loadPrimitiveAndWrapperTypes(String primitiveName, String wrapperName) {
    ITypeBinding primitive = ast.resolveWellKnownType(primitiveName);
    ITypeBinding wrapper = ast.resolveWellKnownType(wrapperName);
    primitiveToWrapperTypes.put(primitive, wrapper);
    wrapperToPrimitiveTypes.put(wrapper, primitive);
  }

  /**
   * Given a JDT type binding created by the parser, either replace it with an iOS
   * equivalent, or return the given type.
   */
  public ITypeBinding mapType(ITypeBinding binding) {
    if (binding == null) {  // happens when mapping a primitive type
      return null;
    }
    if (binding.isArray()) {
      return resolveArrayType(binding.getComponentType());
    }
    ITypeBinding newBinding = typeMap.get(binding);
    if (newBinding == null && binding.isAssignmentCompatible(javaClassType)) {
      newBinding = typeMap.get(javaClassType);
    }
    return newBinding != null ? newBinding : binding;
  }

  /**
   * Given a fully-qualified type name, return its binding.
   */
  public ITypeBinding mapTypeName(String typeName) {
    ITypeBinding binding = ast.resolveWellKnownType(typeName);
    return mapType(binding);
  }

  /**
   * Returns whether a given type has an iOS equivalent.
   */
  public boolean hasIOSEquivalent(ITypeBinding binding) {
    return binding.isArray() || typeMap.containsKey(binding.getTypeDeclaration());
  }

  public ITypeBinding resolveJavaType(String name) {
    ITypeBinding result = javaBindingMap.get(name);
    if (result == null) {
      result = ast.resolveWellKnownType(name);
    }
    return result;
  }

  public ITypeBinding resolveIOSType(String name) {
    return iosBindingMap.get(name);
  }

  public boolean isJavaObjectType(ITypeBinding type) {
    return javaObjectType.equals(type);
  }

  public boolean isJavaStringType(ITypeBinding type) {
    return javaStringType.equals(type);
  }

  public boolean isStringType(ITypeBinding type) {
    return javaStringType.isEqualTo(type) || NSString.isEqualTo(type);
  }

  public IOSTypeBinding resolveArrayType(ITypeBinding binding) {
    IOSTypeBinding arrayBinding = arrayBindingMap.get(binding);
    return arrayBinding != null ? arrayBinding : IOSObjectArray;
  }

  public boolean isJavaVoidType(ITypeBinding type) {
    return type.isEqualTo(javaVoidType);
  }

  public ITypeBinding getWrapperType(ITypeBinding primitiveType) {
    return primitiveToWrapperTypes.get(primitiveType);
  }

  public ITypeBinding getPrimitiveType(ITypeBinding wrapperType) {
    return wrapperToPrimitiveTypes.get(wrapperType);
  }

  public boolean isBoxedPrimitive(ITypeBinding type) {
    return wrapperToPrimitiveTypes.containsKey(type);
  }

  public ITypeBinding getNSNumber() {
    return NSNumber;
  }

  public ITypeBinding getNSObject() {
    return NSObject;
  }

  // Used by SignatureGenerator. Other classes should use getNSObject().
  public ITypeBinding getJavaObject() {
    return javaObjectType;
  }

  public ITypeBinding getNSString() {
    return NSString;
  }

  public ITypeBinding getIOSClass() {
    return IOSClass;
  }

  public PointerTypeBinding getPointerType(ITypeBinding type) {
    PointerTypeBinding result = pointerTypeMap.get(type);
    if (result == null) {
      result = new PointerTypeBinding(type);
      pointerTypeMap.put(type, result);
    }
    return result;
  }

  public ITypeBinding getLocalRefType() {
    if (localRefType == null) {
      ITypeBinding objectType = ast.resolveWellKnownType("java.lang.Object");
      GeneratedTypeBinding refType =
          GeneratedTypeBinding.newTypeBinding("com.google.j2objc.util.ScopedLocalRef",
          objectType, false);
      GeneratedVariableBinding varBinding = new GeneratedVariableBinding("var", Modifier.PUBLIC,
          objectType, true, false, refType, null);
      refType.addField(varBinding);
      GeneratedMethodBinding constructor =
          GeneratedMethodBinding.newConstructor(refType, Modifier.PUBLIC, this);
      constructor.addParameter(objectType);
      refType.addMethod(constructor);
      localRefType = refType;
    }
    return localRefType;
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
}
