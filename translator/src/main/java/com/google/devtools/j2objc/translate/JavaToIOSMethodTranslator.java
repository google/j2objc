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

package com.google.devtools.j2objc.translate;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSParameter;
import com.google.devtools.j2objc.types.JavaMethod;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Map;

/**
 * Translates method invocations and overridden methods from Java core types to
 * iOS equivalents. For example, <code>object.toString()</code> becomes
 * <code>[object description]</code>. Since many methods don't have direct
 * equivalents, other code replaces the method invocation. If the replacement
 * code is too lengthy, though, a call to an emulation library is substituted to
 * prevent code bloat.
 *
 * @author Tom Ball
 */
public class JavaToIOSMethodTranslator extends TreeVisitor {

  private Map<IMethodBinding, JavaMethod> descriptions = Maps.newLinkedHashMap();
  private List<IMethodBinding> overridableMethods = Lists.newArrayList();
  private List<IMethodBinding> mappedMethods = Lists.newArrayList();
  private final ITypeBinding javaLangCloneable;

  private final Map<String, IOSMethod> methodMappings;

  private static final Function<String, IOSMethod> IOS_METHOD_FROM_STRING =
      new Function<String, IOSMethod>() {
    public IOSMethod apply(String value) {
      return IOSMethod.create(value);
    }
  };

  public JavaToIOSMethodTranslator(Map<String, String> methodMappings) {
    this.methodMappings =
        Maps.newHashMap(Maps.transformValues(methodMappings, IOS_METHOD_FROM_STRING));
    loadTargetMethods(Types.resolveJavaType("java.lang.Object"));
    loadTargetMethods(Types.resolveJavaType("java.lang.Class"));
    loadTargetMethods(Types.resolveJavaType("java.lang.String"));
    loadTargetMethods(Types.resolveJavaType("java.lang.Number"));
    loadCharSequenceMethods();
    javaLangCloneable = Types.resolveJavaType("java.lang.Cloneable");
  }

  private void loadTargetMethods(ITypeBinding clazz) {
    for (IMethodBinding method : clazz.getDeclaredMethods()) {
      if (method.isConstructor() && Types.isJavaObjectType(method.getDeclaringClass())) {
        continue;  // No mapping needed for new Object();
      }
      if (method.getName().equals("clone")) {
        continue;
      }
      // track all non-final public, protected and package-private methods
      int mods = method.getModifiers();
      if (!Modifier.isPrivate(mods)) {
        if (!Modifier.isFinal(mods)) {
          overridableMethods.add(method);
        }
        mappedMethods.add(method);
        addDescription(method);
      }
    }
  }

