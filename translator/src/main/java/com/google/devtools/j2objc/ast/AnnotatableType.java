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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.jdt.TreeConverter;
import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Abstract base class of AST nodes that represent an annotatable type (added in JLS8 API, section
 * 9.7.4).
 */
public abstract class AnnotatableType extends Type {

  protected ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);

  public AnnotatableType(org.eclipse.jdt.core.dom.AnnotatableType jdtNode) {
    super(jdtNode);
    if (Options.isJava8Translator()) {
      for (Object x : jdtNode.annotations()) {
        annotations.add((Annotation) TreeConverter.convert(x));
      }
    }
  }

  public AnnotatableType(AnnotatableType other) {
    super(other);
    annotations.copyFrom(other.annotations());
  }

  public AnnotatableType(TypeMirror typeMirror) {
    super(typeMirror);
  }

  public List<Annotation> annotations() {
    return annotations;
  }

  @Override
  public boolean isAnnotatable() {
    return true;
  }

  @Override
  public abstract AnnotatableType copy();
}
