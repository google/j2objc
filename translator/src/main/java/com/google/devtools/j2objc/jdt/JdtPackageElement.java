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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IPackageBinding;

class JdtPackageElement extends JdtElement implements PackageElement {
  private List<AnnotationMirror> annotations = new ArrayList<>();

  JdtPackageElement(IPackageBinding binding) {
    super(binding, binding.getName(), 0);
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.PACKAGE;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitPackage(this, p);
  }

  @Override
  public Element getEnclosingElement() {
    return null;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return Collections.emptySet();
  }

  @Override
  public TypeMirror asType() {
    return null;
  }

  @Override
  public Name getQualifiedName() {
    return name;
  }

  @Override
  public boolean isUnnamed() {
    return name.length() == 0;
  }

  @Override
  public String toString() {
    return name.toString();
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return annotations;
  }

  public void addAnnotation(AnnotationMirror annotation) {
    annotations.add(annotation);
  }
}
