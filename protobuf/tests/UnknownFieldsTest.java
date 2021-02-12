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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import protos.AppleProto;
import protos.PearProto;

/**
 * Tests for unknown fields.
 */
public class UnknownFieldsTest extends ProtobufTest {

  public void testUnknownFieldShouldBeRetained() throws Exception {
    AppleProto apple = AppleProto.newBuilder().setType("generic").build();
    PearProto pear = PearProto.newBuilder().setType("Nashi").setName("pear").build();

    ByteArrayOutputStream serializedPear = new ByteArrayOutputStream();
    pear.writeTo(serializedPear);
    AppleProto fakeApple =
        AppleProto.parseFrom(serializedPear.toByteArray()).toBuilder().build();

    // Verify pear's type is preserved as a fake apple.
    assertTrue(pear.getType().equals(fakeApple.getType()));

    // Verify apple and fakeApple differ in both the known field and unknown field.
    assertFalse(apple.equals(fakeApple));

    // Verify not equal when only fakeApple's unknown field is different.
    assertFalse(apple.equals(fakeApple.toBuilder().setType("generic").build()));

    // Verify equality when known and unknown fields are both the same.
    AppleProto anotherFakeApple =
        AppleProto.parseFrom(serializedPear.toByteArray()).toBuilder().build();
    assertTrue(fakeApple.equals(anotherFakeApple));

    // Verify that serialized size is preserved, indicating the unknown field is.
    assertEquals(pear.getSerializedSize(), fakeApple.getSerializedSize());

    // Verify that the two byte arrays are identical.
    ByteArrayOutputStream serializedFakeApple = new ByteArrayOutputStream();
    fakeApple.writeTo(serializedFakeApple);
    assertTrue(Arrays.equals(serializedPear.toByteArray(), serializedFakeApple.toByteArray()));
  }
}
