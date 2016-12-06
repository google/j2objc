/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.devtools.j2objc.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Tracks classes, fields, and methods that are referenced in source code.
 *
 * @author Daniel Connelly
 */
public class CodeReferenceMap {

  public static class Builder {
    private final Set<String> deadClasses = new HashSet<String>();
    private final Table<String, String, Set<String>> deadMethods = HashBasedTable.create();
    private final ListMultimap<String, String> deadFields =
        MultimapBuilder.hashKeys().arrayListValues().build();

    public CodeReferenceMap build() {
      ImmutableTable.Builder<String, String, ImmutableSet<String>> deadMethodsBuilder =
          ImmutableTable.builder();
      for (Table.Cell<String, String, Set<String>> cell : this.deadMethods.cellSet()) {
        deadMethodsBuilder.put(
            cell.getRowKey(),
            cell.getColumnKey(),
            ImmutableSet.copyOf(cell.getValue()));
      }
      return new CodeReferenceMap(
          ImmutableSet.copyOf(deadClasses),
          deadMethodsBuilder.build(),
          ImmutableMultimap.copyOf(deadFields));
    }

    public Builder addClass(String clazz) {
      deadClasses.add(clazz);
      return this;
    }

    public Builder addMethod(String clazz, String name, String signature) {
      if (!deadMethods.contains(clazz, name)) {
        deadMethods.put(clazz, name, new HashSet<String>());
      }
      deadMethods.get(clazz, name).add(signature);
      return this;
    }

    public Builder addField(String clazz, String field) {
      deadFields.put(clazz, field);
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final ImmutableSet<String> referencedClasses;
  private final ImmutableTable<String, String, ImmutableSet<String>> referencedMethods;
  private final ImmutableMultimap<String, String> referencedFields;
  private final Set<String> hasConstructorRemovedClasses = new HashSet<>();

  private CodeReferenceMap(
      ImmutableSet<String> referencedClasses,
      ImmutableTable<String, String, ImmutableSet<String>> referencedMethods,
      ImmutableMultimap<String, String> referencedFields) {
    this.referencedClasses = referencedClasses;
    this.referencedMethods = referencedMethods;
    this.referencedFields = referencedFields;
  }

  public ImmutableSet<String> getReferencedClasses() {
    return referencedClasses;
  }

  public ImmutableTable<String, String, ImmutableSet<String>> getReferencedMethods() {
    return referencedMethods;
  }

  public ImmutableMultimap<String, String> getReferencedFields() {
    return referencedFields;
  }

  public boolean containsClass(String clazz) {
    return referencedClasses.contains(clazz);
  }

  public boolean containsClass(TypeElement clazz, ElementUtil elementUtil) {
    return containsClass(elementUtil.getBinaryName(clazz));
  }

  public boolean containsMethod(String clazz, String name, String signature) {
    return referencedClasses.contains(clazz)
        || (referencedMethods.contains(clazz, name)
           && referencedMethods.get(clazz, name).contains(signature));
  }

  public boolean containsMethod(ExecutableElement method, TypeUtil typeUtil) {
    String className = typeUtil.elementUtil().getBinaryName(ElementUtil.getDeclaringClass(method));
    String methodName = typeUtil.getReferenceName(method);
    String methodSig = typeUtil.getReferenceSignature(method);
    return containsMethod(className, methodName, methodSig);
  }

  public boolean containsField(String clazz, String field) {
    return referencedClasses.contains(clazz) || referencedFields.containsEntry(clazz, field);
  }

  public boolean isEmpty() {
    return referencedClasses.isEmpty() && referencedMethods.isEmpty() && referencedFields.isEmpty();
  }

  public void addConstructorRemovedClass(String clazz) {
    hasConstructorRemovedClasses.add(clazz);
  }

  public boolean classHasConstructorRemoved(String clazz) {
    return hasConstructorRemovedClasses.contains(clazz);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(referencedClasses.asList().toString() + "\n");
    builder.append(referencedFields.toString() + "\n");
    builder.append(referencedMethods.toString());

    return builder.toString();
  }
}
