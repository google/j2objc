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

package com.google.devtools.j2objc.javac;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

/**
 * Wrapper class around IAnnotationBinding.
 */
public class JdtAnnotationBinding extends JdtBinding implements IAnnotationBinding {
  private final JdtTypeBinding annotationType;
  private JdtMemberValuePairBinding[] allValuePairs;
  private JdtMemberValuePairBinding[] declaredValuePairs;

  JdtAnnotationBinding(IAnnotationBinding binding) {
    super(binding);
    this.annotationType = BindingConverter.wrapBinding(binding.getAnnotationType());
  }

  public JdtMemberValuePairBinding[] getAllMemberValuePairs() {
    if (allValuePairs == null) {
      allValuePairs = wrapPairs(((IAnnotationBinding) binding).getAllMemberValuePairs());
    }
    return allValuePairs;
  }

  public JdtTypeBinding getAnnotationType() {
    return annotationType;
  }

  public JdtMemberValuePairBinding[] getDeclaredMemberValuePairs() {
    if (declaredValuePairs == null) {
      declaredValuePairs = wrapPairs(((IAnnotationBinding) binding).getDeclaredMemberValuePairs());
    }
    return declaredValuePairs;
  }

  private static JdtMemberValuePairBinding[] wrapPairs(IMemberValuePairBinding[] pairs) {
    JdtMemberValuePairBinding[] result = new JdtMemberValuePairBinding[pairs.length];
    for (int i = 0; i < pairs.length; i++) {
      result[i] = BindingConverter.wrapBinding(pairs[i]);
    }
    return result;
  }
}
