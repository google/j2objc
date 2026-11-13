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

package com.google.j2objc.kmp;

import com.google.j2objc.annotations.ObjectiveCName;
import java.util.ArrayList;
import java.util.List;

/** Static Java entry points exercising polymorphic calls across KMP-adapted hierarchies. */
@ObjectiveCName("KmpCallers")
public final class KmpCallers {

  private KmpCallers() {}

  /** Creates a single-element mutable Java {@link List}. */
  public static List<String> singletonList(String item) {
    List<String> out = new ArrayList<>();
    out.add(item);
    return out;
  }

  /** Invokes {@link OpenService#getTags} from Java. */
  public static List<String> callOpenServiceGetTags(OpenService service, List<String> prefix) {
    return service.getTags(prefix);
  }

  /** Invokes {@link OpenService#setTags} from Java. */
  public static void callOpenServiceSetTags(OpenService service, List<String> tags) {
    service.setTags(tags);
  }

  /** Invokes {@link JavaSubService#getTags} through a {@link JavaSubService} reference. */
  public static List<String> callJavaSubServiceGetTags(
      JavaSubService service, List<String> prefix) {
    return service.getTags(prefix);
  }

  /** Invokes {@link GenericItemBase#selectItem} through a parameterized base reference. */
  public static SubItem callGenericItemBaseSelect(
      GenericItemBase<SubItem> service, List<String> keys) {
    return service.selectItem(keys);
  }

  /** Invokes {@link SubItemService#selectItem} through the concrete subclass reference. */
  public static SubItem callSubItemServiceSelect(SubItemService service, List<String> keys) {
    return service.selectItem(keys);
  }

  /** Invokes {@link GenericCovariantBase#compute} through a parameterized base reference. */
  public static Object callGenericCovariantBaseCompute(
      GenericCovariantBase<Integer> service, int tag, List<String> items) {
    return service.compute(tag, items);
  }

  /** Invokes {@link CovariantListSubService#compute} through the covariant subclass reference. */
  public static List<String> callCovariantListSubCompute(
      CovariantListSubService service, int tag, List<String> items) {
    return service.compute(tag, items);
  }

  /** Invokes {@link BaseTaggedInterface#resolveLabels} through the superinterface reference. */
  public static List<String> callBaseTaggedResolve(
      BaseTaggedInterface service, List<String> seed) {
    return service.resolveLabels(seed);
  }

  /** Invokes {@link ConcreteTaggedBase#resolveLabels} through the concrete base reference. */
  public static List<String> callConcreteTaggedResolve(
      ConcreteTaggedBase service, List<String> seed) {
    return service.resolveLabels(seed);
  }

  /** Invokes {@link DefaultShimSubService#resolveLabels} through the shim subclass reference. */
  public static List<String> callDefaultShimResolve(
      DefaultShimSubService service, List<String> seed) {
    return service.resolveLabels(seed);
  }

  /** Invokes {@link KmpSubItemProtocol#pick} through the annotated interface reference. */
  public static SubItem callSubItemProtocolPick(KmpSubItemProtocol service, List<String> items) {
    return service.pick(items);
  }

  /** Invokes {@link MidBridgeService#pick} through the concrete bridge class reference. */
  public static SubItem callMidBridgePick(MidBridgeService service, List<String> items) {
    return service.pick(items);
  }
}
