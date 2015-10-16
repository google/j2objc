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

package com.google.devtools.j2objc.types;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A binding type for functions.
 *
 * @author Keith Stanger
 */
public class FunctionBinding {

  private final String name;
  private final ITypeBinding returnType;
  private final ITypeBinding declaringClass;
  private List<ITypeBinding> parameterTypes = new ArrayList<>();
  private boolean isVarargs = false;

  public FunctionBinding(String name, ITypeBinding returnType, ITypeBinding declaringClass) {
    this.name = name;
    this.returnType = returnType;
    this.declaringClass = declaringClass;
  }

  public String getName() {
    return name;
  }

  public ITypeBinding getReturnType() {
    return returnType;
  }

  public ITypeBinding getDeclaringClass() {
    return declaringClass;
  }

  public List<ITypeBinding> getParameterTypes() {
    return parameterTypes;
  }

  public void addParameter(ITypeBinding paramType) {
    parameterTypes.add(paramType);
  }

  public void addParameters(ITypeBinding... paramTypes) {
    for (ITypeBinding paramType : paramTypes) {
      parameterTypes.add(paramType);
    }
  }

  public boolean isVarargs() {
    return isVarargs;
  }

  public void setIsVarargs(boolean value) {
    isVarargs = value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(returnType.getName() + " " + name + "(");
    for (int i = 0; i < parameterTypes.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(parameterTypes.get(i).getName());
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof FunctionBinding)) {
      return false;
    }
    // C functions can't be overloaded, so it is sufficient to compare the names.
    return ((FunctionBinding) other).getName().equals(name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
