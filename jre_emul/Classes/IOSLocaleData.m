// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  IOSLocaleData.m
//  JreEmulation
//
//  Created by Tom Ball on 8/23/13.
//

#import "IOSLocaleData.h"
#import "java/lang/Integer.h"
#import "libcore/icu/LocaleData.h"

@implementation IOSLocaleData

// Initializes a LocalData instance for a specified locale id.
+ (void)initLocaleDataImplWithNSString:(NSString *)localeId
              withLibcoreIcuLocaleData:(LibcoreIcuLocaleData *)result {
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  [dateFormatter setLocale:locale];
  IOSClass *stringClass = NSString_class_();

  IOSObjectArray *amPm = [IOSObjectArray arrayWithLength:2 type:stringClass];
  [amPm replaceObjectAtIndex:0 withObject:[dateFormatter AMSymbol]];
  [amPm replaceObjectAtIndex:1 withObject:[dateFormatter PMSymbol]];
  LibcoreIcuLocaleData_set_amPm_(result, amPm);

  NSArray *symbols = [dateFormatter eraSymbols];
  IOSObjectArray *eras = [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_eras_(result, eras);

  // Month symbols
  symbols = [dateFormatter monthSymbols];
  IOSObjectArray *longMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_longMonthNames_(result, longMonthNames);

  symbols = [dateFormatter shortMonthSymbols];
  IOSObjectArray *shortMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_shortMonthNames_(result, shortMonthNames);

  symbols = [dateFormatter veryShortMonthSymbols];
  IOSObjectArray *tinyMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_tinyMonthNames_(result, tinyMonthNames);

  symbols = [dateFormatter standaloneMonthSymbols];
  IOSObjectArray *longStandAloneMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_longStandAloneMonthNames_(result, longStandAloneMonthNames);

  symbols = [dateFormatter shortStandaloneMonthSymbols];
  IOSObjectArray *shortStandAloneMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_shortStandAloneMonthNames_(result, shortStandAloneMonthNames);

  symbols = [dateFormatter veryShortStandaloneMonthSymbols];
  IOSObjectArray *tinyStandAloneMonthNames =
      [IOSObjectArray arrayWithNSArray:symbols type:stringClass];
  LibcoreIcuLocaleData_set_tinyStandAloneMonthNames_(result, tinyStandAloneMonthNames);

  // Weekday symbols. Java weekday indices start with 1, so a pad is inserted at the beginning.
  NSMutableArray *weekdays =
      [NSMutableArray arrayWithArray:[dateFormatter weekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *longWeekdayNames = [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_longWeekdayNames_(result, longWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter shortWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *shortWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_shortWeekdayNames_(result, shortWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter veryShortWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *tinyWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_tinyWeekdayNames_(result, tinyWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter standaloneWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *longStandAloneWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_longStandAloneWeekdayNames_(result, longStandAloneWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter shortStandaloneWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *shortStandAloneWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_shortStandAloneWeekdayNames_(result, shortStandAloneWeekdayNames);

  weekdays = [NSMutableArray arrayWithArray:[dateFormatter veryShortStandaloneWeekdaySymbols]];
  [weekdays insertObject:@"" atIndex:0];
  IOSObjectArray *tinyStandAloneWeekdayNames =
      [IOSObjectArray arrayWithNSArray:weekdays type:stringClass];
  LibcoreIcuLocaleData_set_tinyStandAloneWeekdayNames_(result, tinyStandAloneWeekdayNames);

  // Relative date names.
  [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
  [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
  [dateFormatter setDoesRelativeDateFormatting:YES];
  NSDate *today = [NSDate date];
  LibcoreIcuLocaleData_set_today_(result, [dateFormatter stringFromDate:today]);
  NSTimeInterval daysSeconds = 24 * 60 * 60;
  NSDate *yesterday = [NSDate dateWithTimeInterval:-daysSeconds sinceDate:today];
  LibcoreIcuLocaleData_set_yesterday_(result, [dateFormatter stringFromDate:yesterday]);
  NSDate *tomorrow = [NSDate dateWithTimeInterval:daysSeconds sinceDate:today];
  LibcoreIcuLocaleData_set_tomorrow_(result, [dateFormatter stringFromDate:tomorrow]);
  [dateFormatter setDoesRelativeDateFormatting:NO];

  // Time formats.
  [dateFormatter setDateStyle:NSDateFormatterNoStyle];
  [dateFormatter setTimeStyle:NSDateFormatterFullStyle];
  LibcoreIcuLocaleData_set_fullTimeFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setTimeStyle:NSDateFormatterLongStyle];
  LibcoreIcuLocaleData_set_longTimeFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
  LibcoreIcuLocaleData_set_mediumTimeFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
  LibcoreIcuLocaleData_set_shortTimeFormat_(result, [dateFormatter dateFormat]);

  // Date formats.
  [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
  [dateFormatter setDateStyle:NSDateFormatterFullStyle];
  LibcoreIcuLocaleData_set_fullDateFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setDateStyle:NSDateFormatterLongStyle];
  LibcoreIcuLocaleData_set_longDateFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
  LibcoreIcuLocaleData_set_mediumDateFormat_(result, [dateFormatter dateFormat]);
  [dateFormatter setDateStyle:NSDateFormatterShortStyle];
  LibcoreIcuLocaleData_set_shortDateFormat_(result, [dateFormatter dateFormat]);

  // Decimal format symbols.
  NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
  [numberFormatter setNumberStyle:NSNumberFormatterNoStyle];
  [numberFormatter setLocale:locale];
  result->zeroDigit_ = [[numberFormatter zeroSymbol] characterAtIndex:0];
  if (result->zeroDigit_ == 0) {
    result->zeroDigit_ = '0';
  }
  result->decimalSeparator_ = [[numberFormatter decimalSeparator] characterAtIndex:0];
  result->groupingSeparator_ = [[numberFormatter groupingSeparator] characterAtIndex:0];
  result->percent_ = [[numberFormatter percentSymbol] characterAtIndex:0];
  result->perMill_ = [[numberFormatter perMillSymbol] characterAtIndex:0];
  result->monetarySeparator_ = [[numberFormatter currencyGroupingSeparator] characterAtIndex:0];
  result->minusSign_ = [[numberFormatter minusSign] characterAtIndex:0];
  LibcoreIcuLocaleData_set_exponentSeparator_(result, [numberFormatter exponentSymbol]);
  LibcoreIcuLocaleData_set_infinity_(result, [numberFormatter positiveInfinitySymbol]);
  LibcoreIcuLocaleData_set_NaN_(result, [numberFormatter notANumberSymbol]);
  LibcoreIcuLocaleData_set_currencySymbol_(result, [numberFormatter currencySymbol]);
  LibcoreIcuLocaleData_set_internationalCurrencySymbol_(
      result, [numberFormatter internationalCurrencySymbol]);

  // Number formats.
  [numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];
  [numberFormatter setAllowsFloats:NO];
  NSString *pattern = [NSString stringWithFormat:@"%@;%@",
                       [numberFormatter positiveFormat], [numberFormatter negativeFormat]];
  LibcoreIcuLocaleData_set_integerPattern_(result, pattern);
  [numberFormatter setAllowsFloats:YES];
  pattern = [NSString stringWithFormat:@"%@;%@",
             [numberFormatter positiveFormat], [numberFormatter negativeFormat]];
  LibcoreIcuLocaleData_set_numberPattern_(result, pattern);
  [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
  pattern = [NSString stringWithFormat:@"%@;%@",
             [numberFormatter positiveFormat], [numberFormatter negativeFormat]];
  LibcoreIcuLocaleData_set_currencyPattern_(result, pattern);
  [numberFormatter setNumberStyle:NSNumberFormatterPercentStyle];
  pattern = [NSString stringWithFormat:@"%@;%@",
             [numberFormatter positiveFormat], [numberFormatter negativeFormat]];
  LibcoreIcuLocaleData_set_percentPattern_(result, pattern);

  // Calendar data.
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSLocale *currentLocale = [calendar locale];
  [calendar setLocale:locale];
  JavaLangInteger *firstWeekday = JavaLangInteger_valueOfWithInt_((int) [calendar firstWeekday]);
  LibcoreIcuLocaleData_set_firstDayOfWeek_(result, firstWeekday);
  JavaLangInteger *minimalDays =
      JavaLangInteger_valueOfWithInt_((int) [calendar minimumDaysInFirstWeek]);
  LibcoreIcuLocaleData_set_minimalDaysInFirstWeek_(result, minimalDays);
  [calendar setLocale:currentLocale];

  RELEASE_(dateFormatter);
  RELEASE_(numberFormatter);
}

@end
