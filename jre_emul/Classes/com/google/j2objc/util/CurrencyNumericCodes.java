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

package com.google.j2objc.util;

/**
 * Defines a table of ISO 4217 codes and their numbers, to support
 * Currency.getNumericCode(). An interface is used to avoid adding a
 * large data table into jre_core. This interface and the implementation
 * class should be removed when ICU currency support is available.
 */
public interface CurrencyNumericCodes {

  /**
   * Returns the numeric code for a ISO 4217 currency code string.
   *
   * @returns the numeric code, or zero if the currency code isn't known.
   */
  public int getNumericCode(String currencyCode);
}
