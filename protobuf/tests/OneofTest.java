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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.ExtensionRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import protos.OneofFoo;
import protos.OneofMsg;
import protos.OneofMsg.OneofGroupCase;
import protos.OneofMsgOrBuilder;

/**
 * Tests for correct serialization and deserialization of oneof fields.
 */
public class OneofTest extends ProtobufTest {

  public void testParseFromByteArray() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    OneofMsg msg = OneofMsg.parseFrom(ONEOF_STRING_BYTES, registry);
    checkFields(msg, OneofGroupCase.ONEOF_STRING);
    msg = OneofMsg.parseFrom(ONEOF_INT_BYTES, registry);
    checkFields(msg, OneofGroupCase.ONEOF_INT);
    msg = OneofMsg.parseFrom(ONEOF_MESSAGE_BYTES, registry);
    checkFields(msg, OneofGroupCase.ONEOF_MESSAGE);
  }

  public void testMergeFromInputStream(OneofGroupCase groupCase) throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    ByteArrayInputStream in = new ByteArrayInputStream(getBytes(groupCase));
    OneofMsg.Builder builder = OneofMsg.newBuilder().mergeFrom(in, registry);
    checkFields(builder, groupCase);
    checkFields(builder.build(), groupCase);
  }

  public void testMergeFromInputStream() throws Exception {
    testMergeFromInputStream(OneofGroupCase.ONEOF_STRING);
    testMergeFromInputStream(OneofGroupCase.ONEOF_INT);
    testMergeFromInputStream(OneofGroupCase.ONEOF_MESSAGE);
  }

  public void testMergeFromOtherMessage() throws Exception {
    OneofMsg filledMsg = getFilledMessage(OneofGroupCase.ONEOF_STRING);
    OneofMsg.Builder builder = OneofMsg.newBuilder();
    builder.mergeFrom(filledMsg);
    checkFields(builder, OneofGroupCase.ONEOF_STRING);
    checkFields(builder.build(), OneofGroupCase.ONEOF_STRING);
  }

  public void testSerialization(OneofGroupCase groupCase) throws Exception {
    OneofMsg msg = getFilledMessage(groupCase);
    byte[] expectedBytes = getBytes(groupCase);

    assertEquals(expectedBytes.length, msg.getSerializedSize());
    byte[] bytes1 = msg.toByteArray();
    checkBytes(expectedBytes, bytes1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes2 = out.toByteArray();
    checkBytes(expectedBytes, bytes2);
  }

  public void testSerialization() throws Exception {
    testSerialization(OneofGroupCase.ONEOF_STRING);
    testSerialization(OneofGroupCase.ONEOF_INT);
    testSerialization(OneofGroupCase.ONEOF_MESSAGE);
  }

  public void testReflection() throws Exception {
    Descriptor descriptor = OneofMsg.getDescriptor();
    FieldDescriptor stringField = descriptor.findFieldByNumber(6);
    FieldDescriptor intField = descriptor.findFieldByNumber(4);
    FieldDescriptor messageField = descriptor.findFieldByNumber(1);

    OneofMsg msg = getFilledMessage(OneofGroupCase.ONEOF_STRING);
    assertEquals("goodbye", msg.getField(stringField));
    assertFalse(msg.hasField(intField));
    assertFalse(msg.hasField(messageField));

    OneofMsg.Builder builder = msg.toBuilder();
    builder.setField(messageField, OneofFoo.newBuilder().setFoo("baz").build());
    assertEquals("baz", builder.getOneofMessage().getFoo());
    assertFalse(builder.hasOneofString());
    assertFalse(builder.hasOneofInt());
  }

  public void testOneofDescriptor() throws Exception {
    Descriptor descriptor = OneofMsg.getDescriptor();
    List<OneofDescriptor> oneofs = descriptor.getOneofs();
    assertEquals(1, oneofs.size());
    OneofDescriptor oneof = oneofs.get(0);
    assertEquals("oneof_group", oneof.getName());
    assertEquals(descriptor, oneof.getContainingType());
  }

  public void testFieldOrder() throws Exception {
    Descriptor descriptor = OneofMsg.getDescriptor();
    List<FieldDescriptor> fields = descriptor.getFields();
    assertEquals(3, fields.get(0).getNumber());
    assertEquals(2, fields.get(1).getNumber());
    assertEquals(5, fields.get(2).getNumber());
    assertEquals(6, fields.get(3).getNumber());
    assertEquals(4, fields.get(4).getNumber());
    assertEquals(1, fields.get(5).getNumber());

    OneofDescriptor oneof = descriptor.getOneofs().get(0);
    List<FieldDescriptor> oneofFields = oneof.getFields();
    assertEquals(6, oneofFields.get(0).getNumber());
    assertEquals(4, oneofFields.get(1).getNumber());
    assertEquals(1, oneofFields.get(2).getNumber());

    assertEquals(oneof, fields.get(3).getContainingOneof());
    assertEquals(oneof, fields.get(4).getContainingOneof());
    assertEquals(oneof, fields.get(5).getContainingOneof());
  }

  public void testCaseForNumber() throws Exception {
    assertEquals(OneofGroupCase.ONEOF_STRING, OneofGroupCase.forNumber(6));
    assertEquals(OneofGroupCase.ONEOF_INT, OneofGroupCase.forNumber(4));
    assertEquals(OneofGroupCase.ONEOF_MESSAGE, OneofGroupCase.forNumber(1));
    assertEquals(OneofGroupCase.ONEOFGROUP_NOT_SET, OneofGroupCase.forNumber(0));
    assertNull(OneofGroupCase.forNumber(100));
    assertNull(OneofGroupCase.forNumber(-1));
  }

  private void checkFields(OneofMsgOrBuilder msg, OneofGroupCase fieldToCheck) {
    assertEquals("hello", msg.getRegularString());
    assertEquals(42, msg.getRegularInt());
    assertEquals("foo", msg.getRegularMessage().getFoo());
    assertEquals(fieldToCheck, msg.getOneofGroupCase());
    switch (fieldToCheck) {
      case ONEOF_STRING:
        assertEquals("goodbye", msg.getOneofString());
        break;
      case ONEOF_INT:
        assertEquals(24, msg.getOneofInt());
        break;
      case ONEOF_MESSAGE:
        assertEquals("bar", msg.getOneofMessage().getFoo());
        break;
    }
    if (!fieldToCheck.equals(OneofGroupCase.ONEOF_STRING)) {
      assertFalse(msg.hasOneofString());
    }
    if (!fieldToCheck.equals(OneofGroupCase.ONEOF_INT)) {
      assertFalse(msg.hasOneofInt());
    }
    if (!fieldToCheck.equals(OneofGroupCase.ONEOF_MESSAGE)) {
      assertFalse(msg.hasOneofMessage());
    }
  }

  private OneofMsg getFilledMessage(OneofGroupCase fieldToSet) {
    OneofMsg.Builder builder = OneofMsg.newBuilder()
        .setRegularString("hello")
        .setRegularInt(42)
        .setRegularMessage(OneofFoo.newBuilder().setFoo("foo").build());
    switch (fieldToSet) {
      case ONEOF_STRING:
        builder.setOneofString("goodbye");
        break;
      case ONEOF_INT:
        builder.setOneofInt(24);
        break;
      case ONEOF_MESSAGE:
        builder.setOneofMessage(OneofFoo.newBuilder().setFoo("bar").build());
        break;
      default:
        // Add nothing.
    }
    return builder.build();
  }

  private static byte[] getBytes(OneofGroupCase groupCase) {
    switch (groupCase) {
      case ONEOF_STRING: return ONEOF_STRING_BYTES;
      case ONEOF_INT: return ONEOF_INT_BYTES;
      case ONEOF_MESSAGE: return ONEOF_MESSAGE_BYTES;
      default: return null;
    }
  }

  private static final byte[] ONEOF_STRING_BYTES = asBytes(new int[] {
    0x10, 0x2A, 0x1A, 0x05, 0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x2A, 0x05, 0x0A, 0x03, 0x66, 0x6F, 0x6F,
    0x32, 0x07, 0x67, 0x6F, 0x6F, 0x64, 0x62, 0x79, 0x65
  });

  private static final byte[] ONEOF_INT_BYTES = asBytes(new int[] {
    0x10, 0x2A, 0x1A, 0x05, 0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x18, 0x2A, 0x05, 0x0A, 0x03, 0x66,
    0x6F, 0x6F
  });

  private static final byte[] ONEOF_MESSAGE_BYTES = asBytes(new int[] {
    0x0A, 0x05, 0x0A, 0x03, 0x62, 0x61, 0x72, 0x10, 0x2A, 0x1A, 0x05, 0x68, 0x65, 0x6C, 0x6C, 0x6F,
    0x2A, 0x05, 0x0A, 0x03, 0x66, 0x6F, 0x6F
  });
}
