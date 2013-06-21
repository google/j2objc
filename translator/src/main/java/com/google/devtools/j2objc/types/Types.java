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
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.Weak;
import com.google.j2objc.annotations.WeakOuter;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
// TODO(user): convert to injectable implementation, to allow translator
// core to be reused for other languages.
public class Types {
  private final AST ast;
  private final Map<Object, IBinding> bindingMap;
  private final Map<ITypeBinding, ITypeBinding> typeMap = Maps.newHashMap();
  private final Map<ITypeBinding, ITypeBinding> renamedTypeMap = Maps.newHashMap();
  private final Map<IVariableBinding, ITypeBinding> variablesNeedingCasts = Maps.newHashMap();
  private final List<IMethodBinding> functions = Lists.newArrayList();
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

  private static Types instance;

  // Non-standard naming pattern is used, since in this case it's more readable.
  private final IOSTypeBinding NSCopying;
  private final IOSTypeBinding NSObject;
  private final IOSTypeBinding NSNumber;
  private final IOSTypeBinding NSString;
  private final IOSTypeBinding NSZone;
  private final IOSTypeBinding NS_ANY;
  private final IOSTypeBinding IOSClass;

  public IOSArrayTypeBinding IOSBooleanArray;
  public IOSArrayTypeBinding IOSByteArray;
  public IOSArrayTypeBinding IOSCharArray;
  public IOSArrayTypeBinding IOSDoubleArray;
  public IOSArrayTypeBinding IOSFloatArray;
  public IOSArrayTypeBinding IOSIntArray;
  public IOSArrayTypeBinding IOSLongArray;
  public IOSArrayTypeBinding IOSObjectArray;
  public IOSArrayTypeBinding IOSShortArray;

  private final Map<String, ITypeBinding> javaBindingMap = Maps.newHashMap();
  private final Map<String, ITypeBinding> iosBindingMap = Maps.newHashMap();

  // Map a primitive type to its emulation array type.
  private final Map<ITypeBinding, IOSArrayTypeBinding> arrayBindingMap = Maps.newHashMap();
  private final Map<IOSArrayTypeBinding, ITypeBinding> componentTypeMap = Maps.newHashMap();

  private final Set<Block> autoreleasePoolBlocks = Sets.newHashSet();

  // The first argument of a iOS method isn't named, but Java requires some sort of valid parameter
  // name.  The method mapper therefore uses this string, which the generators ignore.
  public static final String EMPTY_PARAMETER_NAME = "__empty_parameter__";
  public static final String NS_ANY_TYPE = "NS_ANY_TYPE";  // type of "id"

  private static final int STATIC_FINAL_MODIFIERS = Modifier.STATIC | Modifier.FINAL;

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
    NSCopying = IOSTypeBinding.newInterface("NSCopying", javaCloneableType);
    NSObject = IOSTypeBinding.newClass("NSObject", javaObjectType);
    NSNumber = IOSTypeBinding.newClass("NSNumber", javaNumberType, NSObject);
    NSString = IOSTypeBinding.newClass("NSString", javaStringType, NSObject);
    NS_ANY = IOSTypeBinding.newUnmappedClass("id");
    IOSClass = IOSTypeBinding.newUnmappedClass("IOSClass");
    NSZone = IOSTypeBinding.newUnmappedClass("NSZone");

