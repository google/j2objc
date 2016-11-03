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

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

class JdtAnnotationValue implements AnnotationValue {
  private final Object value;

  public JdtAnnotationValue(Object value) {
    if (value instanceof IVariableBinding) {
      this.value = BindingConverter.getElement((IVariableBinding) value);
    } else if (value instanceof ITypeBinding) {
      this.value = BindingConverter.getType((ITypeBinding) value);
    } else if (value instanceof IAnnotationBinding) {
      this.value = new JdtAnnotationMirror((IAnnotationBinding) value);
    } else if (value instanceof Object[]) {
      List<AnnotationValue> newValues = new ArrayList<AnnotationValue>();
      for (Object o : ((Object[]) value)) {
        newValues.add(new JdtAnnotationValue(o));
      }
      this.value = newValues;
    } else {
      // It's a string or a wrapped primitive type, like Integer.
      this.value = value;
    }
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
    return v.visit(this, p);
  }
}
