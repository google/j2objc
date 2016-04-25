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

import com.google.devtools.j2objc.types.LambdaTypeBinding;
import com.google.j2objc.annotations.Property;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility methods for working with binding types.
 *
 * @author Keith Stanger
 */
public final class BindingUtil {

  // Flags defined in JVM spec, table 4.1. These constants are also defined in
  // java.lang.reflect.Modifier, but aren't public.
  public static final int ACC_BRIDGE = 0x40;
  public static final int ACC_VARARGS = 0x80;
  public static final int ACC_SYNTHETIC = 0x1000;
  public static final int ACC_ANNOTATION = 0x2000;
  public static final int ACC_ENUM = 0x4000;

  // Not defined in JVM spec, but used by reflection support.
  public static final int ACC_ANONYMOUS = 0x8000;

  // Class files can only use the lower 16 bits.
  public static final int ACC_FLAG_MASK = 0xFFFF;

  public static boolean isStatic(IBinding binding) {
    return Modifier.isStatic(binding.getModifiers());
  }

  public static boolean isDefault(IBinding binding) {
    return Modifier.isDefault(binding.getModifiers());
  }

  public static boolean isFinal(IBinding binding) {
    return Modifier.isFinal(binding.getModifiers());
  }

  public static boolean isPrivate(IBinding binding) {
    return Modifier.isPrivate(binding.getModifiers());
  }

  public static boolean isVolatile(IVariableBinding binding) {
    return Modifier.isVolatile(binding.getModifiers());
  }

  public static boolean isPrimitiveConstant(IVariableBinding binding) {
    return isFinal(binding) && binding.getType().isPrimitive()
        && binding.getConstantValue() != null
        // Exclude local variables declared final.
        && binding.getDeclaringClass() != null;
  }

  public static boolean isStringConstant(IVariableBinding binding) {
    Object constantValue = binding.getConstantValue();
    return constantValue != null && constantValue instanceof String
        && UnicodeUtils.hasValidCppCharacters((String) constantValue);
  }

  /**
   * Returns whether this variable will be declared in global scope in ObjC.
   */
  public static boolean isGlobalVar(IVariableBinding binding) {
    return isStatic(binding) || isPrimitiveConstant(binding);
  }

  /**
   * Returns whether this variable will be an ObjC instance variable.
   */
  public static boolean isInstanceVar(IVariableBinding binding) {
    return binding.isField() && !isGlobalVar(binding);
  }

  /**
   * Return if a class or a method in a class or in an interface is abstract.
   *
   * The following cases are true:
   * - if binding is an abstract class;
   * - if binding is an abstract method in a class; or
   * - if binding is a non-default method in an interface.
   *
   * If your code is run before AbstractMethodRewriter, a MethodDeclaration's modifiers may not be
   * identical to its method binding's. This is because JDT only marks a MethodDeclaration node
   * abstract when the "abstract" modifier is present in the code (and therefore the method must be
   * from a class), whereas a binding's modifier reflects the method's abstract nature (and
   * therefore a non-default method from an interface is abstract).
   *
   * Code past AbstractMethodRewriter can safely assume Modifier.isAbstract(decl.getModifiers())
   * is the same as BindingUtil.isAbstract(decl.getMethodBinding()).
   */
  public static boolean isAbstract(IBinding binding) {
    return Modifier.isAbstract(binding.getModifiers());
  }

  public static boolean isNative(IBinding binding) {
    return Modifier.isNative(binding.getModifiers());
  }

  public static boolean isSynchronized(IBinding binding) {
    return Modifier.isSynchronized(binding.getModifiers());
  }

  public static boolean isSynthetic(int modifiers) {
    return (modifiers & ACC_SYNTHETIC) > 0;
  }

  public static boolean isSynthetic(IBinding binding) {
    return isSynthetic(binding.getModifiers());
  }

  public static boolean isVoid(ITypeBinding type) {
    return type.isPrimitive() && type.getBinaryName().charAt(0) == 'V';
  }

  public static boolean isBoolean(ITypeBinding type) {
    return type.isPrimitive() && type.getBinaryName().charAt(0) == 'Z';
  }

  public static boolean isFloatingPoint(ITypeBinding type) {
    if (!type.isPrimitive()) {
      return false;
    }
    char binaryName = type.getBinaryName().charAt(0);
    return binaryName == 'F' || binaryName == 'D';
  }

