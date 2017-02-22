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
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.MessageFormat.Field;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;


public class Support_MessageFormat extends Support_Format {

	public Support_MessageFormat(String p1) {
		super(p1);
	}

	@Override
    public void runTest() {
		t_formatToCharacterIterator();
		t_format_with_FieldPosition();
	}

	public static void main(String[] args) {
		new Support_MessageFormat("").runTest();
	}

	public void t_format_with_FieldPosition() {
		// This test assumes a default DateFormat.is24Hour setting.
                /* J2ObjC: DateFormat.is24Hour is Android-specific.
		DateFormat.is24Hour = null;*/

		String pattern = "On {4,date} at {3,time}, he ate {2,number, integer} hamburger{2,choice,1#|1<s} and drank {1, number} liters of coke. That was {0,choice,1#just enough|1<more than enough} food!";
		MessageFormat format = new MessageFormat(pattern, Locale.US);

		Date date = new GregorianCalendar(2005, 1, 28, 14, 20, 16).getTime();
		Integer hamburgers = new Integer(8);
		Object[] objects = new Object[] { hamburgers, new Double(3.5),
				hamburgers, date, date };

		super.text = "On Feb 28, 2005 at 2:20:16 PM, he ate 8 hamburgers and drank 3.5 liters of coke. That was more than enough food!";

		// test with MessageFormat.Field.ARGUMENT
		t_FormatWithField(1, format, objects, null, Field.ARGUMENT, 3, 15);

		// test other format fields that are included in the formatted text
		t_FormatWithField(2, format, objects, null, DateFormat.Field.AM_PM, 0,
				0);
		t_FormatWithField(3, format, objects, null,
				NumberFormat.Field.FRACTION, 0, 0);

		// test fields that are not included in the formatted text
		t_FormatWithField(4, format, objects, null, DateFormat.Field.ERA, 0, 0);
		t_FormatWithField(5, format, objects, null,
				NumberFormat.Field.EXPONENT_SIGN, 0, 0);
	}

	public void t_formatToCharacterIterator() {
		// This test assumes a default DateFormat.is24Hour setting.
                /* J2ObjC: DateFormat.is24Hour is Android-specific.
		DateFormat.is24Hour = null;*/

		String pattern = "On {4,date} at {3,time}, he ate {2,number, integer} hamburger{2,choice,1#|1<s} and drank {1, number} liters of coke. That was {0,choice,1#just enough|1<more than enough} food!";
		MessageFormat format = new MessageFormat(pattern, Locale.US);

		Date date = new GregorianCalendar(2005, 1, 28, 14, 20, 16).getTime();
		Integer hamburgers = new Integer(8);
		Object[] objects = new Object[] { hamburgers, new Double(3.5), hamburgers, date, date };

		t_Format(1, objects, format, getMessageVector1());
	}

  private Vector<FieldContainer> getMessageVector1() {
    Vector<FieldContainer> v = new Vector<FieldContainer>();
    v.add(new FieldContainer(3, 6, DateFormat.Field.MONTH));
    v.add(new FieldContainer(3, 6, Field.ARGUMENT, 4));
    v.add(new FieldContainer(6, 7, Field.ARGUMENT, 4));
    v.add(new FieldContainer(7, 9, DateFormat.Field.DAY_OF_MONTH));
    v.add(new FieldContainer(7, 9, Field.ARGUMENT, 4));
    v.add(new FieldContainer(9, 11, Field.ARGUMENT, 4));
    v.add(new FieldContainer(11, 15, DateFormat.Field.YEAR));
    v.add(new FieldContainer(11, 15, Field.ARGUMENT, 4));
    v.add(new FieldContainer(19, 20, DateFormat.Field.HOUR1));
    v.add(new FieldContainer(19, 20, Field.ARGUMENT, 3));
    v.add(new FieldContainer(20, 21, Field.ARGUMENT, 3));
    v.add(new FieldContainer(21, 23, DateFormat.Field.MINUTE));
    v.add(new FieldContainer(21, 23, Field.ARGUMENT, 3));
    v.add(new FieldContainer(23, 24, Field.ARGUMENT, 3));
    v.add(new FieldContainer(24, 26, DateFormat.Field.SECOND));
    v.add(new FieldContainer(24, 26, Field.ARGUMENT, 3));
    v.add(new FieldContainer(26, 27, Field.ARGUMENT, 3));
    v.add(new FieldContainer(27, 29, DateFormat.Field.AM_PM));
    v.add(new FieldContainer(27, 29, Field.ARGUMENT, 3));
    v.add(new FieldContainer(38, 39, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(38, 39, Field.ARGUMENT, 2));
    v.add(new FieldContainer(49, 50, Field.ARGUMENT, 2));
    v.add(new FieldContainer(61, 62, NumberFormat.Field.INTEGER));
    v.add(new FieldContainer(61, 62, Field.ARGUMENT, 1));
    v.add(new FieldContainer(62, 63, NumberFormat.Field.DECIMAL_SEPARATOR));
    v.add(new FieldContainer(62, 63, Field.ARGUMENT, 1));
    v.add(new FieldContainer(63, 64, NumberFormat.Field.FRACTION));
    v.add(new FieldContainer(63, 64, Field.ARGUMENT, 1));
    v.add(new FieldContainer(90, 106, Field.ARGUMENT, 0));
    return v;
  }

}
