/*
 * Copyright (C) 2013 The Android Open Source Project
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

package java.lang;

import java.lang.reflect.Array;

/**
 * Superclass of exceptions related to reflection.
 *
 * @since 1.7
 */
public class ReflectiveOperationException extends Exception {
  /**
   * Constructs a new exception with no detail message.
   */
  public ReflectiveOperationException() {
  }

  /**
   * Constructs a new exception with the given detail message.
   */
  public ReflectiveOperationException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the given cause.
   */
  public ReflectiveOperationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new exception with the given detail message and cause.
   */
  public ReflectiveOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
