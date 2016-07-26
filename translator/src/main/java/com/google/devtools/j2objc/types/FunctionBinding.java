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
import com.google.devtools.j2objc.jdt.BindingConverter;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * A binding type for functions.
 *
 * @author Keith Stanger
 */
public class FunctionBinding {

  private final String name;
  // Some functions (eg. constructors) have an equivalent function that returns
  // a retained result.
  private final String retainedResultName;
  private final TypeMirror returnType;
  private final TypeMirror declaringClass;
  private List<TypeMirror> parameterTypes = new ArrayList<>();
  private boolean isVarargs = false;

  public FunctionBinding(
      String name, String retainedResultName, TypeMirror returnType,
      TypeMirror declaringClass) {
    this.name = name;
    this.retainedResultName = retainedResultName;
    this.returnType = returnType;
    this.declaringClass = declaringClass;
  }

  public FunctionBinding(String name, String retainedResultName, ITypeBinding returnType,
      ITypeBinding declaringClass) {
    this(name, retainedResultName, BindingConverter.getType(returnType),
        BindingConverter.getType(declaringClass));
  }

  public FunctionBinding(String name, ITypeBinding returnType, ITypeBinding declaringClass) {
    this(name, null, BindingConverter.getType(returnType),
        BindingConverter.getType(declaringClass));
  }

  public FunctionBinding(String name, TypeMirror returnType, TypeMirror declaringClass) {
    this(name, null, returnType, declaringClass);
  }

  public String getName() {
    return name;
  }

  public String getRetainedResultName() {
    return retainedResultName;
  }

  public TypeMirror getReturnType() {
    return returnType;
  }

  public TypeMirror getDeclaringClass() {
    return declaringClass;
  }

  public List<TypeMirror> getParameterTypes() {
    return parameterTypes;
  }

  public void addParameters(ITypeBinding... paramTypes) {
    for (ITypeBinding paramType : paramTypes) {
      parameterTypes.add(BindingConverter.getType(paramType));
    }
  }

  public void addParameters(TypeMirror... paramTypes) {
    for (TypeMirror paramType : paramTypes) {
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
    sb.append(returnType.toString() + " " + name + "(");
    for (int i = 0; i < parameterTypes.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(parameterTypes.get(i).toString());
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
