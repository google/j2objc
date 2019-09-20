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

package java.util;

import junit.framework.TestCase;

/**
 * Supplemental tests for java.util.TreeMap support.
 *
 * @author Keith Stanger
 */
public class TreeMapTest extends TestCase {

  private void iterationHelper(
      Map<Integer, Integer> map, int entriesSum, int keysSum, int valuesSum) {
    int sum = 0;
    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
      sum += entry.getKey() + entry.getValue();
    }
    assertEquals(entriesSum, sum);
    sum = 0;
    for (int i : map.keySet()) {
      sum += i;
    }
    assertEquals(keysSum, sum);
    sum = 0;
    for (int i : map.values()) {
      sum += i;
    }
    assertEquals(valuesSum, sum);
  }

  public void testIteration() {
    TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
    for (int i = 0; i < 1000; i++) {
      map.put(i, i * 2);
    }
    iterationHelper(map, 1498500, 499500, 999000);
    iterationHelper(map.subMap(200, 750), 782925, 260975, 521950);
    iterationHelper(map.subMap(1001, 1002), 0, 0, 0);
    iterationHelper(map.subMap(-2, -1), 0, 0, 0);
    iterationHelper(new TreeMap<Integer, Integer>(), 0, 0, 0);
  }
}
