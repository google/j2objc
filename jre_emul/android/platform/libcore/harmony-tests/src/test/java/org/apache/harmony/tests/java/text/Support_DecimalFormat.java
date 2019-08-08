/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

public class Support_DecimalFormat extends Support_Format {

  public Support_DecimalFormat(String p1) {
    super(p1);
  }

  @Override public void runTest() {
    t_formatToCharacterIterator();
    t_format_with_FieldPosition();
  }

  public static void main(String[] args) {
    new Support_DecimalFormat("").runTest();
  }

  public void t_format_with_FieldPosition() {
    DecimalFormat format = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    Number number = new Double(10000000.76);
    String text = "$10,000,000.76";

    t_FormatWithField(0, format, number, text, NumberFormat.Field.CURRENCY, 0, 1);
    t_FormatWithField(1, format, number, text, NumberFormat.Field.INTEGER, 1, 11);
    t_FormatWithField(2, format, number, text, NumberFormat.Field.GROUPING_SEPARATOR, 3, 4);
    t_FormatWithField(3, format, number, text, NumberFormat.Field.DECIMAL_SEPARATOR, 11, 12);
    t_FormatWithField(4, format, number, text, NumberFormat.Field.FRACTION, 12, 14);

    // test fields that are not included in the formatted text
    t_FormatWithField(5, format, number, text, NumberFormat.Field.SIGN, 0, 0);
    t_FormatWithField(6, format, number, text, NumberFormat.Field.EXPONENT, 0, 0);
    t_FormatWithField(7, format, number, text, NumberFormat.Field.EXPONENT_SIGN, 0, 0);
    t_FormatWithField(8, format, number, text, NumberFormat.Field.EXPONENT_SYMBOL, 0, 0);
    t_FormatWithField(9, format, number, text, NumberFormat.Field.PERCENT, 0, 0);
    t_FormatWithField(10, format, number, text, NumberFormat.Field.PERMILLE, 0, 0);

    // test Exponential
    format = new DecimalFormat("000000000.0#E0");
    text = "100000007.6E-1";
    t_FormatWithField(11, format, number, text, NumberFormat.Field.INTEGER, 0, 9);
    t_FormatWithField(12, format, number, text, NumberFormat.Field.DECIMAL_SEPARATOR, 9, 10);
    t_FormatWithField(13, format, number, text, NumberFormat.Field.FRACTION, 10, 11);
    t_FormatWithField(14, format, number, text, NumberFormat.Field.EXPONENT_SYMBOL, 11, 12);
    t_FormatWithField(15, format, number, text, NumberFormat.Field.EXPONENT_SIGN, 12, 13);
    t_FormatWithField(16, format, number, text, NumberFormat.Field.EXPONENT, 13, 14);

    // test fields that are not included in the formatted text
    t_FormatWithField(17, format, number, text, NumberFormat.Field.GROUPING_SEPARATOR, 0, 0);
    t_FormatWithField(18, format, number, text, NumberFormat.Field.SIGN, 0, 0);
    t_FormatWithField(19, format, number, text, NumberFormat.Field.CURRENCY, 0, 0);
    t_FormatWithField(20, format, number, text, NumberFormat.Field.PERCENT, 0, 0);
    t_FormatWithField(21, format, number, text, NumberFormat.Field.PERMILLE, 0, 0);

    // test currency instance with TR Locale
    number = new Double(350.76);
    format = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
    // Turkey either uses "123,45 TL" or "₺123,45"; google3 uses the former because most
    // platforms' fonts don't include U+20BA TURKISH LIRA SIGN. http://b/16727554.
    text = "₺350,76";
    t_FormatWithField(23, format, number, text, NumberFormat.Field.CURRENCY, 0, 1);
    t_FormatWithField(22, format, number, text, NumberFormat.Field.INTEGER, 1, 4);
    t_FormatWithField(22, format, number, text, NumberFormat.Field.DECIMAL_SEPARATOR, 4, 5);
    t_FormatWithField(22, format, number, text, NumberFormat.Field.FRACTION, 5, 7);

    // test fields that are not included in the formatted text
    t_FormatWithField(25, format, number, text, NumberFormat.Field.GROUPING_SEPARATOR, 0, 0);
    t_FormatWithField(27, format, number, text, NumberFormat.Field.SIGN, 0, 0);
    t_FormatWithField(28, format, number, text, NumberFormat.Field.EXPONENT, 0, 0);
    t_FormatWithField(29, format, number, text, NumberFormat.Field.EXPONENT_SIGN, 0, 0);
    t_FormatWithField(30, format, number, text, NumberFormat.Field.EXPONENT_SYMBOL, 0, 0);
    t_FormatWithField(31, format, number, text, NumberFormat.Field.PERCENT, 0, 0);
    t_FormatWithField(32, format, number, text, NumberFormat.Field.PERMILLE, 0, 0);
  }

