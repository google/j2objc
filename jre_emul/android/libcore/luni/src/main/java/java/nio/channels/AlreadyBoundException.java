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

package java.nio.channels;

/**
 * An {@code AlreadyBoundException} is thrown when an attempt is made to bind a NetworkChannel that
 * is already bound.
 *
 * @hide Until ready for a public API change
 */
public class AlreadyBoundException extends IllegalStateException {

  private static final long serialVersionUID = 6796072983322737592L;

  public AlreadyBoundException() {
  }
}
