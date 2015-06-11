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

import com.google.j2objc.PrefixDummy;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.ProtocolMessageEnum;

import abc_def.gHiJkL.Foo2bar;
import abc_def.gHiJkL.Foo_bar;
import abc_def.gHiJkL.fooBar;

import protos.EmptyFile;
import protos.MsgWithDefaults;
import protos.MsgWithDefaultsOrBuilder;
import protos.MsgWithNestedExtensions;
import protos.MsgWithRequiredFields;
import protos.MsgWithSpecialFieldNames;
import protos.SingleFile;
import protos.Typical;
import protos.TypicalData;
import protos.TypicalDataMessage;
import protos.TypicalDataOrBuilder;
import protos.TypicalDataSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Tests for various protocol buffer features to ensure that the generated
 * objective-c wrappers and runtime are compatible where required.
 * This test is run in both java and objective-c.
 */
public class CompatibilityTest extends ProtobufTest {

  private static InputStream getResourceStream(String path) throws FileNotFoundException {
    InputStream result = ClassLoader.getSystemResourceAsStream(path);
    if (result == null) {
      throw new FileNotFoundException(path);
    }
    return result;
  }

  private static InputStream getTestData(String name) throws FileNotFoundException {
    String osName = System.getProperty("os.name");
    if (osName.contains("iPhone")) {
      return getResourceStream(name);
    } else {
      return new FileInputStream(new File("testdata/" + name));
    }
  }

