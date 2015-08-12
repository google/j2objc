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

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Abstract base class of AST nodes that represent an annotatable type (added in JLS8 API, section
 * 9.7.4).
 */
public abstract class AnnotatableType extends Type {

  protected ChildList<Annotation> annotations = ChildList.create(Annotation.class, this);

  public AnnotatableType(org.eclipse.jdt.core.dom.AnnotatableType jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveBinding();
    if (Options.isJava8Translator()) {
      for (Object x : jdtNode.annotations()) {
        annotations.add((Annotation) TreeConverter.convert(x));
      }
    }
  }

  public AnnotatableType(AnnotatableType other) {
    super(other);
    typeBinding = other.getTypeBinding();
    annotations.copyFrom(other.annotations());
  }

  public AnnotatableType(ITypeBinding typeBinding) {
    super(typeBinding);
  }

  public List<Annotation> annotations() {
    return annotations;
  }

  public boolean isAnnotatable() {
    return true;
  }

  @Override
  public abstract AnnotatableType copy();
}
