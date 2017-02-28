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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormat.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.harmony.tests.java.text.Support_Format.FieldContainer;

public class Support_SimpleDateFormat extends Support_Format {

  public Support_SimpleDateFormat(String p1) {
    super(p1);
  }

  @Override public void runTest() {
    t_formatToCharacterIterator();
    t_format_with_FieldPosition();
  }

  public static void main(String[] args) {
    new Support_SimpleDateFormat("").runTest();
  }

  public void t_format_with_FieldPosition() {
    TimeZone tz = TimeZone.getTimeZone("EST");
    Calendar cal = new GregorianCalendar(tz);
    cal.set(1999, Calendar.SEPTEMBER, 13, 17, 19, 01);
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();
    SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US);
    format.setTimeZone(tz);

    // test with all pattern chars, and multiple occurences
    format.applyPattern("G GGGG y yy yyyy M MM MMM MMMM d dd ddd " +
                        "k kk kkk H HH HHH h hh hhh m mmm s ss sss S SS SSS EE EEEE " +
                        "D DD DDD F FF w www W WWW " +
                        "a  aaa  K KKK z zzzz Z ZZZZ");

    StringBuffer textBuffer = new StringBuffer();
    // Really, GGGG should be "Anno Domini", but the RI doesn't support that and no one cares.
    textBuffer.append("AD AD 1999 99 1999 9 09 Sep September 13 13 013 ");
    textBuffer.append("17 17 017 17 17 017 5 05 005 19 019 1 01 001 0 00 000 Mon Monday ");
    textBuffer.append("256 256 256 2 02 38 038 3 003 ");
    textBuffer.append("PM  PM  5 005 GMT-5 GMT-05:00 -0500 GMT-05:00");

    // to avoid passing the huge StringBuffer each time.
    super.text = textBuffer.toString();

    // test if field positions are set correctly for these fields occurring
    // multiple times.
    t_FormatWithField(0, format, date, null, Field.ERA, 0, 2);
    t_FormatWithField(1, format, date, null, Field.YEAR, 6, 10);
    t_FormatWithField(2, format, date, null, Field.MONTH, 19, 20);
    t_FormatWithField(3, format, date, null, Field.DAY_OF_MONTH, 38, 40);
    t_FormatWithField(4, format, date, null, Field.HOUR_OF_DAY1, 48, 50);
    t_FormatWithField(5, format, date, null, Field.HOUR_OF_DAY0, 58, 60);
    t_FormatWithField(6, format, date, null, Field.HOUR1, 68, 69);
    t_FormatWithField(7, format, date, null, Field.MINUTE, 77, 79);
    t_FormatWithField(8, format, date, null, Field.SECOND, 84, 85);
    t_FormatWithField(9, format, date, null, Field.MILLISECOND, 93, 94);
    t_FormatWithField(10, format, date, null, Field.DAY_OF_WEEK, 102, 105);
    t_FormatWithField(11, format, date, null, Field.DAY_OF_YEAR, 113, 116);
    t_FormatWithField(12, format, date, null, Field.DAY_OF_WEEK_IN_MONTH, 125, 126);
    t_FormatWithField(13, format, date, null, Field.WEEK_OF_YEAR, 130, 132);
    t_FormatWithField(14, format, date, null, Field.WEEK_OF_MONTH, 137, 138);
    t_FormatWithField(15, format, date, null, Field.AM_PM, 143, 145);
    t_FormatWithField(16, format, date, null, Field.HOUR0, 151, 152);
    t_FormatWithField(17, format, date, null, Field.TIME_ZONE, 157, 162);

    // test fields that are not included in the formatted text
    t_FormatWithField(18, format, date, null, NumberFormat.Field.EXPONENT_SIGN, 0, 0);

    // test with simple example
    format.applyPattern("h:m z");

    super.text = "5:19 GMT-5";
    t_FormatWithField(21, format, date, null, Field.HOUR1, 0, 1);
    t_FormatWithField(22, format, date, null, Field.MINUTE, 2, 4);
    t_FormatWithField(23, format, date, null, Field.TIME_ZONE, 5, 10);

