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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Node type for a com.google.j2objc.annotations.Property annotation.
 */
public class PropertyAnnotation extends Annotation {

  private final Set<String> attributes;

  private static final ImmutableList<String> PROPERTY_ATTRIBUTES =
      ImmutableList.of(
          "weak",
          "readonly",
          "copy",
          "assign",
          "nonatomic",
          "getter",
          "setter",
          "retain",
          "unsafe_unretained",
          "class",
          "nonnull",
          "nullable",
          "null_resettable",
          "null_unspecified",
          // Default values aren't kept, but may be listed in set when debugging.
          "atomic",
          "readwrite",
          "strong");
  private static final Ordering<String> ATTRIBUTE_ORDERING = Ordering.explicit(PROPERTY_ATTRIBUTES);
  private static final Comparator<String> ATTRIBUTES_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
      if (a.startsWith("getter")) { a = "getter"; }
      if (a.startsWith("setter")) { a = "setter"; }
      if (b.startsWith("getter")) { b = "getter"; }
      if (b.startsWith("setter")) { b = "setter"; }
      return ATTRIBUTE_ORDERING.compare(a, b);
    }
  };

  public PropertyAnnotation() {
    this.attributes = Sets.newHashSet();
  }

  public PropertyAnnotation(PropertyAnnotation other) {
    super(other);
    this.attributes = new HashSet<String>(other.attributes);
  }

  @Override
  public Kind getKind() {
    return Kind.PROPERTY_ANNOTATION;
  }

  public void addAttribute(String attribute) {
    this.attributes.add(attribute);
  }

  public String getAttribute(String attribute) {
    for (String attr : attributes) {
      if (attr.startsWith(attribute)) { // Might be key=value string.
        return attr;
      }
    }
    return null;
  }

  public boolean hasAttribute(String attribute) {
    return getAttribute(attribute) != null;
  }

  public void removeAttribute(String attribute) {
    for (Iterator<String> iter = attributes.iterator(); iter.hasNext(); ) {
      String attr = iter.next();
      if (attr.startsWith(attribute)) { // Might be key=value string.
        iter.remove();
        break;
      }
    }
  }

  public String getGetter() {
    return getAttributeKeyValue("getter");
  }

  public String getSetter() {
    return getAttributeKeyValue("setter");
  }

  /**
   * Return the value for a specified key. Attributes are sequentially searched
   * rather than use a map, because most attributes are not key-value pairs.
   */
  private String getAttributeKeyValue(String key) {
    String prefix = key + '=';
    String attribute = getAttribute(prefix);
    return attribute != null ? attribute.substring(prefix.length()) : null;
  }

  public Set<String> getPropertyAttributes() {
    return Sets.newHashSet(attributes);
  }

  /**
   * Return a sorted comma-separated list of the property attributes for this annotation.
   */
  public static String toAttributeString(Set<String> attributes) {
    List<String> list = new ArrayList<String>(attributes);
    Collections.sort(list, ATTRIBUTES_COMPARATOR);
    return Joiner.on(", ").join(list);
  }

  @Override
  public PropertyAnnotation copy() {
    return new PropertyAnnotation(this);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public void validateInner() {
    // Validate after node is fully integrated into AST, so error() can find line info.
    if (TreeUtil.getCompilationUnit(this) != null) {
      for (String attr : this.attributes) {
        if (!attr.startsWith("getter=") && !attr.startsWith("setter=") // Accessors checked later.
            && !PROPERTY_ATTRIBUTES.contains(attr)) {
          ErrorUtil.error(this, "Invalid @Property attribute: " + attr);
        }
      }
    }
  }
}
