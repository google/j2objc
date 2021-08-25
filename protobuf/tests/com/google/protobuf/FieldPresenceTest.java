// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.google.protobuf;

import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.FieldPresenceTestProto.TestAllTypes;
import com.google.protobuf.FieldPresenceTestProto.TestOptionalFieldsOnly;
import com.google.protobuf.FieldPresenceTestProto.TestRepeatedFieldsOnly;
import com.google.protobuf.testing.proto.TestProto3Optional;
import junit.framework.TestCase;

/**
 * Unit tests for protos that doesn't support field presence test for optional non-message fields.
 */
public class FieldPresenceTest extends TestCase {

  public void testHasMethodForProto3Optional() throws Exception {
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalInt32());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalInt64());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalUint32());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalUint64());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalSint32());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalSint64());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalFixed32());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalFixed64());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalFloat());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalDouble());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalBool());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalString());
    assertFalse(TestProto3Optional.getDefaultInstance().hasOptionalBytes());

    TestProto3Optional.Builder builder = TestProto3Optional.newBuilder().setOptionalInt32(0);
    assertTrue(builder.hasOptionalInt32());
    assertTrue(builder.build().hasOptionalInt32());

    TestProto3Optional.Builder otherBuilder = TestProto3Optional.newBuilder().setOptionalInt32(1);
    otherBuilder.mergeFrom(builder.build());
    assertTrue(otherBuilder.hasOptionalInt32());
    assertEquals(0, otherBuilder.getOptionalInt32());

    // TODO(tball): b/197406391 optional enum setter method not found in generated proto.
    // TestProto3Optional.Builder builder3 =
    //     TestProto3Optional.newBuilder().setOptionalNestedEnumValue(5);
    // assertTrue(builder3.hasOptionalNestedEnum());

    TestProto3Optional.Builder builder4 =
        TestProto3Optional.newBuilder().setOptionalNestedEnum(TestProto3Optional.NestedEnum.FOO);
    assertTrue(builder4.hasOptionalNestedEnum());

    TestProto3Optional proto = TestProto3Optional.parseFrom(builder.build().toByteArray());
    assertTrue(proto.hasOptionalInt32());
    assertTrue(proto.toBuilder().hasOptionalInt32());
  }

  public void testOneofEquals() throws Exception {
    TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    TestAllTypes message1 = builder.build();
    // Set message2's oneof_uint32 field to default value. The two
    // messages should be different when check with oneof case.
    builder.setOneofUint32(0);
    TestAllTypes message2 = builder.build();
    assertFalse(message1.equals(message2));
  }

  public void testLazyField() throws Exception {
    // Test default constructed message.
    TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    TestAllTypes message = builder.build();
    assertFalse(message.hasOptionalLazyMessage());
    assertEquals(0, message.getSerializedSize());
    assertEquals(ByteString.EMPTY, message.toByteString());

    // Set default instance to the field.
    builder.setOptionalLazyMessage(TestAllTypes.NestedMessage.getDefaultInstance());
    message = builder.build();
    assertTrue(message.hasOptionalLazyMessage());
    assertEquals(2, message.getSerializedSize());

    // Test parse zero-length from wire sets the presence.
    // TODO(tball): b/195482347 generate parseFrom(ByteString) methods.
    // TestAllTypes parsed = TestAllTypes.parseFrom(message.toByteString());
    TestAllTypes parsed = TestAllTypes.parseFrom(message.toByteArray());
    assertTrue(parsed.hasOptionalLazyMessage());
    assertEquals(message.getOptionalLazyMessage(), parsed.getOptionalLazyMessage());
  }

  // TODO(tball): b/197405269 setSerializedSize() is wrong for message with optional fields.
//   public void testFieldPresence() {
//     // Optional non-message fields set to their default value are treated the
//     // same way as not set.

//     // Serialization will ignore such fields.
//     TestAllTypes.Builder builder = TestAllTypes.newBuilder();
//     builder.setOptionalInt32(0);
//     builder.setOptionalString("");
//     builder.setOptionalBytes(ByteString.EMPTY);
//     builder.setOptionalNestedEnum(TestAllTypes.NestedEnum.FOO);
//     TestAllTypes message = builder.build();
//     assertEquals(0, message.getSerializedSize());

//     // mergeFrom() will ignore such fields.
//     TestAllTypes.Builder a = TestAllTypes.newBuilder();
//     a.setOptionalInt32(1);
//     a.setOptionalString("x");
//     a.setOptionalBytes(ByteString.copyFromUtf8("y"));
//     a.setOptionalNestedEnum(TestAllTypes.NestedEnum.BAR);
//     TestAllTypes.Builder b = TestAllTypes.newBuilder();
//     b.setOptionalInt32(0);
//     b.setOptionalString("");
//     b.setOptionalBytes(ByteString.EMPTY);
//     b.setOptionalNestedEnum(TestAllTypes.NestedEnum.FOO);
//     a.mergeFrom(b.build());
//     message = a.build();
//     assertEquals(1, message.getOptionalInt32());
//     assertEquals("x", message.getOptionalString());
//     assertEquals(ByteString.copyFromUtf8("y"), message.getOptionalBytes());
//     assertEquals(TestAllTypes.NestedEnum.BAR, message.getOptionalNestedEnum());

