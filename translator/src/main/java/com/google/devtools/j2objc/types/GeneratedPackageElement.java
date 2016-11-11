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

package com.google.devtools.j2objc.types;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;

/**
 * Element class for packages created during translation.
 * TODO(kstanger): Eliminate this class in favor of Javac's Elements.getPackageElement().
 *
 * @author Keith Stanger
 */
public class GeneratedPackageElement extends GeneratedElement implements PackageElement {

  public GeneratedPackageElement(String name) {
    super(name, ElementKind.PACKAGE, null, false);
  }

  @Override
  public Name getQualifiedName() {
    return getSimpleName();
  }

  @Override
  public boolean isUnnamed() {
    return getName().isEmpty();
  }

  @Override
  public TypeMirror asType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitPackage(this, p);
  }
}
