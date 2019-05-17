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

import com.google.protobuf.ExtensionRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import protos.GroupFe;
import protos.GroupRe;
import protos.MessageData;
import protos.MessageData.GroupF;
import protos.MessageData.GroupR;
import protos.MessageData.SubMsg;
import protos.MessageData.SubMsg.InnerMsg;
import protos.MessageDataOrBuilder;
import protos.MessageFields;
import protos.MessageSet;

/**
 * Tests for correct behavior of message and group fields.
 */
public class MessagesTest extends ProtobufTest {

  public void testParseFromByteArray() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MessageFields.registerAllExtensions(registry);
    MessageData msg = MessageData.parseFrom(ALL_MESSAGES_BYTES, registry);
    checkFields(msg);
  }

  public void testParseReversedMessageSets() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MessageFields.registerAllExtensions(registry);
    MessageData msg = MessageData.parseFrom(
        ALL_MESSAGES_WITH_MESSAGE_SETS_REVERSED_BYTES, registry);
    checkFields(msg);
  }

  public void testMergeFromInputStream() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MessageFields.registerAllExtensions(registry);
    ByteArrayInputStream in = new ByteArrayInputStream(ALL_MESSAGES_BYTES);
    MessageData.Builder builder = MessageData.newBuilder().mergeFrom(in, registry);
    MessageData msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testMergeFromOtherMessage() throws Exception {
    MessageData filledMsg = getFilledMessage();
    MessageData.Builder builder = MessageData.newBuilder();
    builder.setExtension(MessageFields.msgFe, SubMsg.newBuilder().setIntF(1).build());
    builder.mergeFrom(filledMsg);
    MessageData msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testMergeFromByteArray() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MessageFields.registerAllExtensions(registry);
    MessageData filledMsg = getFilledMessage();
    MessageData.Builder builder =
        MessageData.newBuilder().mergeFrom(filledMsg.toByteArray(), registry);
    MessageData msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testMergeFromByteString() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MessageFields.registerAllExtensions(registry);
    MessageData filledMsg = getFilledMessage();
    MessageData.Builder builder =
        MessageData.newBuilder().mergeFrom(filledMsg.toByteString(), registry);
    MessageData msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testSerialization() throws Exception {
    MessageData msg = getFilledMessage();

    assertEquals(168, msg.getSerializedSize());
    byte[] bytes1 = msg.toByteArray();
    checkBytes(ALL_MESSAGES_BYTES, bytes1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes2 = out.toByteArray();
    checkBytes(ALL_MESSAGES_BYTES, bytes2);
  }

  // TODO(kstanger): Correct field names is not supported in the wrappers.
  /*public void testGroupName() throws Exception {
    FieldDescriptor groupField = MessageData.getDescriptor().findFieldByNumber(4);
    assertEquals("groupf", groupField.getName());
  }*/

  public void testGroupSerialization() throws Exception {
    GroupF group = GroupF.newBuilder().setIntF(1).build();
    assertEquals(2, group.getSerializedSize());
    byte[] bytes = group.toByteArray();
    byte[] expected = asBytes(new int[] { 0x08, 0x01 });
    checkBytes(expected, bytes);
  }

  public void testRemoveRepeatedMessageField() throws Exception {
    MessageData data = MessageData.newBuilder()
        .addMsgR(SubMsg.newBuilder().setUintF(40).build())
        .addMsgR(SubMsg.newBuilder().setUintF(41).build())
        .addMsgR(SubMsg.newBuilder().setUintF(42).build())
        .removeMsgR(1)
        .build();
    assertEquals(2, data.getMsgRCount());
    assertEquals(40, data.getMsgR(0).getUintF());
    assertEquals(42, data.getMsgR(1).getUintF());
  }

  public void testMergeExistingMessageFields() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MessageFields.registerAllExtensions(registry);

    MessageData toMerge = MessageData.newBuilder()
        .setMsgF(SubMsg.newBuilder().setUintF(123).build())
        .setGroupF(GroupF.newBuilder().setUintF(234).build())
        .addMsgR(SubMsg.newBuilder().setUintF(345).build())
        .addGroupR(GroupR.newBuilder().setUintF(456).build())
        .setExtension(MessageFields.msgFe, SubMsg.newBuilder().setUintF(567).build())
        .setExtension(MessageFields.groupFe, GroupFe.newBuilder().setUintF(678).build())
        .addExtension(MessageFields.msgRe, SubMsg.newBuilder().setUintF(789).build())
        .addExtension(MessageFields.groupRe, GroupRe.newBuilder().setUintF(890).build())
        .build();
    byte[] toMergeBytes = toMerge.toByteArray();

    // Save the singular fields so we can verify that they aren't modified by merging.
    SubMsg field1 = SubMsg.newBuilder().setIntF(321).build();
    GroupF field2 = GroupF.newBuilder().setIntF(432).build();
    SubMsg field3 = SubMsg.newBuilder().setIntF(765).build();
    GroupFe field4 = GroupFe.newBuilder().setIntF(876).build();
    MessageData.Builder builder = MessageData.newBuilder()
        .setMsgF(field1)
        .setGroupF(field2)
        .addMsgR(SubMsg.newBuilder().setIntF(543).build())
        .addGroupR(GroupR.newBuilder().setIntF(654).build())
        .setExtension(MessageFields.msgFe, field3)
        .setExtension(MessageFields.groupFe, field4)
        .addExtension(MessageFields.msgRe, SubMsg.newBuilder().setIntF(987).build())
        .addExtension(MessageFields.groupRe, GroupRe.newBuilder().setIntF(98).build());

    MessageData.Builder builder1 = builder.build().toBuilder();
    builder1.mergeFrom(toMerge);
    checkMergedFields(builder1);

    MessageData.Builder builder2 = builder.build().toBuilder();
    builder2.mergeFrom(toMergeBytes, registry);
    // TODO(kstanger): This is a bug in the native ObjC runtime. It fails to
    // merge message type extension fields when reading from data. Instead it
    // just overwrites the existing message field.
    //checkMergedFields(builder2);

    assertFalse(field1.hasUintF());
    assertFalse(field2.hasUintF());
    assertFalse(field3.hasUintF());
    assertFalse(field4.hasUintF());
  }

  private void checkMergedFields(MessageDataOrBuilder msg) {
    assertEquals(321, msg.getMsgF().getIntF());
    assertEquals(123, msg.getMsgF().getUintF());
    assertEquals(432, msg.getGroupF().getIntF());
    assertEquals(234, msg.getGroupF().getUintF());
    assertEquals(2, msg.getMsgRCount());
    assertEquals(543, msg.getMsgR(0).getIntF());
    assertEquals(345, msg.getMsgR(1).getUintF());
    assertEquals(2, msg.getGroupRCount());
    assertEquals(654, msg.getGroupR(0).getIntF());
    assertEquals(456, msg.getGroupR(1).getUintF());
    assertEquals(765, msg.getExtension(MessageFields.msgFe).getIntF());
    assertEquals(567, msg.getExtension(MessageFields.msgFe).getUintF());
    assertEquals(876, msg.getExtension(MessageFields.groupFe).getIntF());
    assertEquals(678, msg.getExtension(MessageFields.groupFe).getUintF());
    assertEquals(2, msg.getExtensionCount(MessageFields.msgRe));
    assertEquals(987, msg.getExtension(MessageFields.msgRe, 0).getIntF());
    assertEquals(789, msg.getExtension(MessageFields.msgRe, 1).getUintF());
    assertEquals(2, msg.getExtensionCount(MessageFields.groupRe));
    assertEquals(98, msg.getExtension(MessageFields.groupRe, 0).getIntF());
    assertEquals(890, msg.getExtension(MessageFields.groupRe, 1).getUintF());
  }

  private void checkFields(MessageDataOrBuilder msg) {
    assertEquals(11, msg.getMsgF().getIntF());
    assertEquals(12, msg.getInnerMsgF().getIntF());
    assertEquals(13, msg.getRecursiveMsgF().getMsgF().getIntF());
    assertEquals(14, msg.getGroupF().getIntF());
    assertEquals(15, msg.getMsgSetF().getExtension(MessageData.msgExt).getIntF());
    assertEquals(21, msg.getMsgR(0).getIntF());
    assertEquals(31, msg.getMsgR(1).getIntF());
    assertEquals(22, msg.getInnerMsgR(0).getIntF());
    assertEquals(32, msg.getInnerMsgR(1).getIntF());
    assertEquals(23, msg.getRecursiveMsgR(0).getMsgF().getIntF());
    assertEquals(33, msg.getRecursiveMsgR(1).getMsgF().getIntF());
    assertEquals(24, msg.getGroupR(0).getIntF());
    assertEquals(34, msg.getGroupR(1).getIntF());
    assertEquals(25, msg.getMsgSetR(0).getExtension(MessageData.msgExt).getIntF());
    assertEquals(35, msg.getMsgSetR(1).getExtension(MessageData.msgExt).getIntF());
    assertEquals(41, msg.getExtension(MessageFields.msgFe).getIntF());
    assertEquals(42, msg.getExtension(MessageFields.innerMsgFe).getIntF());
    assertEquals(43, msg.getExtension(MessageFields.recursiveMsgFe).getMsgF().getIntF());
    assertEquals(44, msg.getExtension(MessageFields.groupFe).getIntF());
    assertEquals(51, msg.getExtension(MessageFields.msgRe, 0).getIntF());
    assertEquals(61, msg.getExtension(MessageFields.msgRe, 1).getIntF());
    assertEquals(52, msg.getExtension(MessageFields.innerMsgRe, 0).getIntF());
    assertEquals(62, msg.getExtension(MessageFields.innerMsgRe, 1).getIntF());
    assertEquals(53, msg.getExtension(MessageFields.recursiveMsgRe, 0).getMsgF().getIntF());
    assertEquals(63, msg.getExtension(MessageFields.recursiveMsgRe, 1).getMsgF().getIntF());
    assertEquals(54, msg.getExtension(MessageFields.groupRe, 0).getIntF());
    assertEquals(64, msg.getExtension(MessageFields.groupRe, 1).getIntF());
  }

  private MessageData getFilledMessage() {
    return MessageData.newBuilder()
        .setMsgF(SubMsg.newBuilder().setIntF(11).build())
        .setInnerMsgF(InnerMsg.newBuilder().setIntF(12).build())
        .setRecursiveMsgF(MessageData.newBuilder().setMsgF(
            SubMsg.newBuilder().setIntF(13).build()).build())
        .setGroupF(GroupF.newBuilder().setIntF(14).build())
        .setMsgSetF(MessageSet.newBuilder().setExtension(
            MessageData.msgExt, SubMsg.newBuilder().setIntF(15).build()))
        .addMsgR(SubMsg.newBuilder().setIntF(21).build())
        .addMsgR(SubMsg.newBuilder().setIntF(31).build())
        .addInnerMsgR(InnerMsg.newBuilder().setIntF(22).build())
        .addInnerMsgR(InnerMsg.newBuilder().setIntF(32).build())
        .addRecursiveMsgR(MessageData.newBuilder().setMsgF(
            SubMsg.newBuilder().setIntF(23).build()).build())
        .addRecursiveMsgR(MessageData.newBuilder().setMsgF(
            SubMsg.newBuilder().setIntF(33).build()).build())
        .addGroupR(GroupR.newBuilder().setIntF(24).build())
        .addGroupR(GroupR.newBuilder().setIntF(34).build())
        .addMsgSetR(MessageSet.newBuilder().setExtension(
            MessageData.msgExt, SubMsg.newBuilder().setIntF(25).build()))
        .addMsgSetR(MessageSet.newBuilder().setExtension(
            MessageData.msgExt, SubMsg.newBuilder().setIntF(35).build()))
        .setExtension(MessageFields.msgFe, SubMsg.newBuilder().setIntF(41).build())
        .setExtension(MessageFields.innerMsgFe, InnerMsg.newBuilder().setIntF(42).build())
        .setExtension(MessageFields.recursiveMsgFe, MessageData.newBuilder().setMsgF(
            SubMsg.newBuilder().setIntF(43).build()).build())
        .setExtension(MessageFields.groupFe, GroupFe.newBuilder().setIntF(44).build())
        .addExtension(MessageFields.msgRe, SubMsg.newBuilder().setIntF(51).build())
        .addExtension(MessageFields.msgRe, SubMsg.newBuilder().setIntF(61).build())
        .addExtension(MessageFields.innerMsgRe, InnerMsg.newBuilder().setIntF(52).build())
        .addExtension(MessageFields.innerMsgRe, InnerMsg.newBuilder().setIntF(62).build())
        .addExtension(MessageFields.recursiveMsgRe, MessageData.newBuilder().setMsgF(
            SubMsg.newBuilder().setIntF(53).build()).build())
        .addExtension(MessageFields.recursiveMsgRe, MessageData.newBuilder().setMsgF(
            SubMsg.newBuilder().setIntF(63).build()).build())
        .addExtension(MessageFields.groupRe, GroupRe.newBuilder().setIntF(54).build())
        .addExtension(MessageFields.groupRe, GroupRe.newBuilder().setIntF(64).build())
        .build();
  }

  private static final byte[] ALL_MESSAGES_BYTES = asBytes(new int[] {
    0x0A, 0x02, 0x08, 0x0B, 0x12, 0x02, 0x08, 0x0C, 0x1A, 0x04, 0x0A, 0x02, 0x08, 0x0D, 0x23, 0x08,
    0x0E, 0x24, 0x2A, 0x09, 0x0B, 0x10, 0xE8, 0x07, 0x1A, 0x02, 0x08, 0x0F, 0x0C, 0xAA, 0x01, 0x02,
    0x08, 0x15, 0xAA, 0x01, 0x02, 0x08, 0x1F, 0xB2, 0x01, 0x02, 0x08, 0x16, 0xB2, 0x01, 0x02, 0x08,
    0x20, 0xBA, 0x01, 0x04, 0x0A, 0x02, 0x08, 0x17, 0xBA, 0x01, 0x04, 0x0A, 0x02, 0x08, 0x21, 0xC3,
    0x01, 0x08, 0x18, 0xC4, 0x01, 0xC3, 0x01, 0x08, 0x22, 0xC4, 0x01, 0xCA, 0x01, 0x09, 0x0B, 0x10,
    0xE8, 0x07, 0x1A, 0x02, 0x08, 0x19, 0x0C, 0xCA, 0x01, 0x09, 0x0B, 0x10, 0xE8, 0x07, 0x1A, 0x02,
    0x08, 0x23, 0x0C, 0xCA, 0x3E, 0x02, 0x08, 0x29, 0xD2, 0x3E, 0x02, 0x08, 0x2A, 0xDA, 0x3E, 0x04,
    0x0A, 0x02, 0x08, 0x2B, 0xE3, 0x3E, 0x08, 0x2C, 0xE4, 0x3E, 0xEA, 0x3F, 0x02, 0x08, 0x33, 0xEA,
    0x3F, 0x02, 0x08, 0x3D, 0xF2, 0x3F, 0x02, 0x08, 0x34, 0xF2, 0x3F, 0x02, 0x08, 0x3E, 0xFA, 0x3F,
    0x04, 0x0A, 0x02, 0x08, 0x35, 0xFA, 0x3F, 0x04, 0x0A, 0x02, 0x08, 0x3F, 0x83, 0x40, 0x08, 0x36,
    0x84, 0x40, 0x83, 0x40, 0x08, 0x40, 0x84, 0x40
  });

  // Same as above, except that the message set fields have the type id and messages in the reverse
  // order.
  private static final byte[] ALL_MESSAGES_WITH_MESSAGE_SETS_REVERSED_BYTES = asBytes(new int[] {
    0x0A, 0x02, 0x08, 0x0B, 0x12, 0x02, 0x08, 0x0C, 0x1A, 0x04, 0x0A, 0x02, 0x08, 0x0D, 0x23, 0x08,
    0x0E, 0x24, 0x2A, 0x09, 0x0B, 0x1A, 0x02, 0x08, 0x0F, 0x10, 0xE8, 0x07, 0x0C, 0xAA, 0x01, 0x02,
    0x08, 0x15, 0xAA, 0x01, 0x02, 0x08, 0x1F, 0xB2, 0x01, 0x02, 0x08, 0x16, 0xB2, 0x01, 0x02, 0x08,
    0x20, 0xBA, 0x01, 0x04, 0x0A, 0x02, 0x08, 0x17, 0xBA, 0x01, 0x04, 0x0A, 0x02, 0x08, 0x21, 0xC3,
    0x01, 0x08, 0x18, 0xC4, 0x01, 0xC3, 0x01, 0x08, 0x22, 0xC4, 0x01, 0xCA, 0x01, 0x09, 0x0B, 0x1A,
    0x02, 0x08, 0x19, 0x10, 0xE8, 0x07, 0x0C, 0xCA, 0x01, 0x09, 0x0B, 0x1A, 0x02, 0x08, 0x23, 0x10,
    0xE8, 0x07, 0x0C, 0xCA, 0x3E, 0x02, 0x08, 0x29, 0xD2, 0x3E, 0x02, 0x08, 0x2A, 0xDA, 0x3E, 0x04,
    0x0A, 0x02, 0x08, 0x2B, 0xE3, 0x3E, 0x08, 0x2C, 0xE4, 0x3E, 0xEA, 0x3F, 0x02, 0x08, 0x33, 0xEA,
    0x3F, 0x02, 0x08, 0x3D, 0xF2, 0x3F, 0x02, 0x08, 0x34, 0xF2, 0x3F, 0x02, 0x08, 0x3E, 0xFA, 0x3F,
    0x04, 0x0A, 0x02, 0x08, 0x35, 0xFA, 0x3F, 0x04, 0x0A, 0x02, 0x08, 0x3F, 0x83, 0x40, 0x08, 0x36,
    0x84, 0x40, 0x83, 0x40, 0x08, 0x40, 0x84, 0x40
  });
}
