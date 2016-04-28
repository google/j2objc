/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.j2objc;

/**
 * Thrown when a reflection API is used but reflection was disabled during translation.
 */
public class ReflectionStrippedError extends Error {

  private static final String EXCEPTION_MESSAGE =
      ": Reflection is unavailable. Fix this by avoiding reflection or building without"
      + " --strip-reflection.";

  private static final long serialVersionUID = 1896945698960498962L;

  /**
   * Create a new ReflectionStrippedError.
   */
  public ReflectionStrippedError(Class<?> cls) {
    super(cls.getName() + EXCEPTION_MESSAGE);
  }
}
