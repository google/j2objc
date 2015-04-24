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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;

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

  public static boolean isStatic(IBinding binding) {
    return Modifier.isStatic(binding.getModifiers());
  }

  public static boolean isFinal(IBinding binding) {
    return Modifier.isFinal(binding.getModifiers());
  }

  public static boolean isPrivate(IBinding binding) {
    return Modifier.isPrivate(binding.getModifiers());
  }

  public static boolean isPrimitiveConstant(IVariableBinding binding) {
    return isConstant(binding) && binding.getType().isPrimitive();
  }

  public static boolean isConstant(IVariableBinding binding) {
    return binding != null && isStatic(binding) && isFinal(binding)
        && binding.getConstantValue() != null;
  }

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

  public static boolean isSynthetic(IMethodBinding m) {
    return isSynthetic(m.getModifiers());
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
   * Returns a set containing the bindings of all classes and interfaces that
   * are inherited by the given type.
   */
  public static Set<ITypeBinding> getAllInheritedTypes(ITypeBinding type) {
    Set<ITypeBinding> inheritedTypes = Sets.newHashSet();
    collectAllInheritedTypes(type, inheritedTypes);
    return inheritedTypes;
  }

  public static void collectAllInheritedTypes(ITypeBinding type, Set<ITypeBinding> inheritedTypes) {
    collectAllInterfaces(type, inheritedTypes);
    while (true) {
      type = type.getSuperclass();
      if (type == null) {
        break;
      }
      inheritedTypes.add(type);
    }
  }

  /**
   * Returns a set containing bindings of all interfaces implemented by the
   * given class, and all super-interfaces of those.
   */
  public static Set<ITypeBinding> getAllInterfaces(ITypeBinding type) {
    Set<ITypeBinding> interfaces = Sets.newHashSet();
    collectAllInterfaces(type, interfaces);
    return interfaces;
  }

  public static void collectAllInterfaces(ITypeBinding type, Set<ITypeBinding> interfaces) {
    Deque<ITypeBinding> typeQueue = Lists.newLinkedList();

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

  public static String getMethodKey(IMethodBinding binding) {
    return binding.getDeclaringClass().getBinaryName() + '.' + binding.getName()
        + getSignature(binding);
  }

  public static String getSignature(IMethodBinding binding) {
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
        || var.getName().startsWith("this$")
        && hasNamedAnnotation(var.getDeclaringClass(), "WeakOuter");
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
   * Returns true if method is an Objective-C dealloc method.
   */
  public static boolean isDestructor(IMethodBinding m) {
    String methodName = m.getName();
    return !m.isConstructor() && !isStatic(m) && m.getParameterTypes().length == 0
        && (methodName.equals(NameTable.FINALIZE_METHOD)
            || methodName.equals(NameTable.DEALLOC_METHOD));
  }
}