    // test fields that are not included in the formatted text
    t_FormatWithField(24, format, date, null, Field.ERA, 0, 0);
    t_FormatWithField(25, format, date, null, Field.YEAR, 0, 0);
    t_FormatWithField(26, format, date, null, Field.MONTH, 0, 0);
    t_FormatWithField(27, format, date, null, Field.DAY_OF_MONTH, 0, 0);
    t_FormatWithField(28, format, date, null, Field.HOUR_OF_DAY1, 0, 0);
    t_FormatWithField(29, format, date, null, Field.HOUR_OF_DAY0, 0, 0);
    t_FormatWithField(30, format, date, null, Field.SECOND, 0, 0);
    t_FormatWithField(31, format, date, null, Field.MILLISECOND, 0, 0);
    t_FormatWithField(32, format, date, null, Field.DAY_OF_WEEK, 0, 0);
    t_FormatWithField(33, format, date, null, Field.DAY_OF_YEAR, 0, 0);
    t_FormatWithField(34, format, date, null, Field.DAY_OF_WEEK_IN_MONTH, 0, 0);
    t_FormatWithField(35, format, date, null, Field.WEEK_OF_YEAR, 0, 0);
    t_FormatWithField(36, format, date, null, Field.WEEK_OF_MONTH, 0, 0);
    t_FormatWithField(37, format, date, null, Field.AM_PM, 0, 0);
    t_FormatWithField(38, format, date, null, Field.HOUR0, 0, 0);

    t_FormatWithField(39, format, date, null, NumberFormat.Field.EXPONENT, 0, 0);

