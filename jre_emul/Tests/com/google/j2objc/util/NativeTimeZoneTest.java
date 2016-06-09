/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Test the NSTimeZone-backed TimeZone implementation class.
 *
 * The testing strategy here is by no means comprehensive, but suffices for catching regressions:
 * if the underlying concrete TimeZone subclass is not based on a time zone database, it will not be
 * able to handle US time zone changes in 2005 as well as the historical transitions in Australia.
 *
 * @author Lukhnos Liu
 */
public class NativeTimeZoneTest extends TestCase {

  /**
   * Test that the TimeZone instance returns accurate offsets for instants in America/Los_Angeles
   * (commonly known as PT, which includes PST and PDT). This also accounts for the daylight
   * saving time (DST) change due to Energy Policy Act of 2005.
   */
  public void testUSPacificTimeZoneTransitionsSince1970() {
    TimeZone la = TimeZone.getTimeZone("America/Los_Angeles");
    assertEquals(-25200000, la.getOffset(1457863200000L)); // 2016-03-13T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1446368400000L)); // 2015-11-01T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1425808800000L)); // 2015-03-08T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1414918800000L)); // 2014-11-02T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1394359200000L)); // 2014-03-09T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1383469200000L)); // 2013-11-03T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1362909600000L)); // 2013-03-10T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1352019600000L)); // 2012-11-04T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1331460000000L)); // 2012-03-11T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1320570000000L)); // 2011-11-06T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1300010400000L)); // 2011-03-13T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1289120400000L)); // 2010-11-07T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1268560800000L)); // 2010-03-14T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1257066000000L)); // 2009-11-01T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1236506400000L)); // 2009-03-08T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1225616400000L)); // 2008-11-02T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1205056800000L)); // 2008-03-09T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1194166800000L)); // 2007-11-04T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1173607200000L)); // 2007-03-11T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1162112400000L)); // 2006-10-29T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1143972000000L)); // 2006-04-02T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1130662800000L)); // 2005-10-30T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1112522400000L)); // 2005-04-03T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1099213200000L)); // 2004-10-31T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1081072800000L)); // 2004-04-04T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1067158800000L)); // 2003-10-26T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1049623200000L)); // 2003-04-06T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1035709200000L)); // 2002-10-27T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(1018173600000L)); // 2002-04-07T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(1004259600000L)); // 2001-10-28T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(986119200000L)); // 2001-04-01T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(972810000000L)); // 2000-10-29T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(954669600000L)); // 2000-04-02T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(941360400000L)); // 1999-10-31T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(923220000000L)); // 1999-04-04T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(909306000000L)); // 1998-10-25T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(891770400000L)); // 1998-04-05T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(877856400000L)); // 1997-10-26T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(860320800000L)); // 1997-04-06T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(846406800000L)); // 1996-10-27T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(828871200000L)); // 1996-04-07T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(814957200000L)); // 1995-10-29T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(796816800000L)); // 1995-04-02T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(783507600000L)); // 1994-10-30T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(765367200000L)); // 1994-04-03T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(752058000000L)); // 1993-10-31T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(733917600000L)); // 1993-04-04T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(720003600000L)); // 1992-10-25T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(702468000000L)); // 1992-04-05T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(688554000000L)); // 1991-10-27T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(671018400000L)); // 1991-04-07T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(657104400000L)); // 1990-10-28T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(638964000000L)); // 1990-04-01T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(625654800000L)); // 1989-10-29T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(607514400000L)); // 1989-04-02T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(594205200000L)); // 1988-10-30T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(576064800000L)); // 1988-04-03T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(562150800000L)); // 1987-10-25T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(544615200000L)); // 1987-04-05T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(530701200000L)); // 1986-10-26T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(514980000000L)); // 1986-04-27T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(499251600000L)); // 1985-10-27T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(483530400000L)); // 1985-04-28T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(467802000000L)); // 1984-10-28T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(452080800000L)); // 1984-04-29T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(436352400000L)); // 1983-10-30T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(420026400000L)); // 1983-04-24T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(404902800000L)); // 1982-10-31T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(388576800000L)); // 1982-04-25T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(372848400000L)); // 1981-10-25T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(357127200000L)); // 1981-04-26T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(341398800000L)); // 1980-10-26T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(325677600000L)); // 1980-04-27T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(309949200000L)); // 1979-10-28T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(294228000000L)); // 1979-04-29T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(278499600000L)); // 1978-10-29T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(262778400000L)); // 1978-04-30T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(247050000000L)); // 1977-10-30T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(230724000000L)); // 1977-04-24T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(215600400000L)); // 1976-10-31T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(199274400000L)); // 1976-04-25T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(183546000000L)); // 1975-10-26T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(162381600000L)); // 1975-02-23T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(152096400000L)); // 1974-10-27T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(126698400000L)); // 1974-01-06T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(120646800000L)); // 1973-10-28T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(104925600000L)); // 1973-04-29T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(89197200000L)); // 1972-10-29T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(73476000000L)); // 1972-04-30T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(57747600000L)); // 1971-10-31T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(41421600000L)); // 1971-04-25T03:00:00.000-07:00
    assertEquals(-28800000, la.getOffset(25693200000L)); // 1970-10-25T01:00:00.000-08:00
    assertEquals(-25200000, la.getOffset(9972000000L)); // 1970-04-26T03:00:00.000-07:00
  }