  public void t_formatToCharacterIterator() {
    Number number = new Double(350.76);
    Number negativeNumber = new Double(-350.76);

    Locale us = Locale.US;
    Locale tr = new Locale("tr", "TR");

    // test number instance
    t_Format(1, number, NumberFormat.getNumberInstance(us), getNumberVectorUS());

    // test integer instance
    // testFormat(2, number, NumberFormat.getIntegerInstance(us),
    // getPercentVectorUS());

    // test percent instance
    t_Format(3, number, NumberFormat.getPercentInstance(us), getPercentVectorUS());

    // test permille pattern
    DecimalFormat format = new DecimalFormat("###0.##\u2030");
    t_Format(4, number, format, getPermilleVector());

    // test exponential pattern with positive exponent
    format = new DecimalFormat("00.0#E0");
    t_Format(5, number, format, getPositiveExponentVector());

    // test exponential pattern with negative exponent
    format = new DecimalFormat("0000.0#E0");
    t_Format(6, number, format, getNegativeExponentVector());

    // test currency instance with US Locale
    t_Format(7, number, NumberFormat.getCurrencyInstance(us), getPositiveCurrencyVectorUS());

    // test negative currency instance with US Locale
    t_Format(8, negativeNumber, NumberFormat.getCurrencyInstance(us), getNegativeCurrencyVectorUS());

    // test currency instance with TR Locale
    t_Format(9, number, NumberFormat.getCurrencyInstance(tr), getPositiveCurrencyVectorTR());

    // test negative currency instance with TR Locale
    t_Format(10, negativeNumber, NumberFormat.getCurrencyInstance(tr), getNegativeCurrencyVectorTR());

    // test multiple grouping separators
    number = new Long(100300400);
    t_Format(11, number, NumberFormat.getNumberInstance(us), getNumberVector2US());

    // test 0
    number = new Long(0);
    t_Format(12, number, NumberFormat.getNumberInstance(us), getZeroVector());
  }

  private static Vector<FieldContainer> getNumberVectorUS() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 3, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(3, 4, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(4, 6, NumberFormat.Field.FRACTION));
    return v;
  }

  private static Vector<FieldContainer> getPositiveCurrencyVectorTR() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, NumberFormat.Field.CURRENCY));
    v.add(new FieldContainer(1, 4, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(4, 5, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(5, 7, NumberFormat.Field.FRACTION));
    return v;
  }

  private static Vector<FieldContainer> getNegativeCurrencyVectorTR() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, NumberFormat.Field.SIGN));
    v.add(new FieldContainer(1, 2, NumberFormat.Field.CURRENCY));
    v.add(new FieldContainer(2, 5, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(5, 6, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(6, 8, NumberFormat.Field.FRACTION));
    return v;
  }

  private static Vector<FieldContainer> getPositiveCurrencyVectorUS() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, NumberFormat.Field.CURRENCY));
    v.add(new FieldContainer(1, 4, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(4, 5, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(5, 7, NumberFormat.Field.FRACTION));
    return v;
  }

  private static Vector<FieldContainer> getNegativeCurrencyVectorUS() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, NumberFormat.Field.SIGN));
    v.add(new FieldContainer(1, 2, NumberFormat.Field.CURRENCY));
    v.add(new FieldContainer(2, 5, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(5, 6, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(6, 8, NumberFormat.Field.FRACTION));
    return v;
  }

  private static Vector<FieldContainer> getPercentVectorUS() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 2, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(2, 3, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(2, 3, NumberFormat.Field.GROUPING_SEPARATOR));
    v.add(new FieldContainer(3, 6, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(6, 7, NumberFormat.Field.PERCENT));
    return v;
  }

  private static Vector<FieldContainer> getPermilleVector() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 6, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(6, 7, NumberFormat.Field.PERMILLE));
    return v;
  }

  private static Vector<FieldContainer> getNegativeExponentVector() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 4, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(4, 5, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(5, 6, NumberFormat.Field.FRACTION));
    v.add(new FieldContainer(6, 7, NumberFormat.Field.EXPONENT_SYMBOL));
    v.add(new FieldContainer(7, 8, NumberFormat.Field.EXPONENT_SIGN));
    v.add(new FieldContainer(8, 9, NumberFormat.Field.EXPONENT));
    return v;
  }

  private static Vector<FieldContainer> getPositiveExponentVector() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 2, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(2, 3, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(3, 5, NumberFormat.Field.FRACTION));
    v.add(new FieldContainer(5, 6, NumberFormat.Field.EXPONENT_SYMBOL));
    v.add(new FieldContainer(6, 7, NumberFormat.Field.EXPONENT));
    return v;
  }

  private static Vector<FieldContainer> getNumberVector2US() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 3, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(3, 4, NumberFormat.Field.GROUPING_SEPARATOR));
    v.add(new FieldContainer(3, 4, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(4, 7, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(7, 8, NumberFormat.Field.GROUPING_SEPARATOR));
    v.add(new FieldContainer(7, 8, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(8, 11, NumberFormat.Field.INTEGER));
    return v;
  }

  private static Vector<FieldContainer> getZeroVector() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, NumberFormat.Field.INTEGER));
    return v;
  }
}