    // test with simple example with pattern char Z
    format.applyPattern("h:m Z z");
    super.text = "5:19 -0500 GMT-5";
    t_FormatWithField(40, format, date, null, Field.HOUR1, 0, 1);
    t_FormatWithField(41, format, date, null, Field.MINUTE, 2, 4);
    t_FormatWithField(42, format, date, null, Field.TIME_ZONE, 5, 10);
  }

  public void t_formatToCharacterIterator() {
    TimeZone tz = TimeZone.getTimeZone("EST");
    Calendar cal = new GregorianCalendar(tz);
    cal.set(1999, Calendar.SEPTEMBER, 13, 17, 19, 01);
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();
    SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US);
    format.setTimeZone(tz);

    format.applyPattern("yyyyMMddHHmmss");
    t_Format(1, date, format, getDateVector1());

    format.applyPattern("w W dd MMMM yyyy EEEE");
    t_Format(2, date, format, getDateVector2());

    format.applyPattern("h:m z");
    t_Format(3, date, format, getDateVector3());

    format.applyPattern("h:m Z");
    t_Format(5, date, format, getDateVector5());

    // with all pattern chars, and multiple occurences
    format.applyPattern("G GGGG y yy yyyy M MM MMM MMMM d dd ddd k kk kkk H HH HHH h hh hhh m mmm s ss sss S SS SSS EE EEEE D DD DDD F FF w www W WWW a  aaa  K KKK z zzzz Z ZZZZ");
    t_Format(4, date, format, getDateVector4());
  }

  private Vector<FieldContainer> getDateVector1() {
    // "19990913171901"
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 4, Field.YEAR));
    v.add(new FieldContainer(4, 6, Field.MONTH));
    v.add(new FieldContainer(6, 8, Field.DAY_OF_MONTH));
    v.add(new FieldContainer(8, 10, Field.HOUR_OF_DAY0));
    v.add(new FieldContainer(10, 12, Field.MINUTE));
    v.add(new FieldContainer(12, 14, Field.SECOND));
    return v;
  }

  private Vector<FieldContainer> getDateVector2() {
    // "12 3 5 March 2002 Monday"
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 2, Field.WEEK_OF_YEAR));
    v.add(new FieldContainer(3, 4, Field.WEEK_OF_MONTH));
    v.add(new FieldContainer(5, 7, Field.DAY_OF_MONTH));
    v.add(new FieldContainer(8, 17, Field.MONTH));
    v.add(new FieldContainer(18, 22, Field.YEAR));
    v.add(new FieldContainer(23, 29, Field.DAY_OF_WEEK));
    return v;
  }

  private Vector<FieldContainer> getDateVector3() {
    // "5:19 EDT"
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, Field.HOUR1));
    v.add(new FieldContainer(2, 4, Field.MINUTE));
    v.add(new FieldContainer(5, 10, Field.TIME_ZONE));
    return v;
  }

  private Vector<FieldContainer> getDateVector5() {
    // "5:19 -0400"
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(0, 1, Field.HOUR1));
    v.add(new FieldContainer(2, 4, Field.MINUTE));
    v.add(new FieldContainer(5, 10, Field.TIME_ZONE));
    return v;
  }

  private Vector<FieldContainer> getDateVector4() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();

    // "AD AD 1999 99 1999 9 09 Sep September 13 13 013 17 17 017 17 17 017 5
    // 05
    // 005 19 019 1 01 001 0 00 000 Mon Monday 256 256 256 2 02 38 038 3 003
    // PM
    // PM 5 005 EDT Eastern Daylight Time -0400 -0400"
    v.add(new FieldContainer(0, 2, Field.ERA));
    v.add(new FieldContainer(3, 5, Field.ERA));
    v.add(new FieldContainer(6, 10, Field.YEAR));
    v.add(new FieldContainer(11, 13, Field.YEAR));
    v.add(new FieldContainer(14, 18, Field.YEAR));
    v.add(new FieldContainer(19, 20, Field.MONTH));
    v.add(new FieldContainer(21, 23, Field.MONTH));
    v.add(new FieldContainer(24, 27, Field.MONTH));
    v.add(new FieldContainer(28, 37, Field.MONTH));
    v.add(new FieldContainer(38, 40, Field.DAY_OF_MONTH));
    v.add(new FieldContainer(41, 43, Field.DAY_OF_MONTH));
    v.add(new FieldContainer(44, 47, Field.DAY_OF_MONTH));
    v.add(new FieldContainer(48, 50, Field.HOUR_OF_DAY1));
    v.add(new FieldContainer(51, 53, Field.HOUR_OF_DAY1));
    v.add(new FieldContainer(54, 57, Field.HOUR_OF_DAY1));
    v.add(new FieldContainer(58, 60, Field.HOUR_OF_DAY0));
    v.add(new FieldContainer(61, 63, Field.HOUR_OF_DAY0));
    v.add(new FieldContainer(64, 67, Field.HOUR_OF_DAY0));
    v.add(new FieldContainer(68, 69, Field.HOUR1));
    v.add(new FieldContainer(70, 72, Field.HOUR1));
    v.add(new FieldContainer(73, 76, Field.HOUR1));
    v.add(new FieldContainer(77, 79, Field.MINUTE));
    v.add(new FieldContainer(80, 83, Field.MINUTE));
    v.add(new FieldContainer(84, 85, Field.SECOND));
    v.add(new FieldContainer(86, 88, Field.SECOND));
    v.add(new FieldContainer(89, 92, Field.SECOND));
    v.add(new FieldContainer(93, 94, Field.MILLISECOND));
    v.add(new FieldContainer(95, 97, Field.MILLISECOND));
    v.add(new FieldContainer(98, 101, Field.MILLISECOND));
    v.add(new FieldContainer(102, 105, Field.DAY_OF_WEEK));
    v.add(new FieldContainer(106, 112, Field.DAY_OF_WEEK));
    v.add(new FieldContainer(113, 116, Field.DAY_OF_YEAR));
    v.add(new FieldContainer(117, 120, Field.DAY_OF_YEAR));
    v.add(new FieldContainer(121, 124, Field.DAY_OF_YEAR));
    v.add(new FieldContainer(125, 126, Field.DAY_OF_WEEK_IN_MONTH));
    v.add(new FieldContainer(127, 129, Field.DAY_OF_WEEK_IN_MONTH));
    v.add(new FieldContainer(130, 132, Field.WEEK_OF_YEAR));
    v.add(new FieldContainer(133, 136, Field.WEEK_OF_YEAR));
    v.add(new FieldContainer(137, 138, Field.WEEK_OF_MONTH));
    v.add(new FieldContainer(139, 142, Field.WEEK_OF_MONTH));
    v.add(new FieldContainer(143, 145, Field.AM_PM));
    v.add(new FieldContainer(147, 149, Field.AM_PM));
    v.add(new FieldContainer(151, 152, Field.HOUR0));
    v.add(new FieldContainer(153, 156, Field.HOUR0));
    v.add(new FieldContainer(157, 162, Field.TIME_ZONE));
    v.add(new FieldContainer(163, 172, Field.TIME_ZONE));
    v.add(new FieldContainer(173, 178, Field.TIME_ZONE));
    v.add(new FieldContainer(179, 188, Field.TIME_ZONE));
    return v;
  }
}
