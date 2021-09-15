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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import junit.framework.TestCase;
import protobuf_unittest.UnittestProto;
import protobuf_unittest.UnittestProto.TestAllExtensions;
import protobuf_unittest.UnittestProto.TestAllTypes;
import protobuf_unittest.UnittestProto.TestExtensionInsideTable;
import protobuf_unittest.UnittestProto.TestFieldOrderings;
import protobuf_unittest.UnittestProto.TestOneof2;
import protobuf_unittest.UnittestProto.TestOneofBackwardsCompatible;
import protobuf_unittest.UnittestProto.TestPackedExtensions;
import protobuf_unittest.UnittestProto.TestPackedTypes;

/**
 * Tests related to parsing and serialization.
 *
 * @author kenton@google.com (Kenton Varda)
 */
@SuppressWarnings("ProtoParseWithRegistry")
public class WireFormatTest extends TestCase {

  public void testSerialization() throws Exception {
    TestAllTypes message = TestUtil.getAllSet();

    ByteString rawBytes = message.toByteString();
    assertEquals(rawBytes.size(), message.getSerializedSize());

    TestAllTypes message2 = TestAllTypes.parseFrom(rawBytes);

    TestUtil.assertAllFieldsSet(message2);
  }

  public void testSerializationPacked() throws Exception {
    TestPackedTypes message = TestUtil.getPackedSet();

    ByteString rawBytes = message.toByteString();
    assertEquals(rawBytes.size(), message.getSerializedSize());

    @SuppressWarnings("ProtoParseWithRegistry")
    TestPackedTypes message2 = TestPackedTypes.parseFrom(rawBytes);

    TestUtil.assertPackedFieldsSet(message2);
  }

  public void testSerializeExtensions() throws Exception {
    // TestAllTypes and TestAllExtensions should have compatible wire formats,
    // so if we serialize a TestAllExtensions then parse it as TestAllTypes
    // it should work.

    TestAllExtensions message = TestUtil.getAllExtensionsSet();
    ByteString rawBytes = message.toByteString();
    assertEquals(rawBytes.size(), message.getSerializedSize());

    TestAllTypes message2 = TestAllTypes.parseFrom(rawBytes);

    TestUtil.assertAllFieldsSet(message2);
  }

  public void testSerializePackedExtensions() throws Exception {
    // TestPackedTypes and TestPackedExtensions should have compatible wire
    // formats; check that they serialize to the same string.
    TestPackedExtensions message = TestUtil.getPackedExtensionsSet();
    ByteString rawBytes = message.toByteString();

    TestPackedTypes message2 = TestUtil.getPackedSet();
    ByteString rawBytes2 = message2.toByteString();

    assertEquals(rawBytes, rawBytes2);
  }

  public void testSerializationPackedWithoutGetSerializedSize() throws Exception {
    // Write directly to an OutputStream, without invoking getSerializedSize()
    // This used to be a bug where the size of a packed field was incorrect,
    // since getSerializedSize() was never invoked.
    TestPackedTypes message = TestUtil.getPackedSet();

    // Directly construct a CodedOutputStream around the actual OutputStream,
    // in case writeTo(OutputStream output) invokes getSerializedSize();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CodedOutputStream codedOutput = CodedOutputStream.newInstance(outputStream);

    message.writeTo(codedOutput);

    codedOutput.flush();

    @SuppressWarnings("ProtoParseWithRegistry")
    TestPackedTypes message2 = TestPackedTypes.parseFrom(outputStream.toByteArray());

    TestUtil.assertPackedFieldsSet(message2);
  }

  public void testParseExtensions() throws Exception {
    // TestAllTypes and TestAllExtensions should have compatible wire formats,
    // so if we serialize a TestAllTypes then parse it as TestAllExtensions
    // it should work.

    TestAllTypes message = TestUtil.getAllSet();
    ByteString rawBytes = message.toByteString();

    ExtensionRegistryLite registry = TestUtil.getExtensionRegistry();

    TestAllExtensions message2 = TestAllExtensions.parseFrom(rawBytes, registry);

    TestUtil.assertAllExtensionsSet(message2);
  }

