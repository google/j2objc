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

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * Abstract base class for IBinding providing default implementations for
 * most interface methods.
 *
 * @author Keith Stanger
 */
public abstract class AbstractBinding implements IBinding {

  private List<IAnnotationBinding> annotations = Lists.newArrayList();

  @Override
  public IAnnotationBinding[] getAnnotations() {
    return annotations.toArray(new IAnnotationBinding[annotations.size()]);
  }

  public void addAnnotations(IBinding binding) {
    annotations.addAll(Arrays.asList(binding.getAnnotations()));
  }

  public void addAnnotation(IAnnotationBinding annotation) {
    annotations.add(annotation);
  }

  @Override
  public IJavaElement getJavaElement() {
    throw new AssertionError("not implemented");
  }

  @Override
  public int getModifiers() {
    return 0;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public boolean isRecovered() {
    return false;
  }

  @Override
  public boolean isSynthetic() {
    return true;
  }
}
