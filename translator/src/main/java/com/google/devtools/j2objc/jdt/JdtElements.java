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

import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Utility methods for elements. Methods not referenced by the
 * translator are not implemented.
 */
// TODO(tball): add to ProcessingEnvironment impl, make package-private.
public class JdtElements implements Elements {

  private static final JdtElements INSTANCE = new JdtElements();

  // TODO(tball): remove when added to ProcessingEnvironment.
  public static JdtElements getInstance() {
    return INSTANCE;
  }

  @Override
  public PackageElement getPackageElement(CharSequence name) {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeElement getTypeElement(CharSequence name) {
    throw new AssertionError("not implemented");
  }

  @Override
  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
      AnnotationMirror a) {
    throw new AssertionError("not implemented");
  }

  @Override
  public String getDocComment(Element e) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isDeprecated(Element e) {
    throw new AssertionError("not implemented");
  }

  @Override
  public Name getBinaryName(TypeElement type) {
    throw new AssertionError("not implemented");
  }

  @Override
  public PackageElement getPackageOf(Element type) {
    Element owner = type.getEnclosingElement();
    while (owner != null) {
      type = owner;
      owner = type.getEnclosingElement();
    }
    return type instanceof PackageElement ? (PackageElement) type : null;
  }

  @Override
  public List<? extends Element> getAllMembers(TypeElement type) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean hides(Element hider, Element hidden) {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean overrides(ExecutableElement overrider, ExecutableElement overridden,
      TypeElement type) {
    throw new AssertionError("not implemented");
  }

  @Override
  public String getConstantExpression(Object value) {
    throw new AssertionError("not implemented");
  }

  @Override
  public void printElements(Writer w, Element... elements) {
    throw new AssertionError("not implemented");
  }

  @Override
  public Name getName(CharSequence cs) {
    return new StringName(cs.toString());
  }

  public boolean isFunctionalInterface(TypeElement type) {
    throw new AssertionError("not implemented");
  }
}
