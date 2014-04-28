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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Types is a singleton service class for type-related operations.
 *
 * @author Tom Ball
 */
// TODO(tball): convert to injectable implementation, to allow translator
// core to be reused for other languages.
public class Types {
  private final AST ast;
  private final Map<Object, IBinding> bindingMap;
  private final Map<ITypeBinding, ITypeBinding> typeMap = Maps.newHashMap();
  private final Map<ITypeBinding, ITypeBinding> renamedTypeMap = Maps.newHashMap();
  private final Map<ITypeBinding, ITypeBinding> primitiveToWrapperTypes =
      new HashMap<ITypeBinding, ITypeBinding>();
  private final Map<ITypeBinding, ITypeBinding> wrapperToPrimitiveTypes =
      new HashMap<ITypeBinding, ITypeBinding>();
  private final List<IVariableBinding> releaseableFields = Lists.newArrayList();
  private final ITypeBinding javaObjectType;
  private final ITypeBinding javaClassType;
  private final ITypeBinding javaCloneableType;
  private final ITypeBinding javaNumberType;
  private final ITypeBinding javaStringType;
  private final ITypeBinding javaVoidType;
  private final ITypeBinding voidType;
  private final ITypeBinding booleanType;

  // Lazily load localRefType, since its initialization requires Types to be fully initialized.
  private ITypeBinding localRefType;

  private static Types instance;

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

  private final Set<Block> autoreleasePoolBlocks = Sets.newHashSet();
  private final Set<Expression> nilChecks = Sets.newHashSet();
  private final Set<Expression> deferredFieldSetters = Sets.newHashSet();

  // The first argument of a iOS method isn't named, but Java requires some sort of valid parameter
  // name.  The method mapper therefore uses this string, which the generators ignore.
  public static final String EMPTY_PARAMETER_NAME = "__empty_parameter__";