  private static byte[] readStream(InputStream in) throws IOException {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      while (true) {
        int r = in.read(buf);
        if (r == -1) {
          break;
        }
        out.write(buf, 0, r);
      }
      return out.toByteArray();
    } finally {
      in.close();
    }
  }

  public void testSetAndGetInt() throws Exception {
    TypicalData data = TypicalData.newBuilder().setMyInt(42).build();
    assertEquals(42, data.getMyInt());
  }

  public void testSetAndGetByteString() throws Exception {
    ByteString bstr = ByteString.copyFrom("foo".getBytes());
    TypicalData data = TypicalData.newBuilder().setMyBytes(bstr).build();
    assertEquals("foo", new String(data.getMyBytes().toByteArray()));
  }

  public void testSetAndGetRepeatedInt32() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(34);
    list.add(56);
    TypicalData data = TypicalData.newBuilder()
        .addRepeatedInt32(12)
        .addAllRepeatedInt32(list)
        .setRepeatedInt32(2, 67)
        .build();
    assertEquals(3, data.getRepeatedInt32Count());
    assertEquals(12, data.getRepeatedInt32(0));
    byte[] bytes = data.toByteArray();
    TypicalData other = TypicalData.parseFrom(bytes);
    assertEquals(12, other.getRepeatedInt32(0));
    // compareTo will fail in objc if the returned type is not JavaLangInteger.
    assertEquals(0, other.getRepeatedInt32List().get(1).compareTo(34));
    assertEquals(67, other.getRepeatedInt32(2));
  }

  public void testSetAndGetRepeatedInt64() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedInt64(123).build();
    assertEquals(1, data.getRepeatedInt64Count());
    assertEquals(123, data.getRepeatedInt64(0));
  }

  public void testSetAndGetRepeatedUint32() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedUint32(123).build();
    assertEquals(1, data.getRepeatedUint32Count());
    assertEquals(123, data.getRepeatedUint32(0));
  }

  public void testSetAndGetRepeatedUint64() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedUint64(123).build();
    assertEquals(1, data.getRepeatedUint64Count());
    assertEquals(123, data.getRepeatedUint64(0));
  }

  public void testSetAndGetRepeatedbBool() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedBool(true).build();
    assertEquals(1, data.getRepeatedBoolCount());
    assertTrue(data.getRepeatedBool(0));
  }

  public void testSetAndGetRepeatedDouble() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedDouble(0.5).build();
    assertEquals(1, data.getRepeatedDoubleCount());
    assertEquals(0.5, data.getRepeatedDouble(0), 0.0001);
  }

  public void testSetAndGetRepeatedFloat() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedFloat(0.5f).build();
    assertEquals(1, data.getRepeatedFloatCount());
    assertEquals(0.5f, data.getRepeatedFloat(0), 0.0001);
  }

  public void testSetAndGetRepeatedString() throws Exception {
    TypicalData data = TypicalData.newBuilder().addRepeatedString("coin").build();
    assertEquals(1, data.getRepeatedStringCount());
    assertEquals("coin", data.getRepeatedString(0));
    List<String> list = data.getRepeatedStringList();
    assertEquals(1, list.size());
  }

  public void testSetAndGetRepeatedBytes() throws Exception {
    List<ByteString> list = new ArrayList<ByteString>();
    list.add(ByteString.copyFrom("def".getBytes()));
    list.add(ByteString.copyFrom("ghi".getBytes()));
    TypicalData data = TypicalData.newBuilder()
        .addRepeatedBytes(ByteString.copyFrom("abc".getBytes()))
        .addAllRepeatedBytes(list)
        .setRepeatedBytes(2, ByteString.copyFrom("jkl".getBytes()))
        .build();
    assertEquals(3, data.getRepeatedBytesCount());
    assertEquals("abc", new String(data.getRepeatedBytes(0).toByteArray()));
    byte[] bytes = data.toByteArray();
    TypicalData other = TypicalData.parseFrom(bytes);
    assertEquals("abc", new String(other.getRepeatedBytes(0).toByteArray()));
    assertEquals("def", new String(other.getRepeatedBytesList().get(1).toByteArray()));
    assertEquals("jkl", new String(other.getRepeatedBytes(2).toByteArray()));
  }

  public void testSetAndGetRepeatedEnum() throws Exception {
    TypicalData data =
        TypicalData.newBuilder().addRepeatedEnum(TypicalData.EnumType.VALUE1).build();
    assertEquals(1, data.getRepeatedEnumCount());
    assertEquals(TypicalData.EnumType.VALUE1, data.getRepeatedEnum(0));
    assertEquals(TypicalData.EnumType.VALUE1, data.getRepeatedEnumList().get(0));
  }

  public void testSetAndGetRepeatedTypicalData() throws Exception {
    TypicalData data = TypicalData.newBuilder().setMyInt(42).build();
    TypicalDataSet dataset = TypicalDataSet.newBuilder().addRepeatedTypicalData(data).build();
    assertEquals(1, dataset.getRepeatedTypicalDataCount());
    assertEquals(42, dataset.getRepeatedTypicalData(0).getMyInt());
  }

  public void testClear() throws Exception {
    TypicalData.Builder dataBuilder = TypicalData.newBuilder().setMyInt(22).setMyString("foo");
    assertEquals(22, dataBuilder.getMyInt());
    assertEquals("foo", dataBuilder.getMyString());
    dataBuilder.clear();
    assertFalse(dataBuilder.hasMyInt());
    assertFalse(dataBuilder.hasMyString());
  }

  public void testClearExtension() throws Exception {
    TypicalData.Builder builder = TypicalData.newBuilder();
    builder.setExtension(Typical.myPrimitiveExtension, 11);
    assertTrue(builder.hasExtension(Typical.myPrimitiveExtension));
    builder.clearExtension(Typical.myPrimitiveExtension);
    assertFalse(builder.hasExtension(Typical.myPrimitiveExtension));
  }

  public void testClearRepeatedField() throws Exception {
    TypicalData.Builder builder = TypicalData.newBuilder()
        .addRepeatedInt32(1)
        .addRepeatedInt32(2);
    assertEquals(2, builder.getRepeatedInt32Count());
    builder.clearRepeatedInt32();
    assertEquals(0, builder.getRepeatedInt32Count());
  }

  public void testProtocolMessageEnum() throws Exception {
    TypicalData data = TypicalData.newBuilder()
        .setMyEnumType(TypicalData.EnumType.VALUE1)
        .build();
    ProtocolMessageEnum type = data.getMyEnumType();
    assertEquals(1, type.getNumber());
  }

  public void testMergeFrom() throws Exception {
    TypicalData input = TypicalData.newBuilder()
        .setMyInt(42)
        .setMyMessage(TypicalDataMessage.newBuilder().setMyMessageInt(43))
        .build();
    TypicalData output = TypicalData.newBuilder()
        .mergeFrom(input.toByteString(), ExtensionRegistry.getEmptyRegistry())
        .build();
    assertEquals(42, output.getMyInt());
    assertEquals(43, output.getMyMessage().getMyMessageInt());
  }

  public void testMergeFromOtherMessage() throws Exception {
    TypicalData data = TypicalData.newBuilder().setMyInt(123).build();
    Message dataAsMsg = data;
    TypicalData.Builder builder1 = TypicalData.newBuilder().mergeFrom(dataAsMsg);
    TypicalData.Builder builder2 = TypicalData.newBuilder().mergeFrom(data);
    assertEquals(123, builder1.getMyInt());
    assertEquals(123, builder2.getMyInt());
  }

  public void testMergeAndParseFromInputStream() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    registry.add(Typical.myPrimitiveExtension);
    byte[] rawData = asBytes(new int[]{
        0x08, 0x06, 0x60, 0x01, 0x7A, 0x03, 0x62, 0x61, 0x72, 0xC8, 0x3E, 0x2D });
    checkMergeAndParse(
        TypicalData.newBuilder().mergeFrom(new ByteArrayInputStream(rawData), registry).build(),
        true);
    checkMergeAndParse(TypicalData.parseFrom(new ByteArrayInputStream(rawData), registry), true);

    // test API without ExtensionRegistry
    checkMergeAndParse(
        TypicalData.newBuilder().mergeFrom(new ByteArrayInputStream(rawData)).build(), false);
    checkMergeAndParse(TypicalData.parseFrom(new ByteArrayInputStream(rawData)), false);
  }

  public void testMergeAndParseDelimitedFromInputStream() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    registry.add(Typical.myPrimitiveExtension);
    byte[] rawData = asBytes(new int[]{
        0x0C, 0x08, 0x06, 0x60, 0x01, 0x7A, 0x03, 0x62, 0x61, 0x72, 0xC8, 0x3E, 0x2D,
        0x0C, 0x08, 0x06, 0x60, 0x01, 0x7A, 0x03, 0x62, 0x61, 0x72, 0xC8, 0x3E, 0x2D });
    ByteArrayInputStream in = new ByteArrayInputStream(rawData);
    TypicalData.Builder dataBuilder = TypicalData.newBuilder();
    assertTrue(dataBuilder.mergeDelimitedFrom(in, registry));
    checkMergeAndParse(dataBuilder.build(), true);
    // Test that the second message reads correctly.
    dataBuilder = TypicalData.newBuilder();
    assertTrue(dataBuilder.mergeDelimitedFrom(in, registry));
    checkMergeAndParse(dataBuilder.build(), true);

    // Test the parseDelimitedFrom API.
    in = new ByteArrayInputStream(rawData);
    checkMergeAndParse(TypicalData.parseDelimitedFrom(in, registry), true);
    // Test that the second message reads correctly.
    checkMergeAndParse(TypicalData.parseDelimitedFrom(in, registry), true);

    // test API without ExtensionRegistry
    dataBuilder = TypicalData.newBuilder();
    assertTrue(dataBuilder.mergeDelimitedFrom(new ByteArrayInputStream(rawData)));
    checkMergeAndParse(dataBuilder.build(), false);
    checkMergeAndParse(TypicalData.parseDelimitedFrom(new ByteArrayInputStream(rawData)), false);
  }

  private void checkMergeAndParse(TypicalData data, boolean withExtensions) {
    assertEquals(6, data.getMyInt());
    assertTrue(data.getMyBool());
    assertEquals("bar", data.getMyString());
    if (withExtensions) {
      assertEquals(45, ((Integer) data.getExtension(Typical.myPrimitiveExtension)).intValue());
    }
  }

  public void testWriteToOutputStream() throws Exception {
    TypicalData data = TypicalData.newBuilder()
        .setMyInt(7)
        .setMyBool(true)
        .setMyString("foo")
        .setExtension(Typical.myPrimitiveExtension, 45)
        .build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    data.writeTo(out);
    byte[] bytes = out.toByteArray();
    byte[] expected = new byte[]{
        0x08, 0x07, 0x60, 0x01, 0x7A, 0x03, 0x66, 0x6F, 0x6F, (byte) 0xC8, 0x3E, 0x2D };
    checkBytes(expected, bytes);
  }

  public void testWriteDelimitedToOutputStream() throws Exception {
    TypicalData data = TypicalData.newBuilder()
        .setMyInt(7).setMyBool(true).setMyString("foo").build();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    data.writeDelimitedTo(out);
    byte[] bytes = out.toByteArray();
    byte[] expected = new byte[]{ 0x09, 0x08, 0x07, 0x60, 0x01, 0x7A, 0x03, 0x66, 0x6F, 0x6F };
    checkBytes(expected, bytes);
  }

  public void testMergeFromInvalidProtocolBufferException() throws Exception {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{ 0x00 });
      TypicalData output = TypicalData.newBuilder()
          .mergeFrom(in, ExtensionRegistry.getEmptyRegistry())
          .build();
      fail("Expected InvalidProtocolBufferException to be thrown.");
    } catch (InvalidProtocolBufferException e) {
      // Expected
    }
  }

  public void testMergeDelimitedFromInvalidProtocolBufferException() throws Exception {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{ 0x03, 0x01, 0x02 });
      TypicalData.Builder builder = TypicalData.newBuilder();
      builder.mergeDelimitedFrom(in, ExtensionRegistry.getEmptyRegistry());
      builder.build();
      fail("Expected InvalidProtocolBufferException to be thrown.");
    } catch (InvalidProtocolBufferException e) {
      // Expected
    }
  }

  public void testParseFromInvalidProtocolBufferException() throws Exception {
    try {
      TypicalData output = TypicalData.parseFrom(new byte[]{ 0x08 });
      fail("Expected InvalidProtocolBufferException to be thrown.");
    } catch (InvalidProtocolBufferException e) {
      // Expected
    }
  }

  public void testParseDelimitedFromInvalidProtocolBufferException() throws Exception {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{ 0x03, 0x01, 0x02 });
      TypicalData output = TypicalData.parseDelimitedFrom(in);
      fail("Expected InvalidProtocolBufferException to be thrown.");
    } catch (InvalidProtocolBufferException e) {
      // Expected
    }
  }

  public void testParseDelimitedFromEmptyStream() throws Exception {
    TypicalData output = TypicalData.parseDelimitedFrom(new ByteArrayInputStream(new byte[0]));
    assertNull(output);
  }

  public void testFindFieldByNumber() throws Exception {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    Collection<FieldDescriptor> fields = descriptor.getFields();
    for (FieldDescriptor field : fields) {
      FieldDescriptor.Type type = field.getType();
      int fieldId = field.getNumber();
      switch (fieldId) {
        case 1:
          assertEquals(Type.INT32, type);
          break;
        case 2:
          assertEquals(Type.BYTES, type);
          break;
        case 3:
          assertEquals(Type.ENUM, type);
          break;
      }
      FieldDescriptor result = descriptor.findFieldByNumber(fieldId);
      assertEquals(field.getNumber(), result.getNumber());
      assertEquals(field.getName(), result.getName());
    }
  }

  public void testGetMessageType() throws Exception {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(11);
    Descriptor messageDescriptor = fieldDescriptor.getMessageType();
    assertNotNull(messageDescriptor);
    FieldDescriptor messageFieldDescriptor = messageDescriptor.findFieldByNumber(1);
    assertEquals(1, messageFieldDescriptor.getNumber());
  }

  public void testGetJavaType() throws Exception {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor intField = descriptor.findFieldByNumber(1);
    assertEquals(FieldDescriptor.JavaType.INT, intField.getJavaType());

    FieldDescriptor bytesField = descriptor.findFieldByNumber(2);
    assertEquals(FieldDescriptor.JavaType.BYTE_STRING, bytesField.getJavaType());

    FieldDescriptor booleanField = descriptor.findFieldByNumber(5);
    assertEquals(FieldDescriptor.JavaType.BOOLEAN, booleanField.getJavaType());

    FieldDescriptor stringField = descriptor.findFieldByNumber(8);
    assertEquals(FieldDescriptor.JavaType.STRING, stringField.getJavaType());
  }

  public void testNewBuilderForField() throws Exception {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(11);
    TypicalData.Builder dataBuilder = TypicalData.newBuilder();
    TypicalDataMessage.Builder messageBuilder = (TypicalDataMessage.Builder)
        dataBuilder.newBuilderForField(fieldDescriptor);
    TypicalDataMessage message = messageBuilder.setMyMessageInt(10).build();
    assertEquals(10, message.getMyMessageInt());

    fieldDescriptor = descriptor.findFieldByNumber(1);
    try {
      dataBuilder.newBuilderForField(fieldDescriptor);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected.
    }
  }

  public void testEnumDescriptor() throws Exception {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(3);
    assertEquals(Type.ENUM, fieldDescriptor.getType());
    EnumDescriptor enumDescriptor = fieldDescriptor.getEnumType();
    assertNotNull(enumDescriptor);

    EnumValueDescriptor enumValueDescriptor = enumDescriptor.findValueByNumber(1);
    assertEquals(1, enumValueDescriptor.getNumber());
    assertEquals("VALUE1", enumValueDescriptor.getName());
  }

  public void testExtensionRegistry() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    Typical.registerAllExtensions(registry);

    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(1);
    assertFalse(fieldDescriptor.isExtension());

    ExtensionRegistry.ExtensionInfo extensionInfo =
        registry.findExtensionByNumber(descriptor, 1000);
    assertNotNull(extensionInfo);

    FieldDescriptor extensionFieldDescriptor = extensionInfo.descriptor;
    assertNotNull(extensionFieldDescriptor);
    assertEquals(1000, extensionFieldDescriptor.getNumber());
    assertTrue(extensionFieldDescriptor.isExtension());

    Message message = extensionInfo.defaultInstance;
    assertTrue(message instanceof TypicalDataMessage);

    TypicalDataMessage data = ((TypicalDataMessage.Builder) message.toBuilder())
        .setMyMessageInt(100)
        .build();
    assertEquals(100, data.getMyMessageInt());

    // Primitive extension
    extensionInfo = registry.findExtensionByNumber(descriptor, 1001);
    assertNotNull(extensionInfo);

    extensionFieldDescriptor = extensionInfo.descriptor;
    assertNotNull(extensionFieldDescriptor);
    assertEquals(1001, extensionFieldDescriptor.getNumber());
    assertTrue(extensionFieldDescriptor.isExtension());
    assertNull(extensionInfo.defaultInstance);
  }

  public void testEnumValues() throws Exception {
    TypicalData.EnumType[] values = TypicalData.EnumType.values();
    assertEquals(5, values.length);
    assertEquals(TypicalData.EnumType.VALUE1, values[0]);
    assertEquals(TypicalData.EnumType.VALUE2, values[1]);
    assertEquals(TypicalData.EnumType.VALUE3, values[2]);
    assertEquals(TypicalData.EnumType.VALUE4, values[3]);
    assertEquals(TypicalData.EnumType.VALUE9, values[4]);
  }

  public void testEnumOrdinal() throws Exception {
    assertEquals(0, TypicalData.EnumType.VALUE1.ordinal());
    assertEquals(1, TypicalData.EnumType.VALUE2.ordinal());
    assertEquals(2, TypicalData.EnumType.VALUE3.ordinal());
    assertEquals(3, TypicalData.EnumType.VALUE4.ordinal());
    assertEquals(4, TypicalData.EnumType.VALUE9.ordinal());
  }

  public void testEnumGetNumber() throws Exception {
    assertEquals(1, TypicalData.EnumType.VALUE1.getNumber());
    assertEquals(2, TypicalData.EnumType.VALUE2.getNumber());
    assertEquals(3, TypicalData.EnumType.VALUE3.getNumber());
    assertEquals(4, TypicalData.EnumType.VALUE4.getNumber());
    assertEquals(9, TypicalData.EnumType.VALUE9.getNumber());
  }

  public void testEnumValueOf() throws Exception {
    assertEquals(TypicalData.EnumType.VALUE1, TypicalData.EnumType.valueOf(1));
    assertEquals(TypicalData.EnumType.VALUE2, TypicalData.EnumType.valueOf(2));
    assertEquals(TypicalData.EnumType.VALUE3, TypicalData.EnumType.valueOf(3));
    assertEquals(TypicalData.EnumType.VALUE4, TypicalData.EnumType.valueOf(4));
    assertEquals(TypicalData.EnumType.VALUE9, TypicalData.EnumType.valueOf(9));
  }

  public void testEnumValueOfWithString() throws Exception {
    assertEquals(TypicalData.EnumType.VALUE1, TypicalData.EnumType.valueOf("VALUE1"));
    assertEquals(TypicalData.EnumType.VALUE2, TypicalData.EnumType.valueOf("VALUE2"));
    assertEquals(TypicalData.EnumType.VALUE3, TypicalData.EnumType.valueOf("VALUE3"));
    assertEquals(TypicalData.EnumType.VALUE4, TypicalData.EnumType.valueOf("VALUE4"));
    assertEquals(TypicalData.EnumType.VALUE9, TypicalData.EnumType.valueOf("VALUE9"));
  }

  private int switchHelper(TypicalData.EnumType enumType) {
    switch (enumType) {
      case VALUE1: return 1;
      case VALUE2: return 2;
      case VALUE3: return 3;
      case VALUE4: return 4;
      case VALUE9: return 9;
    }
    return -1;
  }

  public void testEnumSwitchStatement() throws Exception {
    assertEquals(1, switchHelper(TypicalData.EnumType.VALUE1));
    assertEquals(2, switchHelper(TypicalData.EnumType.VALUE2));
    assertEquals(3, switchHelper(TypicalData.EnumType.VALUE3));
    assertEquals(4, switchHelper(TypicalData.EnumType.VALUE4));
    assertEquals(9, switchHelper(TypicalData.EnumType.VALUE9));
  }

  public void testGetFieldsCompiles() throws Exception {
    Collection<FieldDescriptor> fields = TypicalData.Builder.getDescriptor().getFields();
    assertTrue(fields.size() > 0);
    fields = TypicalData.newBuilder().build().getDescriptorForType().getFields();
    assertTrue(fields.size() > 0);
  }

  public void testMessageOrBuilderInterface() throws Exception {
    TypicalDataOrBuilder builder = TypicalData.newBuilder().setMyInt(42);
    assertTrue(builder.hasMyInt());
    assertEquals(42, builder.getMyInt());
    TypicalDataOrBuilder data = TypicalData.newBuilder().setMyInt(42).build();
    assertTrue(data.hasMyInt());
    assertEquals(42, data.getMyInt());
  }

  public void testMessageOrBuilderInterfaceSingleFile() throws Exception {
    SingleFile.Data1.InternalOrBuilder internalBuilder = SingleFile.Data1.Internal.newBuilder()
        .setIntValue(24);
    assertEquals(24, internalBuilder.getIntValue());
    SingleFile.Data1OrBuilder builder = SingleFile.Data1.newBuilder().setIntValue(42);
    assertTrue(builder.hasIntValue());
    assertEquals(42, builder.getIntValue());
    SingleFile.Data1OrBuilder data = SingleFile.Data1.newBuilder()
        .setIntValue(42)
        .addRepeatedString("foo")
        .build();
    assertTrue(data.hasIntValue());
    assertEquals(42, data.getIntValue());
    List<String> strList = data.getRepeatedStringList();
    assertEquals(1, strList.size());
    assertEquals("foo", strList.get(0));
  }

  public void testSetAndGetFieldWithFieldDescriptor() throws Exception {
    FieldDescriptor[] fields = new FieldDescriptor[19];
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    for (int i = 1; i <= 18; i++) {
      fields[i] = descriptor.findFieldByNumber(i);
    }

    TypicalData.Builder dataBuilder = TypicalData.newBuilder();
    dataBuilder.setField(fields[1], new Integer(42));
    dataBuilder.setField(fields[2], ByteString.copyFrom("foo".getBytes()));
    dataBuilder.setField(fields[3], TypicalData.EnumType.VALUE9.getValueDescriptor());
    dataBuilder.setField(fields[11], TypicalDataMessage.newBuilder().build());
    dataBuilder.setField(fields[12], Boolean.TRUE);
    dataBuilder.setField(fields[13], new Float(43.8));
    dataBuilder.setField(fields[14], new Double(44.5));
    dataBuilder.setField(fields[15], "bar");
    dataBuilder.setField(fields[16], new Integer(24));
    dataBuilder.setField(fields[17], new Long(4422));
    dataBuilder.setField(fields[18], new Long(2244));
    dataBuilder.addRepeatedField(fields[4], new Integer(72));
    dataBuilder.addRepeatedField(fields[8], "aaa");
    dataBuilder.addRepeatedField(fields[8], "bbb");
    dataBuilder.setRepeatedField(fields[8], 1, "ccc");
    ArrayList<Double> doubles = new ArrayList<Double>();
    doubles.add(1.2);
    doubles.add(3.4);
    dataBuilder.setField(fields[7], doubles);

    checkGetters(dataBuilder.build(), fields);
    checkGetters(dataBuilder, fields);
  }

  private void checkGetters(TypicalDataOrBuilder data, FieldDescriptor[] fields) {
    assertEquals(42, data.getMyInt());
    assertEquals("foo", new String(data.getMyBytes().toByteArray()));
    assertEquals(TypicalData.EnumType.VALUE9, data.getMyEnumType());
    assertTrue(data.getMyBool());
    assertEquals(new Float(43.8), data.getMyFloat());
    assertEquals(new Double(44.5), data.getMyDouble());
    assertEquals("bar", data.getMyString());
    assertEquals(24, data.getMyUint());
    assertEquals(4422, data.getMyLong());
    assertEquals(2244, data.getMyUlong());
    assertEquals(1, data.getRepeatedInt32Count());
    assertEquals(72, data.getRepeatedInt32(0));
    assertEquals("aaa", data.getRepeatedString(0));
    assertEquals("ccc", data.getRepeatedString(1));
    assertEquals(1.2, data.getRepeatedDouble(0), 0.0001);

    Object result;
    result = data.getField(fields[1]);
    assertTrue(result instanceof Integer);
    assertEquals(42, result);
    result = data.getField(fields[2]);
    assertTrue(result instanceof ByteString);
    assertEquals("foo", new String(((ByteString) result).toByteArray()));
    result = data.getField(fields[3]);
    assertTrue(result instanceof EnumValueDescriptor);
    assertEquals(9, ((EnumValueDescriptor) result).getNumber());
    result = data.getField(fields[11]);
    assertTrue(result instanceof TypicalDataMessage);
    assertEquals(TypicalDataMessage.newBuilder().build(), result);
    result = data.getField(fields[12]);
    assertTrue(result instanceof Boolean);
    assertEquals(Boolean.TRUE, result);
    result = data.getField(fields[13]);
    assertTrue(result instanceof Float);
    assertEquals(43.8, ((Float) result).floatValue(), 0.0001);
    result = data.getField(fields[14]);
    assertTrue(result instanceof Double);
    assertEquals(44.5, ((Double) result).doubleValue(), 0.0001);
    result = data.getField(fields[15]);
    assertTrue(result instanceof String);
    assertEquals("bar", result);
    result = data.getField(fields[16]);
    assertTrue(result instanceof Integer);
    assertEquals(24, result);
    result = data.getField(fields[17]);
    assertTrue(result instanceof Long);
    assertEquals(4422L, result);
    result = data.getField(fields[18]);
    assertTrue(result instanceof Long);
    assertEquals(2244L, result);
    assertEquals(1, data.getRepeatedFieldCount(fields[4]));
    result = data.getRepeatedField(fields[4], 0);
    assertTrue(result instanceof Integer);
    assertEquals(72, result);
    assertEquals(2, data.getRepeatedFieldCount(fields[8]));
    result = data.getRepeatedField(fields[8], 1);
    assertEquals("ccc", result);
    assertEquals(2, data.getRepeatedFieldCount(fields[7]));
    result = data.getRepeatedField(fields[7], 1);
    assertEquals(3.4, ((Double) result).doubleValue(), 0.0001);
  }

  public void testClearFieldWithDescriptor() throws Exception {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor intField = descriptor.findFieldByNumber(1);
    FieldDescriptor repeatedIntField = descriptor.findFieldByNumber(4);
    TypicalData.Builder dataBuilder = TypicalData.newBuilder()
        .setMyInt(42)
        .addRepeatedInt32(43)
        .addRepeatedInt32(44);

    assertEquals(42, dataBuilder.getMyInt());
    dataBuilder.clearField(intField);
    assertFalse(dataBuilder.hasMyInt());

    assertEquals(2, dataBuilder.getRepeatedInt32Count());
    dataBuilder.clearField(repeatedIntField);
    assertEquals(0, dataBuilder.getRepeatedInt32Count());
  }

  public void testGetUnsetField() throws Exception {
    TypicalData data = TypicalData.newBuilder().build();
    Descriptor descriptor = TypicalData.getDescriptor();
    assertEquals(0, data.getField(descriptor.findFieldByNumber(1)));
    Object result = data.getField(descriptor.findFieldByNumber(3));
    assertTrue(result instanceof EnumValueDescriptor);
    assertEquals(TypicalData.EnumType.VALUE1.getValueDescriptor().getNumber(),
                 ((EnumValueDescriptor) result).getNumber());
    assertTrue(data.getField(descriptor.findFieldByNumber(11)) instanceof TypicalDataMessage);
  }

  public void testFieldDescriptorMethodsThrowNullPointer() throws Exception {
    TypicalData.Builder dataBuilder = TypicalData.newBuilder();
    checkHasFieldThrowsNullPointer(dataBuilder);
    checkHasFieldThrowsNullPointer(dataBuilder.build());
    checkGetFieldThrowsNullPointer(dataBuilder);
    checkGetFieldThrowsNullPointer(dataBuilder.build());
    checkSetFieldThrowsNullPointer(dataBuilder);
  }

  public void checkHasFieldThrowsNullPointer(TypicalDataOrBuilder data) {
    try {
      data.hasField(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
  }

  public void checkGetFieldThrowsNullPointer(TypicalDataOrBuilder data) {
    try {
      data.getField(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
  }

  public void checkSetFieldThrowsNullPointer(TypicalData.Builder data) {
    try {
      data.setField(null, "foo");
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
  }

  public void testAddingNullValues() throws Exception {
    TypicalData.Builder dataBuilder = TypicalData.newBuilder();
    try {
      dataBuilder.setMyMessage((TypicalDataMessage) null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setMyMessage((TypicalDataMessage.Builder) null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setMyString(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setMyBytes(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setMyEnumType(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.addRepeatedMessage((TypicalDataMessage) null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.addRepeatedMessage((TypicalDataMessage.Builder) null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setRepeatedMessage(0, (TypicalDataMessage) null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.addRepeatedString(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setRepeatedString(0, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.addRepeatedBytes(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setRepeatedBytes(0, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.addRepeatedEnum(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setRepeatedEnum(0, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.setExtension(Typical.myExtension, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
    try {
      dataBuilder.addExtension(Typical.myRepeatedExtension, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected.
    }
  }

  public void testGetSerializedSize() throws Exception {
    GeneratedMessage data = TypicalData.newBuilder().setMyInt(1).build();
    assertEquals(2, data.getSerializedSize());
  }

  public void testGetAllFields() throws Exception {
    GeneratedMessage data = TypicalData.newBuilder()
        .setMyInt(1)
        .addRepeatedInt32(2)
        .setExtension(Typical.myExtension, TypicalDataMessage.getDefaultInstance())
        .setExtension(Typical.myPrimitiveExtension, 3)
        .build();
    Map<FieldDescriptor, Object> allFields = data.getAllFields();
    assertEquals(4, allFields.size());
    assertNotNull(allFields.get(Typical.myExtension.getDescriptor()));
    assertEquals(4, data.toBuilder().getAllFields().size());
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor intField = descriptor.findFieldByNumber(1);
    assertEquals(1, allFields.get(intField));
    assertEquals(3, allFields.get(Typical.myPrimitiveExtension.getDescriptor()));
  }

  public void testFunnyNames() throws Exception {
    Foo_bar msg1 = Foo_bar.newBuilder().build();
    Foo2bar msg2 = Foo2bar.newBuilder().build();
    fooBar msg3 = fooBar.newBuilder().build();
  }

  public void testPackagePrefix() throws Exception {
    PrefixDummy dummy = PrefixDummy.newBuilder().build();
  }

  public void testToStringOnMessage() throws Exception {
    // Using the Message type to ensure translation of toString compiles on the
    // interface type.
    Message data = TypicalData.newBuilder().setMyInt(31).build();
    String result = data.toString();
    assertTrue("Unexpected toString result: " + result,
               // Java and ObjC results are not identical.
               result.contains("my_int: 31") || result.contains("myInt: 31"));
  }

  public void testSetAndGetExtensions() throws Exception {
    TypicalDataMessage extensionMessage =
        TypicalDataMessage.newBuilder().setMyMessageInt(321).build();
    List<Integer> repeatedInts = new ArrayList<Integer>();
    repeatedInts.add(1);
    repeatedInts.add(2);
    List<TypicalDataMessage> repeatedData = new ArrayList<TypicalDataMessage>();
    repeatedData.add(TypicalDataMessage.newBuilder().setMyMessageInt(432).build());
    repeatedData.add(TypicalDataMessage.newBuilder().setMyMessageInt(543).build());
    TypicalData.Builder dataBuilder = TypicalData.newBuilder()
        .setExtension(Typical.myPrimitiveExtension, 123)
        .setExtension(Typical.myExtension, extensionMessage)
        .setExtension(Typical.myRepeatedPrimitiveExtension, repeatedInts)
        .addExtension(Typical.myRepeatedPrimitiveExtension, 3)
        .setExtension(Typical.myRepeatedExtension, repeatedData)
        .setExtension(Typical.myEnumExtension, TypicalData.EnumType.VALUE1)
        .setExtension(Typical.myBytesExtension, ByteString.copyFrom("abc".getBytes()))
        .setExtension(Typical.myBoolExtension, Boolean.TRUE)
        .setExtension(MsgWithNestedExtensions.intExt, 456);
    checkGetExtensions(dataBuilder);
    checkGetExtensions(dataBuilder.build());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    dataBuilder.build().writeTo(out);
    byte[] msgBytes = asBytes(new int[] {
      0xC2, 0x3E, 0x03, 0x08, 0xC1, 0x02, 0xC8, 0x3E, 0x7B, 0xD0, 0x3E, 0x01, 0xD0, 0x3E, 0x02,
      0xD0, 0x3E, 0x03, 0xDA, 0x3E, 0x03, 0x08, 0xB0, 0x03, 0xDA, 0x3E, 0x03, 0x08, 0x9F, 0x04,
      0xE0, 0x3E, 0x01, 0xEA, 0x3E, 0x03, 0x61, 0x62, 0x63, 0xF0, 0x3E, 0x01, 0x80, 0x7D, 0xC8,
      0x03 });

    checkBytes(msgBytes, out.toByteArray());
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    Typical.registerAllExtensions(registry);
    ByteArrayInputStream in = new ByteArrayInputStream(msgBytes);
    TypicalData data = TypicalData.newBuilder().mergeFrom(in, registry).build();
    checkGetExtensions(data);
  }

  private void checkGetExtensions(TypicalDataOrBuilder data) {
    assertEquals(123, ((Integer) data.getExtension(Typical.myPrimitiveExtension)).intValue());
    Object msg = data.getExtension(Typical.myExtension);
    assertTrue(msg instanceof TypicalDataMessage);
    assertEquals(321, ((TypicalDataMessage) msg).getMyMessageInt());
    Object result = data.getExtension(Typical.myRepeatedPrimitiveExtension);
    assertTrue(result instanceof List);
    assertTrue(((List) result).get(0) instanceof Integer);
    assertEquals(3, data.getExtensionCount(Typical.myRepeatedPrimitiveExtension));
    assertEquals(2,
        ((Integer) data.getExtension(Typical.myRepeatedPrimitiveExtension, 1)).intValue());
    assertEquals(3,
        ((Integer) data.getExtension(Typical.myRepeatedPrimitiveExtension, 2)).intValue());
    assertEquals(2, data.getExtensionCount(Typical.myRepeatedExtension));
    result = data.getExtension(Typical.myRepeatedExtension, 1);
    assertTrue(result instanceof TypicalDataMessage);
    assertEquals(543, ((TypicalDataMessage) result).getMyMessageInt());
    assertEquals(TypicalData.EnumType.VALUE1, data.getExtension(Typical.myEnumExtension));
    result = data.getExtension(Typical.myBytesExtension);
    assertTrue(result instanceof ByteString);
    assertEquals("abc", new String(((ByteString) result).toByteArray()));
    result = data.getExtension(Typical.myBoolExtension);
    assertEquals(Boolean.TRUE, result);
    assertEquals(456, ((Integer) data.getExtension(MsgWithNestedExtensions.intExt)).intValue());
  }

  public void testEmptyFieldOptions() {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    FieldDescriptor intField = descriptor.findFieldByNumber(1);
    assertNotNull(intField.getOptions());
  }

  public void testMessageGetClass() {
    Message msg = TypicalData.newBuilder().build();
    assertEquals(TypicalData.class, msg.getClass());
  }

  public void testToBuilder() throws Exception {
    TypicalData data = TypicalData.newBuilder()
        .setMyInt(42)
        .setMyMessage(TypicalDataMessage.newBuilder().setMyMessageInt(43))
        .build();
    TypicalData.Builder builder = data.toBuilder();
    TypicalDataMessage message = builder.getMyMessage();
    assertNotNull(message);
    assertEquals(43, message.getMyMessageInt());
  }

  public void testWriteDelimitedToLargeProto() throws Exception {
    TypicalData.Builder builder1 = TypicalData.newBuilder();
    for (int i = 0; i < 1000; i++) {
      builder1.addRepeatedInt32(i);
      builder1.addRepeatedUint32(i);
      builder1.addRepeatedInt64(i);
      builder1.addRepeatedUint64(i);
    }
    TypicalData.Builder builder2 = TypicalData.newBuilder();
    for (int i = 1000; i < 2000; i++) {
      builder2.addRepeatedInt32(i);
      builder2.addRepeatedUint32(i);
      builder2.addRepeatedInt64(i);
      builder2.addRepeatedUint64(i);
    }
    TypicalData.Builder builder3 = TypicalData.newBuilder();
    for (int i = 2000; i < 3000; i++) {
      builder3.addRepeatedInt32(i);
      builder3.addRepeatedUint32(i);
      builder3.addRepeatedInt64(i);
      builder3.addRepeatedUint64(i);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    builder1.build().writeDelimitedTo(out);
    builder2.build().writeDelimitedTo(out);
    builder3.build().writeDelimitedTo(out);
    byte[] bytes = out.toByteArray();
    byte[] expected = readStream(getTestData("largeproto"));
    checkBytes(expected, bytes);
  }

  public void testMergeFromLargeProto() throws Exception {
    TypicalData.Builder builder1 = TypicalData.newBuilder();
    TypicalData.Builder builder2 = TypicalData.newBuilder();
    TypicalData.Builder builder3 = TypicalData.newBuilder();
    InputStream in = getTestData("largeproto");
    try {
      assertTrue(builder1.mergeDelimitedFrom(in));
      assertTrue(builder2.mergeDelimitedFrom(in));
      assertTrue(builder3.mergeDelimitedFrom(in));
    } finally {
      in.close();
    }
    TypicalData data1 = builder1.build();
    TypicalData data2 = builder2.build();
    TypicalData data3 = builder3.build();
    assertEquals(0, data1.getRepeatedInt32(0));
    assertEquals(999, data1.getRepeatedInt32(999));
    assertEquals(1000, data2.getRepeatedInt32(0));
    assertEquals(1999, data2.getRepeatedInt32(999));
    assertEquals(2000, data3.getRepeatedInt32(0));
    assertEquals(2999, data3.getRepeatedInt32(999));
  }

  public void testDefaultInstance() throws Exception {
    checkDefaults(MsgWithDefaults.getDefaultInstance());
    checkDefaults(MsgWithDefaults.newBuilder());
    checkDefaults(MsgWithDefaults.newBuilder().getDefaultInstanceForType());
    checkDefaults(MsgWithDefaults.newBuilder().build().getDefaultInstanceForType());
    checkDefaults(MsgWithDefaults.newBuilder().build().newBuilderForType());
    checkDefaults((MsgWithDefaults) Typical.myExtensionWithDefaults.getMessageDefaultInstance());
  }

  private void checkDefaults(MsgWithDefaultsOrBuilder data) {
    assertEquals(13, data.getMyInt32());
    assertTrue(data.getMyBool());
    assertEquals("foo", data.getMyString());
    assertEquals(TypicalData.EnumType.VALUE4, data.getMyEnum());
  }

  public void testMessageLite() throws Exception {
    // Mainly a compilation test for the Lite classes.
    MessageLite.Builder builder = TypicalData.newBuilder();
    MessageLite message = builder.build();
    assertTrue(message instanceof MessageLite);
  }

  public void testMutatingBuilderDoesntMutateMessage() throws Exception {
    TypicalData.Builder builder = TypicalData.newBuilder();
    TypicalData data = builder.build();
    builder.setMyInt(23);
    assertEquals(0, data.getMyInt());

    builder = data.toBuilder();
    builder.setMyInt(45);
    assertEquals(0, data.getMyInt());
  }

  public void testBuildUninitializedMessage() throws Exception {
    MsgWithRequiredFields.Builder builder = MsgWithRequiredFields.newBuilder();
    try {
      builder.build();
      fail("Expected UninitializedMessageException");
    } catch (RuntimeException e) {
      // Expected.
    }
    MsgWithRequiredFields uninitialized = builder.buildPartial(); // Shouldn't fail.
    assertEquals(0, uninitialized.getRequiredInt32());
  }

  public void testSpecialFieldNames() throws Exception {
    MsgWithSpecialFieldNames data = MsgWithSpecialFieldNames.newBuilder()
        .setId(123)
        .setAndEq(4.56)
        .setZone("test")
        .addSelf(777)
        .addSelf(888)
        .build();
    assertEquals(123, data.getId());
    assertEquals(4.56, data.getAndEq(), 0.0001);
    assertEquals("test", data.getZone());
    assertEquals(2, data.getSelfCount());
    assertEquals(777, data.getSelf(0));
    assertEquals(888, data.getSelf(1));
  }

  public void testEmptyFile() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    // Should be a noop, test that this compiles.
    EmptyFile.registerAllExtensions(registry);
  }

  public void testSettingMessageFieldsWithBuilders() throws Exception {
    TypicalDataMessage.Builder msgBuilder = TypicalDataMessage.newBuilder().setMyMessageInt(1);
    TypicalData.Builder builder = TypicalData.newBuilder()
        .setMyMessage(msgBuilder)
        .addRepeatedMessage(msgBuilder);
    msgBuilder.setMyMessageInt(2);
    assertEquals(1, builder.getMyMessage().getMyMessageInt());
    assertEquals(1, builder.getRepeatedMessage(0).getMyMessageInt());
  }

  public void testIsEqualAndHashCode() throws Exception {
    TypicalDataMessage subMsg1 = TypicalDataMessage.newBuilder().setMyMessageInt(11).build();
    TypicalData.Builder builder1 = TypicalData.newBuilder()
        .setMyMessage(subMsg1)
        .setMyInt(22);
    TypicalData data1 = builder1.build();
    TypicalDataMessage subMsg2 = TypicalDataMessage.newBuilder().setMyMessageInt(11).build();
    TypicalData.Builder builder2 = TypicalData.newBuilder()
        .setMyMessage(subMsg2)
        .setMyInt(22);
    TypicalData data2 = builder2.build();
    TypicalDataMessage subMsg3 = TypicalDataMessage.newBuilder().setMyMessageInt(33).build();
    TypicalData.Builder builder3 = TypicalData.newBuilder()
        .setMyMessage(subMsg3)
        .setMyInt(22);
    TypicalData data3 = builder3.build();
    // Builders are not equal.
    assertFalse(builder1.equals(builder2));
    assertFalse(builder2.equals(builder1));
    assertTrue(data1.equals(data2));
    assertTrue(data2.equals(data1));
    assertEquals(data1.hashCode(), data2.hashCode());
    assertFalse(data1.equals(data3));
    assertFalse(data3.equals(data1));
  }

  public void testNewBuilderWithPrototype() throws Exception {
    TypicalData data = TypicalData.newBuilder().setMyInt(123).build();
    TypicalData.Builder builder = TypicalData.newBuilder(data);
    assertEquals(123, builder.getMyInt());
  }

  public void testEnumValueConstants() throws Exception {
    assertEquals(TypicalData.EnumType.VALUE1_VALUE, 1);
    assertEquals(TypicalData.EnumType.VALUE2_VALUE, 2);
    assertEquals(TypicalData.EnumType.VALUE3_VALUE, 3);
    assertEquals(TypicalData.EnumType.VALUE4_VALUE, 4);
    assertEquals(TypicalData.EnumType.VALUE9_VALUE, 9);
  }

  public void testMessageLiteInterface() throws Exception {
    ExtensionRegistryLite registry = ExtensionRegistryLite.newInstance();
    TypicalData data = TypicalData.newBuilder().build();
    MessageLite messageLite = data;
    MessageLite.Builder builderLite = messageLite.newBuilderForType();
    messageLite.writeTo(new ByteArrayOutputStream());
    messageLite.writeDelimitedTo(new ByteArrayOutputStream());
    builderLite.mergeFrom(new ByteArrayInputStream(new byte[0]));
    builderLite.mergeFrom(new ByteArrayInputStream(new byte[0]), registry);
    builderLite.mergeDelimitedFrom(new ByteArrayInputStream(new byte[0]));
    builderLite.mergeDelimitedFrom(new ByteArrayInputStream(new byte[0]), registry);
    assertEquals(0, messageLite.getSerializedSize());
  }

  /**
   * The returned list must be able to see edits to the internal list. However
   * when the internal list is cleared the returned list maintains a view of the
   * elements as they where before the clear.
   */
  public void testGetRepeatedList() throws Exception {
    TypicalData.Builder builder = TypicalData.newBuilder()
        .addRepeatedInt32(1)
        .addRepeatedInt32(2)
        .addRepeatedInt32(3);
    List<Integer> list = builder.getRepeatedInt32List();
    assertEquals(3, list.size());
    builder.setRepeatedInt32(1, 4);
    assertEquals(4, list.get(1).intValue());
    builder.clearRepeatedInt32();
    assertEquals(3, list.size());
  }

  public void testGetByteArray() throws Exception {
    // Make sure it compiles with the MessageLite type.
    MessageLite data = TypicalData.newBuilder().setMyInt(42).build();
    byte[] bytes = data.toByteArray();
    byte[] expected = new byte[]{ 0x08, 0x2A };
    checkBytes(expected, bytes);
  }

  public void testExtensionRegistryGetUnmodifiable() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    ExtensionRegistry registry2 = registry.getUnmodifiable();
    registry.add(Typical.myPrimitiveExtension);
    // Extension added to registry should be visible in registry2.
    Descriptor descriptor = TypicalData.getDescriptor();
    ExtensionRegistry.ExtensionInfo extensionInfo =
        registry2.findExtensionByNumber(descriptor, 1001);
    assertNotNull(extensionInfo);

    ExtensionRegistryLite registryLite = ExtensionRegistryLite.newInstance();
    ExtensionRegistryLite registryLite2 = registryLite.getUnmodifiable();
    assertNotNull(registryLite2);
  }

  public void testMessageLiteToBuilderAndMergeFrom() throws Exception {
    TypicalData input = TypicalData.newBuilder().setMyInt(123).build();
    MessageLite msg = TypicalData.getDefaultInstance();

    // mergeFrom(byte[], ExtensionRegistryLite)
    MessageLite.Builder builder = msg.toBuilder();
    builder.mergeFrom(input.toByteString().toByteArray(), ExtensionRegistry.getEmptyRegistry());
    assertEquals(123, ((TypicalData) builder.build()).getMyInt());

    // mergeFrom(byte[])
    builder = msg.toBuilder();
    builder.mergeFrom(input.toByteString().toByteArray());
    assertEquals(123, ((TypicalData) builder.build()).getMyInt());

    // mergeFrom(ByteString, ExtensionRegistryLite)
    builder = msg.toBuilder();
    builder.mergeFrom(input.toByteString(), ExtensionRegistry.getEmptyRegistry());
    assertEquals(123, ((TypicalData) builder.build()).getMyInt());

    // mergeFrom(ByteString)
    builder = msg.toBuilder();
    builder.mergeFrom(input.toByteString());
    assertEquals(123, ((TypicalData) builder.build()).getMyInt());
  }
}
