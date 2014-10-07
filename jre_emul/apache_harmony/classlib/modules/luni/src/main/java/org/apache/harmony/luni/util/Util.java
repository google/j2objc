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

/*
 * CHANGES BY BOBW:
 *
 * Calendar, TimeZone and AssertionError references commented-out.
 */

package org.apache.harmony.luni.util;


import java.io.ByteArrayOutputStream;
import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.TimeZone;

public final class Util {

	private static String[] WEEKDAYS = new String[] { "", "Sunday", "Monday",
			"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	private static String[] MONTHS = new String[] { "January", "February",
			"March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };

	private static final String defaultEncoding;

	static {
		String encoding = System.getProperty("os.encoding");
		if (encoding != null) {
			try {
				"".getBytes(encoding);
            } catch (Throwable t) {
                encoding = null;
			}
		}
        defaultEncoding = encoding;
	}

    /**
     * Get bytes from String using default encoding; default encoding can
     *   be changed via "os.encoding" property
     * @param name input String
     * @return byte array
     */
    public static byte[] getBytes(String name) {
		if (defaultEncoding != null) {
			try {
				return name.getBytes(defaultEncoding);
			} catch (java.io.UnsupportedEncodingException e) {
			}
		}      
        return name.getBytes();
	}
    
    /**
     * Get bytes from String with UTF8 encoding
     * @param name
     *          input String
     * @return byte array
     */
    public static byte[] getUTF8Bytes(String name) {
        try {
            return name.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return getBytes(name);
        }
    }

	public static String toString(byte[] bytes) {
		if (defaultEncoding != null) {
			try {
				return new String(bytes, 0, bytes.length, defaultEncoding);
			} catch (java.io.UnsupportedEncodingException e) {
			}
		}
		return new String(bytes, 0, bytes.length);
	}

    public static String toUTF8String(byte[] bytes) {
        return toUTF8String(bytes, 0, bytes.length);
    }    
    
	public static String toString(byte[] bytes, int offset, int length) {
		if (defaultEncoding != null) {
			try {
				return new String(bytes, offset, length, defaultEncoding);
			} catch (java.io.UnsupportedEncodingException e) {
			}
		}
		return new String(bytes, offset, length);
	}

    public static String toUTF8String(byte[] bytes, int offset, int length) {
        try {
            return new String(bytes, offset, length, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return toString(bytes, offset, length);
        }
    }
    
	/**
	 * Answers the millisecond value of the date and time parsed from the
	 * specified String. Many date/time formats are recognized
	 * 
	 * @param string
	 *            the String to parse
	 * @return the millisecond value parsed from the String
	 */
	public static long parseDate(String string) {
		int offset = 0, length = string.length(), state = 0;
		int year = -1, month = -1, date = -1;
		int hour = -1, minute = -1, second = -1;
		final int PAD = 0, LETTERS = 1, NUMBERS = 2;
		StringBuilder buffer = new StringBuilder();

		while (offset <= length) {
			char next = offset < length ? string.charAt(offset) : '\r';
			offset++;

			int nextState;
			if ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))
				nextState = LETTERS;
			else if (next >= '0' && next <= '9')
				nextState = NUMBERS;
			else if (" ,-:\r\t".indexOf(next) == -1)
				throw new IllegalArgumentException();
			else
				nextState = PAD;

			if (state == NUMBERS && nextState != NUMBERS) {
				int digit = Integer.parseInt(buffer.toString());
				buffer.setLength(0);
				if (digit >= 70) {
					if (year != -1
							|| (next != ' ' && next != ',' && next != '\r'))
						throw new IllegalArgumentException();
					year = digit;
				} else if (next == ':') {
					if (hour == -1)
						hour = digit;
					else if (minute == -1)
						minute = digit;
					else
						throw new IllegalArgumentException();
				} else if (next == ' ' || next == ',' || next == '-'
						|| next == '\r') {
					if (hour != -1 && minute == -1)
						minute = digit;
					else if (minute != -1 && second == -1)
						second = digit;
					else if (date == -1)
						date = digit;
					else if (year == -1)
						year = digit;
					else
						throw new IllegalArgumentException();
				} else if (year == -1 && month != -1 && date != -1)
					year = digit;
				else
					throw new IllegalArgumentException();
			} else if (state == LETTERS && nextState != LETTERS) {
				String text = buffer.toString().toUpperCase();
				buffer.setLength(0);
				if (text.length() < 3)
					throw new IllegalArgumentException();
				if (parse(text, WEEKDAYS) != -1) {
				} else if (month == -1 && (month = parse(text, MONTHS)) != -1) {
				} else if (text.equals("GMT")) {
				} else
					throw new IllegalArgumentException();
			}

			if (nextState == LETTERS || nextState == NUMBERS)
				buffer.append(next);
			state = nextState;
		}

		if (year != -1 && month != -1 && date != -1) {
			if (hour == -1)
				hour = 0;
			if (minute == -1)
				minute = 0;
			if (second == -1)
				second = 0;
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			int current = cal.get(Calendar.YEAR) - 80;
			if (year < 100) {
				year += current / 100 * 100;
				if (year < current)
					year += 100;
			}
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DATE, date);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime().getTime();
		}
		throw new IllegalArgumentException();
	}

	private static int parse(String string, String[] array) {
		int length = string.length();
		for (int i = 0; i < array.length; i++) {
			if (string.regionMatches(true, 0, array[i], 0, length))
				return i;
		}
		return -1;
	}

	public static String convertFromUTF8(byte[] buf, int offset, int utfSize)
			throws UTFDataFormatException {
		return convertUTF8WithBuf(buf, new char[utfSize], offset, utfSize);
	}

	public static String convertUTF8WithBuf(byte[] buf, char[] out, int offset,
			int utfSize) throws UTFDataFormatException {
		int count = 0, s = 0, a;
		while (count < utfSize) {
			if ((out[s] = (char) buf[offset + count++]) < '\u0080')
				s++;
			else if (((a = out[s]) & 0xe0) == 0xc0) {
				if (count >= utfSize)
					throw new UTFDataFormatException("Second byte at " + 
						count + " does not match UTF8 Specification");
				int b = buf[count++];
				if ((b & 0xC0) != 0x80)
					throw new UTFDataFormatException("Second byte at " + 
						count + " does not match UTF8 Specification");
				out[s++] = (char) (((a & 0x1F) << 6) | (b & 0x3F));
			} else if ((a & 0xf0) == 0xe0) {
				if (count + 1 >= utfSize)
					throw new UTFDataFormatException("Third byte at " + 
						(count + 1) + " does not match UTF8 Specification");
				int b = buf[count++];
				int c = buf[count++];
				if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80))
					throw new UTFDataFormatException("Second or third byte at " + 
						(count - 2) + " does not match UTF8 Specification");
				out[s++] = (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
			} else {
				throw new UTFDataFormatException("Input at " + 
					(count - 1) + " does not match UTF8 Specification");
			}
		}
		return new String(out, 0, s);
	}

	/**
	 * '%' and two following hex digit characters are converted to the
	 * equivalent byte value. All other characters are passed through
	 * unmodified. e.g. "ABC %24%25" -> "ABC $%"
	 * 
	 * @param s
	 *            java.lang.String The encoded string.
	 * @return java.lang.String The decoded version.
	 */
    public static String decode(String s, boolean convertPlus) {
        return decode(s, convertPlus, null);
    }

    /**
     * '%' and two following hex digit characters are converted to the
     * equivalent byte value. All other characters are passed through
     * unmodified. e.g. "ABC %24%25" -> "ABC $%"
     * 
     * @param s
     *            java.lang.String The encoded string.
     * @param encoding
     *            the specified encoding
     * @return java.lang.String The decoded version.
     */
    public static String decode(String s, boolean convertPlus, String encoding) {
        if (!convertPlus && s.indexOf('%') == -1)
            return s;
        StringBuilder result = new StringBuilder(s.length());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (convertPlus && c == '+')
                result.append(' ');
            else if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= s.length())
                        throw new IllegalArgumentException("Incomplete % sequence at\\: " + i);
                    int d1 = Character.digit(s.charAt(i + 1), 16);
                    int d2 = Character.digit(s.charAt(i + 2), 16);
                    if (d1 == -1 || d2 == -1)
                        throw new IllegalArgumentException("Invalid % sequence (" + 
                        	s.substring(i, i + 3) + ") at \\: " + String.valueOf(i));
                    out.write((byte) ((d1 << 4) + d2));
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');
                if (encoding == null) {
                    result.append(out.toString());
                } else {
                    try {
                        result.append(out.toString(encoding));
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
                continue;
            } else
                result.append(c);
            i++;
        }
        return result.toString();
    }


    /**
     * Encode the given url string as RFC-1738 required.
     * 
     * @param urlString
     * @return encoded URL string
     */
    public static String encodeURL(String urlStr) {
        String digits = "0123456789ABCDEF"; //$NON-NLS-1$

        StringBuilder buf = new StringBuilder(urlStr.length() + 16);
        for (int i = 0; i < urlStr.length(); i++) {
            char ch = urlStr.charAt(i);
            if ('%' == ch) {
                if (i + 1 < urlStr.length()
                        && i + 2 < urlStr.length()
                        && digits.indexOf(Character.toUpperCase(urlStr
                                .charAt(i + 1))) != -1
                        && digits.indexOf(Character.toUpperCase(urlStr
                                .charAt(i + 2))) != -1) {
                    buf.append(ch);
                    buf.append(urlStr.charAt(i + 1));
                    buf.append(urlStr.charAt(i + 2));
                    i += 2;
                } else {
                    buf.append("%25"); //$NON-NLS-1$
                }
            } else if ("\"<>%\\^[]`+$,{}`~| ".indexOf(ch) == -1) { //$NON-NLS-1$
                buf.append(ch);
            } else {
                byte[] bytes = null;
                try {
                    bytes = new String(new char[] { ch }).getBytes("UTF-8"); //$NON-NLS-1$
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
                for (int j = 0; j < bytes.length; j++) {
                    buf.append('%');
                    buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
                    buf.append(digits.charAt(bytes[j] & 0xf));
                }
            }
        }
        return buf.toString();
    }

	public static String toASCIILowerCase(String s) {
        int len = s.length();
		StringBuilder buffer = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if ('A' <= c && c <= 'Z') {
				buffer.append((char) (c + ('a' - 'A')));
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}
	
	public static String toASCIIUpperCase(String s) {
        int len = s.length();
        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
	        if ('a' <= c && c <= 'z') {
	        	buffer.append((char) (c - ('a' - 'A')));
	        } else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}
}
