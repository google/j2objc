/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "java/util/regex/PatternSyntaxException.h"
#include "unicode/uregex.h"

// ICU documentation: http://icu-project.org/apiref/icu4c/classRegexPattern.html

static NSString *regexDetailMessage(UErrorCode status) {
  // These human-readable error messages were culled from "utypes.h", and then slightly tuned
  // to make more sense in context.
  // If we don't have a special-case, we'll just return the textual name of
  // the enum value (such as U_REGEX_RULE_SYNTAX), which is better than nothing.
  switch (status) {
    case U_REGEX_INTERNAL_ERROR: return @"An internal error was detected";
    case U_REGEX_RULE_SYNTAX: return @"Syntax error in regexp pattern";
    case U_REGEX_INVALID_STATE: return @"Matcher in invalid state for requested operation";
    case U_REGEX_BAD_ESCAPE_SEQUENCE: return @"Unrecognized backslash escape sequence in pattern";
    case U_REGEX_PROPERTY_SYNTAX: return @"Incorrect Unicode property";
    case U_REGEX_UNIMPLEMENTED: return @"Use of unimplemented feature";
    case U_REGEX_MISMATCHED_PAREN: return @"Incorrectly nested parentheses in regexp pattern";
    case U_REGEX_NUMBER_TOO_BIG: return @"Decimal number is too large";
    case U_REGEX_BAD_INTERVAL: return @"Error in {min,max} interval";
    case U_REGEX_MAX_LT_MIN: return @"In {min,max}, max is less than min";
    case U_REGEX_INVALID_BACK_REF: return @"Back-reference to a non-existent capture group";
    case U_REGEX_INVALID_FLAG: return @"Invalid value for match mode flags";
    case U_REGEX_LOOK_BEHIND_LIMIT:
      return @"Look-behind pattern matches must have a bounded maximum length";
    case U_REGEX_SET_CONTAINS_STRING:
      return @"Regular expressions cannot have UnicodeSets containing strings";
    case U_REGEX_OCTAL_TOO_BIG: return @"Octal character constants must be <= 0377.";
    case U_REGEX_MISSING_CLOSE_BRACKET: return @"Missing closing bracket in character class";
    case U_REGEX_INVALID_RANGE: return @"In a character range [x-y], x is greater than y";
    case U_REGEX_STACK_OVERFLOW: return @"Regular expression backtrack stack overflow";
    case U_REGEX_TIME_OUT: return @"Maximum allowed match time exceeded";
    case U_REGEX_STOPPED_BY_CALLER: return @"Matching operation aborted by user callback function";
    default:
      return [NSString stringWithUTF8String:u_errorName(status)];
  }
}

static void throwPatternSyntaxException(UErrorCode status, NSString *pattern, UParseError error) {
  @throw [[[JavaUtilRegexPatternSyntaxException alloc] initWithNSString:regexDetailMessage(status)
                                                           withNSString:pattern
                                                                withInt:error.offset] autorelease];
}

void JavaUtilRegexPattern_closeImplWithLong_(jlong addr) {
  uregex_close((URegularExpression *)addr);
}

jlong JavaUtilRegexPattern_compileImplWithNSString_withInt_(NSString *regex, jint flags) {
  flags |= UREGEX_ERROR_ON_UNKNOWN_ESCAPES;

  UErrorCode status = U_ZERO_ERROR;
  UParseError error;
  error.offset = -1;

  jint patLen = (jint)[regex length];
  jchar buffer[patLen];
  [regex getCharacters:buffer range:NSMakeRange(0, patLen)];
  URegularExpression *result = uregex_open(buffer, patLen, flags, &error, &status);

  if (!U_SUCCESS(status)) {
    throwPatternSyntaxException(status, regex, error);
  }
  return (jlong)result;
}
