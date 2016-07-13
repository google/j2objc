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

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

/**
 * Wrapper class around IMemberValuePairBinding.
 */
public class JdtMemberValuePairBinding extends JdtBinding implements IMemberValuePairBinding {
  private JdtMethodBinding methodBinding;

  JdtMemberValuePairBinding(IMemberValuePairBinding binding) {
    super(binding);
  }

  public JdtMethodBinding getMethodBinding() {
    if (methodBinding == null) {
      methodBinding =
          BindingConverter.wrapBinding(((IMemberValuePairBinding) binding).getMethodBinding());
    }
    return methodBinding;
  }

  public Object getValue() {
    Object result = ((IMemberValuePairBinding) binding).getValue();
    return result instanceof IBinding ? BindingConverter.wrapBinding((IBinding) result) : result;
  }

  public boolean isDefault() {
    return ((IMemberValuePairBinding) binding).isDefault();
  }
}
