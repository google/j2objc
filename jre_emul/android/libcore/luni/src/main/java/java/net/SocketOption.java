/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

/**
 * An option associated with a socket.
 *
 * <p>See {@link java.nio.channels.NetworkChannel#setOption},
 * {@link java.nio.channels.NetworkChannel#getOption} and
 * {@link java.nio.channels.NetworkChannel#supportedOptions} for methods that use SocketOption.
 *
 * <p>See {@link StandardSocketOptions} for valid SocketOptions.
 *
 * @param <T> the type of the value
 * @since 1.7
 * @hide Until ready for a public API change
 */
public interface SocketOption<T> {

  /**
   * Returns the name of the option.
   */
  String name();

  /**
   * Returns the type of the value of the option.
   */
  Class<T> type();
}
