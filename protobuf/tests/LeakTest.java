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

import junit.framework.TestCase;

/** Unit tests to ensure no memory leaks in repeated fields. */

/*-[
#import "com/google/protobuf/RepeatedField.h"
]-*/

public class LeakTest extends TestCase {

  public void testArrayLeak() {
    assertTrue(assertRepeatedFieldArrayDoesNotLeakMemory());
  }

  private native boolean assertRepeatedFieldArrayDoesNotLeakMemory() /*-[
    CGPRepeatedField field = { NULL };
    CGPRepeatedFieldReserve(&field, 4, sizeof(jint));

    // Drains the autoreleased Java iterator created during fast enumeration inside
    // CGPNewRepeatedFieldArray, which retains field.data until the pool drains.
    @autoreleasepool {
      __unused NSArray *array = CGPNewRepeatedFieldArray(&field, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT);
    }

    BOOL isNotLeaking = (field.data->ref_count == 1);

    CGPRepeatedFieldClear(&field, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT);
    return isNotLeaking;
  ]-*/;
}
