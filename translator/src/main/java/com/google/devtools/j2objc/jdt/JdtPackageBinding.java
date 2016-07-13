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

package com.google.devtools.j2objc.jdt;

import org.eclipse.jdt.core.dom.IPackageBinding;

/**
 * Wrapper class around IPackageBinding.
 */
public class JdtPackageBinding extends JdtBinding implements IPackageBinding {

  protected JdtPackageBinding(IPackageBinding binding) {
    super(binding);
  }

  @Override
  public String[] getNameComponents() {
    return ((IPackageBinding) binding).getNameComponents();
  }

  @Override
  public boolean isUnnamed() {
    return ((IPackageBinding) binding).isUnnamed();
  }

}
