/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

/*
 * Stub implementation of java.lang.Number.
 *
 * @see java.lang.Object
 */

/**
 * The abstract class {@code Number} is the superclass of platform
 * classes representing numeric values that are convertible to the
 * primitive types {@code byte}, {@code double}, {@code float}, {@code
 * int}, {@code long}, and {@code short}.
 *
 * The specific semantics of the conversion from the numeric value of
 * a particular {@code Number} implementation to a given primitive
 * type is defined by the {@code Number} implementation in question.
 *
 * For platform classes, the conversion is often analogous to a
 * narrowing primitive conversion or a widening primitive conversion
 * as defining in <cite>The Java&trade; Language Specification</cite>
 * for converting between primitive types.  Therefore, conversions may
 * lose information about the overall magnitude of a numeric value, may
 * lose precision, and may even return a result of a different sign
 * than the input.
 *
 * See the documentation of a given {@code Number} implementation for
 * conversion details.
 *
 * @author      Lee Boynton
 * @author      Arthur van Hoff
 * @jls 5.1.2 Widening Primitive Conversions
 * @jls 5.1.3 Narrowing Primitive Conversions
 * @since   JDK1.0
 */
public abstract class Number implements java.io.Serializable {
  private static final long serialVersionUID = -8742448824652078965L;
  
  public Number() {}

  /**
   * Returns the value of the specified number as a {@code byte},
   * which may involve rounding or truncation.
   *
   * <p>This implementation returns the result of {@link #intValue} cast
   * to a {@code byte}.
   *
   * @return  the numeric value represented by this object after conversion
   *          to type {@code byte}.
   * @since   JDK1.1
   */
  public byte byteValue() {
	  return 0;
  }

  /**
   * Returns the value of the specified number as a {@code double},
   * which may involve rounding.
   *
   * @return  the numeric value represented by this object after conversion
   *          to type {@code double}.
   */
  public abstract double doubleValue();

  /**
   * Returns the value of the specified number as a {@code float},
   * which may involve rounding.
   *
   * @return  the numeric value represented by this object after conversion
   *          to type {@code float}.
   */
  public abstract float floatValue();

  /**
   * Returns the value of the specified number as an {@code int},
   * which may involve rounding or truncation.
   *
   * @return  the numeric value represented by this object after conversion
   *          to type {@code int}.
   */
  public abstract int intValue();

  /**
   * Returns the value of the specified number as a {@code long},
   * which may involve rounding or truncation.
   *
   * @return  the numeric value represented by this object after conversion
   *          to type {@code long}.
   */
  public abstract long longValue();

  /**
   * Returns the value of the specified number as a {@code short},
   * which may involve rounding or truncation.
   *
   * <p>This implementation returns the result of {@link #intValue} cast
   * to a {@code short}.
   *
   * @return  the numeric value represented by this object after conversion
   *          to type {@code short}.
   * @since   JDK1.1
   */
  public short shortValue() {
    return 0;
  }
}