  private void loadCharSequenceMethods() {
    ITypeBinding charSequence = Types.resolveJavaType("java.lang.CharSequence");
    for (IMethodBinding method : charSequence.getDeclaredMethods()) {
      if (method.getName().equals("length")) {
        overridableMethods.add(0, method);
        NameTable.rename(method, "sequenceLength");
        mappedMethods.add(method);
        addDescription(method);
      } else if (method.getName().equals("subSequence")) {
        overridableMethods.add(0, method);
        NameTable.rename(method, "subSequenceFrom");
        mappedMethods.add(method);
        addDescription(method);
      }
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    // See if method has been directly mapped.
    IMethodBinding binding = node.getMethodBinding();
    JavaMethod desc = getDescription(binding);
    if (desc != null) {
      mapMethod(node, binding, methodMappings.get(desc.getKey()));
      return true;
    }

    // See if an overrideable superclass method has been mapped.
    for (IMethodBinding overridable : overridableMethods) {
      if (!binding.isConstructor()
          && (binding.isEqualTo(overridable) || binding.overrides(overridable))) {
        JavaMethod md = getDescription(overridable);
        if (md == null) {
          continue;
        }
        String key = md.getKey();
        IOSMethod iosMethod = methodMappings.get(key);
        if (iosMethod != null) {
          mapMethod(node, binding, iosMethod);
        }
        return true;
      }
    }
    return true;
  }

  private void mapMethod(MethodDeclaration node, IMethodBinding binding, IOSMethod iosMethod) {
    IOSMethodBinding iosBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
    node.setName(new SimpleName(iosBinding));
    node.setMethodBinding(iosBinding);

    // Map parameters, if any.
    List<SingleVariableDeclaration> parameters = node.getParameters();
    int n = parameters.size();
    if (n > 0) {
      List<IOSParameter> iosArgs = iosMethod.getParameters();
      assert n == iosArgs.size() || iosMethod.isVarArgs();

      for (int i = 0; i < n; i++) {
        ITypeBinding newParamType = Types.resolveIOSType(iosArgs.get(i).getType());
        if (newParamType != null) {
          parameters.get(i).setType(Type.newType(newParamType));
        }
      }
    }
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    // translate any embedded method invocations
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
    for (Expression e : node.getArguments()) {
      e.accept(this);
    }
    if (node.getAnonymousClassDeclaration() != null) {
      node.getAnonymousClassDeclaration().accept(this);
    }

    IMethodBinding binding = node.getMethodBinding();
    JavaMethod md = descriptions.get(binding);
    if (md != null) {
      assert !node.hasRetainedResult();
      String key = md.getKey();
      if (key.equals("java.lang.String.String(Ljava/lang/String;)V")) {
        // Special case: replace new String(constant) to constant (avoid clang warning).
        Expression arg = node.getArguments().get(0);
        if (arg instanceof StringLiteral) {
          node.replaceWith(arg.copy());
          return false;
        }
      }
      IOSMethod iosMethod = methodMappings.get(key);
      if (iosMethod != null) {
        IOSMethodBinding methodBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
        MethodInvocation newInvocation = new MethodInvocation(methodBinding,
            new SimpleName(Types.resolveIOSType(iosMethod.getDeclaringClass())));

        // Set parameters.
        copyInvocationArguments(null, node.getArguments(), newInvocation.getArguments());

        node.replaceWith(newInvocation);
      } else {
        ErrorUtil.error(node, createMissingMethodMessage(binding));
      }
    }
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    // If this type implements Cloneable but its parent doesn't, add a
    // copyWithZone: method that calls clone().
    ITypeBinding type = node.getTypeBinding();
    if (type.isAssignmentCompatible(javaLangCloneable)) {
      ITypeBinding superclass = type.getSuperclass();
      if (superclass == null || !superclass.isAssignmentCompatible(javaLangCloneable)) {
        addCopyWithZoneMethod(node);
      }
    }
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
    JavaMethod md = getDescription(binding);
    if (md == null && !binding.getName().equals("clone")) { // never map clone()
      IVariableBinding receiver =
          node.getExpression() != null ? TreeUtil.getVariableBinding(node.getExpression()) : null;
      ITypeBinding clazz =
          receiver != null ? receiver.getType() : binding.getDeclaringClass();
      if (clazz != null && !clazz.isArray()) {
        for (IMethodBinding method : descriptions.keySet()) {
          if (binding.isSubsignature(method)
              && clazz.isAssignmentCompatible(method.getDeclaringClass())) {
            md = descriptions.get(method);
            break;
          }
        }
      }
    }
    if (md != null) {
      String key = md.getKey();
      IOSMethod iosMethod = methodMappings.get(key);
      if (iosMethod == null) {
        ErrorUtil.error(node, createMissingMethodMessage(binding));
        return;
      }
      IOSMethodBinding newBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
      node.setMethodBinding(newBinding);
      NameTable.rename(binding, iosMethod.getName());
      if (node.getExpression() instanceof SimpleName) {
        SimpleName expr = (SimpleName) node.getExpression();
        if (expr.getIdentifier().equals(binding.getDeclaringClass().getName())
            || expr.getIdentifier().equals(binding.getDeclaringClass().getQualifiedName())) {
          NameTable.rename(binding.getDeclaringClass(), iosMethod.getDeclaringClass());
        }
      }
    } else {
      // Not mapped, check if it overrides a mapped method.
      for (IMethodBinding methodBinding : mappedMethods) {
        if (binding.overrides(methodBinding)) {
          JavaMethod desc = getDescription(methodBinding);
          if (desc != null) {
            IOSMethod iosMethod = methodMappings.get(desc.getKey());
            if (iosMethod != null) {
              IOSMethodBinding newBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
              node.setMethodBinding(newBinding);
              break;
            }
          }
        }
      }
    }
    return;
  }

  private void copyInvocationArguments(Expression receiver, List<Expression> oldArgs,
      List<Expression> newArgs) {
    // set the receiver as the first argument
    if (receiver != null) {
      Expression delegate = receiver.copy();
      delegate.accept(this);
      newArgs.add(delegate);
    }

    // copy remaining arguments
    for (Expression oldArg : oldArgs) {
      newArgs.add(oldArg.copy());
    }
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    // translate any embedded method invocations
    for (Expression e : node.getArguments()) {
      e.accept(this);
    }

    IMethodBinding binding = node.getMethodBinding();
    JavaMethod md = getDescription(binding);
    if (md != null) {
      String key = md.getKey();
      IOSMethod iosMethod = methodMappings.get(key);
      if (iosMethod == null) {
        // Method has same name as a mapped method's, but it's ignored since
        // it doesn't override it.
        return super.visit(node);
      }
      IOSMethodBinding newBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
      node.setMethodBinding(newBinding);
    } else {
      // Not mapped, check if it overrides a mapped method.
      for (IMethodBinding methodBinding : mappedMethods) {
        if (binding.overrides(methodBinding)) {
          JavaMethod desc = getDescription(methodBinding);
          if (desc != null) {
            IOSMethod iosMethod = methodMappings.get(desc.getKey());
            if (iosMethod != null) {
              IOSMethodBinding newBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
              node.setMethodBinding(newBinding);
            }
          }
        }
      }
    }
    return true;
  }

  private JavaMethod getDescription(IMethodBinding binding) {
    if (descriptions.containsKey(binding)) {
      return descriptions.get(binding);
    }
    return addDescription(binding);
  }

  private JavaMethod addDescription(IMethodBinding binding) {
    JavaMethod desc = JavaMethod.getJavaMethod(binding);
    if (desc != null) {
      if (methodMappings.containsKey(desc.getKey())) {
        descriptions.put(binding, desc);
        return desc;
      }
      String objcName = getObjectiveCNameValue(binding);
      if (objcName != null) {
        try {
          String signature =
              String.format("%s %s", binding.getDeclaringClass().getName(), objcName);
          IOSMethod method = IOSMethod.create(signature);
          methodMappings.put(desc.getKey(),  method);
        } catch (IllegalArgumentException e) {
          ErrorUtil.error("invalid Objective-C method name: " + objcName);
        }
        descriptions.put(binding, desc);
        return desc;
      }
    }
    return null;  // binding isn't mapped.
  }

  /**
   * Returns ObjectiveCName value, or null if method is not annotated.
   * <p>
   * This method warns if source attempts to specify an overridden method
   * that doesn't have the same ObjectiveCName value. This prevents
   * developers from accidentally breaking polymorphic methods.
   *
   * @return the ObjectiveCName value, or null if not annotated or when
   *     a warning is reported.
   */
  private static String getObjectiveCNameValue(IMethodBinding method) {
    IAnnotationBinding annotation = BindingUtil.getAnnotation(method, ObjectiveCName.class);
    if (annotation != null) {
      String selector = (String) BindingUtil.getAnnotationValue(annotation, "value");
      if (BindingUtil.getAnnotation(method, Override.class) != null) {
        // Check that overridden method has same Objective-C name.
        IMethodBinding superMethod = BindingUtil.getOriginalMethodBinding(method);
        if (superMethod != method.getMethodDeclaration()) {
          IAnnotationBinding superAnnotation  =
              BindingUtil.getAnnotation(superMethod, ObjectiveCName.class);
          if (superAnnotation == null) {
            ErrorUtil.warning("ObjectiveCName(" + selector
                + ") set on overridden method that is not also renamed.");
            return null;
          } else {
            String superSelector =
                (String) BindingUtil.getAnnotationValue(superAnnotation, "value");
            if (!selector.equals(superSelector)) {
              ErrorUtil.warning("Conflicting Objective-C names set for " + method
                  + ", which overrides " + superMethod);
              return null;
            }
          }
        }
      }
      return selector;
    }
    return null;
  }

  /**
   * Explicitly walk block statement lists, to work around a bug in
   * ASTNode.visitChildren that skips list members.
   */
  @Override
  public boolean visit(Block node) {
    for (Statement s : node.getStatements()) {
      s.accept(this);
    }
    return false;
  }

  private String createMissingMethodMessage(IMethodBinding binding) {
    StringBuilder sb = new StringBuilder("Internal error: ");
    sb.append(binding.getDeclaringClass().getName());
    if (!binding.isConstructor()) {
      sb.append('.');
      sb.append(binding.getName());
    }
    sb.append('(');
    ITypeBinding[] args = binding.getParameterTypes();
    int nargs = args.length;
    for (int i = 0; i < nargs; i++) {
      sb.append(args[i].getName());
      if (i + 1 < nargs) {
        sb.append(',');
      }
    }
    sb.append(") not mapped");
    return sb.toString();
  }

  private MethodInvocation makeCloneInvocation(ITypeBinding declaringClass) {
    GeneratedMethodBinding cloneBinding = GeneratedMethodBinding.newMethod(
        "clone", 0, Types.resolveIOSType("NSObject"), declaringClass);
    return new MethodInvocation(cloneBinding, null);
  }

  private void addCopyWithZoneMethod(TypeDeclaration node) {
    // Create copyWithZone: method.
    ITypeBinding type = node.getTypeBinding().getTypeDeclaration();
    IOSMethod iosMethod = IOSMethod.create("id copyWithZone:(NSZone *)zone");
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC, Types.resolveIOSType("id"), type);
    MethodDeclaration cloneMethod = new MethodDeclaration(binding);

    // Add NSZone *zone parameter.
    GeneratedVariableBinding zoneBinding = new GeneratedVariableBinding(
        "zone", 0, Types.resolveIOSType("NSZone"), false, true, binding.getDeclaringClass(),
        binding);
    binding.addParameter(zoneBinding.getType());
    cloneMethod.getParameters().add(new SingleVariableDeclaration(zoneBinding));

    Block block = new Block();
    cloneMethod.setBody(block);

    MethodInvocation cloneInvocation = makeCloneInvocation(type);
    block.getStatements().add(new ReturnStatement(cloneInvocation));

    node.getBodyDeclarations().add(cloneMethod);
  }
}
