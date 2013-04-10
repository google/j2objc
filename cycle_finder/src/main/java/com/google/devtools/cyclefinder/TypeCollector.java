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

package com.google.devtools.cyclefinder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Recursively visits links between type bindings, collecting all reachable
 * types.
 */
class TypeCollector {

  private Map<String, ITypeBinding> allTypes = Maps.newHashMap();
  private SetMultimap<String, IVariableBinding> anonymousCaptures = HashMultimap.create();
  private Set<String> staticAnonymousClasses = Sets.newHashSet();

  public Map<String, ITypeBinding> getTypes() {
    return allTypes;
  }

  public Set<IVariableBinding> getCaptures(String anonymousType) {
    return anonymousCaptures.get(anonymousType);
  }

  public boolean isStaticAnonymousClass(String key) {
    return staticAnonymousClasses.contains(key);
  }

  public void visitType(ITypeBinding type) {
    if (type == null) {
      return;
    }
    type = getElementType(type);
    if (allTypes.containsKey(type.getKey()) || type.isPrimitive()) {
      return;
    }
    if (type.isParameterizedType()) {
      for (ITypeBinding typeParam : type.getTypeArguments()) {
        if (typeParam.isWildcardType() && typeParam.getBound() != null
            && typeParam.getBound().isWildcardType()) {
          // Double wildcard, this might recurse infinitely.
          return;
        }
      }
    }
    allTypes.put(type.getKey(), type);
    visitType(type.getSuperclass());
    visitType(type.getDeclaringClass());
    for (IVariableBinding field : type.getDeclaredFields()) {
      visitType(field.getType());
    }
    for (ITypeBinding interfaze : type.getInterfaces()) {
      visitType(interfaze);
    }
  }

  public void visitAST(ASTNode ast) {
    ast.accept(new ASTVisitor() {
      @Override
      public boolean visit(TypeDeclaration node) {
        visitType(node.resolveBinding());
        return true;
      }
      @Override
      public boolean visit(AnonymousClassDeclaration node) {
        visitType(node.resolveBinding());
        MethodDeclaration parentMethod = getEnclosingMethodDeclaration(node);
        if (parentMethod != null) {
          parentMethod.accept(new CaptureFinder());
        }
        if (isStaticContext(node)) {
          staticAnonymousClasses.add(node.resolveBinding().getKey());
        }
        return true;
      }
      @Override
      public boolean visit(ClassInstanceCreation node) {
        visitType(node.resolveTypeBinding());
        return true;
      }
      @Override
      public boolean visit(MethodInvocation node) {
        visitType(node.resolveTypeBinding());
        return true;
      }
    });
  }

  private static boolean isStaticContext(AnonymousClassDeclaration node) {
    ITypeBinding type = node.resolveBinding();
    IMethodBinding declaringMethod = type.getDeclaringMethod();
    if (declaringMethod != null) {
      return Modifier.isStatic(declaringMethod.getModifiers());
    }
    ASTNode parent = node.getParent();
    while (parent != null) {
      if (parent instanceof BodyDeclaration) {
        return Modifier.isStatic(((BodyDeclaration) parent).getModifiers());
      }
      parent = parent.getParent();
    }
    return false;
  }

  private static MethodDeclaration getEnclosingMethodDeclaration(ASTNode node) {
    while (node != null) {
      if (node instanceof MethodDeclaration) {
        return (MethodDeclaration) node;
      }
      node = node.getParent();
    }
    return null;
  }

  private ITypeBinding getElementType(ITypeBinding type) {
    if (type.isArray()) {
      return type.getElementType();
    }
    return type;
  }

  private class CaptureFinder extends ASTVisitor {
    private ArrayList<IVariableBinding> finalVars = Lists.newArrayList();
    private ArrayList<Integer> stack = Lists.newArrayList();
    private int idx = 0;

    private void handleVariableDeclaration(VariableDeclaration node) {
      IVariableBinding var = node.resolveBinding();
      if (!getElementType(var.getType()).isPrimitive() && Modifier.isFinal(var.getModifiers())) {
        finalVars.add(idx++, var);
      }
    }

    @Override
    public void endVisit(SingleVariableDeclaration node) {
      handleVariableDeclaration(node);
    }

    @Override
    public void endVisit(VariableDeclarationFragment node) {
      handleVariableDeclaration(node);
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      String anonymousType = node.resolveBinding().getKey();
      for (int i = 0; i < idx; i++) {
        IVariableBinding var = finalVars.get(i);
        visitType(var.getType());
        anonymousCaptures.put(anonymousType, var);
      }
      return false;
    }

    @Override
    public boolean visit(Block node) {
      stack.add(idx);
      return true;
    }

    @Override
    public void endVisit(Block node) {
      idx = stack.remove(stack.size() - 1);
    }

    @Override
    public boolean visit(ForStatement node) {
      stack.add(idx);
      return true;
    }

    @Override
    public void endVisit(ForStatement node) {
      idx = stack.remove(stack.size() - 1);
    }
  }
}
