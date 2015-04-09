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

import protos.Color;
import protos.EnumFields;
import protos.EnumMsg;
import protos.EnumMsg.InnerMsg.Utensil;
import protos.EnumMsg.Shape;
import protos.EnumMsgOrBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for correct behavior of enum fields.
 */
public class EnumsTest extends ProtobufTest {

  public void testParseFromByteArray() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    EnumFields.registerAllExtensions(registry);
    EnumMsg msg = EnumMsg.parseFrom(ALL_ENUMS_BYTES, registry);
    checkFields(msg);
  }

  public void testMergeFromInputStream() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    EnumFields.registerAllExtensions(registry);
    ByteArrayInputStream in = new ByteArrayInputStream(ALL_ENUMS_BYTES);
    EnumMsg.Builder builder = EnumMsg.newBuilder().mergeFrom(in, registry);
    EnumMsg msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testMergeFromOtherMessage() throws Exception {
    EnumMsg filledMsg = getFilledMessage();
    EnumMsg.Builder builder = EnumMsg.newBuilder();
    builder.mergeFrom(filledMsg);
    EnumMsg msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  // TODO(kstanger): This fails with native ObjC because it doesn't sign-extend
  // when writing the negative enum value.
  /*public void testSerialization() throws Exception {
    EnumMsg msg = getFilledMessage();

    assertEquals(71, msg.getSerializedSize());
    byte[] bytes1 = msg.toByteArray();
    checkBytes(ALL_ENUMS_BYTES, bytes1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes2 = out.toByteArray();
    checkBytes(ALL_ENUMS_BYTES, bytes2);
  }*/

  // Tests that unknown enum values are skipped and don't cause a
  // InvalidProtocolBufferException.
  public void testBadEnumValue() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    EnumFields.registerAllExtensions(registry);
    byte[] bytes = asBytes(new int[] {
      0x08, 0x09, 0xA8, 0x01, 0x09, 0xCA, 0x02, 0x01, 0x09, 0xC8, 0x3E, 0x09, 0xE8, 0x3F, 0x09,
      0x8A, 0x41, 0x01, 0x09 });
    EnumMsg msg = EnumMsg.parseFrom(bytes, registry);
    assertFalse(msg.hasEnumF());
    assertEquals(0, msg.getEnumRCount());
    assertEquals(0, msg.getEnumPCount());
    // TODO(kstanger): Native ObjC behavior differs from Java behavior here.
    //assertFalse(msg.hasExtension(EnumFields.enumFe));
    //assertEquals(0, msg.getExtensionCount(EnumFields.enumRe));
    //assertEquals(0, msg.getExtensionCount(EnumFields.enumPe));
  }

  public void testAddAll() throws Exception {
    List<Shape> shapes = new ArrayList<Shape>();
    shapes.add(Shape.TRIANGLE);
    shapes.add(Shape.SQUARE);
    shapes.add(Shape.CIRCLE);
    EnumMsg.Builder builder = EnumMsg.newBuilder().addAllEnumR(shapes);
    assertEquals(Shape.TRIANGLE, builder.getEnumR(0));
    assertEquals(Shape.SQUARE, builder.getEnumR(1));
    assertEquals(Shape.CIRCLE, builder.getEnumR(2));
  }

  private void checkFields(EnumMsgOrBuilder msg) {
    assertEquals(Shape.SQUARE, msg.getEnumF());
    assertEquals(Color.RED, msg.getOuterEnumF());
    assertEquals(Utensil.FORK, msg.getInnerEnumF());
    assertEquals(Shape.CIRCLE, msg.getEnumR(0));
    assertEquals(Shape.TRIANGLE, msg.getEnumR(1));
    assertEquals(Color.BLUE, msg.getOuterEnumR(0));
    assertEquals(Color.YELLOW, msg.getOuterEnumR(1));
    assertEquals(Utensil.SPOON, msg.getInnerEnumR(0));
    assertEquals(Utensil.KNIFE, msg.getInnerEnumR(1));
    assertEquals(Shape.HEXAGON, msg.getEnumP(0));
    assertEquals(Shape.SQUARE, msg.getEnumP(1));
    assertEquals(Shape.HEXAGON, msg.getExtension(EnumFields.enumFe));
    assertEquals(Color.GREEN, msg.getExtension(EnumFields.outerEnumFe));
    assertEquals(Utensil.KNIFE, msg.getExtension(EnumFields.innerEnumFe));
    assertEquals(Shape.TRIANGLE, msg.getExtension(EnumFields.enumRe, 0));
    assertEquals(Shape.CIRCLE, msg.getExtension(EnumFields.enumRe, 1));
    assertEquals(Color.RED, msg.getExtension(EnumFields.outerEnumRe, 0));
    assertEquals(Color.BLUE, msg.getExtension(EnumFields.outerEnumRe, 1));
    assertEquals(Utensil.SPOON, msg.getExtension(EnumFields.innerEnumRe, 0));
    assertEquals(Utensil.FORK, msg.getExtension(EnumFields.innerEnumRe, 1));
    assertEquals(Shape.TRIANGLE, msg.getExtension(EnumFields.enumPe, 0));
    assertEquals(Shape.CIRCLE, msg.getExtension(EnumFields.enumPe, 1));
  }

  private EnumMsg getFilledMessage() {
    return EnumMsg.newBuilder()
        .setEnumF(Shape.SQUARE)
        .setOuterEnumF(Color.RED)
        .setInnerEnumF(Utensil.FORK)
        .addEnumR(Shape.CIRCLE)
        .addEnumR(Shape.TRIANGLE)
        .addOuterEnumR(Color.BLUE)
        .addOuterEnumR(Color.YELLOW)
        .addInnerEnumR(Utensil.SPOON)
        .addInnerEnumR(Utensil.KNIFE)
        .addEnumP(Shape.HEXAGON)
        .addEnumP(Shape.SQUARE)
        .setExtension(EnumFields.enumFe, Shape.HEXAGON)
        .setExtension(EnumFields.outerEnumFe, Color.GREEN)
        .setExtension(EnumFields.innerEnumFe, Utensil.KNIFE)
        .addExtension(EnumFields.enumRe, Shape.TRIANGLE)
        .addExtension(EnumFields.enumRe, Shape.CIRCLE)
        .addExtension(EnumFields.outerEnumRe, Color.RED)
        .addExtension(EnumFields.outerEnumRe, Color.BLUE)
        .addExtension(EnumFields.innerEnumRe, Utensil.SPOON)
        .addExtension(EnumFields.innerEnumRe, Utensil.FORK)
        .addExtension(EnumFields.enumPe, Shape.TRIANGLE)
        .addExtension(EnumFields.enumPe, Shape.CIRCLE)
        .build();
  }

  private static final byte[] ALL_ENUMS_BYTES = asBytes(new int[] {
    0x08, 0x02, 0x10, 0x07, 0x18, 0x02, 0xA8, 0x01, 0x01, 0xA8, 0x01, 0x03, 0xB0, 0x01, 0x03, 0xB0,
    0x01, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01, 0xB8, 0x01, 0x01, 0xB8, 0x01,
    0x03, 0xCA, 0x02, 0x02, 0x04, 0x02, 0xC8, 0x3E, 0x04, 0xD0, 0x3E, 0xD2, 0x09, 0xD8, 0x3E, 0x03,
    0xE8, 0x3F, 0x03, 0xE8, 0x3F, 0x01, 0xF0, 0x3F, 0x07, 0xF0, 0x3F, 0x03, 0xF8, 0x3F, 0x01, 0xF8,
    0x3F, 0x02, 0x8A, 0x41, 0x02, 0x03, 0x01
  });
}