//     // equals()/hashCode() should produce the same results.
//     TestAllTypes empty = TestAllTypes.getDefaultInstance();
//     message = builder.build();
//     assertEquals(message, empty);
//     assertEquals(empty, message);
//     assertEquals(empty.hashCode(), message.hashCode());
//   }

  public void testMessageField() {
    TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    assertThat(builder.hasOptionalNestedMessage()).isFalse();
    assertThat(builder.build().hasOptionalNestedMessage()).isFalse();

    TestAllTypes.NestedMessage.Builder nestedBuilder = TestAllTypes.NestedMessage.newBuilder();
    builder.setOptionalNestedMessage(nestedBuilder.build());
    assertThat(builder.hasOptionalNestedMessage()).isTrue();
    assertThat(builder.build().hasOptionalNestedMessage()).isTrue();

    nestedBuilder.setValue(1);
    builder.setOptionalNestedMessage(nestedBuilder.build());
    assertThat(builder.build().getOptionalNestedMessage().getValue()).isEqualTo(1);

    builder.clearOptionalNestedMessage();
    assertThat(builder.hasOptionalNestedMessage()).isFalse();
    assertThat(builder.build().hasOptionalNestedMessage()).isFalse();

    // Unlike non-message fields, if we set a message field to its default value (i.e.,
    // default instance), the field should be seen as present.
    builder.setOptionalNestedMessage(TestAllTypes.NestedMessage.getDefaultInstance());
    assertThat(builder.hasOptionalNestedMessage()).isTrue();
    assertThat(builder.build().hasOptionalNestedMessage()).isTrue();
  }

  public void testSerializeAndParse() throws Exception {
    TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    builder.setOptionalInt32(1234);
    builder.setOptionalString("hello");
    builder.setOptionalNestedMessage(TestAllTypes.NestedMessage.getDefaultInstance());
    // Set an oneof field to its default value and expect it to be serialized (i.e.,
    // an oneof field set to the default value should be treated as present).
    builder.setOneofInt32(0);

    // TODO(tball): b/195482347 generate parseFrom(ByteString) methods.
    // ByteString data = builder.build().toByteString();
    byte[] data = builder.build().toByteArray();

    TestAllTypes message = TestAllTypes.parseFrom(data);
    assertEquals(1234, message.getOptionalInt32());
    assertEquals("hello", message.getOptionalString());
    // Fields not set will have the default value.
    assertEquals(ByteString.EMPTY, message.getOptionalBytes());
    assertEquals(TestAllTypes.NestedEnum.FOO, message.getOptionalNestedEnum());
    // The message field is set despite that it's set with a default instance.
    assertTrue(message.hasOptionalNestedMessage());
    assertEquals(0, message.getOptionalNestedMessage().getValue());
    // The oneof field set to its default value is also present.
    assertEquals(TestAllTypes.OneofFieldCase.ONEOF_INT32, message.getOneofFieldCase());
  }

  // Regression test for b/16173397
  // Make sure we haven't screwed up the code generation for repeated fields.
  public void testRepeatedFields() throws Exception {
    TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    builder.setOptionalInt32(1234);
    builder.setOptionalString("hello");
    builder.setOptionalNestedMessage(TestAllTypes.NestedMessage.getDefaultInstance());
    builder.addRepeatedInt32(4321);
    builder.addRepeatedString("world");
    builder.addRepeatedNestedMessage(TestAllTypes.NestedMessage.getDefaultInstance());

    // TODO(tball): b/195482347 generate parseFrom(ByteString) methods.
    // ByteString data = builder.build().toByteString();
    byte[] data = builder.build().toByteArray();

    TestOptionalFieldsOnly optionalOnlyMessage = TestOptionalFieldsOnly.parseFrom(data);
    assertEquals(1234, optionalOnlyMessage.getOptionalInt32());
    assertEquals("hello", optionalOnlyMessage.getOptionalString());
    assertTrue(optionalOnlyMessage.hasOptionalNestedMessage());
    assertEquals(0, optionalOnlyMessage.getOptionalNestedMessage().getValue());

    TestRepeatedFieldsOnly repeatedOnlyMessage = TestRepeatedFieldsOnly.parseFrom(data);
    assertEquals(1, repeatedOnlyMessage.getRepeatedInt32Count());
    assertEquals(4321, repeatedOnlyMessage.getRepeatedInt32(0));
    assertEquals(1, repeatedOnlyMessage.getRepeatedStringCount());
    assertEquals("world", repeatedOnlyMessage.getRepeatedString(0));
    assertEquals(1, repeatedOnlyMessage.getRepeatedNestedMessageCount());
    assertEquals(0, repeatedOnlyMessage.getRepeatedNestedMessage(0).getValue());
  }

  // TODO(tball): b/197410978 builder.isInitialized() isn't implemented.
//   public void testIsInitialized() throws Exception {
//     TestAllTypes.Builder builder = TestAllTypes.newBuilder();

//     // Test optional proto2 message fields.
//     UnittestProto.TestRequired.Builder proto2Builder = builder.getOptionalProto2MessageBuilder();
//     assertFalse(builder.isInitialized());
//     assertFalse(builder.buildPartial().isInitialized());

//     proto2Builder.setA(1).setB(2).setC(3);
//     assertTrue(builder.isInitialized());
//     assertTrue(builder.buildPartial().isInitialized());

//     // Test oneof proto2 message fields.
//     proto2Builder = builder.getOneofProto2MessageBuilder();
//     assertFalse(builder.isInitialized());
//     assertFalse(builder.buildPartial().isInitialized());

//     proto2Builder.setA(1).setB(2).setC(3);
//     assertTrue(builder.isInitialized());
//     assertTrue(builder.buildPartial().isInitialized());

//     // Test repeated proto2 message fields.
//     proto2Builder = builder.addRepeatedProto2MessageBuilder();
//     assertFalse(builder.isInitialized());
//     assertFalse(builder.buildPartial().isInitialized());

//     proto2Builder.setA(1).setB(2).setC(3);
//     assertTrue(builder.isInitialized());
//     assertTrue(builder.buildPartial().isInitialized());
//   }

}
