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

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;

import protos.Typical;
import protos.TypicalData;
import protos.TypicalDataMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the performance of protocol buffers.
 *
 * @author Keith Stanger
 */
class PerformanceBenchmarks {

  public static void main(String[] args) throws Exception {
    System.out.println("Running performance tests...");
    Method[] methods = PerformanceBenchmarks.class.getDeclaredMethods();
    for (Method method : methods) {
      if (method.getName().startsWith("test")) {
        long startTime = System.currentTimeMillis();
        method.invoke(null);
        long endTime = System.currentTimeMillis();
        System.out.println(method.getName() + ": " + (endTime - startTime));
      }
    }
  }

  private static void testSetPrimitiveFields() {
    for (int i = 0; i < 100000; i++) {
      setAllPrimitiveFields(TypicalData.newBuilder());
    }
  }

  private static void testGetPrimitiveFields() {
    TypicalData.Builder builder = TypicalData.newBuilder();
    setAllPrimitiveFields(builder);
    TypicalData data = builder.build();
    for (int i = 0; i < 100000; i++) {
      data.getMyInt();
      data.getMyBool();
      data.getMyFloat();
      data.getMyDouble();
      data.getMyUint();
      data.getMyLong();
      data.getMyUlong();
    }
  }

  private static void testGetPrimitiveFieldsWithDescriptors() {
    TypicalData.Builder builder = TypicalData.newBuilder();
    setAllPrimitiveFields(builder);
    TypicalData data = builder.build();
    List<FieldDescriptor> fields = getPrimitiveFieldDescriptors();
    for (int i = 0; i < 10000; i++) {
      for (FieldDescriptor field : fields) {
        data.getField(field);
      }
    }
  }

  private static void testSetPrimitiveFieldsWithDescriptors() {
    List<FieldDescriptor> fields = getPrimitiveFieldDescriptors();
    List<Object> values = new ArrayList<Object>();
    values.add(Integer.valueOf(1));
    values.add(Boolean.TRUE);
    values.add(Float.valueOf(2.3f));
    values.add(Double.valueOf(4.5));
    values.add(Integer.valueOf(6));
    values.add(Long.valueOf(7));
    values.add(Long.valueOf(8));
    for (int i = 0; i < 5000; i++) {
      TypicalData.Builder builder = TypicalData.newBuilder();
      for (int j = 0; j < 7; j++) {
        builder.setField(fields.get(j), values.get(j));
      }
    }
  }

  private static void testSetRepeatedFields() {
    for (int i = 0; i < 1000; i++) {
      setAllRepeatedFields(TypicalData.newBuilder(), 25);
    }
  }

  private static void testGetRepeatedFields() {
    TypicalData.Builder builder = TypicalData.newBuilder();
    setAllRepeatedFields(builder, 25);
    TypicalData data = builder.build();
    for (int i = 0; i < 3000; i++) {
      for (int j = 0; j < 25; j++) {
        data.getRepeatedInt32(j);
        data.getRepeatedInt64(j);
        data.getRepeatedUint32(j);
        data.getRepeatedUint64(j);
        data.getRepeatedBool(j);
        data.getRepeatedFloat(j);
        data.getRepeatedDouble(j);
        data.getRepeatedString(j);
        data.getRepeatedBytes(j);
        data.getRepeatedEnum(j);
      }
    }
  }

  private static void testGetRepeatedFieldList() {
    TypicalData.Builder builder = TypicalData.newBuilder();
    setAllRepeatedFields(builder, 25);
    TypicalData data = builder.build();
    for (int i = 0; i < 2000; i++) {
      data.getRepeatedInt32List();
      data.getRepeatedInt64List();
      data.getRepeatedUint32List();
      data.getRepeatedUint64List();
      data.getRepeatedBoolList();
      data.getRepeatedFloatList();
      data.getRepeatedDoubleList();
      data.getRepeatedStringList();
      data.getRepeatedBytesList();
      data.getRepeatedEnumList();
    }
  }

