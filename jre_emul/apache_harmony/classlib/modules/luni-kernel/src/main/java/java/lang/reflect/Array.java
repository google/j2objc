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

/*-[
#import "IOSBooleanArray.h"
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSDoubleArray.h"
#import "IOSFloatArray.h"
#import "IOSIntArray.h"
#import "IOSLongArray.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveClass.h"
#import "IOSShortArray.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/NegativeArraySizeException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Short.h"
]-*/

package java.lang.reflect;

/**
 * This class provides static methods to create and access arrays dynamically.
 */
public final class Array {

    /**
     * Prevent this class from being instantiated.
     */
    private Array(){
        //do nothing
    }

    /**
     * Returns the element of the array at the specified index. This reproduces
     * the effect of {@code array[index]}. If the array component is a primitive
     * type, the result is automatically wrapped.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element, possibly wrapped
     * @throws NullPointerException
     *             if the array is null
     * @throws IllegalArgumentException
     *             if {@code array} is not an array
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native Object get(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id result = nil;
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else {
        if ([[array class] isSubclassOfClass:[IOSObjectArray class]]) {
          IOSObjectArray *objectArray = (IOSObjectArray *) array;
          result = [objectArray objectAtIndex:index];
        } else if ([[array class] isSubclassOfClass:[IOSArray class]]) {
          // Return a wrapped instance of primitive element.
          IOSPrimitiveClass *elementType = (IOSPrimitiveClass *) [array elementType];
          NSString *binaryName = [elementType binaryName];

          // All primitive JVM binary names are single character.
          switch ([binaryName characterAtIndex:0]) {
            case 'Z':
              result = [[JavaLangBoolean alloc]
                        initWithBOOL:[self getBooleanWithId:array withInt:index]];
              break;
            case 'B':
              result = [[JavaLangByte alloc]
                        initWithChar:[self getByteWithId:array withInt:index]];
              break;
            case 'C':
              result = [[JavaLangCharacter alloc]
                        initWithUnichar:[self getCharWithId:array withInt:index]];
              break;
            case 'D':
              result = [[JavaLangDouble alloc]
                        initWithDouble:[self getDoubleWithId:array withInt:index]];
              break;
            case 'F':
              result = [[JavaLangFloat alloc]
                        initWithFloat:[self getFloatWithId:array withInt:index]];
              break;
            case 'I':
              result = [[JavaLangInteger alloc]
                        initWithInt:[self getIntWithId:array withInt:index]];
              break;
            case 'J':
              result = [[JavaLangLong alloc]
                        initWithLongInt:[self getLongWithId:array withInt:index]];
              break;
            case 'S':
              result = [[JavaLangShort alloc]
                        initWithShortInt:[self getShortWithId:array withInt:index]];
              break;
            default:
              exception = [[JavaLangAssertionError alloc] initWithNSString:@"invalid JVM type"];
              break;
          }
        } else {
          // Object isn't an array.
          exception = [[JavaLangIllegalArgumentException alloc] init];
        }
      }
      if (!result && !exception) {
        // Something's wrong above!
        exception = [[JavaLangAssertionError alloc] init];
      }

#if ! __has_feature(objc_arc)
      [result autorelease];
      [exception autorelease];
#endif

      if (exception) {
        @throw exception;
      }
      return result;
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code boolean}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native boolean getBoolean(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSBooleanArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSBooleanArray *booleanArray = (IOSBooleanArray *) array;
        if (index < 0 || index >= [booleanArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [booleanArray booleanAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return false;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code byte}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native byte getByte(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSByteArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSByteArray *byteArray = (IOSByteArray *) array;
        if (index < 0 || index >= [byteArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [byteArray byteAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code char}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native char getChar(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSCharArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSCharArray *charArray = (IOSCharArray *) array;
        if (index < 0 || index >= [charArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [charArray charAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code double}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native double getDouble(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSDoubleArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSDoubleArray *doubleArray = (IOSDoubleArray *) array;
        if (index < 0 || index >= [doubleArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [doubleArray doubleAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0.0;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code float}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native float getFloat(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSFloatArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSFloatArray *floatArray = (IOSFloatArray *) array;
        if (index < 0 || index >= [floatArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [floatArray floatAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0.0f;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to an
     * {@code int}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native int getInt(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSIntArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSIntArray *intArray = (IOSIntArray *) array;
        if (index < 0 || index >= [intArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [intArray intAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the length of the array. This reproduces the effect of {@code
     * array.length}
     *
     * @param array
     *            the array
     * @return the length of the array
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array
     */
    public static native int getLength(Object array)
        throws IllegalArgumentException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return [(IOSArray *) array count];
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code long}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native long getLong(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSLongArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSLongArray *longArray = (IOSLongArray *) array;
        if (index < 0 || index >= [longArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [longArray longAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0L;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns the element of the array at the specified index, converted to a
     * {@code short}, if possible. This reproduces the effect of {@code
     * array[index]}
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @return the requested element
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static native short getShort(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSShortArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSShortArray *shortArray = (IOSShortArray *) array;
        if (index < 0 || index >= [shortArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          return [shortArray shortAtIndex:index];
        }
      }
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
      return 0;  // Not run, but needed by compiler.
    ]-*/;

    /**
     * Returns a new multidimensional array of the specified component type and
     * dimensions. This reproduces the effect of {@code new
     * componentType[d0][d1]...[dn]} for a dimensions array of { d0, d1, ... ,
     * dn }.
     *
     * @param componentType
     *            the component type of the new array
     * @param dimensions
     *            the dimensions of the new array
     * @return the new array
     * @throws NullPointerException
     *             if the component type is {@code null}
     * @throws NegativeArraySizeException
     *             if any of the dimensions are negative
     * @throws IllegalArgumentException
     *             if the array of dimensions is of size zero, or exceeds the
     *             limit of the number of dimension for an array (currently 255)
     */
    public static Object newInstance(Class<?> componentType, int[] dimensions)
        throws NegativeArraySizeException, IllegalArgumentException {
      throw new AssertionError("multi-dimension support not implemented");
    }

    /**
     * Returns a new array of the specified component type and length. This
     * reproduces the effect of {@code new componentType[size]}.
     *
     * @param componentType
     *            the component type of the new array
     * @param size
     *            the length of the new array
     * @return the new array
     * @throws NullPointerException
     *             if the component type is null
     * @throws NegativeArraySizeException
     *             if {@code size < 0}
     */
    public static native Object newInstance(Class<?> componentType, int size)
        throws NegativeArraySizeException /*-[
      id exception = nil;
      if (!componentType) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (size < 0) {
        exception = [[JavaLangNegativeArraySizeException alloc] init];
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      NSString *type = [componentType getName];
      id result;
      if ([type isEqualToString:@"JavaLangBoolean"]) {
        result = [[IOSBooleanArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangByte"]) {
        result = [[IOSByteArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangCharacter"]) {
        result = [[IOSCharArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangDouble"]) {
        result = [[IOSDoubleArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangFloat"]) {
        result = [[IOSFloatArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangInteger"]) {
        result = [[IOSIntArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangLong"]) {
        result = [[IOSLongArray alloc] initWithLength:size];
      } else if ([type isEqualToString:@"JavaLangShort"]) {
        result = [[IOSShortArray alloc] initWithLength:size];
      } else {
        // TODO(user): If autorelease'd below, ArrayTest fails with a
        // "message sent to deallocated instance" error.
      	return [[IOSObjectArray alloc] initWithLength:size type:componentType];
      }
#if ! __has_feature(objc_arc)
      [result autorelease];
#endif
      return result;
    ]-*/;

    /**
     * Sets the element of the array at the specified index to the value. This
     * reproduces the effect of {@code array[index] = value}. If the array
     * component is a primitive type, the value is automatically unwrapped.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     *
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void set(Object array, int index, Object value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if ([[array class] isSubclassOfClass:[IOSArray class]]) {
        // It's an array, check index.
        if (index < 0 || index >= [(IOSArray *) array count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        }
      } else {
        // It's not an array.
        exception = [[JavaLangIllegalArgumentException alloc] init];
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }

      if ([[value class] isSubclassOfClass:[JavaLangBoolean class]]) {
        [self setBooleanWithId:array
                       withInt:index
                      withBOOL:[(JavaLangBoolean *) value booleanValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangByte class]]) {
        [self setByteWithId:array
                    withInt:index
                   withChar:[(JavaLangByte *) value byteValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangCharacter class]]) {
        [self setCharWithId:array
                    withInt:index
                withUnichar:[(JavaLangCharacter *) value charValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangDouble class]]) {
        [self setDoubleWithId:array
                      withInt:index
                   withDouble:[(JavaLangDouble *) value doubleValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangFloat class]]) {
        [self setFloatWithId:array
                     withInt:index
                   withFloat:[(JavaLangFloat *) value floatValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangInteger class]]) {
        [self setIntWithId:array
                   withInt:index
                   withInt:[(JavaLangInteger *) value intValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangLong class]]) {
        [self setLongWithId:array
                    withInt:index
                withLongInt:[(JavaLangLong *) value longValue]];
        return;
      }
      if ([[value class] isSubclassOfClass:[JavaLangShort class]]) {
        [self setShortWithId:array
                     withInt:index
                withShortInt:[(JavaLangShort *) value shortValue]];
        return;
      }

      // Default: it's an object array.
      IOSObjectArray *objectArray = (IOSObjectArray *) array;
      [objectArray replaceObjectAtIndex:index withObject:value];
    ]-*/;

    /**
     * Sets the element of the array at the specified index to the {@code
     * boolean} value. This reproduces the effect of {@code array[index] =
     * value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     *
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setBoolean(Object array, int index, boolean value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSBooleanArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSBooleanArray *booleanArray = (IOSBooleanArray *) array;
        if (index < 0 || index >= [booleanArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [booleanArray replaceBooleanAtIndex:index withBoolean:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Sets the element of the array at the specified index to the {@code byte}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setByte(Object array, int index, byte value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSByteArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSByteArray *byteArray = (IOSByteArray *) array;
        if (index < 0 || index >= [byteArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [byteArray replaceByteAtIndex:index withByte:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Set the element of the array at the specified index to the {@code char}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setChar(Object array, int index, char value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSCharArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSCharArray *charArray = (IOSCharArray *) array;
        if (index < 0 || index >= [charArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [charArray replaceCharAtIndex:index withChar:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Set the element of the array at the specified index to the {@code double}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setDouble(Object array, int index, double value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSDoubleArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSDoubleArray *doubleArray = (IOSDoubleArray *) array;
        if (index < 0 || index >= [doubleArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [doubleArray replaceDoubleAtIndex:index withDouble:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Set the element of the array at the specified index to the {@code float}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setFloat(Object array, int index, float value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSFloatArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSFloatArray *floatArray = (IOSFloatArray *) array;
        if (index < 0 || index >= [floatArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [floatArray replaceFloatAtIndex:index withFloat:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Set the element of the array at the specified index to the {@code int}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setInt(Object array, int index, int value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSIntArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSIntArray *intArray = (IOSIntArray *) array;
        if (index < 0 || index >= [intArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [intArray replaceIntAtIndex:index withInt:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Set the element of the array at the specified index to the {@code long}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setLong(Object array, int index, long value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSLongArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSLongArray *longArray = (IOSLongArray *) array;
        if (index < 0 || index >= [longArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [longArray replaceLongAtIndex:index withLong:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

    /**
     * Set the element of the array at the specified index to the {@code short}
     * value. This reproduces the effect of {@code array[index] = value}.
     *
     * @param array
     *            the array
     * @param index
     *            the index
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the {@code array} is {@code null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static native void setShort(Object array, int index, short value)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException /*-[
      id exception = nil;
      if (!array) {
        exception = [[JavaLangNullPointerException alloc] init];
      } else if (![[array class] isSubclassOfClass:[IOSShortArray class]]) {
        exception = [[JavaLangIllegalArgumentException alloc] init];
      } else {
        IOSShortArray *shortArray = (IOSShortArray *) array;
        if (index < 0 || index >= [shortArray count]) {
          exception = [[JavaLangArrayIndexOutOfBoundsException alloc] init];
        } else {
          [shortArray replaceShortAtIndex:index withShort:value];
        }
      }
      if (exception) {
#if ! __has_feature(objc_arc)
        [exception autorelease];
#endif
        @throw exception;
      }
      return;
    ]-*/;

}
