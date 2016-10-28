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

package com.google.devtools.cyclefinder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.util.CaptureInfo;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Collects implicit capture fields from populated CaptureInfo instances.
 */
public class CaptureFields {

  private final Set<TypeElement> haveOuterReference = new HashSet<>();
  private final ListMultimap<TypeElement, VariableElement> captureFields =
      ArrayListMultimap.create();

  public void collect(CompilationUnit unit) {
    unit.accept(new TreeVisitor() {
      @Override
      public void endVisit(TypeDeclaration node) {
        collect(unit, node.getTypeElement());
      }
      @Override
      public void endVisit(AnonymousClassDeclaration node) {
        collect(unit, node.getTypeElement());
      }
    });
  }

  private void collect(CompilationUnit unit, TypeElement type) {
    CaptureInfo captureInfo = unit.getEnv().captureInfo();
    if (captureInfo.getOuterField(type) != null) {
      haveOuterReference.add(type);
    }
    captureFields.replaceValues(type, captureInfo.getCaptureFields(type));
  }

  public boolean hasOuterReference(TypeElement type) {
    return haveOuterReference.contains(type);
  }

  public List<VariableElement> getCaptureFields(TypeElement type) {
    return captureFields.get(type);
  }
}
