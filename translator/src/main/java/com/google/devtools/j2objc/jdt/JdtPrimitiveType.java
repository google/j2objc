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

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

class JdtPrimitiveType extends JdtTypeMirror implements PrimitiveType {

  JdtPrimitiveType(JdtTypeBinding binding) {
    super(binding);
    assert binding.isPrimitive();
  }

  @Override
  public TypeKind getKind() {
    String binaryName = ((JdtTypeBinding) binding).getBinaryName();
    if (binaryName.length() == 1) {
      switch (binaryName.charAt(0)) {
        case 'B': return TypeKind.BYTE;
        case 'C': return TypeKind.CHAR;
        case 'D': return TypeKind.DOUBLE;
        case 'F': return TypeKind.FLOAT;
        case 'I': return TypeKind.INT;
        case 'J': return TypeKind.LONG;
        case 'S': return TypeKind.SHORT;
        case 'V': return TypeKind.VOID;
        case 'Z': return TypeKind.BOOLEAN;
        default:
          // Fall through and throw assertion error.
      }
    }
    throw new AssertionError("unknown primitive type binary name: " + binaryName);
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitPrimitive(this, p);
  }
}
