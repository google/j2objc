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
 * $Id: XResources_el.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xml.utils.res;

//
//  LangResources_en.properties
//

/**
 * The Greek resource bundle.
 * @xsl.usage internal
 */
public class XResources_el extends XResourceBundle
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
    { "ui_language", "el" }, { "help_language", "el" }, { "language", "el" },
    { "alphabet", new CharArrayWrapper(
      new char[]{ 0x03b1, 0x03b2, 0x03b3, 0x03b4, 0x03b5, 0x03b6, 0x03b7,
                  0x03b8, 0x03b9, 0x03ba, 0x03bb, 0x03bc, 0x03bd, 0x03be,
                  0x03bf, 0x03c0, 0x03c1, 0x03c2, 0x03c3, 0x03c4, 0x03c5,
                  0x03c6, 0x03c7, 0x03c8, 0x03c9 }) },
    { "tradAlphabet", new CharArrayWrapper(
      new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                  'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                  'Y', 'Z' }) },

    //language orientation
    { "orientation", "LeftToRight" },

    //language numbering 
    //{"numbering", "additive"},
    { "numbering", "multiplicative-additive" },
    { "multiplierOrder", "precedes" },

    // largest numerical value
    //{"MaxNumericalValue", new Integer()},
    //These would not be used for EN. Only used for traditional numbering   
    { "numberGroups", new IntArrayWrapper(new int[]{ 100, 10, 1 }) },

    //These only used for mutiplicative-additive numbering
    { "multiplier", new LongArrayWrapper(new long[]{ 1000 }) },
    { "multiplierChar", new CharArrayWrapper(new char[]{ 0x03d9 }) },

    // chinese only??
    { "zero", new CharArrayWrapper(new char[0]) },

    //{"digits", new char[]{'a','b','c','d','e','f','g','h','i'}},
    { "digits", new CharArrayWrapper(
      new char[]{ 0x03b1, 0x03b2, 0x03b3, 0x03b4, 0x03b5, 0x03db, 0x03b6,
                  0x03b7, 0x03b8 }) },
    { "tens", new CharArrayWrapper(
      new char[]{ 0x03b9, 0x03ba, 0x03bb, 0x03bc, 0x03bd, 0x03be, 0x03bf,
                  0x03c0, 0x03df }) },
    { "hundreds", new CharArrayWrapper(
      new char[]{ 0x03c1, 0x03c2, 0x03c4, 0x03c5, 0x03c6, 0x03c7, 0x03c8,
                  0x03c9, 0x03e1 }) },

    //{"thousands", new char[]{0x10D9,0x10DA,0x10DB,0x10DC,0x10DD,0x10DE,0x10DF,0x10E0,0x10E1}},  
    //hundreds, etc...
    { "tables", new StringArrayWrapper(new String[]{ "hundreds", "tens", "digits" }) }
  };
  }
}
