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

import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.ExtensionRegistry;
import protos.Fruit;
import protos.FruitBox;
import protos.Greetings;
import protos.Text;

@SuppressWarnings("NanoEnumEvaluator")
public class Proto3EnumTest extends ProtobufTest {

  public void testUnknownEnumConstant() throws Exception {
    // Verify UNRECOGNIZED is a valid enum.
    assertThat(Fruit.UNRECOGNIZED.getDeclaringClass()).isEqualTo(Fruit.class);

    // Which doesn't have an accessible enum number.
    try {
      Fruit.UNRECOGNIZED.getNumber();
      fail();
    } catch (IllegalArgumentException e) {
      // Exception successfully thrown.
    }

    // Or descriptor.
    try {
      Fruit.UNRECOGNIZED.getValueDescriptor();
      fail();
    } catch (IllegalStateException e) {
      // Exception successfully thrown.
    }

    // Verify UNRECOGNIZED is returned correctly in the enum's values array.
    Fruit[] values = Fruit.values();
    assertThat(values).hasLength(5);
    assertThat(values[0]).isEqualTo(Fruit.UNSPECIFIED);
    assertThat(values[1]).isEqualTo(Fruit.APPLE);
    assertThat(values[2]).isEqualTo(Fruit.BANANA);
    assertThat(values[3]).isEqualTo(Fruit.ORANGE);
    assertThat(values[4]).isEqualTo(Fruit.UNRECOGNIZED);
  }

  // Verify EnumType.forNumber() works same as proto2.
  public void testEnumForNumber() throws Exception {
    assertThat(Fruit.forNumber(0)).isEqualTo(Fruit.UNSPECIFIED);
    assertThat(Fruit.forNumber(1)).isEqualTo(Fruit.APPLE);
    assertThat(Fruit.forNumber(2)).isEqualTo(Fruit.BANANA);
    assertThat(Fruit.forNumber(3)).isEqualTo(Fruit.ORANGE);

    assertThat(Fruit.forNumber(-1)).isNull();
    assertThat(Fruit.forNumber(10000)).isNull();
  }

  // Verify message containing an enum is parsed correctly. A binary payload is used
  // so that an unrecognized enum value is also tested.
  public void testUnrecognizedEnum() throws Exception {
    FruitBox box =
        FruitBox.parseFrom(
            new byte[] {0x08, (byte) Fruit.APPLE.getNumber()},
            ExtensionRegistry.getEmptyRegistry());
    assertThat(box.getFruit()).isEqualTo(Fruit.APPLE);
    box =
        FruitBox.parseFrom(
            new byte[] {0x08, (byte) Fruit.BANANA.getNumber()},
            ExtensionRegistry.getEmptyRegistry());
    assertThat(box.getFruit()).isEqualTo(Fruit.BANANA);
    box =
        FruitBox.parseFrom(
            new byte[] {0x08, (byte) Fruit.ORANGE.getNumber()},
            ExtensionRegistry.getEmptyRegistry());
    assertThat(box.getFruit()).isEqualTo(Fruit.ORANGE);

    box = FruitBox.parseFrom(new byte[] {0x08, 0x7f}, ExtensionRegistry.getEmptyRegistry());
    assertThat(box.getFruit()).isSameInstanceAs(Fruit.UNRECOGNIZED);
  }

  // Verify that a proto3 enum with a member that has a number value of -1
  // (the same as UNRECOGNIZED), is generated correctly and accessible.
  public void testNegativeEnumNumber() throws Exception {
    assertThat(Greetings.ENUM_TYPE_NAME_UNKNOWN.getNumber()).isEqualTo(0);
    assertThat(Greetings.HELLO.getNumber()).isEqualTo(1);
    assertThat(Greetings.GOODBYE.getNumber()).isEqualTo(-1);

    assertThat(Greetings.forNumber(0)).isEqualTo(Greetings.ENUM_TYPE_NAME_UNKNOWN);
    assertThat(Greetings.forNumber(1)).isEqualTo(Greetings.HELLO);
    assertThat(Greetings.forNumber(-1)).isEqualTo(Greetings.GOODBYE);

    Text text = Text.parseFrom(new byte[] {0x08, 0x7f}, ExtensionRegistry.getEmptyRegistry());
    assertThat(text.getGreeting()).isSameInstanceAs(Greetings.UNRECOGNIZED);
  }

  public void testSingularParseUnknownEnumSerialization() throws Exception {
    // field 1 (fruit), value 5 (unrecognized)
    // Tag: (1 << 3) | 0 = 8
    // Value: 5
    byte[] bytes = new byte[] {0x08, 0x05};
    FruitBox box = FruitBox.parseFrom(bytes, ExtensionRegistry.getEmptyRegistry());
    
    byte[] outputBytes = box.toByteArray();
    assertThat(outputBytes).isEqualTo(bytes);
  }

  public void testRepeatedParseUnknownEnumSerialization() throws Exception {
    // field 2 (fruits), repeated, packed
    // Tag: (2 << 3) | 2 = 18
    // Length: 3
    // Values: 1 (APPLE), 2 (BANANA), 5 (unrecognized)
    byte[] bytes = new byte[] {0x12, 0x03, 0x01, 0x02, 0x05};
    FruitBox box = FruitBox.parseFrom(bytes, ExtensionRegistry.getEmptyRegistry());
    
    byte[] outputBytes = box.toByteArray();
    assertThat(outputBytes).isEqualTo(bytes);
  }

  public void testMapParseUnknownEnumSerialization() throws Exception {
    // field 3 (fruit_map), map
    // Tag: (3 << 3) | 2 = 26
    // Length: 4
    // Map Entry:
    //   key: field 1, value 1 -> 0x08 0x01
    //   value: field 2, value 5 -> 0x10 0x05
    byte[] bytes = new byte[] {0x1a, 0x04, 0x08, 0x01, 0x10, 0x05};
    FruitBox box = FruitBox.parseFrom(bytes, ExtensionRegistry.getEmptyRegistry());
    
    byte[] outputBytes = box.toByteArray();
    assertThat(outputBytes).isEqualTo(bytes);
  }

}
