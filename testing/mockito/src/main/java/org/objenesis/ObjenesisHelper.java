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

package org.objenesis;

/**
 * A j2objc-specific reimplementation of ObjenesisHelper.newInstance(),
 * to only be used by the j2objc Mockito port. This was done because
 * the Mockito team keeps adding newInstance() references, even though
 * we worked with them to eliminate them to support an iOS port.
 */
public final class ObjenesisHelper {

  /**
   * Creates a new object without any constructor being called.
   *
   * @param <T> Type instantiated
   * @param clazz Class to instantiate
   * @return New instance of clazz
   */
  public static native <T> T newInstance(Class<T> clazz) /*-[
    return [((IOSClass *) nil_chk(clazz)) newInstance];
  ]-*/;
}
