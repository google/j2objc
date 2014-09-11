/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

/**
 * Identifies fields in formatted strings. If a {@code FieldPosition} is passed
 * to the format method with such a parameter, then the indices will be set to
 * the start and end indices of the field in the formatted string.
 *
 * <p>A {@code FieldPosition} can be created by using the integer constants in the
 * various format classes (for example {@code NumberFormat.INTEGER_FIELD}) or
 * one of the fields of type {@code Format.Field}.
 *
 * <p>If more than one field position is needed, the method
 * {@link NumberFormat#formatToCharacterIterator(Object)} should be used.
 */
public class FieldPosition {

  private int field;
  private int beginIndex;
  private int endIndex;
  private Format.Field attribute;

  /**
   * Constructs a new {@code FieldPosition} for the given field id.
   */
  public FieldPosition(int field) {
    this.field = field;
  }

  /**
   * Constructs a new {@code FieldPosition} for the given {@code Field} attribute.
   */
  public FieldPosition(Format.Field attribute) {
    this.attribute = attribute;
    this.field = -1;
  }

  /**
   * Constructs a new {@code FieldPosition} for the given {@code Field} attribute and field id.
   */
  public FieldPosition(Format.Field attribute, int field) {
    this.attribute = attribute;
    this.field = field;
  }

  /**
   * Compares the given object to this field position and indicates if
   * they are equal. In order to be equal, {@code object} must be an instance
   * of {@code FieldPosition} with the same field, begin index and end index.
   */
  @Override public boolean equals(Object object) {
    if (!(object instanceof FieldPosition)) {
      return false;
    }
    FieldPosition pos = (FieldPosition) object;
    return field == pos.field && this.attribute == pos.attribute &&
        beginIndex == pos.beginIndex && endIndex == pos.endIndex;
  }

  /**
   * Returns the index of the beginning of the field.
   */
  public int getBeginIndex() {
    return beginIndex;
  }

  /**
   * Returns the index one past the end of the field.
   */
  public int getEndIndex() {
    return endIndex;
  }

  /**
   * Returns the field which is being identified.
   */
  public int getField() {
    return field;
  }

  /**
   * Returns the attribute which is being identified.
   */
  public Format.Field getFieldAttribute() {
    return attribute;
  }

  @Override public int hashCode() {
    int attributeHash = (attribute == null) ? 0 : attribute.hashCode();
    return attributeHash + field * 10 + beginIndex * 100 + endIndex;
  }

  /**
   * Sets the index of the beginning of the field.
   */
  public void setBeginIndex(int index) {
    beginIndex = index;
  }

  /**
   * Sets the index of the end of the field.
   */
  public void setEndIndex(int index) {
    endIndex = index;
  }

  /**
   * Returns the string representation of this field position.
   */
  @Override public String toString() {
    return getClass().getName() + "[" +
        "attribute=" + attribute +
        ",field=" + field +
        ",beginIndex=" + beginIndex +
        ",endIndex=" + endIndex +
        "]";
  }
}
