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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks dead classes and methods that can be ignored during translation.
 *
 * @author Daniel Connelly
 */
public class DeadCodeMap {

  public static class Builder {
    private final Set<String> deadClasses = new HashSet<String>();
    private final Table<String, String, Set<String>> deadMethods = HashBasedTable.create();
    private final ListMultimap<String, String> deadFields = ArrayListMultimap.create();

    public DeadCodeMap build() {
      ImmutableTable.Builder<String, String, ImmutableSet<String>> deadMethodsBuilder =
          ImmutableTable.builder();
      for (Table.Cell<String, String, Set<String>> cell : this.deadMethods.cellSet()) {
        deadMethodsBuilder.put(
            cell.getRowKey(),
            cell.getColumnKey(),
            ImmutableSet.copyOf(cell.getValue()));
      }
      return new DeadCodeMap(
          ImmutableSet.copyOf(deadClasses),
          deadMethodsBuilder.build(),
          ImmutableMultimap.copyOf(deadFields));
    }

    public Builder addDeadClass(String clazz) {
      deadClasses.add(clazz);
      return this;
    }

    public Builder addDeadMethod(String clazz, String name, String signature) {
      if (!deadMethods.contains(clazz, name)) {
        deadMethods.put(clazz, name, new HashSet<String>());
      }
      deadMethods.get(clazz, name).add(signature);
      return this;
    }

    public Builder addDeadField(String clazz, String field) {
      deadFields.put(clazz, field);
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final ImmutableSet<String> deadClasses;
  private final ImmutableTable<String, String, ImmutableSet<String>> deadMethods;
  private final ImmutableMultimap<String, String> deadFields;

  private DeadCodeMap(
      ImmutableSet<String> deadClasses,
      ImmutableTable<String, String, ImmutableSet<String>> deadMethods,
      ImmutableMultimap<String, String> deadFields) {
    this.deadClasses = deadClasses;
    this.deadMethods = deadMethods;
    this.deadFields = deadFields;
  }

  public boolean isDeadClass(String clazz) {
    return deadClasses.contains(clazz);
  }

  public boolean isDeadMethod(String clazz, String name, String signature) {
    return deadClasses.contains(clazz)
        || deadMethods.contains(clazz, name) && deadMethods.get(clazz, name).contains(signature);
  }

  public boolean isDeadField(String clazz, String field) {
    return deadClasses.contains(clazz) || deadFields.containsEntry(clazz, field);
  }

  public boolean isEmpty() {
    return deadClasses.isEmpty() && deadMethods.isEmpty() && deadFields.isEmpty();
  }
}