  public void testParsePackedExtensions() throws Exception {
    // Ensure that packed extensions can be properly parsed.
    TestPackedExtensions message = TestUtil.getPackedExtensionsSet();
    ByteString rawBytes = message.toByteString();

    ExtensionRegistryLite registry = TestUtil.getExtensionRegistry();

    TestPackedExtensions message2 = TestPackedExtensions.parseFrom(rawBytes, registry);

    TestUtil.assertPackedExtensionsSet(message2);
  }

  public void testSerializeDelimited() throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    TestUtil.getAllSet().writeDelimitedTo(output);
    output.write(12);
    TestUtil.getPackedSet().writeDelimitedTo(output);
    output.write(34);

    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

    TestUtil.assertAllFieldsSet(TestAllTypes.parseDelimitedFrom(input));
    assertEquals(12, input.read());
    TestUtil.assertPackedFieldsSet(TestPackedTypes.parseDelimitedFrom(input));
    assertEquals(34, input.read());
    assertEquals(-1, input.read());

    // We're at EOF, so parsing again should return null.
    assertTrue(TestAllTypes.parseDelimitedFrom(input) == null);
  }

  private ExtensionRegistry getTestFieldOrderingsRegistry() {
    ExtensionRegistry result = ExtensionRegistry.newInstance();
    result.add(UnittestProto.myExtensionInt);
    result.add(UnittestProto.myExtensionString);
    return result;
  }

  public void testParseMultipleExtensionRanges() throws Exception {
    // Make sure we can parse a message that contains multiple extensions
    // ranges.
    TestFieldOrderings source =
        TestFieldOrderings.newBuilder()
            .setMyInt(1)
            .setMyString("foo")
            .setMyFloat(1.0F)
            .setExtension(UnittestProto.myExtensionInt, 23)
            .setExtension(UnittestProto.myExtensionString, "bar")
            .build();
    TestFieldOrderings dest =
        TestFieldOrderings.parseFrom(source.toByteString(), getTestFieldOrderingsRegistry());
    assertEquals(source, dest);
  }

  private static ExtensionRegistry getTestExtensionInsideTableRegistry() {
    ExtensionRegistry result = ExtensionRegistry.newInstance();
    result.add(UnittestProto.testExtensionInsideTableExtension);
    return result;
  }

  public void testExtensionInsideTable() throws Exception {
    // Make sure the extension within the range of table is parsed correctly in experimental
    // runtime.
    TestExtensionInsideTable source =
        TestExtensionInsideTable.newBuilder()
            .setField1(1)
            .setExtension(UnittestProto.testExtensionInsideTableExtension, 23)
            .build();
    TestExtensionInsideTable dest =
        TestExtensionInsideTable.parseFrom(
            source.toByteString(), getTestExtensionInsideTableRegistry());
    assertEquals(source, dest);
  }

  // ================================================================
  // oneof

  public void testOneofWireFormat() throws Exception {
    TestOneof2.Builder builder = TestOneof2.newBuilder();
    TestUtil.setOneof(builder);
    TestOneof2 message = builder.build();

    ByteString rawBytes = message.toByteString();
    assertEquals(rawBytes.size(), message.getSerializedSize());

    @SuppressWarnings("ProtoParseWithRegistry")
    TestOneof2 message2 = TestOneof2.parseFrom(rawBytes);
    TestUtil.assertOneofSet(message2);
  }

  public void testOneofOnlyLastSet() throws Exception {
    TestOneofBackwardsCompatible source =
        TestOneofBackwardsCompatible.newBuilder().setFooInt(100).setFooString("101").build();

    ByteString rawBytes = source.toByteString();
    TestOneof2 message = TestOneof2.parseFrom(rawBytes);
    assertFalse(message.hasFooInt());
    assertTrue(message.hasFooString());
  }
}