  public static boolean isLambda(ITypeBinding type) {
    return type instanceof LambdaTypeBinding;
  }

  /**
   * Tests if this type is private to it's source file. A public type declared
   * within a private type is considered private.
   */
  public static boolean isPrivateInnerType(ITypeBinding type) {
    if (isPrivate(type) || type.isLocal() || type.isAnonymous()) {
      return true;
    }
    ITypeBinding declaringClass = type.getDeclaringClass();
    if (declaringClass != null) {
      return isPrivateInnerType(declaringClass);
    }
    return false;
  }

  /**
   * Determines if a type can access fields and methods from an outer class.
   */
  public static boolean hasOuterContext(ITypeBinding type) {
    if (type.getDeclaringClass() == null) {
      return false;
    }
    // Local types can't be declared static, but if the declaring method is
    // static then the local type is effectively static.
    IMethodBinding declaringMethod = type.getTypeDeclaration().getDeclaringMethod();
    if (declaringMethod != null) {
      return !BindingUtil.isStatic(declaringMethod);
    }
    return !BindingUtil.isStatic(type);
  }

  /**
   * Convert an IBinding to a ITypeBinding. Returns null if the binding cannot
   * be converted to a type binding.
   */
  public static ITypeBinding toTypeBinding(IBinding binding) {
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
   * Gets the list of erased types that would be included in a ObjC declaration
   * for this type. For example, if the type would be declared as
   * "Foo<Bar, Baz> *" then the returned bounds are the bindings for the class
   * Foo, and interfaces Bar and Baz. If one of the bounds is a class type, then
   * that type will be the first element in the list.
   */
  public static List<ITypeBinding> getTypeBounds(ITypeBinding type) {
    if (isIntersectionType(type)) {
      return getIntersectionTypeBounds(type);
    } else if (!(type.isTypeVariable() || type.isCapture() || type.isWildcardType())) {
      return Collections.singletonList(type.getErasure());
    }
    List<ITypeBinding> bounds = new ArrayList<>();
    // The first element of the list will be the class type. If it is still null
    // after collecting the bounds then remove it.
    bounds.add(null);
    collectBounds(type, bounds);
    if (bounds.get(0) == null) {
      bounds.remove(0);
    }
    return bounds;
  }

  private static List<ITypeBinding> getIntersectionTypeBounds(ITypeBinding type) {
    ITypeBinding[] interfaces = type.getInterfaces();
    List<ITypeBinding> bounds = new ArrayList<>(interfaces.length);
    for (ITypeBinding bound : interfaces) {
      bounds.add(bound.getErasure());
    }
    return bounds;
  }

  private static void collectBounds(ITypeBinding type, List<ITypeBinding> bounds) {
    ITypeBinding[] boundsArr = type.getTypeBounds();
    if (boundsArr.length == 0) {
      if (type.isWildcardType()) {
        for (ITypeBinding intrface : type.getInterfaces()) {
          addBound(intrface, bounds);
        }
      }
      addBound(type, bounds);
    } else {
      for (ITypeBinding bound : boundsArr) {
        collectBounds(bound, bounds);
      }
    }
  }

  private static void addBound(ITypeBinding type, List<ITypeBinding> bounds) {
    type = type.getErasure();
    if (type.isInterface()) {
      bounds.add(type);
    } else if (bounds.get(0) != null) {
      throw new AssertionError("Type has multiple class type bounds");
    } else {
      bounds.set(0, type);
    }
  }

  /**
   * Returns a set containing the bindings of all classes and interfaces that
   * are inherited by the given type.
   */
  public static Set<ITypeBinding> getInheritedTypes(ITypeBinding type) {
    Set<ITypeBinding> inheritedTypes = getInheritedTypesInclusive(type);
    inheritedTypes.remove(type);
    return inheritedTypes;
  }

  public static Set<ITypeBinding> getInheritedTypesInclusive(ITypeBinding type) {
    Set<ITypeBinding> inheritedTypes = new HashSet<>();
    collectInheritedTypesInclusive(type, inheritedTypes);
    return inheritedTypes;
  }

  /**
   * Returns a set containing the bindings of all classes and interfaces that
   * are inherited by the given type in the same order that they would be
   * searched by the ObjC compiler.
   */
  public static LinkedHashSet<ITypeBinding> getOrderedInheritedTypes(ITypeBinding type) {
    LinkedHashSet<ITypeBinding> inheritedTypes = getOrderedInheritedTypesInclusive(type);
    inheritedTypes.remove(type);
    return inheritedTypes;
  }

  public static LinkedHashSet<ITypeBinding> getOrderedInheritedTypesInclusive(ITypeBinding type) {
    LinkedHashSet<ITypeBinding> inheritedTypes = new LinkedHashSet<>();
    collectInheritedTypesInclusive(type, inheritedTypes);
    return inheritedTypes;
  }

  private static void collectInheritedTypesInclusive(
      ITypeBinding type, Set<ITypeBinding> inheritedTypes) {
    if (type == null) {
      return;
    }
    inheritedTypes.add(type);
    for (ITypeBinding interfaze : type.getInterfaces()) {
      collectInheritedTypesInclusive(interfaze, inheritedTypes);
    }
    collectInheritedTypesInclusive(type.getSuperclass(), inheritedTypes);
  }

  /**
   * Returns a set containing bindings of all interfaces implemented by the
   * given class, and all super-interfaces of those.
   */
  public static Set<ITypeBinding> getAllInterfaces(ITypeBinding type) {
    Set<ITypeBinding> interfaces = new HashSet<>();
    collectAllInterfaces(type, interfaces);
    return interfaces;
  }

  public static void collectAllInterfaces(ITypeBinding type, Set<ITypeBinding> interfaces) {
    Deque<ITypeBinding> typeQueue = new LinkedList<>();

    while (type != null) {
      typeQueue.add(type);
      type = type.getSuperclass();
    }

    while (!typeQueue.isEmpty()) {
      ITypeBinding nextType = typeQueue.poll();
      List<ITypeBinding> newInterfaces = Arrays.asList(nextType.getInterfaces());
      interfaces.addAll(newInterfaces);
      typeQueue.addAll(newInterfaces);
    }
  }

  /**
   * Returns the type binding for a specific interface of a specific type.
   */
  public static ITypeBinding findInterface(ITypeBinding implementingType, String qualifiedName) {
    if (implementingType.isInterface()
        && implementingType.getErasure().getQualifiedName().equals(qualifiedName)) {
      return implementingType;
    }
    for (ITypeBinding interfaze : getAllInterfaces(implementingType)) {
      if (interfaze.getErasure().getQualifiedName().equals(qualifiedName)) {
        return interfaze;
      }
    }
    return null;
  }

  /**
   * Returns the inner type with the specified name.
   */
  public static ITypeBinding findDeclaredType(ITypeBinding type, String name) {
    for (ITypeBinding innerType : type.getDeclaredTypes()) {
      if (innerType.getName().equals(name)) {
        return innerType;
      }
    }
    return null;
  }

  /**
   * Returns the method binding for a specific method of a specific type.
   */
  public static IMethodBinding findDeclaredMethod(
      ITypeBinding type, String methodName, String... paramTypes) {
    outer: for (IMethodBinding method : type.getDeclaredMethods()) {
      if (method.getName().equals(methodName)) {
        ITypeBinding[] foundParamTypes = method.getParameterTypes();
        if (paramTypes.length == foundParamTypes.length) {
          for (int i = 0; i < paramTypes.length; i++) {
            if (!paramTypes[i].equals(foundParamTypes[i].getQualifiedName())) {
              continue outer;
            }
          }
          return method;
        }
      }
    }
    return null;
  }

  /**
   * Locate method which matches either Java or Objective C getter name patterns.
   */
  public static IMethodBinding findGetterMethod(String propertyName, ITypeBinding propertyType,
      ITypeBinding declaringClass) {
    // Try Objective-C getter naming convention.
    IMethodBinding getter = BindingUtil.findDeclaredMethod(declaringClass, propertyName);
    if (getter == null) {
      // Try Java getter naming conventions.
      String prefix = BindingUtil.isBoolean(propertyType) ? "is" : "get";
      getter = BindingUtil.findDeclaredMethod(declaringClass,
          prefix + NameTable.capitalize(propertyName));
    }
    return getter;
  }

  /**
   * Locate method which matches the Java/Objective C setter name pattern.
   */
  public static IMethodBinding findSetterMethod(String propertyName, ITypeBinding declaringClass) {
    return BindingUtil.findDeclaredMethod(declaringClass,
        "set" + NameTable.capitalize(propertyName));
  }

  public static String getMethodKey(IMethodBinding binding) {
    return binding.getDeclaringClass().getBinaryName() + '.' + binding.getName()
        + getSignature(binding);
  }

  public static String getSignature(IMethodBinding binding) {
    StringBuilder sb = new StringBuilder("(");
    appendParametersSignature(binding, sb);
    sb.append(')');
    appendReturnTypeSignature(binding, sb);
    return sb.toString();
  }

  /**
   * Return a signature string for the purpose of discovering default methods.
   *
   * To test if a class overrides or redeclares a default method, we just need the method name and
   * its parameters signature. We don't need to test the return type -- the compiler frontend does
   * not allow incompatible return types, and so we don't have to deal with malformed code.
   */
  public static String getDefaultMethodSignature(IMethodBinding method) {
    StringBuilder sb = new StringBuilder(method.getName());
    sb.append('(');
    appendParametersSignature(method, sb);
    sb.append(')');
    return sb.toString();
  }

  /**
   * Get a method's signature for dead code elimination purposes.
   *
   * Since DeadCodeEliminator runs before InnerClassExtractor, inner class constructors do not yet
   * have the parameter for capturing outer class, and therefore we need this special case.
   */
  public static String getProGuardSignature(IMethodBinding binding) {
    StringBuilder sb = new StringBuilder("(");

    // If the method is an inner class constructor, prepend the outer class type.
    if (binding.isConstructor()) {
      ITypeBinding declClass = binding.getDeclaringClass();
      ITypeBinding outerClass = declClass.getDeclaringClass();
      if (outerClass != null && !declClass.isInterface() && !declClass.isAnnotation()
          && !Modifier.isStatic(declClass.getModifiers())) {
        appendParameterSignature(outerClass.getErasure(), sb);
      }
    }

    appendParametersSignature(binding, sb);
    sb.append(')');
    appendReturnTypeSignature(binding, sb);
    return sb.toString();
  }

  private static void appendReturnTypeSignature(IMethodBinding binding, StringBuilder sb) {
    if (binding.getReturnType() != null) {
      appendParameterSignature(binding.getReturnType().getErasure(), sb);
    }
  }

  private static void appendParametersSignature(IMethodBinding binding, StringBuilder sb) {
    for (ITypeBinding parameter : binding.getParameterTypes()) {
      appendParameterSignature(parameter.getErasure(), sb);
    }
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

  public static boolean hasAnnotation(IBinding binding, Class<?> annotationClass) {
    return getAnnotation(binding, annotationClass) != null;
  }

  /**
   * Less strict version of the above where we don't care about the annotation's package.
   */
  public static boolean hasNamedAnnotation(IBinding binding, String annotationName) {
    for (IAnnotationBinding annotation : binding.getAnnotations()) {
      if (annotation.getName().equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if a binding has a named "Nullable" annotation. Package names aren't
   * checked because different nullable annotations are defined by several different
   * Java frameworks.
   */
  public static boolean hasNullableAnnotation(IBinding binding) {
    return hasNamedAnnotation(binding, "Nullable");
  }

  /**
   * Return true if a binding has a named "Nonnull" annotation. Package names aren't
   * checked because different nonnull annotations are defined in several Java
   * frameworks, with varying but similar names.
   */
  public static boolean hasNonnullAnnotation(IBinding binding) {
    Pattern p = Pattern.compile("No[nt][Nn]ull");
    for (IAnnotationBinding annotation : binding.getAnnotations()) {
      if (p.matcher(annotation.getName()).matches()) {
        return true;
      }
    }
    return false;
  }

  public static IAnnotationBinding getAnnotation(IBinding binding, Class<?> annotationClass) {
    for (IAnnotationBinding annotation : binding.getAnnotations()) {
      if (typeEqualsClass(annotation.getAnnotationType(), annotationClass)) {
        return annotation;
      }
    }
    return null;
  }

  public static boolean typeEqualsClass(ITypeBinding type, Class<?> cls) {
    return type.getQualifiedName().equals(cls.getName());
  }

  public static Object getAnnotationValue(IAnnotationBinding annotation, String name) {
    for (IMemberValuePairBinding pair : annotation.getAllMemberValuePairs()) {
      if (name.equals(pair.getName())) {
        return pair.getValue();
      }
    }
    return null;
  }

  public static boolean isWeakReference(IVariableBinding var) {
    return hasNamedAnnotation(var, "Weak")
        || hasWeakPropertyAttribute(var)
        || var.getName().startsWith("this$")
        && hasNamedAnnotation(var.getDeclaringClass(), "WeakOuter");
  }

  private static boolean hasWeakPropertyAttribute(IVariableBinding var) {
    IAnnotationBinding propertyAnnotation = getAnnotation(var, Property.class);
    if (propertyAnnotation == null) {
      return false;
    }
    return parseAttributeString(propertyAnnotation).contains("weak");
  }

  /**
   * Returns true if the specified binding is of an annotation that has
   * a runtime retention policy.
   */
  public static boolean isRuntimeAnnotation(IAnnotationBinding binding) {
    return isRuntimeAnnotation(binding.getAnnotationType());
  }

  /**
   * Returns true if the specified binding is of an annotation that has
   * a runtime retention policy.
   */
  public static boolean isRuntimeAnnotation(ITypeBinding binding) {
    if (binding != null && binding.isAnnotation()) {
      for (IAnnotationBinding ann : binding.getAnnotations()) {
        if (ann.getName().equals("Retention")) {
          IVariableBinding retentionBinding =
              (IVariableBinding) ann.getDeclaredMemberValuePairs()[0].getValue();
          return retentionBinding.getName().equals(RetentionPolicy.RUNTIME.name());
        }
      }
      if (binding.isNested()) {
        return BindingUtil.isRuntimeAnnotation(binding.getDeclaringClass());
      }
    }
    return false;
  }

  /**
   * Returns an alphabetically sorted list of an annotation type's members.
   * This is necessary since an annotation's values can be specified in any
   * order, but the annotation's constructor needs to be invoked using its
   * declaration order.
   */
  public static IMethodBinding[] getSortedAnnotationMembers(ITypeBinding annotation) {
    IMethodBinding[] members = annotation.getDeclaredMethods();
    Arrays.sort(members, new Comparator<IMethodBinding>() {
      @Override
      public int compare(IMethodBinding o1, IMethodBinding o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return members;
  }

  /**
   * Returns an alphabetically sorted list of an annotation's member values.
   * This is necessary since an annotation's values can be specified in any
   * order, but the annotation's constructor needs to be invoked using its
   * declaration order.
   */
  public static IMemberValuePairBinding[] getSortedMemberValuePairs(
      IAnnotationBinding annotation) {
    IMemberValuePairBinding[] valuePairs = annotation.getAllMemberValuePairs();
    Arrays.sort(valuePairs, new Comparator<IMemberValuePairBinding>() {
      @Override
      public int compare(IMemberValuePairBinding o1, IMemberValuePairBinding o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return valuePairs;
  }

  /**
   * Returns the attributes of a Property annotation.
   */
  public static Set<String> parseAttributeString(IAnnotationBinding propertyAnnotation) {
    assert propertyAnnotation.getName().equals("Property");
    String attributesStr = (String) getAnnotationValue(propertyAnnotation, "value");
    Set<String> attributes = new HashSet<>();
    attributes.addAll(Arrays.asList(attributesStr.split(",\\s*")));
    attributes.remove(""); // Clear any empty strings.
    return attributes;
  }

  /**
   * Returns all declared constructors for a specified type.
   */
  public static Set<IMethodBinding> getDeclaredConstructors(ITypeBinding type) {
    Set<IMethodBinding> constructors = new HashSet<>();
    for (IMethodBinding m : type.getDeclaredMethods()) {
      if (m.isConstructor()) {
        constructors.add(m);
      }
    }
    return constructors;
  }

  /**
   * Returns true if there's a SuppressedWarning annotation with the specified warning.
   * The SuppressWarnings annotation can be inherited from the owning method or class,
   * but does not have package scope.
   */
  public static boolean suppressesWarning(String warning, IBinding binding) {
    if (binding == null) {
      return false;
    }
    IAnnotationBinding annotation = getAnnotation(binding, SuppressWarnings.class);
    if (annotation != null) {
      for (IMemberValuePairBinding valuePair : annotation.getAllMemberValuePairs()) {
        for (Object suppressedWarning : (Object[]) valuePair.getValue()) {
          if (suppressedWarning.equals(warning)) {
            return true;
          }
        }
      }
    }
    if (binding instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) binding;
      IMethodBinding owningMethod = var.getDeclaringMethod();
      if (owningMethod != null) {
        return suppressesWarning(warning, owningMethod);
      } else {
        return suppressesWarning(warning, var.getDeclaringClass());
      }
    }
    if (binding instanceof IMethodBinding) {
      return suppressesWarning(warning, ((IMethodBinding) binding).getDeclaringClass());
    }
    return false;
  }


  /**
   * Returns true if any of the declared methods in the interface or its supers is default.
   */
  public static boolean hasDefaultMethodsInFamily(ITypeBinding type) {
    assert type.isInterface();

    for (IMethodBinding method : type.getDeclaredMethods()) {
      if (isDefault(method)) {
        return true;
      }
    }

    for (ITypeBinding parent : type.getInterfaces()) {
      if (hasDefaultMethodsInFamily(parent)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true if any of the declared methods in the interface is static.
   */
  public static boolean hasStaticInterfaceMethods(ITypeBinding type) {
    assert type.isInterface();

    for (IMethodBinding method : type.getDeclaredMethods()) {
      if (isStatic(method)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true if the binding is a Java 8 intersection type. For example,
   * Comparator.thenComparing() returns a lambda with a return type of
   * "Comparator<T> & Serializable". Since there's no ITypeBinding
   * isIntersectionType(), we rely on the JDT returning an empty string for the
   * type's qualified name.
   */
  public static boolean isIntersectionType(ITypeBinding binding) {
    return binding.isInterface() && binding.getQualifiedName().isEmpty();
  }

  /**
   * Dumps the results from all methods in ITypeBinding to System.out.
   * Used only when debugging.
   */
  public static void dumpTypeBinding(ITypeBinding binding) {
    System.out.println("dump type: " + binding.getName());
    System.out.println("  getBinaryName: " + binding.getBinaryName());
    System.out.println("  getClass: " + binding.getClass());
    System.out.println("  getComponentType: " + binding.getComponentType());
    dumpArray("  getDeclaredTypes:", binding.getDeclaredTypes());
    System.out.println("  getDeclaringClass: " + binding.getDeclaringClass());
    System.out.println("  getElementType: " + binding.getElementType());
    System.out.println("  getErasure: " + binding.getErasure());
    System.out.println("  getGenericTypeOfWildcardType: " + binding.getGenericTypeOfWildcardType());
    dumpArray("  getInterfaces:", binding.getInterfaces());
    System.out.println("  getKey: " + binding.getKey());
    System.out.println("  getModifiers:0x" + Integer.toHexString(binding.getModifiers()));
    System.out.println("  getName: " + binding.getName());
    System.out.println("  getPackage: " + binding.getPackage());
    System.out.println("  getQualifiedName: " + binding.getQualifiedName());
    System.out.println("  getSuperclass: " + binding.getSuperclass());
    dumpArray("  getTypeArguments:", binding.getTypeArguments());
    dumpArray("  getTypeBounds:", binding.getTypeBounds());
    System.out.println("  getTypeDeclaration: " + binding.getTypeDeclaration());
    dumpArray("  getTypeParameters:", binding.getTypeParameters());
    System.out.println("  getWildcard: " + binding.getWildcard());
    System.out.println("  isAnnotation: " + binding.isAnnotation());
    System.out.println("  isAnonymous: " + binding.isAnonymous());
    System.out.println("  isArray: " + binding.isArray());
    System.out.println("  isCapture: " + binding.isCapture());
    System.out.println("  isClass: " + binding.isClass());
    System.out.println("  isEnum: " + binding.isEnum());
    System.out.println("  isFromSource: " + binding.isFromSource());
    System.out.println("  isGenericType: " + binding.isGenericType());
    System.out.println("  isInterface: " + binding.isInterface());
    System.out.println("  isLocal: " + binding.isLocal());
    System.out.println("  isMember: " + binding.isMember());
    System.out.println("  isNested: " + binding.isNested());
    System.out.println("  isNullType: " + binding.isNullType());
    System.out.println("  isParameterizedType: " + binding.isParameterizedType());
    System.out.println("  isPrimitive: " + binding.isPrimitive());
    System.out.println("  isRawType: " + binding.isRawType());
    System.out.println("  isSynthetic: " + binding.isSynthetic());
    System.out.println("  isTopLevel: " + binding.isTopLevel());
    System.out.println("  isTypeVariable: " + binding.isTypeVariable());
    System.out.println("  isUpperbound: " + binding.isUpperbound());
    System.out.println("  isWildcardType: " + binding.isWildcardType());
  }

  private static void dumpArray(String description, ITypeBinding[] array) {
    System.out.print(description + " [");
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        System.out.print(", ");
      }
      System.out.print(array[i].getName());
    }
    System.out.println("]");
  }
}
