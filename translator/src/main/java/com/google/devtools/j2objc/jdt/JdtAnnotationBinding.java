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

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Wrapper class around IAnnotationBinding.
 */
public class JdtAnnotationBinding extends JdtBinding implements IAnnotationBinding {
  private JdtTypeBinding annotationType;
  private JdtMemberValuePairBinding[] allValuePairs;
  private JdtMemberValuePairBinding[] declaredValuePairs;

  JdtAnnotationBinding(IAnnotationBinding binding) {
    super(binding);
  }

  public IMemberValuePairBinding[] getAllMemberValuePairs() {
    if (allValuePairs == null) {
      allValuePairs = wrapPairs(((IAnnotationBinding) binding).getAllMemberValuePairs());
    }
    return allValuePairs;
  }

  public ITypeBinding getAnnotationType() {
    if (annotationType == null) {
      annotationType =
          BindingConverter.wrapBinding(((IAnnotationBinding) binding).getAnnotationType());
    }
    return annotationType;
  }

  public IMemberValuePairBinding[] getDeclaredMemberValuePairs() {
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
