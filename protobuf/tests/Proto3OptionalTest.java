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

import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.testing.proto.TestProto3Optional;

/** Tests for proto3 optional feature. */
public class Proto3OptionalTest extends ProtobufTest {

  public void testHasMethodForProto3Optional() throws Exception {
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalInt32()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalInt64()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalUint32()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalUint64()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalSint32()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalSint64()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalFixed32()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalFixed64()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalFloat()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalDouble()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalBool()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalString()).isFalse();
    assertThat(TestProto3Optional.getDefaultInstance().hasOptionalBytes()).isFalse();

    TestProto3Optional.Builder builder = TestProto3Optional.newBuilder().setOptionalInt32(0);
    assertThat(builder.hasOptionalInt32()).isTrue();
    assertThat(builder.build().hasOptionalInt32()).isTrue();

    TestProto3Optional.Builder otherBuilder = TestProto3Optional.newBuilder().setOptionalInt32(1);
    otherBuilder.mergeFrom(builder.build());
    assertThat(otherBuilder.hasOptionalInt32()).isTrue();
    assertThat(otherBuilder.getOptionalInt32()).isEqualTo(0);

    assertThat(builder.build().toByteArray()).isEqualTo(new byte[] {0x8, 0x0});

    // TODO(tball): b/197406391 optional enum setter method not found in generated proto.
    // TestProto3Optional.Builder builder3 =
    //     TestProto3Optional.newBuilder().setOptionalNestedEnumValue(5);
    // assertThat(builder3.hasOptionalNestedEnum()).isTrue();

    TestProto3Optional.Builder builder4 =
        TestProto3Optional.newBuilder().setOptionalNestedEnum(TestProto3Optional.NestedEnum.FOO);
    assertThat(builder4.hasOptionalNestedEnum()).isTrue();

    TestProto3Optional proto =
        TestProto3Optional.parseFrom(
            builder.build().toByteArray(), ExtensionRegistry.newInstance());
    assertThat(proto.hasOptionalInt32()).isTrue();
    assertThat(proto.toBuilder().hasOptionalInt32()).isTrue();
  }

  public void testEquals() throws Exception {
    TestProto3Optional.Builder builder = TestProto3Optional.newBuilder();
    TestProto3Optional message1 = builder.build();
    // Set message2's optional string field to default value. The two
    // messages should be different.
    builder.setOptionalString("");
    TestProto3Optional message2 = builder.build();
    assertThat(message1.equals(message2)).isFalse();
  }
}
