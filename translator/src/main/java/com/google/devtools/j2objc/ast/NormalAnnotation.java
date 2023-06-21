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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;

/**
 * Node type for an annotation with member value pairs.
 */
public class NormalAnnotation extends Annotation {

  private ChildList<MemberValuePair> values = ChildList.create(MemberValuePair.class, this);

  public NormalAnnotation() {}

  public NormalAnnotation(NormalAnnotation other) {
    super(other);
    values.copyFrom(other.getValues());
  }

  @Override
  public Kind getKind() {
    return Kind.NORMAL_ANNOTATION;
  }

  public List<MemberValuePair> getValues() {
    return values;
  }

  public void addValue(MemberValuePair value) {
    values.add(value);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      typeName.accept(visitor);
      if (needsReflection && ElementUtil.isRuntimeAnnotation(this.getAnnotationMirror())) {
        values.accept(visitor);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public NormalAnnotation copy() {
    return new NormalAnnotation(this);
  }
}