  /**
   * New South Wales observed DST during World War I and World War II, and has been observing DST
   * since 1971.
   */
  public void testAustraliaEasternTimeZoneTransitions() {
    TimeZone sydney = TimeZone.getTimeZone("Australia/Sydney");
    assertEquals(36000000, sydney.getOffset(1459612800000L)); // 2016-04-03T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1443888000000L)); // 2015-10-04T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1428163200000L)); // 2015-04-05T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1412438400000L)); // 2014-10-05T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1396713600000L)); // 2014-04-06T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1380988800000L)); // 2013-10-06T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1365264000000L)); // 2013-04-07T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1349539200000L)); // 2012-10-07T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1333209600000L)); // 2012-04-01T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1317484800000L)); // 2011-10-02T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1301760000000L)); // 2011-04-03T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1286035200000L)); // 2010-10-03T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1270310400000L)); // 2010-04-04T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1254585600000L)); // 2009-10-04T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1238860800000L)); // 2009-04-05T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1223136000000L)); // 2008-10-05T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1207411200000L)); // 2008-04-06T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1193500800000L)); // 2007-10-28T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1174752000000L)); // 2007-03-25T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1162051200000L)); // 2006-10-29T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1143907200000L)); // 2006-04-02T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1130601600000L)); // 2005-10-30T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1111852800000L)); // 2005-03-27T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1099152000000L)); // 2004-10-31T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1080403200000L)); // 2004-03-28T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1067097600000L)); // 2003-10-26T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1048953600000L)); // 2003-03-30T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1035648000000L)); // 2002-10-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(1017504000000L)); // 2002-03-31T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(1004198400000L)); // 2001-10-28T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(985449600000L)); // 2001-03-25T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(967305600000L)); // 2000-08-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(954000000000L)); // 2000-03-26T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(941299200000L)); // 1999-10-31T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(922550400000L)); // 1999-03-28T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(909244800000L)); // 1998-10-25T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(891100800000L)); // 1998-03-29T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(877795200000L)); // 1997-10-26T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(859651200000L)); // 1997-03-30T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(846345600000L)); // 1996-10-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(828201600000L)); // 1996-03-31T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(814896000000L)); // 1995-10-29T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(794332800000L)); // 1995-03-05T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(783446400000L)); // 1994-10-30T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(762883200000L)); // 1994-03-06T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(751996800000L)); // 1993-10-31T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(731433600000L)); // 1993-03-07T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(719942400000L)); // 1992-10-25T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(699379200000L)); // 1992-03-01T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(688492800000L)); // 1991-10-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(667929600000L)); // 1991-03-03T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(657043200000L)); // 1990-10-28T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(636480000000L)); // 1990-03-04T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(625593600000L)); // 1989-10-29T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(606240000000L)); // 1989-03-19T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(594144000000L)); // 1988-10-30T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(574790400000L)); // 1988-03-20T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(562089600000L)); // 1987-10-25T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(542736000000L)); // 1987-03-15T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(530035200000L)); // 1986-10-19T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(511286400000L)); // 1986-03-16T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(499190400000L)); // 1985-10-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(478627200000L)); // 1985-03-03T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(467740800000L)); // 1984-10-28T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(447177600000L)); // 1984-03-04T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(436291200000L)); // 1983-10-30T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(415728000000L)); // 1983-03-06T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(404841600000L)); // 1982-10-31T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(386697600000L)); // 1982-04-04T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(372787200000L)); // 1981-10-25T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(352224000000L)); // 1981-03-01T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(341337600000L)); // 1980-10-26T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(320774400000L)); // 1980-03-02T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(309888000000L)); // 1979-10-28T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(289324800000L)); // 1979-03-04T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(278438400000L)); // 1978-10-29T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(257875200000L)); // 1978-03-05T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(246988800000L)); // 1977-10-30T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(226425600000L)); // 1977-03-06T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(215539200000L)); // 1976-10-31T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(194976000000L)); // 1976-03-07T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(183484800000L)); // 1975-10-26T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(162921600000L)); // 1975-03-02T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(152035200000L)); // 1974-10-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(131472000000L)); // 1974-03-03T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(120585600000L)); // 1973-10-28T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(100022400000L)); // 1973-03-04T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(89136000000L)); // 1972-10-29T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(67968000000L)); // 1972-02-27T02:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(57686400000L)); // 1971-10-31T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(-813229200000L)); // 1944-03-26T01:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(-828345600000L)); // 1943-10-03T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(-844678800000L)); // 1943-03-28T01:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(-860400000000L)); // 1942-09-27T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(-876128400000L)); // 1942-03-29T01:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(-883641600000L)); // 1942-01-01T03:00:00.000+11:00
    assertEquals(36000000, sydney.getOffset(-1665392400000L)); // 1917-03-25T01:00:00.000+10:00
    assertEquals(39600000, sydney.getOffset(-1672567140000L)); // 1917-01-01T01:01:00.000+11:00
    assertEquals(36000000, sydney.getOffset(-1672567140001L)); // 1917-01-01T01:00:59.999+10:00
  }

