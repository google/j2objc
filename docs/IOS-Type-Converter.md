---
title: iOS Type Converter
layout: docs
---

The JavaToIOSTypeConverter translates references to those types that are replaced by Foundation framework classes.  Array classes are also replaced with native replacements included with the JRE emulation library; these native classes have a "IOS" prefix.  This is the current list of mapped types:

Java Class or Interface | Foundation Class or Protocol
----------------------- | ----------------------------
java.lang.Object | NSObject
java.lang.Number | NSNumber
java.lang.String | NSString
java.lang.Throwable | NSException
java.lang.Cloneable | NSCopying, NSMutableCopying
java.lang.Class | IOSClass
 | 
Object[] | IOSObjectArray
boolean[] | IOSBooleanArray
byte[] | IOSByteArray
char[] | IOSCharArray
double[] | IOSDoubleArray
float[] | IOSFloatArray
int[] | IOSIntArray
long[] | IOSLongArray
short[] | IOSShortArray
