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

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/*-[
#import "NSDictionaryMap.h"
]-*/

/**
 * JUnit tests for NSDictionaryMap.
 */
public class NSDictionaryMapTest extends TestCase {

  private static native Map<Integer, String> makeNSDictionaryMap() /*-[
    NSDictionary *dictionary = @{
      @1  : @"one",
      @2  : @"two",
      @3  : @"three",
      @42 : @"forty-two"
    };
    return [NSDictionaryMap mapWithDictionary:dictionary];
  ]-*/;

  // Verifies NSDictionary's Map.forEach() implementation.
  public void testForEach() {
    Map<Integer, String> nsDictionaryMap = makeNSDictionaryMap();
    Map<Integer, String> duplicateMap = new HashMap<>();
    nsDictionaryMap.forEach((key, value) -> duplicateMap.put(key, value));
    assertEquals("Maps sizes differ", duplicateMap.size(), nsDictionaryMap.size());
    for (Integer integer : nsDictionaryMap.keySet()) {
      assertTrue(duplicateMap.containsKey(integer));
      assertEquals(duplicateMap.get(integer), nsDictionaryMap.get(integer));
    }
  }
}