    initializeBaseClasses();
    initializeArrayTypes();
    initializeTypeMap();
    initializeCommonJavaTypes();
    populateArrayTypeMaps();
    populatePrimitiveAndWrapperTypeMaps();
    bindingMap = BindingMapBuilder.buildBindingMap(unit);
    setGlobalRenamings();
  }

  private void initializeBaseClasses() {
    iosBindingMap.put("NSObject", NSObject);
    iosBindingMap.put("IOSClass", IOSClass);
    iosBindingMap.put("NSString", NSString);
    iosBindingMap.put("NSNumber", NSNumber);
    iosBindingMap.put("NSCopying", NSCopying);
    iosBindingMap.put("NSZone", NSZone);
    iosBindingMap.put("id", NS_ANY);
  }

  private void initializeArrayTypes() {
    IOSBooleanArray = new IOSArrayTypeBinding(
        "IOSBooleanArray", "booleanAtIndex", "getBooleans",
        ast.resolveWellKnownType("java.lang.Boolean"), ast.resolveWellKnownType("boolean"));
    IOSByteArray = new IOSArrayTypeBinding(
        "IOSByteArray", "byteAtIndex", "getBytes", ast.resolveWellKnownType("java.lang.Byte"),
        ast.resolveWellKnownType("byte"));
    IOSCharArray = new IOSArrayTypeBinding(
        "IOSCharArray", "charAtIndex", "getChars", ast.resolveWellKnownType("java.lang.Character"),
        ast.resolveWellKnownType("char"));
    IOSDoubleArray = new IOSArrayTypeBinding(
        "IOSDoubleArray", "doubleAtIndex", "getDoubles",
        ast.resolveWellKnownType("java.lang.Double"), ast.resolveWellKnownType("double"));
    IOSFloatArray = new IOSArrayTypeBinding(
        "IOSFloatArray", "floatAtIndex", "getFloats", ast.resolveWellKnownType("java.lang.Float"),
        ast.resolveWellKnownType("float"));
    IOSIntArray = new IOSArrayTypeBinding(
        "IOSIntArray", "intAtIndex", "getInts", ast.resolveWellKnownType("java.lang.Integer"),
        ast.resolveWellKnownType("int"));
    IOSLongArray = new IOSArrayTypeBinding(
        "IOSLongArray", "longAtIndex", "getLongs", ast.resolveWellKnownType("java.lang.Long"),
        ast.resolveWellKnownType("long"));
    IOSObjectArray = new IOSArrayTypeBinding(
        "IOSObjectArray", "objectAtIndex", "getObjects",
        ast.resolveWellKnownType("java.lang.Object"), null);
    IOSShortArray = new IOSArrayTypeBinding(
        "IOSShortArray", "shortAtIndex", "getShorts", ast.resolveWellKnownType("java.lang.Short"),
        ast.resolveWellKnownType("short"));

    iosBindingMap.put("IOSBooleanArray", IOSBooleanArray);
    iosBindingMap.put("IOSByteArray", IOSByteArray);
    iosBindingMap.put("IOSCharArray", IOSCharArray);
    iosBindingMap.put("IOSDoubleArray", IOSDoubleArray);
    iosBindingMap.put("IOSFloatArray", IOSFloatArray);
    iosBindingMap.put("IOSIntArray", IOSIntArray);
    iosBindingMap.put("IOSLongArray", IOSLongArray);
    iosBindingMap.put("IOSObjectArray", IOSObjectArray);
    iosBindingMap.put("IOSShortArray", IOSShortArray);
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
  }

  private void populateArrayTypeMaps() {
    addPrimitiveMappings("boolean", IOSBooleanArray);
    addPrimitiveMappings("byte", IOSByteArray);
    addPrimitiveMappings("char", IOSCharArray);
    addPrimitiveMappings("double", IOSDoubleArray);
    addPrimitiveMappings("float", IOSFloatArray);
    addPrimitiveMappings("int", IOSIntArray);
    addPrimitiveMappings("long", IOSLongArray);
    addPrimitiveMappings("short", IOSShortArray);
  }

  private void addPrimitiveMappings(String typeName, IOSArrayTypeBinding arrayType) {
    ITypeBinding primitiveType = ast.resolveWellKnownType(typeName);
    arrayBindingMap.put(primitiveType, arrayType);
    componentTypeMap.put(arrayType, primitiveType);
  }

  private void populatePrimitiveAndWrapperTypeMaps() {
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("boolean"), ast.resolveWellKnownType("java.lang.Boolean"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("byte"), ast.resolveWellKnownType("java.lang.Byte"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("char"), ast.resolveWellKnownType("java.lang.Character"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("short"), ast.resolveWellKnownType("java.lang.Short"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("int"), ast.resolveWellKnownType("java.lang.Integer"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("long"), ast.resolveWellKnownType("java.lang.Long"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("float"), ast.resolveWellKnownType("java.lang.Float"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("double"), ast.resolveWellKnownType("java.lang.Double"));
    loadPrimitiveAndWrapperTypes(
        ast.resolveWellKnownType("void"), ast.resolveWellKnownType("java.lang.Void"));
  }

  private void loadPrimitiveAndWrapperTypes(ITypeBinding primitive, ITypeBinding wrapper) {
    primitiveToWrapperTypes.put(primitive, wrapper);
    wrapperToPrimitiveTypes.put(wrapper, primitive);
  }

  private void setGlobalRenamings() {
    // longValue => longLongValue, because of return value
    // difference with NSNumber.longValue.
    renameLongValue(ast.resolveWellKnownType("java.lang.Byte"));
    renameLongValue(ast.resolveWellKnownType("java.lang.Double"));
    renameLongValue(ast.resolveWellKnownType("java.lang.Float"));
    renameLongValue(ast.resolveWellKnownType("java.lang.Integer"));
    renameLongValue(ast.resolveWellKnownType("java.lang.Long"));
    renameLongValue(ast.resolveWellKnownType("java.lang.Short"));
  }

  void renameLongValue(ITypeBinding type) {
    for (IMethodBinding method : type.getDeclaredMethods()) {
      if (method.getName().equals("longValue")) {
        NameTable.rename(method, "longLongValue");
        break;
      }
    }
  }

  /**
   * Returns true if the specified binding is for a static final variable.
   */
  public static boolean isConstantVariable(IVariableBinding binding) {
    return (binding.getModifiers() & Types.STATIC_FINAL_MODIFIERS) == Types.STATIC_FINAL_MODIFIERS;
  }

  public static boolean isStaticVariable(IVariableBinding binding) {
    return (binding.getModifiers() & Modifier.STATIC) > 0;
  }

  public static boolean isPrimitiveConstant(IVariableBinding binding) {
    return binding != null && isConstantVariable(binding) && binding.getType().isPrimitive() &&
        binding.getConstantValue() != null;
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

  /**
   * Returns a Type AST node for a specific type binding.
   */
  public static Type makeType(ITypeBinding binding) {
    Type type;
    if (binding.isPrimitive()) {
      PrimitiveType.Code typeCode = PrimitiveType.toCode(binding.getName());
      type = instance.ast.newPrimitiveType(typeCode);
    } else if (binding.isArray() && !(binding instanceof IOSArrayTypeBinding)) {
      Type componentType = makeType(binding.getComponentType());
      type = instance.ast.newArrayType(componentType);
    } else {
      String typeName = binding.getErasure().getName();
      if (typeName == "") {
        // Debugging aid for anonymous (no-name) classes.
        typeName = "$Local$";
      }
      SimpleName name = instance.ast.newSimpleName(typeName);
      addBinding(name, binding);
      type = instance.ast.newSimpleType(name);
    }
    addBinding(type, binding);
    return type;
  }

  /**
   * Creates a replacement iOS type for a given JDT type.
   */
  public static Type makeIOSType(Type type) {
    ITypeBinding binding = Types.getTypeBinding(type);
    return makeIOSType(binding);
  }

  public static Type makeIOSType(ITypeBinding binding) {
    if (binding.isArray()) {
      ITypeBinding componentType = binding.getComponentType();
      return Types.makeType(Types.resolveArrayType(componentType));
    }
    ITypeBinding newBinding = Types.mapType(binding);
    return binding != newBinding ? Types.makeType(newBinding) : null;
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

  public static boolean isJavaNumberType(ITypeBinding type) {
    return type.isAssignmentCompatible(instance.javaNumberType);
  }

  public static boolean isFloatingPointType(ITypeBinding type) {
    return type.isEqualTo(instance.ast.resolveWellKnownType("double")) ||
        type.isEqualTo(instance.ast.resolveWellKnownType("float")) ||
        type == instance.ast.resolveWellKnownType("java.lang.Double") ||
        type == instance.ast.resolveWellKnownType("java.lang.Float");
  }

  public static boolean isBooleanType(ITypeBinding type) {
    return instance.booleanType.equals(type);
  }

  public static ITypeBinding resolveIOSType(Type type) {
    if (type instanceof SimpleType) {
      String name = ((SimpleType) type).getName().getFullyQualifiedName();
      return resolveIOSType(name);
    }
    return null;
  }

  public static IOSArrayTypeBinding resolveArrayType(ITypeBinding binding) {
    IOSArrayTypeBinding arrayBinding = instance.arrayBindingMap.get(binding);
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
    }
    return null;
  }

  public static IMethodBinding getMethodBinding(Object node) {
    IBinding binding = getBinding(node);
    return binding instanceof IMethodBinding ? ((IMethodBinding) binding) : null;
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

  static ITypeBinding getIOSArrayComponentType(IOSArrayTypeBinding arrayType) {
    ITypeBinding type = instance.componentTypeMap.get(arrayType);
    return type != null ? type : instance.NSObject;
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

  public static void addFunction(IMethodBinding binding) {
    instance.functions.add(binding);
  }

  public static boolean isFunction(IMethodBinding binding) {
    if (instance.functions.contains(binding)) {
      return true;
    }
    IMethodBinding decl = binding.getMethodDeclaration();
    return decl != null ? instance.functions.contains(decl) : false;
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

  /**
   * Returns the declaration for a specified binding from a list of
   * type declarations.
   */
  public static TypeDeclaration getTypeDeclaration(ITypeBinding binding, List<?> declarations) {
    binding = binding.getTypeDeclaration();
    for (Object decl : declarations) {
      ITypeBinding type = getTypeBinding(decl).getTypeDeclaration();
      if (binding.isEqualTo(type)) {
        return decl instanceof TypeDeclaration ? (TypeDeclaration) decl : null;
      }
    }
    return null;
  }

  /**
   * Adds a variable that needs to be cast when referenced.  This is necessary
   * for gcc to verify parameters of generic interface's methods
   */
  public static void addVariableCast(IVariableBinding var, ITypeBinding castType) {
    instance.variablesNeedingCasts.put(var.getVariableDeclaration(), castType);
  }

  public static boolean variableHasCast(IVariableBinding var) {
    return instance.variablesNeedingCasts.containsKey(var.getVariableDeclaration());
  }

  public static ITypeBinding getCastForVariable(IVariableBinding var) {
    return instance.variablesNeedingCasts.get(var.getVariableDeclaration());
  }

  public static void addReleaseableFields(Collection<IVariableBinding> fields) {
    for (IVariableBinding field : fields) {
      instance.releaseableFields.add(field.getVariableDeclaration());
    }
  }

  public static boolean isReleaseableField(IVariableBinding var) {
    return var != null ? instance.releaseableFields.contains(var.getVariableDeclaration()) : false;
  }

  public static NullLiteral newNullLiteral() {
    NullLiteral nullLiteral = instance.ast.newNullLiteral();
    addBinding(nullLiteral, NullType.SINGLETON);
    return nullLiteral;
  }

  public static BooleanLiteral newBooleanLiteral(boolean value) {
    BooleanLiteral boolLiteral = instance.ast.newBooleanLiteral(value);
    addBinding(boolLiteral, instance.booleanType);
    return boolLiteral;
  }

  public static ITypeBinding getWrapperType(ITypeBinding primitiveType) {
    return instance.primitiveToWrapperTypes.get(primitiveType);
  }

  public static ITypeBinding getPrimitiveType(ITypeBinding wrapperType) {
    return instance.wrapperToPrimitiveTypes.get(wrapperType);
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

  public static boolean isWeakReference(IVariableBinding var) {
    return hasWeakAnnotation(var) || isWeakOuterReference(var);
  }

  private static boolean isWeakOuterReference(IVariableBinding var) {
    return var.getName().startsWith("this$") && hasWeakAnnotation(var.getDeclaringClass());
  }

  public static boolean hasAnyAnnotation(IBinding binding, Class<?>[] annotations) {
    for (IAnnotationBinding annotation : binding.getAnnotations()) {
      String name = annotation.getAnnotationType().getQualifiedName();
      for (Class<?> annotationClass : annotations) {
        if (name.equals(annotationClass.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasAnnotation(IBinding binding, Class<?> annotation) {
    return hasAnyAnnotation(binding, new Class<?>[] { annotation });
  }

  public static boolean hasWeakAnnotation(IBinding binding) {
    return hasAnyAnnotation(binding, new Class<?>[] { Weak.class, WeakOuter.class });
  }

  public static boolean hasAutoreleasePoolAnnotation(IBinding binding) {
    boolean hasAnnotation = hasAnnotation(binding, AutoreleasePool.class);

    if (hasAnnotation && binding instanceof IMethodBinding) {
      if (!isVoidType(((IMethodBinding) binding).getReturnType())) {
        J2ObjC.warning(
            "Warning: Ignoring AutoreleasePool annotation on method with non-void return type");
        return false;
      }
    }

    return hasAnnotation;
  }

  public static void addAutoreleasePool(Block block) {
    if (Options.useGC()) {
      J2ObjC.warning(block, "@AutoreleasePool ignored in GC mode");
    }
    instance.autoreleasePoolBlocks.add(block);
  }

  public static boolean hasAutoreleasePool(Block block) {
    return instance.autoreleasePoolBlocks.contains(block);
  }

  // JDT doesn't have any way to dynamically create a null literal binding.
  private static class NullType implements ITypeBinding {
    static final NullType SINGLETON = new NullType();

    public IAnnotationBinding[] getAnnotations() {
      return new IAnnotationBinding[0];
    }

    public int getKind() {
      return IBinding.TYPE;
    }

    public boolean isDeprecated() {
      return false;
    }

    public boolean isRecovered() {
      return false;
    }

    public boolean isSynthetic() {
      return false;
    }

    public IJavaElement getJavaElement() {
      return null;
    }

    public String getKey() {
      return null;
    }

    public boolean isEqualTo(IBinding binding) {
      return binding.getName().equals("null");
    }

    public ITypeBinding createArrayType(int dimension) {
      return null;
    }

    public String getBinaryName() {
      return "N";
    }

    public ITypeBinding getBound() {
      return null;
    }

    public ITypeBinding getGenericTypeOfWildcardType() {
      return null;
    }

    public int getRank() {
      return -1;
    }

    public ITypeBinding getComponentType() {
      return null;
    }

    public IVariableBinding[] getDeclaredFields() {
      return new IVariableBinding[0];
    }

    public IMethodBinding[] getDeclaredMethods() {
      return new IMethodBinding[0];
    }

    public int getDeclaredModifiers() {
      return 0;
    }

    public ITypeBinding[] getDeclaredTypes() {
      return new ITypeBinding[0];
    }

    public ITypeBinding getDeclaringClass() {
      return null;
    }

    public IMethodBinding getDeclaringMethod() {
      return null;
    }

    public int getDimensions() {
      return 0;
    }

    public ITypeBinding getElementType() {
      return null;
    }

    public ITypeBinding getErasure() {
      return null;
    }

    public ITypeBinding[] getInterfaces() {
      return new ITypeBinding[0];
    }

    public int getModifiers() {
      return 0;
    }

    public String getName() {
      return "null";
    }

    public IPackageBinding getPackage() {
      return null;
    }

    public String getQualifiedName() {
      return "null";
    }

    public ITypeBinding getSuperclass() {
      return null;
    }

    public ITypeBinding[] getTypeArguments() {
      return new ITypeBinding[0];
    }

    public ITypeBinding[] getTypeBounds() {
      return new ITypeBinding[0];
    }

    public ITypeBinding getTypeDeclaration() {
      return SINGLETON;
    }

    public ITypeBinding[] getTypeParameters() {
      return new ITypeBinding[0];
    }

    public ITypeBinding getWildcard() {
      return null;
    }

    public boolean isAnnotation() {
      return false;
    }

    public boolean isAnonymous() {
      return false;
    }

    public boolean isArray() {
      return false;
    }

    public boolean isAssignmentCompatible(ITypeBinding variableType) {
      return true;
    }

    public boolean isCapture() {
      return false;
    }

    public boolean isCastCompatible(ITypeBinding type) {
      return false;
    }

    public boolean isClass() {
      return false;
    }

    public boolean isEnum() {
      return false;
    }

    public boolean isFromSource() {
      return false;
    }

    public boolean isGenericType() {
      return false;
    }

    public boolean isInterface() {
      return false;
    }

    public boolean isLocal() {
      return false;
    }

    public boolean isMember() {
      return false;
    }

    public boolean isNested() {
      return false;
    }

    public boolean isNullType() {
      return true;
    }

    public boolean isParameterizedType() {
      return false;
    }

    public boolean isPrimitive() {
      return false;
    }

    public boolean isRawType() {
      return false;
    }

    public boolean isSubTypeCompatible(ITypeBinding type) {
      return false;
    }

    public boolean isTopLevel() {
      return false;
    }

    public boolean isTypeVariable() {
      return false;
    }

    public boolean isUpperbound() {
      return false;
    }

    public boolean isWildcardType() {
      return false;
    }
  }

  /**
   * Returns the signature of an element, defined in the Java Language
   * Specification 3rd edition, section 13.1.
   */
  public static String getSignature(IBinding binding) {
    if (binding instanceof ITypeBinding) {
      return ((ITypeBinding) binding).getBinaryName();
    }
    if (binding instanceof IMethodBinding) {
      return getSignature((IMethodBinding) binding);
    }
    return binding.getName();
  }

  private static String getSignature(IMethodBinding binding) {
    StringBuilder sb = new StringBuilder("(");
    for (ITypeBinding parameter : binding.getParameterTypes()) {
      appendParameterSignature(parameter.getErasure(), sb);
    }
    sb.append(')');
    if (binding.getReturnType() != null) {
      appendParameterSignature(binding.getReturnType().getErasure(), sb);
    }
    return sb.toString();
  }

  private static void appendParameterSignature(ITypeBinding parameter, StringBuilder sb) {
    if (!parameter.isPrimitive() && !parameter.isArray()) {
      sb.append('L');
    }
    sb.append(parameter.getBinaryName().replace('.', '/'));
    if (!parameter.isPrimitive() && !parameter.isArray()) {
      sb.append(';');
    }
  }
}
