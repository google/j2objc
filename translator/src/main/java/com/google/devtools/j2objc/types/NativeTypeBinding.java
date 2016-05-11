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

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Type binding for native C types.
 *
 * @author Keith Stanger
 */
public class NativeTypeBinding extends AbstractTypeBinding {

  private final String name;

  public NativeTypeBinding(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getQualifiedName() {
    return name;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public String getKey() {
    return "NATIVE_" + name.replace(" ", "_");
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    return isEqualTo(variableType);
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    return binding instanceof NativeTypeBinding
        && ((NativeTypeBinding) binding).getName().equals(name);
  }
}
