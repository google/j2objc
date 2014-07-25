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

package com.google.devtools.j2objc.gen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Map;
import java.util.Set;

/**
 * Scans for parameters and local variable declarations that hide
 * fields.  Java allows this, but gcc warns loudly.
 *
 * @author Tom Ball
 */
public class HiddenFieldDetector extends TreeVisitor {
  private final Set<IVariableBinding> fieldNameConflicts = Sets.newLinkedHashSet();

  /**
   * A map of each type to a set of their field names.
   */
  Map<String, Set<String>> fieldNameMap = Maps.newHashMap();
  private static final Set<String> NO_FIELDS = Sets.newLinkedHashSet();

  public static Set<IVariableBinding> getFieldNameConflicts(TreeNode node) {
    HiddenFieldDetector detector = new HiddenFieldDetector();
    detector.run(node);
    return detector.fieldNameConflicts;
  }


  @Override
  public boolean visit(TypeDeclaration node) {
    if (!node.isInterface()) {
      Set<String> names = Sets.newLinkedHashSet();
      ITypeBinding binding = node.getTypeBinding();
      addFields(binding, true, names);
      fieldNameMap.put(binding.getBinaryName(), names);
    }
    return super.visit(node);
  }

  private void addFields(ITypeBinding binding, boolean includePrivate, Set<String> names) {
    for (IVariableBinding field : binding.getDeclaredFields()) {
      if (includePrivate || (field.getModifiers() & Modifier.PRIVATE) == 0) {
        names.add(NameTable.javaFieldToObjC(field.getName()));
      }
    }
    ITypeBinding superType = binding.getSuperclass();
    if (superType != null) {
      addFields(superType, false, names);
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    IMethodBinding binding = node.getMethodBinding();
    if (binding != null) {
      Set<String> fieldNames = fieldNameMap.get(binding.getDeclaringClass().getBinaryName());
      if (fieldNames == null) {
        fieldNames = NO_FIELDS;
      }

      for (SingleVariableDeclaration param : node.getParameters()) {
        IVariableBinding varBinding = param.getVariableBinding();
        if (varBinding != null && fieldNames.contains(varBinding.getName())) {
          fieldNameConflicts.add(varBinding);
        }
      }
    }
    return super.visit(node);
  }
}
