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

import com.google.j2objc.annotations.AutoreleasePool;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MapEntry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import protos.MapMsg;
import protos.MapMsgOrBuilder;
import protos.MapValue;

/**
 * Tests for correct serialization and deserialization of map fields.
 */
public class MapsTest extends ProtobufTest {

  @AutoreleasePool
  public void testParseFromByteArray() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    MapMsg msg = MapMsg.parseFrom(ALL_MAPS_BYTES, registry);
    checkFields(msg);
  }

  public void testMergeFromInputStream() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    ByteArrayInputStream in = new ByteArrayInputStream(ALL_MAPS_BYTES);
    MapMsg.Builder builder = MapMsg.newBuilder().mergeFrom(in, registry);
    MapMsg msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testMergeFromOtherMessage() throws Exception {
    MapMsg filledMsg = getFilledMessage();
    MapMsg.Builder builder = MapMsg.newBuilder();
    builder.mergeFrom(filledMsg);
    MapMsg msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testSerialization() throws Exception {
    MapMsg msg = getFilledMessage();

    assertEquals(150, msg.getSerializedSize());
    byte[] bytes1 = msg.toByteArray();
    checkBytes(ALL_MAPS_BYTES, bytes1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes2 = out.toByteArray();
    checkBytes(ALL_MAPS_BYTES, bytes2);
  }

  public void testClearMapField() throws Exception {
    MapMsg.Builder builder = getFilledMessage().toBuilder();
    builder.clearStringString();
    assertEquals(0, builder.getStringStringCount());
    builder.clearStringInt();
    assertEquals(0, builder.getStringIntCount());
    builder.clearStringMessage();
    assertEquals(0, builder.getStringMessageCount());
    builder.clearIntString();
    assertEquals(0, builder.getIntStringCount());
    builder.clearIntInt();
    assertEquals(0, builder.getIntIntCount());
    builder.clearIntMessage();
    assertEquals(0, builder.getIntMessageCount());
  }

  public void testRemove() throws Exception {
    Descriptor descriptor = MapMsg.Builder.getDescriptor();
    FieldDescriptor stringStringField = descriptor.findFieldByNumber(1);

    MapMsg.Builder builder = getFilledMessage().toBuilder();
    builder.removeStringString("duck");
    assertEquals("default", builder.getStringStringOrDefault("duck", "default"));
    assertEquals(1, builder.getStringStringCount());
    assertEquals("meow", builder.getStringStringOrThrow("cat"));
    Object rawEntry = builder.getRepeatedField(stringStringField, 0);
    assertTrue(rawEntry instanceof MapEntry);
    MapEntry<?, ?> entry = (MapEntry<?, ?>) rawEntry;
    assertEquals("cat", entry.getKey());
    assertEquals("meow", entry.getValue());
  }

  public void testContains() throws Exception {
    MapMsg msg = getFilledMessage();
    assertTrue(msg.containsStringMessage("abc"));
    assertFalse(msg.containsStringMessage("ABC"));
    assertTrue(msg.containsIntMessage(5280));
    assertFalse(msg.containsIntMessage(5281));
  }

  public void testGetMap() throws Exception {
    MapMsg.Builder builder = getFilledMessage().toBuilder();
    Map<Integer, Integer> intIntMap = builder.getIntIntMap();
    assertEquals(49, (int) intIntMap.get(7));
    assertEquals(64, (int) intIntMap.get(8));
    builder.putIntInt(9, 81);
    assertEquals(3, intIntMap.size());
    assertEquals(81, (int) intIntMap.get(9));
    assertTrue(intIntMap.containsKey(9));
    assertFalse(intIntMap.containsKey(1));
    builder.clearIntInt();
    assertEquals(0, intIntMap.size());

    try {
      intIntMap.clear();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected.
    }
    try {
      intIntMap.put(1, 2);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected.
    }

    Map<Boolean, MapMsg.Color> boolEnumMap = builder.getBoolEnumMap();
    assertEquals(MapMsg.Color.GREEN, boolEnumMap.get(true));
  }

  public void testGetOrDefault() throws Exception {
    MapMsg msg = getFilledMessage();
    assertEquals("five", msg.getIntStringOrDefault(5, "default"));
    assertEquals("six", msg.getIntStringOrDefault(6, "default"));
    assertEquals("default", msg.getIntStringOrDefault(7, "default"));
  }

  public void testMapFieldDescriptor() throws Exception {
    Descriptor descriptor = MapMsg.Builder.getDescriptor();
    FieldDescriptor stringStringField = descriptor.findFieldByNumber(1);
    FieldDescriptor boolEnumField = descriptor.findFieldByNumber(7);
    assertEquals(Type.MESSAGE, stringStringField.getType());
    assertTrue(stringStringField.isRepeated());

    Descriptor entryDescriptor = stringStringField.getMessageType();
    assertNotNull(entryDescriptor);
    assertEquals(2, entryDescriptor.getFields().size());

    FieldDescriptor keyFieldDescriptor = entryDescriptor.findFieldByNumber(1);
    FieldDescriptor valueFieldDescriptor = entryDescriptor.findFieldByNumber(2);
    assertEquals("key", keyFieldDescriptor.getName());
    assertEquals(Type.STRING, keyFieldDescriptor.getType());
    assertEquals("value", valueFieldDescriptor.getName());
    assertEquals(Type.STRING, valueFieldDescriptor.getType());

    MapMsg msg = getFilledMessage();
    Object rawValue = msg.getField(stringStringField);
    assertTrue(rawValue instanceof List);
    List<?> list = (List<?>) rawValue;
    assertEquals(2, list.size());
    Object rawEntry = list.get(0);
    assertTrue(rawEntry instanceof MapEntry);
    MapEntry<?, ?> entry = (MapEntry<?, ?>) rawEntry;
    assertEquals("duck", entry.getKey());
    assertEquals("quack", entry.getValue());

    rawEntry = msg.getRepeatedField(stringStringField, 1);
    assertTrue(rawEntry instanceof MapEntry);
    entry = (MapEntry<?, ?>) rawEntry;
    assertEquals("cat", entry.getKey());
    assertEquals("meow", entry.getValue());

    list = (List<?>) msg.getField(boolEnumField);
    entry = (MapEntry<?, ?>) list.get(0);
    assertTrue(entry.getKey() instanceof Boolean);
    assertTrue(entry.getValue() instanceof Integer);
    assertEquals(Boolean.TRUE, entry.getKey());
    assertEquals(Integer.valueOf(0), entry.getValue());
    entry = (MapEntry<?, ?>) msg.getRepeatedField(boolEnumField, 1);
    assertTrue(entry.getKey() instanceof Boolean);
    assertTrue(entry.getValue() instanceof Integer);
    assertEquals(Boolean.FALSE, entry.getKey());
    assertEquals(Integer.valueOf(2), entry.getValue());

    MapMsg.Builder builder = msg.toBuilder();
    MapEntry<String, String> stringStringEntry =
        ((List<MapEntry<String, String>>) msg.getField(stringStringField)).get(0);
    stringStringEntry = stringStringEntry.toBuilder().setValue("neigh").build();
    builder.setRepeatedField(stringStringField, 0, stringStringEntry);
    stringStringEntry = stringStringEntry.toBuilder().setKey("cow").setValue("moo").build();
    builder.addRepeatedField(stringStringField, stringStringEntry);
    assertEquals(3, builder.getStringStringCount());
    assertEquals(3, builder.getRepeatedFieldCount(stringStringField));
    assertEquals("moo", builder.getStringStringOrThrow("cow"));
    builder.clearField(stringStringField);
    assertEquals("default", builder.getStringStringOrDefault("cow", "default"));
    assertEquals(0, builder.getStringStringCount());

    List<MapEntry<String, String>> newStringStringList = new ArrayList<>();
    newStringStringList.add(
        stringStringEntry.toBuilder().setKey("parrot").setValue("squawk").build());
    newStringStringList.add(stringStringEntry.toBuilder().setKey("pig").setValue("oink").build());
    builder.setField(stringStringField, newStringStringList);
    assertEquals("squawk", builder.getStringStringOrThrow("parrot"));
    assertEquals("oink", builder.getStringStringOrThrow("pig"));
  }

  public void testGetAllFields() throws Exception {
    Descriptor descriptor = MapMsg.Builder.getDescriptor();
    Map<FieldDescriptor, Object> allFields = getFilledMessage().getAllFields();

    Object rawField = allFields.get(descriptor.findFieldByNumber(2));
    assertTrue(rawField instanceof List);
    Object rawEntry = ((List<?>) rawField).get(0);
    assertTrue(rawEntry instanceof MapEntry);
    MapEntry<?, ?> entry = (MapEntry<?, ?>) rawEntry;
    assertEquals("triangle", entry.getKey());
    assertEquals(Integer.valueOf(3), entry.getValue());
    rawEntry = ((List<?>) rawField).get(1);
    assertTrue(rawEntry instanceof MapEntry);
    entry = (MapEntry<?, ?>) rawEntry;
    assertEquals("square", entry.getKey());
    assertEquals(Integer.valueOf(4), entry.getValue());

    rawField = allFields.get(descriptor.findFieldByNumber(7));
    assertTrue(rawField instanceof List);
    rawEntry = ((List<?>) rawField).get(0);
    assertTrue(rawEntry instanceof MapEntry);
    entry = (MapEntry<?, ?>) rawEntry;
    assertEquals(Boolean.TRUE, entry.getKey());
    assertEquals(Integer.valueOf(0), entry.getValue());
    rawEntry = ((List<?>) rawField).get(1);
    assertTrue(rawEntry instanceof MapEntry);
    entry = (MapEntry<?, ?>) rawEntry;
    assertEquals(Boolean.FALSE, entry.getKey());
    assertEquals(Integer.valueOf(2), entry.getValue());
  }

  public void testMixingMapAndListApi() throws Exception {
    Descriptor descriptor = MapMsg.Builder.getDescriptor();
    FieldDescriptor field = descriptor.findFieldByNumber(1);
    MapMsg.Builder builder = getFilledMessage().toBuilder();
    MapEntry<String, String> entry = (MapEntry<String, String>) builder.getRepeatedField(field, 0);
    entry = entry.toBuilder().setKey("cat").setValue("purr").build();
    builder.setRepeatedField(field, 0, entry);
    assertEquals(2, builder.getRepeatedFieldCount(field));
    assertEquals(1, builder.getStringStringCount());
    assertEquals("meow", builder.getStringStringOrThrow("cat"));
  }

  public void testEquals() throws Exception {
    MapMsg msg1 = getFilledMessage();
    MapMsg msg2 = getFilledMessage();
    MapMsg msg3 = getFilledMessage().toBuilder().putIntString(7, "seven").build();
    assertEquals(msg1, msg2);
    assertFalse(msg1.equals(msg3));
    assertFalse(msg3.equals(msg2));
    assertEquals(msg1.hashCode(), msg2.hashCode());
  }

  public void testToString() throws Exception {
    String result = getFilledMessage().toString();
    assertTrue(result.contains("int_int"));
    assertTrue(result.contains("key: 7"));
    assertTrue(result.contains("value: 49"));
  }

  public void testIsInitialized() throws Exception {
    MapMsg.Builder builder = MapMsg.newBuilder();
    builder.build();  // Check no exception.
    builder.putStringMessage("foo", MapValue.newBuilder().buildPartial());
    try {
      builder.build();
      fail("Expected UninitializedMessageException");
    } catch (RuntimeException e) {
      // Expected.
    }
  }

  private void checkFields(MapMsgOrBuilder msg) {
    assertEquals(2, msg.getStringStringCount());
    assertEquals("quack", msg.getStringStringOrThrow("duck"));
    assertEquals("meow", msg.getStringStringOrThrow("cat"));
    assertEquals(2, msg.getStringIntCount());
    assertEquals(3, msg.getStringIntOrThrow("triangle"));
    assertEquals(4, msg.getStringIntOrThrow("square"));
    assertEquals(2, msg.getStringMessageCount());
    assertEquals("ABC", msg.getStringMessageOrThrow("abc").getFoo());
    assertEquals("DEF", msg.getStringMessageOrThrow("def").getFoo());
    assertEquals(2, msg.getIntStringCount());
    assertEquals("five", msg.getIntStringOrThrow(5));
    assertEquals("six", msg.getIntStringOrThrow(6));
    assertEquals(2, msg.getIntIntCount());
    assertEquals(49, msg.getIntIntOrThrow(7));
    assertEquals(64, msg.getIntIntOrThrow(8));
    assertEquals(2, msg.getIntMessageCount());
    assertEquals("yard", msg.getIntMessageOrThrow(3).getFoo());
    assertEquals("mile", msg.getIntMessageOrThrow(5280).getFoo());
    assertEquals(2, msg.getBoolEnumCount());
    assertEquals(MapMsg.Color.GREEN, msg.getBoolEnumOrThrow(true));
    assertEquals(MapMsg.Color.RED, msg.getBoolEnumOrThrow(false));
  }

  private MapMsg getFilledMessage() {
    return MapMsg.newBuilder()
        .putStringString("duck", "quack")
        .putStringString("cat", "meow")
        .putStringInt("triangle", 3)
        .putStringInt("square", 4)
        .putStringMessage("abc", MapValue.newBuilder().setFoo("ABC").build())
        .putStringMessage("def", MapValue.newBuilder().setFoo("DEF").build())
        .putIntString(5, "five")
        .putIntString(6, "six")
        .putIntInt(7, 49)
        .putIntInt(8, 64)
        .putIntMessage(3, MapValue.newBuilder().setFoo("yard").build())
        .putIntMessage(5280, MapValue.newBuilder().setFoo("mile").build())
        .putBoolEnum(true, MapMsg.Color.GREEN)
        .putBoolEnum(false, MapMsg.Color.RED)
        .build();
  }

  private static final byte[] ALL_MAPS_BYTES = asBytes(new int[] {
    0x0A, 0x0D, 0x0A, 0x04, 0x64, 0x75, 0x63, 0x6B, 0x12, 0x05, 0x71, 0x75, 0x61, 0x63, 0x6B, 0x0A,
    0x0B, 0x0A, 0x03, 0x63, 0x61, 0x74, 0x12, 0x04, 0x6D, 0x65, 0x6F, 0x77, 0x12, 0x0C, 0x0A, 0x08,
    0x74, 0x72, 0x69, 0x61, 0x6E, 0x67, 0x6C, 0x65, 0x10, 0x03, 0x12, 0x0A, 0x0A, 0x06, 0x73, 0x71,
    0x75, 0x61, 0x72, 0x65, 0x10, 0x04, 0x1A, 0x0C, 0x0A, 0x03, 0x61, 0x62, 0x63, 0x12, 0x05, 0x0A,
    0x03, 0x41, 0x42, 0x43, 0x1A, 0x0C, 0x0A, 0x03, 0x64, 0x65, 0x66, 0x12, 0x05, 0x0A, 0x03, 0x44,
    0x45, 0x46, 0x22, 0x08, 0x08, 0x05, 0x12, 0x04, 0x66, 0x69, 0x76, 0x65, 0x22, 0x07, 0x08, 0x06,
    0x12, 0x03, 0x73, 0x69, 0x78, 0x2A, 0x04, 0x08, 0x07, 0x10, 0x31, 0x2A, 0x04, 0x08, 0x08, 0x10,
    0x40, 0x32, 0x0A, 0x08, 0x03, 0x12, 0x06, 0x0A, 0x04, 0x79, 0x61, 0x72, 0x64, 0x32, 0x0B, 0x08,
    0xA0, 0x29, 0x12, 0x06, 0x0A, 0x04, 0x6D, 0x69, 0x6C, 0x65, 0x3A, 0x04, 0x08, 0x01, 0x10, 0x00,
    0x3A, 0x04, 0x08, 0x00, 0x10, 0x02
  });
}
