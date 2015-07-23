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
import com.google.protobuf.ExtensionRegistry;

import protos.StringMsg;
import protos.StringMsgOrBuilder;
import protos.StringFields;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Tests for correct serialization and deserialization of string fields.
 */
public class StringsTest extends ProtobufTest {

  public void testParseFromByteArray() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    StringFields.registerAllExtensions(registry);
    StringMsg msg = StringMsg.parseFrom(ALL_STRINGS_BYTES, registry);
    checkFields(msg);
  }

  public void testMergeFromInputStream() throws Exception {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    StringFields.registerAllExtensions(registry);
    ByteArrayInputStream in = new ByteArrayInputStream(ALL_STRINGS_BYTES);
    StringMsg.Builder builder = StringMsg.newBuilder().mergeFrom(in, registry);
    StringMsg msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testMergeFromOtherMessage() throws Exception {
    StringMsg filledMsg = getFilledMessage();
    StringMsg.Builder builder = StringMsg.newBuilder();
    builder.mergeFrom(filledMsg);
    StringMsg msg = builder.build();
    checkFields(builder);
    checkFields(msg);
  }

  public void testSerialization() throws Exception {
    StringMsg msg = getFilledMessage();

    assertEquals(171, msg.getSerializedSize());
    byte[] bytes1 = msg.toByteArray();
    checkBytes(ALL_STRINGS_BYTES, bytes1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes2 = out.toByteArray();
    checkBytes(ALL_STRINGS_BYTES, bytes2);
  }

  public void testReallyLongString() throws Exception {
    char[] chars = new char[10000];
    Arrays.fill(chars, 'a');
    String str = new String(chars);
    StringMsg msg = StringMsg.newBuilder()
        .setAsciiF(str)
        .build();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes = out.toByteArray();
    assertEquals(10003, bytes.length);
    for (int i = 3; i < 10003; i++) {
      assertEquals('a', bytes[i]);
    }

    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    StringMsg msg2 = StringMsg.parseFrom(in);
    assertEquals(str, msg2.getAsciiF());
  }

  public void testReallyLongNonAsciiString() throws Exception {
    char[] chars = new char[10000];
    Arrays.fill(chars, '\ufdfd');
    String str = new String(chars);
    StringMsg msg = StringMsg.newBuilder()
        .setNonAsciiF(str)
        .build();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    byte[] bytes = out.toByteArray();
    assertEquals(30004, bytes.length);
    for (int i = 4; i < 30004; ) {
      assertEquals((byte) 0xef, bytes[i++]);
      assertEquals((byte) 0xb7, bytes[i++]);
      assertEquals((byte) 0xbd, bytes[i++]);
    }

    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    StringMsg msg2 = StringMsg.parseFrom(in);
    assertEquals(str, msg2.getNonAsciiF());
  }

  public void testDefaultValue() throws Exception {
    StringMsg msg = StringMsg.newBuilder().build();
    assertEquals("", msg.getEmptyF());
    assertEquals("abc", msg.getDefaultStringF());
    assertEquals("", new String(msg.getBytesF().toByteArray()));
    assertEquals("def", new String(msg.getDefaultBytesF().toByteArray()));
    assertEquals("", msg.getExtension(StringFields.emptyFe));
    assertEquals("ghi", msg.getExtension(StringFields.defaultStringFe));
    assertEquals("", new String(msg.getExtension(StringFields.bytesFe).toByteArray()));
    assertEquals("jkl", new String(msg.getExtension(StringFields.defaultBytesFe).toByteArray()));
  }

  private void checkFields(StringMsgOrBuilder msg) {
    assertEquals("", msg.getEmptyF());
    assertEquals("foo", msg.getAsciiF());
    assertEquals("你好", msg.getNonAsciiF());
    assertEquals("dog", new String(msg.getBytesF().toByteArray()));
    assertEquals("", msg.getEmptyR(0));
    assertEquals("", msg.getEmptyR(1));
    assertEquals("bar", msg.getAsciiR(0));
    assertEquals("baz", msg.getAsciiR(1));
    assertEquals("مرحبا", msg.getNonAsciiR(0));
    assertEquals("привет", msg.getNonAsciiR(1));
    assertEquals("cat", new String(msg.getBytesR(0).toByteArray()));
    assertEquals("rat", new String(msg.getBytesR(1).toByteArray()));
    assertEquals("", msg.getExtension(StringFields.emptyFe));
    assertEquals("abc", msg.getExtension(StringFields.asciiFe));
    assertEquals("γειά σου", msg.getExtension(StringFields.nonAsciiFe));
    assertEquals("pig", new String(msg.getExtension(StringFields.bytesFe).toByteArray()));
    assertEquals("", msg.getExtension(StringFields.emptyRe, 0));
    assertEquals("", msg.getExtension(StringFields.emptyRe, 1));
    assertEquals("def", msg.getExtension(StringFields.asciiRe, 0));
    assertEquals("ghi", msg.getExtension(StringFields.asciiRe, 1));
    assertEquals("नमस्ते", msg.getExtension(StringFields.nonAsciiRe, 0));
    assertEquals("halló", msg.getExtension(StringFields.nonAsciiRe, 1));
    assertEquals("cow", new String(msg.getExtension(StringFields.bytesRe, 0).toByteArray()));
    assertEquals("moo", new String(msg.getExtension(StringFields.bytesRe, 1).toByteArray()));
  }

  private StringMsg getFilledMessage() {
    return StringMsg.newBuilder()
        .setEmptyF("")
        .setAsciiF("foo")
        .setNonAsciiF("你好")
        .setBytesF(ByteString.copyFrom("dog".getBytes()))
        .addEmptyR("")
        .addEmptyR("")
        .addAsciiR("bar")
        .addAsciiR("baz")
        .addNonAsciiR("مرحبا")
        .addNonAsciiR("привет")
        .addBytesR(ByteString.copyFrom("cat".getBytes()))
        .addBytesR(ByteString.copyFrom("rat".getBytes()))
        .setExtension(StringFields.emptyFe, "")
        .setExtension(StringFields.asciiFe, "abc")
        .setExtension(StringFields.nonAsciiFe, "γειά σου")
        .setExtension(StringFields.bytesFe, ByteString.copyFrom("pig".getBytes()))
        .addExtension(StringFields.emptyRe, "")
        .addExtension(StringFields.emptyRe, "")
        .addExtension(StringFields.asciiRe, "def")
        .addExtension(StringFields.asciiRe, "ghi")
        .addExtension(StringFields.nonAsciiRe, "नमस्ते")
        .addExtension(StringFields.nonAsciiRe, "halló")
        .addExtension(StringFields.bytesRe, ByteString.copyFrom("cow".getBytes()))
        .addExtension(StringFields.bytesRe, ByteString.copyFrom("moo".getBytes()))
        .build();
  }

  private static final byte[] ALL_STRINGS_BYTES = asBytes(new int[] {
    0x0A, 0x00, 0x12, 0x03, 0x66, 0x6F, 0x6F, 0x1A, 0x06, 0xE4, 0xBD, 0xA0, 0xE5, 0xA5, 0xBD, 0x22,
    0x03, 0x64, 0x6F, 0x67, 0xAA, 0x01, 0x00, 0xAA, 0x01, 0x00, 0xB2, 0x01, 0x03, 0x62, 0x61, 0x72,
    0xB2, 0x01, 0x03, 0x62, 0x61, 0x7A, 0xBA, 0x01, 0x0A, 0xD9, 0x85, 0xD8, 0xB1, 0xD8, 0xAD, 0xD8,
    0xA8, 0xD8, 0xA7, 0xBA, 0x01, 0x0C, 0xD0, 0xBF, 0xD1, 0x80, 0xD0, 0xB8, 0xD0, 0xB2, 0xD0, 0xB5,
    0xD1, 0x82, 0xC2, 0x01, 0x03, 0x63, 0x61, 0x74, 0xC2, 0x01, 0x03, 0x72, 0x61, 0x74, 0xCA, 0x3E,
    0x00, 0xD2, 0x3E, 0x03, 0x61, 0x62, 0x63, 0xDA, 0x3E, 0x0F, 0xCE, 0xB3, 0xCE, 0xB5, 0xCE, 0xB9,
    0xCE, 0xAC, 0x20, 0xCF, 0x83, 0xCE, 0xBF, 0xCF, 0x85, 0xE2, 0x3E, 0x03, 0x70, 0x69, 0x67, 0xEA,
    0x3F, 0x00, 0xEA, 0x3F, 0x00, 0xF2, 0x3F, 0x03, 0x64, 0x65, 0x66, 0xF2, 0x3F, 0x03, 0x67, 0x68,
    0x69, 0xFA, 0x3F, 0x12, 0xE0, 0xA4, 0xA8, 0xE0, 0xA4, 0xAE, 0xE0, 0xA4, 0xB8, 0xE0, 0xA5, 0x8D,
    0xE0, 0xA4, 0xA4, 0xE0, 0xA5, 0x87, 0xFA, 0x3F, 0x06, 0x68, 0x61, 0x6C, 0x6C, 0xC3, 0xB3, 0x82,
    0x40, 0x03, 0x63, 0x6F, 0x77, 0x82, 0x40, 0x03, 0x6D, 0x6F, 0x6F
  });
}
