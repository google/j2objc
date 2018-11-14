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
package test.java.time;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** Test suite for java.time package. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  TestClock_Fixed.class,
  TestClock_Offset.class,
  TestClock_System.class,
  TestClock_Tick.class,
  TestDuration.class,
  TestInstant.class,
  TestLocalDate.class,
  TestLocalDateTime.class,
  TestLocalTime.class,
  TestMonthDay.class,
  TestOffsetDateTime.class,
  TestOffsetDateTime_instants.class,
  TestOffsetTime.class,
  TestPeriod.class,
  TestYear.class,
  TestYearMonth.class,
  TestZoneId.class,
  TestZoneOffset.class,
  TestZonedDateTime.class,
  test.java.time.chrono.TestChronoLocalDate.class,
  test.java.time.chrono.TestIsoChronoImpl.class,
  test.java.time.format.TestCharLiteralParser.class,
  test.java.time.format.TestCharLiteralPrinter.class,
  test.java.time.format.TestDateTimeFormatter.class,
  test.java.time.format.TestDateTimeFormatterBuilder.class,
  test.java.time.format.TestDateTimeParsing.class,
  test.java.time.format.TestDateTimeTextProvider.class,
  test.java.time.format.TestDecimalStyle.class,
  test.java.time.format.TestFractionPrinterParser.class,
  test.java.time.format.TestNumberParser.class,
  test.java.time.format.TestNumberPrinter.class,
  test.java.time.format.TestPadPrinterDecorator.class,
  test.java.time.format.TestReducedParser.class,
  test.java.time.format.TestReducedPrinter.class,
  test.java.time.format.TestSettingsParser.class,
  test.java.time.format.TestStringLiteralParser.class,
  test.java.time.format.TestStringLiteralPrinter.class,
  test.java.time.format.TestTextParser.class,
  test.java.time.format.TestTextPrinter.class,
  test.java.time.format.TestZoneOffsetParser.class,
  test.java.time.format.TestZoneOffsetPrinter.class,
  test.java.time.temporal.TestChronoField.class,
  test.java.time.temporal.TestChronoUnit.class,
  test.java.time.temporal.TestDateTimeBuilderCombinations.class,
  test.java.time.temporal.TestDateTimeValueRange.class,
  test.java.time.temporal.TestIsoWeekFields.class,
  test.java.time.temporal.TestJulianFields.class,
  test.java.time.zone.TestFixedZoneRules.class,
})
public class Tests {}
