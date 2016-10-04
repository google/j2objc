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
import com.google.devtools.j2objc.jdt.JdtTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.ParserEnvironment;
import java.util.HashMap;
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
  private final ITypeBinding javaThrowableType;
  private final ITypeBinding javaVoidType;

  // Lazily load localRefType, since its initialization requires Types to be fully initialized.
  private ITypeBinding localRefType;

  // Non-standard naming pattern is used, since in this case it's more readable.
  private final IOSTypeBinding NSCopying;
  private final IOSTypeBinding NSObject;
  private final IOSTypeBinding NSNumber;
  private final IOSTypeBinding NSString;
  private final IOSTypeBinding NSException;
  private final IOSTypeBinding IOSClass;

  private IOSTypeBinding IOSObjectArray;

  // Special IOS types.
  private final IOSTypeBinding idType;

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
  private final IOSMethodBinding allocMethod;
  private final IOSMethodBinding deallocMethod;

  public Types(ParserEnvironment env) {
    this.env = env;

    // Find core java types.
    javaObjectType = resolveWellKnownType("java.lang.Object");
    javaClassType = resolveWellKnownType("java.lang.Class");
    javaCloneableType = resolveWellKnownType("java.lang.Cloneable");
    javaStringType = resolveWellKnownType("java.lang.String");
    javaThrowableType = resolveWellKnownType("java.lang.Throwable");
    javaVoidType = resolveWellKnownType("java.lang.Void");
    ITypeBinding binding = resolveWellKnownType("java.lang.Integer");
    javaNumberType = binding.getSuperclass();

    // Create core IOS types.
    NSCopying = mapIOSType(IOSTypeBinding.newInterface("NSCopying", javaCloneableType));
    NSObject = mapIOSType(IOSTypeBinding.newClass("NSObject", javaObjectType));
    NSNumber = mapIOSType(IOSTypeBinding.newClass("NSNumber", javaNumberType, NSObject));
    NSString = mapIOSType(IOSTypeBinding.newClass("NSString", javaStringType, NSObject));
    NSException = mapIOSType(IOSTypeBinding.newClass("NSException", javaThrowableType, NSObject));
    IOSClass = mapIOSType(IOSTypeBinding.newUnmappedClass("IOSClass"));
    mapIOSType(IOSTypeBinding.newUnmappedClass("NSZone"));
    idType = mapIOSType(IOSTypeBinding.newUnmappedClass("id"));

    initializeArrayTypes();
    initializeTypeMap();
    initializeCommonJavaTypes();
    populatePrimitiveAndWrapperTypeMaps();

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
    typeMap.put(javaNumberType, NSNumber);
    typeMap.put(javaStringType, NSString);
    typeMap.put(javaThrowableType, NSException);
  }

  private void initializeCommonJavaTypes() {
    ITypeBinding charSequence = BindingUtil.findInterface(javaStringType, "java.lang.CharSequence");
    javaBindingMap.put("java.lang.CharSequence", charSequence);
    iosBindingMap.put("JavaLangCharSequence", charSequence);
    javaBindingMap.put("java.lang.Number", javaNumberType);
    javaBindingMap.put("java.lang.Throwable", javaThrowableType);
  }

  private void initializePrimitiveArray(String javaTypeName, String iosTypeName) {
    ITypeBinding javaType = resolveWellKnownType(javaTypeName);
    IOSTypeBinding iosType = mapIOSType(IOSTypeBinding.newUnmappedClass(iosTypeName));
    iosType.setHeader("IOSPrimitiveArray.h");
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

  private JdtTypeBinding resolveWellKnownType(String name) {
    return (JdtTypeBinding) BindingConverter.unwrapElement(env.resolve(name));
  }

  private void loadPrimitiveAndWrapperTypes(String primitiveName, String wrapperName) {
    ITypeBinding primitive = resolveWellKnownType(primitiveName);
    ITypeBinding wrapper = resolveWellKnownType(wrapperName);
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
    // getTypeDeclaration will return the canonical binding for the type with
    // type parameters and type annotations removed. Note that getErasure() does
    // not strip type annotations.
    binding = binding.getTypeDeclaration();
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
    ITypeBinding binding = resolveWellKnownType(typeName);
    return mapType(binding);
  }

  /**
   * Returns whether a given type has an iOS equivalent.
   */
  public boolean hasIOSEquivalent(ITypeBinding binding) {
    return binding.isArray() || typeMap.containsKey(binding.getTypeDeclaration());
  }

  public TypeMirror resolveJavaTypeMirror(String name) {
    return BindingConverter.getType(resolveJavaType(name));
  }

  public ITypeBinding resolveJavaType(String name) {
    ITypeBinding result = javaBindingMap.get(name);
    if (result == null) {
      result = resolveWellKnownType(name);
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

  public boolean isJavaStringType(TypeMirror type) {
    return javaStringType.equals(BindingConverter.unwrapTypeMirrorIntoTypeBinding(type));
  }

  public boolean isStringType(ITypeBinding type) {
    return javaStringType.isEqualTo(type) || NSString.isEqualTo(type);
  }

  public boolean isIdType(ITypeBinding type) {
    return type == idType || type == NSObject || type == javaObjectType;
  }

  public IOSTypeBinding resolveArrayType(ITypeBinding binding) {
    IOSTypeBinding arrayBinding = arrayBindingMap.get(binding);
    return arrayBinding != null ? arrayBinding : IOSObjectArray;
  }

  public TypeElement resolveArrayType(TypeMirror mirror) {
    IOSTypeBinding arrayBinding = arrayBindingMap.get(
        BindingConverter.unwrapTypeMirrorIntoBinding(mirror));
    return BindingConverter.getTypeElement(arrayBinding != null ? arrayBinding : IOSObjectArray);
  }

  public TypeElement getObjectArrayElement() {
    return BindingConverter.getTypeElement(IOSObjectArray);
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

  public TypeMirror getPrimitiveType(TypeMirror wrapperType) {
    return BindingConverter.getType(wrapperToPrimitiveTypes.get(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(wrapperType)));
  }

  public boolean isBoxedPrimitive(TypeElement type) {
    return isBoxedPrimitive(BindingConverter.unwrapTypeElement(type));
  }

  public boolean isBoxedPrimitive(ITypeBinding type) {
    return wrapperToPrimitiveTypes.containsKey(type);
  }

  public boolean isBoxedPrimitive(TypeMirror type) {
    return wrapperToPrimitiveTypes.containsKey(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(type));
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

  public TypeElement getJavaObjectElement() {
    return BindingConverter.getTypeElement(javaObjectType);
  }

  public ITypeBinding getNSString() {
    return NSString;
  }

  public ITypeBinding getIOSClass() {
    return IOSClass;
  }

  public TypeMirror getIOSClassMirror() {
    return BindingConverter.getType(IOSClass);
  }

  public ITypeBinding getIdType() {
    return idType;
  }

  public TypeMirror getIdTypeMirror() {
    return BindingConverter.getType(idType);
  }

  public ITypeBinding getIOSObjectArray() {
    return IOSObjectArray;
  }

  public PointerTypeBinding getPointerType(ITypeBinding type) {
    PointerTypeBinding result = pointerTypeMap.get(type);
    if (result == null) {
      result = new PointerTypeBinding(type);
      pointerTypeMap.put(type, result);
    }
    return result;
  }

  public TypeMirror getPointerType(TypeMirror type) {
    return BindingConverter.getType(getPointerType(
        BindingConverter.unwrapTypeMirrorIntoTypeBinding(type)));
  }

  public ITypeBinding getLocalRefType() {
    if (localRefType == null) {
      ITypeBinding objectType = resolveWellKnownType("java.lang.Object");
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

  public IOSMethodBinding getAllocMethod() {
    return allocMethod;
  }

  public IOSMethodBinding getDeallocMethod() {
    return deallocMethod;
  }
}
