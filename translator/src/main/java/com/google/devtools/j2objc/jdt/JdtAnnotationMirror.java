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

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

class JdtAnnotationMirror implements AnnotationMirror {

  final IAnnotationBinding binding;

  JdtAnnotationMirror(IAnnotationBinding binding) {
    this.binding = binding;
  }

  @Override
  public DeclaredType getAnnotationType() {
    return (DeclaredType) BindingConverter.getType(binding.getAnnotationType());
  }

  @Override
  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
    Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
    for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
      ExecutableElement element = new JdtExecutableElement(pair.getMethodBinding());
      AnnotationValue value = new JdtAnnotationValue(pair.getValue());
      elementValues.put(element, value);
    }
    return elementValues;
  }
}
