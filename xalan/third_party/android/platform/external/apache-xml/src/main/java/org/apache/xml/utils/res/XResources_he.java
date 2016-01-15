/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: XResources_he.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xml.utils.res;

//
//  LangResources_en.properties
//

/**
 * The Hebrew resource bundle.
 * @xsl.usage internal
 */
public class XResources_he extends XResourceBundle
{

  /**
   * Get the association list.
   *
   * @return The association list.
   */
  public Object[][] getContents()
  {
    return new Object[][]
  {
    { "ui_language", "he" }, { "help_language", "he" }, { "language", "he" },
    { "alphabet", new CharArrayWrapper(
      new char[]{ 0x05D0, 0x05D1, 0x05D2, 0x05D3, 0x05D4, 0x05D5, 0x05D6,
                  0x05D7, 0x05D8, 0x05D9, 0x05DA, 0x05DB, 0x05DC, 0x05DD,
                  0x05DE, 0x05DF, 0x05E0, 0x05E1 }) },
    { "tradAlphabet", new CharArrayWrapper(
      new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                  'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                  'Y', 'Z' }) },

    //language orientation
    { "orientation", "RightToLeft" },

    //language numbering   
    { "numbering", "additive" },

    // largest numerical value
    //{"MaxNumericalValue", new Integer()},
    //These would not be used for EN. Only used for traditional numbering   
    { "numberGroups", new IntArrayWrapper(new int[]{ 10, 1 }) },

    //These only used for mutiplicative-additive numbering
    //{"multiplier", "10"},
    //{"multiplierChar", "M"}, 
    //{"digits", new char[]{'a','b','c','d','e','f','g','h','i'}},
    { "digits", new CharArrayWrapper(
      new char[]{ 0x05D0, 0x05D1, 0x05D2, 0x05D3, 0x05D4, 0x05D5, 0x05D6,
                  0x05D7, 0x05D8 }) },
    { "tens", new CharArrayWrapper(
      new char[]{ 0x05D9, 0x05DA, 0x05DB, 0x05DC, 0x05DD, 0x05DE, 0x05DF,
                  0x05E0, 0x05E1 }) },

    //hundreds, etc...
    { "tables", new StringArrayWrapper(new String[]{ "tens", "digits" }) }
  };
  }
}
