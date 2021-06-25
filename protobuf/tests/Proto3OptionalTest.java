import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import protos.TestProto3Optional;

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

  public void testSerializeAndParse() throws Exception {
    TestProto3Optional.Builder builder = TestProto3Optional.newBuilder();
    builder.setOptionalInt32(1234);
    builder.setOptionalString("hello");
    builder.setOptionalNestedMessage(TestProto3Optional.NestedMessage.getDefaultInstance());

    TestProto3Optional message =
        TestProto3Optional.parseFrom(
            builder.build().toByteArray(), ExtensionRegistry.newInstance());
    assertThat(message.getOptionalInt32()).isEqualTo(1234);
    assertThat(message.getOptionalString()).isEqualTo("hello");
    // Fields not set will have the default value.
    assertThat(message.getOptionalBytes()).isEqualTo(ByteString.EMPTY);
    assertThat(message.getOptionalNestedEnum()).isEqualTo(TestProto3Optional.NestedEnum.UNSPECIFIED);
    // The message field is set despite that it's set with a default instance.
    assertThat(message.hasOptionalNestedMessage()).isTrue();
    assertThat(message.getOptionalNestedMessage().getBb()).isEqualTo(0);
  }
}