  private Types(CompilationUnit unit) {
    ast = unit.getAST();

    // Find core java types.
    javaObjectType = ast.resolveWellKnownType("java.lang.Object");
    javaClassType = ast.resolveWellKnownType("java.lang.Class");
    javaCloneableType = ast.resolveWellKnownType("java.lang.Cloneable");
    javaStringType = ast.resolveWellKnownType("java.lang.String");
    javaVoidType = ast.resolveWellKnownType("java.lang.Void");
    voidType = ast.resolveWellKnownType("void");
    booleanType = ast.resolveWellKnownType("boolean");
    ITypeBinding binding = ast.resolveWellKnownType("java.lang.Integer");
    javaNumberType = binding.getSuperclass();

    // Create core IOS types.
    NSCopying = mapIOSType(IOSTypeBinding.newInterface("NSCopying", javaCloneableType));
    NSObject = mapIOSType(IOSTypeBinding.newClass("NSObject", javaObjectType));
    NSNumber = mapIOSType(IOSTypeBinding.newClass("NSNumber", javaNumberType, NSObject));
    NSString = mapIOSType(IOSTypeBinding.newClass("NSString", javaStringType, NSObject));
    IOSClass = mapIOSType(IOSTypeBinding.newUnmappedClass("IOSClass"));
    mapIOSType(IOSTypeBinding.newUnmappedClass("id"));
    mapIOSType(IOSTypeBinding.newUnmappedClass("NSZone"));

    initializeArrayTypes();
    initializeTypeMap();
    initializeCommonJavaTypes();
    populatePrimitiveAndWrapperTypeMaps();
    bindingMap = BindingMapBuilder.buildBindingMap(unit);
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
   * Initialize this service using the AST returned by the parser.
   */
  public static void initialize(CompilationUnit unit) {
    instance = new Types(unit);
  }

  public static void cleanup() {
    instance = null;
  }

  /**
   * Given a JDT type binding created by the parser, either replace it with an iOS
   * equivalent, or return the given type.
   */
  public static ITypeBinding mapType(ITypeBinding binding) {
    if (binding == null) {  // happens when mapping a primitive type
      return null;
    }
    if (binding.isArray()) {
      return resolveArrayType(binding.getComponentType());
    }
    ITypeBinding newBinding = instance.typeMap.get(binding);
    if (newBinding == null && binding.isAssignmentCompatible(instance.javaClassType)) {
      newBinding = instance.typeMap.get(instance.javaClassType);
    }
    return newBinding != null ? newBinding : binding;
  }

  /**
   * Given a fully-qualified type name, return its binding.
   */
  public static ITypeBinding mapTypeName(String typeName) {
    ITypeBinding binding = instance.ast.resolveWellKnownType(typeName);
    return mapType(binding);
  }

  /**
   * Returns whether a given type has an iOS equivalent.
   */
  public static boolean hasIOSEquivalent(ITypeBinding binding) {
    return binding.isArray() || instance.typeMap.containsKey(binding.getTypeDeclaration());
  }

  public static ITypeBinding resolveJavaType(String name) {
    ITypeBinding result = instance.javaBindingMap.get(name);
    if (result == null) {
      result = instance.ast.resolveWellKnownType(name);
    }
    return result;
  }

  public static ITypeBinding resolveIOSType(String name) {
    return instance.iosBindingMap.get(name);
  }

  public static boolean isJavaObjectType(ITypeBinding type) {
    return instance.javaObjectType.equals(type);
  }

  public static boolean isJavaStringType(ITypeBinding type) {
    return instance.javaStringType.equals(type);
  }

  public static boolean isFloatingPointType(ITypeBinding type) {
    return type.isEqualTo(instance.ast.resolveWellKnownType("double")) ||
        type.isEqualTo(instance.ast.resolveWellKnownType("float")) ||
        type == instance.ast.resolveWellKnownType("java.lang.Double") ||
        type == instance.ast.resolveWellKnownType("java.lang.Float");
  }

  public static boolean isBooleanType(ITypeBinding type) {
    return type.isEqualTo(instance.booleanType) ||
        type == instance.ast.resolveWellKnownType("java.lang.Boolean");
  }

  public static boolean isLongType(ITypeBinding type) {
    return type.isEqualTo(instance.ast.resolveWellKnownType("long")) ||
        type == instance.ast.resolveWellKnownType("java.lang.Long");
  }

  public static ITypeBinding resolveIOSType(Type type) {
    if (type instanceof SimpleType) {
      String name = ((SimpleType) type).getName().getFullyQualifiedName();
      return resolveIOSType(name);
    }
    return null;
  }

  public static IOSTypeBinding resolveArrayType(ITypeBinding binding) {
    IOSTypeBinding arrayBinding = instance.arrayBindingMap.get(binding);
    return arrayBinding != null ? arrayBinding : instance.IOSObjectArray;
  }

  public static IBinding getBinding(Object node) {
    IBinding binding = instance.bindingMap.get(node);
    assert binding != null;
    return binding;
  }

  public static void addBinding(Object node, IBinding binding) {
    assert binding != null;
    instance.bindingMap.put(node, binding);
  }

  /**
   * Return a type binding for a specified ASTNode or IOS node, or null if
   * no type binding exists.
   */
  public static ITypeBinding getTypeBinding(Object node) {
    IBinding binding = getBinding(node);
    if (binding instanceof ITypeBinding) {
      return (ITypeBinding) binding;
    } else if (binding instanceof IMethodBinding) {
      IMethodBinding m = (IMethodBinding) binding;
      return m.isConstructor() ? m.getDeclaringClass() : m.getReturnType();
    } else if (binding instanceof IVariableBinding) {
      return ((IVariableBinding) binding).getType();
    } else if (binding instanceof IAnnotationBinding) {
      return ((IAnnotationBinding) binding).getAnnotationType();
    }
    return null;
  }

  /**
   * Return a type binding for a specified ASTNode or IOS node, or null if
   * no type binding exists.
   */
  public static IAnnotationBinding getAnnotationBinding(Object node) {
    IBinding binding = getBinding(node);
    if (binding instanceof IAnnotationBinding) {
      return (IAnnotationBinding) binding;
    }
    return null;
  }

  public static IMethodBinding getMethodBinding(Object node) {
    IBinding binding = getBinding(node);
    return binding instanceof IMethodBinding ? ((IMethodBinding) binding) : null;
  }

  /**
   * Gets a GeneratedMethodBinding for an ASTNode, replacing the IMethodBinding
   * if there is one.
   */
  public static GeneratedMethodBinding getGeneratedMethodBinding(Object node) {
    IMethodBinding binding = getMethodBinding(node);
    if (binding == null) {
      return null;
    }
    // We must create a copy since the caller expects to mutate the binding and
    // we need to avoid side-effect for other nodes that refer to the same
    // binding.
    GeneratedMethodBinding newBinding = new GeneratedMethodBinding(binding.getMethodDeclaration());
    addBinding(node, newBinding);
    return newBinding;
  }

  public static IVariableBinding getVariableBinding(Object node) {
    IBinding binding = getBinding(node);
    return binding instanceof IVariableBinding ? ((IVariableBinding) binding) : null;
  }

  /**
   * Walks an AST and asserts there is a resolved binding for every
   * ASTNode type that is supposed to have one.
   */
  public static void verifyNode(ASTNode node) {
    BindingMapVerifier.verify(node, instance.bindingMap);
  }

  public static void verifyNodes(List<? extends ASTNode> nodes) {
    for (ASTNode node : nodes) {
      BindingMapVerifier.verify(node, instance.bindingMap);
    }
  }

  public static ITypeBinding renameTypeBinding(String newName, ITypeBinding newDeclaringClass,
      ITypeBinding originalBinding) {
    ITypeBinding renamedBinding =
        RenamedTypeBinding.rename(newName, newDeclaringClass, originalBinding);
    instance.renamedTypeMap.put(originalBinding, renamedBinding);
    return renamedBinding;
  }

  public static ITypeBinding getRenamedBinding(ITypeBinding original) {
    return original != null && instance.renamedTypeMap.containsKey(original)
        ? instance.renamedTypeMap.get(original) : original;
  }

  public static boolean isVoidType(Type type) {
    return isVoidType(getTypeBinding(type));
  }

  public static boolean isVoidType(ITypeBinding type) {
    return type.isEqualTo(instance.voidType);
  }

  public static boolean isJavaVoidType(ITypeBinding type) {
    return type.isEqualTo(instance.javaVoidType);
  }

  public static void addReleaseableFields(Collection<IVariableBinding> fields) {
    for (IVariableBinding field : fields) {
      instance.releaseableFields.add(field.getVariableDeclaration());
    }
  }

  public static boolean isReleaseableField(IVariableBinding var) {
    return var != null ? instance.releaseableFields.contains(var.getVariableDeclaration()) : false;
  }

  public static ITypeBinding getWrapperType(ITypeBinding primitiveType) {
    return instance.primitiveToWrapperTypes.get(primitiveType);
  }

  public static ITypeBinding getPrimitiveType(ITypeBinding wrapperType) {
    return instance.wrapperToPrimitiveTypes.get(wrapperType);
  }

  public static boolean isBoxedPrimitive(ITypeBinding type) {
    return instance.wrapperToPrimitiveTypes.containsKey(type);
  }

  public static ITypeBinding getNSNumber() {
    return instance.NSNumber;
  }

  public static ITypeBinding getNSObject() {
    return instance.NSObject;
  }

  public static ITypeBinding getNSString() {
    return instance.NSString;
  }

  public static ITypeBinding getIOSClass() {
    return instance.IOSClass;
  }

  public static void addAutoreleasePool(Block block) {
    if (Options.useGC()) {
      ErrorUtil.warning(block, "@AutoreleasePool ignored in GC mode");
    }
    instance.autoreleasePoolBlocks.add(block);
  }

  public static boolean hasAutoreleasePool(Block block) {
    return instance.autoreleasePoolBlocks.contains(block);
  }

  public static void addNilCheck(Expression expression) {
    instance.nilChecks.add(expression);
  }

  public static boolean hasNilCheck(Expression expression) {
    return instance.nilChecks.contains(expression);
  }

  public static void addDeferredFieldSetter(Expression expression) {
    instance.deferredFieldSetters.add(expression);
  }

  public static boolean hasDeferredFieldSetter(Expression expression) {
    return instance.deferredFieldSetters.contains(expression);
  }

  public static ITypeBinding getLocalRefType() {
    synchronized (instance) {
      if (instance.localRefType == null) {
        ITypeBinding objectType = instance.ast.resolveWellKnownType("java.lang.Object");
        GeneratedTypeBinding refType =
            GeneratedTypeBinding.newTypeBinding("com.google.j2objc.util.ScopedLocalRef",
            objectType, false);
        GeneratedVariableBinding varBinding = new GeneratedVariableBinding("var", Modifier.PUBLIC,
            objectType, true, false, refType, null);
        refType.addField(varBinding);
        GeneratedMethodBinding constructor =
            GeneratedMethodBinding.newConstructor(refType, Modifier.PUBLIC);
        constructor.addParameter(objectType);
        refType.addMethod(constructor);
        instance.localRefType = refType;
      }
      return instance.localRefType;
    }
  }
}