  private static void testClearRepeatedFields() {
    TypicalData.Builder builder = TypicalData.newBuilder();
    for (int i = 0; i < 1000; i++) {
      setAllRepeatedFields(builder, 25);
      builder.clearRepeatedInt32();
      builder.clearRepeatedInt64();
      builder.clearRepeatedUint32();
      builder.clearRepeatedUint64();
      builder.clearRepeatedBool();
      builder.clearRepeatedFloat();
      builder.clearRepeatedDouble();
      builder.clearRepeatedString();
      builder.clearRepeatedBytes();
      builder.clearRepeatedEnum();
    }
  }

  private static void testGetRepeatedFieldsWithDescriptors() {
    TypicalData.Builder builder = TypicalData.newBuilder();
    setAllRepeatedFields(builder, 25);
    TypicalData data = builder.build();
    List<FieldDescriptor> fields = getRepeatedFieldDescriptors();
    for (int i = 0; i < 50; i++) {
      for (int j = 0; j < 25; j++) {
        for (FieldDescriptor field : fields) {
          data.getRepeatedField(field, j);
        }
      }
    }
  }

  private static void testAddRepeatedFieldsWithDescriptors() {
    List<FieldDescriptor> fields = getRepeatedFieldDescriptors();
    List<Object> values = new ArrayList<Object>();
    values.add(Integer.valueOf(1));
    values.add(Long.valueOf(2));
    values.add(Integer.valueOf(3));
    values.add(Long.valueOf(4));
    values.add(Boolean.TRUE);
    values.add(Float.valueOf(5.6f));
    values.add(Double.valueOf(7.8));
    values.add("foo");
    values.add(ByteString.copyFrom("bar".getBytes()));
    values.add(TypicalData.EnumType.VALUE1.getValueDescriptor());
    for (int i = 0; i < 150; i++) {
      TypicalData.Builder builder = TypicalData.newBuilder();
      for (int j = 0; j < 25; j++) {
        for (int k = 0; k < 10; k++) {
          builder.addRepeatedField(fields.get(k), values.get(k));
        }
      }
    }
  }

  private static void testSetPrimitiveExtension() {
    for (int i = 0; i < 3000; i++) {
      TypicalData.Builder builder = TypicalData.newBuilder();
      for (int j = 0; j < 25; j++) {
        builder.setExtension(Typical.myPrimitiveExtension, j);
      }
    }
  }

  private static void testGetPrimitiveExtension() {
    TypicalData data = TypicalData.newBuilder()
        .setExtension(Typical.myPrimitiveExtension, 3).build();
    for (int i = 0; i < 100000; i++) {
      data.getExtension(Typical.myPrimitiveExtension);
    }
  }

