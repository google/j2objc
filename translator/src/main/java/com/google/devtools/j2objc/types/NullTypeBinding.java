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
 * Binding class for the null type.
 *
 * @author Keith Stanger
 */
public class NullTypeBinding extends AbstractTypeBinding {

  private static final NullTypeBinding INSTANCE = new NullTypeBinding();

  public static NullTypeBinding getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isNullType() {
    return true;
  }

  @Override
  public String getName() {
    return "null";
  }

  @Override
  public String getQualifiedName() {
    return getName();
  }

  @Override
  public String getKey() {
    return "N";
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    return !variableType.isPrimitive();
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    return binding instanceof ITypeBinding && ((ITypeBinding) binding).isNullType();
  }
}
