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

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import protos.TypicalData;
import protos.TypicalDataMessage;

/** Tests for correct serialization and deserialization with trailing zeros fields. */
public class TrailingZerosTest extends ProtobufTest {

  public void testReadsGenericMessageWithTrailingZeros() throws Exception {
    TypicalData data =
        TypicalData.newBuilder()
            .addRepeatedInt32(12)
            .addAllRepeatedInt32(Arrays.asList(1, 3, 5, 8, 11))
            .setRepeatedInt32(2, 67)
            .setMyBool(true)
            .build();
    byte[] serializedData = data.toByteArray();
    byte[] paddedSerializedData = Arrays.copyOf(serializedData, serializedData.length + 6);
    TypicalData deserilizedData = TypicalData.parseFrom(paddedSerializedData);
    assertEquals(data, deserilizedData);
  }

  public void testReadsMessageWithEmbeddedNullsAndTrailingZeros() throws Exception {
    TypicalData data = TypicalData.newBuilder().setMyString("Hello\u0000\u0000\u0000").build();
    byte[] serializedData = data.toByteArray();
    byte[] paddedSerializedData = Arrays.copyOf(serializedData, serializedData.length + 6);
    TypicalData deserilizedData = TypicalData.parseFrom(paddedSerializedData);
    assertEquals(data, deserilizedData);
  }

  public void testReadsMessageWithSubmessageAndTrailingZeros() throws Exception {
    TypicalData data =
        TypicalData.newBuilder()
            .setMyMessage(TypicalDataMessage.newBuilder().setMyMessageInt(0).build())
            .build();
    byte[] serializedData = data.toByteArray();
    byte[] paddedSerializedData = Arrays.copyOf(serializedData, serializedData.length + 6);
    TypicalData deserilizedData = TypicalData.parseFrom(paddedSerializedData);
    assertEquals(data, deserilizedData);
  }

  public void testReadsMessageFromEmptyBuffer() throws Exception {
    byte[] serializedData = new byte[0];
    TypicalData.parseFrom(serializedData);
    // success
  }

  public void testReadsMessageFromBufferWithAllZeros() throws Exception {
    byte[] serializedData = new byte[2];
    TypicalData.parseFrom(serializedData);
    // success
  }

  public void testFailsToReadMessageWithTooManyTrailingZeros() throws Exception {
    TypicalData data = TypicalData.newBuilder().build();
    byte[] serializedData = data.toByteArray();
    byte[] paddedSerializedData = Arrays.copyOf(serializedData, serializedData.length + 8);
    try {
      TypicalData.parseFrom(paddedSerializedData);
      fail("should not have parsed a buffer with too many trailing zeros");
    } catch (InvalidProtocolBufferException e) {
      // expected
    }
  }
}