  private static void testWriteTo() throws Exception {
    TypicalData.Builder builder = TypicalData.newBuilder();
    setAllPrimitiveFields(builder);
    setAllRepeatedFields(builder, 5);
    setMessageField(builder);
    TypicalData data = builder.build();
    for (int i = 0; i < 10000; i++) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      data.writeTo(out);
    }
  }

  private static void testMergeFrom() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(PROTO_DATA);
    ExtensionRegistry registry =  ExtensionRegistry.getEmptyRegistry();
    for (int i = 0; i < 30000; i++) {
      TypicalData.newBuilder().mergeFrom(in, registry);
    }
  }

  private static void setAllPrimitiveFields(TypicalData.Builder builder) {
    builder.setMyInt(1);
    builder.setMyBool(true);
    builder.setMyFloat(2.3f);
    builder.setMyDouble(4.5);
    builder.setMyUint(6);
    builder.setMyLong(7);
    builder.setMyUlong(8);
  }

  private static void setAllRepeatedFields(TypicalData.Builder builder, int times) {
    TypicalData.EnumType[] enumValues = TypicalData.EnumType.values();
    int numValues = enumValues.length;
    for (int i = 0; i < times; i++) {
      builder.addRepeatedInt32(i);
      builder.addRepeatedInt64(i);
      builder.addRepeatedUint32(i);
      builder.addRepeatedUint64(i);
      builder.addRepeatedBool(i % 2 == 1);
      builder.addRepeatedFloat(i);
      builder.addRepeatedDouble(i);
      builder.addRepeatedString("test" + i);
      builder.addRepeatedBytes(ByteString.copyFrom(("abc" + i).getBytes()));
      builder.addRepeatedEnum(enumValues[i % numValues]);
    }
  }

  private static void setMessageField(TypicalData.Builder builder) {
    builder.setMyMessage(TypicalDataMessage.newBuilder().setMyMessageInt(42).build());
  }

  private static List<FieldDescriptor> getPrimitiveFieldDescriptors() {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
    fields.add(descriptor.findFieldByNumber(1));
    fields.add(descriptor.findFieldByNumber(12));
    fields.add(descriptor.findFieldByNumber(13));
    fields.add(descriptor.findFieldByNumber(14));
    fields.add(descriptor.findFieldByNumber(16));
    fields.add(descriptor.findFieldByNumber(17));
    fields.add(descriptor.findFieldByNumber(18));
    return fields;
  }

  private static List<FieldDescriptor> getRepeatedFieldDescriptors() {
    Descriptor descriptor = TypicalData.Builder.getDescriptor();
    List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
    fields.add(descriptor.findFieldByNumber(4));
    fields.add(descriptor.findFieldByNumber(19));
    fields.add(descriptor.findFieldByNumber(20));
    fields.add(descriptor.findFieldByNumber(21));
    fields.add(descriptor.findFieldByNumber(5));
    fields.add(descriptor.findFieldByNumber(6));
    fields.add(descriptor.findFieldByNumber(7));
    fields.add(descriptor.findFieldByNumber(8));
    fields.add(descriptor.findFieldByNumber(9));
    fields.add(descriptor.findFieldByNumber(10));
    return fields;
  }

  // So we don't have to add byte casts within hard-coded arrays.
  private static byte[] asBytes(int[] ints) {
    byte[] bytes = new byte[ints.length];
    for (int i = 0; i < ints.length; i++) {
      bytes[i] = (byte) ints[i];
    }
    return bytes;
  }

  private static final byte[] PROTO_DATA = asBytes(new int[] {
    0x08, 0x01, 0x20, 0x00, 0x20, 0x01, 0x20, 0x02, 0x20, 0x03, 0x20, 0x04, 0x28, 0x00, 0x28, 0x01,
    0x28, 0x00, 0x28, 0x01, 0x28, 0x00, 0x35, 0x00, 0x00, 0x00, 0x00, 0x35, 0x00, 0x00, 0x80, 0x3F,
    0x35, 0x00, 0x00, 0x00, 0x40, 0x35, 0x00, 0x00, 0x40, 0x40, 0x35, 0x00, 0x00, 0x80, 0x40, 0x39,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x39, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0,
    0x3F, 0x39, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x39, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x08, 0x40, 0x39, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x40, 0x42, 0x05, 0x74, 0x65,
    0x73, 0x74, 0x30, 0x42, 0x05, 0x74, 0x65, 0x73, 0x74, 0x31, 0x42, 0x05, 0x74, 0x65, 0x73, 0x74,
    0x32, 0x42, 0x05, 0x74, 0x65, 0x73, 0x74, 0x33, 0x42, 0x05, 0x74, 0x65, 0x73, 0x74, 0x34, 0x4A,
    0x04, 0x61, 0x62, 0x63, 0x30, 0x4A, 0x04, 0x61, 0x62, 0x63, 0x31, 0x4A, 0x04, 0x61, 0x62, 0x63,
    0x32, 0x4A, 0x04, 0x61, 0x62, 0x63, 0x33, 0x4A, 0x04, 0x61, 0x62, 0x63, 0x34, 0x50, 0x01, 0x50,
    0x02, 0x50, 0x03, 0x50, 0x04, 0x50, 0x09, 0x5A, 0x02, 0x08, 0x2A, 0x60, 0x01, 0x6D, 0x33, 0x33,
    0x13, 0x40, 0x71, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x12, 0x40, 0x80, 0x01, 0x06, 0x88, 0x01,
    0x07, 0x90, 0x01, 0x08, 0x98, 0x01, 0x00, 0x98, 0x01, 0x01, 0x98, 0x01, 0x02, 0x98, 0x01, 0x03,
    0x98, 0x01, 0x04, 0xA0, 0x01, 0x00, 0xA0, 0x01, 0x01, 0xA0, 0x01, 0x02, 0xA0, 0x01, 0x03, 0xA0,
    0x01, 0x04, 0xA8, 0x01, 0x00, 0xA8, 0x01, 0x01, 0xA8, 0x01, 0x02, 0xA8, 0x01, 0x03, 0xA8, 0x01,
    0x04 });
}
