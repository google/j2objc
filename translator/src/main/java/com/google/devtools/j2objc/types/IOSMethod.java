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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * An immutable description of an iOS method, containing its name, type and
 * parameters.  These descriptions are used when mapping Java method
 * use to equivalent iOS methods.
 *
 * @see JavaMethod
 * @author Tom Ball
 */
public class IOSMethod {
  private final String name;
  private final String declaringClass;
  private final List<IOSParameter> parameters;
  private boolean varArgs = false;

  private IOSMethod(
      String name, String declaringClass, List<IOSParameter> parameters, boolean varArgs) {
    this.name = name;
    this.declaringClass = declaringClass;
    this.parameters = parameters != null ? parameters : Collections.<IOSParameter>emptyList();
    this.varArgs = varArgs;
  }

  public static IOSMethod create(String s) {
    if (s.endsWith(";")) {
      s = s.substring(0, s.length() - 1);
    }
    int i = s.indexOf(' ');
    String className = s.substring(0, i);
    s = s.substring(i + 1);

    ImmutableList.Builder<IOSParameter> parameters = ImmutableList.builder();
    String name = s;
    boolean varArgs = false;
    i = s.indexOf(':');
    if (i > 0) {  // if there are parameters
      name = s.substring(0, i);
      String[] argDefs = splitParameterString(s);
      for (i = 0; i < argDefs.length; i++) {
        IOSParameter param = new IOSParameter(argDefs[i], i);
        parameters.add(param);
        if (param.isVarArgs()) {
          varArgs = true;
          break;
        }
      }
    }
    return new IOSMethod(name, className, parameters.build(), varArgs);
  }

  public String getName() {
    return name;
  }

  public String getDeclaringClass() {
    return declaringClass;
  }

  public List<IOSParameter> getParameters() {
    return parameters;
  }

  public boolean isVarArgs() {
    return varArgs;
  }

  private static String[] splitParameterString(String s) {
    List<String> result = Lists.newArrayList();
    String[] parts = s.split(" ");
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      // If a type has spaces in it (ie, foo *), combine the parts.
      if (part.contains("(")) {
        while (!part.contains(")")) {
          if (i + 1 >= parts.length) {
            throw new IllegalArgumentException("invalid Objective-C parameter string: " + s);
          }
          part += ' ' + parts[++i];
        }
      }
      result.add(part);
    }
    return result.toArray(new String[0]);
  }
}
