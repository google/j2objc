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
}