  /**
   * Test the local date-based offset getter.
   */
  public void testGetOffsetWithLocalDates() {
    TimeZone la = TimeZone.getTimeZone("America/Los_Angeles");

    // Recall Java calendar month is 0-based, and the dayOfWeek, parameter is actually ignored by
    // some implementations.

    // 2016-03-12T03:00:00.000-08:00
    assertEquals(
        -28800000, la.getOffset(GregorianCalendar.AD, 2016, 2, 12, Calendar.SATURDAY, 10800000));

    // 2016-03-13T01:59:59.999-08:00
    assertEquals(
        -28800000, la.getOffset(GregorianCalendar.AD, 2016, 2, 13, Calendar.SUNDAY, 7200000 - 1));

    // 2016-03-13T02:00:00.000-08:00 (but 2 AM that day local time in America/Los_Angeles does not
    // exist, even though TimeZone returns something that is after the DST transition)
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2016, 2, 13, Calendar.SUNDAY, 7200000));

    // 2016-03-13T02:30:00.000-08:00 (but 2:30 AM is a non-existent local time)
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2016, 2, 13, Calendar.SUNDAY, 9000000));

    // 2016-03-13T02:59:59.999-08:00 (but 2:59:59.999 AM is a non-existent local time)
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2016, 2, 13, Calendar.SUNDAY, 10800000 - 1));

    // 2016-03-13T03:00:00.000-07:00
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2016, 2, 13, Calendar.SUNDAY, 10800000));

    // 2016-03-13T03:00:00.001-07:00
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2016, 2, 13, Calendar.SUNDAY, 10800000 + 1));

    // 2015-10-31T01:00:00.000-07:00
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2015, 9, 31, Calendar.SATURDAY, 3600000));

    // 2015-11-01T00:00:00.000-07:00
    assertEquals(-25200000, la.getOffset(GregorianCalendar.AD, 2015, 10, 1, Calendar.SUNDAY, 0));

    // 2015-11-01T00:59:59.000-07:00
    assertEquals(
        -25200000, la.getOffset(GregorianCalendar.AD, 2015, 10, 1, Calendar.SUNDAY, 3600000 - 1));

    // 2015-11-01T01:00:00.000-08:00 (this is the evidence that TimeZone implementations always
    // prefer the local datetime in terms of the raw offset).
    assertEquals(
        -28800000, la.getOffset(GregorianCalendar.AD, 2015, 10, 1, Calendar.SUNDAY, 3600000));

    // 2015-11-01T01:59:59.999-08:00
    assertEquals(
        -28800000, la.getOffset(GregorianCalendar.AD, 2015, 10, 1, Calendar.SUNDAY, 7200000 - 1));

    // 2015-11-01T02:00:00.000-08:00
    assertEquals(
        -28800000, la.getOffset(GregorianCalendar.AD, 2015, 10, 1, Calendar.SUNDAY, 7200000));

    // Don't test any BC dates as it's meaningless.
  }

  /**
   * Test the locale-dependent display name of a time zone.
   *
   * JVM and iOS/OS X don't agree on the localized names, nor do they have the same localization
   * coverage, and so we are just testing a few shared traits here.
   */
  public void testGetDisplayName() {
    TimeZone tz = TimeZone.getTimeZone("Europe/Paris");

    // JVM says Central European (Summer) Time, OS X/iOS uses Central European Standard/Summer Time.
    // Short names also differ and are not tested here.
    assertTrue(
        tz.getDisplayName(true, TimeZone.LONG, Locale.ENGLISH).startsWith("Central European"));
    assertTrue(tz.getDisplayName(true, TimeZone.LONG, Locale.ENGLISH).contains("Summer"));
    assertTrue(
        tz.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH).startsWith("Central European"));
    assertFalse(tz.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH).startsWith("Summer"));

    // French time zone names change across environment and OS versions, and so we only test common
    // substrings here.
    final String ete = "été"; // French for "summer"
    final String avancee = "avancée"; // French for "forward"
    String frStdName = tz.getDisplayName(false, TimeZone.LONG, Locale.FRANCE);
    String frDstName = tz.getDisplayName(true, TimeZone.LONG, Locale.FRANCE);
    assertTrue(frStdName.contains("Europe central"));
    assertFalse(frStdName.contains(ete) || frStdName.contains(avancee));
    assertTrue(frDstName.contains("Europe central"));
    assertTrue(frDstName.contains(ete) || frDstName.contains(avancee));

    // Similarly for German, though they finally agree on the DST name.
    assertTrue(
        tz.getDisplayName(true, TimeZone.LONG, Locale.GERMAN)
            .equals("Mitteleuropäische Sommerzeit"));
    assertTrue(
        tz.getDisplayName(false, TimeZone.LONG, Locale.GERMAN).startsWith("Mitteleuropäische"));
    assertFalse(tz.getDisplayName(false, TimeZone.LONG, Locale.GERMAN).contains("Sommer"));
  }

  public void testIsTimeZoneUsingDaylightSavingTime() {
    assertTrue(TimeZone.getTimeZone("America/Los_Angeles").useDaylightTime());
    assertFalse(TimeZone.getTimeZone("America/Phonenix").useDaylightTime()); // No DST in Arizona.
    assertTrue(TimeZone.getTimeZone("America/Denver").useDaylightTime());
    assertTrue(TimeZone.getTimeZone("Europe/Paris").useDaylightTime());
    assertTrue(TimeZone.getTimeZone("Australia/Sydney").useDaylightTime());
    assertFalse(TimeZone.getTimeZone("Asia/Tokyo").useDaylightTime());
    assertFalse(TimeZone.getTimeZone("Asia/Kolkata").useDaylightTime());
  }

  public void testCommonRawOffsets() {
    assertEquals(-8 * 3600000, TimeZone.getTimeZone("America/Los_Angeles").getRawOffset());
    assertEquals(-7 * 3600000, TimeZone.getTimeZone("America/Phoenix").getRawOffset());
    assertEquals(-7 * 3600000, TimeZone.getTimeZone("America/Denver").getRawOffset());
    assertEquals(-5 * 3600000, TimeZone.getTimeZone("America/New_York").getRawOffset());
    assertEquals(19800000, TimeZone.getTimeZone("Asia/Kolkata").getRawOffset()); // UTC+5.54
    assertEquals(9 * 3600000, TimeZone.getTimeZone("Asia/Tokyo").getRawOffset());
    assertEquals(10 * 3600000, TimeZone.getTimeZone("Australia/Sydney").getRawOffset());
  }

  public void testGetAvailableIds() {
    List<String> availableIds = Arrays.asList(TimeZone.getAvailableIDs(-7 * 3600000));
    assertTrue(availableIds.contains("America/Phoenix"));
    assertTrue(availableIds.contains("America/Denver"));
    assertFalse(availableIds.contains("America/Los_Angeles"));
  }

  public void testIds() {
    assertEquals("GMT", TimeZone.getTimeZone("GMT").getID());
    assertEquals("UTC", TimeZone.getTimeZone("UTC").getID());

    String vmName = System.getProperty("java.vendor");
    if (vmName != null && vmName.startsWith("J2ObjC")) {
      // NSTimeZone handles this custom time zone format and normalizes it.
      assertEquals("GMT+0530", TimeZone.getTimeZone("GMT+05:30").getID());
    } else {
      assertEquals("GMT+05:30", TimeZone.getTimeZone("GMT+05:30").getID());
    }
    assertEquals("America/New_York", TimeZone.getTimeZone("America/New_York").getID());
    assertEquals(TimeZone.getTimeZone("GMT").getID(), TimeZone.getTimeZone("").getID()); // WIP
    assertEquals(TimeZone.getTimeZone("GMT"), TimeZone.getTimeZone("america/new_york"));
  }

  /**
   * getTimeZone(String) always give you a new instance.
   */
  public void testEqualityAndSameness() {
    TimeZone a;
    TimeZone b;

    a = TimeZone.getTimeZone("GMT");
    b = TimeZone.getTimeZone("GMT");
    assertEquals(a, b);
    assertNotSame(a, b);

    a = TimeZone.getTimeZone("UTC");
    b = TimeZone.getTimeZone("UTC");
    assertEquals(a, b);
    assertNotSame(a, b);

    a = TimeZone.getTimeZone("UTC");
    b = TimeZone.getTimeZone("GMT");
    assertFalse(a.equals(b));
    assertNotSame(a, b);

    a = TimeZone.getTimeZone("America/New_York");
    b = TimeZone.getTimeZone("America/New_York");
    assertEquals(a, b);
    assertNotSame(a, b);
  }

  public void testHasSameRules() {
    TimeZone a;
    TimeZone b;

    a = TimeZone.getTimeZone("America/New_York");
    b = TimeZone.getTimeZone("US/Eastern");
    assertTrue(a.hasSameRules(b));
    assertFalse(a.hasSameRules(null));

    // Arizona does not observe DST, so even Phoenix and Denver have the same rawOffest, they have
    // different rules.
    a = TimeZone.getTimeZone("America/Phoenix");
    b = TimeZone.getTimeZone("America/Denver");
    assertEquals(a.getRawOffset(), b.getRawOffset());
    assertFalse(a.hasSameRules(b));
  }

  /**
   * Although Android SDK documentation says three-letter time zone IDs other than UTC and GMT are
   * not supported, actual implementations may still support them. The time zone "EST" is in
   * practice UTC-5 with the following properties.
   */
  public void testEST() {
    TimeZone tz = TimeZone.getTimeZone("EST");

    assertEquals("EST", tz.getID());
    assertFalse(tz.useDaylightTime());
    assertEquals(-5 * 3600000, tz.getRawOffset());
    assertEquals(0, tz.getDSTSavings());
  }
}
