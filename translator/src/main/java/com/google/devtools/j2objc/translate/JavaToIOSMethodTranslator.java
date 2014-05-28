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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSParameter;
import com.google.devtools.j2objc.types.JavaMethod;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
public class JavaToIOSMethodTranslator extends ErrorReportingASTVisitor {
  private AST ast;
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

  public JavaToIOSMethodTranslator(AST ast, Map<String, String> methodMappings) {
    this.ast = ast;
    this.methodMappings =
        ImmutableMap.copyOf(Maps.transformValues(methodMappings, IOS_METHOD_FROM_STRING));
    loadTargetMethods(ast.resolveWellKnownType("java.lang.Object"));
    loadTargetMethods(ast.resolveWellKnownType("java.lang.Class"));
    loadTargetMethods(ast.resolveWellKnownType("java.lang.String"));
    loadTargetMethods(Types.resolveJavaType("java.lang.Number"));
    loadCharSequenceMethods();
    javaLangCloneable = ast.resolveWellKnownType("java.lang.Cloneable");
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
    IMethodBinding binding = Types.getMethodBinding(node);
    JavaMethod desc = getDescription(binding);
    if (desc != null) {
      mapMethod(node, binding, methodMappings.get(desc.getKey()));
      return true;
    }

    // See if an overrideable superclass method has been mapped.
    for (IMethodBinding overridable : overridableMethods) {
      if (!binding.isConstructor() &&
          (binding.isEqualTo(overridable) || binding.overrides(overridable))) {
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
    node.setName(ASTFactory.newSimpleName(ast, iosBinding));
    Types.addBinding(node, iosBinding);

    // Map parameters, if any.
    List<SingleVariableDeclaration> parameters = ASTUtil.getParameters(node);
    int n = parameters.size();
    if (n > 0) {
      List<IOSParameter> iosArgs = iosMethod.getParameters();
      assert n == iosArgs.size() || iosMethod.isVarArgs();

      for (int i = 0; i < n; i++) {
        ITypeBinding newParamType = Types.resolveIOSType(iosArgs.get(i).getType());
        if (newParamType != null) {
          parameters.get(i).setType(ASTFactory.newType(ast, newParamType));
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
    for (Expression e : ASTUtil.getArguments(node)) {
      e.accept(this);
    }
    if (node.getAnonymousClassDeclaration() != null) {
      node.getAnonymousClassDeclaration().accept(this);
    }

    IMethodBinding binding = Types.getMethodBinding(node);
    JavaMethod md = descriptions.get(binding);
    if (md != null) {
      String key = md.getKey();
      IOSMethod iosMethod = methodMappings.get(key);
      if (iosMethod != null) {
        IOSMethodBinding methodBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
        MethodInvocation newInvocation = ASTFactory.newMethodInvocation(ast, methodBinding,
            ASTFactory.newSimpleName(ast, Types.resolveIOSType(iosMethod.getDeclaringClass())));

        // Set parameters.
        copyInvocationArguments(null, ASTUtil.getArguments(node),
            ASTUtil.getArguments(newInvocation));

        ASTUtil.setProperty(node, newInvocation);
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
    ITypeBinding type = Types.getTypeBinding(node);
    if (type.isAssignmentCompatible(javaLangCloneable)) {
      ITypeBinding superclass = type.getSuperclass();
      if (superclass == null || !superclass.isAssignmentCompatible(javaLangCloneable)) {
        addCopyWithZoneMethod(node);
      }
    }
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    JavaMethod md = getDescription(binding);
    if (md == null && !binding.getName().equals("clone")) { // never map clone()
      IVariableBinding receiver =
          node.getExpression() != null ? Types.getVariableBinding(node.getExpression()) : null;
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
      Types.addBinding(node, newBinding);
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
              Types.addBinding(node, newBinding);
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
      Expression delegate = NodeCopier.copySubtree(ast, receiver);
      delegate.accept(this);
      newArgs.add(delegate);
    }

    // copy remaining arguments
    for (Expression oldArg : oldArgs) {
      newArgs.add(NodeCopier.copySubtree(ast, oldArg));
    }
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    // translate any embedded method invocations
    for (Expression e : ASTUtil.getArguments(node)) {
      e.accept(this);
    }

    IMethodBinding binding = Types.getMethodBinding(node);
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
      Types.addBinding(node, newBinding);
    } else {
      // Not mapped, check if it overrides a mapped method.
      for (IMethodBinding methodBinding : mappedMethods) {
        if (binding.overrides(methodBinding)) {
          JavaMethod desc = getDescription(methodBinding);
          if (desc != null) {
            IOSMethod iosMethod = methodMappings.get(desc.getKey());
            if (iosMethod != null) {
              IOSMethodBinding newBinding = IOSMethodBinding.newMappedMethod(iosMethod, binding);
              Types.addBinding(node, newBinding);
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
    if (desc != null && methodMappings.containsKey(desc.getKey())) {
      descriptions.put(binding, desc);
      return desc;
    }
    return null;  // binding isn't mapped.
  }

  /**
   * Explicitly walk block statement lists, to work around a bug in
   * ASTNode.visitChildren that skips list members.
   */
  @Override
  public boolean visit(Block node) {
    for (Statement s : ASTUtil.getStatements(node)) {
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

  private SingleVariableDeclaration makeZoneParameter(GeneratedVariableBinding zoneBinding) {
    SimpleName zoneName = ast.newSimpleName("zone");
    Types.addBinding(zoneName, zoneBinding);
    SingleVariableDeclaration zoneParam = ast.newSingleVariableDeclaration();
    zoneParam.setName(zoneName);
    zoneParam.setType(ASTFactory.newType(ast, zoneBinding.getType()));
    Types.addBinding(zoneParam, zoneBinding);
    return zoneParam;
  }

  private MethodInvocation makeCloneInvocation(ITypeBinding declaringClass,
      GeneratedVariableBinding zoneBinding) {
    GeneratedMethodBinding cloneBinding = GeneratedMethodBinding.newMethod(
        "clone", 0, Types.resolveIOSType("NSObject"), declaringClass);
    return ASTFactory.newMethodInvocation(ast, cloneBinding, null);
  }

  private void addCopyWithZoneMethod(TypeDeclaration node) {
    // Create copyWithZone: method.
    ITypeBinding type = Types.getTypeBinding(node).getTypeDeclaration();
    IOSMethod iosMethod = IOSMethod.create("id copyWithZone:(NSZone *)zone");
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC, Types.resolveIOSType("id"), type);
    MethodDeclaration cloneMethod = ASTFactory.newMethodDeclaration(ast, binding);

    // Add NSZone *zone parameter.
    GeneratedVariableBinding zoneBinding = new GeneratedVariableBinding(
        "zone", 0, Types.resolveIOSType("NSZone"), false, true, binding.getDeclaringClass(),
        binding);
    binding.addParameter(zoneBinding.getType());
    ASTUtil.getParameters(cloneMethod).add(makeZoneParameter(zoneBinding));

    Block block = ast.newBlock();
    cloneMethod.setBody(block);

    MethodInvocation cloneInvocation = makeCloneInvocation(type, zoneBinding);
    ReturnStatement returnStmt = ast.newReturnStatement();
    returnStmt.setExpression(cloneInvocation);
    ASTUtil.getStatements(block).add(returnStmt);

    ASTUtil.getBodyDeclarations(node).add(cloneMethod);
  }
}
