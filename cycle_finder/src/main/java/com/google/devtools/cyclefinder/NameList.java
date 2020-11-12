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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * Manages a set of suppress list or restrict-to list entries.
 *
 * @author Keith Stanger
 */
public class NameList {

  private Set<String> fields = Sets.newHashSet();
  private SetMultimap<String, String> fieldsWithTypes = HashMultimap.create();
  private Set<String> types = Sets.newHashSet();
  private Set<String> namespaces = Sets.newHashSet();
  private Set<String> outers = Sets.newHashSet();

  public boolean containsField(TypeNode origin, String fieldName) {
    return fields.contains(origin.getQualifiedName() + '.' + fieldName);
  }

  public boolean isSuppressListedTypeForField(String fieldName, TypeNode type) {
    return fieldsWithTypes.containsEntry(fieldName, type.getQualifiedName());
  }

  public boolean hasOuterForType(TypeNode type) {
    return outers.contains(type.getQualifiedName());
  }

  public boolean containsType(TypeNode type) {
    String typeName = type.getQualifiedName();
    if (types.contains(typeName)) {
      return true;
    }
    while (true) {
      if (namespaces.contains(typeName)) {
        return true;
      }
      int idx = typeName.lastIndexOf('.');
      if (idx < 0) {
        break;
      }
      typeName = typeName.substring(0, idx);
    }
    return false;
  }

  private static final Splitter ENTRY_SPLITTER =
      Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();

  public void addEntry(String entry) {
    String[] tokens = Iterables.toArray(ENTRY_SPLITTER.split(entry), String.class);
    if (tokens.length < 2) {
      badEntry(entry);
    }

    String entryType = tokens[0].toLowerCase();
    if (entryType.equals("field")) {
      if (tokens.length == 2) {
        fields.add(tokens[1]);
      } else if (tokens.length == 3) {
        fieldsWithTypes.put(tokens[1], tokens[2]);
      } else {
        badEntry(entry);
      }
    } else if (entryType.equals("type") && tokens.length == 2) {
      types.add(tokens[1]);
    } else if (entryType.equals("namespace") && tokens.length == 2) {
      namespaces.add(tokens[1]);
    } else if (entryType.equals("outer") && tokens.length == 2) {
      outers.add(tokens[1]);
    } else {
      badEntry(entry);
    }
  }

  private void badEntry(String entry) {
    throw new IllegalArgumentException("Invalid suppress-list entry: " + entry);
  }

  public void addFile(String file, String encoding) throws IOException {
    BufferedReader in = new BufferedReader(
        new InputStreamReader(new FileInputStream(new File(file)), encoding));
    try {
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        String entry = line.split("#", 2)[0].trim();
        if (!Strings.isNullOrEmpty(entry)) {
          addEntry(entry);
        }
      }
    } finally {
      in.close();
    }
  }

  public static NameList createFromFiles(Iterable<String> files, String encoding)
      throws IOException {
    NameList nameList = new NameList();
    for (String file : files) {
      nameList.addFile(file, encoding);
    }
    return nameList;
  }
}
